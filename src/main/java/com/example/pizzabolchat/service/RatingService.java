package com.example.pizzabolchat.service;

import com.example.pizzabolchat.model.*;
import com.example.pizzabolchat.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;
    private final MessageRepository messageRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public void rateMessage(User voter, Long messageId, boolean like){
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Сообщение не найдено"));

        ChatTurn turn = message.getTurn();
        if (turn == null || turn.getResponder() == null){
            throw new IllegalStateException("Нельзя оценить это сообщение");
        }

        if (!turn.getAsker().getId().equals(voter.getId())){
            throw new IllegalStateException("Оценивать может тольок автор вопроса");
        }

        User ratedUser = turn.getResponder();
        Chat chat = turn.getChat();

        if (ratingRepository.existsByChatIdAndUserIdAndRatedUserId(
                chat.getId(), voter.getId(), ratedUser.getId())){
            throw new IllegalStateException("Вы уже оценили этого пользователя в этом чате");
        }

        Rating rating = new Rating();
        rating.setChat(chat);
        rating.setUser(voter);
        rating.setRatedUser(ratedUser);
        rating.setScore(like ? 10 : -10);
        rating.setCreatedAt(OffsetDateTime.now());
        ratingRepository.save(rating);

        Profile profile = profileRepository.findByUserId(ratedUser.getId());
        if (profile == null){
            throw new IllegalStateException("Профель не найден");
        }

        BigDecimal current = profile.getRating() == null ? BigDecimal.ZERO : profile.getRating();
        BigDecimal delta = BigDecimal.valueOf(like ? 10 : -10);
        BigDecimal updated = current.add(delta);

        profile.setRating(updated);
        profileRepository.save(profile);

        if (updated.compareTo(BigDecimal.valueOf(-100)) <= 0){
            ratedUser.setBanned(true);
            userRepository.save(ratedUser);
        }
    }
}
