import json
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
folder=root/'S7-07-independent'
source=audit.BASE/'FT地牢_第7组逐图重做_20261104/07-entrance'
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
template=audit.decode(folder/'room.nbt')
blocks={tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
assert audit.digest(folder/'room.nbt')==audit.digest(source/'incoming/S7R2-07/room.nbt')
native=read(folder/'independent-native-frozen.json')[0]
routes=read(folder/'independent-routes.json')[0]
assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
boundary=read(folder/'independent-boundaries.json')[0]
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
derived=audit.decode(source/'views/PREVIEW_ONLY_cutaway.nbt')
mapped={tuple(entry['pos']):derived['palette'][entry['state']] for entry in derived['blocks']}
assert mapped.keys()<=blocks.keys()
for position,state in blocks.items():
    east,up,south=position
    remove=up==15 or (south==25 and up>=7)
    assert mapped.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if remove else state),position
doors=[state for state in blocks.values() if state['Name'].endswith('_door')]
assert len(doors)==4 and all(state['Properties']['open']=='true' for state in doors)
containers=[entry for entry in template['blocks'] if template['palette'][entry['state']]['Name']=='minecraft:barrel']
assert containers and all(not entry.get('nbt',{}).get('Items') and not entry.get('nbt',{}).get('LootTable') for entry in containers)
assert all(state.get('Properties',{}).get('lit')=='false' for state in blocks.values() if state['Name'].endswith('campfire'))
assert all(blocks[(east,15,south)]['Name'] not in audit.AIR for east in range(27) for south in range(26))
result={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':audit.digest(folder/'room.nbt'),'native_selected':native['survival_checked'],'reachable_targets':len(routes['targets']),'air_only_cut':'PASS','open_door_halves':len(doors),'empty_barrels':len(containers),'roof_columns':702,'scope':'Offline native selected survival/body routes include half-slab stairs and authored open doors. Actual door interaction, world ticks, gameplay and deployment NOT_RUN.'}
(folder/'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result,indent=2))
