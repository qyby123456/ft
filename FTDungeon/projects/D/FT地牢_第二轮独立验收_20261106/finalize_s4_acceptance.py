import hashlib
import json
import sys
from datetime import datetime
from pathlib import Path


root = Path(sys.argv[1]).resolve()
prior = root.parent / 'S4-submission-20261107-074242-062606'


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
assert len(old_files) == 633 and len(current_files) == 665
allowed_changes = {'ready-for-review.json', 'DESIGN-R2.md', 'REVIEW_QUEUE.md', 'progress.json', 'README.md', 'qa/worldtest/native-test/build.gradle'}
changed = []
for original in old_files.keys() & current_files.keys():
    if old_files[original]['sha256'] != current_files[original]['sha256']:
        relative = Path(current_files[original]['snapshot']).relative_to(root).as_posix()
        assert relative in allowed_changes, relative
        changed.append(relative)
assert len(changed) == 6
digest = 'b01fdaf300987e299d141f1963530ad2b1861be8fe4c17631cdc879f7c7415d8'
assert hashlib.sha256((root / 'assembly/s4-wetland-connected.nbt').read_bytes()).hexdigest() == digest
support = load(prior / 'independent-qa/independent-all-support.json')
assert len(support) == 11 and sum(item['nonair_checked'] for item in support) == 157430
assert all(not item['failures'] and not item['unsupported'] for item in support)
native = load(prior / 'independent-qa/independent-native-frozen.json')
assert len(native) == 11 and all(item['registry_properties'] == 'PASS' and item['template_roundtrip_block_count'] == 'PASS' for item in native)
for folder, count in (('independent-qa', 112), ('independent-posttick-qa', 168)):
    routes = load(prior / folder / 'independent-routes.json')
    targets = [target for item in routes for target in item['targets']]
    assert len(targets) == count and all(target['reachable'] and target['standing_clear'] for target in targets)
payload = load(prior / 'independent-qa/payload-entities-parts.json')
assert payload['sha256'] == digest and not payload['room_source_differences']
assert payload['translated_entities_exact'] == 2 and payload['parts_payload_and_entities_exact']
assert payload['part_count'] == 18 and payload['parts_volume'] == payload['full_volume'] == 667320
assert payload['original_targets'] == 56 and not payload['original_target_positions_missing_in_merged']
assert payload['union_barrier_enclosure']['barrier_cells'] == 35488
assert payload['union_barrier_enclosure']['all_room_volume_cells_enclosed'] == 123439
assert payload['union_barrier_enclosure']['all_merged_target_cells_enclosed'] == 56
reproduction = load(prior / 'independent-qa/reproduction.json')
assert reproduction['all_29_nbt_outputs_binary_equal'] and reproduction['room_outputs_copied_from_candidate'] is False
assert all(item['binary_equal'] for item in reproduction['rooms'] + reproduction['assembly_and_parts'])
classification = load(prior / 'independent-qa/world-r2-classification.json')
assert classification['source_sha256'] == digest and len(classification['phases']) == 3
assert [phase['air_to_vine'] for phase in classification['phases']] == [0, 8, 8]
assert all(phase['armor_stand_positions_and_equipment_preserved'] == 2 for phase in classification['phases'])
for phase in classification['phases']:
    assert phase['default_be_payloads'] == {'minecraft:skull': 12, 'minecraft:lectern': 3, 'minecraft:furnace': 6}
world = root / 'independent-access-gravity'
assert load(world / 'independent-result.json')['exit_code'] == 0
access = load(world / 'access-gravity-report.json')
assert access['candidate_sha256'] == digest
assert access['entry_return_passed'] and access['unauthorized_return_rejected'] and access['terminal_barriers_24_cells_unchanged']
assert len(access['access']) == 2 and all(item['command_permission'] == 0 for item in access['access'])
installation = load(root / 'access/datapack-tested/installation.json')
assert installation['candidate_sha256'] == digest and installation['boundary_modified'] is False
assert installation['dimension'] == 'minecraft:overworld'
for item in installation['files']:
    expected = root / 'access/datapack-tested' / item['path']
    actual = world / 'server/world/datapacks/s4-access' / item['path']
    assert hashlib.sha256(expected.read_bytes()).hexdigest() == item['sha256']
    assert actual.read_bytes() == expected.read_bytes()
assert len(installation['files']) == 8
assert load(world / 'server/world/datapacks/s4-access/data/minecraft/tags/functions/load.json')['values'] == ['fts4_access:load']
assert load(world / 'server/world/datapacks/s4-access/data/minecraft/tags/functions/tick.json')['values'] == ['fts4_access:tick']
source = (world / 'native-test/src/main/java/net/farmingtales/s4waterqa/AccessGravityTests.java').read_text(encoding='utf-8')
assert 'function fts4_access:load' not in source and 'function fts4_access:tick' not in source
assert 'setMaxUpStep' not in source and 'setNoGravity(true)' not in source
assert len(access['natural_travel']) == 2
natural = []
for walk in access['natural_travel']:
    assert walk['roundtrip'] and walk['label'] in ('S411Gravity', 'S419Gravity')
    samples = walk['samples']
    assert len(samples) == walk['frames'] - 1
    ticks = [item['game_time'] for item in samples]
    assert all(after == before + 1 for before, after in zip(ticks, ticks[1:]))
    assert all(item['input'][1] == 0 and item['no_gravity'] is False for item in samples)
    heights = [item['feet'][1] for item in samples]
    assert max(after - before for before, after in zip(heights, heights[1:])) <= 0.500001
    assert max(heights) - min(heights) >= (5 if walk['label'] == 'S411Gravity' else 4)
    assert abs(heights[-1] - heights[0]) < 0.09
    first_feet, last_feet = samples[0]['feet'], samples[-1]['feet']
    assert ((last_feet[0] - first_feet[0]) ** 2 + (last_feet[2] - first_feet[2]) ** 2) ** 0.5 < 0.14
    natural.append(dict(label=walk['label'], frames=walk['frames'], ticks=ticks[-1]-ticks[0], min_y=min(heights), max_y=max(heights), max_up_step=walk['max_up_step'], endpoint_height_error=abs(heights[-1]-heights[0]), endpoint_on_ground=samples[-1]['on_ground'], endpoint_scope='Original controller completion tolerance: horizontal<0.14, vertical<0.09; final completed frame is not sampled; no extra final-settled-standing claim'))
result = dict(group='S4', accepted=True, verdict='ACCEPTED', reviewed_at=datetime.now().astimezone().isoformat(), source_sha256=digest, frozen_files_verified=665, inherited_files_verified=633, source_and_previous_world_evidence_unchanged=True, harmless_changed_metadata=changed, native_support_checks=157430, static_routes=112, post_world_routes=168, reconstructed_nbt_outputs=29, natural_travel=natural, access_pack_files_actually_installed=8, unresolved_required_items=[], requirements=[
    dict(requirement='Ten original functional reference rooms, real preview and original recipes', result='PASS', evidence=[str(prior/'REVIEW.md'), str(prior/'independent-qa/reproduction.json')]),
    dict(requirement='Native state, support, original and post-world routes, source-preserving merge/18 parts/union barrier', result='PASS', evidence=[str(prior/'independent-qa/payload-entities-parts.json'), str(prior/'independent-qa/independent-all-support.json'), str(prior/'independent-posttick-qa/independent-routes.json')]),
    dict(requirement='Current-source world stability and actual functional ray/use evidence', result='PASS', evidence=[str(prior/'REVIEW.md'), str(prior/'independent-qa/world-r2-classification.json')]),
    dict(requirement='Installable controlled external access with actual isolated validation and reserved-space instructions', result='PASS', evidence=[str(root/'access/INSTALL.md'), str(world/'access-gravity-report.json')]),
    dict(requirement='Normal gravity Player.travel for necessary room11/19 raised routes and return', result='PASS', evidence=[str(world/'access-gravity-report.json')])
], limits=['No formal deployment; single unrotated overworld instance in reserved empty volume, structure installed before access pack', 'Previous 800-tick world and ray/use run were independently inspected producer evidence; new access and gravity tests actually parent-rerun', 'Planar target walks use collision Entity.move; new natural gravity proof covers required raised library/outlook routes, not human whole-map keyboard play', 'Engine maxUpStep1.0 left unchanged; actual observed rises at most0.5', '8 finite-tick vine growths and vanilla state/BE normalizations retained; no indefinite plant-growth, random-tick, client GUI, combat or rewards guarantee'])
(root / 'ACCEPTANCE.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps({key:value for key,value in result.items() if key not in ('requirements','limits','harmless_changed_metadata')}, ensure_ascii=False))
