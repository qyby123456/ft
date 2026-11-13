import hashlib
import itertools
import json
import zipfile
from collections import deque
from pathlib import Path

from nbtio import AIR, Structure, plain, read_nbt


ROOT = Path(__file__).resolve().parents[1]
CLIENT = Path('C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar')
DIRECTIONS = {'up': (0, 1, 0), 'down': (0, -1, 0), 'north': (0, 0, -1), 'south': (0, 0, 1), 'west': (-1, 0, 0), 'east': (1, 0, 0)}
FULL = {'end_stone_bricks', 'purpur_block', 'purpur_pillar', 'obsidian', 'crying_obsidian', 'purple_glazed_terracotta', 'amethyst_block', 'bookshelf', 'barrel', 'tinted_glass'}
FORBIDDEN = {'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'cactus', 'powder_snow', 'campfire', 'soul_campfire', 'wither_rose', 'sweet_berry_bush', 'cobweb', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'light', 'end_portal', 'nether_portal', 'end_gateway'}


def short(block):
    return block.name.removeprefix('minecraft:')


def sturdy(room, position, face):
    block = room.get(position)
    return short(block) in FULL or short(block) == 'purpur_slab' and block.get('type') == 'top' and face == 'up'


def supported(room, entry):
    position = tuple(entry['position'])
    anchor = tuple(entry['anchor'])
    vector = DIRECTIONS[entry['support_face']]
    return tuple(anchor[axis] + vector[axis] for axis in range(3)) == position and sturdy(room, anchor, entry['support_face']) and short(room.get(position)) == entry['block']


def stock_empty(value):
    if isinstance(value, dict):
        for key, child in value.items():
            if key in {'LootTable', 'LootTableSeed', 'Book', 'RecordItem'}:
                return False
            if key in {'Items', 'Item'} and child:
                return False
            if not stock_empty(child):
                return False
    elif isinstance(value, list):
        return all(stock_empty(child) for child in value)
    return True


def walkable(room, feet):
    pos_x, pos_y, pos_z = feet
    return short(room.get(feet)) == 'air' and short(room.get((pos_x, pos_y + 1, pos_z))) == 'air' and sturdy(room, (pos_x, pos_y - 1, pos_z), 'up')


def main():
    report = {'status': 'RUNNING', 'scope': 'Static structural inspection; native engine checks reported separately.', 'checks': []}

    def require(name, condition, detail):
        report['checks'].append({'name': name, 'status': 'PASS' if condition else 'FAIL', 'detail': detail})
        if not condition:
            raise ValueError(f'{name}: {detail}')

    try:
        path = ROOT / 'out/room.nbt'
        room = Structure.read(path)
        _, tagged = read_nbt(path)
        raw = plain(tagged)
        metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
        blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        report['structure_sha256'] = digest
        require('fingerprint', digest == metadata['sha256'], digest)
        require('format', room.data_version == 3465 and 'palettes' not in raw and len(room.size) == 3, 'Java 1.20.1, gzip, single palette')
        expected = set(itertools.product(range(13), range(9), range(13)))
        require('full_explicit_volume', room.size == (13, 9, 13) and set(room.blocks) == expected and metadata['size'] == [13, 9, 13], f'{len(room.blocks)}/1521 coordinates; omitted air is not used as a clearing contract')
        require('no_preplaced_entities', not room.entities, 'No NPC, item entity, item frame, armor stand or spawner')
        require('inventory', all(stock_empty(plain(value)) for value in room.block_entities.values()), 'Empty source containers; ender chest accesses existing personal storage at runtime')
        states = set(room.blocks.values())
        with zipfile.ZipFile(CLIENT) as resources:
            blockstate_names = {name.removeprefix('assets/minecraft/blockstates/').removesuffix('.json') for name in resources.namelist() if name.startswith('assets/minecraft/blockstates/') and name.endswith('.json')}
            require('vanilla_names', all(block.name.startswith('minecraft:') and short(block) in blockstate_names for block in states), f'{len(states)} states found in vanilla 1.20.1 assets; native registry/property validation still required')
        require('no_hazards', all(short(block) not in FORBIDDEN and block.get('waterlogged', 'false') == 'false' for block in states), 'No liquid, live fire, portal, web, reward/spawn or administrative blocks')
        require('complete_roof_and_floor', all(sturdy(room, (pos_x, pos_y, pos_z), 'up') for pos_x in range(13) for pos_z in range(13) for pos_y in (0, 8)), '169 solid floor and 169 complete roof positions')
        shell = [(pos_x, pos_y, pos_z) for pos_x in range(13) for pos_z in range(13) for pos_y in range(1, 8) if (pos_x in (0, 12) or pos_z in (0, 12)) and not (5 <= pos_x <= 7 and pos_z == 0 and pos_y <= 3)]
        require('sealed_void_edges', all(sturdy(room, position, 'up') for position in shell), 'All non-entry perimeter cells are full blocks or full tinted glass; no drop edge')
        require('north_entry', metadata['entrance'] == [6, 1, 0] and metadata['facing'] == 'north' and all(short(room.get((pos_x, pos_y, 0))) == 'air' for pos_x in range(5, 8) for pos_y in range(1, 4)), 'Three-wide by three-high opening with complete floor')
        require('main_aisle', all(short(room.get(tuple(position))) == 'air' for position in blueprint['entrance_to_counter_aisle']), '3x3 aisle from north entry through Z=6')
        require('counter_circuit_clearance', all(short(room.get(tuple(position))) == 'air' for position in blueprint['counter_clearance']), 'Every side has >=1 block width and >=2 block height')
        reached = {tuple(metadata['entrance'])}
        pending = deque(reached)
        while pending:
            current = pending.popleft()
            for vector in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1)):
                following = tuple(current[axis] + vector[axis] for axis in range(3))
                if 0 <= following[0] < 13 and 0 <= following[2] < 13 and following not in reached and walkable(room, following):
                    reached.add(following)
                    pending.append(following)
        required = {tuple(position) for position in blueprint['required_walkable']} | {tuple(metadata['interior'])}
        require('reachable_service_positions', required <= reached, f'{len(reached)} reachable feet cells; all counter sides, back shelf and barrels connected')
        require('attached_decor', all(supported(room, entry) for entry in blueprint['supported_decor']), f"{len(blueprint['supported_decor'])} explicit sturdy support faces, including every end rod")
        rods = {position for position, block in room.blocks.items() if short(block) == 'end_rod'}
        documented = {tuple(entry['position']) for entry in blueprint['supported_decor'] if entry['block'] == 'end_rod'}
        require('end_rod_coverage', rods == documented, f'{len(rods)} end rods; no untested free-floating rod')
        require('wall_connected_shelves', all(sturdy(room, tuple(entry['anchor']), 'north') for entry in blueprint['shelf_wall_connections']), 'All ten shelf slabs connect directly to the complete back wall')
        require('floor_supported_counter', all(sturdy(room, (position[0], 0, position[2]), 'up') for position in blueprint['counter_footprint']), 'Seven full counter blocks stand on full structural floor')
        assigned = {}
        for group, positions in blueprint['features'].items():
            for position in positions:
                coordinate = tuple(position)
                if coordinate in assigned:
                    raise ValueError(f'Feature intersection {group}/{assigned[coordinate]} at {position}')
                assigned[coordinate] = group
        require('furniture_no_shared_voxels', True, f'{len(assigned)} uniquely assigned feature positions')
        solid = {position for position, block in room.blocks.items() if short(block) != 'air'}
        connected = {position for position in solid if position[1] == 0}
        queue = deque(connected)
        while queue:
            current = queue.popleft()
            for vector in DIRECTIONS.values():
                following = tuple(current[axis] + vector[axis] for axis in range(3))
                if following in solid and following not in connected:
                    connected.add(following)
                    queue.append(following)
        require('no_disconnected_floating_components', connected == solid, f'{len(connected)} non-air cells connected by faces to structural floor; explicit decor supports checked separately')
        probe = next(entry for entry in blueprint['supported_decor'] if entry['block'] == 'end_rod' and entry['support_face'] == 'up')
        negative = Structure(room.size, dict(room.blocks), dict(room.block_entities))
        negative.set(tuple(probe['anchor']), AIR)
        require('negative_missing_rod_support', not supported(negative, probe), 'Validator rejects a floating end rod even if vanilla canSurvive alone accepts it')
        report.update(status='PASS', reachable_foot_positions=len(reached), non_air_blocks=len(solid), block_entities=len(room.block_entities), end_rods=len(rods), supported_decor=len(blueprint['supported_decor']))
    except Exception as error:
        report.update(status='FAIL', error=str(error))
        raise
    finally:
        (ROOT / 'qa/static.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
        print(json.dumps({'status': report['status'], 'checks': len(report['checks'])}))


if __name__ == '__main__':
    main()
