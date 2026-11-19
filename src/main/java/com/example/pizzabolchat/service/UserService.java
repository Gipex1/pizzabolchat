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
}