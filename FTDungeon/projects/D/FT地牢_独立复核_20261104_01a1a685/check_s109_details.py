import json
from pathlib import Path
from collections import Counter, deque
import audit
from verify_dense_partitions import records

case = Path('S1-09-independent')
size, blocks = records(case / 'room.nbt')
assert size == [29, 17, 29]
entities = {position: record['nbt'] for position, record in blocks.items() if record['nbt'] is not None}
assert Counter(payload['id'] for payload in entities.values()) == {'minecraft:chest': 1, 'minecraft:banner': 2}
assert entities[14, 5, 10].get('Items', []) == [] and not entities[14, 5, 10].get('LootTable') and not entities[14, 5, 10].get('Lock')
assert blocks[14, 5, 10]['state']['Properties']['facing'] == 'south'
for east in range(13, 16):
    for height in range(6, 9):
        for south in range(9, 12):
            assert blocks[east, height, south]['state']['Name'] == 'minecraft:air'
assert all(blocks[14, height, 11]['state']['Name'] == 'minecraft:air' for height in range(5, 8))
assert all(blocks[east, 16, south]['state']['Name'] not in audit.AIR for east in range(29) for south in range(29))
names = {position: record['state']['Name'] for position, record in blocks.items()}
counts = Counter(names.values())
assert counts['minecraft:carved_pumpkin'] == 2 and counts['minecraft:stone_brick_slab'] == 15
distance = {position: 0 for position, name in names.items() if name.endswith('_log')}
queue = deque(distance)
while queue:
    east, height, south = queue.popleft()
    for adjacent in ((east-1,height,south),(east+1,height,south),(east,height-1,south),(east,height+1,south),(east,height,south-1),(east,height,south+1)):
        if adjacent not in distance and names.get(adjacent, '').endswith('_leaves'):
            distance[adjacent] = distance[east,height,south] + 1
            queue.append(adjacent)
leaves = [position for position, name in names.items() if name.endswith('_leaves')]
assert len(leaves) == 171 and all(distance.get(position, 99) <= 6 for position in leaves)
native = json.loads((case / 'independent-native-frozen.json').read_text(encoding='utf-8'))[0]
route = json.loads((case / 'independent-routes.json').read_text(encoding='utf-8'))[0]
boundary = json.loads((case / 'independent-boundaries.json').read_text(encoding='utf-8'))[0]
assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS' and not native['survival_failures'] and not native['survival_unsupported']
assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets'])
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and boundary['fluid_counts'] == {'minecraft:water': 6} and boundary['fluid_outside_or_air_neighbors_excluding_up_count'] == 0
metadata_path = Path(json.loads((case / 'file-audit.json').read_text(encoding='utf-8'))['groups'][0]['candidate']).with_suffix('.json')
metadata = json.loads(metadata_path.read_text(encoding='utf-8'))
assert len(metadata['body_route']) == 226
assert {tuple(point) for point in metadata['upper_targets'] + metadata['stair_targets']} <= {tuple(point) for point in metadata['body_route']}
assert len([target for target in route['targets'] if target['label'].startswith('body-')]) == 226
result = {'status': 'PASS', 'source_sha256': audit.digest(case / 'room.nbt'), 'native_survival_checked': native['survival_checked'], 'body_targets_including_ports': len(route['targets']), 'declared_body_targets': 226, 'complete_roof_columns': 841, 'empty_chests': 1, 'banner_entities': 2, 'lid_air_cells': 27, 'supported_leaves': len(leaves), 'sealed_water': 6, 'half_step_slabs': 15, 'scope': 'Independent offline native source and collision checks. World tick behavior, actual raycast/chest opening/menu, physics, gameplay and deployment NOT_RUN.'}
(case / 'independent-details.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
print(json.dumps(result, indent=2))
