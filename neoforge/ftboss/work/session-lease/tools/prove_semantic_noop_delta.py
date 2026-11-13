import hashlib
import json
import re
from pathlib import Path

work = Path(__file__).resolve().parents[1]

def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))

def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()

old_receipt = work / 'reports/native-repair-six-family-candidate-7751b0a9.json'
new_receipt = work / 'reports/native-repair-semantic-noop-candidate-e770d538.json'
assert digest(old_receipt) == '4a3b9bf6c0acc7499b1183469f370a90a4b1105a660e7f25370598ee39aae847'
assert digest(new_receipt) == '2a3975ba894985b0cab12e7f6a5c3bb432792cdf8102a1fff0f60b9ddf891cea'
old = load(old_receipt)
new = load(new_receipt)
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
for name in before:
    old_text = before[name].read_text(encoding='utf-8-sig')
    new_text = after[name].read_text(encoding='utf-8-sig')
    if old_text == new_text:
        continue
    changed.append(name)
    if name.endswith('.FoodWeaponLifecycle.java'):
        new_fragment = '                if (!java.util.Objects.equals(nativeBefore, var2)) {\n                    NativeLifecycleItemData.commit(var0, "FTBossWeaponLifecycle", nativeBefore, var2);\n                }'
        old_fragment = '                NativeLifecycleItemData.commit(var0, "FTBossWeaponLifecycle", nativeBefore, var2);'
        assert new_text.count(new_fragment) == 1
        assert new_text.replace(new_fragment, old_fragment) == old_text
    elif name.endswith('.FoodWeaponEvolution.java'):
        new_fragment = '        if (!java.util.Objects.equals(data(var0), var1)) {\n        FoodWeaponData.write(var0, "FTBossFoodEvolution", var1);\n        }'
        old_fragment = '        FoodWeaponData.write(var0, "FTBossFoodEvolution", var1);'
        assert new_text.count(new_fragment) == 1
        assert new_text.replace(new_fragment, old_fragment) == old_text
    else:
        assert re.sub(r'[0-9a-f]{64}', '<HASH>', old_text) == re.sub(r'[0-9a-f]{64}', '<HASH>', new_text), name
        assert any(Path(entry['source']).name == after[name].name for entry in new['authorityBindingUpdates']), name
        binding_only.append(name)
assert {'net.farmingtales.ftboss.item.FoodWeaponLifecycle.java', 'net.farmingtales.ftboss.item.FoodWeaponEvolution.java'}.issubset(changed)
assert load(Path(new['attempt']) / 'asm.json')['failures'] == 0
files = [old_receipt, new_receipt, old_arguments, new_arguments, Path(__file__), Path(new['attempt']) / 'asm.json']
for name in before:
    files.extend([before[name], after[name]])
output = work / 'reports/native-semantic-noop-delta-e770d538.json'
assert not output.exists()
result = {
    'oldCandidate': str(old_receipt), 'newCandidate': str(new_receipt),
    'onlyBehavioralSourceChanges': ['FoodWeaponLifecycle.ensureTracked skips identical lifecycle commit', 'FoodWeaponEvolution.saveData skips identical evolution write'],
    'changedSources': changed, 'otherChangedSourcesAreHashBindingRefreshOnly': binding_only,
    'allOtherEffectiveSourcesByteIdentical': True, 'sixExactNativeTaskPoliciesUnchanged': True,
    'sourceAuthorizationGuardsAndRetirementChecksUnchanged': True,
    'newProductClasses': 0, 'classCount': 1381, 'effectiveSourceCount': 716, 'guardCount': 208,
    'targetedAsmFailures': 0, 'gameplayConstantsChanged': False,
    'staticDeltaIsNotFullBehavioralEquivalenceOrRuntimeAcceptance': True,
    'installable': False, 'deployed': False, 'inputHashes': {str(path): digest(path) for path in files}
}
output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'changedSources': changed, 'hashes': len(result['inputHashes']), 'failures': 0}))
