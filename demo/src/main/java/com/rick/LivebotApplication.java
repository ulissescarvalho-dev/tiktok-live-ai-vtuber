package com.rick;

import com.rick.chat.LeitorChat;
import com.rick.fila.GerenciadorFila;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
            @Value("${app.tiktok.usuario}") String usuarioTikTok) {

        return args -> {
            log.info("=== INICIANDO BOT NA LIVE DE {} ===", usuarioTikTok);
            leitor.conectarNaLive(usuarioTikTok, gerenciador);
        };
    }
}
