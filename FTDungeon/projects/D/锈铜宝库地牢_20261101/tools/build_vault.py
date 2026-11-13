import collections
import gzip
import itertools
import json
import struct
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT / "out/datapack"
NAMESPACE = "ftcoppervault"
SIZE = (33, 17, 37)
BLOCKS = {}
ENTITIES = {}
ROOF = set()


def put(block_x, block_y, block_z, name, **properties):
    position = (block_x, block_y, block_z)
    BLOCKS[position] = {'Name': 'minecraft:' + name}
    if properties:
        BLOCKS[position]['Properties'] = {key: str(value).lower() for key, value in properties.items()}
    ENTITIES.pop(position, None)


def box(minimum, maximum, name, **properties):
    for position in itertools.product(*(range(minimum[axis], maximum[axis] + 1) for axis in range(3))):
        put(*position, name, **properties)


def noise(block_x, block_y, block_z):
    return abs(block_x * 73856093 ^ block_y * 19349663 ^ block_z * 83492791) % 100


def inside(block_x, block_z, inset=0):
    delta_x, delta_z = abs(block_x - 16), abs(block_z - 20)
    return max(delta_x, delta_z) <= 16 - inset and delta_x + delta_z <= 29 - 2 * inset


def copper(block_x, block_y, block_z):
    value = noise(block_x // 2, block_y // 2, block_z // 2)
    return ('waxed_cut_copper' if value < 48 else 'waxed_exposed_cut_copper' if value < 63
            else 'waxed_weathered_cut_copper' if value < 90 else 'waxed_oxidized_cut_copper')


def stair(block_x, block_y, block_z, facing):
    put(block_x, block_y, block_z, 'waxed_cut_copper_stairs', facing=facing, half='bottom', shape='straight', waterlogged=False)


def slab(block_x, block_y, block_z, material='waxed_cut_copper', half='bottom'):
    put(block_x, block_y, block_z, material + '_slab', type=half, waterlogged=False)


def container(block_x, block_y, block_z, name='barrel', facing='up', loot=None, chest_type='single'):
    properties = {'facing': facing}
    if name == 'chest':
        properties.update(type=chest_type, waterlogged=False)
    else:
        properties['open'] = False
    put(block_x, block_y, block_z, name, **properties)
    payload = {'id': 'minecraft:' + name}
    if loot:
        payload['LootTable'] = 'minecraft:chests/' + loot
    ENTITIES[block_x, block_y, block_z] = payload


def lamp(position, behind):
    put(*behind, 'redstone_block')
    put(*position, 'redstone_lamp', lit=True)


def foundation_and_walls():
    for block_x, block_z in itertools.product(range(33), range(4, 37)):
        if not inside(block_x, block_z):
            continue
        put(block_x, 0, block_z, 'deepslate_tiles')
        put(block_x, 1, block_z, 'polished_deepslate')
        material = 'deepslate_bricks' if noise(block_x, 2, block_z) < 74 else 'deepslate_tiles'
        if inside(block_x, block_z, 2) and not inside(block_x, block_z, 3):
            material = copper(block_x, 2, block_z)
        if block_x in (7, 8, 24, 25) or block_z in (11, 12, 28, 29):
            material = copper(block_x, 2, block_z) if (block_x + block_z) % 3 else 'deepslate_tiles'
        put(block_x, 2, block_z, material)
        if not inside(block_x, block_z, 2):
            for block_y in range(3, 14):
                material = 'deepslate_bricks' if block_y in (3, 4, 12, 13) else copper(block_x, block_y, block_z)
                put(block_x, block_y, block_z, material)
    for block_x, block_z in ((3, 7), (8, 5), (24, 5), (29, 7), (1, 12), (1, 28),
                             (3, 33), (8, 35), (24, 35), (29, 33), (31, 12), (31, 28)):
        box((block_x, 3, block_z), (block_x, 13, block_z), 'deepslate_bricks')
        for block_y in (5, 10):
            put(block_x, block_y, block_z, 'chiseled_deepslate')
        slab(block_x, 14, block_z, 'deepslate_brick')
    for block_x, block_z in itertools.product(range(33), range(4, 37)):
        if inside(block_x, block_z):
            distance = min(block_x, 32 - block_x, block_z - 4, 36 - block_z)
            roof_y = 13 + min(3, distance // 4)
            put(block_x, roof_y, block_z, 'deepslate_tiles' if (block_x + block_z) % 7 else 'waxed_weathered_cut_copper')
            ROOF.add((block_x, roof_y, block_z))
            if (block_x in (8, 16, 24) or block_z in (12, 20, 28)) and roof_y >= 15:
                put(block_x, roof_y - 1, block_z, 'waxed_exposed_cut_copper')
                ROOF.add((block_x, roof_y - 1, block_z))


def niche(map_position, facing):
    for horizontal in range(-4, 5):
        for height in range(4, 12):
            edge = abs(horizontal) == 4 or height == 4 or height == 11 or (height == 10 and abs(horizontal) >= 3)
            put(*map_position(horizontal, height, 0), 'waxed_cut_copper' if edge else 'gold_block')
    for horizontal in range(-1, 2):
        for height in range(6, 9):
            put(*map_position(horizontal, height, 1), 'waxed_cut_copper')
    container(*map_position(0, 7, 1), facing=facing)
    put(*map_position(0, 7, 2), 'acacia_button', face='wall', facing=facing, powered=False)
    for horizontal in (-4, 4):
        put(*map_position(horizontal, 12, 0), 'chiseled_deepslate')
    lamp(map_position(0, 12, 0), map_position(0, 12, -1))


def grilles_and_bells():
    for side_x, facing in ((2, 'east'), (30, 'west')):
        for center_z in (11, 29):
            for block_z in range(center_z - 2, center_z + 3):
                for block_y in (10, 11):
                    put(side_x, block_y, block_z, 'acacia_trapdoor', facing=facing, half='bottom', open=True, powered=False, waterlogged=False)
    for block_x in (7, 25):
        for block_z in (12, 29):
            for block_y in range(10, 15):
                put(block_x, block_y, block_z, 'chain', axis='y', waterlogged=False)
            put(block_x, 9, block_z, 'bell', attachment='ceiling', facing='north', powered=False)
            ENTITIES[block_x, 9, block_z] = {'id': 'minecraft:bell'}
            put(block_x, 14, block_z, 'chiseled_deepslate')
            ROOF.add((block_x, 14, block_z))


def altar():
    for height, radius in ((3, 7), (4, 5), (5, 3)):
        for block_x, block_z in itertools.product(range(16 - radius, 17 + radius), range(20 - radius, 21 + radius)):
            if abs(block_x - 16) + abs(block_z - 20) > radius * 2 - 2:
                continue
            edge = max(abs(block_x - 16), abs(block_z - 20)) == radius
            put(block_x, height, block_z, 'waxed_cut_copper' if edge else ('deepslate_tiles' if height == 3 else copper(block_x, height, block_z)))
        for offset in range(-1, 2):
            for block_x, block_z, facing in ((16 + offset, 20 - radius, 'south'), (16 + offset, 20 + radius, 'north'),
                                              (16 - radius, 20 + offset, 'east'), (16 + radius, 20 + offset, 'west')):
                stair(block_x, height, block_z, facing)
    container(15, 6, 20, 'chest', 'north', 'bastion_treasure', 'left')
    container(16, 6, 20, 'chest', 'north', None, 'right')
    for block_x, block_z in ((14, 19), (17, 19), (14, 22), (18, 21)):
        put(block_x, 6, block_z, 'raw_gold_block')
    for block_x, block_z in ((13, 18), (18, 18), (13, 23), (19, 22), (12, 20), (20, 20), (16, 24), (17, 16)):
        surface = max(height for height in range(3, 6) if (block_x, height, block_z) in BLOCKS)
        put(block_x, surface + 1, block_z, 'light_weighted_pressure_plate', power=0)


def brazier(center_x, center_z):
    for offset_x, offset_z in itertools.product(range(-1, 2), repeat=2):
        slab(center_x + offset_x, 5, center_z + offset_z, half='double')
    put(center_x, 6, center_z, 'waxed_cut_copper')
    put(center_x, 7, center_z, 'campfire', facing='north', lit=True, signal_fire=False, waterlogged=False)
    ENTITIES[center_x, 7, center_z] = {'id': 'minecraft:campfire'}
    for offset_x, offset_z in ((-1, -1), (-1, 1), (1, -1), (1, 1)):
        put(center_x + offset_x, 6, center_z + offset_z, 'lightning_rod', facing='up', powered=False, waterlogged=False)
    for offset_x, offset_z in ((-1, 0), (1, 0), (0, -1), (0, 1)):
        put(center_x + offset_x, 6, center_z + offset_z, 'iron_bars', east=offset_x == 0, west=offset_x == 0,
            north=offset_z == 0, south=offset_z == 0, waterlogged=False)
        slab(center_x + offset_x, 7, center_z + offset_z)


def storage_and_floor_details():
    for base_x, base_z in ((5, 10), (26, 10), (6, 30), (25, 30)):
        for offset in (0, 1):
            container(base_x + offset, 3, base_z, facing='up', loot='simple_dungeon' if offset == 0 else None)
        container(base_x, 4, base_z, facing='up')
        put(base_x, 5, base_z, 'light_weighted_pressure_plate', power=0)
        container(base_x, 3, base_z + (1 if base_z < 20 else -1), facing='north')
    for block_x, block_z in ((5, 15), (27, 15), (5, 25), (27, 25), (10, 9), (22, 9), (10, 31), (22, 31)):
        for offset_x, offset_z in itertools.product(range(2), repeat=2):
            put(block_x + offset_x, 2, block_z + offset_z, 'iron_trapdoor', facing='north', half='top', open=False, powered=False, waterlogged=False)
            put(block_x + offset_x, 1, block_z + offset_z, 'glowstone')
    for block_x, block_z in ((4, 18), (28, 23), (8, 13), (24, 27), (12, 32), (21, 7)):
        put(block_x, 3, block_z, 'light_weighted_pressure_plate', power=0)


def gate():
    box((11, 0, 0), (21, 1, 5), 'polished_deepslate')
    box((11, 2, 0), (21, 2, 5), 'deepslate_tiles')
    for side_x in (11, 12, 20, 21):
        box((side_x, 3, 0), (side_x, 6, 4), 'deepslate_bricks')
        for block_z in range(5):
            put(side_x, 4, block_z, copper(side_x, 4, block_z))
    box((13, 3, 4), (19, 9, 5), 'waxed_cut_copper')
    for block_x in range(13, 20):
        for block_y in range(3, 9):
            for block_z in (4, 5):
                put(block_x, block_y, block_z, 'air')
    for block_x in (13, 14, 17, 18, 19):
        for block_y in range(3, 9):
            put(block_x, block_y, 4, 'iron_bars', east=True, west=True, north=False, south=False, waterlogged=False)
    for block_x in (15, 16):
        for block_y in range(5, 9):
            put(block_x, block_y, 4, 'iron_bars', east=True, west=True, north=False, south=False, waterlogged=False)
        for half, block_y in (('lower', 3), ('upper', 4)):
            put(block_x, block_y, 4, 'iron_door', facing='south', half=half,
                hinge='left' if block_x == 15 else 'right', open=False, powered=False)
        for block_z in (3, 5):
            put(block_x, 3, block_z, 'light_weighted_pressure_plate', power=0)
    for block_x in (12, 20):
        box((block_x, 3, 4), (block_x, 10, 4), 'deepslate_bricks')
        lamp((block_x, 6, 3), (block_x, 6, 4))
    lamp((16, 9, 4), (16, 9, 5))
    put(15, 2, 4, 'gold_block')
    put(16, 2, 4, 'gold_block')


def generate():
    BLOCKS.clear()
    ENTITIES.clear()
    ROOF.clear()
    foundation_and_walls()
    niche(lambda horizontal, height, depth: (16 + horizontal, height, 34 - depth), 'north')
    niche(lambda horizontal, height, depth: (2 + depth, height, 20 + horizontal), 'east')
    niche(lambda horizontal, height, depth: (30 - depth, height, 20 - horizontal), 'west')
    grilles_and_bells()
    altar()
    for center_x, center_z in ((11, 15), (21, 15), (11, 25), (21, 25)):
        brazier(center_x, center_z)
    storage_and_floor_details()
    gate()


def encode_string(value):
    encoded = value.encode('utf-8')
    return struct.pack('>H', len(encoded)) + encoded


def nbt(value):
    if isinstance(value, dict):
        entries = []
        for key, child in value.items():
            tag_type, payload = nbt(child)
            entries.append(bytes([tag_type]) + encode_string(key) + payload)
        return 10, b''.join(entries) + b'\0'
    if isinstance(value, list):
        entries = [nbt(child) for child in value]
        tag_type = entries[0][0] if entries else 0
        assert all(entry[0] == tag_type for entry in entries)
        return 9, bytes([tag_type]) + struct.pack('>i', len(entries)) + b''.join(entry[1] for entry in entries)
    if isinstance(value, int):
        return 3, struct.pack('>i', value)
    if isinstance(value, str):
        return 8, encode_string(value)
    raise TypeError(type(value))


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def write_structure():
    palette, palette_map, entries = [], {}, []
    for position in itertools.product(*(range(length) for length in SIZE)):
        state = BLOCKS.get(position, {'Name': 'minecraft:air'})
        key = json.dumps(state, sort_keys=True)
        if key not in palette_map:
            palette_map[key] = len(palette)
            palette.append(state)
        entry = {'pos': list(position), 'state': palette_map[key]}
        if position in ENTITIES:
            entry['nbt'] = ENTITIES[position]
        entries.append(entry)
    template = {'DataVersion': 3465, 'size': list(SIZE), 'palette': palette, 'blocks': entries, 'entities': []}
    path = PACK / f'data/{NAMESPACE}/structures/treasury.nbt'
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(gzip.compress(b'\x0a\x00\x00' + nbt(template)[1], mtime=0))
    (ROOT / 'out/FT_Copper_Vault_1.20.1.nbt').write_bytes(path.read_bytes())
    write_json(ROOT / 'qa/blueprint.json', {'roof': sorted(ROOF), **template})
    return template


def function(name, lines):
    path = PACK / f'data/{NAMESPACE}/functions/{name}.mcfunction'
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text('\n'.join(lines) + '\n', encoding='utf-8')


def tell(message, color='gold'):
    return 'tellraw @s ' + json.dumps({'text': message, 'color': color}, ensure_ascii=False)


def write_pack():
    write_json(PACK / 'pack.mcmeta', {'pack': {'pack_format': 15, 'description': '锈铜宝库 · 原版建筑 · Java 1.20.1 · 33×17×37'}})
    write_json(PACK / 'data/minecraft/tags/functions/load.json', {'values': [NAMESPACE + ':load']})
    write_json(PACK / f'data/{NAMESPACE}/tags/blocks/air.json', {'replace': False, 'values': ['minecraft:air', 'minecraft:cave_air', 'minecraft:void_air']})
    function('load', ['scoreboard objectives add ftcv.build dummy'])
    function('help', [tell('锈铜宝库：站在西北角最低基础高度，沿 +X、+Z 留出 33×37 格、向上 17 格完整空域。'),
                      tell('创造模式执行 /function ftcoppervault:build；入口朝北，行走高度比执行高度高三格。'),
                      tell('仅生成建筑，无自动刷怪或 FT 挑战联动。原生 /place template 会绕过防覆盖检查。', 'gray')])
    function('build', ['execute if entity @s[type=minecraft:player,gamemode=creative] align xyz run function ftcoppervault:internal/build',
                       'execute unless entity @s[type=minecraft:player,gamemode=creative] run ' + tell('请由创造模式玩家执行建造。', 'red')])
    lines = ['scoreboard players set #ok ftcv.build 1', 'scoreboard players set #placed ftcv.build 0', 'tag @s add ftcv.builder']
    for block_x, block_z in itertools.product((0, 16, 32), (0, 16, 32, 36)):
        lines.append(f'execute unless loaded ~{block_x} ~0 ~{block_z} run scoreboard players set #ok ftcv.build 0')
    for position in ((0, 0, 0), (32, 16, 36)):
        coordinates = ' '.join('~' + str(value) for value in position)
        lines += ['scoreboard players set #bounds ftcv.build 0',
                  f'execute store success score #bounds ftcv.build if block {coordinates} #ftcoppervault:air',
                  'execute if score #bounds ftcv.build matches 0 run scoreboard players set #ok ftcv.build 0']
    lines += ['execute if entity @e[tag=!ftcv.builder,dx=32,dy=16,dz=36] run scoreboard players set #ok ftcv.build 0',
              'execute if score #ok ftcv.build matches 1 run function ftcoppervault:internal/scan',
              'execute if score #ok ftcv.build matches 1 store success score #placed ftcv.build run place template ftcoppervault:treasury ~ ~ ~ none none 1.0 0',
              'tag @s remove ftcv.builder',
              'execute if score #placed ftcv.build matches 1 run tp @s ~16 ~3 ~1.5 0 0',
              'execute if score #placed ftcv.build matches 1 run ' + tell('锈铜宝库已建造：中央宝箱、四座火盆、三面金龛。入口金压板可开门。'),
              'execute if score #ok ftcv.build matches 0 run ' + tell('拒绝放置：空域有方块或其它实体、区块未加载，或超出世界高度；未写入建筑。', 'red'),
              'execute if score #ok ftcv.build matches 1 if score #placed ftcv.build matches 0 run ' + tell('模板放置失败，请检查数据包资源和服务器日志。', 'red')]
    function('internal/build', lines)
    function('internal/scan', [f'execute if score #ok ftcv.build matches 1 run function ftcoppervault:internal/scan_{block_y}' for block_y in range(SIZE[1])])
    for block_y in range(SIZE[1]):
        function(f'internal/scan_{block_y}', [f'execute unless block ~{block_x} ~{block_y} ~{block_z} #ftcoppervault:air run scoreboard players set #ok ftcv.build 0'
                                            for block_x, block_z in itertools.product(range(SIZE[0]), range(SIZE[2]))])
    archive = ROOT / 'out/ft-copper-vault-1.20.1.zip'
    with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as bundle:
        for path in sorted(PACK.rglob('*')):
            if path.is_file():
                bundle.write(path, path.relative_to(PACK).as_posix())


def verify(template):
    assert len(template['blocks']) == 33 * 17 * 37
    assert len({tuple(entry['pos']) for entry in template['blocks']}) == len(template['blocks'])
    assert all(all(0 <= position[axis] < SIZE[axis] for axis in range(3)) for position in BLOCKS)
    assert len([state for state in BLOCKS.values() if state['Name'] == 'minecraft:campfire']) == 4
    assert len([state for state in BLOCKS.values() if state['Name'] == 'minecraft:bell']) == 4
    assert not any('command_block' in state['Name'] or 'spawner' in state['Name'] for state in BLOCKS.values())
    for position, state in BLOCKS.items():
        if state['Name'] == 'minecraft:redstone_lamp':
            assert any(BLOCKS.get(tuple(position[axis] + delta[axis] for axis in range(3)), {}).get('Name') == 'minecraft:redstone_block'
                       for delta in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)))
    walkable = set()
    passable = {'minecraft:air', 'minecraft:light_weighted_pressure_plate', 'minecraft:iron_door', 'minecraft:acacia_button'}
    for block_x, block_z in itertools.product(range(2, 31), range(37)):
        for height in range(3, 7):
            below = BLOCKS.get((block_x, height - 1, block_z), {}).get('Name', 'minecraft:air')
            if below not in passable and all(BLOCKS.get((block_x, height + offset, block_z), {}).get('Name', 'minecraft:air') in passable for offset in (0, 1)):
                walkable.add((block_x, height, block_z))
    reached, pending = {(15, 3, 1)}, collections.deque([(15, 3, 1)])
    while pending:
        current = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for delta_y in (-1, 0, 1):
                candidate = (current[0] + delta_x, current[1] + delta_y, current[2] + delta_z)
                if candidate in walkable and candidate not in reached:
                    reached.add(candidate)
                    pending.append(candidate)
    for target in ((15, 6, 19), (16, 6, 19), (5, 3, 20), (27, 3, 20), (16, 3, 31), (16, 3, 8)):
        assert target in reached, ('Unreachable', target)
    for path in PACK.rglob('*.json'):
        json.loads(path.read_text(encoding='utf-8'))
    for path in (PACK / f'data/{NAMESPACE}/functions').rglob('*.mcfunction'):
        for line in path.read_text(encoding='utf-8').splitlines():
            if 'run function ' in line:
                reference = line.split('run function ')[1]
                assert (PACK / ('data/' + reference.replace(':', '/functions/') + '.mcfunction')).is_file(), reference
    counts = collections.Counter(state['Name'] for state in BLOCKS.values() if state['Name'] != 'minecraft:air')
    report = {'status': 'PASS', 'size_xyz': SIZE, 'dense_volume': len(template['blocks']), 'non_air_blocks': sum(counts.values()),
              'palette_states': len(template['palette']), 'block_entities': len(ENTITIES), 'materials': dict(counts.most_common()),
              'two_block_headroom_reachable_cells': len(reached), 'landmark_routes': 'PASS', 'powered_lamps': 'PASS',
              'scope': 'Static geometry and pack audit; not an in-game placement test.', 'player_worlds_modified': False}
    write_json(ROOT / 'qa/geometry.json', report)
    (ROOT / 'out/materials.csv').write_text('block_id,count\n' + ''.join(f'{name},{count}\n' for name, count in counts.most_common()), encoding='utf-8-sig')
    print(json.dumps({key: value for key, value in report.items() if key != 'materials'}, ensure_ascii=False))


def main():
    generate()
    template = write_structure()
    write_pack()
    verify(template)
    write_json(ROOT / 'out/manifest.json', {'name': '锈铜宝库', 'namespace': NAMESPACE, 'minecraft': '1.20.1', 'DataVersion': 3465,
                'pack_format': 15, 'size_xyz': SIZE, 'origin': 'north-west minimum foundation block', 'floor_block_y': 2,
                'standing_y': 3, 'entrance_facing': 'north (-Z)', 'roof_included': True, 'uses_only_vanilla_blocks': True,
                'explicit_air': True, 'gameplay': 'architecture only; no automatic mobs or FT challenge linkage',
                'reference': 'user-attached copper treasury image', 'paid_image_generation_requests': 0})


if __name__ == '__main__':
    main()
