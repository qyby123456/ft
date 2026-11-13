import json
from pathlib import Path
import audit

ROOT = Path(__file__).resolve().parent
SOURCE = audit.BASE / 'FT地牢_第2组重做B_20261104/S2-01/revision-02'
SUPPLEMENT = SOURCE / 'supplement'
checked = 0
for manifest, folder in [(SOURCE/'FILE_MANIFEST.json', SOURCE), (SUPPLEMENT/'SUPPLEMENT_MANIFEST.json', SUPPLEMENT)]:
    for name, expected in json.loads(manifest.read_text(encoding='utf-8')).items():
        assert audit.digest(folder/name) == expected, name
        checked += 1
registration = json.loads((SUPPLEMENT/'registration.json').read_text(encoding='utf-8'))
current = Path(registration['source_nbt'])
assert audit.digest(current) == registration['source_sha256']
template = audit.decode(current)
blocks = {tuple(entry['pos']): template['palette'][entry['state']] for entry in template['blocks']}
metadata = json.loads(current.with_suffix('.json').read_text(encoding='utf-8'))
ports = []
for port in metadata['connections']:
    east, up, south = port['position']
    for offset in range(-(port['width']//2), port['width']//2+1):
        across = (east+offset, south) if port['facing'] in {'north','south'} else (east, south+offset)
        assert blocks[(across[0],up-1,across[1])]['Name'] not in audit.AIR
        for height in range(port['height']):
            assert blocks[(across[0],up+height,across[1])]['Name'] in audit.AIR, (port, offset, height)
    ports.append({'direction':port['facing'],'supported_clear_width':port['width'],'height':port['height'],'feet_y':up})
results = []
for record in json.loads((SUPPLEMENT/'derivation.json').read_text(encoding='utf-8'))['derivatives']:
    path = Path(record['derived'])
    assert audit.digest(path) == record['derived_sha256']
    derived = audit.decode(path)
    actual = {tuple(entry['pos']):derived['palette'][entry['state']] for entry in derived['blocks']}
    assert actual.keys() == blocks.keys()
    changed = 0
    for position, state in actual.items():
        east, up, south = position
        cut = up >= 16 and south >= 6
        near = {'north':6 <= south <= 9,'south':south >= 27,'west':east <= 3 and south >= 6,'east':east >= 21 and south >= 6}[record['direction']]
        cut = cut or (up >= 4 and near)
        if cut:
            assert state['Name'] in audit.AIR
        else:
            assert state == blocks[position]
        changed += state != blocks[position]
    assert changed == record['removed_nonair_blocks']
    results.append({'direction':record['direction'],'air_cut_only':True,'changed':changed})
output = {'group':2,'source_sha256':registration['source_sha256'],'hashes_verified':checked,'interfaces':ports,'derivations':results,'scope':'Independent byte/hash/coordinate checks, not world simulation.'}
(ROOT/'S2-01-supplement-independent.json').write_text(json.dumps(output,indent=2),encoding='utf-8')
print(json.dumps(output,indent=2))
