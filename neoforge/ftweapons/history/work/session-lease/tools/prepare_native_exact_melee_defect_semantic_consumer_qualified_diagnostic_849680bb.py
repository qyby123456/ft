import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):
    return json.loads(path.read_text(encoding="utf-8"))
parent_path = work / "reports/native-exact-melee-defect-semantic-diagnostic-preparation-849680bb.json"
parent = load(parent_path)
for name, expected in parent["inputHashes"].items():
    assert digest(Path(name)) == expected, Path(name).name
failed_path = work / "reports/native-repair-runtime-exact-melee-defect-semantic-diagnostic-849680bb.json"
failed = load(failed_path)
terminal_path = Path(failed["attempt"]) / "terminal308.json"
terminal = load(terminal_path)
assert failed["exitCode"] == 0 and failed["probe"]["failures"] == 1 and terminal["stopSent"] and terminal["completeSave"]
assert "NoSuchFieldException: timerSemanticFailure" in failed["probe"]["failure"]
source = Path(parent["newObserver"]).read_text(encoding="utf-8")
anchor = '                        publicationRow.put("diagnostic_commitTimerSemanticFailure", String.valueOf(read(action, "timerSemanticFailure")));'
assert source.count(anchor) == 1
replacement = r'''                        Object diagnosticCommit = null;
                        for (Object producer : (List<?>)read(action, "producers")) {
                            if (producer.getClass().getSimpleName().equals("EvolutionProducer")) diagnosticCommit = read(producer, "consumerCommit");
                        }
                        if (diagnosticCommit == null) throw new AssertionError("Actual original evolution consumer commit missing");
                        publicationRow.put("diagnostic_commitTimerSemanticFailure", String.valueOf(read(diagnosticCommit, "timerSemanticFailure")));'''
source = source.replace(anchor, replacement)
source = source.replace('(List<?>)read(action, "timerSemantics")', '(List<?>)read(diagnosticCommit, "timerSemantics")')
new_source = work / "tests/exact-melee-defect-semantic-consumer-qualified-diagnostic-849680bb/PositiveWeapon436.java"
new_launcher = work / "tools/run_native_exact_melee_defect_semantic_consumer_qualified_diagnostic_849680bb.py"
preparation = work / "reports/native-exact-melee-defect-semantic-consumer-qualified-diagnostic-preparation-849680bb.json"
output = work / "reports/native-repair-runtime-exact-melee-defect-semantic-consumer-qualified-diagnostic-849680bb.json"
launcher = Path(parent["newLauncher"]).read_text(encoding="utf-8")
launcher = launcher.replace(Path(parent["newObserver"]).relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
launcher = launcher.replace(parent_path.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
launcher = launcher.replace(failed_path.relative_to(work).as_posix(), output.relative_to(work).as_posix())
helper = work / "tools/prepare_native_frost_ingress_runtime.py"
namespace = {"__file__": str(helper)}
exec(compile(helper.read_text(encoding="utf-8").split("candidate = work /")[0], str(helper), "exec"), namespace)
namespace["add"](new_source.relative_to(root).as_posix(), source)
namespace["add"](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent_path, failed_path, terminal_path, Path(parent["newObserver"]), Path(parent["newLauncher"]), new_source, new_launcher, helper]
with preparation.open("x", encoding="utf-8") as stream:
    json.dump({"newObserver": str(new_source), "newLauncher": str(new_launcher), "sameFrozenProduct": "849680bb", "onlyActualSample": 13,
               "diagnosticOnly": True, "authorityFieldsWritten": False, "installable": False,
               "inputHashes": {str(path): digest(path) for path in paths}}, stream, ensure_ascii=False, indent=2)
print(json.dumps({"prepared": preparation.name, "diagnosticOnly": True}))
