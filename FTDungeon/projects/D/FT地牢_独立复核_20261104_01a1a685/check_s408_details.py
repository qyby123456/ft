import json
from collections import deque
from pathlib import Path
import audit

folder = Path(__file__).resolve().parent / 'S4-08-independent'
source = audit.BASE / 'FT地牢_第4组逐图返工_20261104/S4-08'
template = audit.decode(folder / 'room.nbt')
blocks = {tuple(entry['pos']): template['palette'][entry['state']] for entry in template['blocks']}
air = {'Name': 'minecraft:air'}
def state(position):
    return blocks.get(tuple(position), air)
native = json.loads((folder / 'independent-native-frozen.json').read_text())[0]
route = json.loads((folder / 'independent-routes.json').read_text())[0]
assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS'
assert not native['survival_failures'] and not native['survival_unsupported']
assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets'])
assert not template.get('entities') and not any('nbt' in entry for entry in template['blocks'])
leaves = [entry for entry in blocks.values() if entry['Name'].endswith('_leaves')]
assert leaves and all(entry['Properties']['persistent'] == 'true' for entry in leaves)
pads = [position for position, entry in blocks.items() if entry['Name'] == 'minecraft:lily_pad']
assert len(pads) == 23 and all(state((east, height - 1, south))['Name'] == 'minecraft:water' for east, height, south in pads)
wood = {position for position, entry in blocks.items() if entry['Name'].endswith(('_log', '_wood'))}
visited = {(17, 10, 17)}
assert visited <= wood
pending = deque(visited)
deltas = [(1,0,0), (-1,0,0), (0,1,0), (0,-1,0), (0,0,1), (0,0,-1)]
while pending:
    position = pending.popleft()
    for delta in deltas:
        neighbor = tuple(coordinate + offset for coordinate, offset in zip(position, delta))
        if neighbor in wood and neighbor not in visited:
            visited.add(neighbor)
            pending.append(neighbor)
assert {(12,5,16), (23,5,19), (16,5,11), (17,5,23)} <= visited
edges = set()
for position, entry in blocks.items():
    if entry['Name'] != 'minecraft:water':
        continue
    for delta in [deltas[index] for index in (0,1,3,4,5)]:
        neighbor = tuple(coordinate + offset for coordinate, offset in zip(position, delta))
        if state(neighbor)['Name'] in {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}:
            edges.add((position, neighbor))
assert edges == {((east, height, 28), (east, height, 29)) for east in range(6, 9) for height in range(5, 8)}
assert sum(entry['Name'].endswith('_stairs') for entry in blocks.values()) == 24
boundary = json.loads((folder / 'independent-boundaries.json').read_text())[0]
assert all(set(component['materials']) == {'minecraft:lily_pad'} for component in boundary['solids_not_connected_to_y0_by_six_face_adjacency'])
derivative = audit.decode(source / 'preview-only/upper-cutaway.nbt')
derived = {tuple(entry['pos']): derivative['palette'][entry['state']] for entry in derivative['blocks']}
assert derivative['size'] == template['size']
for east in range(35):
    for height in range(27):
        for south in range(35):
            assert derived.get((east, height, south), air) == (air if height >= 14 else state((east, height, south)))
result = {'status': 'PASS', 'native_selected': native['survival_checked'], 'body_targets': len(route['targets']), 'stairs': 24, 'water_supported_lily_pads': len(pads), 'persistent_leaf_blocks': len(leaves), 'wood_only_root_endpoints_connected': 4, 'intentional_waterfall_air_faces': len(edges), 'unexpected_water_side_bottom_air_faces': 0, 'display_cut_only_y14_and_above': True, 'scope': 'Static original full source and native body shape checks; waterfall scheduling, weather/light, real movement, gameplay and deployment NOT_RUN. Water amount existence does not establish a majority flooded footprint.'}
(folder / 'independent-details.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
print(json.dumps(result, indent=2))
