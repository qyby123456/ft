import json
import math
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
folder=root/'S7-09-independent'
source=audit.BASE/'FT地牢_第7组逐图重做_20261104/09-treasure'
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
template,blocks=mapping(folder/'room.nbt')
assert audit.digest(folder/'room.nbt')==audit.digest(source/'incoming/S7R2-09/room.nbt')
native=read(folder/'independent-native-frozen.json')[0]
routes=read(folder/'independent-routes.json')[0]
assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
boundary=read(folder/'independent-boundaries.json')[0]
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
for port in read(source/'incoming/S7R2-09/room.json')['connections']:
    east,up,south=port['feet']
    for offset in range(-1,2):
        block_x,block_z=(east+offset,south) if port['direction'] in {'north','south'} else (east,south+offset)
        assert blocks[(block_x,up-1,block_z)]['Name'] not in audit.AIR
        assert all(blocks[(block_x,up+rise,block_z)]['Name']=='minecraft:air' for rise in range(4))
entities={tuple(entry['pos']):entry['nbt'] for entry in template['blocks'] if 'nbt' in entry}
assert len(entities)==11
containers={position:entity for position,entity in entities.items() if entity['id'] in {'minecraft:chest','minecraft:barrel'}}
assert len(containers)==7 and sum(entity['id']=='minecraft:chest' for entity in containers.values())==1
assert all(not entity.get('Items') and not entity.get('LootTable') for entity in containers.values())
assert all(entity.get('Patterns') for position,entity in entities.items() if position not in containers)
assert blocks[(15,6,5)]['Name']=='minecraft:air'
operations=read(source/'qa/isolated-run-02/container-access.json')['operations']
assert {tuple(item['position']) for item in operations}==containers.keys()
reachable={tuple(target['position']) for target in routes['targets'] if target['reachable']}
for operation in operations:
    assert tuple(operation['access_feet']) in reachable
    east,up,south=operation['position']
    assert blocks[(east,up,south)]['Properties']['facing']=='south'
    eye=[operation['access_feet'][0]+0.5,operation['access_feet'][1]+1.62,operation['access_feet'][2]+0.5]
    face=[east+0.5,up+0.5,south+1.001]
    assert math.dist(eye,face)<4.5
    for index in range(101):
        point=[eye[axis]+(face[axis]-eye[axis])*index/100 for axis in range(3)]
        cell=tuple(math.floor(value) for value in point)
        assert blocks.get(cell,{'Name':'minecraft:air'})['Name']=='minecraft:air'
assert all(blocks[(east,18,south)]['Name'] not in audit.AIR for east in range(31) for south in range(29))
assert sum(state['Name']=='minecraft:water' for state in blocks.values())==18
unused,derived=mapping(source/'views/PREVIEW_ONLY_cutaway.nbt')
assert derived.keys()<=blocks.keys()
for position,state in blocks.items():
    east,up,south=position
    cut=up==18 or(up>=2 and(east==0 or south==28))
    assert derived.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if cut else state)
report={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':audit.digest(folder/'room.nbt'),'native_selected':native['survival_checked'],'body_targets_including_duplicates':len(routes['targets']),'ports':3,'roof_columns':899,'empty_chest':1,'empty_barrels':6,'patterned_banners':4,'container_feet_independently_reachable_and_air_ray_clear':7,'air_above_chest':True,'water':18,'exact_air_cut':True,'scope':'Offline native shapes, data and conservative container-access geometry. Real container UI/lid interaction, external cats/entities, world ticks, gameplay and deployment NOT_RUN.'}
(folder/'independent-details.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report,indent=2))
