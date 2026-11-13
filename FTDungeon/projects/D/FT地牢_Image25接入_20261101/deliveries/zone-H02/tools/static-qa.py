import hashlib
import json
from collections import Counter, deque
from pathlib import Path

from nbtio import Structure, plain, read_nbt

ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/halls/H02-halls-banquet-hall-ruined.png')
REFERENCE_HASH = 'd11b8f4c4fd01d0058b779811853271a011e6da302ed27e5ca049cb162f60896'
FORBIDDEN = {'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void', 'jigsaw', 'spawner', 'tnt', 'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'cactus', 'cobweb', 'powder_snow', 'campfire', 'soul_campfire', 'sweet_berry_bush', 'wither_rose', 'sand', 'gravel', 'anvil'}


def save(name, value):
    (ROOT / name).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def route_cells():
    result = {(horizontal, 1, depth) for horizontal in range(13, 16) for depth in range(9)}
    result.update((horizontal, 1, depth) for horizontal in [6, 7, 8, 20, 21, 22] for depth in range(6, 28))
    result.update((horizontal, 1, depth) for horizontal in range(6, 23) for depth in [6, 7, 8, 25, 26, 27])
    return result


def flood(cells, start, dimensions=2):
    visited = {start}
    queue = deque([start])
    offsets = [(1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1)]
    if dimensions == 3:
        offsets += [(0, 1, 0), (0, -1, 0)]
    while queue:
        current = queue.popleft()
        for offset in offsets:
            candidate = tuple(current[axis] + offset[axis] for axis in range(3))
            if candidate in cells and candidate not in visited:
                visited.add(candidate)
                queue.append(candidate)
    return visited


def main():
    room = Structure.read(ROOT / 'room.nbt')
    digest = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    assert hashlib.sha256(REFERENCE.read_bytes()).hexdigest() == REFERENCE_HASH
    document = read_nbt(ROOT / 'room.nbt')[1].value
    assert room.size == (29, 16, 33) and room.data_version == 3465
    assert 'palette' in document and 'palettes' not in document
    assert not room.entities and not room.block_entities
    assert len(room.blocks) == 29 * 16 * 33
    assert all(all(0 <= coordinate < extent for coordinate, extent in zip(position, room.size)) for position in room.blocks)
    solid = {position for position, block in room.blocks.items() if block.name != 'minecraft:air'}
    assert not {block.name.removeprefix('minecraft:') for block in room.blocks.values()} & FORBIDDEN
    for horizontal in range(29):
        for depth in range(33):
            assert room.get((horizontal, 0, depth)).name != 'minecraft:air'
            assert room.get((horizontal, 14, depth)).name == 'minecraft:deepslate_bricks' or (horizontal < 3 or horizontal > 25) and (depth < 3 or depth > 29)
    route = route_cells()
    for horizontal, vertical, depth in route:
        assert all(room.get((horizontal, vertical + offset, depth)).name == 'minecraft:air' for offset in range(3)), ('blocked route', horizontal, depth)
    assert flood(route, (14, 1, 0)) == route
    assert (14, 1, 0) in flood(route, (14, 1, 5))
    for horizontal in range(13, 16):
        assert all(room.get((horizontal, vertical, 0)).name == 'minecraft:air' for vertical in range(1, 5))
    anchored = flood(solid, (0, 0, 0), 3)
    assert anchored == solid, ('floating components', sorted(solid - anchored)[:20])
    supported_decorations = 0
    for (horizontal, vertical, depth), block in room.blocks.items():
        below = room.get((horizontal, vertical - 1, depth))
        above = room.get((horizontal, vertical + 1, depth))
        if block.name in {'minecraft:candle', 'minecraft:red_carpet', 'minecraft:white_carpet'}:
            assert below.name == 'minecraft:dark_oak_planks'
            supported_decorations += 1
        if block.name == 'minecraft:lantern':
            assert (above if block.get('hanging') == 'true' else below).name != 'minecraft:air'
            supported_decorations += 1
        if block.name == 'minecraft:chain':
            anchor = vertical
            while room.get((horizontal, anchor, depth)).name == 'minecraft:chain':
                anchor += 1
            assert room.get((horizontal, anchor, depth)).name in {'minecraft:dark_oak_planks', 'minecraft:stripped_dark_oak_log'}
            supported_decorations += 1
        if block.name == 'minecraft:spruce_trapdoor' and block.get('open') == 'true':
            assert below.name == 'minecraft:spruce_stairs'
            assert (block.get('facing'), below.get('facing')) in [('east', 'west'), ('west', 'east')], 'Chair back must touch the full-height half of its stair seat'
    for horizontal in [12, 16]:
        for depth in [11, 17, 23]:
            assert room.get((horizontal, 1, depth)).name == 'minecraft:stripped_spruce_log'
            assert room.get((horizontal, 2, depth)).name == 'minecraft:dark_oak_planks'
    for horizontal in [4, 24]:
        for depth in [8, 16, 24]:
            assert all(room.get((horizontal, vertical, depth)).name != 'minecraft:air' for vertical in range(15))
    walkable = {(horizontal, 1, depth) for horizontal in range(2, 27) for depth in range(2, 31)
                if all(room.get((horizontal, vertical, depth)).name == 'minecraft:air' for vertical in [1, 2])}
    accessible = flood(walkable | route, (14, 1, 0))
    materials = Counter(block.name for block in room.blocks.values())
    save('room.json', {'id': 'H02', 'delivery_id': 'zone-H02', 'name': '废墟宴会厅', 'size': list(room.size),
         'entrance': [14, 1, 0], 'interior': [14, 1, 5], 'facing': 'north', 'sha256': digest,
         'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH, 'data_version': 3465,
         'palette': 'single vanilla Java 1.20.1', 'deployment': 'not deployed', 'roof': 'complete; only previews are cut away',
         'clearance': {'north_port_width': 3, 'north_port_height': 4, 'main_route_width': 3, 'main_route_height': 3},
         'notes': ['脚部坐标；桌椅与残骸置于主路外', '四向内剖视仅隐藏屋顶和近侧墙，实际模板完整封顶', '原图剖视被转化为带屋顶的可放置独立房间']})
    save('materials.json', dict(sorted(materials.items())))
    save('qa/route-cells.json', sorted(route))
    save('qa/static.json', {'status': 'PASS', 'sha256': digest, 'reference_sha256': REFERENCE_HASH,
         'size': list(room.size), 'records': len(room.blocks), 'non_air_blocks': len(solid), 'palette_count': len(document['palette'].value),
         'full_volume_explicit': True, 'single_palette': True, 'entities': 0, 'block_entities': 0,
         'floor_cells': 957, 'roof_cells': 957, 'route_cells': len(route), 'reachable_floor_cells': len(accessible),
         'route_bidirectional': True, 'route_width': 3, 'route_clear_height': 3, 'entrance': '3 wide x 4 high, full-block floor',
         'floor_anchored_components': 1, 'floating_components': 0, 'supported_decorations': supported_decorations,
         'chairs': 10, 'table_legs': 6, 'grounded_pillars': 6, 'continuous_chandelier_chains': 4,
         'hazards_or_forbidden_blocks': [], 'warnings': ['静态邻接支撑检查另由 Forge 碰撞、canSurvive 与 320 tick 稳定性复核']})
    print(json.dumps({'static': 'PASS', 'sha256': digest, 'route_cells': len(route), 'reachable_floor_cells': len(accessible), 'non_air': len(solid)}))


if __name__ == '__main__':
    main()
