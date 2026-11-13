import hashlib
import json
from pathlib import Path
import audit

ROOT = Path(__file__).resolve().parent
SUPPLEMENT = ROOT / 'S8-01-R2-supplement'
SOURCE = audit.BASE / 'FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-01'
checked = 0
for manifest, folder in [(SOURCE/'checksums.json',SOURCE),(SUPPLEMENT/'evidence-checksums.json',SUPPLEMENT)]:
    for name, expected in json.loads(manifest.read_text(encoding='utf-8')).items():
        assert audit.digest(folder/name) == expected, name
        checked += 1
specification = next(entry for entry in json.loads((ROOT/'route-input.json').read_text(encoding='utf-8')) if entry['group']==8)
current = SOURCE/'incoming/S8R_IMG01/room.nbt'
assert audit.digest(current) == specification['sha256']
template = audit.decode(current)
blocks = {tuple(entry['pos']): template['palette'][entry['state']] for entry in template['blocks']}
assert all(blocks[(east,20,south)]['Name'] not in audit.AIR for east in range(35) for south in range(35))
metadata = json.loads(current.with_suffix('.json').read_text(encoding='utf-8'))
for port in metadata['connections']:
    east,up,south = port['position']
    for offset in [-1,0,1]:
        across = (east+offset,south) if port['facing'] in {'north','south'} else (east,south+offset)
        assert blocks[(across[0],up-1,across[1])]['Name'] not in audit.AIR
        for height in range(3):
            assert blocks[(across[0],up+height,across[1])]['Name']=='minecraft:air'
derivations = []
for record in json.loads((SUPPLEMENT/'directional-provenance.json').read_text(encoding='utf-8'))['records']:
    path = Path(record['derived_nbt'])
    assert audit.digest(path)==record['derived_sha256']
    derived = audit.decode(path)
    actual = {tuple(entry['pos']):derived['palette'][entry['state']] for entry in derived['blocks']}
    assert actual.keys() == blocks.keys()
    changed = 0
    for position,state in actual.items():
        inside = any(all(bounds[0][axis] <= position[axis] <= bounds[1][axis] for axis in range(3)) for bounds in record['air_cut_inclusive_boxes'])
        if inside:
            assert state['Name']=='minecraft:air'
        else:
            assert state==blocks[position]
        changed += state != blocks[position]
    assert changed==record['changed_positions']
    derivations.append({'direction':record['direction'],'verified_air_cut_only':True,'changed':changed})
output = {'group':8,'source_sha256':specification['sha256'],'manifest_entries_independently_checked':checked,'roof_full_coverage':'PASS','four_3x3_air_interfaces_supported':'PASS','directional_derivations':derivations,'scope':'Independent supplement byte/hash/coordinate verification, not image generation or world simulation.'}
(ROOT/'S8-01-supplement-independent.json').write_text(json.dumps(output,indent=2),encoding='utf-8')
print(json.dumps(output,indent=2))
