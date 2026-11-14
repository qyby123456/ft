import subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = Path(__file__).resolve().parents[1]

def add(relative, text):
    assert not (root / relative).exists(), relative
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

source = (work / 'tests/paid-melee-ingress-8e76bf0d/PositiveWeapon436.java').read_text(encoding='utf-8')
anchor = '                        publicationRow.put("traceComplete", complete.invoke(session));'
assert source.count(anchor) == 1
source = source.replace(anchor, anchor + '''
                        for (String flag : List.of("coverageInstalled", "traceRevoked", "next")) publicationRow.put("session_" + flag, read(session, flag));
                        for (String flag : List.of("firstObservationFailure", "firstRevocationSite434")) {
                            Object value = read(session, flag);
                            publicationRow.put("session_" + flag, value == null ? null : value.toString());
                            if (value instanceof Throwable failure) {
                                List<String> frames = new ArrayList<>();
                                for (StackTraceElement frame : failure.getStackTrace()) { frames.add(frame.toString()); if (frames.size() == 10) break; }
                                publicationRow.put("session_" + flag + "Frames", frames);
                            }
                        }
                        Object firstStep = read(session, "firstFailedStep");
                        if (firstStep != null) for (String flag : List.of("kind", "phase", "closed")) publicationRow.put("failedStep_" + flag, read(firstStep, flag));''')
add('work/session-lease/tests/paid-melee-session-diagnostic/PositiveWeapon436.java', source)
launcher = (work / 'tools/run_native_paid_melee_ingress_8e76bf0d.py').read_text(encoding='utf-8')
launcher = launcher.replace('tests/paid-melee-ingress-8e76bf0d/PositiveWeapon436.java', 'tests/paid-melee-session-diagnostic/PositiveWeapon436.java')
launcher = launcher.replace('native-repair-runtime-paid-melee-ingress-8e76bf0d.json', 'native-repair-runtime-paid-melee-session-diagnostic.json')
add('work/session-lease/tools/run_native_paid_melee_session_diagnostic.py', launcher)
print('Prepared read-only first failing Session observation, same frozen 8e76bf0d product')
