import hashlib
import json
import subprocess
import sys
from collections import Counter, deque
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
sys.path.insert(0, 'D:/ft工程文件/FT地牢_R5_石匠工坊_20261102/tools')
from nbtio import Structure, plain, read_nbt, state


REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/corners/C05-corners-crystal-corner-cavern.png')
REFERENCE_HASH = '790bb6fe1bbc37401333dd80f98c925abb13f1136459310848daaf7a41053a01'
AIR = state('air')
SIZE = (25, 14, 25)
STEPS = {'north': (0, 0, -1), 'south': (0, 0, 1), 'east': (1, 0, 0), 'west': (-1, 0, 0), 'up': (0, 1, 0), 'down': (0, -1, 0)}


def check(condition, message):
    if not condition:
        raise ValueError(message)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    check(ROOT.name == 'zone-C05', 'Unexpected delivery root')
    check(digest(REFERENCE) == REFERENCE_HASH, 'Reference SHA-256 mismatch')
    subprocess.run(['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'tools/recipe.mjs'), '--out-dir', str(ROOT), '--name', 'room', '--data-version', '3465', '--compression', 'gzip', '--json'], cwd=SKILL, check=True)
    room = Structure.read(ROOT / 'room.nbt')
    _, document = read_nbt(ROOT / 'room.nbt')
    values = plain(document)
    check(room.size == SIZE and room.data_version == 3465, 'Size or version mismatch')
    check('palette' in values and 'palettes' not in values, 'Expected single-palette Java structure')
    check(len(values['blocks']) == 8750 and len(room.blocks) == 8750, 'Missing or duplicate volume records')
    check(not room.entities and not room.block_entities, 'Unexpected entities or block entities')
    materials = Counter(block.name for block in room.blocks.values())
    banned = {'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void', 'jigsaw', 'spawner', 'tnt', 'lava', 'fire', 'soul_fire', 'magma_block', 'cactus', 'cobweb', 'powder_snow', 'campfire', 'soul_campfire', 'budding_amethyst', 'chest', 'barrel', 'dispenser', 'dropper'}
    check(all(name.startswith('minecraft:') and name.split(':')[1] not in banned for name in materials), 'Forbidden material')
    for position in room.blocks:
        check(all(0 <= value < limit for value, limit in zip(position, SIZE)), 'Out-of-bounds block')
    for horizontal in range(25):
        for depth in range(25):
            check(room.get((horizontal, 0, depth)) != AIR, 'Foundation hole')
            check(room.get((horizontal, 13, depth)) != AIR, 'Roof hole')
    route = {(horizontal, depth) for horizontal in range(25) for depth in range(25) if 10 <= horizontal <= 14 and 0 <= depth <= 14 or 10 <= horizontal <= 24 and 10 <= depth <= 14}
    for horizontal, depth in route:
        check(room.get((horizontal, 1, depth)).name in {'minecraft:deepslate_tiles', 'minecraft:polished_deepslate'}, 'Unsafe route floor')
        for vertical in range(2, 5):
            check(room.get((horizontal, vertical, depth)) == AIR, 'Obstructed five-wide route')
    for horizontal in range(10, 15):
        for vertical in range(2, 7):
            check(room.get((horizontal, vertical, 0)) == AIR, 'North portal obstructed')
    for depth in range(10, 15):
        for vertical in range(2, 7):
            check(room.get((24, vertical, depth)) == AIR, 'East portal obstructed')
    visited = {(12, 0)}
    pending = deque(visited)
    while pending:
        horizontal, depth = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            candidate = horizontal + offset_x, depth + offset_z
            if candidate in route and candidate not in visited:
                visited.add(candidate)
                pending.append(candidate)
    check(visited == route and (24, 12) in visited and (12, 4) in visited, 'Corner route disconnected')
    buds = lanterns = chains = water = rails = 0
    for (horizontal, vertical, depth), block in room.blocks.items():
        if 'amethyst_bud' in block.name or block.name == 'minecraft:amethyst_cluster':
            offset = STEPS[block.get('facing')]
            support = room.get((horizontal - offset[0], vertical - offset[1], depth - offset[2]))
            check(support.name in {'minecraft:amethyst_block', 'minecraft:calcite'}, 'Crystal attachment lacks support')
            buds += 1
        if block.name == 'minecraft:water':
            water += 1
            check(vertical == 1 and block.get('level') == '0', 'Unexpected pool depth or flow state')
            for offset_x, offset_y, offset_z in [(0, -1, 0), (1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1)]:
                check(room.get((horizontal + offset_x, vertical + offset_y, depth + offset_z)) != AIR, 'Uncontained water')
        if block.name == 'minecraft:lantern':
            if block.get('hanging') == 'true':
                check(room.get((horizontal, vertical + 1, depth)).name == 'minecraft:chain', 'Hanging lantern misses chain')
            else:
                check(room.get((horizontal, vertical - 1, depth)).name == 'minecraft:stripped_spruce_wood', 'Standing lantern misses solid crate top')
            lanterns += 1
        if block.name == 'minecraft:chain':
            check(room.get((horizontal, vertical + 1, depth)) != AIR, 'Unanchored chain')
            chains += 1
        if block.name == 'minecraft:rail':
            check(room.get((horizontal, vertical - 1, depth)) != AIR, 'Unsupported rail')
            rails += 1
    check((buds, lanterns, chains, water, rails) == (29, 6, 8, 33, 6), 'Theme count mismatch: ' + str((buds, lanterns, chains, water, rails)))
    metadata = {
        'id': 'zone-C05', 'source_id': 'C05', 'name': '水晶角洞', 'size': list(SIZE),
        'entrance': [12, 2, 0], 'interior': [12, 2, 4], 'facing': 'north', 'exit': [24, 2, 12], 'exit_facing': 'east',
        'ports': [{'name': 'north', 'feet': [12, 2, 0], 'facing': 'north', 'width': 5, 'height': 5}, {'name': 'east', 'feet': [24, 2, 12], 'facing': 'east', 'width': 5, 'height': 5}],
        'sha256': digest(ROOT / 'room.nbt'), 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH,
        'minecraft': '1.20.1', 'data_version': 3465, 'format': 'gzip single-palette vanilla structure',
        'main_route': {'feet_y': 2, 'width': 5, 'headroom': 3, 'feet_cells': len(route), 'shape': 'north to east right-angle dry route', 'rectangles_xz': [[10, 0, 14, 14], [10, 10, 24, 14]]},
        'water': {'cells': water, 'source_y': 1, 'depth': 1, 'basins_xz': [[15, 17, 20, 20], [5, 10, 7, 12]], 'route_disjoint': True},
        'features': {'crystal_attachments': buds, 'lanterns': lanterns, 'chains': chains, 'abandoned_rails': rails, 'complete_roof': True},
        'deviations': ['概念图为开放剖视；最终结构补齐洞顶与四侧围护，仅软件预览剖切。', '蓝紫巨晶用原版紫水晶块、芽簇、方解石和海晶灯转译；不模拟图中自发蓝光或使用模组方块。', '矿车为实体，按无实体交付要求省略，保留六格废轨；木箱为无库存实心装饰。', '为确保五格宽干燥拐角通路，水池与石柱移到主路侧边；入口按接入契约固定在北侧中央，增加东侧出口。'],
        'scope': {'production_deployment': False, 'player_save_accessed': False, 'integration_performed': False, 'receiver_acceptance_required': True}
    }
    save(ROOT / 'room.json', metadata)
    save(ROOT / 'qa/materials.json', dict(sorted(materials.items())))
    save(ROOT / 'qa/static.json', {'status': 'PASS', 'id': 'zone-C05', 'sha256': metadata['sha256'], 'reference_sha256': REFERENCE_HASH, 'size': list(SIZE), 'records': 8750, 'palette_states': len(values['palette']), 'non_air_records': 8750 - materials['minecraft:air'], 'roof_cells': 625, 'foundation_cells': 625, 'main_route_feet_cells': len(route), 'route_width': 5, 'route_headroom': 3, 'ports': metadata['ports'], 'crystal_attachments_supported': buds, 'supported_lanterns': lanterns, 'supported_chains': chains, 'contained_source_water_cells': water, 'abandoned_rails': rails, 'entities': 0, 'block_entities': 0, 'inventory_items': 0, 'forbidden_blocks': 0, 'full_volume_explicit': True, 'scope': 'Static checks; native registry, collision and tick evidence reported separately.'})
    fixture = Structure((1, 1, 1))
    fixture.set((0, 0, 0), AIR)
    fixture.write(ROOT / 'src/main/resources/data/ftzonec05qa/structures/empty.nbt')
    print(json.dumps({'status': 'PASS', 'sha256': metadata['sha256'], 'size': list(SIZE), 'route_cells': len(route)}))


if __name__ == '__main__':
    main()
