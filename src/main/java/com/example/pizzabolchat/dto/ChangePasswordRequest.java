package com.example.pizzabolchat.dto;

public record ChangePasswordRequest(
        String oldPassword,
        String newPassword
) {}