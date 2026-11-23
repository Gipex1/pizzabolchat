package com.example.pizzabolchat.repository;

import com.example.pizzabolchat.model.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {
    Optional<AuthSession> findByTokenHash(String tokenHash);
}
