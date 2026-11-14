import hashlib
import json
from pathlib import Path
work = Path(__file__).resolve().parents[1]
root = work.parents[1]
parent = work / 'reports/native-trident-strict-qualified-preparation-338f0feb.json'
frozen = json.loads(parent.read_text(encoding='utf-8'))
for name, wanted in frozen['inputHashes'].items():
    assert hashlib.sha256(Path(name).read_bytes()).hexdigest() == wanted, name
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
new_source = work / 'tests/trident-sequence-revocation-338f0feb/PositiveWeapon436.java'
new_launcher = work / 'tools/run_native_trident_sequence_revocation_338f0feb.py'
preparation = work / 'reports/native-trident-sequence-revocation-preparation-338f0feb.json'
source = old_source.read_text(encoding='utf-8')
anchor = '                        Object observationFailure = read(session, "firstObservationFailure");'
assert source.count(anchor) == 1
source = source.replace(anchor, '                        Object revocationSite = read(session, "firstRevocationSite434");\n                        if (revocationSite instanceof Throwable failure) {\n                            java.io.StringWriter diagnostic = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(diagnostic));\n                            publicationRow.put("firstRevocationSite434", diagnostic.toString());\n                        }\n                        Object failedStep = read(session, "firstFailedStep");\n                        if (failedStep != null) {\n                            Map<String, Object> failed = new LinkedHashMap<>(); publicationRow.put("firstFailedStep", failed);\n                            for (String name : List.of("observedPrefixes", "phase", "started", "entered", "copyVerified", "directFieldsVerified", "writeObserved", "originalReturned", "closed", "failedPhase", "nativeTimerPrefixesPrepared", "nativeTimerComponentAssigned")) failed.put(name, read(failedStep, name));\n                            Object operation = read(failedStep, "expected");\n                            failed.put("expectedKind", String.valueOf(read(operation, "kind")));\n                            for (String name : List.of("leaf", "originalReceiver", "originalValue", "originalArgument", "originalReturn")) {\n                                Object value = read(failedStep, name); failed.put(name + "Class", value == null ? null : value.getClass().getName());\n                            }\n                        }\n' + anchor)
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8')
launcher = launcher.replace(old_source.relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
launcher = launcher.replace('reports/native-repair-runtime-trident-foundation-gate-338f0feb.json', 'reports/native-repair-runtime-trident-sequence-revocation-338f0feb.json')
anchor = "save(attempt / 'inputs-before308.json', inputs)"
head, tail = launcher.rsplit(anchor, 1)
launcher = head + "inputs[str(WORK / 'reports/native-trident-sequence-revocation-preparation-338f0feb.json')] = digest(WORK / 'reports/native-trident-sequence-revocation-preparation-338f0feb.json')\n" + anchor + tail
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent, helper, old_source, old_launcher, new_source, new_launcher]
with preparation.open('x', encoding='utf-8') as stream:
    json.dump({'candidate': frozen['candidate'], 'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'authorityFieldsWritten': False, 'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}}, stream, ensure_ascii=False, indent=2)
print(json.dumps({'prepared': preparation.name}))
