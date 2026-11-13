import hashlib
import json
from pathlib import Path

from nbtio import AIR, Structure, Tag, compound, state, string


ROOT = Path(__file__).resolve().parents[1]
SIZE = (15, 9, 15)
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-rooms-medium-random-thread3-20261102/out/R2-abandoned-kitchen-mess.png')
REFERENCE_SHA256 = '8dab754ca881b61b789b78e3cfe09f12f26f6be4bbc56ea583f298cdb244865d'
ENTRANCE = (7, 1, 0)
INTERIOR = (7, 1, 3)
TARGETS = [(7, 1, 3), (7, 1, 7), (7, 1, 11), (3, 1, 11), (11, 1, 11), (2, 1, 4), (12, 1, 4)]


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def write_json(path, value):
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    Path(path).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def empty_entity(identifier):
    return compound(id=string('minecraft:' + identifier), Items=Tag(9, [], 10))


def build():
    assert digest(REFERENCE) == REFERENCE_SHA256
    room = Structure(SIZE)
    room.box((0, 0, 0), (14, 8, 14), AIR)
    materials = ['stone_bricks'] * 6 + ['cracked_stone_bricks'] * 3 + ['mossy_stone_bricks']
    for block_x in range(15):
        for block_z in range(15):
            room.set((block_x, 0, block_z), state(materials[(block_x * 11 + block_z * 7) % 10]))
            room.set((block_x, 8, block_z), state('stone_bricks' if block_x in (0, 14) or block_z in (0, 14) else 'dark_oak_planks'))
    for block_y in range(1, 8):
        for coordinate in range(15):
            for position in [(coordinate, block_y, 0), (coordinate, block_y, 14), (0, block_y, coordinate), (14, block_y, coordinate)]:
                room.set(position, state('cracked_stone_bricks' if sum(position) % 7 == 0 else 'stone_bricks'))
    for block_x in (0, 14):
        for block_z in (0, 7, 14):
            room.box((block_x, 1, block_z), (block_x, 7, block_z), state('polished_andesite'))
    for block_x in range(1, 14):
        room.set((block_x, 6, 14), state('dark_oak_log', axis='x'))
        room.set((block_x, 8, 7), state('dark_oak_log', axis='x'))
    for block_z in range(15):
        for block_x in (0, 14):
            room.set((block_x, 6, block_z), state('dark_oak_log', axis='z'))
        room.set((7, 8, block_z), state('dark_oak_log', axis='z'))
    for block_x in (1, 13):
        room.box((block_x, 1, 1), (block_x, 7, 1), state('polished_andesite'))
        room.set((block_x, 6, 1), state('dark_oak_log', axis='y'))
    room.box((6, 1, 0), (8, 3, 0), AIR)
    room.box((5, 4, 0), (9, 4, 0), state('chiseled_stone_bricks'))
    for block_x in (0, 14):
        for block_z in (5, 6):
            for block_y in (3, 4):
                room.set((block_x, block_y, block_z), state('iron_bars', north=True, south=True, east=False, west=False, waterlogged=False))
    for block_x in (5, 9):
        room.box((block_x, 1, 12), (block_x, 4, 13), state('stone_bricks'))
    room.box((5, 4, 12), (9, 4, 13), state('stone_bricks'))
    room.box((6, 5, 13), (8, 8, 14), state('stone_bricks'))
    room.box((6, 0, 12), (8, 0, 13), state('polished_andesite'))
    for block_x in (6, 8):
        room.set((block_x, 1, 13), state('smoker', facing='north', lit=False), empty_entity('smoker'))
    room.set((7, 1, 13), state('campfire', facing='north', lit=True, signal_fire=False, waterlogged=False), empty_entity('campfire'))
    room.set((7, 2, 13), state('cauldron'))
    room.set((7, 3, 13), state('chain', axis='y', waterlogged=False))
    for block_x in (6, 7, 8):
        room.set((block_x, 1, 12), state('iron_bars', north=False, south=block_x != 7, east=True, west=True, waterlogged=False))
    table_positions = []
    table_legs = []
    benches = []
    for start_x in (3, 10):
        for block_x in (start_x, start_x + 1):
            for block_z in range(5, 10):
                position = (block_x, 1, block_z)
                table_positions.append(position)
                if block_z in (5, 9):
                    table_legs.append(position)
                    room.set(position, state('spruce_stairs', half='top', facing='east' if block_x == start_x else 'west', shape='straight', waterlogged=False))
                else:
                    room.set(position, state('spruce_slab', type='top', waterlogged=False))
        for block_x in (start_x - 1, start_x + 2):
            for block_z in range(5, 10):
                benches.append((block_x, 1, block_z))
                room.set((block_x, 1, block_z), state('dark_oak_slab', type='bottom', waterlogged=False))
        for block_z in (6, 9):
            room.set((start_x, 2, block_z), state('flower_pot'))
        room.set((start_x + 1, 2, 5), state('candle', candles=2, lit=True, waterlogged=False))
        room.set((start_x + 1, 2, 8), state('potted_dead_bush'))
    for block_x in (1, 2, 3):
        room.set((block_x, 1, 13), state('spruce_planks'))
        for block_y in (2, 4):
            room.set((block_x, block_y, 13), state('spruce_trapdoor', facing='north', half='top', open=False, powered=False, waterlogged=False))
    for position in [(1, 1, 11), (1, 1, 12), (2, 1, 13), (13, 1, 11)]:
        room.set(position, state('barrel', facing='up' if position[2] != 13 else 'north', open=False), empty_entity('barrel'))
    for position in [(1, 3, 13), (2, 3, 13), (1, 5, 13)]:
        room.set(position, state('flower_pot'))
    room.set((2, 5, 13), state('potted_brown_mushroom'))
    room.set((3, 3, 13), state('candle', candles=1, lit=True, waterlogged=False))
    room.set((3, 5, 13), state('potted_dead_bush'))
    room.set((11, 1, 13), state('water_cauldron', level=3))
    for block_x in (12, 13):
        room.set((block_x, 1, 13), state('spruce_planks'))
        room.set((block_x, 2, 13), state('flower_pot' if block_x == 12 else 'potted_fern'))
    for block_x in (11, 12, 13):
        room.set((block_x, 4, 13), state('spruce_trapdoor', facing='north', half='top', open=False, powered=False, waterlogged=False))
        room.set((block_x, 5, 13), state('flower_pot' if block_x != 12 else 'potted_red_mushroom'))
    for block_x in (2, 3, 11, 12):
        room.set((block_x, 6, 12), state('dark_oak_log', axis='x'))
        room.set((block_x, 6, 13), state('dark_oak_log', axis='z'))
        room.set((block_x, 5, 12), state('hanging_roots'))
    for block_z in (3, 10):
        for block_y in (6, 7):
            room.set((7, block_y, block_z), state('chain', axis='y', waterlogged=False))
        room.set((7, 5, block_z), state('lantern', hanging=True, waterlogged=False))
    for position in [(1, 5, 2), (13, 5, 2), (4, 5, 13), (10, 5, 13)]:
        room.set(position, state('cobweb'))
    room.set((1, 1, 3), state('spruce_trapdoor', facing='east', half='bottom', open=True, powered=False, waterlogged=False))
    room.set((1, 2, 3), state('spruce_trapdoor', facing='east', half='bottom', open=True, powered=False, waterlogged=False))
    room.set((2, 1, 2), state('spruce_stairs', half='bottom', facing='west', shape='straight', waterlogged=False))
    room.set((3, 1, 2), state('spruce_slab', type='bottom', waterlogged=False))
    room.set((13, 1, 3), state('composter', level=0))
    room.write(ROOT / 'out/room.nbt')
    metadata = {
        'id': 'R2', 'name': '废弃厨房食堂', 'size': list(SIZE),
        'entrance': list(ENTRANCE), 'interior': list(INTERIOR), 'facing': 'north',
        'sha256': digest(ROOT / 'out/room.nbt'), 'reference': str(REFERENCE),
        'reference_sha256': REFERENCE_SHA256, 'minecraft': '1.20.1', 'data_version': 3465,
        'coordinate_order': 'XYZ; entrance/interior are player feet, not floor',
        'full_roof': True, 'entrance_clearance': [3, 3], 'central_aisle_x': [6, 7, 8],
        'walk_targets': TARGETS, 'theme': 'messy abandoned kitchen and refectory',
        'furniture': {'tables': 2, 'long_benches': 4, 'table_blocks': table_positions, 'table_leg_blocks': table_legs, 'bench_blocks': benches},
        'reward_inventory': False, 'deployed': False,
        'adaptations': ['15x15 footprint preserves two dining sets and a three-block central aisle.', 'Vanilla flower pots and potted plants stand in for crockery and old ingredients; no loose food entities.', 'A stone-enclosed non-spreading campfire stands in for embers; hanging cauldron has an anchored chain.', 'A real roof is retained; cutaway is a render-only operation.'],
    }
    write_json(ROOT / 'out/room.json', metadata)
    empty = Structure((1, 1, 1))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / 'forge/src/main/resources/data/ftr2qa/structures/empty.nbt')
    return room, metadata


if __name__ == '__main__':
    structure, metadata = build()
    print(json.dumps({'status': 'BUILT', 'size': SIZE, 'sha256': metadata['sha256']}, ensure_ascii=False))
