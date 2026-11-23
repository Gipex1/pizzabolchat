package com.example.pizzabolchat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ChatController {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public Map<String, Object> broadcast(Map<String, Object> payload) {
        return Map.of(
                "sender", payload.getOrDefault("sender", ""),
                "content", payload.getOrDefault("content", ""));
    }
}
