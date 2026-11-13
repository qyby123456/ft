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

source = (work / 'tests/paid-melee-session-diagnostic/PositiveWeapon436.java').read_text(encoding='utf-8')
source = source.replace('List.of("kind", "phase", "closed")', 'List.of("phase", "closed", "failedPhase")')
anchor = '                        publicationRow.put("traceComplete", complete.invoke(session));'
assert source.count(anchor) == 1
source = source.replace(anchor, anchor + '''
                        Class<?> registry = Class.forName("net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry");
                        publicationRow.put("registryInvalid", readStatic(registry, "invalid"));
                        String[][] required = (String[][])readStatic(registry, "REQUIRED");
                        boolean[] installed = (boolean[])readStatic(registry, "INSTALLED");
                        Object[] observed = (Object[])readStatic(registry, "OBSERVED");
                        Object[] targets = (Object[])readStatic(registry, "TARGETS");
                        List<Map<String, Object>> absent = new ArrayList<>(); publicationRow.put("registryNotOrdinarilyInstalledTargets", absent);
                        for (int index = 0; index < required.length; index++) {
                            if (installed[index] && observed[index] != null && targets[index] != null) continue;
                            Map<String, Object> state = new LinkedHashMap<>(); absent.add(state);
                            state.put("index", index); state.put("mixin", required[index][0]); state.put("target", required[index][1]);
                            state.put("installed", installed[index]); state.put("observed", observed[index] != null); state.put("targetPresent", targets[index] != null);
                        }
                        publicationRow.put("registryLateStages", readStatic(registry, "lateStages"));
                        Object writer = read(read(session, "lineage"), "issuer");
                        Object prepared = read(writer, "fieldPreparation");
                        if (prepared != null) {
                            for (String flag : List.of("unsupported", "retired", "sealed", "bound")) publicationRow.put("fieldPrepared_" + flag, read(prepared, flag));
                            publicationRow.put("fieldPreparedCoveragePresent", read(prepared, "coverage") != null);
                            publicationRow.put("fieldPreparedCatalogueGenerationPresent", read(prepared, "catalogueGeneration") != null);
                        }''')
add('work/session-lease/tests/paid-melee-coverage-diagnostic/PositiveWeapon436.java', source)
launcher = (work / 'tools/run_native_paid_melee_session_diagnostic.py').read_text(encoding='utf-8')
launcher = launcher.replace('tests/paid-melee-session-diagnostic/PositiveWeapon436.java', 'tests/paid-melee-coverage-diagnostic/PositiveWeapon436.java')
launcher = launcher.replace('native-repair-runtime-paid-melee-session-diagnostic.json', 'native-repair-runtime-paid-melee-coverage-diagnostic.json')
add('work/session-lease/tools/run_native_paid_melee_coverage_diagnostic.py', launcher)
print('Prepared read-only missing registration/coverage catalogue state, same frozen 8e76bf0d product')
