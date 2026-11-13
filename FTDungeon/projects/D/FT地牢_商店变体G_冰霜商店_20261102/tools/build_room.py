import hashlib
import json
from collections import Counter
from pathlib import Path

from PIL import Image

from nbtio import AIR, Structure, Tag, compound, state, string


ROOT = Path(__file__).resolve().parents[1]
DESIGN = Path('D:/ft工程文件/image2.5/dungeon-shop-variants-thread3-20261102/out/shop-variant-G-frost-shop.png')
REFERENCE_HASH = 'defa7b24b65ee89ec5db3d7575826363f295da10b0b34a397c8a606888af2df1'
SIZE = (17, 10, 15)
PILLARS = ((2, 2), (14, 2), (2, 12), (14, 12))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def empty_container(kind):
    return compound(id=string('minecraft:' + kind), Items=Tag(9, [], 10))


def build():
    reference = ROOT / 'reference/frost-shop-design.png'
    assert digest(reference) == REFERENCE_HASH
    with Image.open(reference) as image:
        image.load()
        assert image.size == (1536, 1024)
    room = Structure(SIZE)
    room.box((0, 0, 0), (16, 9, 14), AIR)
    groups = {}
    ownership = {}

    def put(position, name, group=None, entity=None, **properties):
        if group is not None:
            previous = ownership.get(position)
            if previous is not None and previous != group:
                raise ValueError(f'Furniture intersection: {position} / {previous} / {group}')
            ownership[position] = group
            groups.setdefault(group, set()).add(position)
        room.set(position, state(name, **properties), entity)

    for pos_x in range(17):
        for pos_z in range(15):
            edge = pos_x in (0, 16) or pos_z in (0, 14)
            floor = 'polished_diorite' if edge else ('light_blue_concrete', 'calcite', 'polished_diorite')[(pos_x * 7 + pos_z * 11) % 11 % 3]
            if 6 <= pos_x <= 10 and pos_z <= 8:
                floor = 'polished_diorite' if pos_x in (6, 10) or pos_z % 3 == 0 else 'light_blue_concrete'
            if (pos_x, pos_z) in ((5, 4), (11, 4), (5, 11), (11, 11)):
                floor = 'sea_lantern'
            if 11 <= pos_x <= 13 and 2 <= pos_z <= 3:
                floor = 'blue_ice'
            put((pos_x, 0, pos_z), floor)
            put((pos_x, 9, pos_z), 'quartz_bricks' if edge or (pos_x + pos_z) % 4 == 0 else 'packed_ice')
            if edge:
                for pos_y in range(1, 9):
                    wall = 'stone_bricks' if pos_y in (1, 7, 8) else ('packed_ice' if (pos_x + pos_z) % 3 else 'blue_ice')
                    put((pos_x, pos_y, pos_z), wall)

    for pos_x in range(7, 10):
        for pos_y in range(1, 4):
            room.set((pos_x, pos_y, 0), AIR)
        put((pos_x, 4, 0), 'quartz_bricks')
    for pos_x in (6, 10):
        for pos_y in range(1, 5):
            put((pos_x, pos_y, 0), 'chiseled_stone_bricks')

    for pos_x, pos_z in PILLARS:
        for pos_y in range(1, 9):
            material = 'chiseled_stone_bricks' if pos_y in (1, 8) else ('blue_ice' if pos_y in (3, 6) else 'packed_ice')
            put((pos_x, pos_y, pos_z), material, 'pillars')

    for pos_x in range(5, 12):
        put((pos_x, 1, 9), 'dark_oak_planks', 'counter')
    put((5, 2, 9), 'snow', 'counter_decor', layers=1)
    put((6, 2, 9), 'brewing_stand', 'counter_decor', entity=empty_container('brewing_stand'), has_bottle_0=False, has_bottle_1=False, has_bottle_2=False)
    put((7, 2, 9), 'blue_ice', 'counter_decor')
    put((8, 2, 9), 'amethyst_cluster', 'counter_decor', facing='up', waterlogged=False)
    put((9, 2, 9), 'light_weighted_pressure_plate', 'counter_decor', power=0)
    put((10, 2, 9), 'flower_pot', 'counter_decor')
    put((11, 2, 9), 'snow', 'counter_decor', layers=1)

    for pos_x in (5, 8, 11):
        for pos_y in range(1, 7):
            put((pos_x, pos_y, 13), 'dark_oak_log', 'shelves', axis='y')
    for pos_y in (2, 4, 6):
        for pos_x in (6, 7, 9, 10):
            put((pos_x, pos_y, 13), 'dark_oak_slab', 'shelves', type='top', waterlogged=False)
    for pos_x, pos_y, material in ((6, 3, 'bookshelf'), (7, 3, 'packed_ice'), (9, 3, 'blue_ice'), (10, 3, 'bookshelf'), (6, 5, 'blue_ice'), (9, 5, 'bookshelf')):
        put((pos_x, pos_y, 13), material, 'shelf_decor')
    for pos_x in (7, 10):
        put((pos_x, 5, 13), 'large_amethyst_bud', 'shelf_decor', facing='up', waterlogged=False)
    for pos_x in (6, 8, 10):
        put((pos_x, 7, 13), 'snow', 'shelf_decor', layers=1)
    for pos_x in (7, 8, 9):
        put((pos_x, 7, 14), 'dark_oak_planks')

    for side, first_x in (('west', 1), ('east', 13)):
        center_x = first_x + 1
        group = 'display_' + side
        for pos_x in range(first_x, first_x + 3):
            for pos_z in range(5, 8):
                put((pos_x, 1, pos_z), 'dark_oak_planks', group)
                put((pos_x, 4, pos_z), 'glass', group)
                put((pos_x, 5, pos_z), 'white_carpet', group)
                if pos_x in (first_x, first_x + 2) or pos_z in (5, 7):
                    for pos_y in (2, 3):
                        put((pos_x, pos_y, pos_z), 'light_blue_stained_glass', group)
        put((center_x, 2, 6), 'sea_lantern', group)
        put((center_x, 3, 6), 'amethyst_cluster', group, facing='up', waterlogged=False)

    for pos_z in (4, 9):
        put((1, 5, pos_z), 'dark_oak_slab', 'wall_goods', type='top', waterlogged=False)
        put((1, 6, pos_z), 'small_amethyst_bud', 'wall_goods', facing='up', waterlogged=False)
    put((1, 1, 9), 'barrel', 'barrel', entity=empty_container('barrel'), facing='up', open=False)
    put((1, 2, 9), 'blue_ice', 'barrel')
    for position in ((15, 1, 9), (15, 1, 10), (15, 2, 10)):
        put(position, 'blue_ice', 'ice_stock')
    for pos_y in (3, 4):
        put((15, pos_y, 9), 'iron_bars', 'frost_axe_relief', north=False, east=True, south=False, west=False, waterlogged=False)
    for pos_z in (8, 9, 10):
        put((15, 5, pos_z), 'blue_ice', 'frost_axe_relief')
        put((16, 5, pos_z), 'dark_oak_planks')

    lamp_supports = []
    for pos_x in (6, 10):
        for pos_y in (6, 7, 8):
            put((pos_x, pos_y, 9), 'chain', 'lamps', axis='y', waterlogged=False)
        put((pos_x, 5, 9), 'soul_lantern', 'lamps', hanging=True, waterlogged=False)
        lamp_supports.append({'lantern': [pos_x, 5, 9], 'chains': [[pos_x, pos_y, 9] for pos_y in (6, 7, 8)], 'ceiling': [pos_x, 9, 9]})

    icicles = []
    for pos_x, pos_z in ((5, 1), (8, 1), (11, 1), (4, 13), (12, 13), (1, 10), (15, 4)):
        put((pos_x, 8, pos_z), 'blue_ice', 'icicles')
        put((pos_x, 7, pos_z), 'light_blue_stained_glass_pane', 'icicles', north=pos_z == 1, east=pos_x == 15, south=pos_z == 13, west=pos_x == 1, waterlogged=False)
        icicles.append({'anchor': [pos_x, 9, pos_z], 'cap': [pos_x, 8, pos_z], 'tip': [pos_x, 7, pos_z]})
    for pos_x, pos_z in ((1, 4), (15, 4), (4, 13), (12, 13)):
        put((pos_x, 1, pos_z), 'snow', 'snowdrifts', layers=2)

    counter_ring = {(pos_x, 1, pos_z) for pos_x in range(4, 13) for pos_z in (8, 10)} | {(4, 1, 9), (12, 1, 9)}
    counter_clearance = sorted((pos_x, pos_y, pos_z) for pos_x, _, pos_z in counter_ring for pos_y in (1, 2, 3))
    pillar_ring = {(pos_x + offset_x, pos_y, pos_z + offset_z) for pos_x, pos_z in PILLARS for offset_x in (-1, 0, 1) for offset_z in (-1, 0, 1) if (offset_x, offset_z) != (0, 0) for pos_y in range(1, 9)}
    required = counter_ring | {(pos_x + offset_x, 1, pos_z + offset_z) for pos_x, pos_z in PILLARS for offset_x in (-1, 0, 1) for offset_z in (-1, 0, 1) if (offset_x, offset_z) != (0, 0)} | {(8, 1, 3), (8, 1, 12), (2, 1, 4), (14, 1, 4), (2, 1, 8), (14, 1, 8)}
    for pos_x, _, pos_z in required:
        put((pos_x, 0, pos_z), 'polished_diorite')
    blueprint = {'size': list(SIZE), 'features': {name: [list(position) for position in sorted(positions)] for name, positions in sorted(groups.items())}, 'pillar_footprints': [list(position) for position in PILLARS], 'pillar_clearance': [list(position) for position in sorted(pillar_ring)], 'counter_clearance': [list(position) for position in counter_clearance], 'entrance_to_counter_aisle': [[pos_x, pos_y, pos_z] for pos_x in range(7, 10) for pos_y in (1, 2, 3) for pos_z in range(9)], 'required_walkable': [list(position) for position in sorted(required)], 'lamp_supports': lamp_supports, 'icicle_supports': icicles, 'no_furniture_overlaps': True}
    target = ROOT / 'out/room.nbt'
    room.write(target)
    metadata = {
        'id': 'SHOP_G_FROST', 'variant': 'G', 'name': '冰霜商店', 'size': list(SIZE), 'entrance': [8, 1, 0], 'interior': [8, 1, 3], 'facing': 'north', 'sha256': digest(target), 'reference': str(DESIGN), 'reference_sha256': REFERENCE_HASH,
        'minecraft_version': '1.20.1', 'data_version': 3465,
        'integration': {'status': 'STANDALONE_CANDIDATE_NOT_REGISTERED', 'id_is_provisional': True, 'assigned_slot': None, 'deployed': False, 'requires_coordinator_acceptance': True},
        'layout': {'counter': {'from': [5, 1, 9], 'to': [11, 1, 9]}, 'independent_pillars': [list(position) for position in PILLARS], 'pillar_full_height_air_ring': 1, 'counter_circulation_width': 1, 'north_opening': [3, 3], 'dry_main_aisle': True},
        'theme': ['冰砖以原版石砖/石英砖和蓝冰/浮冰组合表达', '四根独立到顶冰柱', '霜覆深橡木柜台与空药水架', '两侧落地冰蓝玻璃展柜', '冷光灯由完整天花板三节链条悬挂', '后墙双层陈列架、蓝冰库存与方块化冰斧浮雕'],
        'safety': {'loot_tables': 0, 'reward_items': 0, 'preplaced_entities': 0, 'fluid_blocks': 0, 'fire': 0},
        'design_adaptations': ['概念图约11×9扩展为17×15以保证四根独立柱的全高八邻域净空、真实展柜和安全通路。', '原版无冰砖/冻结药瓶/冻鱼/天平/无实体武器架方块，以石英砖、空酿造台、蓝冰、紫水晶、金质测重板和蓝冰斧形浮雕表达；不放入真实商品或奖励。', '不使用会融化的普通冰、真实水或粉雪。玻璃细柱在蓝冰帽与完整屋顶下表达冰锥，白地毯只作稳定霜顶。'],
    }
    write_json(ROOT / 'out/room.json', metadata)
    write_json(ROOT / 'out/blueprint.json', blueprint)
    write_json(ROOT / 'out/materials.json', dict(sorted(Counter(block.name for block in room.blocks.values() if block != AIR).items())))
    write_json(ROOT / 'reference/fingerprint.json', {'source': str(DESIGN), 'copied_reference': str(reference), 'sha256': REFERENCE_HASH, 'dimensions': [1536, 1024], 'paid_image_requests': 0})
    return room, metadata, blueprint


if __name__ == '__main__':
    structure, metadata, _ = build()
    print(json.dumps({'status': 'BUILT', 'id': metadata['id'], 'size': SIZE, 'records': len(structure.blocks), 'sha256': metadata['sha256']}, ensure_ascii=False))
