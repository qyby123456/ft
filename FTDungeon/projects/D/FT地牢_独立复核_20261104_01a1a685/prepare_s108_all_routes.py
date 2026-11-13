import json
from pathlib import Path

folder = Path(__file__).resolve().parent / 'S1-08-independent'
report = json.loads((folder / 'file-audit.json').read_text(encoding='utf-8'))['groups'][0]
metadata = json.loads(Path(report['candidate']).with_suffix('.json').read_text(encoding='utf-8'))
specification = json.loads((folder / 'route-input.json').read_text())
specification[0]['targets'] = [target for target in specification[0]['targets'] if not target['label'].startswith('declared-route-')]
specification[0]['targets'].extend({'label':f'body-{index}','position':position} for index,position in enumerate(metadata['body_route']))
assert len(metadata['body_route']) == 691
(folder / 'route-input.json').write_text(json.dumps(specification,indent=2),encoding='utf-8')
print('Full courtyard body targets',len(specification[0]['targets']))
