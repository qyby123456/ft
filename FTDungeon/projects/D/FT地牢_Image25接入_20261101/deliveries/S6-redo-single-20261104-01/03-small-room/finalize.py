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
    assert metadata['sha256'] == fingerprint
    assert metadata['id'] == 'S6-REDO-03' and metadata['size'] == [19, 12, 19]
    acceptance_path = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S6-02-ACCEPTANCE.json')
    acceptance = json.loads(acceptance_path.read_text(encoding='utf-8'))
    assert acceptance['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    assert acceptance['nbt_sha256'] == sha(ROOT.parent / '02-medium-room-r2/room.nbt')
    preserved = []
    for folder_name in ['01-large-chamber-r3', '02-medium-room', '02-medium-room-r2']:
        folder = ROOT.parent / folder_name
        manifest = json.loads((folder / 'manifest.json').read_text(encoding='utf-8'))
        for record in manifest['files']:
            assert sha(folder / record['path']) == record['sha256'], str(folder / record['path'])
        preserved.append({'folder': str(folder), 'verified_file_count': len(manifest['files']), 'manifest_sha256': sha(folder / 'manifest.json'), 'status': 'ALL_HASHES_UNCHANGED'})
    assert not any(folder.name.startswith('04') for folder in ROOT.parent.iterdir())
    save(ROOT / 'qa/predecessor-lock.json', {'S6_02_acceptance': str(acceptance_path), 'acceptance_sha256': sha(acceptance_path), 'preserved': preserved, 'S6_04': 'LOCKED_NOT_STARTED', 'deployment': 'NOT_PERFORMED'})
    reports = {}
    for name in ['static', 'native', 'body-routes']:
        report = json.loads((ROOT / f'qa/{name}.json').read_text(encoding='utf-8'))
        assert report['status'] == 'PASS' and report['sha256'] == fingerprint
        reports[name] = report
    assert len(reports['body-routes']['targets']) == len(metadata['validation_targets']) == 8
    assert len(reports['native']['fragile_survival']) == 2
    assert all(record['odds'] == 0 for record in reports['native']['native_fire_odds'])
    captures = json.loads((ROOT / 'lodestone/screenshots.json').read_text(encoding='utf-8'))
    assert captures['final_nbt_sha256'] == fingerprint and not captures['page_errors']
    assert len(captures['screenshots']) == 8
    final = load(ROOT / 'room.nbt')
    final_cells = cells(final)
    source = {tuple(record['pos']): record for record in final['blocks']}
    cutaway_path = ROOT / 'lodestone/inspection-cutaway.nbt'
    cutaway = load(cutaway_path)
    assert cutaway['palette'] == final['palette'] and cutaway['size'] == final['size']
    assert all(record == source[tuple(record['pos'])] for record in cutaway['blocks'])
    assert captures['cutaway']['cutaway_nbt_sha256'] == sha(cutaway_path)
    detail_path = ROOT / 'lodestone/hearth-detail.nbt'
    detail = load(detail_path)
    assert detail['size'] == [8, 6, 4]
    assert len(detail['blocks']) == 8 * 6 * 4
    for position, cell in cells(detail).items():
        assert cell == final_cells[(position[0] + 6, position[1] + 2, position[2] + 15)]
    for capture in captures['screenshots']:
        assert capture['sha256'] == sha(ROOT / 'lodestone' / capture['file'])
        expected = fingerprint if 'exterior' in capture['file'] else sha(detail_path) if 'closeup' in capture['file'] else sha(cutaway_path)
        assert capture['loaded_nbt_sha256'] == expected
    draft_reports = []
    for folder_name, reason in [('rejected-side-route-01', 'Narrow side door unreachable behind pool rim; removed three dry rim blocks'), ('rejected-niche-proportion-02', 'Long niche opening too square; changed 3x3 opening to horizontal 4x2')]:
        folder = ROOT / 'qa' / folder_name
        old = cells(load(folder / 'room.nbt'))
        changes = [{'position': list(position), 'before': old[position], 'after': final_cells[position]} for position in sorted(old) if old[position] != final_cells[position]]
        report = {'status': 'REJECTED_NOT_CANDIDATE', 'reason': reason, 'preserved_nbt': str(folder / 'room.nbt'), 'preserved_sha256': sha(folder / 'room.nbt'), 'final_sha256': fingerprint, 'changes_to_final': changes}
        save(folder / 'status.json', report)
        draft_reports.append({'folder': str(folder), 'status': report['status'], 'sha256': report['preserved_sha256'], 'changes_to_final_count': len(changes)})
    duplicate = json.loads((ROOT / 'qa/duplicates.json').read_text(encoding='utf-8'))
    assert not duplicate['matches']
    metadata.update({'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs')})
    save(ROOT / 'room.json', metadata)
    save(ROOT / 'assembly.json', {'module': metadata['id'], 'size': metadata['size'], 'local_origin': [0, 0, 0], 'world_origin': None, 'assembly_order': [{'file': 'room.nbt', 'relative_origin': [0, 0, 0], 'rotation': 'NONE'}], 'connection_anchors': metadata['connectors'], 'interior_safe_feet': metadata['interior'], 'central_empty_chest': [9, 3, 9], 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_image_may_start': False})
    save(ROOT / 'registration.pending.json', {'id': metadata['id'], 'group': 'S6-volcanic-cavern', 'reference_number': 3, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'candidate': str(ROOT / 'room.nbt'), 'candidate_sha256': fingerprint, 'size': metadata['size'], 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'registry_updated': False, 'world_origin': None, 'deployment': 'NOT_PERFORMED', 'S6_04_unlocked': False, 'review_evidence': ['analysis.md', 'assembly.json', 'qa/static.json', 'qa/native.json', 'qa/body-routes.json', 'qa/predecessor-lock.json', 'qa/duplicates.json', 'lodestone/screenshots.json']})
    save(ROOT / 'qa/checklist.json', {'status': 'AUTHOR_CHECKS_COMPLETE_NOT_ACCEPTED', 'sha256': fingerprint, 'reference_sha256': sha(REFERENCE), 'checks': {'volume': 'PASS_4332', 'connectors': 'PASS_NORTH_INFERRED_3x4_EAST_VISIBLE_3x4_SOUTH_VISIBLE_1x3', 'native_registry_template_container': 'PASS', 'route_targets': 'PASS_8', 'player_body': 'PASS_0.6x1.8_HALF_GRID_STATIC_EDGE_SAMPLES', 'fragile_lanterns': 'PASS_2', 'native_fire_odds': 'ALL_ZERO_FOR_ACTUAL_PALETTE', 'lava': 'PASS_SIX_NEIGHBORS_SEALED_LEVEL_ZERO', 'solids_to_base': 'PASS', 'chest_lid_air': 'PASS_STATIC', 'lodestone': 'PASS_8_ACTUAL_BROWSER_SCREENSHOTS', 'cutaway_and_crop_provenance': 'PASS_NO_STATE_SUBSTITUTIONS', 'accepted_files': 'UNCHANGED', 'duplicate_exact_geometry': f"NO_MATCHES_IN_{duplicate['reviewed_count']}_LOCAL_PEERS"}, 'author_visual_review': 'Viewed actual whole room, doors/chest and long-niche closeups; center single chest, asymmetric basins, wide/narrow doors and horizontally recessed niche visible. Independent image fidelity judgment pending.', 'differences_from_reference': ['Roof/full boundary walls and north connector inferred from cutaway', 'Lava sealed behind glass, no open pools for contact', 'Magma/fire wall and floor detail represented by safe warm shroomlight', '19-wide interpretation instead of literal 9-wide prompt; image scale ambiguous', 'No actual vines, loot or combat entities'], 'retained_failed_candidates': draft_reports, 'world_ticks': 'NOT_RUN', 'deployment': 'NOT_PERFORMED', 'limitations': ['Immutable vanilla collision shapes and sampled routes, not continuous player physics', 'No Forge placement, neighbor updates, lava/fire/random ticks, rotations, lighting propagation or chest gameplay', 'Zero internal vanilla FireBlock odds is not external-world fire safety certification', 'Exact fingerprint equality is not semantic or near-duplicate certification'], 'S6_04': 'LOCKED'})
    save(ROOT / 'manifest.json', {'id': metadata['id'], 'reference_count': 1, 'candidate_structure_count': 1, 'retained_failed_candidates': 2, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'nbt_sha256': fingerprint, 'size': metadata['size'], 'stage': 'READY_FOR_REVIEW_NOT_ACCEPTED', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'deployment': 'NOT_PERFORMED', 'files': [{'path': path.relative_to(ROOT).as_posix(), 'sha256': sha(path), 'bytes': path.stat().st_size} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name != 'manifest.json']})
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    for record in manifest['files']:
        assert sha(ROOT / record['path']) == record['sha256']
    print('S6-03 candidate SHA256:', fingerprint)
    print('Verified', len(manifest['files']), 'current/retained files; previous verified:', [record['verified_file_count'] for record in preserved], '; S6-04 locked')


if __name__ == '__main__':
    finalize()
