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
    metadata = read('incoming/S4-02/room.json')
    native = read('qa/native-review.json')
    geometry = read('geometry-check.json')
    stability = read('qa/vine-stability.json')
    body = read('qa/body-routes.json')
    preview = read('preview-check.json')
    repetition = read('repetition-check.json')
    candidate = ROOT / 'incoming/S4-02/room.nbt'
    digest = hashlib.sha256(candidate.read_bytes()).hexdigest()
    for record in [metadata, geometry, stability, body, preview, native['rooms'][0]]:
        assert record['sha256'] == digest
    for record in [native, geometry, stability, body, preview]:
        assert record['status'] == 'PASS'
    assert repetition['status'] == 'PASS_EXACT_DUPLICATE_CHECK'
    assert candidate.read_bytes() == (ROOT / 'nbt/s4-02-drowned-study.nbt').read_bytes()
    shutil.copy2(ROOT / 'preview-reverse.png', ROOT / 'incoming/S4-02/preview.png')
    accepted = json.loads(Path(metadata['previous_acceptance']).read_text(encoding='utf-8'))
    assert accepted['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    record = {
        'group': 4, 'image': 2, 'status': 'SELF_CHECK_PASS_PENDING_INDEPENDENT_ACCEPTANCE',
        'recorded_at': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(),
        'name': metadata['name'], 'size': metadata['size'], 'sha256': digest,
        'previous_image_independent_acceptance': metadata['previous_acceptance'],
        'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'],
        'new_geometry': 'Independent drowned study recipe; no S4-01 tree/dais geometry imported. QA harness reuse only.',
        'native_template_roundtrip': native['status'], 'loaded_empty_decorative_chests': 2,
        'port_interfaces': metadata['connections'], 'port_5x4x4_support_clearance': 'PASS',
        'body_route_targets': len(body['targets']), 'body_route_0_6x1_8_half_grid': body['status'],
        'body_route_half_grid_nodes': body['reachable_half_grid_nodes'],
        'fragile_survival_and_six_direction_vine_updates': stability,
        'water_lily_furniture_support_boundaries': geometry['status'],
        'actual_nbt_lodestone_three_views': preview['status'],
        'repetition_unique_nbt': repetition['compared_unique_nbt_count'],
        'repetition_exact_geometry_duplicates': 0,
        'highest_coarse_interior_similarity': repetition['comparisons'][0],
        'repetition_scope': 'Coarse screen includes unrelated preview cuts; not a universal uniqueness proof. Independent visual review required.',
        'world_placement_ticks_gameplay': 'NOT_RUN', 'integration': 'NOT_RUN', 'deployment': 'NOT_RUN',
        'independent_acceptance': 'PENDING', 'next_image_started': False,
        'gate': 'S4-03 and later NOT_AUTHORIZED; do not start or pre-generate.'
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
