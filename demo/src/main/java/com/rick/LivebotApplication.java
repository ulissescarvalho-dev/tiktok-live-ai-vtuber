package com.rick;

import com.rick.chat.LeitorChat;
import com.rick.fila.GerenciadorFila;
import com.rick.ia.CerebroIA;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LivebotApplication {

    public static void main(String[] args) {
        SpringApplication.run(LivebotApplication.class, args);
    }

    @Bean
    public CommandLineRunner iniciarBot(LeitorChat leitor, GerenciadorFila gerenciador, CerebroIA cerebro) {
        return args -> {
            System.out.println("=== AQUECENDO A IA DO BOB ESPONJA NA MEMÓRIA... ===");
            cerebro.processarMensagem("Sistema", "Diga apenas: Estou pronto!");
            System.out.println("=== IA PRONTA! CONECTANDO NA LIVE... ===");

            leitor.conectarNaLive("@ulissescarv00xf", gerenciador);
        };
    }
}
