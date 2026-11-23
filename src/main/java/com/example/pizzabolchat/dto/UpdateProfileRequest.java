package com.example.pizzabolchat.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank String username,
        String avatarUrl
) {}
