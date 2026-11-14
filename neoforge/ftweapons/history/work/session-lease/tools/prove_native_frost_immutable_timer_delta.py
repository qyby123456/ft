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
        if value.endswith('.java'):
            path = Path(value)
            package = re.search(r'^package\s+([^;]+);', path.read_text(encoding='utf-8-sig'), re.MULTILINE)
            key = package.group(1) + '.' + path.name
            assert key not in result
            result[key] = path
    assert len(result) == 716
    return result


baseline_path = work / 'reports/native-repair-frost-ingress-candidate-9535b6b4.json'
assert digest(baseline_path) == '3bef872269e02f58c954818c7fa92c7daa6f085fdaac10cbcac8bc82b398f2bf'
baseline_delta = work / 'reports/native-frost-ingress-delta-9535b6b4.json'
for name, expected in load(baseline_delta)['inputHashes'].items():
    assert digest(Path(name)) == expected, name
candidate_path = work / ('reports/native-repair-frost-ingress-candidate-' + tag + '.json')
baseline = load(baseline_path)
candidate = load(candidate_path)
assert candidate['classCount'] == 1381 and candidate['effectiveSourceCount'] == 716 and candidate['guardCount'] == 208 and not candidate['newClasses']
old_arguments = Path(baseline['attempt']) / 'complete-arguments316.json'
new_arguments = Path(candidate['attempt']) / 'complete-arguments316.json'
before = sources(old_arguments)
after = sources(new_arguments)
assert before.keys() == after.keys()
behavioral = []
bindings = []
for name in before:
    previous = before[name].read_text(encoding='utf-8-sig')
    current = after[name].read_text(encoding='utf-8-sig')
    if previous == current:
        continue
    basename = after[name].name
    if basename == 'NativeTimerTransitionPlan.java':
        new_line = '        Tag encoded = image.timerPatchSnapshot(source);'
        old_lines = '        Tag encoded = NativeExpectedFieldObservation.timerPatchForRead(\n            NativeFoundationItemData.registries(), detached.getComponentsPatch());'
        assert current.count(new_line) == 1 and current.replace(new_line, old_lines) == previous
        behavioral.append(name)
    elif basename == 'NativeItemComponentObservation.java':
        start = current.index('    Tag timerPatchSnapshot(ItemStack actual) {')
        finish = current.index('    public void observeRepairStorage(', start)
        assert current[:start] + current[finish:] == previous
        block = current[start:finish]
        for requirement in ['NativeTimerTransitionPlan.class', 'nativeWeaponBefore', 'actual != this.stack', '!matches(actual)', 'return this.encodedPatch.copy();']:
            assert requirement in block
        behavioral.append(name)
    else:
        assert re.sub(r'[0-9a-f]{64}', '<HASH>', previous) == re.sub(r'[0-9a-f]{64}', '<HASH>', current), name
        assert any(Path(entry['source']).name == basename for entry in candidate['authorityBindingUpdates']), name
        bindings.append(name)
assert len(behavioral) == 2
asm = Path(candidate['attempt']) / 'asm.json'
assert load(asm)['failures'] == 0
paths = [Path(__file__), baseline_path, baseline_delta, candidate_path, old_arguments, new_arguments, asm, *before.values(), *after.values()]
output = work / ('reports/native-frost-immutable-timer-delta-' + tag + '.json')
assert not output.exists()
output.write_text(json.dumps({'baselineCandidate': str(baseline_path), 'candidate': str(candidate_path), 'baselineFrostDelta': str(baseline_delta), 'classCount': 1381, 'effectiveSourceCount': 716, 'guardCount': 208, 'newProductClasses': 0, 'behavioralChangesSince9535b6b4': behavioral, 'bindingOnlyChanges': bindings, 'allOtherEffectiveSourcesByteIdentical': True, 'oldTimerPlanByteIdenticalOutsideImmutablePatchRead': True, 'oldComponentObservationByteIdenticalOutsideExactCallerReadMethod': True, 'runtimeAccepted': False, 'installable': False, 'inputHashes': {str(path): digest(path) for path in paths}}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'behavioralChangesSince9535b6b4': len(behavioral), 'bindingOnly': len(bindings), 'targetedAsmFailures': 0}))
