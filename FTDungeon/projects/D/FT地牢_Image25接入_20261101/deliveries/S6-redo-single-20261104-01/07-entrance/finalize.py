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
    assert metadata['sha256'] == fingerprint and metadata['id'] == 'S6-REDO-07'
    predecessor = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S6-06-independent')
    acceptance_path = predecessor / 'independent-details.json'
    acceptance = json.loads(acceptance_path.read_text(encoding='utf-8'))
    predecessor_sha = sha(ROOT.parent / '06-corner/room.nbt')
    assert acceptance['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    assert acceptance['sha256'] == predecessor_sha == sha(predecessor / 'room.nbt') == '4962b0aba909ba3f6897bd6288edb46e20995e91472d27f1d90f0113582330fd'
    preserved = []
    for folder_name in ['01-large-chamber-r3', '02-medium-room', '02-medium-room-r2', '03-small-room', '04-great-hall', '05-corridor', '06-corner']:
        folder = ROOT.parent / folder_name
        manifest = json.loads((folder / 'manifest.json').read_text(encoding='utf-8'))
        for record in manifest['files']:
            assert sha(folder / record['path']) == record['sha256'], str(folder / record['path'])
        preserved.append({'folder': str(folder), 'verified_file_count': len(manifest['files']), 'manifest_sha256': sha(folder / 'manifest.json'), 'status': 'ALL_HASHES_UNCHANGED'})
    assert not any(folder.name.startswith('08') for folder in ROOT.parent.iterdir())
    save(ROOT / 'qa/predecessor-lock.json', {'S6_06_acceptance': str(acceptance_path), 'acceptance_sha256': sha(acceptance_path), 'S6_06_frozen_nbt': str(predecessor / 'room.nbt'), 'frozen_nbt_sha256': predecessor_sha, 'preserved': preserved, 'S6_08': 'LOCKED_NOT_STARTED', 'deployment': 'NOT_PERFORMED'})
    reports = {}
    for name in ['static', 'native', 'body-routes']:
        report = json.loads((ROOT / f'qa/{name}.json').read_text(encoding='utf-8'))
        assert report['status'] == 'PASS' and report['sha256'] == fingerprint
        reports[name] = report
    assert len(reports['body-routes']['targets']) == len(metadata['validation_targets']) == 16
    assert reports['body-routes']['half_height_stair_points'] > 0
    assert len(reports['native']['fragile_survival']) == 10
    assert sum(record['block'].endswith('_door') for record in reports['native']['fragile_survival']) == 4
    assert all(record['odds'] == 0 for record in reports['native']['native_fire_odds'])
    correction = json.loads((ROOT / 'qa/checker-correction.json').read_text(encoding='utf-8'))
    assert correction['nbt_sha256_before_and_after'] == fingerprint and correction['shared_source_modified'] is False
    captures = json.loads((ROOT / 'lodestone/screenshots.json').read_text(encoding='utf-8'))
    assert captures['final_nbt_sha256'] == fingerprint and not captures['page_errors'] and len(captures['screenshots']) == 9
    assert captures['disk_skill_modified'] is False
    final = load(ROOT / 'room.nbt')
    final_cells = cells(final)
    cutaway_path = ROOT / 'lodestone/inspection-cutaway.nbt'
    cutaway = load(cutaway_path)
    assert cutaway['palette'] == final['palette'] and cutaway['size'] == final['size']
    expected_cutaway = [record for record in final['blocks'] if not (record['pos'][1] >= 10 or (record['pos'][1] >= 5 and (record['pos'][0] <= 2 or record['pos'][2] <= 10)))]
    assert cutaway['blocks'] == expected_cutaway
    assert captures['cutaway']['cutaway_nbt_sha256'] == sha(cutaway_path)
    detail_path = ROOT / 'lodestone/gate-stairs-detail.nbt'
    detail = load(detail_path)
    provenance = json.loads((ROOT / 'lodestone/detail-provenance.json').read_text(encoding='utf-8'))
    assert provenance['final_nbt_sha256'] == fingerprint and provenance['detail_sha256'] == sha(detail_path)
    assert detail['size'] == [9, 10, 8] and len(detail['blocks']) == 720
    for position, cell in cells(detail).items():
        assert cell == final_cells[(position[0] + 9, position[1] + 2, position[2] + 4)]
    actual_models = {5451: fingerprint, 5452: sha(cutaway_path), 5453: sha(detail_path)}
    for capture in captures['screenshots']:
        assert capture['sha256'] == sha(ROOT / 'lodestone' / capture['file'])
        port = int(capture['url'].split(':')[2].split('/')[0])
        assert capture['loaded_nbt_sha256'] == actual_models[port]
    duplicate = json.loads((ROOT / 'qa/duplicates.json').read_text(encoding='utf-8'))
    assert not duplicate['matches']
    metadata.update({'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs')})
    save(ROOT / 'room.json', metadata)
    save(ROOT / 'assembly.json', {'module': metadata['id'], 'size': metadata['size'], 'local_origin': [0, 0, 0], 'world_origin': None, 'assembly_order': [{'file': 'room.nbt', 'relative_origin': [0, 0, 0], 'rotation': 'NONE'}], 'connection_anchors': metadata['connectors'], 'interior_safe_feet': metadata['interior'], 'front_to_back_feet_difference': 2, 'stair_rises': metadata['stair_rises'], 'gate_state': 'PAIRED_PHYSICAL_DOORS_OPEN', 'front_walkway_width': 7, 'gate_clear_width': 2, 'rear_passage_width': 5, 'connector_widths': 'declared3-wide minimum subregions of broader approach/rear passage', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_image_may_start': False})
    save(ROOT / 'registration.pending.json', {'id': metadata['id'], 'group': 'S6-volcanic-cavern', 'reference_number': 7, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'candidate': str(ROOT / 'room.nbt'), 'candidate_sha256': fingerprint, 'size': metadata['size'], 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'registry_updated': False, 'world_origin': None, 'deployment': 'NOT_PERFORMED', 'S6_08_unlocked': False, 'review_evidence': ['analysis.md', 'assembly.json', 'qa/static.json', 'qa/native.json', 'qa/body-routes.json', 'qa/checker-correction.json', 'qa/predecessor-lock.json', 'qa/duplicates.json', 'lodestone/screenshots.json']})
    save(ROOT / 'qa/checklist.json', {'status': 'AUTHOR_CHECKS_COMPLETE_NOT_ACCEPTED', 'sha256': fingerprint, 'reference_sha256': sha(REFERENCE), 'checks': {'volume': 'PASS_14256', 'true_entrance': 'PAIRED_DOOR_AND_TWO_STAIR_RISES', 'north_port_feet': [13, 3, 0], 'south_port_feet': [13, 5, 32], 'port_clearance': 'PASS_3x5_DEPTH2_MINIMUM', 'native_registry_template': 'PASS_LOCAL_OPEN_DOOR_CELL_SHAPE_ADAPTATION_DOCUMENTED', 'native_and_body_targets': 16, 'body_grid_points': reports['body-routes']['reachable_grid_points'], 'half_step_grid_points': reports['body-routes']['half_height_stair_points'], 'native_door_halves_support': 'PASS_4', 'native_lantern_support': 'PASS_6', 'native_fire_odds': 'ALL_ZERO_INCLUDING_ACTUAL_DOOR_STATES', 'lava': 'PASS_48_LEVEL_ZERO_SIX_NEIGHBOR_ENCLOSED', 'solids_to_base': 'PASS', 'court': 'OPEN_SKY_INTENTIONAL', 'rear_roof_columns': 49, 'entities_and_containers': 'NONE', 'lodestone': 'PASS_9_ACTUAL_BROWSER_SCREENSHOTS', 'full_final_nbt_views': 'EXTERIOR_FRONT_REAR_APPROACH_COURT_REVERSE_NICHE', 'cutaway_crop_provenance': 'PASS_EXACT_REMOVAL_OR_TRANSLATION', 'previous_files': 'ALL_MANIFEST_HASHES_UNCHANGED', 'duplicate_exact_geometry': f"NO_MATCHES_IN_{duplicate['reviewed_count']}_LOCAL_PEERS"}, 'author_visual_review': 'Viewed actual full module from front, courtyard rear arch with two alcoves, guard closeup, gate/stair source crop and inspection section. Reference fidelity awaits independent review.', 'differences_from_reference': ['Physical paired doors default OPEN, interaction not tested', 'Exterior lava river/cave scenery omitted from connector module', 'Court lava slots and wall falls sealed with orange glass; hot floor/brazier approximated by safe shroomlight', 'Sandstone notice panel and terracotta supply shapes have no text/map/container/entity function', '27x16x33 is a practical perspective interpretation; court intentionally open to sky'], 'viewer_instrumentation': 'In-memory response exposes existing renderer/camera only; no block/material/pack changes or disk skill edits', 'geometry_revisions': 0, 'world_ticks': 'NOT_RUN', 'heat_damage': 'NOT_RUN', 'door_interaction': 'NOT_RUN', 'deployment': 'NOT_PERFORMED', 'limitations': ['Immutable static vanilla checks and sampled stair/body routes are not continuous player physics or Forge placement', 'No neighbor updates, fluid/fire/random ticks, lighting propagation, rotations or gameplay interaction tested', 'Cutaway removes front door and some lava-window cells; crops/sections must not be deployed or used to certify boundaries/support', 'Open court weather, lighting and spawning not certified', 'Exact category fingerprints do not prove semantic/global uniqueness', 'Explicit air clears terrain across full bounding box'], 'S6_08': 'LOCKED'})
    save(ROOT / 'manifest.json', {'id': metadata['id'], 'reference_count': 1, 'candidate_structure_count': 1, 'geometry_revisions': 0, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'nbt_sha256': fingerprint, 'size': metadata['size'], 'stage': 'READY_FOR_REVIEW_NOT_ACCEPTED', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'deployment': 'NOT_PERFORMED', 'files': [{'path': path.relative_to(ROOT).as_posix(), 'sha256': sha(path), 'bytes': path.stat().st_size} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name != 'manifest.json']})
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    for record in manifest['files']:
        assert sha(ROOT / record['path']) == record['sha256']
    print('S6-07 candidate SHA256:', fingerprint)
    print('Verified', len(manifest['files']), 'files; preserved previous counts:', [record['verified_file_count'] for record in preserved], '; S6-08 locked')


if __name__ == '__main__':
    finalize()
