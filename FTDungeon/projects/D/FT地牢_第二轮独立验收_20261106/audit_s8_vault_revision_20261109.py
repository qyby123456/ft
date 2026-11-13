import copy
import hashlib
import json
import math
import shutil
import subprocess
import zipfile
from collections import Counter
from itertools import combinations
from pathlib import Path
from audit_s8_submission import decode, load

workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
root = workspace / 'preserved-review-snapshots/S8-vault-shell-revision-20261108'
prior = workspace / 'preserved-review-snapshots/S8-interaction-revision-20261107-1355/access-revision-r4'
revision = load(root / 'REVISION.json')
old_revision = load(prior / 'REVISION.json')
old_data, old_blocks = decode(prior / 'assembly/s8-functional-access-revision.nbt')
data, blocks = decode(root / 'assembly/s8-functional-vault-shell.nbt')
assert data['size'] == old_data['size'] == [240,29,145]
assert not data['entities'] and not old_data['entities']
air = {'state': {'Name':'minecraft:air'}, 'nbt':None}
def normalized(value):
    if value is None or value['state']['Name'] in {'minecraft:air','minecraft:cave_air','minecraft:void_air'}:
        return air
    return value

changes = {point for point in old_blocks.keys() | blocks.keys() if normalized(old_blocks.get(point)) != normalized(blocks.get(point))}
declared = {tuple(item['candidate_position']) for item in revision['changes']}
assert changes == declared and len(changes) == 1941
for item in revision['changes']:
    point = tuple(item['candidate_position'])
    assert normalized(old_blocks.get(point))['state']['Name'] == item['before_name']
    assert normalized(blocks.get(point))['state']['Name'] == item['after_name']
    assert point == tuple(item['local_position'][axis] + [190,2,75][axis] for axis in range(3))
    assert 7 <= item['local_position'][1] <= 13
unchanged = []
for module in revision['modules']:
    original = next((prior/module['id']).glob('*.nbt'))
    current = next((root/module['id']).glob('*.nbt'))
    assert hashlib.sha256(current.read_bytes()).hexdigest() == module['sha256']
    if module['id'] != 'S8-20':
        assert original.read_bytes() == current.read_bytes()
        unchanged.append(module['id'])
    room_data, room_blocks = decode(current)
    assert room_data['size'] == module['size'] and not room_data['entities']
    for point,value in room_blocks.items():
        merged = tuple(point[axis]+module['origin'][axis] for axis in range(3))
        assert blocks.get(merged) == value
assert len(unchanged) == 9
boundary = {point:value for point,value in old_blocks.items() if value['state']['Name'] == 'minecraft:barrier'}
assert len(boundary) == 90282
assert all(blocks.get(point) == value for point,value in boundary.items())
assert {point for point,value in blocks.items() if value['state']['Name']=='minecraft:barrier'} == set(boundary)
payloads = {point:value for point,value in old_blocks.items() if value['nbt'] is not None}
assert len(payloads) == 56 and all(blocks.get(point) == value for point,value in payloads.items())
rebuilt = {}
boxes = []
with zipfile.ZipFile(root/'s8_access_revision_install.zip') as package:
    assert package.testzip() is None
    packaged = {Path(name).name:package.read(name) for name in package.namelist() if name.endswith('.nbt')}
    assert len(packaged) == 20
    for part in revision['parts']:
        source = root/'assembly/parts'/Path(part['path']).name
        assert source.read_bytes() == packaged[source.name]
        part_data,part_blocks = decode(source)
        assert part_data['size'] == part['size'] and max(part['size']) <= 48 and not part_data['entities']
        boxes.append((part['origin'],part['size']))
        for point,value in part_blocks.items():
            actual = tuple(point[axis]+part['origin'][axis] for axis in range(3))
            assert actual not in rebuilt
            rebuilt[actual] = value
assert rebuilt == blocks and sum(math.prod(size) for origin,size in boxes) == 1009200
for first,second in combinations(boxes,2):
    assert not all(max(first[0][axis],second[0][axis]) < min(first[0][axis]+first[1][axis],second[0][axis]+second[1][axis]) for axis in range(3))
old_plan = load(prior/'assembly/qa/route-input.json')[0]
new_plan = load(root/'assembly/qa/route-input.json')[-1]
assert old_plan['start'] == new_plan['start'] and old_plan['targets'] == new_plan['targets'] and len(new_plan['targets']) == 58
counts = Counter()
min_change = [min(point[axis] for point in changes) for axis in range(3)]
max_change = [max(point[axis] for point in changes)+1 for axis in range(3)]
def check_box(minimum,maximum,label):
    counts[label] += 1
    if any(maximum[axis] <= min_change[axis] or minimum[axis] >= max_change[axis] for axis in range(3)):
        return
    for east in range(math.floor(minimum[0]),math.ceil(maximum[0]-1e-8)):
        for height in range(math.floor(minimum[1]),math.ceil(maximum[1]-1e-8)):
            for south in range(math.floor(minimum[2]),math.ceil(maximum[2]-1e-8)):
                assert (east,height,south) not in changes, (label,(east,height,south))
def body(first,last,label):
    check_box([min(first[0],last[0])-.31,min(first[1],last[1])-.06,min(first[2],last[2])-.31],[max(first[0],last[0])+.31,max(first[1],last[1])+1.81,max(first[2],last[2])+.31],label)
def visit(value):
    if isinstance(value,list):
        for child in value:
            visit(child)
    elif isinstance(value,dict):
        if value.get('first_hit') and 'eye' in value and 'hit' in value:
            check_box([min(value['eye'][axis],value['hit'][axis])-.002 for axis in range(3)],[max(value['eye'][axis],value['hit'][axis])+.002 for axis in range(3)],'eye_rays')
            for index,point in enumerate(value['route']):
                body(point,point,'route_body_nodes')
                if index:
                    body(value['route'][index-1],point,'route_sweeps')
        for key,child in value.items():
            if key != 'route':
                visit(child)
interaction_root = workspace/'s8-independent-interactions-20261107-1400'
for filename in ['interaction-results.json','door-open-controls.json','door-reset-controls.json']:
    visit(load(interaction_root/filename))
for filename,group in [(interaction_root/'pressure-motion.json','plate'),(workspace/'s8-independent-followup-movement-20261107-1600/motion-trace.json','course')]:
    previous = {}
    for row in load(filename):
        if 'position' not in row:
            continue
        point = row['position']
        body(point,point,'natural_samples')
        key = json.dumps(row.get(group))
        if key in previous:
            body(previous[key],point,'natural_sweeps')
        previous[key] = point
for target in load(interaction_root/'interaction-results.json')['results']:
    point = tuple(target['position'])
    assert old_blocks.get(point) == blocks.get(point)
    check_box([value-1 for value in point],[value+2 for value in point],'functional_target_dependencies')
for point,value in old_blocks.items():
    if value['nbt'] is not None:
        check_box([coordinate-1 for coordinate in point],[coordinate+2 for coordinate in point],'block_entity_dependencies')
    if value['state']['Name'] in {'minecraft:water','minecraft:lava'} or value['state'].get('Properties',{}).get('waterlogged') == 'true':
        check_box([coordinate-7 for coordinate in point],[coordinate+8 for coordinate in point],'fluid_dependencies')
assert counts['eye_rays'] == 168 and counts['functional_target_dependencies'] == 105
report = dict(status='PASS_INDEPENDENT_DELTA_PARTITION_PARENT_RUNTIME_DEPENDENCIES',accepted=False,source_sha256=revision['sha256'],changes=len(changes),unchanged_nine_rooms=unchanged,merged_records=len(blocks),full_volume=1009200,parts=20,barrier_cells=90282,preserved_block_entities=56,preserved_targets=58,dependency_counts=dict(counts),dependency_intersections=0,dependency_evidence='Actual parent-thread independent interaction and natural-course traces; not maker trace claims',inheritance_limit='Lower functional runtime geometry and local fluids preserved; roof lighting and sight lines intentionally changed; new package installation audited separately')
(root/'independent-delta-parts-dependencies.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(report,ensure_ascii=False),flush=True)
qa = root/'independent-current-qa'
qa.mkdir()
plans = load(root/'assembly/qa/route-input.json')
for plan in plans:
    original = Path(plan['candidate'])
    plan['candidate'] = str(root/original.relative_to(Path(load(root/'submission-snapshot.json')['source_directory'])))
old_qa = workspace/'preserved-review-snapshots/S8-submission-20261107-104351-507543/independent-qa'
vault_plan = copy.deepcopy(next(plan for plan in load(old_qa/'route-input.json') if Path(plan['candidate']).parent.name=='S8-20'))
vault_plan['candidate'] = str(next((root/'S8-20').glob('*.nbt')))
vault_plan['sha256'] = revision['new_room_sha256']
plans.append(vault_plan)
(qa/'route-input.json').write_text(json.dumps(plans,ensure_ascii=False,indent=2),encoding='utf-8')
shutil.copyfile(old_qa/'vanilla-block-tags.json',qa/'vanilla-block-tags.json')
classpath = (root/'assembly/qa/classpath.txt').read_text(encoding='utf-8').strip()
for tool,arguments in [('IndependentNativeAudit',['frozen']),('IndependentAllSupportAudit',[]),('IndependentRouteAudit',[])]:
    command = ['java','-Xmx1536m','-cp',classpath,tool,str(qa),*arguments]
    with (qa/(tool+'.log')).open('w',encoding='utf-8') as output:
        process = subprocess.run(command,stdout=output,stderr=subprocess.STDOUT,timeout=240)
    assert process.returncode == 0, tool
print('INDEPENDENT_NATIVE_QA_FINISHED',flush=True)
