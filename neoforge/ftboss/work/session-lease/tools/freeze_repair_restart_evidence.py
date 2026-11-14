import hashlib
import json
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
hashes = {}


def load_checked(name):
    path = WORK / 'reports' / ('native-repair-runtime-' + name + '.json')
    result = json.loads(path.read_text(encoding='utf-8-sig'))
    terminal_path = Path(result['attempt']) / 'terminal308.json'
    terminal = json.loads(terminal_path.read_text(encoding='utf-8-sig'))
    assert result['exitCode'] == 0 and result['probe']['failures'] == 0
    assert terminal['exitCode'] == 0 and terminal['stopSent'] and terminal['completeSave']
    for source in [path, terminal_path]:
        hashes[str(source)] = hashlib.sha256(source.read_bytes()).hexdigest()
    return result['probe']


replay = load_checked('repair-replay-credential')['cases'][0]
transactions = replay['repairTransactions']
assert len(transactions) == 5
for operation in transactions:
    assert operation['accepted'] and operation['exactDuplicateReusedTerminalReply']
    assert operation['conflictCode'] == 'REQUEST_CONFLICT' and operation['staleCode'] == 'STALE'
    assert operation['replayWeaponAndSuppliesUnchanged']
    assert operation['replayLeaseAndGenerationIdentitiesUnchanged']
journal = load_checked('repair-journal')['cases'][0]
assert journal['signedJournalSequence'] == 5 and journal['signedJournalStatus'] == 'LEASE_ISSUED'
assert journal['signedJournalActualPaid'] == [3, 3, 2]
for key in ['signedJournalFreshStoreReadMatches', 'signedJournalCorruptCopyRejected',
            'signedJournalOriginalBytesUnchanged', 'journalReadReissuedNoAuthority']:
    assert journal[key]
orphan = load_checked('repair-orphan-restart')
for key in ['actualNewServerBoot', 'stalePersistedWeaponDiffersFromClaim',
            'orphanIssuedJournalDidNotReissueAuthority', 'claimNotAvailableFromOrphan']:
    assert orphan[key]
seed = load_checked('repair-partial-seed-native-task')['cases'][0]
resume = load_checked('repair-partial-resume')['cases'][0]
assert seed['partialRepairActualProgress'] == resume['loadedPartialPayment'] == [3, 0, 0]
assert seed['partialRepairTaskIdentity'] == resume['loadedPartialTaskIdentity']
assert resume['actualNewServerBoot'] and resume['partialTaskLoadedFromOriginalPlayerSave']
assert resume['resumedOriginalSignedTaskSequence'] == 5
assert resume['resumedOriginalSignedTaskStatus'] == 'LEASE_ISSUED'
assert resume['resumedOriginalSignedTaskPaid'] == [3, 3, 2]
assert resume['nonNullLease'] and resume['repairGenerationPresent'] and resume['attackReturned']
assert resume['targetHealthAfter'] < resume['targetHealthBefore']
assert resume['publications'] and all(entry['traceComplete'] and not entry['prepared_retired']
                                    for entry in resume['publications'])
candidate = WORK / 'reports/native-repair-candidate-24201031.json'
hashes[str(candidate)] = hashlib.sha256(candidate.read_bytes()).hexdigest()
assert hashes[str(candidate)] == 'e863d86b83c24b9158b15280afbf0e5996d48dcda97e33821d153543b7d79309'
evidence = {
    'candidate': 'native-repair-24201031',
    'fiveOperationExactReplayConflictAndStaleFocusedAccepted': True,
    'signedJournalFreshStoreAndCorruptCopyFocusedAccepted': True,
    'actualOrphanRestartNoAuthorityReissueFocusedAccepted': True,
    'actualPartialPaymentRestartSameTaskContinuationFocusedAccepted': True,
    'sameTaskIdentity': resume['loadedPartialTaskIdentity'],
    'beforePayment': [3, 0, 0], 'afterPayment': [3, 3, 2],
    'finalJournalSequence': 5, 'finalJournalStatus': 'LEASE_ISSUED',
    'genuineNonNullLeaseSignedSessionAfterRestart': True,
    'authorityWrittenByReflection': False, 'authenticatedMultiplayerAccepted': False,
    'fullyClaimedItemRestartAccepted': False, 'abortRecoveryAccepted': False,
    'fullLifecycleAccepted': False, 'promoted': False, 'installable': False, 'deployed': False,
    'inputHashes': hashes,
}
output = WORK / 'reports/repair-replay-journal-restart-focused-evidence.json'
with output.open('x', encoding='utf-8') as stream:
    json.dump(evidence, stream, ensure_ascii=False, indent=2)
    stream.write('\n')
print({'evidence': str(output), 'checksPassed': True, 'fullLifecycleAccepted': False})
