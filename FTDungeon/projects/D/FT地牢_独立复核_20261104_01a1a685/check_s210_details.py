import json
from collections import Counter
from pathlib import Path
import audit
from verify_dense_partitions import verify

folder = Path(__file__).resolve().parent / 'S2-10-independent'
source = audit.BASE / 'FT地牢_第2组重做B_20261104/S2-10'
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template = audit.decode(path)
    return template,{tuple(entry['pos']):{'state':template['palette'][entry['state']],'nbt':entry.get('nbt')} for entry in template['blocks']}
template, records = mapping(folder / 'room.nbt')
native = read(folder / 'independent-native-frozen.json')[0]
route = read(folder / 'independent-routes.json')[0]
boundary = read(folder / 'independent-boundaries.json')[0]
assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS'
assert not native['survival_failures'] and not native['survival_unsupported']
assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets'])
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
assert boundary['fluid_counts'] == {'minecraft:water':40}
metadata = read(source / 'incoming/G2B-10/room.json')
reachable = {tuple(target['position']) for target in route['targets']}
assert len(metadata['regions']) == 8 and len(metadata['internal_links']) == 10
assert all(tuple(region['center']) in reachable for region in metadata['regions'])
for link in metadata['internal_links']:
    assert tuple(link['center']) in reachable
    first,last = link['bounds_xz_inclusive']
    for east in range(first[0],last[0]+1):
        for south in range(first[1],last[1]+1):
            across = south if link['axis'] == 'x' else east
            center_across = link['center'][2] if link['axis'] == 'x' else link['center'][0]
            if abs(across-center_across) > link['clear_width']//2:
                continue
            height = link['feet_y']
            assert records[east,height-1,south]['state']['Name'] not in audit.AIR
            assert all(records[east,height+rise,south]['state']['Name'] == 'minecraft:air' for rise in range(link['clear_height']))
entities = {position:record['nbt'] for position,record in records.items() if record['nbt'] is not None}
assert Counter(payload['id'] for payload in entities.values()) == {'minecraft:chest':5,'minecraft:barrel':2}
for chest in metadata['chests']:
    position = tuple(chest['position'])
    payload = entities[position]
    assert records[position]['state']['Properties']['facing'] == chest['facing']
    assert not payload.get('LootTable') and not payload.get('Lock')
    items = payload['Items']
    assert len(items) == chest['expected_slots'] and len({item['Slot'] for item in items}) == len(items)
    assert all(0 <= item['Slot'] < 27 for item in items)
    assert sorted((item['id'].removeprefix('minecraft:'),item['Count']) for item in items) == sorted(tuple(item) for item in chest['expected_items'])
    east,height,south = position
    assert all(records[east,height+rise,south]['state']['Name'] == 'minecraft:air' for rise in (1,2))
    front = chest['front_feet']
    assert tuple(front) in reachable
    assert all(records[front[0],front[1]+rise,front[2]]['state']['Name'] == 'minecraft:air' for rise in range(3))
assert all(not payload.get('Items') and not payload.get('LootTable') for payload in entities.values() if payload['id'] == 'minecraft:barrel')
for roof in metadata['roof']:
    region = next(region for region in metadata['regions'] if region['id'] == roof['region'])
    left,unused,front = region['bounds_inclusive'][0]
    right,unused,back = region['bounds_inclusive'][1]
    covered = sum(records[east,roof['top_y'],south]['state']['Name'] != 'minecraft:air' for east in range(left,right+1) for south in range(front,back+1))
    sky = sum(all(records[east,height,south]['state']['Name'] == 'minecraft:air' for height in range(roof['top_y'],18)) for east in range(left+1,right) for south in range(front+1,back))
    assert covered == roof['covered_columns_at_top'] and sky == roof['true_open_sky_inner_columns']
for item in read(source / 'derivation.json')['derivatives']:
    unused,derived = mapping(Path(item['derived']))
    assert derived.keys() == records.keys()
    removed = set()
    for position,record in records.items():
        east,height,south = position
        near = {'north':south<=3,'south':south>=64,'west':east<=3,'east':east>=62}[item['direction']]
        cut = height>=10 or (height>=3 and near)
        expected = {'state':{'Name':'minecraft:air'},'nbt':None} if cut else record
        assert derived[position] == expected
        if cut and record['state']['Name'] != 'minecraft:air':
            removed.add(position)
    assert removed == {tuple(position) for position in item['removed_positions']} and len(removed) == item['removed_nonair']
    assert {position:record['nbt'] for position,record in derived.items() if record['nbt'] is not None} == entities
assembly = read(source / 'assembly.json')
normalized = {'source_sha256':assembly['source_sha256'],'size':assembly['full_size'],'part_count':len(assembly['tiles']),'parts':[{'step':part['step'],'path':part['path'],'relative_origin':part['offset'],'size':part['size'],'sha256':part['sha256'],'blocks':part['explicit_cells'],'air_blocks':part['explicit_air_cells']} for part in assembly['tiles']]}
assembly_path = folder / 'normalized-assembly.json'
assembly_path.write_text(json.dumps(normalized,indent=2),encoding='utf-8')
partitions = verify(folder / 'room.nbt',assembly_path)
(folder / 'independent-partitions.json').write_text(json.dumps(partitions,indent=2),encoding='utf-8')
result = {'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','native_selected':native['survival_checked'],'body_targets':len(route['targets']),'regions':8,'clear_floor_supported_links':10,'true_chests_fixed_example_item_stacks':10,'empty_barrels':2,'sealed_water':40,'exact_air_only_cutaways':4,'dense_reassembled_records':partitions['records'],'scope':'Independent offline native body shapes/selected survival and decoded state/entity equality. Fixed example inventories are not loot balance certification. Weather/light, actual interaction, world updates, shard assembly and deployment NOT_RUN.'}
(folder / 'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result,indent=2))
