import hashlib
import json
import math
from collections import defaultdict
from pathlib import Path
from audit_s8_submission import load

workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
root = workspace / 'preserved-review-snapshots/S8-followup-20261107-1600'
current = workspace / 'preserved-review-snapshots/S8-interaction-revision-20261107-1355'
revision = load(current / 'access-revision-r4/REVISION.json')
digest = revision['sha256']
installation = workspace / 's8-independent-followup-installation-20261107-1600'
movement = workspace / 's8-independent-followup-movement-20261107-1600'
for kind in ('installation', 'movement'):
    progress = load(root / ('independent-' + kind + '-run.json'))
    assert progress['status'] == 'FINISHED' and progress['exit_code'] == 0 and progress['source_sha256'] == digest
for runtime in (installation, movement):
    assert hashlib.sha256((runtime / 'candidate.nbt').read_bytes()).hexdigest() == digest
    execution = load(runtime / 'execution-result.json')
    assert execution['pass'] and execution['source_sha256'] == digest
clear = load(installation / 'clear-result.json')
assert clear['seeded_stone_cells'] == clear['checked_cells'] == 1009200 and clear['remaining_non_air'] == 0
assert len(clear['commands']) == 41 and sum(row['result'] for row in clear['commands'][:-1]) == 1009200
assert all(row['permission_2'] and row['source_player'] == 'S8InstallHost' for row in clear['commands'])
installed = load(installation / 'installation-result.json')
assert installed['source_sha256'] == digest and installed['full_volume_checked'] == 1009200
assert installed['source_records'] == 199790 and installed['implicit_air_verified'] == 809410
assert installed['barriers'] == 90282 and installed['block_entity_payloads_expected'] == installed['block_entity_payloads_verified'] == 56
assert not installed['failures'] and len(installed['neighbor_shape_changes']) == 57
assert len(load(installation / 'bed-normalization.json')) == 12
access = load(installation / 'access-result.json')
assert access['barriers_before'] == access['barriers_after'] == 90282 and len(access['access']) == 4
for index, row in enumerate(access['access']):
    assert row['command_result'] == 1 and row['destination_correct'] and row['body_collision_free'] and row['supported_floor']
    assert row['function'] == ('s8_admin:enter' if index % 2 == 0 else 's8_admin:return')
    if index >= 2:
        assert row['guest_has_permission_2'] is False
    else:
        assert row['operator_permission_2'] and row['source_is_player']
for item in load(installation / 'install-input.json')['frozen_pack_files']:
    path = installation / item['path']
    assert len(path.read_bytes()) == item['bytes'] and hashlib.sha256(path.read_bytes()).hexdigest() == item['sha256']
saved = load(workspace / 's8-independent-saved-readback-20261107-1600/saved-readback.json')
assert saved['pass'] and saved['source_sha256'] == digest and saved['chunks_read'] == 150
assert saved['full_volume_checked'] == 1009200 and saved['barriers'] == 90282 and saved['payloads_verified'] == 56
assert saved['implicit_air_verified'] == 809410 and not saved['failures']
assert len(saved['neighbor_shape_changes']) == 57 and len(saved['bed_normalizations']) == 12
motion = load(movement / 'motion-results.json')
inputs = load(movement / 'motion-input.json')
traces = load(movement / 'motion-trace.json')
milestones = load(movement / 'motion-milestones.json')
preserved = load(movement / 'preserved-targets.json')
assert len(preserved) == 105 and all(row['unchanged'] and row['before'] == row['after'] for row in preserved)
assert motion['source_sha256'] == digest and not motion['findings'] and len(motion['courses']) == 4
assert len(traces) == 1082 and all(row['tick'] == index + 1 and row['body_clear'] and not row['flying'] and row['health'] > 0 for index, row in enumerate(traces))
source = (movement / 'native-test/src/main/java/net/farmingtales/s8qa/MotionTests.java').read_text(encoding='utf-8')
assert source.count('player.moveTo(') == source.count('player.setDeltaMovement(') == source.count('player.travel(') == 1
assert '.teleportTo(' not in source and '.setPos(' not in source
groups = defaultdict(list)
for row in traces:
    group = groups[row['course']]
    row['vertical_motion'] = row['position'][1] - group[-1]['position'][1] if group else 0
    group.append(row)
courses = []
maximum_step = 0
for course in motion['courses']:
    identifier = course['course']
    plan = next(plan for plan in inputs['courses'] if plan['id'] == identifier)
    marks = [row for row in milestones if row['course'] == identifier]
    samples = groups[identifier]
    assert course['pass'] and course['position_writes'] == 1 and course['ticks'] == len(samples)
    assert len(marks) == course['waypoints'] == len(plan['points'])
    assert [mark['point'] for mark in marks] == list(range(len(marks)))
    assert marks[0]['initial_position_write'] and marks[-1]['on_ground']
    error = math.dist(marks[0]['position'], marks[-1]['position'])
    assert error < .13
    assert all(mark.get('body_clear', True) for mark in marks)
    for before, after in zip(samples, samples[1:]):
        assert after['course_tick'] == before['course_tick'] + 1
        assert abs(after['position'][1] - before['position'][1]) <= .6
        assert math.hypot(*(after['position'][axis] - before['position'][axis] for axis in (0, 2))) < .3
        maximum_step = max(maximum_step, math.dist(before['position'], after['position']))
    courses.append(dict(course=identifier, native_updates=len(samples), waypoints=len(marks), grounded_return=True, return_error=error))
assert maximum_step < .6
coverage = []
for change in revision['changes']:
    module = next(module for module in revision['modules'] if module['id'] == change['room'])
    point = [value + offset for value, offset in zip(change['pos'], module['origin'])]
    ladder = change['name'].endswith('ladder')
    backing = change['room'] == 'S8-13' and change['pos'][0] == 20 and not ladder
    if ladder or backing:
        adjacent = [point[0], point[1], point[2] + 1] if backing else point
        matches = [row for row in traces if row['on_climbable'] and [math.floor(value) for value in row['position']] == adjacent]
        outbound = [row for row in matches if row['vertical_motion'] > .001]
        returning = [row for row in matches if row['vertical_motion'] < -.001]
        kind = 'STRUCTURAL_BACKING_ADJACENT_LADDER_USE' if backing else 'NATIVE_LADDER_ASCENT_AND_DESCENT'
    else:
        top = point[1] + (.5 if change['name'].endswith('slab') else 1)
        stacked = change['room'] == 'S8-13' and change['pos'] == [8, 2, 9]
        if stacked:
            top += .5
        matches = [row for row in traces if row['on_ground'] and abs(row['position'][1] - top) < .09 and all(row['position'][axis] + .3 > point[axis] and row['position'][axis] - .3 < point[axis] + 1 for axis in (0, 2))]
        if change['room'] == 'S8-13' and change['pos'][2] == 5:
            west = change['pos'][0] == 19
            outbound = [row for row in matches if row['waypoint'] <= 6] if west else [row for row in matches if 19 <= row['waypoint'] <= 24]
            returning = [row for row in matches if 6 < row['waypoint'] <= 10] if west else [row for row in matches if 25 <= row['waypoint'] <= 29]
        else:
            course = next(course for course in inputs['courses'] if course['id'] == matches[0]['course'])
            turn = next(index for index, point in enumerate(course['points']) if point['label'] == 'fixture-access-end')
            outbound = [row for row in matches if row['waypoint'] <= turn]
            returning = [row for row in matches if row['waypoint'] > turn]
        kind = 'STACKED_SUPPORT_VIA_OVERLYING_SLAB' if stacked else 'GROUNDED_OUTBOUND_AND_RETURN'
    assert outbound and returning, change
    coverage.append(dict(room=change['room'], local_position=change['pos'], candidate_position=point, kind=kind, outbound_samples=len(outbound), return_samples=len(returning)))
assert len(coverage) == 29
qa = current / 'independent-current-qa'
native = load(qa / 'independent-native-frozen.json')
support = load(qa / 'independent-all-support.json')
routes = load(qa / 'independent-routes.json')
assert len(native) == len(support) == len(routes) == 3
assert all(row['registry_properties'] == row['template_roundtrip_block_count'] == 'PASS' for row in native)
assert all(not row['failures'] and not row['unsupported'] for row in support)
assert all(row['start_clear'] and all(target['reachable'] and target['standing_clear'] for target in row['targets']) for row in routes)
for item in load(root / 'submission-snapshot.json')['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
result = dict(accepted=False, status='PARENT_CURRENT_INSTALL_SAVED_WORLD_AND_FOUR_NATURAL_COURSES_PASS', source_sha256=digest, frozen_files=216, full_volume=1009200, full_solid_seed_and_clear=True, implicit_air=809410, parts=20, barriers=90282, payloads=56, saved_chunks=150, shape_updates=57, legacy_bed_normalizations=12, admin_hosted_enter_return_operations=4, preserved_functional_targets=105, courses=courses, natural_travel_updates=len(traces), max_per_travel_displacement=maximum_step, all29_new_blocks_mapped=coverage, current_native_support_checks=sum(row['nonair_checked'] for row in support), current_route_targets=sum(len(row['targets']) for row in routes), limitations=['Admin-hosted functions, not autonomous permission0 access hooks', 'Unconnected server transport, no real client or production deployment', 'Four necessary local continuous courses, not a new full merged natural walk', 'Prior original11 ladder and old4413-waypoint walk bind old4d hash with explicit inheritance', 'Visual fidelity is pending because image tool returned no image content'])
(root / 'independent-followup-inspection.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps({key: value for key, value in result.items() if key not in ('all29_new_blocks_mapped', 'courses')}, ensure_ascii=False))
