import json
import shutil

from PIL import Image
from deliver import ROOT, ROOM, ROOM_ID, REFERENCE, digest, save

THREAD = '01a1a64f-3e44-7c72-ab6d-8d3efa9147a3'


def read(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def assembly():
    metadata = read('incoming/S8R_IMG01/room.json')
    save(ROOT / 'assembly.json', {'size': metadata['size'], 'single_chunk_constraint': False, 'origin': [0, 0, 0], 'axes': '+X east, +Y up, +Z south', 'modules': [{'id': ROOM_ID, 'nbt': str(ROOM / 'room.nbt'), 'sha256': digest(ROOM / 'room.nbt'), 'size': metadata['size'], 'offset': [0, 0, 0], 'rotation_pivot': [17, 0, 17], 'placement_order': 1, 'dependencies': [], 'connection_anchors': metadata['connections']}], 'split': False, 'split_reason': '35x21x35 is below Java structure limits; no chunk-size cap imposed', 'interface_elevation_warning': 'north entrance feet Y1; all other ports feet Y3; never connect equal world origins assuming equal port heights', 'rotation_clockwise': '[x,y,z] -> [34-z,y,x]; rotate block states and all anchors together; north->east->south->west', 'bounds_policy': 'Explicit air present. Check the entire 35x21x35 world volume, entities, fluids and borders; reject any conflict rather than clearing existing content.', 'world_origin': None, 'runtime_slot': None, 'deployment': 'NOT_DEPLOYED', 'world_placement_and_400tick_stability': 'NOT_RUN'})


def main():
    metadata = read('incoming/S8R_IMG01/room.json')
    native = read('qa/native-review.json')
    static = read('qa/static.json')
    duplicates = read('qa/takeover-material-independent.json')
    browser = read('qa/takeover-lodestone.json')
    provenance = read('qa/takeover-preview-provenance.json')
    actual = digest(ROOM / 'room.nbt')
    assert actual == metadata['sha256'] == native['rooms'][0]['sha256'] == static['nbt_sha256'] == duplicates['candidate_sha256'] == provenance['final_nbt_sha256']
    assert metadata['size'] == [35, 21, 35] and metadata['reference_sha256'] == digest(REFERENCE)
    assert metadata['recipe_sha256'] == digest(ROOT / 'recipe-r2.mjs')
    assert all(item['status'] == 'PASS' for item in (native, static, browser, provenance))
    rotations = native['rooms'][0]['rotated_anchors_states_and_path_checks']
    assert [item['degrees_clockwise'] for item in rotations] == [0, 90, 180, 270]
    assert all(item['status'] == 'PASS' and item['checked_targets'] == 9 for item in rotations)
    assert not duplicates['exact_matches'] and not duplicates['high_interior_matches_at_0_9'] and not duplicates['errors']
    snapshot = ROOT / 'views/single-01-r2' / ROOM_ID
    assert digest(snapshot / 'room.nbt') == actual
    images = [snapshot / f'{mode}-{direction}.png' for mode in ('interior', 'exterior') for direction in ('north', 'south', 'east', 'west')]
    images += [ROOT / item['screenshot'] for item in browser['records']]
    for target in images:
        with Image.open(target) as image:
            image.verify()
    complete = next(item for item in browser['records'] if item['label'] == 'complete')
    assert complete['served_sha256'] == actual
    shutil.copyfile(ROOT / 'qa/takeover-lodestone-cutaway-north.png', ROOM / 'preview.png')
    shutil.copyfile(ROOT / 'qa/takeover-lodestone-complete.png', ROOT / 'lodestone-preview.png')
    save(ROOT / 'qa/lodestone-browser.json', {'status': 'PASS', 'nbt_sha256': actual, 'complete': complete, 'inspection_views': browser['records'][1:], 'inspection_provenance': provenance})
    save(ROOT / 'qa/duplicate-selfcheck.json', duplicates)
    visual = {'status': 'PRODUCER_REVIEW_PASS_WITH_DECLARED_LIMITATIONS', 'independent_auditor': False, 'nbt_sha256': actual, 'reference_sha256': digest(REFERENCE), 'reference_index': 1, 'evidence': ['qa/takeover-lodestone-cutaway-north.png', 'views/single-01-r2/S8R_IMG01/interior-four-views.png', 'views/single-01-r2/S8R_IMG01/exterior-four-views.png', 'qa/takeover-geometry-comparison.png'], 'observed_preserved_features': ['single central crystal nucleus on three-tier round dais', 'white ascent and wider amethyst-calcite floor ring with left/right bypass', 'four banded thick columns and connected upper beams', 'three lateral/back purple arch branches plus recessed north stair entrance', 'peripheral calcite crystal niches and supported hanging crystals/lanterns'], 'scale_basis': '35x21x35 is an explained modeling estimate, not a block-exact image measurement; main floor feet Y3, roof Y20, nucleus tip Y14', 'declared_differences': ['Vanilla opaque cubic amethyst cannot reproduce translucent concept-art prisms or purple bloom.', 'Final complete shell is deliberately retained; reference-like open views are explicitly labeled inspection cuts.', 'Cut planes can remove support beams from inspection views; final NBT support is checked separately.', 'Software four-view beams partly occlude the center; the closer real Lodestone north cutaway is the primary interior evidence.', 'All four Lodestone inspection cameras share the same northwest cut planes; far-side cameras can show retained walls, not an unobstructed independent interior cut.'], 'topology_review': 'Central ring plus four columns is required by this particular reference. Closest 0.6898 interior solid IoU is this same image old checkpoint, excluded from candidates. Other read snapshots have substantially lower interior occupied overlap; this does not certify global graph uniqueness.', 'world_placement_or_gameplay_claimed': False}
    save(ROOT / 'qa/takeover-visual-review.json', visual)
    baseline_manifest = read('checkpoint-original/checksums.json')
    baseline_changes = []
    for relative, expected in baseline_manifest.items():
        target = ROOT / 'checkpoint-original' / relative
        assert target.exists(), relative
        if digest(target) != expected:
            assert target.suffix == '.log', 'Non-log baseline mismatch: ' + relative
            baseline_changes.append(relative)
    save(ROOT / 'qa/takeover-checkpoint-audit.json', {'status': 'PASS_ORIGINAL_CONTENT_INTEGRITY_RECHECKED', 'initial_manifest_entries_verified_before_any_write': 42, 'initial_missing_or_mismatched': [], 'original_nbt_sha256': baseline_manifest['incoming/S8R_IMG01/room.nbt'], 'original_recipe_sha256': baseline_manifest['recipe.mjs'], 'baseline_copy_made_after_first_native_recheck': True, 'expected_rerun_log_differences_in_backup': baseline_changes, 'old_candidate_retained': False, 'old_batch9_candidate_retained': False, 'old_review_and_baseline_preserved': True, 'repair_reason': 'Initial complete WebGL shot hid all interior. Closer inspection showed reduced floor ring, sparse crystal niches and absent recessed stair entrance. Rebuilt this same image before attempting another.', 'failed_command_audit': ['First PowerShell apply_patch wrapper rejected the multiline patch before changing files; the build consequently found no preview recipe. Retried successfully with the same apply-patch engine directly.', 'Four-view renderer refused changed NBT in the old single-01 snapshot. Original snapshot left intact; new single-01-r2 snapshot generated and hash-verified.'], 'partial_writes_in_final_fourpiece_bundle': False})
    assembly()
    qa = {'status': 'PRODUCER_CHECKS_PASS_INDEPENDENT_REGISTRATION_REQUIRED', 'sha256': actual, 'reference_sha256': digest(REFERENCE), 'recipe_sha256': digest(ROOT / 'recipe-r2.mjs'), 'static': static, 'native': native['rooms'][0], 'preview': {'sha256': digest(ROOM / 'preview.png'), 'source': 'actual Lodestone WebGL inspection of a verified air-cut derivative of final NBT', 'provenance': str(ROOT / 'qa/takeover-preview-provenance.json')}, 'material_independent_screening': {'comparisons': duplicates['comparison_count'], 'exact_matches': 0, 'interior_iou_over_0_9': 0, 'full_report': str(ROOT / 'qa/takeover-material-independent.json'), 'global_final_audit': 'NOT_COMPLETED'}, 'visual_producer_review': str(ROOT / 'qa/takeover-visual-review.json'), 'world_placement_and_400tick_stability': 'NOT_RUN', 'forge_neighbor_updates_chunk_reload_gameplay': 'NOT_RUN', 'independent_acceptance': None, 'next_image_locked': True, 'deployment': 'NOT_DEPLOYED'}
    save(ROOM / 'qa.json', qa)
    save(ROOT / 'qa/packaging.json', {'status': 'PASS_BUNDLE_HASHES_AND_IMAGES_ONLY', 'room_count': 1, 'nbt_sha256': actual, 'software_view_count': 8, 'lodestone_camera_count': len(browser['records']), 'preview_source_and_final_nbt_link': 'PASS', 'snapshot_reference_recipe_metadata_hashes': 'PASS', 'independent_acceptance': 'NOT_GRANTED'})
    save(ROOT / 'registration-request.json', {'status': 'AWAITING_INDEPENDENT_AUDIT_REGISTRATION', 'active_producer_thread': THREAD, 'retired_producer_thread': '01a1a5df-ead1-7fa3-ba71-2cf0013e1e97', 'group': 8, 'reference_index': 1, 'room_id': ROOM_ID, 'reference': str(REFERENCE), 'reference_sha256': digest(REFERENCE), 'nbt': str(ROOM / 'room.nbt'), 'sha256': actual, 'size': metadata['size'], 'recipe': str(ROOT / 'recipe-r2.mjs'), 'recipe_sha256': digest(ROOT / 'recipe-r2.mjs'), 'metadata': str(ROOM / 'room.json'), 'qa': str(ROOM / 'qa.json'), 'assembly': str(ROOT / 'assembly.json'), 'duplicate_audit': str(ROOT / 'qa/takeover-material-independent.json'), 'visual_review': str(ROOT / 'qa/takeover-visual-review.json'), 'preview': str(ROOM / 'preview.png'), 'lodestone_complete_url': 'http://127.0.0.1:5588/', 'lodestone_inspection_url': 'http://127.0.0.1:5589/', 'independent_auditor_thread': None, 'independent_verdict': None, 'world_400tick_test': 'NOT_RUN', 'next_image_started': False, 'next_image_allowed_only_after': 'independent auditor explicitly records PASS against this exact final NBT hash; producer cannot self-unlock', 'runtime_slot': None, 'deployment': 'NOT_DEPLOYED'})
    files = [target for target in ROOT.rglob('*') if target.is_file() and target != ROOT / 'checksums.json' and '__pycache__' not in target.parts]
    save(ROOT / 'checksums.json', {target.relative_to(ROOT).as_posix(): digest(target) for target in sorted(files)})
    print('PACKAGED S8-01 R2', actual, '; independent acceptance NOT granted; S8-02 locked')


if __name__ == '__main__':
    main()
