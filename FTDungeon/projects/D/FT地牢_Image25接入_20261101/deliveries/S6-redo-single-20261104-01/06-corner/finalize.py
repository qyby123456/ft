import json
from pathlib import Path
import sys

sys.dont_write_bytecode = True
from check import ROOT, REFERENCE, load, save, sha


def cells(data):
    return {tuple(record['pos']): {'state': data['palette'][record['state']], 'nbt': record.get('nbt')} for record in data['blocks']}


def finalize():
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    fingerprint = sha(ROOT / 'room.nbt')
    assert metadata['sha256'] == fingerprint and metadata['id'] == 'S6-REDO-06'
    review_root = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685')
    review = review_root / 'REVIEW-20261105-S1R3-S605.md'
    text = review.read_text(encoding='utf-8')
    predecessor_sha = sha(ROOT.parent / '05-corridor/room.nbt')
    assert predecessor_sha == 'ebe6df2e25340b831a977910b4b12eae78d47dc16aa968d2afad1d3e77cb7310'
    assert predecessor_sha in text and 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION' in text and 'S6-06-corner.png' in text
    frozen = review_root / 'S6-05-independent/room.nbt'
    assert sha(frozen) == predecessor_sha
    preserved = []
    for folder_name in ['01-large-chamber-r3', '02-medium-room', '02-medium-room-r2', '03-small-room', '04-great-hall', '05-corridor']:
        folder = ROOT.parent / folder_name
        manifest = json.loads((folder / 'manifest.json').read_text(encoding='utf-8'))
        for record in manifest['files']:
            assert sha(folder / record['path']) == record['sha256'], str(folder / record['path'])
        preserved.append({'folder': str(folder), 'verified_file_count': len(manifest['files']), 'manifest_sha256': sha(folder / 'manifest.json'), 'status': 'ALL_HASHES_UNCHANGED'})
    assert not any(folder.name.startswith('07') for folder in ROOT.parent.iterdir())
    save(ROOT / 'qa/predecessor-lock.json', {'S6_05_acceptance_report': str(review), 'report_sha256': sha(review), 'S6_05_frozen_nbt': str(frozen), 'frozen_nbt_sha256': sha(frozen), 'preserved': preserved, 'S6_07': 'LOCKED_NOT_STARTED', 'deployment': 'NOT_PERFORMED'})
    reports = {}
    for name in ['static', 'native', 'body-routes']:
        report = json.loads((ROOT / f'qa/{name}.json').read_text(encoding='utf-8'))
        assert report['status'] == 'PASS' and report['sha256'] == fingerprint
        reports[name] = report
    assert len(reports['body-routes']['targets']) == len(metadata['validation_targets']) == 16
    assert len(reports['native']['fragile_survival']) == 5
    assert all(record['odds'] == 0 for record in reports['native']['native_fire_odds'])
    captures = json.loads((ROOT / 'lodestone/screenshots.json').read_text(encoding='utf-8'))
    assert captures['final_nbt_sha256'] == fingerprint and not captures['page_errors'] and len(captures['screenshots']) == 8
    assert captures['disk_skill_modified'] is False
    final = load(ROOT / 'room.nbt')
    final_cells = cells(final)
    source = {tuple(record['pos']): record for record in final['blocks']}
    cutaway_path = ROOT / 'lodestone/inspection-cutaway.nbt'
    cutaway = load(cutaway_path)
    assert cutaway['palette'] == final['palette'] and cutaway['size'] == final['size']
    expected_cutaway = [record for record in final['blocks'] if record['pos'][1] < 10 and not (record['pos'][1] >= 3 and (record['pos'][0] <= 2 or record['pos'][2] >= 26 or record['pos'][2] <= 1))]
    assert cutaway['blocks'] == expected_cutaway
    assert all(record == source[tuple(record['pos'])] for record in cutaway['blocks'])
    assert captures['cutaway']['cutaway_nbt_sha256'] == sha(cutaway_path)
    detail_path = ROOT / 'lodestone/corner-niche-detail.nbt'
    detail = load(detail_path)
    provenance = json.loads((ROOT / 'lodestone/detail-provenance.json').read_text(encoding='utf-8'))
    assert provenance['final_nbt_sha256'] == fingerprint and provenance['detail_sha256'] == sha(detail_path)
    assert detail['size'] == [6, 9, 6] and len(detail['blocks']) == 324
    for position, cell in cells(detail).items():
        assert cell == final_cells[(position[0] + 9, position[1] + 1, position[2] + 13)]
    eye_views = []
    for capture in captures['screenshots']:
        assert capture['sha256'] == sha(ROOT / 'lodestone' / capture['file'])
        expected = fingerprint if 'exterior' in capture['file'] or 'eye-level' in capture['file'] else sha(detail_path) if 'closeup' in capture['file'] else sha(cutaway_path)
        assert capture['loaded_nbt_sha256'] == expected
        if 'eye-level' in capture['file']:
            assert capture['camera']['position'][1] == capture['camera']['target'][1] == 4.62
            eye_views.append(capture['camera'])
    assert len(eye_views) == 3
    duplicate = json.loads((ROOT / 'qa/duplicates.json').read_text(encoding='utf-8'))
    assert not duplicate['matches']
    metadata.update({'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs')})
    save(ROOT / 'room.json', metadata)
    aisle = {'feet_y': 3, 'north_arm': {'x': [6, 8], 'z': [0, 22]}, 'east_arm': {'x': [6, 28], 'z': [20, 22]}, 'turn_center': [7, 3, 21]}
    save(ROOT / 'assembly.json', {'module': metadata['id'], 'size': metadata['size'], 'local_origin': [0, 0, 0], 'world_origin': None, 'assembly_order': [{'file': 'room.nbt', 'relative_origin': [0, 0, 0], 'rotation': 'NONE'}], 'connection_anchors': metadata['connectors'], 'interior_safe_feet': metadata['interior'], 'dry_three_wide_L_aisle': aisle, 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_image_may_start': False})
    save(ROOT / 'registration.pending.json', {'id': metadata['id'], 'group': 'S6-volcanic-cavern', 'reference_number': 6, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'candidate': str(ROOT / 'room.nbt'), 'candidate_sha256': fingerprint, 'size': metadata['size'], 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'registry_updated': False, 'world_origin': None, 'deployment': 'NOT_PERFORMED', 'S6_07_unlocked': False, 'review_evidence': ['analysis.md', 'assembly.json', 'qa/static.json', 'qa/native.json', 'qa/body-routes.json', 'qa/predecessor-lock.json', 'qa/duplicates.json', 'lodestone/screenshots.json']})
    save(ROOT / 'qa/checklist.json', {'status': 'AUTHOR_CHECKS_COMPLETE_NOT_ACCEPTED', 'sha256': fingerprint, 'reference_sha256': sha(REFERENCE), 'checks': {'volume': 'PASS_10092', 'genuine_L': 'PASS_TWO_PERPENDICULAR_ARMS_AROUND_SOLID_INNER_CORNER', 'three_wide_dry_aisle': aisle, 'ports': 'PASS_NORTH_AND_EAST_3x5_DEPTH2', 'extra_boundary_openings': 0, 'native_registry_template': 'PASS', 'native_and_body_targets': 16, 'body_points': reports['body-routes']['reachable_grid_points'], 'native_lantern_support': 'PASS_5', 'native_fire_odds': 'ALL_ZERO_ACTUAL_PALETTE', 'lava': 'PASS_40_LEVEL_ZERO_SIX_NEIGHBOR_ENCLOSED', 'solids_to_base': 'PASS', 'roof_columns': reports['static']['roof_columns'], 'entities_and_containers': 'NONE', 'lodestone': 'PASS_8_ACTUAL_BROWSER_SCREENSHOTS', 'eye_level_final_nbt': 'PASS_3_VIEWS_INCLUDING_CORNER', 'cutaway_crop_provenance': 'PASS_EXACT_REMOVAL_OR_TRANSLATION', 'previous_files': 'ALL_MANIFEST_HASHES_UNCHANGED', 'duplicate_exact_geometry': f"NO_MATCHES_IN_{duplicate['reviewed_count']}_LOCAL_PEERS"}, 'author_visual_review': 'Viewed actual cutaway, overhead, full-NBT eye views and niche crop; true L footprint, corner lantern, obsidian supports and lava glazing are visible. Image fidelity awaits independent review.', 'differences_from_reference': ['Open exterior lava river/cave scenery omitted from connector module', 'Falls sealed behind orange glass; hot floor veins approximated by safe shroomlight and gilded blackstone', '29x12x29 is a practical perspective interpretation', 'Full module has a roof; section inspection removes roof and near walls only'], 'viewer_instrumentation': 'In-memory browser response exposes existing renderer/camera only; no block/material/pack changes or disk skill edits', 'geometry_revisions': 0, 'world_ticks': 'NOT_RUN', 'deployment': 'NOT_PERFORMED', 'limitations': ['Immutable static vanilla checks and sampled routes are not continuous player physics or Forge placement', 'No neighbor updates, fluid/fire/random ticks, lighting propagation, rotations or gameplay interaction tested', 'Cutaway removes west lava cells; crops/sections must not be deployed or used to certify fluid boundaries', 'Exact category fingerprints do not prove semantic/global uniqueness'], 'S6_07': 'LOCKED'})
    save(ROOT / 'manifest.json', {'id': metadata['id'], 'reference_count': 1, 'candidate_structure_count': 1, 'geometry_revisions': 0, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'nbt_sha256': fingerprint, 'size': metadata['size'], 'stage': 'READY_FOR_REVIEW_NOT_ACCEPTED', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'deployment': 'NOT_PERFORMED', 'files': [{'path': path.relative_to(ROOT).as_posix(), 'sha256': sha(path), 'bytes': path.stat().st_size} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name != 'manifest.json']})
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    for record in manifest['files']:
        assert sha(ROOT / record['path']) == record['sha256']
    print('S6-06 candidate SHA256:', fingerprint)
    print('Verified', len(manifest['files']), 'files; preserved previous counts:', [record['verified_file_count'] for record in preserved], '; S6-07 locked')


if __name__ == '__main__':
    finalize()
