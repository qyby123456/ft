import json
from collections import Counter
from pathlib import Path
import audit

folder = Path(__file__).resolve().parent / 'S8-09-independent'
source = audit.BASE / 'FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-09'
def mapping(path):
    template = audit.decode(path)
    return template, {tuple(entry['pos']): {'state': template['palette'][entry['state']], 'nbt': entry.get('nbt')} for entry in template['blocks']}
template, records = mapping(folder / 'room.nbt')
blocks = {position: record['state'] for position, record in records.items()}
native = json.loads((folder / 'independent-native-frozen.json').read_text())[0]
route = json.loads((folder / 'independent-routes.json').read_text())[0]
boundary = json.loads((folder / 'independent-boundaries.json').read_text())[0]
assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS'
assert not native['survival_failures'] and not native['survival_unsupported']
assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets'])
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_counts']
assert len(records) == 29 * 17 * 27 and not template.get('entities')
entities = {position: record['nbt'] for position, record in records.items() if record['nbt'] is not None}
assert Counter(payload['id'] for payload in entities.values()) == {'minecraft:chest': 1, 'minecraft:barrel': 4, 'minecraft:banner': 3}
assert all(not payload.get('Items') and not payload.get('LootTable') and not payload.get('Lock') for payload in entities.values())
assert blocks[14,5,20]['Name'] == 'minecraft:chest' and blocks[14,5,20]['Properties']['facing'] == 'north'
assert all(blocks[east,height,south]['Name'] == 'minecraft:air' for east in range(13,16) for height in (6,7) for south in range(19,22))
assert all(blocks[14,height,20]['Name'] == 'minecraft:air' for height in range(6,16))
assert all(blocks[14,height,19]['Name'] == 'minecraft:air' for height in range(5,8))
for east in (0,28):
    for south in range(8,13):
        assert blocks[east,1,south]['Name'] not in audit.AIR
        assert all(blocks[east,height,south]['Name'] == 'minecraft:air' for height in range(2,5))
assert all(blocks[east,16,south]['Name'] not in audit.AIR for east in range(29) for south in range(27))
assert sum(state['Name'].endswith('_stairs') for state in blocks.values()) == 15
rules = json.loads((source / 'qa/cut-rules.json').read_text())
for direction, boxes in rules.items():
    derivative, derived = mapping(source / f'views/{direction}/inspection-only-{direction}.nbt')
    assert derivative['size'] == template['size'] and derived.keys() == records.keys()
    for position, record in records.items():
        cut = any(all(first[axis] <= position[axis] <= last[axis] for axis in range(3)) for first,last in boxes)
        expected = {'state': {'Name': 'minecraft:air'}, 'nbt': None} if cut else record
        assert derived[position] == expected, (direction,position)
result = {'status': 'PASS', 'native_selected': native['survival_checked'], 'body_targets': len(route['targets']), 'empty_chests': 1, 'empty_barrels': 4, 'banner_entities': 3, 'roof_columns': 783, 'stairs': 15, 'supported_5x3_ports': 2, 'exact_box_air_only_derivatives': 4, 'scope': 'Independent native static source and collision audit; world ticks, entity obstruction, real container GUI/raycast, physics, gameplay and deployment NOT_RUN. Pure color banners and simplified statues/crystals remain declared approximations.'}
(folder / 'independent-details.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
print(json.dumps(result, indent=2))
