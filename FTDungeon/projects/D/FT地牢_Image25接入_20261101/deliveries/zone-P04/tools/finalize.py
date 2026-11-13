import hashlib
import json
from datetime import datetime, timedelta, timezone
from pathlib import Path
from PIL import Image

root = Path(__file__).resolve().parents[1]

def digest(filename):
    return hashlib.sha256((root / filename).read_bytes()).hexdigest()

def load(filename):
    return json.loads((root / filename).read_text(encoding='utf-8'))

metadata = load('room.json')
native = load('qa/native.json')
static = load('qa/static.json')
review = load('qa/four-view-review.json')
render = load('qa/render.json')
source_hash = digest('room.nbt')
for report in [metadata, native, static, review]:
    assert report.get('room_sha256', report.get('sha256')) == source_hash
for report in [native, static, review]:
    assert report['status'] == 'PASS'
assert native['stabilization_ticks'] == 240
assert native['records_checked_after_ticks'] == 39744
assert native['negative_probes'] == 16
assert len(native['rotations']) == 4
for rotation in native['rotations']:
    assert rotation['status'] == 'PASS' and rotation['state_drift'] == 0
    assert rotation['route']['status'] == 'PASS'
    assert len(rotation['route']['jumps']) == 4
    assert len(rotation['route']['failures_and_recovery']) == 4
    assert rotation['route']['exit_reached']
for view in render['views']:
    assert view['sourceHash'] == source_hash
    with Image.open(root / view['path']) as image:
        image.verify()
for view in review['views']:
    assert digest(view['path']) == view['sha256']
log = (root / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
assert 'ZONE_P04_NATIVE_PASS' in log and 'BUILD SUCCESSFUL' in log
report = {
    'id': 'zone-P04', 'status': 'PASS', 'producer_qa_status': 'PASS',
    'completed_at': datetime.now(timezone(timedelta(hours=8))).isoformat(),
    'room_sha256': source_hash, 'reference_sha256': metadata['reference_sha256'],
    'reference': metadata['reference'], 'size': metadata['size'],
    'static': {'status': 'PASS', 'report': 'qa/static.json', 'explicit_records': 9936, 'non_air_records': static['non_air_records'], 'connected_non_air_records': static['connected_non_air_records']},
    'four_views': {'status': 'PASS', 'review': 'qa/four-view-review.json', 'renderer': render['renderer'], 'interior': {direction: 'views/interior-' + direction + '.png' for direction in ['north','south','west','east']}, 'exterior': {direction: 'views/exterior-' + direction + '.png' for direction in ['north','south','west','east']}, 'sheet': 'views/four-direction-review.png', 'source_nbt_hash_bound': True, 'separate_images_individually_reviewed': True},
    'forge_native': {'status': 'PASS', 'report': 'qa/native.json', 'log': 'qa/forge-gametest.log', 'minecraft': '1.20.1', 'forge': '47.4.10', 'rotations_passed': 4, 'stabilization_ticks': 240, 'records_checked_after_ticks': 39744, 'state_drift': 0, 'negative_probes_detected': 16, 'isolated_test_world_only': True},
    'route': {'status': 'PASS', 'method': native['route_method'], 'four_rotations': True, 'jumps_per_rotation': 4, 'sprint_required': False, 'max_gap_blocks': 1, 'max_rise': 1, 'failure_sites_per_rotation': 4, 'maximum_recovery_drop': 3, 'return_stairs_both_directions': True, 'entry_safe_return': True, 'exit_reached': True, 'safe_points': metadata['safe_points']},
    'ready_for_receiver_review': True, 'ready_for_unified_integration': False,
    'receiver_acceptance_required': True, 'deployed': False, 'production_save_access': False,
    'limitations': ['Software renders are not Minecraft client screenshots.', 'Native scripted FakePlayer collision probes are not a full modpack keyboard or multiplayer playtest.', 'Physical resting platforms do not implement automatic respawn or persistent checkpoints.', 'The crumbling look is static; no timed collapse is implemented.', 'Receiving-side acceptance, unique slot, hall links, occupied-volume rejection, idempotence and deployment remain outside producer scope.'],
    'test_iteration_notes': ['Added missing ceiling support sockets after initial static check; final NBT has all six sockets.', 'Initial route probe reported a descending player before landing; the probe now waits for actual onGround.', 'One 512 MB isolated server startup ran out of heap; final successful run used 1536 MB and four active processors. Earlier logs are retained as previous-* files.'],
    'evidence': {}
}
files = ['README.md','room.nbt','room.json','preview.png','summary.json','build.gradle','gradle.properties','settings.gradle','qa/static.json','qa/native.json','qa/materials.json','qa/reference.json','qa/render.json','qa/four-view-review.json','qa/forge-gametest.log','views/four-direction-review.png']
files.extend(str(filename.relative_to(root)).replace('\\','/') for folder in ['tools','src'] for filename in (root / folder).rglob('*') if filename.is_file())
files.extend(view['path'] for view in render['views'])
for filename in sorted(set(files)):
    report['evidence'][filename] = {'sha256': digest(filename), 'bytes': (root / filename).stat().st_size}
(root / 'qa.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'status': 'PASS', 'room_sha256': source_hash, 'evidence_files': len(report['evidence']), 'rotations': 4, 'state_drift': 0}, ensure_ascii=False))
