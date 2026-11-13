import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):
    return json.loads(path.read_text(encoding="utf-8"))
parent_path = work / "reports/native-exact-melee-configured-signed-preparation-849680bb.json"
parent = load(parent_path)
for name, wanted in parent["inputHashes"].items():
    assert digest(Path(name)) == wanted, Path(name).name
failed_path = work / "reports/native-repair-runtime-exact-melee-configured-signed-849680bb.json"
failed = load(failed_path)
terminal_path = Path(failed["attempt"]) / "terminal308.json"
terminal = load(terminal_path)
assert failed["exitCode"] == 0 and failed["probe"]["failures"] == 1 and terminal["completeSave"] and terminal["stopSent"]
assert failed["probe"]["cases"][-1]["id"] == "ftboss:defect_claw"
source = Path(parent["newObserver"]).read_text(encoding="utf-8")
anchor = "for (int sample = 0; sample < 19; sample++) {"
assert source.count(anchor) == 1
source = source.replace(anchor, "for (int sample : new int[]{13}) {")
anchor = "                        int expectedSize = verifyActualOriginalSessionReturns(session, preparation, publicationRow);"
assert source.count(anchor) == 1
injection = r'''                        for (String field : List.of("firstObservationFailure", "firstRevocationSite434")) {
                            Object problem = read(session, field);
                            java.io.StringWriter diagnostic = new java.io.StringWriter();
                            if (problem instanceof Throwable throwable) throwable.printStackTrace(new java.io.PrintWriter(diagnostic));
                            publicationRow.put("diagnostic_" + field, diagnostic.toString());
                        }
                        publicationRow.put("diagnostic_commitTimerSemanticFailure", String.valueOf(read(action, "timerSemanticFailure")));
                        List<Object> cursorRows = new ArrayList<>(); publicationRow.put("diagnostic_actualSemanticCursors", cursorRows);
                        for (Object cursor : (List<?>)read(action, "timerSemantics")) {
                            Map<String, Object> observedCursor = new LinkedHashMap<>(); cursorRows.add(observedCursor);
                            for (String field : List.of("next", "returnedHelpers", "timerEntered", "rangedActive", "rangedEntered", "timerReturned", "closed", "originalFailure", "status", "admittedKeys", "image", "projection"))
                                observedCursor.put(field, String.valueOf(read(cursor, field)));
                            Object program = read(cursor, "program");
                            for (String field : List.of("profile", "unsupported", "initialData", "baseline", "instructions"))
                                observedCursor.put("program_" + field, String.valueOf(read(program, field)));
                        }
                        Object diagnosticBook = read(session, "timerSemantics");
                        List<Object> diagnosticGroups = new ArrayList<>(); publicationRow.put("diagnostic_actualSemanticGroups", diagnosticGroups);
                        if (diagnosticBook != null) for (Object group : ((Map<?, ?>)read(diagnosticBook, "groups")).values()) {
                            Map<String, Object> observedGroup = new LinkedHashMap<>(); diagnosticGroups.add(observedGroup);
                            for (String field : List.of("profile", "selectedInstructions", "emptyProjection", "alternatives", "timers", "ranged", "projection", "acquired", "rooted", "guardObserved", "guardEmpty", "done"))
                                observedGroup.put(field, String.valueOf(read(group, field)));
                        }
                        if (Boolean.TRUE.equals(publicationRow.get("session_traceRevoked"))) throw new AssertionError("Excluded read-only diagnostic captured genuine defect semantic revocation");
                        int expectedSize = verifyActualOriginalSessionReturns(session, preparation, publicationRow);'''
source = source.replace(anchor, injection)
source = source.replace("nineteenConfiguredOriginalMeleeGenerationTwoFullSignedSessions", "singleDefectDiagnosticUnexpectedlyCompletedOriginalSession")
new_source = work / "tests/exact-melee-defect-semantic-diagnostic-849680bb/PositiveWeapon436.java"
new_launcher = work / "tools/run_native_exact_melee_defect_semantic_diagnostic_849680bb.py"
preparation = work / "reports/native-exact-melee-defect-semantic-diagnostic-preparation-849680bb.json"
output = work / "reports/native-repair-runtime-exact-melee-defect-semantic-diagnostic-849680bb.json"
launcher = Path(parent["newLauncher"]).read_text(encoding="utf-8")
launcher = launcher.replace(Path(parent["newObserver"]).relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
launcher = launcher.replace(parent_path.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
launcher = launcher.replace(failed_path.relative_to(work).as_posix(), output.relative_to(work).as_posix())
helper = work / "tools/prepare_native_frost_ingress_runtime.py"
namespace = {"__file__": str(helper)}
exec(compile(helper.read_text(encoding="utf-8").split("candidate = work /")[0], str(helper), "exec"), namespace)
namespace["add"](new_source.relative_to(root).as_posix(), source)
namespace["add"](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent_path, Path(parent["newObserver"]), Path(parent["newLauncher"]), failed_path, terminal_path, new_source, new_launcher, helper]
with preparation.open("x", encoding="utf-8") as stream:
    json.dump({"newObserver": str(new_source), "newLauncher": str(new_launcher), "sameFrozenProduct": "849680bb",
               "onlyActualSample": 13, "diagnosticOnly": True, "authorityFieldsWritten": False, "installable": False,
               "inputHashes": {str(path): digest(path) for path in paths}}, stream, ensure_ascii=False, indent=2)
print(json.dumps({"prepared": preparation.name, "diagnosticOnly": True}))
