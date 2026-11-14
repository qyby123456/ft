import hashlib
import json
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
prior_path = WORK / 'reports/native-continuation-acceptance-2ca083af.json'
new_path = WORK / 'reports/native-pending-and-wear-focused-evidence.json'
hashes = {}
for path in [prior_path, new_path]:
    receipt = json.loads(path.read_text(encoding='utf-8-sig'))
    assert receipt['installable'] is False and receipt['deployed'] is False and receipt['promoted'] is False
    for source, wanted in receipt['inputHashes'].items():
        assert hashlib.sha256(Path(source).read_bytes()).hexdigest() == wanted, source
        hashes[source] = wanted
    hashes[str(path)] = hashlib.sha256(path.read_bytes()).hexdigest()
hashes[str(Path(__file__))] = hashlib.sha256(Path(__file__).read_bytes()).hexdigest()
result = {
    'candidate': 'native-repair-2ca083af',
    'priorFrozenContinuationAcceptance': str(prior_path),
    'additionalFrozenPendingAndWearAcceptance': str(new_path),
    'priorNormalRestartSignedSessionAndRejectionEvidenceReusedWithoutRerun': True,
    'realFailedOriginalSavePendingNewProcessLoadFocusedAccepted': True,
    'independentlyChangedOriginalWearStateRejectionFocusedAccepted': True,
    'additionalRunsNormallyStoppedAndSaved': True,
    'preservedNonAcceptance': {
        'runtime': 'repair-runtime-d0dd60c8',
        'observerAndJavaPassed': True,
        'excludedFromFrozenWearAcceptanceBecauseAuxiliaryPathWasMisdecoded': True,
        'replacementWithExplicitUtf8Runtime': 'repair-runtime-53112e22',
    },
    'remainingAcceptance': [
        'verified process interruption after genuine active-lineage CAS and before physical publication, then new-process no-reissue',
        'actual transformed Curios factory and loader-final provenance',
        'six retained families including axe and staff, fractional wear and lifecycle recovery',
        'ten external recipes and retired protocol/catalog/translation closure',
        'actual client, dedicated server, authenticated multiplayer, four GL directions and final migration closure',
    ],
    'activeCasCrashProtocolNotYetExecuted': str(WORK / 'ACTIVE_CAS_CRASH_ACCEPTANCE.md'),
    'authorityWrittenByReflection': False,
    'fullLifecycleAccepted': False, 'fullMigrationAccepted': False,
    'promoted': False, 'installable': False, 'deployed': False, 'inputHashes': hashes,
}
output = WORK / 'reports/native-continuation-acceptance-2ca083af-stage2.json'
with output.open('x', encoding='utf-8') as stream:
    json.dump(result, stream, ensure_ascii=False, indent=2)
    stream.write(chr(10))
print({'evidence': str(output), 'pendingAndWearGatesAdded': True, 'inputCount': len(hashes), 'fullMigrationAccepted': False})
