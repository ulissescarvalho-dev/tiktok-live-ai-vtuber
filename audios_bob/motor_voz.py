import sys, os, subprocess
import soundfile as sf
import sounddevice as sd

os.chdir(r'C:\Users\carva\AppData\Local\Applio\data\runtime')
sys.path.insert(0, r'C:\Users\carva\AppData\Local\Applio\app')
sys.path.insert(0, r'C:\Users\carva\AppData\Local\Applio\app\backend\applio')

from backend.applio.rvc.infer.infer import VoiceConverter

PASTA_SAIDA = os.path.dirname(os.path.abspath(__file__))
BASE_MP3 = os.path.join(PASTA_SAIDA, 'base.mp3')
BOB_WAV = os.path.join(PASTA_SAIDA, 'bob.wav')
PTH = r'C:\Users\carva\AppData\Local\Applio\logs\BobEsponja\modelo.pth'

vc = VoiceConverter()
cabo_id = next((i for i, d in enumerate(sd.query_devices()) if 'CABLE Input' in d['name'] and d['max_output_channels'] > 0), None)

print('WORKER_PRONTO', flush=True)

for linha in sys.stdin:
    texto = linha.strip()
    if not texto:
        continue
    try:
        subprocess.run([
            sys.executable, '-m', 'edge_tts',
            '--voice', 'pt-BR-AntonioNeural',
            '--rate=+22%', '--pitch=+12Hz',
            '--text', texto, '--write-media', BASE_MP3
        ], check=True)

        vc.convert_audio(
            audio_input_path=BASE_MP3,
            audio_output_path=BOB_WAV,
            model_path=PTH,
            index_path='',
            pitch=6,
            f0_method='fcpe',
            index_rate=0.0,
            volume_envelope=1.0,
            protect=0.33,
            hop_length=128,
            f0_autotune=False,
            f0_autotune_strength=1.0,
            proposed_pitch=False,
            proposed_pitch_threshold=155.0,
            export_format='WAV',
            embedder_model='contentvec',
            sid=0
        )

        data, fs = sf.read(BOB_WAV)
        sd.play(data, fs, device=cabo_id)
        sd.wait()
        print('FIM_FALA', flush=True)
    except Exception as e:
        print('ERRO:' + str(e), flush=True)