package com.example.pizzabolchat.config;

import com.example.pizzabolchat.exception.AuthException;
import com.example.pizzabolchat.model.User;
import com.example.pizzabolchat.security.UserPrincipal;
import com.example.pizzabolchat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final UserService userService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("token");

            if (token == null) {
                String auth = accessor.getFirstNativeHeader("Authorization");
                if (auth != null && auth.startsWith("Bearer ")) {
                    token = auth.substring(7);
                }
            }

            if (token == null) {
                throw new MessageDeliveryException("Токен не передан");
            }

            try {
                User user = userService.getUserByToken(token);
                accessor.setUser(new UserPrincipal(user.getId()));
            } catch (AuthException e) {
                throw new MessageDeliveryException(e.getMessage());
            }
        }

        return message;
    }
}
