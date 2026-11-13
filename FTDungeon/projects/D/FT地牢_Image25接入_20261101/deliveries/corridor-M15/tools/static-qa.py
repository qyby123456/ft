import hashlib
import json
from collections import Counter, deque
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

from nbtio import plain, read_nbt


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M15-blackstone-deepslate-overgrown.png')
REFERENCE_HASH = '501484bf3ef3bc469ec605adcadc0170fda464876eb3dcb94641d26a115b250b'


def dump(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    fingerprint = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    _, raw = read_nbt(ROOT / 'room.nbt')
    data = plain(raw)
    palette = data['palette']
    records = data['blocks']
    cells = {tuple(entry['pos']): palette[entry['state']] for entry in records}
    histogram = Counter(cell['Name'] for cell in cells.values())
    checks = {}

    def check(name, passed, detail):
        checks[name] = {'status': 'PASS' if passed else 'FAIL', 'detail': detail}
        if not passed:
            raise AssertionError(f'{name}: {detail}')

    def name(position):
        return cells[position]['Name'].removeprefix('minecraft:')

    check('reference_sha256', hashlib.sha256(REFERENCE.read_bytes()).hexdigest() == REFERENCE_HASH, REFERENCE_HASH)
    check('gzip_nbt_1_20_1', (ROOT / 'room.nbt').read_bytes()[:2] == b'\x1f\x8b' and data['DataVersion'] == 3465 and 'palettes' not in data, 'gzip / DataVersion 3465 / single palette')
    check('medium_straight_size', data['size'] == [15, 12, 40], '40 blocks = 2.5 chunks, straight Z axis; no stairs in travel surface')
    check('complete_unique_volume', len(cells) == len(records) == 7200 and all(0 <= position[axis] < data['size'][axis] for position in cells for axis in range(3)), '7200 explicit records including air; importer must check entire volume, never blindly clear')
    check('no_entities_or_block_entities', not data.get('entities') and not any('nbt' in entry for entry in records), 'No mobs, containers, loot, spawners, commands or block entities')
    banned = {'water', 'lava', 'fire', 'soul_fire', 'powder_snow', 'cobweb', 'tnt', 'spawner', 'command_block', 'structure_block', 'jigsaw', 'barrier', 'structure_void', 'sand', 'gravel'}
    check('vanilla_no_hazards', all(entry['Name'].startswith('minecraft:') and entry['Name'].split(':')[1] not in banned for entry in palette), 'Native registry/property validity is independently checked by Forge')
    floor = all(name((horizontal, 0, depth)) not in {'air', 'vine', 'moss_carpet'} for horizontal in range(15) for depth in range(40))
    roof = all(name((horizontal, 10, depth)) in {'deepslate_tiles', 'polished_blackstone_bricks'} for horizontal in range(15) for depth in range(40))
    check('continuous_floor_and_roof', floor and roof, '600 full floor blocks and 600 full roof blocks; broken arches are only decorative ribs')
    walls = all(name((horizontal, height, depth)) != 'air' for horizontal in (0, 1, 13, 14) for height in range(1, 10) for depth in range(40))
    check('no_side_branches', walls, 'Both long sides have complete two-block-thick shell; niches are blind')
    corridor = all(name((horizontal, height, depth)) == 'air' for horizontal in range(5, 10) for height in range(1, 6) for depth in range(40))
    check('five_wide_five_high_straight_route', corridor, 'x=5..9, feet y=1, z=0..39; no obstacles or level changes')
    supports = []
    for position, entry in cells.items():
        horizontal, height, depth = position
        short = entry['Name'].split(':')[1]
        properties = entry.get('Properties', {})
        if short in {'candle', 'moss_carpet'}:
            supports.append(name((horizontal, height - 1, depth)) not in {'air', 'vine', 'candle', 'moss_carpet'})
        elif short == 'vine':
            faces = {'west': (-1, 0, 0), 'east': (1, 0, 0), 'north': (0, 0, -1), 'south': (0, 0, 1), 'up': (0, 1, 0)}
            supports.append(all(name(tuple(position[axis] + offset[axis] for axis in range(3))) not in {'air', 'vine', 'candle'} for face, offset in faces.items() if properties.get(face) == 'true'))
        elif short == 'chain':
            supports.append(name((horizontal, height + 1, depth)) in {'chain', 'polished_blackstone_bricks'})
        elif short == 'oak_leaves':
            supports.append(properties.get('persistent') == 'true')
    check('static_attachments_supported', all(supports), f'{len(supports)} attachment checks; native canSurvive and 200 tick checks are separate')
    visited = {(7, 1, 0)}
    pending = deque(visited)
    while pending:
        horizontal, height, depth = pending.popleft()
        for change_x, change_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            candidate = horizontal + change_x, height, depth + change_z
            if candidate in cells and candidate not in visited and name(candidate) == 'air' and name((candidate[0], 2, candidate[2])) == 'air' and name((candidate[0], 0, candidate[2])) != 'air':
                visited.add(candidate)
                pending.append(candidate)
    check('entrance_exit_walkable', (7, 1, 39) in visited and (7, 1, 2) in visited, f'{len(visited)} connected floor cells; native player collision AABB is additionally checked')
    metadata = {
        'id': 'corridor-M15', 'source_id': 'M15', 'name': '苔蚀黑石断拱烛廊', 'kind': 'medium-straight-corridor',
        'size': [15, 12, 40], 'entrance': [7, 1, 0], 'interior': [7, 1, 2], 'exit': [7, 1, 39], 'facing': 'north',
        'passage': {'axis': 'z', 'length': 40, 'chunks': 2.5, 'clear_width': 5, 'clear_height': 5, 'feet_y': 1},
        'sha256': fingerprint, 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH,
        'theme': 'blackstone-deepslate', 'condition': 'overgrown', 'light': 'candles', 'feature': 'broken-arches',
        'data_version': 3465, 'minecraft_version': '1.20.1', 'builder': 'minecraft-builder-skill / Lodestone / tools/build-m15.mjs',
        'rotation_pivot': [0, 0, 0], 'deployed': False, 'player_saves_accessed': False,
        'import_safety': 'Check the complete rotated 15x12x40 volume, world bounds, fluids and entities before first placement. Never clear occupied terrain. Use a per-id persistent generation marker; reject and diagnose failures. No production integration is delivered.'
    }
    dump(ROOT / 'room.json', metadata)
    report = {'id': 'corridor-M15', 'status': 'PASS', 'timestamp': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(), 'sha256': fingerprint, 'checks': checks, 'palette_states': len(palette), 'records': len(records), 'materials': dict(histogram), 'native_result': 'qa/native-world-qa.json', 'native_result_included_in_static_status': False, 'client_gameplay_tested': False, 'deployed': False}
    dump(ROOT / 'qa/static-qa.json', report)
    dump(ROOT / 'qa.json', report)
    dump(ROOT / 'materials.json', {'sha256': fingerprint, 'counts_include_air': True, 'materials': dict(histogram)})
    print(json.dumps({'status': 'PASS', 'checks': len(checks), 'sha256': fingerprint, 'size': data['size'], 'records': len(records)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
