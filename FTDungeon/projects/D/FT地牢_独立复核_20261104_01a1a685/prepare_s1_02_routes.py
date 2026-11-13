import json
from pathlib import Path

folder=Path(__file__).resolve().parent/'S1-02-independent'
path=folder/'route-input.json'
specification=json.loads(path.read_text(encoding='utf-8'))
specification[0]['targets']=[target for target in specification[0]['targets'] if target['label'] not in {'west-ring','east-ring','front-ring','back-ring','table','storage','water-channel'}]
for label,point in [('west-ring',[7,2,10]),('east-ring',[15,2,10]),('front-ring',[11,2,7]),('back-ring',[11,2,13]),('table',[6,2,5]),('storage',[17,2,4]),('water-channel',[18,2,13])]:
    specification[0]['targets'].append({'label':label,'position':point})
path.write_text(json.dumps(specification,indent=2),encoding='utf-8')
