import json
from pathlib import Path
import audit
def mapping(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
def verify_cut(blocks,path,predicate):
    unused,derived=mapping(path)
    assert derived.keys()<=blocks.keys()
    for position,state in blocks.items():
        assert derived.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if predicate(*position) else state),position

ROOT=Path(__file__).resolve().parent
folder=ROOT/'S3-04-R2-independent'
source=audit.BASE/'FT地牢_第3组重做B_20261104/S3-04'
template,blocks=mapping(folder/'room.nbt')
old_template,old=mapping(ROOT/'S3-04-independent/room.nbt')
assert blocks.keys()==old.keys()
changes=[position for position in blocks if blocks[position]!=old[position]]
assert len(changes)==960
assert all(any(abs(east-center)<=2 for center in [9,25]) and any(abs(south-center)<=2 for center in [10,22,34,46]) and 7<=up<=21 for east,up,south in changes)
assert sum(state['Name']=='minecraft:spruce_log' for state in old.values())==1094
assert sum(state['Name']=='minecraft:spruce_log' for state in blocks.values())==206
assert not any(state['Name'] in {'minecraft:snow','minecraft:ice'} for state in blocks.values())
for entry in template['blocks']:
    if entry.get('nbt'):
        previous=next(item for item in old_template['blocks'] if item['pos']==entry['pos'])
        assert entry['nbt']==previous['nbt']
predicate=lambda east,up,south: up>=24 or (up>=17 and 4<east<30 and not(any(abs(east-center)<=3 for center in [9,25]) and any(abs(south-center)<=2 for center in [10,22,34,46]))) or (up>=3 and (south<2 or east>=33)) or (up>=15 and (east>=30 or south<4))
verify_cut(blocks,source/'preview-only/preview-only-cutaway.nbt',predicate)
native=json.loads((folder/'independent-native-frozen.json').read_text())[0]
routes=json.loads((folder/'independent-routes.json').read_text())[0]
assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
assert all(any(blocks[(east,up,south)]['Name'] not in audit.AIR for up in [24,25,26]) for east in range(35) for south in range(57))
assert audit.digest(source/'room.nbt')==audit.digest(folder/'room.nbt')
report={'sha256':audit.digest(folder/'room.nbt'),'changed_column_positions':len(changes),'spruce_logs_before_after':[1094,206],'block_entities_unchanged':True,'preview_air_only':'PASS','roof_columns':1995,'independent_selected_checks':native['survival_checked'],'route_targets_including_duplicates':len(routes['targets']),'scope':'Static independent review only;no world ticks or deployment.'}
(folder/'independent-details.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report,indent=2))
