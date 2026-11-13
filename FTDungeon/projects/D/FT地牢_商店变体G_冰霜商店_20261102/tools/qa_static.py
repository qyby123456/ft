import json
from collections import deque

from build_room import ROOT, SIZE, digest, write_json
from nbtio import AIR, Structure, plain, read_nbt


HAZARDS = {'ice', 'frosted_ice', 'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'cactus', 'powder_snow', 'campfire', 'soul_campfire', 'wither_rose', 'sweet_berry_bush', 'cobweb', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'light'}


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


def run():
    path = ROOT / 'out/room.nbt'
    room = Structure.read(path)
    _, root = read_nbt(path)
    metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    checks = []

    def check(name, passed, detail):
        checks.append({'name': name, 'status': 'PASS' if passed else 'FAIL', 'detail': detail})

    width, height, depth = SIZE
    check('gzip_single_palette_java_1_20_1', path.read_bytes()[:2] == b'\x1f\x8b' and room.data_version == 3465 and 'palette' in root.value and 'palettes' not in root.value, {'data_version': room.data_version})
    check('dimensions_records_and_fingerprint', room.size == SIZE and metadata['size'] == list(SIZE) and len(room.blocks) == width * height * depth and digest(path) == metadata['sha256'], {'size': room.size, 'explicit_records': len(room.blocks), 'sha256': digest(path)})
    hazards = sorted({block.name for block in room.blocks.values() if not block.name.startswith('minecraft:') or block.name.split(':')[1] in HAZARDS or block.get('waterlogged') == 'true'})
    check('vanilla_only_no_fluid_ice_melting_or_hazards', not hazards, hazards)
    check('no_entities_reward_stock_or_loot', not room.entities and all(empty_stock(plain(entity)) for entity in room.block_entities.values()), {'entities': len(room.entities), 'block_entities': len(room.block_entities), 'reward_items': 0})
    check('floor_roof_complete', all(room.get((pos_x, pos_y, pos_z)) != AIR for pos_x in range(width) for pos_z in range(depth) for pos_y in (0, height - 1)), {'floor_and_roof_cells': width * depth * 2})
    unexpected_shell = []
    for pos_x in range(width):
        for pos_z in range(depth):
            if pos_x not in (0, width - 1) and pos_z not in (0, depth - 1):
                continue
            for pos_y in range(1, height - 1):
                is_opening = pos_z == 0 and 7 <= pos_x <= 9 and 1 <= pos_y <= 3
                if (room.get((pos_x, pos_y, pos_z)) == AIR) != is_opening:
                    unexpected_shell.append([pos_x, pos_y, pos_z])
    check('closed_shell_only_north_3x3_entry', not unexpected_shell, unexpected_shell)
    features = {name: {tuple(position) for position in positions} for name, positions in blueprint['features'].items()}
    check('four_independent_floor_to_roof_pillars', len(features['pillars']) == 32 and {(position[0], position[2]) for position in features['pillars']} == {(2, 2), (14, 2), (2, 12), (14, 12)}, {'pillar_records': len(features['pillars']), 'footprints': blueprint['pillar_footprints']})
    blocked_pillars = [position for position in blueprint['pillar_clearance'] if room.get(tuple(position)) != AIR]
    check('pillars_full_height_eight_neighbor_air_ring', not blocked_pillars, {'checked_cells': len(blueprint['pillar_clearance']), 'blocked': blocked_pillars})
    intersections = []
    for group, positions in features.items():
        if group == 'pillars':
            continue
        for other, other_positions in features.items():
            if group < other and positions & other_positions:
                intersections.append([group, other, sorted(positions & other_positions)])
    check('furniture_feature_groups_disjoint', not intersections, intersections)
    check('independent_seven_block_counter', features['counter'] == {(pos_x, 1, 9) for pos_x in range(5, 12)}, sorted(features['counter']))
    for name in ('counter_clearance', 'entrance_to_counter_aisle'):
        blocked = [position for position in blueprint[name] if room.get(tuple(position)) != AIR]
        check(name, not blocked, {'checked_cells': len(blueprint[name]), 'blocked': blocked})
    check('shelves_attached_to_back_wall', all(position[2] == 13 and room.get((position[0], position[1], 14)) != AIR for position in features['shelves'] | features['shelf_decor']), {'shelf_positions': len(features['shelves']), 'back_wall_z': 14})
    displays = []
    for side, first_x in (('west', 1), ('east', 13)):
        footprint = {(pos_x, 1, pos_z) for pos_x in range(first_x, first_x + 3) for pos_z in range(5, 8)}
        base_ok = all(room.get(position).name == 'minecraft:dark_oak_planks' and room.get((position[0], 0, position[2])) != AIR for position in footprint)
        case_ok = all(room.get((pos_x, pos_y, pos_z)).name == 'minecraft:light_blue_stained_glass' for pos_x in range(first_x, first_x + 3) for pos_z in range(5, 8) if pos_x in (first_x, first_x + 2) or pos_z in (5, 7) for pos_y in (2, 3))
        center_x = first_x + 1
        center_ok = room.get((center_x, 2, 6)).name == 'minecraft:sea_lantern' and room.get((center_x, 3, 6)).name == 'minecraft:amethyst_cluster'
        displays.append({'side': side, 'floored_base': base_ok, 'glass_shell': case_ok, 'contained_supported_crystal': center_ok})
    check('two_floored_display_cases_no_intersections', all(item['floored_base'] and item['glass_shell'] and item['contained_supported_crystal'] for item in displays), displays)
    unsupported = []
    for position, block in room.blocks.items():
        name = block.name.split(':')[1]
        needs_floor = name in ('snow', 'white_carpet', 'brewing_stand', 'flower_pot', 'light_weighted_pressure_plate', 'amethyst_cluster', 'large_amethyst_bud', 'small_amethyst_bud')
        if needs_floor:
            below = room.get((position[0], position[1] - 1, position[2]))
            if below == AIR or below.name.endswith('_slab') and below.get('type') not in ('top', 'double'):
                unsupported.append(position)
    check('all_floor_decorations_supported', not unsupported, unsupported)
    lamps_valid = len(blueprint['lamp_supports']) == 2
    for lamp in blueprint['lamp_supports']:
        lantern = tuple(lamp['lantern'])
        chains = [tuple(position) for position in lamp['chains']]
        ceiling = tuple(lamp['ceiling'])
        lamps_valid = lamps_valid and room.get(lantern).name == 'minecraft:soul_lantern' and room.get(lantern).get('hanging') == 'true' and all(room.get(chain).name == 'minecraft:chain' for chain in chains) and room.get(ceiling) != AIR and [position[1] for position in chains] == [6, 7, 8] and ceiling[1] == 9 and lantern[1] == 5 and lantern[2] == 9
    check('cold_lanterns_continuous_ceiling_chains_above_counter', lamps_valid, blueprint['lamp_supports'])
    check('icicles_attached_to_complete_roof', all(room.get(tuple(item['anchor'])) != AIR and room.get(tuple(item['cap'])).name == 'minecraft:blue_ice' and room.get(tuple(item['tip'])).name == 'minecraft:light_blue_stained_glass_pane' for item in blueprint['icicle_supports']), blueprint['icicle_supports'])

    def walkable(position):
        pos_x, pos_y, pos_z = position
        return pos_y == 1 and 0 <= pos_x < width and 0 <= pos_z < depth and room.get(position) == AIR and room.get((pos_x, 2, pos_z)) == AIR and room.get((pos_x, 0, pos_z)) != AIR

    entrance = tuple(metadata['entrance'])
    reached = {entrance}
    pending = deque([entrance])
    while pending:
        pos_x, pos_y, pos_z = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            candidate = (pos_x + offset_x, pos_y, pos_z + offset_z)
            if candidate not in reached and walkable(candidate):
                reached.add(candidate)
                pending.append(candidate)
    required = {tuple(position) for position in blueprint['required_walkable']}
    all_walkable = {(pos_x, 1, pos_z) for pos_x in range(width) for pos_z in range(depth) if walkable((pos_x, 1, pos_z))}
    check('entrance_interior_counter_pillars_and_displays_reachable', walkable(entrance) and tuple(metadata['interior']) in reached and required <= reached, {'reachable': len(reached), 'required': len(required), 'unreachable_required': sorted(required - reached)})
    check('all_empty_ground_walk_cells_connected', reached == all_walkable, {'walkable': len(all_walkable), 'unreachable': sorted(all_walkable - reached)})
    check('main_and_required_routes_non_slippery', all(room.get((position[0], 0, position[2])).name not in ('minecraft:ice', 'minecraft:blue_ice', 'minecraft:packed_ice') for position in required | {(pos_x, 1, pos_z) for pos_x in range(7, 10) for pos_z in range(9)}), {'required_route_cells': len(required)})
    report = {'status': 'PASS' if all(item['status'] == 'PASS' for item in checks) else 'FAIL', 'structure_sha256': digest(path), 'checks': checks, 'passed': sum(item['status'] == 'PASS' for item in checks), 'failed': sum(item['status'] == 'FAIL' for item in checks), 'navigation': {'reachable_ground_cells': len(reached), 'all_walkable_cells': len(all_walkable)}, 'scope': 'Static serialized NBT QA, not native placement or client playtesting.'}
    write_json(ROOT / 'qa/static.json', report)
    print(json.dumps({'status': report['status'], 'passed': report['passed'], 'failed': report['failed'], 'reachable': len(reached)}, ensure_ascii=False))
    if report['failed']:
        for item in checks:
            if item['status'] == 'FAIL':
                print(json.dumps(item, ensure_ascii=False))
        raise SystemExit(1)
    return report


if __name__ == '__main__':
    run()
