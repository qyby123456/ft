from collections import Counter, deque
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
TARGET = ROOT / 'incoming/G2B-04'
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import AIR, Structure


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def cut(direction, point):
    east, up, south = point
    roof = up >= 16 and south >= 8
    near = {'north': 4 <= south <= 6, 'south': south >= 35, 'west': east <= 3 and south >= 4, 'east': east >= 25 and south >= 4}[direction]
    return roof or up >= 2 and near


def main():
    source = TARGET / 'room.nbt'
    structure = Structure.read(source)
    sha = digest(source)
    reference = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out/S2-04-great-hall.png')
    connections = [{'facing': side, 'position': point, 'width': width, 'height': height, 'outward_normal': normal} for side, point, width, height, normal in [('north', [14, 1, 0], 7, 4, [0, 0, -1]), ('south', [14, 3, 38], 5, 4, [0, 0, 1]), ('west', [0, 2, 19], 5, 4, [-1, 0, 0]), ('east', [28, 2, 19], 5, 4, [1, 0, 0])]]
    targets = [{'name': label, 'position': point} for label, point in [('north-landing', [14, 1, 1]), ('north-step', [14, 2, 2]), ('north-upper', [14, 2, 3]), ('front-hall', [14, 2, 8]), ('aisle-front', [14, 2, 12]), ('aisle-middle', [14, 2, 22]), ('aisle-back', [14, 2, 32]), ('west-aisle-front', [4, 2, 11]), ('east-aisle-front', [24, 2, 11]), ('west-aisle-back', [4, 2, 32]), ('east-aisle-back', [24, 2, 32]), ('south-step', [14, 3, 35]), ('west-threshold', [2, 2, 19]), ('east-threshold', [26, 2, 19])]]
    metadata = {'id': 'G2B-04', 'name': '砂岩旗帜列柱大殿', 'size': list(structure.size), 'entrance': [14, 1, 0], 'interior': [14, 2, 8], 'facing': 'north', 'connections': connections, 'route_targets': targets, 'reference': str(reference), 'reference_sha256': digest(reference), 'sha256': sha, 'coordinate_system': 'Local XYZ feet, east +X, north -Z', 'hall_feet_y': 2, 'roof': {'type': 'stepped sandstone vault', 'covered_z_inclusive': [8, 38], 'height_formula': '17+floor(min(x,28-x)/5)', 'front_z_4_to_7': 'INTENTIONALLY_OPEN_LOW_ENTRY_FORECOURT'}, 'landmark': {'type': 'long geometric aisle, paired triple column rows, wall banners and suspended lanterns', 'column_roots': [[east, 2, south] for east in [7, 21] for south in [11, 22, 32]], 'no_central_platform': True}, 'registration': 'PENDING_INDEPENDENT_ACCEPTANCE', 'runtime_slot': None, 'next_image': 'S2-05 LOCKED_NOT_STARTED', 'world_placement': 'NOT_RUN', 'deviations': ['Estimated block proportions, no pixel-identical claim', 'Vanilla wall banners approximate long pictorial cloth', 'Urns approximated by terracotta/flower pots', 'Exterior dunes and cacti omitted', 'Partial stepped vault reconstructed; front entry intentionally open, not a fully sealed room']}
    save(TARGET / 'room.json', metadata)
    materials = Counter(state.name for state in structure.blocks.values() if state != AIR)
    assert not materials['minecraft:water'] and not materials['minecraft:lava'] and not materials['minecraft:chest']
    assert not structure.entities and len(structure.block_entities) == 9
    fragile, lights, banners = [], [], []
    for point, state in structure.blocks.items():
        east, up, south = point
        if state.name in {'minecraft:flower_pot', 'minecraft:dead_bush'}:
            below = structure.get((east, up-1, south))
            assert below != AIR
            if state.name == 'minecraft:dead_bush':
                assert below.name == 'minecraft:sand'
                assert structure.get((east, up-2, south)) != AIR
            fragile.append(list(point))
        if state.name == 'minecraft:lantern':
            roof_y = 17 + min(east, 28-east) // 5
            assert structure.get((east, roof_y, south)).name == 'minecraft:cut_sandstone'
            assert all(structure.get((east, height, south)).name == 'minecraft:chain' for height in range(up+1, roof_y))
            lights.append({'position': list(point), 'direct_chain_to_roof_y': roof_y})
        if state.name == 'minecraft:orange_wall_banner':
            facing = dict(state.properties)['facing']
            offset = {'north': (0, 1), 'south': (0, -1), 'east': (-1, 0), 'west': (1, 0)}[facing]
            assert structure.get((east+offset[0], up, south+offset[1])) != AIR
            banners.append(list(point))
    assert len(fragile) == 12 and len(lights) == 8 and len(banners) == 9
    for connector in connections:
        east, up, south = connector['position']
        for offset in range(-(connector['width']//2), connector['width']//2+1):
            horizontal = (east, south+offset) if connector['facing'] in {'east', 'west'} else (east+offset, south)
            assert structure.get((horizontal[0], up-1, horizontal[1])) != AIR
            for height in range(connector['height']):
                assert structure.get((horizontal[0], up+height, horizontal[1])) == AIR
    solids = {point for point, state in structure.blocks.items() if state != AIR}
    reached = {point for point in solids if point[1] == 0}
    pending = deque(reached)
    while pending:
        east, up, south = pending.popleft()
        for neighbor in [(east-1, up, south), (east+1, up, south), (east, up-1, south), (east, up+1, south), (east, up, south-1), (east, up, south+1)]:
            if neighbor in solids and neighbor not in reached:
                reached.add(neighbor)
                pending.append(neighbor)
    assert not solids - reached, sorted(solids - reached)[:20]
    assert all(structure.get((east, 17 + min(east, 28-east)//5, south)).name == 'minecraft:cut_sandstone' for east in range(29) for south in range(8,39))
    save(TARGET / 'materials.json', dict(materials))
    save(TARGET / 'qa.json', {'status': 'STATIC_PASS_NATIVE_PENDING', 'sha256': sha, 'supported_pots_and_bushes': fragile, 'lantern_roof_connections': lights, 'backed_wall_banners': banners, 'six_face_solids': len(solids), 'six_face_connected_to_y0': len(reached), 'detached_solids': [], 'scope': 'Six-face graph is static topology, not physical simulation', 'liquids': 0, 'containers': 0, 'four_connectors_full_width_height_clear': True, 'rear_vault_complete_z8_to38': True, 'front_open_by_design': True, 'world_ticks': 'NOT_RUN'})
    save(ROOT / 'assembly.json', {'single_template': True, 'origin': [0, 0, 0], 'world_origin': None, 'bounds_inclusive': [[0,0,0],[28,20,38]], 'order': [{'step': 1, 'path': str(source), 'offset': [0,0,0]}], 'connections': connections, 'hall_feet_y': 2, 'warning': 'Multi-chunk full-volume occupancy required. North exterior Y1, hall/side Y2, south Y3. Partial vault with open front; weather, lighting/spawning, overhead terrain and world ticks NOT_RUN. No deployment permission.'})
    derivatives = []
    for direction in ['north', 'south', 'west', 'east']:
        preview = Structure.read(source)
        removed = []
        for point, state in list(preview.blocks.items()):
            if cut(direction, point) and state != AIR:
                preview.set(point, AIR)
                removed.append(point)
        path = ROOT / 'preview-only' / f'{direction}.nbt'
        preview.write(path)
        check = Structure.read(path)
        changed = set(removed)
        assert all(check.get(point) == state for point, state in structure.blocks.items() if point not in changed)
        derivatives.append({'direction': direction, 'source': str(source), 'source_sha256': sha, 'derived': str(path), 'derived_sha256': digest(path), 'removed_nonair': len(removed), 'deployable': False, 'roof_and_beam_cut': 'y>=16,z>=8', 'near_wall_rule': {'north': 'y>=2,4<=z<=6', 'south': 'y>=2,z>=35', 'west': 'y>=2,x<=3,z>=4', 'east': 'y>=2,x>=25,z>=4'}[direction], 'only_declared_air_replacements': True})
    save(ROOT / 'derivation.json', {'source_sha256': sha, 'exterior': str(source), 'derivatives': derivatives})
    save(ROOT / 'qa/route-input.json', [{'group': 2, 'candidate': str(source), 'sha256': sha, 'size': list(structure.size), 'start': metadata['entrance'], 'swimming': False, 'targets': [{'label': item['facing'], 'position': item['position']} for item in connections] + [{'label': item['name'], 'position': item['position']} for item in targets]}])
    print('S2-04 PREPARED ' + sha)


if __name__ == '__main__':
    main()
