package com.example.pizzabolchat.anonchat;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.Map;

@Controller
public class AnonymousChatController {

    private static final int MAX_MESSAGE_LENGTH = 500;

    @MessageMapping("/anonchat/send")
    @SendTo("/topic/anonchat/messages")
    public ChatMessage send(
            @Payload ChatMessageRequest request,
            @Header(SimpMessageHeaderAccessor.SESSION_ATTRIBUTES) Map<String, Object> sessionAttributes
    ) {
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        String content = request.content().trim();
        if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Message is too long (maximum 500 characters)");
        }

        Object name = sessionAttributes.get(AnonymousChatHandshakeInterceptor.NAME_ATTRIBUTE);
        String sender = name instanceof String ? (String) name : "Guest";
        return new ChatMessage(sender, content, Instant.now().toString());
    }

    public record ChatMessageRequest(String content) {
    }

    public record ChatMessage(String sender, String content, String sentAt) {
    }
}
