import json
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
folder=root/'S3-06-independent-final'
source=audit.BASE/'FT地牢_第3组重做B_20261104/S3-06'
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return {tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
blocks=mapping(folder/'room.nbt')
assert audit.digest(source/'room.nbt')==audit.digest(folder/'room.nbt')
native=read(folder/'independent-native-frozen.json')[0]
routes=read(folder/'independent-routes.json')[0]
assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
roof=0
for east in range(37):
    for south in range(35):
        if east<=18 or south>=16:
            assert blocks[(east,18,south)]['Name'] not in audit.AIR
            roof+=1
        else:
            assert all(blocks[(east,up,south)]['Name']=='minecraft:air' for up in range(19))
assert roof==1007
assert not any(state['Name'] in {'minecraft:snow','minecraft:ice'} for state in blocks.values())
for port in read(source/'room.json')['connections']:
    east,up,south=port['position']
    for offset in range(-2,3):
        horizontal=(east+offset,south) if port['facing']=='north' else (east,south+offset)
        assert blocks[(horizontal[0],up-1,horizontal[1])]['Name'] not in audit.AIR
        assert all(blocks[(horizontal[0],up+rise,horizontal[1])]['Name']=='minecraft:air' for rise in range(4))
derived=mapping(source/'preview-only/preview-only-cutaway.nbt')
assert derived.keys()<=blocks.keys()
for position,state in blocks.items():
    east,up,south=position
    remove=up>=16 or (up>=14 and (4<=east<=12 or south>=22)) or (up>=5 and (east<2 or south>32))
    assert derived.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if remove else state),position
boundary=read(folder/'independent-boundaries.json')[0]
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
result={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':audit.digest(folder/'room.nbt'),'native_selected':native['survival_checked'],'reachable_targets':len(routes['targets']),'L_roof_columns':roof,'clear_supported_ports':2,'source_air_only_cut':'PASS','outside_L_explicit_air_erases_existing_terrain':True,'scope':'Offline native static selected survival/body route/geometry checks. World ticks, gameplay and deployment NOT_RUN.'}
(folder/'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result,indent=2))
