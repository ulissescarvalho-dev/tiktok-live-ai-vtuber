package com.rick;

import com.rick.chat.LeitorChat;
import com.rick.fila.GerenciadorFila;
import com.rick.ia.CerebroIA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
public class LivebotApplication {

    private static final Logger log = LoggerFactory.getLogger(LivebotApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(LivebotApplication.class, args);
    }

    @Bean
    public CommandLineRunner iniciarBot(
            LeitorChat leitor,
            GerenciadorFila gerenciador,
            CerebroIA cerebro,
            @Value("${app.tiktok.usuario}") String usuarioTikTok) {

        return args -> {
            log.info("=== INICIANDO BOT NA LIVE DE {} ===", usuarioTikTok);
            leitor.conectarNaLive(usuarioTikTok, gerenciador);

            CompletableFuture.runAsync(() -> {
                log.info("Pré-carregando modelo de IA na GPU em segundo plano...");
                cerebro.processarMensagem("Sistema", "Oi");
                log.info("=== MODELO DE IA PRONTO NA GPU! ===");
            });
        };
    }
}
