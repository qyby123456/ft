import hashlib
import json
import math
from collections import Counter
from pathlib import Path

from audit_s8_submission import decode, load


def main():
    root = Path(__file__).parent
    base = root / 'S7-submission-20261107-083104-571437'
    supplement = root / 'S7-supplement-20261107-083852-053018'
    captured = load(base / 'submission-snapshot.json')['files']
    for item in captured:
        assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
    by_source = {item['source']: item for item in captured}
    empty = {'state': {'Name': 'minecraft:air'}, 'nbt': None}
    report = load(base / 'worldqa-validated/world-report.json')
    inputs = load(base / 'worldqa-validated/inputs.json')
    summaries = []
    for specification in inputs:
        identifier = specification['id']
        original = by_source[specification['candidate']]
        assert original['sha256'] == specification['sha256']
        data, before = decode(Path(original['snapshot']))
        after_data, after = decode(base / 'worldqa-validated' / (identifier + '-after-1200.nbt'))
        assert data['size'] == after_data['size']
        evidence = next(item for item in report['rooms'] if item['id'] == identifier)
        assert evidence['sha256'] == original['sha256'] and evidence['ticks'] == 1200
        kinds = Counter()
        types, states, entities = [], [], []
        for position in before.keys() | after.keys():
            observed = after.get(position, empty)
            expected = before.get(position, empty)
            if expected['state']['Name'] != observed['state']['Name']:
                types.append({'position': position, 'before': expected['state'], 'after': observed['state']})
            elif expected['state'] != observed['state']:
                states.append({'position': position, 'before': expected['state'], 'after': observed['state']})
                properties = expected['state'].get('Properties', {})
                changed = any(value != observed['state'].get('Properties', {}).get(key) for key, value in properties.items())
                kinds[('explicit:' if changed else 'defaults:') + expected['state']['Name']] += 1
            if expected['nbt'] != observed['nbt']:
                entities.append({'position': position, 'before': expected['nbt'], 'after': observed['nbt']})
        summaries.append({'id': identifier, 'source_sha256': original['sha256'], 'declared_capture_volume': math.prod(data['size']), 'sparse_after_records': len(after), 'capture_scope': 'StructureTemplate.fillFromWorld with Blocks.AIR exclusion; absent cells treated as air after inspecting exporter source', 'type_changes': types, 'raw_state_changes': states, 'state_kinds': dict(kinds), 'raw_be_changes': entities})
        print(identifier, 'cells', len(after), 'types', len(types), 'states', len(states), 'BE', len(entities), flush=True)
    movement = load(supplement / 'worldqa-vault-final/movement-report.json')
    paths = load(supplement / 'paths/author-paths.json')[0]
    assert paths['sha256'] == inputs[-1]['sha256'] == movement['inputs'][0]['sha256']
    assert len(paths['targets']) == len(movement['targets']) == 104
    for path, result in zip(paths['targets'], movement['targets']):
        assert path['label'] == result['label'] and result['reached']
        assert result['waypoints_completed'] == len(path['path_feet']) - 1
        assert all(abs(result['final_feet'][axis] - path['path_feet'][-1][axis]) < 0.001 for axis in range(3))
    assert not movement['failures'] and movement['ladder_climbed'] and movement['upper_landing_reached']
    support = load(base / 'independent-qa/independent-all-support.json')
    routes = load(base / 'independent-qa/independent-routes.json')
    assert all(not row['failures'] and not row['unsupported'] for row in support)
    assert all(target['reachable'] and target['standing_clear'] for row in routes for target in row['targets'])
    output = {'stage': 'INDEPENDENT_INSPECTION_OF_FROZEN_PRODUCER_WORLD_EVIDENCE_NOT_PARENT_WORLD_RUN', 'frozen_files_verified': len(captured), 'support_checked': sum(row['nonair_checked'] for row in support), 'static_route_targets': sum(len(row['targets']) for row in routes), 'world': summaries, 'movement_paths_verified': 104, 'move_calls': movement['move_calls'], 'movement_scope': movement['scope'], 'accepted': False}
    (base / 'independent-qa/world-and-movement-inspection.json').write_text(json.dumps(output, ensure_ascii=False, indent=2), encoding='utf-8')
    posttick = base / 'independent-posttick-qa'
    posttick.mkdir(exist_ok=True)
    route_inputs = load(base / 'independent-qa/route-input.json')
    assert len(route_inputs) == len(inputs)
    for route, specification in zip(route_inputs, inputs):
        assert route['sha256'] == specification['sha256']
        candidate = base / 'worldqa-validated' / (specification['id'] + '-after-1200.nbt')
        route['candidate'] = str(candidate)
        route['sha256'] = hashlib.sha256(candidate.read_bytes()).hexdigest()
    (posttick / 'route-input.json').write_text(json.dumps(route_inputs, ensure_ascii=False, indent=2), encoding='utf-8')
    (posttick / 'vanilla-block-tags.json').write_bytes((base / 'independent-qa/vanilla-block-tags.json').read_bytes())


if __name__ == '__main__':
    main()
