import json
from pathlib import Path
from collections import Counter
import audit
from verify_dense_partitions import records, verify

root = Path(r'D:\ft工程文件\FT地牢_第3组重做B_20261104\S3-10')
case = Path('S3-10-independent')
source = root / 'room.nbt'
size, blocks = records(source)
assert audit.digest(source) == audit.digest(case / 'room.nbt')
metadata = json.loads((root / 'room.json').read_text(encoding='utf-8'))
areas = metadata['areas']
def contains(area, east, south, margin=0):
    left, north, right, bottom = area['bounds']
    return left + margin <= east <= right - margin and north + margin <= south <= bottom - margin

roofs = {}
for east in range(size[0]):
    for south in range(size[2]):
        heights = [area['ceiling'] for area in areas if area['ceiling'] is not None and contains(area, east, south)]
        if heights:
            roofs[east, south] = max(heights)
            for height in (max(heights), max(heights) + 1):
                assert blocks[east, height, south]['state']['Name'] not in audit.AIR
assert len(roofs) == 4093
roof_claim = json.loads((root / 'roof-columns.json').read_text(encoding='utf-8'))
assert {(column['x'], column['z']): column['ceiling'] for column in roof_claim['columns']} == roofs
cut_size, cut = records(root / 'preview-only/preview-only-cutaway.nbt')
assert cut_size == size
changed = 0
for position, record in blocks.items():
    east, height, south = position
    ceiling = roofs.get((east, south))
    removed = (ceiling is not None and height >= ceiling) or (height >= 12 and any(area['role'] != 'corridor' and contains(area, east, south) and not contains(area, east, south, 2) for area in areas))
    if removed:
        assert cut[position]['state']['Name'] == 'minecraft:air' and cut[position]['nbt'] is None
        changed += record['state']['Name'] != 'minecraft:air'
    else:
        assert cut[position] == record
assert changed == 14784
entities = [(position, record) for position, record in blocks.items() if record['nbt'] is not None]
assert len(entities) == 1 and entities[0][0] == (76, 7, 15)
assert entities[0][1]['nbt']['Items'] == [] and 'LootTable' not in entities[0][1]['nbt']
for height in range(8, 12):
    assert blocks[76, height, 15]['state']['Name'] == 'minecraft:air'
counts = Counter(record['state']['Name'] for record in blocks.values())
assert not any(counts[name] for name in ('minecraft:water', 'minecraft:lava', 'minecraft:ice', 'minecraft:snow'))
parts = json.loads((root / 'fragments.json').read_text(encoding='utf-8'))
normalized = {'source_sha256': parts['source_sha256'], 'size': parts['source_size'], 'part_count': len(parts['fragments']), 'parts': [{'step': part['order'], 'path': str(root / part['file']), 'relative_origin': part['origin'], 'size': part['size'], 'sha256': part['sha256'], 'blocks': part['records'], 'air_blocks': part['explicit_air_records']} for part in parts['fragments']]}
assembly = case / 'normalized-assembly.json'
assembly.write_text(json.dumps(normalized, indent=2), encoding='utf-8')
partition = verify(source, assembly)
assert records(root / 'reassembled.nbt') == (size, blocks)
routes = json.loads((case / 'independent-routes.json').read_text(encoding='utf-8'))
assert all(route['start_clear'] and all(target['reachable'] and target['standing_clear'] for target in route['targets']) for route in routes)
native = json.loads((case / 'independent-native-frozen.json').read_text(encoding='utf-8'))
assert all(item['registry_properties'] == 'PASS' and item['template_roundtrip_block_count'] == 'PASS' and not item['survival_failures'] and not item['survival_unsupported'] for item in native)
result = {'status': 'PASS', 'source_sha256': audit.digest(source), 'size': size, 'roof_columns_two_layers': len(roofs), 'preview_air_only_changes': changed, 'empty_chests_with_lid_air': len(entities), 'route_targets': sum(len(route['targets']) for route in routes), 'native_survival_checked': sum(item['survival_checked'] for item in native), 'materials': dict(counts), 'partition': partition, 'world_placement_ticks_gameplay': 'NOT_RUN'}
(case / 'independent-details.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
print(json.dumps({key: value for key, value in result.items() if key not in ('materials', 'partition')}, indent=2))
