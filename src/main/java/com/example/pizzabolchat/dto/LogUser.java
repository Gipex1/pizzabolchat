package com.example.pizzabolchat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogUser {
    @NotBlank(message = "token обязателен")
    private String token;
    @NotBlank @Email(message = "Неверный email")
    private String email;
}
