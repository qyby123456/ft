from __future__ import annotations

import hashlib
import json
import sys
from collections import Counter, deque
from datetime import datetime, timezone, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
NBT_TOOLS = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
sys.path.insert(0, str(NBT_TOOLS))
from nbtio import Structure, plain, read_nbt

REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M17-crystal-cave-flooded.png')
REFERENCE_HASH = '29a6f9575aae21c6ea8f5863b62cde1aa63e7de9d9273248d82e51ff0325bf50'
SIZE = (17, 13, 41)
AIR = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
PASSABLE = AIR | {'minecraft:glow_lichen'}
FLUIDS = {'minecraft:water', 'minecraft:lava'}
DIRECTIONS = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))
HAZARDS = {'minecraft:fire', 'minecraft:soul_fire', 'minecraft:cobweb', 'minecraft:powder_snow', 'minecraft:magma_block', 'minecraft:campfire', 'minecraft:soul_campfire', 'minecraft:cactus', 'minecraft:sweet_berry_bush'}
FORBIDDEN = {'minecraft:command_block', 'minecraft:chain_command_block', 'minecraft:repeating_command_block', 'minecraft:structure_block', 'minecraft:jigsaw', 'minecraft:spawner', 'minecraft:barrier', 'minecraft:light', 'minecraft:bedrock', 'minecraft:budding_amethyst'}


def dump(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    structure = Structure.read(ROOT / 'room.nbt')
    tag = plain(read_nbt(ROOT / 'room.nbt')[1])
    failures = []
    checks = {}

    def check(label, condition, details=None):
        checks[label] = {'status': 'PASS' if condition else 'FAIL', 'details': details}
        if not condition:
            failures.append(label)

    def name(position):
        state = structure.blocks.get(tuple(position))
        return state.name if state else 'minecraft:air'

    def full(position):
        state_name = name(position)
        return state_name not in AIR | FLUIDS and not state_name.endswith(('_stairs', '_slab', '_bud', '_cluster')) and state_name != 'minecraft:glow_lichen'

    check('reference_sha256', digest(REFERENCE) == REFERENCE_HASH, REFERENCE_HASH)
    check('gzip_single_palette_1_20_1', (ROOT / 'room.nbt').read_bytes()[:2] == b'\x1f\x8b' and tag['DataVersion'] == 3465 and 'palette' in tag and 'palettes' not in tag)
    check('dimensions_and_explicit_volume', tuple(structure.size) == SIZE and len(tag['blocks']) == 17 * 13 * 41 and len(structure.blocks) == len(tag['blocks']), list(SIZE))
    check('all_coordinates_in_bounds', all(all(0 <= coordinate < SIZE[axis] for axis, coordinate in enumerate(position)) for position in structure.blocks))
    check('no_entities_or_block_entities', not tag.get('entities') and not any('nbt' in entry for entry in tag['blocks']))
    counts = Counter(state.name for state in structure.blocks.values())
    check('vanilla_safe_palette', all(state_name.startswith('minecraft:') and state_name not in FORBIDDEN | HAZARDS for state_name in counts))
    check('complete_floor_and_roof', all(full((block_x, height, block_z)) for block_x in range(17) for block_z in range(41) for height in (0, 1, 12)))
    check('sealed_outer_side_walls', all(full((block_x, height, block_z)) for block_x in (0, 16) for height in range(13) for block_z in range(41)))
    check('both_five_by_five_openings', all(name((block_x, height, block_z)) in AIR for block_x in range(6, 11) for height in range(3, 8) for block_z in (0, 40)))
    check('five_wide_flat_dry_spine', all(full((block_x, 2, block_z)) and name((block_x, 3, block_z)) in AIR and name((block_x, 4, block_z)) in AIR for block_x in range(6, 11) for block_z in range(41)), {'width': 5, 'length': 41, 'feet_y': 3})
    reached = {(8, 3, 0)}
    pending = deque(reached)
    while pending:
        current = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            neighbor = (current[0] + delta_x, 3, current[2] + delta_z)
            if neighbor not in reached and 0 <= neighbor[0] < 17 and 0 <= neighbor[2] < 41 and full((neighbor[0], 2, neighbor[2])) and name(neighbor) in PASSABLE and name((neighbor[0], 4, neighbor[2])) in PASSABLE:
                reached.add(neighbor)
                pending.append(neighbor)
    check('entrance_interior_exit_bidirectional', {(8, 3, 0), (8, 3, 3), (8, 3, 20), (8, 3, 37), (8, 3, 40)} <= reached, {'reachable_feet': len(reached)})
    boundary_failures = []
    for position, state in structure.blocks.items():
        if state.name not in FLUIDS:
            continue
        if dict(state.properties).get('level') != '0':
            boundary_failures.append([list(position), 'non-source'])
        for delta in DIRECTIONS:
            neighbor = tuple(position[axis] + delta[axis] for axis in range(3))
            if delta[1] == 1 and state.name == 'minecraft:water':
                continue
            if name(neighbor) != state.name and not full(neighbor):
                boundary_failures.append([list(position), list(neighbor), name(neighbor)])
    check('liquid_source_bottom_and_lateral_seals', not boundary_failures, {'water_sources': counts['minecraft:water'], 'lava_sources': counts['minecraft:lava'], 'boundary_failures': boundary_failures})
    check('one_block_side_pools_no_water_on_route', all(position[1] == 2 and (3 <= position[0] <= 5 or 11 <= position[0] <= 13) and 3 <= position[2] <= 37 for position, state in structure.blocks.items() if state.name == 'minecraft:water'))
    check('lava_only_in_glazed_wall_shafts', counts['minecraft:lava'] == 30 and all(position[0] in (1, 15) and position[1] in (3, 4, 5) and name((2 if position[0] == 1 else 14, position[1], position[2])) == 'minecraft:glass' for position, state in structure.blocks.items() if state.name == 'minecraft:lava'))
    support_errors = []
    facing_delta = {'up': (0, 1, 0), 'down': (0, -1, 0), 'east': (1, 0, 0), 'west': (-1, 0, 0), 'north': (0, 0, -1), 'south': (0, 0, 1)}
    for position, state in structure.blocks.items():
        properties = dict(state.properties)
        if state.name.endswith(('_cluster', '_bud')):
            delta = facing_delta[properties['facing']]
            support = tuple(position[axis] - delta[axis] for axis in range(3))
            if not full(support):
                support_errors.append([list(position), list(support)])
        if state.name == 'minecraft:glow_lichen':
            attached = [direction for direction in facing_delta if properties.get(direction) == 'true']
            if not attached or any(not full(tuple(position[axis] + facing_delta[direction][axis] for axis in range(3))) for direction in attached):
                support_errors.append([list(position), 'lichen'])
    check('crystal_and_lichen_support', not support_errors, support_errors)
    structural = {position for position, state in structure.blocks.items() if state.name not in AIR | FLUIDS and not state.name.endswith(('_bud', '_cluster')) and state.name != 'minecraft:glow_lichen'}
    grounded = {position for position in structural if position[1] == 0}
    pending = deque(grounded)
    while pending:
        current = pending.popleft()
        for delta in DIRECTIONS:
            neighbor = tuple(current[axis] + delta[axis] for axis in range(3))
            if neighbor in structural and neighbor not in grounded:
                grounded.add(neighbor)
                pending.append(neighbor)
    check('solid_components_connected_to_foundation', grounded == structural, {'structural_blocks': len(structural), 'grounded_blocks': len(grounded), 'floating': [list(position) for position in sorted(structural - grounded)]})
    report = {'id': 'M17', 'status': 'PASS' if not failures else 'FAIL', 'timestamp': datetime.now(timezone(timedelta(hours=8))).isoformat(), 'sha256': digest(ROOT / 'room.nbt'), 'scope': 'Static NBT checks, not native collision or gameplay proof', 'checks': checks, 'failures': failures, 'block_counts': dict(counts)}
    dump(ROOT / 'qa/static-qa.json', report)
    metadata = {
        'id': 'M17', 'batch': 'dungeon-corridors-20261103', 'kind': 'straight_medium_corridor', 'name': '紫晶浅水拱廊',
        'size': list(SIZE), 'entrance': [8, 3, 0], 'interior': [8, 3, 3], 'exit': [8, 3, 40], 'exit_interior': [8, 3, 37],
        'facing': 'north', 'exit_facing': 'south', 'sha256': report['sha256'], 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH,
        'minecraft_version': '1.20.1', 'data_version': 3465, 'generator': 'minecraft-builder-skill / Lodestone 0.6.0',
        'route': {'axis': '+Z', 'width': 5, 'x_inclusive': [6, 10], 'feet_y': 3, 'floor_y': 2, 'headroom_min': 5, 'length_blocks': 41, 'length_chunks': 41 / 16, 'stairs': False, 'branches': False, 'dry': True},
        'liquids': {'water_y': 2, 'water_depth_blocks': 1, 'water_sources': counts['minecraft:water'], 'lava_sources': counts['minecraft:lava'], 'lava_glazed': True, 'source_only': True, 'water_sealed_from_ports': True},
        'containers': 0, 'entities': 0, 'block_entities': 0, 'full_volume_air_explicit': True,
        'design_adaptations': ['保留灰石紫晶、浅水、六道拱、苔痕与橙色熔岩壁光。', '参考图中心浸水改为五格宽等高干燥石脊，两侧为一格深浅水，保证双向步行。', '裸露熔岩改为完整玻璃封闭的壁内竖槽，底、顶、两端与背面均密封。', '原版紫晶不产生紫色动态光；使用紫晶、紫玻璃与珠光蛙明灯表现，不承诺光影包反射。', '破损以裂纹、侵蚀和苔石表现，拱券仍与墙顶连续连接。'],
        'integration': {'deployed': False, 'player_worlds_modified': False, 'global_slot_assigned': False, 'overwrite_policy': 'Integration must reject occupied complete volume including explicit air; never clear a player world unconditionally.', 'port_floor_top_y': 3, 'require_connector_feet_y': 3},
    }
    dump(ROOT / 'room.json', metadata)
    dump(ROOT / 'qa/materials.json', {'sha256': report['sha256'], 'counts_including_air_and_liquids': dict(counts)})
    print(json.dumps({'status': report['status'], 'sha256': report['sha256'], 'checks': len(checks), 'failures': failures, 'water': counts['minecraft:water'], 'lava': counts['minecraft:lava']}, ensure_ascii=False))
    if failures:
        raise SystemExit(1)


if __name__ == '__main__':
    main()
