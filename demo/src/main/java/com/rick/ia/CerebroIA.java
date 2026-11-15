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
                .timeout(Duration.ofSeconds(15))
                .maxRetries(0)
                .build();

        this.promptSistema = "Você é o Bob Esponja Calça Quadrada numa live do TikTok. "
                + "Responda em português em no máximo 1 frase curta e engraçada (até 10 palavras). "
                + "NUNCA use asteriscos, parênteses ou descrições de ação.";
    }

    public String processarMensagem(String usuario, String mensagemDoChat) {
        try {
            String comandoAI = promptSistema + "\n" + usuario + " disse: " + mensagemDoChat;
            String respostaIA = model.generate(comandoAI);

            return respostaIA.replaceAll("\\*.*?\\*", "")
                    .replaceAll("\\(.*?\\)", "")
                    .replace("[Bob Esponja]:", "")
                    .trim();
        } catch (Exception e) {
            log.warn("Falha ao consultar Ollama para o usuário {}: {}", usuario, e.getMessage());
            return "Estou pronto! Estou pronto! Manda outra pergunta!";
        }
    }
}
