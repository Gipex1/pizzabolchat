package com.example.pizzabolchat.service;

import com.example.pizzabolchat.model.Profile;
import com.example.pizzabolchat.model.User;
import com.example.pizzabolchat.repository.ProfileRepository;
import com.example.pizzabolchat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    // Создание профиля при регистрации
    public void createProfile(User user, String username) {
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setUsername(username);
        profile.setAvatarUrl("#"); // Дефолтная аватарка
        profile.setRating(BigDecimal.ZERO);
        profileRepository.save(profile);
    }

    // Получение профиля по ID пользователя
    public Profile getProfileByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId);
        if (profile == null) {
            throw new RuntimeException("Профиль не найден");
        }
        return profile;
    }
}