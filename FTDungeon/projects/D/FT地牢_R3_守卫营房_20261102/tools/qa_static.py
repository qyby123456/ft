import json
from collections import Counter, deque

from PIL import Image

from build_room import REFERENCE, REFERENCE_HASH, ROOT, SIZE, digest, write_json
from nbtio import AIR, Structure, plain, read_nbt


FORBIDDEN = {'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'cactus', 'powder_snow', 'cobweb', 'wither_rose', 'sweet_berry_bush', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'light'}
NOT_FULL = {'air', 'red_bed', 'dark_oak_fence', 'iron_bars', 'ladder', 'red_wall_banner', 'chest', 'barrel', 'lantern', 'chain', 'campfire', 'lectern'}


def empty_inventory(value):
    if isinstance(value, dict):
        for key, child in value.items():
            if key in {'LootTable', 'LootTableSeed', 'Book', 'RecordItem'}:
                return False
            if key in {'Items', 'Item'} and child != []:
                return False
            if not empty_inventory(child):
                return False
    elif isinstance(value, list):
        return all(empty_inventory(child) for child in value)
    return True


def main():
    room = Structure.read(ROOT / 'room.nbt')
    _, root = read_nbt(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    blueprint = json.loads((ROOT / 'blueprint.json').read_text(encoding='utf-8'))
    checks = {}

    def check(name, condition):
        checks[name] = bool(condition)

    def full(position):
        name = room.get(tuple(position)).name.removeprefix('minecraft:')
        return name not in NOT_FULL and not name.endswith('_slab') and name not in FORBIDDEN

    def walkable(position):
        pos_x, pos_y, pos_z = position
        return room.get((pos_x, pos_y, pos_z)) == AIR and room.get((pos_x, pos_y + 1, pos_z)) == AIR and full((pos_x, pos_y - 1, pos_z))

    check('gzip_single_palette_1_20_1', (ROOT / 'room.nbt').read_bytes().startswith(b'\x1f\x8b') and 'palette' in root.value and 'palettes' not in root.value and room.data_version == 3465)
    check('dimensions_explicit_full_volume', room.size == SIZE and metadata['size'] == list(SIZE) and len(room.blocks) == 2890)
    check('coordinates_in_bounds', all(all(0 <= value < SIZE[axis] for axis, value in enumerate(position)) for position in room.blocks))
    check('source_hash_matches_metadata', digest(ROOT / 'room.nbt') == metadata['sha256'])
    check('reference_verified', digest(REFERENCE) == REFERENCE_HASH and Image.open(REFERENCE).size == (1536, 1024))
    check('vanilla_namespace_only', all(block.name.startswith('minecraft:') for block in room.blocks.values()))
    check('no_entities_or_forbidden_blocks', not room.entities and all(block.name.removeprefix('minecraft:') not in FORBIDDEN for block in room.blocks.values()))
    check('no_fluids_or_waterlogging', all(block.get('waterlogged', 'false') == 'false' for block in room.blocks.values()))
    check('no_reward_inventory_or_loot_tables', all(empty_inventory(plain(value)) for value in room.block_entities.values()))
    check('complete_roof', all(full((pos_x, 9, pos_z)) for pos_x in range(17) for pos_z in range(17)))
    check('complete_ground_floor', all(full((pos_x, 0, pos_z)) for pos_x in range(17) for pos_z in range(17)))
    check('north_central_entrance_metadata', metadata['entrance'] == [8, 1, 0] and metadata['interior'] == [8, 1, 3] and metadata['facing'] == 'north')
    check('three_wide_three_high_main_aisle', all(room.get(tuple(position)) == AIR for position in blueprint['main_aisle']) and all(full((pos_x, 0, 0)) for pos_x in range(7, 10)))
    entrance = tuple(metadata['entrance'])
    reached, pending = {entrance}, deque([entrance])
    while pending:
        pos_x, pos_y, pos_z = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            neighbor = (pos_x + delta_x, pos_y, pos_z + delta_z)
            if 0 <= neighbor[0] < 17 and 0 <= neighbor[2] < 17 and neighbor not in reached and walkable(neighbor):
                reached.add(neighbor)
                pending.append(neighbor)
    check('entrance_safe_interior_bidirectional', walkable(entrance) and tuple(metadata['interior']) in reached)
    check('table_circuit_bunks_racks_and_storage_access', all(tuple(position) in reached for name in ('required_walkable', 'circulation') for position in blueprint[name]))
    beds_ok = len(blueprint['bunks']) == 8
    for bunk in blueprint['bunks']:
        for part in ('foot', 'head'):
            position = tuple(bunk[part])
            block = room.get(position)
            beds_ok &= block.name == ('minecraft:red_carpet' if part == 'foot' else 'minecraft:white_carpet')
            beds_ok &= full((position[0], position[1] - 1, position[2]))
        beds_ok &= all(room.get(tuple(post)).name == 'minecraft:stripped_dark_oak_log' for post in bunk['posts'])
    check('eight_beds_paired_and_supported_platform_posts', beds_ok)
    check('no_explosive_interactive_beds_or_respawn_anchors', all(not block.name.endswith('_bed') and block.name != 'minecraft:respawn_anchor' for block in room.blocks.values()))
    racks_ok = len(blueprint['racks']) == 2
    for rack in blueprint['racks']:
        racks_ok &= all(room.get(tuple(position)).name == 'minecraft:dark_oak_planks' and full((position[0], position[1] - 1, position[2])) for position in rack['base'])
        racks_ok &= all(room.get(tuple(position)).name == 'minecraft:dark_oak_fence' for position in rack['shafts'])
        racks_ok &= all(room.get(tuple(position)).name == 'minecraft:iron_bars' for position in rack['blades'])
    check('weapon_rack_grounded_load_paths', racks_ok)
    check('lamps_continuous_chain_and_roof_anchor', all(room.get(tuple(lamp['lantern'])).name == 'minecraft:lantern' and full(lamp['anchor']) and all(room.get(tuple(chain)).name == 'minecraft:chain' for chain in lamp['chains']) for lamp in blueprint['lamps']))
    check('extinguished_hearth_only', room.get(tuple(blueprint['hearth'])).name == 'minecraft:campfire' and room.get(tuple(blueprint['hearth'])).get('lit') == 'false')
    check('chest_lids_clear', all(room.get((pos_x, 2, 8)) == AIR for pos_x in (2, 14)))
    counts = Counter(block.name for block in room.blocks.values())
    report = {'status': 'PASS' if all(checks.values()) else 'FAIL', 'structure_sha256': digest(ROOT / 'room.nbt'), 'size': SIZE,
              'records': len(room.blocks), 'non_air': len(room.blocks) - counts['minecraft:air'], 'block_entities': len(room.block_entities),
              'reachable_foot_positions': len(reached), 'checks': checks, 'errors': [name for name, passed in checks.items() if not passed],
              'limitations': ['Static conservative AIR-only path test; exact collision, registry, block entities, survival and rotations require Forge native QA.']}
    write_json(ROOT / 'qa/static.json', report)
    print(json.dumps(report, ensure_ascii=False))
    raise SystemExit(0 if report['status'] == 'PASS' else 1)


if __name__ == '__main__':
    main()
