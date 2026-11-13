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
    metadata = read('incoming/S4-09/room.json')
    native = read('qa/native-review.json')
    geometry = read('geometry-check.json')
    stability = read('qa/vine-stability.json')
    body = read('qa/body-routes.json')
    preview = read('preview-check.json')
    repetition = read('repetition-check.json')
    cutaway = read('cutaway-check.json')
    candidate = ROOT / 'incoming/S4-09/room.nbt'
    candidate_hash = digest(candidate)
    for record in [metadata, native['rooms'][0], geometry, stability, body, preview, repetition]:
        assert record['sha256'] == candidate_hash
    for record in [native, geometry, stability, body, preview]:
        assert record['status'] == 'PASS'
    assert all(record['reachable'] for record in body['targets'])
    assert repetition['status'] == 'PASS_EXACT_DUPLICATE_CHECK' and not repetition['errors']
    assert geometry['water_bottom_side_air_edges'] == 0 and geometry['stairs'] == 25
    assert geometry['single_empty_chest'] == [15, 9, 7] and geometry['conservative_lid_air_volume']['air_blocks'] == 18
    assert candidate.read_bytes() == (ROOT / 'nbt/s4-09-twin-warden-treasure-sanctum.nbt').read_bytes()
    assert cutaway['status'] == 'PASS_AIR_ONLY_DERIVATIVE' and cutaway['candidate_sha256'] == candidate_hash
    assert digest(ROOT / 'preview-only/upper-cutaway.nbt') == cutaway['derivative_sha256']
    assert len(preview['views']) == 8
    assert all(view['sha256'] == (cutaway['derivative_sha256'] if view['filename'] == 'preview-cutaway.png' else candidate_hash) for view in preview['views'])
    assert digest(Path(metadata['reference'])) == metadata['reference_sha256']
    previous = json.loads(Path(metadata['previous_acceptance']).read_text(encoding='utf-8'))
    assert previous['status'] == 'PASS'
    assert metadata['previous_sha256'] == 'e0d77c77057d8587096cd1e36b3ccff7a5b8cb01e33eef977ba24a928b116223'
    earlier = {}
    for room in ['S4-01', 'S4-02', 'S4-03', 'S4-04', 'S4-05', 'S4-06', 'S4-07', 'S4-08']:
        folder = ROOT.parent / room
        entries = (folder / 'SHA256SUMS.txt').read_text(encoding='utf-8').splitlines()
        for entry in entries:
            expected, relative = entry.split('  ', 1)
            assert expected == digest(folder / relative), (room, relative)
        earlier[room] = {'sha256': digest(folder / 'incoming' / room / 'room.nbt'), 'manifest_entries_verified': len(entries)}
    assert earlier['S4-08']['sha256'] == metadata['previous_sha256']
    shutil.copy2(ROOT / 'preview-exterior.png', ROOT / 'incoming/S4-09/preview.png')
    record = {
        'group': 4, 'image': 9, 'status': 'SELF_CHECK_PASS_PENDING_INDEPENDENT_ACCEPTANCE',
        'recorded_at': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(),
        'name': metadata['name'], 'size': metadata['size'], 'sha256': candidate_hash,
        'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'],
        'previous_independent_acceptance': metadata['previous_acceptance'],
        'accepted_earlier_images_unchanged': earlier,
        'geometry_authorship': 'New image-specific twin moss-stone guardians, single elevated empty chest, three dais stair rows, two forecourt stair rows, paired lateral arches, pottery shelves/table, wall banners and foreground lily pool/dock. Earlier geometry not copied; QA harnesses reused.',
        'native_palette_template_and_entities': native['status'],
        'ports_3x4x3_supported_clearance': 'PASS', 'port_interfaces': metadata['connections'],
        'body_route_targets': len(body['targets']), 'body_route_0_6x1_8_half_grid': body['status'],
        'reachable_half_grid_nodes': body['reachable_half_grid_nodes'],
        'neighbor_shape_and_fragile_survival': stability,
        'chest_guardians_pottery_pool_and_architectural_support': geometry,
        'actual_full_nbt_interior_exterior_and_cutaway': preview,
        'cutaway_derivative_rules': cutaway,
        'repetition_screen': {'compared_unique_nbt': repetition['compared_unique_nbt_count'], 'exact_geometry_duplicates': 0, 'highest_coarse_match': repetition['comparisons'][0], 'scope': repetition['scope'], 'interpretation': 'High coarse sealed-room occupancy similarity is not visual equivalence; independent image review still required.'},
        'deviations': metadata['deviations'],
        'world_placement_ticks_gameplay': 'NOT_RUN', 'integration': 'NOT_RUN', 'deployment': 'NOT_RUN',
        'independent_acceptance': 'PENDING', 'next_image_started': False,
        'gate': 'S4-10 and later NOT_AUTHORIZED; do not start or pre-generate.'
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



