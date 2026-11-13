import json
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
folder=root/'S4-07-independent'
source=audit.BASE/'FT地牢_第4组逐图返工_20261104/S4-07'
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
template,blocks=mapping(folder/'room.nbt')
assert audit.digest(folder/'room.nbt')==audit.digest(source/'incoming/S4-07/room.nbt')
native=read(folder/'independent-native-frozen.json')[0]
assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
closed=read(folder/'independent-routes.json')[0]
opened=read(folder/'open-door-analysis/independent-routes.json')[0]
reverse=read(folder/'reverse-open-analysis/independent-routes.json')[0]
assert closed['start_clear'] and all(not target['reachable'] for target in closed['targets'] if target['label'] in {'interior','north'})
for routes in [opened,reverse]:
    assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
boundary=read(folder/'independent-boundaries.json')[0]
assert not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
assert all(set(component['materials'])=={'minecraft:lily_pad'} for component in boundary['solids_not_connected_to_y0_by_six_face_adjacency'])
pads=[position for position,state in blocks.items() if state['Name']=='minecraft:lily_pad']
assert all(blocks[(east,up-1,south)]['Name']=='minecraft:water' and blocks[(east,up-1,south)]['Properties']['level']=='0' for east,up,south in pads)
for east in [13,14]:
    assert all(blocks.get((east,up,28),{'Name':'minecraft:air'})['Name']=='minecraft:air' for up in [3,4])
for east in range(13,16):
    assert blocks[(east,4,0)]['Name'] not in audit.AIR
    assert all(blocks.get((east,up,0),{'Name':'minecraft:air'})['Name']=='minecraft:air' for up in range(5,9))
for south in range(26,29):
    for east in range(11,17):
        state=blocks[(east,30-south,south)]
        assert state['Name'].endswith('_stairs') and state['Properties']['facing']=='north' and state['Properties']['half']=='bottom'
barrels=[entry for entry in template['blocks'] if template['palette'][entry['state']]['Name']=='minecraft:barrel']
assert len(barrels)==8 and all(entry.get('nbt',{}).get('id')=='minecraft:barrel' and not entry['nbt'].get('Items') and not entry['nbt'].get('LootTable') for entry in barrels)
assert len([entry for entry in template['blocks'] if 'nbt' in entry])==9
assert blocks[(9,7,17)]['Name']=='minecraft:campfire' and blocks[(9,7,17)]['Properties']['lit']=='true'
unused,cut=mapping(source/'preview-only/upper-cutaway.nbt')
assert cut.keys()<=blocks.keys()
for position,state in blocks.items():
    assert cut.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if position[1]>=12 else state)
report={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':audit.digest(folder/'room.nbt'),'native_selected':native['survival_checked'],'open_targets':len(opened['targets']),'reverse_open_targets':len(reverse['targets']),'closed_blocks_interior':True,'independent_only_four_open_properties':read(folder/'independent-door-diff.json')['only_four_open_properties'],'step_blocks':18,'empty_barrels':8,'legal_lily_pads':len(pads),'air_only_cut':True,'scope':'Closed source retained; open-door analysis non-deployable. Static body shape checks, not real interaction, campfire damage, world ticks, water physics, gameplay or deployment.'}
(folder/'independent-details.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report,indent=2))
