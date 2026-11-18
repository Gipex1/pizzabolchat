package com.example.pizabolchat.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Report extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "chat_id", nullable = false)
    @ToString.Exclude
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "reporter_id", nullable = false)
    @ToString.Exclude
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "reported_user_id", nullable = false)
    @ToString.Exclude
    private User reportedUser;

    @Column(name = "reason", nullable = false, length = Integer.MAX_VALUE)
    private String reason;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "status", columnDefinition = "type_status_reports")
    private String status = "pending";

}
