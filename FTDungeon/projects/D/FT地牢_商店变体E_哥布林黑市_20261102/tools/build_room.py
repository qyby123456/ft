import hashlib
import json
from collections import Counter
from pathlib import Path

from PIL import Image

from nbtio import AIR, Structure, Tag, compound, state, string


ROOT = Path(__file__).resolve().parents[1]
DESIGN = Path('D:/ft工程文件/image2.5/dungeon-shop-variants-20261102')
REFERENCE = 'shop-variant-E-goblin-market.png'
REFERENCE_HASH = '7eec11d6c267cb7b9a58c307ea2ae3ad5601fd8fcb34b924f172101395365746'
SIZE = (13, 9, 13)


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + chr(10), encoding='utf-8')


def inventory(kind):
    return compound(id=string('minecraft:' + kind), Items=Tag(9, [], 10))


def main():
    reference = ROOT / 'reference' / REFERENCE
    if digest(reference) != REFERENCE_HASH:
        raise ValueError('Variant E reference fingerprint mismatch')
    with Image.open(reference) as image:
        dimensions = list(image.size)
        image.load()
    write_json(ROOT / 'reference/input-fingerprints.json', {
        'selected': REFERENCE,
        'source': str(DESIGN / 'out' / REFERENCE),
        'sha256': REFERENCE_HASH,
        'size': dimensions,
        'source_styles_md_present': (DESIGN / 'STYLES.md').exists(),
        'note': 'STYLES.md was absent at intake; README/manifest/DISPATCH and the PNG were verified. No image API was called.',
    })
    room = Structure(SIZE)
    room.box((0, 0, 0), (12, 8, 12), AIR)
    features = {}
    owners = {}

    def put(position, name, group=None, entity=None, **properties):
        if group is not None:
            if position in owners and owners[position] != group:
                raise ValueError(f'Feature collision: {position}: {owners[position]} / {group}')
            owners[position] = group
            features.setdefault(group, set()).add(position)
        room.set(position, state(name, **properties), entity)

    for pos_z in range(13):
        for pos_x in range(13):
            floor = ('stone_bricks', 'cracked_stone_bricks', 'mossy_stone_bricks', 'cobblestone')[(pos_x * 7 + pos_z * 3) % 13 % 4]
            if pos_x in (0, 12) or pos_z in (0, 12):
                floor = 'stone_bricks'
            if 4 <= pos_x <= 8 and 2 <= pos_z <= 6:
                floor = 'green_terracotta' if (pos_x + pos_z * 3) % 7 in (0, 1) else 'red_terracotta'
            put((pos_x, 0, pos_z), floor)
            put((pos_x, 8, pos_z), 'mossy_stone_bricks' if (pos_x * 5 + pos_z) % 11 == 0 else 'stone_bricks')
            if pos_x in (0, 12) or pos_z in (0, 12):
                for pos_y in range(1, 8):
                    material = ('stone_bricks', 'mossy_stone_bricks', 'stone_bricks', 'cracked_stone_bricks')[(pos_x + pos_z * 3 + pos_y * 5) % 4]
                    put((pos_x, pos_y, pos_z), material)

    for pos_x, pos_z in ((0, 0), (12, 0), (0, 12), (12, 12)):
        for pos_y in range(1, 8):
            if pos_y in (1, 7):
                put((pos_x, pos_y, pos_z), 'chiseled_stone_bricks', 'pillars')
            else:
                put((pos_x, pos_y, pos_z), 'stripped_spruce_log', 'pillars', axis='y')

    for pos_x in range(5, 8):
        for pos_y in range(1, 4):
            room.set((pos_x, pos_y, 0), AIR)
        put((pos_x, 4, 0), 'chiseled_stone_bricks')

    for pos_z in (3, 7, 11):
        for pos_x in range(13):
            put((pos_x, 7, pos_z), 'dark_oak_log', 'beams', axis='x')

    for pos_x in range(3, 10):
        put((pos_x, 1, 8), 'spruce_planks' if pos_x % 2 else 'dark_oak_planks', 'counter')
    put((3, 2, 8), 'flower_pot', 'counter_decor')
    put((5, 2, 8), 'yellow_candle', 'counter_decor', candles=3, lit=False, waterlogged=False)
    put((7, 2, 8), 'potted_fern', 'counter_decor')
    put((9, 2, 8), 'brewing_stand', 'counter_decor', entity=inventory('brewing_stand'), has_bottle_0=False, has_bottle_1=False, has_bottle_2=False)

    for pos_x in range(3, 10):
        base = 'barrel' if pos_x in (3, 7, 9) else 'bookshelf' if pos_x == 5 else 'dark_oak_planks'
        if base == 'barrel':
            put((pos_x, 1, 11), base, 'shelves', entity=inventory('barrel'), facing='north', open=False)
        else:
            put((pos_x, 1, 11), base, 'shelves')
        put((pos_x, 3, 11), 'spruce_slab', 'shelves', type='top', waterlogged=False)
    put((3, 2, 11), 'flower_pot', 'shelf_decor')
    put((4, 2, 11), 'potted_fern', 'shelf_decor')
    put((5, 2, 11), 'skeleton_skull', 'shelf_decor', entity=compound(id=string('minecraft:skull')), rotation=8)
    put((6, 2, 11), 'flower_pot', 'shelf_decor')
    put((7, 2, 11), 'yellow_candle', 'shelf_decor', candles=2, lit=False, waterlogged=False)
    put((8, 2, 11), 'potted_fern', 'shelf_decor')
    put((9, 2, 11), 'flower_pot', 'shelf_decor')
    put((4, 4, 11), 'red_candle', 'shelf_decor', candles=2, lit=False, waterlogged=False)
    put((6, 4, 11), 'flower_pot', 'shelf_decor')
    put((8, 4, 11), 'yellow_candle', 'shelf_decor', candles=3, lit=False, waterlogged=False)

    for pos_z, name in ((4, 'crafting_table'), (5, 'loom'), (6, 'bookshelf')):
        if name == 'loom':
            put((1, 1, pos_z), name, 'shelves', facing='east')
        else:
            put((1, 1, pos_z), name, 'shelves')
        put((1, 3, pos_z), 'spruce_slab', 'shelves', type='top', waterlogged=False)
    put((1, 2, 4), 'flower_pot', 'shelf_decor')
    put((1, 2, 5), 'yellow_candle', 'shelf_decor', candles=3, lit=False, waterlogged=False)
    put((1, 2, 6), 'potted_fern', 'shelf_decor')
    for pos_z in (4, 5, 6):
        put((11, 1, pos_z), 'spruce_planks', 'shelves')
    put((11, 2, 4), 'skeleton_skull', 'shelf_decor', entity=compound(id=string('minecraft:skull')), rotation=4)
    for pos_y in (2, 3):
        for pos_z in (5, 6):
            put((11, pos_y, pos_z), 'iron_bars', 'cage', east=True, west=False, north=pos_z == 6, south=pos_z == 5, waterlogged=False)
    for pos_z in (5, 6):
        put((11, 4, pos_z), 'spruce_slab', 'cage', type='bottom', waterlogged=False)

    stash = ((1, 1, 1), (1, 2, 1), (1, 1, 2), (1, 1, 10), (11, 1, 1), (11, 2, 1), (11, 1, 2), (11, 1, 10))
    for position in stash:
        put(position, 'barrel', 'barrels', entity=inventory('barrel'), facing='up' if position[2] == 10 else 'north', open=False)
    chest_positions = ((2, 1, 2), (10, 1, 2), (2, 1, 11), (10, 1, 11))
    for position in chest_positions:
        put(position, 'chest', 'chests', entity=inventory('chest'), facing='north', type='single', waterlogged=False)
    put((1, 2, 10), 'yellow_candle', 'stash_decor', candles=4, lit=False, waterlogged=False)
    put((11, 2, 10), 'flower_pot', 'stash_decor')

    for pos_x in (1, 11):
        for pos_y in range(1, 6):
            put((pos_x, pos_y, 11), 'spruce_log', 'awning_posts', axis='y')
    for pos_z in (8, 11):
        for pos_x in range(13):
            put((pos_x, 6, pos_z), 'dark_oak_log', 'awning_frame', axis='x')
    for pos_x in (2, 10):
        for pos_z in (9, 10):
            put((pos_x, 6, pos_z), 'dark_oak_log', 'awning_frame', axis='z')
    for pos_z in range(8, 12):
        for pos_x in range(2, 11):
            if (pos_x, pos_z) in ((2, 10), (10, 10)):
                continue
            color = 'red_wool' if pos_x <= 5 else 'green_wool'
            if (pos_x, pos_z) in ((4, 10), (8, 9), (9, 11)):
                color = 'brown_wool'
            put((pos_x, 5, pos_z), color, 'awning_cloth')
    for pos_x, color in ((3, 'red_wool'), (6, 'red_wool'), (9, 'green_wool')):
        put((pos_x, 4, 8), color, 'awning_cloth')

    lamps = []
    for pos_x in (3, 9):
        put((pos_x, 4, 7), 'lantern', 'lamps', hanging=True, waterlogged=False)
        for pos_y in (5, 6):
            put((pos_x, pos_y, 7), 'chain', 'lamp_chains', axis='y', waterlogged=False)
        lamps.append({'lantern': [pos_x, 4, 7], 'chains': [[pos_x, 5, 7], [pos_x, 6, 7]], 'chain': [pos_x, 5, 7], 'beam': [pos_x, 7, 7]})
    pots = []
    for pos_x in (2, 10):
        put((pos_x, 3, 10), 'cauldron', 'suspended_pots')
        for pos_y in (4, 5):
            put((pos_x, pos_y, 10), 'chain', 'pot_chains', axis='y', waterlogged=False)
        pots.append({'pot': [pos_x, 3, 10], 'chains': [[pos_x, 4, 10], [pos_x, 5, 10]], 'beam': [pos_x, 6, 10], 'support': 'Two contiguous vertical chains fixed to the wall-spanning awning frame; no fire, fluids or pot inventory.'})

    ring_feet = {(pos_x, 1, pos_z) for pos_x in range(2, 11) for pos_z in range(7, 10) if pos_z != 8 or pos_x in (2, 10)}
    aisle = [(pos_x, pos_y, pos_z) for pos_x in range(5, 8) for pos_y in range(1, 4) for pos_z in range(8)]
    blueprint = {
        'size': SIZE,
        'features': {name: sorted(positions) for name, positions in sorted(features.items())},
        'counter_clearance': sorted((pos_x, pos_y, pos_z) for pos_x, _, pos_z in ring_feet for pos_y in (1, 2)),
        'entrance_to_counter_aisle': sorted(aisle),
        'lamp_supports': lamps,
        'suspended_pot_supports': pots,
        'required_walkable': sorted(ring_feet | {(6, 1, 4), (2, 1, 3), (10, 1, 3), (2, 1, 10), (10, 1, 10)}),
        'chest_lid_clearance': [[pos_x, 2, pos_z] for pos_x, _, pos_z in chest_positions],
        'empty_cage': {'wall_attached': True, 'footprint': [[11, 1, 5], [11, 1, 6]], 'entities': 0},
    }
    output = ROOT / 'out/room.nbt'
    room.write(output)
    write_json(ROOT / 'out/blueprint.json', blueprint)
    write_json(ROOT / 'out/materials.json', dict(sorted(Counter(block.name for block in room.blocks.values() if block != AIR).items())))
    write_json(ROOT / 'out/room.json', {
        'id': 'SHOP_E_GOBLIN_MARKET',
        'name': '哥布林黑市·拼棚赃物铺',
        'size': SIZE,
        'entrance': [6, 1, 0],
        'interior': [6, 1, 4],
        'facing': 'north',
        'sha256': digest(output),
        'reference': str(DESIGN / 'out' / REFERENCE),
        'reference_sha256': REFERENCE_HASH,
        'minecraft_version': '1.20.1',
        'data_version': 3465,
        'integration': {'status': 'STANDALONE_CANDIDATE_NOT_REGISTERED', 'id_is_provisional': True, 'assigned_slot': None, 'deployed': False, 'requires_coordinator_acceptance': True},
        'layout': {'clear_internal_floor_size': [11, 11], 'counter_size': [7, 1], 'minimum_counter_clearance_blocks': 1, 'main_aisle_width_blocks': 3, 'main_aisle_height_blocks': 3, 'roof': 'complete', 'wall_corner_pillars': 4, 'awning': 'Wall-spanning wooden frame and two wall-side posts, red/green/brown wool patchwork.', 'suspended_pots': 2},
        'substitutions': ['地毯改为地板内嵌红绿陶瓦，入口到柜台始终三格净宽、三格净高。', '拼布用红/绿/棕羊毛落块，木框横跨墙线；垂布仅在柜台上方，不侵入走道。', '金币改为无库存的黄色蜡烛束与陶瓦配色，不设置金块奖励或掉落物。', '两只空炼药锅由连续双链固定到雨棚木框；不点火、不装液体、不生成物品。', '赃物使用空桶、空箱、空酿造台、花盆、头骨和贴墙空铁笼，无战利品表。', '哥布林仅是风格名；不添加哥布林、店主、笼中生物或其他实体，不提供交易、波次或奖励逻辑。'],
    })
    empty = Structure((1, 1, 1))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / 'src/main/resources/data/ftgoblinqa/structures/empty.nbt')
    print(json.dumps({'status': 'BUILT', 'id': 'SHOP_E_GOBLIN_MARKET', 'size': SIZE, 'records': len(room.blocks), 'block_entities': len(room.block_entities), 'sha256': digest(output)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
