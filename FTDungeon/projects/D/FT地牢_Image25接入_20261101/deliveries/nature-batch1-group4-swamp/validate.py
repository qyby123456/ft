import argparse
import collections
import functools
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import subprocess
import sys

from PIL import Image, ImageChops, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent
TOOLS = ROOT.parents[1] / 'tools'
NBT_TOOLS = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
sys.path.insert(0, str(NBT_TOOLS))
from nbtio import Structure, read_nbt, plain


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def inspect(folder):
    metadata = json.loads((folder / 'room.json').read_text(encoding='utf-8'))
    structure = Structure.read(folder / 'room.nbt')
    raw = plain(read_nbt(folder / 'room.nbt')[1])
    width, height, depth = structure.size
    checks = []

    def require(condition, label):
        if not condition:
            raise ValueError(metadata['id'] + ': ' + label)
        checks.append(label)

    def name(position):
        return structure.get(position).name.removeprefix('minecraft:')

    def solid(position):
        return name(position) not in {'air', 'cave_air', 'void_air', 'water', 'lava', 'lily_pad', 'oak_leaves'}

    def walkable(position):
        block_x, block_y, block_z = position
        return 0 <= block_x < width and 0 <= block_z < depth and name(position) == 'air' and name((block_x, block_y + 1, block_z)) == 'air' and solid((block_x, block_y - 1, block_z))

    require(list(structure.size) == metadata['size'] and raw['DataVersion'] == 3465, 'dimensions_and_1_20_1_version')
    require(not raw.get('entities'), 'no_entities')
    require(all(all(0 <= position[axis] < structure.size[axis] for axis in range(3)) for position in structure.blocks), 'all_blocks_in_bounds')
    forbidden = {'spawner', 'command_block', 'repeating_command_block', 'chain_command_block', 'structure_block', 'structure_void', 'jigsaw', 'lava', 'fire', 'soul_fire'}
    require(not any(block.name.removeprefix('minecraft:') in forbidden for block in structure.blocks.values()), 'no_execution_spawn_or_fire_hazards')
    require(all(not record.get('nbt', {}).get('Items') and 'LootTable' not in record.get('nbt', {}) for record in raw['blocks']), 'empty_containers_no_rewards')
    footprint = [tuple(position) for position in metadata['footprint']]
    require(all(solid((block_x, 0, block_z)) for block_x, block_z in footprint), 'sealed_foundation')
    require(metadata['open_sky'] or all(solid((block_x, height - 1, block_z)) for block_x, block_z in footprint), 'complete_roof_except_courtyard')
    water = [position for position in structure.blocks if name(position) == 'water']
    require(all(solid((block_x, block_y - 1, block_z)) and all(name((block_x + delta_x, block_y, block_z + delta_z)) == 'water' or solid((block_x + delta_x, block_y, block_z + delta_z)) for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1))) for block_x, block_y, block_z in water), 'water_has_bottom_and_four_sides')
    require(all(name((block_x, block_y - 1, block_z)) == 'water' for (block_x, block_y, block_z), block in structure.blocks.items() if block.name == 'minecraft:lily_pad'), 'lily_pad_water_support')
    require(all(solid((block_x, block_y - 1, block_z)) for (block_x, block_y, block_z), block in structure.blocks.items() if block.name in {'minecraft:ochre_froglight', 'minecraft:chest'}), 'lights_and_chests_supported')
    entrance = tuple(metadata['entrance'])
    require(walkable(entrance), 'entrance_safe')
    reached = {entrance}
    pending = collections.deque([entrance])
    while pending:
        block_x, block_y, block_z = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for delta_y in (0, -1, 1):
                target = (block_x + delta_x, block_y + delta_y, block_z + delta_z)
                stair = name((block_x, block_y - 1, block_z)).endswith('_stairs') or name((target[0], target[1] - 1, target[2])).endswith('_stairs')
                if delta_y and not stair:
                    continue
                if target not in reached and walkable(target):
                    reached.add(target)
                    pending.append(target)
    require(tuple(metadata['interior']) in reached, 'interior_reachable')
    if 'centerline_xz' in metadata['topology']:
        route = metadata['topology']['centerline_xz']
        for start, end in zip(route, route[1:]):
            distance = abs(end[0] - start[0]) + abs(end[1] - start[1])
            sign_x = (end[0] > start[0]) - (end[0] < start[0])
            sign_z = (end[1] > start[1]) - (end[1] < start[1])
            require(all((start[0] + sign_x * step, 2, start[1] + sign_z * step) in reached for step in range(distance + 1)), 'declared_meander_segment_connected')
        require(not all((width // 2, 2, block_z) in reached for block_z in range(depth)), 'no_straight_north_south_dry_shortcut')
    for point in metadata['connection_points']:
        block_x, block_y, block_z = point['position']
        slots = [(block_x + (0 if point['face'] == 'east' else offset), block_y, block_z + (offset if point['face'] == 'east' else 0)) for offset in (-1, 0, 1)]
        require(all(slot in reached and name((slot[0], slot[1] + 2, slot[2])) == 'air' for slot in slots), point['face'] + '_dry_reachable_3x3_portal')
    with Image.open(metadata['reference']) as image:
        image.verify()
    metadata['sha256'] = digest(folder / 'room.nbt')
    metadata['reference_sha256'] = digest(Path(metadata['reference']))
    metadata['integration_status'] = 'isolated_candidate_not_deployed'
    save(folder / 'room.json', metadata)
    counts = collections.Counter(block.name for block in structure.blocks.values() if block.name != 'minecraft:air')
    metadata['source_palette_retained'] = sorted(set(counts) & set(metadata['source_evidence']['source_palette_names']))
    metadata['theme_palette_additions'] = sorted(set(counts) - set(metadata['source_evidence']['source_palette_names']))
    metadata['dry_graph_sha256'] = hashlib.sha256(json.dumps(sorted(reached)).encode()).hexdigest()
    save(folder / 'room.json', metadata)
    save(folder / 'materials.json', dict(sorted(counts.items())))
    result = {'id': metadata['id'], 'status': 'PASS', 'sha256': metadata['sha256'], 'checks': checks, 'reachable_dry_positions': len(reached), 'water_blocks': len(water), 'block_count': sum(counts.values()), 'native_world_placement': 'NOT_RUN', 'gameplay': 'NOT_RUN'}
    save(folder / 'qa.json', result)
    return metadata, result


def native(rooms):
    source = (TOOLS / 'VerifyDeliveries.java').read_text(encoding='utf-8')
    source = source.replace('List.of("L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3")', 'List.of(' + ', '.join(json.dumps(room['id']) for room in rooms) + ')')
    marker = 'StructureTemplate template = new StructureTemplate();'
    addition = 'for (var exit : metadata.getAsJsonArray("exits")) { metadata.add("exitTarget", exit.getAsJsonObject().get("position")); checkPath(stateMap, size, coordinate(metadata, "entrance"), coordinate(metadata, "exitTarget")); }\n        '
    addition += 'if (!metadata.getAsJsonObject("vertical_layers").get("observation_feet").isJsonNull()) { metadata.add("observationTarget", metadata.getAsJsonObject("vertical_layers").get("observation_feet")); checkPath(stateMap, size, coordinate(metadata, "entrance"), coordinate(metadata, "observationTarget")); }\n        '
    source = source.replace(marker, addition + marker)
    java_file = ROOT / 'qa/VerifyDeliveries.java'
    java_file.write_text(source, encoding='utf-8')
    mapped = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
    support = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
    dependencies = [line for line in support.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    classpath = os.pathsep.join([str(mapped), *dependencies])
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    classes = ROOT / 'qa/native-classes'
    classes.mkdir(exist_ok=True)
    commands = [('compile', [str(java / 'javac.exe'), '-encoding', 'UTF-8', '-classpath', classpath, '-d', str(classes), str(java_file)]), ('run', [str(java / 'java.exe'), '-Djava.awt.headless=true', '-classpath', str(classes) + os.pathsep + classpath, 'VerifyDeliveries', str(ROOT)])]
    for label, command in commands:
        result = subprocess.run(command, cwd=ROOT, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=180, creationflags=subprocess.CREATE_NO_WINDOW)
        (ROOT / f'qa/native-{label}.log').write_text(result.stdout + result.stderr, encoding='utf-8')
        if result.returncode:
            raise RuntimeError(result.stdout[-3000:] + result.stderr[-3000:])
    print('NATIVE PASS', flush=True)


def render(rooms):
    specification = importlib.util.spec_from_file_location('four_views', TOOLS / 'review_four_views.py')
    renderer = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(renderer)
    original_texture = renderer.renderer.texture

    @functools.lru_cache(maxsize=None)
    def tinted_texture(identifier, uv, rotation, brightness):
        image = original_texture(identifier, uv, rotation, brightness)
        color = {'block/water_still': '#3f8588', 'block/oak_leaves': '#5c853f', 'block/lily_pad': '#48743a'}.get(identifier)
        return ImageChops.multiply(image, Image.new('RGBA', image.size, color)) if color else image

    renderer.renderer.texture = tinted_texture
    sheet = Image.new('RGB', (1800, 1440), '#101a20')
    for index, metadata in enumerate(rooms):
        folder = ROOT / 'incoming' / metadata['id']
        structure = Structure.read(folder / 'room.nbt')
        views = Image.new('RGB', (1800, 1400), '#101a20')
        for view_index, (direction, title, camera) in enumerate(renderer.VIEWS):
            image = renderer.render(structure, metadata, camera, title, False, size=(900, 700))
            image.save(folder / f'interior-{direction}.png')
            views.paste(image, ((view_index % 2) * 900, (view_index // 2) * 700))
            if view_index == 0:
                image.save(folder / 'preview.png')
                sheet.paste(image.resize((600, 480)), ((index % 3) * 600, (index // 3) * 480))
        views.save(folder / 'interior-four-views.png')
        renderer.render(structure, metadata, (-0.35, -1), '完整外壳', True, size=(900, 700)).save(folder / 'exterior.png')
        print('RENDERED ' + metadata['id'], flush=True)
    sheet.save(ROOT / 'overview.png')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--render', action='store_true')
    parser.add_argument('--native', action='store_true')
    arguments = parser.parse_args()
    rooms, reports = zip(*(inspect(folder) for folder in sorted((ROOT / 'incoming').iterdir()) if folder.is_dir()))
    save(ROOT / 'qa/static-review.json', {'status': 'PASS', 'rooms': reports})
    references = [{'path': room['reference'], 'sha256': room['reference_sha256']} for room in rooms]
    overview = Path(rooms[0]['reference']).parent / 'S4-10-overview.png'
    references.append({'path': str(overview), 'sha256': digest(overview), 'role': 'layout_reference_only_not_room'})
    save(ROOT / 'reference-manifest.json', {'group': 'batch1-group4-S4-swamp-wetland', 'thread': '01a1a5df-9064-78f2-8799-cf72f1fbda1e', 'references': references, 'group_sha256': hashlib.sha256(json.dumps(references, sort_keys=True).encode()).hexdigest(), 'image_generation_calls': 0, 'partial_generated_images': 0})
    save(ROOT / 'manifest.json', {'status': 'ISOLATED_CANDIDATE', 'rooms': [{key: room[key] for key in ('id', 'name', 'kind', 'size', 'entrance', 'exits', 'sha256', 'reference')} for room in rooms], 'no_world_or_jar_changes': True})
    if arguments.native:
        native(rooms)
    if arguments.render:
        render(rooms)
    print('STATIC PASS: ' + str(len(rooms)) + ' modules', flush=True)


if __name__ == '__main__':
    main()
