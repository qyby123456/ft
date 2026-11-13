import hashlib
import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent
BASE = Path('D:/ft工程文件')
report = json.loads((ROOT / 'file-audit.json').read_text(encoding='utf-8'))
specifications = []
for record in report['groups']:
    group = record['group']
    source = Path(record['candidate'])
    folder = ROOT / 'route-snapshot-01' / f'S{group}-01'
    folder.mkdir(parents=True, exist_ok=True)
    payload = source.read_bytes()
    actual = hashlib.sha256(payload).hexdigest()
    if actual != record['sha256']:
        print(f'S{group} changed; exclude stale baseline from new route run')
        continue
    target = folder / 'room.nbt'
    target.write_bytes(payload)
    metadata_path = source.with_suffix('.json')
    if group == 5:
        metadata_path = source.parent.parent / 'metadata.json'
    if not metadata_path.is_file():
        metadata_path = None
    metadata = json.loads(metadata_path.read_text(encoding='utf-8-sig')) if metadata_path else {}
    if metadata_path:
        shutil.copyfile(metadata_path, folder / 'source-metadata.json')
    targets = []
    start = metadata.get('entrance')
    for key in ['interior', 'altar_target']:
        if isinstance(metadata.get(key), list):
            targets.append({'label': key, 'position': metadata[key]})
    for key in ['connections', 'connectors', 'route_targets']:
        for item in metadata.get(key, []):
            position = item.get('position', item.get('feet'))
            if position:
                targets.append({'label': item.get('label', item.get('id', item.get('name', item.get('facing', item.get('direction', key))))), 'position': position})
    for key in ['validation_targets', 'ring_route_waypoints', 'route_waypoints']:
        for index, item in enumerate(metadata.get(key, [])):
            position = item.get('position') if isinstance(item, dict) else item
            targets.append({'label': f'{key}-{index}', 'position': position})
    if group == 7:
        more = json.loads((BASE / 'FT地牢_第7组逐图重做_20261104/01-large-chamber/qa/native-targets.json').read_text(encoding='utf-8'))
        targets.extend({'label': f'landing-{index}', 'position': position} for index, position in enumerate(more['targets']))
    if group == 5:
        start = [17, 3, 31]
        targets = [{'label': label, 'position': position} for label, position in [('north', [17,3,0]), ('west', [0,3,10]), ('east', [34,3,10]), ('left-ring', [8,3,19]), ('right-ring', [26,3,19]), ('rear-ring', [17,3,10])]]
    if not start:
        print(f'S{group} no entrance; no route test')
        continue
    specifications.append({'group': group, 'candidate': str(target), 'sha256': actual, 'size': record['size'], 'start': start, 'swimming': group == 5, 'targets': targets})
(ROOT / 'route-input.json').write_text(json.dumps(specifications, ensure_ascii=False, indent=2), encoding='utf-8')
print([(item['group'], len(item['targets'])) for item in specifications])
