#  TikTok Live AI VTuber Bot (Bob Esponja Interativo)

Sistema completo de transmissão ao vivo interativa para o TikTok, integrando leitura nativa de eventos em tempo real, Inteligência Artificial generativa local, clonagem de voz acelerada por GPU, animação 3D automatizada e minijogo interativo em HTML5 Canvas.

##  Arquitetura e Tecnologias

* **Backend & Orquestração:** Java + Spring Boot (WebSockets, filas com prioridade VIP para presentes e controle de processos).
* **Leitura de Live:** Integração nativa com o chat do TikTok (`TikTokLiveJava`) capturando comentários, curtidas e presentes em tempo real.
* **Inteligência Artificial Local:** Ollama (`llama3.2:1b`) integrado via LangChain4j com prompt de personalidade e resposta ultrarrápida.
* **Motor de Voz (TTS + RVC na GPU):** Worker persistente em Python (`Edge-TTS` + `VoiceConverter` RVC rodando em CUDA) enviando áudio diretamente para cabo virtual (`VB-Audio Virtual Cable`).
* **Animação 3D sem Webcam:** Envio de pacotes UDP via protocolo **OSC/VMC** (porta `39539`) para o **VSeeFace**, gerando movimentos contínuos de respiração, cabeça e sincronia labial pelo áudio.
* **Overlay & Minijogo Interativo:** Front-end modular (`HTML5`, `CSS3`, `JavaScript` + `Canvas`) exibido no **OBS Studio** com ranking ao vivo (**Top Siri Cascudo**) e física de queda de itens por interação.

##  Estrutura do Projeto

```text
bottiktok/
├── audios_bob/
│   └── motor_voz.py               # Worker Python persistente de síntese e conversão de voz (GPU)
└── demo/
    └── src/main/
        ├── java/com/rick/
        │   ├── chat/              # Leitor de eventos da live (comentários, curtidas, presentes)
        │   ├── fila/              # Gerenciador de fila com prioridade VIP e animador VMC
        │   ├── ia/                # Integração com Ollama (LLM local)
        │   ├── web/               # Configuração de WebSocket e envio de eventos para tela
        │   └── LivebotApplication.java
        └── resources/static/
            ├── index.html         # Estrutura do overlay e placar
            ├── estilo.css         # Estilização translúcida (Glassmorphism)
            └── jogo.js            # Física em Canvas 2D e cliente WebSocket
```

##  Como Executar Localmente

1. **Pré-requisitos:**
   * Java 17+ e Maven
   * Ollama rodando localmente com o modelo `llama3.2:1b` (`ollama pull llama3.2:1b`)
   * Ambiente Python com Applio/RVC e `edge-tts`, `soundfile` e `sounddevice`
   * VB-Audio Virtual Cable, VSeeFace e OBS Studio (`1080x1920`)

2. **Configuração:**
   * Ajuste o `@usuario` alvo na classe `LivebotApplication.java`.
   * Verifique os caminhos do modelo `.pth` e do ambiente `.venv` no arquivo `audios_bob/motor_voz.py`.

3. **Inicialização:**
   * Execute a aplicação Spring Boot (`LivebotApplication`).
   * No **OBS Studio**, adicione uma fonte de **Navegador** apontando para `http://127.0.0.1:8080` (`1080x1920`) acima da captura transparente do **VSeeFace**.
