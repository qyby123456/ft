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
ROOM = ROOT / 'incoming/S7R2-09'
SHARED = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
MAPPED = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
SUPPORT = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S7-fungal-grove/out/S7-09-treasure.png')
ACCEPTANCE = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S7-08-independent/independent-details.json')
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
    roots = [Path('D:/ft工程文件/FT地牢_自然环境第7组_蘑菇真菌_20261104'), Path('D:/ft工程文件/FT地牢_自然环境第一批第2组_20261104'), Path('D:/ft工程文件/FT地牢_首批第3组_冰原霜穴_20261104'), ROOT.parent / '01-large-chamber', ROOT.parent / '02-medium-room', ROOT.parent / '03-small-room', ROOT.parent / '04-great-hall', ROOT.parent / '05-corridor', ROOT.parent / '06-corner', ROOT.parent / '07-entrance', ROOT.parent / '08-courtyard']
    records = []
    for root in roots:
        for path in sorted(root.glob('incoming/*/room.nbt')):
            data = read(path)
            records.append({'path': str(path), 'sha256': sha(path), 'size': data['size'], 'exact_material_independent_geometry_match': own == signature(data)})
    assert not any(record['exact_material_independent_geometry_match'] for record in records)
    result = {'status': 'PASS_LOCAL_NO_EXACT_GEOMETRY_REUSE', 'source_sha256': sha(ROOM / 'room.nbt'), 'signature': own, 'compared': records,
              'semantic_self_check': {'previous': 'S7-01 two levels, U gallery, stairs, central giant mushroom dais, four elevation-aware ports', 'current': 'S7-09 stepped single chest shrine, twin fungal shelves and supply alcoves, three ground ports with variable elevations; independently authored geometry', 'recipe_geometry_reused': False},
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
        if block_y == 18 or block_y >= 2 and (block_x == 0 or block_z == 28):
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
    write(views / 'derivative.json', {'status': 'PASS_AIR_ONLY_DERIVATION', 'source_sha256': metadata['sha256'], 'derivative_sha256': sha(target), 'source': str(ROOM / 'room.nbt'), 'derivative': str(target), 'changes': 'Closed roof y18 and west x0/south z28 walls above floor removed to air; no additions; final source unchanged; camera-only complete-source views supplied separately', 'changed_positions': changed, 'eligible_for_generation': False})


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--run-dir', type=Path, required=True)
    args = parser.parse_args()
    acceptance = json.loads(ACCEPTANCE.read_text(encoding='utf-8'))
    assert acceptance['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    assert sha(ROOT.parent / '08-courtyard/incoming/S7R2-08/room.nbt') == acceptance['sha256']
    template = load_structure(ROOM / 'room.nbt')
    assert template['size'] == [31, 19, 29] and template['DataVersion'] == 3465 and not template.get('entities')
    palette = template['palette']
    blocks = {tuple(record['pos']): palette[record['state']] for record in template['blocks']}
    assert len(blocks) == len(template['blocks']) == 31 * 19 * 29
    assert all(0 <= point[axis] < template['size'][axis] for point in blocks for axis in range(3))
    forbidden = {'spawner', 'command_block', 'structure_block', 'jigsaw', 'lava', 'fire', 'cobweb', 'powder_snow'}
    assert all(state['Name'].startswith('minecraft:') and state['Name'].split(':')[-1] not in forbidden for state in palette)
    entities = [{'position': record['pos'], 'state': palette[record['state']], 'nbt': record['nbt']} for record in template['blocks'] if record.get('nbt')]
    assert len(entities) == 11 and collections.Counter(entry['nbt']['id'] for entry in entities) == {'minecraft:chest': 1, 'minecraft:barrel': 6, 'minecraft:banner': 4}
    assert all(not entry['nbt'].get('Items') and not entry['nbt'].get('LootTable') for entry in entities)
    def name(point):
        return blocks.get(point, {'Name': 'minecraft:air'})['Name'].split(':')[-1]
    for point, state in blocks.items():
        if state['Name'] == 'minecraft:water':
            block_x, block_y, block_z = point
            assert name((block_x, block_y - 1, block_z)) == 'deepslate_bricks'
            assert all(name((block_x + delta_x, block_y, block_z + delta_z)) in {'water', 'polished_blackstone_bricks'} for delta_x, delta_z in [(1, 0), (-1, 0), (0, 1), (0, -1)])
    assert sum(state['Name'] == 'minecraft:water' for state in blocks.values()) == 18
    assert all(name((block_x, 18, block_z)) != 'air' for block_x in range(31) for block_z in range(29))
    targets = [[15, 2, 28], [15, 2, 25], [15, 2, 20], [15, 2.5, 17], [15, 3, 16], [15, 3, 12], [0, 3, 12], [30, 3, 12], [7, 3, 12], [23, 3, 12], [15, 3.5, 10], [15, 4, 9], [15, 4.5, 8], [15, 5, 7], [15, 5, 6], [3, 3, 11], [4, 3, 11], [5, 3, 11], [25, 3, 14], [26, 3, 14], [4, 2, 20], [12, 2, 22], [18, 2, 22], [26, 2, 20]]
    connections = []
    for port_id, direction, feet, normal, source in [('south-front-arch', 'south', [15, 2, 28], [0, 0, 1], 'foreground stair arch visible in reference'), ('west-side-arch', 'west', [0, 3, 12], [-1, 0, 0], 'left supply-side opening'), ('east-side-arch', 'east', [30, 3, 12], [1, 0, 0], 'right urn-side opening')]:
        connections.append({'id': port_id, 'direction': direction, 'feet': feet, 'normal': normal, 'outside_feet': [feet[axis] + normal[axis] for axis in range(3)], 'opening': [3, 4], 'source': source})
    interactions = [{'position': [15, 5, 5], 'kind': 'chest', 'access_feet': [15, 5, 6]}]
    for point in [[3, 3, 10], [4, 3, 10], [5, 3, 10], [3, 4, 10], [25, 3, 13], [26, 3, 13]]:
        interactions.append({'position': point, 'kind': 'barrel', 'access_feet': [point[0], 3, point[2] + 1]})
    metadata = {'id': 'S7R2-09', 'name': '菌窟阶台秘藏', 'size': template['size'], 'facing': 'south', 'entrance': [15, 2, 28], 'interior': [15, 2, 25], 'test_targets': targets, 'connections': connections, 'interactions': interactions, 'block_entities': {'chest': 1, 'barrel': 6, 'banner': 4, 'automatic_loot': False}, 'source_roof': {'top_y': 18, 'solid_cells': 899, 'open_skylight': False}, 'water_blocks': 18, 'sha256': sha(ROOM / 'room.nbt'), 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'status': 'AWAITING_INDEPENDENT_REGISTRATION_ACCEPTANCE', 'runtime_slot': None, 'geometry_origin': 'Original S7-09 stepped chest shrine and raised twin mushroom alcoves, variable elevation ground paths; no old room geometry imported', 'coordinate_system': 'feet block coordinates may use .5 Y; +X east +Y up +Z south', 'world_placement': 'NOT_RUN'}
    write(ROOM / 'room.json', metadata)
    write(ROOM / 'blocks.json', dict(sorted(collections.Counter(state['Name'] for state in blocks.values()).items())))
    write(ROOT / 'qa/block-entities.json', {'source_sha256': metadata['sha256'], 'entities': entities, 'world_interaction': 'NOT_RUN'})
    write(ROOT / 'qa/static.json', {'status': 'PASS_STATIC_ONLY', 'sha256': metadata['sha256'], 'size': template['size'], 'checks': ['dense unique bounds', 'DataVersion3465', '11 real block entities, empty containers, no automatic loot', 'closed source roof 899 blocks', 'two framed 3x3 pools,18 water sources', 'three elevation-aware 3x4 arches'], 'updates': 'NOT_RUN'})
    report = native(args.run_dir.resolve(), metadata)
    write(ROOT / 'qa/latest-native-run.json', {'directory': str(args.run_dir.resolve()), 'source_sha256': metadata['sha256'], 'status': report['status'], 'all_target_routes': len(report['target_paths']), 'fragile_checks': len(report['static_survival'])})
    duplicate_review(template)
    render(template, metadata)
    print('S7-09 VERIFIED AND SOFTWARE VIEWS COMPLETE; S7-10 NOT STARTED', flush=True)


if __name__ == '__main__':
    main()
