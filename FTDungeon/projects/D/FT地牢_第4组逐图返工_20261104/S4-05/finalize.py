from datetime import datetime
import hashlib
import json
from pathlib import Path
import shutil
from zoneinfo import ZoneInfo

ROOT = Path(__file__).resolve().parent


def read(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    metadata = read('incoming/S4-05/room.json')
    native = read('qa/native-review.json')
    geometry = read('geometry-check.json')
    stability = read('qa/vine-stability.json')
    body = read('qa/body-routes.json')
    preview = read('preview-check.json')
    repetition = read('repetition-check.json')
    cutaway = read('cutaway-check.json')
    candidate = ROOT / 'incoming/S4-05/room.nbt'
    candidate_hash = digest(candidate)
    for record in [metadata, native['rooms'][0], geometry, stability, body, preview, repetition]:
        assert record['sha256'] == candidate_hash
    for record in [native, geometry, stability, body, preview]:
        assert record['status'] == 'PASS'
    assert all(record['reachable'] for record in body['targets'])
    assert repetition['status'] == 'PASS_EXACT_DUPLICATE_CHECK' and not repetition['errors']
    assert geometry['water_dominant'] and geometry['complete_three_layer_roof_columns'] == 585
    assert candidate.read_bytes() == (ROOT / 'nbt/s4-05-flooded-timber-corridor.nbt').read_bytes()
    assert cutaway['status'] == 'PASS_AIR_ONLY_DERIVATIVE' and cutaway['candidate_sha256'] == candidate_hash
    assert digest(ROOT / 'preview-only/roof-west-cutaway.nbt') == cutaway['derivative_sha256']
    assert len(preview['views']) == 5
    assert all(view['sha256'] == (cutaway['derivative_sha256'] if view['filename'] == 'preview-cutaway.png' else candidate_hash) for view in preview['views'])
    assert digest(Path(metadata['reference'])) == metadata['reference_sha256']
    previous = json.loads(Path(metadata['previous_acceptance']).read_text(encoding='utf-8'))
    assert previous['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION' and previous['sha256'] == metadata['previous_sha256']
    earlier = {}
    for room in ['S4-01', 'S4-02', 'S4-03', 'S4-04']:
        folder = ROOT.parent / room
        entries = (folder / 'SHA256SUMS.txt').read_text(encoding='utf-8').splitlines()
        for entry in entries:
            expected, relative = entry.split('  ', 1)
            assert expected == digest(folder / relative), (room, relative)
        earlier[room] = {'sha256': digest(folder / 'incoming' / room / 'room.nbt'), 'manifest_entries_verified': len(entries)}
    assert earlier['S4-04']['sha256'] == previous['sha256']
    shutil.copy2(ROOT / 'preview-interior-south.png', ROOT / 'incoming/S4-05/preview.png')
    record = {
        'group': 4, 'image': 5, 'status': 'SELF_CHECK_PASS_PENDING_INDEPENDENT_ACCEPTANCE',
        'recorded_at': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(),
        'name': metadata['name'], 'size': metadata['size'], 'sha256': candidate_hash,
        'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'],
        'previous_independent_acceptance': metadata['previous_acceptance'],
        'accepted_earlier_images_unchanged': earlier,
        'geometry_authorship': 'New image-specific enclosed longitudinal flooded corridor, five paired timber frames, ten wall-bracket lamps, full masonry ceiling, hanging cave vines and shallow offset stone route. Earlier geometry not copied; QA harnesses reused.',
        'native_palette_template_and_entities': native['status'],
        'ports_3x4x3_supported_clearance': 'PASS', 'port_interfaces': metadata['connections'],
        'body_route_targets': len(body['targets']), 'body_route_0_6x1_8_half_grid': body['status'],
        'reachable_half_grid_nodes': body['reachable_half_grid_nodes'],
        'neighbor_shape_and_fragile_survival': stability,
        'water_roof_and_architectural_support': geometry,
        'actual_full_nbt_interior_exterior_and_cutaway': preview,
        'cutaway_derivative_rules': cutaway,
        'repetition_screen': {'compared_unique_nbt': repetition['compared_unique_nbt_count'], 'exact_geometry_duplicates': 0, 'highest_coarse_match': repetition['comparisons'][0], 'scope': repetition['scope'], 'interpretation': 'High coarse sealed-room occupancy similarity is not visual equivalence; independent image review still required.'},
        'deviations': metadata['deviations'],
        'world_placement_ticks_gameplay': 'NOT_RUN', 'integration': 'NOT_RUN', 'deployment': 'NOT_RUN',
        'independent_acceptance': 'PENDING', 'next_image_started': False,
        'gate': 'S4-06 and later NOT_AUTHORIZED; do not start or pre-generate.'
    }
    (ROOT / 'acceptance.json').write_text(json.dumps(record, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    entries = []
    for path in sorted(ROOT.rglob('*')):
        if path.is_file() and path.name != 'SHA256SUMS.txt' and '__pycache__' not in path.parts:
            entries.append(f'{digest(path)}  {path.relative_to(ROOT).as_posix()}')
    (ROOT / 'SHA256SUMS.txt').write_text('\n'.join(entries) + '\n', encoding='utf-8')
    for entry in entries:
        expected, relative = entry.split('  ', 1)
        assert expected == digest(ROOT / relative)
    print(json.dumps({'status': record['status'], 'sha256': candidate_hash, 'manifest_files_verified': len(entries), 'earlier_accepted_images_verified': earlier}, indent=2))


if __name__ == '__main__':
    main()
