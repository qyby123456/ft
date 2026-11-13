import json
from pathlib import Path
import sys

sys.dont_write_bytecode = True
from check import ROOT, REFERENCE, load, save, sha


def finalize():
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    assert metadata['sha256'] == sha(ROOT / 'room.nbt')
    metadata['analysis_sha256'] = sha(ROOT / 'analysis.md')
    metadata['recipe_sha256'] = sha(ROOT / 'recipe.mjs')
    save(ROOT / 'room.json', metadata)
    for name in ('static', 'native'):
        report = json.loads((ROOT / f'qa/{name}.json').read_text(encoding='utf-8'))
        assert report['status'] == 'PASS' and report['sha256'] == metadata['sha256']
    captures = json.loads((ROOT / 'lodestone/screenshots.json').read_text(encoding='utf-8'))
    assert captures['final_nbt_sha256'] == metadata['sha256'] and not captures['page_errors']
    assert len(captures['screenshots']) == 6
    derived = ROOT / 'lodestone/inspection-cutaway.nbt'
    cutaway = captures['cutaway']
    assert cutaway['final_nbt_sha256'] == metadata['sha256']
    assert cutaway['cutaway_nbt_sha256'] == sha(derived)
    final_data = load(ROOT / 'room.nbt')
    derived_data = load(derived)
    source = {tuple(record['pos']): record for record in final_data['blocks']}
    assert derived_data['palette'] == final_data['palette']
    assert all(record == source[tuple(record['pos'])] for record in derived_data['blocks'])
    for capture in captures['screenshots']:
        assert sha(ROOT / 'lodestone' / capture['file']) == capture['sha256']
        expected = metadata['sha256'] if 'exterior' in capture['file'] else sha(derived)
        assert capture['loaded_nbt_sha256'] == expected
    accepted_root = ROOT.parent / '01-large-chamber-r3'
    previous = json.loads((accepted_root / 'manifest.json').read_text(encoding='utf-8'))
    verified = []
    for record in previous['files']:
        assert sha(accepted_root / record['path']) == record['sha256'], record['path']
        verified.append(record['path'])
    assert sha(accepted_root / 'room.nbt') == '56dc00708486d1a0273878573ae05e165beda4eedc2f64b4301612c01e9f7920'
    locked = [path.name for path in ROOT.parent.iterdir() if path.name.startswith('03')]
    assert not locked
    save(ROOT / 'qa/predecessor-lock.json', {'S6_01_R3': 'ALL_MANIFEST_FILE_HASHES_UNCHANGED', 'verified_files': verified, 'S6_03': 'LOCKED_NOT_STARTED', 'independent_acceptance_02': 'PENDING', 'no_world_or_other_group_writes': True})
    save(ROOT / 'qa/rejected-visual-layout-01/status.json', {'status': 'REJECTED_VISUAL_LAYOUT', 'candidate': False, 'reason': 'storage and door wrongly put on same wall; image places them on adjacent perspective walls', 'sha256': sha(ROOT / 'qa/rejected-visual-layout-01/room.nbt')})
    save(ROOT / 'qa/rejected-fire-buffer-02/status.json', {'status': 'REJECTED_NATIVE_FIRE_BUFFER', 'candidate': False, 'reason': 'lava(18,2,6) vs bookshelf(22,3,7) has dx4,dz1,rise1; final moved heat patch to (16,2,6)', 'sha256': sha(ROOT / 'qa/rejected-fire-buffer-02/room.nbt')})
    save(ROOT / 'assembly.json', {'module': metadata['id'], 'size': metadata['size'], 'local_origin': [0, 0, 0], 'world_origin': None, 'assembly_order': [{'step': 1, 'file': 'room.nbt', 'relative_origin': [0, 0, 0], 'rotation': 'NONE'}], 'connection_anchors': metadata['connectors'], 'interior_safe_feet': metadata['interior'], 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_image_may_start': False})
    save(ROOT / 'registration.pending.json', {'id': metadata['id'], 'group': 'S6-volcanic-cavern', 'reference_number': 2, 'source': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'candidate_nbt': str(ROOT / 'room.nbt'), 'candidate_sha256': metadata['sha256'], 'size': metadata['size'], 'analysis': str(ROOT / 'analysis.md'), 'assembly': str(ROOT / 'assembly.json'), 'screenshots': str(ROOT / 'lodestone/screenshots.json'), 'static_report': str(ROOT / 'qa/static.json'), 'native_report': str(ROOT / 'qa/native.json'), 'duplicates': str(ROOT / 'qa/duplicates.json'), 'status': 'PENDING_INDEPENDENT_REVIEW', 'registry_updated': False, 'world_origin': None, 'deployment': 'NOT_PERFORMED', 'S6_03_unlocked': False})
    save(ROOT / 'qa/visual-self-review.json', {'status': 'AUTHOR_REVIEW_ONLY_PENDING_INDEPENDENT', 'reference_sha256': sha(REFERENCE), 'nbt_sha256': metadata['sha256'], 'viewed': ['lodestone-interior-cutaway.png', 'lodestone-rear-door-and-storage.png', 'lodestone-exterior.png'], 'visible_landmarks': ['left storage wall', 'two purple crystals on flat table', 'single rectangular rear portal', 'warm side niche', 'two capped obsidian/gilded pillars', 'sealed lava window and channels'], 'differences_and_inference': ['complete roof and plain excavation envelope are inferred for underground placement, not shown by the cutaway reference', 'red nether brick strips approximate red hanging detail without introducing growable flammable vines', 'lava is sealed with orange glass; image shows open lava', 'five-block table spans about 22 percent of 23-block interior; scale is interpretive', 'author visual judgment is not independent fidelity acceptance'], 'next_reference_started': False})
    save(ROOT / 'manifest.json', {'id': metadata['id'], 'reference_count': 1, 'candidate_structure_count': 1, 'qa_rejected_draft_count': 2, 'stage': 'READY_FOR_REVIEW_NOT_ACCEPTED', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'nbt_sha256': metadata['sha256'], 'size': metadata['size'], 'static': 'PASS', 'native': 'PASS_STATIC_SCOPE_ONLY', 'actual_lodestone_screenshot_count': len(captures['screenshots']), 'next_reference_started': False, 'deployment': 'NOT_PERFORMED', 'files': [{'path': path.relative_to(ROOT).as_posix(), 'sha256': sha(path), 'bytes': path.stat().st_size} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name != 'manifest.json']})
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    for record in manifest['files']:
        assert sha(ROOT / record['path']) == record['sha256']
    print('FINAL candidate SHA256:', metadata['sha256'])
    print('Verified', len(manifest['files']), 'file hashes; predecessor', len(verified), 'hashes unchanged; S6-03 locked')


if __name__ == '__main__':
    finalize()
