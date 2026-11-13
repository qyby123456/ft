import hashlib
import json
from collections import Counter
from pathlib import Path

from nbtio import AIR, Structure, Tag, compound, state, string


ROOT = Path(__file__).resolve().parents[1]
SIZE = (23, 18, 25)
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-stair-rooms-20261102/out/stair-ruined.png')
REFERENCE_HASH = '88c6aa37c0987bac1df530c263e96a0424528e3f51b3797b1c18ab5fb491fab5'
STEPS = {'north': (0, 0, -1), 'east': (1, 0, 0), 'south': (0, 0, 1), 'west': (-1, 0, 0)}


def fingerprint(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def save_json(path, value):
    Path(path).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def masonry(position):
    pos_x, pos_y, pos_z = position
    variation = (pos_x * 37 + pos_y * 19 + pos_z * 43) % 23
    return state('mossy_stone_bricks' if variation < 5 else 'cracked_stone_bricks' if variation < 10 else 'stone_bricks')


def build():
    for source in (REFERENCE, ROOT / 'reference/stair-ruined.png'):
        if fingerprint(source) != REFERENCE_HASH:
            raise ValueError('Reference fingerprint differs: ' + str(source))
    room = Structure(SIZE)
    features = {name: [] for name in ('stairs_up', 'stairs_down', 'debris', 'broken_top', 'beam', 'beam_piers', 'rails', 'cobwebs', 'empty_barrels', 'torches', 'lanterns', 'chains', 'cold_window', 'carpet')}

    def put(position, block=None):
        room.set(tuple(position), masonry(position) if block is None else block)

    def solid(start, end):
        for pos_y in range(start[1], end[1] + 1):
            for pos_z in range(start[2], end[2] + 1):
                for pos_x in range(start[0], end[0] + 1):
                    put((pos_x, pos_y, pos_z))

    for pos_y in range(SIZE[1]):
        for pos_z in range(SIZE[2]):
            for pos_x in range(SIZE[0]):
                position = (pos_x, pos_y, pos_z)
                outer = pos_y in (0, 17) or pos_x in (0, 22) or pos_z in (0, 24)
                put(position, masonry(position) if outer else AIR)
    solid((1, 5, 1), (21, 5, 9))
    solid((8, 5, 10), (14, 5, 15))
    solid((1, 11, 16), (21, 11, 23))
    for pos_z in (3, 8):
        for pos_x in (2, 8, 14, 20):
            solid((pos_x, 1, pos_z), (pos_x, 4, pos_z))
    for pos_z in (16, 23):
        solid((1, 10, pos_z), (21, 10, pos_z))
        for pos_x in (2, 8, 14, 20):
            solid((pos_x, 1, pos_z), (pos_x, 9, pos_z))
    for pos_x in (1, 21):
        for pos_z in (1, 23):
            solid((pos_x, 1, pos_z), (pos_x, 16, pos_z))

    for pos_z in range(10, 16):
        stair_y = pos_z - 4
        for pos_x in range(3, 8):
            solid((pos_x, 1, pos_z), (pos_x, stair_y - 1, pos_z))
            put((pos_x, stair_y, pos_z), state('stone_brick_stairs', facing='south', half='bottom', shape='straight', waterlogged='false'))
            features['stairs_up'].append([pos_x, stair_y, pos_z])
        solid((8, 1, pos_z), (8, stair_y + 1, pos_z))
    for pos_z in (14, 15):
        for pos_x in (3, 4):
            stair = [pos_x, pos_z - 4, pos_z]
            features['stairs_up'].remove(stair)
            features['broken_top'].append(stair)
            room.box((pos_x, 9, pos_z), (pos_x, 12, pos_z), AIR)
            put((pos_x, 9, pos_z), state('stone_brick_slab', type='bottom', waterlogged='false'))
            features['debris'].append([pos_x, 9, pos_z])
        solid((4, 9, pos_z), (4, pos_z - 3, pos_z))
    solid((3, 10, 13), (4, 10, 13))

    for pos_z in range(10, 15):
        stair_y = 15 - pos_z
        for pos_x in range(15, 20):
            if stair_y > 1:
                solid((pos_x, 1, pos_z), (pos_x, stair_y - 1, pos_z))
            if pos_x <= 17:
                put((pos_x, stair_y, pos_z), state('stone_brick_stairs', facing='north', half='bottom', shape='straight', waterlogged='false'))
                features['stairs_down'].append([pos_x, stair_y, pos_z])
            else:
                put((pos_x, stair_y, pos_z))
                for pos_y in range(stair_y + 1, stair_y + (3 if pos_z in (12, 13) else 2)):
                    put((pos_x, pos_y, pos_z), state('cracked_stone_bricks'))
                    features['debris'].append([pos_x, pos_y, pos_z])
    solid((20, 1, 10), (20, 6, 15))
    solid((14, 1, 10), (14, 5, 15))
    for pos_z in (9, 14, 15):
        put((14, 6, pos_z))
    features['rails'].extend([14, 6, pos_z] for pos_z in range(10, 14))
    features['rails'].extend([pos_x, 12, 16] for pos_x in list(range(1, 5)) + list(range(8, 22)))
    features['rails'].extend([pos_x, 6, 15] for pos_x in range(9, 14))
    features['rails'].extend([[1, 6, 9], [2, 6, 9], [21, 6, 9]])
    for position in features['rails']:
        put(position, state('dark_oak_fence', north='false', east='false', south='false', west='false', waterlogged='false'))

    solid((14, 1, 22), (15, 13, 22))
    solid((14, 12, 21), (15, 13, 21))
    features['beam_piers'] = [[pos_x, 12, pos_z] for pos_x in (14, 15) for pos_z in (21, 22)]
    solid((14, 14, 21), (22, 14, 22))
    solid((15, 15, 21), (22, 15, 22))
    for pos_z in (21, 22):
        put((14, 15, pos_z), state('stone_brick_stairs', facing='east', half='bottom', shape='straight', waterlogged='false'))
    features['beam'] = [[pos_x, pos_y, pos_z] for pos_x in range(14, 23) for pos_y in (14, 15) for pos_z in (21, 22)]

    for position in ((4, 6, 7), (8, 6, 6), (18, 6, 6), (3, 12, 18), (18, 1, 19), (19, 1, 20), (18, 1, 15), (19, 1, 15)):
        put(position, state('cobblestone_slab', type='bottom', waterlogged='false'))
        features['debris'].append(list(position))
    features['debris'] = [position for position in features['debris'] if room.get(tuple(position)).name.endswith(('_slab', 'cracked_stone_bricks'))]
    for pos_x, pos_z in ((7, 3), (8, 3), (9, 3), (7, 4), (9, 5), (13, 3), (14, 3), (15, 4), (13, 5)):
        put((pos_x, 6, pos_z), state('red_carpet'))
        features['carpet'].append([pos_x, 6, pos_z])
    for position in ((2, 6, 4), (3, 6, 4), (3, 7, 4), (2, 6, 5)):
        room.set(position, state('barrel', facing='up', open='false'), compound(id=string('minecraft:barrel'), Items=Tag(9, [], 10)))
        features['empty_barrels'].append(list(position))
    for position, facing in (((1, 9, 7), 'east'), ((21, 4, 18), 'west'), ((1, 14, 18), 'east')):
        put(position, state('wall_torch', facing=facing))
        features['torches'].append({'pos': list(position), 'facing': facing})
    for position in ((9, 6, 6), (13, 1, 18)):
        put(position, state('lantern', hanging='false', waterlogged='false'))
        features['lanterns'].append({'pos': list(position), 'hanging': False})
    for pos_x, lamp_y, pos_z in ((11, 10, 6), (17, 14, 19)):
        put((pos_x, lamp_y, pos_z), state('lantern', hanging='true', waterlogged='false'))
        features['lanterns'].append({'pos': [pos_x, lamp_y, pos_z], 'hanging': True})
        for pos_y in range(lamp_y + 1, 17):
            put((pos_x, pos_y, pos_z), state('chain', axis='y', waterlogged='false'))
            features['chains'].append([pos_x, pos_y, pos_z])
    for position in ((1, 12, 11), (21, 8, 11), (1, 4, 18), (21, 15, 20), (7, 15, 23), (21, 3, 9)):
        put(position, state('cobweb'))
        features['cobwebs'].append(list(position))
    for pos_x in range(3, 8):
        for pos_y in range(13, 16):
            put((pos_x, pos_y, 24), state('light_blue_stained_glass'))
            features['cold_window'].append([pos_x, pos_y, 24])
        put((pos_x, 16, 24), state('sea_lantern'))

    portals = [
        {'name': 'main', 'feet': [11, 6, 0], 'interior': [11, 6, 4], 'facing': 'north', 'width': 3, 'height': 3},
        {'name': 'upper', 'feet': [11, 12, 24], 'interior': [11, 12, 20], 'facing': 'south', 'width': 3, 'height': 3},
        {'name': 'lower', 'feet': [11, 1, 24], 'interior': [11, 1, 20], 'facing': 'south', 'width': 3, 'height': 3},
    ]
    for portal in portals:
        center_x, feet_y, pos_z = portal['feet']
        room.box((center_x - 1, feet_y, pos_z), (center_x + 1, feet_y + 2, pos_z), AIR)
        solid((center_x - 2, feet_y + 3, pos_z), (center_x + 2, feet_y + 3, pos_z))
    for position in features['rails']:
        connections = {}
        for direction, offset in STEPS.items():
            adjacent = room.get(tuple(value + delta for value, delta in zip(position, offset)))
            connections[direction] = adjacent.name in {'minecraft:dark_oak_fence', 'minecraft:stone_bricks', 'minecraft:mossy_stone_bricks', 'minecraft:cracked_stone_bricks'}
        put(position, state('dark_oak_fence', **connections, waterlogged='false'))

    required = set()

    def lane(start_x, end_x, feet_y, start_z, end_z):
        required.update((pos_x, feet_y, pos_z) for pos_x in range(start_x, end_x + 1) for pos_z in range(start_z, end_z + 1))

    lane(10, 12, 6, 0, 8)
    lane(5, 17, 6, 7, 8)
    lane(5, 7, 6, 9, 9)
    for pos_z in range(10, 16):
        lane(5, 7, pos_z - 3, pos_z, pos_z)
    lane(5, 7, 12, 16, 20)
    lane(5, 12, 12, 18, 20)
    lane(10, 12, 12, 18, 24)
    lane(15, 17, 6, 9, 9)
    for pos_z in range(10, 15):
        lane(15, 17, 16 - pos_z, pos_z, pos_z)
    lane(15, 17, 1, 15, 21)
    lane(10, 17, 1, 19, 21)
    lane(10, 12, 1, 19, 24)
    blueprint = {'size': SIZE, 'levels': {'lower': 1, 'main': 6, 'upper': 12}, 'portals': portals, 'features': features,
                 'required_walkable': sorted(required, key=lambda position: (position[1], position[2], position[0])),
                 'structural_policy': 'Every surviving stair has a full masonry footing; rubble rests on stairs or floor; broken beam has a wall anchor and a grounded pier. No gravity blocks.',
                 'broken_stair_policy': 'Two western columns lose their top two stair rows. The eastern three-column stair remains continuous, guarded by masonry cheeks. No jump required.',
                 'shell_policy': 'Complete base and roof; only three declared 3x3 portals interrupt walls. Blue breach is sealed glass, not a roof leak.'}
    output = ROOT / 'out'
    output.mkdir(exist_ok=True)
    room.write(output / 'room.nbt')
    metadata = {'id': 'STAIR_C_RUINED', 'name': '破损楼梯间', 'size': SIZE, 'entrance': [11, 6, 0], 'interior': [11, 6, 4], 'facing': 'north',
                'sha256': fingerprint(output / 'room.nbt'), 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH,
                'reference_dimensions': [1536, 1024], 'interfaces': portals, 'assigned_slot': None, 'deployment_status': 'independent_candidate_not_deployed',
                'coordinate_semantics': 'All portal/interior coordinates are player feet relative to NBT origin. Main entrance is at local Y=6; connecting road placement must account for this.',
                'runtime': {'minecraft': '1.20.1', 'forge': '47.4.10', 'DataVersion': 3465},
                'notes': ['Full explicit volume includes air; installer must preflight the entire volume before any placement.', 'Upper original stair is visibly broken, but a preserved three-wide side lane connects the upper exit.', 'Lower fallen column occupies two of five stair columns, leaving three columns clear.', 'Three separate exits require coordinator-confirmed multi-level roads; no FT core integration is claimed.']}
    save_json(output / 'room.json', metadata)
    save_json(output / 'blueprint.json', blueprint)
    save_json(output / 'materials.json', dict(sorted(Counter(block.name for block in room.blocks.values() if block != AIR).items())))
    empty = Structure((5, 5, 5))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / 'src/main/resources/data/ftstaircqa/structures/empty.nbt')
    print(json.dumps({'id': metadata['id'], 'size': SIZE, 'records': len(room.blocks), 'required_walkable': len(required), 'sha256': metadata['sha256']}, ensure_ascii=False))


if __name__ == '__main__':
    build()
