package com.rick.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class PainelSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(PainelSocketHandler.class);
    private final CopyOnWriteArrayList<WebSocketSession> sessoes = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessoes.add(session);
        log.info("[OBS Tela]: Painel visual conectado! Sessões ativas: {}", sessoes.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessoes.remove(session);
        log.info("[OBS Tela]: Painel visual desconectado. Sessões ativas: {}", sessoes.size());
    }

    public void enviarEventoParaTela(String jsonEvento) {
        for (WebSocketSession sessao : sessoes) {
            if (sessao.isOpen()) {
                try {
                    synchronized (sessao) {
                        sessao.sendMessage(new TextMessage(jsonEvento));
                    }
                } catch (Exception e) {
                    log.warn("[Erro WebSocket]: Falha ao enviar mensagem para sessão {}: {}", sessao.getId(), e.getMessage());
                }
            }
        }
    }
}
