import hashlib
import json
from collections import Counter, deque
from pathlib import Path

from PIL import Image

from nbtio import Structure


ROOT = Path(__file__).resolve().parents[1]
AIR = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
FORBIDDEN = {'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'cactus', 'powder_snow', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'light', 'respawn_anchor'}


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def require(condition, message):
    if not condition:
        raise AssertionError(message)


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    room = Structure.read(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    blueprint = json.loads((ROOT / 'blueprint.json').read_text(encoding='utf-8'))
    require(room.size == (15, 10, 16) and metadata['size'] == list(room.size), 'Incorrect short dimensions')
    require(room.data_version == 3465, 'Incorrect Minecraft data version')
    require(len(room.blocks) == 2400, 'All air/solid positions must be explicit')
    require(digest(ROOT / 'room.nbt') == metadata['sha256'], 'Metadata fingerprint mismatch')
    require(digest(metadata['reference']) == metadata['reference_sha256'], 'Reference fingerprint mismatch')
    with Image.open(metadata['reference']) as reference:
        require(reference.size == (1536, 1024), 'Wrong reference dimensions')
        reference.load()
    require(not room.entities and not room.block_entities, 'No entities or inventory-bearing block entities allowed')
    for position, block in room.blocks.items():
        require(all(0 <= coordinate < limit for coordinate, limit in zip(position, room.size)), 'Out of bounds')
        require(block.name.startswith('minecraft:'), 'Vanilla palette only')
        require(block.name.split(':')[1] not in FORBIDDEN and not block.name.endswith('_bed'), f'Forbidden block {block.name}')
        require(dict(block.properties).get('waterlogged', 'false') == 'false', 'Unexpected waterlogged state')
        if block.name == 'minecraft:cobweb':
            require(position[0] in (1, 13) and position[1] == 6, 'Cobweb outside closed cell upper corner')
    for pos_x in range(15):
        for pos_z in range(16):
            require(room.get((pos_x, 9, pos_z)).name not in AIR, 'Roof hole')
            require(room.get((pos_x, 0, pos_z)).name not in AIR, 'Floor hole')
    clear = {tuple(position) for position in blueprint['clear_passage']}
    require(len(clear) == 144, 'Incorrect main-passage volume')
    require(all(room.get(position).name in AIR for position in clear), 'Main passage obstructed')
    for pos_x in range(6, 9):
        for pos_z in range(16):
            require(room.get((pos_x, 0, pos_z)).name.endswith('stone_bricks'), 'Unsafe floor under main passage')
    visited = {(7, 1, 0)}
    queue = deque(visited)
    while queue:
        pos_x, pos_y, pos_z = queue.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            candidate = (pos_x + offset_x, pos_y, pos_z + offset_z)
            if candidate in clear and candidate not in visited:
                visited.add(candidate)
                queue.append(candidate)
    require((7, 1, 15) in visited and len(visited) == 48, 'Disconnected through-route')
    for safe in blueprint['safe_points']:
        pos_x, pos_y, pos_z = safe
        require(all(room.get((pos_x + offset_x, pos_y + offset_y, pos_z + offset_z)).name in AIR for offset_x in (-1, 0, 1) for offset_y in (0, 1, 2) for offset_z in (-1, 0, 1)), 'Unsafe 3x3x3 landing')
    for beam in blueprint['beams']:
        for pos_x in range(beam['from'][0], beam['to'][0] + 1):
            require(room.get((pos_x, 7, beam['from'][2])).name == 'minecraft:stripped_dark_oak_log', 'Broken beam')
        for pos_x, unused_y, pos_z in beam['supports']:
            require(all(room.get((pos_x, pos_y, pos_z)).name.endswith('stone_bricks') for pos_y in range(7)), 'Beam pillar interrupted')
    for lamp in blueprint['lamps']:
        require(room.get(tuple(lamp['lamp'])).name == 'minecraft:soul_lantern', 'Missing lamp')
        require(all(room.get(tuple(position)).name == 'minecraft:chain' for position in lamp['chain']), 'Missing lamp suspension')
        require(room.get(tuple(lamp['anchor'])).name == 'minecraft:stripped_dark_oak_log', 'Unanchored lamp')
    for cage in blueprint['cages']:
        require(cage['bounds'][0][1] >= 4, 'Cage inside minimum headroom')
        for support in cage['suspension']:
            for key in ('base', 'cap'):
                require(room.get(tuple(support[key])).name == 'minecraft:polished_deepslate', 'Cage cap/base missing')
            require(room.get(tuple(support['chain'])).name == 'minecraft:chain', 'Cage suspension missing')
            require(room.get(tuple(support['anchor'])).name.endswith('stone_bricks'), 'Cage roof anchor missing')
            require(all(room.get(tuple(position)).name == 'minecraft:iron_bars' for position in support['sides']), 'Cage disconnected bar column')
    for cell in blueprint['cells']:
        pos_x, pos_y, pos_z = cell['door']
        for offset_y, half in ((0, 'lower'), (1, 'upper')):
            door = room.get((pos_x, pos_y + offset_y, pos_z))
            require(door.name == 'minecraft:iron_door' and dict(door.properties)['half'] == half and dict(door.properties)['open'] == 'false', 'Invalid decorative cell door')
        pos_x, pos_y, pos_z = cell['bunk']
        require(all(room.get((pos_x + offset_x, pos_y, pos_z + offset_z)).name == 'minecraft:dark_oak_planks' for offset_x in (0, 1) for offset_z in (0, 1)), 'Bedding platform missing')
    solids = {position for position, block in room.blocks.items() if block.name not in AIR}
    connected = {(0, 0, 0)}
    queue = deque(connected)
    while queue:
        current = queue.popleft()
        for offset in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            candidate = tuple(current[axis] + offset[axis] for axis in range(3))
            if candidate in solids and candidate not in connected:
                connected.add(candidate)
                queue.append(candidate)
    require(connected == solids, 'Disconnected floating block component')
    roundtrip = ROOT / 'qa/python-roundtrip.nbt'
    room.write(roundtrip)
    require(Structure.read(roundtrip).blocks == room.blocks, 'NBT roundtrip mismatch')
    report = {
        'status': 'PASS', 'structure_sha256': metadata['sha256'], 'reference_sha256': metadata['reference_sha256'],
        'size': list(room.size), 'records': len(room.blocks), 'palette_states': len(set(room.blocks.values())),
        'main_route': {'clear_air_cells': 144, 'reachable_floor_cells': 48, 'north_to_south': True, 'width': 3, 'height': 3},
        'roof_complete_blocks': 240, 'floor_complete_blocks': 240, 'safe_3x3x3_points': blueprint['safe_points'],
        'supported_lamps': len(blueprint['lamps']), 'supported_cages': len(blueprint['cages']), 'suspension_chains': 12,
        'cells': len(blueprint['cells']), 'containers': 0, 'items': 0, 'entities': 0, 'liquids': 0,
        'solid_connectivity': {'connected': len(connected), 'total': len(solids)},
        'limitations': ['Static support graph does not substitute native collision, rotation, canSurvive, or ticking checks.']
    }
    write_json(ROOT / 'qa/static.json', report)
    write_json(ROOT / 'qa/materials.json', dict(sorted(Counter(block.name for block in room.blocks.values()).items())))
    print(json.dumps({'status': 'PASS', 'records': len(room.blocks), 'sha256': metadata['sha256']}))


if __name__ == '__main__':
    main()
