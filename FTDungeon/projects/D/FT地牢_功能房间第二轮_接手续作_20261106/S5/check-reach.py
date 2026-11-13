import hashlib
import json
import subprocess
import sys
from pathlib import Path

root=Path(__file__).resolve().parent
qa=root/(sys.argv[1] if len(sys.argv)>1 else 'qa/interaction-reach-final')
result=json.loads((qa/'reach-results.json').read_text(encoding='utf-8'))
candidate=root/'merged/merged.nbt'
digest=hashlib.sha256(candidate.read_bytes()).hexdigest()
assert result['sha256']==digest
assert len(result['interactions'])==37
design=json.loads((root/'merged/design.json').read_text(encoding='utf-8'))
targets=[]
for index,interaction in enumerate(result['interactions']):
    if not interaction['passed']:continue
    assert interaction['passed'] and interaction['ray_hit_target'] and interaction['eye_to_hit']<=4.5
    feet=interaction['feet']
    targets.append({'label':f'interaction-{index+1:02}-{interaction["block"]}-{interaction["target"]}','position':[feet[0]-0.5,feet[1],feet[2]-0.5]})
specifications=[{'group':801,'candidate':str(candidate),'sha256':digest,'swimming':False,'start':design['entry'],'targets':targets},{'group':802,'candidate':str(candidate),'sha256':digest,'swimming':False,'start':design['finish'],'targets':targets}]
(qa/'route-input.json').write_text(json.dumps(specifications,ensure_ascii=False,indent=2),encoding='utf-8')
(qa/'vanilla-block-tags.json').write_bytes((root/'S5-11/qa/vanilla-block-tags.json').read_bytes())
classpath='D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/classes;'+';'.join(Path('D:/ft工程文件/FT地牢_第7组逐图重做_20261104/10-overview/qa/isolated-run-05/classpath.txt').read_text(encoding='utf-8-sig').strip().splitlines())
process=subprocess.run(['C:/Program Files/Java/jdk-25.0.2/bin/java.exe','-Xmx1536m','-cp',classpath,'IndependentRouteAudit',str(qa)],cwd=qa,capture_output=True,timeout=360)
(qa/'routes.log').write_bytes(process.stdout+process.stderr)
assert process.returncode==0
routes=json.loads((qa/'independent-routes.json').read_text(encoding='utf-8'))
failures=[{'start_group':report['group'],**target} for report in routes for target in report['targets'] if not target['reachable'] or not target['standing_clear']]
usable=[interaction for interaction in result['interactions'] if interaction['passed']]
unusable=[interaction for interaction in result['interactions'] if not interaction['passed']]
summary={'sha256':digest,'requested_interactions':len(result['interactions']),'usable_interactions':len(targets),'usable_levers':sum('lever' in interaction['block'] for interaction in usable),'usable_containers':sum('lever' not in interaction['block'] for interaction in usable),'minimum_eye_to_hit':min(interaction['eye_to_hit'] for interaction in usable),'maximum_eye_to_hit':max(interaction['eye_to_hit'] for interaction in usable),'both_directions_standing_targets':sum(len(report['targets']) for report in routes),'route_failures':failures,'interaction_failures':unusable,'usable_subset_routes_passed':not failures,'passed':not failures and not unusable,'scope':'Producer native isolated world interaction evidence combined with immutable native collision route graph from both entry and finish; exact recorded feet converted to audit input by subtracting 0.5 in x/z. All failed targets retained explicitly in interaction_failures; no live-player movement or survival certification.'}
(qa/'combined-summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(summary,ensure_ascii=False))
assert not failures
