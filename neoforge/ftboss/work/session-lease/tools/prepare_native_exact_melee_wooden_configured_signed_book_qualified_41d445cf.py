import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):
    return json.loads(path.read_text(encoding='utf-8'))
parent = work / 'reports/native-exact-melee-wooden-configured-signed-plan-qualified-preparation-41d445cf.json'
frozen = load(parent)
for name, wanted in frozen['inputHashes'].items():
    assert digest(Path(name)) == wanted, Path(name).name
failed_path = work / 'reports/native-repair-runtime-exact-melee-wooden-configured-signed-plan-qualified-41d445cf.json'
failed = load(failed_path)
terminal = Path(failed['attempt']) / 'terminal308.json'
end = load(terminal)
assert failed['exitCode'] == 0 and failed['probe']['failures'] == 1 and end['completeSave'] and end['stopSent']
assert 'non-timer melee plan' in failed['probe']['failure']
arguments = load(work / 'build/native-repair-41d445cf/complete-arguments316.json')
book_source = next(Path(value) for value in arguments if isinstance(value, str) and Path(value).name == 'OwnerTimerSemanticAuthority.java')
assert 'this.nativeReturned.size() == this.prepared.nativePuts.size()' in book_source.read_text(encoding='utf-8')
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
source = old_source.read_text(encoding='utf-8')
start = source.index('                        Object[] steps = (Object[])read(session, "steps");')
end = source.index('                        Object failure = read(session, "firstObservationFailure");', start)
source = source[:start] + '                        int expectedSize = verifyActualOriginalSessionReturns(session, preparation, publicationRow);\n' + source[end:]
anchor = '    private static Object read('
assert source.count(anchor) == 1
method = """    private static boolean exactIdentityCoverage(java.util.Collection<?> expected, java.util.Collection<?> returned) {
        return expected.size() == returned.size() && expected.stream().allMatch(value -> returned.stream().anyMatch(observed -> observed == value));
    }

    private static int verifyActualOriginalSessionReturns(Object session, Object preparation, Map<String, Object> row) throws Exception {
        Object[] staticSteps = (Object[])read(session, "steps");
        Object plan = read(session, "expected");
        var size = plan.getClass().getDeclaredMethod("size"); size.setAccessible(true);
        int staticSize = ((Number)size.invoke(plan)).intValue();
        row.put("originalStaticExpectedPlanSize", staticSize);
        row.put("actualStaticSessionStepCount", staticSteps.length);
        if (staticSteps.length != staticSize || read(session, "active") != null)
            throw new AssertionError("Original declared static plan or active terminal step mismatch");
        Object book = read(session, "timerSemantics");
        row.put("originalDynamicSemanticBookPresent", book != null);
        if (book == null) {
            boolean cursorMatches = read(session, "expectedCursor") == read(plan, "expectedAfter");
            row.put("exactExpectedTerminalCursorIdentity", cursorMatches);
            if (!cursorMatches || staticSize <= 0) throw new AssertionError("Original static plan lacks exact completed terminal cursor");
            var operation = plan.getClass().getDeclaredMethod("operation", int.class); operation.setAccessible(true);
            for (int index = 0; index < staticSteps.length; index++) {
                Object step = staticSteps[index];
                if (read(step, "session") != session || read(step, "expected") != operation.invoke(plan, index)
                    || !Boolean.TRUE.equals(read(step, "entered")) || !Boolean.TRUE.equals(read(step, "originalReturned"))
                    || !Boolean.TRUE.equals(read(step, "closed")) || read(step, "originalFailure") != null || read(step, "observationFailure") != null)
                    throw new AssertionError("An exact original static plan step failed to return/close");
            }
            row.put("exactOriginalStaticStepIdentityAndReturnCoverage", true);
            row.put("originalExpectedReturnedStepCount", staticSize);
            return staticSize;
        }
        if (!Boolean.TRUE.equals(read(book, "frozen")) || read(book, "session") != session || read(book, "prepared") != preparation
            || read(preparation, "timerSemantics") != book || read(session, "lineage") != read(preparation, "lineage"))
            throw new AssertionError("Original dynamic semantic book is not frozen/bound to the actual prepared Session");
        Map<?, ?> nativePuts = (Map<?, ?>)read(preparation, "nativePuts");
        Map<?, ?> materialized = (Map<?, ?>)read(book, "materialized");
        java.util.Set<?> nativeReturned = (java.util.Set<?>)read(book, "nativeReturned");
        Map<?, ?> targetWrites = (Map<?, ?>)read(book, "targetWrites");
        java.util.Set<?> targetReturned = (java.util.Set<?>)read(book, "targetReturned");
        if (!exactIdentityCoverage(nativePuts.keySet(), nativeReturned)
            || !exactIdentityCoverage(nativePuts.values(), materialized.values())
            || !exactIdentityCoverage(targetWrites.keySet(), targetReturned))
            throw new AssertionError("Exact original quoted native/target leaves or bindings did not all return");
        List<Object> nativeOperations = new ArrayList<>(); row.put("actualOriginalMaterializedNativeOperations", nativeOperations);
        for (var entry : materialized.entrySet()) {
            String kind = read(entry.getKey(), "kind").toString();
            if (!kind.equals("NATIVE_PUT")) throw new AssertionError("Unexpected materialized original native operation kind");
            nativeOperations.add(Map.of("kind", kind, "key", String.valueOf(read(entry.getKey(), "key")), "exactQuotedBindingAndReturnedLeafIdentity", true));
        }
        int expectedReturns = materialized.size();
        Map<?, ?> groups = (Map<?, ?>)read(book, "groups");
        List<Object> observedGroups = new ArrayList<>(); row.put("actualOriginalCompletedSemanticGroups", observedGroups);
        for (Object group : groups.values()) {
            Object projection = read(group, "projection");
            java.util.Set<?> timers = (java.util.Set<?>)read(group, "timers");
            java.util.Set<?> ranged = (java.util.Set<?>)read(group, "ranged");
            boolean acquired = Boolean.TRUE.equals(read(group, "acquired"));
            boolean rooted = Boolean.TRUE.equals(read(group, "rooted"));
            if (!Boolean.TRUE.equals(read(group, "done")) || projection == null)
                throw new AssertionError("Original selected dynamic semantic group was not complete");
            Map<?, ?> writes = (Map<?, ?>)read(projection, "writes");
            Map<?, ?> rangedLongs = (Map<?, ?>)read(projection, "rangedLongs");
            if (!timers.equals(writes.keySet()) || acquired != rooted
                || (acquired ? !ranged.equals(rangedLongs.keySet()) : !ranged.isEmpty()))
                throw new AssertionError("Original semantic group lacks exact quoted timer/ranged key return coverage");
            expectedReturns += timers.size() + ranged.size() + (acquired ? 1 : 0) + (rooted ? 1 : 0);
            observedGroups.add(Map.of("done", true, "profile", String.valueOf(read(group, "profile")),
                "quotedTimerWrites", writes.size(), "returnedTimers", timers.size(), "returnedRanged", ranged.size(),
                "acquired", acquired, "rooted", rooted, "exactQuotedKeyCoverage", true));
        }
        row.put("originalQuotedNativePutCount", nativePuts.size());
        row.put("originalReturnedNativeLeafCount", nativeReturned.size());
        row.put("originalQuotedTargetWriteCount", targetWrites.size());
        row.put("originalReturnedTargetLeafCount", targetReturned.size());
        row.put("exactOriginalNativeBindingAndTargetReturnIdentityCoverage", true);
        row.put("originalExpectedReturnedStepCount", expectedReturns);
        if (expectedReturns <= 0) throw new AssertionError("No actual original dynamic semantic step returned");
        return expectedReturns;
    }

"""
source = source.replace(anchor, method + anchor)
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
new_source = work / 'tests/exact-melee-wooden-configured-signed-book-qualified-41d445cf/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
launcher = launcher.replace('native-repair-runtime-exact-melee-wooden-configured-signed-plan-qualified-41d445cf.json', 'native-repair-runtime-exact-melee-wooden-configured-signed-book-qualified-41d445cf.json')
preparation = work / 'reports/native-exact-melee-wooden-configured-signed-book-qualified-preparation-41d445cf.json'
launcher = launcher.replace(parent.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
new_launcher = work / 'tools/run_native_exact_melee_wooden_configured_signed_book_qualified_41d445cf.py'
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent, failed_path, terminal, book_source, old_source, old_launcher, helper, new_source, new_launcher]
namespace['add'](preparation.relative_to(root).as_posix(), json.dumps({
    'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'actualCases': 1,
    'fixtureCorrectionOnly': 'Honor original Session static-or-dynamic completion branch; independently require exact full quoted native/target identity returns, all selected groups done and exact original timer/ranged key coverage; count every actually declared dynamic return rather than fixed four or static array length',
    'productChanged': False, 'authorityFieldsWritten': False, 'authenticatedMultiplayerAccepted': False,
    'excludedStaticOnlyObserver': str(failed_path), 'inputHashes': {str(path): digest(path) for path in paths}
}, ensure_ascii=False, indent=2) + '\n')
print(json.dumps({'prepared': preparation.name, 'originalStaticOrDynamicCompletionBranchRetained': True}))
