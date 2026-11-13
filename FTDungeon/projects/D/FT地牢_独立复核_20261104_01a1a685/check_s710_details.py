import json
from collections import Counter
from pathlib import Path
import audit

folder = Path(__file__).resolve().parent / 'S7-10-independent'
source = audit.BASE / 'FT地牢_第7组逐图重做_20261104/10-overview'
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template = audit.decode(path)
    return template, {tuple(entry['pos']): {'state': template['palette'][entry['state']], 'nbt': entry.get('nbt')} for entry in template['blocks']}
template, records = mapping(folder / 'room.nbt')
native = read(folder / 'independent-native-frozen.json')[0]
route = read(folder / 'independent-routes.json')[0]
boundary = read(folder / 'independent-boundaries.json')[0]
assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS'
assert not native['survival_failures'] and not native['survival_unsupported']
assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets'])
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
assert boundary['fluid_counts'] == {'minecraft:water': 116}
reachable = {tuple(target['position']) for target in route['targets'] if target['reachable']}
layout = read(source / 'qa/topology.json')
assert len(layout['zones']) == 6 and len(layout['corridor_rectangles']) == 7
assert len({(zone['bounds'][2]-zone['bounds'][0]+1, zone['bounds'][3]-zone['bounds'][1]+1) for zone in layout['zones']}) == 6
sky = 0
for zone in layout['zones']:
    assert tuple(zone['target']) in reachable
    west,north,east,south = zone['bounds']
    for block_x in range(west,east+1):
        for block_z in range(north,south+1):
            opening = zone.get('skylight')
            in_sky = opening and opening[0] <= block_x <= opening[2] and opening[1] <= block_z <= opening[3]
            assert (records[block_x,zone['roof'],block_z]['state']['Name'] == 'minecraft:air') == bool(in_sky)
            sky += bool(in_sky)
assert sky == 396
ports = read(source / 'connections.json')['ports']
assert len(ports) == 12
for port in ports:
    assert tuple(port['feet']) in reachable
    assert all(records[tuple(position)]['state']['Name'] == 'minecraft:air' for position in port['air_cells'])
entities = {position: record['nbt'] for position,record in records.items() if record['nbt'] is not None}
assert Counter(payload['id'] for payload in entities.values()) == {'minecraft:chest':2,'minecraft:barrel':6,'minecraft:banner':3}
assert all(not payload.get('Items') and not payload.get('LootTable') and not payload.get('Lock') for payload in entities.values())
operations = read(source / 'qa/isolated-run-05/container-access.json')['operations']
assert len(operations) == 8
for operation in operations:
    position = tuple(operation['position'])
    assert position in entities and tuple(operation['access_feet']) in reachable
    east,height,south = position
    assert records[position]['state']['Properties']['facing'] == 'south'
    assert all(records[east,height+rise,south+1]['state']['Name'] == 'minecraft:air' for rise in range(2))
    if operation['kind'] == 'chest':
        assert records[east,height+1,south]['state']['Name'] == 'minecraft:air'
unused, derived = mapping(source / 'views/PREVIEW_ONLY_cutaway.nbt')
corridors = {(east,south) for rectangle in layout['corridor_rectangles'] for east in range(rectangle['cells'][0],rectangle['cells'][2]+1) for south in range(rectangle['cells'][1],rectangle['cells'][3]+1)}
changed = set()
for position, record in records.items():
    east,height,south = position
    rooms = [zone for zone in layout['zones'] if zone['bounds'][0] <= east <= zone['bounds'][2] and zone['bounds'][1] <= south <= zone['bounds'][3]]
    cut = any(height == zone['roof'] or (height > zone['floor'] and (east == zone['bounds'][0] or south == zone['bounds'][3])) for zone in rooms)
    cut |= height == 8 and (east,south) in corridors
    cut |= not rooms and (east,south) not in corridors and 3 <= height <= 8 and ((east+1,south) in corridors or (east,south-1) in corridors)
    expected = {'state':{'Name':'minecraft:air'}, 'nbt':None} if cut else record
    assert derived[position] == expected
    if derived[position] != record:
        changed.add(position)
assert changed == {tuple(position) for position in read(source / 'views/derivative.json')['changed_positions']}
result = {'status':'PASS_INDEPENDENT_STATIC_CHECKS_PENDING_FINAL_HANDOVER','native_selected':native['survival_checked'],'body_targets':len(route['targets']),'zones':6,'corridor_rectangles':7,'ports':12,'empty_chests':2,'empty_barrels':6,'banner_entities':3,'sealed_water':116,'true_skylight_columns':sky,'exact_air_only_cut_changes':len(changed),'scope':'Offline body shape/selected survival/source and partition equality. Dense parts overwrite the whole box including gaps. Rotation zero partition origins only. Real ticks, opening, world assembly/physics, gameplay and deployment NOT_RUN.'}
(folder / 'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result,indent=2))
