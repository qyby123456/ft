import importlib.util
import shutil
from pathlib import Path

from prepare import ROOT, TARGET, Structure, AIR, digest, save


def load(path):
    import json
    return json.loads(path.read_text(encoding='utf-8-sig'))


def mask(audit, path):
    template = audit.decode(path)
    size = template['size']
    points = {tuple(entry['pos']) for entry in template['blocks'] if template['palette'][entry['state']]['Name'] not in audit.AIR}
    interior = {point for point in points if all(1 < point[axis] < size[axis] - 2 for axis in range(3))}
    return [audit.occupancy(interior, size, rotation) for rotation in range(4)]


def main():
    source = TARGET / 'room.nbt'
    sha = digest(source)
    assert sha == 'bb8885701818a29fbaf9904da9d191680511628304b97194c814d2989464ed87'
    native = load(ROOT / 'qa/native-review.json')
    survival = load(ROOT / 'qa/independent-native-frozen.json')[0]
    routes = load(ROOT / 'qa/independent-routes.json')[0]
    assert native['status'] == 'PASS' and native['rooms'][0]['sha256'] == sha
    assert survival['sha256'] == routes['sha256'] == sha
    assert survival['vanilla_block_tags_bound'] and survival['survival_checked'] == 9
    assert not survival['survival_failures'] and not survival['survival_unsupported']
    assert routes['start_clear'] and len(routes['targets']) == 17
    assert all(target['reachable'] and target['standing_clear'] for target in routes['targets'])
    structure = Structure.read(source)
    derivation = load(ROOT / 'derivation.json')
    assert derivation['source_sha256'] == sha
    for entry in derivation['derivatives']:
        path = Path(entry['derived'])
        assert digest(path) == entry['derived_sha256'] and entry['source_sha256'] == sha
        preview = Structure.read(path)
        changed = 0
        for point, state in structure.blocks.items():
            if preview.get(point) == state:
                continue
            east, up, south = point
            near = {'north': 4 <= south <= 7, 'south': south >= 21, 'west': east <= 3 and south >= 4, 'east': east >= 19 and south >= 4}[entry['direction']]
            assert preview.get(point) == AIR
            assert (up >= 12 and south >= 4) or (up >= 2 and near)
            changed += 1
        assert changed == entry['removed_nonair']
    screenshots = load(ROOT / 'qa/lodestone-screenshots.json')
    assert len(screenshots) == 5
    for entry in screenshots:
        assert not entry['errors'] and entry['source_sha256'] == sha
        assert digest(Path(entry['screenshot'])) == entry['screenshot_sha256']
        assert digest(Path(entry['rendered'])) == entry['rendered_sha256']
    shutil.copyfile(TARGET / 'lodestone-north.png', TARGET / 'preview.png')
    reviewer = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685')
    specification = importlib.util.spec_from_file_location('readonly_audit', reviewer / 'audit.py')
    audit = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(audit)
    own = mask(audit, source)[0]
    duplicates = []
    for group, relative, candidate, unused_preview in audit.GROUPS:
        peer = audit.BASE / relative / candidate
        if group == 2:
            peer = audit.BASE / 'FT地牢_第2组重做B_20261104/S2-01/revision-02/incoming/G2B-01-R2/room.nbt'
        peer_masks = mask(audit, peer)
        score = max(len(own & other) / len(own | other) for other in peer_masks)
        duplicates.append({'peer_group': group, 'peer': str(peer), 'sha256': digest(peer), 'normalized_interior_jaccard_best_y_rotation': score})
    acceptance = load(reviewer / 'S2-01-ACCEPTANCE.json')
    assert duplicates[0]['sha256'] == acceptance['nbt_sha256']
    save(ROOT / 'qa/duplicate-selfcheck.json', {'source_sha256': sha, 'scope': 'Maker screening only. Material-independent interior occupancy on normalized 25x17x25 grid, four Y rotations, excludes outer two blocks and fluids. Scores are not semantic-uniqueness certification or independent acceptance.', 'comparisons': duplicates})
    qa_path = TARGET / 'qa.json'
    qa = load(qa_path)
    qa.update({'status': 'SELF_CHECK_PASS_PENDING_INDEPENDENT_ACCEPTANCE', 'native_bound_tags_survival': 'PASS_9_STATIC_POINTS', 'player_volume_route_targets': 17, 'player_volume_routes': 'PASS_OFFLINE_SHAPES_NOT_GAME_PHYSICS', 'validator_provenance': str(reviewer), 'independent_acceptance': 'NOT_GRANTED', 'source_and_preview_hashes_verified': True})
    save(qa_path, qa)
    metadata = load(TARGET / 'room.json')
    save(ROOT / 'registration.json', {'group': 2, 'image': 2, 'id': 'G2B-02', 'name': metadata['name'], 'status': 'READY_FOR_INDEPENDENT_REVIEW_NOT_ACCEPTED', 'candidate': str(source), 'sha256': sha, 'size': metadata['size'], 'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'], 'connections': metadata['connections'], 'hall_feet_y': 2, 'metadata': str(TARGET / 'room.json'), 'qa': str(qa_path), 'assembly': str(ROOT / 'assembly.json'), 'derivation': str(ROOT / 'derivation.json'), 'screenshots': str(ROOT / 'qa/lodestone-screenshots.json'), 'duplicate_selfcheck': str(ROOT / 'qa/duplicate-selfcheck.json'), 'active_reviewer_thread': '01a1a685-045b-76d2-aa2b-2a3f24520603', 'reviewer_messaged': False, 'next_image': 'S2-03 LOCKED_NOT_STARTED', 'world_ticks_gameplay_deployment': 'NOT_RUN', 's2_01_source_unchanged_sha256': acceptance['nbt_sha256']})
    files = [{'path': str(path.relative_to(ROOT)), 'sha256': digest(path)} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name != 'FILE_MANIFEST.json' and '__pycache__' not in path.parts]
    save(ROOT / 'FILE_MANIFEST.json', {'source_sha256': sha, 'files': files})
    assert all(digest(ROOT / item['path']) == item['sha256'] for item in files)
    print(f'FINALIZED {sha}; {len(files)} file hashes; 17 routes; 9 survival checks; independent acceptance PENDING')
    print('Duplicate screening:', [(item['peer_group'], round(item['normalized_interior_jaccard_best_y_rotation'], 4)) for item in duplicates])


if __name__ == '__main__':
    main()
