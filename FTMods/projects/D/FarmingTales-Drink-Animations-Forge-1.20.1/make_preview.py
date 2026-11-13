"""Actual smoke-client frames with sound cues reconstructed from played resource/volume/pitch logs."""
from pathlib import Path
import subprocess
import json
import wave
import numpy as np
from PIL import Image, ImageDraw, ImageFont

root = Path(__file__).resolve().parent
frames = root / 'build/preview-v1.2-frames'
out = root / 'preview'
frames.mkdir(parents=True, exist_ok=True)
out.mkdir(exist_ok=True)
font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 22)
small = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 15)
for i, path in enumerate(sorted((root / 'run-smoke/screenshots').glob('preview-*.png'))):
    tick = 65 + i
    if tick > 455:
        break
    caption = '自然持握'
    if 80 <= tick < 140:
        caption = 'G · 单手检视 / 正常拿出、停顿、收回'
    elif 180 <= tick < 207:
        caption = 'V · 握住木塞 / 用力拔出'
    elif 207 <= tick < 221:
        caption = '木塞拔出 · 露出玻璃瓶口'
    elif 221 <= tick < 228:
        caption = 'V · 前臂带酒瓶一起抬起'
    elif 228 <= tick < 268:
        caption = 'V · 小口慢饮'
    elif 268 <= tick < 300:
        caption = 'V · 放下酒瓶'
    elif tick >= 414:
        caption = '停止奔跑 · 平滑回到持握姿势'
    elif tick >= 365:
        caption = '奔跑 · 起步与摆臂'
    elif 330 <= tick < 355:
        caption = '跳跃 / 落地 · 酒液晃动'
    elif tick >= 310:
        caption = '行走 · 酒液晃动'
    image = Image.open(path).convert('RGB').resize((768, 512), Image.Resampling.LANCZOS)
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, 768, 65), fill=(21, 28, 34))
    draw.text((18, 7), caption, font=font, fill=(245, 217, 165))
    draw.text((18, 38), '橙子果酒 1.2 · 实际画面 + 按游戏触发记录同步混音', font=small, fill=(182, 193, 202))
    image.save(frames / f'{i:04d}.png')
ffmpeg = root.parents[2] / 'ffmpeg/bin/ffmpeg.exe'
rate=44100
mix=np.zeros(int(391/20*rate),dtype=np.float64)
assets=root.parents[2]/'.gradle-user/caches/forge_gradle/assets'
index=json.loads((assets/'indexes/5.json').read_text())['objects']
for line in (root/'run-smoke/sound-cues.jsonl').read_text().splitlines():
    cue=json.loads(line)
    namespace,name=cue['resource'].split(':',1)
    if namespace=='minecraft':
        hashed=index['minecraft/sounds/'+name+'.ogg']['hash']
        path=assets/'objects'/hashed[:2]/hashed
    else:
        path=root/'src/main/resources/assets'/namespace/'sounds'/(name+'.ogg')
    pitch=cue['pitch']
    pcm=subprocess.run([str(ffmpeg),'-v','error','-i',str(path),'-af',f'asetrate={int(rate*pitch)},aresample={rate}', '-f','f32le','-ac','1','-ar',str(rate),'-'],capture_output=True,check=True).stdout
    signal=np.frombuffer(pcm,dtype='<f4').astype(np.float64)*cue['volume']
    start=round((cue['tick']-65)/20*rate)
    count=min(len(signal),len(mix)-start)
    if start>=0 and count>0:mix[start:start+count]+=signal[:count]
audio=out/'orange-wine-v1.2-audio.wav'
with wave.open(str(audio),'wb') as wav:
    wav.setnchannels(1);wav.setsampwidth(2);wav.setframerate(rate)
    wav.writeframes((np.clip(mix,-1,1)*32767).astype('<i2').tobytes())
base = [str(ffmpeg), '-hide_banner', '-loglevel', 'error', '-y', '-framerate', '20', '-i', str(frames / '%04d.png')]
subprocess.run(base + ['-i',str(audio),'-c:v','libx264','-crf','20','-pix_fmt','yuv420p','-c:a','aac','-b:a','160k','-shortest','-movflags','+faststart',str(out/'orange-wine-v1.2.mp4')], check=True)
subprocess.run(base + ['-filter_complex', '[0:v]fps=15,split[a][b];[a]palettegen=max_colors=128[p];[b][p]paletteuse=dither=bayer:bayer_scale=4', '-loop', '0', str(out / 'orange-wine-v1.2.gif')], check=True)
print('Created 20 fps actual-client MP4 with synchronized sound cues and silent GIF.')
