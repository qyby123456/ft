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
ROOM = ROOT / 'incoming/S7R2-05'
SHARED = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
MAPPED = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
SUPPORT = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S7-fungal-grove/out/S7-05-corridor.png')
ACCEPTANCE = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S7-04-ACCEPTANCE.json')
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
                [str(java / 'java.exe'), '-Xmx1G', '-Djava.awt.headless=true', '-cp', str(run / 'classes') + os.pathsep + classpath, 'NativeRoomAudit', str(run)]]
    write(run / 'commands.json', {'cwd': str(run), 'compile': commands[0], 'execute': commands[1], 'source_sha256': metadata['sha256'], 'audit_java_sha256': sha(run / 'NativeRoomAudit.java'), 'tags_sha256': sha(run / 'vanilla-block-tags.json')})
    for name, command in zip(['compile', 'run'], commands):
        completed = subprocess.run(command, cwd=run, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=180, creationflags=subprocess.CREATE_NO_WINDOW)
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
    roots = [Path('D:/ft工程文件/FT地牢_自然环境第7组_蘑菇真菌_20261104'), Path('D:/ft工程文件/FT地牢_自然环境第一批第2组_20261104'), Path('D:/ft工程文件/FT地牢_首批第3组_冰原霜穴_20261104'), ROOT.parent / '01-large-chamber', ROOT.parent / '02-medium-room', ROOT.parent / '03-small-room', ROOT.parent / '04-great-hall']
    records = []
    for root in roots:
        for path in sorted(root.glob('incoming/*/room.nbt')):
            data = read(path)
            records.append({'path': str(path), 'sha256': sha(path), 'size': data['size'], 'exact_material_independent_geometry_match': own == signature(data)})
    assert not any(record['exact_material_independent_geometry_match'] for record in records)
    result = {'status': 'PASS_LOCAL_NO_EXACT_GEOMETRY_REUSE', 'source_sha256': sha(ROOM / 'room.nbt'), 'signature': own, 'compared': records,
              'semantic_self_check': {'previous': 'S7-01 two levels, U gallery, stairs, central giant mushroom dais, four elevation-aware ports', 'current': 'S7-05 narrow straight covered corridor, repeated wall-integrated mushroom stems/lights, red ceiling caps, four off-axis pools, two end arches only; no branches, flags, free columns or central furnishing', 'recipe_geometry_reused': False},
              'limits': 'Material-independent exact geometry under four rotations is not semantic all-group similarity acceptance; only existing frozen artifacts compared read-only.'}
    write(ROOT / 'qa/duplicate-review.json', result)


def render(template, metadata):
    specification = importlib.util.spec_from_file_location('medium_viewer', SHARED / 'review_four_views.py')
    renderer = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(renderer)
    structure = renderer.Structure.read(ROOM / 'room.nbt')
    views = ROOT / 'views'
    views.mkdir(exist_ok=True)
    for direction, title, camera in renderer.VIEWS:
        renderer.render(structure, metadata, camera, title, False, size=(1440, 1100)).save(views / (direction + '.png'))
    renderer.render(structure, metadata, (0.7, 1), '完整外壳', True, size=(1440, 1100)).save(views / 'exterior.png')
    derivative = copy.deepcopy(template)
    air = next(index for index, state in enumerate(derivative['palette']) if state['Name'] == 'minecraft:air')
    changed = []
    for record in derivative['blocks']:
        block_x, block_y, block_z = record['pos']
        if block_y == 11 or block_y >= 2 and block_z == 42:
            if record['state'] != air:
                changed.append(record['pos'])
            record['state'] = air
            record.pop('nbt', None)
    kind, payload = nbt_payload(derivative)
    assert kind == 10
    target = views / 'PREVIEW_ONLY_cutaway.nbt'
    target.write_bytes(gzip.compress(b'\x0a\x00\x00' + payload, mtime=0))
    original = {tuple(record['pos']): record for record in template['blocks']}
    assert all(record == original[tuple(record['pos'])] or record['state'] == air for record in derivative['blocks'])
    write(views / 'derivative.json', {'status': 'PASS_AIR_ONLY_DERIVATION', 'source_sha256': metadata['sha256'], 'derivative_sha256': sha(target), 'source': str(ROOM / 'room.nbt'), 'derivative': str(target), 'changes': 'Roof y11 and south end boundary above floor replaced with air; complete parallel side walls, mushrooms and vegetation retained; no added blocks; final source unchanged', 'changed_positions': changed, 'eligible_for_generation': False})


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--run-dir', type=Path, required=True)
    args = parser.parse_args()
    acceptance = json.loads(ACCEPTANCE.read_text(encoding='utf-8'))
    assert acceptance['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION' and Path(acceptance['next_allowed']).name == 'S7-05-corridor.png'
    assert sha(ROOT.parent / '04-great-hall/incoming/S7R2-04/room.nbt') == acceptance['nbt_sha256']
    template = load_structure(ROOM / 'room.nbt')
    assert template['size'] == [15, 12, 43] and template['DataVersion'] == 3465 and not template.get('entities')
    palette = template['palette']
    blocks = {tuple(record['pos']): palette[record['state']] for record in template['blocks']}
    assert len(blocks) == len(template['blocks']) == 15 * 12 * 43
    assert all(0 <= point[axis] < template['size'][axis] for point in blocks for axis in range(3))
    forbidden = {'spawner', 'command_block', 'structure_block', 'jigsaw', 'lava', 'fire', 'cobweb', 'powder_snow'}
    assert all(state['Name'].startswith('minecraft:') and state['Name'].split(':')[-1] not in forbidden for state in palette)
    assert all(not record.get('nbt', {}).get('Items') and not record.get('nbt', {}).get('LootTable') for record in template['blocks'])

    def name(point):
        return blocks.get(point, {'Name': 'minecraft:air'})['Name'].split(':')[-1]

    for point, state in blocks.items():
        if state['Name'] == 'minecraft:water':
            block_x, block_y, block_z = point
            assert name((block_x, block_y - 1, block_z)) == 'deepslate_bricks'
            assert all(name((block_x + delta_x, block_y, block_z + delta_z)) in {'water', 'polished_blackstone_bricks'} for delta_x, delta_z in [(1, 0), (-1, 0), (0, 1), (0, -1)])
    assert all(name((block_x, 11, block_z)) != 'air' for block_x in range(15) for block_z in range(43))
    assert all(name((block_x, block_y, block_z)) == 'air' for block_x in range(5, 10) for block_y in [2, 3] for block_z in range(1, 42))
    assert all(name((block_x, block_y, block_z)) != 'air' for block_x in [0, 14] for block_y in range(2, 11) for block_z in range(1, 42))
    assert sum(state['Name'] == 'minecraft:water' for state in blocks.values()) == 20
    assert not any(state['Name'].endswith('banner') or state['Name'].endswith('chest') for state in palette)
    for stem_x in [2, 12]:
        for stem_z in [5, 13, 21, 29, 37]:
            assert all(name((stem_x, block_y, stem_z)) != 'air' for block_y in range(2, 12))
            assert name((stem_x, 6, stem_z)) == 'shroomlight'
    targets = [[7, 2, block_z] for block_z in [0, 3, 5, 13, 21, 29, 37, 41, 42]]
    targets += [[block_x, 2, block_z] for block_x in [5, 9] for block_z in [7, 17, 27, 39]]
    connections = []
    for port_id, direction, feet, normal, source in [
        ('north-end-arch', 'north', [7, 2, 0], [0, 0, -1], 'near end specified by prompt; camera cropped in reference'),
        ('south-end-arch', 'south', [7, 2, 42], [0, 0, 1], 'visible distant arch in one-point reference')]:
        connections.append({'id': port_id, 'direction': direction, 'feet': feet, 'normal': normal, 'outside_feet': [feet[axis] + normal[axis] for axis in range(3)], 'opening': [3, 4], 'source': source})
    metadata = {'id': 'S7R2-05', 'name': '菌光直廊·双端贯通', 'size': template['size'], 'facing': 'north', 'entrance': [7, 2, 0], 'interior': [7, 2, 3], 'test_targets': targets,
                'connections': connections, 'sha256': sha(ROOM / 'room.nbt'), 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'status': 'AWAITING_INDEPENDENT_REGISTRATION_ACCEPTANCE', 'runtime_slot': None,
                'geometry_origin': 'Original single-image S7-05 straight corridor, ten wall-integrated mushroom buttresses, complete ceiling and two end arches only; no old geometry imported', 'coordinate_system': 'integer feet blocks, +X east +Y up +Z south', 'world_placement': 'NOT_RUN'}
    write(ROOM / 'room.json', metadata)
    write(ROOM / 'blocks.json', dict(sorted(collections.Counter(state['Name'] for state in blocks.values()).items())))
    write(ROOT / 'qa/static.json', {'status': 'PASS_STATIC_ONLY', 'sha256': metadata['sha256'], 'size': template['size'], 'checks': ['dense unique bounds', 'DataVersion3465', 'no entities or automatic loot', 'no prohibited blocks', 'full roof', 'ten wall mushroom stems/lights, complete parallel sides, clear straight route', 'four pools with full bottom and four-side frames'], 'water_blocks': sum(state['Name'] == 'minecraft:water' for state in blocks.values()), 'updates': 'NOT_RUN'})
    report = native(args.run_dir.resolve(), metadata)
    write(ROOT / 'qa/latest-native-run.json', {'directory': str(args.run_dir.resolve()), 'source_sha256': metadata['sha256'], 'status': report['status'], 'all_target_routes': len(report['target_paths']), 'fragile_checks': len(report['static_survival'])})
    duplicate_review(template)
    render(template, metadata)
    print('S7-05 VERIFIED AND SOFTWARE VIEWS COMPLETE; S7-06 NOT STARTED', flush=True)


if __name__ == '__main__':
    main()
