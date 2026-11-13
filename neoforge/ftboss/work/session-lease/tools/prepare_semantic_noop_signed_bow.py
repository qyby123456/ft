import subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = Path(__file__).resolve().parents[1]
source = work / 'tools/run_native_repair_reload_successor_source.py'
text = source.read_text(encoding='utf-8')
text = text.replace('native-repair-candidate-2ca083af.json', 'native-repair-semantic-noop-candidate-e770d538.json')
text = text.replace('842ff9f8dbc7d746fa9979595e4172ff16c0279fc92bb91fa0c9b92c7652aa74', '2a3975ba894985b0cab12e7f6a5c3bb432792cdf8102a1fff0f60b9ddf891cea')
text = text.replace('native-repair-runtime-repair-reload-successor-source.json', 'native-repair-runtime-semantic-noop-signed-bow.json')
relative = 'work/session-lease/tools/run_native_semantic_noop_signed_bow.py'
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
print('Prepared same frozen paid bow successor signed-Session fixture on e770d538; not broad regression replay')
