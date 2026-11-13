import json
from pathlib import Path
import audit
from verify_dense_partitions import verify

folder = Path(__file__).resolve().parent / 'S6-10-independent'
source = audit.BASE / 'FT地牢_Image25接入_20261101/deliveries/S6-redo-single-20261104-01/10-overview'
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template = audit.decode(path)
    return template, {tuple(entry['pos']): {'state':template['palette'][entry['state']], 'nbt':entry.get('nbt')} for entry in template['blocks']}
template, records = mapping(folder / 'room.nbt')
native = read(folder / 'independent-native-frozen.json')[0]
route = read(folder / 'independent-routes.json')[0]
boundary = read(folder / 'independent-boundaries.json')[0]
assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS'
assert not native['survival_failures'] and not native['survival_unsupported']
assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets'])
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
metadata = read(source / 'room.json')
reachable = {tuple(target['position']) for target in route['targets']}
assert len(metadata['regions']) == 8 and all(tuple(region['target']) in reachable for region in metadata['regions'])
entities = {position:record['nbt'] for position,record in records.items() if record['nbt'] is not None}
assert len(entities) == 3 and not template.get('entities')
for container in metadata['containers']:
    position = tuple(container['position'])
    payload = entities[position]
    assert payload['id'] == records[position]['state']['Name'] == 'minecraft:chest'
    assert not payload.get('Items') and not payload.get('LootTable') and not payload.get('Lock')
    east,height,south = position
    assert all(records[east,height+rise,south]['state']['Name'] == 'minecraft:air' for rise in (1,2))
    front = container['approach']
    assert tuple(front) in reachable
    assert all(records[front[0],front[1]+rise,front[2]]['state']['Name'] == 'minecraft:air' for rise in range(3))
lava = [position for position,record in records.items() if record['state']['Name'] == 'minecraft:lava']
assert len(lava) == 179
for position in lava:
    assert records[position]['state'].get('Properties',{}).get('level','0') == '0'
    for delta in [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]:
        adjacent = tuple(value+delta[axis] for axis,value in enumerate(position))
        name = records[adjacent]['state']['Name']
        assert name not in audit.AIR or name == 'minecraft:lava'
roof_columns = 0
assert all(records[east,height,south]['state']['Name'] == 'minecraft:air' for east in range(21,44) for south in range(15,38) for height in range(11,18))
for roof in read(source / 'qa/static.json')['roof_regions']:
    region = next(region for region in metadata['regions'] if region['id'] == roof['region'])
    west,north,east,south = region['bounds_xz']
    assert all(records[block_x,roof['roof_y'],block_z]['state']['Name'] not in audit.AIR for block_x in range(west,east+1) for block_z in range(north,south+1))
    assert roof['columns'] == (east-west+1)*(south-north+1)
    roof_columns += roof['columns']
unused, cut = mapping(source / 'lodestone/inspection-cutaway.nbt')
assert cut == {position:record for position,record in records.items() if not(position[1]>=8 or (position[1]>=3 and (position[0]==0 or position[2] in {14,24,42,48,50})))}
unused, detail = mapping(source / 'lodestone/treasure-detail.nbt')
assert detail == {tuple(value-[46,0,50][axis] for axis,value in enumerate(position)):record for position,record in records.items() if all([46,0,50][axis]<=value<=[64,13,72][axis] for axis,value in enumerate(position))}
assembly = read(source / 'assembly.json')
normalized = {'source_sha256':assembly['source_sha256'],'size':assembly['source_size'],'part_count':len(assembly['parts']),'parts':[{'step':part['order'],'path':str(source/part['file']),'relative_origin':part['origin'],'size':part['size'],'sha256':part['sha256'],'blocks':part['explicit_records']} for part in assembly['parts']]}
assembly_path = folder / 'normalized-assembly.json'
assembly_path.write_text(json.dumps(normalized,indent=2),encoding='utf-8')
partition_result = verify(folder / 'room.nbt',assembly_path)
(folder / 'independent-partitions.json').write_text(json.dumps(partition_result,indent=2),encoding='utf-8')
result = {'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','native_selected':native['survival_checked'],'body_targets':len(route['targets']),'regions':8,'empty_chests':3,'six_face_sealed_lava':179,'roof_columns_checked':roof_columns,'exact_source_cut_and_translated_detail':True,'dense_partition_records':partition_result['records'],'scope':'Independent offline shapes/survival and exact decoded partition values, rotation zero only. Interim shard seams, world lava/fire ticks, actual interaction, physics, gameplay and deployment NOT_RUN.'}
(folder / 'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result,indent=2))
