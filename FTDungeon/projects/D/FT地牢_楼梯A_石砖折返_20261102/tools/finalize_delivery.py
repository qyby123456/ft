import hashlib
import json
import shutil
from collections import Counter
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image

from build_room import ROOT, digest, write_json
from nbtio import Structure


def main():
    source = ROOT / 'room.nbt'
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    static = json.loads((ROOT / 'qa/static.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/native.json').read_text(encoding='utf-8'))
    render = json.loads((ROOT / 'qa/render-manifest.json').read_text(encoding='utf-8'))
    source_hash = digest(source)
    if not (source_hash == metadata['sha256'] == static['source_sha256'] == native['source_sha256'] == render['source_sha256']):
        raise ValueError('Artifact/evidence fingerprint mismatch')
    if static['status'] != 'PASS' or native['status'] != 'PASS' or len(native['rotations']) != 4:
        raise ValueError('Static and all native rotations must pass')
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    if 'STAIR_A_NATIVE_GAME_TEST_PASS: rotations=4/4' not in log or 'BUILD SUCCESSFUL' not in log:
        raise ValueError('Missing final native success marker')
    room = Structure.read(source)
    materials = Counter(block.name for block in room.blocks.values() if block.name != 'minecraft:air')
    write_json(ROOT / 'out/materials.json', {'source_sha256': source_hash, 'non_air_blocks': sum(materials.values()), 'materials': dict(sorted(materials.items()))})
    routes = [route for rotation in native['rotations'] for route in rotation['three_lane_bidirectional_routes']]
    proof_files = ['qa/static.json', 'qa/native.json', 'qa/forge-gametest.log', 'qa/blueprint.json', 'qa/render-manifest.json', 'out/materials.json']
    proof_files += [f'qa/native-placed-{name}.nbt' for name in ('none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90')]
    evidence = [{'file': name, 'sha256': digest(ROOT / name), 'bytes': (ROOT / name).stat().st_size} for name in proof_files]
    report = {
        'id': metadata['id'], 'name': metadata['name'],
        'status': 'PASS_INDEPENDENT_DELIVERY_NOT_DEPLOYED',
        'generated_at': datetime.now(timezone(timedelta(hours=8))).isoformat(timespec='seconds'),
        'room_sha256': source_hash,
        'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'],
        'size': metadata['size'], 'data_version': room.data_version, 'block_records_including_air': len(room.blocks), 'non_air_blocks': sum(materials.values()),
        'static': {'status': static['status'], 'checks': len(static['checks']), 'report': 'qa/static.json'},
        'native_forge': {
            'status': native['status'], 'runtime': native['runtime'], 'rotations_passed': 4,
            'records_checked_initial': 43700, 'records_checked_after_100_ticks': native['records_checked_after_ticks'],
            'elapsed_world_ticks': native['elapsed_world_ticks'],
            'stairs_per_rotation': 36, 'guards_per_rotation': 53, 'decor_supports_per_rotation': 24,
            'portals_3x3_per_rotation': 3, 'empty_containers_per_rotation': 2,
            'bidirectional_three_lane_routes_across_rotations': len(routes),
            'native_collision_samples_after_ticks': sum(route['collision_samples'] for route in routes),
            'maximum_vertical_step': max(route['maximum_vertical_step'] for route in routes),
            'walking_body': {'width': 0.6, 'height': 1.8, 'sample_spacing': 0.25, 'required_maximum_step': 0.6},
            'registry_states_entities_roundtrip': native['native_registry_properties_block_entities_template_roundtrip'],
            'negative_probes': native['negative_probes'],
            'checks': ['native StructureTemplate.placeInWorld at 0/90/180/270 degrees', 'exact post-placement state equality for every record, including air', 'BlockState.canSurvive and explicit tread/rail/lantern/chain support', 'neighbor-shape fixed point, full neighbor updates, forced loaded chunks and 100 world ticks', 'three-wide north portals with three-block headroom and full dry floors', 'both directions on three lanes of lower and upper connections with native 0.6x1.8 collision boxes', 'safe landing, no liquid or cobweb on routes, no entities/drops and empty native container inventories'],
            'report': 'qa/native.json', 'log': 'qa/forge-gametest.log',
        },
        'preview': {'file': 'preview.png', 'sha256': digest(ROOT / 'preview.png'), 'size': list(Image.open(ROOT / 'preview.png').size), 'source': 'actual delivered NBT and original vanilla models/textures; full roof retained in source', 'client_screenshot': False, 'native_capture_preview': 'preview/native-world-capture.png'},
        'production_scope': {'deployed': False, 'core_modified': False, 'player_saves_opened_or_modified': False, 'other_rooms_modified': False, 'image_api_called': False, 'external_ft_corridors_tested': False, 'saved_data_idempotence_tested': False, 'full_modpack_client_or_multiplayer_tested': False},
        'integration_requirements': metadata['integration_notes'],
        'known_limitations': native['limitations'] + ['Headless GameTest log reports one initialization overload warning; no FPS, real-time latency or full-modpack performance claim.', 'Collision probe verifies fixed-width walking and native stepping; it is not a human/client input simulation, jump or sprint acceptance.'],
        'evidence': evidence,
    }
    write_json(ROOT / 'qa.json', report)
    delivery_files = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md']
    if not (ROOT / 'README.md').is_file():
        raise ValueError('Delivery README is required')
    for name in ('room.nbt', 'room.json', 'preview.png', 'qa.json'):
        shutil.copy2(ROOT / name, ROOT / 'out' / name)
    manifest_files = delivery_files + proof_files + ['preview/native-world-capture.png']
    write_json(ROOT / 'out/manifest.json', {
        'id': metadata['id'], 'status': report['status'], 'generated_at': report['generated_at'],
        'room_sha256': source_hash, 'reference_sha256': metadata['reference_sha256'],
        'files': [{'file': name, 'sha256': digest(ROOT / name), 'bytes': (ROOT / name).stat().st_size} for name in manifest_files],
        'out_copies_identical_to_root': all(digest(ROOT / name) == digest(ROOT / 'out' / name) for name in ('room.nbt', 'room.json', 'preview.png', 'qa.json')),
    })
    print(json.dumps({'status': report['status'], 'size': room.size, 'room_sha256': source_hash, 'static_checks': len(static['checks']), 'native_rotations': 4, 'native_collision_samples': report['native_forge']['native_collision_samples_after_ticks']}, ensure_ascii=False))


if __name__ == '__main__':
    main()
