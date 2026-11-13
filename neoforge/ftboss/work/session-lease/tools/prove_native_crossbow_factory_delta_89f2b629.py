import hashlib
import json
import re
from pathlib import Path


work = Path(__file__).resolve().parents[1]
helper = work / 'tools/prove_native_ranged_use_head_delta.py'
head = work / 'reports/native-ranged-use-head-delta-f4f2deae.json'
expected_helper = json.loads(head.read_text(encoding='utf-8'))['inputHashes'][str(helper)]
assert hashlib.sha256(helper.read_bytes()).hexdigest() == expected_helper
exec(compile(helper.read_text(encoding='utf-8').split('before = load(before_path)')[0], str(helper), 'exec'))
before_path = work / 'reports/native-repair-ranged-use-candidate-76bc665d.json'
after_path = work / 'reports/native-repair-ranged-use-candidate-89f2b629.json'
before = load(before_path)
after = load(after_path)
assert digest(after_path) == '9d8cac26609e29e7e89c35a2d33251ad972d2974f316a8c15da800cd10e5a70d'
assert before['classCount'] == after['classCount'] == 1381
assert before['effectiveSourceCount'] == after['effectiveSourceCount'] == 716
assert before['guardCount'] == after['guardCount'] == 208
for receipt in [before, after]:
    for name, expected in receipt['inputHashes'].items():
        assert digest(Path(name)) == expected, name
previous = source_map(before)
current = source_map(after)
assert previous.keys() == current.keys()
ledger = 'net.farmingtales.ftboss.item.FoodWeaponSourceLedger'
binding = 'net.farmingtales.ftboss.item.NativeCapabilityInstallationProof170'
changed = {key for key in previous if previous[key][1] != current[key][1]}
assert changed == {ledger, binding}, sorted(changed)
expected = previous[ledger][1].replace('        boolean nativeRepairFactory = liveNative(var5) && var5.kind == FoodWeaponSourceLedger.NativeKind.BOW && var5.weapon == weapon', '        boolean nativeRepairFactory = liveNative(var5)\n            && (var5.kind == FoodWeaponSourceLedger.NativeKind.BOW || var5.kind == FoodWeaponSourceLedger.NativeKind.CROSSBOW) && var5.weapon == weapon')
assert expected != previous[ledger][1] and expected == current[ledger][1]
old_classes = Path(before['attempt']) / 'complete-classes'
new_classes = Path(after['attempt']) / 'complete-classes'
old_graph = {path.relative_to(old_classes).as_posix(): digest(path) for path in old_classes.rglob('*.class')}
new_graph = {path.relative_to(new_classes).as_posix(): digest(path) for path in new_classes.rglob('*.class')}
assert len(old_graph) == len(new_graph) == 1381 and old_graph.keys() == new_graph.keys()
replacements = {old_graph[key]: new_graph[key] for key in old_graph if old_graph[key] != new_graph[key]}
assert re.sub(r'[a-f0-9]{64}', lambda match: replacements.get(match.group(0), match.group(0)), previous[binding][1]) == current[binding][1]
for path in [Path(__file__), helper, head, work / 'reports/native-crossbow-component-delta-76bc665d.json']:
    digest(path)
output = work / 'reports/native-crossbow-factory-delta-89f2b629.json'
assert not output.exists()
output.write_text(json.dumps({'failures': 0, 'before': str(before_path), 'after': str(after_path), 'exactInverseBehaviorSources': [ledger], 'bindingOnlySources': [binding], 'unrelatedEffectiveSourcesUnchanged': 714, 'classes': 1381, 'sources': 716, 'guards': 208, 'newClasses': [], 'runtimeAccepted': False, 'inputHashes': hashes}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'report': str(output), 'failures': 0, 'inputHashCount': len(hashes)}))
