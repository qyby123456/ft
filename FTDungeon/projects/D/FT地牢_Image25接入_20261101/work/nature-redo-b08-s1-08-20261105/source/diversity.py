import csv
import hashlib
import io
import json
import math
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(Path(os.environ['APPDATA']) / '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure

GRID = 20
AIRLIKE = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air', 'minecraft:cave_vines', 'minecraft:cave_vines_plant', 'minecraft:vine', 'minecraft:torch', 'minecraft:wall_torch', 'minecraft:grass', 'minecraft:fern', 'minecraft:glow_lichen'}


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def similarity(left, right):
    union = left | right
    return len(left & right) / len(union) if union else 1.0


def rotated(mask, turns):
    for step in range(turns):
        mask = {(GRID - 1 - forward, across) for across, forward in mask}
    return mask


def descriptor(path, current):
    structure = Structure.read(path)
    metadata_path = path.with_name('room.json')
    metadata = json.loads(metadata_path.read_text(encoding='utf-8')) if metadata_path.exists() else {}
    foot = int(metadata.get('entrance', [0, 2, 0])[1])
    width, height, depth = structure.size
    walk = set()
    water = set()
    obstacles = set()
    upper = set()
    def empty(point):
        block = structure.get(point).name
        return block in AIRLIKE or block.endswith('_carpet')
    for across in range(GRID):
        for forward in range(GRID):
            sourceX = min(width - 1, int((across + .5) * width / GRID))
            sourceZ = min(depth - 1, int((forward + .5) * depth / GRID))
            below = structure.get((sourceX, foot - 1, sourceZ)).name
            if empty((sourceX, foot, sourceZ)) and empty((sourceX, foot + 1, sourceZ)) and below not in AIRLIKE | {'minecraft:water', 'minecraft:lava'}:
                walk.add((across, forward))
            if any(structure.get((sourceX, vertical, sourceZ)).name in {'minecraft:water', 'minecraft:lava'} for vertical in [foot - 1, foot, foot + 1]):
                water.add((across, forward))
            if 1 < sourceX < width - 2 and 1 < sourceZ < depth - 2 and not empty((sourceX, foot, sourceZ)):
                obstacles.add((across, forward))
            if any(not empty((sourceX, vertical, sourceZ)) for vertical in range(foot + 3, max(foot + 4, height - 1))):
                upper.add((across, forward))
    ports = set()
    for side in ['north', 'south', 'west', 'east']:
        count = width if side in ['north', 'south'] else depth
        openings = []
        for offset in range(count):
            sourceX = offset if side in ['north', 'south'] else 0 if side == 'west' else width - 1
            sourceZ = offset if side in ['west', 'east'] else 0 if side == 'north' else depth - 1
            if all(empty((sourceX, vertical, sourceZ)) for vertical in [foot, foot + 1, foot + 2]): openings.append(offset)
        if len(openings) >= 3: ports.add(side)
    vertical_profile = []
    for fraction in [.15, .3, .45, .6, .75, .9]:
        vertical = min(height - 1, max(foot, int(height * fraction)))
        cells = [structure.get((across, vertical, forward)).name not in AIRLIKE for across in range(1, width - 1) for forward in range(1, depth - 1)]
        vertical_profile.append(sum(cells) / len(cells) if cells else 0)
    palette = {state.name for state in structure.blocks.values()} - AIRLIKE
    decor = {name for name in palette if any(word in name for word in ['leaves', 'log', 'vines', 'lantern', 'wool', 'bookshelf', 'barrel', 'chest', 'cobweb', 'iron_bars'])}
    loot_positions = {(min(GRID - 1, int(point[0] * GRID / width)), min(GRID - 1, int(point[2] * GRID / depth))) for point in structure.block_entities if structure.get(point).name in {'minecraft:chest', 'minecraft:barrel'}}
    return {'id': path.parent.name, 'path': str(path.resolve()), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'current_group': current, 'size': structure.size, 'walk': walk, 'water': water, 'obstacles': obstacles, 'upper': upper, 'ports': ports, 'vertical_profile': vertical_profile, 'palette': palette, 'decor': decor, 'loot_positions': loot_positions, 'mechanics': 'UNKNOWN_FROM_NBT_ALONE' if not current else 'NO_PUZZLE_OR_WAVE_RUNTIME_IMPLEMENTED', 'line_of_sight': 'NOT_RAYCAST_IN_THIS_STATIC_SCREENING', 'topology_limits': 'Normalized collision/water/obstacle masks and boundary exits are structural proxies; not a full skeleton graph or player experience judgment.'}


def compare(left, right):
    candidates = []
    for turns in range(4):
        walk = similarity(left['walk'], rotated(right['walk'], turns))
        obstacle = similarity(left['obstacles'], rotated(right['obstacles'], turns))
        water = similarity(left['water'], rotated(right['water'], turns))
        upper = similarity(left['upper'], rotated(right['upper'], turns))
        ports = right['ports']
        directions = ['north', 'east', 'south', 'west']
        port_match = similarity(left['ports'], {directions[(directions.index(side) + turns) % 4] for side in ports})
        right_size = right['size'] if turns % 2 == 0 else [right['size'][2], right['size'][1], right['size'][0]]
        size = sum(min(first, second) / max(first, second) for first, second in zip(left['size'], right_size)) / 3
        vertical = max(0, 1 - sum(abs(first - second) for first, second in zip(left['vertical_profile'], right['vertical_profile'])) / 6)
        landmark = similarity(left['loot_positions'], rotated(right['loot_positions'], turns))
        core = .35 * walk + .25 * obstacle + .15 * port_match + .15 * water + .10 * upper
        score = .72 * core + .10 * size + .08 * vertical + .04 * landmark + .04 * similarity(left['palette'], right['palette']) + .02 * similarity(left['decor'], right['decor'])
        candidates.append({'score': score, 'core_layout': core, 'walk_mask': walk, 'obstacle_mask': obstacle, 'fluid_mask': water, 'overhead_mask': upper, 'ports': port_match, 'size': size, 'vertical_profile': vertical, 'container_positions': landmark, 'palette': similarity(left['palette'], right['palette']), 'decor_vocabulary': similarity(left['decor'], right['decor']), 'quarter_turns': turns})
    result = max(candidates, key=lambda item: item['score'])
    result['high_similarity'] = result['score'] >= .86 and result['core_layout'] >= .90
    result['mechanics_and_waves'] = 'NOT_A_DISTINCTIVENESS_CREDIT; verify runtime separately'
    return result


def main():
    group = json.loads((ROOT / 'group.json').read_text(encoding='utf-8'))
    paths = [(ROOT / room['id'] / 'room.nbt', True) for room in group['rooms']]
    paths.append((ROOT.parent / 'nature-redo-b01-s1-01-r3-20261105/nature-redo-b01-s1-01/room.nbt', False))
    paths.append((ROOT.parent / 'nature-redo-b02-s1-02-20261105/nature-redo-b02-s1-02/room.nbt', False))
    paths.append((ROOT.parent / 'nature-redo-b03-s1-03-20261105/nature-redo-b03-s1-03/room.nbt', False))
    paths.append((ROOT.parent / 'nature-redo-b04-s1-04-20261105/nature-redo-b04-s1-04/room.nbt', False))
    paths.append((ROOT.parent / 'nature-redo-b05-s1-05-20261105/nature-redo-b05-s1-05/room.nbt', False))
    paths.append((ROOT.parent / 'nature-redo-b06-s1-06-20261105/nature-redo-b06-s1-06/room.nbt', False))
    paths.append((ROOT.parent / 'nature-redo-b07-s1-07-20261105/nature-redo-b07-s1-07/room.nbt', False))
    paths += [(path, False) for path in sorted((ROOT.parents[1] / 'deliveries').glob('*/room.nbt'))]
    descriptors = []
    errors = []
    for path, current in paths:
        try:
            descriptors.append(descriptor(path, current))
        except Exception as error:
            errors.append({'path': str(path), 'error': str(error)})
    matrix = [[1.0 for item in descriptors] for other in descriptors]
    pairs = []
    for left_index, left in enumerate(descriptors):
        for right_index in range(left_index + 1, len(descriptors)):
            right = descriptors[right_index]
            comparison = compare(left, right)
            matrix[left_index][right_index] = matrix[right_index][left_index] = round(comparison['score'], 4)
            pairs.append({'left': left['id'], 'right': right['id'], 'involves_current_group': left['current_group'] or right['current_group'], **comparison})
    high = [pair for pair in pairs if pair['high_similarity']]
    records = [{key: sorted(value) if isinstance(value, set) else value for key, value in item.items()} for item in descriptors]
    save(ROOT / 'audit/descriptors.json', records)
    save(ROOT / 'audit/pairs.json', sorted(pairs, key=lambda item: item['score'], reverse=True))
    save(ROOT / 'audit/high-similarity.json', high)
    with (ROOT / 'audit/similarity-matrix.csv').open('w', newline='', encoding='utf-8-sig') as stream:
        writer = csv.writer(stream)
        writer.writerow(['room', *[item['id'] for item in descriptors]])
        for descriptor_value, row in zip(descriptors, matrix): writer.writerow([descriptor_value['id'], *row])
    current_high = [pair for pair in high if pair['involves_current_group']]
    result = {'status': 'SNAPSHOT_REQUIRES_GLOBAL_FINAL_AUDIT', 'rooms': len(descriptors), 'pairs': len(pairs), 'errors': errors, 'high_similarity_pairs': len(high), 'current_group_high_similarity': current_high, 'scope': 'S1-06 only, accepted S1-01 R3 through S1-05 plus currently delivered canonical room.nbt files; other unfinished work excluded.', 'required_final_work': ['Re-enumerate every completed group after all producers finish.', 'Compare actual route branching/skeleton, silhouette, vertical transitions, puzzle/encounter runtime, raycast sightlines, landmarks, decorative vocabulary and loot placement.', 'Review high structural similarity even if palette differs; rework any experience-isomorphic room.', 'Do not treat an absent mechanism or unknown wave system as unique gameplay.']}
    result['scope'] = 'S1-08 only, accepted S1-01 R3 through S1-07 plus current canonical deliveries. Lower-floor normalized masks are only screening proxies; multi-level native body-route and true skylight checks are separate.'
    save(ROOT / 'audit/summary.json', result)
    lines = ['# 跨组重复度：当前交付快照（非最终验收）', '', f'- 纳入 {len(descriptors)} 个真实 NBT，比较 {len(pairs)} 对；当前组相关高重复候选 {len(current_high)} 对。', '- 相似度按四个水平旋转取最大值；核心布局占72%，调色板仅4%，装饰词汇仅2%。不使用文件哈希或方块数量作相似度。', '- 核心包含可走区域、障碍/地标空间分布、水域、出口方向及上层占据形状；另计尺寸、垂直层次、容器位置。', '- 房间拓扑仍是归一化网格代理，不是完整骨架图；机关、波次、动态视线必须结合运行时证据补审。缺失玩法不能作为差异加分。', '- 高重复判据：综合≥0.86且核心≥0.90。矩阵、描述符与成对分项分数都保留，可人工复核。', '- 其他建筑线程仍有未交付工作；本报告不得标记全量跨组终审通过。', '', '## 当前组相关高重复候选', '']
    for pair in current_high: lines.append(f'- {pair["left"]} / {pair["right"]}: {pair["score"]:.4f}，核心 {pair["core_layout"]:.4f}，需人工审查/返工，换材质不能解除。')
    if not current_high: lines.append('- 当前启发式未检出高重复候选，但不替代最终体验审计。')
    lines += ['', '## 当前单图构图', '', '- S1-08：古树、侧池、真天窗、可达U形上廊与16级半阶；没有07默认关门前庭。', '- 本次仅制作S1-08，与已接受01..07及当前交付快照比较。代理不代表完整上下层拓扑或最终玩法终审。', '']
    (ROOT / 'audit/REPORT.md').write_text('\n'.join(lines), encoding='utf-8')
    print(json.dumps({'rooms': len(descriptors), 'pairs': len(pairs), 'current_group_high': current_high, 'read_errors': len(errors)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
