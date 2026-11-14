import hashlib
import json
import re
import sys
from pathlib import Path

work = Path(__file__).resolve().parents[1]
tag = sys.argv[1]
assert re.fullmatch('[0-9a-f]{8}', tag)


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


hashes = {}


def retain(path):
    path = Path(path)
    wanted = digest(path)
    assert str(path) not in hashes or hashes[str(path)] == wanted, str(path)
    hashes[str(path)] = wanted


def inputs(receipt):
    result = load(receipt)
    for name, wanted in result['inputHashes'].items():
        assert digest(name) == wanted, name
        assert name not in hashes or hashes[name] == wanted, name
        hashes[name] = wanted
    retain(receipt)
    return result


delta_path = work / ('reports/native-melee-wear-delta-' + tag + '.json')
delta = inputs(delta_path)
assert delta['classCount'] == 1381 and delta['effectiveSourceCount'] == 716 and delta['guardCount'] == 208
assert delta['targetedAsmFailures'] == 0 and delta['newProductClasses'] == 0
preparation = work / ('reports/native-melee-post-combat-preparation-' + tag + '.json')
inputs(preparation)
receipts = [work / ('reports/native-repair-runtime-paid-melee-ingress-' + tag + '.json'), work / ('reports/native-repair-runtime-melee-post-combat-restart-' + tag + '.json')]
expected = ['minecraft:iron_sword', 'minecraft:iron_axe']
results = []
for receipt in receipts:
    result = inputs(receipt)
    attempt = Path(result['attempt'])
    terminal = load(attempt / 'terminal308.json')
    assert result['exitCode'] == result['probe']['failures'] == 0
    assert terminal['stopSent'] and terminal['completeSave']
    assert [case['id'] for case in result['probe']['cases']] == expected
    for name in ['terminal308.json', 'runtime308.log', 'live-process308.json', 'server/actual-storage-view308.json', 'restart-world-seed.json', 'compile-command308.json', 'compile308.log']:
        retain(attempt / name)
    world = attempt / 'server/world'
    for path in [world / 'level.dat', world / 'data/ftboss_native_weapon_wear.dat', *sorted((world / 'ftboss-authority').rglob('*.bin')), *sorted((world / 'playerdata').glob('*.dat'))]:
        retain(path)
    assert not [path for directory in ['ftboss-authority', 'playerdata'] for path in (world / directory).rglob('*') if path.is_file() and path.suffix in ['.tmp', '.pending']]
    results.append(result)

flags = ['genuineNonNullLeaseAndGenerationBeforeAttack', 'repeatedOriginalEnsureTrackedPreservedExactComponentIdentityWholeImageAndLiveAuthority', 'foreignDirectEntryDelegatesExactlyOnceWithoutAuthorityOrDataMutation', 'foreignDirectEntryPreservesOriginalCallbackExceptionIdentity', 'originalPlayerAttackReturned', 'originalMeleeSnapshotValid', 'originalMeleeSnapshotRepairEpochPresent', 'sameLeaseAndGenerationAfterAttack', 'sameOriginalSourceAndSignedSnapshotDeniedOutsideActualHurtScope', 'actualOriginalPostCombatSaveCalled', 'actualOriginalPostCombatSaveAuthenticatedCommitted']
for index, case in enumerate(results[0]['probe']['cases']):
    assert all(case[flag] for flag in flags)
    assert not case['generationRetiredAfterAttack']
    assert case['wearBefore'] == 0 and case['wearAfter'] == index + 1 and case['healthAfter'] < case['healthBefore']
    assert case['capturedNativeMeleePurpose'] == 'NATIVE_MELEE_ACTUAL_HURT'
    assert case['publications'] and all(publication['traceComplete'] and publication['preparedCommitted'] and publication['prepared_bound'] and publication['prepared_sealed'] and not publication['prepared_retired'] for publication in case['publications'])
    assert case['damageEvents'] and all(event['resolvedSnapshotValid'] and event['repairEpochPresent'] and event['serverSignaturePresent'] for event in case['damageEvents'])
for case in results[1]['probe']['cases']:
    assert all(case[flag] for flag in ['originalWholeItemAndWearCoherent', 'genuineNonNullLeaseAndGeneration', 'sameEpochGenerationThreeToFour', 'consumedReplayDeniedWithoutChangingFirstSuccessor', 'actualOriginalGenerationFourSaveCommitted'])
assert results[1]['probe']['paidSwordAndAxePostCombatContinuationAcrossNewProcess']
retain(Path(__file__))
output = work / ('reports/native-melee-paid-signed-combat-focused-evidence-' + tag + '.json')
assert not output.exists()
output.write_text(json.dumps({'candidate': delta['newCandidate'], 'acceptedRuntimeReceipts': [str(path) for path in receipts], 'familiesActuallyTested': expected, 'realGenuinePaidLeaseGenerationThreeSignedMeleeSession': True, 'originalSwordOneAndAxeTwoWearWithLiveAuthority': True, 'originalPostCombatAuthenticatedContinuationSaveCommitted': True, 'actualNewProcessSameEpochGenerationThreeToFourAndConsumedReplayDenied': True, 'actualNewProcessWholeItemAndWearCoherentAndOriginalSuccessorSaveCommitted': True, 'foreignDirectEntryNoAuthorityAndOriginalExceptionIdentityPreserved': True, 'sameActualSourceAndSignedSnapshotDeniedAfterHurtScope': True, 'authorityWrittenByReflection': False, 'classCount': 1381, 'effectiveSourceCount': 716, 'guardCount': 208, 'completeSixFamilySignedCombatAccepted': False, 'fullLifecycleAccepted': False, 'authenticatedMultiplayerAccepted': False, 'installable': False, 'deployed': False, 'inputHashes': hashes}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'output': str(output), 'hashes': len(hashes), 'focusedFailures': 0, 'completeMigration': False}))
