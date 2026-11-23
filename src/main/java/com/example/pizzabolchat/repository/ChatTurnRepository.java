package com.example.pizzabolchat.repository;

import com.example.pizzabolchat.model.ChatTurn;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatTurnRepository extends JpaRepository<ChatTurn, Long> {

    List<ChatTurn> findByStatus(String status);

    List<ChatTurn> findByAskerIdOrderByIdDesc(Long askerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from ChatTurn t where t.id = :id")
    Optional<ChatTurn> findByIdForUpdate(@Param("id") Long id);

    List<ChatTurn> findByStatusAndResponderIsNotNullAndDeadlineAtBefore(
            String status, OffsetDateTime deadline);
}
