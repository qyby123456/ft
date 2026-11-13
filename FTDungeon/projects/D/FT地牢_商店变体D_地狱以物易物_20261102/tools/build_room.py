import hashlib
import json
from collections import Counter
from pathlib import Path

from nbtio import AIR, Structure, Tag, compound, state, string


ROOT = Path(__file__).resolve().parents[1]
SIZE = (13, 9, 13)
IDENTITY = 'SHOP_D_NETHER_BARTER'
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-shop-variants-20261102/out/shop-variant-D-nether-barter.png')
REFERENCE_HASH = 'adc38f2ab9553f7c40975172b105a362ae8b32655d02ecbf73a1aa6e850c8821'


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def build():
    if hashlib.sha256(REFERENCE.read_bytes()).hexdigest() != REFERENCE_HASH:
        raise ValueError('Reference image fingerprint changed')
    room = Structure(SIZE)
    room.box((0, 0, 0), (12, 8, 12), AIR)
    room.box((0, 0, 0), (12, 0, 12), state('polished_blackstone_bricks'))
    room.box((0, 8, 0), (12, 8, 12), state('polished_blackstone_bricks'))
    for height in range(1, 8):
        for horizontal in range(13):
            for position in ((0, height, horizontal), (12, height, horizontal), (horizontal, height, 0), (horizontal, height, 12)):
                material = 'gilded_blackstone' if height in (2, 6) and horizontal % 3 == 0 else 'nether_bricks' if height in (3, 4) else 'polished_blackstone_bricks'
                room.set(position, state(material))
    pillars = []
    for pos_x in (0, 12):
        for pos_z in (0, 12):
            for height in range(1, 8):
                position = (pos_x, height, pos_z)
                room.set(position, state('gold_block' if height in (1, 6) else 'polished_blackstone'))
                pillars.append(position)
    room.box((5, 1, 0), (7, 3, 0), AIR)
    for pos_x in range(5, 8):
        for pos_z in range(1, 6):
            room.set((pos_x, 0, pos_z), state('red_glazed_terracotta', facing='north'))
    for distance in range(2, 11):
        for position in ((2, 0, distance), (10, 0, distance), (distance, 0, 2), (distance, 0, 10)):
            room.set(position, state('shroomlight'))
    counter = []
    for pos_x in range(4, 9):
        position = (pos_x, 1, 7)
        room.set(position, state('gold_block' if pos_x in (4, 5, 8) else 'polished_blackstone_bricks'))
        counter.append(position)
    room.set((4, 2, 7), state('gold_block'))
    room.set((5, 2, 7), state('anvil', facing='north'))
    room.set((6, 2, 7), state('brewing_stand', has_bottle_0=False, has_bottle_1=False, has_bottle_2=False),
             compound(id=string('minecraft:brewing_stand'), Items=Tag(9, [], 10), Fuel=Tag(1, 0), BrewTime=Tag(2, 0)))
    room.set((7, 2, 7), state('flower_pot'))
    room.set((8, 2, 7), state('skeleton_skull', rotation=0), compound(id=string('minecraft:skull')))
    shelves = []
    for height in (1, 3):
        for pos_x in range(3, 8):
            position = (pos_x, height, 11)
            room.set(position, state('polished_blackstone_bricks'))
            shelves.append(position)
    for position, material in (((3, 2, 11), 'potted_crimson_fungus'), ((4, 2, 11), 'gold_block'), ((5, 2, 11), 'flower_pot'),
                               ((6, 2, 11), 'potted_crimson_fungus'), ((7, 2, 11), 'flower_pot'), ((3, 4, 11), 'flower_pot'),
                               ((4, 4, 11), 'gold_block'), ((5, 4, 11), 'potted_crimson_fungus'), ((6, 4, 11), 'flower_pot')):
        room.set(position, state(material))
    room.set((7, 4, 11), state('skeleton_skull', rotation=0), compound(id=string('minecraft:skull')))
    room.box((4, 6, 11), (8, 6, 11), state('gold_block'))
    room.box((5, 6, 11), (7, 6, 11), state('polished_blackstone'))
    barrels = [(1, 1, 3), (11, 1, 3), (1, 1, 9)]
    for position in barrels:
        room.set(position, state('barrel', facing='up', open=False), compound(id=string('minecraft:barrel'), Items=Tag(9, [], 10)))
        room.set((position[0], 2, position[2]), state('potted_crimson_fungus' if position[2] == 3 else 'red_candle', **({} if position[2] == 3 else {'candles': 3, 'lit': False, 'waterlogged': False})))
    supports = []
    lamp_supports = []
    for pos_x in (4, 8):
        beam, chain, lantern = (pos_x, 7, 4), (pos_x, 6, 4), (pos_x, 5, 4)
        room.set(beam, state('polished_blackstone_bricks'))
        room.set(chain, state('chain', axis='y', waterlogged=False))
        room.set(lantern, state('lantern', hanging=True, waterlogged=False))
        lamp_supports.append({'beam': beam, 'chain': chain, 'lantern': lantern})
        supports.extend([{'position': lantern, 'support': chain}, {'position': chain, 'support': beam}, {'position': beam, 'support': (pos_x, 8, 4)}])
    for pos_x, wall in ((1, 0), (11, 12)):
        room.set((pos_x, 3, 4), state('polished_blackstone_bricks'))
        room.set((pos_x, 4, 4), state('soul_lantern', hanging=False, waterlogged=False))
        supports.extend([{'position': (pos_x, 3, 4), 'support': (wall, 3, 4)}, {'position': (pos_x, 4, 4), 'support': (pos_x, 3, 4)}])
    hazards = [(1, 1, 5), (11, 1, 5)]
    for pos_x, height, pos_z in hazards:
        room.set((pos_x, height, pos_z), state('magma_block'))
        for offset_x, offset_y, offset_z in ((0, 1, 0), (-1, 0, 0), (1, 0, 0), (0, 0, -1), (0, 0, 1)):
            position = (pos_x + offset_x, height + offset_y, pos_z + offset_z)
            if position[0] in (0, 12):
                continue
            room.set(position, state('orange_stained_glass'))
    cage = []
    for pos_x in range(9, 12):
        for pos_z in range(9, 12):
            room.set((pos_x, 1, pos_z), state('nether_bricks'))
            room.set((pos_x, 4, pos_z), state('nether_bricks'))
            for height in (2, 3):
                if pos_x not in (9, 11) and pos_z not in (9, 11):
                    continue
                position = (pos_x, height, pos_z)
                room.set(position, state('iron_bars', north=False, south=False, east=False, west=False, waterlogged=False))
                cage.append(position)
    room.set((10, 2, 10), state('gold_block'))
    for position in cage:
        properties = {'waterlogged': False}
        for direction, offset_x, offset_z in (('north', 0, -1), ('south', 0, 1), ('west', -1, 0), ('east', 1, 0)):
            neighbor = room.get((position[0] + offset_x, position[1], position[2] + offset_z))
            properties[direction] = neighbor.name == 'minecraft:iron_bars' or neighbor.name in {'minecraft:nether_bricks', 'minecraft:polished_blackstone_bricks', 'minecraft:gilded_blackstone', 'minecraft:gold_block'}
        room.set(position, state('iron_bars', **properties))
    for height in range(5, 8):
        position = (10, height, 10)
        room.set(position, state('chain', axis='y', waterlogged=False))
        supports.append({'position': position, 'support': (10, height + 1, 10)})
    ring = [(pos_x, height, pos_z) for height in (1, 2) for pos_x in range(3, 10) for pos_z in range(6, 9)
            if pos_x in (3, 9) or pos_z in (6, 8)]
    aisle = [(pos_x, height, pos_z) for height in range(1, 4) for pos_x in range(5, 8) for pos_z in range(0, 6)]
    required = [position for position in ring if position[1] == 1] + [(6, 1, 3), (4, 1, 10), (1, 1, 2), (11, 1, 2), (1, 1, 8), (8, 1, 10)]
    for position, block in room.blocks.items():
        if block.name in {'minecraft:flower_pot', 'minecraft:potted_crimson_fungus', 'minecraft:skeleton_skull', 'minecraft:red_candle', 'minecraft:brewing_stand', 'minecraft:anvil'}:
            supports.append({'position': position, 'support': (position[0], position[1] - 1, position[2])})
    for position in shelves:
        supports.append({'position': position, 'support': (position[0], position[1], 12)})
    blueprint = {'size': SIZE, 'features': {'pillars': pillars, 'counter': counter, 'shelves': shelves, 'barrels': barrels, 'cage': cage},
                 'counter_clearance': ring, 'entrance_to_counter_aisle': aisle, 'required_walkable': required,
                 'lamp_supports': lamp_supports, 'supports': supports, 'encased_magma': hazards,
                 'negative_probes': {'lamp': (4, 5, 4), 'chain': (4, 6, 4), 'aisle': (6, 1, 2), 'magma': (1, 1, 5), 'magma_cover': (1, 2, 5)}}
    output = ROOT / 'out'
    output.mkdir(exist_ok=True)
    room.write(output / 'room.nbt')
    fingerprint = hashlib.sha256((output / 'room.nbt').read_bytes()).hexdigest()
    metadata = {'id': IDENTITY, 'name': '地狱以物易物商店 · 变体 D', 'size': SIZE, 'entrance': (6, 1, 0), 'interior': (6, 1, 3), 'facing': 'north',
                'sha256': fingerprint, 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH, 'minecraft_version': '1.20.1', 'data_version': 3465,
                'integration': {'status': 'STANDALONE_CANDIDATE_NOT_REGISTERED', 'id_is_provisional': True, 'assigned_slot': None, 'deployed': False,
                                'coordinator_thread': '01a19758-b0e5-75c2-822a-4860527a1e8b', 'requires_coordinator_acceptance': True},
                'layout': {'roof': 'complete', 'pillars': 'embedded in wall corners', 'main_aisle_width_blocks': 3, 'main_aisle_height_blocks': 3, 'minimum_counter_clearance_blocks': 1},
                'substitutions': ['内部扩为11×11以保留柜台四侧净空、后墙货架与隔离笼。',
                                  '四根金带黑石柱嵌入墙角，修正概念图独立立柱与入口空间冲突。',
                                  '魂火采用原版灵魂灯笼，不使用可伤人裸魂火。',
                                  '流动熔岩池改为六面封闭的岩浆块展示箱；地面发光缝改为完整菌光体地板。',
                                  '小金锭、天平、药瓶艺术比例改为金块、受支撑铁砧、空酿造台与花盆，不投放物品实体。',
                                  '铁笼为空装饰；红纹为内嵌陶瓦；木桶为空且与裸火、熔岩完全隔离。'],
                'gameplay': {'merchant_entities': 0, 'trade_logic': False, 'reward_logic': False, 'loot_tables': 0, 'inventory_items': 0}}
    write_json(output / 'room.json', metadata)
    write_json(output / 'blueprint.json', blueprint)
    write_json(output / 'materials.json', dict(sorted(Counter(block.name for block in room.blocks.values()).items())))
    print(json.dumps({'status': 'BUILT', 'id': IDENTITY, 'size': SIZE, 'records': len(room.blocks), 'sha256': fingerprint}, ensure_ascii=False))


if __name__ == '__main__':
    build()
