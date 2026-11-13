import json
import sys

sys.dont_write_bytecode = True
from check import ROOT, REFERENCE, load, save, sha


def cells(data):
    return {tuple(record['pos']): {'state': data['palette'][record['state']], 'nbt': record.get('nbt')} for record in data['blocks']}


def finalize():
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    current_sha = sha(ROOT / 'room.nbt')
    assert current_sha == metadata['sha256']
    previous_root = ROOT.parent / '02-medium-room'
    previous_sha = sha(previous_root / 'room.nbt')
    assert previous_sha == 'e0ab5d51d4136502dc7f9991f2ced649849151016cf9063e376708fb97ca9c28'
    before_data = load(previous_root / 'room.nbt')
    after_data = load(ROOT / 'room.nbt')
    before = cells(before_data)
    after = cells(after_data)
    assert before.keys() == after.keys() and before_data['size'] == after_data['size']
    changes = [{'position': list(position), 'before': before[position], 'after': after[position]} for position in sorted(before) if before[position] != after[position]]
    assert changes and all(4 <= record['position'][0] <= 8 and 3 <= record['position'][1] <= 8 and 21 <= record['position'][2] <= 24 for record in changes)
    old_metadata = json.loads((previous_root / 'room.json').read_text(encoding='utf-8'))
    assert metadata['validation_targets'] == old_metadata['validation_targets']
    assert metadata['connectors'] == old_metadata['connectors']
    lava_before = {position for position, cell in before.items() if cell['state']['Name'] == 'minecraft:lava'}
    lava_after = {position for position, cell in after.items() if cell['state']['Name'] == 'minecraft:lava'}
    assert lava_before == lava_after
    for blockX in range(5, 8):
        for blockY in range(4, 7):
            for blockZ in range(21, 24):
                assert after[(blockX, blockY, blockZ)]['state']['Name'] == 'minecraft:air'
    assert all(after[(blockX, blockY, 24)]['state']['Name'] == 'minecraft:shroomlight' for blockX in range(5, 8) for blockY in (4, 5))
    assert after[(6, 3, 21)]['state']['Name'] == after[(6, 4, 21)]['state']['Name'] == 'minecraft:air'
    unchanged = []
    for folder in [previous_root, ROOT.parent / '01-large-chamber-r3']:
        manifest_path = folder / 'manifest.json'
        manifest = json.loads(manifest_path.read_text(encoding='utf-8'))
        for record in manifest['files']:
            assert sha(folder / record['path']) == record['sha256'], str(folder / record['path'])
        unchanged.append({'folder': str(folder), 'manifest_sha256': sha(manifest_path), 'verified_file_count': len(manifest['files']), 'status': 'ALL_MANIFEST_FILE_HASHES_UNCHANGED'})
    save(ROOT / 'qa/revision-diff.json', {'status': 'PASS_LOCALIZED_ONLY', 'source': str(previous_root / 'room.nbt'), 'source_sha256': previous_sha, 'revision_sha256': current_sha, 'changed_cells': len(changes), 'allowed_bounds_inclusive': [[4, 3, 21], [8, 8, 24]], 'outside_bounds_changes': 0, 'changes': changes, 'lava_coordinates_and_states_unchanged': True, 'table_two_crystals_storage_and_all_outside_cells_unchanged': True, 'connectors_unchanged': True, 'all_original_route_targets_unchanged': True, 'reference_count': 1})
    save(ROOT / 'qa/predecessor-lock.json', {'preserved_deliveries': unchanged, 'S6_03': 'LOCKED_NOT_STARTED', 'deployment': 'NOT_PERFORMED'})
    assert not any(path.name.startswith('03') for path in ROOT.parent.iterdir())
    for name in ['static', 'native', 'body-routes']:
        report = json.loads((ROOT / f'qa/{name}.json').read_text(encoding='utf-8'))
        assert report['status'] == 'PASS' and report['sha256'] == current_sha
    captures = json.loads((ROOT / 'lodestone/screenshots.json').read_text(encoding='utf-8'))
    assert captures['final_nbt_sha256'] == current_sha and not captures['page_errors'] and len(captures['screenshots']) == 8
    final_records = {tuple(record['pos']): record for record in after_data['blocks']}
    cutaway_path = ROOT / 'lodestone/inspection-cutaway.nbt'
    cutaway = load(cutaway_path)
    assert cutaway['palette'] == after_data['palette']
    assert all(record == final_records[tuple(record['pos'])] for record in cutaway['blocks'])
    assert captures['cutaway']['cutaway_nbt_sha256'] == sha(cutaway_path)
    detail_path = ROOT / 'lodestone/hearth-detail.nbt'
    detail = load(detail_path)
    detail_records = cells(detail)
    assert detail['size'] == [7, 8, 5]
    for position, cell in detail_records.items():
        original = (position[0] + 3, position[1] + 2, position[2] + 20)
        assert cell == after[original]
    for capture in captures['screenshots']:
        assert sha(ROOT / 'lodestone' / capture['file']) == capture['sha256']
        expected = current_sha if 'exterior' in capture['file'] else sha(detail_path) if 'closeup' in capture['file'] else sha(cutaway_path)
        assert capture['loaded_nbt_sha256'] == expected
    metadata.update({'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs')})
    save(ROOT / 'room.json', metadata)
    save(ROOT / 'assembly.json', {'module': metadata['id'], 'size': metadata['size'], 'world_origin': None, 'assembly_order': [{'file': 'room.nbt', 'relative_origin': [0, 0, 0], 'rotation': 'NONE'}], 'connection_anchors': metadata['connectors'], 'interior_safe_feet': metadata['interior'], 'acceptance': 'PENDING_INDEPENDENT_REREVIEW', 'next_image_may_start': False})
    save(ROOT / 'registration.pending.json', {'id': metadata['id'], 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'candidate': str(ROOT / 'room.nbt'), 'candidate_sha256': current_sha, 'supersedes_sha256': previous_sha, 'supersedes_preserved': True, 'revision_scope': 'furnace alcove geometry only', 'acceptance': 'PENDING_INDEPENDENT_REREVIEW', 'registry_updated': False, 'world_origin': None, 'deployment': 'NOT_PERFORMED', 'S6_03_unlocked': False, 'review_evidence': ['qa/revision-diff.json', 'qa/static.json', 'qa/native.json', 'qa/body-routes.json', 'qa/predecessor-lock.json', 'lodestone/screenshots.json']})
    save(ROOT / 'qa/checklist.json', {'status': 'AUTHOR_CHECKS_COMPLETE_NOT_ACCEPTED', 'sha256': current_sha, 'checks': {'localized_diff_only': 'PASS', 'thick_side_frame_and_lintel': 'PASS_GEOMETRY', 'three_by_three_recess_with_rear_glow_panel': 'PASS_GEOMETRY', 'hearth_platform': 'PASS_GEOMETRY', 'native_registry_template': 'PASS', 'original_nine_targets': 'PASS', 'half_grid_player_body_routes': 'PASS_SAMPLED_STATIC', 'native_fragile_support': 'PASS_7', 'native_fuel_buffer': 'PASS_9_BOOKSHELVES_MIN_HORIZONTAL_CHEBYSHEV_5', 'lava_seal': 'PASS_UNCHANGED_SOURCES', 'old_102_and_S6_01_46_manifest_hashes': 'PASS_UNCHANGED', 'actual_lodestone_full_exterior_interior_detail': 'PASS_8_SCREENSHOTS'}, 'visual_self_review': 'Thick front jambs, three-block recessed cavity, six-block warm back panel and raised hearth are visible in actual Lodestone detail and whole-room screenshots. Not independent acceptance.', 'safe_warm_substitute': 'shroomlight; no new lava, fire, campfire, furnace function or fuel', 'world_ticks': 'NOT_RUN', 'limitations': ['Collision is static immutable vanilla shapes with half-grid and 0.1-block edge samples, not continuous physics', 'No Forge placement, neighbor updates, random/fluid/fire ticks, rotations or gameplay', 'Glass containment is not proof of perpetual fire safety', 'Local exact geometry fingerprint does not certify near-duplicate uniqueness'], 'S6_03': 'LOCKED'})
    save(ROOT / 'manifest.json', {'id': metadata['id'], 'reference_count': 1, 'candidate_structure_count': 1, 'nbt_sha256': current_sha, 'previous_sha256': previous_sha, 'size': metadata['size'], 'stage': 'READY_FOR_REREVIEW_NOT_ACCEPTED', 'acceptance': 'PENDING_INDEPENDENT_REREVIEW', 'next_reference_started': False, 'deployment': 'NOT_PERFORMED', 'files': [{'path': path.relative_to(ROOT).as_posix(), 'sha256': sha(path), 'bytes': path.stat().st_size} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name != 'manifest.json']})
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    for record in manifest['files']:
        assert sha(ROOT / record['path']) == record['sha256']
    print('New NBT SHA256:', current_sha)
    print('Localized changes:', len(changes), '; verified files:', len(manifest['files']), '; preserved predecessor counts:', [record['verified_file_count'] for record in unchanged])


if __name__ == '__main__':
    finalize()
