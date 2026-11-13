import hashlib
import json
import sys
from collections import Counter, deque
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
NBT_TOOLS = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
sys.path.insert(0, str(NBT_TOOLS))
from nbtio import Structure, plain, read_nbt

REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S11-nether-burnt.png')
REFERENCE_SHA = '2bdda4f75bf87f19ae80540a5de09af4b720dde05e515cdcd2631fba795d65a6'
SIZE = (13, 12, 16)
ENTRANCE = (6, 3, 0)
EXIT = (6, 3, 15)
FORBIDDEN = {'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void', 'jigsaw', 'spawner', 'tnt', 'chest', 'trapped_chest', 'barrel', 'dispenser', 'dropper', 'hopper', 'fire', 'soul_fire', 'magma_block', 'campfire', 'soul_campfire', 'cobweb', 'powder_snow', 'water'}


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def dump(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def prepare():
    assert digest(REFERENCE) == REFERENCE_SHA, 'Reference hash mismatch'
    room = Structure.read(ROOT / 'room.nbt')
    metadata = {
        'id': 'S11', 'delivery_id': 'corridor-S11', 'name': '烬狱吊笼短廊',
        'kind': 'short', 'theme': 'nether', 'condition': 'burnt', 'length_blocks': 16,
        'size': list(SIZE), 'entrance': list(ENTRANCE), 'interior': [6, 3, 3], 'facing': 'north',
        'exit': list(EXIT), 'exit_interior': [6, 3, 12], 'exit_facing': 'south',
        'ports': [
            {'name': 'north', 'feet': list(ENTRANCE), 'facing': 'north', 'clear_width': 5, 'clear_height': 4},
            {'name': 'south', 'feet': list(EXIT), 'facing': 'south', 'clear_width': 5, 'clear_height': 4},
        ],
        'walkway': {'axis': 'z', 'feet_y': 3, 'x_range_inclusive': [4, 8], 'z_range_inclusive': [0, 15], 'turns': 0, 'branches': 0, 'stairs': 0},
        'sha256': digest(ROOT / 'room.nbt'), 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_SHA,
        'reference_documents': [str(REFERENCE.parents[1] / name) for name in ['README.md', 'manifest.json', 'SPEC.md', 'out/corridor_index.json']],
        'minecraft': '1.20.1', 'data_version': 3465, 'compression': 'gzip', 'palette': 'single',
        'features': {'hanging_cages': 4, 'wall_torches': 6, 'roof_anchored_chains': 8, 'sealed_lava_cells': 24},
        'safety_adaptation': '原图敞口侧沟改为有底、四面封边、橙色玻璃齐平盖板的真实熔岩槽；主路五格宽；吊笼底部高于三格通行净空。',
        'roof': 'Complete permanent roof at y=11; cutaways affect preview only.',
        'placement_policy': 'Independent candidate only. Validate the complete target volume before any future integration; never clear an occupied player world.',
        'deployed': False, 'player_saves_touched': False, 'core_catalog_modified': False,
    }
    dump(ROOT / 'room.json', metadata)
    Structure((1, 1, 1)).write(ROOT / 'forge/src/main/resources/data/fts11qa/structures/empty.nbt')
    return room, metadata


def static_check(room, metadata):
    checks = {}

    def verify(name, condition):
        checks[name] = bool(condition)
        assert condition, name

    def block_name(position):
        return room.get(position).name.removeprefix('minecraft:')

    verify('gzip_signature', (ROOT / 'room.nbt').read_bytes()[:2] == b'\x1f\x8b')
    _, tag = read_nbt(ROOT / 'room.nbt')
    values = plain(tag)
    verify('java_1_20_1_single_palette', room.data_version == 3465 and 'palette' in values and 'palettes' not in values)
    verify('size_13x12x16', room.size == SIZE)
    verify('explicit_complete_volume_unique_and_in_bounds', len(room.blocks) == 2496 and all(all(0 <= coordinate < limit for coordinate, limit in zip(position, SIZE)) for position in room.blocks))
    verify('vanilla_namespace_only', all(block.name.startswith('minecraft:') for block in room.blocks.values()))
    verify('no_entities_block_entities_loot_or_forbidden_blocks', not room.entities and not room.block_entities and not any(block.name.removeprefix('minecraft:') in FORBIDDEN for block in room.blocks.values()))
    verify('reference_and_structure_hashes_match', metadata['reference_sha256'] == digest(REFERENCE) == REFERENCE_SHA and metadata['sha256'] == digest(ROOT / 'room.nbt'))
    verify('complete_roof', all(block_name((horizontal, 11, depth)) in {'red_nether_bricks', 'cracked_polished_blackstone_bricks'} for horizontal in range(13) for depth in range(16)))
    verify('both_side_walls_closed_no_branches', all(block_name((horizontal, height, depth)) != 'air' for horizontal in [0, 1, 11, 12] for height in range(3, 11) for depth in range(16)))
    verify('straight_five_wide_three_high_dry_level_path', all(block_name((horizontal, height, depth)) == 'air' for horizontal in range(4, 9) for depth in range(16) for height in range(3, 6)))
    verify('continuous_full_block_safe_path_floor', all(block_name((horizontal, 2, depth)) in {'polished_blackstone_bricks', 'cracked_polished_blackstone_bricks', 'nether_bricks'} for horizontal in range(4, 9) for depth in range(16)))
    verify('both_end_ports_five_wide_four_high', all(block_name((horizontal, height, depth)) == 'air' for horizontal in range(4, 9) for depth in [0, 15] for height in range(3, 7)))

    lava = [position for position in room.blocks if block_name(position) == 'lava']
    verify('twenty_four_sealed_source_lava_cells', len(lava) == 24 and all(room.get(position).get('level') == '0' for position in lava))
    sealed = True
    for horizontal, height, depth in lava:
        neighbors = [(horizontal - 1, height, depth), (horizontal + 1, height, depth), (horizontal, height, depth - 1), (horizontal, height, depth + 1)]
        sealed &= block_name((horizontal, height - 1, depth)) == 'obsidian'
        sealed &= block_name((horizontal, height + 1, depth)) == 'orange_stained_glass'
        sealed &= all(block_name(neighbor) in {'lava', 'obsidian', 'polished_blackstone_bricks'} for neighbor in neighbors)
    verify('lava_solid_bottom_sides_and_glass_lid', sealed)
    torches = [position for position in room.blocks if block_name(position) == 'wall_torch']
    verify('six_wall_torches_on_solid_wall', len(torches) == 6 and all(block_name((horizontal - 1 if horizontal == 2 else horizontal + 1, height, depth)) != 'air' for horizontal, height, depth in torches))
    chains = [position for position in room.blocks if block_name(position) == 'chain']
    verify('eight_chains_anchored_to_permanent_roof', len(chains) == 8 and all(height == 10 and block_name((horizontal, 11, depth)) != 'air' and block_name((horizontal, 9, depth)) == 'iron_trapdoor' for horizontal, height, depth in chains))
    counts = Counter(block_name(position) for position in room.blocks)
    cages_intact = all(block_name((start_x + offset_x, height, start_z + offset_z)) == ('iron_bars' if height in [7, 8] else 'iron_trapdoor') for start_x in [3, 8] for start_z in [2, 11] for offset_x in range(2) for offset_z in range(2) for height in range(6, 10))
    verify('four_complete_hanging_cages', counts['iron_trapdoor'] == 32 and counts['iron_bars'] == 38 and cages_intact)

    pending = deque([ENTRANCE])
    visited = {ENTRANCE}
    while pending:
        horizontal, height, depth = pending.popleft()
        for neighbor in [(horizontal - 1, height, depth), (horizontal + 1, height, depth), (horizontal, height, depth - 1), (horizontal, height, depth + 1)]:
            if neighbor in visited or not (4 <= neighbor[0] <= 8 and 0 <= neighbor[2] < 16):
                continue
            if block_name(neighbor) == 'air' and block_name((neighbor[0], neighbor[1] + 1, neighbor[2])) == 'air':
                visited.add(neighbor)
                pending.append(neighbor)
    verify('north_south_and_both_interior_points_mutually_walkable', EXIT in visited and (6, 3, 3) in visited and (6, 3, 12) in visited and len(visited) == 80)
    report = {'id': 'S11', 'status': 'PASS', 'sha256': metadata['sha256'], 'checks': checks, 'size': list(SIZE), 'records': len(room.blocks), 'non_air_records': len(room.blocks) - counts['air'], 'safe_main_path_cells': 80, 'materials': dict(sorted(counts.items())), 'limitations': ['Static block inspection is not a Minecraft client or Forge runtime test.']}
    dump(ROOT / 'qa/static.json', report)
    return report


if __name__ == '__main__':
    built, information = prepare()
    result = static_check(built, information)
    print(json.dumps({'status': result['status'], 'sha256': information['sha256'], 'checks': len(result['checks']), 'records': result['records'], 'safe_path_cells': result['safe_main_path_cells']}, ensure_ascii=False))
