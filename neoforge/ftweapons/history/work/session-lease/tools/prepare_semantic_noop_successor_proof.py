import subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = Path(__file__).resolve().parents[1]
text = (work / 'tools/prove_semantic_noop_delta.py').read_text(encoding='utf-8')
text = text.replace('e770d538', '524a9d86').replace('2a3975ba894985b0cab12e7f6a5c3bb432792cdf8102a1fff0f60b9ddf891cea', '64eae770e091193ef783dc46068cbbd0e66b9feb5786e1aec06df96074984adb')
anchor = '    else:\n        assert re.sub'
assert text.count(anchor) == 1
branch = '''    elif name.endswith('.OwnerNativeSemanticAuthority.java'):
        new_fragment = '            boolean unchanged = !transition.identityRequired && transition.physical.matches(stack)\\n                && FoodWeaponExpectedTrace.same(transition.input.expected, NativeExpectedRawFields.observe(transition.expected));\\n            nativeTransitionReturned(transition, stack, unchanged ? 0 : 1, !unchanged);'
        old_fragment = '            nativeTransitionReturned(transition, stack, 1, true);'
        assert new_text.count(new_fragment) == 1
        assert new_text.replace(new_fragment, old_fragment) == old_text
'''
text = text.replace(anchor, branch + anchor)
text = text.replace("'FoodWeaponEvolution.saveData skips identical evolution write']", "'FoodWeaponEvolution.saveData skips identical evolution write', 'OwnerNativeSemanticAuthority.nativeLifecycleReturned recognizes zero-write identity-preserving independently planned initialization']")
relative = 'work/session-lease/tools/prove_semantic_noop_successor_delta.py'
assert not (root / relative).exists()
lines = text.splitlines()
previous = []
while lines:
    chunk = []
    length = 0
    while lines and length + len(lines[0]) < 6000:
        line = lines.pop(0)
        chunk.append(line)
        length += len(line) + 1
    assert chunk
    if previous:
        body = '*** Update File: ' + relative + '\n@@\n' + ''.join(' ' + line + '\n' for line in previous[-4:])
        suffix = '*** End of File\n'
    else:
        body = '*** Add File: ' + relative + '\n'
        suffix = ''
    patch = '*** Begin Patch\n' + body + ''.join('+' + line + '\n' for line in chunk) + suffix + '*** End Patch\n'
    result = subprocess.run(['C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe', '--codex-run-as-apply-patch', patch], cwd=root, capture_output=True)
    assert result.returncode == 0, result.stderr.decode('utf-8', errors='replace')
    previous.extend(chunk)
print('Prepared exact three-source delta proof for 524a9d86')
