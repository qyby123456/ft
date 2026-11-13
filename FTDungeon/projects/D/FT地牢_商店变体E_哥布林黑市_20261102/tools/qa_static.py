import copy
import json
from collections import deque
from pathlib import Path

from build_room import REFERENCE_HASH, ROOT, SIZE, digest, write_json
from nbtio import AIR, Structure, plain, read_nbt


FORBIDDEN = {'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'cactus', 'powder_snow', 'campfire', 'soul_campfire', 'wither_rose', 'sweet_berry_bush', 'cobweb', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'light'}
BLOCK_ENTITIES = {'barrel': 'barrel', 'chest': 'chest', 'brewing_stand': 'brewing_stand', 'skeleton_skull': 'skull'}


def empty_stock(value):
    if isinstance(value, dict):
        for key, child in value.items():
            if key in ('LootTable', 'LootTableSeed', 'Book', 'RecordItem') or key in ('Items', 'Item') and child:
                return False
            if not empty_stock(child):
                return False
    if isinstance(value, list):
        return all(empty_stock(child) for child in value)
    return True


def support_chain(room, item, base_key, base_kind):
    previous = tuple(item[base_key])
    if room.get(previous).name != 'minecraft:' + base_kind:
        return False
    for value in item['chains']:
        segment = tuple(value)
        if segment != (previous[0], previous[1] + 1, previous[2]) or room.get(segment).name != 'minecraft:chain' or room.get(segment).get('axis') != 'y':
            return False
        previous = segment
    beam = tuple(item['beam'])
    return beam == (previous[0], previous[1] + 1, previous[2]) and room.get(beam).name == 'minecraft:dark_oak_log'


def main():
    metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    room = Structure.read(ROOT / 'out/room.nbt')
    _, raw = read_nbt(ROOT / 'out/room.nbt')
    features = {name: {tuple(value) for value in positions} for name, positions in blueprint['features'].items()}
    checks = []

    def check(name, passed, details=None):
        checks.append({'name': name, 'status': 'PASS' if passed else 'FAIL', 'details': details})

    check('reference_sha256', metadata['reference_sha256'] == REFERENCE_HASH == digest(ROOT / 'reference' / Path(metadata['reference']).name), REFERENCE_HASH)
    check('structure_sha256', metadata['sha256'] == digest(ROOT / 'out/room.nbt'), metadata['sha256'])
    check('gzip_single_palette_java_1_20_1', (ROOT / 'out/room.nbt').read_bytes()[:2] == bytes((31, 139)) and room.data_version == 3465 and 'palette' in raw.value and 'palettes' not in raw.value, room.data_version)
    check('metadata_and_full_explicit_volume', room.size == SIZE == tuple(metadata['size']) and len(room.blocks) == 1521 and all(0 <= value < limit for position in room.blocks for value, limit in zip(position, SIZE)), {'size': room.size, 'records': len(room.blocks)})
    check('north_centered_safe_foot_markers', metadata['entrance'] == [6, 1, 0] and metadata['interior'] == [6, 1, 4] and metadata['facing'] == 'north')
    check('no_entities_or_goblin', not room.entities, len(room.entities))
    forbidden = [(position, block.name) for position, block in room.blocks.items() if not block.name.startswith('minecraft:') or block.name.split(':')[-1] in FORBIDDEN or block.get('waterlogged') == 'true']
    check('vanilla_only_no_hazards_fluids_or_admin_blocks', not forbidden, forbidden)
    expected_entities = {position: 'minecraft:' + BLOCK_ENTITIES[block.name.split(':')[-1]] for position, block in room.blocks.items() if block.name.split(':')[-1] in BLOCK_ENTITIES}
    actual_entities = {position: plain(entity).get('id') for position, entity in room.block_entities.items()}
    check('exact_block_entity_types_and_positions', expected_entities == actual_entities and len(actual_entities) == 18, {'expected': len(expected_entities), 'actual': len(actual_entities)})
    check('empty_containers_no_reward_or_loot_table', all(empty_stock(plain(entity)) for entity in room.block_entities.values()), {'block_entities': len(room.block_entities)})
    solid_shell = {'minecraft:stone_bricks', 'minecraft:cracked_stone_bricks', 'minecraft:mossy_stone_bricks', 'minecraft:cobblestone', 'minecraft:red_terracotta', 'minecraft:green_terracotta'}
    check('full_solid_floor', all(room.get((pos_x, 0, pos_z)).name in solid_shell for pos_x in range(13) for pos_z in range(13)))
    check('complete_roof', all(room.get((pos_x, 8, pos_z)).name in solid_shell for pos_x in range(13) for pos_z in range(13)))
    holes = []
    for pos_x in range(13):
        for pos_z in range(13):
            if pos_x not in (0, 12) and pos_z not in (0, 12):
                continue
            for pos_y in range(1, 8):
                opening = pos_z == 0 and 5 <= pos_x <= 7 and 1 <= pos_y <= 3
                if (room.get((pos_x, pos_y, pos_z)) == AIR) != opening:
                    holes.append((pos_x, pos_y, pos_z))
    check('closed_shell_except_north_3x3_door', not holes, holes)
    pillar_columns = {(value[0], value[2]) for value in features['pillars']}
    check('four_wall_embedded_pillars', pillar_columns == {(0, 0), (12, 0), (0, 12), (12, 12)} and len(features['pillars']) == 28, sorted(pillar_columns))
    furniture = set().union(*(features[name] for name in ('counter', 'counter_decor', 'shelves', 'shelf_decor', 'barrels', 'chests', 'cage')))
    contacts = [(pillar, item) for pillar in features['pillars'] for item in furniture if sum(abs(pillar[axis] - item[axis]) for axis in range(3)) <= 1]
    check('no_corner_pillar_furniture_overlap_or_face_contact', not contacts, contacts)
    check('independent_counter_7x1', features['counter'] == {(pos_x, 1, 8) for pos_x in range(3, 10)})
    blocked_ring = [value for value in blueprint['counter_clearance'] if room.get(tuple(value)) != AIR]
    check('counter_four_sides_one_block_clearance', not blocked_ring, {'cells': len(blueprint['counter_clearance']), 'blocked': blocked_ring})
    blocked_aisle = [value for value in blueprint['entrance_to_counter_aisle'] if room.get(tuple(value)) != AIR]
    check('main_aisle_three_wide_three_high', len(blueprint['entrance_to_counter_aisle']) == 72 and not blocked_aisle, {'cells': len(blueprint['entrance_to_counter_aisle']), 'blocked': blocked_aisle})
    check('shelves_one_deep_and_wall_attached', all(pos_x in (1, 11) or pos_z == 11 for pos_x, _, pos_z in features['shelves'] | features['shelf_decor']))
    check('stash_barrels_confined_to_corner_clusters', len(features['barrels']) == 8 and all(pos_x in (1, 11) and pos_z in (1, 2, 10) for pos_x, _, pos_z in features['barrels']))
    unsupported = []
    for pos_x, pos_y, pos_z in features['counter_decor'] | features['shelf_decor'] | features['stash_decor']:
        below = room.get((pos_x, pos_y - 1, pos_z))
        if below == AIR or below.name.endswith('_slab') and below.get('type') not in ('top', 'double'):
            unsupported.append((pos_x, pos_y, pos_z))
    check('decorations_grounded_or_shelf_supported', not unsupported, unsupported)
    check('four_chests_openable', all(room.get(tuple(value)) == AIR for value in blueprint['chest_lid_clearance']) and len(features['chests']) == 4)
    check('cage_empty_wall_fixed_grounded', not room.entities and len(features['cage']) == 6 and all(room.get((11, 1, pos_z)).name == 'minecraft:spruce_planks' and room.get((12, 2, pos_z)) != AIR for pos_z in (5, 6)))
    frame = features['awning_frame']
    check('awning_frame_wall_anchored_and_posts_grounded', all((edge, 6, pos_z) in frame for edge in (0, 12) for pos_z in (8, 11)) and all((pos_x, 6, 11) in frame and room.get((pos_x, 0, 11)) != AIR and all((pos_x, pos_y, 11) in features['awning_posts'] for pos_y in range(1, 6)) for pos_x in (1, 11)))
    attached = set(frame)
    pending = deque(frame)
    cloth = features['awning_cloth']
    while pending:
        current = pending.popleft()
        for offset in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            next_position = tuple(current[axis] + offset[axis] for axis in range(3))
            if next_position in cloth and next_position not in attached:
                attached.add(next_position)
                pending.append(next_position)
    check('all_patchwork_connected_to_fixed_frame', cloth <= attached and {'minecraft:red_wool', 'minecraft:green_wool', 'minecraft:brown_wool'} <= {room.get(position).name for position in cloth}, {'cloth_blocks': len(cloth)})
    check('two_chained_lanterns_above_counter_front_walkway', len(blueprint['lamp_supports']) == 2 and all(support_chain(room, item, 'lantern', 'lantern') and item['lantern'][2] == 7 and item['lantern'][1] == 4 for item in blueprint['lamp_supports']))
    check('two_empty_pots_chain_fixed_to_frame', len(blueprint['suspended_pot_supports']) == 2 and all(support_chain(room, item, 'pot', 'cauldron') for item in blueprint['suspended_pot_supports']))
    altered = copy.deepcopy(room)
    pot_fixture = blueprint['suspended_pot_supports'][0]
    altered.set(tuple(pot_fixture['chains'][0]), AIR)
    check('negative_missing_pot_chain_detected', not support_chain(altered, pot_fixture, 'pot', 'cauldron'))
    check('negative_reward_inventory_detected', not empty_stock({'Items': [{'id': 'minecraft:diamond', 'Count': 1}]}))

    def walkable(position):
        pos_x, pos_y, pos_z = position
        return pos_y == 1 and 0 <= pos_x < 13 and 0 <= pos_z < 13 and room.get(position) == AIR and room.get((pos_x, 2, pos_z)) == AIR and room.get((pos_x, 0, pos_z)).name in solid_shell

    start = tuple(metadata['entrance'])
    reached = {start}
    pending = deque([start])
    while pending:
        pos_x, _, pos_z = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            next_position = (pos_x + offset_x, 1, pos_z + offset_z)
            if next_position not in reached and walkable(next_position):
                reached.add(next_position)
                pending.append(next_position)
    missing = [value for value in blueprint['required_walkable'] if tuple(value) not in reached]
    check('entrance_interior_counter_circuit_and_chest_approaches_reachable', walkable(start) and tuple(metadata['interior']) in reached and not missing, {'reachable_foot_positions': len(reached), 'missing': missing})
    check('standalone_provisional_undeployed', metadata['integration']['status'] == 'STANDALONE_CANDIDATE_NOT_REGISTERED' and metadata['integration']['assigned_slot'] is None and metadata['integration']['deployed'] is False)
    report = {'status': 'PASS' if all(item['status'] == 'PASS' for item in checks) else 'FAIL', 'structure_sha256': metadata['sha256'], 'record_count': len(room.blocks), 'block_entities': len(room.block_entities), 'reachable_foot_positions': len(reached), 'checks': checks, 'limitations': ['Static support is an explicit design graph; vanilla canSurvive and player collision are checked separately by Forge.', 'No core integration, gallery connection or player save was changed.']}
    write_json(ROOT / 'qa/static.json', report)
    print(json.dumps({'status': report['status'], 'checks': len(checks), 'failed': [item['name'] for item in checks if item['status'] != 'PASS'], 'reachable_foot_positions': len(reached)}))
    raise SystemExit(0 if report['status'] == 'PASS' else 1)


if __name__ == '__main__':
    main()
