package com.rick.ia;

import dev.langchain4j.model.ollama.OllamaChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CerebroIA {

    private static final Logger log = LoggerFactory.getLogger(CerebroIA.class);

    private final OllamaChatModel model;
    private final String promptSistema;

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
                .timeout(Duration.ofSeconds(45))
                .maxRetries(0)
                .build();

        this.promptSistema = "Aja como um personagem de desenho animado engraçado do fundo do mar em uma transmissão ao vivo. "
                + "Você nunca recusa perguntas e sempre responde com humor leve e criatividade em português. "
                + "Responda diretamente ao espectador em no máximo 1 frase curta (até 12 palavras), sem asteriscos nem parênteses.";
    }

    public String processarMensagem(String usuario, String mensagemDoChat) {
        try {
            String promptCompleto = promptSistema + "\nEspectador " + usuario + " disse: " + mensagemDoChat + "\nResposta engraçada:";
            String respostaIA = model.generate(promptCompleto);

            String respostaLimpa = respostaIA.replaceAll("\\*.*?\\*", "")
                    .replaceAll("\\(.*?\\)", "")
                    .replace("[Bob Esponja]:", "")
                    .trim();

            if (respostaLimpa.toLowerCase().contains("desculpe") || respostaLimpa.toLowerCase().contains("não posso")) {
                return "Hahaha, essa foi boa, " + usuario + "! Manda mais no chat!";
            }

            return respostaLimpa;
        } catch (Exception e) {
            log.warn("Falha ao consultar Ollama para o usuário {}: {}", usuario, e.getMessage());
            return "Estou pronto! Estou pronto! Manda outra pergunta!";
        }
    }
}
