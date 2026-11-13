import hashlib
import json
from collections import Counter
from pathlib import Path

from PIL import Image

from nbtio import AIR, Structure, Tag, compound, integer, state, string


ROOT = Path(__file__).resolve().parents[1]
SIZE = (17, 11, 17)
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-shop-variants-20261102/out/shop-variant-F-cataclysm-iron.png')
REFERENCE_SHA256 = '28ba3cc97775b9f94cb68083d19dee1778079488f678f22bdfd0b5cdfe08e65f'


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def empty_inventory(kind):
    return compound(id=string('minecraft:' + kind), Items=Tag(9, [], 10))


def banner_entity():
    patterns = [compound(Pattern=string(pattern), Color=integer(color)) for pattern, color in [('mr', 14), ('cs', 14), ('bo', 4)]]
    return compound(id=string('minecraft:banner'), Patterns=Tag(9, patterns, 10))


def main():
    reference = ROOT / 'reference/shop-variant-F-cataclysm-iron.png'
    if digest(reference) != REFERENCE_SHA256:
        raise ValueError('Design reference SHA-256 mismatch')
    with Image.open(reference) as image:
        dimensions = list(image.size)
    write_json(ROOT / 'reference/input-fingerprints.json', {
        'source': str(REFERENCE), 'copy': str(reference), 'sha256': digest(reference), 'size': dimensions,
        'source_styles_md_present': False,
        'note': 'STYLES.md is named by the source README but absent on disk. README, DISPATCH, manifest and F prompt were preserved without inventing that file.',
    })
    room = Structure(SIZE)
    room.box((0, 0, 0), (16, 10, 16), AIR)
    features = {}
    ownership = {}

    def put(position, name, group=None, entity=None, **properties):
        if group:
            previous = ownership.get(position)
            if previous is not None and previous != group:
                raise ValueError(f'Feature collision at {position}: {previous} / {group}')
            ownership[position] = group
            features.setdefault(group, set()).add(position)
        room.set(position, state(name, **properties), entity)

    for pos_z in range(17):
        for pos_x in range(17):
            floor = 'deepslate_tiles' if (pos_x + pos_z) % 5 else 'polished_deepslate'
            if pos_x in (0, 16) or pos_z in (0, 16):
                floor = 'obsidian'
            put((pos_x, 0, pos_z), floor)
            put((pos_x, 10, pos_z), 'deepslate_tiles')
            if pos_x in (0, 16) or pos_z in (0, 15, 16):
                for pos_y in range(1, 10):
                    material = 'polished_blackstone_bricks'
                    if (pos_x * 7 + pos_z * 3 + pos_y) % 9 == 0:
                        material = 'cracked_polished_blackstone_bricks'
                    if pos_y in (1, 9):
                        material = 'obsidian'
                    put((pos_x, pos_y, pos_z), material)

    pillar_lines = [(0, 0), (16, 0), (0, 16), (16, 16), (0, 6), (16, 6), (0, 12), (16, 12)]
    for pos_x, pos_z in pillar_lines:
        for pos_y in range(1, 9):
            material = 'gold_block' if pos_y in (1, 8) else 'chiseled_polished_blackstone' if pos_y in (2, 7) else 'polished_deepslate'
            put((pos_x, pos_y, pos_z), material, 'pillars')

    for pos_x in range(7, 10):
        for pos_y in range(1, 4):
            room.set((pos_x, pos_y, 0), AIR)
        put((pos_x, 4, 0), 'chiseled_polished_blackstone', 'entrance_frame')
    for pos_x in (6, 10):
        for pos_y in range(1, 5):
            put((pos_x, pos_y, 0), 'gilded_blackstone' if pos_y == 1 else 'polished_blackstone_bricks', 'entrance_frame')

    for pos_z in (6, 12):
        for pos_x in range(17):
            put((pos_x, 9, pos_z), 'polished_deepslate', 'beams')
        for pos_x, facing in ((1, 'east'), (15, 'west')):
            put((pos_x, 8, pos_z), 'polished_blackstone_brick_stairs', 'ceiling_ribs', facing=facing, half='top', shape='straight', waterlogged=False)

    rune = {(8, 3), (7, 4), (9, 4), (6, 5), (8, 5), (10, 5), (7, 6), (9, 6), (8, 7)}
    for pos_x in range(5, 12):
        for pos_z in range(2, 9):
            material = 'black_terracotta'
            if pos_x in (5, 11) or pos_z in (2, 8):
                material = 'gold_block' if pos_x in (5, 11) and pos_z in (2, 8) else 'gilded_blackstone'
            if (pos_x, pos_z) in rune:
                material = 'red_terracotta'
            put((pos_x, 0, pos_z), material, 'floor_inlay')

    for pos_x in range(4, 13):
        material = 'gilded_blackstone' if pos_x in (4, 8, 12) else 'polished_blackstone_bricks'
        put((pos_x, 1, 10), material, 'counter')
        if pos_x in (4, 6, 8, 10, 12):
            put((pos_x, 2, 10), 'polished_blackstone_slab', 'counter_decor', type='bottom', waterlogged=False)
    put((5, 2, 10), 'skeleton_skull', 'counter_decor', entity=compound(id=string('minecraft:skull')), rotation=8)
    put((7, 2, 10), 'red_candle', 'counter_decor', candles=3, lit=True, waterlogged=False)
    put((9, 2, 10), 'brewing_stand', 'counter_decor', entity=empty_inventory('brewing_stand'), has_bottle_0=False, has_bottle_1=False, has_bottle_2=False)
    put((11, 2, 10), 'potted_crimson_fungus', 'counter_decor')

    for frame_x, inner_x in (((1, 5), (2, 3, 4)), ((11, 15), (12, 13, 14))):
        for pos_x in frame_x:
            for pos_y in range(1, 7):
                put((pos_x, pos_y, 14), 'gilded_blackstone' if pos_y in (1, 6) else 'polished_blackstone_bricks', 'shelves')
        for pos_x in inner_x:
            put((pos_x, 1, 14), 'chiseled_polished_blackstone', 'shelves')
            for pos_y in (3, 5):
                put((pos_x, pos_y, 14), 'polished_blackstone_slab', 'shelves', type='top', waterlogged=False)
            put((pos_x, 7, 14), 'polished_blackstone_slab', 'shelves', type='bottom', waterlogged=False)
            if pos_x not in (3, 13):
                put((pos_x, 2, 14), 'potted_warped_fungus', 'shelf_decor')
            if pos_x in (2, 12):
                put((pos_x, 4, 14), 'skeleton_skull', 'shelf_decor', entity=compound(id=string('minecraft:skull')), rotation=8)
            else:
                put((pos_x, 4, 14), 'potted_crimson_fungus', 'shelf_decor')
            put((pos_x, 6, 14), 'red_candle', 'shelf_decor', candles=2, lit=True, waterlogged=False)

    lava_cells = []
    for pos_x in (3, 13):
        source = (pos_x, 2, 15)
        glass = (pos_x, 2, 14)
        put(source, 'lava', 'sealed_lava', level=0)
        put(glass, 'glass', 'sealed_lava_glazing')
        for neighbor in ((pos_x - 1, 2, 15), (pos_x + 1, 2, 15), (pos_x, 1, 15), (pos_x, 3, 15), (pos_x, 2, 16)):
            put(neighbor, 'obsidian', 'lava_enclosure')
        lava_cells.append({'source': source, 'glass': glass, 'enclosure': [
            (pos_x - 1, 2, 15), (pos_x + 1, 2, 15), (pos_x, 1, 15), (pos_x, 3, 15), (pos_x, 2, 16), glass,
        ]})

    for pos_x in (6, 10):
        for pos_y in range(1, 6):
            put((pos_x, pos_y, 14), 'gilded_blackstone' if pos_y == 1 else 'chiseled_polished_blackstone', 'gothic_arch')
        put((pos_x, 6, 14), 'polished_blackstone_brick_stairs', 'gothic_arch', facing='east' if pos_x == 6 else 'west', half='top', shape='straight', waterlogged=False)
    for pos_x, facing in ((7, 'east'), (9, 'west')):
        put((pos_x, 7, 14), 'polished_blackstone_brick_stairs', 'gothic_arch', facing=facing, half='top', shape='straight', waterlogged=False)
    put((8, 8, 14), 'gilded_blackstone', 'gothic_arch')
    for pos_y in range(1, 8):
        put((8, pos_y, 15), 'red_nether_bricks', 'rune_backing')
    put((8, 6, 14), 'black_wall_banner', 'banners', entity=banner_entity(), facing='north')
    banner_supports = [{'banner': (8, 6, 14), 'backing': (8, 6, 15)}]
    for pos_x, wall_x, facing in ((1, 0, 'east'), (15, 16, 'west')):
        for pos_z in (5, 11):
            put((pos_x, 7, pos_z), 'black_wall_banner', 'banners', entity=banner_entity(), facing=facing)
            banner_supports.append({'banner': (pos_x, 7, pos_z), 'backing': (wall_x, 7, pos_z)})

    displays = []
    for center_x, kind in ((2, 'skeleton_skull'), (14, 'amethyst_cluster')):
        for pos_x in range(center_x - 1, center_x + 2):
            for pos_z in range(2, 5):
                put((pos_x, 1, pos_z), 'gilded_blackstone' if pos_z in (2, 4) else 'polished_blackstone_bricks', 'display_bases')
                put((pos_x, 3, pos_z), 'glass', 'display_glass')
                if (pos_x, pos_z) != (center_x, 3):
                    put((pos_x, 2, pos_z), 'glass', 'display_glass')
        if kind == 'skeleton_skull':
            put((center_x, 2, 3), kind, 'display_artifacts', entity=compound(id=string('minecraft:skull')), rotation=8)
        else:
            put((center_x, 2, 3), kind, 'display_artifacts', facing='up', waterlogged=False)
        displays.append({'artifact': (center_x, 2, 3), 'base': (center_x, 1, 3), 'bounds': [(center_x - 1, 1, 2), (center_x + 1, 3, 4)]})

    for pos_x in (2, 14):
        put((pos_x, 1, 12), 'chest', 'empty_storage', entity=empty_inventory('chest'), facing='north', type='single', waterlogged=False)

    lamp_supports = []
    for pos_x in (7, 9):
        put((pos_x, 5, 6), 'lantern', 'lamps', hanging=True, waterlogged=False)
        chains = []
        for pos_y in (6, 7, 8):
            put((pos_x, pos_y, 6), 'chain', 'chains', axis='y', waterlogged=False)
            chains.append((pos_x, pos_y, 6))
        lamp_supports.append({'lantern': (pos_x, 5, 6), 'chains': chains, 'beam': (pos_x, 9, 6), 'roof': (pos_x, 10, 6)})

    aisle = [(pos_x, pos_y, pos_z) for pos_x in range(7, 10) for pos_y in range(1, 4) for pos_z in range(10)]
    ring_feet = {(pos_x, 1, pos_z) for pos_x in range(3, 14) for pos_z in (9, 11)} | {(pos_x, 1, 10) for pos_x in (3, 13)}
    ring_clear = [(pos_x, pos_y, pos_z) for pos_x, _, pos_z in sorted(ring_feet) for pos_y in (1, 2)]
    required = sorted(ring_feet | {(8, 1, 12), (2, 1, 11), (14, 1, 11), (3, 1, 13), (8, 1, 13), (13, 1, 13)})
    blueprint = {
        'size': SIZE, 'north_opening': {'x': [7, 9], 'y': [1, 3], 'z': 0},
        'features': {name: sorted(coordinates) for name, coordinates in sorted(features.items())},
        'pillar_wall_lines': pillar_lines, 'counter_bounds': [(4, 1, 10), (12, 1, 10)],
        'entrance_to_counter_aisle': aisle, 'counter_clearance': ring_clear, 'required_walkable': required,
        'lamp_supports': lamp_supports, 'banner_supports': banner_supports, 'display_cases': displays,
        'lava_cells': lava_cells, 'lava_safety': {'sealed_faces_per_cell': 6, 'minimum_fireproof_radius': 4, 'passage_exposure': False},
        'merchant_placeholder': {'feet': [8, 1, 12], 'spawned_entity': False},
    }
    output = ROOT / 'out/room.nbt'
    room.write(output)
    metadata = {
        'id': 'SHOP_F_DARK_IRON', 'name': '灾变暗铁商店·赤契铁窖', 'variant': 'F', 'size': SIZE,
        'entrance': [8, 1, 0], 'interior': [8, 1, 5], 'facing': 'north', 'sha256': digest(output),
        'reference': str(REFERENCE), 'reference_sha256': REFERENCE_SHA256,
        'minecraft_version': '1.20.1', 'data_version': 3465,
        'integration': {'status': 'STANDALONE_CANDIDATE_NOT_REGISTERED', 'id_is_provisional': True, 'assigned_slot': None, 'deployed': False, 'requires_coordinator_acceptance': True},
        'layout': {'main_aisle_width_blocks': 3, 'main_aisle_height_blocks': 3, 'minimum_counter_clearance_blocks': 1, 'roof': 'complete', 'lava_sources': 2, 'lava_exposed_to_walkway': False},
        'substitutions': [
            '只参考暗铁、黑曜石、金饰与红符文视觉语言；布局、拱肋、地板纹样原创，不复制 Cataclysm 素材。',
            '暗铁由原版磨制深板岩、黑石、雕纹黑石与黑曜石表达，不依赖 Cataclysm 或其他模组方块。',
            '红符文地毯转译为无碰撞高差的红陶瓦嵌地；墙面纹样由合法图案 NBT 的黑旗表达。',
            '熔岩火盆改为后墙内两只六面封闭的黑曜石玻璃炉芯；不使用开放熔岩、明火或可燃支架。',
            '精细武器、金币、天平和药瓶转译为雕纹台、镀金黑石、空酿造台、蜡烛、菌盆与封闭玻璃展柜。',
            '柜台九格宽，保留前后与两端一格净空；柱子仅在墙线上，悬灯只挂通道上方。',
            '这是陈列建筑，不生成店主、怪物或奖励，不实现交易或地牢核心行为。',
        ],
    }
    write_json(ROOT / 'out/room.json', metadata)
    write_json(ROOT / 'out/blueprint.json', blueprint)
    materials = Counter(block.name for block in room.blocks.values() if block != AIR)
    write_json(ROOT / 'out/materials.json', {'non_air_blocks': sum(materials.values()), 'counts': dict(sorted(materials.items()))})
    empty = Structure((1, 1, 1))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / 'src/main/resources/data/ftironshopqa/structures/empty.nbt')
    print(json.dumps({'status': 'BUILT', 'size': SIZE, 'records': len(room.blocks), 'block_entities': len(room.block_entities), 'sha256': digest(output)}))


if __name__ == '__main__':
    main()
