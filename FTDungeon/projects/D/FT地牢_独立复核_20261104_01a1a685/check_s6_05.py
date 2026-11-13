import json
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
folder=root/'S6-05-independent'
source=audit.BASE/'FT地牢_Image25接入_20261101/deliveries/S6-redo-single-20261104-01/05-corridor'
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return {tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
blocks=mapping(folder/'room.nbt')
assert audit.digest(folder/'room.nbt')==audit.digest(source/'room.nbt')
native=read(folder/'independent-native-frozen.json')[0]
assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
routes=read(folder/'independent-routes.json')[0]
assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
for port in read(source/'room.json')['connectors']:
    east,up,south=port['position']
    for offset in range(-(port['width']//2),port['width']//2+1):
        assert blocks[(east+offset,up-1,south)]['Name'] not in audit.AIR
        assert all(blocks.get((east+offset,up+rise,south),{'Name':'minecraft:air'})['Name']=='minecraft:air' for rise in range(port['height']))
assert all(blocks[(east,11,south)]['Name'] not in audit.AIR for east in range(17) for south in range(35))
assert all(blocks.get((east,up,south),{'Name':'minecraft:air'})['Name']=='minecraft:air' for east in [7,8,9] for south in range(35) for up in [3,4])
cut=mapping(source/'lodestone/inspection-cutaway.nbt')
assert cut=={position:state for position,state in blocks.items() if not(position[1]>=10 or ((position[0]==0 or position[2]<=1) and position[1]>=3))}
first,last=read(source/'lodestone/detail-provenance.json')['bounds_inclusive']
detail=mapping(source/'lodestone/lava-window-detail.nbt')
assert detail=={tuple(position[index]-first[index] for index in range(3)):state for position,state in blocks.items() if all(first[index]<=position[index]<=last[index] for index in range(3))}
boundary=read(folder/'independent-boundaries.json')[0]
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
result={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':audit.digest(folder/'room.nbt'),'native_selected':native['survival_checked'],'reachable_targets':len(routes['targets']),'ports':2,'roof_columns':595,'three_wide_clear_center':True,'source_cutaway_and_detail':'EXACT','lava_cells':boundary['fluid_counts']['minecraft:lava'],'scope':'Static offline shapes, selected survival and geometry checks. World ticks, heat/damage, interaction and formal deployment NOT_RUN.'}
(folder/'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result,indent=2))
