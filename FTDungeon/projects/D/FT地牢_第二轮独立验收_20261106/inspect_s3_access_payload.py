import hashlib
import json
import math
from itertools import combinations, product
from pathlib import Path

from audit_s8_submission import decode, load

workspace = Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root = workspace / 'preserved-review-snapshots/S3-access-revision-20261107-1103'
prior = Path(__file__).parent / 'S3-submission-20261107-072954-274564'
snapshot = load(root / 'submission-snapshot.json')
old_ready = load(prior / 'ready-for-review.json')
old_files = {item['source']: Path(item['snapshot']) for item in load(prior / 'submission-snapshot.json')['files']}
for item in snapshot['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
layout = load(root / 'assembly.json')
merged_path = root / 'rooms/s3-merged/s3-merged.nbt'
data, merged = decode(merged_path)
old_data, old_merged = decode(old_files[old_ready['merged']['candidate']['path']])
empty = {'state': {'Name': 'minecraft:air'}, 'nbt': None}
changes = []
room_reports = []
for specification, module in zip(old_ready['rooms'], layout['modules']):
    identifier = specification['id']
    assert identifier == module['id']
    source_path = root / 'rooms' / identifier / (identifier + '.nbt')
    original, before = decode(old_files[specification['candidate']['path']])
    current, after = decode(source_path)
    assert original['size'] == current['size'] == module['size'] and not current['entities']
    assert hashlib.sha256(source_path.read_bytes()).hexdigest() == module['sha256']
    differing = []
    for position in before.keys() | after.keys():
        first, second = before.get(position,empty), after.get(position,empty)
        if first != second:
            assert identifier == 's3-16' and first == empty and second['state']['Name'] == 'minecraft:stone_brick_slab'
            differing.append(position)
    assert len(differing) == (3 if identifier == 's3-16' else 0)
    assert load(root / 'rooms' / identifier / 'design.json')['targets'] == load(old_files[specification['design']['path']])['targets']
    for position, item in after.items():
        global_position = tuple(value + offset for value, offset in zip(position,module['origin']))
        assert merged[global_position] == item
    room_reports.append({'id':identifier,'original_payload_retained':True,'added_air_cells':differing,'sha256':module['sha256']})
for position in old_merged.keys() | merged.keys():
    first, second = old_merged.get(position,empty), merged.get(position,empty)
    if first != second:
        assert first == empty and second['state']['Name'] == 'minecraft:stone_brick_slab'
        changes.append(position)
assert set(changes) == {(120,3,75),(120,4,77),(120,4,78)}
assert {position for position,item in old_merged.items() if item['state']['Name']=='minecraft:barrier'} == {position for position,item in merged.items() if item['state']['Name']=='minecraft:barrier'}
parts = load(root / 'parts/manifest.json')
assert parts['source_sha256'] == hashlib.sha256(merged_path.read_bytes()).hexdigest()
restored = {}
boxes = []
for part in parts['parts']:
    candidate = root / 'parts' / Path(part['path']).name
    assert hashlib.sha256(candidate.read_bytes()).hexdigest() == part['sha256']
    piece, cells = decode(candidate)
    assert piece['size'] == part['size'] and max(piece['size']) <=48 and not piece['entities']
    boxes.append((part['origin'],part['size']))
    for position,item in cells.items():
        global_position = tuple(value+offset for value,offset in zip(position,part['origin']))
        assert global_position not in restored
        restored[global_position] = item
assert restored == merged and len(boxes) == 25
assert sum(math.prod(size) for origin,size in boxes) == math.prod(data['size'])
assert all(any(max(first[0][axis],second[0][axis]) >= min(first[0][axis]+first[1][axis],second[0][axis]+second[1][axis]) for axis in range(3)) for first,second in combinations(boxes,2))
boundary = set()
for axis in range(3):
    others=[value for value in range(3) if value!=axis]
    for edge,first,second in product((0,data['size'][axis]-1),range(data['size'][others[0]]),range(data['size'][others[1]])):
        point=[0,0,0];point[axis]=edge;point[others[0]]=first;point[others[1]]=second;boundary.add(tuple(point))
assert len(boundary)==117690 and all(merged[position]['state']['Name']=='minecraft:barrier' for position in boundary)
result={'accepted':False,'status':'CURRENT_PAYLOAD_PARTS_AND_THREE_AIR_ADDITIONS_PASS','source_sha256':parts['source_sha256'],'rooms':room_reports,'merged_changes':changes,'merged_records':len(merged),'parts':25,'full_volume':math.prod(data['size']),'barrier_cells':117690,'original_targets_retained':True,'all_original_nonair_and_be_retained':True}
(root / 'independent-payload-inspection.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({key:value for key,value in result.items() if key!='rooms'},ensure_ascii=False))
