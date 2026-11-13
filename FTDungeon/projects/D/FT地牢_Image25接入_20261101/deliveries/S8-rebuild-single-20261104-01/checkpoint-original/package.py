import hashlib
import json
import urllib.request

from PIL import Image
from deliver import ROOT, ROOM, ROOM_ID, REFERENCE, digest, load, save


def main():
    metadata = json.loads((ROOM / 'room.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    static = json.loads((ROOT / 'qa/static.json').read_text(encoding='utf-8'))
    duplicates = json.loads((ROOT / 'qa/duplicate-selfcheck.json').read_text(encoding='utf-8'))
    browser = json.loads((ROOT / 'qa/lodestone-browser.json').read_text(encoding='utf-8'))
    actual = digest(ROOM / 'room.nbt')
    assert actual == metadata['sha256'] == native['rooms'][0]['sha256'] == static['nbt_sha256'] == duplicates['candidate_sha256']
    assert len(native['rooms']) == 1 and native['status'] == static['status'] == browser['status'] == 'PASS'
    assert duplicates['comparison_count'] > 0 and not duplicates['exact_matches']
    assert metadata['reference_sha256'] == digest(REFERENCE)
    snapshot = ROOT / 'views/single-01' / ROOM_ID
    assert digest(snapshot / 'room.nbt') == actual
    for mode in ['interior', 'exterior']:
        for direction in ['north', 'south', 'west', 'east']:
            with Image.open(snapshot / f'{mode}-{direction}.png') as image:
                image.verify()
    for target in [ROOM / 'preview.png', ROOT / 'lodestone-preview.png']:
        with Image.open(target) as image:
            image.verify()
    served = urllib.request.urlopen('http://127.0.0.1:5588/__minecraft_builder__/build.nbt', timeout=10).read()
    assert hashlib.sha256(served).hexdigest() == actual
    qa = {'status': 'TECHNICAL_SELFCHECK_PASS_AWAITING_ACCEPTANCE', 'sha256': actual, 'static': static, 'native': native['rooms'][0], 'all_declared_ports_and_route_targets_native_collision': 'PASS', 'lodestone_browser': browser, 'duplicate_selfcheck': {'compared': duplicates['comparison_count'], 'exact_geometry_matches': 0, 'final_topology_audit': 'AWAITING_COORDINATOR'}, 'world_placement': 'NOT_RUN', 'forge_rotation_and_ticks': 'NOT_RUN', 'gameplay': 'NOT_RUN'}
    save(ROOM / 'qa.json', qa)
    save(ROOT / 'qa/packaging.json', {'status': 'PASS', 'room_count': 1, 'nbt_sha256': actual, 'view_count': 8, 'reference_and_snapshot_hashes': 'PASS', 'actual_lodestone_webgl_load': 'PASS', 'served_final_nbt_sha256': 'PASS', 'visual_checks': ['software north cutaway shows central crystal, ring, pillars, arches and supported beams', 'actual Lodestone screenshot shows complete exterior and roof'], 'acceptance': 'AWAITING_REGISTRATION_ACCEPTANCE'})
    save(ROOT / 'registration-request.json', {'status': 'AWAITING_REGISTRATION_ACCEPTANCE', 'thread': '01a1a5df-ead1-7fa3-ba71-2cf0013e1e97', 'group': 8, 'reference_index': 1, 'room_id': ROOM_ID, 'reference': str(REFERENCE), 'reference_sha256': digest(REFERENCE), 'nbt': str(ROOM / 'room.nbt'), 'sha256': actual, 'size': [23, 13, 23], 'metadata': str(ROOM / 'room.json'), 'assembly': str(ROOT / 'assembly.json'), 'qa': str(ROOM / 'qa.json'), 'duplicate_audit': str(ROOT / 'qa/duplicate-selfcheck.json'), 'preview': str(ROOM / 'preview.png'), 'lodestone': 'http://127.0.0.1:5588/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S8R_IMG01', 'next_image_started': False, 'next_image_allowed_only_after': 'explicit coordinator registration acceptance; then same group next index', 'runtime_slot': None, 'deployment': 'NOT_DEPLOYED'})
    files = [target for target in ROOT.rglob('*') if target.is_file() and target.name != 'checksums.json' and '__pycache__' not in target.parts]
    save(ROOT / 'checksums.json', {target.relative_to(ROOT).as_posix(): digest(target) for target in sorted(files)})
    print('PASS: exactly one room, native route checks, 8 views, live Lodestone, hashes; awaiting registration acceptance')


if __name__ == '__main__':
    main()
