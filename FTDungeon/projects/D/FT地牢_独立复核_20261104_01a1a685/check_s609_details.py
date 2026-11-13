import json
from pathlib import Path
import audit

folder = Path(__file__).resolve().parent / 'S6-09-independent'
source = audit.BASE / 'FT地牢_Image25接入_20261101/deliveries/S6-redo-single-20261104-01/09-treasure'
def mapping(path):
    template = audit.decode(path)
    return template, {tuple(entry['pos']): {'state': template['palette'][entry['state']], 'nbt': entry.get('nbt')} for entry in template['blocks']}
template, records = mapping(folder / 'room.nbt')
blocks = {position: record['state'] for position, record in records.items()}
metadata = json.loads((source / 'room.json').read_text(encoding='utf-8'))
native = json.loads((folder / 'independent-native-frozen.json').read_text())[0]
route = json.loads((folder / 'independent-routes.json').read_text())[0]
boundary = json.loads((folder / 'independent-boundaries.json').read_text())[0]
assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS'
assert not native['survival_failures'] and not native['survival_unsupported']
assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets'])
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
assert sum(record['nbt'] is not None for record in records.values()) == 3 and not template.get('entities')
for container in metadata['containers']:
    east, height, south = container['position']
    record = records[east, height, south]
    payload = record['nbt']
    assert record['state']['Name'] == payload['id'] == 'minecraft:chest'
    assert record['state']['Properties']['facing'] == 'north'
    assert not payload.get('Items') and not payload.get('LootTable') and not payload.get('Lock')
    assert all(blocks[east, height + rise, south]['Name'] == 'minecraft:air' for rise in (1, 2))
    front = container['approach']
    assert all(blocks[front[0], front[1] + rise, front[2]]['Name'] == 'minecraft:air' for rise in range(3))
for port in metadata['connectors']:
    east, height, south = port['position']
    for inward in range(3):
        for offset in range(-1, 2):
            horizontal = {'north': (east + offset, south + inward), 'west': (east + inward, south + offset), 'east': (east - inward, south + offset)}[port['facing']]
            assert blocks[horizontal[0], height - 1, horizontal[1]]['Name'] not in audit.AIR
            assert all(blocks[horizontal[0], height + rise, horizontal[1]]['Name'] == 'minecraft:air' for rise in range(5))
lava = [position for position, state in blocks.items() if state['Name'] == 'minecraft:lava']
assert len(lava) == 100
for position in lava:
    assert blocks[position].get('Properties', {}).get('level', '0') == '0'
    for delta in [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]:
        state = blocks[tuple(coordinate + offset for coordinate, offset in zip(position, delta))]
        assert state['Name'] not in audit.AIR or state['Name'] == 'minecraft:lava'
assert all(blocks[east, 17, south]['Name'] not in audit.AIR for east in range(33) for south in range(8, 35))
assert sum(state['Name'].endswith('_stairs') for state in blocks.values()) == 28
unused, cut = mapping(source / 'lodestone/inspection-cutaway.nbt')
assert cut == {position: record for position, record in records.items() if not (position[1] >= 16 or (position[1] >= 5 and (position[0] <= 2 or position[2] <= 10)))}
unused, detail = mapping(source / 'lodestone/treasure-dais-detail.nbt')
assert detail == {tuple(position[index] - [9, 4, 19][index] for index in range(3)): record for position, record in records.items() if all([9, 4, 19][index] <= position[index] <= [23, 15, 31][index] for index in range(3))}
result = {'status': 'PASS', 'native_selected': native['survival_checked'], 'body_targets': len(route['targets']), 'empty_chests': 3, 'clear_3x5x3_ports': 3, 'sealed_source_lava': 100, 'roof_columns': 891, 'stairs': 28, 'exact_source_record_cut_and_translated_detail': True, 'scope': 'Offline only; actual opening, heat/fire, world ticks, physics, gameplay and deployment NOT_RUN.'}
(folder / 'independent-details.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
print(json.dumps(result, indent=2))
