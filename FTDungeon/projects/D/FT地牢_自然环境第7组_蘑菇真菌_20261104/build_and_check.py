import collections
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import subprocess
import sys

from PIL import Image, ImageDraw, ImageFont
from source_evidence import collect

ROOT = Path(__file__).resolve().parent
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
SHARED = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
REFERENCES = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S7-fungal-grove/out')
sys.path.insert(0, 'D:/ft工程文件/血棘祭炉祭坛厅地牢_20261101/tools')
from structure_io import load_structure


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def check(room, folder):
    template = load_structure(folder / 'room.nbt')
    width, height, depth = template['size']
    assert template['size'] == room['size'] and template['DataVersion'] == 3465
    assert not template.get('entities')
    palette = template['palette']
    blocks = {tuple(record['pos']): palette[record['state']] for record in template['blocks']}
    assert len(blocks) == len(template['blocks'])
    assert len(blocks) == width * height * depth
    assert all(0 <= point[axis] < room['size'][axis] for point in blocks for axis in range(3))
    assert all(state['Name'].startswith('minecraft:') for state in palette)
    forbidden = {'command_block', 'spawner', 'structure_block', 'jigsaw', 'lava', 'fire', 'cobweb', 'powder_snow'}
    assert not any(state['Name'].split(':')[1] in forbidden for state in palette)
    assert all(not record.get('nbt', {}).get('Items') and not record.get('nbt', {}).get('LootTable') for record in template['blocks'])
    middle_x, middle_z = width // 2, depth // 2
    ports = {'north': [middle_x, 1, 0], 'south': [middle_x, 1, depth - 1], 'east': [width - 1, 1, middle_z], 'west': [0, 1, middle_z]}
    entrance = tuple(ports['north'])
    transparent = {'air', 'cave_air', 'glow_lichen', 'red_mushroom', 'brown_mushroom', 'stone_pressure_plate'}

    def name(point):
        return blocks.get(point, {'Name': 'minecraft:air'})['Name'].split(':')[1]

    def walk(point):
        block_x, block_y, block_z = point
        return (0 <= block_x < width and 1 <= block_y < height - 1 and 0 <= block_z < depth
                and name(point) in transparent and name((block_x, block_y + 1, block_z)) in transparent
                and name((block_x, block_y - 1, block_z)) not in transparent)

    reached = {entrance}
    pending = collections.deque([entrance])
    while pending:
        block_x, block_y, block_z = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for delta_y in (0, 1, -1):
                following = (block_x + delta_x, block_y + delta_y, block_z + delta_z)
                if following not in reached and walk(following):
                    reached.add(following)
                    pending.append(following)
    connections = []
    for direction in ['north', *room['exits']]:
        point = ports[direction]
        assert tuple(point) in reached, (room['id'], direction, 'unreachable')
        delta_x, delta_z = (1, 0) if direction in ('north', 'south') else (0, 1)
        for lateral in (-1, 0, 1):
            assert walk((point[0] + delta_x * lateral, 1, point[2] + delta_z * lateral))
            assert all(name((point[0] + delta_x * lateral, level, point[2] + delta_z * lateral)) == 'air' for level in (1, 2, 3))
        normal = {'north': [0, 0, -1], 'south': [0, 0, 1], 'east': [1, 0, 0], 'west': [-1, 0, 0]}[direction]
        connections.append({'direction': direction, 'feet': point, 'outward_normal': normal, 'outside_feet': [point[axis] + normal[axis] for axis in range(3)], 'opening': [3, 3], 'role': 'entrance' if direction == 'north' else 'exit', 'reachable': True})
    if room['kind'] != 'courtyard':
        assert all(name((block_x, height - 1, block_z)) != 'air' for block_x in range(width) for block_z in range(depth) if name((block_x, 0, block_z)) != 'air')
    for point, state in blocks.items():
        block_x, block_y, block_z = point
        if state['Name'].endswith((':red_mushroom', ':brown_mushroom')):
            assert name((block_x, block_y - 1, block_z)) == 'mycelium'
        if state['Name'].endswith(':amethyst_cluster'):
            assert name((block_x, block_y - 1, block_z)) not in transparent
        if state['Name'].endswith(':glow_lichen'):
            properties = state.get('Properties', {})
            assert any(properties.get(direction) == 'true' and name((block_x + delta_x, block_y, block_z + delta_z)) not in transparent
                       for direction, delta_x, delta_z in [('east', 1, 0), ('west', -1, 0), ('north', 0, -1), ('south', 0, 1)])
    reference = REFERENCES / ('S7-' + room['id'][-2:] + '-' + room['kind'] + '.png')
    with Image.open(reference) as image:
        image.verify()
    evidence = json.loads((ROOT / 'qa/source-evidence.json').read_text(encoding='utf-8'))
    adaptation = next(entry for entry in evidence['adaptations'] if entry['room'] == room['id'])
    source = evidence['sources'][adaptation['source']]
    metadata = {**room, 'facing': 'north', 'entrance': list(entrance), 'interior': [middle_x, 1, 2],
                'mod_source': source['path'], 'mod_source_sha256': source['sha256'], 'mod_source_size': source['size'], 'adaptation': adaptation['changes'],
                'sha256': sha(folder / 'room.nbt'), 'reference': str(reference), 'reference_sha256': sha(reference),
                'connections': connections, 'coordinate_system': '+X east, +Y up, +Z south; feet coordinates',
                'integration_status': 'candidate; not deployed; unique slot pending coordinator'}
    assert tuple(metadata['interior']) in reached
    if room['kind'] == 'large-chamber':
        assert (2, 4, 2) in reached and (width - 3, 4, depth - 3) in reached
    if room['kind'] == 'corridor':
        assert (middle_x, 3, 8) in reached
    metadata['reachable_feet_levels'] = sorted({point[1] for point in reached})
    write(folder / 'room.json', metadata)
    materials = dict(sorted(collections.Counter(state['Name'] for state in blocks.values()).items()))
    write(folder / 'blocks.json', materials)
    qa = {'status': 'PASS', 'id': room['id'], 'sha256': metadata['sha256'], 'checks': ['gzip NBT roundtrip', 'DataVersion 3465', 'bounds and unique positions', 'explicit air volume', 'all ports 3x3 clear and supported', 'all ports reachable', 'safe interior reachable', 'roof complete except courtyard', 'plants and lichen supported', 'no entities or automatic loot', 'no fluids or hazardous blocks'], 'reachable_positions': len(reached), 'materials': materials,
          'native_validation': 'see ../../qa/native-review.json', 'world_placement': 'NOT RUN', 'forge_gametest': 'NOT RUN', 'gameplay': 'NOT RUN'}
    write(folder / 'qa.json', qa)
    return metadata


def native_check(rooms):
    source = (SHARED / 'VerifyDeliveries.java').read_text(encoding='utf-8')
    source = source.replace('"L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3"', ', '.join(json.dumps(room['id']) for room in rooms))
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
        if result.returncode:
            raise RuntimeError(result.stdout[-4000:] + result.stderr[-4000:])
    report = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    assert report['status'] == 'PASS' and len(report['rooms']) == 9


def render(rooms):
    specification = importlib.util.spec_from_file_location('four_views', SHARED / 'review_four_views.py')
    renderer = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(renderer)
    sheet = Image.new('RGB', (2160, 1680), '#101a20')
    for index, room in enumerate(rooms):
        folder = ROOT / 'incoming' / room['id']
        structure = renderer.Structure.read(folder / 'room.nbt')
        views = folder / 'views'
        views.mkdir(exist_ok=True)
        for direction, title, camera in renderer.VIEWS:
            path = views / (direction + '.png')
            renderer.render(structure, room, camera, title, False, size=(960, 740)).save(path)
        renderer.render(structure, room, (-0.6, -1), '完整外壳', True, size=(960, 740)).save(views / 'exterior.png')
        with Image.open(views / 'north.png') as image:
            image.save(folder / 'preview.png')
            sheet.paste(image.resize((720, 560)), ((index % 3) * 720, (index // 3) * 560))
        print('RENDER', room['id'], flush=True)
    sheet.save(ROOT / 'overview.png')


def main():
    rooms = json.loads((ROOT / 'rooms.json').read_text(encoding='utf-8'))
    (ROOT / 'qa').mkdir(exist_ok=True)
    collect()
    print('REAL MOD NBT EVIDENCE READ BEFORE GENERATION', flush=True)
    metadata = []
    for room in rooms:
        folder = ROOT / 'incoming' / room['id']
        folder.mkdir(parents=True, exist_ok=True)
        environment = {**os.environ, 'S7_ROOM_ID': room['id']}
        result = subprocess.run(['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'fungal-recipe.mjs'), '--out-dir', str(folder), '--name', 'room', '--data-version', '3465', '--json'], env=environment, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=90, creationflags=subprocess.CREATE_NO_WINDOW)
        (folder / 'build.log').write_text(result.stdout + result.stderr, encoding='utf-8')
        assert result.returncode == 0, result.stdout + result.stderr
        metadata.append(check(room, folder))
        print('BUILD/STATIC PASS', room['id'], flush=True)
    native_check(rooms)
    print('NATIVE 9/9 PASS', flush=True)
    render(metadata)
    overview_reference = REFERENCES / 'S7-10-overview.png'
    write(ROOT / 'manifest.json', {'group': 'S7-fungal-grove', 'batch': 'first 01-10; 09 placeable rooms + 01 reference overview', 'thread_id': '01a1a5df-d735-7b43-9d3b-2939bfc6c0ed', 'rooms': metadata, 'reference_overview': str(overview_reference), 'reference_overview_sha256': sha(overview_reference), 'new_image_api_calls': 0, 'image_cleanup': 'not required; no image generation invoked', 'static_qa': '9/9 PASS', 'native_qa': '9/9 PASS', 'forge_world_placement': 'NOT RUN', 'deployed': False})
    write(ROOT / 'SHA256SUMS.json', {str(path.relative_to(ROOT)): sha(path) for path in ROOT.rglob('*') if path.is_file() and path.name != 'SHA256SUMS.json' and 'native-classes' not in str(path)})
    print('COMPLETE', ROOT, flush=True)


if __name__ == '__main__':
    main()
