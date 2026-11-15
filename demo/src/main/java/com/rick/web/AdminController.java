package com.rick.web;

import com.rick.fila.GerenciadorFila;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final GerenciadorFila gerenciadorFila;

    @Value("${app.admin.api-key}")
    private String apiKeyConfigurada;

    public AdminController(GerenciadorFila gerenciadorFila) {
        this.gerenciadorFila = gerenciadorFila;
    }

    private boolean naoAutorizado(String chaveRecebida) {
        return chaveRecebida == null || !chaveRecebida.equals(apiKeyConfigurada);
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        if (naoAutorizado(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Chave de API inválida"));
        }
        return ResponseEntity.ok(Map.of(
                "pausado", gerenciadorFila.isPausado(),
                "itensNaFila", gerenciadorFila.tamanhoFila()
        ));
    }

    @PostMapping("/pausar")
    public ResponseEntity<?> pausar(
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey,
            @RequestParam boolean ativo) {
        if (naoAutorizado(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Chave de API inválida"));
        }
        gerenciadorFila.definirPausa(ativo);
        return ResponseEntity.ok(Map.of("pausado", gerenciadorFila.isPausado()));
    }

    @PostMapping("/falar")
    public ResponseEntity<?> falar(
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey,
            @RequestBody Map<String, String> corpo) {
        if (naoAutorizado(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Chave de API inválida"));
        }
        String frase = corpo.get("frase");
        if (frase == null || frase.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "O campo 'frase' é obrigatório"));
        }
        gerenciadorFila.falarDiretoComPrioridade(frase);
        return ResponseEntity.ok(Map.of("mensagem", "Frase enviada com prioridade máxima", "frase", frase));
    }

    @DeleteMapping("/fila")
    public ResponseEntity<?> limparFila(@RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        if (naoAutorizado(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Chave de API inválida"));
        }
        gerenciadorFila.limparFila();
        return ResponseEntity.ok(Map.of("mensagem", "Fila esvaziada com sucesso"));
    }
}