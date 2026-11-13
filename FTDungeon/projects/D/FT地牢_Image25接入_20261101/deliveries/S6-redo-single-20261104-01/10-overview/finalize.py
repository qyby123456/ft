import copy
import json
import sys
from pathlib import Path

sys.dont_write_bytecode = True
from check import ROOT, REFERENCE, load, save, sha


def read(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def cells(data):
    return {tuple(record['pos']): {'state': data['palette'][record['state']], 'nbt': record.get('nbt')} for record in data['blocks']}


def finalize():
    fingerprint = sha(ROOT / 'room.nbt')
    metadata = read('room.json')
    assert metadata['sha256'] == fingerprint and metadata['id'] == 'S6-REDO-10'
    assert metadata['reference_sha256'] == sha(REFERENCE)
    previous_root = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S6-09-independent')
    previous_report = previous_root / 'independent-details.json'
    independent = json.loads(previous_report.read_text(encoding='utf-8'))
    assert independent['status'] == 'PASS' and independent['body_targets'] == 22
    previous_sha = '131bcf998861c665f47808445c12cf6d09c2e79f4a94b7041d196c557bfc89d0'
    assert sha(previous_root / 'room.nbt') == sha(ROOT.parent / '09-treasure/room.nbt') == previous_sha
    preserved = []
    for folder_name in ['01-large-chamber-r3', '02-medium-room', '02-medium-room-r2', '03-small-room', '04-great-hall', '05-corridor', '06-corner', '07-entrance', '08-courtyard', '09-treasure']:
        folder = ROOT.parent / folder_name
        manifest = json.loads((folder / 'manifest.json').read_text(encoding='utf-8'))
        for record in manifest['files']:
            assert sha(folder / record['path']) == record['sha256'], str(folder / record['path'])
        preserved.append({'folder': str(folder), 'verified_file_count': len(manifest['files']), 'manifest_sha256': sha(folder / 'manifest.json'), 'status': 'ALL_HASHES_UNCHANGED'})
    assert not any(folder.name.startswith('11') for folder in ROOT.parent.iterdir())
    save(ROOT / 'qa/predecessor-lock.json', {'S6_09_independent_details': str(previous_report), 'details_sha256': sha(previous_report), 'report_status': independent['status'], 'user_reported_sequential_acceptance': 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION', 'frozen_nbt_sha256': previous_sha, 'preserved': preserved, 'S6_11': 'LOCKED_NOT_STARTED'})
    reports = {name: read(f'qa/{name}.json') for name in ['static', 'native', 'body-routes', 'containers', 'reassembly']}
    assert all(report['status'] == 'PASS' and report['sha256'] == fingerprint for report in reports.values())
    assert len(reports['body-routes']['targets']) == len(metadata['validation_targets']) == 33
    assert len(reports['native']['fragile_survival']) == 12
    assert all(record['odds'] == 0 for record in reports['native']['native_fire_odds'])
    assert len(reports['containers']['containers']) == 3
    assert reports['reassembly']['verified_coordinates'] == 85410
    source = load(ROOT / 'room.nbt')
    source_cells = cells(source)
    reconstructed = {}
    placement = read('shards/placement.json')
    assert placement['final_nbt_sha256'] == fingerprint and len(placement['parts']) == 4
    for part in placement['parts']:
        assert sha(ROOT / part['file']) == part['sha256']
        data = load(ROOT / part['file'])
        assert max(data['size']) <= 48 and data['DataVersion'] == source['DataVersion']
        for point, cell in cells(data).items():
            restored = tuple(point[axis] + part['origin'][axis] for axis in range(3))
            payload = copy.deepcopy(cell['nbt'])
            if payload:
                for axis, key in enumerate(['x', 'y', 'z']):
                    if key in payload:
                        payload[key] += part['origin'][axis]
            assert restored not in reconstructed
            reconstructed[restored] = {'state': cell['state'], 'nbt': payload}
    assert reconstructed == source_cells
    cut_path = ROOT / 'lodestone/inspection-cutaway.nbt'
    cut = load(cut_path)
    expected_cut = [record for record in source['blocks'] if not (record['pos'][1] >= 8 or (record['pos'][1] >= 3 and (record['pos'][0] == 0 or record['pos'][2] in [14, 24, 42, 48, 50])))]
    assert cut['blocks'] == expected_cut and cut['palette'] == source['palette'] and cut['size'] == source['size']
    detail_path = ROOT / 'lodestone/treasure-detail.nbt'
    detail = load(detail_path)
    assert detail['size'] == [19, 14, 23] and len(detail['blocks']) == 6118
    for point, cell in cells(detail).items():
        assert cell == source_cells[(point[0] + 46, point[1], point[2] + 50)]
    assert sum(bool(record.get('nbt')) for record in detail['blocks']) == 1
    captures = read('lodestone/screenshots.json')
    assert captures['final_nbt_sha256'] == fingerprint and not captures['page_errors']
    assert captures['disk_skill_modified'] is False and len(captures['screenshots']) == 17
    assert captures['cutaway']['cutaway_nbt_sha256'] == sha(cut_path)
    detail_provenance = read('lodestone/detail-provenance.json')
    assert detail_provenance['final_nbt_sha256'] == fingerprint and detail_provenance['detail_sha256'] == sha(detail_path)
    for capture in captures['screenshots']:
        assert capture['sha256'] == sha(ROOT / 'lodestone' / capture['file'])
        port = int(capture['url'].split(':')[2].split('/')[0])
        assert capture['loaded_nbt_sha256'] == {5481: fingerprint, 5482: sha(cut_path), 5483: sha(detail_path)}[port]
    revisions = []
    for draft_name in ['draft-r0', 'draft-r1']:
        draft = ROOT / 'qa' / draft_name
        prior = cells(load(draft / 'room.nbt'))
        differences = [{'position': list(point), 'before': prior[point], 'after': source_cells[point]} for point in prior if prior[point] != source_cells[point]]
        assert differences and all(record['after']['state']['Name'] == 'minecraft:air' for record in differences)
        revisions.append({'draft': draft_name, 'sha256': sha(draft / 'room.nbt'), 'reason_file': str((draft / 'FAILURE.md').relative_to(ROOT)), 'changed_coordinates': len(differences), 'changes': differences})
    save(ROOT / 'qa/revision-diff.json', {'final_nbt_sha256': fingerprint, 'retained_failed_candidates': revisions, 'scope': 'Exact source state and BE comparison; only passage clearance additions relative to retained drafts'})
    duplicate = read('qa/duplicates.json')
    assert not duplicate['matches']
    metadata.update({'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs')})
    save(ROOT / 'room.json', metadata)
    save(ROOT / 'assembly.json', {'module': metadata['id'], 'source_size': metadata['size'], 'source_sha256': fingerprint, 'placement_manifest': 'shards/placement.json', 'parts': placement['parts'], 'air_policy': placement['air_policy'], 'connectors': metadata['connectors'], 'rotation': 'NONE', 'world_origin': None, 'runtime': 'NOT_RUN', 'deployment': 'NOT_PERFORMED'})
    save(ROOT / 'validation.json', {'id': metadata['id'], 'sha256': fingerprint, 'status': 'READY_FOR_REVIEW_NOT_ACCEPTED', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'checks': {'native_template_load_save': 'PASS', 'native_route_targets': 33, 'body_route_targets': 33, 'body_reachable_grid_points': reports['body-routes']['reachable_grid_points'], 'half_height_stair_points': reports['body-routes']['half_height_stair_points'], 'lantern_survival': 12, 'native_fire_odds': 'ALL_ZERO', 'sealed_source_lava': 179, 'empty_actual_chests': 3, 'major_regions': 8, 'exact_reassembled_coordinates': 85410, 'parts_all_dimensions_le_48': True, 'lodestone_actual_screenshots': 17, 'exact_derivative_provenance': True, 'previous_manifest_files_unchanged': preserved, 'exact_classified_geometry_peer_count': duplicate['reviewed_count']}, 'visual_review': 'Full source roof envelope, inside routes/regions and source cutaway inspected; fidelity remains subject to independent review.', 'geometry_revisions': 2, 'reference_adaptations': ['65x18x73 block interpretation, solid region roofs except open central courtyard', 'Glass-enclosed source lava replaces exposed lava surfaces; shroomlight replaces fire and smoke', 'Three actual vanilla wooden chests, empty inventories, no loot configured', 'Open entry arch without door leaves; no closed-door state substitute'], 'runtime': 'NOT_RUN', 'limitations': ['Discrete vanilla native body collision sampling is not continuous player physics', 'No world ticks, fluid/fire/neighbor updates, animated lid/menu interaction, spawning, rotation or gameplay tests', 'Mathematical shard equivalence does not certify sequential live-tick placement; seams may temporarily expose lava', 'Explicit air must be placed and clears the entire bounding box', 'Inspection crops remove roofs or boundaries and must not be deployed', 'Exact classified geometry fingerprints do not establish semantic or global uniqueness'], 'S6_11': 'LOCKED_NOT_STARTED', 'deployment': 'NOT_PERFORMED'})
    save(ROOT / 'manifest.json', {'id': metadata['id'], 'reference_count': 1, 'candidate_structure_count': 1, 'retained_failed_candidate_count': 2, 'placement_part_count': 4, 'geometry_revisions': 2, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'nbt_sha256': fingerprint, 'size': metadata['size'], 'stage': 'READY_FOR_REVIEW_NOT_ACCEPTED', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'deployment': 'NOT_PERFORMED', 'files': [{'path': path.relative_to(ROOT).as_posix(), 'sha256': sha(path), 'bytes': path.stat().st_size} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name != 'manifest.json']})
    manifest = read('manifest.json')
    assert all(sha(ROOT / record['path']) == record['sha256'] for record in manifest['files'])
    print('S6-10 SHA256:', fingerprint)
    print('Verified files:', len(manifest['files']), '; preserved counts:', [record['verified_file_count'] for record in preserved])
    print('33 body targets; 4 exact parts; S6-11 locked; ready for independent review')


if __name__ == '__main__':
    finalize()
