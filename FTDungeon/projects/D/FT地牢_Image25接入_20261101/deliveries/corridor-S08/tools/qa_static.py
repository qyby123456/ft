import gzip
import hashlib
import io
import json
import struct
from collections import Counter, deque
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S08-ice-frost-intact.png')
REFERENCE_HASH = '5d95e20ec07b165939bf147ee8368de8fe12ec1d66b0ace897f3d904dde06758'


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


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
            return [payload(subtype) for unused in range(count)]
        if kind == 10:
            result = {}
            while True:
                subtype = take(1)[0]
                if subtype == 0:
                    return result
                name = text()
                if name in result:
                    raise ValueError('Duplicate NBT key')
                result[name] = payload(subtype)
        if kind in (7, 11, 12):
            count = struct.unpack('>i', take(4))[0]
            return [payload({7: 1, 11: 3, 12: 4}[kind]) for unused in range(count)]
        raise ValueError(f'Unsupported NBT tag {kind}')

    assert take(1)[0] == 10
    text()
    result = payload(10)
    assert not stream.read()
    return result


def main():
    (ROOT / 'qa').mkdir(exist_ok=True)
    document = read_nbt(ROOT / 'room.nbt')
    size = document['size']
    palette = document['palette']
    records = document['blocks']
    blocks = {tuple(record['pos']): palette[record['state']] for record in records}
    counts = Counter(block['Name'] for block in blocks.values())
    checks = {}

    def check(label, condition):
        checks[label] = bool(condition)
        if not condition:
            raise AssertionError(label)

    def name(position):
        return blocks.get(tuple(position), {}).get('Name', 'minecraft:air')

    def walkable(position):
        across, height, depth = position
        return name(position) == 'minecraft:air' and name((across, height + 1, depth)) == 'minecraft:air' and name((across, height - 1, depth)) in {'minecraft:packed_ice', 'minecraft:blue_ice', 'minecraft:smooth_quartz'}

    check('reference_sha256', digest(REFERENCE) == REFERENCE_HASH)
    check('gzip_java_1201_single_palette', document['DataVersion'] == 3465 and 'palettes' not in document)
    check('short_one_chunk_size', size == [11, 10, 16])
    check('all_cells_explicit_unique', len(records) == len(blocks) == 11 * 10 * 16)
    check('coordinates_inside_volume', all(all(0 <= value < size[axis] for axis, value in enumerate(position)) for position in blocks))
    check('no_entities_or_block_entities', not document.get('entities') and not any('nbt' in record for record in records))
    forbidden = {'ice', 'frosted_ice', 'water', 'lava', 'snow', 'powder_snow', 'fire', 'soul_fire', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'light', 'chest', 'barrel'}
    check('no_melting_ice_layers_liquids_hazards_commands_or_stock', not any(identifier.removeprefix('minecraft:') in forbidden for identifier in counts))
    check('vanilla_namespaces', all(identifier.startswith('minecraft:') for identifier in counts))
    check('continuous_solid_foundation_and_roof', all(name((across, height, depth)) == 'minecraft:stone_bricks' for across in range(11) for depth in range(16) for height in (0, 9)))
    check('solid_parallel_side_shells', all(name((across, height, depth)) == 'minecraft:stone_bricks' for across in (0, 10) for height in range(2, 9) for depth in range(16)))
    check('three_wide_three_high_straight_clearance', all(name((across, height, depth)) == 'minecraft:air' for across in range(4, 7) for height in range(2, 5) for depth in range(16)))
    check('both_portals_three_wide_four_high', all(name((across, height, depth)) == 'minecraft:air' for across in range(4, 7) for height in range(2, 6) for depth in (0, 15)))
    torch_supports = []
    icicles = []
    direction = {'east': (-1, 0, 0), 'west': (1, 0, 0), 'north': (0, 0, 1), 'south': (0, 0, -1)}
    for position, block in blocks.items():
        if block['Name'] == 'minecraft:wall_torch':
            delta = direction[block['Properties']['facing']]
            support = tuple(value + delta[axis] for axis, value in enumerate(position))
            check(f'torch_support_{position}', name(support) in {'minecraft:stone_bricks', 'minecraft:chiseled_stone_bricks'})
            torch_supports.append({'position': position, 'support': support})
        if block['Name'] == 'minecraft:light_blue_stained_glass_pane':
            above = (position[0], position[1] + 1, position[2])
            check(f'icicle_attachment_{position}', name(above) in {'minecraft:packed_ice', 'minecraft:light_blue_stained_glass_pane'})
            icicles.append(position)
    entrance = (5, 2, 0)
    interior = (5, 2, 7)
    exit_position = (5, 2, 15)
    visited = {entrance}
    pending = deque([entrance])
    while pending:
        position = pending.popleft()
        for delta in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1)):
            candidate = tuple(value + delta[axis] for axis, value in enumerate(position))
            if candidate not in visited and all(0 <= value < size[axis] for axis, value in enumerate(candidate)) and walkable(candidate):
                visited.add(candidate)
                pending.append(candidate)
    check('entrance_interior_exit_connected', interior in visited and exit_position in visited)
    check('all_three_main_lanes_connected', all((across, 2, depth) in visited for across in range(4, 7) for depth in range(16)))
    metadata = {
        'id': 'corridor-S08', 'source_id': 'S08', 'name': '冰霜石拱短直廊', 'size': size,
        'producer_thread_id': '01a19f19-87b3-7760-bd8f-467086bad426',
        'reference_spec': 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/SPEC.md',
        'reference_manifest': 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/manifest.json',
        'entrance': entrance, 'interior': interior, 'exit': exit_position, 'facing': 'north',
        'portals': [{'facing': 'north', 'feet': entrance}, {'facing': 'south', 'feet': exit_position}],
        'corridor_axis': 'z', 'length_blocks': 16, 'length_class': 'short', 'main_clear_width': 3,
        'sha256': digest(ROOT / 'room.nbt'), 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH,
        'minecraft': 'Java 1.20.1', 'data_version': 3465, 'block_records': len(records),
        'preview': 'preview.png', 'qa': 'qa.json', 'generator': 'Minecraft Builder / Lodestone 0.6.0',
        'deployment': 'not_deployed', 'production_saves_touched': False,
        'reference_adaptations': ['The illustrated closed end door is an open south portal to preserve straight-through passage.', 'Packed ice and blue ice do not melt beside warm wall torches.', 'Solid snow blocks and supported quartz ledges replace melt-prone snow layers.', 'Attached light-blue glass panes approximate icicles without falling blocks.'],
        'limitations': ['Ice floor intentionally retains normal vanilla ice slipperiness.', 'No unique FT integration slot is assigned by this delivery.'],
    }
    blueprint = {'size': size, 'entrance': entrance, 'interior': interior, 'exit': exit_position, 'floor_y': 1, 'arches_z': [0, 5, 10, 15], 'torches': torch_supports, 'icicles': icicles, 'main_path': [[5, 2, depth] for depth in range(16)]}
    report = {'status': 'PASS', 'structure_sha256': metadata['sha256'], 'reference_sha256': REFERENCE_HASH, 'checks': checks, 'counts': dict(sorted(counts.items())), 'reachable_floor_cells': len(visited), 'block_records': len(records), 'palette_entries': len(palette), 'entities': 0, 'block_entities': 0}
    for path, content in [(ROOT / 'room.json', metadata), (ROOT / 'blueprint.json', blueprint), (ROOT / 'qa/static.json', report)]:
        path.write_text(json.dumps(content, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'status': report['status'], 'size': size, 'records': len(records), 'sha256': metadata['sha256'], 'checks': len(checks)}))


if __name__ == '__main__':
    main()
