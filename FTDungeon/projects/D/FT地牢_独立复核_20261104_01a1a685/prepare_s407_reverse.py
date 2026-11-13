import json
from pathlib import Path

folder=Path(__file__).resolve().parent/'S4-07-independent'
target=folder/'reverse-open-analysis'
target.mkdir(exist_ok=False)
spec=json.loads((folder/'open-door-analysis/route-input.json').read_text(encoding='utf-8'))
spec[0]['start']=[14,5,0]
spec[0]['targets'].extend([{'label':'south-lateral','position':[14,3,28]},{'label':'south-entrance','position':[13,3,28]}])
(target/'route-input.json').write_text(json.dumps(spec,indent=2),encoding='utf-8')
