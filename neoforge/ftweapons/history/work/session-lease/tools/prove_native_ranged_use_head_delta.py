import hashlib
import json
import re
from pathlib import Path


work = Path(__file__).resolve().parents[1]
root = work.parents[1]
before_path = work / 'reports/native-repair-frost-ingress-candidate-2e5c6034.json'
after_path = work / 'reports/native-repair-ranged-use-candidate-f4f2deae.json'
hashes = {}


def digest(path):
    value = hashlib.sha256(path.read_bytes()).hexdigest()
    hashes[str(path)] = value
    return value


def load(path):
    digest(path)
    return json.loads(path.read_text(encoding='utf-8-sig'))


def source_map(receipt):
    arguments = load(Path(receipt['attempt']) / 'complete-arguments316.json')
    result = {}
    for value in arguments:
        if not value.endswith('.java'):
            continue
        path = Path(value)
        text = path.read_text(encoding='utf-8-sig')
        package = re.search(r'^package\s+([^;]+);', text, re.M).group(1)
        key = package + '.' + path.stem
        assert key not in result
        result[key] = (path, text)
        digest(path)
    assert len(result) == 716
    return result


before = load(before_path)
after = load(after_path)
assert digest(before_path) == '4b8d5adca7c6e8531088fa9d9e71f431c5c2e340bf8798ddceab04f0302ff891'
assert digest(after_path) == '364940ee51d24f928443cab7d78e216d9ecd00b023bd1e2fd7006c473e64ea4f'
assert before['classCount'] == after['classCount'] == 1381
assert before['effectiveSourceCount'] == after['effectiveSourceCount'] == 716
assert before['guardCount'] == after['guardCount'] == 208
for receipt in [before, after]:
    for name, expected in receipt['inputHashes'].items():
        assert digest(Path(name)) == expected, name
previous = source_map(before)
current = source_map(after)
assert previous.keys() == current.keys()
changed = {key for key in previous if previous[key][1] != current[key][1]}
prefix = 'net.farmingtales.ftboss.'
behavior = {prefix + 'item.' + name for name in ['FoodWeaponSourceLedger', 'OwnerNativeSemanticAuthority']}
behavior.add(prefix + 'mixin.NativeReadCutItemStackMixin')
bindings = {prefix + 'item.NativeCapabilityInstallationProof170'}
assert changed == behavior | bindings, sorted(changed)
ledger = current[prefix + 'item.FoodWeaponSourceLedger'][1]
start = ledger.index('    private static boolean nativeRangedUseItem(')
finish = ledger.index('    private static boolean nativeFrostMerged(', start)
block = ledger[start:finish]
for required in ['OwnerFieldCoverageRegistry.nativeItemStackCertified165()', 'marker.rangedUseHead = true;', 'marker.before.equals(NativeFoundationItemData.image(marker.actual))', 'nativeFrostOriginalUseFrame()', 'OwnerRepairGenerationAuthority.matches(marker.actual, marker.repairGeneration)']:
    assert required in block
ledger = ledger[:start] + ledger[finish:]
for inserted in ['    private static final ThreadLocal<NativeIngressToken> PRIVATE_RANGED_USE = new ThreadLocal<>();\n', '        private boolean rangedUseHead;\n']:
    assert ledger.count(inserted) == 1
    ledger = ledger.replace(inserted, '')
assert ledger.count(', ITEM_USE -> false;') == 3
ledger = ledger.replace(', ITEM_USE -> false;', ' -> false;')
ledger = ledger.replace('        FROST("net.farmingtales.ftboss.item.FrostQueenStaffItem", "ftboss:frost_queen_staff", 0),\n        ITEM_USE("net.minecraft.world.item.ItemStack", "minecraft:original_item_use", 0);', '        FROST("net.farmingtales.ftboss.item.FrostQueenStaffItem", "ftboss:frost_queen_staff", 0);')
assert ledger == previous[prefix + 'item.FoodWeaponSourceLedger'][1]
semantic = current[prefix + 'item.OwnerNativeSemanticAuthority'][1]
semantic = semantic.replace('if (operation == null) return FoodWeaponSourceLedger.nativeFrostOriginalHead(receiver, kind)\n            || FoodWeaponSourceLedger.nativeRangedOriginalHead(receiver, kind);', 'if (operation == null) return FoodWeaponSourceLedger.nativeFrostOriginalHead(receiver, kind);')
assert semantic == previous[prefix + 'item.OwnerNativeSemanticAuthority'][1]
mixin = current[prefix + 'mixin.NativeReadCutItemStackMixin'][1]
for inserted in ['      FoodWeaponSourceLedger.nativeRangedOriginalUseEntered((ItemStack)(Object)this, level, player, hand);\n', '      FoodWeaponSourceLedger.nativeRangedOriginalUseReturned((ItemStack)(Object)this);\n']:
    assert mixin.count(inserted) == 1
    mixin = mixin.replace(inserted, '')
assert mixin == previous[prefix + 'mixin.NativeReadCutItemStackMixin'][1]
old_classes = Path(before['attempt']) / 'complete-classes'
new_classes = Path(after['attempt']) / 'complete-classes'
old_graph = {path.relative_to(old_classes).as_posix(): digest(path) for path in old_classes.rglob('*.class')}
new_graph = {path.relative_to(new_classes).as_posix(): digest(path) for path in new_classes.rglob('*.class')}
assert len(old_graph) == len(new_graph) == 1381 and old_graph.keys() == new_graph.keys()
replacements = {old_graph[key]: new_graph[key] for key in old_graph if old_graph[key] != new_graph[key]}
for key in bindings:
    expected = re.sub(r'[a-f0-9]{64}', lambda match: replacements.get(match.group(0), match.group(0)), previous[key][1])
    assert expected == current[key][1], key
digest(Path(__file__))
output = work / 'reports/native-ranged-use-head-delta-f4f2deae.json'
assert not output.exists()
output.write_text(json.dumps({'failures': 0, 'before': str(before_path), 'after': str(after_path), 'exactInverseBehaviorSources': sorted(behavior), 'bindingOnlySources': sorted(bindings), 'unrelatedEffectiveSourcesUnchanged': 712, 'classes': 1381, 'sources': 716, 'guards': 208, 'newClasses': [], 'runtimeAccepted': False, 'inputHashes': hashes}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'report': str(output), 'failures': 0, 'inputHashCount': len(hashes)}))
