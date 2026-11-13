"""Embed selected sounds from this installation; never edit the source archives."""
from pathlib import Path
import hashlib
import json
import subprocess
import tempfile
import zipfile

ROOT = Path(__file__).absolute().parent
ASSETS = Path('E:/zhuomian/农场物语1.4.3懒人包/.minecraft/assets')
MODS = Path('Z:/mods')
DEST = ROOT / 'src/main/resources/assets/farmingtales_drink_anim'
FFMPEG = Path('Z:/ffmpeg/bin/ffmpeg.exe')
FFPROBE = FFMPEG.with_name('ffprobe.exe')
sources = []


def sha(data):
    return hashlib.sha256(data).hexdigest()


def save(name, data, source, process=None):
    target = DEST / 'sounds' / (name + '.ogg')
    target.parent.mkdir(parents=True, exist_ok=True)
    original = data
    if process:
        with tempfile.TemporaryDirectory(prefix='wine-sound-') as tmp:
            raw = Path(tmp) / 'input.ogg'
            raw.write_bytes(data)
            subprocess.run([str(FFMPEG), '-y', '-v', 'error', '-i', str(raw),
                '-af', process, '-ac', '1', '-ar', '44100', '-c:a', 'libvorbis', '-q:a', '5', str(target)], check=True)
    else:
        target.write_bytes(data)
    probe = json.loads(subprocess.check_output([str(FFPROBE), '-v', 'error', '-show_entries',
        'format=duration:stream=channels,sample_rate', '-of', 'json', str(target)], text=True))
    sources.append(dict(resource='farmingtales_drink_anim:' + name,
        packaged_path='sounds/' + name + '.ogg', source=source,
        source_sha256=sha(original), sha256=sha(target.read_bytes()),
        processing=process or 'byte-for-byte copy', duration_seconds=float(probe['format']['duration']),
        channels=probe['streams'][0]['channels']))


index_path = next(f for f in (ASSETS / 'indexes').glob('*.json')
    if 'minecraft/sounds/random/drink.ogg' in json.loads(f.read_text())['objects'])
index = json.loads(index_path.read_text())['objects']
for target, entry in {
    'vanilla/drink': 'random/drink',
    'vanilla/cloth1': 'step/cloth1', 'vanilla/cloth3': 'step/cloth3',
    'vanilla/leather1': 'item/armor/equip_leather1', 'vanilla/leather2': 'item/armor/equip_leather2',
    'vanilla/liquid1': 'item/bottle/empty1', 'vanilla/liquid2': 'item/bottle/empty2',
}.items():
    key = 'minecraft/sounds/' + entry + '.ogg'
    digest = index[key]['hash']
    source_file = ASSETS / 'objects' / digest[:2] / digest
    data = source_file.read_bytes()
    assert hashlib.sha1(data).hexdigest() == digest
    save(target, data, dict(provider='Minecraft / Mojang', entry=key,
        file=str(source_file), asset_index=str(index_path), sha1=digest))

jar = next(MODS.glob('*Sounds-2.2.1+1.20.1+forge.jar'))
entry = 'assets/sounds/sounds/block/glass/step1.ogg'
with zipfile.ZipFile(jar) as archive:
    save('inspection/glass_touch', archive.read(entry), dict(provider='Sounds 2.2.1',
        file=str(jar), container_sha256=sha(jar.read_bytes()), entry=entry),
        'atrim=end=0.24,asetpts=PTS-STARTPTS,afade=t=in:d=0.003,afade=t=out:st=0.16:d=0.08')

legacy = ROOT / 'backups/1.3.6-before-elbow-audio-and-continuity/src/main/resources/assets/farmingtales_drink_anim/sounds'
for name in ('cork', 'cork_pull'):
    source_file = legacy / (name + '.ogg')
    save(name, source_file.read_bytes(), dict(provider='FarmingTales Drink Animations 1.3.6', file=str(source_file)))

# Only documented, nonvocal Minecraft physical effects: a small metal snap and pressure hiss.
components=[]
with tempfile.TemporaryDirectory(prefix='can-physical-') as tmp:
    inputs=[]
    for i,entry in enumerate(['block/iron_trapdoor/open1', 'random/fizz']):
        key='minecraft/sounds/'+entry+'.ogg';digest=index[key]['hash']
        source_file=ASSETS/'objects'/digest[:2]/digest;data=source_file.read_bytes()
        assert hashlib.sha1(data).hexdigest()==digest
        path=Path(tmp)/f'part{i}.ogg';path.write_bytes(data);inputs.extend(['-i',str(path)])
        components.append(dict(file=str(source_file),entry=key,sha256=sha(data),sha1=digest))
    process=('[0:a]atrim=end=0.30,asetpts=PTS-STARTPTS,asetrate=88200,aresample=44100,'
             'highpass=f=650,volume=0.80,afade=t=out:st=0.09:d=0.06[metal];'
             '[1:a]atrim=start=0.04:end=0.64,asetpts=PTS-STARTPTS,highpass=f=2200,'
             'lowpass=f=10000,volume=0.16,afade=t=in:d=0.015,afade=t=out:st=0.12:d=0.48,adelay=45[hiss];'
             '[metal][hiss]amix=inputs=2:normalize=0,alimiter=limit=0.85:level=0[out]')
    mixed=Path(tmp)/'physical.ogg'
    subprocess.run([str(FFMPEG),'-y','-v','error',*inputs,'-filter_complex',process,
        '-map','[out]','-ac','1','-ar','44100','-c:a','libvorbis','-q:a','5',str(mixed)],check=True)
    provenance=ROOT/'preview/can-open-physical-source.ogg';provenance.write_bytes(mixed.read_bytes())
    save('container/can_open',mixed.read_bytes(),dict(provider='Minecraft physical foley composite',
        file=str(provenance),components=components,recipe=process))


def event(subtitle, names, volume=1):
    return dict(subtitle='subtitles.farmingtales_drink_anim.' + subtitle,
        sounds=[dict(name='farmingtales_drink_anim:' + n, volume=volume) for n in names])


events = {
    'gulp': event('gulp', ['vanilla/drink'], .65),
    'slosh': event('slosh', ['vanilla/liquid1', 'vanilla/liquid2'], .3),
    'cloth': event('cloth', ['vanilla/cloth1', 'vanilla/cloth3'], .3),
    'cork': event('cork', ['cork']), 'cork_pull': event('cork_pull', ['cork_pull']),
    'inspect_lift': event('cloth', ['vanilla/leather1'], .15),
    'inspect_support': event('cloth', ['vanilla/cloth3'], .15),
    'inspect_turn': event('bottle_turn', ['vanilla/leather1', 'vanilla/leather2'], .15),
    'inspect_liquid': event('slosh', ['vanilla/liquid1', 'vanilla/liquid2'], .3),
    'inspect_settle': event('bottle_touch', ['inspection/glass_touch'], .7),
    'inspect_return': event('cloth', ['vanilla/leather2'], .15),
    'cap_twist': event('cap_twist', ['vanilla/leather2'], .35),
    'cap_release': event('cap_release', ['inspection/glass_touch'], .45),
    'can_open': event('can_open', ['container/can_open'], .95),
}
(DEST / 'sounds.json').write_text(json.dumps(events, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
(DEST / 'sound_sources.json').write_text(json.dumps(dict(version='1.5.0', sources=sources),
    ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps(sources, ensure_ascii=False, indent=2))
