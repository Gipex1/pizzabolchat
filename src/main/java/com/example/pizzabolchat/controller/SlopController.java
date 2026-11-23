package com.example.pizzabolchat.controller;

import com.example.pizzabolchat.dto.SlopDtos.AnswerRequest;
import com.example.pizzabolchat.dto.SlopDtos.AskRequest;
import com.example.pizzabolchat.dto.SlopDtos.RateRequest;
import com.example.pizzabolchat.exception.AuthException;
import com.example.pizzabolchat.model.User;
import com.example.pizzabolchat.service.SlopService;
import com.example.pizzabolchat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/slops")
@RequiredArgsConstructor
public class SlopController {

    private final SlopService slopService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> ask(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody AskRequest request
    ) {
        try {
            return ResponseEntity.ok(slopService.ask(current(authHeader), request.question()));
        } catch (AuthException e) {
            return error(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (IllegalStateException e) {
            return error(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> waiting(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            current(authHeader);
            return ResponseEntity.ok(slopService.waiting());
        } catch (AuthException e) {
            return error(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> my(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            return ResponseEntity.ok(slopService.my(current(authHeader)));
        } catch (AuthException e) {
            return error(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @PostMapping("/{id}/claim")
    public ResponseEntity<?> claim(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            return ResponseEntity.ok(slopService.claim(current(authHeader), id));
        } catch (AuthException e) {
            return error(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (IllegalStateException e) {
            return error(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/answer")
    public ResponseEntity<?> answer(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody AnswerRequest request
    ) {
        try {
            slopService.answer(current(authHeader), id, request.text());
            return ResponseEntity.ok(Map.of("message", "Ответ отправлен"));
        } catch (AuthException e) {
            return error(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (IllegalStateException e) {
            return error(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/timeout")
    public ResponseEntity<?> timeout(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            slopService.fail(current(authHeader), id);
            return ResponseEntity.ok(Map.of("message", "Слоп освобождён"));
        } catch (AuthException e) {
            return error(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (IllegalStateException e) {
            return error(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/messages/{messageId}/rate")
    public ResponseEntity<?> rate(
            @PathVariable Long messageId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody RateRequest request
    ) {
        try {
            slopService.rate(current(authHeader), messageId, request.like());
            return ResponseEntity.ok(Map.of("message", "Оценка сохранена"));
        } catch (AuthException e) {
            return error(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (IllegalStateException e) {
            return error(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private User current(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthException("Токен не передан");
        }
        return userService.getUserByToken(authHeader.substring(7));
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}
