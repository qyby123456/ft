import collections
import hashlib
import importlib.util
import json
from pathlib import Path
import subprocess
import sys

from PIL import Image

ROOT = Path(__file__).resolve().parent
SHARED = ROOT.parents[1] / 'tools'
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S8-crystal-geode/out/S8-01-large-chamber.png')
ROOM_ID = 'S8R_IMG01'
ROOM = ROOT / 'incoming' / ROOM_ID
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import read_nbt, plain


def save(target, value):
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def digest(target):
    return hashlib.sha256(target.read_bytes()).hexdigest()


def load(target):
    return plain(read_nbt(target)[1])


def module(name, target):
    specification = importlib.util.spec_from_file_location(name, target)
    result = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(result)
    return result


def build():
    subprocess.run(['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'recipe.mjs'), '--out-dir', str(ROOM), '--name', 'room', '--data-version', '3465'], cwd=SKILL, check=True)
    connections = [{'role': 'entrance', 'facing': 'north', 'position': [11, 1, 0]}, {'role': 'exit', 'facing': 'south', 'position': [11, 1, 22]}, {'role': 'exit', 'facing': 'east', 'position': [22, 1, 12]}, {'role': 'exit', 'facing': 'west', 'position': [0, 1, 12]}]
    for connection in connections:
        connection.update(width=3, height=3)
    targets = [{'label': 'left-ring', 'position': [5, 1, 11]}, {'label': 'right-ring', 'position': [17, 1, 11]}, {'label': 'rear-ring', 'position': [11, 1, 17]}, {'label': 'dais-landing', 'position': [11, 4, 9]}, *[{'label': connection['facing'], 'position': connection['position']} for connection in connections]]
    save(ROOM / 'room.json', {'id': ROOM_ID, 'name': '单晶核紫晶环廊大殿', 'size': [23, 13, 23], 'entrance': [11, 1, 0], 'interior': [11, 1, 2], 'facing': 'north', 'connections': connections, 'route_targets': targets, 'reference': str(REFERENCE), 'reference_sha256': digest(REFERENCE), 'sha256': digest(ROOM / 'room.nbt'), 'reference_analysis': str(ROOT / 'REFERENCE_ANALYSIS.md'), 'coordinate_system': '+X east, +Y up, +Z south; coordinates are feet', 'status': 'AWAITING_REGISTRATION_ACCEPTANCE', 'runtime_slot': None, 'mechanisms': [], 'deployment': 'NOT_DEPLOYED'})


def static():
    data = load(ROOM / 'room.nbt')
    metadata = json.loads((ROOM / 'room.json').read_text(encoding='utf-8'))
    states = {tuple(record['pos']): data['palette'][record['state']] for record in data['blocks']}
    checks = []

    def require(condition, label):
        if not condition:
            raise ValueError(label)
        checks.append(label)

    def name(position):
        return states.get(tuple(position), {'Name': 'minecraft:air'})['Name'].removeprefix('minecraft:')

    require(data['DataVersion'] == 3465 and data['size'] == [23, 13, 23], 'size and 1.20.1 DataVersion')
    require(len(states) == len(data['blocks']) == 23 * 13 * 23, 'unique full-volume positions')
    require(all(all(0 <= position[axis] < data['size'][axis] for axis in range(3)) for position in states), 'bounds')
    require(not data.get('entities'), 'no spawned entities')
    forbidden = {'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'powder_snow', 'cobweb', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw'}
    require(all(state['Name'].removeprefix('minecraft:') not in forbidden for state in states.values()), 'no hazards or forbidden blocks')
    require(all(name((block_x, 12, block_z)) == 'deepslate_tiles' for block_x in range(23) for block_z in range(23)), 'complete roof')
    require(all(name((block_x, 0, block_z)) != 'air' for block_x in range(23) for block_z in range(23)), 'complete foundation')
    for connection in metadata['connections']:
        door_x, door_y, door_z = connection['position']
        side = connection['facing'] in {'east', 'west'}
        for offset in [-1, 0, 1]:
            block_x, block_z = door_x + (0 if side else offset), door_z + (offset if side else 0)
            require(name((block_x, door_y - 1, block_z)) != 'air' and all(name((block_x, block_y, block_z)) == 'air' for block_y in range(door_y, door_y + 3)), 'dry 3x3 ' + connection['facing'] + ' port')
    for position, state in states.items():
        if state['Name'] == 'minecraft:amethyst_cluster':
            delta = 1 if state['Properties']['facing'] == 'down' else -1
            require(name((position[0], position[1] + delta, position[2])) == 'amethyst_block', 'crystal support')
        if state['Name'] == 'minecraft:lantern':
            require(name((position[0], position[1] + 1, position[2])) == 'chain', 'lantern support')
        if state['Name'] == 'minecraft:chain':
            require(name((position[0], position[1] + 1, position[2])) in {'chain', 'deepslate_bricks', 'deepslate_tiles'}, 'chain to roof')
    counts = collections.Counter(state['Name'] for state in states.values() if state['Name'] != 'minecraft:air')
    save(ROOM / 'materials.json', dict(sorted(counts.items())))
    save(ROOT / 'qa/static.json', {'status': 'PASS', 'checks': sorted(set(checks)), 'nbt_sha256': metadata['sha256'], 'scope': 'static structural checks; collision path checked separately with native engine'})


if __name__ == '__main__':
    {'build': build, 'static': static}[sys.argv[1]]()
