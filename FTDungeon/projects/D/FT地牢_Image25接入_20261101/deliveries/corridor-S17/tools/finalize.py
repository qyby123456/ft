import hashlib
import json
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read(path):
    return json.loads(path.read_text(encoding='utf-8'))


def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    fingerprint = digest(ROOT / 'room.nbt')
    metadata = read(ROOT / 'room.json')
    if metadata['sha256'] != fingerprint:
        raise ValueError('Metadata hash mismatch')
    reports = {name: read(ROOT / relative) for name, relative in {'static': 'qa/static.json', 'forge': 'qa/forge.json', 'visual': 'qa/visual-review.json'}.items()}
    for name, report in reports.items():
        if report['status'] != 'PASS' or report['structure_sha256'] != fingerprint:
            raise ValueError('Missing/mismatched PASS evidence: ' + name)
    forge = reports['forge']
    if len(forge['rotations']) != 4 or forge['stabilization_ticks'] < 200 or forge['records_checked_after_ticks'] != 12480:
        raise ValueError('Incomplete Forge evidence')
    for rotation in forge['rotations']:
        if digest(ROOT / rotation['world_capture']) != rotation['world_capture_sha256']:
            raise ValueError('Changed Forge capture')
    renderer = read(ROOT / 'qa/render-manifest.json')
    if renderer['structure_sha256'] != fingerprint:
        raise ValueError('Stale render manifest')
    for filename in renderer['files']:
        with Image.open(ROOT / filename) as image:
            image.verify()
    for filename, expected in reports['visual']['reviewed_image_sha256'].items():
        if digest(ROOT / filename) != expected:
            raise ValueError('Changed reviewed image: ' + filename)
    reproduction = read(ROOT / 'qa/reproducibility.json')
    if reproduction['status'] != 'PASS' or reproduction['structure_sha256'] != fingerprint or reproduction['rebuilt_sha256'] != fingerprint or not reproduction['identical_bytes']:
        raise ValueError('Rebuild reproducibility evidence missing or stale')
    command = read(ROOT / 'qa/forge-command.json')
    log = Path(command['log']).read_text(encoding='utf-8', errors='replace')
    if 'CORRIDOR_S17_FORGE_PASS' not in log or read(ROOT / 'qa/forge-exit.json')['exit_code'] != 0:
        raise ValueError('Forge process has no successful matching terminal evidence')
    result = {
        'id': metadata['id'], 'status': 'PASS', 'structure_sha256': fingerprint,
        'reference_sha256': metadata['reference_sha256'],
        'updated_at': datetime.now(timezone(timedelta(hours=8))).isoformat(),
        'checks': {'static_nbt': 'PASS', 'four_direction_cutaways': 'PASS', 'four_direction_complete_exteriors': 'PASS', 'visual_support_route_roof': 'PASS', 'forge_registry_properties_template_roundtrip': 'PASS', 'forge_four_rotation_placement': 'PASS', 'forge_200_tick_state_survival_collision_stability': 'PASS', 'forge_world_capture_reload': 'PASS', 'negative_blocked_route_and_missing_chain_controls': 'PASS', 'builder_rebuild_byte_identical': 'PASS'},
        'counts': {'static_checks': len(reports['static']['checks']), 'explicit_records': 3120, 'static_reachable_foot_positions': reports['static']['reachable_foot_positions'], 'forge_rotations': 4, 'native_records_checked_after_ticks': 12480, 'stability_ticks': 200, 'cages': 4, 'wall_torches': 6},
        'evidence': {'static': 'qa/static.json', 'visual': 'qa/visual-review.json', 'forge': 'qa/forge.json', 'forge_command': 'qa/forge-command.json', 'forge_exit': 'qa/forge-exit.json', 'render_manifest': 'qa/render-manifest.json', 'reproducibility': 'qa/reproducibility.json', 'interior_four_views': 'qa/four-views/interior-four-views.png', 'exterior_four_views': 'qa/four-views/exterior-four-views.png', 'floorplan': 'qa/four-views/floorplan.png', 'forge_log': command['log']},
        'known_deviations': metadata['reference_adaptations'],
        'scope': 'Independent corridor candidate. Static and software visual evidence plus actual isolated Minecraft/Forge four-rotation placement and 200-tick revalidation.',
        'limitations': ['Not a full modpack client screenshot, multiplayer, gameplay or FPS test.', 'Receiver must independently recheck and allocate integration slot; no FT core integration, occupied-volume rejection, idempotence or player world deployment tested.'],
        'ready_for_receiver_review': True, 'receiver_recheck_required': True,
        'deployed': False, 'production_save_modified': False,
    }
    save(ROOT / 'qa.json', result)
    filenames = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'recipe.mjs', 'README.md', 'qa/static.json', 'qa/forge.json', 'qa/visual-review.json', 'qa/render-manifest.json', 'qa/four-views/interior-four-views.png', 'qa/four-views/exterior-four-views.png', 'qa/four-views/floorplan.png', 'reference/fingerprint.json']
    filenames.extend(['qa/reproducibility.json', 'qa/forge-command.json', 'qa/forge-exit.json'])
    filenames.extend(rotation['world_capture'] for rotation in forge['rotations'])
    save(ROOT / 'delivery-manifest.json', {'id': metadata['id'], 'status': 'PASS', 'structure_sha256': fingerprint, 'delivery_root': str(ROOT), 'files': {filename: {'sha256': digest(ROOT / filename), 'bytes': (ROOT / filename).stat().st_size} for filename in filenames}, 'deployment': 'NOT_DEPLOYED', 'receiver_review': 'REQUIRED'})
    print(json.dumps({'id': metadata['id'], 'status': 'PASS', 'sha256': fingerprint, 'delivery': str(ROOT)}))


if __name__ == '__main__':
    main()
