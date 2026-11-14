import difflib
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
    return hashlib.sha256(path.read_bytes()).hexdigest()


def sources(arguments):
    result = {}
    for value in load(arguments):
        if not value.endswith('.java'):
            continue
        path = Path(value)
        package = re.search(r'^package\s+([^;]+);', path.read_text(encoding='utf-8-sig'), re.MULTILINE)
        key = package.group(1) + '.' + path.name
        assert key not in result
        result[key] = path
    assert len(result) == 716
    return result


old_path = work / 'reports/native-repair-melee-ingress-candidate-eef72352.json'
assert digest(old_path) == '15f4f0e9a3f6fe21fffe9a969d2cf522835e6145388598b26d8db33502bf9e55'
new_path = work / ('reports/native-repair-frost-ingress-candidate-' + tag + '.json')
old = load(old_path)
new = load(new_path)
assert old['classCount'] == new['classCount'] == 1381 and old['effectiveSourceCount'] == new['effectiveSourceCount'] == 716
assert old['guardCount'] == new['guardCount'] == 208 and not new['newClasses']
old_arguments = Path(old['attempt']) / 'complete-arguments316.json'
new_arguments = Path(new['attempt']) / 'complete-arguments316.json'
before = sources(old_arguments)
after = sources(new_arguments)
assert before.keys() == after.keys()
behavior_names = {'FoodWeaponSourceLedger.java', 'OwnerNativeSemanticAuthority.java', 'FoodWeaponUseBridge.java', 'FourWeaponUseGuard.java', 'NativeReadCutItemStackMixin.java', 'NativeReadCutLivingEntityMixin.java', 'NativeComponentReadCutHooks.java', 'NativeWeaponObservationMixinPlugin.java'}
changed = []
binding_only = []
diffs = []
protected = []
for name in before:
    previous = before[name].read_text(encoding='utf-8-sig')
    current = after[name].read_text(encoding='utf-8-sig')
    if previous == current:
        continue
    changed.append(name)
    diffs.extend(difflib.unified_diff(previous.splitlines(True), current.splitlines(True), fromfile=str(before[name]), tofile=str(after[name])))
    basename = after[name].name
    if basename not in behavior_names:
        assert re.sub(r'[0-9a-f]{64}', '<HASH>', previous) == re.sub(r'[0-9a-f]{64}', '<HASH>', current), name
        assert any(Path(entry['source']).name == basename for entry in new['authorityBindingUpdates']), name
        binding_only.append(name)
    elif basename == 'NativeComponentReadCutHooks.java':
        start = current.index('    static void verifyFrostIngress(')
        finish = current.index('    private record Target(', start)
        assert (current[:start] + current[finish:]).rstrip() == previous.rstrip()
        protected.append(name)
    elif basename == 'NativeWeaponObservationMixinPlugin.java':
        start = current.index('        if (targetClassName.equals("net.farmingtales.ftboss.item.FrostQueenStaffItem")')
        finish = current.index('        int expected = switch', start)
        assert (current[:start] + current[finish:]).rstrip() == previous.rstrip()
        protected.append(name)
    elif basename == 'FourWeaponUseGuard.java':
        assert current.count('        if (FoodWeaponSourceLedger.nativeFrostGateAllowed(var0)) return blocksUse(var0);\n') == 2
        assert current.replace('        if (FoodWeaponSourceLedger.nativeFrostGateAllowed(var0)) return blocksUse(var0);\n', '').rstrip() == previous.rstrip()
        protected.append(name)
    elif basename == 'FoodWeaponUseBridge.java':
        branch = '            if (FoodWeaponSourceLedger.nativeFrostGateAllowed(var1)) return FoodWeaponSourceLedger.nativeFrostOriginalUse(var1, var0, var2, var3, var4,\n                arguments -> NativeEvergaleBridge.use(var0, var1, var2, var3, var4));\n'
        assert current.count(branch) == 1 and current.replace(branch, '').rstrip() == previous.rstrip()
        protected.append(name)
assert set(after[name].name for name in changed if after[name].name in behavior_names) == behavior_names
assert len(changed) == 8 + len(binding_only)
asm = Path(new['attempt']) / 'asm.json'
assert load(asm)['failures'] == 0
patch_path = work / ('reports/native-frost-ingress-delta-' + tag + '.patch.txt')
output = work / ('reports/native-frost-ingress-delta-' + tag + '.json')
assert not patch_path.exists() and not output.exists()
patch_path.write_text(''.join(diffs), encoding='utf-8')
files = [old_path, new_path, old_arguments, new_arguments, asm, Path(__file__), patch_path, *before.values(), *after.values()]
hashes = {str(path): digest(path) for path in files}
output.write_text(json.dumps({'oldCandidate': str(old_path), 'newCandidate': str(new_path), 'classCount': 1381, 'effectiveSourceCount': 716, 'guardCount': 208, 'newProductClasses': 0, 'changedSources': changed, 'explicitBehavioralSourceNames': sorted(behavior_names), 'bindingOnlySources': binding_only, 'existingOriginalGuardMethodsByteIdenticalOutsideExactFrostBranch': protected, 'allOtherEffectiveSourcesByteIdentical': True, 'fullPatchPreservedForIndependentReview': str(patch_path), 'staticDeltaIsNotScopeOrRuntimeAcceptance': True, 'installable': False, 'deployed': False, 'inputHashes': hashes}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'behavioralSourceCount': 8, 'bindingOnly': len(binding_only), 'hashes': len(hashes), 'targetedAsmFailures': 0}))
