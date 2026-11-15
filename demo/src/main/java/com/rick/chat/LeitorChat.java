package com.rick.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rick.fila.GerenciadorFila;
import com.rick.web.PainelSocketHandler;
import io.github.jwdeveloper.tiktok.client.TikTokLive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class LeitorChat {

    private static final Logger log = LoggerFactory.getLogger(LeitorChat.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final PainelSocketHandler painel;

    public LeitorChat(PainelSocketHandler painel) {
        this.painel = painel;
    }

    public void conectarNaLive(String usuarioTikTok, GerenciadorFila gerenciador) {
        Thread threadChat = new Thread(() -> {
            try {
                log.info("Iniciando conexão nativa com o TikTok: {}", usuarioTikTok);

                TikTokLive.newClient(usuarioTikTok)
                        .configure(settings -> {
                            settings.getHttpSettings().setTimeout(Duration.ofSeconds(15));
                            settings.setPrintToConsole(false);
                        })
                        .onComment((liveClient, event) -> {
                            String usuario = event.getUser().getProfileName();
                            String texto = event.getText().trim();

                            enviarDropJogo("COMENTARIO", usuario, 1);

                            if (texto.length() < 3 || texto.matches("^[kK]+$")) {
                                return;
                            }
                            gerenciador.adicionarNaFila(usuario, texto);
                        })
                        .onLike((liveClient, event) -> {
                            String usuario = event.getUser().getProfileName();
                            enviarDropJogo("LIKE", usuario, 2);
                        })
                        .onGift((liveClient, event) -> {
                            String usuario = event.getUser().getProfileName();
                            String nomePresente = event.getGift().getName();

                            log.info("[ALERTA DE PRESENTE]: {} enviou um {}!", usuario, nomePresente);
                            enviarDropJogo("PRESENTE", usuario, 50);
                            gerenciador.adicionarPresenteNaFila(usuario, nomePresente);
                        })
                        .onConnected((liveClient, event) -> {
                            log.info("=== CONECTADO COM SUCESSO À LIVE DE {} ===", usuarioTikTok);
                        })
                        .onError((liveClient, event) -> {
                            log.warn("[Erro TikTok]: {}", event.getException().getMessage());
                        })
                        .buildAndConnect();

            } catch (Exception e) {
                log.warn("[Aviso]: Conexão finalizada para {}. Verifique se o perfil está AO VIVO.", usuarioTikTok);
            }
        }, "tiktok-chat-listener");

        threadChat.setDaemon(true);
        threadChat.start();
    }

    private void enviarDropJogo(String categoria, String usuario, int pontos) {
        try {
            Map<String, Object> payload = Map.of(
                    "tipo", "DROP",
                    "categoria", categoria,
                    "usuario", usuario,
                    "pontos", pontos
            );
            painel.enviarEventoParaTela(objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            log.warn("Erro ao serializar evento de drop para o jogo: {}", e.getMessage());
        }
    }
}
