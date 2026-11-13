import hashlib
import json
import sys
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
tag = sys.argv[1]
parent_path = work / ("reports/native-passive-root-diagnostic7-preparation-" + tag + ".json")
parent = json.loads(parent_path.read_text(encoding="utf-8"))
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
for name, expected in parent["inputHashes"].items():
    assert digest(Path(name)) == expected, Path(name).name
source = Path(parent["newObserver"]).read_text(encoding="utf-8")
anchor = "                        int expectedSize = verifyActualOriginalSessionReturns(session, preparation, publicationRow);"
assert source.count(anchor) == 1
extra = '''                        publicationRow.put("diagnostic_lateStages", ((int[])readStatic(registry, "lateStages")).clone());
                        for (int index = 0; index < required.length; index++) {
                            if (!required[index][0].equals("net.farmingtales.ftboss.mixin.NativeExpectedMapWriteMixin")) continue;
                            Map<String, Object> mapProof = new LinkedHashMap<>(); publicationRow.put("diagnostic_actualLateMapProof", mapProof);
                            mapProof.put("installed", installed[index]);
                            mapProof.put("targetPresent", targets[index] != null); mapProof.put("observationPresent", observations[index] != null);
                            if (targets[index] == null || observations[index] == null) continue;
                            var targetNode = (org.objectweb.asm.tree.ClassNode)targets[index];
                            var sourceNode = ((org.spongepowered.asm.mixin.extensibility.IMixinInfo)observations[index]).getClassNode(0);
                            try { mapProof.put("completeNow", completeInstallation.invoke(null, required[index], targetNode, sourceNode)); }
                            catch (Throwable failure) {
                                Throwable cause = failure instanceof java.lang.reflect.InvocationTargetException wrapped ? wrapped.getCause() : failure;
                                mapProof.put("failure", cause.toString()); mapProof.put("stack", java.util.Arrays.stream(cause.getStackTrace()).limit(8).map(Object::toString).toList());
                            }
                            org.objectweb.asm.ClassWriter targetWriter = new org.objectweb.asm.ClassWriter(0); targetNode.accept(targetWriter);
                            Files.write(Path.of("native-passive-runtime-coverage-CompoundTag.class"), targetWriter.toByteArray());
                            org.objectweb.asm.ClassWriter sourceWriter = new org.objectweb.asm.ClassWriter(0); sourceNode.accept(sourceWriter);
                            Files.write(Path.of("native-passive-runtime-map-source.class"), sourceWriter.toByteArray());
                            List<Map<String,Object>> methods = new ArrayList<>(); mapProof.put("actualWrapperMetadata", methods);
                            for (var method : targetNode.methods) {
                                if (!(method.name.equals("putLong") || method.name.startsWith("putLong$mixinextras$wrapped$")
                                    || method.name.endsWith("$ftboss$nativeFxCooldown") || method.name.startsWith("mixinextras$bridge$putLong"))) continue;
                                Map<String,Object> detail = new LinkedHashMap<>(); methods.add(detail);
                                detail.put("name", method.name); detail.put("descriptor", method.desc); detail.put("access", method.access);
                                detail.put("maxLocals", method.maxLocals); detail.put("maxStack", method.maxStack);
                                detail.put("catchCount", method.tryCatchBlocks == null ? null : method.tryCatchBlocks.size());
                                detail.put("exceptions", method.exceptions);
                            }
                        }
'''
source = source.replace(anchor, extra + anchor)
source = source.replace('report.put("nineteenConfiguredOriginalMeleeGenerationTwoFullSignedSessions", true);',
    'report.put("singleDefectClawOriginalMeleeGenerationTwoFullSignedSession", true);')
assert "diagnostic_actualLateMapProof" in source and "singleDefectClawOriginal" in source
observer = work / ("tests/native-passive-root-diagnostic9-" + tag + "/PositiveWeapon436.java")
launcher = work / ("tools/run_native_passive_root_diagnostic9_" + tag + ".py")
preparation = work / ("reports/native-passive-root-diagnostic9-preparation-" + tag + ".json")
text = Path(parent["newLauncher"]).read_text(encoding="utf-8")
text = text.replace(Path(parent["newObserver"]).relative_to(work).as_posix(), observer.relative_to(work).as_posix())
text = text.replace(parent_path.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
text = text.replace("reports/native-repair-runtime-passive-root-diagnostic7-" + tag + ".json", "reports/native-repair-runtime-passive-root-diagnostic9-" + tag + ".json")
helper = work / "tools/prepare_native_frost_ingress_runtime.py"
namespace = {"__file__": str(helper)}
exec(compile(helper.read_text(encoding="utf-8").split("candidate = work /")[0], str(helper), "exec"), namespace)
namespace["add"](observer.relative_to(root).as_posix(), source)
namespace["add"](launcher.relative_to(root).as_posix(), text)
paths = [Path(__file__), parent_path, Path(parent["newObserver"]), Path(parent["newLauncher"]), observer, launcher, helper]
with preparation.open("x", encoding="utf-8") as stream:
    json.dump({"newObserver": str(observer), "newLauncher": str(launcher), "readOnlyDiagnostic": True,
               "authorityFieldsWritten": False, "inputHashes": {str(path): digest(path) for path in paths}}, stream, ensure_ascii=False, indent=2)
print(json.dumps({"prepared": preparation.name, "actualLateMapInputExport": True}))
