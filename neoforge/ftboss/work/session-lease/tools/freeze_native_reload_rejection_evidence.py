import hashlib
import json
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
hashes = {}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def accepted(name):
    path = WORK / 'reports' / ('native-repair-runtime-' + name + '.json')
    result = json.loads(path.read_text(encoding='utf-8-sig'))
    terminal_path = Path(result['attempt']) / 'terminal308.json'
    terminal = json.loads(terminal_path.read_text(encoding='utf-8-sig'))
    assert result['exitCode'] == terminal['exitCode'] == result['probe']['failures'] == 0
    assert terminal['stopSent'] and terminal['completeSave']
    for source, wanted in result['inputHashes'].items():
        assert digest(Path(source)) == wanted
        hashes[source] = wanted
    for source in [path, terminal_path]:
        hashes[str(source)] = digest(source)
    return result['probe']


negative = accepted('repair-reload-negative')
assert negative['negativeOriginalLoadCount'] == len(negative['cases']) == 12
assert negative['validControlRetainedUnusedSuccessor'] and negative['rawProviderAbaRetiredActualReloadedLeaseAndInput']
assert negative['playerFilePreservedAcrossNegativesAndRevocation']
assert {case['mutation'] for case in negative['cases']} == {
    'save', 'owner', 'weapon', 'identity', 'epoch', 'generation', 'slot', 'shape', 'missing', 'wrong_type', 'whole_item', 'duplicate_weapon'}
for case in negative['cases']:
    assert case['originalLoadRan'] and case['noLeaseOrGeneration'] and case['committedDocumentAndAuthorityBytesUnchanged']
revoked = accepted('repair-reload-save-revoke')
for key in ['actualOriginalReloadNonNullLease', 'newOriginalSaveCommittedBeforeRevocation',
            'rawProviderAbaRetiredActualReloadedLeaseAndInput', 'validCommittedContinuationAndWholeItemPreservedAfterRevocation']:
    assert revoked[key]
inactive = accepted('repair-reload-inactive')
for key in ['actualNewServerBoot', 'originalUnusedCommittedTicketAndWholeItemCoherent', 'originalLineageInactiveBeforeLoad',
            'inactiveLineageAndSecondReplayGrantedNoAuthority', 'continuationReservedNoReissue', 'originalInactiveLineageBytesUnchanged']:
    assert inactive[key]
assert inactive['originalTicketGeneration'] == revoked['committedSavedGeneration']
timer = accepted('repair-reload-timer-aba')
for key in ['actualOriginalReloadNonNullLease', 'newOriginalSaveCommittedBeforeRevocation',
            'publicTimerAbaRetiredActualReloadedLeaseAndInput', 'validCommittedContinuationAndWholeItemPreservedAfterRevocation']:
    assert timer[key]
positive = WORK / 'reports/native-reload-successor-focused-evidence.json'
assert json.loads(positive.read_text(encoding='utf-8'))['completeOriginalSignedSessionAfterBothRestarts']
for source in [positive, Path(__file__)]:
    hashes[str(source)] = digest(source)
result = {
    'candidate': 'native-repair-2ca083af',
    'modifiedOriginalPlayerNbtNegativeLoadCases': 12,
    'modifiedTicketAndWholeItemAndDuplicateWeaponRejectedWithoutConsumingValidContinuation': True,
    'authenticatedDocumentAndOriginalAuthorityBytesUnchangedAcrossTwelveNegatives': True,
    'validOriginalControlRetainsItsOneUseSuccessor': True,
    'actualRawProviderAbaRetiresRestoredLeaseAndInput': True,
    'actualPublicTimerAbaRetiresRestoredLeaseAndInput': True,
    'newSuccessfulOriginalSaveThenRealProviderRevocationKeepsCommittedTicketAndWholeItem': True,
    'newProcessUnusedCommittedTicketAndCoherentWearCannotReviveInactiveLineage': True,
    'inactiveLineageBytesUnchangedAndReservedContinuationDoesNotReissue': True,
    'failedSavePendingReloadAccepted': False,
    'interruptedReservedContinuationNewBootAccepted': False,
    'sameProcessLogoutLoginAccepted': False,
    'authenticatedMultiplayerAccepted': False,
    'fullLifecycleAccepted': False,
    'authorityWrittenByReflection': False,
    'promoted': False, 'installable': False, 'deployed': False, 'inputHashes': hashes,
}
output = WORK / 'reports/native-reload-rejection-focused-evidence.json'
with output.open('x', encoding='utf-8') as stream:
    json.dump(result, stream, ensure_ascii=False, indent=2)
    stream.write(chr(10))
print({'evidence': str(output), 'focusedReloadRejectionsPassed': True, 'inputCount': len(hashes)})
