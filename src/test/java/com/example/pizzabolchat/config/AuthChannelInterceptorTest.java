package com.example.pizzabolchat.config;

import com.example.pizzabolchat.anonchat.AnonymousChatHandshakeInterceptor;
import com.example.pizzabolchat.model.User;
import com.example.pizzabolchat.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthChannelInterceptorTest {

    @Test
    void connectSetsPrincipalWithoutLoadingUserEmail() {
        UserService userService = mock(UserService.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(userService.getUserByToken("raw-token")).thenReturn(user);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("token", "raw-token");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = new AuthChannelInterceptor(userService)
                .preSend(message, mock(MessageChannel.class));

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertNotNull(resultAccessor.getUser());
        assertEquals("7", resultAccessor.getUser().getName());
        verify(user, never()).getEmail();
    }

    @Test
    void anonymousChatConnectSkipsAccountAuthentication() {
        UserService userService = mock(UserService.class);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionAttributes(Map.of(AnonymousChatHandshakeInterceptor.SESSION_ATTRIBUTE, true));
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = new AuthChannelInterceptor(userService)
                .preSend(message, mock(MessageChannel.class));

        assertSame(message, result);
        verify(userService, never()).getUserByToken(org.mockito.ArgumentMatchers.anyString());
    }
}
