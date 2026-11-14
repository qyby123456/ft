import hashlib
import json
import subprocess
import sys
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = Path(__file__).resolve().parents[1]
mode = sys.argv[1]
assert mode in ['regression', 'bow']
candidate = work / 'reports/native-repair-semantic-noop-candidate.json'
receipt = json.loads(candidate.read_text(encoding='utf-8'))
tag = Path(receipt['attempt']).name.removeprefix('native-repair-')
frozen = work / ('reports/native-repair-semantic-noop-candidate-' + tag + '.json')
if frozen.exists():
    assert frozen.read_bytes() == candidate.read_bytes()
else:
    frozen.write_bytes(candidate.read_bytes())
if mode == 'regression':
    text = (work / 'tools/run_native_semantic_noop_regression.py').read_text(encoding='utf-8')
    old_name = 'native-repair-semantic-noop-candidate-e770d538.json'
    old_hash = '2a3975ba894985b0cab12e7f6a5c3bb432792cdf8102a1fff0f60b9ddf891cea'
    old_output = 'native-repair-runtime-semantic-noop-regression.json'
else:
    text = (work / 'tools/run_native_semantic_noop_signed_bow.py').read_text(encoding='utf-8')
    old_name = 'native-repair-semantic-noop-candidate-e770d538.json'
    old_hash = '2a3975ba894985b0cab12e7f6a5c3bb432792cdf8102a1fff0f60b9ddf891cea'
    old_output = 'native-repair-runtime-semantic-noop-signed-bow.json'
text = text.replace(old_name, frozen.name).replace(old_hash, hashlib.sha256(frozen.read_bytes()).hexdigest())
text = text.replace(old_output, 'native-repair-runtime-semantic-noop-' + mode + '-' + tag + '.json')
relative = 'work/session-lease/tools/run_native_semantic_noop_' + mode + '_' + tag + '.py'
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
print(json.dumps({'candidate': str(frozen), 'sha256': hashlib.sha256(frozen.read_bytes()).hexdigest(), 'launcher': relative, 'mode': mode}))
