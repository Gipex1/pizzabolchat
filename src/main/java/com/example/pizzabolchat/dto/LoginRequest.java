package com.example.pizzabolchat.dto;

public record LoginRequest(
        String email,
        String password
) {}