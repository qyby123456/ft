import hashlib
import json
from collections import Counter, defaultdict
from pathlib import Path

from PIL import Image

from nbtio import AIR, State, Structure, Tag, compound, string


ROOT = Path(__file__).resolve().parents[1]
DESIGN = Path('D:/ft工程文件/image2.5/dungeon-shop-variants-20261102')
REFERENCE = 'shop-variant-B-monster-shop.png'
REFERENCE_HASH = 'c21adfa37ec561da27affb7def791fc78f5e727b74cb2dbb7b92666210522212'
SIZE = (11, 8, 11)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    reference = ROOT / 'reference' / REFERENCE
    original_reference = DESIGN / 'out' / REFERENCE
    if digest(reference) != REFERENCE_HASH or original_reference.exists() and digest(original_reference) != REFERENCE_HASH:
        raise ValueError('Variant B design fingerprint mismatch')
    with Image.open(reference) as image:
        dimensions = list(image.size)
    write_json(ROOT / 'reference/input-fingerprints.json', {
        'variant': 'B', 'images': [{'file': REFERENCE, 'source': str(DESIGN / 'out' / REFERENCE), 'size': dimensions, 'sha256': REFERENCE_HASH}],
        'source_documents': [{'file': 'SOURCE_' + name, 'sha256': digest(ROOT / 'reference' / ('SOURCE_' + name))} for name in ('README.md', 'manifest.json', 'DISPATCH.md')],
        'missing_source_document': 'STYLES.md is named in README but absent from the supplied directory; not fabricated.',
    })
    room = Structure(SIZE)
    groups = defaultdict(set)
    supports = []

    def put(position, name, group, entity=None, **properties):
        state = State('minecraft:' + name, tuple(sorted((key, str(value).lower()) for key, value in properties.items())))
        room.set(position, state, entity)
        for previous in groups.values():
            previous.discard(position)
        groups[group].add(position)

    def inventory(identifier):
        return compound(id=string('minecraft:' + identifier), Items=Tag(9, [], 10))

    def supported(position, below, category):
        supports.append({'block': list(position), 'support': list(below), 'category': category})

    for pos_y in range(SIZE[1]):
        for pos_z in range(SIZE[2]):
            for pos_x in range(SIZE[0]):
                room.set((pos_x, pos_y, pos_z), AIR)
    for pos_x in range(11):
        for pos_z in range(11):
            material = ('stone_bricks', 'mossy_stone_bricks', 'cracked_stone_bricks')[(pos_x * 7 + pos_z * 3) % 3]
            if 3 <= pos_x <= 7 and 2 <= pos_z <= 5:
                material = 'yellow_terracotta' if pos_x in (3, 7) or pos_z in (2, 5) else 'red_terracotta'
            put((pos_x, 0, pos_z), material, 'floor')
            put((pos_x, 7, pos_z), 'dark_oak_planks', 'roof')
            if pos_x in (0, 10) or pos_z in (0, 10):
                for pos_y in range(1, 7):
                    if pos_z == 0 and 4 <= pos_x <= 6 and pos_y <= 3:
                        continue
                    material = ('stone_bricks', 'mossy_stone_bricks', 'cracked_stone_bricks')[(pos_x * 5 + pos_y * 3 + pos_z) % 3]
                    put((pos_x, pos_y, pos_z), material, 'walls')
    for pos_x, pos_z in ((0, 0), (10, 0), (0, 10), (10, 10)):
        for pos_y in range(1, 7):
            put((pos_x, pos_y, pos_z), 'polished_deepslate' if pos_y in (1, 6) else 'deepslate_bricks', 'pillars')
    for pos_z in (0, 3, 9, 10):
        for pos_x in range(1, 10):
            put((pos_x, 6, pos_z), 'dark_oak_log', 'beams', axis='x')
    for pos_x in (0, 10):
        for pos_z in range(1, 10):
            put((pos_x, 6, pos_z), 'dark_oak_log', 'beams', axis='z')

    for pos_x in range(3, 8):
        put((pos_x, 1, 6), 'dark_oak_planks', 'counter')
    put((3, 2, 6), 'brewing_stand', 'counter_decor', entity=inventory('brewing_stand'), has_bottle_0=False, has_bottle_1=False, has_bottle_2=False)
    put((4, 2, 6), 'lime_candle', 'counter_decor', candles=2, lit=True, waterlogged=False)
    put((6, 2, 6), 'skeleton_skull', 'counter_decor', entity=compound(id=string('minecraft:skull')), rotation=0)
    put((7, 2, 6), 'potted_red_mushroom', 'counter_decor')
    for pos_x in (3, 4, 6, 7):
        supported((pos_x, 2, 6), (pos_x, 1, 6), 'counter_display')

    for pos_x in (1, 9):
        for pos_z in range(5, 9):
            put((pos_x, 1, pos_z), 'bookshelf', 'shelves')
            for pos_y in (2, 4):
                put((pos_x, pos_y, pos_z), 'shroomlight' if pos_y == 4 and pos_z == 6 else 'dark_oak_planks', 'shelves')
            if pos_z in (5, 8):
                put((pos_x, 3, pos_z), 'dark_oak_log', 'shelves', axis='y')
        put((pos_x, 3, 6), 'potted_brown_mushroom', 'shelf_decor')
        put((pos_x, 3, 7), 'skeleton_skull', 'shelf_decor', entity=compound(id=string('minecraft:skull')), rotation=4 if pos_x == 1 else 12)
        put((pos_x, 5, 6), 'potted_red_mushroom', 'shelf_decor')
        put((pos_x, 5, 7), 'lime_candle', 'shelf_decor', candles=2, lit=True, waterlogged=False)
        for pos_y, pos_z in ((3, 6), (3, 7), (5, 6), (5, 7)):
            supported((pos_x, pos_y, pos_z), (pos_x, pos_y - 1, pos_z), 'shelf_display')
        put((pos_x, 1, 9), 'barrel', 'barrels', entity=inventory('barrel'), facing='up', open=False)
    for pos_x in (2, 8):
        put((pos_x, 1, 9), 'bookshelf', 'shelves')
        put((pos_x, 2, 9), 'dark_oak_planks', 'shelves')
        put((pos_x, 3, 9), 'potted_brown_mushroom', 'shelf_decor')
        supported((pos_x, 3, 9), (pos_x, 2, 9), 'shelf_display')

    for pos_x in range(1, 4):
        for pos_z in range(1, 4):
            put((pos_x, 1, pos_z), 'dark_oak_planks', 'eye_plinth')
            put((pos_x, 2, pos_z), 'lime_stained_glass', 'eye_vat')
            put((pos_x, 3, pos_z), 'lime_stained_glass', 'eye_vat')
            put((pos_x, 4, pos_z), 'dark_oak_slab', 'eye_lid', type='bottom', waterlogged=False)
            for pos_y in range(2, 5):
                supported((pos_x, pos_y, pos_z), (pos_x, pos_y - 1, pos_z), 'eye_vat')
    put((2, 2, 2), 'shroomlight', 'eye_core')
    put((2, 2, 1), 'black_concrete', 'eye_pupil')

    cage_positions = {(pos_x, pos_y, pos_z) for pos_x in range(7, 10) for pos_z in range(1, 4) if pos_x in (7, 9) or pos_z in (1, 3) for pos_y in (2, 3)}
    for pos_x in range(7, 10):
        for pos_z in range(1, 4):
            put((pos_x, 1, pos_z), 'polished_deepslate', 'cage_base')
            put((pos_x, 4, pos_z), 'polished_deepslate_slab', 'cage_roof', type='bottom', waterlogged=False)
    for position in sorted(cage_positions):
        pos_x, pos_y, pos_z = position
        neighbors = {}
        for name, offset in {'north': (0, 0, -1), 'east': (1, 0, 0), 'south': (0, 0, 1), 'west': (-1, 0, 0)}.items():
            neighbor = tuple(position[axis] + offset[axis] for axis in range(3))
            neighbors[name] = neighbor in cage_positions or room.get(neighbor) != AIR
        put(position, 'iron_bars', 'cage_bars', waterlogged=False, **neighbors)
        supported(position, (pos_x, pos_y - 1, pos_z), 'cage')
    put((8, 2, 2), 'skeleton_skull', 'cage_effigy', entity=compound(id=string('minecraft:skull')), rotation=0)
    supported((8, 2, 2), (8, 1, 2), 'cage_effigy')

    for pos_y in (1, 2):
        for pos_x in range(4, 7):
            put((pos_x, pos_y, 8), 'purple_terracotta', 'merchant_statue')
    for pos_x in (3, 7):
        put((pos_x, 2, 8), 'green_terracotta', 'merchant_statue')
        put((pos_x, 3, 8), 'purple_terracotta', 'merchant_statue')
    for pos_x in range(3, 8):
        put((pos_x, 4, 8), 'purple_terracotta', 'merchant_statue')
    for pos_x in (4, 6):
        put((pos_x, 3, 8), 'ochre_froglight', 'merchant_statue', axis='z')
    put((5, 3, 8), 'black_concrete', 'merchant_statue')
    for pos_x in (3, 7):
        put((pos_x, 5, 8), 'bone_block', 'merchant_horns', axis='y')
        supported((pos_x, 5, 8), (pos_x, 4, 8), 'merchant_horns')

    for pos_x, name in ((4, 'lantern'), (6, 'soul_lantern')):
        put((pos_x, 5, 3), 'chain', 'chains', axis='y', waterlogged=False)
        put((pos_x, 4, 3), name, 'lanterns', hanging=True, waterlogged=False)
    front = compound(messages=Tag(9, [string(json.dumps({'text': text}, ensure_ascii=False)) for text in ('怪物商店', 'MONSTER SHOP', '方块雕像 / 陈列', '无交易 / 无刷怪')], 8), color=string('yellow'), has_glowing_text=Tag(1, 1))
    back = compound(messages=Tag(9, [string('{"text":""}') for unused in range(4)], 8), color=string('black'), has_glowing_text=Tag(1, 0))
    put((5, 5, 9), 'dark_oak_wall_sign', 'sign', entity=compound(id=string('minecraft:sign'), front_text=front, back_text=back, is_waxed=Tag(1, 1)), facing='north', waterlogged=False)
    for pos_x in range(3, 8):
        put((pos_x, 5, 10), 'dark_oak_planks', 'sign_backing')

    target = ROOT / 'out/room.nbt'
    room.write(target)
    clearance = [(pos_x, pos_y, pos_z) for pos_x in range(2, 9) for pos_z in range(5, 8) if pos_x in (2, 8) or pos_z in (5, 7) for pos_y in (1, 2)]
    aisle = [(pos_x, pos_y, pos_z) for pos_x in range(4, 7) for pos_y in range(1, 4) for pos_z in range(6)]
    write_json(ROOT / 'out/blueprint.json', {
        'size': list(SIZE), 'features': {name: [list(position) for position in sorted(positions)] for name, positions in sorted(groups.items())},
        'counter_clearance': [list(position) for position in sorted(clearance)], 'entrance_to_counter_aisle': [list(position) for position in sorted(aisle)],
        'required_walkable': [[5, 1, 3], [5, 1, 5], [2, 1, 6], [8, 1, 6], [5, 1, 7], [2, 1, 8], [8, 1, 8], [4, 1, 2], [6, 1, 2]],
        'lamp_supports': [{'lantern': [pos_x, 4, 3], 'chain': [pos_x, 5, 3], 'beam': [pos_x, 6, 3]} for pos_x in (4, 6)],
        'support_links': supports, 'negative_support_probes': [[2, 1, 2], [7, 1, 2]],
        'merchant_separation': {'counter_z': 6, 'clear_row_z': 7, 'statue_z': 8, 'kind': 'static connected vanilla blocks, not an entity'},
    })
    write_json(ROOT / 'out/room.json', {
        'id': 'SHOP_B_MONSTER', 'name': '怪物商店·紫袍藏品铺', 'variant': 'B', 'size': list(SIZE), 'entrance': [5, 1, 0], 'interior': [5, 1, 3], 'facing': 'north',
        'sha256': digest(target), 'reference': str(DESIGN / 'out' / REFERENCE), 'reference_sha256': REFERENCE_HASH, 'minecraft_version': '1.20.1', 'data_version': 3465,
        'integration': {'status': 'STANDALONE_CANDIDATE_NOT_REGISTERED', 'id_is_provisional': True, 'assigned_slot': None, 'deployed': False, 'requires_coordinator_acceptance': True, 'coordinator_thread': '01a19758-b0e5-75c2-822a-4860527a1e8b'},
        'layout': {'main_aisle_width_blocks': 3, 'main_aisle_height_blocks': 3, 'counter_clearance_blocks': 1, 'roof': 'complete', 'merchant': 'nonliving block sculpture behind counter'},
        'substitutions': ['怪物店主改为紫袍、骨角、双发光眼的落地方块雕像；无实体、AI、刷怪和交易。', '眼球罐转译为西北角有木台支撑的绿玻璃大罐、发光内核和黑瞳；不使用物品实体。', '悬挂小怪笼改为东北角落地铁笼，内部仅静态头骨；每根笼柱均接触底座，笼顶为连续盖板。', '绿色吊灯改用原版灵魂灯；发光蘑菇通过有支撑的盆栽与下层菌光体表现。', '地毯内嵌陶瓦地板；柜台与雕像间保留完整一格店主通行带；所有货品为空容器或方块。'],
    })
    write_json(ROOT / 'out/materials.json', dict(sorted(Counter(block.name for block in room.blocks.values() if block != AIR).items())))
    empty = Structure((1, 1, 1))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / 'src/main/resources/data/ftshopqa/structures/empty.nbt')
    print(json.dumps({'status': 'BUILT', 'records': len(room.blocks), 'sha256': digest(target), 'entities': len(room.entities), 'block_entities': len(room.block_entities)}))


if __name__ == '__main__':
    main()
