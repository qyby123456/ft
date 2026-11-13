import hashlib
import json
from collections import Counter
from pathlib import Path

from PIL import Image

from nbtio import AIR, Structure, Tag, compound, state, string


ROOT = Path(__file__).resolve().parents[1]
DESIGN = Path('D:/ft工程文件/image2.5/dungeon-shop-variants-thread3-20261102')
REFERENCE = 'shop-variant-H-swamp-witch-apothecary.png'
REFERENCE_HASH = '0c7a6641ef53e737e675dbedbcaf2f3c88f4afc475eabf5de38fb3ba9718fc20'
SIZE = (15, 10, 15)
PILLARS = ((3, 4), (11, 4), (3, 10), (11, 10))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def inventory_entity(kind):
    values = {'id': string('minecraft:' + kind), 'Items': Tag(9, [], 10)}
    if kind == 'brewing_stand':
        values.update(Fuel=Tag(1, 0), BrewTime=Tag(2, 0))
    return compound(**values)


def build():
    room = Structure(SIZE)
    room.box((0, 0, 0), (14, 9, 14), AIR)
    groups = {}
    occupied = {}

    def put(position, name, group, entity=None, **properties):
        previous = occupied.get(position)
        if previous and previous != group:
            raise ValueError(f'Feature overlap {position}: {previous} / {group}')
        occupied[position] = group
        groups.setdefault(group, set()).add(position)
        room.set(position, state(name, **properties), entity)

    for pos_x in range(15):
        for pos_z in range(15):
            floor = ('mossy_stone_bricks', 'stone_bricks', 'mossy_cobblestone', 'mud_bricks')[(pos_x * 13 + pos_z * 7) % 4]
            if pos_x in (0, 14) or pos_z in (0, 14):
                floor = 'polished_deepslate'
            put((pos_x, 0, pos_z), floor, 'floor')
            put((pos_x, 9, pos_z), 'dark_oak_planks', 'roof')
    for pos_y in range(1, 9):
        for pos_x in range(15):
            for pos_z in range(15):
                if pos_x not in (0, 14) and pos_z not in (0, 14):
                    continue
                if pos_z == 0 and 6 <= pos_x <= 8 and pos_y <= 3:
                    continue
                wall = 'mossy_stone_bricks' if pos_y <= 2 or (pos_x + pos_z + pos_y) % 4 == 0 else 'mud_bricks'
                if pos_y == 8:
                    wall = 'dark_oak_log'
                put((pos_x, pos_y, pos_z), wall, 'walls', **({'axis': 'x' if pos_z in (0, 14) else 'z'} if wall == 'dark_oak_log' else {}))
    for pos_x, pos_z in PILLARS:
        for pos_y in range(1, 9):
            put((pos_x, pos_y, pos_z), 'mossy_stone_bricks' if pos_y % 3 else 'mossy_cobblestone', 'pillars')
    for pos_z in (4, 10):
        for pos_x in range(1, 14):
            if (pos_x, pos_z) not in PILLARS:
                put((pos_x, 8, pos_z), 'dark_oak_log', 'beams', axis='x')
    for pos_x in range(5, 10):
        for pos_y in (1, 2):
            put((pos_x, pos_y, 8), 'dark_oak_planks' if pos_y == 1 else 'spruce_planks', 'counter')
    put((5, 3, 8), 'potted_red_mushroom', 'counter_decor')
    put((6, 3, 8), 'brewing_stand', 'counter_decor', entity=inventory_entity('brewing_stand'), has_bottle_0=False, has_bottle_1=False, has_bottle_2=False)
    put((7, 3, 8), 'cauldron', 'counter_decor')
    put((8, 3, 8), 'potted_fern', 'counter_decor')
    put((9, 3, 8), 'lime_stained_glass', 'counter_decor')

    for pos_x in (1, 13):
        for pos_z in range(6, 9):
            put((pos_x, 1, pos_z), 'bookshelf' if pos_z != 7 else 'dark_oak_planks', 'shelves')
            put((pos_x, 3, pos_z), 'dark_oak_slab', 'shelves', type='top', waterlogged=False)
        put((pos_x, 2, 6), 'potted_brown_mushroom', 'specimens')
        put((pos_x, 2, 7), 'green_stained_glass', 'specimens')
        put((pos_x, 2, 8), 'potted_fern', 'specimens')
        put((pos_x, 4, 6), 'potted_red_mushroom', 'specimens')
        put((pos_x, 4, 7), 'lime_stained_glass', 'specimens')
        put((pos_x, 4, 8), 'brewing_stand', 'specimens', entity=inventory_entity('brewing_stand'), has_bottle_0=False, has_bottle_1=False, has_bottle_2=False)
    for pos_x in range(5, 10):
        put((pos_x, 1, 13), 'barrel' if pos_x in (5, 9) else 'bookshelf', 'back_shelves', entity=inventory_entity('barrel') if pos_x in (5, 9) else None, **({'facing': 'north', 'open': False} if pos_x in (5, 9) else {}))
        put((pos_x, 3, 13), 'dark_oak_slab', 'back_shelves', type='top', waterlogged=False)
        put((pos_x, 5, 13), 'dark_oak_log', 'back_shelves', axis='x')
        put((pos_x, 2, 13), 'potted_fern' if pos_x % 2 else 'green_stained_glass', 'specimens')
        put((pos_x, 4, 13), 'potted_brown_mushroom' if pos_x % 2 else 'lime_stained_glass', 'specimens')
    put((7, 1, 12), 'lectern', 'recipe_stand', entity=compound(id=string('minecraft:lectern')), facing='north', has_book=False, powered=False)

    for pos_x in (6, 8):
        for pos_y in (7, 8):
            put((pos_x, pos_y, 6), 'chain', 'lamp_chains', axis='y', waterlogged=False)
        put((pos_x, 6, 6), 'lantern', 'lamps', hanging=True, waterlogged=False)
    cages = []
    for center_x in (2, 12):
        for pos_x in range(center_x - 1, center_x + 2):
            for pos_z in range(6, 9):
                put((pos_x, 5, pos_z), 'dark_oak_slab', 'cages', type='top', waterlogged=False)
                put((pos_x, 7, pos_z), 'dark_oak_planks', 'cages')
                if (pos_x, pos_z) == (center_x, 7):
                    put((pos_x, 6, pos_z), 'shroomlight', 'cages')
                else:
                    put((pos_x, 6, pos_z), 'iron_bars', 'cages', waterlogged=False, north=pos_z > 6, south=pos_z < 8, west=pos_x > center_x - 1 or pos_x == 1, east=pos_x < center_x + 1 or pos_x == 13)
        put((center_x, 8, 7), 'chain', 'cage_chains', axis='y', waterlogged=False)
        cages.append({'center': [center_x, 6, 7], 'chain': [center_x, 8, 7], 'ceiling': [center_x, 9, 7], 'base_y': 5, 'lid_y': 7})

    for pos_x in (1, 2, 3):
        for pos_z in (1, 2):
            put((pos_x, 1, pos_z), 'verdant_froglight' if (pos_x, pos_z) == (2, 1) else 'polished_blackstone_bricks', 'cauldron_plinth', **({'axis': 'y'} if (pos_x, pos_z) == (2, 1) else {}))
    put((2, 2, 1), 'water_cauldron', 'cauldron', level=3)
    for pos_x, pos_z in ((1, 1), (3, 1), (1, 2), (2, 2), (3, 2)):
        put((pos_x, 2, pos_z), 'polished_blackstone_brick_slab', 'cauldron_rim', type='bottom', waterlogged=False)
    put((12, 1, 1), 'water', 'basin_water', level=0)
    put((12, 2, 1), 'lily_pad', 'basin_plant')
    put((11, 1, 1), 'mossy_cobblestone', 'basin_rim')
    put((12, 1, 2), 'mossy_cobblestone', 'basin_rim')
    put((13, 1, 1), 'grass_block', 'basin_rim', snowy=False)
    put((13, 2, 1), 'sugar_cane', 'basin_plant', age=0)

    for pos_x, pos_z in ((1, 12), (13, 12)):
        put((pos_x, 1, pos_z), 'barrel', 'barrels', entity=inventory_entity('barrel'), facing='up', open=False)
        put((pos_x, 2, pos_z), 'potted_red_mushroom' if pos_x == 1 else 'potted_brown_mushroom', 'specimens')
    for pos_x, pos_z, face in ((1, 3, 'west'), (13, 3, 'east'), (1, 10, 'west'), (13, 10, 'east'), (4, 13, 'south'), (10, 13, 'south')):
        for pos_y in range(2, 8):
            put((pos_x, pos_y, pos_z), 'vine', 'vines', up=False, north=False, south=face == 'south', east=face == 'east', west=face == 'west')
    for pos_x in (5, 9):
        put((pos_x, 8, 12), 'rooted_dirt', 'herb_mounts')
        put((pos_x, 7, 12), 'hanging_roots', 'herbs', waterlogged=False)
    messages = ['沼泽女巫药铺', 'SWAMP WITCH', '标本陈列 无库存', '配方不含可取物']
    text = compound(messages=Tag(9, [string(json.dumps({'text': message}, ensure_ascii=False)) for message in messages], 8), color=string('dark_green'), has_glowing_text=Tag(1, 0))
    back = compound(messages=Tag(9, [string('{"text":""}') for _ in range(4)], 8), color=string('black'), has_glowing_text=Tag(1, 0))
    put((7, 6, 13), 'dark_oak_wall_sign', 'sign', entity=compound(id=string('minecraft:sign'), front_text=text, back_text=back, is_waxed=Tag(1, 1)), facing='north', waterlogged=False)
    put((13, 1, 11), 'dark_oak_fence', 'broom', waterlogged=False, north=True, south=True, east=True, west=False)
    put((13, 1, 10), 'hay_block', 'broom', axis='y')

    ring = [(pos_x, pos_y, pos_z) for pos_x in range(4, 11) for pos_z in range(7, 10) if pos_x in (4, 10) or pos_z in (7, 9) for pos_y in (1, 2, 3)]
    pillar_ring = [(pos_x + offset_x, pos_y, pos_z + offset_z) for pos_x, pos_z in PILLARS for offset_x in (-1, 0, 1) for offset_z in (-1, 0, 1) if offset_x or offset_z for pos_y in range(1, 8)]
    blueprint = {
        'size': list(SIZE),
        'features': {name: [list(position) for position in sorted(positions)] for name, positions in groups.items()},
        'counter_clearance': ring, 'pillar_clearance': pillar_ring,
        'entrance_to_counter_aisle': [(pos_x, pos_y, pos_z) for pos_x in range(6, 9) for pos_y in range(1, 4) for pos_z in range(7)],
        'required_walkable': [[7, 1, 3], [7, 1, 7], [4, 1, 8], [10, 1, 8], [7, 1, 9], [7, 1, 11], [4, 1, 12], [10, 1, 12]],
        'lamp_supports': [{'lantern': [pos_x, 6, 6], 'chain': [pos_x, 7, 6], 'upper_chain': [pos_x, 8, 6], 'beam': [pos_x, 9, 6]} for pos_x in (6, 8)],
        'cage_supports': cages, 'contained_water': [[12, 1, 1]],
        'contained_water_boundaries': [[12, 0, 1], [11, 1, 1], [13, 1, 1], [12, 1, 0], [12, 1, 2]],
    }
    return room, blueprint


def main():
    reference = ROOT / 'reference' / REFERENCE
    if digest(reference) != REFERENCE_HASH:
        raise ValueError('Reference SHA-256 mismatch')
    with Image.open(reference) as image:
        image.load()
        dimensions = list(image.size)
    room, blueprint = build()
    target = ROOT / 'out/room.nbt'
    room.write(target)
    write_json(ROOT / 'out/blueprint.json', blueprint)
    write_json(ROOT / 'out/materials.json', dict(sorted(Counter(block.name for block in room.blocks.values() if block != AIR).items())))
    write_json(ROOT / 'reference/input-fingerprints.json', {'source': str(DESIGN / 'out' / REFERENCE), 'sha256': REFERENCE_HASH, 'dimensions': dimensions, 'source_readme': str(DESIGN / 'README.md'), 'source_manifest': str(DESIGN / 'manifest.json'), 'source_prompt': str(DESIGN / 'prompts/H-swamp-witch-apothecary.txt'), 'source_qa': str(DESIGN / 'qa/verify.json')})
    write_json(ROOT / 'out/room.json', {
        'id': 'SHOP_H_SWAMP_WITCH', 'name': '沼泽女巫药铺', 'variant': 'H',
        'size': list(SIZE), 'entrance': [7, 1, 0], 'interior': [7, 1, 3], 'facing': 'north',
        'sha256': digest(target), 'reference': str(DESIGN / 'out' / REFERENCE), 'reference_sha256': REFERENCE_HASH,
        'minecraft_version': '1.20.1', 'data_version': 3465,
        'integration': {'status': 'STANDALONE_NOT_DEPLOYED', 'id_is_provisional': True, 'assigned_slot': None, 'coordinator_thread': '01a19758-b0e5-75c2-822a-4860527a1e8b'},
        'layout': {'counter': {'x': [5, 9], 'y': [1, 2], 'z': 8}, 'pillar_centers_xz': PILLARS, 'minimum_counter_clearance': 1, 'main_aisle': [3, 3], 'roof': 'complete'},
        'safety': {'inventory_items': 0, 'entities': 0, 'loot_tables': 0, 'trading_logic_added': False, 'reward_logic_added': False, 'world_liquid_source_cells': 1, 'exposed_fire': 0},
        'substitutions': ['扩大为15×10×15，以落实四根独立落地柱、柱旁一格净空、柜台环通和靠墙陈设。', '标本瓶使用绿色玻璃与盆栽，空酿造台无药水无燃料；不放可领取药水、书、金币或物品实体。', '药锅使用装满水的炼药锅，底座青翠蛙明灯代替裸火与绿色药液；不伪造原版不存在的绿色流体。', '双吊笼采用顶板/底板/铁栏围合，内置菌光体固定方块，通过铁链接入完整屋顶；无关押实体。', '一格封闭水盆保留睡莲及有水邻接的甘蔗。', '扫帚用靠墙栅栏与干草块意象表达；天平和散币不以可掉落物堆砌。', '灯笼使用原版琥珀光，绿色只由蛙明灯、玻璃及苔色表达；不宣称原版彩色动态光或雾粒子。'],
    })
    empty = Structure((1, 1, 1))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / 'src/main/resources/data/ftwitchqa/structures/empty.nbt')
    print(json.dumps({'status': 'BUILT', 'size': SIZE, 'records': len(room.blocks), 'sha256': digest(target)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
