import argparse
import hashlib
import json
from collections import Counter
from pathlib import Path

from nbtio import AIR, Structure, Tag, compound, plain, state, string


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-rooms-medium-random-thread3-20261102/out/R4-underground-spring-cave.png')
REFERENCE_HASH = 'a8047daa62bd14344fbe2cfe04426b5577f5e2ddedbf2e2930c67d2c7be8b827'
SIZE = (17, 13, 19)


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def masonry(position, floor=False):
    pos_x, pos_y, pos_z = position
    selector = (pos_x * 37 + pos_y * 19 + pos_z * 53) % 13
    names = ['stone_bricks', 'mossy_stone_bricks', 'cracked_stone_bricks', 'cobbled_deepslate']
    return state(names[1] if selector < (4 if floor else 5) else names[2] if selector == 5 else names[3] if selector == 6 else names[0])


def construct():
    room = Structure(SIZE)
    features = {}

    def place(position, block, group=None, block_entity=None):
        room.set(position, block, block_entity)
        if group:
            features.setdefault(group, []).append(list(position))

    for pos_y in range(SIZE[1]):
        for pos_z in range(SIZE[2]):
            for pos_x in range(SIZE[0]):
                position = (pos_x, pos_y, pos_z)
                shell = pos_y in (0, 1, 2, 12) or pos_x in (0, 16) or pos_z in (0, 18)
                place(position, masonry(position, pos_y == 2) if shell else AIR)
    for pos_x in range(7, 10):
        for pos_y in range(3, 6):
            place((pos_x, pos_y, 0), AIR)
    for pos_x in range(6, 11):
        place((pos_x, 6, 0), state('chiseled_stone_bricks'), 'portal_lintel')
    for pos_z in (4, 15):
        for pos_x in (2, 14):
            for pos_y in range(3, 12):
                place((pos_x, pos_y, pos_z), masonry((pos_x, pos_y, pos_z)), 'pillars')
            for offset in (-1, 1):
                place((pos_x + offset, 10, pos_z), state('stone_bricks'), 'capitals')
        for pos_x in range(2, 15):
            place((pos_x, 11, pos_z), masonry((pos_x, 11, pos_z)), 'roof_ribs')

    water_positions = set()
    for pos_x in range(3, 14):
        for pos_z in range(3, 15):
            open_basin = (pos_z <= 5 and pos_x not in (7, 8, 9)) or pos_z >= 11
            under_bridge = 6 <= pos_z <= 10 and not (pos_x in (4, 8, 12) and pos_z in (7, 9))
            for pos_y in (1, 2):
                if open_basin or (under_bridge and pos_y == 1):
                    water_positions.add((pos_x, pos_y, pos_z))
                    place((pos_x, pos_y, pos_z), state('water', level=0), 'pool_water')
    for pos_x, pos_z in ((4, 4), (12, 4), (4, 12), (8, 13), (12, 12)):
        place((pos_x, 0, pos_z), state('sea_lantern'), 'submerged_lights')
    for pos_x in range(3, 14):
        for pos_z in (6, 10):
            if pos_z == 6 and 7 <= pos_x <= 9:
                continue
            place((pos_x, 3, pos_z), state('spruce_fence', east=pos_x < 13 and not (pos_z == 6 and pos_x == 6), west=pos_x > 3 and not (pos_z == 6 and pos_x == 10), north=False, south=False, waterlogged=False), 'bridge_rails')
    for pos_z in range(6, 11):
        for pos_x in range(1, 16):
            place((pos_x, 2, pos_z), state('stone_bricks'), 'bridge_deck')

    for pos_z, height in ((15, 3), (16, 5), (17, 6)):
        for pos_x in range(6, 11):
            for pos_y in range(3, height + 1):
                place((pos_x, pos_y, pos_z), masonry((pos_x, pos_y, pos_z)), 'spring_rock')
    for pos_x in (7, 9):
        for pos_z in range(15, 18):
            for pos_y in range(3, 9):
                place((pos_x, pos_y, pos_z), masonry((pos_x, pos_y, pos_z)), 'spring_cheeks')
    place((8, 7, 17), state('water', level=0), 'spring_source')
    for pos_x in (6, 10):
        place((pos_x, 7, 17), state('amethyst_cluster', facing='up', waterlogged=False), 'crystals')

    for post_x, lamp_x in ((2, 3), (14, 13)):
        for pos_y in range(3, 7):
            place((post_x, pos_y, 6), state('stripped_spruce_log', axis='y'), 'lamp_posts')
        place((lamp_x, 6, 6), state('stripped_spruce_log', axis='x'), 'lamp_arms')
        place((lamp_x, 5, 6), state('lantern', hanging=True, waterlogged=False), 'lanterns')

    for pos_x, pos_z, length in ((4, 2, 3), (12, 2, 2), (5, 17, 3), (11, 17, 2), (1, 10, 3), (15, 10, 3)):
        for offset in range(length):
            thickness = 'tip' if offset == length - 1 else 'frustum' if offset == length - 2 else 'base' if offset == 0 else 'middle'
            place((pos_x, 11 - offset, pos_z), state('pointed_dripstone', thickness=thickness, vertical_direction='down', waterlogged=False), 'stalactites')
    for pos_x, pos_z, length in ((1, 2, 2), (15, 2, 2), (1, 16, 3), (15, 16, 2)):
        for offset in range(length):
            thickness = 'tip' if offset == length - 1 else 'frustum' if offset == length - 2 else 'base'
            place((pos_x, 3 + offset, pos_z), state('pointed_dripstone', thickness=thickness, vertical_direction='up', waterlogged=False), 'stalagmites')
    for pos_x, pos_z, length in ((1, 5, 3), (15, 5, 4), (1, 13, 4), (15, 13, 3), (4, 17, 4), (12, 17, 4)):
        for offset in range(length):
            block = 'cave_vines' if offset == length - 1 else 'cave_vines_plant'
            properties = {'berries': offset == length - 1}
            if block == 'cave_vines':
                properties['age'] = 0
            place((pos_x, 11 - offset, pos_z), state(block, **properties), 'hanging_vines')
    for pos_x, pos_z in ((1, 11), (15, 11), (5, 16), (11, 16)):
        place((pos_x, 2, pos_z), state('warped_nylium'), 'fungus_soil')
        place((pos_x, 3, pos_z), state('warped_fungus'), 'cyan_fungi')
    for pos_x, pos_z in ((3, 1), (13, 1), (3, 17), (13, 17)):
        place((pos_x, 3, pos_z), state('moss_block'), 'crystal_plinths')
        place((pos_x, 4, pos_z), state('amethyst_cluster', facing='up', waterlogged=False), 'crystals')
    for position in ((3, 3, 16), (4, 3, 16), (3, 4, 16)):
        place(position, state('barrel', facing='north', open=False), 'empty_barrels', compound(id=string('minecraft:barrel'), Items=Tag(9, [], 10)))
    for pos_x, pos_z in ((1, 1), (15, 1), (4, 2), (12, 2), (1, 17), (15, 17)):
        place((pos_x, 3, pos_z), state('moss_carpet'), 'moss')
    for pos_z in (4, 12, 16):
        for pos_x, face in ((1, 'west'), (15, 'east')):
            properties = {side: side == face for side in ('north', 'south', 'east', 'west', 'up', 'down')}
            place((pos_x, 6, pos_z), state('glow_lichen', waterlogged=False, **properties), 'wall_lichen')

    main_clearance = [[pos_x, pos_y, pos_z] for pos_x in range(7, 10) for pos_y in range(3, 6) for pos_z in range(0, 10)]
    bridge_clearance = [[pos_x, pos_y, pos_z] for pos_x in range(1, 16) for pos_y in range(3, 6) for pos_z in range(7, 10)]
    required_feet = [[8, 3, pos_z] for pos_z in range(10)] + [[pos_x, 3, 8] for pos_x in range(1, 16)]
    required_feet += [[1, 3, pos_z] for pos_z in range(8, 16)] + [[15, 3, pos_z] for pos_z in range(8, 16)]
    return room, {
        'size': SIZE, 'features': features, 'main_clearance': main_clearance,
        'bridge_clearance': bridge_clearance, 'required_feet': required_feet,
        'water_envelope': {'pool': [3, 1, 3, 13, 2, 14], 'cascade': [8, 3, 11, 8, 7, 17]},
        'water_source': [8, 7, 17], 'pool_depth': 2,
        'notes': ['All geometry uses vanilla 1.20.1 blocks.', 'Roof is retained in final NBT.', 'Main route and transverse bridge have 3x3 clearance.', 'Pool is connected below the supported stone bridge.'],
    }


def metadata(room, stage):
    return {
        'id': 'R4', 'name': '地下泉池洞穴', 'size': list(room.size),
        'entrance': [8, 3, 0], 'interior': [8, 3, 3], 'facing': 'north',
        'sha256': digest(ROOT / 'room.nbt'), 'reference': str(REFERENCE),
        'reference_sha256': REFERENCE_HASH, 'data_version': 3465,
        'stage': stage, 'deployment': 'independent_candidate_not_integrated_not_deployed',
        'coordinate_contract': 'Local integer XYZ; entrance and interior are player FEET coordinates.',
        'design_translation': ['17x13x19 actual volume; concept dimensions are approximate.', 'North central entrance, T-shaped dry stone route, supported bridge with rails, connected spring pools, real cascade, embedded columns, dripstone, hanging vines, empty supply barrels.', 'Vanilla amethyst and warped fungus represent the blue crystals and cyan fungi; no custom colored lighting, particles, entities, loot or trading.'],
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--adopt-native', action='store_true')
    parser.add_argument('--rebuild-final', action='store_true')
    args = parser.parse_args()
    for name in ('qa', 'preview', 'reference', 'src/main/resources/data/ftr4qa/structures'):
        (ROOT / name).mkdir(parents=True, exist_ok=True)
    if digest(REFERENCE) != REFERENCE_HASH:
        raise ValueError('Reference fingerprint mismatch')
    (ROOT / 'reference/R4-underground-spring-cave.png').write_bytes(REFERENCE.read_bytes())
    room, blueprint = construct()
    stage = 'bootstrap_source_water'
    if args.adopt_native:
        previous = json.loads((ROOT / 'qa/native.json').read_text(encoding='utf-8'))
        if previous['status'] != 'BOOTSTRAP_PASS':
            raise ValueError('Only a successful isolated bootstrap capture can be adopted')
        if previous['source_nbt_sha256'] != digest(ROOT / 'room.nbt'):
            raise ValueError('Bootstrap source hash differs from current source')
        (ROOT / 'qa/bootstrap-source.nbt').write_bytes((ROOT / 'room.nbt').read_bytes())
        write_json(ROOT / 'qa/bootstrap-native.json', previous)
        (ROOT / 'qa/bootstrap-forge-gametest.log').write_bytes((ROOT / 'qa/forge-gametest.log').read_bytes())
        before = room
        room = Structure.read(ROOT / 'qa/native-placed-none.nbt')
        for rotation in ('clockwise_90', 'clockwise_180', 'counterclockwise_90'):
            candidate = Structure.read(ROOT / f'qa/native-placed-{rotation}.nbt')
            if candidate.blocks != room.blocks or candidate.block_entities != room.block_entities:
                raise ValueError('Four inverse-rotated bootstrap captures differ')
        if {position: plain(entity) for position, entity in before.block_entities.items()} != {position: plain(entity) for position, entity in room.block_entities.items()}:
            raise ValueError('Native block entity contents differ from original empty barrels')
        room.block_entities = before.block_entities
        changes = [
            {'position': list(position), 'before': {'name': before.get(position).name, 'properties': dict(before.get(position).properties)}, 'after': {'name': block.name, 'properties': dict(block.properties)}}
            for position, block in room.blocks.items() if before.get(position) != block
        ]
        write_json(ROOT / 'qa/equilibration.json', {'bootstrap_source_sha256': previous['source_nbt_sha256'], 'ticks': 240, 'inverse_rotated_captures_equal': True, 'changes': changes})
        stage = 'native_equilibrated_final'
    if args.rebuild_final:
        equilibration = json.loads((ROOT / 'qa/equilibration.json').read_text(encoding='utf-8'))
        for change in equilibration['changes']:
            after = change['after']
            room.set(tuple(change['position']), state(after['name'], **after['properties']))
        stage = 'native_equilibrated_final'
    room.write(ROOT / 'room.nbt')
    write_json(ROOT / 'room.json', metadata(room, stage))
    write_json(ROOT / 'blueprint.json', blueprint)
    write_json(ROOT / 'materials.json', dict(sorted(Counter(block.name for block in room.blocks.values()).items())))
    Structure((1, 1, 1), {(0, 0, 0): AIR}).write(ROOT / 'src/main/resources/data/ftr4qa/structures/empty.nbt')
    print(json.dumps({'status': 'BUILT', 'stage': stage, 'sha256': digest(ROOT / 'room.nbt'), 'records': len(room.blocks)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
