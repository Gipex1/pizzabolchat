package com.example.pizzabolchat.service;

import com.example.pizzabolchat.dto.SlopDtos.AnswerEvent;
import com.example.pizzabolchat.dto.SlopDtos.MySlopDto;
import com.example.pizzabolchat.dto.SlopDtos.SlopDto;
import com.example.pizzabolchat.model.Chat;
import com.example.pizzabolchat.model.ChatMember;
import com.example.pizzabolchat.model.ChatTurn;
import com.example.pizzabolchat.model.Message;
import com.example.pizzabolchat.model.Profile;
import com.example.pizzabolchat.model.Rating;
import com.example.pizzabolchat.model.User;
import com.example.pizzabolchat.repository.ChatMemberRepository;
import com.example.pizzabolchat.repository.ChatRepository;
import com.example.pizzabolchat.repository.ChatTurnRepository;
import com.example.pizzabolchat.repository.MessageRepository;
import com.example.pizzabolchat.repository.ProfileRepository;
import com.example.pizzabolchat.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SlopService {

    private static final int MAX_TOKENS = 7;
    private static final int MAX_RATING = 10;
    private static final int ANSWER_SECONDS = 60;

    private final ChatRepository chatRepository;
    private final ChatTurnRepository chatTurnRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final MessageRepository messageRepository;
    private final RatingRepository ratingRepository;
    private final ProfileRepository profileRepository;
    private final SimpMessagingTemplate messaging;

    @Transactional
    public SlopDto ask(User user, String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Вопрос не может быть пустым");
        }
        if (question.length() > 444) {
            throw new IllegalArgumentException("Вопрос слишком длинный (максимум 444 символа)");
        }

        Profile profile = profile(user);
        if (profile.getTokens() == null || profile.getTokens() < 1) {
            throw new IllegalStateException("Не хватает токенов");
        }
        profile.setTokens(profile.getTokens() - 1);
        profileRepository.save(profile);
        push(user, "/queue/tokens", Map.of("tokens", profile.getTokens()));

        Chat chat = chatRepository.save(new Chat());

        ChatTurn turn = new ChatTurn();
        turn.setChat(chat);
        turn.setAsker(user);
        turn.setStatus("waiting");
        turn.setDeadlineAt(OffsetDateTime.now().plusSeconds(ANSWER_SECONDS));
        turn = chatTurnRepository.save(turn);

        Message message = new Message();
        message.setChat(chat);
        message.setSender(user);
        message.setTurn(turn);
        message.setContent(question.trim());
        message.setMessageType("text");
        messageRepository.save(message);

        addMember(chat, user);

        SlopDto dto = new SlopDto(turn.getId(), question.trim(), null);
        messaging.convertAndSend("/topic/slops", dto);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<SlopDto> waiting() {
        return chatTurnRepository.findByStatus("waiting").stream()
                .filter(turn -> turn.getResponder() == null)
                .map(turn -> new SlopDto(turn.getId(), questionOf(turn), null))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MySlopDto> my(User user) {
        return chatTurnRepository.findByAskerIdOrderByIdDesc(user.getId()).stream()
                .map(turn -> new MySlopDto(
                        turn.getId(),
                        questionOf(turn),
                        answerOf(turn),
                        turn.getStatus(),
                        turn.getAnsweredAt(),
                        turn.getResponder() != null))
                .toList();
    }

    @Transactional
    public SlopDto claim(User user, Long turnId) {
        ChatTurn turn = turn(turnId);

        if (!"waiting".equals(turn.getStatus()) || turn.getResponder() != null) {
            throw new IllegalStateException("На этот слоп уже ответили");
        }
        if (turn.getAsker().getId().equals(user.getId())) {
            throw new IllegalStateException("Нельзя отвечать на свой вопрос");
        }

        turn.setResponder(user);
        turn.setDeadlineAt(OffsetDateTime.now().plusSeconds(ANSWER_SECONDS));
        chatTurnRepository.save(turn);
        addMember(turn.getChat(), user);

        return new SlopDto(turn.getId(), questionOf(turn), turn.getDeadlineAt());
    }

    @Transactional
    public void answer(User user, Long turnId, String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Ответ не может быть пустым");
        }

        ChatTurn turn = turn(turnId);
        checkClaim(turn, user);

        if (turn.getDeadlineAt() != null
                && turn.getDeadlineAt().plusSeconds(2).isBefore(OffsetDateTime.now())) {
            throw new IllegalStateException("Время вышло");
        }

        Message answer = new Message();
        answer.setChat(turn.getChat());
        answer.setSender(user);
        answer.setTurn(turn);
        answer.setContent(text.trim());
        answer.setMessageType("text");
        answer = messageRepository.save(answer);

        turn.setStatus("answered");
        turn.setAnsweredAt(OffsetDateTime.now());
        chatTurnRepository.save(turn);

        addTokens(user, 1);

        push(turn.getAsker(), "/queue/answers", new AnswerEvent(
                turn.getId(), answer.getId(), questionOf(turn), answer.getContent(), turn.getAnsweredAt()));
    }

    @Transactional
    public void fail(User user, Long turnId) {
        release(turnId, user.getId());
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void expireClaimed() {
        OffsetDateTime limit = OffsetDateTime.now().minusSeconds(3);
        List<ChatTurn> stale = chatTurnRepository
                .findByStatusAndResponderIsNotNullAndDeadlineAtBefore("waiting", limit);

        for (ChatTurn turn : stale) {
            release(turn.getId(), turn.getResponder().getId());
        }
    }

    @Transactional
    public void rate(User asker, Long messageId, boolean like) {
        Message answer = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Ответ не найден"));
        ChatTurn turn = answer.getTurn();

        if (turn == null || !"answered".equals(turn.getStatus())) {
            throw new IllegalStateException("Ответ ещё не принят");
        }
        if (turn.getAsker() == null || !turn.getAsker().getId().equals(asker.getId())) {
            throw new IllegalStateException("Оценивать может только автор вопроса");
        }
        if (turn.getResponder() == null) {
            throw new IllegalStateException("Ответчик не найден");
        }
        if (ratingRepository.existsByChatIdAndUserIdAndRatedUserId(
                turn.getChat().getId(), asker.getId(), turn.getResponder().getId())) {
            throw new IllegalStateException("Вы уже оценили этот ответ");
        }

        Rating rating = new Rating();
        rating.setChat(turn.getChat());
        rating.setUser(asker);
        rating.setRatedUser(turn.getResponder());
        rating.setScore(like ? 5 : 1);
        rating.setCreatedAt(OffsetDateTime.now());
        ratingRepository.save(rating);

        changeRating(turn.getResponder(), like ? 1 : -1);
    }

    private void release(Long turnId, Long responderId) {
        ChatTurn turn = turn(turnId);

        if (!"waiting".equals(turn.getStatus()) || turn.getResponder() == null
                || !turn.getResponder().getId().equals(responderId)) {
            return;
        }

        User responder = turn.getResponder();
        turn.setResponder(null);
        turn.setDeadlineAt(OffsetDateTime.now().plusSeconds(ANSWER_SECONDS));
        chatTurnRepository.save(turn);

        addTokens(responder, -2);
        changeRating(responder, -1);
        push(responder, "/queue/penalty",
                Map.of("error", "Ответ не дан: -2 токена, -1 рейтинг"));
    }

    private ChatTurn turn(Long turnId) {
        return chatTurnRepository.findByIdForUpdate(turnId)
                .orElseThrow(() -> new IllegalArgumentException("Слоп не найден"));
    }

    private void checkClaim(ChatTurn turn, User user) {
        if (turn.getResponder() == null || !turn.getResponder().getId().equals(user.getId())) {
            throw new IllegalStateException("Этот слоп закреплён за другим ответчиком");
        }
        if (!"waiting".equals(turn.getStatus())) {
            throw new IllegalStateException("Ответ уже отправлен");
        }
    }

    private void addMember(Chat chat, User user) {
        ChatMember member = new ChatMember();
        member.setChat(chat);
        member.setUser(user);
        member.setJoinedAt(OffsetDateTime.now());
        chatMemberRepository.save(member);
    }

    private String questionOf(ChatTurn turn) {
        return messageRepository
                .findFirstByTurnIdAndSenderIdOrderByIdAsc(turn.getId(), turn.getAsker().getId())
                .map(Message::getContent)
                .orElse("");
    }

    private String answerOf(ChatTurn turn) {
        if (turn.getResponder() == null) {
            return null;
        }
        return messageRepository
                .findFirstByTurnIdAndSenderIdOrderByIdAsc(turn.getId(), turn.getResponder().getId())
                .map(Message::getContent)
                .orElse(null);
    }

    private Profile profile(User user) {
        Profile profile = profileRepository.findByUserId(user.getId());
        if (profile == null) {
            throw new IllegalStateException("Профиль не найден");
        }
        return profile;
    }

    private void addTokens(User user, int delta) {
        Profile profile = profile(user);
        int value = (profile.getTokens() == null ? 0 : profile.getTokens()) + delta;
        profile.setTokens(Math.max(0, Math.min(MAX_TOKENS, value)));
        profileRepository.save(profile);
        push(user, "/queue/tokens", Map.of("tokens", profile.getTokens()));
    }

    private void changeRating(User user, int delta) {
        Profile profile = profile(user);
        int current = profile.getRating() == null ? 0 : profile.getRating().intValue();
        profile.setRating(BigDecimal.valueOf(Math.max(0, Math.min(MAX_RATING, current + delta))));
        profileRepository.save(profile);
        push(user, "/queue/rating", Map.of("rating", profile.getRating()));
    }

    private void push(User user, String destination, Object payload) {
        messaging.convertAndSendToUser(String.valueOf(user.getId()), destination, payload);
    }
}
