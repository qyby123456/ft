import json
from collections import deque

from build_room import ROOT, SIZE, REFERENCE_SHA, digest, write_json
from nbtio import Structure, plain


AIR_NAMES = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
FORBIDDEN = {'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'cactus', 'powder_snow', 'campfire', 'wither_rose', 'sweet_berry_bush', 'cobweb', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'light'}


def empty_inventory(value):
    if isinstance(value, dict):
        for key, child in value.items():
            if key in {'LootTable', 'LootTableSeed', 'RecordItem', 'Book', 'Offers', 'Recipes'}:
                return False
            if key in {'Items', 'Item'} and child != []:
                return False
            if not empty_inventory(child):
                return False
    elif isinstance(value, list):
        return all(empty_inventory(child) for child in value)
    return True


def main():
    room = Structure.read(ROOT / 'out/room.nbt')
    metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    checks = {}

    def require(name, condition):
        checks[name] = bool(condition)
        if not condition:
            raise ValueError(name)

    def air(position):
        return room.get(tuple(position)).name in AIR_NAMES

    require('version_dimensions_complete_volume', room.data_version == 3465 and room.size == SIZE and tuple(metadata['size']) == SIZE and len(room.blocks) == 1144)
    require('structure_fingerprint', metadata['sha256'] == digest(ROOT / 'out/room.nbt'))
    require('design_fingerprint', metadata['reference_sha256'] == REFERENCE_SHA and digest(ROOT / 'reference/shop-variant-I-undead-pawn-shop.png') == REFERENCE_SHA)
    require('north_centered_interface', metadata['facing'] == 'north' and metadata['entrance'] == [6, 1, 0])
    require('no_entities_or_stock', not room.entities and all(empty_inventory(plain(entity)) for entity in room.block_entities.values()))
    require('no_forbidden_or_fluid_states', all(block.name.startswith('minecraft:') and block.name.removeprefix('minecraft:') not in FORBIDDEN and block.get('waterlogged', 'false') == 'false' for block in room.blocks.values()))
    require('roof_complete', all(not air((pos_x, 7, pos_z)) for pos_x in range(13) for pos_z in range(11)))
    features = {name: {tuple(position) for position in values} for name, values in blueprint['features'].items()}
    flattened = [position for positions in features.values() for position in positions]
    require('feature_groups_do_not_overlap', len(flattened) == len(set(flattened)))
    require('pillars_in_shell_corners', len(features['pillars']) == 24 and all(position[0] in (0, 12) and position[2] in (0, 10) for position in features['pillars']))
    require('counter_four_sided_clearance', all(air(position) for position in blueprint['counter_clearance']))
    require('main_aisle_3_by_3', all(air(position) for position in blueprint['entrance_to_counter_aisle']))
    require('furniture_founded_on_floor', all(not air((position[0], 0, position[2])) for name in ('counter', 'cage_base', 'display_base', 'barrels', 'brazier_base') for position in features[name]))
    require('barrels_only_corners', features['barrels'] == {(1, 1, 1), (11, 1, 1), (1, 1, 9), (11, 1, 9)})
    require('all_declared_anchors_present', all(not air(entry['anchor']) and not air(entry['position']) for entry in blueprint['supported_decor']))
    for lamp in blueprint['lamp_supports']:
        require('lamp_chain_' + str(lamp['lantern'][0]), room.get(tuple(lamp['lantern'])).name == lamp['block'] and room.get(tuple(lamp['chain'])).name == 'minecraft:chain' and room.get(tuple(lamp['beam'])).name == 'minecraft:dark_oak_log')
    for pos_x in (2, 10):
        require('guarded_soul_brazier_' + str(pos_x), room.get((pos_x, 2, 9)).name == 'minecraft:soul_campfire' and room.get((pos_x, 1, 9)).name == 'minecraft:polished_blackstone_bricks' and all(room.get((pos_x, pos_y, 8)).name == 'minecraft:iron_bars' for pos_y in (2, 3)) and not air((pos_x - 1, 2, 9)) and not air((pos_x + 1, 2, 9)) and not air((pos_x, 2, 10)))
    require('relic_enclosure_roof_and_base', all(not air((pos_x, pos_y, pos_z)) for pos_x in range(9, 12) for pos_z in range(2, 5) for pos_y in (1, 4)))
    entrance = tuple(metadata['entrance'])
    reached = {entrance}
    pending = deque([entrance])
    while pending:
        current = pending.popleft()
        for offset in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1)):
            next_pos = tuple(current[index] + offset[index] for index in range(3))
            if 0 <= next_pos[0] < 13 and 0 <= next_pos[2] < 11 and next_pos not in reached and air(next_pos) and air((next_pos[0], 2, next_pos[2])) and not air((next_pos[0], 0, next_pos[2])):
                reached.add(next_pos)
                pending.append(next_pos)
    require('safe_interior_and_counter_ring_reachable', tuple(metadata['interior']) in reached and all(tuple(position) in reached for position in blueprint['required_walkable']))
    require('no_trade_or_deployment', metadata['integration']['deployed'] is False and metadata['integration']['assigned_slot'] is None)
    report = {'status': 'PASS', 'structure_sha256': metadata['sha256'], 'checks': checks, 'records': len(room.blocks), 'block_entities': len(room.block_entities), 'reachable_air_only_foot_positions': len(reached), 'limitations': ['Collision shapes, native registry validity and neighbor updates require the separate Forge native test.']}
    write_json(ROOT / 'qa/static.json', report)
    print(json.dumps(report, ensure_ascii=False))


if __name__ == '__main__':
    main()
