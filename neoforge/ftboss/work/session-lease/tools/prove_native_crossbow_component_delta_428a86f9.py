import hashlib
import json
import re
from pathlib import Path


work = Path(__file__).resolve().parents[1]
helper = work / 'tools/prove_native_ranged_use_head_delta.py'
head_evidence = work / 'reports/native-ranged-use-head-delta-f4f2deae.json'
expected_helper = json.loads(head_evidence.read_text(encoding='utf-8'))['inputHashes'][str(helper)]
assert hashlib.sha256(helper.read_bytes()).hexdigest() == expected_helper
exec(compile(helper.read_text(encoding='utf-8').split('before = load(before_path)')[0], str(helper), 'exec'))
before_path = work / 'reports/native-repair-ranged-use-candidate-f4f2deae.json'
after_path = work / 'reports/native-repair-ranged-use-candidate-428a86f9.json'
before = load(before_path)
after = load(after_path)
assert digest(after_path) == '2b47e9d8a7f4d917b5455ffaec08002467adf3364d8f343601f4013811518e09'
assert before['classCount'] == after['classCount'] == 1381
assert before['effectiveSourceCount'] == after['effectiveSourceCount'] == 716
assert before['guardCount'] == after['guardCount'] == 208
for receipt in [before, after]:
    for name, expected in receipt['inputHashes'].items():
        assert digest(Path(name)) == expected, name
previous = source_map(before)
current = source_map(after)
assert previous.keys() == current.keys()
prefix = 'net.farmingtales.ftboss.'
behavior = {prefix + 'item.' + name for name in ['OwnerNativeSemanticAuthority', 'OwnerFullRepairGuard', 'NativeItemComponentObservation']}
behavior.add(prefix + 'mixin.NativeItemComponentStackFieldsMixin')
bindings = {prefix + 'item.' + name for name in ['NativeStackFieldRequirementProof172', 'OwnerFieldInstallationProof', 'NativeCapabilityInstallationProof170']}
changed = {key for key in previous if previous[key][1] != current[key][1]}
assert changed == behavior | bindings, sorted(changed)
semantic = current[prefix + 'item.OwnerNativeSemanticAuthority'][1]
start = semantic.index('    private static boolean nativeCrossbowChargePlan(')
finish = semantic.index('    private static boolean nativeMeleeCaller(', start)
bounded_methods = semantic[start:finish]
for required in ['ChargedProjectiles.EMPTY', 'operation.crossbowWrites == 1', 'operation.crossbowAmmoAfter', 'nativeCrossbowComponentReady', 'nativeCrossbowComponentReturned', 'operation.physical.matches(operation.stack)', 'previous != operation.crossbowPrevious']:
    assert required in bounded_methods
semantic = semantic[:start] + semantic[finish:]
semantic = semantic.replace('            || kind != 1 && kind != 4 || !(actor instanceof ServerPlayer player)\n            || stack.getItem() != net.minecraft.world.item.Items.BOW && stack.getItem() != net.minecraft.world.item.Items.CROSSBOW\n            || kind == 4 && stack.getItem() != net.minecraft.world.item.Items.CROSSBOW', '            || kind != 1 || !(actor instanceof ServerPlayer player) || stack.getItem() != net.minecraft.world.item.Items.BOW')
semantic = semantic.replace('            if (stack.getItem() == net.minecraft.world.item.Items.CROSSBOW\n                && !(kind == 1 ? nativeCrossbowChargePlan(operation, player) : nativeCrossbowFirePlan(operation))) return null;\n', '')
for inserted in ['        if (operation.crossbowCharge) return nativeCrossbowChargeMutation(operation, receiver, kind);\n', '        if (operation.crossbowFire && !operation.crossbowComponentDone) return nativeCrossbowFireMutation(operation, receiver, kind);\n']:
    assert semantic.count(inserted) == 1
    semantic = semantic.replace(inserted, '')
start = semantic.index('            if (operation.crossbowCharge && originalFailure == null')
finish = semantic.index('            boolean valid = originalFailure == null', start)
semantic = semantic[:start] + semantic[finish:]
semantic = semantic.replace('!(transition.crossbowCharge || transition.crossbowFire ? transition.physical.nativeCrossbowChargeSame(after, transition.crossbowFire) : transition.physical.nativeWearSame(after, writes, customData))', '!transition.physical.nativeWearSame(after, writes, customData)')
for inserted in ['        private boolean crossbowCharge;\n', '        private int crossbowWrites;\n', '        private ItemStack crossbowAmmo;\n', '        private CompoundTag crossbowAmmoAfter;\n', '        private boolean crossbowFire;\n', '        private boolean crossbowComponentDone;\n', '        private Object crossbowPrevious;\n', '        private CompoundTag crossbowComponentAfter;\n']:
    assert semantic.count(inserted) == 1
    semantic = semantic.replace(inserted, '')
assert semantic == previous[prefix + 'item.OwnerNativeSemanticAuthority'][1]
guard = current[prefix + 'item.OwnerFullRepairGuard'][1]
guard = guard.replace('        if (caller.getDeclaringClass() != OwnerNativeSemanticAuthority.class\n            || !(caller.getMethodName().equals("nativeOperationReturned") || caller.getMethodName().equals("nativeCrossbowComponentReturned")\n                && OwnerNativeSemanticAuthority.nativeCrossbowComponentReady(stack, lease, generation))', '        if (caller.getDeclaringClass() != OwnerNativeSemanticAuthority.class || !caller.getMethodName().equals("nativeOperationReturned")')
assert guard == previous[prefix + 'item.OwnerFullRepairGuard'][1]
physical = current[prefix + 'item.NativeItemComponentObservation'][1]
start = physical.index('    boolean nativeCrossbowChargeSame(')
finish = physical.index('    boolean nativeMeleeDamageOverride(', start)
physical = physical[:start] + physical[finish:]
assert physical == previous[prefix + 'item.NativeItemComponentObservation'][1]
mixin = current[prefix + 'mixin.NativeItemComponentStackFieldsMixin'][1]
mixin = mixin.replace('        net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority.nativeCrossbowComponentReturned((ItemStack)(Object)this,type,value,callback.getReturnValue());\n', '')
assert mixin == previous[prefix + 'mixin.NativeItemComponentStackFieldsMixin'][1]
old_classes = Path(before['attempt']) / 'complete-classes'
new_classes = Path(after['attempt']) / 'complete-classes'
old_graph = {path.relative_to(old_classes).as_posix(): digest(path) for path in old_classes.rglob('*.class')}
new_graph = {path.relative_to(new_classes).as_posix(): digest(path) for path in new_classes.rglob('*.class')}
assert len(old_graph) == len(new_graph) == 1381 and old_graph.keys() == new_graph.keys()
replacements = {old_graph[key]: new_graph[key] for key in old_graph if old_graph[key] != new_graph[key]}
for key in bindings:
    expected = re.sub(r'[a-f0-9]{64}', lambda match: replacements.get(match.group(0), match.group(0)), previous[key][1])
    assert expected == current[key][1], key
for path in [Path(__file__), helper, head_evidence]:
    digest(path)
output = work / 'reports/native-crossbow-component-delta-428a86f9.json'
assert not output.exists()
output.write_text(json.dumps({'failures': 0, 'before': str(before_path), 'after': str(after_path), 'boundedBehaviorSources': sorted(behavior), 'bindingOnlySources': sorted(bindings), 'unrelatedEffectiveSourcesUnchanged': 709, 'classes': 1381, 'sources': 716, 'guards': 208, 'newClasses': [], 'runtimeAccepted': False, 'fullMethodSemanticAcceptanceClaimed': False, 'inputHashes': hashes}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'report': str(output), 'failures': 0, 'inputHashCount': len(hashes)}))
