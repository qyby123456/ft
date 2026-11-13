import hashlib
from collections import Counter
from pathlib import Path

from structure_io import load_nbt, write_json, write_nbt


ROOT = Path(__file__).resolve().parents[1]
DELIVERY = ROOT / 'delivery/M7'
SIZE = (19, 14, 19)
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-design-batch3-20261101/out/M7-cataclysm-sacrifice-crypt-hero.png')
REFERENCE_SHA256 = 'c9b49119f192c80c1d8aca9299efc8787ec1d7cafdb342745c78fea180f057be'
BLOCKS = {}
BLOCK_ENTITIES = {}
FEATURES = {'columns': [], 'ribs': [], 'chains': [], 'lanterns': [], 'braziers': [], 'lava': [], 'candles': [], 'skulls': [], 'banners': []}


def put(position, name, properties=None, nbt=None):
    assert all(0 <= coordinate < limit for coordinate, limit in zip(position, SIZE)), position
    state = {'Name': 'minecraft:' + name}
    if properties:
        state['Properties'] = {name: str(value).lower() for name, value in properties.items()}
    BLOCKS[position] = state
    BLOCK_ENTITIES.pop(position, None)
    if nbt:
        BLOCK_ENTITIES[position] = nbt


def fill(start, end, name, properties=None):
    for block_x in range(start[0], end[0] + 1):
        for block_y in range(start[1], end[1] + 1):
            for block_z in range(start[2], end[2] + 1):
                put((block_x, block_y, block_z), name, properties)


def candle(position, count=3):
    put(position, 'black_candle', {'candles': count, 'lit': True, 'waterlogged': False})
    FEATURES['candles'].append(position)


def build():
    assert hashlib.sha256(REFERENCE.read_bytes()).hexdigest() == REFERENCE_SHA256
    fill((0, 0, 0), (18, 1, 18), 'polished_basalt', {'axis': 'y'})
    for block_x in range(19):
        for block_z in range(19):
            surface = 'cracked_polished_blackstone_bricks' if (block_x * 13 + block_z * 7) % 11 < 2 else 'polished_blackstone_bricks'
            put((block_x, 2, block_z), surface)
    for block_y in range(3, 13):
        for offset in range(19):
            material = 'gilded_blackstone' if block_y in (4, 10) and offset % 6 == 3 else 'polished_blackstone_bricks'
            for position in ((offset, block_y, 0), (offset, block_y, 18), (0, block_y, offset), (18, block_y, offset)):
                put(position, material)
    fill((0, 13, 0), (18, 13, 18), 'deepslate_tiles')
    for block_x in (2, 16):
        for block_z in (4, 10, 16):
            fill((block_x, 3, block_z), (block_x, 12, block_z), 'polished_basalt', {'axis': 'y'})
            for block_y in (3, 9, 12):
                put((block_x, block_y, block_z), 'chiseled_polished_blackstone')
            for block_y in (4, 10):
                put((block_x, block_y, block_z), 'gold_block')
            FEATURES['columns'].append([block_x, 3, block_z])
    for block_z in (4, 10, 16):
        for block_x, low, high in ((3, 9, 10), (4, 10, 11), (5, 11, 12), (13, 11, 12), (14, 10, 11), (15, 9, 10)):
            fill((block_x, low, block_z), (block_x, high, block_z), 'polished_blackstone_bricks')
        fill((6, 12, block_z), (12, 12, block_z), 'polished_blackstone_bricks')
        put((9, 12, block_z), 'gilded_blackstone')
        FEATURES['ribs'].append({'z': block_z, 'foundation_columns': [[2, 3, block_z], [16, 3, block_z]], 'crown': [9, 12, block_z]})
    for block_x in (2, 16):
        for block_z in list(range(5, 10)) + list(range(11, 16)):
            position = (block_x, 10, block_z)
            put(position, 'chain', {'axis': 'z', 'waterlogged': False})
            FEATURES['chains'].append(position)
    for block_x in (5, 13):
        for block_z in (5, 14):
            for block_y in range(9, 13):
                position = (block_x, block_y, block_z)
                put(position, 'chain', {'axis': 'y', 'waterlogged': False})
                FEATURES['chains'].append(position)
            position = (block_x, 8, block_z)
            put(position, 'lantern', {'hanging': True, 'waterlogged': False})
            FEATURES['lanterns'].append(position)
    ring = {(block_x, block_z) for block_x in range(5, 14) for block_z in range(6, 15) if block_x in (5, 13) or block_z in (6, 14)}
    for block_x, block_z in sorted(ring):
        put((block_x, 1, block_z), 'lava', {'level': 0})
        put((block_x, 2, block_z), 'orange_stained_glass')
        FEATURES['lava'].append([block_x, 1, block_z])
    for block_x, block_z in ((5, 6), (13, 6), (5, 14), (13, 14)):
        put((block_x, 2, block_z), 'gilded_blackstone')
    fill((6, 3, 7), (12, 3, 13), 'polished_blackstone_bricks')
    fill((7, 4, 8), (11, 4, 12), 'obsidian')
    fill((8, 4, 9), (10, 4, 11), 'redstone_block')
    fill((8, 5, 9), (10, 5, 11), 'red_stained_glass')
    for block_x in range(7, 12):
        for block_z in range(8, 13):
            if block_x in (7, 11) or block_z in (8, 12):
                put((block_x, 5, block_z), 'chiseled_polished_blackstone')
    for block_x in (7, 11):
        for block_z in (8, 12):
            put((block_x, 5, block_z), 'gold_block')
            candle((block_x, 6, block_z), 3)
    for block_x in (8, 9, 10):
        put((block_x, 3, 6), 'polished_blackstone_brick_stairs', {'facing': 'south', 'half': 'bottom', 'shape': 'straight', 'waterlogged': False})
        put((block_x, 4, 7), 'polished_blackstone_brick_stairs', {'facing': 'south', 'half': 'bottom', 'shape': 'straight', 'waterlogged': False})
    FEATURES['sacrifice_basin'] = {'base': [6, 3, 7], 'rim_min': [7, 5, 8], 'rim_max': [11, 5, 12], 'surface': 'red_stained_glass', 'underlay': 'redstone_block', 'fluid': False, 'explanation': 'Sealed red glass over redstone translates the conceptual blood pool; no custom or dangerous blood fluid.'}
    for block_x in (4, 14):
        fill((block_x - 1, 3, 15), (block_x + 1, 3, 17), 'polished_blackstone_bricks')
        put((block_x, 3, 16), 'gilded_blackstone')
        put((block_x, 4, 16), 'campfire', {'facing': 'north', 'lit': True, 'signal_fire': False, 'waterlogged': False}, {'id': 'minecraft:campfire', 'Items': []})
        for delta_x, delta_z in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            connections = {'up': True, 'north': 'none', 'east': 'none', 'south': 'none', 'west': 'none', 'waterlogged': False}
            if delta_z == 1:
                connections['south'] = 'low'
            if block_x == 4 and delta_x == -1:
                connections['west'] = 'low'
            if block_x == 14 and delta_x == 1:
                connections['east'] = 'low'
            put((block_x + delta_x, 4, 16 + delta_z), 'polished_blackstone_brick_wall', connections)
        FEATURES['braziers'].append([block_x, 4, 16])
    for block_x in (1, 17):
        facing = 'east' if block_x == 1 else 'west'
        for block_z in (7, 13):
            position = (block_x, 8, block_z)
            put(position, 'red_wall_banner', {'facing': facing}, {'id': 'minecraft:banner', 'Patterns': [{'Color': 15, 'Pattern': 'bo'}, {'Color': 15, 'Pattern': 'cs'}, {'Color': 4, 'Pattern': 'mr'}]})
            FEATURES['banners'].append(position)
    for block_x in (5, 13):
        position = (block_x, 9, 17)
        put(position, 'red_wall_banner', {'facing': 'north'}, {'id': 'minecraft:banner', 'Patterns': [{'Color': 15, 'Pattern': 'bo'}, {'Color': 4, 'Pattern': 'mr'}]})
        FEATURES['banners'].append(position)
    for block_x in (6, 12):
        fill((block_x, 3, 17), (block_x, 8, 17), 'obsidian')
        put((block_x, 8, 17), 'gold_block')
    for block_x, block_y in ((7, 9), (8, 10), (9, 11), (10, 10), (11, 9)):
        put((block_x, block_y, 17), 'gilded_blackstone')
        if block_x != 9:
            put((block_x, block_y + 1, 17), 'polished_blackstone_bricks')
    for block_x in range(7, 12):
        for block_y in range(4, 9):
            put((block_x, block_y, 18), 'redstone_block' if block_x == 9 or block_y == 6 else 'crying_obsidian')
    for block_x in (8, 9, 10):
        for block_y in range(4, 9):
            put((block_x, block_y, 17), 'iron_bars', {'north': False, 'south': True, 'east': block_x < 10, 'west': block_x > 8, 'waterlogged': False})
    for block_x, block_z in ((3, 2), (15, 2), (3, 12), (15, 12)):
        put((block_x, 3, block_z), 'chiseled_polished_blackstone')
        position = (block_x, 4, block_z)
        put(position, 'skeleton_skull', {'rotation': 8}, {'id': 'minecraft:skull'})
        FEATURES['skulls'].append(position)
    for block_x, block_z in ((4, 2), (14, 2), (1, 5), (17, 5), (1, 11), (17, 11)):
        candle((block_x, 3, block_z), 2)
    for block_x in (7, 11):
        put((block_x, 3, 0), 'gilded_blackstone')
        fill((block_x, 4, 0), (block_x, 6, 0), 'chiseled_polished_blackstone')
    fill((8, 3, 0), (10, 5, 0), 'air')
    fill((7, 6, 0), (11, 6, 0), 'gilded_blackstone')
    for block_x in range(8, 11):
        put((block_x, 2, 0), 'polished_blackstone')
    for block_y in range(SIZE[1]):
        for block_x in range(SIZE[0]):
            for block_z in range(SIZE[2]):
                if (block_x, block_y, block_z) not in BLOCKS:
                    put((block_x, block_y, block_z), 'air')


def package():
    palette = []
    palette_indices = {}
    records = []
    for position, state in sorted(BLOCKS.items(), key=lambda item: (item[0][1], item[0][2], item[0][0])):
        key = (state['Name'], tuple(sorted(state.get('Properties', {}).items())))
        if key not in palette_indices:
            palette_indices[key] = len(palette)
            palette.append(state)
        record = {'pos': list(position), 'state': palette_indices[key]}
        if position in BLOCK_ENTITIES:
            record['nbt'] = BLOCK_ENTITIES[position]
        records.append(record)
    template = {'DataVersion': 3465, 'size': list(SIZE), 'palette': palette, 'blocks': records, 'entities': []}
    write_nbt(DELIVERY / 'room.nbt', template)
    assert load_nbt(DELIVERY / 'room.nbt') == template
    structure_hash = hashlib.sha256((DELIVERY / 'room.nbt').read_bytes()).hexdigest()
    metadata = {'id': 'M7', 'name': '暗铁献祭地窖', 'size': list(SIZE), 'entrance': [9, 3, 0], 'interior': [9, 3, 4], 'facing': 'north', 'sha256': structure_hash, 'reference': REFERENCE.as_posix(), 'reference_sha256': REFERENCE_SHA256, 'template_id': 'ftdungeonmobs:image25/m7', 'design_size': [13, 8, 13], 'adaptation': 'Expanded to 19×14×19 for complete gothic roof, supported ornaments, protected flame bowls, and dry circumnavigation. Original geometry; Cataclysm visual vocabulary only.', 'deployment': 'NOT_DEPLOYED'}
    write_json(DELIVERY / 'room.json', metadata)
    write_json(ROOT / 'qa/features.json', FEATURES)
    write_json(ROOT / 'qa/materials.json', dict(sorted(Counter(state['Name'] for state in BLOCKS.values() if state['Name'] != 'minecraft:air').items())))
    write_nbt(ROOT / 'src/main/resources/data/m7verification/structures/empty.nbt', {'DataVersion': 3465, 'size': [1, 1, 1], 'palette': [{'Name': 'minecraft:air'}], 'blocks': [{'pos': [0, 0, 0], 'state': 0}], 'entities': []})
    print(f'M7_BUILD_PASS size={SIZE} records={len(records)} palette={len(palette)} sha256={structure_hash}')


if __name__ == '__main__':
    build()
    package()
