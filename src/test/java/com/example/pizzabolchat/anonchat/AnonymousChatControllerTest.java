package com.example.pizzabolchat.anonchat;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnonymousChatControllerTest {

    private final AnonymousChatController controller = new AnonymousChatController();

    @Test
    void broadcastsTrimmedMessageWithServerAssignedGuestName() {
        AnonymousChatController.ChatMessage message = controller.send(
                new AnonymousChatController.ChatMessageRequest("  Привет  "),
                Map.of(AnonymousChatHandshakeInterceptor.NAME_ATTRIBUTE, "Guest-ab12"));

        assertEquals("Guest-ab12", message.sender());
        assertEquals("Привет", message.content());
    }

    @Test
    void rejectsMessagesOverLimit() {
        String content = "x".repeat(501);

        assertThrows(IllegalArgumentException.class, () -> controller.send(
                new AnonymousChatController.ChatMessageRequest(content),
                Map.of()));
    }
}
