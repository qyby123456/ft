import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]

def load(path):
    return json.loads(path.read_text(encoding='utf-8'))

def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()

candidate = work / 'reports/native-repair-six-family-candidate-7751b0a9.json'
assert digest(candidate) == '4a3b9bf6c0acc7499b1183469f370a90a4b1105a660e7f25370598ee39aae847'
delta_path = work / 'reports/native-six-family-policy-delta-7751b0a9.json'
delta = load(delta_path)
hashes = dict(delta['inputHashes'])
receipts = [work / 'reports/native-repair-runtime-six-family-paid-repair-task-custody.json',
            work / 'reports/native-repair-runtime-six-family-paid-restart.json']
expected = ['minecraft:iron_sword', 'minecraft:bow', 'minecraft:crossbow', 'minecraft:iron_axe', 'minecraft:trident', 'ftboss:frost_queen_staff']
results = []
for receipt in receipts:
    result = load(receipt)
    attempt = Path(result['attempt'])
    terminal = load(attempt / 'terminal308.json')
    assert result['exitCode'] == result['probe']['failures'] == 0
    assert terminal['stopSent'] and terminal['completeSave']
    assert [case['id'] for case in result['probe']['cases']] == expected
    for name, wanted in result['inputHashes'].items():
        assert name not in hashes or hashes[name] == wanted, name
        hashes[name] = wanted
    for path in [receipt, attempt / 'terminal308.json', attempt / 'runtime308.log', attempt / 'live-process308.json',
                 attempt / 'server/actual-storage-view308.json', attempt / 'restart-world-seed.json', attempt / 'compile-command308.json', attempt / 'compile308.log']:
        hashes[str(path)] = digest(path)
    results.append(result)
for case in results[0]['probe']['cases']:
    assert case['nonNullLease'] and case['repairGenerationPresent'] and case['damageAfterRepair'] == 0
    assert case['actualSuccessorOriginalSaveCommitted'] and case['oldPhysicalGenerationRetired']
    assert case['sameEpochGenerationZeroToOne'] and case['consumedReplayDeniedAndFirstSuccessorUnchanged']
    transactions = case['repairTransactions']
    assert [transaction['operation'] for transaction in transactions] == ['START_REPAIR', 'SUBMIT_REPAIR', 'SUBMIT_REPAIR', 'SUBMIT_REPAIR', 'CLAIM_REPAIR']
    assert [transaction['expectedPayment'] for transaction in transactions] == [0, 3, 3, 2, 0]
    assert all(transaction['remainingSupply'] == 0 for transaction in transactions[1:4])
    if not case['originalFoundationApplicable']:
        assert [transaction['signedSequence'] for transaction in transactions] == [1, 2, 3, 4, 5]
        assert transactions[-1]['signedStatus'] == 'LEASE_ISSUED'
    else:
        assert all(transaction['accepted'] and transaction['code'] == 'COMMITTED' for transaction in transactions)
    assert case['syntheticLocalMinecraftRegisterAdvertisementNotAuthenticatedHandshake'] and not case['configureMockConnectionUsed']
for case in results[1]['probe']['cases']:
    assert case['genuineNonNullLeaseAndGeneration'] and case['sameEpochGenerationOneToTwo']
    assert case['consumedReplayDeniedWithoutChangingFirstSuccessor'] and case['actualOriginalGenerationTwoSaveCommitted']
failed = []
for name, cause in [
    ('native-repair-runtime-six-family-paid-repair.json', 'Fixture erroneously forced four-weapon Foundation initialization on legacy sword; failed before payment.'),
    ('native-repair-runtime-six-family-paid-repair-applicable.json', 'Fixture erroneously routed legacy sword through Foundation-only transaction open; original DENIED preserved.'),
    ('native-repair-runtime-six-family-paid-repair-original-routes.json', 'Synthetic local channel had no response payload advertisement; original network check threw and product rolled back.'),
    ('native-repair-runtime-six-family-paid-repair-local-channels.json', 'Observer read cleared NativeTask after successful CLAIM; corrected observer retains previously observed original task UUID.')]:
    path = work / 'reports' / name
    result = load(path)
    terminal_path = Path(result['attempt']) / 'terminal308.json'
    terminal = load(terminal_path)
    assert result['exitCode'] == 0 and result['probe']['failures'] == 1 and terminal['completeSave']
    failed.append({'attempt': result['attempt'], 'cause': cause, 'accepted': False})
    hashes[str(path)] = digest(path); hashes[str(terminal_path)] = digest(terminal_path)
for path in [candidate, delta_path, Path(__file__), work / 'tools/prepare_six_family_paid_repair.py', work / 'reports/six-family-paid-repair-helper-origin.json']:
    hashes[str(path)] = digest(path)
for name, wanted in hashes.items():
    assert digest(name) == wanted, name
output = work / 'reports/native-six-family-paid-repair-restart-focused-evidence.json'
assert not output.exists()
output.write_text(json.dumps({
    'candidate': str(candidate), 'registeredRepresentativeIds': expected,
    'acceptedRuntimeAttempts': [result['attempt'] for result in results],
    'onlyNewExactAuthorityItemClasses': ['net.minecraft.world.item.AxeItem', 'net.farmingtales.ftboss.item.FrostQueenStaffItem'],
    'allSixActualStartThreeOriginalPaymentsAndClaimIssuedNonNullGenuineLeaseAndGeneration': True,
    'paymentCountsPerRepresentative': [3, 3, 2],
    'legacyRepresentativesUsedOriginalStatueMenuExecuteRepairAndAuthenticatedJournalSequenceOneToFive': True,
    'foundationRepresentativesUsedOriginalTransactionAuthorityAndCommittedReplies': True,
    'originalWholeItemAndWearMatchedOnActualLoad': True,
    'sameEpochZeroToOneSameProcessAndOneToTwoRealNewProcess': True,
    'allTwelveConsumedReplayLoadsDeniedReissueWithoutChangingFirstSuccessor': True,
    'allOriginalSuccessorSavesCommittedAndBothProcessesNormallySaved': True,
    'syntheticLocalMinecraftChannelAdvertisementUsed': True,
    'negotiatedOrAuthenticatedClientHandshakeClaimed': False,
    'productionNetworkChecksDisabledOrConfigureMockConnectionUsed': False,
    'preservedExcludedFixtureRuns': failed,
    'prior2ca083afFrozenEvidenceRetainsOriginalCandidateScope': True,
    'priorEvidenceAutomaticallyRelabeledForNewCandidate': False,
    'remainingAcceptance': [
        'actual signed combat Sessions for six retained families, including staff casting and axe melee',
        'retained subclasses/external provider and fractional wear/lifecycle matrix',
        'eight registered staff with zero original native durability use separate lifecycle paths; no arbitrary durability added',
        'new-candidate rejection/failure-path audit with justified frozen-evidence reuse',
        'recipes, retired-feature closure, real client/dedicated/authenticated multiplayer, four GL directions and final migration'
    ],
    'newProductClasses': 0, 'authorityWrittenByReflection': False, 'authorityForgedOrRetiredStateReset': False,
    'authenticatedMultiplayerAccepted': False, 'fullSixFamilyRuntimeMatrixAccepted': False,
    'fullSignedCombatSessionRunByTheseFixtures': False, 'fullLifecycleAccepted': False,
    'promoted': False, 'installable': False, 'deployed': False, 'inputHashes': hashes
}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'sixRepresentativePaidRepairAndRealRestart': True, 'hashes': len(hashes), 'failures': 0}))
