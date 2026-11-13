import json
from pathlib import Path

root=Path(__file__).resolve().parent
path=root/'S7-15/design.json'
data=json.loads(path.read_text(encoding='utf-8'))
extra=[{'label':'balance beam approach','position':[29,3,15]},{'label':'low hurdle approach','position':[31,3,23]},{'label':'stepping stones approach','position':[35,3,15]}]
labels={target['label'] for target in data['targets']}
data['targets'] += [entry for entry in extra if entry['label'] not in labels]
data['revision_3']={'reason':'Explicit obstacle equipment requested in independent 034601 review','added':['Raised log balance beam on supports','Low log hurdle on two posts','Three half-height stepping stones'],'preserved':'Original six targets and four dummies, three targets, benches and fence retained','limits':'Equipment optional side course; no real player jumping certification'}
path.write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
path=root/'S7-20/design.json'
data=json.loads(path.read_text(encoding='utf-8'))
data['revision_3']={'reason':'Independent vault massing review','changes':['Stepped left fungal-earth mass','Staggered roof crowns and side buttresses','Entry and chest sightline preserved'],'preserved':['All original targets','Secret passage','Chest and gold','Lever wall/south and north stone backing'],'old_external_lever_closure':'Applies only to parent hash; revised source requires fresh independent check'}
path.write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
