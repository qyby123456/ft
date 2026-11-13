"""Archive user references and index all animation tracks; never load addon code."""
from pathlib import Path, PurePosixPath
import collections
import hashlib
import json
import shutil
import zipfile

ROOT = Path(__file__).resolve().parent
LIBRARY = ROOT / 'blockbench/03-参考素材'
SOURCES = [
    ('Trench-Tactics-0.05', Path('C:/Users/YOUR_USERNAME/Downloads/Trench_Tactics v0.05.zip')),
    ('LRTactical-0.4.3', Path('C:/Users/YOUR_USERNAME/Downloads/lrtactical-1.20.1-0.4.3.jar')),
]


def describe(track):
    if not isinstance(track, dict):
        return dict(constant=track)
    try:
        frames = sorted((float(t), v) for t, v in track.items())
    except ValueError:
        return dict(metadata=track)
    result = dict(keyframes=len(frames), times=[t for t, _ in frames],
                  interpolation=dict(collections.Counter(
                      v.get('lerp_mode', 'linear') if isinstance(v, dict) else 'linear' for _, v in frames)))
    result['steps'] = [t for t, v in frames if isinstance(v, dict) and 'pre' in v and 'post' in v and v['pre'] != v['post']]
    numeric = []
    for t, v in frames:
        if isinstance(v, dict):
            v = v.get('post', v.get('pre'))
        if isinstance(v, (int, float)):
            v = [v] * 3
        if isinstance(v, list) and len(v) == 3 and all(isinstance(x, (int, float)) for x in v):
            numeric.append((t, v))
    if numeric:
        result['range'] = [[min(v[i] for _, v in numeric), max(v[i] for _, v in numeric)] for i in range(3)]
        result['holds'] = [[a, b] for (a, x), (b, y) in zip(numeric, numeric[1:]) if x == y and b - a >= .15]
    return result


def archive(folder, source):
    out = LIBRARY / folder
    out.mkdir(parents=True, exist_ok=True)
    original = out / source.name
    digest = hashlib.sha256(source.read_bytes()).hexdigest()
    if original.exists():
        assert hashlib.sha256(original.read_bytes()).hexdigest() == digest
    else:
        shutil.copy2(source, original)
    files, models, scripts, metadata = [], [], [], []
    with zipfile.ZipFile(source) as z:
        for name in z.namelist():
            path = PurePosixPath(name)
            if path.is_absolute() or '..' in path.parts:
                raise ValueError(f'Unsafe archive path: {name}')
            animation = ('/animations/' in name or '/player_animator/' in name) and name.endswith('.json')
            model = ('/geo_models/' in name or '/models/' in name) and name.endswith('.json')
            script = name.endswith('.lua')
            meta = path.name.lower().startswith(('license', 'readme')) or name in ('pack.mcmeta', 'META-INF/mods.toml')
            if not any((animation, model, script, meta)):
                continue
            data = z.read(name)
            category = '原始动画' if animation else '模型层级参考' if model else '状态机参考' if script else '来源说明'
            dest = out / category / path
            # Extended paths retain exact archive names on Windows.
            native = Path('\\\\?\\' + str(dest.resolve()))
            native.parent.mkdir(parents=True, exist_ok=True)
            native.write_bytes(data)
            item = dict(entry=name, file=dest.relative_to(out).as_posix(), sha256=hashlib.sha256(data).hexdigest())
            if animation:
                value = json.loads(data)
                item['format'] = 'player_animator' if '/player_animator/' in name else 'bedrock'
                item['clips'] = []
                for clip, a in value.get('animations', {}).items():
                    item['clips'].append(dict(name=clip, seconds=a.get('animation_length'), loop=a.get('loop'),
                        bones={bone: {channel: describe(track) for channel, track in tracks.items()}
                               for bone, tracks in a.get('bones', {}).items()},
                        sound_effects=a.get('sound_effects'), particle_effects=a.get('particle_effects')))
                if item['format'] == 'player_animator':
                    item['player_animation'] = value
                files.append(item)
            elif model:
                value = json.loads(data)
                item['bones'] = [dict(name=b['name'], parent=b.get('parent'), pivot=b.get('pivot'), rotation=b.get('rotation'))
                    for g in value.get('minecraft:geometry', []) for b in g.get('bones', [])]
                models.append(item)
            elif script:
                scripts.append(item)
            else:
                metadata.append(item)
    report = dict(source=str(source), archive=source.name, sha256=digest, bytes=source.stat().st_size,
                  animation_files=len(files), clips=sum(len(f['clips']) for f in files), files=files,
                  models=models, scripts=scripts, metadata=metadata)
    (out / '素材与轨道索引.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(folder, 'animation files=', len(files), 'total clips=', report['clips'], 'models=', len(models), 'scripts=', len(scripts))
    for f in files:
        print(PurePosixPath(f['entry']).name, '; '.join(c['name'] + '=' + str(c['seconds']) for c in f['clips']))


if __name__ == '__main__':
    for folder, source in SOURCES:
        archive(folder, source)
