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

old_path = work / 'reports/native-repair-melee-ingress-candidate-25cdcaa5.json'
new_path = work / ('reports/native-repair-melee-ingress-candidate-' + tag + '.json')
assert digest(old_path) == 'c78d1eb785cd4d542599700b7486e2dd77a59048d94cb7ee9aa8b4c095375bf5'
old = load(old_path)
new = load(new_path)
assert old['classCount'] == new['classCount'] == 1381
assert old['effectiveSourceCount'] == new['effectiveSourceCount'] == 716
assert old['guardCount'] == new['guardCount'] == 208 and not new['newClasses']
old_arguments = Path(old['attempt']) / 'complete-arguments316.json'
new_arguments = Path(new['attempt']) / 'complete-arguments316.json'

def sources(arguments):
    result = {}
    for value in load(arguments):
        if not value.endswith('.java'):
            continue
        path = Path(value)
        text = path.read_text(encoding='utf-8-sig')
        package = re.search(r'^package\s+([^;]+);', text, re.MULTILINE)
        key = (package.group(1) + '.' if package else '') + path.name
        assert key not in result, key
        result[key] = path
    assert len(result) == 716
    return result

before = sources(old_arguments)
after = sources(new_arguments)
assert before.keys() == after.keys()
changed = []
binding_only = []
diffs = []
behavior = {}
for name in before:
    old_text = before[name].read_text(encoding='utf-8-sig')
    new_text = after[name].read_text(encoding='utf-8-sig')
    if old_text == new_text:
        continue
    changed.append(name)
    diffs.extend(difflib.unified_diff(old_text.splitlines(True), new_text.splitlines(True), fromfile='before/' + name, tofile='after/' + name))
    if name.endswith('.FoodWeaponSourceLedger.java'):
        old_start = old_text.index('    private static boolean nativeMeleeBoundary(')
        old_finish = old_text.index('    public static boolean nativeMeatActualHurt(', old_start)
        new_start = new_text.index('    private static boolean nativeMeleeBoundary(')
        new_finish = new_text.index('    public static boolean nativeMeatActualHurt(', new_start)
        restored = new_text[:new_start] + old_text[old_start:old_finish] + new_text[new_finish:]
        assert restored.count('        OwnerNativeSemanticAuthority.nativeMeleeAppliedDamage(var0, var1, var2, var4);\n') == 1
        restored = restored.replace('        OwnerNativeSemanticAuthority.nativeMeleeAppliedDamage(var0, var1, var2, var4);\n', '')
        assert restored == old_text
        behavior[name] = 'Original scoped melee helpers extended with exact original item copy/hurtEnemy/postHurtEnemy boundaries and one actual damage callback'
    elif name.endswith('.OwnerNativeSemanticAuthority.java'):
        start = new_text.index('    private static boolean nativeMeleeCaller() {')
        finish = new_text.index('    static NativeWear nativeFactoryEntered(', start)
        restored = new_text[:start] + new_text[finish:]
        restored = restored.replace('        if (operation.meleeSite != 0) return nativeMeleeItemMutation(operation, receiver, kind);\n', '')
        fields = '        private int meleeSite;\n        private int meleeAmount;\n        private int meleeWrites;\n        private int meleeRequested;\n        private boolean meleeValidated;\n'
        assert restored.count(fields) == 1
        restored = restored.replace(fields, '')
        native_plan = '            if (receipt instanceof NativeWear operation && operation.meleeSite == 3) {\n                if (!nativeMeleePlanEligible(operation, stack, input, detached, initialization)) return null;\n            } else if (!FourWeaponFoundationAdapter.read(detached).valid()) return null;\n'
        assert restored.count(native_plan) == 1
        restored = restored.replace(native_plan, '            if (!FourWeaponFoundationAdapter.read(detached).valid()) return null;\n')
        assert restored == old_text
        behavior[name] = 'Separate exact-source original melee item operations with immutable physical/input plans, observed write count, lease/generation and independent terminal'
    elif name.endswith('.OwnerFullRepairGuard.java'):
        start = new_text.index('    static synchronized boolean refreshNativeMelee(')
        finish = new_text.index('    private static void deny(', start)
        assert new_text[:start] + new_text[finish:] == old_text
        assert 'OwnerNativeSemanticAuthority.nativeMeleeTerminalReady(stack, lease, generation)' in new_text[start:finish]
        behavior[name] = 'New exact-caller melee terminal refresh requiring live private independently validated operation; original guard methods unchanged'
    elif name.endswith('.NativeItemComponentObservation.java'):
        start = new_text.index('    boolean nativeMeleeDamageOverride(')
        finish = new_text.index('    boolean nativeDamageOverride(', start)
        assert new_text[:start] + new_text[finish:] == old_text
        behavior[name] = 'Exact-caller original component prototype observation for melee terminal; bow observation unchanged'
    elif name.endswith('.NativeReadCutPlayerMixin.java'):
        start = new_text.index('\n    @WrapOperation(method = "attack', new_text.index('private boolean ftboss$nativeMeleeHurt'))
        restored = new_text[:start] + '}\n'
        restored = restored.replace('import net.minecraft.world.entity.LivingEntity;\n', '').replace('import net.minecraft.world.item.ItemStack;\n', '')
        assert restored == old_text
        assert new_text.count('@WrapOperation(') == 4 and new_text.count('require = 1, expect = 1, allow = 1') == 4
        behavior[name] = 'Three additional exact single original Player.attack item operation sites; original actual-hurt site unchanged'
    else:
        assert re.sub(r'[0-9a-f]{64}', '<HASH>', old_text) == re.sub(r'[0-9a-f]{64}', '<HASH>', new_text), name
        assert any(Path(entry['source']).name == after[name].name for entry in new['authorityBindingUpdates']), name
        binding_only.append(name)
assert len(behavior) == 5
assert len(changed) == len(binding_only) + len(behavior)
assert load(Path(new['attempt']) / 'asm.json')['failures'] == 0
diff_path = work / ('reports/native-melee-wear-delta-' + tag + '.patch.txt')
output = work / ('reports/native-melee-wear-delta-' + tag + '.json')
assert not diff_path.exists() and not output.exists()
diff_path.write_text(''.join(diffs), encoding='utf-8')
files = [old_path, new_path, old_arguments, new_arguments, Path(__file__), diff_path, Path(new['attempt']) / 'asm.json']
for name in before:
    files.extend([before[name], after[name]])
result = {
    'oldCandidate': str(old_path), 'newCandidate': str(new_path), 'changedSources': changed,
    'behavioralChanges': behavior, 'otherChangedSourcesAreHashBindingRefreshOnly': binding_only,
    'exactlyReversibleBoundedEffectiveSourceDelta': True, 'noBroadExistingGuardBypass': True,
    'newProductClasses': 0, 'classCount': 1381, 'effectiveSourceCount': 716, 'guardCount': 208,
    'targetedAsmFailures': 0, 'allOtherEffectiveSourcesByteIdentical': True,
    'staticProofIsNotRuntimeOrCompleteMeleeLifecycleAcceptance': True,
    'installable': False, 'deployed': False, 'inputHashes': {str(path): digest(path) for path in files}
}
output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'behavioralSources': list(behavior), 'bindingOnly': binding_only, 'hashes': len(result['inputHashes']), 'failures': 0}))

