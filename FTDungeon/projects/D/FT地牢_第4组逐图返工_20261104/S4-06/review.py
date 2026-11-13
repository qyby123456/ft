import collections
import hashlib
import json
from pathlib import Path
import shutil
import sys

ROOT = Path(__file__).resolve().parent
TOOLS = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-06-corner.png')
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import plain, read_nbt


def save(relative, value):
    (ROOT / relative).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    target = ROOT / 'incoming/S4-06'
    target.mkdir(parents=True, exist_ok=True)
    shutil.copy2(ROOT / 'nbt/s4-06-mosslit-wetland-corner.nbt', target / 'room.nbt')
    digest = hashlib.sha256((target / 'room.nbt').read_bytes()).hexdigest()
    previous_path = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-05-independent/independent-details.json')
    previous = json.loads(previous_path.read_text(encoding='utf-8'))
    assert previous['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    assert hashlib.sha256((ROOT.parent / 'S4-05/incoming/S4-05/room.nbt').read_bytes()).hexdigest() == previous['sha256']
    waypoints = [[5, 4, south] for south in [0, 3, 6, 9, 12, 15, 18, 19]] + [[east, 4, 19] for east in [6, 9, 12, 15, 18, 21, 24]]
    metadata = {'id': 'S4-06', 'name': '苔灯积水折角', 'size': [25, 15, 25], 'sha256': digest, 'reference': str(REFERENCE), 'reference_sha256': hashlib.sha256(REFERENCE.read_bytes()).hexdigest(), 'previous_acceptance': str(previous_path), 'previous_sha256': previous['sha256'], 'entrance': [5, 4, 0], 'interior': [5, 4, 19], 'facing': 'north', 'origin': [0, 0, 0], 'axes': {'x': 'east', 'y': 'up', 'z': 'south'}, 'connections': [{'facing': facing, 'position': point, 'width': 3, 'height': 4, 'inward_depth': 3} for facing, point in [('north', [5, 4, 0]), ('east', [24, 4, 19])]], 'route_waypoints': waypoints, 'runtime_slot': None, 'registration': 'PENDING_INDEPENDENT_ACCEPTANCE', 'world_placement_ticks_gameplay': 'NOT_RUN', 'integration': 'NOT_RUN', 'deployment': 'NOT_RUN', 'deviations': ['25x15x25 estimated from image; not a measured blueprint.', 'Open outer L-shaped water channel wraps the southwest convex corner of a solid northeast ruin mass. Exits north/east are perpendicular; no opposite straight-through port.', 'Two-block-wide waterlogged top slabs approximate reference submerged stepping stones for offline dry-body accessibility; no complete dry floor.', 'Verdant froglights and grounded stone brackets approximate green-gold corner lamps; no shader reflection, mist or exterior forest.', 'Moss and rooted timber model the cropped ruin perimeter. External water/trees are omitted; whole-volume collision rejection required at later authorized import.', 'Candidate retains all designed walls and inner ruin cap; optional air-only cutaway is preview-only.']}
    save('incoming/S4-06/room.json', metadata)
    data = plain(read_nbt(target / 'room.nbt')[1])
    assert data['size'] == [25, 15, 25] and not data['entities']
    palette = data['palette']
    states = {tuple(record['pos']): palette[record['state']] for record in data['blocks']}
    names = {point: state['Name'] for point, state in states.items()}
    counts = collections.Counter(names.values())
    assert not [record for record in data['blocks'] if 'nbt' in record]
    assert not {'minecraft:' + name for name in ['chest', 'spawner', 'command_block', 'jigsaw', 'structure_block', 'lava', 'fire']}.intersection(counts)
    vegetation = {'minecraft:air', 'minecraft:water', 'minecraft:vine', 'minecraft:lily_pad', 'minecraft:cave_vines', 'minecraft:cave_vines_plant', 'minecraft:fern'}
    fluid = {point for point, state in states.items() if state['Name'] == 'minecraft:water' or state.get('Properties', {}).get('waterlogged') == 'true'}
    deltas = [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]
    for east, up, south in fluid:
        for delta in [deltas[index] for index in [0, 1, 3, 4, 5]]:
            adjacent = (east + delta[0], up + delta[1], south + delta[2])
            assert names.get(adjacent, 'minecraft:air') not in {'minecraft:air', 'minecraft:vine', 'minecraft:lily_pad', 'minecraft:fern'}, ('fluid_boundary', [east, up, south], adjacent)
    for point, name in names.items():
        if name == 'minecraft:lily_pad':
            assert names.get((point[0], point[1] - 1, point[2])) == 'minecraft:water', point
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
    lamps = [point for point, name in names.items() if name == 'minecraft:verdant_froglight']
    assert set(lamps) == {(8, 7, 3), (9, 7, 15), (22, 7, 16)}
    assert all(point in connected for point in lamps)
    inner_mass = [(east, up, south) for east in range(10, 25) for up in range(4, 11) for south in range(0, 15)]
    assert all(point in solid for point in inner_mass)
    assert all(names.get((east, 7, south), 'minecraft:air') in vegetation for east, south in [(5, 6), (5, 12), (5, 19), (12, 19), (18, 19), (24, 19)])
    centerline = [(5, 4, south) for south in range(20)] + [(east, 4, 19) for east in range(6, 25)]
    assert all(names.get((east, up - 1, south)) in {'minecraft:stone_brick_slab', 'minecraft:mossy_stone_bricks', 'minecraft:cracked_stone_bricks', 'minecraft:stone_bricks'} for east, up, south in centerline)
    assert names[(14, 7, 10)] not in vegetation
    water_columns = counts['minecraft:water']
    save('materials.json', dict(counts))
    channel_columns = sum(not (east >= 10 and south <= 14) for east in range(2, 24) for south in range(1, 23))
    assert channel_columns == 288 and water_columns > channel_columns / 2
    save('geometry-check.json', {'status': 'PASS', 'sha256': digest, 'water_bottom_side_air_edges': 0, 'architectural_solid_components': 1, 'supported_lamps': lamps, 'center_corner_lamp': [9, 7, 15], 'fern_planter': [8, 4, 16], 'water_surface_columns': water_columns, 'channel_floor_columns': channel_columns, 'water_dominant': water_columns > channel_columns / 2, 'turn_degrees': 90, 'inner_mass_columns': 225, 'centerline': centerline, 'scope': 'Static solid corner, floor supports, enclosure and architectural connectivity; actual world ticks/physics NOT_RUN.'})
    from native import run_native
    run_native()


if __name__ == '__main__':
    main()
