package com.rick.chat;

import com.rick.fila.GerenciadorFila;
import com.rick.web.PainelSocketHandler;
import io.github.jwdeveloper.tiktok.client.TikTokLive;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LeitorChat {

    private final PainelSocketHandler painel;

    public LeitorChat(PainelSocketHandler painel) {
        this.painel = painel;
    }

    public void conectarNaLive(String usuarioTikTok, GerenciadorFila gerenciador) {
        new Thread(() -> {
            try {
                System.out.println("Iniciando conexão nativa com o TikTok: " + usuarioTikTok);

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

                            System.out.println("\n[ALERTA DE PRESENTE]: " + usuario + " enviou um " + nomePresente + "!");
                            enviarDropJogo("PRESENTE", usuario, 50);
                            gerenciador.adicionarPresenteNaFila(usuario, nomePresente);
                        })
                        .onConnected((liveClient, event) -> {
                            System.out.println("=== CONECTADO COM SUCESSO À LIVE DE " + usuarioTikTok + " ===");
                        })
                        .onError((liveClient, event) -> {
                            System.out.println("[Erro TikTok]: " + event.getException().getMessage());
                        })
                        .buildAndConnect();

            } catch (Exception e) {
                System.out.println("\n[Aviso]: Conexão finalizada. Verifique se o perfil está AO VIVO.");
            }
        }).start();
    }

    private void enviarDropJogo(String categoria, String usuario, int pontos) {
        String usuarioLimpo = usuario.replace("\\", "\\\\").replace("\"", "\\\"");
        String json = String.format(
                "{\"tipo\":\"DROP\", \"categoria\":\"%s\", \"usuario\":\"%s\", \"pontos\":%d}",
                categoria, usuarioLimpo, pontos
        );
        painel.enviarEventoParaTela(json);
    }
}
