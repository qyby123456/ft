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
    assert metadata['sha256'] == fingerprint and metadata['id'] == 'S6-REDO-05'
    acceptance_path = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S6-04-ACCEPTANCE.json')
    acceptance = json.loads(acceptance_path.read_text(encoding='utf-8'))
    assert acceptance['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    assert acceptance['nbt_sha256'] == sha(ROOT.parent / '04-great-hall/room.nbt')
    preserved = []
    for folder_name in ['01-large-chamber-r3', '02-medium-room', '02-medium-room-r2', '03-small-room', '04-great-hall']:
        folder = ROOT.parent / folder_name
        manifest = json.loads((folder / 'manifest.json').read_text(encoding='utf-8'))
        for record in manifest['files']:
            assert sha(folder / record['path']) == record['sha256'], str(folder / record['path'])
        preserved.append({'folder': str(folder), 'verified_file_count': len(manifest['files']), 'manifest_sha256': sha(folder / 'manifest.json'), 'status': 'ALL_HASHES_UNCHANGED'})
    assert not any(folder.name.startswith('06') for folder in ROOT.parent.iterdir())
    save(ROOT / 'qa/predecessor-lock.json', {'S6_04_acceptance': str(acceptance_path), 'acceptance_sha256': sha(acceptance_path), 'preserved': preserved, 'S6_06': 'LOCKED_NOT_STARTED', 'deployment': 'NOT_PERFORMED'})
    reports = {}
    for name in ['static', 'native', 'body-routes']:
        report = json.loads((ROOT / f'qa/{name}.json').read_text(encoding='utf-8'))
        assert report['status'] == 'PASS' and report['sha256'] == fingerprint
        reports[name] = report
    assert len(reports['body-routes']['targets']) == len(metadata['validation_targets']) == 14
    assert len(reports['native']['fragile_survival']) == 8
    assert all(record['odds'] == 0 for record in reports['native']['native_fire_odds'])
    captures = json.loads((ROOT / 'lodestone/screenshots.json').read_text(encoding='utf-8'))
    assert captures['final_nbt_sha256'] == fingerprint and not captures['page_errors'] and len(captures['screenshots']) == 7
    assert captures['disk_skill_modified'] is False
    final = load(ROOT / 'room.nbt')
    final_cells = cells(final)
    source = {tuple(record['pos']): record for record in final['blocks']}
    cutaway_path = ROOT / 'lodestone/inspection-cutaway.nbt'
    cutaway = load(cutaway_path)
    assert cutaway['palette'] == final['palette'] and cutaway['size'] == final['size']
    assert all(record == source[tuple(record['pos'])] for record in cutaway['blocks'])
    assert captures['cutaway']['cutaway_nbt_sha256'] == sha(cutaway_path)
    detail_path = ROOT / 'lodestone/lava-window-detail.nbt'
    detail = load(detail_path)
    assert detail['size'] == [4, 10, 5] and len(detail['blocks']) == 200
    for position, cell in cells(detail).items():
        assert cell == final_cells[(position[0], position[1] + 1, position[2] + 6)]
    eye_views = []
    for capture in captures['screenshots']:
        assert capture['sha256'] == sha(ROOT / 'lodestone' / capture['file'])
        expected = fingerprint if 'exterior' in capture['file'] or 'eye-level' in capture['file'] else sha(detail_path) if 'closeup' in capture['file'] else sha(cutaway_path)
        assert capture['loaded_nbt_sha256'] == expected
        if 'eye-level' in capture['file']:
            assert capture['camera']['position'][1] == capture['camera']['target'][1] == 4.62
            eye_views.append(capture['camera'])
    assert len(eye_views) == 2
    duplicate = json.loads((ROOT / 'qa/duplicates.json').read_text(encoding='utf-8'))
    assert not duplicate['matches']
    save(ROOT / 'qa/checker-correction.json', {'status': 'CHECKER_EXPECTATION_CORRECTED_NO_NBT_CHANGE', 'nbt_sha256_before_and_after': fingerprint, 'original_failure': 'checker expected chain at y10 although recipe crossbeam at y10 is the solid chain anchor', 'corrected_assertion': 'lantern y7, chain y8..9, solid beam y10; native canSurvive and attachment graph subsequently passed', 'geometry_revision_count': 0, 'failed_geometry_candidates': 0})
    metadata.update({'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs')})
    save(ROOT / 'room.json', metadata)
    save(ROOT / 'assembly.json', {'module': metadata['id'], 'size': metadata['size'], 'local_origin': [0, 0, 0], 'world_origin': None, 'assembly_order': [{'file': 'room.nbt', 'relative_origin': [0, 0, 0], 'rotation': 'NONE'}], 'connection_anchors': metadata['connectors'], 'interior_safe_feet': metadata['interior'], 'unbroken_dry_aisle': {'x': [7, 9], 'feet_y': 3, 'z': [0, 34]}, 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_image_may_start': False})
    save(ROOT / 'registration.pending.json', {'id': metadata['id'], 'group': 'S6-volcanic-cavern', 'reference_number': 5, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'candidate': str(ROOT / 'room.nbt'), 'candidate_sha256': fingerprint, 'size': metadata['size'], 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'registry_updated': False, 'world_origin': None, 'deployment': 'NOT_PERFORMED', 'S6_06_unlocked': False, 'review_evidence': ['analysis.md', 'assembly.json', 'qa/static.json', 'qa/native.json', 'qa/body-routes.json', 'qa/predecessor-lock.json', 'qa/duplicates.json', 'lodestone/screenshots.json']})
    save(ROOT / 'qa/checklist.json', {'status': 'AUTHOR_CHECKS_COMPLETE_NOT_ACCEPTED', 'sha256': fingerprint, 'reference_sha256': sha(REFERENCE), 'checks': {'volume': 'PASS_7140', 'straight_3_wide_dry_aisle': 'PASS_ALL35_Z_ROWS', 'end_ports': 'PASS_TWO_3x5_DEPTH2', 'side_branch_count': 0, 'native_registry_template': 'PASS', 'native_and_body_route_targets': 'PASS_14', 'body_scope': '0.6x1.8 flat y3 half-grid with0.1-edge samples and dry sturdy floor under footprint', 'native_lantern_support': 'PASS_8', 'native_fire_odds': 'ALL_ZERO_ACTUAL_PALETTE', 'lava': 'PASS_LEVEL_ZERO_SIX_NEIGHBOR_ENCLOSED', 'solids_to_base': 'PASS', 'roof_columns': 'PASS_595', 'entities_and_containers': 'NONE', 'lodestone': 'PASS_7_ACTUAL_BROWSER_SCREENSHOTS', 'eye_level_full_final_nbt': 'PASS_FORWARD_REVERSE', 'cutaway_crop_provenance': 'PASS_NO_STATE_CHANGES', 'previous_files': 'ALL_MANIFEST_HASHES_UNCHANGED', 'duplicate_exact_geometry': f"NO_MATCHES_IN_{duplicate['reviewed_count']}_LOCAL_PEERS"}, 'author_visual_review': 'Viewed actual full-NBT eye-level axis, cutaway and sealed window detail. Low roof, paired four-bay columns/lanterns, center straight aisle and distant doorway visible. Image fidelity still awaits independent acceptance.', 'differences_from_reference': ['Hot pillar/floor veins approximated by safe shroomlight; no magma damage or fire/smoke particles', 'Open lava/falls sealed with orange glass and stone', '17x12x35 is perspective interpretation, not exact inverse reconstruction', 'Plain full-volume exterior for underground placement; no outer cave scenery'], 'eye_camera_positions': eye_views, 'viewer_instrumentation': 'in-memory browser response adds one exposed camera/renderer bridge; eye views call existing renderer via camera coordinates only; model/material/resource pack/disk skill unchanged', 'world_ticks': 'NOT_RUN', 'deployment': 'NOT_PERFORMED', 'limitations': ['Static immutable vanilla collision and sampled routes, not continuous player physics', 'No Forge placement, neighbor updates, fluid/fire/random ticks, lighting propagation, rotations, actual interactions or gameplay', 'Glass containment and zero internal fuel odds are not external-world fire certification', 'Exact fingerprint is not semantic/near-duplicate/global uniqueness proof'], 'S6_06': 'LOCKED'})
    save(ROOT / 'manifest.json', {'id': metadata['id'], 'reference_count': 1, 'candidate_structure_count': 1, 'geometry_revisions': 0, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'nbt_sha256': fingerprint, 'size': metadata['size'], 'stage': 'READY_FOR_REVIEW_NOT_ACCEPTED', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'deployment': 'NOT_PERFORMED', 'files': [{'path': path.relative_to(ROOT).as_posix(), 'sha256': sha(path), 'bytes': path.stat().st_size} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name != 'manifest.json']})
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    for record in manifest['files']:
        assert sha(ROOT / record['path']) == record['sha256']
    print('S6-05 candidate SHA256:', fingerprint)
    print('Verified', len(manifest['files']), 'files; previous counts:', [record['verified_file_count'] for record in preserved], '; S6-06 locked')


if __name__ == '__main__':
    finalize()
