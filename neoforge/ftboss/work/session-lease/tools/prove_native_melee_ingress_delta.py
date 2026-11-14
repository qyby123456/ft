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
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()

old_path = work / 'reports/native-repair-semantic-noop-candidate-524a9d86.json'
new_path = work / ('reports/native-repair-melee-ingress-candidate-' + tag + '.json')
assert digest(old_path) == '64eae770e091193ef783dc46068cbbd0e66b9feb5786e1aec06df96074984adb'
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
new_methods = None
for name in before:
    old_text = before[name].read_text(encoding='utf-8-sig')
    new_text = after[name].read_text(encoding='utf-8-sig')
    if old_text == new_text:
        continue
    changed.append(name)
    diffs.extend(difflib.unified_diff(old_text.splitlines(True), new_text.splitlines(True), fromfile='before/' + name, tofile='after/' + name))
    if name.endswith('.FoodWeaponBondHits.java'):
        fragment = 'if (var4 && !(FoodWeaponSourceLedger.actualHurtSnapshot(var2) && "NATIVE_MELEE_ACTUAL_HURT".equals(var2.getString("DamagePurpose"))))'
        assert new_text.count(fragment) == 1
        assert new_text.replace(fragment, 'if (var4)') == old_text
    elif name.endswith('.NativeReadCutPlayerMixin.java'):
        assert 'public abstract class NativeReadCutPlayerMixin {\n}' in old_text
        assert new_text.count('@WrapOperation(') == 1 and new_text.count('private boolean ') == 1
        assert 'target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"' in new_text
        assert 'require = 1, expect = 1, allow = 1' in new_text
        assert 'return FoodWeaponSourceLedger.nativeMeleeActualHurt((Player)(Object)this, target, source, amount, original);' in new_text
    elif name.endswith('.FoodWeaponSourceLedger.java'):
        start = new_text.index('    private static boolean nativeMeleeBoundary() {')
        finish = new_text.index('    public static boolean nativeMeatActualHurt(', start)
        new_methods = new_text[start:finish]
        restored = new_text[:start] + new_text[finish:]
        replacements = [
            ('                : (var0.earlyIngress != null\n                    ? (var0.earlyIngress.kind == NativeIngressKind.MELEE ? RootSite.NATIVE_MELEE_IMPACT : RootSite.NATIVE_MEAT_IMPACT)\n                    : FoodWeaponSourceLedger.RootSite.NATIVE_CARRIER_IMPACT);',
             '                : (var0.earlyIngress != null ? FoodWeaponSourceLedger.RootSite.NATIVE_MEAT_IMPACT : FoodWeaponSourceLedger.RootSite.NATIVE_CARRIER_IMPACT);'),
            ('            return var0.earlyIngress == null ? matchesNativeIngress(var0.receipt, var0.carrier, var0.player)\n                : var0.earlyIngress.kind == NativeIngressKind.MELEE ? nativeMeleeReady(var0.earlyIngress) : meatHurtReady(var0.earlyIngress);',
             '            return var0.earlyIngress == null ? matchesNativeIngress(var0.receipt, var0.carrier, var0.player) : meatHurtReady(var0.earlyIngress);'),
            (' && !"NATIVE_IRON_ACTUAL_HURT".equals(var1)\n                && !"NATIVE_MELEE_ACTUAL_HURT".equals(var1)) {', ' && !"NATIVE_IRON_ACTUAL_HURT".equals(var1)) {'),
            ('        CORAL_BARDICHE("com.github.L_Ender.cataclysm.items.Coral_Bardiche", "cataclysm:coral_bardiche", 1),\n        MELEE("net.minecraft.world.entity.player.Player", "minecraft:player_attack", 0);',
             '        CORAL_BARDICHE("com.github.L_Ender.cataclysm.items.Coral_Bardiche", "cataclysm:coral_bardiche", 1);'),
            ('        NATIVE_MELEE_IMPACT,\n', ''),
            ('            case MELEE -> false;\n', ''),
        ]
        for actual, expected in replacements:
            assert restored.count(actual) == 1, actual
            restored = restored.replace(actual, expected, 1)
        assert restored.count('case MEAT, MELEE -> false;') == 2
        restored = restored.replace('case MEAT, MELEE -> false;', 'case MEAT -> false;')
        assert restored == old_text, 'Unbounded source-ledger modification outside original scoped melee entry'
    else:
        assert re.sub(r'[0-9a-f]{64}', '<HASH>', old_text) == re.sub(r'[0-9a-f]{64}', '<HASH>', new_text), name
        assert any(Path(entry['source']).name == after[name].name for entry in new['authorityBindingUpdates']), name
        binding_only.append(name)
assert new_methods is not None
assert len(changed) == len(binding_only) + 3
assert load(Path(new['attempt']) / 'asm.json')['failures'] == 0
diff_path = work / ('reports/native-melee-ingress-delta-' + tag + '.patch.txt')
output = work / ('reports/native-melee-ingress-delta-' + tag + '.json')
assert not diff_path.exists() and not output.exists()
diff_path.write_text(''.join(diffs), encoding='utf-8')
files = [old_path, new_path, old_arguments, new_arguments, Path(__file__), diff_path, Path(new['attempt']) / 'asm.json']
for name in before:
    files.extend([before[name], after[name]])
result = {
    'oldCandidate': str(old_path), 'newCandidate': str(new_path), 'changedSources': changed,
    'otherChangedSourcesAreHashBindingRefreshOnly': binding_only,
    'onlyThreeBehavioralSources': ['NativeReadCutPlayerMixin', 'FoodWeaponSourceLedger', 'FoodWeaponBondHits'],
    'sourceLedgerChangesReversedExactlyToOldEffectiveSource': True,
    'originalNativeIngressAndCarrierPathsRejectNewMeleeKind': True,
    'newOriginalMeleeMethodsSha256': hashlib.sha256(new_methods.encode('utf-8')).hexdigest(),
    'actualHurtPurposeRequiresExactLivePrivateScopeAndOriginalDamageBinding': True,
    'newProductClasses': 0, 'classCount': 1381, 'effectiveSourceCount': 716, 'guardCount': 208,
    'targetedAsmFailures': 0, 'allOtherEffectiveSourcesByteIdentical': True,
    'staticProofIsNotRuntimeOrCompleteMeleeLifecycleAcceptance': True,
    'installable': False, 'deployed': False, 'inputHashes': {str(path): digest(path) for path in files}
}
output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'changedSources': changed, 'hashes': len(result['inputHashes']), 'failures': 0}))
