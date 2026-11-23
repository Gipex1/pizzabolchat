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

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegUser request) {
        User user = userService.register(request);
        String token = userService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(user.getId(), user.getEmail(), token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        String token = userService.login(request.email(), request.password());
        User user = userService.getUserByEmail(request.email());
        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(), token));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream()
                .map(u -> Map.<String, Object>of("id", u.getId(), "email", u.getEmail()))
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(Map.of("id", user.getId(), "email", user.getEmail()));
    }

    // Получить профиль текущего пользователя по токену
    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Токен не передан"));
            }
            String token = authHeader.substring(7);
            User user = userService.getUserByToken(token);

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("created_at", user.getCreatedAt());

            if (user.getProfile() != null) {
                response.put("username", user.getProfile().getUsername());
                response.put("avatarUrl", user.getProfile().getAvatarUrl());
                response.put("avatar_url", user.getProfile().getAvatarUrl());
                response.put("rating", user.getProfile().getRating());
            } else {
                response.put("username", "Без имени");
                response.put("avatarUrl", "#");
                response.put("avatar_url", "#");
                response.put("rating", 0.0);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody UpdateProfileRequest request
    ) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Токен не передан"));
            }
            userService.updateProfile(authHeader.substring(7), request.username(), request.avatarUrl());
            return getMyProfile(authHeader);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/email")
    public ResponseEntity<Map<String, String>> updateEmail(@PathVariable Long id, @RequestBody UpdateEmailRequest request) {
        userService.updateEmail(id, request.email());
        return ResponseEntity.ok(Map.of("message", "Email изменён"));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Map<String, String>> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request.oldPassword(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Пароль изменён"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAccount(@PathVariable Long id, @RequestBody DeleteAccountRequest request) {
        userService.deleteAccount(id, request.password());
        return ResponseEntity.ok(Map.of("message", "Аккаунт удалён"));
    }
}