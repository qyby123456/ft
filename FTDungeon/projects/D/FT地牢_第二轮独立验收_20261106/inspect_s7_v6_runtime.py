import hashlib
import json
import math
import sys
from pathlib import Path


root=Path(sys.argv[1]).resolve()


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


snapshot=load(root/'submission-snapshot.json')
for item in snapshot['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest()==item['sha256']
digest='906e63eea067ab01c4f0df2619102dd17a446b7c6858c732f4f03a6f3582de22'
runtime_paths={}
for mode in ('worldqa-proof','naturalqa-complete'):
    pointer=load(root/('independent-'+mode+'-result.json'))
    assert pointer['exit_code']==0 and pointer['verified_frozen_files']==828
    runtime=Path(pointer['runtime'])
    java='native-test/src/main/java/net/farmingtales/s7qa/FungalTests.java'
    assert (runtime/java).read_bytes()==(root/mode/java).read_bytes()
    runtime_paths[mode]=runtime
eyes=load(runtime_paths['worldqa-proof']/'eye-use-report.json')
assert eyes['sha256']==digest and not eyes['failures'] and eyes['functional_target_count']==39
assert len(eyes['targets'])==39
assert len({tuple(item['target']) for item in eyes['targets']})==39
for item in eyes['targets']:
    assert item['verified'] and item['distance']<=4.5
    assert abs(math.dist(item['eye'],item['first_hit'])-item['distance'])<1e-6
    assert abs(item['eye'][1]-item['feet'][1]-1.62)<1e-6
    assert item['eye'][0]==item['feet'][0] and item['eye'][2]==item['feet'][2]
assert len(eyes['door_controls'])==6
for item in eyes['door_controls']:
    assert len(item['controls'])==2 and all(control['opened'] and control['closed'] for control in item['controls'])
installation=load(runtime_paths['worldqa-proof']/'installation-report.json')
assert installation['sha256']==digest and installation['scanned_full_volume']==1969040
assert installation['barriers']==60115 and installation['type_differences_including_omitted_air']==0
assert installation['armor_stands_after_second_install']==3
assert installation['entry_trigger_permission0'] and installation['return_trigger_permission0'] and installation['remote_trigger_blocked']
natural=load(runtime_paths['naturalqa-complete']/'natural-motion-report.json')
assert natural['sha256']==digest and natural['step_height']==.6 and len(natural['courses'])==13
course_summaries=[]
for course in natural['courses']:
    assert course['passed'] and course['phases_completed']==course['phases_total'] and course['world_ticks']>0
    trace=course['trace']
    assert trace and all(after['world_tick']>before['world_tick'] for before,after in zip(trace,trace[1:]))
    course_summaries.append({key:value for key,value in course.items() if key!='trace'})
source=(runtime_paths['worldqa-proof']/'native-test/src/main/java/net/farmingtales/s7qa/FungalTests.java').read_text(encoding='utf-8')
assert 'function fts7:load' in source and 'function fts7:tick' in source
result=dict(status='PASS_SCOPED_AUTOMATIC_HOOKS_PENDING',accepted=False,source_sha256=digest,verified_frozen_files=828,actual_parent_runtimes={key:str(value) for key,value in runtime_paths.items()},eye_targets=39,door_control_checks=12,max_eye_distance=max(item['distance'] for item in eyes['targets']),actual_install_full_volume=1969040,actual_install_type_differences=0,natural_courses=course_summaries,open_requirements=['Actual automatic load/tick scheduling and permission-zero delayed trigger tests without explicit function calls','Current12-cell additions native support/routes, full entity-part payload proof, exact reproduction and final whole-group audit'],limits=['Eyes reset to reachable standing point per target; not continuous walk to each interaction','Independent13courses reset between courses, not a whole-map continuous playthrough','Installation functions executed from identical mod resources; not automatically loaded standalone ZIP certification'])
(root/'independent-runtime-inspection.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({key:value for key,value in result.items() if key!='natural_courses'},ensure_ascii=False))
