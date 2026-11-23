package com.example.pizzabolchat.dto;

import java.time.OffsetDateTime;

public final class SlopDtos {

    private SlopDtos() {
    }

    public record AskRequest(String question) {
    }

    public record AnswerRequest(String text) {
    }

    public record RateRequest(boolean like) {
    }

    public record SlopDto(Long id, String question, OffsetDateTime deadlineAt) {
    }

    public record MySlopDto(Long id, String question, String answer, String status,
                            OffsetDateTime answeredAt, boolean claimed) {
    }

    public record AnswerEvent(Long turnId, Long messageId, String question, String answer,
                              OffsetDateTime answeredAt) {
    }
}
