package com.example.pizzabolchat.controller;

import com.example.pizzabolchat.dto.*;
import com.example.pizzabolchat.model.User;
import com.example.pizzabolchat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Регистрация + автоматическая авторизация
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegUser request
    ) {
        User user = userService.register(request);

        String token = userService.login(
                request.getEmail(),
                request.getPassword()
        );

        AuthResponse response = new AuthResponse(
                user.getId(),
                user.getEmail(),
                token
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Авторизация
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request
    ) {
        String token = userService.login(
                request.email(),
                request.password()
        );

        User user = userService.getUserByEmail(request.email());

        return ResponseEntity.ok(
                new AuthResponse(
                        user.getId(),
                        user.getEmail(),
                        token
                )
        );
    }

    // Получить всех пользователей
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users =
                userService.getAllUsers().stream()
                        .map(user -> Map.<String, Object>of(
                                "id", user.getId(),
                                "email", user.getEmail()
                        ))
                        .toList();

        return ResponseEntity.ok(users);
    }

    // Получить пользователя по ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(
            @PathVariable Long id
    ) {
        User user = userService.getUserById(id);

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail()
        ));
    }

    // Изменить email
    @PatchMapping("/{id}/email")
    public ResponseEntity<Map<String, String>> updateEmail(
            @PathVariable Long id,
            @RequestBody UpdateEmailRequest request
    ) {
        userService.updateEmail(id, request.email());

        return ResponseEntity.ok(
                Map.of("message", "Email успешно изменён")
        );
    }

    // Изменить пароль
    @PatchMapping("/{id}/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(
                id,
                request.oldPassword(),
                request.newPassword()
        );

        return ResponseEntity.ok(
                Map.of("message", "Пароль успешно изменён")
        );
    }

    // Удалить аккаунт
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAccount(
            @PathVariable Long id,
            @RequestBody DeleteAccountRequest request
    ) {
        userService.deleteAccount(id, request.password());

        return ResponseEntity.ok(
                Map.of("message", "Аккаунт успешно удалён")
        );
    }
}