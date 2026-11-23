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
@Table(name = "matching_queue")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class MatchingQueue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "joined_at")
    private OffsetDateTime joinedAt;

    @Column(name = "status", columnDefinition = "varchar(50)")
    private String status;
}
