import hashlib
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parent
ORIGINAL = Path('D:/ft工程文件/小型群系类地牢_现场生成器_20261106/runtime/next-commit-iofix')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def write(name, value):
    (ROOT / name).write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')


def main():
    frozen = []
    for folder, count in [(ORIGINAL.parent / 'next-commit', 95), (ORIGINAL, 78)]:
        ready = read(folder / 'ready.json')
        assert len(ready['files']) == count
        for name, expected in ready['files'].items():
            path = Path(name)
            if not path.is_absolute():
                path = folder / path
            assert digest(path) == expected, str(path)
        frozen.append({'path': str(folder / 'ready.json'), 'sha256': digest(folder / 'ready.json'), 'unchangedFiles': count})
    copied = []
    for folder in ['core-snapshot', 'src/main/resources/data/ftbd_candidate']:
        for path in sorted((ROOT / folder).rglob('*')):
            if path.is_file():
                assert digest(path) == digest(ORIGINAL / path.relative_to(ROOT)), str(path)
                copied.append(str(path.relative_to(ROOT)))
    for path in sorted((ROOT / 'src/main/java/cn/ft/biomedungeon/runtime').glob('*.java')):
        if path.name not in {'BiomeDungeonBackend.java', 'Placement.java', 'ArrivalGeometry.java'}:
            assert digest(path) == digest(ORIGINAL / path.relative_to(ROOT)), str(path)
    for log in ['build-harness-porch2.log', 'build-library-porch2.log']:
        assert 'BUILD SUCCESSFUL' in (ROOT / log).read_text(encoding='utf-8-sig')
    assert 'BACKEND_PLANNER_PASS assertions=12' in (ROOT / 'planner-test.log').read_text(encoding='utf-8-sig')
    library = ROOT / 'build/libs/ft-biome-backend-library-1.0.0-ft-backend.jar'
    harness = ROOT / 'build/libs/ft-biome-backend-library-1.0.0-ft-backend-test-harness-only.jar'
    with zipfile.ZipFile(library) as production, zipfile.ZipFile(harness) as test:
        assert production.testzip() is None and test.testzip() is None
        names = production.namelist()
        assert not any(name.endswith(('DungeonMod.class', 'UnifiedEngine.class', 'RuntimeEngine.class', 'BackendHarness.class', 'GateContractFixture.class')) for name in names)
        assert 'META-INF/mods.toml' not in names and not any(name.startswith('data/ftdungeonmobs/') for name in names)
        for name in names:
            if name.startswith(('cn/', 'data/')) and not name.endswith('/'):
                assert production.read(name) == test.read(name), 'Test/library bytes differ: ' + name
    results = {}
    logs = []
    for case in ['ready', 'interrupted', 'occupied', 'journal-io']:
        result = read(ROOT / (case + '-porch2-result.json'))
        assert result['status'].startswith('PASS_'), result
        assert result['harnessJarSha256'] == digest(harness)
        assert result['oldCandidateCommandsOrMarkerOrReceiptUsed'] is False
        for process in result['processes']:
            path = Path(process['log'])
            assert digest(path) == process['sha256']
            assert process['exitCode'] != 0 if process['forced'] else process['exitCode'] == 0
            logs.append(path)
        results[case] = result
    assert results['ready']['ready']['state'] == 'READY' and results['ready']['ready']['observedTicks'] >= 2400
    assert results['ready']['restart']['stage'] == 'READY_REUSED_NO_WORLD_WRITES'
    assert results['ready']['wrongGatePartRefused']['state'] == 'FAILED'
    assert results['interrupted']['final']['state'] == 'FAILED'
    assert results['occupied']['final']['completed'] == results['journal-io']['final']['completed'] == 0
    assert 'Occupied envelope' in results['occupied']['final']['error']
    assert 'failure journal:' in results['journal-io']['final']['error']
    geometry = {'landing': [1015.5, 202.0, 997.5], 'returnGateAnchor': [1018, 202, 997], 'returnGateFacing': 'SOUTH',
                'entranceOutward': 'NORTH', 'platformMin': [1010, 201, 995], 'platformMaxExclusive': [1021, 208, 1000],
                'envelopeMin': [994, 196, 933], 'envelopeMaxExclusive': [1120, 227, 1075],
                'scope': 'Only exact tested seed267611119455246637/dimensionftdungeonmobs:dungeon/anchor1000,200,1000/ordinal0; consume runtime Status for other requests'}
    write('result.json', {'status': 'READY_SOURCE_BACKEND_NATIVE_ISOLATED_TESTED_HOST_INTEGRATION_PENDING',
          'policy': 'ft-biome-backend-v1-side-porch2', 'librarySha256': digest(library), 'harnessSha256': digest(harness),
          'mainClassesByteIdenticalToInstalledHarness': True, 'originalFrozen': frozen, 'unchangedCoreAndCatalogFiles': copied,
          'plannerFourOrdinalsAssertions': 12, 'results': results, 'testedGeometry': geometry,
          'hostReturnGateTestScope': 'Native contract fixture only; actual FT gate animation/teleport/registry integration independently pending',
          'firstReadySourceValidation': 'Full original source state/BE semantic readback and native transitions; no source overwrite by side porch',
          'readyReuse': 'Preserves ordinary room/player changes; strict porch except exactly12 correctly identified host gate cells',
          'noHiddenSeedRetry': True, 'formalWorldModified': False, 'hostSourceModified': False, 'independentHostIntegration': 'PENDING'})
    selected = [path for folder in ['src', 'core-snapshot', 'gradle', 'history'] for path in (ROOT / folder).rglob('*') if path.is_file()]
    selected.extend(path for path in ROOT.iterdir() if path.is_file() and path.name not in {'ready.json', 'installed-test-porch2-runner.log'})
    selected.extend([library, harness, *logs])
    files = {path.relative_to(ROOT).as_posix(): digest(path) for path in sorted(set(selected))}
    consume = {name: expected for name, expected in files.items() if name.startswith(('src/main/', 'core-snapshot/'))}
    assert len(consume) > 30 and 'src/main/java/cn/ft/biomedungeon/runtime/BiomeDungeonBackend.java' in consume
    write('ready.json', {'component': 'ft-biome-backend', 'ready': True, 'status': 'READY_SOURCE_BACKEND_NATIVE_ISOLATED_TESTED_HOST_INTEGRATION_PENDING',
          'policy': 'ft-biome-backend-v1-side-porch2', 'api': 'cn.ft.biomedungeon.runtime.BiomeDungeonBackend',
          'copyOnly': ['src/main/java', 'core-snapshot', 'src/main/resources/data/ftbd_candidate'],
          'excludeTestHostFromMain': True, 'library': str(library.relative_to(ROOT)), 'librarySha256': digest(library),
          'independentHostIntegration': 'PENDING', 'productionPromotion': False, 'result': 'result.json',
          'consumeFiles': consume, 'files': files})
    print('BACKEND_READY files=' + str(len(files)) + ' consume=' + str(len(consume)) + ' librarySha256=' + digest(library))


if __name__ == '__main__':
    main()
