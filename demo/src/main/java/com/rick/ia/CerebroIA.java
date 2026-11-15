package com.rick.ia;

import dev.langchain4j.model.ollama.OllamaChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;

@Service
public class CerebroIA {

    private static final Logger log = LoggerFactory.getLogger(CerebroIA.class);
    private static final int MAX_HISTORICO = 4;

    private final OllamaChatModel model;
    private final String promptSistema;
    private final Deque<String> historicoRecente = new ArrayDeque<>();

    public CerebroIA(
            @Value("${app.ia.url}") String ollamaUrl,
            @Value("${app.ia.modelo}") String modeloNome,
            @Value("${app.ia.max-tokens}") int maxTokens,
            @Value("${app.ia.temperatura}") double temperatura) {

        this.model = OllamaChatModel.builder()
                .baseUrl(ollamaUrl)
                .modelName(modeloNome)
                .numPredict(maxTokens)
                .temperature(temperatura)
                .timeout(Duration.ofSeconds(15))
                .maxRetries(0)
                .build();

        this.promptSistema = "Você é o Bob Esponja Calça Quadrada numa live do TikTok. "
                + "Responda em português em no máximo 1 frase curta e engraçada (até 10 palavras). "
                + "NUNCA use asteriscos, parênteses ou descrições de ação.";
    }

    public synchronized String processarMensagem(String usuario, String mensagemDoChat) {
        try {
            StringBuilder contexto = new StringBuilder(promptSistema).append("\n");

            if (!historicoRecente.isEmpty()) {
                contexto.append("Contexto recente da conversa:\n");
                for (String interacao : historicoRecente) {
                    contexto.append("- ").append(interacao).append("\n");
                }
            }

            contexto.append("Agora, ").append(usuario).append(" disse: ").append(mensagemDoChat);

            String respostaIA = model.generate(contexto.toString());
            String respostaLimpa = respostaIA.replaceAll("\\*.*?\\*", "")
                    .replaceAll("\\(.*?\\)", "")
                    .replace("[Bob Esponja]:", "")
                    .trim();

            if (!"Sistema".equalsIgnoreCase(usuario)) {
                registrarNoHistorico(usuario, mensagemDoChat, respostaLimpa);
            }

            return respostaLimpa;
        } catch (Exception e) {
            log.warn("Falha ao consultar Ollama para o usuário {}: {}", usuario, e.getMessage());
            return "Estou pronto! Estou pronto! Manda outra pergunta!";
        }
    }

    private void registrarNoHistorico(String usuario, String mensagem, String resposta) {
        if (historicoRecente.size() >= MAX_HISTORICO) {
            historicoRecente.pollFirst();
        }
        historicoRecente.addLast(usuario + ": \"" + mensagem + "\" -> Você respondeu: \"" + resposta + "\"");
    }

    public synchronized int tamanhoHistorico() {
        return historicoRecente.size();
    }

    public synchronized void limparMemoria() {
        historicoRecente.clear();
        log.info("Memória recente da IA foi limpa.");
    }
}
