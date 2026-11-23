package com.example.pizzabolchat.service;

import com.example.pizzabolchat.dto.RegUser;
import com.example.pizzabolchat.exception.AuthException;
import com.example.pizzabolchat.model.AuthSession;
import com.example.pizzabolchat.model.Profile;
import com.example.pizzabolchat.model.User;
import com.example.pizzabolchat.repository.AuthSessionRepository;
import com.example.pizzabolchat.repository.ProfileRepository;
import com.example.pizzabolchat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;

    // Регистрация
    @Transactional
    public User register(RegUser req) {
        if (userRepository.findByEmail(req.getEmail()) != null) {
            throw new RuntimeException("Email уже занят");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPasswordHash(
                passwordEncoder.encode(req.getPassword())
        );

        user = userRepository.save(user);

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setUsername(req.getEmail().split("@")[0]);
        profile.setAvatarUrl("#");
        profile.setRating(BigDecimal.ZERO);

        profileRepository.save(profile);

        return user;
    }

    // Авторизация
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user == null ||
                !passwordEncoder.matches(
                        password,
                        user.getPasswordHash()
                )) {
            throw new RuntimeException("Неверный email или пароль");
        }

        String token = UUID.randomUUID().toString();

        AuthSession session = new AuthSession();
        session.setUser(user);
        session.setTokenHash(hashToken(token));
        OffsetDateTime now = OffsetDateTime.now();
        session.setCreatedAt(now);
        session.setExpiresAt(now.plusDays(30));
        authSessionRepository.save(session);

        return token;
    }

    // Найти пользователя по токену
    @Transactional(readOnly = true)
    public User getUserByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthException("Токен не передан");
        }

        AuthSession session = authSessionRepository.findByTokenHash(hashToken(token))
                .orElseThrow(() -> new AuthException("Токен недействителен"));

        if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new AuthException("Срок действия токена истёк");
        }

        return session.getUser();
    }

    // Обновить профиль текущего пользователя
    @Transactional
    public void updateProfile(String token, String username, String avatarUrl) {
        User user = getUserByToken(token);
        Profile profile = profileRepository.findByUserId(user.getId());

        if (profile == null) {
            profile = new Profile();
            profile.setUser(user);
            profile.setRating(BigDecimal.ZERO);
        }

        profile.setUsername(username);
        profile.setAvatarUrl(avatarUrl == null || avatarUrl.isBlank() ? "#" : avatarUrl);
        profileRepository.save(profile);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // Получить всех пользователей
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Получить пользователя по ID
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Пользователь не найден")
                );
    }

    // Изменить email текущего пользователя
    @Transactional
    public void updateEmailByToken(String token, String newEmail) {
        updateEmail(getUserByToken(token).getId(), newEmail);
    }

    // Изменить email
    @Transactional
    public User updateEmail(Long id, String newEmail) {
        User user = getUserById(id);

        if (newEmail == null || !newEmail.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Некорректный email");
        }
        newEmail = newEmail.trim();

        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            return user;
        }

        User existing = userRepository.findByEmail(newEmail);

        if (existing != null &&
                !existing.getId().equals(id)) {
            throw new IllegalStateException("Этот email уже занят");
        }

        user.setEmail(newEmail);
        return userRepository.save(user);
    }

    // Изменить пароль текущего пользователя
    @Transactional
    public void changePasswordByToken(String token, String oldPassword, String newPassword) {
        changePassword(getUserByToken(token).getId(), oldPassword, newPassword);
    }

    // Изменить пароль
    @Transactional
    public void changePassword(
            Long id,
            String oldPassword,
            String newPassword
    ) {
        User user = getUserById(id);

        if (oldPassword == null || !passwordEncoder.matches(
                oldPassword,
                user.getPasswordHash()
        )) {
            throw new IllegalArgumentException("Неверный текущий пароль");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException(
                    "Пароль должен содержать минимум 6 символов"
            );
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalStateException("Новый пароль совпадает с текущим");
        }

        user.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );
    }

    // Удалить аккаунт текущего пользователя
    @Transactional
    public void deleteAccountByToken(String token, String password) {
        deleteAccount(getUserByToken(token).getId(), password);
    }

    // Удалить аккаунт
    @Transactional
    public void deleteAccount(Long id, String password) {
        User user = getUserById(id);

        if (password == null || !passwordEncoder.matches(
                password,
                user.getPasswordHash()
        )) {
            throw new IllegalArgumentException("Неверный пароль");
        }

        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("Пользователь не найден");
        }

        return user;
    }
}