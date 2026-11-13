import hashlib
import json
import re
import subprocess
import sys
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = Path(__file__).resolve().parents[1]
tag = sys.argv[1]
assert re.fullmatch('[0-9a-f]{8}', tag)


def add(path, text):
    assert not path.exists(), path
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
            body = '*** Update File: ' + path.as_posix() + '\n@@\n' + ''.join(' ' + line + '\n' for line in previous[-4:])
            suffix = '*** End of File\n'
        else:
            body = '*** Add File: ' + path.as_posix() + '\n'
            suffix = ''
        patch = '*** Begin Patch\n' + body + ''.join('+' + line + '\n' for line in chunk) + suffix + '*** End Patch\n'
        result = subprocess.run(['C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe', '--codex-run-as-apply-patch', patch], cwd=root, capture_output=True)
        assert result.returncode == 0, result.stderr.decode('utf-8', errors='replace')
        previous.extend(chunk)


old_source = work / ('tests/paid-frost-ingress-' + tag + '/PositiveWeapon436.java')
source = old_source.read_text(encoding='utf-8')
anchor = '                        publicationRow.put("traceComplete", complete.invoke(session));'
assert source.count(anchor) == 1
source = source.replace(anchor, anchor + '''
                        for (String flag : List.of("traceRevoked", "coverageInstalled", "next")) publicationRow.put("session_" + flag, read(session, flag));
                        Object observationFailure = read(session, "firstObservationFailure");
                        if (observationFailure instanceof Throwable failure) {
                            java.io.StringWriter diagnostic = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(diagnostic));
                            publicationRow.put("firstObservationFailure", diagnostic.toString());
                        }
''')
new_source = work / ('tests/frost-session-diagnostic-' + tag + '/PositiveWeapon436.java')
add(new_source, source)
old_launcher = work / ('tools/run_native_paid_frost_ingress_' + tag + '.py')
launcher = old_launcher.read_text(encoding='utf-8')
launcher = launcher.replace('tests/paid-frost-ingress-' + tag + '/PositiveWeapon436.java', 'tests/frost-session-diagnostic-' + tag + '/PositiveWeapon436.java')
launcher = launcher.replace('reports/native-repair-runtime-paid-frost-ingress-' + tag + '.json', 'reports/native-repair-runtime-frost-session-diagnostic-' + tag + '.json')
new_launcher = work / ('tools/run_native_frost_session_diagnostic_' + tag + '.py')
add(new_launcher, launcher)
output = work / ('reports/native-frost-session-diagnostic-preparation-' + tag + '.json')
assert not output.exists()
output.write_text(json.dumps({'sameCandidateNoProductChanges': True, 'readOnlySessionDiagnostic': True, 'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in [Path(__file__), old_source, old_launcher, new_source, new_launcher]}}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'tag': tag, 'sessionDiagnosticPrepared': True}))
