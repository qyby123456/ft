import collections
import hashlib
import json
from pathlib import Path
import shutil
import sys

ROOT = Path(__file__).resolve().parent
TOOLS = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-08-courtyard.png')
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import plain, read_nbt


def save(relative, value):
    (ROOT / relative).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    target = ROOT / 'incoming/S4-08'
    target.mkdir(parents=True, exist_ok=True)
    shutil.copy2(ROOT / 'nbt/s4-08-rooted-gallery-courtyard.nbt', target / 'room.nbt')
    digest = hashlib.sha256((target / 'room.nbt').read_bytes()).hexdigest()
    previous_path = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-07-independent/independent-details.json')
    previous = json.loads(previous_path.read_text(encoding='utf-8'))
    assert previous['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    assert hashlib.sha256((ROOT.parent / 'S4-07/incoming/S4-07/room.nbt').read_bytes()).hexdigest() == previous['sha256']
    waypoints = [[17, 5, 0], [17, 5, 34], [0, 5, 17], [34, 5, 25], [17, 5, 9], [9, 5, 9], [9, 5, 17], [9, 5, 23], [17, 5, 25], [25, 5, 25], [25, 5, 17], [25, 5, 9], [29, 5, 22], [29, 6, 21], [29, 9, 18], [29, 12, 15], [29, 13, 13], [29, 13, 6], [20, 13, 4], [10, 13, 4], [4, 13, 4], [4, 13, 12], [4, 13, 20], [4, 13, 29], [17, 5, 11], [11, 5, 17], [23, 5, 17], [17, 5, 23], [10, 5, 30]]
    ports = [('north', [17, 5, 0]), ('south', [17, 5, 34]), ('west', [0, 5, 17]), ('east', [34, 5, 25])]
    metadata = {'id': 'S4-08', 'name': '盘根瀑水回廊庭院', 'size': [35, 27, 35], 'sha256': digest, 'reference': str(REFERENCE), 'reference_sha256': hashlib.sha256(REFERENCE.read_bytes()).hexdigest(), 'previous_acceptance': str(previous_path), 'previous_sha256': previous['sha256'], 'entrance': [17, 5, 34], 'interior': [17, 5, 25], 'facing': 'south', 'origin': [0, 0, 0], 'axes': {'x': 'east', 'y': 'up', 'z': 'south'}, 'connections': [{'facing': facing, 'position': point, 'width': 3, 'height': 4, 'inward_depth': 3} for facing, point in ports], 'route_waypoints': waypoints, 'runtime_slot': None, 'registration': 'PENDING_INDEPENDENT_ACCEPTANCE', 'world_placement_ticks_gameplay': 'NOT_RUN', 'integration': 'NOT_RUN', 'deployment': 'NOT_RUN', 'deviations': ['35x27x35 estimated from image, not a measured blueprint.', 'Upper west/north gallery kept with fences; internal east stair added for walking access instead of an unreachable decorative balcony.', 'Four lower supported interfaces added with side/asymmetric east positionZ25 to avoid staircase; outer forest omitted.', 'Central raised rooted island and real persistent-leaf tree retained; leaf distance=1 is authored initial state, no world growth/decay test.', 'Contained source trough and three-wide falling water column approximate foreground waterfall; intentional exposed falling face precisely recorded, no actual fluid ticks.', 'Waterlogged top-slab ring/approaches approximate submerged stones, offline native body shape only.', 'Preview cutaway removes only Y>=14 and is never a candidate.']}
    metadata['route_waypoints'] = [[18, 5, 23] if point == [17, 5, 23] else point for point in waypoints]
    save('incoming/S4-08/room.json', metadata)
    data = plain(read_nbt(target / 'room.nbt')[1])
    assert data['size'] == [35, 27, 35] and not data['entities']
    palette = data['palette']
    states = {tuple(record['pos']): palette[record['state']] for record in data['blocks']}
    names = {point: state['Name'] for point, state in states.items()}
    counts = collections.Counter(names.values())
    assert not [record for record in data['blocks'] if 'nbt' in record]
    assert not {'minecraft:' + name for name in ['spawner', 'chest', 'command_block', 'jigsaw', 'structure_block', 'lava', 'fire']}.intersection(counts)
    fluid = {point for point, state in states.items() if state['Name'] == 'minecraft:water' or state.get('Properties', {}).get('waterlogged') == 'true'}
    deltas = [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]
    exposed = []
    for east, up, south in fluid:
        for delta in [deltas[index] for index in [0, 1, 3, 4, 5]]:
            adjacent = (east + delta[0], up + delta[1], south + delta[2])
            if names.get(adjacent, 'minecraft:air') in {'minecraft:air', 'minecraft:vine', 'minecraft:lily_pad'}:
                exposed.append([[east, up, south], list(adjacent)])
    allowed = {((east, up, 28), (east, up, 29)) for east in range(6, 9) for up in range(5, 8)}
    assert {(tuple(first), tuple(second)) for first, second in exposed} == allowed, exposed
    for point, name in names.items():
        if name == 'minecraft:lily_pad':
            assert names.get((point[0], point[1] - 1, point[2])) == 'minecraft:water', point
    vegetation = {'minecraft:air', 'minecraft:water', 'minecraft:vine', 'minecraft:lily_pad', 'minecraft:cave_vines', 'minecraft:cave_vines_plant', 'minecraft:fern'}
    solid = {point for point, name in names.items() if name not in vegetation}
    connected = {(0, 0, 0)}
    pending = list(connected)
    while pending:
        point = pending.pop()
        for delta in deltas:
            adjacent = tuple(point[axis] + delta[axis] for axis in range(3))
            if adjacent in solid and adjacent not in connected:
                connected.add(adjacent)
                pending.append(adjacent)
    assert solid == connected, list(solid - connected)[:20]
    assert counts['minecraft:ochre_froglight'] == 7
    assert counts['minecraft:fern'] == 3
    assert counts['minecraft:oak_leaves'] > 300
    assert all(names.get((17, up, 17)) == 'minecraft:dark_oak_log' for up in range(8, 22))
    assert counts['minecraft:stone_brick_stairs'] == 24
    assert counts['minecraft:dark_oak_fence'] > 60
    root_endpoints = [[12, 5, 16], [23, 5, 19], [16, 5, 11], [17, 5, 23]]
    wood = {point for point, name in names.items() if name == 'minecraft:dark_oak_log'}
    trunk = {(17, 8, 17)}
    pending = list(trunk)
    while pending:
        point = pending.pop()
        for delta in deltas:
            adjacent = tuple(point[axis] + delta[axis] for axis in range(3))
            if adjacent in wood and adjacent not in trunk:
                trunk.add(adjacent)
                pending.append(adjacent)
    assert all(tuple(point) in trunk for point in root_endpoints), root_endpoints
    save('materials.json', dict(counts))
    save('geometry-check.json', {'status': 'PASS', 'sha256': digest, 'unexpected_water_bottom_side_air_edges': 0, 'intentional_waterfall_faces': sorted(exposed), 'waterfall_scope': 'Nine authored south faces of falling water, not containment error; source trough sides/bottom supported; actual world fluid propagation NOT_RUN.', 'architectural_solid_components': 1, 'supported_lamps': 7, 'fern_count': 3, 'persistent_leaf_blocks': counts['minecraft:oak_leaves'], 'trunk_and_roots_wood_component': len(trunk), 'root_endpoints': root_endpoints, 'stairs': 24, 'gallery_walk_foot_y': 13, 'water_surface_columns': counts['minecraft:water'], 'water_dominant': counts['minecraft:water'] > 200, 'scope': 'Static plant/water support, solid architecture/root connections; full half-grid native body routes separately. World ticks/physics NOT_RUN.'})
    from native import run_native
    run_native()


if __name__ == '__main__':
    main()
