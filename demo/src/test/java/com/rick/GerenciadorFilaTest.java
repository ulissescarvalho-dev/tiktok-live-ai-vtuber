package com.rick;

import com.rick.fila.GerenciadorFila;
import com.rick.ia.CerebroIA;
import com.rick.web.PainelSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class GerenciadorFilaTest {

    private GerenciadorFila gerenciador;

    @BeforeEach
    void configurar() {
        CerebroIA cerebroMock = Mockito.mock(CerebroIA.class);
        PainelSocketHandler painelMock = Mockito.mock(PainelSocketHandler.class);
        gerenciador = new GerenciadorFila(cerebroMock, painelMock);
    }

    @Test
    @DisplayName("Deve priorizar presentes (VIP) na frente de comentários comuns")
    void devePriorizarPresenteNaFrenteDeComentarios() {
        gerenciador.adicionarNaFila("Usuario1", "Oi Bob Esponja");
        gerenciador.adicionarNaFila("Usuario2", "Tudo bem?");
        gerenciador.adicionarPresenteNaFila("DoadorVIP", "Rosa");

        assertEquals(3, gerenciador.tamanhoFila());
        assertEquals("TE ENVIOU UM PRESENTE: Rosa", gerenciador.obterProximoTextoParaTeste());
        assertEquals("Oi Bob Esponja", gerenciador.obterProximoTextoParaTeste());
        assertEquals("Tudo bem?", gerenciador.obterProximoTextoParaTeste());
    }

    @Test
    @DisplayName("Deve respeitar o limite máximo de 3 comentários comuns na fila para evitar atrasos")
    void deveRespeitarLimiteMaximoDeComentariosComuns() {
        gerenciador.adicionarNaFila("User1", "Mensagem 1");
        gerenciador.adicionarNaFila("User2", "Mensagem 2");
        gerenciador.adicionarNaFila("User3", "Mensagem 3");
        gerenciador.adicionarNaFila("User4", "Mensagem 4 excedente");

        assertEquals(3, gerenciador.tamanhoFila());
    }

    @Test
    @DisplayName("Deve permitir entrada de presente VIP mesmo quando a fila comum está cheia")
    void devePermitirPresenteMesmoComFilaComumCheia() {
        gerenciador.adicionarNaFila("User1", "Msg 1");
        gerenciador.adicionarNaFila("User2", "Msg 2");
        gerenciador.adicionarNaFila("User3", "Msg 3");
        gerenciador.adicionarPresenteNaFila("SuperFa", "Coroa");

        assertEquals(4, gerenciador.tamanhoFila());
        assertEquals("TE ENVIOU UM PRESENTE: Coroa", gerenciador.obterProximoTextoParaTeste());
    }

    @Test
    @DisplayName("Deve ignorar comentários comuns quando pausado, mas permitir fala direta do moderador")
    void deveIgnorarComentariosQuandoPausadoEPermitirModerador() {
        gerenciador.definirPausa(true);
        assertTrue(gerenciador.isPausado());

        gerenciador.adicionarNaFila("Usuario1", "Mensagem ignorada na pausa");
        assertEquals(0, gerenciador.tamanhoFila());

        gerenciador.falarDiretoComPrioridade("Aviso importante na live!");
        assertEquals(1, gerenciador.tamanhoFila());
        assertEquals("FALE EXATAMENTE ISTO AGORA: Aviso importante na live!", gerenciador.obterProximoTextoParaTeste());
    }

    @Test
    @DisplayName("Deve bloquear flood de mensagens comuns do mesmo usuário dentro de 15 segundos, mas liberar presentes")
    void deveBloquearFloodDoMesmoUsuarioMasLiberarPresentes() {
        gerenciador.adicionarNaFila("Spammer", "Primeira mensagem");
        gerenciador.adicionarNaFila("Spammer", "Segunda mensagem seguida (bloqueada)");
        gerenciador.adicionarNaFila("Spammer", "Terceira mensagem seguida (bloqueada)");

        assertEquals(1, gerenciador.tamanhoFila());

        gerenciador.adicionarPresenteNaFila("Spammer", "Leão");
        assertEquals(2, gerenciador.tamanhoFila());
        assertEquals("TE ENVIOU UM PRESENTE: Leão", gerenciador.obterProximoTextoParaTeste());
        assertEquals("Primeira mensagem", gerenciador.obterProximoTextoParaTeste());
    }

}
