package com.example.pizzabolchat.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "chats")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Chat extends BaseEntity {
    @Column(columnDefinition = "type_status_chat")
    private String status = "active";

    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "ended_at")
    private LocalDateTime endedAt;
}
