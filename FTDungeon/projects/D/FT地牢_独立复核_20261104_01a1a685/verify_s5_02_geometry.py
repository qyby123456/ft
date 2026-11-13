import json
from pathlib import Path
import audit

ROOT=Path(__file__).resolve().parent
CASE=ROOT/'S5-02-independent'
template=audit.decode(CASE/'room.nbt')
blocks={tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
for east in range(3):
    for south in range(7,12):
        assert blocks[(east,2,south)]['Name'] not in audit.AIR
        assert all(blocks[(east,up,south)]=={'Name':'minecraft:water','Properties':{'level':'0'}} for up in range(3,6))
for east,south in [(12,13),(16,13),(12,16),(16,16)]:
    assert all(blocks[(east,up,south)]['Name']=='minecraft:dark_prismarine' for up in [4,5])
assert blocks[(14,4,14)]['Name']=='minecraft:water'
assert blocks[(14,5,14)]['Name']=='minecraft:water'
for east in range(11,18):
    for south in range(12,18):
        state=blocks[(east,6,south)]
        assert state['Name']=='minecraft:prismarine_brick_slab' and state['Properties']['type']=='top' and state['Properties']['waterlogged']=='true'
bench=[state for position,state in blocks.items() if position[1]==5 and state['Name']=='minecraft:dark_oak_slab']
assert len(bench)==10 and all(state['Properties']['waterlogged']=='true' for state in bench)
entities=[entry for entry in template['blocks'] if 'nbt' in entry]
assert len(entities)==2 and all(entry['nbt']['id']=='minecraft:chest' and not entry['nbt'].get('Items') and not entry['nbt'].get('LootTable') for entry in entities)
routes=json.loads((CASE/'independent-routes.json').read_text(encoding='utf-8'))[0]
assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
output={'group':5,'image':2,'west_gate':'5x3x3 source-water-only and supported','table':'four two-block legs, water beneath thin top slab','workbench':'10 real waterlogged wood top slabs','empty_chests':2,'independent_flat_swim_targets':len(routes['targets']),'world_evidence':'world-independent.json','scope':'Static source verification plus separate independent re-decoded maker world evidence; not game-client rendering certification.'}
(CASE/'geometry-independent.json').write_text(json.dumps(output,indent=2),encoding='utf-8')
print(json.dumps(output,indent=2))
