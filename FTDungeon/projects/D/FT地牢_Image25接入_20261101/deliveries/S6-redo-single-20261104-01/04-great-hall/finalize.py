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
    assert metadata['sha256'] == fingerprint and metadata['id'] == 'S6-REDO-04'
    acceptance_path = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S6-03-ACCEPTANCE.json')
    acceptance = json.loads(acceptance_path.read_text(encoding='utf-8'))
    assert acceptance['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    assert acceptance['nbt_sha256'] == sha(ROOT.parent / '03-small-room/room.nbt')
    preserved = []
    for folder_name in ['01-large-chamber-r3', '02-medium-room', '02-medium-room-r2', '03-small-room']:
        folder = ROOT.parent / folder_name
        manifest = json.loads((folder / 'manifest.json').read_text(encoding='utf-8'))
        for record in manifest['files']:
            assert sha(folder / record['path']) == record['sha256'], str(folder / record['path'])
        preserved.append({'folder': str(folder), 'verified_file_count': len(manifest['files']), 'manifest_sha256': sha(folder / 'manifest.json'), 'status': 'ALL_HASHES_UNCHANGED'})
    assert not any(folder.name.startswith('05') for folder in ROOT.parent.iterdir())
    save(ROOT / 'qa/predecessor-lock.json', {'S6_03_acceptance': str(acceptance_path), 'acceptance_sha256': sha(acceptance_path), 'preserved': preserved, 'S6_05': 'LOCKED_NOT_STARTED', 'deployment': 'NOT_PERFORMED'})
    reports = {}
    for name in ['static', 'native', 'body-routes']:
        report = json.loads((ROOT / f'qa/{name}.json').read_text(encoding='utf-8'))
        assert report['status'] == 'PASS' and report['sha256'] == fingerprint
        reports[name] = report
    assert len(reports['body-routes']['targets']) == len(metadata['validation_targets']) == 13
    assert reports['body-routes']['half_height_stair_points'] > 0
    assert len(reports['native']['fragile_survival']) == 16
    assert all(record['odds'] == 0 for record in reports['native']['native_fire_odds'])
    captures = json.loads((ROOT / 'lodestone/screenshots.json').read_text(encoding='utf-8'))
    assert captures['final_nbt_sha256'] == fingerprint and not captures['page_errors'] and len(captures['screenshots']) == 8
    final = load(ROOT / 'room.nbt')
    final_cells = cells(final)
    source = {tuple(record['pos']): record for record in final['blocks']}
    cutaway_path = ROOT / 'lodestone/inspection-cutaway.nbt'
    cutaway = load(cutaway_path)
    assert cutaway['palette'] == final['palette'] and cutaway['size'] == final['size']
    assert all(record == source[tuple(record['pos'])] for record in cutaway['blocks'])
    assert captures['cutaway']['cutaway_nbt_sha256'] == sha(cutaway_path)
    detail_path = ROOT / 'lodestone/arch-detail.nbt'
    detail = load(detail_path)
    assert detail['size'] == [11, 12, 6] and len(detail['blocks']) == 792
    for position, cell in cells(detail).items():
        assert cell == final_cells[(position[0] + 8, position[1] + 2, position[2] + 31)]
    for capture in captures['screenshots']:
        assert capture['sha256'] == sha(ROOT / 'lodestone' / capture['file'])
        expected = fingerprint if 'exterior' in capture['file'] else sha(detail_path) if 'closeup' in capture['file'] else sha(cutaway_path)
        assert capture['loaded_nbt_sha256'] == expected
    old_folder = ROOT / 'qa/rejected-outer-route-01'
    old = cells(load(old_folder / 'room.nbt'))
    changes = [{'position': list(position), 'before': old[position], 'after': final_cells[position]} for position in sorted(old) if old[position] != final_cells[position]]
    assert len(changes) == 50
    assert all(record['position'][0] in [2, 24] and record['position'][1] == 3 and 3 <= record['position'][2] <= 30 for record in changes)
    assert all(record['before']['state']['Name'] == 'minecraft:polished_blackstone_bricks' and record['after']['state']['Name'] == 'minecraft:air' for record in changes)
    save(old_folder / 'status.json', {'status': 'REJECTED_NOT_CANDIDATE', 'reason': 'raised outer rim plus embedded wall pillars blocked two declared outer patrol targets; lowered dry rim only', 'preserved_sha256': sha(old_folder / 'room.nbt'), 'final_sha256': fingerprint, 'lava_coordinates_and_states_unchanged': True, 'changes_to_final_count': len(changes), 'changes_to_final': changes})
    duplicate = json.loads((ROOT / 'qa/duplicates.json').read_text(encoding='utf-8'))
    assert not duplicate['matches']
    metadata.update({'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs')})
    save(ROOT / 'room.json', metadata)
    save(ROOT / 'assembly.json', {'module': metadata['id'], 'size': metadata['size'], 'local_origin': [0, 0, 0], 'world_origin': None, 'assembly_order': [{'file': 'room.nbt', 'relative_origin': [0, 0, 0], 'rotation': 'NONE'}], 'connection_anchors': metadata['connectors'], 'interior_safe_feet': metadata['interior'], 'raised_platform': {'floor_y': 3, 'feet_y': 4, 'x': [9, 17], 'z': [32, 36], 'stairs': {'x': [10, 16], 'y': 3, 'z': 31, 'facing': 'south'}}, 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_image_may_start': False})
    save(ROOT / 'registration.pending.json', {'id': metadata['id'], 'group': 'S6-volcanic-cavern', 'reference_number': 4, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'candidate': str(ROOT / 'room.nbt'), 'candidate_sha256': fingerprint, 'size': metadata['size'], 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'registry_updated': False, 'world_origin': None, 'deployment': 'NOT_PERFORMED', 'S6_05_unlocked': False, 'review_evidence': ['analysis.md', 'assembly.json', 'qa/static.json', 'qa/native.json', 'qa/body-routes.json', 'qa/predecessor-lock.json', 'qa/duplicates.json', 'lodestone/screenshots.json']})
    save(ROOT / 'qa/checklist.json', {'status': 'AUTHOR_CHECKS_COMPLETE_NOT_ACCEPTED', 'sha256': fingerprint, 'reference_sha256': sha(REFERENCE), 'checks': {'volume': 'PASS_16983', 'six_column_centers': 'PASS', 'ports': 'PASS_FOUR', 'native_registry_template': 'PASS', 'route_targets': 'PASS_13', 'player_body': 'PASS_0.6x1.8_HALF_GRID_STEP_SWEEP_STATIC', 'native_lantern_support': 'PASS_16', 'native_fire_odds': 'ALL_ZERO_ACTUAL_PALETTE', 'lava': 'PASS_SIX_NEIGHBORS_SEALED_LEVEL_ZERO', 'solids_to_base': 'PASS', 'roof_columns': 'PASS_999', 'entities_and_containers': 'NONE', 'lodestone': 'PASS_8_ACTUAL_BROWSER_SCREENSHOTS', 'cutaway_crop_provenance': 'PASS_NO_STATE_CHANGES', 'previous_files': 'ALL_MANIFEST_HASHES_UNCHANGED', 'duplicate_exact_geometry': f"NO_MATCHES_IN_{duplicate['reviewed_count']}_LOCAL_PEERS"}, 'author_visual_review': 'Viewed actual whole hall, central aisle, raised arch and exterior. Double three-column rows, wide center aisle, flanking channels, red-black panels and stepped raised far portal readable. Independent fidelity acceptance pending.', 'differences_from_reference': ['Actual red banners represented by nonflammable terracotta panels, visually cloth-like but solid, not banners', 'Heat patterns replaced by safe shroomlight instead of magma/fire', 'Lava sealed behind orange glass', 'Full roof and plain excavation exterior inferred from cutaway; not a surface building', 'Outer world stairs/terrain excluded; one internal stair lift retained', '27x17x37 scale interpretive, not exact reconstruction'], 'retained_failed_candidate': {'path': str(old_folder), 'sha256': sha(old_folder / 'room.nbt'), 'changed_dry_rim_cells': len(changes)}, 'world_ticks': 'NOT_RUN', 'deployment': 'NOT_PERFORMED', 'limitations': ['Native static collision and discrete surface sampling/lift-horizontal-drop routing, not continuous physics', 'No Forge placement, neighbor updates, fluid/fire/random ticks, lighting propagation, rotations or actual interaction/gameplay', 'Glass and zero internal FireBlock odds do not certify external-world fire safety', 'Exact fingerprint is not semantic/near-duplicate or global uniqueness proof'], 'S6_05': 'LOCKED'})
    save(ROOT / 'manifest.json', {'id': metadata['id'], 'reference_count': 1, 'candidate_structure_count': 1, 'retained_failed_candidates': 1, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'nbt_sha256': fingerprint, 'size': metadata['size'], 'stage': 'READY_FOR_REVIEW_NOT_ACCEPTED', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'deployment': 'NOT_PERFORMED', 'files': [{'path': path.relative_to(ROOT).as_posix(), 'sha256': sha(path), 'bytes': path.stat().st_size} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name != 'manifest.json']})
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    for record in manifest['files']:
        assert sha(ROOT / record['path']) == record['sha256']
    print('S6-04 candidate SHA256:', fingerprint)
    print('Verified', len(manifest['files']), 'files; previous counts:', [record['verified_file_count'] for record in preserved], '; S6-05 locked')


if __name__ == '__main__':
    finalize()
