import hashlib
import json
from pathlib import Path


work = Path(__file__).resolve().parents[1]
helper = work / 'tools/prove_native_ranged_use_head_delta.py'
head = work / 'reports/native-ranged-use-head-delta-f4f2deae.json'
assert hashlib.sha256(helper.read_bytes()).hexdigest() == json.loads(head.read_text(encoding='utf-8'))['inputHashes'][str(helper)]
exec(compile(helper.read_text(encoding='utf-8').split('before = load(before_path)')[0], str(helper), 'exec'))
before_path = work / 'reports/native-repair-ranged-use-candidate-89f2b629.json'
after_path = work / 'reports/native-repair-ranged-use-candidate-9d1607e8.json'
before = load(before_path)
after = load(after_path)
assert digest(after_path) == '7a0661df7ceb74920c4ccf4db3425fb7e444e1e75eddfb6e047376b908fb5e97'
assert before['classCount'] == after['classCount'] == 1381
assert before['effectiveSourceCount'] == after['effectiveSourceCount'] == 716
assert before['guardCount'] == after['guardCount'] == 208
for receipt in [before, after]:
    for name, expected in receipt['inputHashes'].items():
        assert digest(Path(name)) == expected, name
previous = source_map(before)
current = source_map(after)
assert previous.keys() == current.keys()
semantic = 'net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority'
assert {key for key in previous if previous[key][1] != current[key][1]} == {semantic}
restored = current[semantic][1]
start = restored.index('    private static boolean nativeCrossbowChargeUnchanged(')
finish = restored.index('    private static boolean nativeCrossbowFirePlan(', start)
assert 'OwnerFullRepairGuard.permits(operation.stack)' in restored[start:finish]
assert 'operation.physical.matches(operation.stack)' in restored[start:finish]
restored = restored[:start] + restored[finish:]
for inserted in ['        operation.crossbowAmmoBefore = NativeFoundationItemData.image(ammo, operation.input.registries).copy();\n', '            if (originalFailure == null && nativeCrossbowChargeUnchanged(operation)) return;\n', '        private CompoundTag crossbowAmmoBefore;\n']:
    assert restored.count(inserted) == 1
    restored = restored.replace(inserted, '')
assert restored == previous[semantic][1]
old_classes = Path(before['attempt']) / 'complete-classes'
new_classes = Path(after['attempt']) / 'complete-classes'
old_graph = {path.relative_to(old_classes).as_posix(): digest(path) for path in old_classes.rglob('*.class')}
new_graph = {path.relative_to(new_classes).as_posix(): digest(path) for path in new_classes.rglob('*.class')}
assert len(old_graph) == len(new_graph) == 1381 and old_graph.keys() == new_graph.keys()
for path in [Path(__file__), helper, head, work / 'reports/native-crossbow-factory-delta-89f2b629.json']:
    digest(path)
output = work / 'reports/native-crossbow-noop-delta-9d1607e8.json'
assert not output.exists()
output.write_text(json.dumps({'failures': 0, 'before': str(before_path), 'after': str(after_path), 'exactInverseBehaviorSources': [semantic], 'bindingOnlySources': [], 'unrelatedEffectiveSourcesUnchanged': 715, 'classes': 1381, 'sources': 716, 'guards': 208, 'newClasses': [], 'runtimeAccepted': False, 'inputHashes': hashes}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'report': str(output), 'failures': 0, 'inputHashCount': len(hashes)}))
