from collections import Counter, deque
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
TARGET = ROOT / 'incoming/G2B-03'
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import AIR, Structure


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    source = TARGET / 'room.nbt'
    structure = Structure.read(source)
    sha = digest(source)
    reference = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out/S2-03-small-room.png')
    connections = [{'facing': side, 'position': point, 'width': width, 'height': height, 'outward_normal': normal} for side, point, width, height, normal in [('north', [9, 1, 0], 5, 3, [0, 0, -1]), ('south', [9, 2, 20], 3, 4, [0, 0, 1]), ('east', [18, 2, 13], 3, 4, [1, 0, 0])]]
    targets = [{'name': label, 'position': point} for label, point in [('north-landing', [9, 1, 1]), ('north-step', [9, 2, 2]), ('north-upper', [9, 2, 3]), ('north-threshold', [9, 2, 5]), ('chest-north', [9, 2, 8]), ('chest-west', [6, 2, 11]), ('chest-east', [12, 2, 11]), ('chest-south', [9, 2, 15]), ('west-storage-access', [5, 2, 14]), ('east-niche-access', [14, 2, 9]), ('south-threshold', [9, 2, 18]), ('south-outer', [9, 2, 19]), ('east-threshold', [17, 2, 13]), ('east-inner', [15, 2, 13])]]
    metadata = {'id': 'G2B-03', 'name': '砂岩单箱遗迹庭院', 'size': list(structure.size), 'entrance': [9, 1, 0], 'interior': [9, 2, 8], 'facing': 'north', 'connections': connections, 'route_targets': targets, 'reference': str(reference), 'reference_sha256': digest(reference), 'sha256': sha, 'coordinate_system': 'Local XYZ feet, east +X, north -Z', 'hall_feet_y': 2, 'open_sky': True, 'roof': 'INTENTIONALLY_ABSENT_REFERENCE_OPEN_COURTYARD', 'landmark': {'type': 'single empty chest on low 3x3 slab surround, central 1x1 full support', 'chest': [9, 3, 11], 'base_inclusive': [[8, 2, 10], [10, 2, 12]], 'lid_clear': True}, 'west_niche': 'Storage alcove, not connector', 'registration': 'PENDING_INDEPENDENT_ACCEPTANCE', 'runtime_slot': None, 'next_image': 'S2-04 LOCKED_NOT_STARTED', 'world_placement': 'NOT_RUN', 'deviations': ['Estimated block proportions, no pixel-identical claim', 'Urns approximated by terracotta and flower pots', 'Dry hanging foliage approximated by wall-connected terracotta, not vines', 'Exterior dunes/cacti omitted from module; no hazardous cactus at connectors', 'Open sky intentional; integration must evaluate weather, light, spawning and overhead terrain']}
    save(TARGET / 'room.json', metadata)
    materials = Counter(state.name for state in structure.blocks.values() if state != AIR)
    assert not materials['minecraft:water'] and not materials['minecraft:lava']
    assert materials['minecraft:chest'] == 1 and not structure.entities
    assert len(structure.block_entities) == 1
    for data in structure.block_entities.values():
        assert not data.value.get('Items') and not data.value.get('LootTable')
    assert structure.get((9, 4, 11)) == AIR and structure.get((9, 2, 11)).name == 'minecraft:cut_sandstone'
    fragile = []
    for point, state in structure.blocks.items():
        if state.name in {'minecraft:flower_pot', 'minecraft:dead_bush'}:
            below = structure.get((point[0], point[1] - 1, point[2]))
            assert below != AIR
            if state.name == 'minecraft:dead_bush':
                assert below.name == 'minecraft:sand'
            fragile.append(list(point))
    assert len(fragile) == 10
    for connector in connections:
        east, up, south = connector['position']
        for offset in range(-(connector['width'] // 2), connector['width'] // 2 + 1):
            horizontal = (east, south + offset) if connector['facing'] == 'east' else (east + offset, south)
            assert structure.get((horizontal[0], up - 1, horizontal[1])) != AIR
            for height in range(connector['height']):
                assert structure.get((horizontal[0], up + height, horizontal[1])) == AIR
    solids = {point for point, state in structure.blocks.items() if state != AIR}
    reached = {point for point in solids if point[1] == 0}
    pending = deque(reached)
    while pending:
        east, up, south = pending.popleft()
        for neighbor in [(east-1, up, south), (east+1, up, south), (east, up-1, south), (east, up+1, south), (east, up, south-1), (east, up, south+1)]:
            if neighbor in solids and neighbor not in reached:
                reached.add(neighbor)
                pending.append(neighbor)
    assert not solids - reached, sorted(solids - reached)
    assert all(structure.get((east, 10, south)) == AIR for east in range(4, 15) for south in range(6, 17))
    save(TARGET / 'materials.json', dict(materials))
    save(TARGET / 'qa.json', {'status': 'STATIC_PASS_NATIVE_PENDING', 'sha256': sha, 'supported_fragile_positions': fragile, 'six_face_solids': len(solids), 'six_face_connected_to_y0': len(reached), 'detached_solids': [], 'scope': 'Six-face graph is static connectivity, not physical attachment proof', 'liquids': 0, 'containers_empty': True, 'chest_lid_clear': True, 'three_connectors_full_width_height_clear': True, 'roof_absent_by_design': True, 'world_ticks': 'NOT_RUN'})
    save(ROOT / 'assembly.json', {'single_template': True, 'origin': [0, 0, 0], 'world_origin': None, 'bounds_inclusive': [[0, 0, 0], [18, 10, 20]], 'order': [{'step': 1, 'path': str(source), 'offset': [0, 0, 0]}], 'connections': connections, 'hall_feet_y': 2, 'warning': 'Open sky. North exterior Y1, hall/south/east Y2. No world placement authorization. Evaluate full occupancy, weather/light/spawning/overhead terrain before any deployment.'})
    derivatives = []
    for direction in ['north', 'south', 'west', 'east']:
        preview = Structure.read(source)
        removed = []
        for point, state in list(preview.blocks.items()):
            east, up, south = point
            near = {'north': 4 <= south <= 5, 'south': south >= 17, 'west': east <= 3 and south >= 4, 'east': east >= 15 and south >= 4}[direction]
            if up >= 2 and near and state != AIR:
                preview.set(point, AIR)
                removed.append(point)
        path = ROOT / 'preview-only' / f'{direction}.nbt'
        preview.write(path)
        check = Structure.read(path)
        changed = set(removed)
        assert all(check.get(point) == state for point, state in structure.blocks.items() if point not in changed)
        derivatives.append({'direction': direction, 'source': str(source), 'source_sha256': sha, 'derived': str(path), 'derived_sha256': digest(path), 'removed_nonair': len(removed), 'deployable': False, 'roof_cut': 'NONE_SOURCE_HAS_NO_ROOF', 'near_wall_rule': {'north': 'y>=2,4<=z<=5', 'south': 'y>=2,z>=17', 'west': 'y>=2,x<=3,z>=4', 'east': 'y>=2,x>=15,z>=4'}[direction], 'only_declared_air_replacements': True})
    save(ROOT / 'derivation.json', {'source_sha256': sha, 'exterior': str(source), 'derivatives': derivatives})
    save(ROOT / 'qa/route-input.json', [{'group': 2, 'candidate': str(source), 'sha256': sha, 'size': list(structure.size), 'start': metadata['entrance'], 'swimming': False, 'targets': [{'label': item['facing'], 'position': item['position']} for item in connections] + [{'label': item['name'], 'position': item['position']} for item in targets]}])
    print('S2-03 PREPARED ' + sha)


if __name__ == '__main__':
    main()
