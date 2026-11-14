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
intermediate_path = work / 'reports/native-repair-ranged-use-candidate-3ba845eb.json'
after_path = work / 'reports/native-repair-ranged-use-candidate-338f0feb.json'
before = load(before_path)
intermediate = load(intermediate_path)
after = load(after_path)
assert digest(before_path) == '7a0661df7ceb74920c4ccf4db3425fb7e444e1e75eddfb6e047376b908fb5e97'
for receipt in [before, intermediate, after]:
    assert receipt['classCount'] == 1381 and receipt['effectiveSourceCount'] == 716 and receipt['guardCount'] == 208
    assert receipt['failures'] == 0 and not receipt['newClasses']
    for name, expected in receipt['inputHashes'].items():
        assert digest(Path(name)) == expected, name
previous = source_map(before)
partial = source_map(intermediate)
current = source_map(after)
assert previous.keys() == partial.keys() == current.keys()
behavior = {'net.farmingtales.ftboss.item.' + name for name in [
    'FoodSocketAttributePowers', 'FoodWeaponBondHits', 'FoodWeaponLifecycle', 'FoodWeaponSourceLedger',
    'OwnerFullRepairGuard', 'OwnerNativeSemanticAuthority', 'WeaponMaterialBuild']}
binding = 'net.farmingtales.ftboss.item.NativeCapabilityInstallationProof170'
changed = {key for key in previous if previous[key][1] != current[key][1]}
assert changed == behavior | {binding}
pattern = r'(Map\.entry\("[^"]+", ")[a-f0-9]{64}("\))'
assert re.sub(pattern, r'\1SHA256\2', previous[binding][1]) == re.sub(pattern, r'\1SHA256\2', current[binding][1])
attribute = 'net.farmingtales.ftboss.item.FoodSocketAttributePowers'
assert {key for key in partial if partial[key][1] != current[key][1]} == {attribute}
expected = partial[attribute][1]
anchor = '    private static boolean offenseContext(FoodSocketAttributePowers.OffensePlan var0) {'
method = '    private static boolean offenseWeaponContinuous(FoodSocketAttributePowers.OffensePlan plan) {\n        if (plan.hit.kind() == net.farmingtales.ftboss.util.FoodWeaponBondRules.Kind.TRIDENT\n            && "NATIVE_PROJECTILE".equals(plan.hit.snapshot().getString("DamagePurpose"))\n            && "TRIDENT".equals(plan.hit.snapshot().getString("NativeEntry"))) {\n            return !plan.actualWeapon.isEmpty()\n                && FoodWeaponBondHits.equipped(plan.player, plan.hit) == plan.actualWeapon;\n        }\n        return plan.player.getItemInHand(plan.hand) == plan.actualWeapon;\n    }\n\n'
assert expected.count(anchor) == 1
expected = expected.replace(anchor, method + anchor)
for name in ['var0', 'var1']:
    anchor = '&& ' + name + '.player.getItemInHand(' + name + '.hand) == ' + name + '.actualWeapon'
    assert expected.count(anchor) == 1
    expected = expected.replace(anchor, '&& offenseWeaponContinuous(' + name + ')')
anchor = '            this.actualWeapon = var1.getItemInHand(this.hand);'
replacement = '            this.actualWeapon = var4.kind() == net.farmingtales.ftboss.util.FoodWeaponBondRules.Kind.TRIDENT\n                && "NATIVE_PROJECTILE".equals(var4.snapshot().getString("DamagePurpose"))\n                && "TRIDENT".equals(var4.snapshot().getString("NativeEntry"))\n                ? FoodWeaponBondHits.equipped(var1, var4) : var1.getItemInHand(this.hand);'
assert expected.count(anchor) == 1
expected = expected.replace(anchor, replacement)
assert expected == current[attribute][1]
for evidence in ['native-trident-paid-signed-carrier-evidence-04a9311d.json', 'native-trident-native-image-resolution-evidence-28325ac4.json']:
    scoped = load(work / 'reports' / evidence)
    assert scoped['failures'] == 0 and not scoped['fullSignedSessionAccepted']
    for name, wanted in scoped['inputHashes'].items():
        assert digest(Path(name)) == wanted, name
preparations = [work / 'reports/native-trident-foundation-gate-preparation-338f0feb.json',
                work / 'reports/native-trident-strict-qualified-preparation-338f0feb.json',
                work / 'reports/native-trident-foreign-controls-preparation-338f0feb.json']
for path in preparations:
    receipt = load(path)
    assert not receipt['authorityFieldsWritten']
    for name, wanted in receipt['inputHashes'].items():
        assert digest(Path(name)) == wanted, name
runtimes = []
for mode in ['foundation-gate', 'external-remove', 'foreign-release']:
    runtime_path = work / ('reports/native-repair-runtime-trident-' + mode + '-338f0feb.json')
    runtime = load(runtime_path)
    attempt = Path(runtime['attempt'])
    terminal_path = attempt / 'terminal308.json'
    terminal = load(terminal_path)
    assert runtime['exitCode'] == terminal['exitCode'] == 0
    assert terminal['stopSent'] and terminal['completeSave']
    assert runtime['probe']['failures'] == 0 and terminal['probe']['failures'] == 0
    assert not runtime['probe']['authorityWrittenByReflection'] and not runtime['probe']['authenticatedMultiplayerAccepted']
    for name, wanted in runtime['inputHashes'].items():
        assert digest(Path(name)) == wanted, name
    digest(attempt / 'runtime308.log')
    cases = runtime['probe']['cases']
    assert len(cases) == 6
    for case in cases:
        assert case['genuineNonNullLeaseAndGenerationBeforeAttack'] and not case['lifecycleBrokenBeforeUse']
        assert case['dispatchedMaterialFactor'] == case['nativeFoundationMaterialFactor'] == 1.0
    trident = cases[-1]
    assert trident['id'] == 'minecraft:trident'
    assert trident['wearBefore'] == 0 and trident['wearAfter'] == 1
    assert trident['healthAfter'] < trident['healthBefore']
    if mode == 'foundation-gate':
        for key in ['missingNativeCellsDenied', 'malformedNativeFoundationDenied', 'sameGenuineAuthorityAfterGateControls',
                    'actualUseStarted', 'earlyImageSame', 'earlyLeaseSame', 'earlyGenerationSame', 'earlyGenerationLive',
                    'earlyHandSame', 'earlyCurrentInputLive', 'earlyPreviousInputRetired', 'earlyPaidStateLiveWithObservedSemanticTransition',
                    'actualOriginalReleaseReturned', 'originalMainHandEmptyAfterThrow', 'sameLeaseAndGenerationAfterAttack',
                    'closedRealSignedImpactCustodyDenied', 'foreignRealSignedCustodyHelperDenied']:
            assert trident[key], key
        for key in ['earlyInputSame', 'generationRetiredAfterUse', 'generationRetiredAfterRelease', 'generationRetiredAfterAttack']:
            assert not trident[key], key
        assert trident['actualOriginalUseResult'] == 'CONSUME'
        assert len(trident['actualProjectileFlights']) == 1
        flight = trident['actualProjectileFlights'][0]
        for key in ['snapshotPresent', 'snapshotValid', 'repairEpochPresent', 'signaturePresent', 'paidCarrierPresent', 'copiedPickupHasNoPrivateAuthority']:
            assert flight[key], key
        assert flight['purpose'] == 'NATIVE_PROJECTILE' and flight['nativeEntry'] == 'TRIDENT'
        assert len(trident['damageEvents']) >= 2
        for event in trident['damageEvents']:
            for key in ['resolvedSnapshotPresent', 'resolvedSnapshotValid', 'repairEpochPresent', 'serverSignaturePresent']:
                assert event[key], key
        assert trident['publications']
        for publication in trident['publications']:
            for key in ['publicationPresent', 'preparedCommitted', 'originalSequencePresent', 'preparationPresent', 'sessionPresent',
                        'prepared_sealed', 'prepared_bound', 'session_coverageInstalled', 'traceComplete']:
                assert publication[key], key
            for key in ['prepared_unsupported', 'prepared_retired', 'session_traceRevoked']:
                assert publication[key] is False, key
            assert publication.get('firstObservationFailure') is None
    else:
        for key in ['foreignCallbacksPreserveExactInputImageAndAuthority', 'foreignOriginalFactoryExceptionIdentityPreserved',
                    'foreignNativeEntryOriginalThrowReturnedOnce', 'foreignNativeEntryNoSignedCarrier',
                    'foreignNativeEntryAuthorityRetired', 'foreignNativeEntryScopesClosed']:
            assert trident[key], key
        assert trident['foreignOriginalFactoryCallCount'] == 1
        assert not trident['publications']
        if mode == 'external-remove':
            assert trident['externalAbsentComponentRemovalImageUnchanged'] and trident['externalAbsentComponentRemovalRetiresAuthority']
    runtimes.append({'mode': mode, 'runtime': str(runtime_path), 'attempt': str(attempt), 'javaExitCode': 0,
                     'observerFailures': 0, 'normalStopAndCompleteSave': True})
for path in [Path(__file__), helper, head, work / 'reports/trident-abstract-arrow-original-constructor-6a38a668.javap.txt',
             work / 'reports/trident-origin-sync-original-mixin-c0607cae.javap.txt']:
    digest(path)
output = work / 'reports/native-trident-paid-signed-combat-focused-evidence-338f0feb.json'
result = {'failures': 0, 'candidate': str(after_path), 'classes': 1381, 'sources': 716, 'guards': 208,
          'behaviorSourcesChangedSinceCrossbow': sorted(behavior), 'hashBindingOnlySourcesChanged': [binding],
          'unrelatedEffectiveSourcesUnchangedSinceCrossbow': 708,
          'attributeCustodyOnlySourceDeltaFromPartialSession': True, 'unrelatedSourcesUnchangedSincePartialSession': 715,
          'actualOriginalUseReleaseWearConstructorEntityAddInventoryRemovalImpactAccepted': True,
          'genuineNonNullLeaseGenerationPreserved': True, 'fullSignedSessionAccepted': True,
          'earlyReleasePreservesImageLeaseGenerationAndLiveSemanticSuccessor': True,
          'earlyReleasePreservesExactPrivateInputIdentity': False,
          'copiedPickupHasNoPrivateAuthority': True, 'closedAndForeignRealSignedCustodyDenied': True,
          'foreignNativeEntryRetiresAuthorityWithOriginalOnceAndNoSignedCarrier': True,
          'externalImagePreservingMutationRetiresAuthority': True,
          'strictRuntimeEvidence': runtimes, 'pickupAndPersistenceAccepted': False,
          'crossFamilyEvidenceReusedForThisCandidate': False, 'requiresBoundedBehaviorDeltaBeforeCrossFamilyEvidenceReuse': True,
          'authenticatedMultiplayerAccepted': False, 'fullLifecycleAccepted': False, 'installable': False,
          'deployed': False, 'inputHashes': hashes}
with output.open('x', encoding='utf-8') as stream:
    json.dump(result, stream, ensure_ascii=False, indent=2)
    stream.write('\n')
print(json.dumps({'evidence': output.name, 'inputHashes': len(hashes), 'runtimes': len(runtimes), 'fullSignedSessionAccepted': True}))
