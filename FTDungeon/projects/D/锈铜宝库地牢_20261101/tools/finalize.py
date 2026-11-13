import argparse
import gzip
import hashlib
import itertools
import json
import re
import shutil
import zipfile
from pathlib import Path

from PIL import Image

from build_vault import ROOT, PACK, SIZE, nbt, write_json


INCOMING = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/incoming/M2')


def read_json(path):
    return json.loads(path.read_text(encoding='utf-8'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--publish-m2', action='store_true')
    arguments = parser.parse_args()
    geometry = read_json(ROOT / 'qa/geometry.json')
    native = read_json(ROOT / 'qa/native-verification.json')
    m2_native = read_json(ROOT / 'qa/m2-native-verification.json')
    assert geometry['status'] == native['status'] == m2_native['status'] == 'PASS'
    standalone = ROOT / 'out/FT_Copper_Vault_1.20.1.nbt'
    assert digest(standalone) == native['sha256']
    assert standalone.read_bytes() == (PACK / 'data/ftcoppervault/structures/treasury.nbt').read_bytes()
    blueprint = read_json(ROOT / 'qa/blueprint.json')
    template = {key: blueprint[key] for key in ('DataVersion', 'size', 'palette', 'blocks', 'entities')}
    assert gzip.decompress(standalone.read_bytes()) == b'\x0a\0\0' + nbt(template)[1]
    scans = []
    for path in sorted((PACK / 'data/ftcoppervault/functions/internal').glob('scan_*.mcfunction')):
        for line in path.read_text(encoding='utf-8').splitlines():
            match = re.fullmatch(r'execute unless block ~([0-9]+) ~([0-9]+) ~([0-9]+) #ftcoppervault:air run scoreboard players set #ok ftcv.build 0', line)
            assert match is not None
            scans.append(tuple(int(value) for value in match.groups()))
    expected = set(itertools.product(*(range(length) for length in SIZE)))
    assert len(scans) == len(set(scans)) == len(expected) and set(scans) == expected
    air = read_json(PACK / 'data/ftcoppervault/tags/blocks/air.json')
    assert set(air['values']) == {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
    build = (PACK / 'data/ftcoppervault/functions/internal/build.mcfunction').read_text(encoding='utf-8')
    assert build.index('run function ftcoppervault:internal/scan') < build.index('run place template ftcoppervault:treasury')
    assert 'if score #ok ftcv.build matches 1 store success score #placed ftcv.build run place template' in build
    assert build.count('execute unless loaded ') == 12
    assert build.count('scoreboard players set #bounds ftcv.build 0') == 2
    assert native['commands_parsed'] < 65536
    assert not (PACK / 'data/minecraft/tags/functions/tick.json').exists()
    images = [ROOT / 'preview/copper-vault-cutaway.png', ROOT / 'preview/copper-vault-plan.png', ROOT / 'out/m2/preview.png']
    for path in images:
        with Image.open(path) as image:
            image.verify()
    pack_zip = ROOT / 'out/ft-copper-vault-1.20.1.zip'
    with zipfile.ZipFile(pack_zip) as bundle:
        assert bundle.testzip() is None
        assert 'pack.mcmeta' in bundle.namelist()
        for name in bundle.namelist():
            assert not name.startswith(('/', '../'))
            assert bundle.read(name) == (PACK / name).read_bytes()
    m2 = ROOT / 'out/m2'
    metadata = read_json(m2 / 'room.json')
    assert set(metadata) == {'id', 'name', 'size', 'entrance', 'interior', 'facing', 'sha256', 'reference'}
    assert metadata['id'] == 'M2' and metadata['sha256'] == digest(m2 / 'room.nbt') == m2_native['sha256']
    qa = read_json(m2 / 'qa.json')
    qa.update(status='PASS', native_verification=m2_native, live_game_placement_tested=False,
              preview_sha256=digest(m2 / 'preview.png'))
    write_json(m2 / 'qa.json', qa)
    publication = {'published': False}
    if arguments.publish_m2:
        assert INCOMING.resolve().is_relative_to(Path('D:/ft工程文件/FT地牢_Image25接入_20261101/incoming').resolve())
        names = ('room.nbt', 'preview.png', 'qa.json', 'room.json')
        for name in names:
            destination = INCOMING / name
            if destination.exists() and destination.read_bytes() != (m2 / name).read_bytes():
                raise FileExistsError('Refuse to overwrite a different incoming artifact: ' + str(destination))
        INCOMING.mkdir(parents=True, exist_ok=True)
        for name in names:
            shutil.copy2(m2 / name, INCOMING / name)
            assert (INCOMING / name).read_bytes() == (m2 / name).read_bytes()
        publication = {'published': True, 'directory': str(INCOMING), 'files': list(names), 'sha256': metadata['sha256'], 'core_modified': False, 'deployed': False}
    write_json(ROOT / 'qa/m2-handoff.json', publication)
    final_report = {'status': 'PASS', 'standalone_sha256': digest(standalone), 'm2_sha256': metadata['sha256'],
                    'protected_air_volume': len(scans), 'guard_order': 'PASS', 'native_standalone': native,
                    'native_m2': m2_native, 'png_decode': 'PASS', 'datapack_zip_integrity': 'PASS',
                    'm2_handoff': publication, 'player_worlds_modified': False, 'paid_image_api_requests': 0,
                    'live_game_placement_tested': False}
    write_json(ROOT / 'qa/final-verification.json', final_report)
    files = [ROOT / 'README.md', standalone, pack_zip, ROOT / 'out/materials.csv', ROOT / 'out/manifest.json']
    files += sorted((ROOT / 'preview').glob('*.png'))
    files += sorted((ROOT / 'tools').glob('*.py')) + sorted((ROOT / 'tools').glob('*.java'))
    files += [ROOT / 'qa' / name for name in ('geometry.json', 'native-verification.json', 'm2-native-verification.json', 'm2-handoff.json', 'final-verification.json')]
    files += [m2 / name for name in ('room.nbt', 'room.json', 'preview.png', 'qa.json')]
    delivery = ROOT / 'out/FT_Copper_Vault_20261101.zip'
    with zipfile.ZipFile(delivery, 'w', zipfile.ZIP_DEFLATED) as bundle:
        for path in files:
            bundle.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(delivery) as bundle:
        assert bundle.testzip() is None
        assert all(not name.endswith(('.jar', '.class')) for name in bundle.namelist())
    print(json.dumps({'status': 'PASS', 'delivery': str(delivery), 'bytes': delivery.stat().st_size,
                      'sha256': digest(delivery), 'm2_handoff': publication}, ensure_ascii=False))


if __name__ == '__main__':
    main()
