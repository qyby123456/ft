import sys
from pathlib import Path

BATCH = Path(__file__).resolve().parents[1]
HISTORY = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
sys.path.insert(0, str(HISTORY / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save

coverage = BATCH / 'reports/actual-coverage429-read.json'
sealed = BATCH / 'reports/actual-nonempty429-sealed-experimental.json'
data = load(coverage)
assert data['exitCode'] == 0
assert load(sealed)['diagnosticFailures'] == 0
raw = Path(data['attempt']) / 'server/actual-storage-view308.json'
rows = load(raw)['cases']
observations = []
for row in rows:
    assert row['rangedRootPresentBefore429'] is False
    pending = [row]
    found = []
    while pending:
        value = pending.pop()
        if isinstance(value, dict):
            if 'rangedAcquisitionIdentity429' in value:
                found.append(value['rangedAcquisitionIdentity429'])
            pending.extend(value.values())
        elif isinstance(value, list):
            pending.extend(value)
    assert len(found) == 1
    identity = found[0]
    assert identity == {
        'plannedRootIsActualPersistentRoot': True,
        'plannedBufferWasNull': True,
        'plannedBufferIsCurrentStoredBuffer': False,
        'storedBufferPresentAfter': True,
        'authorityFieldsWritten': False,
    }
    observations.append(identity)
assert len(observations) == 2
save(BATCH / 'reports/actual-ranged-identity429-sealed.json', {
    'failures': 0, 'candidateBatchUnchanged': 427,
    'nativeImpactSamples': len(observations), 'observations': observations,
    'missingRootObservedBeforeImpact': True,
    'plannedRootIdentityCorrect': True, 'plannedBufferNull': True,
    'authorityFieldsWritten': False, 'positiveSignedSessionAccepted': False,
    'installable': False,
    'inputHashes': {str(path): digest(path) for path in [coverage, sealed, raw, Path(__file__), BATCH / 'tools/prepare429.py', BATCH / 'tests/LoadedWeapon429.java']},
})
print({'failures': 0, 'samples': 2, 'plannedRootCorrect': True, 'plannedBufferNull': True, 'authorityFieldsWritten': False})
