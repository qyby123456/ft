import hashlib
import json
import math
import sys
from datetime import datetime
from pathlib import Path

from audit_s8_submission import decode, load


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def routes(path, count):
    rows = load(path)
    targets = [target for row in rows for target in row['targets']]
    assert len(targets) == count
    assert all(row['start_clear'] for row in rows)
    assert all(target['reachable'] and target['standing_clear'] for target in targets)


def main():
    root = Path(sys.argv[1]).resolve()
    access = root.parent / 'S2-access-20261107-092940-861925'
    interaction = root.parent / 'S2-supplement-20261107-080712-561936'
    records = {}
    for name in ('submission-snapshot.json', 'supplemental-snapshot.json'):
        records.update({item['source']: item for item in load(root / name)['files']})
    for folder, name in [(interaction, 'supplement-snapshot.json'), (access, 'access-snapshot.json')]:
        for item in load(folder / name)['files']:
            assert digest(Path(item['snapshot'])) == item['sha256']
    for item in records.values():
        assert digest(Path(item['snapshot'])) == item['sha256']
    ready = load(root / 'ready-for-review.json')
    assert len(ready['rooms']) == 10 and ready['assembly']['sha256'] == digest(root / 'assembly/s2-desert-functional-combined.nbt')
    earlier = root.parent / 'S2-submission-20261107-051940-148877'
    prior = load(earlier / 'ready-for-review.json')
    for room, old in zip(ready['rooms'], prior['rooms']):
        assert room['id'] == old['id'] and room['sha256'] == old['sha256']
        for name in ('recipe_path', 'render_path', 'visual_review_path'):
            assert room[name] in records
    native = load(root / 'independent-qa/independent-native-frozen.json')
    assert len(native) == 11 and all(item['registry_properties'] == item['template_roundtrip_block_count'] == 'PASS' for item in native)
    support = load(root / 'independent-qa/independent-all-support.json')
    assert sum(item['nonair_checked'] for item in support) == 127385
    assert all(not item['failures'] and not item['unsupported'] for item in support)
    routes(root / 'independent-qa/independent-routes.json', 120)
    routes(root / 'independent-corridor-qa/independent-routes.json', 1158)
    routes(interaction / 'independent-qa/independent-routes.json', 80)
    payload = load(root / 'independent-qa/native-rotation-payload-parts-barrier.json')
    assert not payload['source_differences'] and not payload['missing_original_target_positions']
    assert payload['parts_exact_state_be_reconstruction'] and payload['part_count'] == 15
    assert payload['parts_volume'] == payload['full_volume'] == 752850
    assert payload['barrier_cells'] == 38872 and payload['room_volume_cells_enclosed'] == 96068 and payload['merged_target_cells_enclosed'] == 69
    followup = load(root / 'independent-qa/static-followup.json')
    assert followup['unclassified_block_entity_changes'] == 0 and len(followup['reproduction']) == 16
    assert all(item['byte_exact'] and digest(Path(item['source'])) == digest(Path(item['reproduced'])) for item in followup['reproduction'])
    ladder = load(root / 'runtime-qa-r4/ladder-physics.json')
    assert ladder['source_sha256'] == ready['rooms'][0]['sha256'] and ladder['top_landing_clear'] and ladder['climbable_observed']
    assert ladder['highest_relative_y'] >= 9
    interacted = load(interaction / 'independent-qa/interaction-inspection.json')
    assert interacted['runtime_fixtures'] == 40 and interacted['server_menus_opened_empty'] == 32 and interacted['doors_opened_and_restored'] == 8
    assert interacted['standing_interactions'] == 39 and interacted['native_jump_interactions'] == 1
    access_result = load(access / 'independent-access-world/independent-result.json')
    assert access_result['status'] == 'PASS_SCOPED' and access_result['report']['roundtrips'] == 2
    assert load(access / 'independent-access-clearance.json')['full_source_air_clearance_cells'] == 752850
    original = load(root / 'runtime-assembly-r1/assembly-input.json')
    natural = load(root / 'independent-natural-world/assembly-runtime.json')
    assert natural['source_sha256'] == ready['assembly']['sha256']
    assert natural['movement_roundtrip_complete'] and natural['movement_waypoints_total'] == natural['movement_waypoints_reached'] == len(original['movement_route']) == 4215
    assert original['movement_target_count'] == 69 and original['movement_route'][0] == original['movement_route'][-1]
    assert math.dist(natural['movement_end'], original['movement_route'][0]) < 0.01
    assert natural['natural_wait_ticks'] == 600 and natural['server_ticks'] == 15612
    assert not natural['lost_water'] and not natural['spread_water'] and not natural['dropped_items']
    assert len(natural['doors']) == 8 and all(item['lever_found'] and item['opened'] and item['restored'] for item in natural['doors'])
    assert len(natural['containers']) == 32 and all(item['server_menu_opened'] and item['occupied_slots'] == 0 for item in natural['containers'])
    assert all(first['tick'] < second['tick'] and first['waypoint'] <= second['waypoint'] for first, second in zip(natural['movement_trace'], natural['movement_trace'][1:]))
    prior_data, prior_blocks = decode(root / 'runtime-assembly-r1/assembly-after.nbt')
    current_data, current_blocks = decode(root / 'independent-natural-world/assembly-after.nbt')
    assert current_data['size'] == prior_data['size'] and current_blocks == prior_blocks
    assert len(current_blocks) == 752850
    empty_checks = 0
    for room in [*ready['rooms'], ready['assembly']]:
        data, blocks = decode(Path(records[room['candidate']]['snapshot']))
        for block in blocks.values():
            entity = block['nbt'] or {}
            if 'Items' in entity:
                assert not entity['Items']
                empty_checks += 1
            assert not entity.get('LootTable')
    requirements = [
        {'requirement': 'Ten reference rooms11-20, complete NBT, real previews and reproducible recipes', 'result': 'PASS', 'evidence': [str(earlier / 'STATIC-REVIEW.md'), str(root / 'independent-qa/static-followup.json')]},
        {'requirement': 'Native state/support, body connectivity, empty containers and observed world stability', 'result': 'PASS', 'evidence': [str(root / 'independent-qa/independent-all-support.json'), str(root / 'independent-natural-world/assembly-runtime.json')]},
        {'requirement': 'Source-preserving merge,15 <=48-axis parts, full reconstruction and barrier enclosure', 'result': 'PASS', 'evidence': [str(root / 'independent-qa/native-rotation-payload-parts-barrier.json'), str(root / 'independent-corridor-qa/independent-routes.json')]},
        {'requirement': 'All functional containers/doors reachable and usable with actual eye rays, including hanging barrel native jump', 'result': 'PASS', 'evidence': [str(interaction / 'REVIEW.md'), str(interaction / 'independent-qa/interaction-inspection.json')]},
        {'requirement': 'Whole-map natural movement and return, necessary ladder use and current-hash ordinary external access', 'result': 'PASS', 'evidence': [str(root / 'independent-natural-world/assembly-runtime.json'), str(root / 'runtime-qa-r4/ladder-physics.json'), str(access / 'ACCESS-REVIEW.md')]},
    ]
    result = {'group': 'S2', 'verdict': 'ACCEPTED', 'accepted': True, 'reviewed_at': datetime.now().astimezone().isoformat(), 'source_sha256': ready['assembly']['sha256'], 'base_frozen_files_verified': len(records), 'interaction_supplement_files': 43, 'access_files': 56, 'independent_natural_ticks': natural['server_ticks'], 'natural_waypoints': 4215, 'whole_world_after_payload_equal_prior_classified_capture': True, 'empty_inventory_checks': empty_checks, 'requirements': requirements, 'unresolved_required_items': [], 'deployment': 'NOT_DEPLOYED; formal instance untouched', 'limits': ['Vanilla1.20.1 unrotated fixed overworld placement in pre-reserved empty area; no production install or combat pack', 'Server FakePlayer/ServerPlayer proof, not human client GUI or damage/survival playthrough', 'Ladder top landing and one hanging barrel native jump are scoped observed evidence; full ladder descent/jump arc not certified', 'Finite ticks with forced chunks do not certify all random ticks or indefinite lighting/growth', 'Configured public trigger entry and fixed return point, not a team or paid-access system']}
    (root / 'ACCEPTANCE.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps({key: value for key, value in result.items() if key not in {'requirements', 'limits'}}, ensure_ascii=False))


if __name__ == '__main__':
    main()
