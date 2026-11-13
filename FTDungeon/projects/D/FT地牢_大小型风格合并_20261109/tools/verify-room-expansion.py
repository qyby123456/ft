from pathlib import Path
from collections import deque
import argparse
import hashlib
import importlib.util
import json
import re

ROOT = Path(__file__).resolve().parents[1]
WORK = ROOT / 'output/room-expansion-v035'
DATA = ROOT / 'src/main/resources/data/fteclipsegen'
SPEC = importlib.util.spec_from_file_location('rebuild', ROOT / 'tools/integrate-style-rebuild.py')
REBUILD = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(REBUILD)
AIR = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def cells(path):
    data = REBUILD.decode(path.read_bytes())
    size = tuple(map(int, data['size']))
    assert size == (16, 16, 16), f'{path}: wrong size'
    assert int(data['DataVersion']) == 3465 and not data['entities']
    palette = [str(entry['Name']) for entry in data['palette']]
    assert all(re.fullmatch(r'minecraft:[a-z0-9_/]+', name) for name in palette)
    result = {}
    for block in data['blocks']:
        position = tuple(map(int, block['pos']))
        assert all(0 <= coordinate < 16 for coordinate in position)
        assert position not in result and 'nbt' not in block
        result[position] = palette[int(block['state'])]
    assert len(result) == 4096
    return result


def supported(name):
    if name in {'minecraft:sea_lantern', 'minecraft:grass_block'}:
        return True
    return name not in AIR and not re.search(r'stairs|slab|pane|fence|chain|leaves|water|lava|trapdoor|button|carpet|rail|torch|lantern|sign|rod|bars|flower|grass|vine|snow$', name)


def features(mapping):
    return {position for position, name in mapping.items() if name not in AIR
            and 1 <= position[0] <= 14 and 2 <= position[1] <= 14 and 1 <= position[2] <= 14}


def audit_room(path, base):
    mapping = cells(path)
    def safe(point, height):
        east, level, south = point
        assert all(mapping.get((east, level + rise, south)) in AIR for rise in range(height)), f'{path}: blocked {point}'
        assert supported(mapping.get((east, level - 1, south), 'minecraft:air')), f'{path}: unsupported {point}'
    endpoints = []
    apertures = set()
    for port in base['ports']:
        for point in port['cells']:
            safe(point, 5)
            endpoints.append(tuple(point))
            east, level, south = point
            apertures.update((east, level + rise, south) for rise in range(5))
    for socket in base['sockets']:
        for field in ['position', 'spawn_position']:
            safe(socket[field], 3)
            endpoints.append(tuple(socket[field]))
    for position, name in mapping.items():
        east, level, south = position
        if level == 15 or (2 <= level <= 14 and (east in (0, 15) or south in (0, 15))):
            assert name not in AIR or position in apertures, f'{path}: shell hole {position}'
    walkable = set()
    for east in range(16):
        for level in range(2, 14):
            for south in range(16):
                if mapping[east, level, south] in AIR and mapping[east, level + 1, south] in AIR and supported(mapping[east, level - 1, south]):
                    walkable.add((east, level, south))
    start = endpoints[0]
    assert start in walkable
    pending = deque([start])
    visited = {start}
    while pending:
        east, level, south = pending.popleft()
        for delta_east, delta_south in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
            for delta_level in [-1, 0, 1]:
                candidate = (east + delta_east, level + delta_level, south + delta_south)
                if candidate not in walkable or candidate in visited:
                    continue
                if delta_level and mapping.get((east if delta_level > 0 else candidate[0], max(level, candidate[1]) + 1, south if delta_level > 0 else candidate[2])) not in AIR:
                    continue
                visited.add(candidate)
                pending.append(candidate)
    assert all(point in visited for point in endpoints), f'{path}: disconnected {[point for point in endpoints if point not in visited]}'
    return mapping, {'status': 'PASS', 'sha256': sha(path), 'denseBlocks': 4096, 'reachableEndpoints': len(endpoints), 'reachableWalkableCells': len(visited)}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--batch', type=int, required=True, choices=range(1, 6))
    parser.add_argument('--style', choices=REBUILD.STYLES)
    args = parser.parse_args()
    plan = json.loads((WORK / 'plan.json').read_text(encoding='utf-8'))
    selected = [entry for entry in plan['threads'] if not args.style or entry['style'] == args.style]
    results, comparisons, issues = [], [], []
    current = []
    for style in selected:
        name = style['style']
        directory = WORK / name / f'batch-{args.batch:02}'
        manifest_path = directory / 'manifest.json'
        manifest = json.loads(manifest_path.read_text(encoding='utf-8'))
        assert manifest['schema'] == 1 and manifest['style'] == 'fteclipsegen:' + name and manifest['batch'] == args.batch
        expected = {room['key']: room for room in style['rooms'] if room['batch'] == args.batch}
        entries = {room['key']: room for room in manifest['templates']}
        assert len(manifest['templates']) == 2 and entries.keys() == expected.keys()
        for filename in ['design.md', 'four-views.html', 'validation.json', 'style-audit.json', 'repetition-audit.json', 'self-audit.json', 'freeze.json']:
            assert (directory / filename).is_file(), f'{name}: missing {filename}'
            if filename.endswith('.json') and filename != 'freeze.json':
                assert REBUILD.audit_passed(json.loads((directory / filename).read_text(encoding='utf-8'))), f'{name}: {filename} failed'
        freeze = json.loads((directory / 'freeze.json').read_text(encoding='utf-8'))
        assert freeze['status'] == 'FROZEN_FOR_PARENT_INTEGRATION'
        assert sha(manifest_path) in json.dumps(freeze), f'{name}: manifest not frozen'
        frozen_files = freeze.get('files', freeze.get('artifacts', {}))
        if isinstance(frozen_files, list):
            frozen_files = {entry['file']: entry['sha256'] for entry in frozen_files}
        assert frozen_files, f'{name}: empty freeze file list'
        for filename, digest in frozen_files.items():
            frozen_path = (directory / filename).resolve()
            assert directory.resolve() in frozen_path.parents, f'{name}: invalid frozen path'
            assert sha(frozen_path) == digest, f'{name}: frozen artifact changed {filename}'
        prior = []
        for old_path in (DATA / 'roomsets' / name).rglob('*.nbt'):
            decoded = REBUILD.decode(old_path.read_bytes())
            if tuple(map(int, decoded['size'])) == (16, 16, 16):
                palette = [str(entry['Name']) for entry in decoded['palette']]
                prior.append((f'original/{old_path.relative_to(DATA / "roomsets" / name)}', features({tuple(map(int, block['pos'])): palette[int(block['state'])] for block in decoded['blocks']})))
        for earlier in range(1, args.batch):
            earlier_directory = WORK / name / f'batch-{earlier:02}'
            earlier_manifest = json.loads((earlier_directory / 'manifest.json').read_text(encoding='utf-8'))
            prior.extend((room['key'], features(cells(earlier_directory / room['file']))) for room in earlier_manifest['templates'])
        for key, entry in entries.items():
            assert entry['baseKey'] == expected[key]['baseKey'] and entry['name'] == expected[key]['name']
            path = (directory / entry['file']).resolve()
            assert directory.resolve() in path.parents and path.suffix == '.nbt'
            assert sha(path) == entry['sha256']
            assert entry['sha256'] in json.dumps(freeze), f'{name}/{key}: NBT not frozen'
            mapping, result = audit_room(path, REBUILD.VARIANTS[entry['baseKey']])
            signature = features(mapping)
            assert len(signature) >= 40, f'{name}/{key}: insufficient interior architecture'
            for other_key, other in prior:
                similarity = len(signature & other) / len(signature | other)
                record = {'style': name, 'key': key, 'comparedWith': other_key, 'jaccard': round(similarity, 6)}
                comparisons.append(record)
                if similarity >= 0.85:
                    issues.append(record)
            prior.append((key, signature))
            current.append((name + '/' + key, signature))
            results.append({'style': name, 'key': key, 'baseKey': entry['baseKey'], 'name': entry['name'], **result})
    for index, (first_key, first) in enumerate(current):
        for second_key, second in current[index + 1:]:
            if first_key.split('/')[0] == second_key.split('/')[0]:
                continue
            similarity = len(first & second) / len(first | second)
            record = {'first': first_key, 'second': second_key, 'jaccard': round(similarity, 6)}
            comparisons.append(record)
            if similarity >= 0.85:
                issues.append(record)
    report = {'status': 'FAIL' if issues else 'PASS', 'batch': args.batch, 'rooms': results, 'comparisons': len(comparisons), 'highestSimilarities': sorted(comparisons, key=lambda record: record['jaccard'], reverse=True)[:30], 'issues': issues, 'method': 'Decoded dense NBT; door/socket support and reachability; complete shell; interior solid occupancy ignores materials and excludes common exterior/floor/roof.'}
    destination = WORK / (f'{args.style}-batch-{args.batch:02}-acceptance.json' if args.style else f'batch-{args.batch:02}-acceptance.json')
    destination.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'status': report['status'], 'batch': args.batch, 'rooms': len(results), 'comparisons': len(comparisons), 'issues': issues}, ensure_ascii=False))
    assert not issues, 'Repetition requires redesign before next batch'


if __name__ == '__main__':
    main()
