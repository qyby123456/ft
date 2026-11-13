import hashlib
import json
import math
import re
import sys
from datetime import datetime
from itertools import combinations
from pathlib import Path

from audit_s8_submission import decode, load


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def checked_routes(path, count):
    reports = load(path)
    assert all(report['start_clear'] for report in reports)
    targets = [target for report in reports for target in report['targets']]
    assert len(targets) == count and all(target['standing_clear'] and target['reachable'] for target in targets)


def main():
    root = Path(sys.argv[1]).resolve()
    prior = root.parent / 'S1-submission-20261107-081456-028610'
    records, old_records = {}, {}
    for name in ('submission-snapshot.json', 'supplemental-snapshot.json', 'delivery-snapshot.json'):
        records.update({item['source']: item for item in load(root / name)['files']})
        old_records.update({item['source']: item for item in load(prior / name)['files']})
    for item in [*records.values(), *old_records.values()]:
        assert digest(Path(item['snapshot'])) == item['sha256']
    current, previous = load(root / 'ready-for-review.json'), load(prior / 'ready-for-review.json')
    assert len(current['rooms']) == 10
    for room, old in zip(current['rooms'], previous['rooms']):
        assert room['id'] == old['id'] and room['sha256'] == old['sha256']
    assert current['merged']['sha256'] == previous['merged']['sha256']
    empty_inventory_checks = 0
    for room in [*current['rooms'], current['merged']]:
        data, blocks = decode(Path(records[room['candidate']]['snapshot']))
        for value in blocks.values():
            payload = value['nbt'] or {}
            if 'Items' in payload:
                assert payload['Items'] == []
                empty_inventory_checks += 1
            assert not payload.get('LootTable')
    for source, record in old_records.items():
        if source in records and Path(source).suffix.lower() in {'.nbt', '.mjs', '.png', '.jpg'}:
            assert records[source]['sha256'] == record['sha256'], source
    native = load(prior / 'independent-qa/independent-native-frozen.json')
    assert len(native) == 11
    assert all(item['registry_properties'] == item['template_roundtrip_block_count'] == 'PASS' for item in native)
    support = load(prior / 'independent-qa/independent-all-support.json')
    assert sum(item['nonair_checked'] for item in support) == 385912
    assert all(not item['failures'] and not item['unsupported'] for item in support)
    checked_routes(prior / 'independent-qa/independent-routes.json', 132)
    checked_routes(prior / 'independent-posttick-qa/independent-routes.json', 132)
    checked_routes(prior / 'independent-corridor-qa/independent-routes.json', 6407)
    checked_routes(root / 'independent-posttick-interaction-qa/independent-routes.json', 76)
    payload = load(prior / 'independent-qa/payload-entities-parts.json')
    assert payload['parts_payload_and_entities_exact'] and payload['part_count'] == 33
    assert payload['parts_volume'] == payload['full_volume'] == 2715840
    assert payload['rectangular_boundary_cells'] == 183520 and payload['rectangular_boundary_nonbarrier_cells'] == 0
    assert not payload['original_target_positions_missing_in_merged']
    corridor = load(prior / 'independent-qa/corridor-scenery.json')
    assert corridor['all_declared_air_height_floor_and_neighbor_steps_checked'] and len(corridor['full_port_envelopes']) == 20
    assert corridor['scenery_depth_extent_retained'] == 64 and corridor['scenery_nonair_exactly_retained'] == 5704
    world = load(prior / 'independent-qa/world-classification.json')
    assert len(world['candidates']) == 11 and all(item['type_changes'] == 0 for item in world['candidates'])
    reproduction = load(prior / 'independent-qa/reproduction.json')
    assert reproduction['all_45_nbt_outputs_binary_equal'] and not reproduction['candidate_nbt_copied_as_generation_input']
    inspection = load(root / 'independent-supplement-qa/inspection.json')
    assert inspection['kinds'] == {'container': 64, 'button': 12} and inspection['all_33_packed_parts_exact']
    natural = load(root / 'independent-natural-world/independent-result.json')
    interactions = load(root / 'independent-interaction-world/independent-result.json')
    assert natural['status'] == interactions['status'] == 'PASS_SCOPED'
    assert natural['natural_waypoints'] == 23 and interactions['actual_native_ray_and_use_interactions'] == 76
    installation = (root / 'access-package/s1_access_1_20_1/data/s1/functions/install.mcfunction').read_text(encoding='utf-8')
    plan = load(root / 'access-package/installation-plan.json')
    boxes = []
    for match in re.findall(r'^execute in minecraft:overworld run fill (\d+) (\d+) (\d+) (\d+) (\d+) (\d+) minecraft:air$', installation, re.M):
        values = [int(value) for value in match]
        lower, upper = values[:3], values[3:]
        assert all(plan['clear_inclusive']['min'][axis] <= lower[axis] <= upper[axis] <= plan['clear_inclusive']['max'][axis] for axis in range(3))
        assert math.prod(upper[axis] - lower[axis] + 1 for axis in range(3)) <= 32768
        boxes.append((lower, upper))
    for first, second in combinations(boxes, 2):
        assert not all(max(first[0][axis], second[0][axis]) <= min(first[1][axis], second[1][axis]) for axis in range(3))
    assert sum(math.prod(upper[axis] - lower[axis] + 1 for axis in range(3)) for lower, upper in boxes) == math.prod(plan['clear_inclusive']['max'][axis] - plan['clear_inclusive']['min'][axis] + 1 for axis in range(3))
    requirements = [
        {'requirement': 'Ten distinct reference rooms, complete NBT, reproducible recipes and actual previews including room20 secret vault', 'result': 'PASS', 'evidence': [str(prior / 'STATIC-REVIEW.md'), str(prior / 'independent-qa/reproduction.json')]},
        {'requirement': 'Native registry/properties, support, player-size routes, empty inventories and finite liquid/fire observation', 'result': 'PASS', 'evidence': [str(prior / 'independent-qa/independent-native-frozen.json'), str(prior / 'independent-qa/independent-all-support.json'), str(prior / 'independent-qa/world-classification.json')]},
        {'requirement': 'Connected merge preserving source payloads, aligned clear ports, glass/seam treatment and external barrier enclosure', 'result': 'PASS', 'evidence': [str(prior / 'independent-qa/payload-entities-parts.json'), str(prior / 'independent-qa/corridor-scenery.json'), str(prior / 'independent-corridor-qa/independent-routes.json')]},
        {'requirement': 'At most48 per-axis parts exactly reconstruct payloads and entities', 'result': 'PASS', 'evidence': [str(prior / 'independent-qa/payload-entities-parts.json')]},
        {'requirement': 'All functional containers and buttons have connected usable stands, actual eye ray first-hit and block.use', 'result': 'PASS', 'evidence': [str(root / 'independent-interaction-world/interaction-report.json'), str(root / 'independent-posttick-interaction-qa/independent-routes.json')]},
        {'requirement': 'Installable current-hash access with full clearance, automatic hooks, ordinary entry/return and natural ladder/stair motion', 'result': 'PASS', 'evidence': [str(root / 'independent-natural-world/independent-result.json'), str(root / 'access-package/installation-plan.json')]},
    ]
    result = {'group': 'S1', 'verdict': 'ACCEPTED', 'accepted': True, 'reviewed_at': datetime.now().astimezone().isoformat(), 'scope': 'Second-round ten-room delivery and connected dungeon under WORK_ORDER.md, with delegated access/interaction/natural ladder-stair follow-ups', 'source_sha256': current['merged']['sha256'], 'current_frozen_files_verified': len(records), 'prior_frozen_files_verified': len(old_records), 'empty_inventory_checks': empty_inventory_checks, 'requirements': requirements, 'unresolved_required_items': [], 'deployment': 'NOT_DEPLOYED; no formal saves/mods/config/KubeJS writes', 'limits': ['Server-side native acceptance; human network client GUI and human playthrough not certified', 'No combat/spawn/elite/Boss pack required by this round; handled separately after review', 'Unrotated fixed overworld location with reserved empty clearance box; other rotations/multiple instances not certified', 'Natural motion probe covers library ladder and watch stair roundtrip; remaining targets verified by collision connectivity and actual native ray/use', 'Finite1200tick observation does not certify indefinite growth, all random ticks, damage or drowning', 'Raw source fence connections normalize in actual world; two source-shape stands fail but all76 post-world stands pass; exact differences retained']}
    (root / 'ACCEPTANCE.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps({key: value for key, value in result.items() if key not in {'requirements', 'limits'}}, ensure_ascii=False))


if __name__ == '__main__':
    main()
