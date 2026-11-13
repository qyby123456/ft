import hashlib
import io
import json
from pathlib import Path
import struct
import zlib
from datetime import datetime

import nbtlib

root = Path(__file__).resolve().parent
primary = root / 'integrationqa-r6'
optional = root / 'optionalqa-r2'
well = root / 'wellrayqa-r1'
jumps = root / 'wellqa-r1'
out = root / 'supplement-integration'
out.mkdir(exist_ok=True)

def read(path):
    return json.loads(path.read_text(encoding='utf-8'))

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

frozen = {}
def check_frozen(value):
    if isinstance(value, dict):
        if 'path' in value and 'sha256' in value:
            path = Path(value['path'])
            assert digest(path) == value['sha256'], f'Frozen drift: {path}'
            frozen[str(path)] = value['sha256']
        for child in value.values():
            check_frozen(child)
    elif isinstance(value, list):
        for child in value:
            check_frozen(child)

check_frozen(read(root / 'ready-for-review.json'))
base_count = len(frozen)
assert base_count == 213
check_frozen(read(root / 'runtime-ready-for-review.json'))
source_hash = digest(root / 'rooms/s3-merged/s3-merged.nbt')
assert source_hash == '933a5f44ccc2c84d00b7ec321080068e99989403de35e7ad7aa3e09bc9a569b7'

deployment = read(primary / 'deployment-report.json')
walk = read(primary / 'natural-travel.json')
ray = read(primary / 'ray-use-report.json')
extra_ray = read(well / 'well-inside-ray.json')
controls = read(optional / 'optional-controls-ray.json')
jump_probes = read(jumps / 'well-jump-trials.json')
for evidence in (deployment, walk, ray, extra_ray, controls, jump_probes):
    assert evidence['source_sha256'] == source_hash
assert deployment['seeded_solid_cells'] == 1572165
assert deployment['block_type_errors'] == deployment['air_clear_errors'] == deployment['declared_be_errors'] == 0
assert deployment['perimeter_barrier_cells'] == 117690
assert deployment['source_entities'] == deployment['nonplayer_entities'] == 0
assert all(deployment['functions_entry_exit'].values())
assert walk['returned_to_start'] and walk['target_count'] == len(walk['visited_targets']) == 41
assert walk['ticks'] == 27722 and walk['jump_inputs'] == 29
assembly = read(root / 'assembly.json')
declared = {target['label']: target['position'] for target in assembly['targets']}
assert {item['label'] for item in walk['visited_targets']} == set(declared)
translated = []
for module in assembly['modules']:
    design = read(root / 'rooms' / module['id'] / 'design.json')
    for target in design['targets']:
        position = [part + offset for part, offset in zip(target['position'], module['origin'])]
        label = module['id'] + ' ' + target['label']
        assert declared[label] == position
        translated.append({'label': label, 'merged_position': position})
assert len(translated) == 40
assert len(ray['interactions']) == 43 and all(item['passed'] for item in ray['interactions'])
assert len(ray['missing']) == 1 and ray['missing'][0]['position'] == [120, 6, 85]
assert extra_ray['standing_supported_and_clear'] and not extra_ray['natural_access_from_main_route_verified']
assert len(extra_ray['interactions']) == 1 and extra_ray['interactions'][0]['passed']
all_ray = ray['interactions'] + extra_ray['interactions']
assert len({tuple(item['position']) for item in all_ray}) == 44
for item in all_ray:
    for attempt in item['attempts']:
        assert attempt['first_hit_target'] and attempt['expected_behavior'] and attempt['reach'] <= 4.5
assert len(controls['results']) == 6 and all(item['passed'] for item in controls['results']) and not controls['missing']
assert len(jump_probes['trials']) == 90 and not any(item['barrel_menu_opened'] or item['landed_on_rim'] for item in jump_probes['trials'])
assert read(primary / 'exit.json')['exit_code'] == 1
assert 'Targets / eye ray interactions incomplete' in (primary / 'tests.xml').read_text(encoding='utf-8')
for folder in (optional, well, jumps):
    assert read(folder / 'exit.json')['exit_code'] == 0

def saved_block(position):
    east, up, south = position
    chunk_east, chunk_south = east // 16, south // 16
    path = primary / f'server/world/region/r.{chunk_east // 32}.{chunk_south // 32}.mca'
    with path.open('rb') as region:
        region.seek(4 * ((chunk_east % 32) + (chunk_south % 32) * 32))
        location = int.from_bytes(region.read(4), 'big')
        assert location >> 8
        region.seek((location >> 8) * 4096)
        length = struct.unpack('>I', region.read(4))[0]
        codec = region.read(1)[0]
        payload = region.read(length - 1)
    assert codec == 2
    chunk = nbtlib.File.parse(io.BytesIO(zlib.decompress(payload)))
    section = next(section for section in chunk['sections'] if int(section['Y']) == up // 16)
    block_states = section['block_states']
    palette = block_states['palette']
    if len(palette) == 1:
        return str(palette[0]['Name'])
    bits = max(4, (len(palette) - 1).bit_length())
    per_long = 64 // bits
    index = (up % 16) * 256 + (south % 16) * 16 + east % 16
    word = int(block_states['data'][index // per_long]) & ((1 << 64) - 1)
    palette_index = (word >> ((index % per_long) * bits)) & ((1 << bits) - 1)
    return str(palette[palette_index]['Name'])

endpoint_blocks = []
for foot in ((-8, 103, 31), (10, 103, 31), (135, 109, 157)):
    support = (foot[0], foot[1] - 1, foot[2])
    support_state = saved_block(support)
    body_states = [saved_block(foot), saved_block((foot[0], foot[1] + 1, foot[2]))]
    assert support_state not in ('minecraft:air', 'minecraft:cave_air')
    assert all(state in ('minecraft:air', 'minecraft:cave_air') for state in body_states)
    endpoint_blocks.append({'foot': foot, 'support': support_state, 'body': body_states})

pack_checks = []
for folder in (primary, optional, well, jumps):
    for path in (root / 'deployment/datapack').rglob('*'):
        if not path.is_file():
            continue
        relative = path.relative_to(root / 'deployment/datapack')
        actual = folder / 'server/world/datapacks/fts3' / relative
        assert digest(actual) == digest(path)
        pack_checks.append({'run': folder.name, 'relative': str(relative), 'sha256': digest(path)})

report = {
    'status': 'SUBSETS_EXECUTED_WELL_NATURAL_ACCESS_GAP_OPEN',
    'independent_acceptance': 'PENDING',
    'timestamp': datetime.now().astimezone().isoformat(),
    'source_sha256': source_hash,
    'source_and_frozen_evidence_unchanged': True,
    'frozen_base_unique_files_verified': base_count,
    'frozen_base_plus_prior_runtime_unique_files_verified': len(frozen),
    'complete_existing_datapack_install': 'PASS',
    'full_volume_seeded_and_cleared': 1572165,
    'parts_installed': 25,
    'source_nonplayer_entities': 0,
    'installed_nonplayer_entities_before_actors': 0,
    'declared_block_entities_preserved': len(deployment['block_entities']),
    'barrier_cells': 117690,
    'documented_named_player_functions': deployment['functions_entry_exit'],
    'saved_endpoint_support_and_clearance': endpoint_blocks,
    'natural_main_route': {'status': 'PASS', 'original_single_room_targets_preserved_in_merge': 40, 'merged_targets_reached': 41, 'ticks': walk['ticks'], 'vanilla_jump_inputs': walk['jump_inputs'], 'returned_to_arrival': True, 'mid_route_teleports': 0, 'lift_entity_move_calls': 0, 'trace_samples': len(walk['trace'])},
    'native_eye_use': {'original_target_blocks': 44, 'naturally_reached_standing_targets': 43, 'independent_inside_well_standing_target': 1, 'actual_use_attempts': sum(len(item['attempts']) for item in all_ray), 'native_eye_use_passed_targets': 44, 'all_naturally_approachable': False, 'locked_targets': 3, 'lock_checks': 9},
    'optional_controls_layer': {'separate_install_function_executed': True, 'levers': 6, 'native_eye_use_checks': 12, 'source_has_installed_controls': False, 'independent_station_trials_reposition_between_tests': True},
    'well_access_gap': {'merged_local_barrel': [120, 6, 85], 'room16_local_barrel': [14, 5, 14], 'independent_inside_feet': [120.5, 104.0, 85.5], 'native_use_from_inside_passed': True, 'reachable_standing_ray_search_found': False, 'bridge_jump_trials': 90, 'trial_rim_landings': 0, 'trial_menu_openings': 0, 'maximum_trial_feet_y': max(item['highest_y'] for item in jump_probes['trials']), 'rim_top_y': 105.5, 'scope': 'Finite probes and planner limitation; no claim that every conceivable maneuver is impossible. Access from normal main route remains unverified. No source edits or substitute target deletion.'},
    'primary_test_exit': {'code': 1, 'expected_reason': 'All 44 targets were required to have naturally reachable standing rays. The suspended barrel has no such demonstrated approach; this failure is retained, not renamed PASS.'},
    'pack_copies': pack_checks,
    'original_targets_translated_without_deletion': translated,
    'limitations': ['No actual remote client/network rendering; named ServerPlayer is registered via PlayerList using EmbeddedChannel and suppressed outbound packets', 'Natural traversal uses survival FakePlayer Player.travel, vanilla gravity/collision and vanilla jumpFromGround, not keyboard input, hunger or combat certification', 'Ray uses ServerPlayerGameMode.useItemOn from actual eye pose, OUTLINE first hit and <=4.5-block reach', 'Well inside fixture begins inside the basin and does not close its natural approach gap', 'Dirty-volume installation fixture preloads chunks to seed all cells; this is not a cold/unloaded chunk startup certification', 'Original empty lecterns correctly produce no book menu; iron doors reject hand toggle; blue-ice well remains non-water and hoist static', 'Prior 1200-tick and ladder evidence was left frozen and not re-certified in this supplement'],
}
(out / 'integration-report.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
files = [root / 'seal-integration.py', out / 'integration-report.json', out / 'REVIEW.md']
for folder in (primary, optional, well, jumps):
    for pattern in ('*.json', '*.log', '*.nbt', '*.xml', '*.ps1'):
        files.extend(path for path in folder.glob(pattern) if path.is_file())
    files.extend(path for path in folder.glob('native-test/*') if path.is_file())
    files.extend(path for path in (folder / 'native-test/src').rglob('*') if path.is_file())
    files.append(folder / 'server/server.properties')
    files.extend(path for path in (folder / 'server/world/datapacks/fts3').rglob('*') if path.is_file())
    files.extend(path for path in (folder / 'server/crash-reports').glob('*') if path.is_file())
manifest = {'status': report['status'], 'source_sha256': source_hash, 'independent_acceptance': 'PENDING', 'files': [{'path': str(path), 'sha256': digest(path), 'bytes': path.stat().st_size} for path in sorted(set(files))]}
(out / 'manifest.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps({key: report[key] for key in ('status', 'source_sha256', 'frozen_base_unique_files_verified', 'complete_existing_datapack_install', 'natural_main_route', 'native_eye_use', 'well_access_gap')}, ensure_ascii=False, indent=2))
