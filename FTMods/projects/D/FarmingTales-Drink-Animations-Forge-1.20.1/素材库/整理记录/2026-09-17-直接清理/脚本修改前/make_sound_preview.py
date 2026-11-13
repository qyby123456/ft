"""Attach the game's measured sound cues to the existing animation previews."""
from pathlib import Path
import json
import subprocess
import wave
import numpy as np

ROOT = Path(__file__).absolute().parent
FFMPEG = 'Z:/ffmpeg/bin/ffmpeg.exe'
RATE = 44100
DEST = ROOT / 'preview/检视与四次饮用-1.3.11'
DEST.mkdir(exist_ok=True)
RESOURCES = ROOT / 'src/main/resources/assets/farmingtales_drink_anim'
VIDEO = ROOT / 'blockbench/肘部联动与原创音效-v14/纤细版预览'
LOG = [json.loads(line) for line in (ROOT / 'run-smoke/sound-cues-SlimSteve29.jsonl').read_text().splitlines()]


def decode(resource):
    assert resource.startswith('farmingtales_drink_anim:')
    asset = RESOURCES / ('sounds/' + resource.split(':')[1] + '.ogg')
    return np.frombuffer(subprocess.check_output([FFMPEG, '-v', 'error', '-i', str(asset),
        '-f', 'f32le', '-ac', '1', '-ar', str(RATE), '-']), dtype='<f4')


def render(label, video, seconds, start_tick, end_tick, offset):
    sound = np.zeros(int(np.ceil(seconds * RATE)), dtype=np.float64)
    cues = []
    for row in LOG:
        if not start_tick <= row['tick'] <= end_tick:
            continue
        cue = dict(row, at_seconds=(row['tick'] - start_tick) / 20 + offset)
        cues.append(cue)
        source = decode(cue['resource'])
        # Minecraft pitch changes playback speed. Logged gain already includes
        # sounds.json, the cue volume and the mod's volume setting.
        pitched = np.interp(np.arange(0, len(source), cue['pitch']), np.arange(len(source)), source)
        sample = pitched * cue['volume']
        first = round(cue['at_seconds'] * RATE)
        count = min(len(sample), len(sound) - first)
        sound[first:first+count] += sample[:count]
    peak = float(np.max(np.abs(sound)))
    assert peak < .95, 'Preview must not clip or apply a hidden limiter'
    wav = DEST / (label + '.wav')
    with wave.open(str(wav), 'wb') as f:
        f.setnchannels(1)
        f.setsampwidth(2)
        f.setframerate(RATE)
        f.writeframes(np.round(sound * 32767).astype('<i2').tobytes())
    output = DEST / (label + '.mp4')
    subprocess.run([FFMPEG, '-y', '-v', 'error', '-i', str(VIDEO / video), '-i', str(wav),
        '-map', '0:v:0', '-map', '1:a:0', '-c:v', 'copy', '-c:a', 'aac', '-b:a', '192k',
        '-shortest', '-movflags', '+faststart', str(output)], check=True)
    return dict(video=str(output), audio=str(wav), peak=peak, cues=cues,
        note='Existing Blockbench animation preview with sound measured in the development client; not a gameplay recording.')


report = {
    'inspection': render('检视配音预览', '拿出-双手检视-收回-60fps.mp4', 8.167, 80, 184, 1.45),
    'drinking': render('四次饮用预览', '拔塞-饮用-收起-换瓶-60fps.mp4', 7.367, 205, 352, 0),
}
assert sum(c['event'].endswith(':gulp') for c in report['drinking']['cues']) == 4
(DEST / '播放时序.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({key:dict(video=r['video'], peak=r['peak'], cues=len(r['cues'])) for key,r in report.items()}, ensure_ascii=False))
