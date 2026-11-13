import collections
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
TARGET = ROOT / 'incoming/G2B-02'
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import AIR, Structure


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    reference = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out/S2-02-medium-room.png')
    structure = Structure.read(TARGET / 'room.nbt')
    sha = digest(TARGET / 'room.nbt')
    connections = [{'facing': side, 'position': point, 'width': width, 'height': 3, 'outward_normal': normal} for side, point, width, normal in [('north', [11, 1, 0], 7, [0, 0, -1]), ('south', [11, 3, 24], 3, [0, 0, 1]), ('east', [22, 3, 15], 3, [1, 0, 0])]]
    targets = [{'name': name, 'position': point} for name, point in [('north-landing', [11, 1, 1]), ('north-step', [11, 2, 2]), ('north-upper', [11, 2, 3]), ('north-threshold', [11, 2, 5]), ('pool-north', [11, 2, 7]), ('pool-west', [5, 2, 13]), ('pool-east', [17, 2, 13]), ('pool-south', [11, 2, 20]), ('storage-alcove', [3, 2, 18]), ('bookcase-access', [18, 2, 20]), ('south-step', [11, 3, 22]), ('south-threshold', [11, 3, 23]), ('east-step', [20, 3, 15]), ('east-threshold', [21, 3, 15])]]
    metadata = {'id': 'G2B-02', 'name': '砂岩藏书蓄水室', 'size': [23, 14, 25], 'entrance': [11, 1, 0], 'interior': [11, 2, 7], 'facing': 'north', 'connections': connections, 'route_targets': targets, 'coordinate_system': 'local XYZ feet; origin northwest lowest platform, east +X north -Z', 'reference': str(reference), 'reference_sha256': digest(reference), 'sha256': sha, 'landmark': {'type': 'raised shallow rectangular sealed basin', 'base': [[7, 2, 10], [15, 2, 16]], 'water': [[9, 3, 12], [13, 3, 14]], 'pool_is_not_walkway': True}, 'west_alcove': 'closed back; NOT external connection', 'roof': 'intact at y12', 'runtime_slot': None, 'registration': 'PENDING_INDEPENDENT_ACCEPTANCE', 'next_image': 'S2-03 LOCKED_NOT_STARTED', 'world_placement': 'NOT_RUN', 'deviations': ['Estimated block-scale dimensions; no pixel-identical claim', 'Intact safety roof; preview-only directional cuts separate', 'Vanilla flower pots over terracotta approximate broken urns', 'No invented altar lamps, high gallery, machinery or automatic loot']}
    save(TARGET / 'room.json', metadata)
    materials = collections.Counter(block.name for block in structure.blocks.values() if block != AIR)
    assert materials['minecraft:water'] == 15
    plants, lights, fluids = [], [], []
    for point, block in structure.blocks.items():
        east, up, south = point
        if block.name == 'minecraft:water':
            assert structure.get((east, up - 1, south)).name == 'minecraft:cut_sandstone'
            for delta_east, delta_south in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
                assert structure.get((east + delta_east, up, south + delta_south)).name in {'minecraft:water', 'minecraft:orange_terracotta', 'minecraft:chiseled_sandstone'}
            fluids.append(list(point))
        if block.name == 'minecraft:dead_bush':
            assert structure.get((east, up - 1, south)).name == 'minecraft:sand'
            plants.append(list(point))
        if block.name in {'minecraft:flower_pot', 'minecraft:sand'}:
            assert structure.get((east, up - 1, south)) != AIR
        if block.name == 'minecraft:lantern':
            assert structure.get((east, up + 1, south)).name == 'minecraft:chain'
            assert structure.get((east, up + 2, south)).name == 'minecraft:cut_sandstone'
            lights.append(list(point))
    assert len(plants) == 2 and len(lights) == 3
    assert all(structure.get((east, 12, south)) != AIR for east in range(23) for south in range(4, 25))
    assert not structure.entities
    for data in structure.block_entities.values():
        assert not data.value.get('Items') and not data.value.get('LootTable')
    save(TARGET / 'materials.json', dict(materials))
    save(TARGET / 'qa.json', {'status': 'STATIC_PASS_NATIVE_PENDING', 'sha256': sha, 'sealed_water_count': len(fluids), 'water_points': fluids, 'supported_dead_bushes': plants, 'supported_hanging_lanterns': lights, 'roof_intact': True, 'all_containers_empty': True, 'native_bound_tags_survival': 'NOT_RUN', 'world_ticks': 'NOT_RUN'})
    save(ROOT / 'assembly.json', {'origin': [0, 0, 0], 'bounds_inclusive': [[0, 0, 0], [22, 13, 24]], 'world_origin': None, 'single_template': True, 'order': [{'step': 1, 'path': str(TARGET / 'room.nbt'), 'offset': [0, 0, 0]}], 'hall_feet_y': 2, 'connections': connections, 'warning': 'North exterior Y1, hall Y2, south/east Y3. Whole-volume occupancy/fluid/entity/border checks required; do not place preview-only NBT.'})
    derivatives = []
    for direction in ['north', 'south', 'west', 'east']:
        preview = Structure.read(TARGET / 'room.nbt')
        removed = []
        for point, block in list(preview.blocks.items()):
            east, up, south = point
            roof = up >= 12 and south >= 4
            near = up >= 2 and {'north': 4 <= south <= 7, 'south': south >= 21, 'west': east <= 3 and south >= 4, 'east': east >= 19 and south >= 4}[direction]
            if (roof or near) and block != AIR:
                preview.set(point, AIR)
                removed.append(point)
        path = ROOT / 'preview-only' / f'{direction}.nbt'
        preview.write(path)
        check = Structure.read(path)
        assert all(check.get(point) == structure.get(point) for point in structure.blocks if point not in set(removed))
        derivatives.append({'direction': direction, 'source': str(TARGET / 'room.nbt'), 'source_sha256': sha, 'derived': str(path), 'derived_sha256': digest(path), 'deployable': False, 'removed_nonair': len(removed), 'rules': {'roof': 'y>=12,z>=4', 'near_wall': {'north': 'y>=2,4<=z<=7', 'south': 'y>=2,z>=21', 'west': 'y>=2,x<=3,z>=4', 'east': 'y>=2,x>=19,z>=4'}[direction]}, 'only_declared_air_replacements': True})
    save(ROOT / 'derivation.json', {'source_sha256': sha, 'derivatives': derivatives, 'exterior': str(TARGET / 'room.nbt')})
    save(ROOT / 'qa/route-input.json', [{'group': 2, 'candidate': str(TARGET / 'room.nbt'), 'sha256': sha, 'size': list(structure.size), 'start': metadata['entrance'], 'swimming': False, 'targets': [{'label': item['facing'], 'position': item['position']} for item in connections] + [{'label': item['name'], 'position': item['position']} for item in targets]}])
    print('S2-02 ONLY PREPARED ' + sha)


if __name__ == '__main__':
    main()
