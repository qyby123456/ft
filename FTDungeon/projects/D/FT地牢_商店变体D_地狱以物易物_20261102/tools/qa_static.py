import copy
import hashlib
import json
from collections import deque
from pathlib import Path

from nbtio import AIR, Structure, Tag, compound, plain, read_nbt, state, string


ROOT = Path(__file__).resolve().parents[1]
FORBIDDEN = {'lava', 'water', 'fire', 'soul_fire', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void', 'jigsaw', 'light', 'cobweb', 'powder_snow', 'campfire', 'soul_campfire', 'cactus', 'wither_rose', 'sweet_berry_bush'}
NON_SOLID = {'air', 'chain', 'lantern', 'soul_lantern', 'iron_bars', 'flower_pot', 'potted_crimson_fungus', 'red_candle', 'brewing_stand', 'skeleton_skull', 'anvil'}


def require(condition, message):
    if not condition:
        raise ValueError(message)


def empty_inventory(value):
    if not isinstance(value, dict):
        return True
    for key, entry in value.items():
        if key in {'LootTable', 'LootTableSeed', 'Book', 'RecordItem'} or key in {'Items', 'Item'} and entry:
            return False
        if isinstance(entry, dict) and not empty_inventory(entry):
            return False
    return True


def solid(room, position):
    block = room.get(position)
    return block.name.removeprefix('minecraft:') not in NON_SOLID and not block.name.endswith('_slab') and block.name != 'minecraft:magma_block'


def enclosed(room, position):
    return all(solid(room, (position[0] + offset[0], position[1] + offset[1], position[2] + offset[2]))
               for offset in ((0, 1, 0), (0, -1, 0), (1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1)))


def walkable(room, position):
    return room.get(position) == AIR and room.get((position[0], position[1] + 1, position[2])) == AIR and solid(room, (position[0], position[1] - 1, position[2]))


def validate(room, metadata, blueprint):
    checks = []
    def check(condition, message):
        require(condition, message)
        checks.append(message)
    check(room.size == (13, 9, 13) == tuple(metadata['size']), 'Exact native size and metadata')
    check(room.data_version == 3465, 'Minecraft Java 1.20.1 DataVersion')
    check(len(room.blocks) == 1521 and all(0 <= coordinate < limit for position in room.blocks for coordinate, limit in zip(position, room.size)), 'Full explicit bounded volume')
    check(not room.entities, 'No preplaced entities')
    check(all(block.name.startswith('minecraft:') and block.name.removeprefix('minecraft:') not in FORBIDDEN and block.get('waterlogged', 'false') == 'false' for block in room.blocks.values()), 'Vanilla-only no fluid bare fire or forbidden blocks')
    check(all(empty_inventory(plain(value)) for value in room.block_entities.values()), 'Empty block entities and no loot tables')
    check(all(solid(room, (pos_x, 8, pos_z)) for pos_x in range(13) for pos_z in range(13)), 'Complete structural roof')
    check(all(solid(room, (pos_x, 0, pos_z)) for pos_x in range(13) for pos_z in range(13)), 'Complete safe full-block floor')
    check(metadata['entrance'] == [6, 1, 0] and metadata['interior'] == [6, 1, 3] and metadata['facing'] == 'north', 'North central entrance and safe interior')
    for key in ('entrance_to_counter_aisle', 'counter_clearance'):
        check(all(room.get(tuple(position)) == AIR for position in blueprint[key]), key + ' collision-free')
    check(all(enclosed(room, tuple(position)) for position in blueprint['encased_magma']), 'Every magma block has six safe full enclosing faces')
    check({position for position, block in room.blocks.items() if block.name == 'minecraft:magma_block'} == {tuple(position) for position in blueprint['encased_magma']}, 'No undeclared hazard')
    check(all(room.get(tuple(value['position'])) != AIR and room.get(tuple(value['support'])) != AIR and sum(abs(first - second) for first, second in zip(value['position'], value['support'])) == 1 for value in blueprint['supports']), 'Every decoration has explicit adjacent support')
    check(all(position[0] in (0, 12) and position[2] in (0, 12) for position in blueprint['features']['pillars']), 'Pillars embedded in wall corners only')
    entrance = tuple(metadata['entrance'])
    reached, pending = {entrance}, deque([entrance])
    while pending:
        position = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            neighbor = (position[0] + offset_x, 1, position[2] + offset_z)
            if 0 <= neighbor[0] < 13 and 0 <= neighbor[2] < 13 and neighbor not in reached and walkable(room, neighbor):
                reached.add(neighbor)
                pending.append(neighbor)
    check(all(tuple(position) in reached for position in blueprint['required_walkable']) and tuple(metadata['interior']) in reached, 'Bidirectional entrance counter ring and display approaches reachable')
    check(all(room.get((pos_x, height, 0)) == AIR for pos_x in (5, 6, 7) for height in (1, 2, 3)), 'North 3x3 door-free aperture')
    check(all(solid(room, (pos_x, 0, 0)) for pos_x in (5, 6, 7)), 'Three entrance feet supported on safe full blocks')
    return {'checks': checks, 'reachable_foot_positions': len(reached), 'declared_supports': len(blueprint['supports']), 'sealed_magma_count': len(blueprint['encased_magma'])}


def main():
    room = Structure.read(ROOT / 'out/room.nbt')
    metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    _, encoded = read_nbt(ROOT / 'out/room.nbt')
    require('palette' in encoded.value and 'palettes' not in encoded.value, 'Single palette required')
    fingerprint = hashlib.sha256((ROOT / 'out/room.nbt').read_bytes()).hexdigest()
    require(fingerprint == metadata['sha256'], 'Metadata fingerprint differs')
    result = validate(room, metadata, blueprint)
    probes = {}
    mutations = {'blocked_entry': ((6, 1, 0), state('stone')), 'missing_roof': ((6, 8, 6), AIR),
                 'missing_chain': ((4, 6, 4), AIR), 'exposed_magma': ((1, 2, 5), AIR), 'extra_magma': ((6, 0, 2), state('magma_block')),
                 'bare_soul_fire': ((6, 1, 2), state('soul_fire')), 'blocked_counter_ring': ((3, 1, 7), state('stone'))}
    for name, (position, block) in mutations.items():
        broken = copy.deepcopy(room)
        broken.set(position, block)
        try:
            validate(broken, metadata, blueprint)
        except ValueError as exception:
            probes[name] = {'status': 'REJECTED', 'reason': str(exception)}
        else:
            raise AssertionError('Negative probe not rejected: ' + name)
    broken = copy.deepcopy(room)
    broken.block_entities[(1, 1, 3)] = compound(id=string('minecraft:barrel'), LootTable=string('minecraft:chests/simple_dungeon'))
    try:
        validate(broken, metadata, blueprint)
    except ValueError as exception:
        probes['loot_table'] = {'status': 'REJECTED', 'reason': str(exception)}
    else:
        raise AssertionError('Negative loot table probe missed')
    report = {'status': 'PASS', 'structure_sha256': fingerprint, 'records': len(room.blocks), 'single_palette': True, **result, 'negative_probes': probes,
              'scope': 'Static geometry audit; registry, collisions, survival and world stabilization are separately checked by Forge.'}
    (ROOT / 'qa/static.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'status': 'PASS', 'checks': len(result['checks']), 'negative_probes': len(probes), 'reachable': result['reachable_foot_positions']}))


if __name__ == '__main__':
    main()
