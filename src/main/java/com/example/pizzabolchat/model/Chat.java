package com.example.pizabolchat.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

//create table chats(
//        id serial primary key,
//        status type_status_chat default 'active',
//        started_at timestamptz default now(),
//        ended_at timestamptz
//);

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
