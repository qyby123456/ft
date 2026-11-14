import hashlib
import json
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
hashes = {}


def accepted(name):
    path = WORK / 'reports' / ('native-repair-runtime-' + name + '.json')
    result = json.loads(path.read_text(encoding='utf-8-sig'))
    terminal_path = Path(result['attempt']) / 'terminal308.json'
    terminal = json.loads(terminal_path.read_text(encoding='utf-8-sig'))
    assert result['exitCode'] == terminal['exitCode'] == result['probe']['failures'] == 0
    assert terminal['stopSent'] and terminal['completeSave']
    for source, wanted in result['inputHashes'].items():
        assert hashlib.sha256(Path(source).read_bytes()).hexdigest() == wanted, source
        hashes[source] = wanted
    for source in [path, terminal_path]:
        hashes[str(source)] = hashlib.sha256(source.read_bytes()).hexdigest()
    return result, result['probe']


seed_result, seed = accepted('repair-reload-save-pending')
for key in ['actualOriginalReloadNonNullLease', 'newOriginalSaveCommittedBeforeRevocation',
            'realWindowsDeleteShareFailureLeavesPendingWithoutRecoverySave', 'originalPlayerFileAndActualLeaseGenerationWholeItemUnchanged']:
    assert seed[key]
pending_result, pending = accepted('repair-reload-pending')
for key in ['actualNewServerBoot', 'previousOriginalPlayerFileWholeItemAndWearCoherent', 'originalLineageActiveAndMatchingBeforeLoad',
            'pendingSaveAndSecondReplayGrantedNoAuthority', 'pendingContinuationDocumentUnchanged', 'originalActiveLineageBytesUnchanged']:
    assert pending[key]
assert pending['originalTicketGeneration'] == seed['committedSavedGeneration']
wear_result, wear = accepted('repair-reload-wear-mismatch-utf8')
for key in ['actualNewServerBoot', 'originalPlayerFileWholeItemAndSignedCommittedTicketUnchanged', 'originalLineageActiveAndMatchingBeforeLoad',
            'independentlyAlteredWearDamageAndSecondReplayGrantedNoAuthority', 'committedContinuationDocumentUnchanged', 'originalActiveLineageBytesUnchanged']:
    assert wear[key]
mutation_path = Path(wear_result['attempt']) / 'wear-negative-input.json'
mutation = json.loads(mutation_path.read_text(encoding='utf-8'))
assert mutation['negativeInputOnly'] and mutation['originalPlayerFileUnchanged'] and mutation['originalSeedAndSignedAuthorityUnchanged']
assert mutation['alteredDamage'] == mutation['originalDamage'] + 1
assert mutation['copiedWearHashBefore'] != mutation['copiedWearHashAfter']
assert Path(mutation['copiedWorld']).resolve() == (Path(wear_result['attempt']) / 'server/world').resolve()
hashes[str(Path(__file__))] = hashlib.sha256(Path(__file__).read_bytes()).hexdigest()
result = {
    'candidate': 'native-repair-2ca083af',
    'actualRealDeleteShareFailedOriginalSaveTestedOnCurrentCandidate': True,
    'pendingSeedStoppedWithoutRecoveryPlayerSave': True,
    'newProcessPendingOriginalSaveAndReplayGrantNoAuthority': True,
    'pendingCheckpointOriginalWholeItemAndWearRemainCoherent': True,
    'pendingRejectionKeepsMatchingActiveLineageAndSignedDocumentUnchanged': True,
    'independentlyAlteredWearDamageInCopiedWorldOnly': True,
    'wearMismatchPlayerFileAndExactCommittedTicketAndActiveLineageUnchanged': True,
    'wearMismatchOriginalLoadAndReplayGrantNoAuthorityAndDoNotConsumeContinuation': True,
    'wearMutation': {'originalDamage': mutation['originalDamage'], 'alteredDamage': mutation['alteredDamage'],
                     'maximum': mutation['maximum'], 'negativeInputOnly': True},
    'processInterruptionBetweenActiveLineageCasAndPhysicalPublicationAccepted': False,
    'loaderFinalBytesAndTransformedCuriosFactoryAccepted': False,
    'authenticatedMultiplayerAccepted': False, 'fullLifecycleAccepted': False,
    'authorityWrittenByReflection': False, 'promoted': False,
    'installable': False, 'deployed': False, 'inputHashes': hashes,
}
output = WORK / 'reports/native-pending-and-wear-focused-evidence.json'
with output.open('x', encoding='utf-8') as stream:
    json.dump(result, stream, ensure_ascii=False, indent=2)
    stream.write(chr(10))
print({'evidence': str(output), 'pendingNewBootAndIndependentWearMismatchPassed': True, 'inputCount': len(hashes)})
