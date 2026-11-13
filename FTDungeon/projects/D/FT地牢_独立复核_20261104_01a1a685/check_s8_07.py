import json
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
folder=root/'S8-07-independent'
source=audit.BASE/'FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-07'
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
template=audit.decode(folder/'room.nbt')
blocks={tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
assert audit.digest(folder/'room.nbt')==audit.digest(source/'incoming/S8R_IMG07/room.nbt')
native=read(folder/'independent-native-frozen.json')[0]
routes=read(folder/'independent-routes.json')[0]
assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
boundary=read(folder/'independent-boundaries.json')[0]
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
for direction,cuboids in read(source/'qa/cut-rules.json').items():
    derived=audit.decode(source/f'views/{direction}/inspection-only-{direction}.nbt')
    mapped={tuple(entry['pos']):derived['palette'][entry['state']] for entry in derived['blocks']}
    assert mapped.keys()<=blocks.keys()
    for position,state in blocks.items():
        remove=any(all(first[index]<=coordinate<=last[index] for index,coordinate in enumerate(position)) for first,last in cuboids)
        assert mapped.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if remove else state)
doors=[(position,state) for position,state in blocks.items() if state['Name'].endswith('_door')]
assert len(doors)==4 and all(state['Properties']['open']=='true' for position,state in doors)
containers=[entry for entry in template['blocks'] if template['palette'][entry['state']]['Name']=='minecraft:barrel']
assert len(containers)==4 and all(not entry.get('nbt',{}).get('Items') and not entry.get('nbt',{}).get('LootTable') for entry in containers)
banners=[entry for entry in template['blocks'] if template['palette'][entry['state']]['Name'].endswith('_wall_banner')]
assert len(banners)==2 and all(entry.get('nbt') for entry in banners)
result={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':audit.digest(folder/'room.nbt'),'native_selected':native['survival_checked'],'reachable_targets':len(routes['targets']),'air_only_cuts':4,'open_door_halves':len(doors),'empty_barrels':len(containers),'banner_entities':len(banners),'scope':'Offline native shape steps/open-door-route and selected static survival, file/geometry. Actual door interaction, world ticks, gameplay and deployment NOT_RUN.'}
(folder/'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result,indent=2))
