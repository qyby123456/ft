import hashlib
import json
from pathlib import Path


work = Path(__file__).resolve().parents[1]
root = work.parents[1]
preparation = work / 'reports/native-trident-combat-preparation-9d1607e8.json'
original = json.loads(preparation.read_text(encoding='utf-8'))
old_source = Path(original['newObserver'])
old_launcher = Path(original['newLauncher'])
for path in [old_source, old_launcher]:
    assert hashlib.sha256(path.read_bytes()).hexdigest() == original['inputHashes'][str(path)]
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
source = old_source.read_text(encoding='utf-8')
anchor = '                var useResult = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);'
assert source.count(anchor) == 1
source = source.replace(anchor, '''                row.put("actualItemClass", actual.getItem().getClass().getName());
                row.put("selectedSlot", player.getInventory().selected);
                row.put("actualIsMainHandBeforeUse", player.getMainHandItem() == actual);
                row.put("actualCountBeforeUse", actual.getCount());
                row.put("actualDamageBeforeUse", actual.getDamageValue());
                row.put("playerAliveBeforeUse", player.isAlive());
''' + anchor)
anchor = '                row.put("generationRetiredAfterUse", read(generation, "retired"));'
assert source.count(anchor) == 1
source = source.replace(anchor, anchor + '''
                row.put("brokenAfterOriginalUse", net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(actual));
                Class<?> registry = Class.forName("net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry");
                row.put("coverageRegistryInvalidAfterOriginalUse", readStatic(registry, "invalid"));
                var certificate = registry.getDeclaredMethod("nativeItemStackCertified165"); certificate.setAccessible(true);
                row.put("itemStackCertificateAfterOriginalUse", certificate.invoke(null));
                var sourceNodes = registry.getDeclaredMethod("nativeItemStackSourceNodes165"); sourceNodes.setAccessible(true);
                Object nodes = sourceNodes.invoke(null);
                row.put("actualItemStackProofSourcesPresent", nodes != null);
                try {
                    Class<?> proof = Class.forName("net.farmingtales.ftboss.item.NativeItemStackInstallationProof165");
                    var verify = java.util.Arrays.stream(proof.getDeclaredMethods()).filter(method -> method.getName().equals("verify") && method.getParameterCount() == 3).findFirst().orElseThrow();
                    verify.setAccessible(true);
                    verify.invoke(null, readStatic(registry, "nativeItemStackTarget165"), nodes, readStatic(registry, "nativeItemStackPristine165"));
                    row.put("actualItemStackWholeParentProofAfterOriginalUse", true);
                } catch (java.lang.reflect.InvocationTargetException failure) {
                    row.put("actualItemStackWholeParentProofFailure", failure.getCause().toString());
                }
''')
source = source.replace('report.put("fullLifecycleAccepted", false);', 'report.put("readOnlyDiagnosticNotPositiveAcceptance", true); report.put("fullLifecycleAccepted", false);')
new_source = work / 'tests/trident-use-diagnostic-9d1607e8/PositiveWeapon436.java'
new_launcher = work / 'tools/run_native_trident_use_diagnostic_9d1607e8.py'
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8').replace('tests/paid-trident-combat-9d1607e8', 'tests/trident-use-diagnostic-9d1607e8').replace('reports/native-repair-runtime-paid-trident-combat-9d1607e8', 'reports/native-repair-runtime-trident-use-diagnostic-9d1607e8')
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
output = work / 'reports/native-trident-use-diagnostic-preparation-9d1607e8.json'
assert not output.exists()
paths = [Path(__file__), preparation, helper, old_source, old_launcher, new_source, new_launcher]
output.write_text(json.dumps({'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'authorityFieldsWritten': False, 'readOnlyDiagnosticNotPositiveAcceptance': True, 'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'launcher': str(new_launcher), 'diagnosticNotAcceptance': True}))
