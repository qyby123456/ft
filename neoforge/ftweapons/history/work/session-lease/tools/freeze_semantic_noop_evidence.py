import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
hashes = {}

def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def remember(path):
    path = Path(path)
    hashes[str(path)] = digest(path)

def verify_inputs(receipt):
    for name, expected in receipt['inputHashes'].items():
        path = Path(name)
        assert digest(path) == expected, name
        hashes[name] = expected

owner = load(root / 'WRITE_OWNER.json')
assert owner['activeWriterThreadId'] == load(root / 'WRITE_OWNER.claim')['threadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
candidate_path = work / 'reports/native-repair-semantic-noop-candidate-524a9d86.json'
assert digest(candidate_path) == '64eae770e091193ef783dc46068cbbd0e66b9feb5786e1aec06df96074984adb'
candidate = load(candidate_path)
verify_inputs(candidate)
remember(candidate_path)
delta_path = work / 'reports/native-semantic-noop-delta-524a9d86.json'
verify_inputs(load(delta_path))
remember(delta_path)
runtime_path = work / 'reports/native-repair-runtime-semantic-noop-regression-524a9d86.json'
runtime = load(runtime_path)
terminal_path = Path(runtime['attempt']) / 'terminal308.json'
terminal = load(terminal_path)
assert runtime['exitCode'] == terminal['exitCode'] == runtime['probe']['failures'] == 0
assert terminal['completeSave'] and terminal['stopSent']
assert len(runtime['probe']['cases']) == 6
for case in runtime['probe']['cases']:
    for flag in ['genuineNonNullLeaseAndGenerationBeforeAttack', 'repeatedOriginalEnsureTrackedPreservedExactComponentIdentityWholeImageAndLiveAuthority',
                 'repeatedOriginalEvolutionSavePreservedExactComponentIdentityWholeImageAndLiveAuthority',
                 'realChangeAndRestoredWholeImageAbaNeverRevivedGeneration', 'negativeControlUsesOrdinaryDataMutationNotExpectedGameplayWear']:
        assert case[flag], (case['id'], flag)
    assert not case.get('publications')
verify_inputs(runtime)
remember(runtime_path)
remember(terminal_path)
bow_path = work / 'reports/native-repair-runtime-semantic-noop-bow-524a9d86.json'
bow = load(bow_path)
bow_terminal_path = Path(bow['attempt']) / 'terminal308.json'
bow_terminal = load(bow_terminal_path)
assert bow['exitCode'] == bow_terminal['exitCode'] == bow['probe']['failures'] == 0
assert bow_terminal['completeSave'] and bow_terminal['stopSent']
bow_case = bow['probe']['cases'][0]
for flag in ['nonNullLeaseAfterActualReload', 'repairGenerationAfterActualReload', 'originalContinuationTicketConsumed',
             'reloadedSuccessorKeptOriginalEpoch', 'replayedConsumedTicketGrantedNoLease', 'replayKeptFirstGenuineSuccessorLive',
             'reloadedPaidWeaponSavedAfterSignedCombat', 'attackReturned']:
    assert bow_case[flag], flag
assert len(bow_case['publications']) == 1
assert bow_case['publications'][0]['traceComplete'] and not bow_case['publications'][0]['prepared_retired']
verify_inputs(bow)
remember(bow_path)
remember(bow_terminal_path)
excluded = []
for name in ['paid-melee-sessions', 'paid-melee-noop', 'paid-staff-session', 'semantic-noop-signed-bow']:
    path = work / ('reports/native-repair-runtime-' + name + '.json')
    result = load(path)
    actual_terminal_path = Path(result['attempt']) / 'terminal308.json'
    actual_terminal = load(actual_terminal_path)
    assert result['exitCode'] == actual_terminal['exitCode'] == 0 and result['probe']['failures'] == 1
    assert actual_terminal['completeSave'] and actual_terminal['stopSent']
    verify_inputs(result)
    remember(path)
    remember(actual_terminal_path)
    excluded.append({'report': str(path), 'attempt': str(result['attempt']), 'probeFailures': 1, 'acceptedAsCombatPass': False})
for path in [Path(__file__), work / 'SEMANTIC_NOOP_AND_COMBAT_GAPS.md']:
    remember(path)
output = work / 'reports/native-semantic-noop-focused-evidence.json'
assert not output.exists()
report = {
    'candidate': str(candidate_path), 'sixNativeDamageableRepresentativesOriginalPaidGenerationThreeReloaded': True,
    'repeatedOriginalTrackingAndEvolutionNoopsPreserveExactComponentIdentityWholeImageAndLiveAuthority': True,
    'actualChangedDataStillPersistsAndRetiresGenerationEvenAfterWholeImageAba': True,
    'mutationNegativeControlIsNotAuthorizedGameplayWear': True,
    'originalSaveDataPrivateEntryInvokedReflectivelyWithoutAuthorityWrites': True,
    'realPaidBowOriginalReloadReplayRejectionAndFullSignedSessionOnNewCandidate': True,
    'bowRuntime': str(bow_path), 'regressionRuntime': str(runtime_path),
    'bowPostCombatSaveOriginalCalled': True, 'bowPostCombatNewProcessReloadNotTestedHere': True,
    'onlyThreeBehavioralSourceChangesWithExactDeltaProof': True,
    'unchangedLifecyclePlanRequiresExactPhysicalIdentityAndIndependentExpectedBeforeEquality': True,
    'originalNativeTransitionStillChecksMutationVersionsExpectedAfterLeaseAndGeneration': True,
    'classCount': 1381, 'effectiveSourceCount': 716, 'guardCount': 208, 'targetedAsmFailures': 0,
    'failedCombatProbesPreservedAndExcluded': excluded,
    'ordinaryMeleeSignedSessionAccepted': False, 'staffSignedSessionAccepted': False,
    'authenticatedMultiplayerAccepted': False, 'fullLifecycleAccepted': False,
    'installable': False, 'deployed': False, 'inputHashes': hashes
}
output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
assert digest(root / 'work/session-chain/reports/actual-current.json') == '1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243'
live = load(Path(bow['attempt']) / 'live-process308.json')
owner['lastRuntimePid'] = live['pid']
owner['lastRuntimeTerminal'] = True
owner['ownedJavaOrPythonProcessesRemaining'] = False
owner['latestNativeRepairPrototype'] = candidate_path.relative_to(root).as_posix()
owner['latestNativeRepairPrototypeSha256'] = digest(candidate_path)
owner['latestNativeSemanticNoopEvidence'] = output.relative_to(root).as_posix()
owner['latestNativeSemanticNoopDeltaEvidence'] = delta_path.relative_to(root).as_posix()
owner['nativeCombatGapProtocol'] = 'work/session-lease/SEMANTIC_NOOP_AND_COMBAT_GAPS.md'
owner['nativeContinuationStatus'] = '524a9d86 six-family exact-component no-op/real-mutation ABA regression plus actual paid bow signed Session accepted; ordinary melee/staff source entry remains open; previous family/stage5 evidence retains original scope; unpromoted NOT-INSTALLABLE'
(root / 'WRITE_OWNER.json').write_text(json.dumps(owner, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'verifiedHashes': len(hashes), 'latestPrototype': '524a9d86', 'sixFamilyNoopMutationAbaFailures': 0, 'paidBowFullSignedSession': True, 'meleeAndStaffCombatAccepted': False, 'sameWriterRetained': True, 'installable': False}))
