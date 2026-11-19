package com.example.pizzabolchat.server;

import com.example.pizzabolchat.dto.RegUser;
import com.example.pizzabolchat.model.User;
import com.example.pizzabolchat.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User register(RegUser req) {
        if (req.getEmail() == null || req.getEmail().trim().isEmpty())
            throw new RuntimeException("Email обязателен");
        if (req.getPassword() == null || req.getPassword().trim().isEmpty())
            throw new RuntimeException("Password обязателен");
        if (repository.findByEmail(req.getEmail()) != null)
            throw new RuntimeException("Email уже занят");
        User u = new User();
        u.setEmail(req.getEmail());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        return repository.save(u);
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
