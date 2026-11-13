import json
from collections import deque
from pathlib import Path
import audit

ROOT=Path(__file__).resolve().parent
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}

unused,roots=mapping(ROOT/'S4-03-R1-independent/room.nbt')
wood={'minecraft:dark_oak_log','minecraft:dark_oak_wood','minecraft:oak_log','minecraft:mangrove_roots'}
seed=(18,9,2)
visited={seed}
queue=deque([seed])
while queue:
    point=queue.popleft()
    for offset in [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]:
        neighbor=tuple(point[index]+offset[index] for index in range(3))
        if neighbor not in visited and roots.get(neighbor,{}).get('Name') in wood:
            visited.add(neighbor)
            queue.append(neighbor)
assert all(point in visited for point in [(16,4,5),(19,4,6),(16,4,9),(19,4,10),(16,4,11)])

source=audit.BASE/'FT地牢_Image25接入_20261101/deliveries/S6-redo-single-20261104-01/03-small-room'
unused,blocks=mapping(ROOT/'S6-03-independent-final/room.nbt')
provenance=read(source/'lodestone/detail-provenance.json')
detail_path=source/'lodestone/hearth-detail.nbt'
assert audit.digest(detail_path)==provenance['detail_sha256']
unused,detail=mapping(detail_path)
first,last=provenance['source_bounds_inclusive']
expected={tuple(position[index]-first[index] for index in range(3)):state for position,state in blocks.items() if all(first[index]<=position[index]<=last[index] for index in range(3))}
assert expected==detail

folder=ROOT/'S5-03-independent'
template,blocks=mapping(folder/'room.nbt')
source=audit.BASE/'FT地牢_第5组逐图重做_20261104/03-coral-lantern-cell'
routes=read(folder/'independent-routes.json')[0]
assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
native=read(folder/'independent-native-frozen.json')[0]
assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
for east,south in [(0,8),(18,11)]:
    for inward in range(3):
        current=east+inward if east==0 else east-inward
        for offset in [-1,0,1]:
            assert blocks[(current,2,south+offset)]['Name'] not in audit.AIR
            assert all(blocks[(current,up,south+offset)]=={'Name':'minecraft:water','Properties':{'level':'0'}} for up in [3,4,5])
chests=[entry for entry in template['blocks'] if template['palette'][entry['state']]['Name']=='minecraft:chest']
assert len(chests)==1 and chests[0]['pos']==[12,5,2] and not chests[0]['nbt'].get('Items') and not chests[0]['nbt'].get('LootTable')
assert blocks[(12,6,2)]['Name']=='minecraft:water'
assert blocks[(9,5,10)]['Name']=='minecraft:sea_lantern'
assert all(blocks[(east,3,south)]['Name']=='minecraft:prismarine_bricks' for east in range(8,11) for south in range(9,12))
for east in range(19):
    for south in range(19):
        assert blocks[(east,15,south)]=={'Name':'minecraft:water','Properties':{'level':'0'}}
        assert all(blocks[(east,up,south)]['Name'] not in audit.AIR for up in range(3))
report={'S4_root_component_blocks':len(visited),'S4_all_five_branch_endpoints_connected':True,'S6_detail_provenance':'EXACT_CUBOID_TRANSLATION','S5':{'sha256':audit.digest(folder/'room.nbt'),'route_targets':len(routes['targets']),'native_selected':native['survival_checked'],'two_3x3x3_source_water_ports':'PASS','empty_chest_with_water_above_lid':'PASS','central_small_lantern_plinth':'PASS','full_source_water_top_and_solid_y0_to_2_base':'PASS','scope':'External8block source-water ring and8block source-water cover mandatory. Independent review of producer1200tick test evidence, not formal integration or new server execution.'}}
(ROOT/'batch-1105-supplement.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report,indent=2))
