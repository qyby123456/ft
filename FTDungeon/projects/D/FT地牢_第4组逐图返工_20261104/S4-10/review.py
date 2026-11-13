import collections
import hashlib
import json
from pathlib import Path
import shutil
import sys

ROOT = Path(__file__).resolve().parent
TOOLS = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-10-overview.png')
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import plain, read_nbt


def save(relative, value):
    destination = ROOT / relative
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    target = ROOT / 'incoming/S4-10'
    target.mkdir(parents=True, exist_ok=True)
    shutil.copy2(ROOT / 'nbt/s4-10-swamp-sanctum-complex.nbt', target / 'room.nbt')
    digest = hashlib.sha256((target / 'room.nbt').read_bytes()).hexdigest()
    previous_path = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-09-independent/independent-details.json')
    previous = json.loads(previous_path.read_text(encoding='utf-8'))
    assert previous['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    previous_hash = 'e2a8b466f10f52a2807d93e265170ce70d9961974a51d453377cc82c3b8e2f34'
    assert hashlib.sha256((ROOT.parent / 'S4-09/incoming/S4-09/room.nbt').read_bytes()).hexdigest() == previous_hash
    regions = [
        {'id': 'library', 'name': '西北书房', 'bounds_xz': [3, 7, 23, 25], 'wall_top_y': 12, 'targets': [[19, 5, 23], [10, 5, 21], [10, 5, 19], [12, 5, 10], [6, 5, 13]]},
        {'id': 'north-hall', 'name': '北主祭厅', 'bounds_xz': [26, 1, 50, 23], 'wall_top_y': 16, 'targets': [[38, 5, 22], [38, 5, 12], [38, 5, 5], [33, 5, 15], [44, 5, 20]]},
        {'id': 'treasure', 'name': '东北藏宝小室', 'bounds_xz': [54, 8, 70, 27], 'wall_top_y': 12, 'targets': [[56, 5, 21], [62, 5, 20], [61, 6, 18], [62, 6, 16]]},
        {'id': 'courtyard', 'name': '中央石像湿庭', 'bounds_xz': [24, 29, 52, 46], 'wall_top_y': 8, 'targets': [[38, 5, 30], [38, 5, 37], [34, 5, 38], [45, 5, 37], [38, 5, 45]]},
        {'id': 'flooded-hall', 'name': '西南四柱积水大厅', 'bounds_xz': [1, 33, 25, 59], 'wall_top_y': 12, 'targets': [[24, 5, 41], [13, 5, 41], [13, 5, 37], [13, 5, 55], [6, 5, 41]]},
        {'id': 'workshop', 'name': '东南工坊储藏室', 'bounds_xz': [52, 36, 70, 59], 'wall_top_y': 11, 'targets': [[54, 5, 41], [60, 5, 41], [60, 5, 50], [60, 5, 55]]},
        {'id': 'entrance', 'name': '南门厅与码头', 'bounds_xz': [30, 47, 47, 63], 'wall_top_y': 14, 'targets': [[38, 5, 72], [38, 5, 66], [38, 5, 62], [38, 5, 56], [38, 5, 49]]}
    ]
    corridor_targets = [[19, 5, 27], [19, 5, 31], [26, 5, 31], [38, 5, 26], [50, 5, 21], [50, 5, 29], [26, 5, 41], [50, 5, 41], [38, 5, 47]]
    for region in regions:
        if region['id'] in {'library', 'treasure', 'flooded-hall', 'workshop'}:
            region['wall_top_y'] = 10
        if region['id'] == 'north-hall':
            region['wall_top_y'] = 12
    waypoints = [point for region in regions for point in region['targets']] + corridor_targets
    metadata = {'id': 'S4-10', 'name': '七域沼泽遗迹总览', 'size': [73, 22, 73], 'sha256': digest, 'reference': str(REFERENCE), 'reference_sha256': hashlib.sha256(REFERENCE.read_bytes()).hexdigest(), 'previous_acceptance': str(previous_path), 'previous_sha256': previous_hash, 'entrance': [38, 5, 72], 'interior': [38, 5, 37], 'origin': [0, 0, 0], 'facing': 'south', 'axes': {'x': 'east', 'y': 'up', 'z': 'south'}, 'connections': [{'facing': 'south', 'position': [38, 5, 72], 'width': 3, 'height': 4, 'inward_depth': 3}], 'regions': regions, 'corridor_waypoints': corridor_targets, 'route_waypoints': waypoints, 'upper_floors': [], 'doors': [], 'chest': {'position': [62, 6, 15], 'facing': 'south', 'front_feet': [62, 6, 16], 'Items': [], 'LootTable': None}, 'registration': 'PENDING_INDEPENDENT_ACCEPTANCE', 'runtime_slot': None, 'world_placement_ticks_gameplay': 'NOT_RUN', 'integration': 'NOT_RUN', 'deployment': 'NOT_RUN', 'deviations': ['73x22x73 image-proportioned original reconstruction, not a measured blueprint; five distinct chambers plus courtyard and entrance, all one floor with a one-step chest dais.', 'Open-top ruins, actual full source has no complete roof. Wall cap timbers/stone kept. No upper gallery inferred from picture.', 'Reference wet floors reconstructed as contained shallow pools and dry stone crossing paths; entrance dock and central bridge use full planks at common foot plane, not raised slabs.', 'Closed doors absent in source and reference: gateway is permanently open. No door-open substitute or changed collision source used.', 'All containers deliberately empty with no LootTable; no awarded or generated treasure claimed.', 'No external swamp biome forest/fog, custom shader reflection, hidden underground levels or waterfall invented. Moss textures are seeded vanilla brick mixes.', 'Block-built low altar, courtyard statue and four hall pillars approximate reference sculpted objects. No mobs, armor stands, commands or spawners.', 'Support substrates below separate room footprints are deliberate installation foundations; surrounding unused volume is explicit air and must clear old terrain when placing fragments.']}
    save('incoming/S4-10/room.json', metadata)
    data = plain(read_nbt(target / 'room.nbt')[1])
    assert data['size'] == [73, 22, 73] and data['DataVersion'] == 3465 and data['entities'] == []
    states = {tuple(record['pos']): data['palette'][record['state']] for record in data['blocks']}
    assert len(states) == 73 * 22 * 73
    names = {point: state['Name'] for point, state in states.items()}
    counts = collections.Counter(names.values())
    prohibited = ['spawner', 'command_block', 'jigsaw', 'structure_block', 'lava', 'fire']
    assert not {'minecraft:' + name for name in prohibited}.intersection(counts)
    assert not [name for name in counts if name.endswith('_door') or name.endswith('_trapdoor') or name.endswith('_fence_gate')]
    water = {point for point, state in states.items() if state['Name'] == 'minecraft:water' or state.get('Properties', {}).get('waterlogged') == 'true'}
    directions = [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]
    edges = []
    for point in water:
        for delta in [directions[index] for index in [0, 1, 3, 4, 5]]:
            adjacent = tuple(point[axis] + delta[axis] for axis in range(3))
            if names.get(adjacent, 'minecraft:air') in {'minecraft:air', 'minecraft:vine', 'minecraft:lily_pad'}:
                edges.append([list(point), list(adjacent)])
    assert not edges, edges[:20]
    for point, name in names.items():
        if name == 'minecraft:lily_pad':
            assert names[(point[0], point[1] - 1, point[2])] == 'minecraft:water', point
    vegetation = {'minecraft:air', 'minecraft:water', 'minecraft:vine', 'minecraft:lily_pad', 'minecraft:stone_button', 'minecraft:flower_pot', 'minecraft:oak_pressure_plate'}
    solid = {point for point, name in names.items() if name not in vegetation}
    start = next(iter(solid))
    connected = {start}
    pending = [start]
    while pending:
        point = pending.pop()
        for delta in directions:
            adjacent = tuple(point[axis] + delta[axis] for axis in range(3))
            if adjacent in solid and adjacent not in connected:
                connected.add(adjacent)
                pending.append(adjacent)
    assert connected == solid, list(solid - connected)[:20]
    entities = [record for record in data['blocks'] if 'nbt' in record]
    chests = [record for record in entities if record['nbt']['id'] == 'minecraft:chest']
    barrels = [record for record in entities if record['nbt']['id'] == 'minecraft:barrel']
    assert len(entities) == 5 and len(chests) == 1 and len(barrels) == 4
    assert all(record['nbt']['Items'] == [] and 'LootTable' not in record['nbt'] for record in entities)
    assert chests[0]['pos'] == [62, 6, 15] and states[(62, 6, 15)]['Properties'] == {'facing': 'south', 'type': 'single', 'waterlogged': 'false'}
    lid_air = [(east, up, south) for east in range(61, 64) for up in range(7, 9) for south in range(14, 17)]
    assert all(names[point] == 'minecraft:air' for point in lid_air)
    leaves = [state for state in states.values() if state['Name'] == 'minecraft:oak_leaves']
    assert leaves and all(state['Properties']['persistent'] == 'true' for state in leaves)
    roofs = []
    for region in regions:
        west, north, east, south = region['bounds_xz']
        columns = [(column, row) for column in range(west + 3, east - 2) for row in range(north + 3, south - 2)]
        open_columns = sum(all(names[(column, up, row)] in vegetation for up in range(17, 22)) for column, row in columns)
        assert open_columns == len(columns), region['id']
        roofs.append({'region': region['id'], 'survey_y': [17, 21], 'open_columns': open_columns, 'survey_columns': len(columns), 'roof': 'INTENTIONAL_OPEN_TOP'})
    save('materials.json', dict(counts))
    save('geometry-check.json', {'status': 'PASS', 'sha256': digest, 'full_explicit_voxel_records': len(states), 'nonair_blocks': len(states) - counts['minecraft:air'], 'architectural_solid_components': 1, 'water_cells': len(water), 'water_bottom_side_air_edges': edges, 'intentional_exposed_waterfall_faces': [], 'persistent_leaf_blocks': len(leaves), 'lilies': counts['minecraft:lily_pad'], 'single_empty_chest': [62, 6, 15], 'empty_barrels': 4, 'conservative_lid_air_blocks': len(lid_air), 'chest_front_air': [[62, up, 16] for up in range(6, 9)], 'region_roofs': roofs, 'stairs': counts['minecraft:stone_brick_stairs'], 'doors_in_source': 0, 'upper_floors': 0, 'major_region_count': len(regions), 'region_sizes_xz': {region['id']: [region['bounds_xz'][2] - region['bounds_xz'][0] + 1, region['bounds_xz'][3] - region['bounds_xz'][1] + 1] for region in regions}, 'body_route_targets': len(waypoints), 'scope': 'Static actual source states and entities; world ticks, fluid propagation, GUI opening and physics NOT_RUN.'})
    from native import run_native
    run_native()


if __name__ == '__main__':
    main()
