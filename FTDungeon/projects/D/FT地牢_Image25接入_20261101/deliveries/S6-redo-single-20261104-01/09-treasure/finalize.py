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
    assert metadata['sha256'] == fingerprint and metadata['id'] == 'S6-REDO-09'
    predecessor = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S6-08-independent')
    acceptance_path = predecessor / 'independent-details.json'
    acceptance = json.loads(acceptance_path.read_text(encoding='utf-8'))
    predecessor_sha = sha(ROOT.parent / '08-courtyard/room.nbt')
    assert acceptance['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    assert acceptance['sha256'] == predecessor_sha == sha(predecessor / 'room.nbt') == 'f8522538a1ea4bb2b5e0c7eeaae80e928908c3fb6ac1c063d60a6415216fb5c9'
    preserved = []
    for folder_name in ['01-large-chamber-r3', '02-medium-room', '02-medium-room-r2', '03-small-room', '04-great-hall', '05-corridor', '06-corner', '07-entrance', '08-courtyard']:
        folder = ROOT.parent / folder_name
        manifest = json.loads((folder / 'manifest.json').read_text(encoding='utf-8'))
        for record in manifest['files']:
            assert sha(folder / record['path']) == record['sha256'], str(folder / record['path'])
        preserved.append({'folder': str(folder), 'verified_file_count': len(manifest['files']), 'manifest_sha256': sha(folder / 'manifest.json'), 'status': 'ALL_HASHES_UNCHANGED'})
    assert not any(folder.name.startswith('10') for folder in ROOT.parent.iterdir())
    save(ROOT / 'qa/predecessor-lock.json', {'S6_08_acceptance': str(acceptance_path), 'acceptance_sha256': sha(acceptance_path), 'S6_08_frozen_nbt': str(predecessor / 'room.nbt'), 'frozen_nbt_sha256': predecessor_sha, 'preserved': preserved, 'S6_10': 'LOCKED_NOT_STARTED', 'deployment': 'NOT_PERFORMED'})
    reports = {}
    for name in ['static', 'native', 'body-routes', 'containers']:
        report = json.loads((ROOT / f'qa/{name}.json').read_text(encoding='utf-8'))
        assert report['status'] == 'PASS' and report['sha256'] == fingerprint
        reports[name] = report
    assert len(reports['body-routes']['targets']) == len(metadata['validation_targets']) == 18
    assert reports['body-routes']['half_height_stair_points'] > 0
    assert len(reports['native']['fragile_survival']) == 6
    assert all(record['odds'] == 0 for record in reports['native']['native_fire_odds'])
    assert len(reports['containers']['containers']) == 3
    for container in metadata['containers']:
        assert container['approach'] in metadata['validation_targets']
    final = load(ROOT / 'room.nbt')
    final_cells = cells(final)
    captures = json.loads((ROOT / 'lodestone/screenshots.json').read_text(encoding='utf-8'))
    assert captures['final_nbt_sha256'] == fingerprint and not captures['page_errors'] and len(captures['screenshots']) == 10
    assert captures['disk_skill_modified'] is False
    cutaway_path = ROOT / 'lodestone/inspection-cutaway.nbt'
    cutaway = load(cutaway_path)
    assert cutaway['palette'] == final['palette'] and cutaway['size'] == final['size']
    expected_cutaway = [record for record in final['blocks'] if not (record['pos'][1] >= 16 or (record['pos'][1] >= 5 and (record['pos'][0] <= 2 or record['pos'][2] <= 10)))]
    assert cutaway['blocks'] == expected_cutaway
    assert captures['cutaway']['cutaway_nbt_sha256'] == sha(cutaway_path)
    detail_path = ROOT / 'lodestone/treasure-dais-detail.nbt'
    detail = load(detail_path)
    provenance = json.loads((ROOT / 'lodestone/detail-provenance.json').read_text(encoding='utf-8'))
    assert provenance['final_nbt_sha256'] == fingerprint and provenance['detail_sha256'] == sha(detail_path)
    assert detail['size'] == [15, 12, 13] and len(detail['blocks']) == 2340
    for position, cell in cells(detail).items():
        assert cell == final_cells[(position[0] + 9, position[1] + 4, position[2] + 19)]
    assert sum(bool(record.get('nbt')) for record in detail['blocks']) == 1
    for capture in captures['screenshots']:
        assert capture['sha256'] == sha(ROOT / 'lodestone' / capture['file'])
        port = int(capture['url'].split(':')[2].split('/')[0])
        assert capture['loaded_nbt_sha256'] == {5471: fingerprint, 5472: sha(cutaway_path), 5473: sha(detail_path)}[port]
    duplicate = json.loads((ROOT / 'qa/duplicates.json').read_text(encoding='utf-8'))
    assert not duplicate['matches']
    metadata.update({'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs')})
    save(ROOT / 'room.json', metadata)
    save(ROOT / 'assembly.json', {'module': metadata['id'], 'size': metadata['size'], 'local_origin': [0, 0, 0], 'world_origin': None, 'assembly_order': [{'file': 'room.nbt', 'relative_origin': [0, 0, 0], 'rotation': 'NONE'}], 'connection_anchors': metadata['connectors'], 'interior_safe_feet': metadata['interior'], 'front_feet_y': 3, 'room_feet_y': 5, 'dais_feet_y': 7, 'containers_and_approaches': metadata['containers'], 'container_state': 'THREE_EMPTY_SINGLE_CHESTS_NO_LOOT_TABLE', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_image_may_start': False})
    save(ROOT / 'registration.pending.json', {'id': metadata['id'], 'group': 'S6-volcanic-cavern', 'reference_number': 9, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'candidate': str(ROOT / 'room.nbt'), 'candidate_sha256': fingerprint, 'size': metadata['size'], 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'registry_updated': False, 'world_origin': None, 'deployment': 'NOT_PERFORMED', 'S6_10_unlocked': False, 'review_evidence': ['analysis.md', 'assembly.json', 'qa/static.json', 'qa/native.json', 'qa/body-routes.json', 'qa/containers.json', 'qa/predecessor-lock.json', 'qa/duplicates.json', 'lodestone/screenshots.json']})
    save(ROOT / 'qa/checklist.json', {'status': 'AUTHOR_CHECKS_COMPLETE_NOT_ACCEPTED', 'sha256': fingerprint, 'reference_sha256': sha(REFERENCE), 'checks': {'volume': 'PASS_20790', 'composition': 'REAL_CHEST_DAIS_TWIN_STONE_GUARDIANS_PAIRED_POOLS_GOLD_EMBLEM', 'port_clearance': 'PASS_NORTH_WEST_EAST_3x5_DEPTH3', 'native_registry_template': 'PASS_UNMODIFIED_SHARED_VERIFIER', 'native_and_body_targets': 18, 'body_grid_points': reports['body-routes']['reachable_grid_points'], 'half_step_grid_points': reports['body-routes']['half_height_stair_points'], 'native_selected_support': 'PASS_6_LANTERNS', 'native_fire_odds': 'ALL_ZERO_ACTUAL_PALETTE', 'lava': 'PASS_100_LEVEL_ZERO_SIX_NEIGHBOR_ENCLOSED', 'solids_to_base': 'PASS', 'stair_blocks': 28, 'roof_columns': 891, 'containers': 'PASS_3_ACTUAL_CHEST_ENTITIES_27_EMPTY_SLOTS_EACH', 'container_approach': 'PASS_3_FRONT_TARGETS', 'container_lid': 'PASS_2_AIR_ABOVE_AND_NATIVE_BOX_ENVELOPE', 'spawned_entities': 'NONE', 'lodestone': 'PASS_10_ACTUAL_BROWSER_SCREENSHOTS', 'cutaway_crop_provenance': 'PASS_EXACT_RECORD_DELETION_OR_POSITION_TRANSLATION_INCLUDING_CHEST_NBT', 'previous_files': 'ALL_MANIFEST_HASHES_UNCHANGED', 'duplicate_exact_geometry': f"NO_MATCHES_IN_{duplicate['reviewed_count']}_LOCAL_PEERS"}, 'author_visual_review': 'Actual full source axis/reverse, all three chests, complete envelope and guardian dais source crop reviewed. Reference fidelity awaits independent review.', 'geometry_revisions': 0, 'differences_from_reference': ['Actual vanilla single wooden chest material; no fake gold chest model', 'Lava pools/falls glass sealed; no exterior lava river or fire/smoke; shroomlight hot-vein approximation', 'Solid terracotta side display and red hanging ornaments, no decorative pot/banner entities', '33x18x35 perspective interpretation with full final roof'], 'viewer_instrumentation': 'In-memory response exposes existing renderer/camera only, original model/material/default pack and unchanged disk skill', 'world_ticks': 'NOT_RUN', 'heat_fire_damage': 'NOT_RUN', 'container_interaction': 'NOT_RUN', 'deployment': 'NOT_PERFORMED', 'limitations': ['Static vanilla sampled body routes are not continuous player physics', 'No neighbor/fluid/fire/random ticks, lighting/spawning, rotations or gameplay tested', 'Lid envelope is static clearance, not animated lid/menu/player/cat/world obstruction test', 'Containers empty; loot acquisition and gameplay not configured or certified', 'Only final NBT static boundary/support checked; crop chest NBT retained untranslated internally and derivative not deployable', 'Exact category fingerprints do not prove semantic/global uniqueness', 'Explicit air clears terrain across full bounding box'], 'S6_10': 'LOCKED'})
    save(ROOT / 'manifest.json', {'id': metadata['id'], 'reference_count': 1, 'candidate_structure_count': 1, 'geometry_revisions': 0, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'nbt_sha256': fingerprint, 'size': metadata['size'], 'stage': 'READY_FOR_REVIEW_NOT_ACCEPTED', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'deployment': 'NOT_PERFORMED', 'files': [{'path': path.relative_to(ROOT).as_posix(), 'sha256': sha(path), 'bytes': path.stat().st_size} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name != 'manifest.json']})
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    for record in manifest['files']:
        assert sha(ROOT / record['path']) == record['sha256']
    print('S6-09 candidate SHA256:', fingerprint)
    print('Verified', len(manifest['files']), 'files; preserved previous counts:', [record['verified_file_count'] for record in preserved], '; S6-10 locked')


if __name__ == '__main__':
    finalize()
