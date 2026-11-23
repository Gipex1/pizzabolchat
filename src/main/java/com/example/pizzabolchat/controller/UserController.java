package com.example.pizzabolchat.controller;

import com.example.pizzabolchat.dto.*;
import com.example.pizzabolchat.exception.AuthException;
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
            User user = userService.getUserByToken(tokenFrom(authHeader));

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
        } catch (AuthException e) {
            return error(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (RuntimeException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody UpdateProfileRequest request
    ) {
        try {
            userService.updateProfile(tokenFrom(authHeader), request.username(), request.avatarUrl());
            return getMyProfile(authHeader);
        } catch (AuthException e) {
            return error(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (RuntimeException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/{id}/email")
    public ResponseEntity<?> updateEmail(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody UpdateEmailRequest request
    ) {
        try {
            userService.updateEmailByToken(tokenFrom(authHeader), request.email());
            return ResponseEntity.ok(Map.of("message", "Email изменён"));
        } catch (AuthException e) {
            return error(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (IllegalStateException e) {
            return error(HttpStatus.CONFLICT, e.getMessage());
        } catch (RuntimeException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ChangePasswordRequest request
    ) {
        try {
            userService.changePasswordByToken(tokenFrom(authHeader), request.oldPassword(), request.newPassword());
            return ResponseEntity.ok(Map.of("message", "Пароль изменён"));
        } catch (AuthException e) {
            return error(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (IllegalStateException e) {
            return error(HttpStatus.CONFLICT, e.getMessage());
        } catch (RuntimeException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody DeleteAccountRequest request
    ) {
        try {
            userService.deleteAccountByToken(tokenFrom(authHeader), request.password());
            return ResponseEntity.ok(Map.of("message", "Аккаунт удалён"));
        } catch (AuthException e) {
            return error(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (RuntimeException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private String tokenFrom(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthException("Токен не передан");
        }
        return authHeader.substring(7);
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}