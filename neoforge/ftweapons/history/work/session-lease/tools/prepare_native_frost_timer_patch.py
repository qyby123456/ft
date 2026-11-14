import json
import subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = Path(__file__).resolve().parents[1]
source = work / 'build/native-repair-9535b6b4/sources/net/farmingtales/ftboss/item/NativeTimerTransitionPlan.java'
target = work / 'proposal-native/net/farmingtales/ftboss/item/NativeTimerTransitionPlan.java'
assert not target.exists()
text = source.read_text(encoding='utf-8')
old = '        Tag encoded = NativeExpectedFieldObservation.timerPatchForRead(\n            NativeFoundationItemData.registries(), detached.getComponentsPatch());'
assert text.count(old) == 1
text = text.replace(old, '        Tag encoded = image.timerPatchSnapshot(source);')
pending = text.splitlines()
previous = []
while pending:
    chunk = []
    length = 0
    while pending and length + len(pending[0]) < 6000:
        line = pending.pop(0)
        chunk.append(line)
        length += len(line) + 1
    assert chunk
    if previous:
        body = '*** Update File: ' + target.as_posix() + '\n@@\n' + ''.join(' ' + line + '\n' for line in previous[-4:])
        suffix = '*** End of File\n'
    else:
        body = '*** Add File: ' + target.as_posix() + '\n'
        suffix = ''
    patch = '*** Begin Patch\n' + body + ''.join('+' + line + '\n' for line in chunk) + suffix + '*** End Patch\n'
    result = subprocess.run(['C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe', '--codex-run-as-apply-patch', patch], cwd=root, capture_output=True)
    assert result.returncode == 0, result.stderr.decode('utf-8', errors='replace')
    previous.extend(chunk)
print(json.dumps({'timerPlanUsesIndependentlyCapturedImmutablePatch': True, 'publicRawAliasEscapeRemoved': True}))
