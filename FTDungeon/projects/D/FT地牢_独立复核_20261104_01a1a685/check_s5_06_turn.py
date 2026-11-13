import json
from pathlib import Path
from collections import deque
import audit

folder=Path(__file__).resolve().parent/'S5-06-independent'
template=audit.decode(folder/'room.nbt')
blocks={tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
def clear(point):
    east,south=point
    return 0<=east<31 and 0<=south<31 and all(blocks[(east,up,south)]['Name'] in {'minecraft:water','minecraft:kelp','minecraft:kelp_plant','minecraft:seagrass','minecraft:tall_seagrass'} for up in [3,4])
start,finish=(0,24),(24,0)
parents={start:None}
queue=deque([start])
while queue:
    east,south=queue.popleft()
    for target in [(east+1,south),(east-1,south),(east,south+1),(east,south-1)]:
        if target not in parents and clear(target):
            parents[target]=(east,south)
            queue.append(target)
assert finish in parents
path=[]
current=finish
while current is not None:
    path.append(current)
    current=parents[current]
assert any(east>=19 and south>=19 for east,south in path)
assert any(not clear((offset,24-offset)) for offset in range(25))
result={'unrestricted_cardinal_two_high_water_route':True,'path':path,'turn_region_visited':True,'straight_gate_diagonal_obstructed':True,'scope':'Independent source-grid supplementary check; full player volume route checked separately with native shapes.'}
(folder/'independent-turn.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print('S5-06 unrestricted two-high wet route visits turn; gate diagonal blocked')
