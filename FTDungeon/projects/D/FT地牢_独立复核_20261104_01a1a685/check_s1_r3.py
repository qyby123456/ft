import json
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
folder=root/'S1-01-R3-independent'
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
previous,old=mapping(root/'S1-01-R2-independent/room.nbt')
template,blocks=mapping(folder/'room.nbt')
expected={(4,10,5),(20,10,5),(4,10,12),(20,10,12),(6,11,5),(18,11,5),(6,11,12),(3,13,10),(21,13,10),(12,5,18)}
changed={position for position in old.keys()|blocks.keys() if old.get(position)!=blocks.get(position)}
assert changed==expected,changed
assert all(old[position]['Name']=='minecraft:air' for position in changed)
assert all(blocks[position]['Name']==('minecraft:mossy_stone_bricks' if position in list(expected- {(3,13,10),(21,13,10),(12,5,18)}) else 'minecraft:dark_oak_log') for position in changed)
assert {tuple(entry['pos']):entry.get('nbt') for entry in previous['blocks'] if entry.get('nbt')}=={tuple(entry['pos']):entry.get('nbt') for entry in template['blocks'] if entry.get('nbt')}
source=Path(read(folder/'file-audit.json')['groups'][0]['candidate'])
assert audit.digest(source)==audit.digest(folder/'room.nbt')
metadata=read(source.with_suffix('.json'))
for position in metadata['route']:
    east,up,south=position
    assert blocks[(east,up-1,south)]['Name'] not in audit.AIR
    assert all(blocks[(east,up+rise,south)]['Name']=='minecraft:air' for rise in range(3))
for port in metadata['ports']:
    east,up,south=port['feet']
    for offset in range(-(port['width']//2),port['width']//2+1):
        horizontal=(east+offset,south) if port['side'] in {'north','south'} else (east,south+offset)
        assert all(blocks[(horizontal[0],up+rise,horizontal[1])]['Name']=='minecraft:air' for rise in range(port['clearHeight']))
native=read(folder/'independent-native-frozen.json')[0]
assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
routes=read(folder/'independent-routes.json')[0]
assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
boundary=read(folder/'independent-boundaries.json')[0]
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
result={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':audit.digest(source),'exact_air_to_solid_additions':sorted(changed),'unchanged_block_entities':True,'three_ports_and_declared_route_clear':True,'native_selected':native['survival_checked'],'body_targets':len(routes['targets']),'unanchored_components':0,'scope':'Independent offline static verification. Producer randomTickSpeed=0 finite world evidence not independently certified here; natural random ticks, gameplay and deployment NOT_RUN.'}
(folder/'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result,indent=2))
