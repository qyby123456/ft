import hashlib
import json
from collections import Counter, deque
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo


ROOT = Path(__file__).resolve().parents[1]
SHARED = ROOT / 'tools'
from nbtio import Structure, plain, read_nbt


REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S05-blackstone-deepslate-flooded.png')
REFERENCE_HASH = '07435538db7e9d2361a768877170d8b9e622b7e49109fca0ec677e35ecf95c1a'
SOLID = {'deepslate_tiles', 'deepslate_bricks', 'polished_blackstone', 'polished_blackstone_bricks', 'cracked_polished_blackstone_bricks', 'chiseled_polished_blackstone', 'mossy_stone_bricks'}
ALLOWED = SOLID | {'air', 'water', 'wall_torch', 'polished_blackstone_brick_stairs', 'iron_bars', 'iron_trapdoor', 'chain'}


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def write_json(path, value):
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    Path(path).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    room = Structure.read(ROOT / 'room.nbt')
    _, encoded = read_nbt(ROOT / 'room.nbt')
    raw = plain(encoded)
    checks = {}
    failures = []

    def check(label, condition, detail=None):
        checks[label] = {'status': 'PASS' if condition else 'FAIL', 'detail': detail}
        if not condition:
            failures.append(label)

    def name(position):
        return room.get(position).name.removeprefix('minecraft:')

    check('reference_fingerprint', digest(REFERENCE) == REFERENCE_HASH)
    check('gzip_single_palette_1201', (ROOT / 'room.nbt').read_bytes()[:2] == b'\x1f\x8b' and room.data_version == 3465 and 'palette' in raw and 'palettes' not in raw)
    check('dimensions_one_chunk_straight', room.size == (11, 10, 16))
    check('complete_explicit_volume', len(room.blocks) == 1760 and all(all(0 <= value < limit for value, limit in zip(position, room.size)) for position in room.blocks))
    check('only_safe_vanilla_blocks', all(block.name.startswith('minecraft:') and block.name.removeprefix('minecraft:') in ALLOWED for block in room.blocks.values()))
    check('no_entities_containers_or_payloads', not room.entities and not room.block_entities)
    check('full_floor_and_roof', all(name((pos_x, pos_y, pos_z)) in SOLID for pos_x in range(11) for pos_z in range(16) for pos_y in (0, 9)))
    check('full_lateral_shell', all(name((pos_x, pos_y, pos_z)) in SOLID for pos_x in (0, 1, 9, 10) for pos_y in range(1, 9) for pos_z in range(16)))
    required_feet = [[pos_x, 2, pos_z] for pos_z in range(16) for pos_x in range(4, 7)]
    check('three_wide_three_high_dry_route', all(name((pos_x, pos_y, pos_z)) == 'air' for pos_x, _, pos_z in required_feet for pos_y in range(2, 5)) and all(name((pos_x, 1, pos_z)) in SOLID for pos_x, _, pos_z in required_feet))
    reached = {(5, 2, 0)}
    queue = deque(reached)
    while queue:
        pos_x, pos_y, pos_z = queue.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            candidate = pos_x + delta_x, pos_y, pos_z + delta_z
            next_x, _, next_z = candidate
            if candidate not in reached and 0 <= next_x < 11 and 0 <= next_z < 16 and name(candidate) == 'air' and name((next_x, 3, next_z)) == 'air' and name((next_x, 1, next_z)) in SOLID:
                reached.add(candidate)
                queue.append(candidate)
    check('north_south_safe_points_connected', set(map(tuple, required_feet)).issubset(reached), {'reachable_dry_feet': len(reached)})
    waters = [position for position in room.blocks if name(position) == 'water']
    seal_errors = []
    for pos_x, pos_y, pos_z in waters:
        if pos_y != 1 or pos_x not in (2, 3, 7, 8) or not 1 <= pos_z <= 14 or dict(room.get((pos_x, pos_y, pos_z)).properties).get('level') != '0':
            seal_errors.append([pos_x, pos_y, pos_z])
        for delta_x, delta_y, delta_z in ((0, -1, 0), (-1, 0, 0), (1, 0, 0), (0, 0, -1), (0, 0, 1)):
            adjacent = pos_x + delta_x, pos_y + delta_y, pos_z + delta_z
            if name(adjacent) not in SOLID | {'water'}:
                seal_errors.append(list(adjacent))
    check('water_bed_horizontal_seals_source_envelope', len(waters) == 52 and not seal_errors, {'water_sources': len(waters), 'errors': seal_errors})
    torch_errors = []
    anchors = []
    for position, block in room.blocks.items():
        pos_x, pos_y, pos_z = position
        properties = dict(block.properties)
        if name(position) == 'wall_torch':
            offset = {'east': (-1, 0), 'west': (1, 0), 'north': (0, 1), 'south': (0, -1)}[properties['facing']]
            if name((pos_x + offset[0], pos_y, pos_z + offset[1])) not in SOLID:
                torch_errors.append(list(position))
        if name(position) == 'chain' and pos_y == 8:
            anchors.append([pos_x, pos_y, pos_z])
    check('torches_and_cage_chains_supported', not torch_errors and len(anchors) == 4 and all(name((pos_x, 9, pos_z)) in SOLID and name((pos_x, 7, pos_z)) == 'chain' and name((pos_x, 6, pos_z)) == 'iron_bars' and name((pos_x, 5, pos_z)) == 'iron_bars' and name((pos_x, 4, pos_z)) == 'iron_trapdoor' for pos_x, _, pos_z in anchors), {'cage_roof_anchors': anchors})
    check('pillars_beams_grounded', all(name((pos_x, pos_y, pos_z)) in SOLID for pos_x in (2, 8) for pos_z in (0, 5, 10, 15) for pos_y in range(9)))
    fingerprint = digest(ROOT / 'room.nbt')
    metadata = {
        'id': 'corridor-S05', 'design_id': 'S05', 'name': '黑石积水吊笼短廊',
        'size': list(room.size), 'entrance': [5, 2, 0], 'interior': [5, 2, 3], 'exit': [5, 2, 15],
        'facing': 'north', 'axis': '+Z', 'length_blocks': 16, 'length_chunks': 1,
        'ports': [{'side': 'north', 'feet': [5, 2, 0], 'width': 3, 'height': 4}, {'side': 'south', 'feet': [5, 2, 15], 'width': 3, 'height': 4}],
        'main_route': {'x': [4, 6], 'feet_y': 2, 'z': [0, 15], 'clear_width': 3, 'clear_height': 3, 'floor_y': 1, 'dry': True},
        'water': {'source_blocks': len(waters), 'y': 1, 'x_columns': [2, 3, 7, 8], 'z': [1, 14], 'depth_blocks': 1, 'sealed': not seal_errors},
        'minecraft': '1.20.1', 'data_version': 3465, 'sha256': fingerprint,
        'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH,
        'batch_documents': [str(REFERENCE.parent.parent / filename) for filename in ('README.md', 'SPEC.md', 'manifest.json')],
        'builder_recipe': str(ROOT / 'tools/corridor-s05.mjs'),
        'adaptations': ['The fully flooded floor becomes two shallow side channels and a level dry three-block-wide central path.', 'The closed far door becomes an open south arch for a genuinely through corridor.', 'Algae are low mossy stone-brick accents, not unsupported vines.', 'Iron cages are empty block-built decoration with four roof-anchored chains, no entities or rewards.'],
        'integration': {'deployed': False, 'assigned_core_slot': None, 'player_saves_touched': False, 'occupancy_check_required_for_entire_volume': True, 'explicit_air_is_not_permission_to_clear': True},
    }
    blueprint = {'size': list(room.size), 'required_feet': required_feet, 'water_positions': [list(position) for position in waters], 'cage_roof_anchors': anchors, 'route_clearance': [[pos_x, pos_y, pos_z] for pos_x, _, pos_z in required_feet for pos_y in range(2, 5)]}
    counts = Counter(block.name for block in room.blocks.values())
    write_json(ROOT / 'materials.json', dict(sorted(counts.items())))
    write_json(ROOT / 'blueprint.json', blueprint)
    write_json(ROOT / 'room.json', metadata)
    result = {'status': 'PASS' if not failures else 'FAIL', 'time': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(), 'source_nbt_sha256': fingerprint, 'reference_sha256': REFERENCE_HASH, 'checks': checks, 'failures': failures, 'records': len(room.blocks), 'non_air_blocks': len(room.blocks) - counts['minecraft:air'], 'palette_size': len(raw['palette']), 'scope': 'Static structure/route/water/support audit; Forge evidence is separate.'}
    write_json(ROOT / 'qa/static.json', result)
    print(json.dumps(result, ensure_ascii=False))
    if failures:
        raise SystemExit(1)


if __name__ == '__main__':
    main()
