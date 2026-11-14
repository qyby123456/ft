import hashlib
import json
import re
import sys
from pathlib import Path

work = Path(__file__).resolve().parents[1]
tag = sys.argv[1]
assert re.fullmatch('[0-9a-f]{8}', tag)
origin = work / 'tools/prove_native_frost_immutable_timer_delta.py'
exec(compile(origin.read_text(encoding='utf-8').split('baseline_path = work /')[0], str(origin), 'exec'))
baseline_path = work / 'reports/native-repair-frost-ingress-candidate-667b6466.json'
assert digest(baseline_path) == '328f54baf61b75cef8bba71b75e24bd795ebdcf7437c238018f175c979f73119'
baseline_delta = work / 'reports/native-frost-immutable-timer-delta-667b6466.json'
for name, wanted in load(baseline_delta)['inputHashes'].items():
    assert digest(Path(name)) == wanted, name
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
    if after[name].name == 'NativeItemStackInstallationProof165.java':
        start = current.index('    private void frostUseObservers(')
        finish = current.index('    private void nativeUseRedirect(', start)
        restored = current[:start] + current[finish:]
        line = '        if (parent.name.equals("use")) frostUseObservers(actual, parent);\n'
        assert restored.count(line) == 1 and restored.replace(line, '') == previous
        for check in ['Whole authentic Frost use callback', 'actualCalls == expectedCalls', 'Objects.equals(value(merged, "priority"), 1)', 'Objects.equals(value(merged, "sessionId"), SESSION)', 'nativeComponentLambdaBody', 'new Analyzer<>(new BasicVerifier())']:
            assert check in current[start:finish]
        behavioral.append(name)
    else:
        assert re.sub(r'[0-9a-f]{64}', '<HASH>', previous) == re.sub(r'[0-9a-f]{64}', '<HASH>', current), name
        assert any(Path(entry['source']).name == after[name].name for entry in candidate['authorityBindingUpdates']), name
        bindings.append(name)
assert len(behavioral) == 1
asm = Path(candidate['attempt']) / 'asm.json'
assert load(asm)['failures'] == 0
paths = [Path(__file__), origin, baseline_path, baseline_delta, candidate_path, old_arguments, new_arguments, asm, *before.values(), *after.values()]
output = work / ('reports/native-frost-itemstack-proof-delta-' + tag + '.json')
assert not output.exists()
output.write_text(json.dumps({'baselineCandidate': str(baseline_path), 'candidate': str(candidate_path), 'priorDelta': str(baseline_delta), 'classCount': 1381, 'effectiveSourceCount': 716, 'guardCount': 208, 'newProductClasses': 0, 'behavioralChangesSince667b6466': behavioral, 'bindingOnlyChanges': bindings, 'allOtherEffectiveSourcesByteIdentical': True, 'allOldItemStackProofCodeByteIdenticalOutsideExactObserverReplay': True, 'runtimeAccepted': False, 'installable': False, 'inputHashes': {str(path): digest(path) for path in paths}}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'behavioralChangesSince667b6466': len(behavioral), 'bindingOnly': len(bindings), 'targetedAsmFailures': 0}))
