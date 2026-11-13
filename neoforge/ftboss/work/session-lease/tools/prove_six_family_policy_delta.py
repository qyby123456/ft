import difflib
import hashlib
import json
import re
from pathlib import Path

work = Path(__file__).resolve().parents[1]

def load(path):
    return json.loads(path.read_text(encoding='utf-8'))

def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()

old_receipt = work / 'reports/native-repair-candidate-2ca083af.json'
new_receipt = work / 'reports/native-repair-six-family-candidate-7751b0a9.json'
assert digest(old_receipt) == '842ff9f8dbc7d746fa9979595e4172ff16c0279fc92bb91fa0c9b92c7652aa74'
assert digest(new_receipt) == '4a3b9bf6c0acc7499b1183469f370a90a4b1105a660e7f25370598ee39aae847'
old = load(old_receipt)
new = load(new_receipt)
assert old['classCount'] == new['classCount'] == 1381
assert old['effectiveSourceCount'] == new['effectiveSourceCount'] == 716
assert old['guardCount'] == new['guardCount'] == 208 and not new['newClasses']
old_arguments = Path(old['attempt']) / 'complete-arguments316.json'
new_arguments = Path(new['attempt']) / 'complete-arguments316.json'

def sources(arguments):
    paths = [Path(value) for value in load(arguments) if value.endswith('.java')]
    result = {}
    for path in paths:
        text = path.read_text(encoding='utf-8-sig')
        package = re.search(r'^package\s+([^;]+);', text, re.MULTILINE)
        key = (package.group(1) + '.' if package else '') + path.name
        assert key not in result, key
        result[key] = path
    assert len(paths) == len(result) == 716
    return result

before = sources(old_arguments)
after = sources(new_arguments)
assert before.keys() == after.keys()
changed = []
binding_only = []
for name in before:
    old_text = before[name].read_text(encoding='utf-8-sig')
    new_text = after[name].read_text(encoding='utf-8-sig')
    if old_text == new_text:
        continue
    changed.append(name)
    if name.endswith('.OwnerRepairGenerationAuthority.java'):
        start = new_text.index('    static boolean nativeTaskObjectPolicy(')
        finish = new_text.index('    private static boolean nativePolicyCurrent(', start)
        expected_method = '''    static boolean nativeTaskObjectPolicy(Object actual) {
        if (actual == null || actual.getClass() != ItemStack.class || !(actual instanceof ItemStack stack) || stack.getCount() != 1
            || !NativeFoodWeaponWear.eligible(stack) || !(actual instanceof OwnerRepairCapabilityFields)) return false;
        Class<?> type = stack.getItem().getClass();
        return type == SwordItem.class || type == BowItem.class || type == CrossbowItem.class || type == AxeItem.class
            || type == TridentItem.class || type == FrostQueenStaffItem.class;
    }

'''
        assert new_text[start:finish] == expected_method
        restored = new_text.replace('import net.minecraft.world.item.AxeItem;\n', '')
        restored = restored.replace('return type == SwordItem.class || type == BowItem.class || type == CrossbowItem.class || type == AxeItem.class\n'
            '            || type == TridentItem.class || type == FrostQueenStaffItem.class;',
            'return type == SwordItem.class || type == BowItem.class || type == CrossbowItem.class || type == TridentItem.class;', 1)
        assert restored == old_text, 'Unexpected non-policy behavioral source change'
    else:
        assert re.sub(r'[0-9a-f]{64}', '<HASH>', old_text) == re.sub(r'[0-9a-f]{64}', '<HASH>', new_text), name
        assert any(Path(entry['source']).name == after[name].name for entry in new['authorityBindingUpdates']), name
        binding_only.append(name)
assert 'net.farmingtales.ftboss.item.OwnerRepairGenerationAuthority.java' in changed
assert load(Path(new['attempt']) / 'asm.json')['failures'] == 0
files = [old_receipt, new_receipt, old_arguments, new_arguments, Path(__file__), Path(new['attempt']) / 'asm.json']
for name in before:
    files.extend([before[name], after[name]])
hashes = {str(path): digest(path) for path in files}
output = work / 'reports/native-six-family-policy-delta-7751b0a9.json'
assert not output.exists()
result = {
    'oldCandidate': str(old_receipt), 'newCandidate': str(new_receipt),
    'onlyBehavioralSourceChange': 'OwnerRepairGenerationAuthority.nativeTaskObjectPolicy exact AxeItem/FrostQueenStaffItem admission',
    'changedSources': changed, 'otherChangedSourcesAreHashBindingRefreshOnly': binding_only,
    'allOtherEffectiveSourcesByteIdentical': True, 'legacyKnownStackPolicyUnchanged': True,
    'originalNativeEligibilityAndCountOneExactItemStackAndCapabilityFieldChecksPreserved': True,
    'genericSubclassOrNameBasedAuthorityAdmissionAdded': False,
    'newProductClasses': 0, 'classCount': 1381, 'effectiveSourceCount': 716, 'guardCount': 208,
    'targetedAsmFailures': 0, 'gameplayConstantsChanged': False,
    'nonDamageableStaffDurabilityChanged': False,
    'staticDeltaIsNotFullBehavioralEquivalenceOrRuntimeAcceptance': True,
    'installable': False, 'deployed': False, 'inputHashes': hashes
}
output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'changedSourceCount': len(changed), 'bindingOnlyCount': len(binding_only), 'hashes': len(hashes), 'failures': 0}))
