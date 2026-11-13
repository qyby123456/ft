import json
from pathlib import Path
import audit

ROOT=Path(__file__).resolve().parent
folder=ROOT/'S8-03-independent-final'
source=audit.BASE/'FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-03'
template=audit.decode(folder/'room.nbt')
blocks={tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
assert audit.digest(folder/'room.nbt')==audit.digest(source/'incoming/S8R_IMG03/room.nbt')
rules=json.loads((source/'qa/cut-rules.json').read_text(encoding='utf-8'))
for direction,cuboids in rules.items():
    cut=audit.decode(source/f'views/{direction}/inspection-only-{direction}.nbt')
    derived={tuple(entry['pos']):cut['palette'][entry['state']] for entry in cut['blocks']}
    assert derived.keys()<=blocks.keys()
    for position,state in blocks.items():
        removed=any(all(first[index]<=position[index]<=last[index] for index in range(3)) for first,last in cuboids)
        assert derived.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if removed else state),(direction,position)
metadata=json.loads((source/'incoming/S8R_IMG03/room.json').read_text(encoding='utf-8'))
for port in metadata['connections']:
    east,up,south=port['position']
    for offset in [-1,0,1]:
        horizontal=(east+offset,south) if port['facing']=='north' else (east,south+offset)
        assert blocks[(horizontal[0],up-1,horizontal[1])]['Name'] not in audit.AIR
        assert all(blocks[(horizontal[0],up+rise,horizontal[1])]['Name']=='minecraft:air' for rise in range(3))
chests=[entry for entry in template['blocks'] if template['palette'][entry['state']]['Name']=='minecraft:chest']
assert len(chests)==1 and chests[0]['pos']==[8,3,9]
assert not chests[0]['nbt'].get('Items') and not chests[0]['nbt'].get('LootTable')
assert blocks[(8,4,9)]['Name']=='minecraft:air'
assert all(blocks[(east,11,south)]['Name'] not in audit.AIR for east in range(17) for south in range(17))
routes=json.loads((folder/'independent-routes.json').read_text(encoding='utf-8'))[0]
assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
result={'sha256':audit.digest(folder/'room.nbt'),'roof_columns':289,'verified_air_only_cardinal_cuts':list(rules),'ports':'PASS','explicit_empty_chest_and_lid_clear':'PASS','route_targets_including_duplicates':len(routes['targets']),'scope':'Static independent review;world ticks and interaction NOT_RUN.'}
(folder/'independent-detail-checks.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result,indent=2))
