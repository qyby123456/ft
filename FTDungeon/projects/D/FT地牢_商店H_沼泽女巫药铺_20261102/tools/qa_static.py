import copy
import json
from collections import Counter, deque

from build_room import PILLARS, REFERENCE, REFERENCE_HASH, ROOT, SIZE, build, digest, write_json
from nbtio import AIR, Structure, Tag, compound, plain, read_nbt, state, string


FORBIDDEN = {'lava', 'fire', 'soul_fire', 'campfire', 'soul_campfire', 'magma_block', 'powder_snow', 'cobweb', 'cactus', 'sweet_berry_bush', 'wither_rose', 'spawner', 'command_block', 'repeating_command_block', 'chain_command_block', 'structure_block', 'jigsaw', 'structure_void', 'light'}
ENTITY_TYPES = {'barrel': 'barrel', 'brewing_stand': 'brewing_stand', 'lectern': 'lectern', 'dark_oak_wall_sign': 'sign'}
PASSABLE = {'air', 'vine', 'hanging_roots'}


def require(condition, message):
    if not condition:
        raise ValueError(message)


def empty_stock(value):
    if isinstance(value, dict):
        for key, child in value.items():
            if key in {'LootTable', 'LootTableSeed', 'Book', 'RecordItem'}:
                return False
            if key in {'Items', 'Item'} and child != []:
                return False
            if key in {'Fuel', 'BrewTime'} and child != 0:
                return False
            if not empty_stock(child):
                return False
    if isinstance(value, list):
        return all(empty_stock(child) for child in value)
    return True


def short_name(block):
    return block.name.removeprefix('minecraft:')


def clear(room, position):
    return short_name(room.get(tuple(position))) in PASSABLE


def solid_top(block):
    name = short_name(block)
    if name in PASSABLE | {'water', 'lily_pad', 'sugar_cane', 'chain', 'lantern', 'iron_bars', 'dark_oak_fence', 'lectern', 'cauldron', 'water_cauldron', 'brewing_stand', 'dark_oak_wall_sign'} or name.startswith('potted_'):
        return False
    return not name.endswith('_slab') or block.get('type') == 'top'


def audit(room, metadata, blueprint):
    require(room.size == SIZE and metadata['size'] == list(SIZE), 'size mismatch')
    require(len(room.blocks) == 2250 and room.data_version == 3465, 'full explicit 1.20.1 volume required')
    require(not room.entities, 'preplaced entities forbidden')
    require(metadata['entrance'] == [7, 1, 0] and metadata['interior'] == [7, 1, 3], 'north interface mismatch')
    for position, block in room.blocks.items():
        require(all(0 <= coordinate < limit for coordinate, limit in zip(position, SIZE)), 'out of bounds')
        name = short_name(block)
        require(block.name.startswith('minecraft:') and name not in FORBIDDEN, 'forbidden or non-vanilla block')
        require(block.get('waterlogged', 'false') == 'false', 'unbounded waterlogged state')
        require((name in ENTITY_TYPES) == (position in room.block_entities), f'block entity mismatch {position}')
        if position in room.block_entities:
            payload = plain(room.block_entities[position])
            require(payload['id'] == 'minecraft:' + ENTITY_TYPES[name] and empty_stock(payload), 'inventory, fuel, loot or entity type invalid')
        if name == 'brewing_stand':
            require(all(block.get(f'has_bottle_{index}') == 'false' for index in range(3)), 'decorative stand contains bottle flags')
        if name.startswith('potted_') or name == 'brewing_stand':
            require(solid_top(room.get((position[0], position[1] - 1, position[2]))), f'unsupported tabletop specimen {position}')
        if name == 'vine':
            steps = {'west': (-1, 0, 0), 'east': (1, 0, 0), 'south': (0, 0, 1), 'north': (0, 0, -1)}
            require(any(block.get(face) == 'true' and solid_top(room.get(tuple(position[axis] + offset[axis] for axis in range(3)))) for face, offset in steps.items()), 'unattached vine')
        if name == 'hanging_roots':
            require(short_name(room.get((position[0], position[1] + 1, position[2]))) == 'rooted_dirt', 'unsupported herb roots')
    for pos_x in range(15):
        for pos_z in range(15):
            require(solid_top(room.get((pos_x, 0, pos_z))), 'floor hole')
            require(solid_top(room.get((pos_x, 9, pos_z))), 'roof hole')
    for pos_x, pos_z in PILLARS:
        require(all(short_name(room.get((pos_x, pos_y, pos_z))) in {'mossy_stone_bricks', 'mossy_cobblestone'} for pos_y in range(1, 9)), 'pillar not floor to ceiling')
    for label in ('counter_clearance', 'pillar_clearance', 'entrance_to_counter_aisle'):
        require(all(room.get(tuple(position)) == AIR for position in blueprint[label]), label + ' not empty')
    for support in blueprint['lamp_supports']:
        require(short_name(room.get(tuple(support['lantern']))) == 'lantern', 'lamp missing')
        for label in ('chain', 'upper_chain'):
            require(short_name(room.get(tuple(support[label]))) == 'chain', 'lamp chain missing')
        require(solid_top(room.get(tuple(support['beam']))), 'lamp roof anchor missing')
    for cage in blueprint['cage_supports']:
        require(short_name(room.get(tuple(cage['chain']))) == 'chain' and solid_top(room.get(tuple(cage['ceiling']))), 'cage ceiling anchor missing')
        center_x, center_y, center_z = cage['center']
        for offset_x in (-1, 0, 1):
            for offset_z in (-1, 0, 1):
                require(short_name(room.get((center_x + offset_x, 5, center_z + offset_z))) == 'dark_oak_slab', 'cage bottom missing')
                require(short_name(room.get((center_x + offset_x, 7, center_z + offset_z))) == 'dark_oak_planks', 'cage lid missing')
                expected = 'shroomlight' if offset_x == offset_z == 0 else 'iron_bars'
                require(short_name(room.get((center_x + offset_x, center_y, center_z + offset_z))) == expected, 'cage ring missing')
    water = [position for position, block in room.blocks.items() if short_name(block) == 'water']
    require(water == [(12, 1, 1)] and room.get(water[0]).get('level') == '0', 'water source position/count mismatch')
    require(all(solid_top(room.get(tuple(position))) for position in blueprint['contained_water_boundaries']), 'water basin open')
    require(short_name(room.get((12, 2, 1))) == 'lily_pad', 'lily pad missing')
    require(short_name(room.get((13, 1, 1))) == 'grass_block' and short_name(room.get((13, 2, 1))) == 'sugar_cane', 'reed soil/water adjacency missing')
    reached = {(7, 1, 0)}
    pending = deque(reached)
    while pending:
        pos_x, pos_y, pos_z = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            next_position = (pos_x + offset_x, pos_y, pos_z + offset_z)
            if not (0 <= next_position[0] < 15 and 0 <= next_position[2] < 15) or next_position in reached:
                continue
            if clear(room, next_position) and clear(room, (next_position[0], 2, next_position[2])) and solid_top(room.get((next_position[0], 0, next_position[2]))):
                reached.add(next_position)
                pending.append(next_position)
    require(all(tuple(position) in reached for position in blueprint['required_walkable']), 'required walkway disconnected')
    return {'status': 'PASS', 'records': len(room.blocks), 'non_air': sum(block != AIR for block in room.blocks.values()), 'reachable_positions': len(reached), 'empty_block_entities': len(room.block_entities), 'source_water_cells': len(water), 'pillars': 4, 'cages': 2, 'lamps': 2}


def main():
    target = ROOT / 'out/room.nbt'
    room = Structure.read(target)
    metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    report = {'status': 'FAIL', 'source_sha256': digest(target)}
    try:
        _, raw = read_nbt(target)
        require('palettes' not in raw.value, 'multiple palettes forbidden')
        require(digest(target) == metadata['sha256'], 'metadata fingerprint stale')
        require(digest(ROOT / 'reference' / REFERENCE) == REFERENCE_HASH, 'reference fingerprint mismatch')
        report.update(audit(room, metadata, blueprint))
        probes = {}
        for label, position, replacement in (
            ('blocked_aisle', (7, 1, 2), state('stone')),
            ('missing_lamp_chain', (6, 7, 6), AIR),
            ('missing_cage_chain', (2, 8, 7), AIR),
            ('pillar_clearance_blocked', (4, 1, 4), state('stone')),
            ('basin_open', (12, 1, 2), AIR),
            ('unsupported_specimen', (1, 3, 6), AIR),
        ):
            changed = copy.deepcopy(room)
            changed.set(position, replacement)
            try:
                audit(changed, metadata, blueprint)
            except ValueError as error:
                probes[label] = {'status': 'REJECTED_AS_EXPECTED', 'reason': str(error)}
            else:
                raise ValueError('negative probe was not rejected: ' + label)
        changed = copy.deepcopy(room)
        changed.block_entities[(6, 3, 8)].value['Items'] = Tag(9, [compound(id=string('minecraft:potion'), Count=Tag(1, 1))], 10)
        try:
            audit(changed, metadata, blueprint)
        except ValueError as error:
            probes['stocked_brewing_stand'] = {'status': 'REJECTED_AS_EXPECTED', 'reason': str(error)}
        else:
            raise ValueError('stocked stand accepted')
        rebuilt, rebuilt_blueprint = build()
        rebuilt_path = ROOT / 'qa/rebuilt-room.nbt'
        rebuilt.write(rebuilt_path)
        require(digest(rebuilt_path) == digest(target), 'rebuild not deterministic')
        require(json.dumps(rebuilt_blueprint, sort_keys=True) == json.dumps(blueprint, sort_keys=True), 'blueprint stale')
        report.update(negative_probes=probes, deterministic_rebuild='PASS', assertions=['single_palette_3465', 'full_floor_roof', 'empty_inventory_and_fuel', 'four_supported_pillars', 'pillar_and_counter_clearance', '3x3_north_aisle', 'connected_counter_circuit', 'two_ceiling_anchored_cages', 'two_chain_lamps', 'wall_mounted_vines', 'rooted_herbs', 'supported_specimens', 'bounded_basin_lily_reed'], limitations=['Static support rules are a targeted independent audit; actual vanilla state validity and collisions require Forge native QA.'])
    except Exception as error:
        report.update(status='FAIL', error=str(error))
        write_json(ROOT / 'qa/static.json', report)
        raise
    write_json(ROOT / 'qa/static.json', report)
    print(json.dumps(report, ensure_ascii=False))


if __name__ == '__main__':
    main()
