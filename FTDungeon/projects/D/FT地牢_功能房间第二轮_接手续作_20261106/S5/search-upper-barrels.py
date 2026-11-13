import hashlib
import json
import subprocess
from pathlib import Path

root=Path(__file__).resolve().parent
qa=root/'qa/upper-barrels-halfgrid'
qa.mkdir(exist_ok=True)
evidence=json.loads((root/'qa/interaction-reach-final/reach-results.json').read_text(encoding='utf-8'))
targets=[]
for index,interaction in enumerate(evidence['interactions']):
    if interaction['passed']:continue
    position=interaction['target']
    for east in range(-10,11):
        for south in range(-10,11):
            for up in range(-14,3):
                targets.append({'label':str(index),'position':[position[0]+east*0.5,position[1]+up*0.5,position[2]+south*0.5]})
candidate=root/'merged/merged.nbt'
specification={'group':821,'candidate':str(candidate),'sha256':hashlib.sha256(candidate.read_bytes()).hexdigest(),'swimming':False,'start':[10,9,23],'targets':targets}
(qa/'route-input.json').write_text(json.dumps([specification],ensure_ascii=False,indent=2),encoding='utf-8')
(qa/'vanilla-block-tags.json').write_bytes((root/'qa/interaction-reach/vanilla-block-tags.json').read_bytes())
classpath='D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/classes;'+';'.join(Path('D:/ft工程文件/FT地牢_第7组逐图重做_20261104/10-overview/qa/isolated-run-05/classpath.txt').read_text(encoding='utf-8-sig').strip().splitlines())
process=subprocess.run(['C:/Program Files/Java/jdk-25.0.2/bin/java.exe','-Xmx1536m','-cp',classpath,'IndependentRouteAudit',str(qa)],cwd=qa,capture_output=True,timeout=360)
(qa/'routes.log').write_bytes(process.stdout+process.stderr)
assert process.returncode==0
routes=json.loads((qa/'independent-routes.json').read_text(encoding='utf-8'))[0]
selected={}
for index,interaction in enumerate(evidence['interactions']):
    key=','.join(map(str,interaction['target']))
    if interaction['passed']:
        selected[key]=[interaction['feet']]
        continue
    candidates=[]
    for target in routes['targets']:
        if int(target['label'])!=index or not target['reachable'] or not target['standing_clear']:continue
        position=target['position'];feet=[position[0]+0.5,position[1],position[2]+0.5]
        distance=sum((feet[axis]+(1.62 if axis==1 else 0)-interaction['target'][axis]-0.5)**2 for axis in range(3))
        if distance<=5.5**2:candidates.append((distance,feet))
    candidates.sort()
    selected[key]=[feet for _,feet in candidates]
(qa/'selected-standing.json').write_text(json.dumps(selected,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'searched_stands':len(targets),'candidates':{key:len(value) for key,value in selected.items() if key in ['156,14,81','159,14,81']}}))
