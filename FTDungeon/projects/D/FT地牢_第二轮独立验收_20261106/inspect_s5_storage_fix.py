import hashlib
import json
import math
import sys
import zipfile
from pathlib import Path

from audit_s8_submission import decode, load


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    root = Path(sys.argv[1]).resolve()
    prior = root.parent / 'S5-submission-20261107-080041-052971'
    records = {}
    for name in ('submission-snapshot.json', 'supplemental-snapshot.json', 'delivery-snapshot.json'):
        records.update({item['source']: item for item in load(root / name)['files']})
    for item in records.values():
        assert digest(Path(item['snapshot'])) == item['sha256']
    current, previous = load(root / 'ready-for-review.json'), load(prior / 'ready-for-review.json')
    changed = []
    empty = {'state': {'Name': 'minecraft:air'}, 'nbt': None}
    changes = {}
    for room, old in zip(current['rooms'], previous['rooms']):
        assert room['id'] == old['id']
        if room['sha256'] != old['sha256']:
            changed.append(room['id'])
    assert changed == ['S5-17']
    for identifier, relative in [('S5-17', 'S5-17/s5-17.nbt'), ('merged', 'merged/merged.nbt')]:
        before_data, before = decode(prior / relative)
        after_data, after = decode(root / relative)
        assert before_data['size'] == after_data['size']
        different = [{'position': point, 'before': before.get(point, empty), 'after': after.get(point, empty)} for point in before.keys() | after.keys() if before.get(point, empty) != after.get(point, empty)]
        assert len(different) == 6
        changes[identifier] = different
    expected_local = {(east, up, 1) for east in (6, 9) for up in (4, 5, 8)}
    assert {tuple(item['position']) for item in changes['S5-17']} == expected_local
    assert {tuple(item['position']) for item in changes['merged']} == {(east, up, 81) for east in (156, 159) for up in (10, 11, 14)}
    changed_parts = [path.name for path in (root / 'merged/parts').glob('*.nbt') if digest(path) != digest(prior / 'merged/parts' / path.name)]
    assert changed_parts == ['part-08.nbt']
    data, blocks = decode(root / 'merged/merged.nbt')
    folder = root / 'qa/room17-storage-verified'
    reach = load(folder / 'reach-results.json')
    assert reach['sha256'] == current['merged']['sha256'] and reach['passed']
    interactions = reach['interactions']
    assert len(interactions) == 37
    assert {tuple(item['target']) for item in interactions} == {point for point, block in blocks.items() if block['state']['Name'] in {'minecraft:chest', 'minecraft:barrel', 'minecraft:lever'}}
    for item in interactions:
        assert all(item[key] for key in ('passed', 'standing_found', 'standing_clear', 'dry_supported', 'ray_hit_target', 'consumed'))
        assert abs(math.dist(item['eye'], item['hit']) - item['eye_to_hit']) < 1e-8
        assert item['eye_to_hit'] <= item['reach_limit'] == 4.5
        if item['block'] == 'minecraft:lever':
            assert item['toggled'] and item['restored']
        else:
            assert item['menu_created']
    natural = []
    for identifier in ('S5-17', 'merged'):
        placed_data, placed = decode(folder / (identifier + '-placed.nbt'))
        after_data, after = decode(folder / (identifier + '-after1200.nbt'))
        assert placed_data['size'] == after_data['size']
        assert len(placed) == len(after) == math.prod(placed_data['size'])
        assert placed == after
        natural.append({'id': identifier, 'full_state_payload_cells': len(after), 'placed_sha256': digest(folder / (identifier + '-placed.nbt')), 'after1200_sha256': digest(folder / (identifier + '-after1200.nbt')), 'full_state_and_be_equal': True})
    archive = root / 'access-package/s5access.zip'
    assert digest(archive) == current['external_access_zip_sha256']
    package = load(root / 'access-package/manifest.json')
    changed_archive = []
    with zipfile.ZipFile(archive) as zipped, zipfile.ZipFile(prior / 'access-package/s5access.zip') as old:
        assert set(zipped.namelist()) == set(old.namelist()) == {item['path'].replace('\\', '/') for item in package['files']}
        for item in package['files']:
            name = item['path'].replace('\\', '/')
            payload = zipped.read(name)
            assert hashlib.sha256(payload).hexdigest() == item['sha256']
            assert payload == (root / 'access-package/s5access' / name).read_bytes()
            if payload != old.read(name):
                changed_archive.append(name)
                assert name == 'pack.mcmeta' or name.endswith('/part_08.nbt') or name.endswith('/part-08.nbt'), name
    routes = load(folder / 'route-input.json')
    assert len(routes) == 2
    stands = {(item['feet'][0] - 0.5, item['feet'][1], item['feet'][2] - 0.5) for item in interactions}
    assert len(stands) == 37
    for route in routes:
        assert route['sha256'] == current['merged']['sha256']
        assert {tuple(item['position']) for item in route['targets']} == stands
        route['candidate'] = str(root / 'merged/merged.nbt')
        route['size'] = data['size']
    destination = root / 'independent-interaction-qa'
    destination.mkdir(exist_ok=True)
    (destination / 'route-input.json').write_text(json.dumps(routes, ensure_ascii=False, indent=2), encoding='utf-8')
    (destination / 'vanilla-block-tags.json').write_bytes((folder / 'vanilla-block-tags.json').read_bytes())
    result = {'stage': 'INDEPENDENT_FROZEN_STORAGE_FIX_INSPECTION', 'source_sha256': current['merged']['sha256'], 'frozen_files_verified': len(records), 'changed_rooms': changed, 'exact_block_changes': changes, 'changed_parts': changed_parts, 'all_functional_storage_and_levers': 37, 'max_eye_to_hit': max(item['eye_to_hit'] for item in interactions), 'world_full_snapshot_comparisons': natural, 'changed_archive_entries': changed_archive, 'independent_routes_prepared': 74, 'accepted': False, 'limitations': ['World results and native ray/use inspected, no parent world rerun', 'Route graph and positioned actors do not certify complete natural player travel', 'Access commands/tags/lobby inherit prior exact-byte tested evidence; changed structure part independently checked separately']}
    (destination / 'storage-fix-inspection.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps({key: value for key, value in result.items() if key != 'exact_block_changes'}, ensure_ascii=False))


if __name__ == '__main__':
    main()
