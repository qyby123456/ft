import hashlib
import json
import math
from pathlib import Path
from audit_s8_submission import load

workspace=Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root=workspace/'preserved-review-snapshots/S8-interaction-revision-20261107-1355'
pointer=load(root/'independent-run.json');assert pointer['exit_code']==0 and pointer['frozen_files_unchanged']==205
runtime=Path(pointer['runtime'])
report=load(runtime/'interaction-results.json')
assert report['source_sha256']=='6988e265a3fc6f89ce8a1b507088ffe8da664a38b28bcf8058b08eedc02d3fee'
assert len(report['results'])==report['target_count']==105 and not report['failures']
assert len({tuple(item['position']) for item in report['results']})==105 and all(item['pass'] for item in report['results'])
rays=[]
def inspect(value):
    if isinstance(value,list):
        for item in value: inspect(item)
    elif isinstance(value,dict):
        if {'eye','hit','first_hit','target','distance','stance','route_certificate'} <= value.keys():
            assert value['first_hit']==value['target'] and value['distance']<=4.5
            assert value['body_clear'] and value['floor_support']
            assert abs(math.dist(value['eye'],value['hit'])-value['distance'])<1e-6
            assert abs(value['eye_height']-1.62)<1e-6
            assert abs(value['eye'][1]-value['stance'][1]-1.62)<1e-6
            route=value['route'];certificate=value['route_certificate']
            assert route[0]==[6.5,4,55.5] and route[-1]==value['stance']
            assert certificate['pass'] and not certificate['findings'] and certificate['points']==len(route)
            rays.append(value)
        for item in value.values():inspect(item)
inspect(report)
openings=load(runtime/'door-open-controls.json');resets=load(runtime/'door-reset-controls.json')
inspect(openings);inspect(resets)
assert len(openings)==len(resets)==6 and all(item['opened'] for item in openings) and all(item['door_restored'] for item in resets)
assert len(rays)==168
execution=load(runtime/'execution-result.json');assert execution['pass'] and execution['route_findings']==execution['failures']==0
for item in load(root/'submission-snapshot.json')['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest()==item['sha256']
result={'accepted':False,'status':'PARENT_ACTUAL105TARGETS168EYE_RAYS_PASS','source_sha256':report['source_sha256'],'runtime':str(runtime),'functional_targets':105,'ray_checks':168,'max_distance':max(item['distance'] for item in rays),'door_open_reset_pairs':6,'menu_open_count':sum(bool(item.get('menu_opened'))+bool(item.get('lectern_menu_opened')) for item in report['results']),'scope':'Parent new world exact Java run; actual ray-derived use with legal entrance routes, reposition before each use; no claim whole-map natural motion for new29blocks','remaining':['new29block access natural ladder/bridge courses','new20part package actual installation','visual inspection mechanism','final whole-group requirement audit']}
(root/'independent-interaction-inspection.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(result,ensure_ascii=False))
