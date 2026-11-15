package com.rick.fila;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class AnimadorVMC {

    private static final Logger log = LoggerFactory.getLogger(AnimadorVMC.class);

    @Value("${app.vmc.host}")
    private String vmcHost;

    @Value("${app.vmc.porta}")
    private int vmcPorta;

    private Thread threadAnimacao;

    @PostConstruct
    public void iniciarMovimentoContinuo() {
        threadAnimacao = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                InetAddress destino = InetAddress.getByName(vmcHost);
                double tempo = 0;

                while (!Thread.currentThread().isInterrupted()) {
                    tempo += 0.05;

                    float balancoZ = (float) (Math.sin(tempo) * 0.06);
                    float giroY = (float) (Math.cos(tempo * 0.7) * 0.08);
                    float acenoX = (float) (Math.sin(tempo * 1.3) * 0.04);

                    enviarOssoVMC(socket, destino, vmcPorta, "Spine", 0f, giroY * 0.5f, balancoZ * 0.5f, 1.0f);
                    enviarOssoVMC(socket, destino, vmcPorta, "Head", acenoX, giroY, balancoZ, 1.0f);

                    float movimentoBraco = (float) (Math.sin(tempo * 0.8) * 0.03);
                    enviarOssoVMC(socket, destino, vmcPorta, "LeftUpperArm", 0f, 0f, -0.55f + movimentoBraco, 0.83f);
                    enviarOssoVMC(socket, destino, vmcPorta, "RightUpperArm", 0f, 0f, 0.55f - movimentoBraco, 0.83f);

                    Thread.sleep(33);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.warn("[Animador VMC]: {}", e.getMessage());
            }
        }, "vmc-udp-animator");

        threadAnimacao.setDaemon(true);
        threadAnimacao.start();
    }

    @PreDestroy
    public void encerrar() {
        if (threadAnimacao != null) {
            threadAnimacao.interrupt();
        }
    }

    private void enviarOssoVMC(DatagramSocket socket, InetAddress ip, int porta, String osso, float rx, float ry, float rz, float rw) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        escreverStringOSC(out, "/VMC/Ext/Bone/Pos");
        escreverStringOSC(out, ",sfffffff");
        escreverStringOSC(out, osso);

        ByteBuffer buf = ByteBuffer.allocate(28);
        buf.putFloat(0f);
        buf.putFloat(0f);
        buf.putFloat(0f);
        buf.putFloat(rx);
        buf.putFloat(ry);
        buf.putFloat(rz);
        buf.putFloat(rw);
        out.write(buf.array());

        byte[] dados = out.toByteArray();
        socket.send(new DatagramPacket(dados, dados.length, ip, porta));
    }

    private void escreverStringOSC(ByteArrayOutputStream out, String s) throws Exception {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        out.write(bytes);
        int pad = 4 - (bytes.length % 4);
        for (int i = 0; i < pad; i++) {
            out.write(0);
        }
    }
}
