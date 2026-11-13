import json
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
folder=root/'S6-06-independent'
source=audit.BASE/'FT地牢_Image25接入_20261101/deliveries/S6-redo-single-20261104-01/06-corner'
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return {tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
blocks=mapping(folder/'room.nbt')
assert audit.digest(folder/'room.nbt')==audit.digest(source/'room.nbt')
native=read(folder/'independent-native-frozen.json')[0]
routes=read(folder/'independent-routes.json')[0]
assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
for port in read(source/'room.json')['connectors']:
    east,up,south=port['position']
    for offset in [-1,0,1]:
        horizontal=(east+offset,south) if port['facing']=='north' else (east,south+offset)
        assert blocks[(horizontal[0],up-1,horizontal[1])]['Name'] not in audit.AIR
        assert all(blocks[(horizontal[0],up+rise,horizontal[1])]['Name']=='minecraft:air' for rise in range(5))
roof=0
for east in range(29):
    for south in range(29):
        if east<=14 or south>=14:
            assert blocks[(east,11,south)]['Name'] not in audit.AIR
            roof+=1
        else:
            assert all(blocks[(east,up,south)]['Name']=='minecraft:air' for up in range(12))
assert all(blocks[(east,up,south)]['Name']=='minecraft:air' for east in range(6,9) for south in range(23) for up in [3,4])
assert all(blocks[(east,up,south)]['Name']=='minecraft:air' for east in range(6,29) for south in range(20,23) for up in [3,4])
derived=mapping(source/'lodestone/inspection-cutaway.nbt')
assert derived=={position:state for position,state in blocks.items() if not(position[1]>=10 or (position[1]>=3 and (position[0]<=2 or position[2]>=26 or position[2]<=1)))}
first,last=read(source/'lodestone/detail-provenance.json')['bounds_inclusive']
assert mapping(source/'lodestone/corner-niche-detail.nbt')=={tuple(position[index]-first[index] for index in range(3)):state for position,state in blocks.items() if all(first[index]<=position[index]<=last[index] for index in range(3))}
boundary=read(folder/'independent-boundaries.json')[0]
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
for position,state in blocks.items():
    if state['Name']=='minecraft:lava':
        for delta in [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]:
            adjacent=tuple(position[index]+delta[index] for index in range(3))
            assert adjacent in blocks and blocks[adjacent]['Name'] not in {'minecraft:air','minecraft:cave_air','minecraft:void_air'}
result={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':audit.digest(folder/'room.nbt'),'native_selected':native['survival_checked'],'reachable_targets':len(routes['targets']),'ports':2,'L_roof_columns':roof,'three_wide_turn_arms_clear':True,'lava_cells_six_face_enclosed':40,'source_cutaway_and_detail':'EXACT','outside_L_explicit_air_erases_terrain':True,'scope':'Offline selected survival/body route/geometry; world ticks, heat/damage, interaction and deployment NOT_RUN.'}
(folder/'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result,indent=2))
