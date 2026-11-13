import collections
import hashlib
import json
from pathlib import Path
import shutil
import sys

ROOT = Path(__file__).resolve().parent
TOOLS = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-09-treasure.png')
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import plain, read_nbt


def save(relative, value):
    (ROOT / relative).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    target = ROOT / 'incoming/S4-09'
    target.mkdir(parents=True, exist_ok=True)
    shutil.copy2(ROOT / 'nbt/s4-09-twin-warden-treasure-sanctum.nbt', target / 'room.nbt')
    digest = hashlib.sha256((target / 'room.nbt').read_bytes()).hexdigest()
    previous_path = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-08-independent/independent-details.json')
    previous = json.loads(previous_path.read_text(encoding='utf-8'))
    assert previous['status'] == 'PASS'
    previous['sha256'] = 'e0d77c77057d8587096cd1e36b3ccff7a5b8cb01e33eef977ba24a928b116223'
    assert hashlib.sha256((ROOT.parent / 'S4-08/incoming/S4-08/room.nbt').read_bytes()).hexdigest() == previous['sha256']
    waypoints = [[15, 4, 30], [15, 4, 27], [15, 4, 23], [15, 4, 19], [15, 5, 18], [15, 6, 17], [15, 6, 15], [15, 6, 13], [15, 7, 12], [15, 8, 11], [15, 9, 10], [15, 9, 9], [15, 9, 8], [14, 9, 7], [16, 9, 7], [0, 6, 12], [30, 6, 12], [5, 6, 13], [25, 6, 13], [7, 6, 9], [24, 6, 10], [9, 6, 10], [21, 6, 10]]
    ports = [('south', [15, 4, 30]), ('west', [0, 6, 12]), ('east', [30, 6, 12])]
    metadata = {'id': 'S4-09', 'name': '双卫湿地藏宝高台', 'size': [31, 22, 31], 'sha256': digest, 'reference': str(REFERENCE), 'reference_sha256': hashlib.sha256(REFERENCE.read_bytes()).hexdigest(), 'previous_acceptance': str(previous_path), 'previous_sha256': previous['sha256'], 'entrance': [15, 4, 30], 'interior': [15, 6, 15], 'facing': 'south', 'origin': [0, 0, 0], 'axes': {'x': 'east', 'y': 'up', 'z': 'south'}, 'connections': [{'facing': facing, 'position': point, 'width': 3, 'height': 4, 'inward_depth': 3} for facing, point in ports], 'route_waypoints': waypoints, 'chest': {'position': [15, 9, 7], 'facing': 'south', 'front_feet': [15, 9, 8], 'items': [], 'LootTable': None, 'policy': 'Explicit empty chest; no treasure contents or loot generation supplied. Room genre only, not a stocked reward.'}, 'runtime_slot': None, 'registration': 'PENDING_INDEPENDENT_ACCEPTANCE', 'world_placement_ticks_gameplay': 'NOT_RUN', 'integration': 'NOT_RUN', 'deployment': 'NOT_RUN', 'deviations': ['31x22x31 estimated from visual proportions; not a measured blueprint.', 'Open-top ruined chamber follows visible reference silhouette; no complete ceiling was inferred. Rear/side stepped caps retained and quantified.', 'Paired immobile block-built moss-stone wardens with button faces approximate sculpted guardians; no mobs/armor stands/spawners.', 'True single chest deliberately empty without LootTable; 4 side barrels also empty. No existing treasure or generated loot claimed.', 'Flower pots and wood shelves approximate pottery display; no decorated-pot custom sherds or textures.', 'Brown patterned wall banners retain gold lozenges in actual block entities; default preview may omit patterns.', 'No courtyard tree, upper gallery or waterfall copied from08. External swamp forest, shader mist and reflected light omitted.']}
    save('incoming/S4-09/room.json', metadata)
    data = plain(read_nbt(target / 'room.nbt')[1])
    assert data['size'] == [31, 22, 31] and not data['entities']
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
    vegetation = {'minecraft:air', 'minecraft:water', 'minecraft:vine', 'minecraft:lily_pad', 'minecraft:cave_vines', 'minecraft:cave_vines_plant', 'minecraft:brown_wall_banner', 'minecraft:stone_button', 'minecraft:flower_pot'}
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
    chests = [record for record in entities if record['nbt']['id'] == 'minecraft:chest']
    assert len(chests) == 1 and chests[0]['pos'] == [15, 9, 7]
    assert chests[0]['nbt']['Items'] == [] and 'LootTable' not in chests[0]['nbt']
    assert states[(15, 9, 7)]['Properties'] == {'facing': 'south', 'type': 'single', 'waterlogged': 'false'}
    barrels = [record for record in entities if record['nbt']['id'] == 'minecraft:barrel']
    assert len(barrels) == 4 and all(record['nbt']['Items'] == [] and 'LootTable' not in record['nbt'] for record in barrels)
    banners = [record for record in entities if record['nbt']['id'] == 'minecraft:banner']
    assert len(banners) == 2 and all(len(record['nbt']['Patterns']) == 3 for record in banners)
    lid_air = [(east, up, south) for east in range(14, 17) for up in range(10, 12) for south in range(6, 9)]
    assert all(names.get(point, 'minecraft:air') == 'minecraft:air' for point in lid_air)
    persistent = [state for state in states.values() if state['Name'] == 'minecraft:oak_leaves']
    assert persistent and all(state['Properties']['persistent'] == 'true' for state in persistent)
    assert counts['minecraft:flower_pot'] == 8
    assert counts['minecraft:stone_brick_stairs'] == 25
    assert all(names[(east, 12, 7)] == 'minecraft:chiseled_stone_bricks' for east in [9, 21])
    open_columns = sum(all(names.get((east, up, south), 'minecraft:air') in vegetation for up in range(10, 22)) for east in range(3, 28) for south in range(18, 28))
    assert open_columns == 250
    save('materials.json', dict(counts))
    save('geometry-check.json', {'status': 'PASS', 'sha256': digest, 'water_bottom_side_air_edges': 0, 'architectural_solid_components': 1, 'single_empty_chest': [15, 9, 7], 'chest_front_feet': [15, 9, 8], 'conservative_lid_air_volume': {'from': [14, 10, 6], 'to': [16, 11, 8], 'air_blocks': len(lid_air)}, 'LootTable': None, 'empty_barrels': 4, 'patterned_banner_entities': 2, 'guardians': [[9, 12, 7], [21, 12, 7]], 'flower_pots': 8, 'persistent_leaf_blocks': len(persistent), 'water_surface_columns': counts['minecraft:water'], 'open_forecourt_columns_no_roof_y10_to21': open_columns, 'roof_scope': 'Intentional open-top ruin; original stepped rear and side caps retained, not a roofed room.', 'stairs': 25, 'scope': 'Static container/lid/plant/water/support checks; actual chest interaction/loot generation/world ticks NOT_RUN.'})
    from native import run_native
    run_native()


if __name__ == '__main__':
    main()
