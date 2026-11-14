package com.rick.web;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class PainelSocketHandler extends TextWebSocketHandler {

    private final CopyOnWriteArrayList<WebSocketSession> sessoes = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessoes.add(session);
        System.out.println("[OBS Tela]: Painel visual conectado!");
    }

    public void enviarEventoParaTela(String jsonEvento) {
        for (WebSocketSession sessao : sessoes) {
            if (sessao.isOpen()) {
                try {
                    sessao.sendMessage(new TextMessage(jsonEvento));
                } catch (Exception e) {
                    System.out.println("[Erro WebSocket]: " + e.getMessage());
                }
            }
        }
    }
}
