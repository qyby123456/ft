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
    assert metadata['sha256'] == fingerprint and metadata['id'] == 'S6-REDO-08'
    predecessor = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S6-07-independent')
    acceptance_path = predecessor / 'independent-details.json'
    acceptance = json.loads(acceptance_path.read_text(encoding='utf-8'))
    predecessor_sha = sha(ROOT.parent / '07-entrance/room.nbt')
    assert acceptance['sha256'] == predecessor_sha == sha(predecessor / 'room.nbt') == '320609e2afcd253650c5bb4ba8ef2b07fef6fd0efb0d89b365e72935d56a0f6e'
    preserved = []
    for folder_name in ['01-large-chamber-r3', '02-medium-room', '02-medium-room-r2', '03-small-room', '04-great-hall', '05-corridor', '06-corner', '07-entrance']:
        folder = ROOT.parent / folder_name
        manifest = json.loads((folder / 'manifest.json').read_text(encoding='utf-8'))
        for record in manifest['files']:
            assert sha(folder / record['path']) == record['sha256'], str(folder / record['path'])
        preserved.append({'folder': str(folder), 'verified_file_count': len(manifest['files']), 'manifest_sha256': sha(folder / 'manifest.json'), 'status': 'ALL_HASHES_UNCHANGED'})
    assert not any(folder.name.startswith('09') for folder in ROOT.parent.iterdir())
    save(ROOT / 'qa/predecessor-lock.json', {'S6_07_independent_details': str(acceptance_path), 'details_sha256': sha(acceptance_path), 'authorization': 'User delegation explicitly states ACCEPTED_FOR_SEQUENTIAL_PRODUCTION for frozen S6-07; independent-details file records checks and frozen hash, no invented acceptance status field', 'S6_07_frozen_nbt': str(predecessor / 'room.nbt'), 'frozen_nbt_sha256': predecessor_sha, 'preserved': preserved, 'S6_09': 'LOCKED_NOT_STARTED', 'deployment': 'NOT_PERFORMED'})
    reports = {}
    for name in ['static', 'native', 'body-routes']:
        report = json.loads((ROOT / f'qa/{name}.json').read_text(encoding='utf-8'))
        assert report['status'] == 'PASS' and report['sha256'] == fingerprint
        reports[name] = report
    assert len(reports['body-routes']['targets']) == len(metadata['validation_targets']) == 17
    assert reports['body-routes']['half_height_stair_points'] > 0
    assert len(reports['native']['fragile_survival']) == 14
    assert all(record['odds'] == 0 for record in reports['native']['native_fire_odds'])
    final = load(ROOT / 'room.nbt')
    final_cells = cells(final)
    revisions = []
    for draft, expected_count in [('draft-r0', 7), ('draft-r1', 6)]:
        path = ROOT / 'qa' / draft / 'room.nbt'
        before = cells(load(path))
        changes = [{'position': list(point), 'before': before[point], 'after': final_cells[point]} for point in sorted(final_cells) if before[point] != final_cells[point]]
        assert len(changes) == expected_count
        assert {tuple(change['position']) for change in changes} <= {(17, 11, 14), (4, 13, 21), (32, 13, 21), (12, 13, 5), (24, 13, 5), (12, 13, 31), (24, 13, 31)}
        revisions.append({'draft': draft, 'sha256': sha(path), 'source_sha256': sha(path.parent / 'recipe.mjs'), 'differences_to_final': changes, 'failure_note': str(path.parent / 'FAILURE.md')})
    save(ROOT / 'qa/revision-diff.json', {'final_nbt_sha256': fingerprint, 'geometry_revisions': 2, 'preserved_drafts': revisions, 'summary': 'One obsidian cap side support plus six root substrates changed from crimson_nylium to soul_soil; no fluid/path/size changes'})
    captures = json.loads((ROOT / 'lodestone/screenshots.json').read_text(encoding='utf-8'))
    assert captures['final_nbt_sha256'] == fingerprint and not captures['page_errors'] and len(captures['screenshots']) == 10
    assert captures['disk_skill_modified'] is False
    cutaway_path = ROOT / 'lodestone/inspection-cutaway.nbt'
    cutaway = load(cutaway_path)
    assert cutaway['palette'] == final['palette'] and cutaway['size'] == final['size']
    expected_cutaway = [record for record in final['blocks'] if not (record['pos'][1] >= 21 or (record['pos'][1] >= 3 and (record['pos'][0] <= 2 or record['pos'][2] <= 2)))]
    assert cutaway['blocks'] == expected_cutaway
    assert captures['cutaway']['cutaway_nbt_sha256'] == sha(cutaway_path)
    detail_path = ROOT / 'lodestone/fountain-detail.nbt'
    detail = load(detail_path)
    provenance = json.loads((ROOT / 'lodestone/detail-provenance.json').read_text(encoding='utf-8'))
    assert provenance['final_nbt_sha256'] == fingerprint and provenance['detail_sha256'] == sha(detail_path)
    assert detail['size'] == [13, 15, 13] and len(detail['blocks']) == 2535
    for position, cell in cells(detail).items():
        assert cell == final_cells[(position[0] + 12, position[1] + 1, position[2] + 12)]
    for capture in captures['screenshots']:
        assert capture['sha256'] == sha(ROOT / 'lodestone' / capture['file'])
        port = int(capture['url'].split(':')[2].split('/')[0])
        assert capture['loaded_nbt_sha256'] == {5461: fingerprint, 5462: sha(cutaway_path), 5463: sha(detail_path)}[port]
    duplicate = json.loads((ROOT / 'qa/duplicates.json').read_text(encoding='utf-8'))
    assert not duplicate['matches']
    metadata.update({'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs')})
    save(ROOT / 'room.json', metadata)
    save(ROOT / 'assembly.json', {'module': metadata['id'], 'size': metadata['size'], 'local_origin': [0, 0, 0], 'world_origin': None, 'assembly_order': [{'file': 'room.nbt', 'relative_origin': [0, 0, 0], 'rotation': 'NONE'}], 'connection_anchors': metadata['connectors'], 'interior_safe_feet': metadata['interior'], 'ground_feet_y': 3, 'upper_feet_y': 13, 'stair_runs': metadata['stair_runs'], 'skylight': metadata['skylight'], 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_image_may_start': False})
    save(ROOT / 'registration.pending.json', {'id': metadata['id'], 'group': 'S6-volcanic-cavern', 'reference_number': 8, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'candidate': str(ROOT / 'room.nbt'), 'candidate_sha256': fingerprint, 'size': metadata['size'], 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'registry_updated': False, 'world_origin': None, 'deployment': 'NOT_PERFORMED', 'S6_09_unlocked': False, 'review_evidence': ['analysis.md', 'assembly.json', 'qa/static.json', 'qa/native.json', 'qa/body-routes.json', 'qa/revision-diff.json', 'qa/predecessor-lock.json', 'qa/duplicates.json', 'lodestone/screenshots.json']})
    save(ROOT / 'qa/checklist.json', {'status': 'AUTHOR_CHECKS_COMPLETE_NOT_ACCEPTED', 'sha256': fingerprint, 'reference_sha256': sha(REFERENCE), 'checks': {'volume': 'PASS_31487', 'composition': 'TIER_FOUNTAIN_DRY_RING_DUAL_STAIRS_UPPER_GALLERY_OPEN_SKYLIGHT', 'port_clearance': 'PASS_NORTH_SOUTH_3x5_DEPTH3', 'native_registry_template': 'PASS_UNMODIFIED_SHARED_VERIFIER', 'native_and_body_targets': 17, 'body_grid_points': reports['body-routes']['reachable_grid_points'], 'half_step_grid_points': reports['body-routes']['half_height_stair_points'], 'native_selected_support': 'PASS_8_LANTERNS_6_ROOTS', 'native_fire_odds': 'ALL_ZERO_ACTUAL_PALETTE', 'lava': 'PASS_152_LEVEL_ZERO_SIX_NEIGHBOR_ENCLOSED', 'solids_to_base': 'PASS', 'stair_blocks': 60, 'open_skylight_columns': 289, 'peripheral_roof_columns': 1080, 'entities_and_containers': 'NONE', 'lodestone': 'PASS_9_ACTUAL_BROWSER_SCREENSHOTS', 'cutaway_crop_provenance': 'PASS_EXACT_REMOVAL_OR_TRANSLATION', 'previous_files': 'ALL_MANIFEST_HASHES_UNCHANGED', 'duplicate_exact_geometry': f"NO_MATCHES_IN_{duplicate['reviewed_count']}_LOCAL_PEERS"}, 'author_visual_review': 'Actual whole front/reverse, ground eye and gallery eye, fountain and layered stairs viewed. Fidelity awaits independent review.', 'geometry_revisions': 2, 'differences_from_reference': ['Lava sealed in glass; safe shroomlight floor veins and no smoke/heat particles', 'Two actual stair runs added to make reference gallery accessible', 'Soul soil supports red roots in tag-independent native survival branch', '37x23x37 is practical perspective interpretation; outer cave scenery omitted'], 'viewer_instrumentation': 'In-memory response exposes existing renderer/camera only, original model/material/default pack and unchanged disk skill', 'world_ticks': 'NOT_RUN', 'heat_damage': 'NOT_RUN', 'interaction': 'NOT_RUN', 'deployment': 'NOT_PERFORMED', 'limitations': ['Static vanilla sampled layered body routes are not continuous player physics', 'No neighbor/fluid/fire/random ticks, weather/lighting propagation/spawning, rotations or gameplay tested', 'Only final NBT certified for static fluid/support; sections/crops not deployable', 'Upper gallery and stair fall hazards are not a gameplay safety certification', 'Exact category fingerprints do not prove semantic/global uniqueness', 'Explicit air clears terrain across full bounding box'], 'S6_09': 'LOCKED'})
    save(ROOT / 'manifest.json', {'id': metadata['id'], 'reference_count': 1, 'candidate_structure_count': 1, 'preserved_failed_drafts': 2, 'geometry_revisions': 2, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'nbt_sha256': fingerprint, 'size': metadata['size'], 'stage': 'READY_FOR_REVIEW_NOT_ACCEPTED', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'deployment': 'NOT_PERFORMED', 'files': [{'path': path.relative_to(ROOT).as_posix(), 'sha256': sha(path), 'bytes': path.stat().st_size} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name != 'manifest.json']})
    checklist = json.loads((ROOT / 'qa/checklist.json').read_text(encoding='utf-8'))
    checklist['checks']['lodestone'] = 'PASS_10_ACTUAL_BROWSER_SCREENSHOTS'
    save(ROOT / 'qa/checklist.json', checklist)
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    for record in manifest['files']:
        record['sha256'] = sha(ROOT / record['path'])
        record['bytes'] = (ROOT / record['path']).stat().st_size
    save(ROOT / 'manifest.json', manifest)
    for record in manifest['files']:
        assert sha(ROOT / record['path']) == record['sha256']
    print('S6-08 candidate SHA256:', fingerprint)
    print('Verified', len(manifest['files']), 'files; preserved previous counts:', [record['verified_file_count'] for record in preserved], '; S6-09 locked')


if __name__ == '__main__':
    finalize()
