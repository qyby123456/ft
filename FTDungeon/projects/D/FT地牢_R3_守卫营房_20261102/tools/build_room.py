import hashlib
import json
from collections import Counter
from pathlib import Path

from nbtio import AIR, Structure, Tag, compound, integer, state, string, write_nbt


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-rooms-medium-random-thread3-20261102/out/R3-guard-barracks.png')
REFERENCE_HASH = '99a2051453f62c923eb60ee8dd9679f82346c1745dfbf7141c221b154dbc7a98'
SIZE = (17, 10, 17)


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def payload(identity, **values):
    return compound(id=string('minecraft:' + identity), **values)


def positions(start, end):
    return [[pos_x, pos_y, pos_z] for pos_y in range(start[1], end[1] + 1)
            for pos_z in range(start[2], end[2] + 1) for pos_x in range(start[0], end[0] + 1)]


def build():
    if digest(REFERENCE) != REFERENCE_HASH:
        raise ValueError('Reference fingerprint differs')
    room = Structure(SIZE)
    room.box((0, 0, 0), (16, 9, 16), AIR)
    stones = ['stone_bricks'] * 13 + ['cracked_stone_bricks'] * 3 + ['mossy_stone_bricks'] * 2
    for pos_z in range(17):
        for pos_x in range(17):
            room.set((pos_x, 0, pos_z), state(stones[(pos_x * 23 + pos_z * 17) % len(stones)]))
            room.set((pos_x, 9, pos_z), state('stone_bricks'))
            for pos_y in range(1, 9):
                if pos_x in (0, 16) or pos_z in (0, 16):
                    room.set((pos_x, pos_y, pos_z), state(stones[(pos_x * 19 + pos_z * 11 + pos_y * 7) % len(stones)]))
    for pos_x in (0, 16):
        for pos_z in (0, 7, 16):
            room.box((pos_x, 1, pos_z), (pos_x, 7, pos_z), state('stripped_dark_oak_log', axis='y'))
            room.set((pos_x, 8, pos_z), state('chiseled_stone_bricks'))
    for pos_z in (0, 16):
        room.box((1, 7, pos_z), (15, 7, pos_z), state('dark_oak_log', axis='x'))
    for pos_x in (0, 16):
        room.box((pos_x, 7, 1), (pos_x, 7, 15), state('dark_oak_log', axis='z'))
    for pos_z in (5, 9, 13):
        room.box((0, 8, pos_z), (16, 8, pos_z), state('dark_oak_log', axis='x'))
    room.box((7, 1, 0), (9, 3, 0), AIR)
    for pos_x in (6, 10):
        room.box((pos_x, 1, 0), (pos_x, 3, 0), state('polished_andesite'))
    room.box((6, 4, 0), (10, 4, 0), state('chiseled_stone_bricks'))
    bunks = []
    for side, first_x in (('west', 1), ('east', 12)):
        room.box((first_x, 3, 10), (first_x + 3, 3, 13), state('dark_oak_planks'))
        posts = []
        for pos_x in (first_x, first_x + 3):
            for pos_z in (10, 13):
                room.box((pos_x, 1, pos_z), (pos_x, 5, pos_z), state('stripped_dark_oak_log', axis='y'))
                posts.extend(positions((pos_x, 1, pos_z), (pos_x, 5, pos_z)))
        for pos_y in (1, 4):
            for pos_x in (first_x + 1, first_x + 2):
                foot, head = [pos_x, pos_y, 11], [pos_x, pos_y, 12]
                for position in (foot, head):
                    room.set((position[0], pos_y - 1, position[2]), state('dark_oak_planks'))
                room.set(tuple(foot), state('red_carpet'))
                room.set(tuple(head), state('white_carpet'))
                bunks.append({'side': side, 'foot': foot, 'head': head, 'support': [pos_x, pos_y - 1, 11], 'posts': posts})
            room.box((first_x + 1, pos_y, 13), (first_x + 2, pos_y, 13), state('dark_oak_slab', type='bottom', waterlogged=False))
        ladder_x = first_x + 2 if side == 'west' else first_x + 1
        room.box((ladder_x, 1, 10), (ladder_x, 2, 10), state('stripped_dark_oak_log', axis='y'))
        for pos_y in range(1, 4):
            room.set((ladder_x, pos_y, 9), state('ladder', facing='north', waterlogged=False))
    empty_items = Tag(9, [], 10)
    for pos_x in (2, 14):
        room.set((pos_x, 1, 8), state('chest', facing='north', type='single', waterlogged=False), payload('chest', Items=empty_items))
    room.set((14, 1, 6), state('barrel', facing='up', open=False), payload('barrel', Items=empty_items))
    racks = []
    for first_x in (2, 12):
        room.box((first_x, 1, 15), (first_x + 2, 1, 15), state('dark_oak_planks'))
        shafts, blades = [], []
        for pos_x in range(first_x, first_x + 3):
            for pos_y in (2, 3):
                room.set((pos_x, pos_y, 15), state('dark_oak_fence', north=False, south=True, east=False, west=False, waterlogged=False))
                shafts.append([pos_x, pos_y, 15])
            blade_y = 5 if pos_x == first_x + 1 else 4
            if blade_y == 5:
                room.set((pos_x, 4, 15), state('chain', axis='y', waterlogged=False))
            room.set((pos_x, blade_y, 15), state('iron_bars', north=False, south=True, east=False, west=False, waterlogged=False))
            blades.append([pos_x, blade_y, 15])
        racks.append({'base': positions((first_x, 1, 15), (first_x + 2, 1, 15)), 'shafts': shafts, 'blades': blades})
    room.box((6, 1, 15), (6, 3, 15), state('polished_andesite'))
    room.box((10, 1, 15), (10, 3, 15), state('polished_andesite'))
    room.box((6, 4, 15), (10, 4, 15), state('chiseled_stone_bricks'))
    room.box((7, 1, 15), (9, 3, 15), state('iron_bars', north=False, south=True, east=True, west=True, waterlogged=False))
    banners = []
    for position, facing in (((1, 5, 7), 'east'), ((15, 5, 7), 'west'), ((5, 5, 15), 'north'), ((11, 5, 15), 'north')):
        patterns = Tag(9, [compound(Pattern=string('cr'), Color=integer(4)), compound(Pattern=string('bo'), Color=integer(14))], 10)
        room.set(position, state('red_wall_banner', facing=facing), payload('banner', Patterns=patterns))
        banners.append(list(position))
    room.box((5, 0, 6), (11, 0, 12), state('red_terracotta'))
    for pos_x in (5, 11):
        room.box((pos_x, 0, 6), (pos_x, 0, 12), state('polished_andesite'))
    for pos_z in (6, 12):
        room.box((5, 0, pos_z), (11, 0, pos_z), state('polished_andesite'))
    room.box((6, 2, 8), (10, 2, 10), state('dark_oak_slab', type='bottom', waterlogged=False))
    for pos_x in (6, 10):
        for pos_z in (8, 10):
            room.set((pos_x, 1, pos_z), state('dark_oak_fence', east=False, west=False, north=False, south=False, waterlogged=False))
    for pos_z in (7, 11):
        room.box((6, 1, pos_z), (10, 1, pos_z), state('dark_oak_slab', type='bottom', waterlogged=False))
    room.set((8, 1, 9), state('dark_oak_planks'))
    room.set((8, 2, 9), state('lectern', facing='north', has_book=False, powered=False), payload('lectern'))
    for pos_x in range(12, 15):
        for pos_z in range(2, 5):
            room.set((pos_x, 1, pos_z), state('stone_brick_slab', type='bottom', waterlogged=False))
    room.set((13, 1, 3), state('campfire', facing='north', lit=False, signal_fire=False, waterlogged=False), payload('campfire', Items=empty_items))
    lamps = []
    for pos_x, lamp_y, pos_z in ((4, 5, 5), (12, 5, 5), (8, 4, 9), (8, 5, 13)):
        room.set((pos_x, lamp_y, pos_z), state('lantern', hanging=True, waterlogged=False))
        chains = positions((pos_x, lamp_y + 1, pos_z), (pos_x, 7, pos_z))
        for position in chains:
            room.set(tuple(position), state('chain', axis='y', waterlogged=False))
        lamps.append({'lantern': [pos_x, lamp_y, pos_z], 'chains': chains, 'anchor': [pos_x, 8, pos_z]})
    partial = {'air', 'red_bed', 'dark_oak_fence', 'iron_bars', 'ladder', 'red_wall_banner', 'chest', 'lantern', 'chain', 'campfire', 'lectern'}
    for position, block in list(room.blocks.items()):
        name = block.name.removeprefix('minecraft:')
        if name not in ('dark_oak_fence', 'iron_bars'):
            continue
        properties = dict(block.properties)
        for direction, delta in {'north': (0, 0, -1), 'south': (0, 0, 1), 'east': (1, 0, 0), 'west': (-1, 0, 0)}.items():
            neighbor = room.get(tuple(position[axis] + delta[axis] for axis in range(3))).name.removeprefix('minecraft:')
            connected = neighbor == name or neighbor not in partial and not neighbor.endswith('_slab')
            properties[direction] = str(connected).lower()
        room.set(position, state(name, **properties))
    room.write(ROOT / 'room.nbt')
    ring = sorted({(pos_x, 1, pos_z) for pos_x in (5, 11) for pos_z in range(6, 15)}
                  | {(pos_x, 1, pos_z) for pos_z in (6, 14) for pos_x in range(5, 12)})
    required = [[8, 1, 3], [3, 1, 8], [13, 1, 8], [5, 1, 11], [11, 1, 11], [3, 1, 14], [13, 1, 14], [8, 1, 14]]
    blueprint = {'size': SIZE, 'bunks': bunks, 'racks': racks, 'banners': banners, 'lamps': lamps,
                 'main_aisle': positions((7, 1, 0), (9, 3, 6)), 'circulation': ring, 'required_walkable': required,
                 'hearth': [13, 1, 3], 'table': positions((6, 2, 8), (10, 2, 10)),
                 'benches': positions((6, 1, 7), (10, 1, 7)) + positions((6, 1, 11), (10, 1, 11))}
    write_json(ROOT / 'blueprint.json', blueprint)
    write_json(ROOT / 'room.json', {'id': 'R3', 'name': '守卫营房·废弃兵营', 'size': SIZE, 'entrance': [8, 1, 0],
               'interior': [8, 1, 3], 'facing': 'north', 'sha256': digest(ROOT / 'room.nbt'), 'reference': str(REFERENCE),
               'reference_sha256': REFERENCE_HASH, 'reference_size': [1536, 1024], 'minecraft': '1.20.1', 'data_version': 3465,
               'category': 'medium_random', 'roof': 'complete', 'inventory_policy': 'empty_no_loot_tables_no_entities',
               'integration_status': 'standalone_not_deployed', 'dimensions_note': '为双层床支撑、家具环路和三格北接口调整为17×10×17；不将概念图近似尺寸当成结构实测。',
               'adaptations': ['FT维度bed_works=false且方块交互开放；为避免原版床爆炸，八组双层铺位采用木平台、红毯床褥与白毯枕头，无可交互床方块。', '兵器采用无实体栅栏/铁栏剪影，旗帜采用黄色十字图案；无可领取武器。', '议事桌保留空讲台作地图台，空箱桶无战利品；炉火熄灭以保护木构。', '北侧接入门三宽三高常开；后墙铁栏为实心背墙前的装饰，不是阻挡入口的关闭门。']})
    empty = Structure((3, 3, 3))
    empty.box((0, 0, 0), (2, 2, 2), AIR)
    empty.write(ROOT / 'src/main/resources/data/ftr3qa/structures/empty.nbt')
    materials = Counter(block.name for block in room.blocks.values() if block != AIR)
    write_json(ROOT / 'qa/materials.json', {'non_air': sum(materials.values()), 'materials': dict(materials.most_common())})
    print(json.dumps({'status': 'BUILT', 'size': SIZE, 'records': len(room.blocks), 'sha256': digest(ROOT / 'room.nbt')}))


if __name__ == '__main__':
    build()
