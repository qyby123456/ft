import json
import re
import sys
from pathlib import Path

WORKSPACE = Path(__file__).resolve().parents[1]
HISTORY = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
sys.path.insert(0, str(HISTORY / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, save

frozen = HISTORY / 'batches/299-native-fresh-factory-runtime/reports/actual-native-map-version299-not-promoted.json'
assert digest(frozen) == '1047eb067fb84070d2fa285bcb861980ec5ad808f5e3e5e8565057c2f7bcc18d'
targets = [
    HISTORY / 'batches/195-native-capability-semantic-regression/reports/actual-independent-semantic-regression195.json',
    HISTORY / 'batches/196-native-capability-caller-provenance/reports/actual-independent-caller-provenance196.json',
    HISTORY / 'batches/197-native-entity-persistent-load-domain/reports/actual-independent-loaded-domain197.json',
]

def normalize(value):
    return str(value).replace(chr(92), '/').lower()

wanted = {normalize(path): path for path in targets}
pins = {key: set() for key in wanted}
pattern = re.compile(r'^\s*("(?:\\.|[^"\\])*"):\s*"([0-9a-f]{64})",?\s*$')
with frozen.open(encoding='utf-8') as stream:
    for line in stream:
        if not any(path.name in line for path in targets):
            continue
        match = pattern.fullmatch(line)
        if match:
            key = normalize(json.loads(match.group(1)))
            if key in pins:
                pins[key].add(match.group(2))
verified = []
for key, path in wanted.items():
    assert len(pins[key]) == 1, {'path': str(path), 'frozenPins': sorted(pins[key])}
    expected = next(iter(pins[key]))
    assert digest(path) == expected, str(path)
    verified.append({'path': str(path), 'sha256': expected, 'size': path.stat().st_size})
baseline_sha = 'b90045a2f07798d1944632b304ff431733f66b603cd42c4bd8bc47d9f72ff782'
baseline_pins = {
    'forge': 'c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942',
    'neoforge': '1ff2342751a32bb69b0e3a06a0ebd586ee3d90330c15ef548b31fe3a84b5f2c5',
}
inputs = [frozen, Path(__file__)]
for directory in [WORKSPACE, HISTORY]:
    baseline = directory / 'baseline.json'
    assert digest(baseline) == baseline_sha, str(baseline)
    metadata = json.loads(baseline.read_text(encoding='utf-8'))
    inputs.append(baseline)
    for family, expected in baseline_pins.items():
        snapshot = directory / metadata[family]['snapshot']
        assert metadata[family]['sha256'] == expected and digest(snapshot) == expected, str(snapshot)
        inputs.append(snapshot)
save(WORKSPACE / 'reports/frozen-handoff436-audit.json', {
    'failures': 0, 'frozenReceipts195196197VerifiedAgainstPinned299': verified,
    'bothBaselineCopiesAndFourReferenceSnapshotsVerified': True,
    'allHistoricalReceiptReferencedFilesRevalidated': False,
    'formalInstanceWritten': False, 'liveDriftAdopted': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [*inputs, *targets]},
})
print({'failures': 0, 'frozenReceiptsVerified': len(verified), 'baselineCopiesVerified': 2, 'referenceSnapshotsVerified': 4, 'allHistoricalReferencesRevalidated': False})
