package com.example.pizzabolchat.service;

import com.example.pizzabolchat.dto.RegUser;
import com.example.pizzabolchat.model.Profile;
import com.example.pizzabolchat.model.User;
import com.example.pizzabolchat.repository.ProfileRepository;
import com.example.pizzabolchat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor // <-- ВАЖНО: эта аннотация создает конструктор сама
public class UserService {

    private final UserRepository repository;
    private final ProfileRepository profileRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public User register(RegUser req) {
        if (repository.findByEmail(req.getEmail()) != null) {
            throw new RuntimeException("Email уже занят");
        }

        User u = new User();
        u.setEmail(req.getEmail());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u = repository.save(u);

        Profile profile = new Profile();
        profile.setUser(u);
        profile.setUsername(req.getEmail().split("@")[0]);
        profile.setAvatarUrl("#");
        profile.setRating(BigDecimal.ZERO);
        profileRepository.save(profile);

        return u;
    }

    public String login(String email, String password) {
        User user = repository.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Неверный email или пароль");
        }
        return UUID.randomUUID().toString();
    }

    // Получить всех пользователей (для админки)
    public java.util.List<User> getAllUsers() {
        return repository.findAll();
    }

    // Получить одного пользователя по ID
    public User getUserById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    // Обновить email пользователя
    @Transactional
    public User updateUser(Long id, String newEmail) {
        User user = getUserById(id);

        // Проверяем, не занят ли новый email
        User existing = repository.findByEmail(newEmail);
        if (existing != null && !existing.getId().equals(id)) {
            throw new RuntimeException("Этот email уже занят другим пользователем");
        }

        user.setEmail(newEmail);
        return repository.save(user);
    }

    // Удалить пользователя
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        repository.delete(user); // Профиль удалится сам (ON DELETE CASCADE)
    }
}