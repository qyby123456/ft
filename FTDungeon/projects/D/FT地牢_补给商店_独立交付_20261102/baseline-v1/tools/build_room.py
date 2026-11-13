import hashlib
import json
from collections import Counter
from pathlib import Path

from PIL import Image

from nbtio import AIR, Structure, Tag, compound, state, string


ROOT = Path(__file__).resolve().parents[1]
DESIGN = Path('D:/ft工程文件/image2.5/dungeon-shop-20261102')
INTEGRATION = Path('D:/ft工程文件/FT地牢_Image25接入_20261101')
CORE = Path('D:/ft工程文件/FT-Dungeon-Mobs-Forge-1.20.1')
SIZE = (11, 8, 11)
EXPECTED_IMAGES = {
    'dungeon-shop-hero.png': 'f26921137e349646d08690a5964a2235636945a41d39179d48d37d5cac68a355',
    'dungeon-shop-merchant.png': '3be525340bb4602e9dbec01e8179fb45dd4acdbb5ecd2c081bdb69d60c0cc23e',
    'dungeon-shop-layout-checked.png': '96449cd195ac4af7a88c9d4cef5b50dab898c232f8d01b81748e5a09270918b0',
}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def inventory_entity(kind):
    return compound(id=string('minecraft:' + kind), Items=Tag(9, [], 10))


def main():
    references = []
    for name, expected in EXPECTED_IMAGES.items():
        path = ROOT / 'reference' / name
        actual = digest(path)
        if actual != expected:
            raise ValueError('Reference fingerprint mismatch: ' + name)
        with Image.open(path) as image:
            image.load()
            dimensions = list(image.size)
        references.append({'file': name, 'source': str(DESIGN / 'out' / name), 'sha256': actual, 'size': dimensions})
    write_json(ROOT / 'reference/input-fingerprints.json', {
        'images': references,
        'selected': 'dungeon-shop-hero.png',
        'layout_checked_actual_size': [1536, 1324],
        'source_manifest_stale_for_layout_checked': True,
        'merchant_handoff_hash_typo': {'declared_63_characters': '3be525340bb4602e9dbec01e8179fb45dd4acbb5ecd2c081bdb69d60c0cc23e', 'resolution': 'Actual source and delivered copy both match the 64-character merchant digest in the original source manifest.'},
        'note': 'Copied source manifest is provenance only; the hashes above were recomputed from the delivered PNG files.',
    })

    room = Structure(SIZE)
    room.box((0, 0, 0), (10, 7, 10), AIR)
    groups = {}
    occupied = {}

    def put(position, name, group=None, entity=None, **properties):
        if group:
            if position in occupied and occupied[position] != group:
                raise ValueError(f'Feature overlap at {position}: {occupied[position]} / {group}')
            occupied[position] = group
            groups.setdefault(group, set()).add(position)
        room.set(position, state(name, **properties), entity)

    for pos_z in range(11):
        for pos_x in range(11):
            floor = ('stone_bricks', 'cracked_stone_bricks', 'mossy_stone_bricks', 'polished_andesite')[(pos_x * 7 + pos_z * 3) % 11 % 4]
            if pos_x in (0, 10) or pos_z in (0, 10):
                floor = 'polished_blackstone_bricks'
            if 4 <= pos_x <= 6 and 1 <= pos_z <= 5:
                floor = 'red_terracotta' if pos_x == 5 else 'brown_terracotta'
            put((pos_x, 0, pos_z), floor)
            put((pos_x, 7, pos_z), 'deepslate_tiles')
            if pos_x in (0, 10) or pos_z in (0, 10):
                for pos_y in range(1, 7):
                    wall = 'polished_blackstone_bricks' if pos_y == 1 else ('stone_bricks', 'cracked_stone_bricks', 'stone_bricks', 'mossy_stone_bricks')[(pos_x * 11 + pos_z * 7 + pos_y) % 4]
                    put((pos_x, pos_y, pos_z), wall)

    for pos_x, pos_z in ((0, 0), (10, 0), (0, 10), (10, 10)):
        for pos_y in range(1, 7):
            put((pos_x, pos_y, pos_z), 'chiseled_stone_bricks' if pos_y in (1, 6) else 'stone_bricks', 'pillars')

    for pos_x in range(4, 7):
        for pos_y in range(1, 4):
            room.set((pos_x, pos_y, 0), AIR)
        put((pos_x, 4, 0), 'polished_blackstone_bricks')
    for pos_x in (3, 7):
        for pos_y in range(1, 4):
            put((pos_x, pos_y, 0), 'chiseled_stone_bricks')

    for pos_z in (3, 8):
        for pos_x in range(11):
            put((pos_x, 6, pos_z), 'dark_oak_log', 'beams', axis='x')

    for pos_x in range(3, 8):
        put((pos_x, 1, 6), 'dark_oak_planks', 'counter')
    put((3, 2, 6), 'flower_pot', 'counter_decor')
    put((5, 2, 6), 'candle', 'counter_decor', candles=2, lit=True, waterlogged=False)
    put((7, 2, 6), 'yellow_candle', 'counter_decor', candles=3, lit=False, waterlogged=False)

    for pos_x in (1, 9):
        for pos_z in (3, 7):
            for pos_y in range(1, 5):
                put((pos_x, pos_y, pos_z), 'dark_oak_log', 'shelves', axis='y')
        for pos_z in range(4, 7):
            base = 'bookshelf' if pos_x == 1 else ('crafting_table' if pos_z == 4 else 'cartography_table' if pos_z == 6 else 'dark_oak_planks')
            put((pos_x, 1, pos_z), base, 'shelves')
            put((pos_x, 3, pos_z), 'dark_oak_slab', 'shelves', type='top', waterlogged=False)
        for pos_z in range(3, 8):
            put((pos_x, 5, pos_z), 'dark_oak_slab', 'shelves', type='bottom', waterlogged=False)
        put((pos_x, 2, 5), 'flower_pot', 'shelf_decor')
        put((pos_x, 4, 4), 'potted_fern', 'shelf_decor')
        put((pos_x, 4, 6), 'skeleton_skull', 'shelf_decor', entity=compound(id=string('minecraft:skull')), rotation=4 if pos_x == 1 else 12)

    for pos_x in (3, 7):
        for pos_y in range(1, 5):
            put((pos_x, pos_y, 9), 'dark_oak_log', 'shelves', axis='y')
    for pos_x in range(4, 7):
        if pos_x == 5:
            put((pos_x, 1, 9), 'chest', 'shelves', entity=inventory_entity('chest'), facing='north', type='single', waterlogged=False)
        else:
            put((pos_x, 1, 9), 'dark_oak_planks', 'shelves')
        put((pos_x, 3, 9), 'dark_oak_slab', 'shelves', type='top', waterlogged=False)
    put((4, 4, 9), 'flower_pot', 'shelf_decor')
    put((5, 4, 9), 'skeleton_skull', 'shelf_decor', entity=compound(id=string('minecraft:skull')), rotation=0)
    put((6, 4, 9), 'brewing_stand', 'shelf_decor', entity=inventory_entity('brewing_stand'), has_bottle_0=False, has_bottle_1=False, has_bottle_2=False)

    for pos_x, pos_z in ((1, 1), (9, 1), (1, 9), (9, 9)):
        put((pos_x, 1, pos_z), 'barrel', 'barrels', entity=inventory_entity('barrel'), facing='up', open=False)

    messages = ['地牢补给', 'SUPPLY SHOP', '陈列 / 无库存', '北侧入口']
    text = compound(messages=Tag(9, [string(json.dumps({'text': text}, ensure_ascii=False)) for text in messages], 8), color=string('yellow'), has_glowing_text=Tag(1, 0))
    back = compound(messages=Tag(9, [string('{"text":""}') for _ in range(4)], 8), color=string('black'), has_glowing_text=Tag(1, 0))
    put((5, 5, 9), 'dark_oak_wall_sign', 'sign', entity=compound(id=string('minecraft:sign'), front_text=text, back_text=back, is_waxed=Tag(1, 1)), facing='north', waterlogged=False)

    for pos_x in (4, 6):
        put((pos_x, 5, 3), 'chain', 'chains', axis='y', waterlogged=False)
        put((pos_x, 4, 3), 'lantern', 'lanterns', hanging=True, waterlogged=False)

    target = ROOT / 'out/room.nbt'
    room.write(target)
    counter_ring = [(pos_x, pos_y, pos_z) for pos_x in range(2, 9) for pos_z in range(5, 8) if pos_x in (2, 8) or pos_z in (5, 7) for pos_y in (1, 2)]
    aisle = [(pos_x, pos_y, pos_z) for pos_x in range(4, 7) for pos_y in range(1, 4) for pos_z in range(6)]
    blueprint = {
        'size': list(SIZE),
        'features': {name: [list(position) for position in sorted(positions)] for name, positions in groups.items()},
        'counter_clearance': [list(position) for position in sorted(counter_ring)],
        'entrance_to_counter_aisle': [list(position) for position in sorted(aisle)],
        'required_walkable': [[5, 1, 3], [5, 1, 5], [2, 1, 6], [8, 1, 6], [5, 1, 7], [5, 1, 8]],
        'lamp_supports': [{'lantern': [pos_x, 4, 3], 'chain': [pos_x, 5, 3], 'beam': [pos_x, 6, 3]} for pos_x in (4, 6)],
    }
    write_json(ROOT / 'out/blueprint.json', blueprint)
    metadata = {
        'id': 'SHOP_SUPPLY_DSH',
        'name': '地牢补给商店·暗木石窖',
        'size': list(SIZE),
        'entrance': [5, 1, 0],
        'interior': [5, 1, 3],
        'facing': 'north',
        'sha256': digest(target),
        'reference': str(DESIGN / 'out/dungeon-shop-hero.png'),
        'reference_sha256': EXPECTED_IMAGES['dungeon-shop-hero.png'],
        'minecraft_version': '1.20.1',
        'data_version': 3465,
        'integration': {'status': 'STANDALONE_CANDIDATE_NOT_REGISTERED', 'id_is_provisional': True, 'assigned_slot': None, 'deployed': False, 'requires_coordinator_acceptance': True},
        'layout': {'clear_internal_floor_size': [9, 9], 'counter_size': [5, 1], 'minimum_counter_clearance_blocks': 1, 'main_aisle_width_blocks': 3, 'main_aisle_height_blocks': 3, 'roof': 'complete'},
        'substitutions': ['内部纵深由概念图约7格扩至9格，落实柜台四侧净空和后墙货架。', '地毯改为地板内嵌红/棕陶瓦，不在主路增加碰撞高度。', '小瓶、卷轴与武器道具转译为花盆、空酿造台、书架、工作台及蜡烛；无物品实体和库存。', '吊灯调整到北门至柜台前的三格通道上方，不放在柜台投影上。', '不生成店主或顾客；本交付为陈列房间，不实现交易、波次、奖励或刷怪逻辑。'],
    }
    write_json(ROOT / 'out/room.json', metadata)
    counts = Counter(block.name for block in room.blocks.values() if block != AIR)
    write_json(ROOT / 'out/materials.json', dict(sorted(counts.items())))
    empty = Structure((1, 1, 1))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / 'src/main/resources/data/ftshopqa/structures/empty.nbt')

    boundary_path = ROOT / 'qa/boundary-snapshot.json'
    if not boundary_path.exists():
        watched = [CORE / 'src/main/resources/data/ftdungeonmobs/dungeon_rooms/catalog.json', INTEGRATION / 'CONTRACT.md', INTEGRATION / 'queue/batch3.json']
        for ident in ('L1', 'L2', 'M1', 'M2', 'S1', 'S2', 'L3', 'L4', 'S3'):
            watched.extend(INTEGRATION / 'incoming' / ident / name for name in ('room.nbt', 'room.json'))
        write_json(boundary_path, {
            'policy': 'Read-only observations, not locks; parallel owners may change these paths. This task only writes inside its independent root.',
            'independent_root': str(ROOT),
            'core_catalog_observed': json.loads(watched[0].read_text(encoding='utf-8-sig')),
            'reserved_batch3_ids_not_touched': ['M5', 'M6', 'M7', 'S5', 'S6', 'S7'],
            'observations': [{'path': str(path), 'sha256': digest(path)} for path in watched],
        })
    print(json.dumps({'status': 'BUILT', 'size': SIZE, 'records': len(room.blocks), 'sha256': digest(target)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
