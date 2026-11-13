import hashlib
import json
import math
import sys
from datetime import datetime
from pathlib import Path


root = Path(sys.argv[1]).resolve()
prior = root.parent / 'S5-submission-20261107-084436-900626'
original = root.parent / 'S5-submission-20261107-065459-179761'
digest = 'e0c71315c81727e81cd15011eda9a0f5da8624387c59b8b9764c42d8d740397e'


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def records(folder):
    result = {}
    for name in ('submission-snapshot.json', 'supplemental-snapshot.json', 'delivery-snapshot.json'):
        for item in load(folder / name)['files']:
            assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
            if item['source'] in result:
                assert result[item['source']]['sha256'] == item['sha256']
            result[item['source']] = item
    return result


old_files, current_files = records(prior), records(root)
assert len(old_files) == 974 and len(current_files) == 1016
assert not old_files.keys() - current_files.keys()
changed = []
for source in old_files.keys() & current_files.keys():
    if old_files[source]['sha256'] != current_files[source]['sha256']:
        relative = Path(current_files[source]['snapshot']).relative_to(root).as_posix()
        assert relative in {'UPPER-BARRELS-FIX.md', 'progress.json', 'delivery.py', 'ready-for-review.json', 'REVISION-REVIEW.md', 'README.md'}
        changed.append(relative)
assert len(changed) == 6
assert hashlib.sha256((root / 'merged/merged.nbt').read_bytes()).hexdigest() == digest
support = load(prior / 'independent-qa/independent-all-support.json')
assert sum(item['nonair_checked'] for item in support) == 176284
assert all(not item['failures'] and not item['unsupported'] for item in support)
routes = load(prior / 'independent-qa/independent-routes.json')
targets = [target for item in routes for target in item['targets']]
assert len(targets) == 125
failed = [target for target in targets if not target['reachable'] or not target['standing_clear']]
assert len(failed) == 1 and failed[0]['position'] == [12, 5, 69]
interaction_routes = [target for item in load(prior / 'independent-interaction-qa/independent-routes.json') for target in item['targets']]
assert len(interaction_routes) == 74 and all(target['reachable'] and target['standing_clear'] for target in interaction_routes)
payload = load(prior / 'independent-qa/native-rotation-payload-parts-barrier.json')
assert payload['sha256'] == digest and not payload['native_rotation_or_payload_mismatches']
assert payload['parts_exact_full_payload_and_volume'] and payload['part_count'] == 10
assert payload['merged_records'] == 591542 and payload['barrier_boundary_cells'] == 57482 and payload['terminal_glass_cells'] == 40
inspection = load(prior / 'independent-interaction-qa/storage-fix-inspection.json')
assert inspection['source_sha256'] == digest and inspection['all_functional_storage_and_levers'] == 37
assert inspection['max_eye_to_hit'] < 4.5 and inspection['independent_routes_prepared'] == 74
assert all(item['full_state_and_be_equal'] and item['placed_sha256'] == item['after1200_sha256'] for item in inspection['world_full_snapshot_comparisons'])
reproduction = load(prior / 'independent-qa/storage-reproduction.json')
assert reproduction['changed_source17_and_merged_byte_exact'] and reproduction['merged_sha256'] == digest
old_reproduction = load(original / 'independent-qa/reproduction-report.json')
assert old_reproduction['passed']
for item in old_reproduction['results']:
    if item['id'] in reproduction['inherited_nine_from_parent_previous_independent_outputs']:
        candidate = root / item['id'] / (item['id'].lower() + '.nbt')
        assert item['byte_equal'] and Path(item['output']).read_bytes() == candidate.read_bytes()
design = load(root / 'merged/design.json')
assert len(design['modules']) == 10
motion = {}
for mode in ('natural-movement', 'natural-detours'):
    folder = root / ('independent-' + mode) / 'qa' / mode
    assert load(folder / 'independent-result.json')['exit_code'] == 0
    result = load(folder / 'motion-results.json')
    assert result['passed'] and result['sha256'] == digest
    source = (folder / 'native-test/src/main/java/net/farmingtales/s5waterqa/WaterTests.java').read_bytes()
    assert source == (root / 'qa' / mode / 'native-test/src/main/java/net/farmingtales/s5waterqa/WaterTests.java').read_bytes()
    code = source.decode('utf-8')
    assert code.count('actor.setPos(') == 1
    assert all(text not in code for text in ('setDeltaMovement(', 'setNoGravity(', 'moveTo(', 'setMaxUpStep(', '.teleportTo('))
    assert 'super.baseTick();' in code and 'super.aiStep();' in code and 'helper.onEachTick(' in code
    assert (folder / 'planned-paths.json').read_bytes() == (root / 'qa' / mode / 'planned-paths.json').read_bytes()
    frames = result['frames']
    assert [frame['tick'] for frame in frames] == list(range(1, len(frames) + 1))
    assert result['ticks'] == len(frames) + 1
    spawn = result['events'][0]['feet']
    assert spawn == [10.5, 9.0, 23.5]
    positions = [spawn] + [frame['feet'] for frame in frames]
    distances = [math.dist(first, second) for first, second in zip(positions, positions[1:])]
    assert max(distances) < 0.9 and math.dist(positions[-1], spawn) < 0.16
    summary = dict(physics_frames=len(frames), control_ticks=result['ticks'], max_tick_displacement=max(distances), distance=sum(distances), final_feet=positions[-1], final_error=math.dist(positions[-1], spawn), final_on_ground=frames[-1]['on_ground'], ladder_ticks=sum(frame['ladder'] for frame in frames), water_ticks=sum(frame['water'] for frame in frames), jump_intent_ticks=sum(frame['jump_intent'] for frame in frames))
    assert summary['final_on_ground']
    if mode == 'natural-movement':
        assert result['waypoints_reached'] == result['total_waypoints'] == 1937
        turn = next(event['tick'] for event in result['events'] if event['event'] == 'finish_turnaround')
        visits = {}
        for label, subset in (('outbound', [frame for frame in frames if frame['tick'] < turn]), ('return', [frame for frame in frames if frame['tick'] >= turn])):
            visits[label] = {}
            for module in design['modules']:
                origin, size = module['origin'], module['size']
                hits = [frame for frame in subset if all(origin[axis] <= frame['feet'][axis] < origin[axis] + size[axis] for axis in range(3))]
                assert hits, (label, module['id'])
                visits[label][module['id']] = dict(frames=len(hits), first_tick=hits[0]['tick'], last_tick=hits[-1]['tick'])
        assert min(point[1] for point in positions) <= 5.01 and max(point[1] for point in positions) >= 9.49
        assert summary['jump_intent_ticks'] == 0
        summary['room_visits'] = visits
        summary['turnaround_tick'] = turn
    else:
        assert summary['ladder_ticks'] > 10 and summary['water_ticks'] > 10
        assert max(point[1] for point in positions) >= 14 and min(point[1] for point in positions) <= 8
        assert [event['event'] for event in result['events']] == ['initial_spawn_only', 'climb', 'descend', 'return_from_ladder', 'to_water', 'enter_water', 'water_hold', 'exit_water', 'return_from_water']
        summary['events'] = result['events']
    motion[mode] = summary
result = dict(group='S5', accepted=True, verdict='ACCEPTED', reviewed_at=datetime.now().astimezone().isoformat(), source_sha256=digest, frozen_files_verified=1016, inherited_files_verified=974, source_and_previous_evidence_unchanged=True, harmless_changed_metadata=changed, native_support_checks=176284, valid_static_routes=124, retained_expected_terminal_failure=failed, functional_targets=37, functional_approach_routes=74, motion=motion, unresolved_required_items=[], requirements=[
    dict(requirement='Ten functional reference rooms, real previews and original recipes', result='PASS', evidence=[str(prior/'STORAGE-FIX-REVIEW.md'), str(original/'independent-qa/reproduction-report.json'), str(prior/'independent-qa/storage-reproduction.json')]),
    dict(requirement='Native support/routes, preserved source payload, 10 parts and full barrier shell', result='PASS', evidence=[str(prior/'independent-qa/native-rotation-payload-parts-barrier.json'), str(prior/'independent-qa/independent-all-support.json')]),
    dict(requirement='All37 storage/lever functional targets with real eye ray/use and reachable stands', result='PASS', evidence=[str(prior/'independent-interaction-qa/storage-fix-inspection.json'), str(prior/'independent-interaction-qa/independent-routes.json')]),
    dict(requirement='Current-hash finite world stability and reserved-space installable external access', result='PASS', evidence=[str(prior/'STORAGE-FIX-REVIEW.md'), str(root.parent/'S5-submission-20261107-080041-052971/INTERACTION-ACCESS-REVIEW.md')]),
    dict(requirement='Continuous native whole-map return, necessary stairs, original ladder and shallow water', result='PASS', evidence=[str(root/'independent-natural-movement/qa/natural-movement/motion-results.json'), str(root/'independent-natural-detours/qa/natural-detours/motion-results.json')])
], limits=['No production deployment, connected client GUI, combat, rewards, oxygen survival or indefinite plant-growth guarantee', 'Natural physics tests are actual parent-run server callbacks; source subclass opts into local player physics, not a socket-connected client', '37 eye-ray/use and1200-tick snapshots were independently inspected producer evidence, not falsely claimed as parent world reruns', 'Fixed unrotated reserved overworld site with separate access lobby; administrator first brings players to lobby', 'Legacy sealed terminal target and expected three-wide training throat remain explicitly recorded'])
(root / 'ACCEPTANCE.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps({key:value for key,value in result.items() if key not in ('requirements','limits','harmless_changed_metadata','motion')}, ensure_ascii=False))
print(json.dumps({name:{key:value for key,value in summary.items() if key not in ('room_visits','events')} for name,summary in motion.items()}, ensure_ascii=False))
