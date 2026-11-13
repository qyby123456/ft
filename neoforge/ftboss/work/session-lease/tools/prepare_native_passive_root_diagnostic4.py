import hashlib
import json
import sys
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
tag = sys.argv[1]
parent_path = work / ("reports/native-passive-root-signed-preparation-" + tag + ".json")
parent = json.loads(parent_path.read_text(encoding="utf-8"))
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
for name, expected in parent["inputHashes"].items():
    assert digest(Path(name)) == expected, Path(name).name
source = Path(parent["newObserver"]).read_text(encoding="utf-8")
anchor = "                        int expectedSize = verifyActualOriginalSessionReturns(session, preparation, publicationRow);"
assert source.count(anchor) == 1
extra = '''                        Class<?> registry = Class.forName("net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry");
                        String[][] required = (String[][])readStatic(registry, "REQUIRED");
                        boolean[] installed = (boolean[])readStatic(registry, "INSTALLED");
                        List<String> missing = new ArrayList<>();
                        for (int index = 0; index < required.length; index++) if (!installed[index]) missing.add(required[index][0]);
                        publicationRow.put("diagnostic_registryMissingInstalledRows", missing);
                        Object[] observations = (Object[])readStatic(registry, "OBSERVED");
                        org.objectweb.asm.tree.ClassNode[] targets = (org.objectweb.asm.tree.ClassNode[])readStatic(registry, "TARGETS");
                        Class<?> installation = Class.forName("net.farmingtales.ftboss.item.OwnerFieldInstallationProof");
                        var completeInstallation = installation.getDeclaredMethod("complete", String[].class, org.objectweb.asm.tree.ClassNode.class, org.objectweb.asm.tree.ClassNode.class);
                        completeInstallation.setAccessible(true);
                        List<String> incomplete = new ArrayList<>();
                        for (int index = 0; index < required.length; index++) {
                            if (!installed[index] || observations[index] == null || targets[index] == null) continue;
                            try {
                                var info = (org.spongepowered.asm.mixin.extensibility.IMixinInfo)observations[index];
                                if (!Boolean.TRUE.equals(completeInstallation.invoke(null, required[index], targets[index], info.getClassNode(0))))
                                    incomplete.add(required[index][0]);
                            } catch (Throwable failure) { incomplete.add(required[index][0] + ":" + failure); }
                        }
                        publicationRow.put("diagnostic_installedButIncompleteProofRows", incomplete);

                        publicationRow.put("diagnostic_registryInvalid", readStatic(registry, "invalid"));
                        publicationRow.put("diagnostic_catalogueGenerationPresent", read(preparation, "catalogueGeneration") != null);
                        for (String diagnosticField : List.of("firstObservationFailure", "firstRevocationSite434")) {
                            Object observed = read(session, diagnosticField);
                            publicationRow.put("diagnostic_" + diagnosticField, observed == null ? null : observed.toString());
                            if (observed instanceof Throwable failure) publicationRow.put("diagnostic_" + diagnosticField + "Stack",
                                java.util.Arrays.stream(failure.getStackTrace()).limit(20).map(Object::toString).toList());
                        }
                        Object book = read(session, "timerSemantics");
                        if (book != null) {
                            Map<?, ?> puts = (Map<?, ?>)read(preparation, "nativePuts");
                            publicationRow.put("diagnostic_nativePutTypes", puts.keySet().stream().map(value -> value.getClass().getName()).toList());
                            publicationRow.put("diagnostic_materializedCount", ((Map<?, ?>)read(book, "materialized")).size());
                            publicationRow.put("diagnostic_nativeReturnedCount", ((java.util.Set<?>)read(book, "nativeReturned")).size());
                            List<Map<String, Object>> groups = new ArrayList<>(); publicationRow.put("diagnostic_groups", groups);
                            for (Object group : ((Map<?, ?>)read(book, "groups")).values()) {
                                Map<String, Object> detail = new LinkedHashMap<>(); groups.add(detail);
                                for (String flag : List.of("done", "acquired", "rooted", "profile", "timers", "ranged")) detail.put(flag, read(group, flag));
                            }
                        }
                        Object failedStep = read(session, "firstFailedStep");
                        if (failedStep != null) {
                            Map<String, Object> detail = new LinkedHashMap<>(); publicationRow.put("diagnostic_failedStep", detail);
                            for (String flag : List.of("phase", "entered", "writeObserved", "copyVerified", "originalReturned", "closed")) detail.put(flag, read(failedStep, flag));
                            Object operation = read(failedStep, "expected"); detail.put("kind", read(operation, "kind").toString());
                            detail.put("key", read(operation, "key"));
                        }
'''
source = source.replace(anchor, extra + anchor)
observer = work / ("tests/native-passive-root-diagnostic4-" + tag + "/PositiveWeapon436.java")
launcher = work / ("tools/run_native_passive_root_diagnostic4_" + tag + ".py")
preparation = work / ("reports/native-passive-root-diagnostic4-preparation-" + tag + ".json")
text = Path(parent["newLauncher"]).read_text(encoding="utf-8")
text = text.replace(Path(parent["newObserver"]).relative_to(work).as_posix(), observer.relative_to(work).as_posix())
text = text.replace(parent_path.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
text = text.replace("reports/native-repair-runtime-passive-root-signed-" + tag + ".json", "reports/native-repair-runtime-passive-root-diagnostic4-" + tag + ".json")
helper = work / "tools/prepare_native_frost_ingress_runtime.py"
namespace = {"__file__": str(helper)}
exec(compile(helper.read_text(encoding="utf-8").split("candidate = work /")[0], str(helper), "exec"), namespace)
namespace["add"](observer.relative_to(root).as_posix(), source)
namespace["add"](launcher.relative_to(root).as_posix(), text)
paths = [Path(__file__), parent_path, Path(parent["newObserver"]), Path(parent["newLauncher"]), observer, launcher, helper]
with preparation.open("x", encoding="utf-8") as stream:
    json.dump({"newObserver": str(observer), "newLauncher": str(launcher), "readOnlyDiagnostic": True,
               "authorityFieldsWritten": False, "inputHashes": {str(path): digest(path) for path in paths}}, stream, ensure_ascii=False, indent=2)
print(json.dumps({"prepared": preparation.name, "readOnlyDiagnostic": True}))
