package com.rick.fila;

import com.rick.ia.CerebroIA;
import com.rick.web.PainelSocketHandler;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.PriorityBlockingQueue;

@Service
public class GerenciadorFila {

    private final PriorityBlockingQueue<ComentarioEvento> fila = new PriorityBlockingQueue<>();
    private final CerebroIA cerebro;
    private final PainelSocketHandler painel;
    private long contadorIds = 0;

    public GerenciadorFila(CerebroIA cerebro, PainelSocketHandler painel) {
        this.cerebro = cerebro;
        this.painel = painel;
        iniciarConsumidor();
    }

    public synchronized void adicionarNaFila(String usuario, String texto) {
        if (fila.size() >= 3) return;
        fila.offer(new ComentarioEvento(usuario, texto, 1, ++contadorIds));
    }

    public synchronized void adicionarPresenteNaFila(String usuario, String nomePresente) {
        fila.offer(new ComentarioEvento(usuario, "TE ENVIOU UM PRESENTE: " + nomePresente, 10, ++contadorIds));
    }

    private void iniciarConsumidor() {
        new Thread(() -> {
            try {
                String py = "C:\\Users\\carva\\AppData\\Local\\Applio\\data\\runtime\\.venv\\Scripts\\python.exe";
                String script = "C:\\Users\\carva\\bottiktok\\audios_bob\\motor_voz.py";

                ProcessBuilder pb = new ProcessBuilder(py, script);
                pb.directory(new File("C:\\Users\\carva\\AppData\\Local\\Applio\\data\\runtime"));
                pb.redirectErrorStream(true);
                Process proc = pb.start();

                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream(), StandardCharsets.UTF_8));
                BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));

                String l;
                while ((l = in.readLine()) != null) {
                    if (l.contains("WORKER_PRONTO")) {
                        System.out.println("=== MOTOR DE VOZ PRONTO NA GPU! ===");
                        break;
                    }
                }

                while (true) {
                    ComentarioEvento ev = fila.take();
                    String fala = cerebro.processarMensagem(ev.usuario, ev.texto)
                            .replace("\"", "").replace("'", "").replace("*", "").trim();
                    System.out.println("[Bob Esponja]: " + fala);

                    painel.enviarEventoParaTela(String.format(
                            "{\"estado\":\"FALANDO\", \"usuario\":\"%s\", \"fala\":\"%s\", \"presente\":%b}",
                            escapar(ev.usuario), escapar(fala), ev.prioridade == 10));

                    out.write(fala + "\n");
                    out.flush();

                    while ((l = in.readLine()) != null) {
                        if (l.contains("FIM_FALA") || l.contains("ERRO:")) break;
                    }
                    painel.enviarEventoParaTela("{\"estado\":\"CALADO\"}");
                }
            } catch (Exception e) {
                System.out.println("[Erro]: " + e.getMessage());
            }
        }).start();
    }

    private String escapar(String t) {
        return t.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    private static class ComentarioEvento implements Comparable<ComentarioEvento> {
        final String usuario, texto;
        final int prioridade;
        final long id;
        ComentarioEvento(String u, String t, int p, long id) { usuario = u; texto = t; prioridade = p; this.id = id; }
        public int compareTo(ComentarioEvento o) {
            int c = Integer.compare(o.prioridade, prioridade);
            return c != 0 ? c : Long.compare(id, o.id);
        }
    }
}
