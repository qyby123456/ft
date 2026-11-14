import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]

def load(path):
    return json.loads(path.read_text(encoding='utf-8'))

def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()

def save(path, value):
    assert not path.exists(), str(path)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

candidate = work / 'reports/native-repair-candidate-2ca083af.json'
assert digest(candidate) == '842ff9f8dbc7d746fa9979595e4172ff16c0279fc92bb91fa0c9b92c7652aa74'
receipts = [work / 'reports/native-repair-runtime-repair-reload-corrupt-continuation-canonical.json',
            work / 'reports/native-repair-runtime-repair-reload-after-corrupt-recovery.json']
hashes = {str(candidate): digest(candidate), str(Path(__file__)): digest(Path(__file__))}
results = []
for receipt in receipts:
    result = load(receipt)
    attempt = Path(result['attempt'])
    terminal = load(attempt / 'terminal308.json')
    assert result['exitCode'] == result['probe']['failures'] == 0
    assert terminal['completeSave'] and terminal['stopSent']
    for name, wanted in result['inputHashes'].items():
        assert digest(name) == wanted, name
        assert name not in hashes or hashes[name] == wanted
        hashes[name] = wanted
    for path in [receipt, attempt / 'terminal308.json', attempt / 'live-process308.json', attempt / 'runtime308.log',
                 attempt / 'server/actual-storage-view308.json', attempt / 'restart-world-seed.json']:
        hashes[str(path)] = digest(path)
    results.append(result)
first, restart = (result['probe'] for result in results)
assert first['allFourCorruptOrMissingCasesAndEightOriginalLoadsDeniedAuthority']
assert first['actualOriginalLoadAfterBackupRestorationIssuedGenuineSuccessor']
assert first['allOtherSignedAuthorityBinsAndOriginalPlayerWearUnchangedDuringRejections']
assert first['actualOriginalPostRecoverySaveCommitted']
assert restart['originalLoadHasGenuineNonNullLeaseAndGeneration'] and restart['sameEpochGenerationOneToTwo']
assert restart['consumedReplayNoReissueAndFirstSuccessorUnchanged'] and restart['actualOriginalPostRestartSaveCommitted']
failed = work / 'reports/native-repair-runtime-repair-reload-corrupt-continuation.json'
failure = load(failed)
assert failure['exitCode'] == 0 and failure['probe']['failures'] == 1
failed_attempt = Path(failure['attempt'])
assert load(failed_attempt / 'terminal308.json')['completeSave']
for path in [failed, failed_attempt / 'terminal308.json', failed_attempt / 'server/actual-storage-view308.json',
             failed_attempt / 'runtime308.log', Path(results[0]['attempt']) / 'preboot-document-corruption.json']:
    hashes[str(path)] = digest(path)
evidence = work / 'reports/native-corrupt-continuation-recovery-focused-evidence.json'
save(evidence, {
    'candidate': str(candidate), 'acceptedRuntimeAttempts': [result['attempt'] for result in results],
    'actualBootWithHmacCorruptedContinuation': True,
    'cases': [entry['mode'] for entry in first['cases']],
    'eightOriginalLoadsAndReplaysGrantNoAuthority': True,
    'wholeItemWearCoherentAndOriginalActiveLineageMatchingBeforeRejections': True,
    'corruptOrMissingDocumentAndAllOtherSignedBinsPlayerWearUnchangedDuringRejections': True,
    'explicitExactUnusedAuthenticatedBackupRestoreThenOriginalLoadIssuesGenuineSuccessor': True,
    'restoreDoesNotResetRetiredLineageOrReopenClaim': True,
    'automaticProductBackupRecoveryClaimed': False,
    'sameEpochGenerationZeroToOneThenOneToTwoOnRealNormallySavedNewProcess': True,
    'bothConsumedReplayChecksDenyReissueAndPreserveFirstSuccessor': True,
    'bothOriginalPostRecoverySavesCommittedAndProcessesStoppedNormally': True,
    'preservedExcludedFixtureFailure': {'attempt': failure['attempt'], 'failures': 1, 'javaExitCode': 0,
        'cause': 'Observer compared relative Files.walk paths with absolute excluded document path, incorrectly including the deliberately altered target in all-other-authority invariants.',
        'correction': 'Separate immutable canonical-path observer; no product change or weakened invariant.'},
    'newProductClasses': 0, 'authorityWrittenByReflection': False,
    'copiedDocumentFaultInjectionAndExactUnusedBackupRestore': True,
    'authorityForgedOrResigned': False, 'retiredStateReset': False, 'testMixinUsed': False,
    'authenticatedMultiplayerAccepted': False, 'fullLifecycleAccepted': False,
    'promoted': False, 'installable': False, 'deployed': False, 'inputHashes': hashes
})
prior_path = work / 'reports/native-continuation-acceptance-2ca083af-stage4.json'
prior = load(prior_path)
merged = dict(prior['inputHashes'])
for name, wanted in hashes.items():
    assert name not in merged or merged[name] == wanted
    merged[name] = wanted
merged[str(prior_path)] = digest(prior_path)
merged[str(evidence)] = digest(evidence)
for name, wanted in merged.items():
    assert digest(name) == wanted, name
remaining = [entry for entry in prior['remainingAcceptance'] if 'corrupt-continuation' not in entry.lower()]
remaining.insert(0, 'Full lifecycle/final recovery audit beyond the focused corrupt/absent continuation and exact-unused-backup recovery cases remains open.')
save(work / 'reports/native-continuation-acceptance-2ca083af-stage5.json', {
    'candidate': prior['candidate'], 'priorFrozenAcceptance': str(prior_path), 'newFocusedEvidence': str(evidence),
    'frozenSignedSessionsRestartCrashPendingWearAndJvmDefinitionProofReusedWithoutRerun': True,
    'fourCorruptOrMissingContinuationCasesAndExplicitUnusedBackupRecoveryNormalRestartAccepted': True,
    'remainingAcceptance': remaining, 'fullLifecycleAccepted': False, 'fullMigrationAccepted': False,
    'promoted': False, 'installable': False, 'deployed': False, 'inputHashes': merged
})
print(json.dumps({'focusedHashes': len(hashes), 'stage5Hashes': len(merged), 'failures': 0}))
