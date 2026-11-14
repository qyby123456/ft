import hashlib
import json
from pathlib import Path


work = Path(__file__).resolve().parents[1]
helper = work / 'tools/prove_native_ranged_use_head_delta.py'
head = work / 'reports/native-ranged-use-head-delta-f4f2deae.json'
assert hashlib.sha256(helper.read_bytes()).hexdigest() == json.loads(head.read_text(encoding='utf-8'))['inputHashes'][str(helper)]
exec(compile(helper.read_text(encoding='utf-8').split('before = load(before_path)')[0], str(helper), 'exec'))
before_path = work / 'reports/native-repair-ranged-use-candidate-9d1607e8.json'
after_path = work / 'reports/native-repair-ranged-use-candidate-3413e22a.json'
before = load(before_path)
after = load(after_path)
assert digest(before_path) == '7a0661df7ceb74920c4ccf4db3425fb7e444e1e75eddfb6e047376b908fb5e97'
assert before['classCount'] == after['classCount'] == 1381
assert before['effectiveSourceCount'] == after['effectiveSourceCount'] == 716
assert before['guardCount'] == after['guardCount'] == 208
for receipt in [before, after]:
    for name, expected in receipt['inputHashes'].items():
        assert digest(Path(name)) == expected, name
previous = source_map(before)
current = source_map(after)
assert previous.keys() == current.keys()
target = 'net.farmingtales.ftboss.item.FoodWeaponLifecycle'
assert {key for key in previous if previous[key][1] != current[key][1]} == {target}
old = '        return !managed(var0) ? false : durabilityDepleted(var0) || SwordCavityBuild.missingRequired(var0);'
new = '        return !managed(var0) ? false : durabilityDepleted(var0)\n            || (FourWeaponFoundationAdapter.applies(var0) ? FourWeaponFoundationAdapter.blocksUse(var0) : SwordCavityBuild.missingRequired(var0));'
assert current[target][1].count(new) == 1
assert current[target][1].replace(new, old) == previous[target][1]
runtime_path = work / 'reports/native-repair-runtime-trident-foundation-gate-3413e22a.json'
runtime = load(runtime_path)
terminal = load(Path(runtime['attempt']) / 'terminal308.json')
assert runtime['exitCode'] == terminal['exitCode'] == 0
assert terminal['stopSent'] and terminal['completeSave']
assert runtime['probe']['failures'] == 1
assert 'did not publish one genuinely paid signed native carrier' in runtime['probe']['failure']
cases = runtime['probe']['cases']
assert len(cases) == 6
for case in cases:
    assert case['genuineNonNullLeaseAndGenerationBeforeAttack']
    assert not case['lifecycleBrokenBeforeUse']
trident = cases[-1]
assert trident['id'] == 'minecraft:trident' and trident['oldSwordRequiredMissing']
assert trident['nativeFoundationApplies'] and not trident['nativeFoundationBlocksUse']
assert all(trident[key] for key in ['missingNativeCellsDenied', 'malformedNativeFoundationDenied', 'sameGenuineAuthorityAfterGateControls', 'actualUseStarted'])
assert not trident['generationRetiredAfterUse']
assert trident['generationRetiredAfterRelease']
for name, expected in runtime['inputHashes'].items():
    assert digest(Path(name)) == expected, name
for relative in ['reports/native-repair-runtime-trident-structure-diagnostic-9d1607e8.json', 'reports/native-trident-structure-diagnostic-preparation-9d1607e8.json', 'reports/native-trident-foundation-gate-preparation-3413e22a.json']:
    receipt = load(work / relative)
    for name, expected in receipt.get('inputHashes', {}).items():
        assert digest(Path(name)) == expected, name
for path in [Path(__file__), helper, head, Path(runtime['attempt']) / 'runtime308.log']:
    digest(path)
output = work / 'reports/native-trident-foundation-gate-evidence-3413e22a.json'
assert not output.exists()
output.write_text(json.dumps({'failures': 0, 'before': str(before_path), 'after': str(after_path), 'exactInverseBehaviorSources': [target], 'unrelatedEffectiveSourcesUnchanged': 715, 'classes': 1381, 'sources': 716, 'guards': 208, 'sixGenuinePaidFamilyGatesAccepted': True, 'missingAndMalformedNativeStructuresDenied': True, 'originalTridentUseLiveAccepted': True, 'fullTridentCombatAccepted': False, 'expectedNextFailure': 'Original release retires generation and emits no signed paid carrier', 'runtime': str(runtime_path), 'runtimeJavaExitCode': 0, 'runtimeObserverFailures': 1, 'normalStopAndSave': True, 'installable': False, 'deployed': False, 'inputHashes': hashes}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'gateEvidence': output.name, 'hashes': len(hashes), 'combatAccepted': False}))
