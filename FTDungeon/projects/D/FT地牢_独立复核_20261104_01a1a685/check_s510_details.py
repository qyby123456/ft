import json
from pathlib import Path
from collections import Counter
import audit
from verify_dense_partitions import records, verify

root = audit.BASE / 'FT地牢_第5组逐图重做_20261104/10-coral-ocean-complex'
case = Path('S5-10-independent')
source = root / 'nbt/s5-10-coral-ocean-complex.nbt'
size, blocks = records(source)
assert audit.digest(source) == audit.digest(case / 'room.nbt')
water = {'Name': 'minecraft:water', 'Properties': {'level': '0'}}
entities = {position: record['nbt'] for position, record in blocks.items() if record['nbt'] is not None}
assert Counter(payload['id'] for payload in entities.values()) == {'minecraft:chest': 2, 'minecraft:barrel': 4}
assert all(payload['Items'] == [] and not payload.get('LootTable') and not payload.get('Lock') for payload in entities.values())
routes = json.loads((root / 'qa/connectivity.json').read_text(encoding='utf-8'))
assert len(routes['paths']) == 27
approaches = routes['container_fronts']
assert len(approaches) == 6
for route in routes['paths'] + approaches:
    points = route['path']
    assert points[0] == [36, 3, 64] and points[-1] == route['target']
    assert all(point[1] in (3, 4) for point in points)
    assert points and all(sum(abs(first[axis] - last[axis]) for axis in range(3)) == 1 for first, last in zip(points, points[1:]))
    assert all(blocks[east, height + rise, south]['state'] == water for east, height, south in points for rise in range(2))
checks = []
for turn, name in enumerate(['NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90']):
    capture = audit.decode(root / f'worldtest/{name}/after-1200-ticks.nbt')
    captured = {tuple(entry['pos']): {'state': capture['palette'][entry['state']], 'nbt': entry.get('nbt')} for entry in capture['blocks']}
    def at(position, rotate=False):
        east, height, south = position
        if rotate:
            for rotation in range(turn):
                east, south = 64 - south, east
        return captured[east + 8, height, south + 8]
    for east in range(-8, 73):
        for south in range(-8, 73):
            assert all(at((east, height, south))['state']['Name'] not in audit.AIR for height in range(3))
            if east < 0 or east > 64 or south < 0 or south > 64:
                assert all(at((east, height, south))['state'] == water for height in range(3, 22))
            assert all(at((east, height, south))['state'] == water for height in range(22, 30))
    for side in ('south', 'east'):
        for depth in range(-3, 3):
            for across in range(-2, 3):
                east, south = (36 + across, 64 - depth) if side == 'south' else (64 - depth, 36 + across)
                assert at((east, 2, south), True)['state']['Name'] not in audit.AIR
                assert all(at((east, height, south), True)['state'] == water for height in range(3, 6))
    for route in routes['paths'] + approaches:
        for east, height, south in route['path']:
            assert all(at((east, height + rise, south), True)['state'] == water for rise in range(2))
    for position, payload in entities.items():
        loaded = at(position, True)['nbt']
        assert {key: value for key, value in loaded.items() if key not in {'x', 'y', 'z'}} == payload
        if payload['id'] == 'minecraft:chest':
            east, height, south = position
            assert all(at((east, height + rise, south), True)['state'] == water for rise in range(1, 4))
    checks.append({'rotation': name, 'eight_block_ring_and_cover': 'PASS', 'solid_bed_layers': 3, 'wet_supported_ports': 2, 'low_wet_routes': 27, 'container_approaches': 6, 'exact_container_payloads': 6})
assembly = json.loads((root / 'qa/shards.json').read_text(encoding='utf-8'))
normalized = {'source_sha256': assembly['source_sha256'], 'size': assembly['source_size'], 'part_count': len(assembly['shards']), 'parts': [{'step': part['order'], 'path': str(root / 'datapack/data/ft_s5_redo/structures' / (part['id'].split(':')[1] + '.nbt')), 'relative_origin': part['origin'], 'size': part['size'], 'sha256': part['sha256'], 'blocks': part['records']} for part in assembly['shards']]}
assembly_path = case / 'normalized-assembly.json'
assembly_path.write_text(json.dumps(normalized, indent=2), encoding='utf-8')
partition = verify(source, assembly_path)
cut_size, derived = records(root / 'preview-only/preview-only-s5-10-water-hidden-not-importable.nbt')
assert cut_size == size
removed = changed = 0
for position, record in blocks.items():
    expected = json.loads(json.dumps(record))
    if expected['state']['Name'] == 'minecraft:water':
        expected['state'] = {'Name': 'minecraft:air'}
        removed += 1
    elif expected['state'].get('Properties', {}).get('waterlogged') == 'true':
        expected['state']['Properties']['waterlogged'] = 'false'
        changed += 1
    assert derived[position] == expected
assert (removed, changed) == (74394, 29)
native = json.loads((case / 'independent-native-frozen.json').read_text(encoding='utf-8'))[0]
route = json.loads((case / 'independent-routes.json').read_text(encoding='utf-8'))[0]
assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS' and not native['survival_failures'] and not native['survival_unsupported']
assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets'])
result = {'status': 'PASS', 'source_sha256': audit.digest(source), 'native_selected': native['survival_checked'], 'offline_body_targets': len(route['targets']), 'captures': checks, 'partition': partition, 'derivative_water_to_air': removed, 'derivative_waterlogged_to_false': changed, 'scope': 'Independent redecoding of producer-executed finite tank tests plus offline collision; not a new server run, player physics, GUI, runtime shard placement or deployment. Permanent ocean integration contract required.'}
(case / 'independent-details.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
print(json.dumps({key: value for key, value in result.items() if key != 'partition'}, indent=2))
