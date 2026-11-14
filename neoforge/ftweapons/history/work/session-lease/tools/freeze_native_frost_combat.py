import hashlib
import json
import re
import sys
from pathlib import Path

work = Path(__file__).resolve().parents[1]
tag = sys.argv[1]
assert re.fullmatch('[0-9a-f]{8}', tag)
hashes = {}


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def retain(path, expected=None):
    path = Path(path)
    actual = hashlib.sha256(path.read_bytes()).hexdigest()
    assert expected is None or expected == actual, path
    assert str(path) not in hashes or hashes[str(path)] == actual, path
    hashes[str(path)] = actual


def inputs(path):
    value = load(path)
    retain(path)
    for name, expected in value['inputHashes'].items():
        retain(name, expected)
    return value


delta = inputs(work / ('reports/native-frost-itemstack-proof-delta-' + tag + '.json'))
inputs(work / 'reports/native-frost-immutable-timer-delta-667b6466.json')
inputs(work / 'reports/native-frost-ingress-delta-9535b6b4.json')
assert delta['classCount'] == 1381 and delta['effectiveSourceCount'] == 716 and delta['guardCount'] == 208 and not delta['newProductClasses']
for name in ['observed-preparation', 'nova-preparation', 'nova-clear-preparation', 'post-combat-preparation']:
    inputs(work / ('reports/native-frost-' + name + '-' + tag + '.json'))
receipts = [work / ('reports/native-repair-runtime-' + stage + '-' + tag + '.json') for stage in ['paid-frost-ingress', 'paid-frost-nova-clear', 'frost-post-combat-restart']]
cases = []
for receipt in receipts:
    result = inputs(receipt)
    attempt = Path(result['attempt'])
    terminal = load(attempt / 'terminal308.json')
    assert result['exitCode'] == result['probe']['failures'] == 0
    assert terminal['stopSent'] and terminal['completeSave']
    assert [case['id'] for case in result['probe']['cases']] == ['ftboss:frost_queen_staff']
    cases.append(result['probe']['cases'][0])
    for name in ['terminal308.json', 'runtime308.log', 'live-process308.json', 'server/actual-storage-view308.json', 'restart-world-seed.json', 'compile-command308.json', 'compile308.log']:
        retain(attempt / name)
    world = attempt / 'server/world'
    for path in [world / 'level.dat', world / 'data/ftboss_native_weapon_wear.dat', *sorted((world / 'ftboss-authority').rglob('*.bin')), *sorted((world / 'playerdata').glob('*.dat'))]:
        retain(path)
    assert not [path for directory in ['ftboss-authority', 'playerdata'] for path in (world / directory).rglob('*') if path.is_file() and path.suffix in ['.tmp', '.pending']]
for case, amount, targets in zip(cases, [1, 4], [1, 2]):
    assert case['genuineNonNullLeaseAndGenerationBeforeAttack'] and case['actualItemStackUseResult'] == 'CONSUME'
    assert case['healthAfter'] < case['healthBefore'] and case['wearAfter'] - case['wearBefore'] == amount
    assert case['capturedNativeFrostPurpose'] == 'NATIVE_FROST_ACTUAL_HURT'
    assert case['sameLeaseAndGenerationAfterAttack'] and not case['generationRetiredAfterAttack']
    assert case['sameActualSourceAndSnapshotDeniedAfterFrostReturn'] and case['actualOriginalPostCombatSaveAuthenticatedCommitted']
    assert len(case['publications']) == targets
    assert all(publication['traceComplete'] and publication['preparedCommitted'] and publication['prepared_sealed'] and publication['prepared_bound'] and not publication['prepared_retired'] for publication in case['publications'])
    assert case['damageEvents'] and all(event['resolvedSnapshotValid'] and event['repairEpochPresent'] and event['serverSignaturePresent'] for event in case['damageEvents'])
assert cases[1]['foreignDirectFrostHurtDelegatesOnceAndPreservesArgumentsResultAndExceptionIdentity']
assert len(cases[1]['distinctActualSignedHurtIds']) == len(cases[1]['distinctActualSignedTargetIds']) == 2
assert cases[1]['originalCooldownActive'] and cases[1]['secondHealthAfter'] < cases[1]['secondHealthBefore']
assert all(cases[2][flag] for flag in ['originalWholeItemAndWearCoherent', 'genuineNonNullLeaseAndGeneration', 'sameEpochGenerationThreeToFour', 'consumedReplayDeniedWithoutChangingFirstSuccessor', 'actualOriginalGenerationFourSaveCommitted'])
for name in ['native-repair-runtime-paid-frost-ingress-9535b6b4.json', 'native-repair-runtime-paid-frost-ingress-667b6466.json', 'native-repair-runtime-frost-certificate-diagnostic-667b6466.json', 'native-repair-runtime-paid-frost-ingress-d036c8bb.json', 'native-repair-runtime-paid-frost-ingress-b8eb35ff.json', 'native-repair-runtime-frost-itemstack-export-b8eb35ff.json', 'native-repair-runtime-paid-frost-nova-' + tag + '.json']:
    failed_path = work / 'reports' / name
    failed = load(failed_path)
    failed_terminal = Path(failed['attempt']) / 'terminal308.json'
    assert failed['probe']['failures'] == 1
    retain(failed_path)
    retain(failed_terminal)
retain(Path(__file__))
output = work / ('reports/native-frost-paid-signed-combat-focused-evidence-' + tag + '.json')
assert not output.exists()
output.write_text(json.dumps({'candidate': delta['candidate'], 'acceptedRuntimeReceipts': [str(path) for path in receipts], 'actualOriginalRayAndTwoTargetNovaOnGenuinePaidLease': True, 'completeLiveSignedSessionsAndOriginalWearOneAndFour': True, 'sameActualSourceAndSnapshotDeniedOutsideActualHurt': True, 'foreignEntryCallbackPreservesOriginalArgumentsResultAndException': True, 'authenticatedOriginalPostCastSaveAndNewProcessGenerationThreeToFour': True, 'originalConsumedReplayDeniedAndSuccessorSaveCommitted': True, 'zeroDurabilityStaffAccepted': False, 'authorityWrittenByReflection': False, 'fullLifecycleAccepted': False, 'authenticatedMultiplayerAccepted': False, 'installable': False, 'deployed': False, 'inputHashes': hashes}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'hashes': len(hashes), 'focusedFailures': 0, 'completeMigration': False}))
