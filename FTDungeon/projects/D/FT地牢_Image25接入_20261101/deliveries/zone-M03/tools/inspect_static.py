import hashlib
import json
from collections import Counter, deque
from pathlib import Path

from nbtio import Structure, plain


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/mines/M03-mines-collapsed-mine-tunnel.png')
REFERENCE_HASH = '14d4e7e36cd29469c254e7a28b6e2f8dca7a94546d434be8abd9c5ec99b48241'
AIR = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
FORBIDDEN = {'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'powder_snow', 'cobweb', 'sand', 'gravel', 'suspicious_sand', 'suspicious_gravel', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'light', 'campfire', 'soul_campfire'}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write(name, value):
    (ROOT / name).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    room = Structure.read(ROOT / 'room.nbt')
    assert digest(REFERENCE) == REFERENCE_HASH
    assert room.size == (23, 12, 25) and room.data_version == 3465
    assert len(room.blocks) == 23 * 12 * 25 and not room.entities
    assert all(0 <= coordinate < limit for position in room.blocks for coordinate, limit in zip(position, room.size))
    counts = Counter(block.name for block in room.blocks.values())
    assert not any(name.removeprefix('minecraft:') in FORBIDDEN for name in counts)
    assert all(name.startswith('minecraft:') for name in counts)
    assert all(room.get((across, 0, depth)).name not in AIR for across in range(23) for depth in range(25))
    assert all(room.get((across, elevation, depth)).name not in AIR for across in range(23) for depth in range(25) for elevation in (10, 11))
    assert all(room.get((across, elevation, depth)).name in AIR for across in range(9, 14) for depth in range(25) for elevation in range(1, 5))
    assert all(room.get((across, elevation, depth)).name in AIR for across in range(9, 14) for depth in (0, 1, 23, 24) for elevation in range(1, 5))
    walkable = {(across, 1, depth) for across in range(23) for depth in range(25) if room.get((across, 1, depth)).name in AIR and room.get((across, 2, depth)).name in AIR and room.get((across, 0, depth)).name not in AIR}
    reachable = {(11, 1, 0)}
    pending = deque(reachable)
    while pending:
        across, elevation, depth = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            neighbor = (across + offset_x, elevation, depth + offset_z)
            if neighbor in walkable and neighbor not in reachable:
                reachable.add(neighbor)
                pending.append(neighbor)
    assert (11, 1, 4) in reachable and (11, 1, 24) in reachable
    solids = {position for position, block in room.blocks.items() if block.name not in AIR}
    connected = {(0, 0, 0)}
    pending = deque(connected)
    while pending:
        position = pending.popleft()
        for offset in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            neighbor = tuple(coordinate + delta for coordinate, delta in zip(position, offset))
            if neighbor in solids and neighbor not in connected:
                connected.add(neighbor)
                pending.append(neighbor)
    assert connected == solids, sorted(solids - connected)[:20]
    anchors = []
    for depth in (3, 9, 15, 21):
        for across in (3, 19):
            assert room.get((across, 1, depth)).name == 'minecraft:stone_bricks'
            assert all(room.get((across, elevation, depth)).name == 'minecraft:spruce_log' for elevation in range(2, 9))
        assert all(room.get((across, 8, depth)).name == 'minecraft:spruce_log' for across in range(3, 20))
        for across in (9, 17):
            lantern, chain, beam = [across, 6, depth], [across, 7, depth], [across, 8, depth]
            assert room.get(tuple(lantern)).name == 'minecraft:lantern'
            assert room.get(tuple(chain)).name == 'minecraft:chain'
            assert room.get(tuple(beam)).name == 'minecraft:spruce_log'
            anchors.append({'kind': 'lantern', 'position': lantern, 'chain': chain, 'anchor': beam})
    for position, block in room.blocks.items():
        if block.name in {'minecraft:rail', 'minecraft:cobblestone_slab', 'minecraft:spruce_fence', 'minecraft:barrel'}:
            below = (position[0], position[1] - 1, position[2])
            assert room.get(below).name not in AIR
    assert len(room.block_entities) == 6
    for position, entity in room.block_entities.items():
        data = plain(entity)
        assert data['id'] == 'minecraft:barrel' and not data.get('Items') and 'LootTable' not in data
    fingerprint = digest(ROOT / 'room.nbt')
    metadata = {
        'id': 'zone-M03', 'name': '塌方矿道', 'category': 'mines', 'kind': 'independent dungeon zone room',
        'size': list(room.size), 'entrance': [11, 1, 0], 'interior': [11, 1, 4], 'exit': [11, 1, 24], 'facing': 'north',
        'sha256': fingerprint, 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH,
        'reference_readme': 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/README.md',
        'reference_manifest': 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/docs/manifest.json',
        'source_dsh_session': 'cs_SqFKuWVLh1', 'minecraft': '1.20.1', 'data_version': 3465,
        'portals': [{'face': 'north', 'center': [11, 1, 0], 'width': 5, 'height': 4}, {'face': 'south', 'center': [11, 1, 24], 'width': 5, 'height': 4}],
        'main_lane': {'minimum_x': 9, 'maximum_x': 13, 'floor_y': 0, 'feet_y': 1, 'minimum_clear_height': 4, 'depth': [0, 24]},
        'collapse_bounds': {'from': [2, 1, 13], 'to': [7, 6, 22], 'traffic': 'side bay only'},
        'frame_stations': [3, 9, 15, 21], 'lantern_anchors': anchors,
        'reference_adaptations': ['塌方移至西侧支巷，北南主通路完整保留五格净宽。', '斜断梁用有碎石承托的阶梯式原木表现。', '不导出实体矿车；以空炼药锅作为报废矿斗保留轮廓。', '概念图剖面补全两格厚顶棚及四面外壳，剖切仅用于渲染。', '容器全部为空，不附带战利品或刷怪逻辑。'],
        'deployed': False, 'player_saves_touched': False, 'integration_slot': None,
        'integration_guard': 'Validate the full volume, liquids and entities before placement. Explicit air is not clearance authorization.'
    }
    write('room.json', metadata)
    write('qa/static.json', {'id': 'zone-M03', 'status': 'PASS', 'structure_sha256': fingerprint, 'reference_sha256': REFERENCE_HASH, 'size': list(room.size), 'records': len(room.blocks), 'non_air_blocks': len(solids), 'palette_states': len(set(room.blocks.values())), 'roof_complete_two_layers': True, 'floor_complete': True, 'portals': '5x4 north/south', 'main_lane_air_cells': 5 * 4 * 25, 'main_lane_width': 5, 'main_lane_height': 4, 'reachable_level_floor_cells': len(reachable), 'entrance_safe_point_exit_connected': True, 'non_air_face_connected_to_foundation': len(connected), 'detached_components': 0, 'grounded_timber_frames': 4, 'grounded_side_frame_posts': 2, 'lantern_chain_beam_assemblies': len(anchors), 'empty_barrels': len(room.block_entities), 'preplaced_entities': 0, 'fluids': 0, 'gravity_blocks': 0, 'hazards_or_commands': 0, 'block_counts': dict(sorted(counts.items()))})
    print(json.dumps({'static': 'PASS', 'sha256': fingerprint, 'records': len(room.blocks), 'non_air': len(solids)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
