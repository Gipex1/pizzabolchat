package com.example.pizzabolchat.service;

import com.example.pizzabolchat.dto.RegUser;
import com.example.pizzabolchat.model.Profile;
import com.example.pizzabolchat.model.User;
import com.example.pizzabolchat.repository.ProfileRepository;
import com.example.pizzabolchat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
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

        return UUID.randomUUID().toString();
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

    // Изменить email
    @Transactional
    public User updateEmail(Long id, String newEmail) {
        User user = getUserById(id);

        User existing = userRepository.findByEmail(newEmail);

        if (existing != null &&
                !existing.getId().equals(id)) {
            throw new RuntimeException("Этот email уже занят");
        }

        user.setEmail(newEmail);
        return userRepository.save(user);
    }

    // Изменить пароль
    @Transactional
    public void changePassword(
            Long id,
            String oldPassword,
            String newPassword
    ) {
        User user = getUserById(id);

        if (!passwordEncoder.matches(
                oldPassword,
                user.getPasswordHash()
        )) {
            throw new RuntimeException("Неверный текущий пароль");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException(
                    "Пароль должен содержать минимум 8 символов"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );
    }

    // Удалить аккаунт
    @Transactional
    public void deleteAccount(Long id, String password) {
        User user = getUserById(id);

        if (!passwordEncoder.matches(
                password,
                user.getPasswordHash()
        )) {
            throw new RuntimeException("Неверный пароль");
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