package com.example.pizzabolchat.repository;

import com.example.pizzabolchat.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}
