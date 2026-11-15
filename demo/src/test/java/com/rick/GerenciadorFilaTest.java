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
}
