import hashlib
import json
import re
from pathlib import Path

work = Path(__file__).resolve().parents[1]
helper = work / 'tools/prove_native_ranged_use_head_delta.py'
head = work / 'reports/native-ranged-use-head-delta-f4f2deae.json'
assert hashlib.sha256(helper.read_bytes()).hexdigest() == json.loads(head.read_text(encoding='utf-8'))['inputHashes'][str(helper)]
exec(compile(helper.read_text(encoding='utf-8').split('before = load(before_path)')[0], str(helper), 'exec'))
before_path = work / 'reports/native-repair-ranged-use-candidate-04a9311d.json'
after_path = work / 'reports/native-repair-ranged-use-candidate-28325ac4.json'
before = load(before_path)
after = load(after_path)
for receipt in [before, after]:
    assert receipt['classCount'] == 1381 and receipt['effectiveSourceCount'] == 716 and receipt['guardCount'] == 208
    assert receipt['failures'] == 0 and not receipt['newClasses']
    for name, expected in receipt['inputHashes'].items():
        assert digest(Path(name)) == expected, name
previous = source_map(before)
current = source_map(after)
assert previous.keys() == current.keys()
ledger = 'net.farmingtales.ftboss.item.FoodWeaponSourceLedger'
binding = 'net.farmingtales.ftboss.item.NativeCapabilityInstallationProof170'
assert {key for key in previous if previous[key][1] != current[key][1]} == {ledger, binding}
old = '        return var1 != null && FoodWeaponExpectedTrace.same(NativeExpectedRawFields.observe(var1), originalRepairRawFields(var0));'
new = '        return var0 != null && var1 != null && FoodWeaponExpectedTrace.same(NativeExpectedRawFields.observe(var1),\n            FourWeaponFoundationAdapter.applies(var0) ? NativeExpectedRawFields.observe(NativeFoundationItemData.image(var0)) : originalRepairRawFields(var0));'
assert current[ledger][1].count(new) == 1
assert current[ledger][1].replace(new, old) == previous[ledger][1]
pattern = r'(Map\.entry\("[^"]+", ")[a-f0-9]{64}("\))'
assert re.sub(pattern, r'\1SHA256\2', previous[binding][1]) == re.sub(pattern, r'\1SHA256\2', current[binding][1])
carrier_evidence = work / 'reports/native-trident-paid-signed-carrier-evidence-04a9311d.json'
for name, expected in load(carrier_evidence)['inputHashes'].items():
    assert digest(Path(name)) == expected, name
runtime_path = work / 'reports/native-repair-runtime-trident-foundation-gate-28325ac4.json'
runtime = load(runtime_path)
attempt = Path(runtime['attempt'])
terminal_path = attempt / 'terminal308.json'
terminal = load(terminal_path)
assert runtime['exitCode'] == terminal['exitCode'] == 0 and terminal['stopSent'] and terminal['completeSave']
assert runtime['probe']['failures'] == 1 and 'complete live signed Session' in runtime['probe']['failure']
trident = runtime['probe']['cases'][-1]
assert trident['id'] == 'minecraft:trident' and trident['sameLeaseAndGenerationAfterAttack'] and not trident['generationRetiredAfterAttack']
assert len(trident['damageEvents']) >= 2 and not trident['publications']
for event in trident['damageEvents']:
    assert all(event[key] for key in ['resolvedSnapshotPresent', 'resolvedSnapshotValid', 'repairEpochPresent', 'serverSignaturePresent'])
for name, expected in runtime['inputHashes'].items():
    assert digest(Path(name)) == expected, name
for path in [Path(__file__), helper, head, before_path, after_path, carrier_evidence, runtime_path, terminal_path, attempt / 'runtime308.log']:
    digest(path)
output = work / 'reports/native-trident-native-image-resolution-evidence-28325ac4.json'
assert not output.exists()
result = {'failures': 0, 'before': str(before_path), 'after': str(after_path), 'exactInverseBehaviorSources': [ledger],
          'behaviorMethod': 'tridentRawMatchesExpected', 'hashBindingOnlySourcesChanged': [binding], 'unrelatedEffectiveSourcesUnchanged': 714,
          'classes': 1381, 'sources': 716, 'guards': 208, 'realImpactEventsResolveGenuineSignedSnapshotAccepted': True,
          'genuineNonNullLeaseGenerationPreserved': True, 'runtime': str(runtime_path), 'runtimeJavaExitCode': 0,
          'runtimeObserverFailures': 1, 'normalStopAndSave': True, 'fullSignedSessionAccepted': False,
          'fullTridentCombatAccepted': False, 'pickupAndPersistenceAccepted': False, 'authenticatedMultiplayerAccepted': False,
          'installable': False, 'deployed': False, 'inputHashes': hashes}
with output.open('x', encoding='utf-8') as stream:
    json.dump(result, stream, ensure_ascii=False, indent=2)
    stream.write('\n')
print(json.dumps({'nativeImageEvidence': output.name, 'inputHashes': len(hashes), 'fullSignedSessionAccepted': False}))
