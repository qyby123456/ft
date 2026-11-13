from pathlib import Path
import argparse
import hashlib
import json
import re
import shutil
import zipfile
import nbtlib

ROOT = Path(__file__).resolve().parents[1]
WORK = ROOT / 'output/style-rebuild-v034'
DATA = ROOT / 'src/main/resources/data/fteclipsegen'
STYLES = ['clockwork', 'forest', 'desert', 'frost', 'swamp', 'coral', 'lava', 'amethyst']
CATALOG = json.loads((DATA / 'catalog.json').read_text(encoding='utf-8'))
VARIANTS = {entry['key']: entry for entry in CATALOG['variants']}


def digest(raw):
    return hashlib.sha256(raw).hexdigest()


def audit_passed(audit):
    return str(audit.get('status', '')).upper() in {'PASS', 'PASSED'} or any(audit.get(field) is True for field in ['allPassed', 'passed', 'pass'])


def decode(raw):
    import gzip
    import io
    return nbtlib.File.parse(io.BytesIO(gzip.decompress(raw) if raw[:2] == b'\x1f\x8b' else raw))


def occupancy(data):
    names = [str(entry['Name']) for entry in data['palette']]
    return {tuple(map(int, cell['pos'])) for cell in data['blocks'] if names[int(cell['state'])] not in {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}}


def verify(style, archive):
    directory = WORK / style
    roomset = directory / 'roomset'
    raw_manifest = (roomset / 'manifest.json').read_bytes()
    manifest = json.loads(raw_manifest)
    assert manifest['schema'] == 1 and manifest['style'] == 'fteclipsegen:' + style
    entries = {entry['key']: entry for entry in manifest['templates']}
    assert len(manifest['templates']) == len(VARIANTS) and entries.keys() == VARIANTS.keys(), style
    for required in ['design.md', 'validation.json', 'gallery.html', 'four-views.html', 'style-audit.json', 'repetition-audit.json', 'self-audit.json']:
        assert (directory / required).is_file(), f'{style}: missing {required}'
    self_audit = json.loads((directory / 'self-audit.json').read_text(encoding='utf-8'))
    assert audit_passed(self_audit), f'{style}: self audit not passed'
    audited_manifest = self_audit.get('manifestSha256') or self_audit.get('fileHashes', {}).get('roomset/manifest.json')
    assert audited_manifest == digest(raw_manifest), f'{style}: self audit covers a different room set'
    for audit_name in ['style-audit.json', 'repetition-audit.json']:
        audit = json.loads((directory / audit_name).read_text(encoding='utf-8'))
        assert audit_passed(audit), f'{style}: {audit_name} not passed'
    results = []
    for key, variant in VARIANTS.items():
        raw = (roomset / (key + '.nbt')).read_bytes()
        assert digest(raw) == entries[key]['sha256'], f'{style}/{key}: manifest hash'
        data = decode(raw)
        size = tuple(map(int, data['size']))
        assert size == tuple(variant['size']) and int(data['DataVersion']) == 3465
        assert len(data['entities']) == 0
        volume = size[0] * size[1] * size[2]
        names = [str(entry['Name']) for entry in data['palette']]
        assert all(re.fullmatch(r'minecraft:[a-z0-9_/]+', name) for name in names)
        cells = {}
        for cell in data['blocks']:
            point = tuple(map(int, cell['pos']))
            assert all(0 <= point[axis] < size[axis] for axis in range(3))
            assert point not in cells and 'nbt' not in cell
            cells[point] = names[int(cell['state'])]
        assert len(cells) == volume
        air = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}

        def safe(point, height=3):
            x, y, z = point
            assert all(cells.get((x, y + rise, z)) in air for rise in range(height)), f'{style}/{key}: blocked {point}'
            support = cells.get((x, y - 1, z), 'minecraft:air')
            assert support not in air and not re.search(r'stairs|slab|pane|fence|chain|leaves|water|lava|trapdoor|button|carpet', support), f'{style}/{key}: unsupported {point}'

        for port in variant['ports']:
            for point in port['cells']:
                safe(point, 5)
        for socket in variant['sockets']:
            safe(socket['position'])
            safe(socket['spawn_position'])
        if key == 'spawn/spawn':
            safe([7, 4, 8])
        if key == 'promenade':
            safe([7, 2, 11])
        if key.startswith('boss/'):
            for point in [[16, 2, 2], [16, 2, 13], [16, 2, 25]]:
                safe(point)
            for x in range(size[0]):
                for y in range(2, size[1]):
                    expected_open = 15 <= x <= 18 and 2 <= y <= 7
                    assert (cells[x, y, 22] in air) == expected_open, f'{style}: victory partition gap'
        old = decode(archive.read(f'data/fteclipsegen/roomsets/{style}/{key}.nbt'))
        before = occupancy(old)
        after = occupancy(data)
        difference = before ^ after
        interior = {point for point in difference if 0 < point[0] < size[0] - 1 and variant['minimum_y'] < point[1] < size[1] - 1 and 0 < point[2] < size[2] - 1}
        assert interior, f'{style}/{key}: unchanged interior geometry'
        results.append({'key': key, 'sha256': digest(raw), 'solidAirChanged': len(difference), 'interiorGeometryChanged': len(interior), 'changedVolumeRatio': round(len(difference) / volume, 4)})
    return {'style': style, 'status': 'PASS', 'templates': len(results), 'manifestSha256': digest(raw_manifest), 'selfAuditSha256': digest((directory / 'self-audit.json').read_bytes()), 'rooms': results}


def integrate(reports, archive):
    archived = []
    for report in reports:
        style = report['style']
        prefix = f'data/fteclipsegen/roomsets/{style}/'
        raw = archive.read(prefix + 'manifest.json')
        fingerprint = digest(raw)
        history = DATA / 'roomsets-history' / style / fingerprint
        for name in archive.namelist():
            if name.startswith(prefix) and not name.endswith('/'):
                target = history / name[len(prefix):]
                target.parent.mkdir(parents=True, exist_ok=True)
                expected = archive.read(name)
                if target.exists():
                    assert target.read_bytes() == expected, 'Immutable archive differs: ' + str(target)
                else:
                    target.write_bytes(expected)
        archived.append({'style': style, 'fingerprint': fingerprint, 'directory': str(history)})
        (history.parent / 'v033.json').write_text(json.dumps(fingerprint) + '\n', encoding='utf-8')
        shutil.copytree(WORK / style / 'roomset', DATA / 'roomsets' / style, dirs_exist_ok=True)
        current_manifest = DATA / 'roomsets' / style / 'manifest.json'
        manifest = json.loads(current_manifest.read_bytes())
        manifest['selection'] = 'balanced-layout-v1'
        current_manifest.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
        report['integratedManifestSha256'] = digest(current_manifest.read_bytes())
    (WORK / 'archived-0.33.json').write_text(json.dumps(archived, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--style', choices=STYLES)
    parser.add_argument('--integrate', action='store_true')
    args = parser.parse_args()
    assert not (args.style and args.integrate), 'Integration requires all eight styles'
    with zipfile.ZipFile(WORK / 'baseline-0.33.0.jar') as archive:
        reports = [verify(style, archive) for style in ([args.style] if args.style else STYLES)]
        if args.integrate:
            integrate(reports, archive)
    report = {'status': 'PASS', 'integrated': args.integrate, 'styles': reports, 'templates': sum(entry['templates'] for entry in reports)}
    destination = WORK / (args.style + '-acceptance.json' if args.style else 'integration.json')
    destination.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'status': 'PASS', 'templates': report['templates'], 'integrated': args.integrate, 'report': str(destination)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
