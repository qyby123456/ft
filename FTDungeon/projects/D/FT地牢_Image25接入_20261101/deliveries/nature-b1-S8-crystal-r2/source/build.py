import collections
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
SHARED = ROOT.parents[1] / 'tools'
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S8-crystal-geode/out')
sys.path.insert(0, 'D:/ft工程文件/血棘祭炉祭坛厅地牢_20261101/tools')
from structure_io import load_structure


def save(target, value):
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def digest(target):
    return hashlib.sha256(target.read_bytes()).hexdigest()


def build():
    (ROOT / 'source/metadata').mkdir(parents=True, exist_ok=True)
    for number in range(1, 10):
        room_id = f'N8_{number:02}'
        folder = ROOT / 'incoming' / room_id
        environment = {**os.environ, 'S8_ROOM_INDEX': str(number), 'S8_METADATA_DIR': str(ROOT / 'source/metadata')}
        result = subprocess.run(['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'source/crystal.mjs'), '--out-dir', str(folder), '--name', 'room', '--data-version', '3465'], cwd=SKILL, env=environment, capture_output=True, text=True, encoding='utf-8')
        print(result.stdout, flush=True)
        if result.returncode:
            raise RuntimeError(result.stderr)
        metadata = json.loads((ROOT / 'source/metadata' / (room_id + '.json')).read_text(encoding='utf-8'))
        reference = next(REFERENCE.glob(f'S8-{number:02}-*.png'))
        metadata.update(sha256=digest(folder / 'room.nbt'), reference=str(reference), reference_sha256=digest(reference), proposed_id=True, deployment='NOT_DEPLOYED', container_policy='empty; no loot table', coordinate_system='+X east, +Y up, +Z south; positions are player feet')
        save(folder / 'room.json', metadata)


def validate():
    reports = []
    for folder in sorted((ROOT / 'incoming').iterdir()):
        metadata = json.loads((folder / 'room.json').read_text(encoding='utf-8'))
        template = load_structure(folder / 'room.nbt')
        size = template['size']
        palette = template['palette']
        records = template['blocks']
        states = {tuple(record['pos']): palette[record['state']] for record in records}
        checks = []

        def require(condition, label):
            if not condition:
                raise ValueError(f'{folder.name}: {label}')
            checks.append(label)

        def name(position):
            return states.get(tuple(position), {'Name': 'minecraft:air'})['Name'].removeprefix('minecraft:')

        def walkable(position):
            block_x, block_y, block_z = position
            return name(position) == 'air' and name((block_x, block_y + 1, block_z)) == 'air' and name((block_x, block_y - 1, block_z)) not in {'air', 'amethyst_cluster', 'water', 'lava'}

        require(template['DataVersion'] == 3465 and size == metadata['size'], '1.20.1 dimensions and DataVersion')
        require(len(states) == len(records), 'unique block positions')
        require(all(all(0 <= position[axis] < size[axis] for axis in range(3)) for position in states), 'all positions in bounds')
        require(not template.get('entities'), 'no spawned entities')
        require(metadata['sha256'] == digest(folder / 'room.nbt'), 'NBT SHA256')
        forbidden = {'lava', 'water', 'fire', 'soul_fire', 'magma_block', 'powder_snow', 'cobweb', 'spawner', 'command_block', 'structure_block', 'jigsaw'}
        require(not any(state['Name'].removeprefix('minecraft:') in forbidden for state in states.values()), 'no fluids, hazards or forbidden blocks')
        require(all(not record.get('nbt', {}).get('Items') and not record.get('nbt', {}).get('LootTable') for record in records), 'empty containers')
        reachable = {tuple(metadata['entrance'])}
        pending = collections.deque(reachable)
        while pending:
            block_x, block_y, block_z = pending.popleft()
            for delta_x, delta_z in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
                for delta_y in [0, -1, 1]:
                    target = (block_x + delta_x, block_y + delta_y, block_z + delta_z)
                    if not (0 <= target[0] < size[0] and 0 <= target[2] < size[2] and 1 <= target[1] < size[1] - 1):
                        continue
                    if delta_y and not (name((block_x, block_y - 1, block_z)).endswith('_stairs') or name((target[0], target[1] - 1, target[2])).endswith('_stairs')):
                        continue
                    if delta_y > 0 and name((block_x, block_y + 2, block_z)) != 'air':
                        continue
                    if delta_y < 0 and name((target[0], target[1] + 2, target[2])) != 'air':
                        continue
                    if target not in reachable and walkable(target):
                        reachable.add(target)
                        pending.append(target)
        require(tuple(metadata['interior']) in reachable, 'entrance to safe point')
        for target in metadata['route_targets']:
            require(tuple(target['position']) in reachable, 'route target ' + target['label'])
        for control in metadata['controls']:
            require(name(control['input']) == 'lever' and name(control['output']) == 'redstone_lamp', 'vanilla control components')
            require(control['input'] == [control['output'][0], control['output'][1] + 1, control['output'][2]], 'lever support and direct output adjacency')
        for connection in metadata['connections']:
            require(tuple(connection['position']) in reachable, 'reachable ' + connection['facing'] + ' connection')
            door_x, door_y, door_z = connection['position']
            for offset in [-1, 0, 1]:
                foot = (door_x + (0 if connection['facing'] == 'east' else offset), door_y, door_z + (offset if connection['facing'] == 'east' else 0))
                require(walkable(foot) and name((foot[0], door_y + 2, foot[2])) == 'air', 'dry supported 3x3 opening')
        for position, state in states.items():
            if state['Name'] == 'minecraft:amethyst_cluster':
                require(name((position[0], position[1] - 1, position[2])) == 'amethyst_block', 'crystal support')
        floor = [(block_x, block_z) for block_x in range(size[0]) for block_z in range(size[2]) if name((block_x, 0, block_z)) != 'air']
        if metadata['open_sky']:
            require(all(name((block_x, size[1] - 1, block_z)) == 'air' for block_x in range(5, size[0] - 5) for block_z in range(5, size[2] - 5)), 'declared courtyard aperture')
        else:
            require(all(name((block_x, size[1] - 1, block_z)) != 'air' for block_x, block_z in floor), 'complete roof over footprint')
        counts = collections.Counter(state['Name'] for state in states.values() if state['Name'] != 'minecraft:air')
        save(folder / 'materials.json', dict(sorted(counts.items())))
        report = {'id': folder.name, 'status': 'PASS', 'checks': sorted(set(checks)), 'reachable_floor_positions': len(reachable), 'reachable_height_levels': sorted({position[1] for position in reachable}), 'verified_route_targets': metadata['route_targets'], 'controls': metadata['controls'], 'mechanism_ticks': 'NOT_RUN', 'sha256': metadata['sha256'], 'nbt_blocks': len(records), 'solid_and_detail_blocks': sum(counts.values()), 'world_placement': 'NOT_RUN', 'gameplay': 'NOT_RUN'}
        save(folder / 'qa.json', report)
        reports.append(report)
    save(ROOT / 'qa/static-review.json', {'status': 'PASS', 'rooms': reports})


if __name__ == '__main__':
    build()
    validate()
