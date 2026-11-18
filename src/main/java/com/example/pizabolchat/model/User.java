package com.example.pizabolchat.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
//ALTER TABLE users ADD CONSTRAINT unique_user_email UNIQUE (email);

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class User extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "is_banned")
    private boolean isBanned = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
