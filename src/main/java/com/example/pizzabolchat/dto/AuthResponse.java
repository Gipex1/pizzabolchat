package com.example.pizzabolchat.dto;

public record AuthResponse(
        Long userId,
        String email,
        String token
) {}