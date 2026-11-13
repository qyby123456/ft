import copy
import hashlib
import json
import math
import zipfile
from collections import deque
from pathlib import Path

from nbtio import AIR, Structure, plain, read_nbt, state


ROOT = Path(__file__).resolve().parents[1]
CLIENT = Path('C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar')
FORBIDDEN = {'water', 'fire', 'soul_fire', 'magma_block', 'campfire', 'soul_campfire', 'cactus', 'powder_snow', 'sweet_berry_bush', 'wither_rose', 'cobweb', 'spawner', 'command_block', 'repeating_command_block', 'chain_command_block', 'jigsaw', 'structure_block', 'structure_void', 'light', 'tnt', 'respawn_anchor'}
FULL_SUPPORT = {'deepslate_tiles', 'polished_deepslate', 'gold_block', 'obsidian', 'polished_blackstone_bricks', 'cracked_polished_blackstone_bricks', 'chiseled_polished_blackstone', 'gilded_blackstone', 'glass', 'red_nether_bricks', 'black_terracotta', 'red_terracotta'}
BLOCK_ENTITIES = {'skeleton_skull': 'skull', 'black_wall_banner': 'banner', 'chest': 'chest', 'brewing_stand': 'brewing_stand'}


def require(condition, message):
    if not condition:
        raise AssertionError(message)


def name(block):
    return block.name.removeprefix('minecraft:')


def dry_air(room, position):
    return room.get(position) == AIR


def walkable(room, feet):
    pos_x, pos_y, pos_z = feet
    return dry_air(room, feet) and dry_air(room, (pos_x, pos_y + 1, pos_z)) and name(room.get((pos_x, pos_y - 1, pos_z))) in FULL_SUPPORT


def lava_enclosed(room, blueprint):
    for cell in blueprint['lava_cells']:
        require(name(room.get(tuple(cell['source']))) == 'lava', 'Lava source missing')
        source = tuple(cell['source'])
        expected = {tuple(source[axis] + direction[axis] for axis in range(3)) for direction in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))}
        require(expected == {tuple(position) for position in cell['enclosure']}, 'Enclosure is not all six adjacent faces')
        require(all(name(room.get(position)) in {'obsidian', 'glass'} for position in expected), 'Broken lava enclosure')


def lamps_supported(room, blueprint):
    for lamp in blueprint['lamp_supports']:
        require(name(room.get(tuple(lamp['lantern']))) == 'lantern', 'Lantern missing')
        require(room.get(tuple(lamp['lantern'])).get('hanging') == 'true', 'Lantern is not hanging')
        require(all(name(room.get(tuple(position))) == 'chain' for position in lamp['chains']), 'Lantern chain broken')
        require(name(room.get(tuple(lamp['beam']))) == 'polished_deepslate', 'Lantern beam missing')
        require(name(room.get(tuple(lamp['roof']))) == 'deepslate_tiles', 'Beam roof missing')
        ordered = [lamp['lantern'], *lamp['chains'], lamp['beam'], lamp['roof']]
        require(all(after[0] == before[0] and after[2] == before[2] and after[1] == before[1] + 1 for before, after in zip(ordered, ordered[1:])), 'Hanging path not continuous')


def no_stock(value):
    if isinstance(value, dict):
        for key, child in value.items():
            require(key not in {'LootTable', 'LootTableSeed', 'Item', 'RecordItem', 'Book', 'Owner', 'SkullOwner', 'Command'}, 'Unwanted stock, owner or command payload')
            if key == 'Items':
                require(child == [], 'Nonempty inventory')
            no_stock(child)
    elif isinstance(value, list):
        for child in value:
            no_stock(child)


def main():
    source = ROOT / 'out/room.nbt'
    metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    room = Structure.read(source)
    _, encoded = read_nbt(source)
    raw = plain(encoded)
    checks = []

    def check(identity, callback):
        callback()
        checks.append({'id': identity, 'status': 'PASS'})

    check('gzip_single_palette_1_20_1', lambda: require(source.read_bytes()[:2] == b'\x1f\x8b' and raw['DataVersion'] == 3465 and 'palette' in raw and 'palettes' not in raw, 'Wrong NBT envelope'))
    check('metadata_sha256_and_real_dimensions', lambda: require(list(room.size) == metadata['size'] == [17, 11, 17] and metadata['sha256'] == hashlib.sha256(source.read_bytes()).hexdigest(), 'Metadata differs'))
    check('full_volume_unique_in_bounds', lambda: require(len(room.blocks) == len(raw['blocks']) == math.prod(room.size) and all(all(0 <= coordinate < limit for coordinate, limit in zip(position, room.size)) for position in room.blocks), 'Incomplete or out of bounds volume'))
    check('no_preplaced_entities', lambda: require(not room.entities, 'Preplaced entities'))
    with zipfile.ZipFile(CLIENT) as archive:
        available = set(archive.namelist())
    check('all_vanilla_names_have_1_20_1_blockstates', lambda: require(all(block.name.startswith('minecraft:') and (block == AIR or 'assets/minecraft/blockstates/' + name(block) + '.json' in available) for block in room.blocks.values()), 'Unknown block; native registry test follows separately'))
    check('forbidden_blocks_and_waterlogging', lambda: require(all(name(block) not in FORBIDDEN and block.get('waterlogged', 'false') == 'false' for block in room.blocks.values()), 'Forbidden block or waterlogged state'))
    check('only_two_declared_lava_sources', lambda: require({position for position, block in room.blocks.items() if name(block) == 'lava'} == {tuple(cell['source']) for cell in blueprint['lava_cells']} and len(blueprint['lava_cells']) == 2 and all(room.get(tuple(cell['source'])).get('level') == '0' for cell in blueprint['lava_cells']), 'Unexpected lava'))
    check('six_sided_fireproof_lava_containment', lambda: lava_enclosed(room, blueprint))
    check('no_combustible_structure_materials', lambda: require(all(not any(token in name(block) for token in ('_planks', '_log', '_wood', '_wool', 'bookshelf', '_carpet', 'leaves')) for block in room.blocks.values()), 'Combustible material'))
    check('full_solid_floor_and_roof', lambda: require(all(name(room.get((pos_x, pos_y, pos_z))) in FULL_SUPPORT for pos_x in range(17) for pos_z in range(17) for pos_y in (0, 10)), 'Floor or roof hole'))
    check('north_interface_3_by_3_with_floor', lambda: require(metadata['entrance'] == [8, 1, 0] and metadata['facing'] == 'north' and all(dry_air(room, (pos_x, pos_y, 0)) for pos_x in range(7, 10) for pos_y in range(1, 4)) and all(walkable(room, (pos_x, 1, 0)) for pos_x in range(7, 10)), 'Bad north interface'))
    check('closed_outer_shell_except_north_entry', lambda: require(all(not dry_air(room, (pos_x, pos_y, pos_z)) for pos_x in range(17) for pos_z in range(17) for pos_y in range(1, 10) if (pos_x in (0, 16) or pos_z in (0, 16)) and not (pos_z == 0 and 7 <= pos_x <= 9 and 1 <= pos_y <= 3)), 'Unexpected shell opening'))
    check('main_3_wide_3_high_aisle', lambda: require(all(dry_air(room, tuple(position)) for position in blueprint['entrance_to_counter_aisle']), 'Main aisle blocked'))
    check('counter_four_sided_clearance', lambda: require(all(dry_air(room, tuple(position)) for position in blueprint['counter_clearance']), 'Counter circulation blocked'))
    features = {key: {tuple(position) for position in positions} for key, positions in blueprint['features'].items()}
    check('structural_pillars_only_on_wall_lines', lambda: require(all(position[0] in (0, 16) or position[2] in (0, 16) for position in features['pillars']), 'Freestanding structural pillar'))
    check('no_pillar_furniture_contact', lambda: require(all(sum(abs(position[axis] - other[axis]) for axis in range(3)) > 1 for position in features['pillars'] for group in ('counter', 'shelves', 'display_bases', 'empty_storage') for other in features[group]), 'Pillar contacts furniture'))
    check('furniture_groups_never_overlap', lambda: require(sum(len(positions) for positions in features.values()) == len(set().union(*features.values())), 'Overlapping feature groups'))
    declared_lava = {tuple(cell['source']) for cell in blueprint['lava_cells']}
    check('wall_shelf_backing_and_legs', lambda: require(all(name(room.get((pos_x, pos_y, 15))) in FULL_SUPPORT or (pos_x, pos_y, 15) in declared_lava for pos_x in range(1, 16) for pos_y in range(1, 8)) and all((pos_x, pos_y, 14) in features['shelves'] for pos_x in (1, 5, 11, 15) for pos_y in range(1, 7)), 'Shelf backing/legs missing'))
    check('continuous_lantern_chain_beam_roof', lambda: lamps_supported(room, blueprint))
    check('lamps_only_over_clear_passage', lambda: require(all(lamp['lantern'][0] in (7, 9) and lamp['lantern'][2] == 6 and all(dry_air(room, (lamp['lantern'][0], pos_y, 6)) for pos_y in range(1, 5)) for lamp in blueprint['lamp_supports']), 'Lamp above furniture or path obstruction'))
    check('wall_banner_backing_and_payload', lambda: require(all(name(room.get(tuple(lamp['backing']))) in FULL_SUPPORT for lamp in blueprint['banner_supports']) and all(plain(payload).get('Patterns') == [{'Pattern': 'mr', 'Color': 14}, {'Pattern': 'cs', 'Color': 14}, {'Pattern': 'bo', 'Color': 4}] for position, payload in room.block_entities.items() if name(room.get(position)) == 'black_wall_banner'), 'Unsupported or wrong banner'))
    for position in features['shelf_decor'] | features['counter_decor'] | features['display_artifacts']:
        support = room.get((position[0], position[1] - 1, position[2]))
        require(name(support) in FULL_SUPPORT or name(support).endswith('_slab') and support.get('type') in {'top', 'double'}, 'Decoration lacks a solid top support')
    checks.append({'id': 'shelf_counter_and_exhibit_decor_solid_top_support', 'status': 'PASS'})
    for position, block in room.blocks.items():
        require((name(block) in BLOCK_ENTITIES) == (position in room.block_entities), 'Block entity presence mismatch')
        if position in room.block_entities:
            require(plain(room.block_entities[position])['id'] == 'minecraft:' + BLOCK_ENTITIES[name(block)], 'Block entity identity mismatch')
            no_stock(plain(room.block_entities[position]))
    checks.append({'id': 'all_block_entities_empty_and_legal_payload', 'status': 'PASS', 'count': len(room.block_entities)})
    reached = {tuple(metadata['entrance'])}
    pending = deque(reached)
    while pending:
        pos_x, pos_y, pos_z = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            following = (pos_x + delta_x, pos_y, pos_z + delta_z)
            if 0 <= following[0] < 17 and 0 <= following[2] < 17 and following not in reached and walkable(room, following):
                reached.add(following)
                pending.append(following)
    check('entrance_safe_point_counter_loop_storage_reachable', lambda: require({tuple(position) for position in blueprint['required_walkable']} | {tuple(metadata['interior'])} <= reached, 'Required destination unreachable'))
    check('chest_open_space', lambda: require(all(dry_air(room, (position[0], position[1] + 1, position[2])) for position in features['empty_storage']), 'Chest cannot open'))
    for label, mutation, validator in (
        ('broken_lava_guard_rejected', lambda altered: altered.set(tuple(blueprint['lava_cells'][0]['glass']), AIR), lambda altered: lava_enclosed(altered, blueprint)),
        ('broken_lantern_chain_rejected', lambda altered: altered.set(tuple(blueprint['lamp_supports'][0]['chains'][0]), AIR), lambda altered: lamps_supported(altered, blueprint)),
        ('blocked_entry_rejected', lambda altered: altered.set((8, 1, 2), state('obsidian')), lambda altered: require(walkable(altered, (8, 1, 2)), 'Blocked aisle')),
    ):
        altered = copy.deepcopy(room)
        mutation(altered)
        rejected = False
        try:
            validator(altered)
        except AssertionError:
            rejected = True
        require(rejected, 'Negative control falsely passed: ' + label)
        checks.append({'id': label, 'status': 'PASS'})
    check('standalone_not_registered_or_deployed', lambda: require(metadata['integration']['assigned_slot'] is None and metadata['integration']['deployed'] is False and metadata['integration']['id_is_provisional'] is True, 'Unauthorized integration state'))
    report = {'status': 'PASS', 'structure_sha256': metadata['sha256'], 'size': room.size, 'block_records': len(room.blocks), 'reachable_foot_positions': len(reached), 'checks': checks, 'checks_passed': len(checks), 'limitations': ['Static support, air-only reachability and asset names; native registry, collision shapes, neighbor updates and fluid ticks are separately tested.']}
    (ROOT / 'qa/static.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'status': report['status'], 'checks': len(checks), 'reachable': len(reached)}))


if __name__ == '__main__':
    main()
