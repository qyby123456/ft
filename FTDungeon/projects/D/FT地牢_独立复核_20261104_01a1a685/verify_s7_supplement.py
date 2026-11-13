import json
from pathlib import Path
import audit

ROOT = Path(__file__).resolve().parent
SOURCE = audit.BASE/'FT地牢_第7组逐图重做_20261104/01-large-chamber'
SUPPLEMENT = SOURCE/'route-evidence-20261104'
checked = 0
for manifest, folder in [(SOURCE/'SHA256SUMS.json',SOURCE),(SUPPLEMENT/'SUPPLEMENT_SHA256.json',SUPPLEMENT)]:
    for name, expected in json.loads(manifest.read_text(encoding='utf-8')).items():
        assert audit.digest(folder/name)==expected,name
        checked += 1
path=SOURCE/'incoming/S7R2-01/room.nbt'
assert audit.digest(path)=='0cd7e686e01720603490127393db383a6fbae2894b62ed5bc1137d98d621f9f2'
template=audit.decode(path)
blocks={tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
assert all(blocks[(east,19,south)]['Name'] not in audit.AIR for east in range(29) for south in range(29))
metadata=json.loads(path.with_suffix('.json').read_text(encoding='utf-8'))
for port in metadata['connections']:
    east,up,south=port['feet']
    for offset in [-1,0,1]:
        across=(east+offset,south) if port['direction']=='north' else (east,south+offset)
        assert blocks[(across[0],up-1,across[1])]['Name'] not in audit.AIR
        for height in range(3):
            assert blocks[(across[0],up+height,across[1])]['Name']=='minecraft:air'
derived=audit.decode(SOURCE/'views/PREVIEW_ONLY_cutaway.nbt')
other={tuple(entry['pos']):derived['palette'][entry['state']] for entry in derived['blocks']}
assert other.keys() <= blocks.keys()
assert all(other.get(position,{'Name':'minecraft:air'})['Name']=='minecraft:air' or other[position]==state for position,state in blocks.items())
fluid_edges=[]
for position,state in blocks.items():
    if state['Name']!='minecraft:water':
        continue
    for direction in [(0,-1,0),(1,0,0),(-1,0,0),(0,0,1),(0,0,-1)]:
        neighbor=tuple(position[index]+direction[index] for index in range(3))
        if blocks.get(neighbor,{'Name':'minecraft:air'})['Name'] in {'minecraft:air','minecraft:cave_air','minecraft:void_air'}:
            fluid_edges.append(position)
assert not fluid_edges
output={'group':7,'sha256':audit.digest(path),'manifest_entries':checked,'roof':'PASS_FULL_TOP_LAYER','ports':'PASS_4_SUPPORTED_3X3_AIR','inspection_derivation':'PASS_ONLY_AIR_REPLACEMENTS','water_bottom_side_air_edges':fluid_edges,'world_ticks':'NOT_RUN'}
(ROOT/'S7-01-supplement-independent.json').write_text(json.dumps(output,indent=2),encoding='utf-8')
print(json.dumps(output,indent=2))
