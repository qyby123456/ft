import hashlib
import json
import random
from collections import Counter
from pathlib import Path

from PIL import Image

from nbtio import AIR, Structure, Tag, compound, state, string


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-rooms-medium-random-thread3-20261102/out/R5-stonemason-workshop.png')
REFERENCE_HASH = 'bec6b7196ece42321c289f968c93a0cb6e36b6ee8642e29543805b938b500dc3'
SIZE = (17, 11, 17)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    assert digest(REFERENCE) == REFERENCE_HASH
    assert Image.open(REFERENCE).size == (1536, 1024)
    room = Structure(SIZE)
    room.box((0, 0, 0), (16, 10, 16), AIR)
    rng = random.Random(2026110205)
    floor_choices = ['stone_bricks'] * 5 + ['cracked_stone_bricks'] * 2 + ['andesite', 'stone', 'cobblestone']
    for pos_z in range(17):
        for pos_x in range(17):
            room.set((pos_x, 0, pos_z), state(rng.choice(floor_choices)))
            room.set((pos_x, 10, pos_z), state('stone_bricks'))
    for pos_y in range(1, 10):
        for position in range(17):
            for pos_x, pos_z in ((position, 0), (position, 16), (0, position), (16, position)):
                material = rng.choice(['stone_bricks'] * 8 + ['cracked_stone_bricks'] * 2 + ['mossy_stone_bricks'])
                room.set((pos_x, pos_y, pos_z), state(material))
    for pos_x, pos_z in ((0, 0), (16, 0), (0, 16), (16, 16), (0, 8), (16, 8), (8, 16)):
        room.box((pos_x, 1, pos_z), (pos_x, 9, pos_z), state('polished_andesite'))
        room.set((pos_x, 2, pos_z), state('chiseled_stone_bricks'))
        room.set((pos_x, 8, pos_z), state('chiseled_stone_bricks'))
    for coordinate in range(1, 16):
        for pos_x, pos_z, axis in ((coordinate, 0, 'x'), (coordinate, 16, 'x'), (0, coordinate, 'z'), (16, coordinate, 'z')):
            room.set((pos_x, 8, pos_z), state('dark_oak_log', axis=axis))
    for pos_x in (6, 10):
        room.box((pos_x, 10, 0), (pos_x, 10, 16), state('dark_oak_planks'))
    room.box((7, 1, 0), (9, 3, 0), AIR)
    room.box((6, 1, 0), (6, 4, 0), state('polished_andesite'))
    room.box((10, 1, 0), (10, 4, 0), state('polished_andesite'))
    room.box((6, 4, 0), (10, 4, 0), state('chiseled_stone_bricks'))

    features = {}

    def feature(name, start, end, block_state):
        room.box(start, end, block_state)
        features.setdefault(name, []).extend([pos_x, pos_y, pos_z] for pos_y in range(start[1], end[1] + 1)
                                                for pos_z in range(start[2], end[2] + 1) for pos_x in range(start[0], end[0] + 1))

    def cell(name, position, block_state):
        feature(name, position, position, block_state)

    feature('statue_plinth', (2, 1, 11), (4, 1, 13), state('stone_bricks'))
    for pos_x in (2, 4):
        feature('unfinished_statue', (pos_x, 2, 12), (pos_x, 4, 12), state('polished_andesite'))
        cell('unfinished_statue', (pos_x, 2, 11), state('polished_andesite'))
    feature('unfinished_statue', (2, 5, 12), (4, 6, 12), state('polished_andesite'))
    feature('unfinished_statue', (1, 6, 12), (1, 7, 12), state('andesite'))
    cell('unfinished_statue', (5, 6, 12), state('polished_andesite'))
    cell('unfinished_statue', (3, 7, 12), state('polished_andesite'))
    cell('unfinished_statue', (3, 8, 12), state('chiseled_stone_bricks'))
    cell('unfinished_statue', (3, 8, 11), state('stone_brick_slab', type='top', waterlogged=False))

    scaffold_posts = [[1, 10], [5, 10], [1, 14], [5, 14]]
    for pos_x, pos_z in scaffold_posts:
        feature('scaffold_posts', (pos_x, 1, pos_z), (pos_x, 5, pos_z), state('spruce_log', axis='y'))
    for pos_z in range(10, 15):
        for pos_x in range(1, 6):
            if pos_x in (1, 5) or pos_z in (10, 14):
                cell('scaffold_platform', (pos_x, 4, pos_z), state('spruce_planks'))
                if (pos_x, pos_z) not in ((3, 10), (1, 10), (5, 10), (1, 14), (5, 14)):
                    cell('scaffold_rails', (pos_x, 5, pos_z), state('spruce_fence', north=False, east=False, south=False, west=False, waterlogged=False))
    feature('ladder_backing', (3, 1, 10), (3, 4, 10), state('spruce_planks'))
    ladders = []
    for pos_y in range(1, 5):
        position = (3, pos_y, 9)
        cell('ladder', position, state('ladder', facing='north', waterlogged=False))
        ladders.append({'position': list(position), 'backing': [3, pos_y, 10]})

    table_legs = [[11, 5], [14, 5], [11, 7], [14, 7]]
    for pos_x, pos_z in table_legs:
        cell('table_legs', (pos_x, 1, pos_z), state('stone_bricks'))
    feature('tabletop', (11, 2, 5), (14, 2, 7), state('polished_andesite'))
    cell('stonecutter', (12, 3, 6), state('stonecutter', facing='north'))
    cell('table_grindstone', (14, 3, 6), state('grindstone', face='floor', facing='west'))
    cell('tools', (11, 3, 6), state('stone_button', face='floor', facing='north', powered=False))
    cell('tools', (13, 3, 5), state('stone_button', face='floor', facing='east', powered=False))
    cell('table_offcuts', (12, 3, 7), state('chiseled_stone_bricks'))
    cell('table_offcuts', (11, 3, 7), state('stone_slab', type='bottom', waterlogged=False))

    feature('gargoyle_plinth', (12, 1, 11), (14, 1, 13), state('stone_bricks'))
    feature('gargoyle_plinth', (12, 2, 11), (14, 2, 13), state('polished_andesite'))
    feature('gargoyle_body', (13, 3, 12), (13, 5, 12), state('andesite'))
    cell('gargoyle_head', (13, 5, 11), state('chiseled_stone_bricks'))
    cell('gargoyle_snout', (13, 4, 10), state('stone_brick_stairs', facing='south', half='bottom', shape='straight', waterlogged=False))
    cell('gargoyle_snout', (13, 4, 11), state('polished_andesite'))
    for pos_x, facing in ((12, 'east'), (14, 'west')):
        cell('gargoyle_wings', (pos_x, 4, 12), state('cobblestone_stairs', facing=facing, half='bottom', shape='straight', waterlogged=False))
        cell('gargoyle_horns', (pos_x, 5, 12), state('cobblestone_wall', up=True, north='none', south='none', east='low' if pos_x == 12 else 'none', west='low' if pos_x == 14 else 'none', waterlogged=False))

    feature('cart_bed', (3, 1, 3), (4, 1, 5), state('spruce_planks'))
    for pos_x in (2, 5):
        cell('cart_wheels', (pos_x, 1, 4), state('grindstone', face='floor', facing='north'))
    for pos_x in (3, 4):
        cell('cart_handles', (pos_x, 1, 2), state('spruce_fence', north=False, south=True, east=False, west=False, waterlogged=False))
    feature('cart_stone', (3, 2, 4), (4, 2, 4), state('stone_bricks'))
    cell('cart_stone', (3, 2, 5), state('stone_brick_slab', type='bottom', waterlogged=False))
    cell('water_bucket', (14, 1, 2), state('water_cauldron', level=2))

    feature('tool_board', (10, 3, 15), (14, 4, 15), state('dark_oak_planks'))
    for pos_x in range(10, 15):
        cell('wall_tools', (pos_x, 4, 14), state('lever', face='wall', facing='north', powered=False))
    feature('wall_shelf', (10, 5, 15), (14, 5, 15), state('spruce_slab', type='top', waterlogged=False))
    for pos_x in (10, 12, 13):
        cell('shelf_offcuts', (pos_x, 6, 15), state('stone_brick_slab', type='bottom', waterlogged=False))
    feature('stone_stock', (6, 1, 14), (6, 3, 15), state('stone_bricks'))
    cell('stone_stock', (6, 4, 15), state('stone_brick_slab', type='bottom', waterlogged=False))
    feature('bench', (11, 1, 14), (13, 1, 14), state('spruce_planks'))
    cell('bench', (12, 2, 14), state('stone_brick_slab', type='bottom', waterlogged=False))
    cell('floor_grindstone', (14, 1, 14), state('grindstone', face='floor', facing='west'))
    for pos_x, pos_z in ((1, 7), (6, 12)):
        position = (pos_x, 1, pos_z)
        room.set(position, state('barrel', facing='up', open=False), compound(id=string('minecraft:barrel'), Items=Tag(9, [], 10)))
        features.setdefault('empty_barrels', []).append(list(position))
    for pos_x, pos_z in ((2, 7), (15, 9), (11, 10)):
        cell('rubble', (pos_x, 1, pos_z), state('cobblestone_slab', type='bottom', waterlogged=False))

    lamps = []
    for pos_x, pos_z in ((10, 4), (6, 12)):
        cell('lanterns', (pos_x, 7, pos_z), state('lantern', hanging=True, waterlogged=False))
        feature('chains', (pos_x, 8, pos_z), (pos_x, 9, pos_z), state('chain', axis='y', waterlogged=False))
        lamps.append({'lantern': [pos_x, 7, pos_z], 'chains': [[pos_x, 8, pos_z], [pos_x, 9, pos_z]], 'anchor': [pos_x, 10, pos_z]})

    full_names = {'stone_bricks', 'cracked_stone_bricks', 'mossy_stone_bricks', 'stone', 'andesite', 'polished_andesite', 'cobblestone', 'chiseled_stone_bricks', 'spruce_planks', 'spruce_log', 'dark_oak_planks', 'dark_oak_log', 'barrel'}
    steps = {'north': (0, 0, -1), 'east': (1, 0, 0), 'south': (0, 0, 1), 'west': (-1, 0, 0)}
    for position, block in list(room.blocks.items()):
        name = block.name.removeprefix('minecraft:')
        if name == 'spruce_fence':
            properties = {'waterlogged': False}
            for facing, offset in steps.items():
                other = room.get(tuple(position[axis] + offset[axis] for axis in range(3))).name.removeprefix('minecraft:')
                properties[facing] = other in full_names or other == 'spruce_fence'
            room.set(position, state(name, **properties))
    room.write(ROOT / 'room.nbt')
    required = [[pos_x, 1, pos_z] for pos_x in range(7, 10) for pos_z in range(16)]
    required += [[3, 1, 8], [10, 1, 6], [12, 1, 4], [12, 1, 8], [15, 1, 6], [11, 1, 12], [10, 1, 14], [6, 1, 11], [13, 1, 2]]
    blueprint = {
        'features': features,
        'main_aisle': [[pos_x, pos_y, pos_z] for pos_x in range(7, 10) for pos_y in range(1, 4) for pos_z in range(16)],
        'required_walkable': required,
        'table_legs': table_legs,
        'table_bounds': [11, 14, 5, 7],
        'scaffold_posts': scaffold_posts,
        'ladders': ladders,
        'lamps': lamps,
        'table_supports': [[11, 2, 6], [12, 2, 6], [14, 2, 6]],
        'statue_plinth': [[pos_x, 1, pos_z] for pos_x in range(2, 5) for pos_z in range(11, 14)],
        'gargoyle_plinth': [[pos_x, pos_y, pos_z] for pos_x in range(12, 15) for pos_y in (1, 2) for pos_z in range(11, 14)],
        'shelf_support': [[pos_x, 5, 16] for pos_x in range(10, 15)],
    }
    write_json(ROOT / 'qa/blueprint.json', blueprint)
    write_json(ROOT / 'room.json', {
        'id': 'R5', 'name': '石匠工坊', 'size': list(SIZE), 'entrance': [8, 1, 0], 'interior': [8, 1, 4], 'facing': 'north',
        'sha256': digest(ROOT / 'room.nbt'), 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH,
        'minecraft': '1.20.1', 'data_version': 3465, 'palette_type': 'single', 'roof': 'full',
        'description': '废弃石匠工坊：未完工石像脚手架、雕刻工作台、石翼兽、手推石料车、工具墙、空木桶与静态水缸。',
        'size_adaptation': '概念图无精确坐标；采用17×11×17保留三格主通道与落地家具，非声称逐像素还原。',
        'safety': {'north_opening': [3, 3], 'main_aisle_width': 3, 'containers_empty': True, 'entities': 0, 'free_fluids': 0, 'fire': 0},
        'deployment': 'NOT_DEPLOYED; isolated delivery only; no FT slot, core source or player save modified',
    })
    write_json(ROOT / 'qa/materials.json', dict(sorted(Counter(block.name for block in room.blocks.values()).items())))
    print(json.dumps({'status': 'BUILT', 'size': SIZE, 'records': len(room.blocks), 'sha256': digest(ROOT / 'room.nbt')}, ensure_ascii=False))


if __name__ == '__main__':
    main()
