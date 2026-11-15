const canvas = document.getElementById('tela-jogo');
const ctx = canvas.getContext('2d');
canvas.width = 1080;
canvas.height = 1920;

const caixaFala = document.getElementById('caixa-fala');
const nomeUsuario = document.getElementById('nome-usuario');
const textoFala = document.getElementById('texto-fala');
const listaRanking = document.getElementById('lista-ranking');

const itens = [];
const placar = {};

function atualizarPlacar() {
    const top = Object.entries(placar)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 3);

    listaRanking.innerHTML = top
        .map(([user, pts], i) => `<div class="item-rank"><span>${i + 1}º ${user}</span><span>${pts} pts</span></div>`)
        .join('');
}

async function carregarRankingInicial() {
    try {
        const resposta = await fetch('/api/ranking/top3');
        if (resposta.ok) {
            const top3 = await resposta.json();
            top3.forEach(item => {
                placar[item.usuario] = item.pontos;
            });
            atualizarPlacar();
        }
    } catch (e) {
        console.warn('Não foi possível carregar o ranking inicial:', e);
    }
}

function criarDrop(categoria, usuario, pontos, totalAcumulado) {
    placar[usuario] = totalAcumulado !== undefined ? totalAcumulado : (placar[usuario] || 0) + pontos;
    atualizarPlacar();

    const cor = categoria === 'PRESENTE' ? '#FFD700' : (categoria === 'LIKE' ? '#FF4D6D' : '#38BDF8');
    const emoji = categoria === 'PRESENTE' ? '🍔' : (categoria === 'LIKE' ? '❤️' : '💬');

    itens.push({
        x: Math.random() * 880 + 100,
        y: -40,
        vy: Math.random() * 3 + 4,
        emoji: emoji,
        usuario: usuario,
        cor: cor
    });
}

function animar() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    for (let i = itens.length - 1; i >= 0; i--) {
        const it = itens[i];
        it.y += it.vy;

        ctx.font = '48px Segoe UI Emoji';
        ctx.textAlign = 'center';
        ctx.fillText(it.emoji, it.x, it.y);

        ctx.font = 'bold 20px Fredoka, Segoe UI';
        ctx.fillStyle = it.cor;
        ctx.fillText(it.usuario, it.x, it.y - 45);

        if (it.y > canvas.height + 60) {
            itens.splice(i, 1);
        }
    }
    requestAnimationFrame(animar);
}

carregarRankingInicial();
animar();

const ws = new WebSocket('ws://' + window.location.host + '/ws-mago');

ws.onmessage = (event) => {
    const dados = JSON.parse(event.data);

    if (dados.tipo === 'DROP') {
        criarDrop(dados.categoria, dados.usuario, dados.pontos, dados.totalAcumulado);
        return;
    }

    if (dados.estado === 'FALANDO') {
        nomeUsuario.textContent = (dados.presente ? '👑 VIP: ' : '💬 ') + dados.usuario;
        textoFala.textContent = dados.fala;
        caixaFala.style.display = 'block';
    } else if (dados.estado === 'CALADO') {
        caixaFala.style.display = 'none';
    }
};
