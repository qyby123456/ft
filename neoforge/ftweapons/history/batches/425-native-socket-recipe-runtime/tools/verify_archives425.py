import hashlib
import json
import sys
import zipfile
from pathlib import Path

ROOT = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
BATCH = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, extended, load, save
dependency_receipt = ROOT / 'batches/299-native-fresh-factory-runtime/reports/actual-native-map-version299-not-promoted.json'
assert digest(dependency_receipt) == '1047eb067fb84070d2fa285bcb861980ec5ad808f5e3e5e8565057c2f7bcc18d'
dependency = load(dependency_receipt)
dependency_server = Path(dependency['actualRuntime']) / 'server'
attempts = list((BATCH / 'build').glob('focused-storage308-*'))
assert len(attempts) == 1
attempt = attempts[0]
runtime_server = attempt / 'server'

def normalize(path):
    return str(path).replace(chr(92), '/').removeprefix('//?/').lower()

pins = {normalize(name): expected for name, expected in dependency['inputHashes'].items() if name.lower().endswith('.jar')}
archives = []
for directory in [dependency_server, runtime_server]:
    for path in sorted(directory.rglob('*.jar')):
        reference = dependency_server / path.relative_to(directory)
        relative = path.relative_to(directory).as_posix().lower()
        matches = {name: expected for name, expected in pins.items() if name == normalize(reference) or name.endswith('/server/' + relative)}
        expected_values = set(matches.values())
        assert len(expected_values) == 1, {'reference': str(reference), 'conflictingOrMissingPins': matches}
        expected = next(iter(expected_values))
        actual = digest(path)
        assert actual == expected, str(path)
        with zipfile.ZipFile(extended(path)) as archive:
            assert archive.testzip() is None, str(path)
            count = len(archive.infolist())
        assert digest(path) == actual, str(path)
        archives.append({'path': str(path), 'referencePath': str(reference), 'frozenPinPaths': sorted(matches), 'sha256': actual, 'zipEntries': count})
assert len(archives) >= dependency['actualJarCrcCount']
save(BATCH / 'reports/actual-archives425-verified.json', {
    'failures': 0, 'archivesChecked': len(archives), 'distinctArchiveContents': len({row['sha256'] for row in archives}),
    'includesCopiedRuntimeMods': any(Path(row['path']).is_relative_to(runtime_server) for row in archives),
    'allArchiveHashesMatchFrozen299Pins': True, 'allArchiveCrcPassed': True, 'archives': archives,
    'fullLifecycleAccepted': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [dependency_receipt, Path(__file__)]}})
print({'archivesChecked': len(archives), 'distinctContents': len({row['sha256'] for row in archives}), 'crcFailures': 0}, flush=True)
