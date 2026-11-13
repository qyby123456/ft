import json
from pathlib import Path
import audit

folder=Path(__file__).resolve().parent/'S5-07-independent'
template=audit.decode(folder/'room.nbt')
blocks={tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
stairs=[state for state in blocks.values() if state['Name'].endswith('_stairs')]
panels=[state for state in blocks.values() if state['Name'].endswith('_trapdoor')]
barrels=[entry for entry in template['blocks'] if template['palette'][entry['state']]['Name']=='minecraft:barrel']
assert len(stairs)==49 and all(state['Properties']['waterlogged']=='true' for state in stairs)
assert len(panels)==24 and all(state['Properties']['open']=='true' and state['Properties']['waterlogged']=='true' for state in panels)
assert len(barrels)==5 and all(not entry.get('nbt',{}).get('Items') and not entry.get('nbt',{}).get('LootTable') for entry in barrels)
assert not any(state['Name'].endswith('_door') for state in blocks.values())
result={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','waterlogged_stairs':49,'open_waterlogged_side_panels':24,'empty_barrels':5,'double_door_linked_interaction':False,'scope':'Static source features; side-open panels replace functional double door, source-water integration contract mandatory.'}
(folder/'independent-features.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result))
