import json
import math
import re
from collections import deque

from build_room import REFERENCE_HASH, ROOT, digest, write_json
from nbtio import AIR, Structure, plain, read_nbt


FORBIDDEN = {'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'cactus', 'powder_snow', 'campfire', 'soul_campfire', 'wither_rose', 'sweet_berry_bush', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'light'}
FLOORS = {'stone_bricks', 'cracked_stone_bricks', 'mossy_stone_bricks', 'andesite'}


def stock_empty(value):
    if isinstance(value, dict):
        for key, child in value.items():
            if key in {'LootTable', 'LootTableSeed', 'Book', 'RecordItem', 'Command'}:
                return False
            if key in {'Items', 'Item'} and child != []:
                return False
            if not stock_empty(child):
                return False
    elif isinstance(value, list):
        return all(stock_empty(child) for child in value)
    return True


def main():
    path = ROOT / 'out/room.nbt'
    metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    room = Structure.read(path)
    raw = plain(read_nbt(path)[1])
    width, height, depth = room.size
    checks = []

    def check(name, condition, evidence=None):
        checks.append({'name': name, 'status': 'PASS' if condition else 'FAIL', 'evidence': evidence})

    check('gzip_single_palette_java_1_20_1', path.read_bytes()[:2] == b'\x1f\x8b' and raw['DataVersion'] == 3465 and 'palette' in raw and 'palettes' not in raw)
    check('size_and_metadata', tuple(metadata['size']) == room.size == (15, 9, 15), room.size)
    check('complete_explicit_unique_volume', len(room.blocks) == math.prod(room.size) and all(0 <= position[axis] < room.size[axis] for position in room.blocks for axis in range(3)), len(room.blocks))
    check('structure_sha256', digest(path) == metadata['sha256'], digest(path))
    check('provisional_valid_unique_style_id', re.fullmatch(r'[A-Z][A-Z0-9_]*', metadata['id']) is not None and metadata['id'] == 'SHOP_A_RUINED', metadata['id'])
    check('no_preplaced_entities', room.entities == [])
    check('vanilla_only_and_no_hazard_or_fluid', all(block.name.startswith('minecraft:') and block.name.split(':')[1] not in FORBIDDEN and block.get('waterlogged', 'false') == 'false' for block in room.blocks.values()))
    check('no_rewards_loot_commands_or_items', all(stock_empty(plain(value)) for value in room.block_entities.values()), {'block_entities': len(room.block_entities)})
    check('expected_empty_containers_and_sign', len(room.block_entities) == 7, [list(position) for position in sorted(room.block_entities)])
    check('complete_flat_full_cube_floor', all(room.get((pos_x, 0, pos_z)).name.split(':')[1] in FLOORS for pos_x in range(width) for pos_z in range(depth)))
    check('complete_roof', all(room.get((pos_x, height - 1, pos_z)).name in {'minecraft:deepslate_tiles', 'minecraft:chiseled_stone_bricks'} for pos_x in range(width) for pos_z in range(depth)))
    entrance = tuple(metadata['entrance'])
    check('north_centered_three_by_three_entry', entrance == (7, 1, 0) and metadata['facing'] == 'north' and all(room.get((pos_x, pos_y, 0)) == AIR for pos_x in (6, 7, 8) for pos_y in (1, 2, 3)))
    groups = {name: set(map(tuple, positions)) for name, positions in blueprint['features'].items()}
    check('all_pillars_embedded_in_corner_wall', len(groups['pillars']) == 32 and all(pos_x in (0, width - 1) and pos_z in (0, depth - 1) for pos_x, unused, pos_z in groups['pillars']))
    check('furniture_not_intersecting_pillars', all(not groups['pillars'] & positions for name, positions in groups.items() if name != 'pillars'))
    aisle = set(map(tuple, blueprint['entrance_to_counter_aisle']))
    check('three_wide_three_high_continuous_central_aisle', len(aisle) == 126 and all(room.get(position) == AIR for position in aisle), {'clear_cells': len(aisle)})
    check('broken_counter_two_sections_three_block_gap', len(groups['counter']) == 6 and all(room.get(tuple(position)) == AIR for position in blueprint['counter_gap']) and all(position[0] in (3, 4, 5, 9, 10, 11) and position[1:] == (1, 9) for position in groups['counter']))
    ring = set(map(tuple, blueprint['counter_clearance']))
    check('counter_one_block_circulation_each_section', len(ring) == 48 and all(room.get(position) == AIR for position in ring), {'clear_cells': len(ring)})
    debris = list(map(tuple, blueprint['grounded_debris']))
    check('all_debris_grounded_on_full_floor', len(debris) == 13 and all(position[1] == 1 and room.get(position) != AIR and room.get((position[0], 0, position[2])).name.split(':')[1] in FLOORS for position in debris), debris)
    check('fallen_beam_and_collapsed_shelf_retained', len(groups['fallen_beam']) == 3 and len(groups['fallen_shelves']) == 6)
    check('debris_avoids_aisle_and_counter_ring', not set(debris) & (aisle | ring))
    offsets = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))
    check('cobwebs_retained_attached_and_off_path', len(groups['cobwebs']) == 10 and not groups['cobwebs'] & (aisle | ring) and all(any(room.get(tuple(position[axis] + offset[axis] for axis in range(3))).name not in {'minecraft:air', 'minecraft:cobweb'} for offset in offsets) for position in groups['cobwebs']))
    check('shelves_attached_to_rear_wall', all(position[2] == 13 and room.get((position[0], position[1], 14)) != AIR for position in groups['shelves']))
    supports = blueprint['lamp_supports']
    check('two_chained_lamps_on_complete_beam', len(supports) == 2 and all(room.get(tuple(item['lantern'])).name == 'minecraft:lantern' and room.get(tuple(item['lantern'])).get('hanging') == 'true' and room.get(tuple(item['chain'])).name == 'minecraft:chain' and room.get(tuple(item['beam'])).name == 'minecraft:dark_oak_log' for item in supports), supports)
    check('ground_lamp_has_full_floor', all(position[1] == 1 and room.get((position[0], 0, position[2])).name.split(':')[1] in FLOORS for position in groups['ground_lamp']))
    check('unlit_candles_no_flammable_fire', all(block.get('lit') == 'false' for block in room.blocks.values() if block.name.endswith('candle')))
    check('empty_chest_lid_clearance', all(room.get((position[0], 2, position[2])) == AIR for position in groups['empty_chests']))
    check('decor_has_physical_support', all(room.get((position[0], position[1] - 1, position[2])) != AIR for name in ('counter_decor', 'shelf_decor') for position in groups[name]))

    def walkable(position):
        pos_x, pos_y, pos_z = position
        return 0 <= pos_x < width and 0 <= pos_z < depth and pos_y == 1 and room.get(position) == AIR and room.get((pos_x, 2, pos_z)) == AIR and room.get((pos_x, 0, pos_z)).name.split(':')[1] in FLOORS

    reached = {entrance}
    pending = deque([entrance])
    while pending:
        current = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            next_position = (current[0] + offset_x, 1, current[2] + offset_z)
            if next_position not in reached and walkable(next_position):
                reached.add(next_position)
                pending.append(next_position)
    targets = [tuple(metadata['interior'])] + list(map(tuple, blueprint['required_walkable']))
    check('bidirectional_paths_counter_circuits_chest_approaches', all(position in reached for position in targets), {'reachable_foot_positions': len(reached), 'required_targets': targets})
    check('central_aisle_walks_all_the_way_to_rear', all((pos_x, 1, pos_z) in reached for pos_x in (6, 7, 8) for pos_z in range(14)))
    check('source_image_fingerprint', digest(ROOT / 'reference/shop-variant-A-broken-unmanned.png') == metadata['reference_sha256'] == REFERENCE_HASH, REFERENCE_HASH)
    integration = metadata['integration']
    check('not_deployed_no_slot_coordinator_required', integration['assigned_slot'] is None and integration['id_is_provisional'] and not integration['deployed'] and integration['requires_coordinator_acceptance'] and integration['coordinator_thread'] == '01a19758-b0e5-75c2-822a-4860527a1e8b')
    report = {'status': 'PASS' if all(item['status'] == 'PASS' for item in checks) else 'FAIL', 'structure_sha256': digest(path), 'checks': checks, 'limitations': ['Air-cell BFS and explicit theme constraints are static evidence, not native collisions or canSurvive.', 'No production instance, saved game, FT core road, commerce, spawning or rewards are exercised.']}
    write_json(ROOT / 'qa/static.json', report)
    print(json.dumps({'status': report['status'], 'checks': len(checks), 'failed': [item['name'] for item in checks if item['status'] != 'PASS']}))
    if report['status'] != 'PASS':
        raise SystemExit(1)


if __name__ == '__main__':
    main()
