import hashlib
import json
from collections import deque
from pathlib import Path

from nbtio import AIR, Structure, plain, read_nbt


ROOT = Path(__file__).resolve().parents[1]
HAZARDS = {'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'cactus', 'powder_snow', 'campfire', 'soul_campfire', 'wither_rose', 'sweet_berry_bush', 'cobweb', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'light'}


def main():
    checks = []

    def check(name, condition, detail):
        checks.append({'name': name, 'status': 'PASS' if condition else 'FAIL', 'detail': detail})

    folder = ROOT / 'out'
    metadata = json.loads((folder / 'room.json').read_text(encoding='utf-8'))
    blueprint = json.loads((folder / 'blueprint.json').read_text(encoding='utf-8'))
    room = Structure.read(folder / 'room.nbt')
    raw = plain(read_nbt(folder / 'room.nbt')[1])
    fingerprint = hashlib.sha256((folder / 'room.nbt').read_bytes()).hexdigest()
    width, height, depth = room.size
    check('metadata_and_hash', metadata['sha256'] == fingerprint and metadata['size'] == list(room.size), fingerprint)
    check('single_palette_1_20_1', raw['DataVersion'] == 3465 and 'palette' in raw and 'palettes' not in raw, {'data_version': raw['DataVersion'], 'palette_size': len(raw['palette'])})
    check('full_volume_unique_bounded_records', len(room.blocks) == width * height * depth and all(all(type(value) is int and 0 <= value < limit for value, limit in zip(position, room.size)) for position in room.blocks), {'records': len(room.blocks), 'explicit_air_included': True})
    check('north_center_entrance', metadata['entrance'] == [width // 2, 1, 0] and metadata['facing'] == 'north', metadata['entrance'])
    check('no_preplaced_entities', not room.entities, len(room.entities))
    forbidden = sorted({block.name for block in room.blocks.values() if not (block.name.startswith('minecraft:') or block.name == 'ftdungeontrading:trading_table') or block.name.removeprefix('minecraft:') in HAZARDS or dict(block.properties).get('waterlogged') == 'true'})
    intended_entities = {'minecraft:barrel': 'minecraft:barrel', 'minecraft:chest': 'minecraft:chest', 'minecraft:brewing_stand': 'minecraft:brewing_stand', 'minecraft:skull': 'minecraft:skeleton_skull', 'minecraft:sign': 'minecraft:dark_oak_wall_sign', 'ftdungeontrading:trading_table': 'ftdungeontrading:trading_table'}
    entity_errors = []
    for position, tagged in room.block_entities.items():
        data = plain(tagged)
        if intended_entities.get(data.get('id')) != room.get(position).name:
            entity_errors.append(list(position))
    skull_count = sum(room.get(position).name == 'minecraft:skeleton_skull' for position in room.block_entities)
    chest_count = sum(room.get(position).name == 'minecraft:chest' for position in room.block_entities)
    barrel_count = sum(room.get(position).name == 'minecraft:barrel' for position in room.block_entities)
    entity_coverage = len(room.block_entities) == 11 and skull_count == 3 and chest_count == 1 and barrel_count == 4
    check('exact_block_allowlist_no_hazards_or_logic_blocks', not forbidden and not entity_errors and entity_coverage, {'forbidden': forbidden, 'entity_errors': entity_errors, 'expected_block_entities': 11, 'observed_block_entities': len(room.block_entities), 'only_modded_block_allowed': 'ftdungeontrading:trading_table'})

    def empty_stock(value):
        if isinstance(value, dict):
            for key, child in value.items():
                if key in ('LootTable', 'LootTableSeed', 'Book', 'RecordItem') or key in ('Items', 'Item') and child:
                    return False
                if not empty_stock(child):
                    return False
        elif isinstance(value, list):
            return all(empty_stock(child) for child in value)
        return True

    check('no_reward_inventory_or_loot_tables', all(empty_stock(plain(entity)) for entity in room.block_entities.values()), {'block_entities': len(room.block_entities), 'nonempty_inventory': 0 if all(empty_stock(plain(entity)) for entity in room.block_entities.values()) else 'FOUND'})
    check('full_solid_floor', all(room.get((pos_x, 0, pos_z)) != AIR for pos_x in range(width) for pos_z in range(depth)), [width, depth])
    check('complete_roof', all(room.get((pos_x, height - 1, pos_z)) != AIR for pos_x in range(width) for pos_z in range(depth)), [width, depth])
    shell_holes = []
    for pos_x in range(width):
        for pos_z in range(depth):
            if pos_x not in (0, width - 1) and pos_z not in (0, depth - 1):
                continue
            for pos_y in range(1, height - 1):
                opening = pos_z == 0 and 4 <= pos_x <= 6 and 1 <= pos_y <= 3
                if (room.get((pos_x, pos_y, pos_z)) == AIR) != opening:
                    shell_holes.append([pos_x, pos_y, pos_z])
    check('closed_shell_only_north_3x3_opening', not shell_holes, shell_holes)

    features = {name: {tuple(position) for position in positions} for name, positions in blueprint['features'].items()}
    columns = {(position[0], position[2]) for position in features['pillars']}
    furniture = set().union(*(features.get(name, set()) for name in ('counter', 'counter_decor', 'shelves', 'shelf_decor', 'barrels')))
    check('four_wall_embedded_pillars', columns == {(0, 0), (10, 0), (0, 10), (10, 10)} and len(features['pillars']) == 24, sorted(columns))
    contacts = [(pillar, item) for pillar in features['pillars'] for item in furniture if sum(abs(pillar[axis] - item[axis]) for axis in range(3)) <= 1]
    check('no_pillar_furniture_overlap_or_face_contact', not contacts, contacts)
    table_primary = room.get((4, 1, 6))
    table_secondary = room.get((5, 1, 6))
    table_nbt = plain(room.block_entities.get((4, 1, 6))) if (4, 1, 6) in room.block_entities else {}
    paired = table_primary.name == table_secondary.name == 'ftdungeontrading:trading_table' and table_primary.get('part') == 'left' and table_secondary.get('part') == 'right' and table_primary.get('facing') == table_secondary.get('facing') == 'north'
    no_stock = table_nbt.get('OfferId') == 'ftdungeontrading:unbound' and table_nbt.get('RemainingStock') == 0 and not table_nbt.get('DisplayRewards') and not table_nbt.get('DisplayCosts')
    check('original_trading_table_complete_pair_unbound_zero_stock', features['counter'] == {(4, 1, 6), (5, 1, 6)} and paired and no_stock and (5, 1, 6) not in room.block_entities, {'footprint': sorted(features['counter']), 'primary_payload': table_nbt})
    ring_blocked = [position for position in blueprint['counter_clearance'] if room.get(tuple(position)) != AIR]
    check('counter_all_sides_one_block_clearance', not ring_blocked, {'checked_cells': len(blueprint['counter_clearance']), 'blocked': ring_blocked})
    aisle_blocked = [position for position in blueprint['entrance_to_counter_aisle'] if room.get(tuple(position)) != AIR]
    check('entrance_to_counter_3_wide_3_high', not aisle_blocked, {'checked_cells': len(blueprint['entrance_to_counter_aisle']), 'blocked': aisle_blocked, 'footprint': [[4, 0], [6, 5]]})
    check('shelves_one_deep_wall_attached', all(position[0] in (1, 9) or position[2] == 9 for position in features['shelves'] | features['shelf_decor']), len(features['shelves']))
    check('barrels_only_four_interior_corners', features['barrels'] == {(1, 1, 1), (9, 1, 1), (1, 1, 9), (9, 1, 9)}, sorted(features['barrels']))

    unsupported = []
    for position in features['shelf_decor'] | features.get('counter_decor', set()):
        below = room.get((position[0], position[1] - 1, position[2]))
        if below == AIR or below.name.endswith('_slab') and below.get('type') not in ('top', 'double'):
            unsupported.append(position)
    check('decorations_have_top_surface_support', not unsupported, unsupported)
    supports = blueprint['lamp_supports']
    lamps_valid = all(room.get(tuple(item['lantern'])).name == 'minecraft:lantern' and room.get(tuple(item['lantern'])).get('hanging') == 'true' and room.get(tuple(item['chain'])).name == 'minecraft:chain' and room.get(tuple(item['beam'])).name == 'minecraft:dark_oak_log' and item['lantern'][0] in (4, 5, 6) and item['lantern'][2] <= 5 for item in supports)
    check('two_chained_lamps_over_aisle_not_counter', len(supports) == 2 and lamps_valid, supports)
    check('chest_lid_clearance', room.get((5, 2, 9)) == AIR, [5, 2, 9])

    def walkable(position):
        pos_x, pos_y, pos_z = position
        return pos_y == 1 and 0 <= pos_x < width and 0 <= pos_z < depth and room.get(position) == AIR and room.get((pos_x, 2, pos_z)) == AIR and room.get((pos_x, 0, pos_z)) != AIR

    start = tuple(metadata['entrance'])
    pending = deque([start])
    reached = {start}
    while pending:
        current = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            next_position = (current[0] + offset_x, 1, current[2] + offset_z)
            if next_position not in reached and walkable(next_position):
                reached.add(next_position)
                pending.append(next_position)
    targets = [tuple(metadata['interior'])] + [tuple(position) for position in blueprint['required_walkable']]
    check('bidirectional_paths_and_counter_circuit', all(position in reached for position in targets), {'reachable_foot_positions': len(reached), 'required': targets})
    check('no_official_slot_or_deployment', metadata['integration']['assigned_slot'] is None and metadata['integration']['id_is_provisional'] and not metadata['integration']['deployed'], metadata['integration'])
    check('shop_explicitly_excluded_from_spawn_points', metadata.get('room_type') == 'shop' and metadata.get('spawn_points') is False and metadata['integration'].get('ready_for_unpatched_core') is False and all('spawn' not in block.name and 'spawner' not in block.name for block in room.blocks.values()), {'room_type': metadata.get('room_type'), 'spawn_points': metadata.get('spawn_points'), 'core_patch_required': metadata['integration'].get('requires_core_patch')})
    dependencies = metadata.get('dependencies', [])
    check('existing_trading_mod_dependency_fingerprint', len(dependencies) == 1 and dependencies[0]['version'] == '1.2.0' and hashlib.sha256((ROOT / 'dependencies' / dependencies[0]['file']).read_bytes()).hexdigest() == dependencies[0]['sha256'], dependencies)

    fingerprints = json.loads((ROOT / 'reference/input-fingerprints.json').read_text(encoding='utf-8'))
    image_checks = []
    for image in fingerprints['images']:
        actual = hashlib.sha256((ROOT / 'reference' / image['file']).read_bytes()).hexdigest()
        image_checks.append(actual == image['sha256'])
    check('reference_fingerprints_current_not_stale_manifest', all(image_checks) and fingerprints['layout_checked_actual_size'] == [1536, 1324], fingerprints)
    result = {'status': 'PASS' if all(item['status'] == 'PASS' for item in checks) else 'FAIL', 'structure_sha256': fingerprint, 'checks': checks, 'limitations': ['Static air-cell BFS is not a Minecraft collision or survival engine; native Forge GameTest is required separately.', 'No core catalog, production world, player client or merchant trading logic is exercised by this report.']}
    (ROOT / 'qa/static.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'status': result['status'], 'checks': len(checks), 'failed': [item['name'] for item in checks if item['status'] != 'PASS']}, ensure_ascii=False))
    if result['status'] != 'PASS':
        raise SystemExit(1)


if __name__ == '__main__':
    main()
