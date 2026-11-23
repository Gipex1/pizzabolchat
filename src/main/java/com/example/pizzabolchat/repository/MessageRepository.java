package com.example.pizzabolchat.repository;

import com.example.pizzabolchat.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findFirstByTurnIdAndSenderIdOrderByIdAsc(Long turnId, Long senderId);

    List<Message> findByTurnIdOrderByIdAsc(Long turnId);
}
