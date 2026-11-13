import argparse
import collections
import copy
import gzip
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import zipfile

ROOT = Path(__file__).resolve().parent
ROOM = ROOT / 'incoming/S7R2-10'
SHARED = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
MAPPED = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
SUPPORT = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S7-fungal-grove/out/S7-10-overview.png')
ACCEPTANCE = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S7-09-independent/independent-details.json')
sys.path.insert(0, 'D:/ft工程文件/血棘祭炉祭坛厅地牢_20261101/tools')
from structure_io import load_structure, nbt_payload


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def official_tags():
    with zipfile.ZipFile(MAPPED) as archive:
        raw = {name[len('data/minecraft/tags/blocks/'):-5]: json.loads(archive.read(name)) for name in archive.namelist() if name.startswith('data/minecraft/tags/blocks/') and name.endswith('.json')}
    resolved = {}

    def resolve(name, chain=()):
        name = name.removeprefix('minecraft:')
        if name in resolved:
            return resolved[name]
        assert name not in chain and name in raw, (name, chain)
        values = set()
        for value in raw[name]['values']:
            if isinstance(value, dict):
                value = value['id']
            if value.startswith('#'):
                values.update(resolve(value[1:], (*chain, name)))
            else:
                values.add(value if ':' in value else 'minecraft:' + value)
        resolved[name] = sorted(values)
        return resolved[name]

    return {'minecraft:' + name: resolve(name) for name in raw}


def native(run, metadata):
    assert not run.exists(), 'Use a new isolated --run-dir, never overwrite tests.'
    run.mkdir(parents=True)
    for source, target in [(ROOM / 'room.nbt', run / 'room.nbt'), (ROOM / 'room.json', run / 'room.json'), (ROOT / 'NativeRoomAudit.java', run / 'NativeRoomAudit.java')]:
        shutil.copyfile(source, target)
    write(run / 'vanilla-block-tags.json', official_tags())
    entries = [str(MAPPED), *[line for line in SUPPORT.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]]
    assert all(Path(entry).is_file() for entry in entries)
    classpath = os.pathsep.join(entries)
    (run / 'classpath.txt').write_text(classpath, encoding='utf-8')
    write(run / 'classpath-files.json', [{'path': entry, 'sha256': sha(Path(entry))} for entry in entries])
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    (run / 'classes').mkdir()
    commands = [[str(java / 'javac.exe'), '-encoding', 'UTF-8', '-cp', classpath, '-d', str(run / 'classes'), str(run / 'NativeRoomAudit.java')],
                [str(java / 'java.exe'), '-Xmx2G', '-Djava.awt.headless=true', '-cp', str(run / 'classes') + os.pathsep + classpath, 'NativeRoomAudit', str(run)]]
    write(run / 'commands.json', {'cwd': str(run), 'compile': commands[0], 'execute': commands[1], 'source_sha256': metadata['sha256'], 'audit_java_sha256': sha(run / 'NativeRoomAudit.java'), 'tags_sha256': sha(run / 'vanilla-block-tags.json')})
    for name, command in zip(['compile', 'run'], commands):
        completed = subprocess.run(command, cwd=run, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=300, creationflags=subprocess.CREATE_NO_WINDOW)
        (run / (name + '.log')).write_text(completed.stdout + completed.stderr, encoding='utf-8')
        print(completed.stdout[-1500:] + completed.stderr[-1500:], flush=True)
        assert completed.returncode == 0, str(run / (name + '.log'))
    assert sha(ROOM / 'room.nbt') == sha(run / 'room.nbt') == metadata['sha256']
    report = json.loads((run / 'native-result.json').read_text(encoding='utf-8'))
    assert report['status'] == 'PASS_OFFLINE_NATIVE_ONLY'
    return report


def duplicate_review(template):
    sys.path.insert(0, 'D:/ft工程文件/结构参考_20261103')
    from analyze_structures import NbtReader

    def read(path):
        reader = NbtReader(path.read_bytes())
        assert reader.number('>B') == 10
        reader.string()
        return reader.value(10)

    def signature(data):
        palette = data.get('palette') or data['palettes'][0]
        ignored = {'air', 'void_air', 'cave_air', 'water', 'lava', 'vine', 'glow_lichen', 'red_mushroom', 'brown_mushroom', 'amethyst_cluster'}
        points = [tuple(record['pos']) for record in data['blocks'] if palette[record['state']]['Name'].split(':')[-1] not in ignored]
        candidates = []
        for turns in range(4):
            rotated = []
            for block_x, block_y, block_z in points:
                for unused in range(turns):
                    block_x, block_z = -block_z, block_x
                rotated.append((block_x, block_y, block_z))
            minimum = [min(point[axis] for point in rotated) for axis in range(3)]
            payload = sorted(tuple(point[axis] - minimum[axis] for axis in range(3)) for point in rotated)
            candidates.append(hashlib.sha256(json.dumps(payload, separators=(',', ':')).encode()).hexdigest())
        return min(candidates)

    own = signature(template)
    roots = [Path('D:/ft工程文件/FT地牢_自然环境第7组_蘑菇真菌_20261104'), Path('D:/ft工程文件/FT地牢_自然环境第一批第2组_20261104'), Path('D:/ft工程文件/FT地牢_首批第3组_冰原霜穴_20261104'), ROOT.parent / '01-large-chamber', ROOT.parent / '02-medium-room', ROOT.parent / '03-small-room', ROOT.parent / '04-great-hall', ROOT.parent / '05-corridor', ROOT.parent / '06-corner', ROOT.parent / '07-entrance', ROOT.parent / '08-courtyard', ROOT.parent / '09-treasure']
    records = []
    for root in roots:
        for path in sorted(root.glob('incoming/*/room.nbt')):
            data = read(path)
            records.append({'path': str(path), 'sha256': sha(path), 'size': data['size'], 'exact_material_independent_geometry_match': own == signature(data)})
    assert not any(record['exact_material_independent_geometry_match'] for record in records)
    result = {'status': 'PASS_LOCAL_NO_EXACT_GEOMETRY_REUSE', 'source_sha256': sha(ROOM / 'room.nbt'), 'signature': own, 'compared': records,
              'semantic_self_check': {'previous': 'S7-01 two levels, U gallery, stairs, central giant mushroom dais, four elevation-aware ports', 'current': 'S7-10 independently authored six-zone complex and seven corridor runs with stepped hall, central courtyard and entry; no prior geometry imported', 'recipe_geometry_reused': False},
              'limits': 'Material-independent exact geometry under four rotations is not semantic all-group similarity acceptance; only existing frozen artifacts compared read-only.'}
    write(ROOT / 'qa/duplicate-review.json', result)


