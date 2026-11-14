import hashlib
import json
import re
from pathlib import Path

work = Path(__file__).resolve().parents[1]
helper = work / 'tools/prove_native_ranged_use_head_delta.py'
head = work / 'reports/native-ranged-use-head-delta-f4f2deae.json'
assert hashlib.sha256(helper.read_bytes()).hexdigest() == json.loads(head.read_text(encoding='utf-8'))['inputHashes'][str(helper)]
exec(compile(helper.read_text(encoding='utf-8').split('before = load(before_path)')[0], str(helper), 'exec'))
before_path = work / 'reports/native-repair-ranged-use-candidate-9d1607e8.json'
after_path = work / 'reports/native-repair-ranged-use-candidate-04a9311d.json'
before = load(before_path)
after = load(after_path)
assert digest(before_path) == '7a0661df7ceb74920c4ccf4db3425fb7e444e1e75eddfb6e047376b908fb5e97'
for receipt in [before, after]:
    assert receipt['classCount'] == 1381 and receipt['effectiveSourceCount'] == 716 and receipt['guardCount'] == 208
    assert receipt['failures'] == 0 and not receipt['newClasses']
    for name, expected in receipt['inputHashes'].items():
        assert digest(Path(name)) == expected, name
previous = source_map(before)
current = source_map(after)
assert previous.keys() == current.keys()
changed = {key for key in previous if previous[key][1] != current[key][1]}
behavior = {'net.farmingtales.ftboss.item.FoodWeaponLifecycle', 'net.farmingtales.ftboss.item.FoodWeaponSourceLedger',
            'net.farmingtales.ftboss.item.OwnerFullRepairGuard', 'net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority',
            'net.farmingtales.ftboss.item.WeaponMaterialBuild'}
bindings = 'net.farmingtales.ftboss.item.NativeCapabilityInstallationProof170'
assert changed == behavior | {bindings}
pattern = r'(Map\.entry\("[^"]+", ")[a-f0-9]{64}("\))'
assert re.sub(pattern, r'\1SHA256\2', previous[bindings][1]) == re.sub(pattern, r'\1SHA256\2', current[bindings][1])
runtime_path = work / 'reports/native-repair-runtime-trident-foundation-gate-04a9311d.json'
runtime = load(runtime_path)
attempt = Path(runtime['attempt'])
terminal_path = attempt / 'terminal308.json'
terminal = load(terminal_path)
assert runtime['exitCode'] == terminal['exitCode'] == 0 and terminal['stopSent'] and terminal['completeSave']
assert runtime['probe']['failures'] == 1
assert 'did not damage target and complete live signed Session' in runtime['probe']['failure']
cases = runtime['probe']['cases']
assert len(cases) == 6
for case in cases:
    assert case['genuineNonNullLeaseAndGenerationBeforeAttack'] and not case['lifecycleBrokenBeforeUse']
    assert case['dispatchedMaterialFactor'] == case['nativeFoundationMaterialFactor'] == 1.0
trident = cases[-1]
assert trident['id'] == 'minecraft:trident'
for key in ['missingNativeCellsDenied', 'malformedNativeFoundationDenied', 'sameGenuineAuthorityAfterGateControls', 'actualUseStarted',
            'earlyImageSame', 'earlyLeaseSame', 'earlyGenerationSame', 'earlyGenerationLive', 'earlyHandSame', 'earlyCurrentInputLive',
            'earlyPreviousInputRetired', 'earlyPaidStateLiveWithObservedSemanticTransition', 'actualOriginalReleaseReturned',
            'originalMainHandEmptyAfterThrow', 'sameLeaseAndGenerationAfterAttack']:
    assert trident[key], key
for key in ['earlyInputSame', 'generationRetiredAfterUse', 'generationRetiredAfterRelease', 'generationRetiredAfterAttack']:
    assert not trident[key], key
assert trident['actualOriginalUseResult'] == 'CONSUME'
assert trident['wearBefore'] == 0 and trident['wearAfter'] == 1 and trident['healthAfter'] < trident['healthBefore']
flights = trident['actualProjectileFlights']
assert len(flights) == 1
for key in ['snapshotPresent', 'snapshotValid', 'repairEpochPresent', 'signaturePresent', 'paidCarrierPresent']:
    assert flights[0][key], key
assert flights[0]['purpose'] == 'NATIVE_PROJECTILE' and flights[0]['nativeEntry'] == 'TRIDENT'
assert not trident['publications']
for name, expected in runtime['inputHashes'].items():
    assert digest(Path(name)) == expected, name
preparation = work / 'reports/native-trident-foundation-gate-preparation-04a9311d.json'
for name, expected in load(preparation)['inputHashes'].items():
    assert digest(Path(name)) == expected, name
for path in [Path(__file__), helper, head, preparation, runtime_path, terminal_path, attempt / 'runtime308.log',
             work / 'reports/trident-abstract-arrow-original-constructor-6a38a668.javap.txt',
             work / 'reports/trident-origin-sync-original-mixin-c0607cae.javap.txt']:
    digest(path)
output = work / 'reports/native-trident-paid-signed-carrier-evidence-04a9311d.json'
assert not output.exists()
result = {'failures': 0, 'candidate': str(after_path), 'classes': 1381, 'sources': 716, 'guards': 208,
          'behaviorSourcesChanged': sorted(behavior), 'hashBindingOnlySourcesChanged': [bindings], 'unrelatedEffectiveSourcesUnchanged': 710,
          'sixGenuinePaidFamilyGatesAndAuthoritativeMaterialFactorsAccepted': True,
          'actualOriginalUseWearConstructorAddInventoryRemovalSignedCarrierAccepted': True,
          'genuineNonNullLeaseGenerationPreservedAfterOriginalThrow': True,
          'earlyReleasePreservesImageLeaseGenerationAndLiveSemanticSuccessor': True,
          'earlyReleasePreservesExactPrivateInputIdentity': False,
          'runtime': str(runtime_path), 'runtimeJavaExitCode': 0, 'runtimeObserverFailures': 1, 'normalStopAndSave': True,
          'fullSignedSessionAccepted': False, 'fullTridentCombatAccepted': False, 'pickupAndPersistenceAccepted': False,
          'authenticatedMultiplayerAccepted': False, 'fullLifecycleAccepted': False, 'crossFamilyEvidenceReusedForThisCandidate': False,
          'requiresBoundedBehaviorDeltaBeforeCrossFamilyEvidenceReuse': True, 'installable': False, 'deployed': False, 'inputHashes': hashes}
with output.open('x', encoding='utf-8') as stream:
    json.dump(result, stream, ensure_ascii=False, indent=2)
    stream.write('\n')
print(json.dumps({'carrierEvidence': output.name, 'inputHashes': len(hashes), 'signedSessionAccepted': False}))
