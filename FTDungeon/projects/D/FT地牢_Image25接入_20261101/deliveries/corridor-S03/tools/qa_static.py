import gzip
import hashlib
import io
import json
import struct
from collections import Counter, deque
from datetime import datetime, timezone, timedelta
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S03-overgrown-ruins-overgrown.png')
REFERENCE_HASH = 'f33bb90a2418cf5d68bc2bc894e9931af8d3d54a6cb8285f7b3c30d9c8e3e1d2'
SIZE = [11, 10, 16]


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def dump(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def read_nbt(path):
    stream = io.BytesIO(gzip.decompress(path.read_bytes()))

    def take(count):
        data = stream.read(count)
        if len(data) != count:
            raise ValueError('Truncated NBT')
        return data

    def text():
        return take(struct.unpack('>H', take(2))[0]).decode('utf-8')

    def payload(kind):
        formats = {1: '>b', 2: '>h', 3: '>i', 4: '>q', 5: '>f', 6: '>d'}
        if kind in formats:
            return struct.unpack(formats[kind], take(struct.calcsize(formats[kind])))[0]
        if kind == 8:
            return text()
        if kind == 9:
            subtype, count = struct.unpack('>bi', take(5))
            assert count >= 0
            return [payload(subtype) for _ in range(count)]
        if kind == 10:
            values = {}
            while True:
                subtype = take(1)[0]
                if subtype == 0:
                    return values
                name = text()
                assert name not in values
                values[name] = payload(subtype)
        if kind in (7, 11, 12):
            count = struct.unpack('>i', take(4))[0]
            assert count >= 0
            return [payload({7: 1, 11: 3, 12: 4}[kind]) for _ in range(count)]
        raise ValueError(f'Unsupported NBT tag {kind}')

    assert take(1) == b'\x0a'
    text()
    root = payload(10)
    assert not stream.read()
    return root


def supported(position, state, blocks, full):
    block_x, height, block_z = position
    name = state['Name']
    properties = state.get('Properties', {})
    above = blocks.get((block_x, height + 1, block_z), {}).get('Name', 'minecraft:air')
    below = blocks.get((block_x, height - 1, block_z), {}).get('Name', 'minecraft:air')
    if name in {'minecraft:chain', 'minecraft:soul_lantern'}:
        return above != 'minecraft:air'
    if name == 'minecraft:moss_carpet':
        return below in full
    if name == 'minecraft:vine':
        directions = {'west': (-1, 0, 0), 'east': (1, 0, 0), 'north': (0, 0, -1), 'south': (0, 0, 1), 'up': (0, 1, 0)}
        return any(properties.get(direction) == 'true' and blocks.get((block_x + offset[0], height + offset[1], block_z + offset[2]), {}).get('Name') in full for direction, offset in directions.items())
    return True


def main():
    nbt_path = ROOT / 'room.nbt'
    tag = read_nbt(nbt_path)
    fingerprint = digest(nbt_path)
    checks = {}

    def check(name, condition):
        checks[name] = bool(condition)
        assert condition, name

    check('reference_sha256', digest(REFERENCE) == REFERENCE_HASH)
    check('gzip_single_palette_java_1_20_1', tag['DataVersion'] == 3465 and 'palette' in tag and 'palettes' not in tag)
    check('short_exactly_16_blocks', tag['size'] == SIZE)
    check('no_template_entities', not tag.get('entities', []))
    blocks = {}
    counts = Counter()
    for record in tag['blocks']:
        position = tuple(record['pos'])
        assert position not in blocks and all(0 <= value < limit for value, limit in zip(position, SIZE)), position
        assert 'nbt' not in record
        state = tag['palette'][record['state']]
        blocks[position] = state
        counts[state['Name']] += 1
    check('explicit_full_volume_unique_records_no_block_entities', len(blocks) == 11 * 10 * 16)
    allowed = {'air', 'stone_bricks', 'mossy_stone_bricks', 'cracked_stone_bricks', 'mossy_cobblestone', 'dark_oak_log', 'chiseled_stone_bricks', 'mossy_stone_brick_stairs', 'chain', 'soul_lantern', 'iron_trapdoor', 'iron_bars', 'vine', 'moss_block', 'moss_carpet'}
    check('controlled_vanilla_palette_no_hazards_containers_or_technical_blocks', all(name.startswith('minecraft:') and name.split(':')[1] in allowed for name in counts))
    full = {'minecraft:' + name for name in {'stone_bricks', 'mossy_stone_bricks', 'cracked_stone_bricks', 'mossy_cobblestone', 'dark_oak_log', 'chiseled_stone_bricks', 'moss_block'}}
    check('full_floor_and_roof', all(blocks[block_x, height, block_z]['Name'] in full for block_x in range(11) for block_z in range(16) for height in [0, 9]))
    check('sealed_parallel_side_walls_no_branches', all(blocks[block_x, height, block_z]['Name'] in full for block_x in [0, 10] for height in range(1, 9) for block_z in range(16)))
    check('both_open_endpoints_5w_4h', all(blocks[block_x, height, block_z]['Name'] == 'minecraft:air' for block_x in range(3, 8) for height in range(1, 5) for block_z in [0, 15]))
    check('straight_3w_3h_air_lane_without_stairs_or_doors', all(blocks[block_x, height, block_z]['Name'] == 'minecraft:air' for block_x in range(4, 7) for height in range(1, 4) for block_z in range(16)))
    check('supported_lanterns_chains_vines_carpets', all(supported(position, state, blocks, full) for position, state in blocks.items()))
    reachable = {(5, 0)}
    pending = deque(reachable)
    while pending:
        block_x, block_z = pending.popleft()
        for next_x, next_z in [(block_x + 1, block_z), (block_x - 1, block_z), (block_x, block_z + 1), (block_x, block_z - 1)]:
            if (next_x, next_z) in reachable or not (0 <= next_x < 11 and 0 <= next_z < 16):
                continue
            if all(blocks[next_x, height, next_z]['Name'] == 'minecraft:air' for height in [1, 2]) and blocks[next_x, 0, next_z]['Name'] in full:
                reachable.add((next_x, next_z))
                pending.append((next_x, next_z))
    check('walkable_north_to_south_and_return', (5, 15) in reachable)
    materials = dict(sorted(counts.items()))
    dump(ROOT / 'qa/materials.json', {'nbt_sha256': fingerprint, 'counts': materials, 'records': len(blocks), 'non_air': len(blocks) - counts['minecraft:air']})
    metadata = {
        'id': 'corridor-S03', 'reference_id': 'S03', 'name': '蔓生废墟·魂灯吊笼短廊', 'category': 'corridor', 'kind': 'short', 'size': SIZE,
        'entrance': [5, 1, 0], 'interior': [5, 1, 2], 'exit': [5, 1, 15], 'exit_interior': [5, 1, 13], 'facing': 'north', 'axis': '+Z', 'length_blocks': 16,
        'clear_lane': {'x': [4, 6], 'feet_y': 1, 'clear_height': 3, 'z': [0, 15]},
        'ports': [{'side': 'north', 'feet': [5, 1, 0], 'opening_width': 5, 'opening_height': 4}, {'side': 'south', 'feet': [5, 1, 15], 'opening_width': 5, 'opening_height': 4}],
        'sha256': fingerprint, 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH, 'data_version': 3465, 'minecraft_version': '1.20.1',
        'entities': 0, 'block_entities': 0, 'deployment': 'independent_only_not_deployed',
        'reference_deviation': '原图尽头关闭木门改为无门开放拱口，确保一字贯通；未新增岔路或通行楼梯。',
        'placement_note': '模板包含显式空气。接入前必须检查整个11×10×16体积及实体，拒绝占用，不得无条件覆盖或清空。',
    }
    dump(ROOT / 'room.json', metadata)
    report = {'status': 'PASS', 'id': 'corridor-S03', 'nbt_sha256': fingerprint, 'timestamp': datetime.now(timezone(timedelta(hours=8))).isoformat(), 'checks': checks, 'record_count': len(blocks), 'palette_count': len(tag['palette']), 'conservative_air_only_reachable_feet': len(reachable), 'reference_sha256': REFERENCE_HASH, 'scope': 'Static NBT verification; native registry, collision and placement are separate evidence.'}
    dump(ROOT / 'qa/static-qa.json', report)
    print(json.dumps({'status': 'PASS', 'records': len(blocks), 'palette': len(tag['palette']), 'sha256': fingerprint, 'materials': materials}, ensure_ascii=False))


if __name__ == '__main__':
    main()
