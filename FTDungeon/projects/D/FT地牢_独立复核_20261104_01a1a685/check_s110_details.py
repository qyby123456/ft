import json
from pathlib import Path
from collections import Counter, deque
import audit
from verify_dense_partitions import records, verify

case = Path('S1-10-independent')
record = json.loads((case / 'file-audit.json').read_text(encoding='utf-8'))['groups'][0]
source = Path(record['candidate'])
root = source.parent
size, blocks = records(source)
assert size == [61, 13, 61] and audit.digest(source) == audit.digest(case / 'room.nbt')
metadata = json.loads(source.with_suffix('.json').read_text(encoding='utf-8'))
assert len(metadata['regions']) == 10 and len(metadata['body_route']) == 539
body = {tuple(point) for point in metadata['body_route']}
assert all(tuple(region['anchor']) in body for region in metadata['regions'])
roofs = {(east, south) for region in metadata['regions'] if region['roof'] for east in range(region['bounds'][0], region['bounds'][2] + 1) for south in range(region['bounds'][1], region['bounds'][3] + 1)}
assert len(roofs) == 1940
assert all(blocks[east, 12, south]['state']['Name'] not in audit.AIR for east, south in roofs)
assert all(blocks[33, height, 46]['state']['Name'] == 'minecraft:air' for height in range(2, 13))
entities = {position: entry['nbt'] for position, entry in blocks.items() if entry['nbt'] is not None}
assert len(entities) == 2 and set(entities) == {(47, 3, 7), (8, 2, 44)}
for (east, height, south), payload in entities.items():
    assert payload['id'] == 'minecraft:chest' and payload.get('Items', []) == [] and not payload.get('LootTable') and not payload.get('Lock')
    assert (east, height, south + 1) in body
    for across in range(east - 1, east + 2):
        for level in range(height + 1, height + 4):
            for forward in range(south - 1, south + 2):
                assert blocks[across, level, forward]['state']['Name'] == 'minecraft:air'
names = {position: entry['state']['Name'] for position, entry in blocks.items()}
distance = {position: 0 for position, name in names.items() if name.endswith('_log')}
queue = deque(distance)
while queue:
    east, height, south = queue.popleft()
    for adjacent in ((east-1,height,south),(east+1,height,south),(east,height-1,south),(east,height+1,south),(east,height,south-1),(east,height,south+1)):
        if adjacent not in distance and names.get(adjacent, '').endswith('_leaves'):
            distance[adjacent] = distance[east,height,south] + 1
            queue.append(adjacent)
leaves = [position for position, name in names.items() if name.endswith('_leaves')]
assert leaves and all(distance.get(position, 99) <= 6 for position in leaves)
assembly = json.loads((root / 'shards/ASSEMBLY.json').read_text(encoding='utf-8'))
normalized = {'source_sha256': assembly['source_nbt_sha256'], 'size': assembly['source_size'], 'part_count': len(assembly['parts']), 'parts': [{'step': part['order'], 'path': part['file'], 'relative_origin': part['offset_from_common_source_origin'], 'size': part['size'], 'sha256': part['sha256'], 'blocks': part['explicit_records'], 'air_blocks': part['air_records']} for part in assembly['parts']]}
assembly_path = case / 'normalized-assembly.json'
assembly_path.write_text(json.dumps(normalized, indent=2), encoding='utf-8')
partition = verify(source, assembly_path)
assert records(root / 'shards/reassembled-source.nbt') == (size, blocks)
render = json.loads((root / 'qa/render.json').read_text(encoding='utf-8'))
assert render['source_nbt_sha256'] == audit.digest(source) and len(render['views']) == 27
display_checks = []
for view in render['views']:
    mode = view['mode']
    removed = 0
    for east, height, south in blocks:
        cut = mode != 'full' and height >= 12
        if mode == 'plan' and height >= 8:
            cut |= any(region['bounds'][0] <= east <= region['bounds'][2] and region['bounds'][1] <= south <= region['bounds'][3] and (east in (region['bounds'][0], region['bounds'][2]) or south in (region['bounds'][1], region['bounds'][3])) for region in metadata['regions'])
        if height >= 2:
            cut |= mode == 'north' and south <= 1 or mode == 'south' and south >= 59 or mode == 'west' and east <= 1 or mode == 'east' and east >= 59
        removed += bool(cut)
    assert view['source_records'] == len(blocks) and view['displayed_records'] == len(blocks) - removed, (view['id'], mode)
    assert audit.digest(root / view['file']) == view['sha256']
    display_checks.append({'view': view['id'], 'mode': mode, 'record_count_matches_declared_cut_predicate': True})
native = json.loads((case / 'independent-native-frozen.json').read_text(encoding='utf-8'))[0]
route = json.loads((case / 'independent-routes.json').read_text(encoding='utf-8'))[0]
boundary = json.loads((case / 'independent-boundaries.json').read_text(encoding='utf-8'))[0]
assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS' and not native['survival_failures'] and not native['survival_unsupported']
assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets'])
assert len([target for target in route['targets'] if target['label'].startswith('body-')]) == 539
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and boundary['fluid_outside_or_air_neighbors_excluding_up_count'] == 0
result = {'status': 'PASS', 'source_sha256': audit.digest(source), 'size': size, 'native_selected': native['survival_checked'], 'body_targets': len(route['targets']), 'declared_body_targets': 539, 'regions': 10, 'complete_roof_columns': len(roofs), 'genuine_courtyard_sky_at': [33, 2, 46], 'empty_chests': 2, 'conservative_lid_air_cells': 54, 'leaves_with_log_distance_at_most_six': len(leaves), 'fluid_counts': boundary['fluid_counts'], 'partition': partition, 'display_record_count_checks': display_checks, 'scope': 'Static source, native collision and exact dense partition equality. Display counts and renderer source inspected; no derived NBT was written for display. World random ticks, actual player physics/GUI, runtime shard placement, gameplay and deployment NOT_RUN.'}
(case / 'independent-details.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
print(json.dumps({key: value for key, value in result.items() if key not in ('partition', 'display_record_count_checks')}, indent=2))
