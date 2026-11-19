package com.example.pizzabolchat.server;

import com.example.pizzabolchat.dto.RegUser;
import com.example.pizzabolchat.model.Profile;
import com.example.pizzabolchat.model.User;
import com.example.pizzabolchat.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional // Важно: если профиль не сохранится, юзер тоже откатится
    public User register(RegUser req) {
        if (repository.findByEmail(req.getEmail()) != null) {
            throw new RuntimeException("Email уже занят");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        // Создаем профиль
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setUsername(req.getEmail().split("@")[0]); // Имя по умолчанию
        profile.setAvatarUrl("#");
        profile.setRating(java.math.BigDecimal.ZERO);

        // Привязываем профиль к юзеру
        user.setProfile(profile);

        // Сохраняем (профиль сохранится сам благодаря CascadeType.ALL)
        return repository.save(user);
    }

    public String login(String email, String password){
        User user = repository.findByEmail(email);
        if(user == null){
            throw new RuntimeException("Error invalid email");
        }
        if(!passwordEncoder.matches(password, user.getPasswordHash())){
            throw new RuntimeException("Error invalid password");
        }

        return java.util.UUID.randomUUID().toString();
    }
}
