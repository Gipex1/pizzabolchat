package com.example.pizzabolchat.anonchat;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.UUID;

@Component
public class AnonymousChatHandshakeInterceptor implements HandshakeInterceptor {

    public static final String SESSION_ATTRIBUTE = "anonymousChat";
    public static final String NAME_ATTRIBUTE = "anonymousChatName";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        attributes.put(SESSION_ATTRIBUTE, true);
        attributes.put(NAME_ATTRIBUTE, "Guest-" + UUID.randomUUID().toString().substring(0, 4));
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
