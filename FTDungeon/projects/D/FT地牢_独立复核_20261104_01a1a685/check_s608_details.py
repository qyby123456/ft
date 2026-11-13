import json
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
folder=root/'S6-08-independent'
source=audit.BASE/'FT地牢_Image25接入_20261101/deliveries/S6-redo-single-20261104-01/08-courtyard'
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
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
for port in read(source/'room.json')['connectors']:
    east,up,south=port['position']
    for inward in range(3):
        block_z=south+inward if port['facing']=='north' else south-inward
        for block_x in range(east-1,east+2):
            assert blocks[(block_x,up-1,block_z)]['Name'] not in audit.AIR
            assert all(blocks[(block_x,up+rise,block_z)]['Name']=='minecraft:air' for rise in range(5))
lava=[position for position,state in blocks.items() if state['Name']=='minecraft:lava']
assert len(lava)==152
for position in lava:
    assert blocks[position].get('Properties',{}).get('level','0')=='0'
    for delta in [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]:
        neighbor=blocks[tuple(coordinate+offset for coordinate,offset in zip(position,delta))]['Name']
        assert neighbor not in audit.AIR or neighbor=='minecraft:lava'
for east in range(37):
    for south in range(37):
        sky=10<=east<=26 and 10<=south<=26
        for up in [21,22]:
            assert (blocks[(east,up,south)]['Name']=='minecraft:air')==sky
stairs=[position for position,state in blocks.items() if state['Name'].endswith('_stairs')]
assert len(stairs)==60
for east in [4,5,6,30,31,32]:
    for step in range(10):
        state=blocks[(east,3+step,8+step)]
        assert state['Name'].endswith('_stairs') and state['Properties']['facing']=='south' and state['Properties']['half']=='bottom'
roots=[position for position,state in blocks.items() if state['Name']=='minecraft:crimson_roots']
assert len(roots)==6 and all(blocks[(east,up-1,south)]['Name']=='minecraft:soul_soil' for east,up,south in roots)
unused,cut=mapping(source/'lodestone/inspection-cutaway.nbt')
assert cut=={position:state for position,state in blocks.items() if not(position[1]>=21 or (position[1]>=3 and (position[0]<=2 or position[2]<=2)))}
unused,detail=mapping(source/'lodestone/fountain-detail.nbt')
assert detail=={tuple(position[index]-[12,1,12][index] for index in range(3)):state for position,state in blocks.items() if all([12,1,12][index]<=position[index]<=[24,15,24][index] for index in range(3))}
assert not any('nbt' in entry for entry in template['blocks'])
report={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':audit.digest(folder/'room.nbt'),'native_selected':native['survival_checked'],'body_targets_including_duplicates':len(routes['targets']),'ports':2,'six_face_sealed_source_lava':152,'source_skylight_columns':289,'roof_columns':1080,'stairs':60,'soul_soil_supported_roots':6,'exact_record_removal_cut':True,'exact_translated_detail':True,'scope':'Offline registry, selected survival and body-shape paths; root substrate directly checked. World ticks, heat/fire, actual interaction, physics, gameplay and deployment NOT_RUN.'}
(folder/'independent-details.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report,indent=2))
