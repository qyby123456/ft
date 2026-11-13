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
    metadata = read('incoming/S4-10/room.json')
    native = read('qa/native-review.json')
    geometry = read('geometry-check.json')
    stability = read('qa/vine-stability.json')
    body = read('qa/body-routes.json')
    preview = read('preview-check.json')
    repetition = read('repetition-check.json')
    cutaway = read('cutaway-check.json')
    split = read('split-check.json')
    fragments = read('qa/fragment-native.json')
    candidate = ROOT / 'incoming/S4-10/room.nbt'
    candidate_hash = digest(candidate)
    for record in [metadata, native['rooms'][0], geometry, stability, body, preview, repetition]:
        assert record['sha256'] == candidate_hash
    for record in [native, geometry, stability, body, preview, fragments]:
        assert record['status'] == 'PASS'
    assert all(record['reachable'] and record['source_to_target_half_grid_path'] for record in body['targets'])
    assert len(body['targets']) == geometry['body_route_targets'] == 42
    assert geometry['major_region_count'] == 7 and len(set(tuple(size) for size in geometry['region_sizes_xz'].values())) == 7
    assert geometry['water_bottom_side_air_edges'] == [] and geometry['conservative_lid_air_blocks'] == 18
    assert stability['changed_states'] == 0 and stability['failures'] == []
    assert candidate.read_bytes() == (ROOT / 'nbt/s4-10-swamp-sanctum-complex.nbt').read_bytes()
    assert split['status'] == 'PASS_EXACT_DENSE_REASSEMBLY' and split['source_sha256'] == candidate_hash
    assert split['all_voxels_including_air_state_properties_and_entity_payloads_equal_source']
    assert fragments['source_sha256'] == candidate_hash and len(fragments['parts']) == len(split['parts']) == 4
    for part, native_part in zip(split['parts'], fragments['parts']):
        assert max(part['size']) <= 48 and digest(ROOT / part['path']) == part['sha256'] == native_part['sha256']
        assert native_part['status'] == 'PASS'
    assert cutaway['status'] == 'PASS_AIR_ONLY_DERIVATIVE' and cutaway['candidate_sha256'] == candidate_hash
    assert digest(ROOT / 'preview-only/upper-cutaway.nbt') == cutaway['derivative_sha256']
    assert len(preview['views']) == 13
    for view in preview['views']:
        assert view['sha256'] == (cutaway['derivative_sha256'] if view['filename'] == 'preview-cutaway.png' else candidate_hash)
        assert (ROOT / view['filename']).stat().st_size > 10000
    assert repetition['status'] == 'PASS_EXACT_DUPLICATE_CHECK' and not repetition['errors'] and not repetition['exact_duplicates']
    assert digest(Path(metadata['reference'])) == metadata['reference_sha256']
    previous = json.loads(Path(metadata['previous_acceptance']).read_text(encoding='utf-8'))
    assert previous['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    earlier = {}
    for room in [f'S4-{index:02d}' for index in range(1, 10)]:
        folder = ROOT.parent / room
        entries = (folder / 'SHA256SUMS.txt').read_text(encoding='utf-8').splitlines()
        for entry in entries:
            expected, relative = entry.split('  ', 1)
            assert expected == digest(folder / relative), (room, relative)
        earlier[room] = {'sha256': digest(folder / 'incoming' / room / 'room.nbt'), 'manifest_entries_verified': len(entries)}
    assert earlier['S4-09']['sha256'] == metadata['previous_sha256']
    shutil.copy2(ROOT / 'preview-exterior.png', ROOT / 'incoming/S4-10/preview.png')
    record = {'group': 4, 'image': 10, 'status': 'SELF_CHECK_PASS_PENDING_INDEPENDENT_ACCEPTANCE', 'recorded_at': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(), 'name': metadata['name'], 'size': metadata['size'], 'sha256': candidate_hash, 'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'], 'previous_independent_acceptance': metadata['previous_acceptance'], 'accepted_earlier_images_unchanged': earlier, 'geometry_authorship': 'New single-source seven-region total reconstruction: northwest library, north altar hall, northeast chest chamber, central statue courtyard, southwest four-column flooded hall, southeast pottery workshop, south gateway/dock, linked by bent passages and central bridge. Prior room geometry not used. QA harnesses reused.', 'native_palette_template_and_entities': native, 'dense_48_max_fragments_and_exact_reassembly': split, 'native_fragment_templates': fragments, 'source_body_routes': body, 'fragile_canSurvive_and_neighbor_shapes': stability, 'container_lid_water_support_leaves_and_roofs': geometry, 'actual_full_source_views_and_cutaway': preview, 'cutaway_derivative': cutaway, 'repetition_screen': {'compared_unique_nbt': repetition['compared_unique_nbt_count'], 'exact_geometry_duplicates': 0, 'highest_coarse_match': repetition['comparisons'][0], 'scope': repetition['scope'], 'interpretation': 'Coarse occupancy similarity is not independent image/layout acceptance.'}, 'deviations': metadata['deviations'], 'closed_door_analysis': {'source_doors': 0, 'source_blocked_gate': False, 'open_property_only_variant': 'NOT_APPLICABLE_NO_DOORS'}, 'world_placement_ticks_gameplay': 'NOT_RUN', 'integration': 'NOT_RUN', 'deployment': 'NOT_RUN', 'independent_acceptance': 'PENDING', 'next_image_started': False, 'gate': 'S4-11 and later NOT_AUTHORIZED; do not start or pre-generate.'}
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
