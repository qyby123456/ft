import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):
    return json.loads(path.read_text(encoding='utf-8'))
parent = work / 'reports/native-exact-melee-wooden-configured-signed-preparation-41d445cf.json'
frozen = load(parent)
for name, wanted in frozen['inputHashes'].items():
    assert digest(Path(name)) == wanted, Path(name).name
failed_path = work / 'reports/native-repair-runtime-exact-melee-wooden-configured-signed-41d445cf.json'
failed = load(failed_path)
terminal = Path(failed['attempt']) / 'terminal308.json'
end = load(terminal)
assert failed['exitCode'] == 0 and failed['probe']['failures'] == 1 and end['completeSave'] and end['stopSent']
assert 'Session incomplete or revoked' in failed['probe']['failure']
publication = failed['probe']['cases'][0]['publications'][0]
assert publication['traceComplete'] and publication['session_coverageInstalled'] and not publication['session_traceRevoked']
sequence_source = work.parent / 'session-continuity/proposal-transactional/net/farmingtales/ftboss/item/FoodWeaponOriginalSequence.java'
assert 'this.next == this.steps.length && this.expectedCursor == this.expected.expectedAfter' in sequence_source.read_text(encoding='utf-8')
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
source = old_source.read_text(encoding='utf-8')
anchor = '                        Object failure = read(session, "firstObservationFailure");'
assert source.count(anchor) == 1
source = source.replace(anchor, """                        Object[] steps = (Object[])read(session, "steps");
                        Object expectedPlan = read(session, "expected");
                        var size = expectedPlan.getClass().getDeclaredMethod("size"); size.setAccessible(true);
                        var operation = expectedPlan.getClass().getDeclaredMethod("operation", int.class); operation.setAccessible(true);
                        int expectedSize = ((Number)size.invoke(expectedPlan)).intValue();
                        publicationRow.put("originalExpectedPlanSize", expectedSize);
                        publicationRow.put("actualOriginalSessionStepCount", steps.length);
                        boolean terminalCursorMatches = read(session, "expectedCursor") == read(expectedPlan, "expectedAfter");
                        publicationRow.put("exactExpectedTerminalCursorIdentity", terminalCursorMatches);
                        List<Object> originalSteps = new ArrayList<>(); publicationRow.put("actualOriginalSteps", originalSteps);
                        if (expectedSize <= 0 || steps.length != expectedSize || read(session, "active") != null
                            || read(session, "timerSemantics") != null || !terminalCursorMatches)
                            throw new AssertionError("Original non-timer melee plan/cursor/active-step terminal mismatch");
                        for (int stepIndex = 0; stepIndex < steps.length; stepIndex++) {
                            Object step = steps[stepIndex];
                            Object expectedOperation = operation.invoke(expectedPlan, stepIndex);
                            boolean matches = read(step, "expected") == expectedOperation && read(step, "session") == session;
                            Map<String, Object> observed = new LinkedHashMap<>();
                            observed.put("index", stepIndex); observed.put("exactOriginalPlanOperationAndSessionIdentity", matches);
                            observed.put("kind", read(expectedOperation, "kind").toString());
                            for (String key : List.of("started", "entered", "originalReturned", "closed", "phase")) observed.put(key, read(step, key));
                            observed.put("originalFailureAbsent", read(step, "originalFailure") == null);
                            observed.put("observationFailureAbsent", read(step, "observationFailure") == null);
                            originalSteps.add(observed);
                            if (!matches || !Boolean.TRUE.equals(observed.get("entered")) || !Boolean.TRUE.equals(observed.get("originalReturned"))
                                || !Boolean.TRUE.equals(observed.get("closed")) || !Boolean.TRUE.equals(observed.get("originalFailureAbsent"))
                                || !Boolean.TRUE.equals(observed.get("observationFailureAbsent")))
                                throw new AssertionError("An exact original plan step did not fully return/close without failures");
                        }
""" + anchor)
anchor = '((Number)publicationRow.get("session_next")).intValue() != 4'
assert source.count(anchor) == 1
source = source.replace(anchor, '((Number)publicationRow.get("session_next")).intValue() != expectedSize')
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
new_source = work / 'tests/exact-melee-wooden-configured-signed-plan-qualified-41d445cf/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
launcher = launcher.replace('native-repair-runtime-exact-melee-wooden-configured-signed-41d445cf.json', 'native-repair-runtime-exact-melee-wooden-configured-signed-plan-qualified-41d445cf.json')
preparation = work / 'reports/native-exact-melee-wooden-configured-signed-plan-qualified-preparation-41d445cf.json'
launcher = launcher.replace(parent.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
new_launcher = work / 'tools/run_native_exact_melee_wooden_configured_signed_plan_qualified_41d445cf.py'
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent, failed_path, terminal, sequence_source, old_source, old_launcher, helper, new_source, new_launcher]
namespace['add'](preparation.relative_to(root).as_posix(), json.dumps({
    'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'actualCases': 1,
    'fixtureCorrectionOnly': 'Require every exact original declared plan step, exact terminal cursor, original operation/session identities and no failures; replace unrelated representative-only hardcoded four-step count',
    'productChanged': False, 'authorityFieldsWritten': False, 'authenticatedMultiplayerAccepted': False,
    'excludedOldFixedCountObserver': str(failed_path), 'inputHashes': {str(path): digest(path) for path in paths}
}, ensure_ascii=False, indent=2) + '\n')
print(json.dumps({'prepared': preparation.name, 'actualExpectedPlanRatherThanHardcodedFour': True}))
