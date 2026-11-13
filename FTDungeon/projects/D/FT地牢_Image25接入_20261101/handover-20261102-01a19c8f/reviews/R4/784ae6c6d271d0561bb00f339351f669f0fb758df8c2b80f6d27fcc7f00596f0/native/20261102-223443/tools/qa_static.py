import copy
import json
from collections import deque

from build_room import REFERENCE, REFERENCE_HASH, ROOT, SIZE, digest, write_json
from nbtio import AIR, Structure, Tag, compound, plain, read_nbt, state, string


NON_SOLID = {'air', 'water', 'lantern', 'pointed_dripstone', 'cave_vines', 'cave_vines_plant', 'warped_fungus', 'glow_lichen', 'amethyst_cluster', 'moss_carpet', 'spruce_fence'}
PASSABLE = {'air', 'cave_vines', 'cave_vines_plant', 'warped_fungus', 'glow_lichen'}
FORBIDDEN = {'lava', 'fire', 'soul_fire', 'campfire', 'soul_campfire', 'magma_block', 'powder_snow', 'cobweb', 'spawner', 'structure_block', 'jigsaw', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_void', 'light'}


def require(condition, message):
    if not condition:
        raise ValueError(message)


def name(block):
    return block.name.removeprefix('minecraft:')


def solid(room, position):
    return name(room.get(position)) not in NON_SOLID


def clear(room, position):
    return name(room.get(position)) in PASSABLE


def walkable(room, feet):
    pos_x, pos_y, pos_z = feet
    return clear(room, feet) and clear(room, (pos_x, pos_y + 1, pos_z)) and solid(room, (pos_x, pos_y - 1, pos_z))


def allowed_water(position):
    pos_x, pos_y, pos_z = position
    return 3 <= pos_x <= 13 and 1 <= pos_y <= 2 and 3 <= pos_z <= 14 or pos_x == 8 and 3 <= pos_y <= 7 and 11 <= pos_z <= 17


def empty_stock(data):
    if isinstance(data, dict):
        for key, value in data.items():
            if key in ('LootTable', 'LootTableSeed', 'Book', 'RecordItem') or key == 'Items' and value:
                return False
            if not empty_stock(value):
                return False
    if isinstance(data, list):
        return all(empty_stock(value) for value in data)
    return True


def validate(room, metadata, blueprint):
    require(room.size == SIZE and list(SIZE) == metadata['size'], 'Dimension mismatch')
    require(room.data_version == 3465 and not room.entities, 'Wrong version/entities')
    require(len(room.blocks) == 17 * 13 * 19, 'Incomplete explicit volume')
    for position, block in room.blocks.items():
        require(all(0 <= value < limit for value, limit in zip(position, SIZE)), 'Out of bounds coordinate')
        require(block.name.startswith('minecraft:') and name(block) not in FORBIDDEN, 'Nonvanilla/forbidden block')
        require(block.get('waterlogged', 'false') == 'false', 'Waterlogged block outside explicit water model')
        if name(block) == 'water':
            require(allowed_water(position), f'Water outside contained envelope: {position}')
            require(0 <= int(block.get('level')) <= 15, 'Invalid water level')
    require(all(empty_stock(plain(entity)) for entity in room.block_entities.values()), 'Stock or loot found')
    require(set(room.block_entities) == {position for position, block in room.blocks.items() if name(block) == 'barrel'}, 'Barrel entity mismatch')
    for pos_x in range(17):
        for pos_z in range(19):
            require(solid(room, (pos_x, 0, pos_z)), 'Perforated basin foundation')
            require(solid(room, (pos_x, 12, pos_z)), 'Incomplete roof')
    for pos_x in range(17):
        for pos_y in range(1, 12):
            for pos_z in range(19):
                if pos_x in (0, 16) or pos_z in (0, 18):
                    opening = pos_z == 0 and 7 <= pos_x <= 9 and 3 <= pos_y <= 5
                    require(clear(room, (pos_x, pos_y, pos_z)) if opening else solid(room, (pos_x, pos_y, pos_z)), 'Shell or entrance damaged')
    for group in ('main_clearance', 'bridge_clearance'):
        require(all(clear(room, tuple(position)) for position in blueprint[group]), 'Dry 3x3 clearance obstructed')
    for pos_x in (7, 8, 9):
        require(solid(room, (pos_x, 2, 0)), 'Entrance floor not full')
    for pos_x, pos_z in ((2, 4), (14, 4), (2, 15), (14, 15)):
        require(all(solid(room, (pos_x, pos_y, pos_z)) for pos_y in range(13)), 'Unsupported/discontinuous column')
    for pos_x in (4, 8, 12):
        for pos_z in (7, 9):
            require(all(solid(room, (pos_x, pos_y, pos_z)) for pos_y in (0, 1, 2)), 'Missing bridge pier')
    for position, block in room.blocks.items():
        pos_x, pos_y, pos_z = position
        block_name = name(block)
        if block_name == 'lantern':
            require(block.get('hanging') == 'true' and solid(room, (pos_x, pos_y + 1, pos_z)), 'Unsupported lantern')
        if block_name in ('moss_carpet', 'warped_fungus'):
            require(solid(room, (pos_x, pos_y - 1, pos_z)), 'Unsupported plant/carpet')
        if block_name == 'amethyst_cluster':
            require(block.get('facing') == 'up' and solid(room, (pos_x, pos_y - 1, pos_z)), 'Unsupported crystal')
        if block_name in ('cave_vines', 'cave_vines_plant'):
            above = (pos_x, pos_y + 1, pos_z)
            require(solid(room, above) or name(room.get(above)) in ('cave_vines', 'cave_vines_plant'), 'Unsupported hanging vine')
        if block_name == 'pointed_dripstone':
            direction = -1 if block.get('vertical_direction') == 'up' else 1
            anchor = (pos_x, pos_y + direction, pos_z)
            require(solid(room, anchor) or name(room.get(anchor)) == 'pointed_dripstone', 'Unsupported dripstone')
        if block_name == 'glow_lichen':
            attached = False
            for face, step in {'west': (-1, 0, 0), 'east': (1, 0, 0), 'north': (0, 0, -1), 'south': (0, 0, 1), 'up': (0, 1, 0), 'down': (0, -1, 0)}.items():
                if block.get(face) == 'true':
                    attached |= solid(room, tuple(value + delta for value, delta in zip(position, step)))
            require(attached, 'Unsupported wall lichen')
    start = tuple(metadata['entrance'])
    require(start == (8, 3, 0) and walkable(room, start), 'Invalid north entrance')
    reached = {start}
    queue = deque([start])
    while queue:
        current_x, current_y, current_z = queue.popleft()
        for step_x, step_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            next_position = (current_x + step_x, current_y, current_z + step_z)
            if 0 <= next_position[0] < 17 and 0 <= next_position[2] < 19 and next_position not in reached and walkable(room, next_position):
                reached.add(next_position)
                queue.append(next_position)
    require(tuple(metadata['interior']) in reached, 'Safety point unreachable')
    require(all(tuple(position) in reached for position in blueprint['required_feet']), 'Dry bridge/bank disconnected')
    water = {position for position, block in room.blocks.items() if name(block) == 'water'}
    water_reached = {tuple(blueprint['water_source'])}
    queue = deque(water_reached)
    while queue:
        current = queue.popleft()
        for step in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            neighbor = tuple(value + delta for value, delta in zip(current, step))
            if neighbor in water and neighbor not in water_reached:
                water_reached.add(neighbor)
                queue.append(neighbor)
    require(water_reached == water, 'Spring and pools not connected beneath bridge')
    return {'records': len(room.blocks), 'non_air': sum(block != AIR for block in room.blocks.values()), 'water_blocks': len(water), 'connected_water_blocks': len(water_reached), 'empty_barrels': len(room.block_entities), 'reachable_dry_feet': len(reached)}


def main():
    room = Structure.read(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    blueprint = json.loads((ROOT / 'blueprint.json').read_text(encoding='utf-8'))
    require(digest(REFERENCE) == REFERENCE_HASH, 'Reference hash mismatch')
    require(digest(ROOT / 'room.nbt') == metadata['sha256'], 'Source hash mismatch')
    _, root = read_nbt(ROOT / 'room.nbt')
    require('palette' in root.value and 'palettes' not in root.value, 'Not single palette')
    result = validate(room, metadata, blueprint)
    negatives = {}
    mutations = {
        'blocked_main_route': lambda candidate: candidate.set((8, 3, 2), state('stone')),
        'perforated_pool': lambda candidate: candidate.set((4, 0, 4), AIR),
        'missing_roof': lambda candidate: candidate.set((6, 12, 6), AIR),
        'water_on_route': lambda candidate: candidate.set((8, 3, 3), state('water', level=0)),
        'missing_lamp_anchor': lambda candidate: candidate.set((3, 6, 6), AIR),
        'broken_column': lambda candidate: candidate.set((2, 5, 4), AIR),
        'missing_bridge_pier': lambda candidate: candidate.set((8, 1, 7), state('water', level=0)),
        'stocked_barrel': lambda candidate: candidate.block_entities.__setitem__((3, 3, 16), compound(id=string('minecraft:barrel'), Items=Tag(9, [compound(id=string('minecraft:diamond'), Count=Tag(1, 1))], 10))),
    }
    for label, mutation in mutations.items():
        candidate = copy.deepcopy(room)
        mutation(candidate)
        try:
            validate(candidate, metadata, blueprint)
        except ValueError as error:
            negatives[label] = {'status': 'REJECTED', 'reason': str(error)}
        else:
            raise ValueError('Negative probe not rejected: ' + label)
    write_json(ROOT / 'qa/static.json', {
        'status': 'PASS', 'source_nbt_sha256': metadata['sha256'], 'checks': result,
        'passed': ['complete explicit gzip single-palette NBT', 'source/reference fingerprints', 'sealed roof/shell/basin foundation', 'north 3x3 entrance and full floor', 'dry 3x3 T-route and bank connectivity', 'four full-height supported columns', 'six bridge piers', 'lamps/plants/dripstone/crystal support', 'three empty barrels, no inventory/loot/entities', 'bounded liquid envelope'],
        'negative_probes': negatives, 'limitations': ['Static material/support heuristic; native collision, canSurvive and scheduled fluid tick tests are separate.'],
    })
    print(json.dumps({'status': 'STATIC_PASS', **result, 'negative_probes_rejected': len(negatives)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
