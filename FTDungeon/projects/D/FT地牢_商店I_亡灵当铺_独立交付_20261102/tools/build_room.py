import hashlib
import json
from collections import Counter
from pathlib import Path

from PIL import Image

from nbtio import AIR, Structure, Tag, compound, integer, state, string


ROOT = Path(__file__).resolve().parents[1]
SIZE = (13, 8, 11)
DESIGN = Path('D:/ft工程文件/image2.5/dungeon-shop-variants-thread3-20261102')
REFERENCE = 'shop-variant-I-undead-pawn-shop.png'
REFERENCE_SHA = '0476c4fbf32422720ff0a91fd80ff8baa64e2514ae3f01d28499cfb67b29d6e9'


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def container(kind):
    return compound(id=string('minecraft:' + kind), Items=Tag(9, [], 10))


def main():
    reference = ROOT / 'reference' / REFERENCE
    if digest(reference) != REFERENCE_SHA:
        raise ValueError('Reference fingerprint mismatch')
    with Image.open(reference) as image:
        image.load()
        if image.size != (1536, 1024):
            raise ValueError('Unexpected reference dimensions')
    room = Structure(SIZE)
    room.box((0, 0, 0), (12, 7, 10), AIR)
    features = {}
    claimed = {}
    supports = []

    def put(position, name, feature=None, entity=None, **properties):
        if feature:
            if position in claimed and claimed[position] != feature:
                raise ValueError(f'Furniture overlap at {position}: {claimed[position]} / {feature}')
            claimed[position] = feature
            features.setdefault(feature, set()).add(position)
        room.set(position, state(name, **properties), entity)

    def supported(position, anchor, kind):
        supports.append({'position': list(position), 'anchor': list(anchor), 'kind': kind})

    for pos_x in range(13):
        for pos_z in range(11):
            floor = ('polished_deepslate', 'stone_bricks', 'cracked_stone_bricks', 'deepslate_bricks')[(pos_x * 5 + pos_z * 7) % 4]
            if pos_x in (0, 12) or pos_z in (0, 10):
                floor = 'polished_blackstone_bricks'
            elif 3 <= pos_x <= 9 and 5 <= pos_z <= 7:
                floor = 'red_terracotta' if pos_x in (3, 9) or pos_z in (5, 7) else 'gray_terracotta'
            elif 5 <= pos_x <= 7 and pos_z < 5:
                floor = 'polished_deepslate'
            put((pos_x, 0, pos_z), floor)
            put((pos_x, 7, pos_z), 'deepslate_tiles')
            if pos_x in (0, 12) or pos_z in (0, 10):
                for pos_y in range(1, 7):
                    wall = 'deepslate_bricks' if pos_y in (1, 2) else 'calcite'
                    if pos_y == 6:
                        put((pos_x, pos_y, pos_z), 'dark_oak_log', axis='x' if pos_z in (0, 10) else 'z')
                    else:
                        put((pos_x, pos_y, pos_z), wall)

    for pos_x, pos_z in ((0, 0), (12, 0), (0, 10), (12, 10)):
        for pos_y in range(1, 7):
            material = 'polished_blackstone_bricks' if pos_y in (1, 3, 6) else 'smooth_quartz'
            put((pos_x, pos_y, pos_z), material, 'pillars')
    for pos_x in range(5, 8):
        for pos_y in range(1, 4):
            room.set((pos_x, pos_y, 0), AIR)
        put((pos_x, 4, 0), 'chiseled_quartz_block')
    for pos_x in (4, 8):
        for pos_y in range(1, 4):
            put((pos_x, pos_y, 0), 'polished_blackstone_bricks')
    for pos_z in (3, 8):
        for pos_x in range(13):
            put((pos_x, 6, pos_z), 'dark_oak_log', 'beams', axis='x')

    for pos_x in range(4, 9):
        put((pos_x, 1, 6), 'dark_oak_planks', 'counter')
    put((4, 2, 6), 'yellow_candle', 'counter_decor', candles=3, lit=True, waterlogged=False)
    put((5, 2, 6), 'skeleton_skull', 'counter_decor', entity=compound(id=string('minecraft:skull')), rotation=0)
    put((6, 2, 6), 'chest', 'counter_decor', entity=container('chest'), facing='north', type='single', waterlogged=False)
    put((7, 2, 6), 'amethyst_cluster', 'counter_decor', facing='up', waterlogged=False)
    put((8, 2, 6), 'soul_lantern', 'counter_decor', hanging=False, waterlogged=False)
    for pos_x in range(4, 9):
        supported((pos_x, 2, 6), (pos_x, 1, 6), 'counter_top')

    for pos_x in range(1, 4):
        for pos_z in range(2, 5):
            put((pos_x, 1, pos_z), 'polished_blackstone_bricks', 'cage_base')
            put((pos_x, 4, pos_z), 'iron_trapdoor', 'cage_roof', facing='north', half='bottom', open=False, powered=False, waterlogged=False)
            if pos_x in (1, 3) or pos_z in (2, 4):
                for pos_y in (2, 3):
                    put((pos_x, pos_y, pos_z), 'iron_bars', 'cage_bars', north=False, south=False, east=False, west=False, waterlogged=False)
    put((2, 2, 3), 'soul_lantern', 'cage_glow', hanging=False, waterlogged=False)
    supported((2, 2, 3), (2, 1, 3), 'cage_light')

    for pos_x in range(9, 12):
        for pos_z in range(2, 5):
            put((pos_x, 1, pos_z), 'gilded_blackstone' if (pos_x, pos_z) == (10, 3) else 'dark_oak_planks', 'display_base')
            put((pos_x, 4, pos_z), 'glass', 'display_roof')
            if pos_x in (9, 11) or pos_z in (2, 4):
                for pos_y in (2, 3):
                    put((pos_x, pos_y, pos_z), 'glass_pane', 'display_glass', north=False, south=False, east=False, west=False, waterlogged=False)
    put((10, 2, 3), 'amethyst_cluster', 'relic', facing='up', waterlogged=False)
    supported((10, 2, 3), (10, 1, 3), 'reliquary')

    for pos_x in range(3, 10):
        put((pos_x, 1, 9), 'bookshelf', 'shelves')
        put((pos_x, 2, 9), 'dark_oak_planks', 'shelves')
        put((pos_x, 4, 9), 'dark_oak_planks', 'shelves')
    for pos_x in (3, 7, 9):
        put((pos_x, 3, 9), 'flower_pot', 'shelf_decor')
        supported((pos_x, 3, 9), (pos_x, 2, 9), 'urn')
    for pos_x in (4, 8):
        put((pos_x, 3, 9), 'skeleton_skull', 'shelf_decor', entity=compound(id=string('minecraft:skull')), rotation=0)
        supported((pos_x, 3, 9), (pos_x, 2, 9), 'bone_trophy')
    put((5, 3, 9), 'candle', 'shelf_decor', candles=3, lit=True, waterlogged=False)
    supported((5, 3, 9), (5, 2, 9), 'shelf_candle')
    put((6, 3, 9), 'bone_block', 'shelf_decor', axis='y')
    supported((6, 3, 9), (6, 2, 9), 'bone_relic')

    for pos_x, pos_z in ((1, 1), (11, 1), (1, 9), (11, 9)):
        put((pos_x, 1, pos_z), 'barrel', 'barrels', entity=container('barrel'), facing='up', open=False)
        put((pos_x, 2, pos_z), 'bone_block', 'bones', axis='y')
        put((pos_x, 3, pos_z), 'skeleton_skull', 'bones', entity=compound(id=string('minecraft:skull')), rotation=0)
        supported((pos_x, 2, pos_z), (pos_x, 1, pos_z), 'bone_stack')
        supported((pos_x, 3, pos_z), (pos_x, 2, pos_z), 'bone_skull')

    for pos_x in (2, 10):
        put((pos_x, 1, 9), 'polished_blackstone_bricks', 'brazier_base')
        fire_data = container('campfire')
        fire_data.value.update(CookingTimes=Tag(11, [0, 0, 0, 0]), CookingTotalTimes=Tag(11, [0, 0, 0, 0]))
        put((pos_x, 2, 9), 'soul_campfire', 'braziers', entity=fire_data, facing='north', lit=True, signal_fire=False, waterlogged=False)
        put((pos_x, 1, 8), 'polished_blackstone_bricks', 'brazier_guard')
        for pos_y in (2, 3):
            put((pos_x, pos_y, 8), 'iron_bars', 'brazier_guard', north=False, south=False, east=False, west=False, waterlogged=False)
        supported((pos_x, 2, 9), (pos_x, 1, 9), 'nonspreading_soul_brazier')

    put((1, 1, 6), 'smithing_table', 'weapon_rack')
    put((1, 2, 6), 'grindstone', 'weapon_rack', face='wall', facing='east')
    put((1, 3, 6), 'tripwire_hook', 'weapon_rack', facing='east', attached=False, powered=False)
    put((1, 1, 7), 'anvil', 'weapon_rack', facing='north')
    put((1, 2, 7), 'iron_bars', 'weapon_rack', north=False, south=False, east=False, west=False, waterlogged=False)
    put((1, 3, 7), 'iron_trapdoor', 'weapon_rack', facing='east', half='bottom', open=True, powered=False, waterlogged=False)
    for position in ((1, 2, 6), (1, 3, 6), (1, 2, 7), (1, 3, 7)):
        supported(position, (0, position[1], position[2]), 'wall_weapon_rack')

    patterns = Tag(9, [compound(Pattern=string('bs'), Color=integer(15)), compound(Pattern=string('gra'), Color=integer(15))], 10)
    put((11, 5, 6), 'red_wall_banner', 'banner', entity=compound(id=string('minecraft:banner'), Patterns=patterns), facing='west')
    supported((11, 5, 6), (12, 5, 6), 'dark_red_wall_banner')
    messages = ['亡灵当铺', 'UNDEAD PAWN', '圣物陈列 / 空箱', '无交易 / 无奖励']
    text = compound(messages=Tag(9, [string(json.dumps({'text': message}, ensure_ascii=False)) for message in messages], 8), color=string('yellow'), has_glowing_text=Tag(1, 0))
    back = compound(messages=Tag(9, [string('{"text":""}') for _ in range(4)], 8), color=string('black'), has_glowing_text=Tag(1, 0))
    put((6, 5, 9), 'dark_oak_wall_sign', 'sign', entity=compound(id=string('minecraft:sign'), front_text=text, back_text=back, is_waxed=Tag(1, 1)), facing='north', waterlogged=False)
    supported((6, 5, 9), (6, 5, 10), 'wall_sign')

    lamps = []
    for pos_x, kind in ((5, 'lantern'), (7, 'soul_lantern')):
        put((pos_x, 5, 3), 'chain', 'chains', axis='y', waterlogged=False)
        put((pos_x, 4, 3), kind, 'lanterns', hanging=True, waterlogged=False)
        lamps.append({'lantern': [pos_x, 4, 3], 'chain': [pos_x, 5, 3], 'beam': [pos_x, 6, 3], 'block': 'minecraft:' + kind})
        supported((pos_x, 4, 3), (pos_x, 5, 3), 'hanging_lantern')
        supported((pos_x, 5, 3), (pos_x, 6, 3), 'ceiling_chain')

    full_materials = {'calcite', 'smooth_quartz', 'deepslate_bricks', 'polished_blackstone_bricks', 'dark_oak_planks', 'bone_block', 'bookshelf', 'glass'}
    offsets = {'north': (0, 0, -1), 'south': (0, 0, 1), 'west': (-1, 0, 0), 'east': (1, 0, 0)}
    for position, block in list(room.blocks.items()):
        name = block.name.removeprefix('minecraft:')
        if name not in ('iron_bars', 'glass_pane'):
            continue
        connections = {}
        for direction, offset in offsets.items():
            neighbor = room.get(tuple(position[index] + offset[index] for index in range(3))).name.removeprefix('minecraft:')
            connections[direction] = neighbor == name or neighbor in full_materials
        room.set(position, state(name, **connections, waterlogged=False))

    target = ROOT / 'out/room.nbt'
    room.write(target)
    ring = [[pos_x, pos_y, pos_z] for pos_x in range(3, 10) for pos_z in range(5, 8) if pos_x in (3, 9) or pos_z in (5, 7) for pos_y in (1, 2)]
    aisle = [[pos_x, pos_y, pos_z] for pos_x in range(5, 8) for pos_z in range(6) for pos_y in range(1, 4)]
    blueprint = {
        'size': list(SIZE),
        'features': {name: [list(position) for position in sorted(positions)] for name, positions in features.items()},
        'counter_clearance': ring, 'entrance_to_counter_aisle': aisle,
        'required_walkable': [[6, 1, 3], [6, 1, 5], [3, 1, 6], [9, 1, 6], [6, 1, 7], [6, 1, 8], [4, 1, 4], [8, 1, 4]],
        'lamp_supports': lamps, 'supported_decor': supports,
        'brazier_front_guards': [[2, 2, 8], [10, 2, 8]],
    }
    write_json(ROOT / 'out/blueprint.json', blueprint)
    metadata = {
        'id': 'SHOP_I_UNDEAD_PAWN', 'name': '亡灵当铺', 'variant': 'I',
        'size': list(SIZE), 'entrance': [6, 1, 0], 'interior': [6, 1, 3], 'facing': 'north',
        'sha256': digest(target), 'reference': str(DESIGN / 'out' / REFERENCE), 'reference_sha256': REFERENCE_SHA,
        'minecraft_version': '1.20.1', 'data_version': 3465,
        'integration': {'status': 'STANDALONE_CANDIDATE_NOT_REGISTERED', 'id_is_provisional': True, 'assigned_slot': None, 'deployed': False, 'requires_coordinator_acceptance': True},
        'layout': {'clear_internal_floor_size': [11, 9], 'counter_size': [5, 1], 'minimum_counter_clearance_blocks': 1, 'main_aisle_width_blocks': 3, 'main_aisle_height_blocks': 3, 'roof': 'complete'},
        'substitutions': [
            '概念图约11×9格转为11×9格室内净地面，外尺寸13×8×11；四柱嵌入外壳墙角，不穿柜台或展柜。',
            '天平、钱币、钥匙与卷轴等小件转译为黄蜡烛、紫晶簇、头骨、书架和空圣物箱；无物品实体与奖励库存。',
            '武器架为锻造台、砂轮、铁砧、绊线钩、铁栏刀架及竖直铁活板门盾饰，不新增展示实体或可领取武器。',
            '灵魂火盆使用不蔓延的原版灵魂营火，石基、背墙和侧挡、前方两格铁栏隔离；不是裸露灵魂火。',
            '暗红旗帜使用红色墙旗与黑色条纹、渐层；裂口与艺术化蛛网未仿造为悬空碎块。',
            '地毯压入地板为红/灰陶瓦；最终结构有完整屋顶。预览剖切不改变交付NBT。',
            '不生成店主、怪物、交易、战利品表、物品实体或刷怪逻辑。',
        ],
    }
    write_json(ROOT / 'out/room.json', metadata)
    write_json(ROOT / 'out/materials.json', dict(sorted(Counter(block.name for block in room.blocks.values() if block != AIR).items())))
    write_json(ROOT / 'reference/input-fingerprints.json', {'selected': REFERENCE, 'source': metadata['reference'], 'sha256': REFERENCE_SHA, 'size': [1536, 1024], 'kind': 'AI concept only; not gameplay evidence'})
    empty = Structure((1, 1, 1))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / 'src/main/resources/data/ftshopqa/structures/empty.nbt')
    print(json.dumps({'status': 'BUILT', 'records': len(room.blocks), 'size': SIZE, 'sha256': digest(target)}))


if __name__ == '__main__':
    main()
