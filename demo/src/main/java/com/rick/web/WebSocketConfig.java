package com.rick.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final PainelSocketHandler painelHandler;

    public WebSocketConfig(PainelSocketHandler painelHandler) {
        this.painelHandler = painelHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(painelHandler, "/ws-mago").setAllowedOrigins("*");
    }
}
