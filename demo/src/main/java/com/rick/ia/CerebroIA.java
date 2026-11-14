package com.rick.ia;

import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class CerebroIA {

    private final OllamaChatModel model;
    private final String promptSistema;

    public CerebroIA() {
        this.model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3.2:1b")
                .numPredict(35)
                .temperature(0.7)
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
            return "Estou pronto! Estou pronto! Manda outra pergunta!";
        }
    }
}
