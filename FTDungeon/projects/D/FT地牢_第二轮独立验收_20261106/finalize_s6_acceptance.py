import gc
import hashlib
import json
import sys
from datetime import datetime
from pathlib import Path

from audit_s8_submission import decode


root = Path(sys.argv[1]).resolve()
prior = root.parent / 'S6-submission-20261107-085416-373660'
base = root.parent / 'S6-submission-20261107-065459-360990'
digest = '5b72d94c89871ebe9c262f92d6c00275ee0c8cfbf32343be96c9cd8e538f1336'


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


base_files, old_files, current_files = records(base), records(prior), records(root)
assert (len(base_files), len(old_files), len(current_files)) == (675,754,763)
assert not old_files.keys() - current_files.keys()
allowed_changes = {'qa/delivery-audit.json','progress.json','qa/revision-response.md','README.md','ready-for-review.json','finalize.mjs','status.mjs','finalize-review2.mjs','finalize-access.mjs'}
changed = []
for source in old_files.keys() & current_files.keys():
    if old_files[source]['sha256'] != current_files[source]['sha256']:
        relative = Path(current_files[source]['snapshot']).relative_to(root).as_posix()
        assert relative in allowed_changes, relative
        changed.append(relative)
assert len(changed) == 9
manifest = load(root / 'ready-for-review.json')
baseline = load(base / 'ready-for-review.json')
assert len(manifest['rooms']) == 10
for room, old in zip(manifest['rooms'], baseline['rooms']):
    assert room['id'] == old['id'] and room['sha256'] == old['sha256']
    assert current_files[room['candidate']]['sha256'] == base_files[room['candidate']]['sha256']
assert manifest['assembly']['sha256'] == baseline['assembly']['sha256'] == digest
support = load(base / 'independent-qa/independent-all-support.json')
assert len(support) == 11 and sum(item['nonair_checked'] for item in support) == 192287
assert all(not item['failures'] and not item['unsupported'] for item in support)
native = load(base / 'independent-qa/independent-native-frozen.json')
assert len(native) == 11 and all(item['registry_properties'] == 'PASS' and item['template_roundtrip_block_count'] == 'PASS' for item in native)
for directory in ('independent-qa','independent-posttick-qa'):
    targets = [target for item in load(base / directory / 'independent-routes.json') for target in item['targets']]
    assert len(targets) == 115 and all(target['reachable'] and target['standing_clear'] for target in targets)
payload = load(base / 'independent-qa/payload-entities-parts.json')
assert payload['sha256'] == digest and len(payload['room_source_differences']) == 12
assert payload['explicit_terminal_glass_cap_cells_verified'] == 12
assert payload['parts_payload_and_entities_exact'] and payload['part_count'] == 20
assert payload['merged_records'] == 252421 and payload['parts_volume'] == payload['full_volume'] == 1341360
assert payload['original_targets'] == 57 and not payload['original_target_positions_missing_in_merged']
enclosure = payload['union_barrier_enclosure']
assert enclosure['barrier_cells'] == 47553 and enclosure['all_room_volume_cells_enclosed'] == 195688 and enclosure['all_merged_target_cells_enclosed'] == 58
reproduction = load(root / 'independent-room-reproduction.json')
assert reproduction['status'] == 'PASS' and len(reproduction['results']) == 10
assert reproduction['copied_candidate_nbt_as_generation_input'] is False
for item in reproduction['results']:
    assert item['binary_equal'] and hashlib.sha256(Path(item['output']).read_bytes()).hexdigest() == item['sha256']
merged_reproduction = load(base / 'independent-qa/assembly-reproduction.json')
assert merged_reproduction['compressed_bytes_equal'] and merged_reproduction['source_sha256'] == merged_reproduction['reproduced_sha256'] == digest
runtime = load(base / 'independent-qa/runtime-capture-inspection.json')
assert len(runtime['fixtures']) == 11
assert all(item['server_ticks'] == 400 and not item['type_changes'] and not item['producer_dangerous'] and item['producer_external_fluid_cells'] == 0 for item in runtime['fixtures'])
source_data, blocks = decode(root / 'assembly/s6-volcanic-connected.nbt')
layout = load(root / 'assembly/layout.json')
air_samples = 0
for corridor in layout['corridors']:
    along_east = corridor['from'][0] != corridor['to'][0]
    for point in corridor['points']:
        for across in range(-1,2):
            for rise in range(4):
                position = (point[0]+(0 if along_east else across),4+rise,point[2]+(across if along_east else 0))
                assert blocks.get(position,{'state':{'Name':'minecraft:air'}})['state']['Name'] == 'minecraft:air', position
                air_samples += 1
assert air_samples == 2688
del source_data, blocks
gc.collect()
hooks = load(prior / 'independent-hooks-world-offline/independent-result.json')
assert hooks['source_sha256'] == digest and hooks['same_world_restart'] and hooks['all_packaged_data_including_minecraft_tags_equals_test_resources']
assert hooks['zip_sha256'] == 'fb3334aadeb07db3c48b643e3b9be63ad4f9598c864a1f9ddb53f7b1b97fb0bf'
archive = root / 'assembly/access/fts6-access-mc1.20.1.zip'
assert hashlib.sha256(archive.read_bytes()).hexdigest() == hooks['zip_sha256']
phases = hooks['initial_and_restart']
assert len(phases) == 2 and phases[0]['pid'] != phases[1]['pid']
assert all(item['status'] == 'PASS' and item['explicit_load_or_tick_calls'] == 0 and len(item['checks']) == 15 for item in phases)
assert phases[0]['natural_server_ticks_elapsed'] >= 150 and phases[1]['natural_server_ticks_elapsed'] >= 300
walk = load(root / 'independent-walk-inspection.json')
assert walk['status'] == 'PASS_SCOPED' and walk['source_sha256'] == digest
assert walk['frozen_files_verified'] == 763 and walk['original_targets_actually_arrived'] == 58
assert walk['functional_blocks'] == 36 and walk['actual_eye_ray_use_operations'] == 48
assert walk['containers'] == 23 and walk['levers'] == 13 and walk['maximum_eye_distance'] <= 4.5
assert all(walk[key] for key in ('native_ladder_climb_and_return','all_source_block_types_preserved','original_entity_fields_preserved','empty_containers','all_controls_and_doors_restored','capture_byte_equal_producer'))
assert walk['full_capture_cells'] == 1341360
scratch = Path(walk['parent_runtime'])
actual_report = load(scratch / 'assembly/access/walk-interaction-result.json')
assert load(root / 'independent-walk-result.json')['exit_code'] == 0 and actual_report['status'] == 'PASS'
assert hashlib.sha256((scratch / 'assembly/access/after-walk.nbt').read_bytes()).hexdigest() == actual_report['post_capture_sha256']
result = dict(group='S6', accepted=True, verdict='ACCEPTED', reviewed_at=datetime.now().astimezone().isoformat(), source_sha256=digest, frozen_files_verified=763, inherited_access_files_verified=754, inherited_static_files_verified=675, harmless_changed_metadata=changed, independent_room_recipe_outputs=10, inherited_independent_merged_reproduction=True, native_support_checks=192287, original_routes=115, post_world_routes=115, corridor_full_width_air_samples=air_samples, part_count=20, barrier_cells=47553, natural_movement_ticks=walk['natural_movement_ticks'], original_targets_actually_arrived=58, functional_blocks=36, eye_ray_use_operations=48, unresolved_required_items=[], requirements=[
    dict(requirement='Ten distinct reference rooms, real previews and reproducible recipes', result='PASS', evidence=[str(base/'REVIEW-IN-PROGRESS.md'),str(root/'independent-room-reproduction.json')]),
    dict(requirement='Native registry/support, original and post-world body routes, current world stability', result='PASS', evidence=[str(base/'independent-qa/independent-native-frozen.json'),str(base/'independent-qa/independent-all-support.json'),str(base/'independent-qa/runtime-capture-inspection.json')]),
    dict(requirement='Source preserving merge, full corridor clearance,20 parts and union barrier', result='PASS', evidence=[str(base/'independent-qa/payload-entities-parts.json'),str(base/'independent-qa/assembly-reproduction.json'),str(root/'ACCEPTANCE.json')]),
    dict(requirement='Actual installed external entry/return, natural hooks/reload and cross-process restart', result='PASS', evidence=[str(prior/'HOOKS-REVIEW.md'),str(prior/'independent-hooks-world-offline/independent-result.json')]),
    dict(requirement='Continuous actual natural travel, all58 targets, original ladder and all36 eye-ray/use targets', result='PASS', evidence=[str(root/'WALK-REVIEW.md'),str(root/'independent-walk-inspection.json'),str(scratch/'assembly/access/walk-interaction-result.json')])
], deployment='NOT_DEPLOYED; formal instance untouched', limits=['Fixed overworld unrotated reserved site, no terrain adaptation/multi-instance claim', 'Server simulated players, not a connected client GUI, combat, rewards or survival playthrough', 'Every20-tick movement trace plus actual operation stops; horizontal job braking preserves vertical velocity', 'Ladder returns to original library floor, not an exact initial-entry coordinate loop', 'World evidence is finite400-tick per fixture plus8466-tick merged movement; no indefinite growth/random tick coverage'])
output = root / 'ACCEPTANCE.json'
temporary = root / 'ACCEPTANCE.json.tmp'
temporary.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
temporary.replace(output)
print(json.dumps({key:value for key,value in result.items() if key not in ('requirements','limits','harmless_changed_metadata')}, ensure_ascii=False))
