package com.rick.fila;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rick.ia.CerebroIA;
import com.rick.web.PainelSocketHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class GerenciadorFila {

    private static final Logger log = LoggerFactory.getLogger(GerenciadorFila.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int LIMITE_FILA_COMUM = 3;
    private static final long COOLDOWN_USUARIO_MS = 15_000L;

    private final PriorityBlockingQueue<ComentarioEvento> fila = new PriorityBlockingQueue<>();
    private final ConcurrentHashMap<String, Long> ultimoEnvioPorUsuario = new ConcurrentHashMap<>();
    private final CerebroIA cerebro;
    private final PainelSocketHandler painel;
    private final AtomicLong contadorIds = new AtomicLong(0);
    private final AtomicBoolean pausado = new AtomicBoolean(false);

    private volatile Process processoPythonAtual;
    private Thread workerThread;

    @Value("${app.voz.python-exe}")
    private String pythonExe;

    @Value("${app.voz.script-path}")
    private String scriptPath;

    @Value("${app.voz.working-dir}")
    private String workingDir;

    @Value("${app.voz.applio-app-dir}")
    private String applioAppDir;

    @Value("${app.voz.modelo-pth}")
    private String modeloPth;

    @Value("${app.voz.dispositivo-audio}")
    private String dispositivoAudio;

    public GerenciadorFila(CerebroIA cerebro, PainelSocketHandler painel) {
        this.cerebro = cerebro;
        this.painel = painel;
    }

    @PostConstruct
    public void iniciar() {
        workerThread = new Thread(this::executarCicloVoz, "worker-voz-gpu");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    @PreDestroy
    public void encerrar() {
        log.info("Encerrando motor de voz e liberando memória da GPU...");
        if (workerThread != null) {
            workerThread.interrupt();
        }
        if (processoPythonAtual != null && processoPythonAtual.isAlive()) {
            processoPythonAtual.destroyForcibly();
        }
    }

    public void adicionarNaFila(String usuario, String texto) {
        if (pausado.get() || fila.size() >= LIMITE_FILA_COMUM) {
            return;
        }

        long agora = System.currentTimeMillis();
        String chaveUsuario = usuario.toLowerCase().trim();
        Long ultimoEnvio = ultimoEnvioPorUsuario.get(chaveUsuario);

        if (ultimoEnvio != null && (agora - ultimoEnvio) < COOLDOWN_USUARIO_MS) {
            return;
        }

        ultimoEnvioPorUsuario.put(chaveUsuario, agora);
        fila.offer(new ComentarioEvento(usuario, texto, 1, contadorIds.incrementAndGet()));
    }

    public void adicionarPresenteNaFila(String usuario, String nomePresente) {
        fila.offer(new ComentarioEvento(usuario, "TE ENVIOU UM PRESENTE: " + nomePresente, 10, contadorIds.incrementAndGet()));
    }

    public void definirPausa(boolean statusPausa) {
        this.pausado.set(statusPausa);
        log.info("Estado da fila alterado para: {}", statusPausa ? "PAUSADO" : "ATIVO");
    }

    public boolean isPausado() {
        return pausado.get();
    }

    public void limparFila() {
        fila.clear();
        ultimoEnvioPorUsuario.clear();
        log.info("Fila de mensagens e cooldowns limpos manualmente pelo moderador.");
    }

    public void falarDiretoComPrioridade(String textoDireto) {
        fila.offer(new ComentarioEvento("Moderador", "FALE EXATAMENTE ISTO AGORA: " + textoDireto, 20, contadorIds.incrementAndGet()));
    }

    public int tamanhoFila() {
        return fila.size();
    }

    public String obterProximoTextoParaTeste() {
        ComentarioEvento ev = fila.poll();
        return ev != null ? ev.texto() : null;
    }

    private void executarCicloVoz() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ProcessBuilder pb = new ProcessBuilder(pythonExe, scriptPath);
                pb.directory(new File(workingDir));
                pb.redirectErrorStream(true);

                Map<String, String> env = pb.environment();
                env.put("VOICE_WORKDIR", workingDir);
                env.put("APPLIO_APP_DIR", applioAppDir);
                env.put("RVC_MODEL_PTH", modeloPth);
                env.put("AUDIO_DEVICE", dispositivoAudio);

                processoPythonAtual = pb.start();

                try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(processoPythonAtual.getOutputStream(), StandardCharsets.UTF_8));
                     BufferedReader in = new BufferedReader(new InputStreamReader(processoPythonAtual.getInputStream(), StandardCharsets.UTF_8))) {

                    String linha;
                    while ((linha = in.readLine()) != null) {
                        if (linha.contains("WORKER_PRONTO")) {
                            log.info("=== MOTOR DE VOZ PRONTO NA GPU! ===");
                            break;
                        }
                    }

                    while (processoPythonAtual.isAlive() && !Thread.currentThread().isInterrupted()) {
                        ComentarioEvento ev = fila.take();
                        String fala = cerebro.processarMensagem(ev.usuario(), ev.texto())
                                .replace("\"", "").replace("'", "").replace("*", "").trim();

                        log.info("[Bob Esponja] ({}): {}", ev.usuario(), fala);
                        enviarEstadoTela("FALANDO", ev.usuario(), fala, ev.prioridade() >= 10);

                        out.write(fala + "\n");
                        out.flush();

                        while ((linha = in.readLine()) != null) {
                            if (linha.contains("FIM_FALA") || linha.contains("ERRO:")) {
                                break;
                            }
                        }

                        enviarEstadoTela("CALADO", null, null, false);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Falha no motor de voz. Reiniciando worker em 3 segundos...", e);
                enviarEstadoTela("CALADO", null, null, false);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void enviarEstadoTela(String estado, String usuario, String fala, boolean presente) {
        try {
            Map<String, Object> payload = estado.equals("FALANDO")
                    ? Map.of("estado", estado, "usuario", usuario, "fala", fala, "presente", presente)
                    : Map.of("estado", estado);

            painel.enviarEventoParaTela(objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            log.warn("Erro ao serializar JSON para tela: {}", e.getMessage());
        }
    }

    private record ComentarioEvento(String usuario, String texto, int prioridade, long id) implements Comparable<ComentarioEvento> {
        @Override
        public int compareTo(ComentarioEvento o) {
            int comparacaoPrioridade = Integer.compare(o.prioridade, this.prioridade);
            return comparacaoPrioridade != 0 ? comparacaoPrioridade : Long.compare(this.id, o.id);
        }
    }
}
