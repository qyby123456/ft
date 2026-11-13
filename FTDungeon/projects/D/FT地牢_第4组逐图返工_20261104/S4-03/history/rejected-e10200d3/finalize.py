from datetime import datetime
import hashlib
import json
from pathlib import Path
import shutil
from zoneinfo import ZoneInfo

ROOT = Path(__file__).resolve().parent


def read(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def main():
    metadata = read('incoming/S4-03/room.json')
    native = read('qa/native-review.json')
    geometry = read('geometry-check.json')
    stability = read('qa/vine-stability.json')
    body = read('qa/body-routes.json')
    preview = read('preview-check.json')
    repetition = read('repetition-check.json')
    candidate = ROOT / 'incoming/S4-03/room.nbt'
    digest = hashlib.sha256(candidate.read_bytes()).hexdigest()
    for record in [metadata, native['rooms'][0], geometry, stability, body, preview]:
        assert record['sha256'] == digest
    for record in [native, geometry, stability, body, preview]:
        assert record['status'] == 'PASS'
    assert repetition['status'] == 'PASS_EXACT_DUPLICATE_CHECK'
    assert candidate.read_bytes() == (ROOT / 'nbt/s4-03-rootbound-cache.nbt').read_bytes()
    assert geometry['exposed_water_columns'] / geometry['basin_columns'] > 0.5
    previous_acceptance = json.loads(Path(metadata['previous_acceptance']).read_text(encoding='utf-8'))
    assert previous_acceptance['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    assert previous_acceptance['next_allowed'] == 'S4-03-small-room.png only'
    previous_candidate = ROOT.parent / 'S4-02/incoming/S4-02/room.nbt'
    assert hashlib.sha256(previous_candidate.read_bytes()).hexdigest() == previous_acceptance['nbt_sha256']
    shutil.copy2(ROOT / 'preview-reverse.png', ROOT / 'incoming/S4-03/preview.png')
    record = {
        'group': 4, 'image': 3, 'status': 'SELF_CHECK_PASS_PENDING_INDEPENDENT_ACCEPTANCE',
        'recorded_at': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(),
        'name': metadata['name'], 'size': metadata['size'], 'sha256': digest,
        'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'],
        'previous_independent_acceptance': metadata['previous_acceptance'], 'previous_candidate_unchanged': True,
        'geometry_authorship': 'New image-specific chest island, offset north/east portals, recessed shelves and roots; no prior room geometry read or copied. QA harness reuse only.',
        'native_palette_template_and_chest_entity': 'PASS', 'empty_chests': 1,
        'port_interfaces': metadata['connections'], 'ports_3x4x3_supported_clearance': 'PASS',
        'body_route_targets': len(body['targets']), 'body_route_0_6x1_8_half_grid': body['status'],
        'reachable_half_grid_nodes': body['reachable_half_grid_nodes'],
        'neighbor_shape_and_fragile_survival': stability,
        'water_and_support_boundary': geometry,
        'actual_nbt_lodestone_three_views': preview['status'],
        'repetition_screen': {'compared_unique_nbt': repetition['compared_unique_nbt_count'], 'exact_geometry_duplicates': 0, 'highest_coarse_match': repetition['comparisons'][0], 'scope': repetition['scope']},
        'world_placement_ticks_gameplay': 'NOT_RUN', 'integration': 'NOT_RUN', 'deployment': 'NOT_RUN',
        'independent_acceptance': 'PENDING', 'next_image_started': False,
        'gate': 'S4-04 and later NOT_AUTHORIZED; do not start or pre-generate.'
    }
    (ROOT / 'acceptance.json').write_text(json.dumps(record, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    entries = []
    for path in sorted(ROOT.rglob('*')):
        if path.is_file() and path.name != 'SHA256SUMS.txt' and '__pycache__' not in path.parts:
            entries.append(f'{hashlib.sha256(path.read_bytes()).hexdigest()}  {path.relative_to(ROOT).as_posix()}')
    (ROOT / 'SHA256SUMS.txt').write_text('\n'.join(entries) + '\n', encoding='utf-8')
    for entry in entries:
        expected, relative = entry.split('  ', 1)
        assert expected == hashlib.sha256((ROOT / relative).read_bytes()).hexdigest()
    print(json.dumps({'status': record['status'], 'sha256': digest, 'files_readback_verified': len(entries)}, indent=2))


if __name__ == '__main__':
    main()
