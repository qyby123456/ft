import json
from pathlib import Path
import audit

ROOT = Path(__file__).resolve().parent
CASE = ROOT / 'S4-01-revision1-independent-final'
template = audit.decode(CASE / 'room.nbt')
blocks = {tuple(entry['pos']): template['palette'][entry['state']] for entry in template['blocks']}
directions = {'north': (0,1), 'south': (0,-1), 'east': (-1,0), 'west': (1,0)}
ports = []
for facing, position in [('north',(24,5,0)),('south',(24,5,48)),('east',(48,5,24)),('west',(0,5,24))]:
    inward = directions[facing]
    for depth in range(4):
        for offset in range(-2,3):
            east = position[0] + inward[0] * depth + (offset if facing in {'north','south'} else 0)
            south = position[2] + inward[1] * depth + (offset if facing in {'east','west'} else 0)
            assert blocks.get((east,4,south),{}).get('Name') == 'minecraft:dark_oak_planks'
            for up in range(5,9):
                assert blocks.get((east,up,south),{'Name':'minecraft:air'})['Name'] == 'minecraft:air', (facing,east,up,south)
    ports.append({'facing': facing,'feet': position,'width':5,'height':4,'inward_depth':4,'air_clearance_and_plank_support':'PASS'})
fluid_failures = []
for position, state in blocks.items():
    if state['Name'] != 'minecraft:water':
        continue
    for direction in [(0,-1,0),(1,0,0),(-1,0,0),(0,0,1),(0,0,-1)]:
        neighbor = tuple(position[index]+direction[index] for index in range(3))
        name = blocks.get(neighbor,{'Name':'minecraft:air'})['Name']
        if name in {'minecraft:air','minecraft:cave_air','minecraft:void_air'}:
            fluid_failures.append(neighbor)
assert not fluid_failures
leaves = [state for state in blocks.values() if state['Name'].endswith('_leaves')]
assert all(state.get('Properties',{}).get('persistent') == 'true' for state in leaves)
assert not template.get('entities')
output = {'group':4,'ports':ports,'water_air_side_or_bottom_leaks':fluid_failures,'persistent_leaves':len(leaves),'roof':'INTENTIONALLY_OPEN_AIR','scope':'Frozen revision static boundary, no world ticks. Solid adjacent blocks may have special permeability; palette consists masonry/planks/logs/clay/mud with no open gates/drains.'}
(CASE/'boundary-check.json').write_text(json.dumps(output,indent=2),encoding='utf-8')
print(json.dumps(output,indent=2))
