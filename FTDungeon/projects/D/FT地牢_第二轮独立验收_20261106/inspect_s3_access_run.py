import hashlib
import json
import math
from pathlib import Path

from audit_s8_submission import load

workspace = Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root = workspace / 'preserved-review-snapshots/S3-access-revision-20261107-1103'
snapshot = load(root / 'submission-snapshot.json')
for item in snapshot['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
pointer = load(root / 'independent-run.json')
assert pointer['exit_code'] == 0 and pointer['frozen_files_unchanged'] == 725
runtime = Path(pointer['runtime'])
travel = load(runtime / 'natural-travel.json')
uses = load(runtime / 'ray-use-report.json')
installation = load(runtime / 'deployment-report.json')
digest = 'f99d5a2a04a510b8bc46bbe7a0e8b75bc4cea94ee1886aa37f14fdf3eaede1d1'
assert travel['source_sha256'] == uses['source_sha256'] == digest
assert travel['target_count'] == len(travel['visited_targets']) == 41 and travel['returned_to_start']
assert travel['ticks'] == 27811 and travel['jump_inputs'] == 35
assert not uses['missing'] and len(uses['interactions']) == 44
attempts = []
for item in uses['interactions']:
    assert item['passed']
    for attempt in item['attempts']:
        assert attempt['first_hit_target'] and attempt['expected_behavior'] and attempt['reach'] <= 4.5
        assert abs(math.dist(attempt['eye'],attempt['hit_location'])-attempt['reach']) < 1e-6
        assert abs(attempt['eye'][1]-attempt['feet'][1]-1.62) < 1e-6
        attempts.append(attempt)
assert len(attempts) == 50
barrel = next(item for item in uses['interactions'] if item['position'] == [120,6,85])
assert barrel['block'] == 'minecraft:barrel' and barrel['attempts'][0]['menu_opened']
assert all(installation['functions_entry_exit'].values())
assert all(after['tick'] > before['tick'] for before,after in zip(travel['trace'],travel['trace'][1:]))
source = (root / 'accessqa-r1/native-test/src/main/java/net/farmingtales/s3qa/Natural.java').read_text(encoding='utf-8')
assert source.count('player.setPos(') == 1 and 'setDeltaMovement' not in source and 'player.travel(new Vec3(0,0,input))' in source
result = {'accepted': False,'status':'PARENT_ACTUAL_INSTALL_NATURAL_TRAVEL_AND_RAY_USE_PASS_SCOPED','source_sha256':digest,'frozen_files_unchanged':725,'runtime':str(runtime),'natural_ticks':travel['ticks'],'jump_inputs':35,'original_targets':41,'functional_blocks':44,'ray_use_attempts':50,'max_reach':max(item['reach'] for item in attempts),'hanging_barrel':barrel,'returned_to_start':True,'limits':['Actual Player.travel controller and server FakePlayer; not real network client','Source natural walker initially placed once, admin portal test separate initializations','Current payload/static/reproduction and final inheritance audit still pending']}
(root / 'independent-runtime-inspection.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({key:value for key,value in result.items() if key!='hanging_barrel'},ensure_ascii=False,indent=2))
