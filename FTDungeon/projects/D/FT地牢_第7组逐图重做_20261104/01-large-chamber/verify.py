import collections
import copy
import gzip
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parent
IDENTITY = 'S7R2-01'
ROOM = ROOT / 'incoming' / IDENTITY
SHARED = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S7-fungal-grove/out/S7-01-large-chamber.png')
sys.path.insert(0, 'D:/ft工程文件/血棘祭炉祭坛厅地牢_20261101/tools')
from structure_io import load_structure, nbt_payload


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def native(metadata):
    source = (SHARED / 'VerifyDeliveries.java').read_text(encoding='utf-8')
    source = source.replace('"L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3"', '"' + IDENTITY + '"')
    anchor = 'int reachable = checkPath(stateMap, size, coordinate(metadata, "entrance"), coordinate(metadata, "interior"));'
    assert anchor in source
    source = source.replace(anchor, anchor + '\n        for (var target : metadata.getAsJsonArray("test_targets")) {\n            var point = target.getAsJsonArray();\n            checkPath(stateMap, size, coordinate(metadata, "entrance"), new BlockPos(point.get(0).getAsInt(), point.get(1).getAsInt(), point.get(2).getAsInt()));\n        }')
    java_source = ROOT / 'qa/VerifyDeliveries.java'
    java_source.write_text(source, encoding='utf-8')
    support = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
    dependencies = [line for line in support.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    classpath = os.pathsep.join(['D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar', *dependencies])
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    classes = ROOT / 'qa/native-classes'
    classes.mkdir(exist_ok=True)
    commands = [[str(java / 'javac.exe'), '-encoding', 'UTF-8', '-cp', classpath, '-d', str(classes), str(java_source)],
                [str(java / 'java.exe'), '-Djava.awt.headless=true', '-cp', str(classes) + os.pathsep + classpath, 'VerifyDeliveries', str(ROOT)]]
    for index, command in enumerate(commands):
        result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=180, creationflags=subprocess.CREATE_NO_WINDOW)
        (ROOT / f'qa/native-{index}.log').write_text(result.stdout + result.stderr, encoding='utf-8')
        assert result.returncode == 0, result.stdout[-5000:] + result.stderr[-3000:]
    report = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    assert report['status'] == 'PASS' and len(report['rooms']) == 1
    write(ROOT / 'qa/native-targets.json', {'status': 'PASS', 'targets': metadata['test_targets'], 'sha256': metadata['sha256'], 'scope': 'Actual vanilla collision and stair-compatible BFS to all exits, both stair landings and central dais; not actual world placement.'})


def duplicate_check(template):
    sys.path.insert(0, 'D:/ft工程文件/结构参考_20261103')
    from analyze_structures import NbtReader

    def read(path):
        reader = NbtReader(path.read_bytes())
        assert reader.number('>B') == 10
        reader.string()
        return reader.value(10)

    def signature(data):
        palette = data.get('palette') or data['palettes'][0]
        empty = {'air', 'cave_air', 'void_air', 'water', 'vine', 'red_mushroom', 'brown_mushroom', 'glow_lichen', 'amethyst_cluster', 'torch', 'wall_torch'}
        points = {tuple(record['pos']) for record in data['blocks'] if palette[record['state']]['Name'].split(':')[-1] not in empty}
        results = []
        for turns in range(4):
            rotated = []
            for block_x, block_y, block_z in points:
                for unused in range(turns):
                    block_x, block_z = -block_z, block_x
                rotated.append((block_x, block_y, block_z))
            minimum = [min(point[axis] for point in rotated) for axis in range(3)]
            normalized = sorted(tuple(point[axis] - minimum[axis] for axis in range(3)) for point in rotated)
            results.append(hashlib.sha256(json.dumps(normalized, separators=(',', ':')).encode()).hexdigest())
        return min(results)

    own = signature(template)
    compared = []
    matches = []
    roots = [Path('D:/ft工程文件/FT地牢_自然环境第7组_蘑菇真菌_20261104'), Path('D:/ft工程文件/FT地牢_自然环境第一批第2组_20261104'), Path('D:/ft工程文件/FT地牢_首批第3组_冰原霜穴_20261104')]
    for folder in roots:
        for path in sorted(folder.glob('incoming/*/room.nbt')):
            data = read(path)
            same = signature(data) == own
            compared.append({'path': str(path), 'sha256': sha(path), 'size': data['size'], 'exact_material_independent_match': same})
            if same:
                matches.append(str(path))
    assert not matches
    write(ROOT / 'qa/duplicate-review.json', {'status': 'PASS_LOCAL_NO_EXACT_GEOMETRY_REUSE', 'signature': own, 'compared': compared, 'exact_matches': matches, 'method': 'Material-independent solid voxel mask, translation normalized, four horizontal rotations.', 'limits': 'Not a semantic topology equivalence proof or final all-group acceptance. Compares old archived group7 and currently available group2/group3 snapshots read-only.'})


def renders(template, metadata):
    specification = importlib.util.spec_from_file_location('common_renderer', SHARED / 'review_four_views.py')
    renderer = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(renderer)
    structure = renderer.Structure.read(ROOM / 'room.nbt')
    views = ROOT / 'views'
    views.mkdir(exist_ok=True)
    for direction, title, camera in renderer.VIEWS:
        renderer.render(structure, metadata, camera, title, False, size=(1440, 1100)).save(views / (direction + '.png'))
    renderer.render(structure, metadata, (0.7, 1), '完整外壳', True, size=(1440, 1100)).save(views / 'exterior.png')
    derivative = copy.deepcopy(template)
    derivative['blocks'] = [record for record in derivative['blocks'] if record['pos'][1] < 19 and not (record['pos'][1] >= 2 and (record['pos'][0] == 0 or record['pos'][2] == 28)) and not (record['pos'][1] >= 17 and (record['pos'][0] in (1, 27) or record['pos'][2] in (1, 27)))]
    kind, payload = nbt_payload(derivative)
    assert kind == 10
    (views / 'PREVIEW_ONLY_cutaway.nbt').write_bytes(gzip.compress(b'\x0a\x00\x00' + payload, mtime=0))
    write(views / 'derivative.json', {'purpose': 'Preview-only, never a generation candidate', 'source': str(ROOM / 'room.nbt'), 'source_sha256': metadata['sha256'], 'derivative_sha256': sha(views / 'PREVIEW_ONLY_cutaway.nbt'), 'changes': 'Remove roof, roof-edge beams, west and south boundary walls for reference-like inspection; final NBT unchanged.'})


def main():
    (ROOT / 'qa').mkdir(exist_ok=True)
    template = load_structure(ROOM / 'room.nbt')
    assert template['size'] == [29, 20, 29] and template['DataVersion'] == 3465
    assert not template.get('entities')
    palette = template['palette']
    blocks = {tuple(record['pos']): palette[record['state']] for record in template['blocks']}
    assert len(blocks) == len(template['blocks']) == 29 * 20 * 29
    assert all(0 <= point[axis] < template['size'][axis] for point in blocks for axis in range(3))
    forbidden = {'command_block', 'spawner', 'jigsaw', 'structure_block', 'lava', 'fire', 'soul_fire', 'cobweb', 'powder_snow'}
    assert not any(state['Name'].split(':')[-1] in forbidden or not state['Name'].startswith('minecraft:') for state in palette)
    assert all(not record.get('nbt', {}).get('LootTable') and not record.get('nbt', {}).get('Items') for record in template['blocks'])

    def name(point):
        return blocks.get(point, {'Name': 'minecraft:air'})['Name'].split(':')[-1]

    for point, state in blocks.items():
        block_x, block_y, block_z = point
        identifier = state['Name'].split(':')[-1]
        if identifier == 'water':
            assert name((block_x, block_y - 1, block_z)) not in {'air', 'water'}
            assert all(name((block_x + delta_x, block_y, block_z + delta_z)) in {'water', 'polished_blackstone_bricks'} for delta_x, delta_z in [(1, 0), (-1, 0), (0, 1), (0, -1)])
        if identifier in {'red_mushroom', 'brown_mushroom'}:
            assert name((block_x, block_y - 1, block_z)) == 'mycelium'
        if identifier == 'amethyst_cluster':
            assert name((block_x, block_y - 1, block_z)) == 'amethyst_block'
        if identifier == 'vine':
            assert name((0 if block_x == 1 else 28, block_y, block_z)) not in {'air', 'water'}
    assert all(name((block_x, 19, block_z)) != 'air' for block_x in range(29) for block_z in range(29))
    connections = [
        {'id': 'north-lower-added', 'direction': 'north', 'feet': [14, 2, 0], 'opening': [3, 3], 'source': 'FT compatibility addition'},
        {'id': 'west-lower', 'direction': 'west', 'feet': [0, 2, 14], 'opening': [3, 3], 'source': 'reference lower side arch'},
        {'id': 'east-lower', 'direction': 'east', 'feet': [28, 2, 14], 'opening': [3, 3], 'source': 'reference lower side arch'},
        {'id': 'north-upper', 'direction': 'north', 'feet': [14, 9, 0], 'opening': [3, 3], 'source': 'reference upper rear arch'}]
    for connection in connections:
        block_x, block_y, block_z = connection['feet']
        horizontal = connection['direction'] == 'north'
        for lateral in (-1, 0, 1):
            point = (block_x + lateral if horizontal else block_x, block_y, block_z if horizontal else block_z + lateral)
            assert all(name((point[0], point[1] + level, point[2])) == 'air' for level in range(3))
            assert name((point[0], point[1] - 1, point[2])) == 'stone_bricks'
        normal = {'north': [0, 0, -1], 'east': [1, 0, 0], 'west': [-1, 0, 0]}[connection['direction']]
        connection['normal'] = normal
        connection['outside_feet'] = [connection['feet'][axis] + normal[axis] for axis in range(3)]
    targets = [[14, 2, 3], [0, 2, 14], [28, 2, 14], [14, 9, 0], [3, 9, 18], [25, 9, 18], [14, 5, 16]]
    metadata = {'id': IDENTITY, 'name': '菌菇中枢·双层拱廊', 'size': template['size'], 'facing': 'north', 'entrance': [14, 2, 0], 'interior': [14, 2, 3], 'connections': connections, 'test_targets': targets, 'sha256': sha(ROOM / 'room.nbt'), 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'status': 'AWAITING_REGISTRATION_ACCEPTANCE', 'runtime_slot': None, 'placement': 'NOT DEPLOYED', 'geometry_origin': 'Original single-image recipe; no old batch imported', 'coordinate_system': 'feet XYZ, +X east, +Y up, +Z south'}
    write(ROOM / 'room.json', metadata)
    write(ROOM / 'blocks.json', dict(sorted(collections.Counter(state['Name'] for state in blocks.values()).items())))
    write(ROOT / 'qa/static.json', {'status': 'PASS', 'sha256': metadata['sha256'], 'checks': ['complete explicit volume and bounds', 'DataVersion 3465', 'no entities or automatic loot', 'no executable or hazardous blocks', '3 sealed shallow pools with full bases', 'plants/vines/crystals supported', 'complete final roof', 'four supported 3x3 connection openings'], 'fluids': {'water_blocks': sum(name(point) == 'water' for point in blocks), 'world_updates': 'NOT RUN'}})
    print('STATIC PASS', flush=True)
    native(metadata)
    print('NATIVE LOAD AND ALL TARGET PATHS PASS', flush=True)
    duplicate_check(template)
    print('LOCAL DUPLICATION CHECK PASS', flush=True)
    renders(template, metadata)
    print('RENDER AND PREVIEW DERIVATIVE COMPLETE; ONLY IMAGE 01', flush=True)


if __name__ == '__main__':
    main()
