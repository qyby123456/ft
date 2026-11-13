import hashlib
import json
import shutil
import sys
from pathlib import Path
import audit

ROOT = Path(__file__).resolve().parent
group = int(sys.argv[1])
expected = sys.argv[2]
label = sys.argv[3]
if not label.replace('-', '').isalnum():
    raise ValueError('Invalid review directory label')
folder = ROOT / label
folder.mkdir(exist_ok=False)
audit.ROOT = folder
audit.GROUPS = [entry for entry in audit.GROUPS if entry[0] == group]
if len(sys.argv) > 4:
    audit.GROUPS = [(group, sys.argv[4], sys.argv[6] if len(sys.argv)>6 else 'room.nbt', sys.argv[5])]
assert len(audit.GROUPS) == 1
audit.main()
report = json.loads((folder / 'file-audit.json').read_text(encoding='utf-8'))
record = report['groups'][0]
assert record['sha256'] == expected
assert all(not manifest['mismatches'] for manifest in record['manifests'])
payload = Path(record['candidate']).read_bytes()
assert hashlib.sha256(payload).hexdigest() == expected
(folder / 'room.nbt').write_bytes(payload)
old_spec = next(entry for entry in json.loads((ROOT / 'route-input.json').read_text(encoding='utf-8')) if entry['group'] == group)
new_spec = dict(old_spec, candidate=str(folder / 'room.nbt'), sha256=expected)
if len(sys.argv) > 4:
    metadata = json.loads(Path(record['candidate']).with_suffix('.json').read_text(encoding='utf-8'))
    targets = [{'label': 'interior', 'position': metadata['interior']}]
    for key in ['connections', 'connectors', 'route_targets']:
        for item in metadata.get(key, []):
            position = item.get('position', item.get('feet'))
            if position:
                targets.append({'label': item.get('label', item.get('facing', key)), 'position': position})
    if metadata.get('altar_target'):
        targets.append({'label':'altar', 'position':metadata['altar_target']})
    for key in ['validation_targets', 'test_targets', 'ring_route_waypoints', 'entrance_route', 'route_waypoints']:
        for index, position in enumerate(metadata.get(key, [])):
            targets.append({'label': f'{key}-{index}', 'position': position})
    new_spec.update(size=record['size'], start=metadata['entrance'], targets=targets)
    supplemental_routes = Path(record['candidate']).parent / 'route-targets.json'
    if supplemental_routes.is_file():
        supplemental = json.loads(supplemental_routes.read_text(encoding='utf-8'))
        assert supplemental['sha256'] == expected
        new_spec['targets'].extend({'label':item['id'],'position':item['pos']} for item in supplemental.get('targets', []))
(folder / 'route-input.json').write_text(json.dumps([new_spec], indent=2), encoding='utf-8')
shutil.copyfile(ROOT / 'vanilla-block-tags.json', folder / 'vanilla-block-tags.json')
old = audit.decode(Path(old_spec['candidate']))
new = audit.decode(folder / 'room.nbt')
old_map = {tuple(entry['pos']): old['palette'][entry['state']] for entry in old['blocks']}
new_map = {tuple(entry['pos']): new['palette'][entry['state']] for entry in new['blocks']}
changes = [{'position': position, 'before': old_map.get(position), 'after': new_map.get(position)} for position in old_map.keys() | new_map.keys() if old_map.get(position) != new_map.get(position)]
(folder / 'revision-diff.json').write_text(json.dumps({'group': group, 'old_sha256': old_spec['sha256'], 'new_sha256': expected, 'changes': changes}, indent=2), encoding='utf-8')
print('CHANGED POSITIONS', len(changes))
