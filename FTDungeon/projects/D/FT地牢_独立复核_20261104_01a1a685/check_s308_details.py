import json
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
folder=root/'S3-08-independent'
source=audit.BASE/'FT地牢_第3组重做B_20261104/S3-08'
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
template,blocks=mapping(folder/'room.nbt')
assert audit.digest(folder/'room.nbt')==audit.digest(source/'room.nbt')
native=read(folder/'independent-native-frozen.json')[0]
routes=read(folder/'independent-routes.json')[0]
assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
boundary=read(folder/'independent-boundaries.json')[0]
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and boundary['fluid_outside_or_air_neighbors_excluding_up_count']==0
metadata=read(source/'room.json')
for port in metadata['connections']:
    east,up,south=port['position']
    for offset in range(-2,3):
        block_x,block_z=(east+offset,south) if port['facing'] in {'north','south'} else (east,south+offset)
        assert blocks[(block_x,up-1,block_z)]['Name'] not in audit.AIR
        assert all(blocks[(block_x,up+rise,block_z)]['Name']=='minecraft:air' for rise in range(4))
unused,derived=mapping(source/'preview-only/preview-only-cutaway.nbt')
assert derived.keys()<=blocks.keys()
for position,state in blocks.items():
    east,up,south=position
    cut=up>=26 or(up>=8 and south>=40) or(up>=16 and east>=40)
    assert derived.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if cut else state)
roof=read(source/'roof-columns.json')['columns']
expected={(east,south) for east in range(43) for south in range(43) if east<=9 or east>=33 or south<=9}
assert len(roof)==len(expected)==1090 and {(entry['x'],entry['z']) for entry in roof}==expected
assert all(blocks[(east,up,south)]['Name'] not in audit.AIR for east,south in expected for up in [26,27,28])
skylight={(east,south) for east in range(10,33) for south in range(10,43)}
assert all(blocks[(east,28,south)]['Name']=='minecraft:air' for east,south in skylight)
assert not any(state['Name'] in {'minecraft:water','minecraft:lava','minecraft:ice','minecraft:snow'} for state in blocks.values())
leaves=[state for state in blocks.values() if state['Name']=='minecraft:spruce_leaves']
assert leaves and all(state['Properties']['persistent']=='true' for state in leaves)
assert not any('nbt' in entry for entry in template['blocks'])
assert sum(state['Name'].endswith('_stairs') for state in blocks.values())==52
for south in range(21,30):
    for east in range(5,9):
        state=blocks[(east,34-south,south)]
        assert state['Name'].endswith('_stairs') and state['Properties']['facing']=='north' and state['Properties']['half']=='bottom'
for south,up in [(33,3),(34,4)]:
    for east in range(24,32):
        state=blocks[(east,up,south)]
        assert state['Name'].endswith('_stairs') and state['Properties']['facing']=='south' and state['Properties']['half']=='bottom'
assert all(blocks[(east,3,south)]['Name']=='minecraft:blue_ice' for east in range(23,33) for south in range(23,33))
report={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':audit.digest(folder/'room.nbt'),'native_selected':native['survival_checked'],'body_targets_including_duplicates':len(routes['targets']),'ports':4,'roof_columns':1090,'source_top_air_columns':len(skylight),'stairs':52,'leaves_persistent':len(leaves),'exact_air_cut':True,'blue_ice_pool':True,'scope':'Offline native selected survival and body paths. Ice sliding, world ticks, actual lighting, gameplay and deployment NOT_RUN.'}
(folder/'independent-details.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report,indent=2))
