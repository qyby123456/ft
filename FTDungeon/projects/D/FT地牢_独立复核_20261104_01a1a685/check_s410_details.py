import json
from pathlib import Path
from collections import Counter
import audit
from verify_dense_partitions import records, verify

root = audit.BASE / 'FT地牢_第4组逐图返工_20261104/S4-10'
case = Path('S4-10-independent')
source = root / 'incoming/S4-10/room.nbt'
size, blocks = records(source)
assert audit.digest(source) == audit.digest(case / 'room.nbt')
metadata = json.loads(source.with_suffix('.json').read_text(encoding='utf-8'))
entities = {position: record['nbt'] for position, record in blocks.items() if record['nbt'] is not None}
assert Counter(payload['id'] for payload in entities.values()) == {'minecraft:chest': 1, 'minecraft:barrel': 4}
assert all(payload.get('Items', []) == [] and not payload.get('LootTable') and not payload.get('Lock') for payload in entities.values())
assert entities[62, 6, 15]['id'] == 'minecraft:chest'
for east in range(61, 64):
    for height in (7, 8):
        for south in range(14, 17):
            assert blocks[east, height, south]['state']['Name'] == 'minecraft:air'
assert all(blocks[62, height, 16]['state']['Name'] == 'minecraft:air' for height in range(6, 9))
names = {position: record['state']['Name'] for position, record in blocks.items()}
counts = Counter(names.values())
leaves = [record['state'] for record in blocks.values() if record['state']['Name'].endswith('_leaves')]
assert len(leaves) == 74 and all(state['Properties']['persistent'] == 'true' for state in leaves)
assert counts['minecraft:lily_pad'] == 46
for (east, height, south), name in names.items():
    if name == 'minecraft:lily_pad':
        assert names[east, height - 1, south] == 'minecraft:water'
sky = {}
assert len(metadata['regions']) == 7
for region in metadata['regions']:
    west, north, east, south = region['bounds_xz']
    columns = [(column, row) for column in range(west + 3, east - 2) for row in range(north + 3, south - 2)]
    assert all(names[column, height, row] == 'minecraft:air' for column, row in columns for height in range(17, 22))
    sky[region['id']] = len(columns)
split = json.loads((root / 'split-check.json').read_text(encoding='utf-8'))
normalized = {'source_sha256': split['source_sha256'], 'size': split['source_size'], 'part_count': len(split['parts']), 'parts': [{'step': part['order'], 'path': str(root / part['path'].replace('\\', '/')), 'relative_origin': part['origin'], 'size': part['size'], 'sha256': part['sha256'], 'blocks': part['explicit_records'], 'air_blocks': part['explicit_air_records']} for part in split['parts']]}
assembly_path = case / 'normalized-assembly.json'
assembly_path.write_text(json.dumps(normalized, indent=2), encoding='utf-8')
partition = verify(source, assembly_path)
cut_size, cut = records(root / 'preview-only/upper-cutaway.nbt')
assert cut_size == size
changed = 0
for position, record in blocks.items():
    if position[1] >= 10:
        assert cut[position] == {'state': {'Name': 'minecraft:air'}, 'nbt': None}
        changed += record['state']['Name'] != 'minecraft:air'
    else:
        assert cut[position] == record
assert changed == 1480
native = json.loads((case / 'independent-native-frozen.json').read_text(encoding='utf-8'))[0]
route = json.loads((case / 'independent-routes.json').read_text(encoding='utf-8'))[0]
boundary = json.loads((case / 'independent-boundaries.json').read_text(encoding='utf-8'))[0]
assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS' and not native['survival_failures'] and not native['survival_unsupported']
assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets'])
assert len([target for target in route['targets'] if target['label'].startswith('route_waypoints-')]) == 42
assert all(set(component['materials']) == {'minecraft:lily_pad'} for component in boundary['solids_not_connected_to_y0_by_six_face_adjacency'])
assert boundary['fluid_counts'] == {'minecraft:water': 333} and boundary['fluid_outside_or_air_neighbors_excluding_up_count'] == 0
result = {'status': 'PASS', 'source_sha256': audit.digest(source), 'size': size, 'native_selected': native['survival_checked'], 'body_targets': len(route['targets']), 'declared_waypoints': 42, 'region_sky_columns_y17_21': sky, 'empty_chests': 1, 'empty_barrels': 4, 'persistent_leaves': 74, 'water_supported_lilies': 46, 'sealed_water': 333, 'precise_preview_air_changes': changed, 'partition': partition, 'world_ticks_real_interaction_physics_gameplay_deployment': 'NOT_RUN'}
(case / 'independent-details.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
print(json.dumps({key: value for key, value in result.items() if key != 'partition'}, indent=2))
