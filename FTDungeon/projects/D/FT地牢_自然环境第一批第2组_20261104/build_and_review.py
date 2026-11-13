import collections
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parent
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
TOOLS = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
REFERENCES = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out')
sys.path.insert(0, str(TOOLS))
import review_four_views as views
from authority import prepare

SPECS = [
    ('large-chamber', '荒漠日轮祭坛', (19, 12, 19), ['north', 'south', 'east']),
    ('medium-room', '砂岩蓄水间', (15, 9, 15), ['north', 'south', 'east']),
    ('small-room', '沙埋藏宝室', (9, 7, 9), ['north']),
    ('great-hall', '双列柱大厅', (21, 12, 15), ['north', 'south', 'east']),
    ('corridor', '砂岩直廊', (7, 8, 19), ['north', 'south']),
    ('corner', '砂岩直角廊', (13, 8, 13), ['north', 'east']),
    ('entrance', '荒漠守卫前厅', (15, 10, 13), ['north', 'south']),
    ('courtyard', '绿洲天井庭院', (19, 11, 19), ['north', 'south', 'east', 'west']),
    ('treasure', '日轮宝库祭坛', (15, 10, 15), ['north']),
]


def save(path, content):
    path.write_text(json.dumps(content, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def static_review(structure, metadata):
    blocks = structure.blocks
    def name(position):
        block = blocks.get(position)
        return block.name.removeprefix('minecraft:') if block else 'air'
    def walk(position):
        blockX, blockY, blockZ = position
        return name(position) == 'air' and name((blockX, blockY + 1, blockZ)) == 'air' and name((blockX, blockY - 1, blockZ)) in {'sandstone', 'smooth_sandstone', 'cut_sandstone', 'orange_terracotta'}
    reached = {tuple(metadata['entrance'])}
    pending = collections.deque(reached)
    while pending:
        blockX, blockY, blockZ = pending.popleft()
        for deltaX, deltaZ in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
            following = (blockX + deltaX, blockY, blockZ + deltaZ)
            if 0 <= following[0] < structure.size[0] and 0 <= following[2] < structure.size[2] and following not in reached and walk(following):
                reached.add(following)
                pending.append(following)
    assert tuple(metadata['interior']) in reached
    for connection in metadata['connections']:
        assert tuple(connection['position']) in reached
        blockX, blockY, blockZ = connection['position']
        for offset in (-1, 0, 1):
            feet = (blockX + offset, blockY, blockZ) if connection['facing'] in ('north', 'south') else (blockX, blockY, blockZ + offset)
            assert walk(feet)
            assert name((feet[0], 3, feet[2])) == 'air'
    for position, block in blocks.items():
        blockX, blockY, blockZ = position
        material = block.name.removeprefix('minecraft:')
        if material in {'lantern', 'flower_pot', 'dead_bush'}:
            support = (blockX, blockY + 1, blockZ) if dict(block.properties).get('hanging') == 'true' else (blockX, blockY - 1, blockZ)
            assert name(support) not in {'air', 'water'}, (material, position)
        if material == 'water':
            assert name((blockX, blockY - 1, blockZ)) not in {'air', 'water'}
            for deltaX, deltaZ in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
                assert name((blockX + deltaX, blockY, blockZ + deltaZ)) in {'water', 'cut_sandstone'}
    materials = collections.Counter(block.name for block in blocks.values() if block.name != 'minecraft:air')
    assert not any(materials.get('minecraft:' + material) for material in ['spawner', 'command_block', 'structure_block', 'jigsaw', 'lava', 'fire'])
    if metadata['roof'] == 'enclosed':
        for position in blocks:
            if position[1] == 0 and name(position) != 'air':
                assert name((position[0], structure.size[1] - 1, position[2])) != 'air'
    return {'status': 'PASS', 'reachable_floor_positions': len(reached), 'all_connectors_reachable': True, 'supported_decorations': True, 'sealed_water_basins': True, 'roof_check': 'PASS', 'materials': dict(materials), 'scope': 'Static geometry checks; native registry and collision validation separate; no world placement claimed.'}


def main():
    authority = prepare()
    modules = []
    for index, (kind, title, size, directions) in enumerate(SPECS):
        size = tuple(authority[index]['output_size'])
        module_id = f'NAT1-G2-{index + 1:02}'
        target = ROOT / 'incoming' / module_id
        target.mkdir(parents=True, exist_ok=True)
        environment = dict(os.environ, FT_GROUP2_INDEX=str(index))
        result = subprocess.run(['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'recipe.mjs'), '--out-dir', str(target), '--name', 'room', '--data-version', '3465'], env=environment, cwd=SKILL, capture_output=True, text=True, encoding='utf-8')
        (target / 'build.log').write_text(result.stdout + result.stderr, encoding='utf-8')
        result.check_returncode()
        centerX, centerZ = size[0] // 2, size[2] // 2
        connections = []
        for direction in directions:
            position = [0 if direction == 'west' else size[0] - 1 if direction == 'east' else centerX, 1, 0 if direction == 'north' else size[2] - 1 if direction == 'south' else centerZ]
            connections.append({'facing': direction, 'position': position, 'width': 3, 'height': 3, 'origin': 'local XYZ; feet coordinates', 'outward_normal': {'north': [0, 0, -1], 'south': [0, 0, 1], 'west': [-1, 0, 0], 'east': [1, 0, 0]}[direction]})
        reference = REFERENCES / f'S2-{index + 1:02}-{kind}.png'
        metadata = {'id': module_id, 'name': title, 'size': size, 'entrance': [centerX, 1, 0], 'interior': [centerX, 1, 2], 'facing': 'north', 'connections': connections, 'exits': connections[1:], 'sha256': digest(target / 'room.nbt'), 'reference': str(reference), 'reference_sha256': digest(reference), 'roof': 'open-courtyard' if kind == 'courtyard' else 'enclosed', 'loot': 'none; empty containers only', 'deployment': 'not deployed; slot unassigned', 'design_deviations': ['Concept cutaway is preview-only; enclosed roof retained except courtyard.', 'Vanilla flower pots replace concept-only cracked urns.', 'Courtyard uses a sealed still-water basin instead of flowing water.']}
        save(target / 'room.json', metadata)
        metadata['authority'] = authority[index]
        save(target / 'room.json', metadata)
        structure = views.Structure.read(target / 'room.nbt')
        assert tuple(structure.size) == size
        report = static_review(structure, metadata)
        save(target / 'qa.json', {**report, 'sha256': metadata['sha256'], 'native_world_placement': 'NOT_RUN'})
        save(target / 'materials.json', report['materials'])
        for exterior in (False, True):
            sheet = views.Image.new('RGB', (1800, 1400), '#101a20')
            for view_index, (direction, label, camera) in enumerate(views.VIEWS):
                image = views.render(structure, metadata, camera, label, exterior, size=(900, 700))
                image.save(target / f'{"exterior" if exterior else "interior"}-{direction}.png')
                sheet.paste(image, ((view_index % 2) * 900, (view_index // 2) * 700))
                if not exterior and view_index == 0:
                    image.save(target / 'preview.png')
            sheet.save(target / f'{"exterior" if exterior else "interior"}-four-views.png')
        modules.append(metadata)
        print('BUILT_AND_REVIEWED ' + module_id, flush=True)
    layout = {'concept': str(REFERENCES / 'S2-10-overview.png'), 'concept_sha256': digest(REFERENCES / 'S2-10-overview.png'), 'purpose': 'Layout reference only, not a tenth room', 'placement': 'unassigned; central integration owns world origins and routes', 'suggested_route': ['07 entrance', '05 corridor', '04 great-hall', '06 corner', '08 courtyard'], 'branches': ['01 large-chamber', '02 medium-room', '03 small-room', '09 treasure']}
    save(ROOT / 'layout-reference.json', layout)
    save(ROOT / 'manifest.json', {'group': 'nature-batch1-group2-sandstone', 'thread': '01a1a5df-15dc-7fb3-b4f7-5b6c50b1a51b', 'image_generation_calls': 0, 'partial_images_created': False, 'modules': modules, 'layout': layout, 'deployment': 'not deployed', 'native_world_placement': 'NOT_RUN'})


if __name__ == '__main__':
    main()
