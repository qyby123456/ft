import hashlib
import json
import sys
from collections import Counter, deque
from datetime import datetime, timedelta, timezone
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
NBT_TOOLS = Path(__file__).resolve().parent
sys.path.insert(0, str(NBT_TOOLS))
from nbtio import AIR, Structure, plain, read_nbt


def main():
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    room = Structure.read(ROOT / 'room.nbt')
    _, raw = read_nbt(ROOT / 'room.nbt')
    data = plain(raw)
    checks = {}

    def require(name, condition):
        checks[name] = bool(condition)
        if not condition:
            raise ValueError(name)

    digest = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    require('structure_sha256', digest == metadata['sha256'])
    require('reference_sha256', hashlib.sha256(Path(metadata['reference']).read_bytes()).hexdigest() == metadata['reference_sha256'] == '2bd12059b40ca01c7f4576e3af80f099382dc20b1c87ae34128332f5391c33ea')
    require('gzip_nbt', (ROOT / 'room.nbt').read_bytes()[:2] == b'\x1f\x8b')
    require('java_1201_single_palette', data['DataVersion'] == 3465 and 'palette' in data and 'palettes' not in data)
    require('size_and_medium_length', room.size == tuple(metadata['size']) == (13, 12, 41) and 32 <= room.size[2] <= 48)
    require('full_explicit_volume', len(room.blocks) == 13 * 12 * 41)
    require('bounds', all(all(0 <= position[axis] < room.size[axis] for axis in range(3)) for position in room.blocks))
    require('no_entities', not data.get('entities'))
    allowed = {'air', 'stone_bricks', 'cracked_stone_bricks', 'mossy_stone_bricks', 'polished_deepslate', 'deepslate_bricks', 'deepslate_tiles', 'red_wool', 'red_nether_bricks', 'chiseled_polished_blackstone', 'chiseled_stone_bricks', 'polished_andesite', 'stone_brick_stairs', 'candle', 'red_wall_banner'}
    require('strict_vanilla_safe_palette', all(block.name.startswith('minecraft:') and block.name.removeprefix('minecraft:') in allowed and block.get('waterlogged', 'false') == 'false' for block in room.blocks.values()))
    counts = Counter(block.name for block in room.blocks.values())
    require('themed_furniture_not_empty_box', counts['minecraft:red_wall_banner'] == 10 and counts['minecraft:candle'] == 20 and counts['minecraft:red_wool'] == 117 and counts['minecraft:stone_brick_stairs'] == 24)
    require('only_patterned_banner_block_entities', len(room.block_entities) == 10 and all(plain(entity).get('id') == 'minecraft:banner' and len(plain(entity).get('Patterns', [])) == 3 and set(plain(entity)) <= {'id', 'Patterns'} for entity in room.block_entities.values()))
    require('complete_solid_roof', all(room.blocks[(across, 11, depth)].name == 'minecraft:deepslate_bricks' for across in range(13) for depth in range(41)))
    require('unbroken_side_walls_no_branches', all(room.blocks[(across, elevation, depth)].name != 'minecraft:air' for across in (0, 1, 11, 12) for elevation in range(1, 11) for depth in range(41)))
    lane = {(across, 1, depth) for across in range(4, 9) for depth in range(41)}
    require('five_wide_three_high_straight_clearance', all(room.blocks[(across, elevation, depth)] == AIR for across in range(4, 9) for elevation in range(1, 4) for depth in range(41)))
    require('flat_full_floor_no_hazards', all(room.blocks[(across, 0, depth)].name in {'minecraft:red_wool', 'minecraft:deepslate_tiles', 'minecraft:polished_deepslate'} for across in range(4, 9) for depth in range(41)))
    require('portals_both_open', metadata['entrance'] == [6, 1, 0] and metadata['exit'] == [6, 1, 40] and all(room.blocks[(across, elevation, depth)] == AIR for depth in (0, 40) for across in range(4, 9) for elevation in range(1, 4)))
    visited = {tuple(metadata['entrance'])}
    queue = deque(visited)
    while queue:
        current = queue.popleft()
        for offset in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1)):
            neighbor = tuple(current[axis] + offset[axis] for axis in range(3))
            if neighbor in lane and neighbor not in visited:
                visited.add(neighbor)
                queue.append(neighbor)
    require('bidirectional_entrance_interior_exit_reachable', tuple(metadata['exit']) in visited and tuple(metadata['interior']) in visited and visited == lane)
    for support in metadata['supported_decor']:
        position, anchor = tuple(support['position']), tuple(support['anchor'])
        block, support_block = room.blocks[position], room.blocks[anchor]
        require('decor_anchor_' + '_'.join(map(str, position)), support_block.name != 'minecraft:air' and sum(abs(position[axis] - anchor[axis]) for axis in range(3)) == 1)
        if support['kind'] == 'candle':
            require('candle_lit_' + '_'.join(map(str, position)), block.name == 'minecraft:candle' and block.get('lit') == 'true' and support_block.name == 'minecraft:polished_andesite')
        else:
            require('banner_support_' + '_'.join(map(str, position)), block.name == 'minecraft:red_wall_banner' and block.get('facing') == ('east' if position[0] == 2 else 'west'))
    report = {
        'status': 'PASS',
        'time': datetime.now(timezone(timedelta(hours=8))).isoformat(),
        'structure_sha256': digest,
        'checks': checks,
        'size': room.size,
        'block_records': len(room.blocks),
        'non_air_blocks': sum(count for name, count in counts.items() if name != 'minecraft:air'),
        'palette_states': len(data['palette']),
        'counts': dict(counts),
        'straight_path_positions': len(lane),
        'straight_path_steps': 40,
        'roof_cells': 13 * 41,
        'supported_decor': len(metadata['supported_decor']),
        'limitations': ['Static assertions are not native collision or runtime support evidence; see native.json.'],
    }
    (ROOT / 'qa/static.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps({key: report[key] for key in ('status', 'size', 'block_records', 'non_air_blocks', 'structure_sha256')}, ensure_ascii=False))


if __name__ == '__main__':
    main()
