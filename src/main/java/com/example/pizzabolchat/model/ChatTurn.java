package com.example.pizzabolchat.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@Entity
@Table(name = "chat_turns")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ChatTurn extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "chat_id", nullable = false)
    @ToString.Exclude
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "asker_id", nullable = false)
    @ToString.Exclude
    private User asker;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "responder_id")
    @ToString.Exclude
    private User responder;

    @Column(name = "deadline_at")
    private OffsetDateTime deadlineAt = OffsetDateTime.now().plusSeconds(60);

    @Column(name = "answered_at")
    private OffsetDateTime answeredAt;

    @Column(name = "status", columnDefinition = "type_status_chat_turns")
    private String status = "waiting";

}
