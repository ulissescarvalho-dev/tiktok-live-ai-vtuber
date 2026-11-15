package com.rick;

import com.rick.ia.CerebroIA;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CerebroIATest {

    @Test
    @DisplayName("Deve retornar resposta de fallback segura quando o servidor Ollama estiver indisponível")
    void deveRetornarFallbackQuandoOllamaFalhar() {
        CerebroIA cerebroOffline = new CerebroIA("http://localhost:9999", "modelo-inexistente", 20, 0.7);

        String resposta = cerebroOffline.processarMensagem("Espectador", "Qual o segredo do hambúrguer?");

        assertNotNull(resposta);
        assertFalse(resposta.isBlank());
        assertEquals("Estou pronto! Estou pronto! Manda outra pergunta!", resposta);
    }
}
