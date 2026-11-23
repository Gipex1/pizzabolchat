package com.example.pizzabolchat.repository;

import com.example.pizzabolchat.model.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
}
