package com.rick.fila;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class AnimadorVMC {

    @PostConstruct
    public void iniciarMovimentoContinuo() {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                InetAddress localhost = InetAddress.getByName("127.0.0.1");
                int portaVSeeFace = 39539;
                double tempo = 0;

                while (true) {
                    tempo += 0.05;

                    float balancoZ = (float) (Math.sin(tempo) * 0.06);
                    float giroY = (float) (Math.cos(tempo * 0.7) * 0.08);
                    float acenoX = (float) (Math.sin(tempo * 1.3) * 0.04);

                    // Corpo e Cabeça
                    enviarOssoVMC(socket, localhost, portaVSeeFace, "Spine", 0f, giroY * 0.5f, balancoZ * 0.5f);
                    enviarOssoVMC(socket, localhost, portaVSeeFace, "Head", acenoX, giroY, balancoZ);

                    // Abaixa os braços para o lado do corpo (w = 0.85 ajusta a inclinação natural)
                    float movimentoBraco = (float) (Math.sin(tempo * 0.8) * 0.03);
                    enviarOssoVMCComW(socket, localhost, portaVSeeFace, "LeftUpperArm", 0f, 0f, -0.55f + movimentoBraco, 0.83f);
                    enviarOssoVMCComW(socket, localhost, portaVSeeFace, "RightUpperArm", 0f, 0f, 0.55f - movimentoBraco, 0.83f);

                    Thread.sleep(33);
                }
            } catch (Exception e) {
                System.out.println("[Animador VMC]: " + e.getMessage());
            }
        }).start();
    }

    private void enviarOssoVMC(DatagramSocket socket, InetAddress ip, int porta, String osso, float rx, float ry, float rz) throws Exception {
        enviarOssoVMCComW(socket, ip, porta, osso, rx, ry, rz, 1.0f);
    }

    private void enviarOssoVMCComW(DatagramSocket socket, InetAddress ip, int porta, String osso, float rx, float ry, float rz, float rw) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        escreverStringOSC(out, "/VMC/Ext/Bone/Pos");
        escreverStringOSC(out, ",sfffffff");
        escreverStringOSC(out, osso);

        ByteBuffer buf = ByteBuffer.allocate(28);
        buf.putFloat(0f); buf.putFloat(0f); buf.putFloat(0f);
        buf.putFloat(rx); buf.putFloat(ry); buf.putFloat(rz); buf.putFloat(rw);
        out.write(buf.array());

        byte[] dados = out.toByteArray();
        socket.send(new DatagramPacket(dados, dados.length, ip, porta));
    }

    private void escreverStringOSC(ByteArrayOutputStream out, String s) throws Exception {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        out.write(bytes);
        int pad = 4 - (bytes.length % 4);
        for (int i = 0; i < pad; i++) out.write(0);
    }
}
