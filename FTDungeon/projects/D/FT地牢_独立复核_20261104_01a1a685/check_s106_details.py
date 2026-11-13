import json
from pathlib import Path
import audit

folder=Path(__file__).resolve().parent/'S1-06-independent'
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
record=read(folder/'file-audit.json')['groups'][0]
source=Path(record['candidate'])
assert audit.digest(source)==audit.digest(folder/'room.nbt')
template=audit.decode(folder/'room.nbt')
blocks={tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
metadata=read(source.with_suffix('.json'))
native=read(folder/'independent-native-frozen.json')[0]
routes=read(folder/'independent-routes.json')[0]
boundary=read(folder/'independent-boundaries.json')[0]
assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
assert len(metadata['route'])==195
for east,up,south in metadata['route']:
    assert blocks[(east,up-1,south)]['Name'] not in audit.AIR
    assert all(blocks[(east,up+rise,south)]['Name']=='minecraft:air' for rise in range(3))
assert all(blocks[(east,11,south)]['Name'] not in audit.AIR for east in range(25) for south in range(25))
assert len(metadata['ports'])==2 and {port['side'] for port in metadata['ports']}=={'west','north'}
for port in metadata['ports']:
    east,up,south=port['feet']
    assert port['width']==5 and port['clearHeight']==3
    for offset in range(-2,3):
        block_x,block_z=(east+offset,south) if port['side']=='north' else(east,south+offset)
        assert blocks[(block_x,up-1,block_z)]['Name'] not in audit.AIR
        assert all(blocks[(block_x,up+rise,block_z)]['Name']=='minecraft:air' for rise in range(3))
assert not template.get('entities') and not any('nbt' in entry for entry in template['blocks'])
report={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':audit.digest(source),'native_selected':native['survival_checked'],'reachable_body_targets':len(routes['targets']),'three_high_route_cells':195,'ports':2,'roof_columns':625,'no_entities_or_containers':True,'scope':'Offline native collision shapes and selected survival. Producer400ticks randomTickSpeed0 are not independent world/natural random tick certification. Real interaction, gameplay and deployment NOT_RUN.'}
(folder/'independent-details.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report,indent=2))
