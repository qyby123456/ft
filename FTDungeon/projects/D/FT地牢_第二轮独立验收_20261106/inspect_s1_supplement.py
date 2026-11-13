import hashlib
import json
import math
import re
import sys
import zipfile
from collections import Counter
from pathlib import Path

from audit_s8_submission import decode, load


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    root = Path(sys.argv[1]).resolve()
    prior = root.parent / 'S1-submission-20261107-081456-028610'
    records = {}
    for name in ('submission-snapshot.json', 'supplemental-snapshot.json', 'delivery-snapshot.json'):
        records.update({item['source']: item for item in load(root / name)['files']})
    for item in records.values():
        assert digest(Path(item['snapshot'])) == item['sha256']
    manifest, previous = load(root / 'ready-for-review.json'), load(prior / 'ready-for-review.json')
    for room, old in zip(manifest['rooms'], previous['rooms']):
        assert room['id'] == old['id'] and room['sha256'] == old['sha256']
    assembly = manifest['merged']
    assert assembly['sha256'] == previous['merged']['sha256']
    inputs = load(root / 'worldqa-interactions/inputs.json')
    interactions = load(root / 'worldqa-interactions/interaction-report.json')
    assert interactions['failures'] == 0 and len(interactions['candidates']) == len(inputs) == 11
    routes, details = [], []
    kinds = Counter()
    for index, (specification, report) in enumerate(zip(inputs, interactions['candidates'])):
        assert specification['id'] == report['id'] and specification['sha256'] == report['source_sha256']
        candidate = Path(records[specification['candidate']]['snapshot'])
        assert digest(candidate) == specification['sha256']
        data, blocks = decode(candidate)
        required = {position for position, value in blocks.items() if value['state']['Name'].endswith('_button') or value['nbt'] and value['nbt'].get('id') in {'minecraft:barrel', 'minecraft:chest', 'minecraft:furnace', 'minecraft:blast_furnace', 'minecraft:smoker', 'minecraft:brewing_stand', 'minecraft:hopper', 'minecraft:dispenser', 'minecraft:dropper'}}
        assert required == {tuple(item['target']) for item in report['interactions']}
        assert not report['unreachable_or_occluded']
        starts = set()
        targets = []
        for item in report['interactions']:
            assert item['passed'] and not item['blocked_lid'] and item['clip'] == 'OUTLINE/NONE actual first hit'
            assert abs(math.dist(item['eye_world'], item['first_hit_world']) - item['distance']) < 1e-8
            assert item['distance'] <= 4.5 and item['use_result'] in {'CONSUME', 'SUCCESS', 'CONSUME_PARTIAL'}
            expected_block = [item['target'][axis] + report['origin'][axis] for axis in range(3)]
            assert item['first_hit_block'] == expected_block
            path = item['connected_path_local_feet']
            assert path and path[-1] == item['standing_local_feet']
            starts.add(tuple(path[0]))
            for first, second in zip(path, path[1:]):
                assert max(abs(first[axis] - second[axis]) for axis in range(3)) <= 0.50001
                assert abs(first[0] - second[0]) + abs(first[2] - second[2]) <= 0.50001
            for axis in (0, 2):
                assert abs(item['eye_world'][axis] - report['origin'][axis] - path[-1][axis]) < 1e-7
            assert abs(item['eye_world'][1] - report['origin'][1] - path[-1][1] - 1.6200000047683716) < 1e-7
            targets.append({'label': item['kind'] + ':' + str(item['target']), 'position': [path[-1][0] - 0.5, path[-1][1], path[-1][2] - 0.5]})
            kinds[item['kind']] += 1
        if not required:
            assert not starts
            details.append({'id': report['id'], 'all_functional_containers_and_buttons': 0})
            continue
        assert len(starts) == 1
        start = next(iter(starts))
        routes.append({'group': 1111 + index, 'candidate': str(candidate), 'sha256': specification['sha256'], 'size': data['size'], 'start': [start[0] - 0.5, start[1], start[2] - 0.5], 'targets': targets, 'swimming': False})
        details.append({'id': report['id'], 'all_functional_containers_and_buttons': len(required), 'max_distance': max(item['distance'] for item in report['interactions'])})
    assert kinds == {'container': 64, 'button': 12}
    natural = load(root / 'worldqa-natural7/natural-travel-report.json')
    assert natural['status'] == 'PASS' and natural['candidate_sha256'] == assembly['sha256']
    assert len(natural['completed']) == 23 and natural['max_up_step'] == 0.6
    assert natural['forced_chunks_at_end'] == 0 and not natural['watch_chunk_loaded_before_entry']
    assert natural['natural_loading_hold_ticks'] >= 600 and natural['elapsed_server_ticks'] == 5880
    trace = natural['trace']
    assert all(first['game_time'] < second['game_time'] for first, second in zip(trace, trace[1:]))
    assert all(first['game_time'] < second['game_time'] for first, second in zip(natural['completed'], natural['completed'][1:]))
    assert all(event['permission'] == 0 and event['command_result'] > 0 for event in natural['events'])
    assert natural['final_position'] == [9995.5, 70.0, 10129.5]
    assert load(root / 'worldqa-natural7/exit.json')['exit_code'] == 0
    plan = load(root / 'access-package/installation-plan.json')
    assert plan['candidate_sha256'] == assembly['sha256']
    archive = root / 'access-package/s1_access_1_20_1.zip'
    assert digest(archive) == digest(root / 'worldqa-natural7/installed-pack.zip')
    pack = root / 'access-package/s1_access_1_20_1'
    with zipfile.ZipFile(archive) as zipped:
        for name in zipped.namelist():
            assert zipped.read(name) == (pack / name).read_bytes()
    installation = (pack / 'data/s1/functions/install.mcfunction').read_text(encoding='utf-8')
    commands = re.findall(r'place template (s1:part_\d+) (\d+) (\d+) (\d+) none none 1\.0 0', installation)
    pieces = plan['chunks']
    assert len(commands) == len(pieces) == 33
    for piece, command in zip(pieces, commands):
        assert piece['resource'] == command[0]
        assert [int(value) for value in command[1:]] == [plan['origin'][axis] + piece['offset'][axis] for axis in range(3)]
        resource = pack / 'data/s1/structures' / (command[0].split(':')[1] + '.nbt')
        assert digest(resource) == piece['sha256'] == records[piece['source']]['sha256']
    destination = root / 'independent-supplement-qa'
    destination.mkdir(exist_ok=True)
    (destination / 'route-input.json').write_text(json.dumps(routes, ensure_ascii=False, indent=2), encoding='utf-8')
    tags = next(Path(item['snapshot']) for item in records.values() if item['source'].endswith('vanilla-block-tags.json'))
    (destination / 'vanilla-block-tags.json').write_bytes(tags.read_bytes())
    result = {'stage': 'INDEPENDENT_FROZEN_S1_SUPPLEMENT_INSPECTION_NOT_PARENT_WORLD_RERUN', 'source_sha256': assembly['sha256'], 'frozen_files_verified': len(records), 'unchanged_ten_rooms_and_merged': True, 'interactions': details, 'kinds': dict(kinds), 'all_76_stands_prepared_for_parent_native_routes': True, 'natural_waypoints': len(natural['completed']), 'natural_trace_frames': len(trace), 'natural_elapsed_ticks': natural['elapsed_server_ticks'], 'zip_sha256': digest(archive), 'all_33_packed_parts_exact': True, 'installed_zip_copy_exact': True, 'pack_loading': 'Actual world datapack ZIP, not mod data resources; source preparation and installed ZIP capture inspected', 'accepted': False}
    (destination / 'inspection.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(result, ensure_ascii=False))


if __name__ == '__main__':
    main()
