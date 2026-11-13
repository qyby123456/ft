import hashlib
import json
import subprocess
from pathlib import Path
from check import READER

root=Path(__file__).resolve().parent
qa=root/'qa/room17-storage-verified'
digest=lambda path:hashlib.sha256(path.read_bytes()).hexdigest()
def states(path):
    data=READER.decode(path)
    return {tuple(entry['pos']):(data['palette'][entry['state']],entry.get('nbt')) for entry in data['blocks']}
results=[]
natural=json.loads((qa/'natural-results.json').read_text(encoding='utf-8'))
assert natural['passed']
for identifier,filename in [('S5-17','s5-17.nbt'),('merged','merged.nbt')]:
    candidate=root/identifier/filename;placed=states(qa/(identifier+'-placed.nbt'));after=states(qa/(identifier+'-after1200.nbt'));source=states(candidate)
    differences=[{'position':position,'placed':placed[position],'after':after[position]} for position in placed if placed[position]!=after[position]]
    adjustments=[{'position':position,'source':source[position],'placed':placed[position]} for position in source if source[position]!=placed[position]]
    assert not differences
    assert natural[identifier]['sha256']==digest(candidate)
    results.append({'id':identifier,'sha256':digest(candidate),'placed_sha256':digest(qa/(identifier+'-placed.nbt')),'after1200_sha256':digest(qa/(identifier+'-after1200.nbt')),'records_compared':len(after),'post_placement_changes':differences,'source_to_placement_adjustments':adjustments,'passed':True})
(qa/'full-state-payload-comparison.json').write_text(json.dumps(results,ensure_ascii=False,indent=2),encoding='utf-8')
coverage=json.loads((root/'qa/post-world-routes/route-input.json').read_text(encoding='utf-8'))[0]
routesqa=qa/'post-tick-routes'
routesqa.mkdir(exist_ok=True)
candidate=qa/'merged-after1200.nbt'
specifications=[{**coverage,'candidate':str(candidate),'sha256':digest(candidate),'group':831,'start':[10,9,23]},{**coverage,'candidate':str(candidate),'sha256':digest(candidate),'group':832,'start':[14,5,69]}]
(routesqa/'route-input.json').write_text(json.dumps(specifications,ensure_ascii=False,indent=2),encoding='utf-8')
(routesqa/'vanilla-block-tags.json').write_bytes((root/'S5-11/qa/vanilla-block-tags.json').read_bytes())
classpath='D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/classes;'+';'.join(Path('D:/ft工程文件/FT地牢_第7组逐图重做_20261104/10-overview/qa/isolated-run-05/classpath.txt').read_text(encoding='utf-8-sig').strip().splitlines())
process=subprocess.run(['C:/Program Files/Java/jdk-25.0.2/bin/java.exe','-Xmx1536m','-cp',classpath,'IndependentRouteAudit',str(routesqa)],cwd=routesqa,capture_output=True,timeout=360)
(routesqa/'routes.log').write_bytes(process.stdout+process.stderr)
assert process.returncode==0
routes=json.loads((routesqa/'independent-routes.json').read_text(encoding='utf-8'))
for report in routes:
    failures=[target for target in report['targets'] if not target['reachable'] or not target['standing_clear']]
    report['expected_solid_jambs']=[[166,9,21],[166,9,25]]
    report['unexpected_failures']=[failure for failure in failures if failure['position'] not in report['expected_solid_jambs']]
    assert not report['unexpected_failures']
(routesqa/'bidirectional-coverage.json').write_text(json.dumps(routes,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'state_and_payload_stable':True,'source_placement_adjustments':{entry['id']:len(entry['source_to_placement_adjustments']) for entry in results},'bidirectional_targets':sum(len(report['targets']) for report in routes)}))
