package com.example.pizzabolchat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegUser {
    @NotBlank @Email(message = "Неверный email")
    private String email;
    @NotBlank(message = "passwordHash обязателен")
    private String password;
}
