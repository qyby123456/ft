import argparse
import sys
import zipfile
from pathlib import Path

WORKSPACE = Path(__file__).resolve().parents[1]
HISTORY = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
sys.path.insert(0, str(HISTORY / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, extended, load, save
parser = argparse.ArgumentParser()
parser.add_argument('--batch', type=Path, required=True)
arguments = parser.parse_args()
batch = arguments.batch.resolve()
assert batch.is_relative_to(WORKSPACE / 'batches') and batch.is_dir()
receipt = HISTORY / 'batches/299-native-fresh-factory-runtime/reports/actual-native-map-version299-not-promoted.json'
assert digest(receipt) == '1047eb067fb84070d2fa285bcb861980ec5ad808f5e3e5e8565057c2f7bcc18d'
dependency = load(receipt)
reference_server = Path(dependency['actualRuntime']) / 'server'
attempts = list((batch / 'build').glob('focused-storage308-*'))
assert len(attempts) == 1
attempt = attempts[0]
current_server = attempt / 'server'

def normalize(path):
    return str(path).replace(chr(92), '/').removeprefix('//?/').lower()

pins = {normalize(name): value for name, value in dependency['inputHashes'].items() if name.lower().endswith('.jar')}
records = []
for directory in [reference_server, current_server]:
    for path in sorted(directory.rglob('*.jar')):
        relative = path.relative_to(directory).as_posix().lower()
        reference = reference_server / path.relative_to(directory)
        matches = {name: value for name, value in pins.items() if name == normalize(reference) or name.endswith('/server/' + relative)}
        expected = set(matches.values())
        assert len(expected) == 1, {'reference': str(reference), 'conflictingOrMissingPins': matches}
        actual = digest(path)
        assert actual == next(iter(expected)), str(path)
        with zipfile.ZipFile(extended(path)) as archive:
            assert archive.testzip() is None, str(path)
            entries = len(archive.infolist())
        assert digest(path) == actual, str(path)
        records.append({'path': str(path), 'referencePath': str(reference), 'frozenPinPaths': sorted(matches), 'sha256': actual, 'zipEntries': entries})
assert len(records) >= dependency['actualJarCrcCount']
copied = sum(Path(row['path']).is_relative_to(current_server) for row in records)
assert copied > 0
inputs = [receipt, Path(__file__), attempt / 'runtime-command308.json', attempt / 'inputs-before308.json']
output = batch / 'reports' / ('actual-archives' + batch.name.split('-')[0] + '-verified.json')
save(output, {
    'failures': 0, 'archivesChecked': len(records), 'distinctArchiveContents': len({row['sha256'] for row in records}),
    'copiedRuntimeArchivesChecked': copied, 'allArchiveHashesMatchFrozen299Pins': True,
    'allArchiveCrcPassed': True, 'archives': records, 'fullLifecycleAccepted': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in inputs}})
print({'report': str(output), 'archivesChecked': len(records), 'distinctContents': len({row['sha256'] for row in records}), 'crcFailures': 0}, flush=True)
