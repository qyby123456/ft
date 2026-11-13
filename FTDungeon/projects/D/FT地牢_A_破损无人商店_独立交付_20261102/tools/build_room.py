import hashlib
import json
from collections import Counter
from pathlib import Path

from PIL import Image

from nbtio import AIR, Structure, Tag, compound, state, string


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-shop-variants-20261102/out/shop-variant-A-broken-unmanned.png')
REFERENCE_HASH = 'a5ec41abe1f7a491b3742e7221ea0c86530d681f0a6b83689c0c45c2ded7fe30'
SIZE = (15, 9, 15)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def empty_inventory(kind):
    return compound(id=string('minecraft:' + kind), Items=Tag(9, [], 10))


def main():
    reference_copy = ROOT / 'reference' / REFERENCE.name
    if digest(reference_copy) != REFERENCE_HASH or digest(REFERENCE) != REFERENCE_HASH:
        raise ValueError('Variant A source/reference fingerprint mismatch')
    with Image.open(reference_copy) as image:
        dimensions = list(image.size)
    write_json(ROOT / 'reference/input-fingerprints.json', {
        'variant': 'A', 'path': str(REFERENCE), 'sha256': REFERENCE_HASH,
        'copy': 'reference/' + REFERENCE.name, 'size': dimensions,
        'styles_md_present_at_source': (REFERENCE.parents[1] / 'STYLES.md').exists(),
        'note': 'STYLES.md is referenced by the source README but absent on inspection; README/manifest/DISPATCH and the verified image were used. No source files modified.',
    })
    room = Structure(SIZE)
    room.box((0, 0, 0), (14, 8, 14), AIR)
    groups = {}
    occupied = {}

    def put(position, name, group=None, entity=None, **properties):
        if group:
            if position in occupied and occupied[position] != group:
                raise ValueError(f'Feature overlap: {position}: {occupied[position]} / {group}')
            occupied[position] = group
            groups.setdefault(group, set()).add(position)
        room.set(position, state(name, **properties), entity)

    for pos_z in range(15):
        for pos_x in range(15):
            pattern = (pos_x * 17 + pos_z * 11 + pos_x * pos_z) % 17
            floor = 'stone_bricks'
            if pattern in (0, 3, 6, 9):
                floor = 'cracked_stone_bricks'
            elif pattern in (1, 7, 13):
                floor = 'mossy_stone_bricks'
            elif pattern in (5, 12):
                floor = 'andesite'
            if pos_x in (0, 14) or pos_z in (0, 14):
                floor = 'stone_bricks'
            put((pos_x, 0, pos_z), floor)
            put((pos_x, 8, pos_z), 'deepslate_tiles')
            if pos_x in (0, 14) or pos_z in (0, 14):
                for pos_y in range(1, 8):
                    selector = (pos_x * 5 + pos_z * 7 + pos_y * 3) % 10
                    wall = 'mossy_stone_bricks' if selector < 2 else 'cracked_stone_bricks' if selector < 5 else 'stone_bricks'
                    put((pos_x, pos_y, pos_z), wall)

    for pos_x, pos_z in ((0, 0), (14, 0), (0, 14), (14, 14)):
        for pos_y in range(1, 9):
            put((pos_x, pos_y, pos_z), 'chiseled_stone_bricks' if pos_y in (1, 7, 8) else 'stone_bricks', 'pillars')
    for pos_x in range(6, 9):
        for pos_y in range(1, 4):
            room.set((pos_x, pos_y, 0), AIR)
        put((pos_x, 4, 0), 'stone_bricks')
    for pos_x in (5, 9):
        for pos_y in range(1, 4):
            put((pos_x, pos_y, 0), 'chiseled_stone_bricks')

    for pos_x in range(15):
        put((pos_x, 7, 4), 'dark_oak_log', 'ceiling_beams', axis='x')
        if pos_x <= 5 or pos_x >= 9:
            put((pos_x, 7, 10), 'dark_oak_log', 'ceiling_beams', axis='x')
    for pos_x in (6, 8):
        put((pos_x, 6, 4), 'chain', 'chains', axis='y', waterlogged=False)
        put((pos_x, 5, 4), 'lantern', 'lanterns', hanging=True, waterlogged=False)

    for pos_x in (3, 4, 10, 11):
        put((pos_x, 1, 9), 'dark_oak_planks', 'counter')
    for pos_x, facing in ((5, 'west'), (9, 'east')):
        put((pos_x, 1, 9), 'dark_oak_stairs', 'counter', facing=facing, half='bottom', shape='straight', waterlogged=False)
    put((3, 2, 9), 'flower_pot', 'counter_decor')
    put((4, 2, 9), 'candle', 'counter_decor', candles=2, lit=False, waterlogged=False)
    put((10, 2, 9), 'brewing_stand', 'counter_decor', entity=empty_inventory('brewing_stand'), has_bottle_0=False, has_bottle_1=False, has_bottle_2=False)
    put((11, 2, 9), 'flower_pot', 'counter_decor')

    for pos_x in (1, 5):
        for pos_y in range(1, 6):
            put((pos_x, pos_y, 13), 'dark_oak_log', 'shelves', axis='y')
    for pos_x in range(2, 5):
        put((pos_x, 1, 13), 'bookshelf' if pos_x < 4 else 'dark_oak_planks', 'shelves')
        for pos_y in (2, 4):
            put((pos_x, pos_y, 13), 'dark_oak_slab', 'shelves', type='top', waterlogged=False)
        put((pos_x, 5, 13), 'dark_oak_log', 'shelves', axis='x')
    put((2, 3, 13), 'flower_pot', 'shelf_decor')

    for pos_x in range(10, 13):
        put((pos_x, 1, 4), 'dark_oak_log', 'fallen_beam', axis='x')
    for position, material in (((10, 1, 5), 'stone_brick_slab'), ((12, 1, 6), 'mossy_stone_brick_slab'), ((3, 1, 6), 'cobblestone_slab'), ((4, 1, 5), 'mossy_cobblestone_slab')):
        put(position, material, 'rubble', type='bottom', waterlogged=False)
    for pos_x in range(10, 13):
        put((pos_x, 1, 12), 'dark_oak_slab', 'fallen_shelves', type='bottom', waterlogged=False)
    for position in ((13, 1, 12), (13, 1, 13)):
        put(position, 'dark_oak_log', 'fallen_shelves', axis='z')
    put((11, 1, 13), 'dark_oak_stairs', 'fallen_shelves', facing='south', half='bottom', shape='straight', waterlogged=False)

    for position, facing in (((2, 1, 3), 'south'), ((13, 1, 10), 'west')):
        put(position, 'chest', 'empty_chests', entity=empty_inventory('chest'), facing=facing, type='single', waterlogged=False)
    for position, facing in (((1, 1, 5), 'up'), ((2, 1, 6), 'east'), ((13, 1, 2), 'west')):
        put(position, 'barrel', 'empty_barrels', entity=empty_inventory('barrel'), facing=facing, open=False)
    put((12, 1, 2), 'lantern', 'ground_lamp', hanging=False, waterlogged=False)

    for position in ((1, 2, 1), (1, 5, 1), (13, 2, 1), (13, 6, 1), (1, 3, 12), (1, 6, 13), (4, 3, 13), (13, 3, 13), (13, 5, 13), (12, 6, 13)):
        put(position, 'cobweb', 'cobwebs')

    front = compound(messages=Tag(9, [string(json.dumps({'text': text}, ensure_ascii=False)) for text in ('无人营业', 'CLOSED', '陈列 · 空箱', '北侧出口')], 8), color=string('gray'), has_glowing_text=Tag(1, 0))
    back = compound(messages=Tag(9, [string('{"text":""}') for unused in range(4)], 8), color=string('black'), has_glowing_text=Tag(1, 0))
    put((7, 5, 13), 'dark_oak_wall_sign', 'sign', entity=compound(id=string('minecraft:sign'), front_text=front, back_text=back, is_waxed=Tag(1, 1)), facing='north', waterlogged=False)

    aisle = [(pos_x, pos_y, pos_z) for pos_x in range(6, 9) for pos_z in range(14) for pos_y in range(1, 4)]
    circulation = set()
    for start_x, end_x in ((3, 5), (9, 11)):
        circulation.update((pos_x, pos_y, pos_z) for pos_x in range(start_x - 1, end_x + 2) for pos_z in range(8, 11) if pos_x in (start_x - 1, end_x + 1) or pos_z in (8, 10) for pos_y in (1, 2))
    required = [(7, 1, 3), (7, 1, 9), (7, 1, 13), (4, 1, 8), (4, 1, 10), (2, 1, 9), (10, 1, 8), (10, 1, 10), (12, 1, 9), (2, 1, 4), (12, 1, 10), (3, 1, 12)]
    supports = [{'lantern': [pos_x, 5, 4], 'chain': [pos_x, 6, 4], 'beam': [pos_x, 7, 4]} for pos_x in (6, 8)]
    blueprint = {
        'size': list(SIZE), 'features': {name: [list(position) for position in sorted(positions)] for name, positions in groups.items()},
        'entrance_to_counter_aisle': sorted(aisle), 'counter_clearance': sorted(circulation),
        'required_walkable': required, 'lamp_supports': supports,
        'grounded_debris': sorted(groups['fallen_beam'] | groups['fallen_shelves'] | groups['rubble']),
        'counter_gap': [[pos_x, pos_y, 9] for pos_x in range(6, 9) for pos_y in range(1, 4)],
        'chest_approaches': [[2, 1, 4], [12, 1, 10]],
    }
    room_path = ROOT / 'out/room.nbt'
    room.write(room_path)
    write_json(ROOT / 'out/blueprint.json', blueprint)
    write_json(ROOT / 'out/materials.json', dict(sorted(Counter(block.name for block in room.blocks.values() if block != AIR).items())))
    write_json(ROOT / 'out/room.json', {
        'id': 'SHOP_A_RUINED', 'name': 'A·破损无人商店', 'size': list(SIZE),
        'entrance': [7, 1, 0], 'interior': [7, 1, 3], 'facing': 'north',
        'sha256': digest(room_path), 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH,
        'minecraft_version': '1.20.1', 'data_version': 3465,
        'integration': {'status': 'STANDALONE_CANDIDATE_NOT_REGISTERED', 'id_is_provisional': True, 'assigned_slot': None, 'deployed': False, 'requires_coordinator_acceptance': True, 'coordinator_thread': '01a19758-b0e5-75c2-822a-4860527a1e8b'},
        'layout': {'main_aisle_width_blocks': 3, 'main_aisle_height_blocks': 3, 'main_aisle_z': [0, 13], 'counter_sections': [[3, 5], [9, 11]], 'counter_z': 9, 'minimum_counter_clearance_blocks': 1, 'roof': 'complete', 'grounded_debris_count': len(blueprint['grounded_debris']), 'cobwebs_off_main_aisle': True},
        'substitutions': [
            '概念图转译为15×9×15完整封顶房间，北侧3×3接口；剖切只用于预览。',
            '断柜台拆成左右两段，断口用原版楼梯，中央三格通路贯穿至后墙。',
            '塌梁、碎石及倒架全部放在Y=1、完整地板之上，不悬空、不堵通路。',
            '地面破洞改成开裂/苔石/安山岩拼接，保留老旧感而不形成跌落坑。',
            '高处蛛网贴墙或接货架；无蛛网放在主路或柜台环路。',
            '斜招牌、散落药瓶/金币转译为合法壁牌、空花盆、未点蜡烛和空酿造台，不生成可拾取物品。',
            '无人、无交易系统、无刷怪、无奖励、无生物或展示实体；保留少量空箱桶。',
        ],
    })
    empty = Structure((1, 1, 1))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / 'src/main/resources/data/ftshopqa/structures/empty.nbt')
    print(json.dumps({'status': 'BUILT', 'id': 'SHOP_A_RUINED', 'size': SIZE, 'records': len(room.blocks), 'sha256': digest(room_path)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
