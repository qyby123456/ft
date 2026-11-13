"""Regenerate the legacy stopper sounds. All other events reference Minecraft resources."""
import math
import random
import struct
import subprocess
import wave
from pathlib import Path

ROOT = Path(__file__).resolve().parent
PACK = ROOT.parents[2]
FFMPEG = PACK / 'ffmpeg/bin/ffmpeg.exe'
OUT = ROOT / 'src/main/resources/assets/farmingtales_drink_anim/sounds'
TMP = ROOT / 'build/audio'
TMP.mkdir(parents=True, exist_ok=True)
RATE = 44100

def render(name, seconds, bubbles, noise_level):
    rng = random.Random(431 + len(name))
    samples = []
    smooth = 0.0
    for index in range(int(seconds * RATE)):
        t = index / RATE
        smooth = .91 * smooth + .09 * rng.uniform(-1, 1)
        envelope = math.sin(math.pi * t / seconds) ** 2
        sample = noise_level * smooth * envelope * (1 + .35 * math.sin(t * 31))
        for start, freq, decay, gain in bubbles:
            dt = t - start
            if dt >= 0:
                phase = 2 * math.pi * freq * (dt + .9 * dt * dt)
                sample += gain * math.exp(-dt * decay) * (1 - math.exp(-dt * 380)) * (math.sin(phase) + .17 * math.sin(2.4 * phase))
        samples.append(sample)
    peak = max(.001, max(abs(x) for x in samples))
    pcm = b''.join(struct.pack('<h', int(max(-1, min(1, x / peak * .6)) * 32767)) for x in samples)
    path = TMP / (name + '.wav')
    with wave.open(str(path), 'wb') as stream:
        stream.setnchannels(1)
        stream.setsampwidth(2)
        stream.setframerate(RATE)
        stream.writeframes(pcm)
    subprocess.run([str(FFMPEG), '-hide_banner', '-loglevel', 'error', '-y', '-i', str(path), '-c:a', 'libvorbis', '-q:a', '5', str(OUT / (name + '.ogg'))], check=True)

def stopper_sound(name,seconds,pull=False):
    rng=random.Random(442)
    low=0.0
    samples=[]
    for i in range(int(seconds*RATE)):
        t=i/RATE
        noise=rng.uniform(-1,1)
        low=.82*low+.18*noise
        if pull:
            envelope=math.sin(math.pi*t/seconds)**2
            sample=(low*.55+(noise-low)*.08)*envelope*(.65+.35*math.sin(t*59)**2)
        else:
            attack=1-math.exp(-t*1600)
            sample=attack*(low*.5*math.exp(-t*65)+math.sin(2*math.pi*130*t)*.15*math.exp(-t*85))
        samples.append(sample)
    path=TMP/(name+'.wav')
    with wave.open(str(path),'wb') as wav:
        wav.setnchannels(1);wav.setsampwidth(2);wav.setframerate(RATE)
        wav.writeframes(b''.join(struct.pack('<h',int(max(-1,min(1,s*2.0))*32767)) for s in samples))
    subprocess.run([str(FFMPEG),'-hide_banner','-loglevel','error','-y','-i',str(path),'-c:a','libvorbis','-q:a','5',str(OUT/(name+'.ogg'))],check=True)

stopper_sound('cork',.16)
stopper_sound('cork_pull',.45,True)
print('Generated stopper Foley. Drinking, liquid and cloth events use Minecraft sound resources.')
