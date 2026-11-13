import json
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
folder=root/'S1-02-independent'
source=audit.BASE/'FT地牢_Image25接入_20261101/work/nature-redo-b02-s1-02-20261105/nature-redo-b02-s1-02'
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
template=audit.decode(folder/'room.nbt')
blocks={tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
assert audit.digest(folder/'room.nbt')==audit.digest(source/'room.nbt')
metadata=read(source/'room.json')
for position in metadata['route']:
    east,up,south=position
    assert blocks[(east,up-1,south)]['Name'] not in audit.AIR
    assert all(blocks[(east,up+rise,south)]['Name']=='minecraft:air' for rise in range(3))
for port in metadata['ports']:
    east,up,south=port['feet']
    for offset in [-1,0,1]:
        assert blocks[(east+offset,up-1,south)]['Name'] not in audit.AIR
        assert all(blocks[(east+offset,up+rise,south)]['Name']=='minecraft:air' for rise in range(3))
assert all(blocks[(east,12,south)]['Name'] not in audit.AIR for east in range(23) for south in range(21))
native=read(folder/'independent-native-frozen.json')[0]
routes=read(folder/'independent-routes.json')[0]
assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
assert routes['start_clear'] and len(routes['targets'])==10 and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
boundary=read(folder/'independent-boundaries.json')[0]
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
containers=[entry for entry in template['blocks'] if template['palette'][entry['state']]['Name'] in {'minecraft:chest','minecraft:barrel'}]
assert len(containers)==3 and all(not entry.get('nbt',{}).get('Items') and not entry.get('nbt',{}).get('LootTable') for entry in containers)
assert all(state.get('Properties',{}).get('persistent')=='true' for state in blocks.values() if state['Name'].endswith('_leaves'))
result={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':audit.digest(folder/'room.nbt'),'handover_hashes':read(folder/'handover-manifest-independent.json')['entries_verified'],'native_selected':native['survival_checked'],'reachable_targets':len(routes['targets']),'extra_targets':'four fountain ring sides, table, storage, water-channel edge','ports':2,'roof_columns':483,'empty_containers':len(containers),'scope':'Offline native shape/body routes and selected survival/geometry. Producer four rotation 400tick randomTickSpeed=0 evidence not independently certified; natural random ticks, actual interaction, gameplay and deployment NOT_RUN.'}
(folder/'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result,indent=2))
