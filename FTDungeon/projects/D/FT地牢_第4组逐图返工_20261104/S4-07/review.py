import collections
import hashlib
import json
from pathlib import Path
import shutil
import sys

ROOT = Path(__file__).resolve().parent
TOOLS = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-07-entrance.png')
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import plain, read_nbt


def save(relative, value):
    (ROOT / relative).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    target = ROOT / 'incoming/S4-07'
    target.mkdir(parents=True, exist_ok=True)
    shutil.copy2(ROOT / 'nbt/s4-07-swamp-gate-courtyard.nbt', target / 'room.nbt')
    digest = hashlib.sha256((target / 'room.nbt').read_bytes()).hexdigest()
    previous_path = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-06-independent/independent-details.json')
    previous = json.loads(previous_path.read_text(encoding='utf-8'))
    assert previous['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    assert hashlib.sha256((ROOT.parent / 'S4-06/incoming/S4-06/room.nbt').read_bytes()).hexdigest() == previous['sha256']
    waypoints = [[13, 3, 28], [13, 4, 27], [13, 5, 26], [13, 5, 25], [14, 5, 25], [13, 5, 24], [13, 5, 22], [13, 5, 19], [13, 5, 16], [13, 5, 13], [13, 5, 10], [14, 5, 7], [14, 5, 4], [14, 5, 2], [14, 5, 0]]
    metadata = {'id': 'S4-07', 'name': '沼泽双门接引前庭', 'size': [29, 20, 29], 'sha256': digest, 'reference': str(REFERENCE), 'reference_sha256': hashlib.sha256(REFERENCE.read_bytes()).hexdigest(), 'previous_acceptance': str(previous_path), 'previous_sha256': previous['sha256'], 'entrance': [13, 3, 28], 'interior': [13, 5, 19], 'facing': 'south', 'origin': [0, 0, 0], 'axes': {'x': 'east', 'y': 'up', 'z': 'south'}, 'connections': [{'facing': 'south', 'position': [13, 3, 28], 'width': 2, 'height': 2, 'inward_depth': 3, 'type': 'Stair approach rises footY3 to5, then two-wide closed double door atZ25; open gate required. Height2 is minimum gate clearance, not a flat3-deep port.'}, {'facing': 'north', 'position': [14, 5, 0], 'width': 3, 'height': 4, 'inward_depth': 3, 'type': 'Open tall rear arch.'}], 'route_waypoints': waypoints, 'runtime_slot': None, 'registration': 'PENDING_INDEPENDENT_ACCEPTANCE', 'world_placement_ticks_gameplay': 'NOT_RUN', 'integration': 'NOT_RUN', 'deployment': 'NOT_RUN', 'deviations': ['29x20x29 estimated from reference proportions, not a measured blueprint.', 'Candidate has closed native double doors. Offline open-state route test changes only OPEN property in memory, not delivered NBT.', 'Map board approximated with framed birch/sandstone mosaic, not an actual filled-map item needing external world map data.', 'Froglights approximate framed lanterns. Lit campfire is real and intentionally off-route; damage/fire behavior not tested.', 'Central waterlogged top slabs approximate reference partly submerged flagstones; dry body shape verified offline, actual world physics NOT_RUN.', 'External swamp forest/water and shader mist/reflections omitted; open courtyard and ruined stepped walls retained.']}
    save('incoming/S4-07/room.json', metadata)
    data = plain(read_nbt(target / 'room.nbt')[1])
    assert data['size'] == [29, 20, 29] and not data['entities']
    palette = data['palette']
    states = {tuple(record['pos']): palette[record['state']] for record in data['blocks']}
    names = {point: state['Name'] for point, state in states.items()}
    counts = collections.Counter(names.values())
    assert not {'minecraft:' + name for name in ['spawner', 'command_block', 'jigsaw', 'structure_block', 'lava', 'fire']}.intersection(counts)
    fluid = {point for point, state in states.items() if state['Name'] == 'minecraft:water' or state.get('Properties', {}).get('waterlogged') == 'true'}
    deltas = [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]
    for east, up, south in fluid:
        for delta in [deltas[index] for index in [0, 1, 3, 4, 5]]:
            adjacent = (east + delta[0], up + delta[1], south + delta[2])
            assert names.get(adjacent, 'minecraft:air') not in {'minecraft:air', 'minecraft:vine', 'minecraft:lily_pad'}, ('fluid_boundary', [east, up, south], adjacent)
    for point, name in names.items():
        if name == 'minecraft:lily_pad':
            assert names.get((point[0], point[1] - 1, point[2])) == 'minecraft:water', point
    vegetation = {'minecraft:air', 'minecraft:water', 'minecraft:vine', 'minecraft:lily_pad', 'minecraft:cave_vines', 'minecraft:cave_vines_plant'}
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
    entities = [record for record in data['blocks'] if 'nbt' in record]
    barrels = [record for record in entities if record['nbt']['id'] == 'minecraft:barrel']
    assert len(barrels) == 8 and all(not record['nbt']['Items'] for record in barrels)
    campfires = [record for record in entities if record['nbt']['id'] == 'minecraft:campfire']
    assert len(campfires) == 1 and len(entities) == 9
    doors = {point: state for point, state in states.items() if state['Name'] == 'minecraft:dark_oak_door'}
    assert set(doors) == {(east, up, 25) for east in [13, 14] for up in [5, 6]}
    assert all(state['Properties']['open'] == 'false' and state['Properties']['facing'] == 'south' for state in doors.values())
    assert len({state['Properties']['hinge'] for state in doors.values()}) == 2
    assert names[(9, 7, 17)] == 'minecraft:campfire' and states[(9, 7, 17)]['Properties']['lit'] == 'true'
    assert all(abs(point[0] - 9) >= 3 for point in waypoints)
    save('materials.json', dict(counts))
    save('geometry-check.json', {'status': 'PASS', 'sha256': digest, 'water_bottom_side_air_edges': 0, 'architectural_solid_components': 1, 'empty_barrels': 8, 'lit_off_route_campfire': [9, 7, 17], 'board': 'Framed birch/sandstone mosaic atX6..10,Y6..11,Z4', 'doors': [list(point) for point in doors], 'candidate_door_state': 'CLOSED', 'stair_rows': [[28, 2], [27, 3], [26, 4]], 'water_surface_columns': counts['minecraft:water'], 'water_dominant': counts['minecraft:water'] > 23 * 19 / 2, 'scope': 'Static supports, enclosure, entity contents and door pairing. Native body gate/stair routes are separate. World interactions/ticks/campfire damage NOT_RUN.'})
    from native import run_native
    run_native()


if __name__ == '__main__':
    main()
