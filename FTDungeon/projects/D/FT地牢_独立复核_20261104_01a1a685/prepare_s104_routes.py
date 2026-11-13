import json
from pathlib import Path

root=Path(__file__).resolve().parent
folder=root/'S1-04-independent'
record=json.loads((folder/'file-audit.json').read_text(encoding='utf-8'))['groups'][0]
metadata=json.loads(Path(record['candidate']).with_suffix('.json').read_text(encoding='utf-8'))
spec=json.loads((folder/'route-input.json').read_text(encoding='utf-8'))
spec[0]['targets']=[target for target in spec[0]['targets'] if not target['label'].startswith('declared-route-')]
spec[0]['targets'].extend({'label':f'declared-route-{index}','position':position} for index,position in enumerate(metadata['route']))
(folder/'route-input.json').write_text(json.dumps(spec,indent=2),encoding='utf-8')
print('Expanded S1 targets',len(spec[0]['targets']))
