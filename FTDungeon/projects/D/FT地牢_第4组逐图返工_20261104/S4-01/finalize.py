from datetime import datetime
import hashlib
import json
from pathlib import Path
import shutil
from zoneinfo import ZoneInfo

ROOT = Path(__file__).resolve().parent


def main():
    native = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    geometry = json.loads((ROOT / 'geometry-check.json').read_text(encoding='utf-8'))
    preview = json.loads((ROOT / 'preview-check.json').read_text(encoding='utf-8'))
    repetition = json.loads((ROOT / 'repetition-check.json').read_text(encoding='utf-8'))
    stability = json.loads((ROOT / 'qa/vine-stability.json').read_text(encoding='utf-8'))
    assert native['status'] == geometry['status'] == preview['status'] == 'PASS'
    assert repetition['status'] == 'PASS_EXACT_DUPLICATE_CHECK'
    target = ROOT / 'incoming/S4-01'
    shutil.copy2(ROOT / 'preview-reverse.png', target / 'preview.png')
    assert (target / 'room.nbt').read_bytes() == (ROOT / 'nbt/s4-01-swamp-heart-rebuild-v1.nbt').read_bytes()
    expected = native['rooms'][0]['sha256']
    assert stability['status'] == 'PASS'
    assert stability['sha256'] == preview['sha256'] == expected
    assert expected == hashlib.sha256((target / 'room.nbt').read_bytes()).hexdigest()
    record = {
        'id': 'S4-01', 'reference_order': 1, 'group': 4,
        'recorded_at': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(),
        'status': 'SELF_CHECK_PASS_PENDING_INDEPENDENT_ACCEPTANCE',
        'minecraft_native_load_roundtrip': 'PASS', 'vanilla_registry_states': 'PASS',
        'revision': 1, 'independent_recheck': 'PENDING',
        'previous_rejected_sha256': '9c8461f6a47a9e66e30e72d9579400daa923de757103e6823a659275b660e353',
        'fix': 'Added persistent oak leaf at (17,20,25), supporting north-facing vine at (17,20,26).',
        'vine_canSurvive_and_neighbor_shape_fixed_point': stability,
        'dimensions': [49, 31, 49], 'connections_5x4_clearance': 'PASS',
        'dry_stair_compatible_routes_to_all_ports_and_dais': 'PASS',
        'water_containment': 'PASS', 'lily_support': 'PASS',
        'palette': 'PASS_VANILLA_ONLY', 'lodestone_actual_nbt_webgl': 'PASS',
        'reference_visual_self_check': 'Core landmarks and proportions checked; deviations documented in room.json and README.md; independent judgement still pending.',
        'available_local_repetition_scan': {'unique_nbt': repetition['compared_unique_nbt_count'], 'exact_geometry_duplicates': 0, 'highest_coarse_interior_jaccard': repetition['comparisons'][0]['interior_normalized_solid_jaccard_best_y_rotation']},
        'sha256': expected, 'old_group4_candidate': 'NOT_USED_NOT_MODIFIED',
        'gameplay': 'NOT_RUN', 'world_placement': 'NOT_RUN', 'deployment': 'NOT_RUN',
        'next_reference': 'S4-02-medium-room.png', 'next_started': False,
        'gate': 'Do not start S4-02 until S4-01 independent acceptance is registered. No claim that all 20 group images are complete.'
    }
    (ROOT / 'acceptance.json').write_text(json.dumps(record, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    checksums = []
    for path in sorted(ROOT.rglob('*')):
        if not path.is_file() or path.name == 'SHA256SUMS.txt' or '__pycache__' in path.parts:
            continue
        checksums.append(f'{hashlib.sha256(path.read_bytes()).hexdigest()}  {path.relative_to(ROOT).as_posix()}')
    (ROOT / 'SHA256SUMS.txt').write_text('\n'.join(checksums) + '\n', encoding='utf-8')
    for line in checksums:
        expected_hash, relative = line.split('  ', 1)
        assert hashlib.sha256((ROOT / relative).read_bytes()).hexdigest() == expected_hash
    print(json.dumps({'status': record['status'], 'sha256': expected, 'files_readback_verified': len(checksums)}, indent=2))


if __name__ == '__main__':
    main()
