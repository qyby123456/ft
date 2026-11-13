import collections
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import subprocess
import sys

from PIL import Image

ROOT = Path(__file__).resolve().parent
SHARED = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S8-crystal-geode/out/S8-01-large-chamber.png')
FOLDER = ROOT / 'incoming/N8_IMG01'
sys.path.insert(0, 'D:/ft工程文件/血棘祭炉祭坛厅地牢_20261101/tools')
from structure_io import load_structure


def save(target, value):
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def digest(target):
    return hashlib.sha256(target.read_bytes()).hexdigest()


def module(name, target):
    specification = importlib.util.spec_from_file_location(name, target)
    result = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(result)
    return result


def build():
    with Image.open(REFERENCE) as image:
        image.verify()
    subprocess.run(['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'recipe.mjs'), '--out-dir', str(FOLDER), '--name', 'room', '--data-version', '3465'], cwd=SKILL, check=True)
    connections = [{'role': 'entrance', 'facing': 'north', 'position': [9, 1, 0]}, {'role': 'exit', 'facing': 'south', 'position': [9, 1, 18]}, {'role': 'exit', 'facing': 'east', 'position': [18, 1, 10]}, {'role': 'exit', 'facing': 'west', 'position': [0, 1, 10]}]
    for connection in connections:
        connection.update(width=3, height=3)
    targets = [{'label': 'left-ring', 'position': [4, 1, 9]}, {'label': 'right-ring', 'position': [14, 1, 9]}, {'label': 'rear-ring', 'position': [9, 1, 14]}, {'label': 'upper-dais-landing', 'position': [9, 4, 8]}, *[{'label': connection['facing'], 'position': connection['position']} for connection in connections]]
    save(FOLDER / 'room.json', {'id': 'N8_IMG01', 'name': '单晶核三向环廊大殿', 'size': [19, 12, 19], 'entrance': [9, 1, 0], 'interior': [9, 1, 2], 'facing': 'north', 'connections': connections, 'route_targets': targets, 'reference': str(REFERENCE), 'reference_sha256': digest(REFERENCE), 'sha256': digest(FOLDER / 'room.nbt'), 'analysis': str(ROOT / 'REFERENCE_ANALYSIS.md'), 'design_authority': 'single reference image; decompressed mod data compatibility-only', 'runtime_slot': None, 'deployment': 'NOT_DEPLOYED', 'mechanisms': [], 'encounters': 'not implemented', 'coordinates': '+X east; +Y up; +Z south; positions are feet'})


def static():
    metadata = json.loads((FOLDER / 'room.json').read_text(encoding='utf-8'))
    template = load_structure(FOLDER / 'room.nbt')
    states = {tuple(record['pos']): template['palette'][record['state']] for record in template['blocks']}
    checks = []

    def require(condition, label):
        if not condition:
            raise ValueError(label)
        checks.append(label)

    def name(position):
        return states.get(tuple(position), {'Name': 'minecraft:air'})['Name'].removeprefix('minecraft:')

    require(template['DataVersion'] == 3465 and template['size'] == [19, 12, 19], '1.20.1 size and DataVersion')
    require(len(states) == len(template['blocks']) == 19 * 12 * 19, 'unique full volume positions')
    require(all(all(0 <= position[axis] < template['size'][axis] for axis in range(3)) for position in states), 'positions in bounds')
    require(not template.get('entities'), 'no spawned entities')
    forbidden = {'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'powder_snow', 'cobweb', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw'}
    require(all(state['Name'].removeprefix('minecraft:') not in forbidden for state in states.values()), 'no hazards or forbidden blocks')
    require(all(name((block_x, 11, block_z)) == 'deepslate_tiles' for block_x in range(19) for block_z in range(19)), 'complete roof')
    require(all(name((block_x, 0, block_z)) != 'air' for block_x in range(19) for block_z in range(19)), 'complete dry foundation')
    for position, state in states.items():
        if state['Name'] == 'minecraft:amethyst_cluster':
            delta = 1 if state['Properties']['facing'] == 'down' else -1
            require(name((position[0], position[1] + delta, position[2])) == 'amethyst_block', 'directional crystal support')
        if state['Name'] == 'minecraft:lantern':
            require(name((position[0], position[1] + 1, position[2])) == 'chain', 'hanging lantern support')
        if state['Name'] == 'minecraft:chain':
            require(name((position[0], position[1] + 1, position[2])) in {'chain', 'deepslate_bricks', 'deepslate_tiles'}, 'chain support to roof')
    require(not any(record.get('nbt', {}).get('Items') or record.get('nbt', {}).get('LootTable') for record in template['blocks']), 'no loot')
    counts = collections.Counter(state['Name'] for state in states.values() if state['Name'] != 'minecraft:air')
    save(FOLDER / 'materials.json', dict(sorted(counts.items())))
    save(FOLDER / 'qa.json', {'static': 'PASS', 'sha256': digest(FOLDER / 'room.nbt'), 'checks': sorted(set(checks)), 'world_placement': 'NOT_RUN', 'gameplay': 'NOT_RUN'})


def native():
    runner = module('native', SHARED / 'run_native_review.py')
    original = (SHARED / 'VerifyDeliveries.java').read_text(encoding='utf-8')
    old_ids = '"L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3"'
    assert old_ids in original
    source = original.replace(old_ids, '"N8_IMG01"')
    line = '        int reachable = checkPath(stateMap, size, coordinate(metadata, "entrance"), coordinate(metadata, "interior"));'
    assert line in source
    source = source.replace(line, line + '\n        for (com.google.gson.JsonElement element : metadata.getAsJsonArray("route_targets")) {\n            checkPath(stateMap, size, coordinate(metadata, "entrance"), coordinate(element.getAsJsonObject(), "position"));\n        }')
    (ROOT / 'tools').mkdir(exist_ok=True)
    (ROOT / 'tools/VerifyDeliveries.java').write_text(source, encoding='utf-8')
    runner.ROOT = ROOT
    sys.argv = ['native', '--root', str(ROOT)]
    runner.main()


def render():
    renderer = module('fourview', SHARED / 'review_four_views.py')
    renderer.REVIEW = ROOT / 'views'
    sys.argv = ['render', '--rooms', 'N8_IMG01', '--revision', 'reference-v1', '--source-root', str(ROOT / 'incoming')]
    renderer.main()
    target = ROOT / 'views/reference-v1/N8_IMG01/interior-north.png'
    with Image.open(target) as image:
        image.save(FOLDER / 'preview.png')


if __name__ == '__main__':
    mode = sys.argv[1]
    {'build': build, 'static': static, 'native': native, 'render': render}[mode]()
