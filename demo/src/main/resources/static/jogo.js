const canvas = document.getElementById('jogoCanvas');
const ctx = canvas.getContext('2d');

function ajustarTela() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
}
window.addEventListener('resize', ajustarTela);
ajustarTela();

const itens = [];
const placar = {};

function atualizarPlacar() {
    const top3 = Object.entries(placar)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 3);

    const medalhas = ['🥇', '🥈', '🥉'];
    document.getElementById('lista-top').innerHTML = top3
        .map(([nome, pontos], idx) =>
            `<div class="item-rank"><span>${medalhas[idx]} ${nome}</span><span>${pontos} pts</span></div>`
        )
        .join('');
}

function spawnItem(usuario, categoria, pontos) {
    placar[usuario] = (placar[usuario] || 0) + pontos;
    atualizarPlacar();

    const quantidade = categoria === 'PRESENTE' ? 12 : 1;
    const icone = categoria === 'PRESENTE' ? '👑' : (categoria === 'LIKE' ? '🫧' : '🍔');
    const tamanho = categoria === 'PRESENTE' ? 65 : 45;

    for (let i = 0; i < quantidade; i++) {
        itens.push({
            x: Math.random() * (canvas.width - 120) + 60,
            y: -50 - (i * 45),
            vy: Math.random() * 4 + 5,
            vx: (Math.random() - 0.5) * 3,
            emoji: icone,
            tamanho: tamanho,
            nome: i === 0 ? usuario : '',
            tipo: 'DROP'
        });
    }
}

// Gera bolhas submarinas contínuas para a tela nunca ficar parada
setInterval(() => {
    itens.push({
        x: Math.random() * canvas.width,
        y: canvas.height + 20,
        vy: -(Math.random() * 2 + 1.5),
        vx: (Math.random() - 0.5) * 1.5,
        emoji: '🫧',
        tamanho: Math.random() * 18 + 14,
        nome: '',
        tipo: 'BOLHA'
    });
}, 800);

function loopAnimacao() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    for (let i = itens.length - 1; i >= 0; i--) {
        const it = itens[i];
        it.x += it.vx;
        it.y += it.vy;

        if (it.tipo === 'DROP') {
            it.vy += 0.25;
        }

        if (it.x < 35 || it.x > canvas.width - 35) {
            it.vx *= -1;
        }

        ctx.font = `${it.tamanho}px Arial`;
        ctx.textAlign = 'center';
        ctx.fillText(it.emoji, it.x, it.y);

        if (it.nome) {
            ctx.font = 'bold 20px Segoe UI';
            ctx.fillStyle = '#FFFFFF';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 4;
            ctx.strokeText(it.nome, it.x, it.y - it.tamanho * 0.7);
            ctx.fillText(it.nome, it.x, it.y - it.tamanho * 0.7);
        }

        if (it.y > canvas.height + 80 || it.y < -100) {
            itens.splice(i, 1);
        }
    }
    requestAnimationFrame(loopAnimacao);
}
loopAnimacao();

const protocolo = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
const ws = new WebSocket(`${protocolo}//${window.location.host}/ws-mago`);

ws.onmessage = (event) => {
    const dados = JSON.parse(event.data);

    if (dados.tipo === 'DROP') {
        spawnItem(dados.usuario, dados.categoria, dados.pontos);
    } else if (dados.estado === 'FALANDO') {
        document.getElementById('nome-usuario').innerText = `Respondendo a ${dados.usuario}:`;
        document.getElementById('texto-fala').innerText = dados.fala;
        document.getElementById('caixa-fala').style.display = 'block';
    } else if (dados.estado === 'CALADO') {
        document.getElementById('caixa-fala').style.display = 'none';
    }
};
