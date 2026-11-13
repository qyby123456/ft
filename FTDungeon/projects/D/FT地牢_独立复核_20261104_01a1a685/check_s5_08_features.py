import json
from collections import deque
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
folder=root/'S5-08-independent-r1'
source=audit.BASE/'FT地牢_第5组逐图重做_20261104/08-coral-skylight-courtyard'
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
template,blocks=mapping(folder/'room.nbt')
assert not any('nbt' in entry for entry in template['blocks']) and not template.get('entities')
rails=[position for position,state in blocks.items() if state['Name']=='minecraft:iron_bars']
assert len(rails)==150 and all(blocks[position]['Properties']['waterlogged']=='true' for position in rails)
masonry={'minecraft:prismarine','minecraft:prismarine_bricks','minecraft:dark_prismarine','minecraft:sandstone','minecraft:sea_lantern'}
solids={position for position,state in blocks.items() if state['Name'] in masonry or state['Name'].endswith('_coral_block')}
visited={position for position in solids if position[1]==0}
pending=deque(visited)
while pending:
    position=pending.popleft()
    for delta in [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]:
        neighbor=tuple(coordinate+offset for coordinate,offset in zip(position,delta))
        if neighbor in solids and neighbor not in visited:
            visited.add(neighbor)
            pending.append(neighbor)
assert visited==solids
def path(waypoints):
    result=[waypoints[0]]
    for target in waypoints[1:]:
        current=list(result[-1])
        axes=[index for index in range(3) if current[index]!=target[index]]
        assert len(axes)==1
        axis=axes[0]
        while current[axis]!=target[axis]:
            current[axis]+=1 if target[axis]>current[axis] else -1
            result.append(list(current))
    return result
paths={
    'ring':path([[10,3,10],[24,3,10],[24,3,24],[10,3,24],[10,3,10]]),
    'west':path([[10,3,17],[10,16,17],[4,16,17],[4,13,17]]),
    'east':path([[24,3,17],[24,16,17],[30,16,17],[30,13,17]]),
    'north':path([[17,3,10],[17,16,10],[17,16,4],[17,13,4]])}
water={'Name':'minecraft:water','Properties':{'level':'0'}}
checks=[]
for turn,name in enumerate(['NONE','CLOCKWISE_90','CLOCKWISE_180','COUNTERCLOCKWISE_90']):
    unused,capture=mapping(source/f'worldtest-r2/{name}/after-1200-ticks.nbt')
    def at(position):
        east,up,south=position
        for unused_turn in range(turn):
            east,south=34-south,east
        return capture[(east+8,up,south+8)]
    for route in paths.values():
        for east,up,south in route:
            assert at((east,up,south))==water and at((east,up+1,south))==water
    assert all(at((east,up,south))==water for east in range(6,29) for south in range(6,29) for up in range(19,22))
    assert all(at(position)['Name']=='minecraft:iron_bars' and at(position)['Properties']['waterlogged']=='true' for position in rails)
    checks.append({'rotation':name,'source_aperture_records':1587,'two_high_wet_paths':{label:len(route) for label,route in paths.items()},'wet_rails':150})
derivatives=list((source/'preview-only').glob('*.nbt'))
assert len(derivatives)==1
unused,derived=mapping(derivatives[0])
assert derived.keys()==blocks.keys()
for position,state in blocks.items():
    expected=json.loads(json.dumps(state))
    if state['Name']=='minecraft:water':
        expected={'Name':'minecraft:air'}
    elif state.get('Properties',{}).get('waterlogged')=='true':
        expected['Properties']['waterlogged']='false'
    assert derived[position]==expected
report={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':audit.digest(folder/'room.nbt'),'supported_masonry_and_full_coral_blocks':len(solids),'waterlogged_rails':150,'source_aperture':23*23,'four_settled_capture_checks':checks,'water_hidden_derivative':'ONLY_WATER_TO_AIR_AND_WATERLOGGED_FALSE_GEOMETRY_ONLY','scope':'Independent re-decoding of producer isolated Forge captures and source. Wet contract mandatory; swimming body routes do not certify breathing, buoyancy, real movement or infinite stability. No deployment.'}
(folder/'independent-features.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report,indent=2))
