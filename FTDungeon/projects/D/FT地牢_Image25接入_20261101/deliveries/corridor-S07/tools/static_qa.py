import argparse
import hashlib
import json
import sys
from collections import Counter, deque
from pathlib import Path

NBT_TOOLS = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
sys.path.insert(0, str(NBT_TOOLS))
from nbtio import Structure, plain, read_nbt

ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S07-crystal-cave-flooded.png')
REFERENCE_HASH = '7775b4e93df8aca46065fcc18c12ceba8cf20979d263ceaf2947f539b9759be9'
AIR = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
SOLID = {'minecraft:stone_bricks', 'minecraft:mossy_stone_bricks', 'minecraft:cracked_stone_bricks', 'minecraft:andesite', 'minecraft:polished_andesite', 'minecraft:chiseled_stone_bricks', 'minecraft:deepslate_tiles', 'minecraft:polished_deepslate', 'minecraft:amethyst_block', 'minecraft:calcite'}
FACING = {'east': (-1, 0, 0), 'west': (1, 0, 0), 'up': (0, -1, 0), 'down': (0, 1, 0), 'north': (0, 0, 1), 'south': (0, 0, -1)}


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--metadata-only', action='store_true')
    arguments = parser.parse_args()
    structure = Structure.read(ROOT / 'room.nbt')
    digest = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    metadata = {
        'id': 'corridor-S07', 'concept_id': 'S07', 'name': '紫晶积水短廊',
        'size': list(structure.size), 'entrance': [6, 2, 0], 'interior': [6, 2, 3],
        'exit': [6, 2, 15], 'facing': 'north', 'axis': 'z', 'length_blocks': 16,
        'length_chunks': 1, 'walkway_width': 3, 'walkway_feet_y': 2,
        'sha256': digest, 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH,
        'reference_documents': [str(REFERENCE.parents[1] / 'README.md'), str(REFERENCE.parents[1] / 'SPEC.md'), str(REFERENCE.parents[1] / 'manifest.json')],
        'minecraft_version': '1.20.1', 'data_version': 3465, 'format': 'gzip Java single-palette structure',
        'water': {'source_y': 1, 'depth_blocks': 1, 'basins': [{'min': [2, 1, 2], 'max': [4, 1, 13]}, {'min': [8, 1, 2], 'max': [10, 1, 13]}], 'expected_sources': 72},
        'safe_points': [[6, 2, depth] for depth in [0, 3, 8, 12, 15]],
        'apertures': {'north': {'min': [5, 2, 0], 'max': [7, 5, 0]}, 'south': {'min': [5, 2, 15], 'max': [7, 5, 15]}},
        'intentional_deviations': ['全幅积水改为两侧一格深密封浅水池，中间三格石面干燥直路，不要求玩家涉水。', '封闭铁栅门改为两侧紫晶栅窗和中央开放拱口，保证一字贯通。', '原版紫水晶不发光；照明来自末地烛，不伪称有着色光或光影反射。'],
        'features': ['苔石砖/裂石砖双层外壳', '两座顶链悬吊铁笼', '十二支末地烛冷白灯条', '双侧晶簇与屋顶紫晶脉', '完整屋顶/地基/双端门槛'],
        'integration': {'deployed': False, 'player_save_modified': False, 'catalog_slot': None, 'note': '独立候选，唯一槽位与正式接入交给统一交接线程分配。'},
        'generator': str(ROOT / 'source/corridor-s07.mjs'),
    }
    write_json(ROOT / 'room.json', metadata)
    if arguments.metadata_only:
        return

    checks = []

    def require(name, condition, **evidence):
        checks.append({'check': name, 'status': 'PASS' if condition else 'FAIL', **evidence})

    def block_name(position):
        return structure.blocks[position].name if position in structure.blocks else 'MISSING'

    def solid(position):
        return block_name(position) in SOLID

    def walkable(position):
        horizontal, vertical, depth = position
        return block_name(position) in AIR and block_name((horizontal, vertical + 1, depth)) in AIR and solid((horizontal, vertical - 1, depth))

    _, raw_tag = read_nbt(ROOT / 'room.nbt')
    raw = plain(raw_tag)
    require('reference_sha256', hashlib.sha256(REFERENCE.read_bytes()).hexdigest() == REFERENCE_HASH)
    require('gzip_single_palette_1_20_1', (ROOT / 'room.nbt').read_bytes()[:2] == b'\x1f\x8b' and raw['DataVersion'] == 3465 and 'palette' in raw and 'palettes' not in raw)
    require('size_and_complete_volume', structure.size == (13, 11, 16) and len(structure.blocks) == 2288 and len(raw['blocks']) == 2288, size=list(structure.size), records=len(structure.blocks))
    require('no_entities_or_block_entities', not structure.entities and not structure.block_entities)
    require('only_allowed_palette', all(block.name in SOLID | AIR | {'minecraft:water', 'minecraft:stone_brick_stairs', 'minecraft:end_rod', 'minecraft:chain', 'minecraft:iron_bars', 'minecraft:amethyst_cluster', 'minecraft:large_amethyst_bud', 'minecraft:medium_amethyst_bud'} for block in structure.blocks.values()))
    require('full_roof_and_base', all(solid((horizontal, vertical, depth)) for horizontal in range(13) for depth in range(16) for vertical in (0, 10)))
    require('continuous_three_wide_dry_route', all(walkable((horizontal, 2, depth)) and block_name((horizontal, 4, depth)) in AIR and block_name((horizontal, 5, depth)) in AIR for horizontal in range(5, 8) for depth in range(16)), width=3, length=16, clear_height=4)
    reached = {tuple(metadata['entrance'])}
    pending = deque(reached)
    while pending:
        horizontal, vertical, depth = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            neighbor = (horizontal + delta_x, vertical, depth + delta_z)
            if neighbor not in reached and walkable(neighbor):
                reached.add(neighbor)
                pending.append(neighbor)
    require('entrance_exit_all_safe_points_bidirectional', all(tuple(position) in reached for position in metadata['safe_points']), reachable_cells=len(reached))
    water = {position for position, block in structure.blocks.items() if block.name == 'minecraft:water'}
    expected = {(horizontal, 1, depth) for horizontal in (2, 3, 4, 8, 9, 10) for depth in range(2, 14)}
    require('exact_shallow_source_water', water == expected and all(dict(structure.blocks[position].properties).get('level') == '0' for position in water), source_count=len(water), depth=1)
    require('water_containment_floor_and_four_sides', all(solid((horizontal, vertical - 1, depth)) and all((horizontal + delta_x, vertical, depth + delta_z) in water or solid((horizontal + delta_x, vertical, depth + delta_z)) for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1))) for horizontal, vertical, depth in water))
    invalid_support = []
    for position, block in structure.blocks.items():
        name = block.name.removeprefix('minecraft:')
        if name in ('amethyst_cluster', 'large_amethyst_bud', 'medium_amethyst_bud', 'end_rod'):
            delta = FACING[dict(block.properties)['facing']]
            support = tuple(position[axis] + delta[axis] for axis in range(3))
            if not solid(support):
                invalid_support.append({'position': position, 'support': support})
        if name == 'chain':
            horizontal, vertical, depth = position
            if not solid((horizontal, vertical + 1, depth)) or not solid((horizontal, vertical - 1, depth)):
                invalid_support.append({'position': position, 'reason': 'chain not meeting roof and cage cap'})
    require('crystals_lights_cage_chains_supported', not invalid_support, unsupported=invalid_support)
    counts = Counter(block.name.removeprefix('minecraft:') for block in structure.blocks.values())
    require('reference_theme_present', counts['end_rod'] == 12 and counts['chain'] == 8 and counts['iron_bars'] == 32 and counts['amethyst_cluster'] == 6, palette_counts=dict(sorted(counts.items())))
    report = {'id': metadata['id'], 'status': 'PASS' if all(check['status'] == 'PASS' for check in checks) else 'FAIL', 'sha256': digest, 'checks': checks, 'scope': 'NBT static geometry and topology; not a native Minecraft placement result'}
    write_json(ROOT / 'qa/static.json', report)
    print(json.dumps({'status': report['status'], 'sha256': digest, 'checks': len(checks), 'failures': [check for check in checks if check['status'] != 'PASS']}, ensure_ascii=False))
    raise SystemExit(0 if report['status'] == 'PASS' else 1)


if __name__ == '__main__':
    main()
