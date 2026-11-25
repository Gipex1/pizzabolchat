package com.example.pizzabolchat.config;

import com.example.pizzabolchat.anonchat.AnonymousChatHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    private final AuthChannelInterceptor authChannelInterceptor;
    private final AnonymousChatHandshakeInterceptor anonymousChatHandshakeInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.setErrorHandler(new StompSubProtocolErrorHandler() {
            @Override
            public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
                if (clientMessage == null) {
                    log.error("STOMP client message processing failed:\n{}", stackTraceWithoutMessages(ex));
                } else {
                    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(clientMessage);
                    log.error("STOMP {} processing failed for destination {}:\n{}",
                            accessor.getCommand(), accessor.getDestination(), stackTraceWithoutMessages(ex));
                }
                return super.handleClientMessageProcessingError(clientMessage, ex);
            }
        });
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "https://pizzabolchat.ru",
                        "https://www.pizzabolchat.ru",
                        "http://109.120.133.51",
                        "http://localhost:*",
                        "http://127.0.0.1:*")
                .withSockJS();
        registry.addEndpoint("/anonchat/ws")
                .addInterceptors(anonymousChatHandshakeInterceptor)
                .setAllowedOriginPatterns(
                        "https://pizzabolchat.ru",
                        "https://www.pizzabolchat.ru",
                        "http://109.120.133.51",
                        "http://localhost:*",
                        "http://127.0.0.1:*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
    }

    private static String stackTraceWithoutMessages(Throwable error) {
        StringBuilder trace = new StringBuilder();
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            trace.append(cause.getClass().getName()).append('\n');
            for (StackTraceElement element : cause.getStackTrace()) {
                trace.append("\tat ").append(element).append('\n');
            }
        }
        return trace.toString();
    }
}
