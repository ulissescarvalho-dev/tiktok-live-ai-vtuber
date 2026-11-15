package com.rick.ranking;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "espectadores")
public class Espectador {

    @Id
    private String usuario;
    private int pontos;
    private LocalDateTime ultimaInteracao;

    public Espectador() {
    }

    public Espectador(String usuario, int pontos) {
        this.usuario = usuario;
        this.pontos = pontos;
        this.ultimaInteracao = LocalDateTime.now();
    }

    public void adicionarPontos(int qtd) {
        this.pontos += qtd;
        this.ultimaInteracao = LocalDateTime.now();
    }

    public String getUsuario() { return usuario; }
    public int getPontos() { return pontos; }
    public LocalDateTime getUltimaInteracao() { return ultimaInteracao; }
}
