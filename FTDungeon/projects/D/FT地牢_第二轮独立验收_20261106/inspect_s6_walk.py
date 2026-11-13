import gc
import hashlib
import json
import sys
from collections import Counter
from pathlib import Path

from audit_s8_submission import decode


root = Path(sys.argv[1]).resolve()


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


records = {}
for name in ('submission-snapshot.json', 'supplemental-snapshot.json', 'delivery-snapshot.json'):
    for item in load(root / name)['files']:
        assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
        records[item['source']] = item
assert len(records) == 763
pointer = load(root / 'independent-walk-result.json')
assert pointer['exit_code'] == 0
scratch = Path(pointer['scratch'])
report = load(scratch / 'assembly/access/walk-interaction-result.json')
assert report['status'] == 'PASS'
digest = '5b72d94c89871ebe9c262f92d6c00275ee0c8cfbf32343be96c9cd8e538f1336'
assert report['source_sha256'] == digest
assert report['initial_teleports'] == 1 and report['mid_route_teleports'] == 0
assert report['maximum_tick_displacement'] < 2 and report['natural_movement_ticks'] > 1000
java_relative = 'qa/runtime/src/main/java/net/farmingtales/s6qa/WalkInteractionTests.java'
source = (scratch / java_relative).read_bytes()
assert source == (root / java_relative).read_bytes()
assert hashlib.sha256(source).hexdigest() == report['test_source_sha256']
code = source.decode('utf-8')
assert code.count('player.setPos(') == 1 and '.teleport' not in code and 'setNoGravity(true)' not in code
assert 'player.setDeltaMovement(0,player.getDeltaMovement().y,0);' in code
assert 'player.setMaxUpStep(.6f)' in code
assert 'player.travel(' in code and 'helper.runAfterDelay' in code
source_data, before = decode(root / 'assembly/s6-volcanic-connected.nbt')
functional = {point:item['state']['Name'] for point,item in before.items() if item['state']['Name'] in ('minecraft:chest','minecraft:barrel','minecraft:smoker','minecraft:furnace','minecraft:lever')}
assert len(functional) == 36 and sum(name == 'minecraft:lever' for name in functional.values()) == 13
operations = [item for item in report['completed'] if 'target' in item]
assert len(operations) == 48


def point(text):
    return tuple(float(value.strip()) for value in text.strip('()').split(','))


observed = {}
for item in operations:
    target = tuple(int(value.strip()) for value in item['target'].split(','))
    assert tuple(int(value.strip()) for value in item['first_hit'].split(',')) == target
    assert item['eye_hit_distance'] <= 4.5 and item['native_use'] == 'CONSUME'
    eye, feet = point(item['eye']), point(item['feet'])
    assert abs(eye[0]-feet[0]) < 1e-6 and abs(eye[2]-feet[2]) < 1e-6 and abs(eye[1]-feet[1]-1.62) < 1e-6
    assert item['native_move_ticks_from_previous'] >= 1
    if item['label'].startswith('reach-use-reset '):
        assert target not in observed
        observed[target] = item
assert set(observed) == set(functional)
targets = load(root / 'assembly/design.json')['targets']
assert len(targets) == 58
arrived = {item['label']:item for item in report['completed'] if 'arrived' in item}
for target in targets:
    assert target['label'] in arrived
    actual = point(arrived[target['label']]['arrived'])
    expected = [target['position'][0]+.5,target['position'][1],target['position'][2]+.5]
    assert sum((first-second)**2 for first,second in zip(actual,expected))**.5 < .2
climb = next(item for item in report['completed'] if item['label'] == 'library ladder native climb')
descent = next(item for item in report['completed'] if item['label'] == 'library ladder descend and return')
assert climb['climbable'] and climb['highest_feet_y'] >= 8 and climb['ticks'] > 1
assert point(descent['feet'])[1] < 132.05 and descent['ticks'] > 1
trace = load(scratch / 'assembly/access/walk-trace.json')
assert [item['tick'] for item in trace] == list(range(20,report['natural_movement_ticks']+1,20))
assert any(item['climbable'] for item in trace)
capture = scratch / 'assembly/access/after-walk.nbt'
assert hashlib.sha256(capture.read_bytes()).hexdigest() == report['post_capture_sha256']
capture_matches_producer = capture.read_bytes() == (root / 'assembly/access/after-walk.nbt').read_bytes()
after_data, after = decode(capture)
assert after_data['size'] == source_data['size'] == [207,36,180] and len(after) == 1341360
state_changes = Counter()
for position, actual in after.items():
    expected = before.get(position, {'state':{'Name':'minecraft:air'},'nbt':None})
    assert actual['state']['Name'] == expected['state']['Name'], (position, expected['state'], actual['state'])
    if actual['state'] != expected['state']:
        state_changes[actual['state']['Name']] += 1
    assert not (actual['nbt'] or {}).get('Items') and not (actual['nbt'] or {}).get('LootTable')
    for key, value in (expected['nbt'] or {}).items():
        assert (actual['nbt'] or {}).get(key) == value, (position,key,value,actual['nbt'])
for position, name in functional.items():
    if name == 'minecraft:lever':
        assert after[position]['state']['Properties']['powered'] == 'false'
doors = [item for item in after.values() if item['state']['Name'] == 'minecraft:iron_door']
assert len(doors) == 12 and all(item['state']['Properties']['open'] == 'false' for item in doors)
del source_data, before, after_data, after
gc.collect()
result = dict(status='PASS_SCOPED', accepted=False, source_sha256=digest, frozen_files_verified=763, parent_runtime=str(scratch), natural_movement_ticks=report['natural_movement_ticks'], distance_blocks=report['distance_blocks'], maximum_tick_displacement=report['maximum_tick_displacement'], original_targets_actually_arrived=58, functional_blocks=36, containers=23, levers=13, actual_eye_ray_use_operations=48, maximum_eye_distance=max(item['eye_hit_distance'] for item in operations), native_ladder_climb_and_return=True, full_capture_cells=1341360, all_source_block_types_preserved=True, original_entity_fields_preserved=True, empty_containers=True, all_controls_and_doors_restored=True, capture_byte_equal_producer=capture_matches_producer, raw_state_differences_by_type=dict(state_changes), limitations=['Exact source and previous access/static/visual inheritance still require final whole-group completion audit', 'Per20-tick trace and operation stops, not full-rate coordinate logging', 'Horizontal velocity is stopped at jobs; vertical velocity is retained, no climb Y injection', 'Ladder return reaches original library floor; not an exact initial-entry coordinate loop', 'No connected client GUI, survival/combat, random tick or rotation certification'])
(root / 'independent-walk-inspection.json').write_text(json.dumps(result, ensure_ascii=False, indent=2),encoding='utf-8')
print(json.dumps(result, ensure_ascii=False))
