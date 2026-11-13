import hashlib
import json
import subprocess
from pathlib import Path
from check import READER

root=Path(__file__).resolve().parent
world=root/'qa/world-revision2'
results=json.loads((world/'natural-results.json').read_text(encoding='utf-8'))
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def states(path):
    template=READER.decode(path)
    return {tuple(entry['pos']):(template['palette'][entry['state']],entry.get('nbt')) for entry in template['blocks']}
comparison=[]
for identifier in [f'S5-{number}' for number in range(11,21)]+['merged']:
    before=states(world/(identifier+'-placed.nbt'))
    after=states(world/(identifier+'-after1200.nbt'))
    changes=[{'position':position,'placed':before[position],'after':after[position]} for position in before if before[position]!=after[position]]
    comparison.append({'id':identifier,'source_sha256':results[identifier]['sha256'],'placed_sha256':digest(world/(identifier+'-placed.nbt')),'after_sha256':digest(world/(identifier+'-after1200.nbt')),'records_compared':len(before),'changes':changes,'exactly_stable':not changes})
(world/'full-state-payload-comparison.json').write_text(json.dumps(comparison,ensure_ascii=False,indent=2),encoding='utf-8')
qa=root/'qa/post-world-routes'
qa.mkdir(exist_ok=True)
design=json.loads((root/'merged/design.json').read_text(encoding='utf-8'))
coverage=json.loads(Path('D:/ft工程文件/FT地牢_第二轮独立验收_20261106/S5-submission-20261107-042602-596016/independent-corridor-qa/route-input.json').read_text(encoding='utf-8-sig'))[0]
candidate=world/'merged-after1200.nbt'
base={**coverage,'candidate':str(candidate),'sha256':digest(candidate)}
specifications=[{**base,'group':701,'start':design['entry']},{**base,'group':702,'start':design['finish']}]
for specification in specifications:
    specification['targets']+=[]
(qa/'route-input.json').write_text(json.dumps(specifications,ensure_ascii=False,indent=2),encoding='utf-8')
(qa/'vanilla-block-tags.json').write_bytes((root/'S5-11/qa/vanilla-block-tags.json').read_bytes())
classpath='D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/classes;'+';'.join(Path('D:/ft工程文件/FT地牢_第7组逐图重做_20261104/10-overview/qa/isolated-run-05/classpath.txt').read_text(encoding='utf-8-sig').strip().splitlines())
result=subprocess.run(['C:/Program Files/Java/jdk-25.0.2/bin/java.exe','-Xmx1536m','-cp',classpath,'IndependentRouteAudit',str(qa)],cwd=qa,capture_output=True,timeout=360)
(qa/'routes.log').write_bytes(result.stdout+result.stderr)
assert result.returncode==0
routes=json.loads((qa/'independent-routes.json').read_text(encoding='utf-8'))
for report in routes:
    failures=[target for target in report['targets'] if not target['reachable'] or not target['standing_clear']]
    report['expected_solid_jambs']=[[166,9,21],[166,9,25]]
    report['unexpected_failures']=[failure for failure in failures if failure['position'] not in report['expected_solid_jambs']]
    report['scope_addendum']='Post-tick real world snapshot, native offline body collision in both directions. Keeps all 886 original points including two solid jamb failures; 3-wide throat contract. Does not simulate player movement.'
    assert not report['unexpected_failures']
(qa/'bidirectional-coverage.json').write_text(json.dumps(routes,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'natural_passed':results['passed'],'state_changes':{report['id']:len(report['changes']) for report in comparison},'bidirectional_targets':len(routes[0]['targets']),'scope':'Producer self-test, not independent acceptance'},ensure_ascii=False))
