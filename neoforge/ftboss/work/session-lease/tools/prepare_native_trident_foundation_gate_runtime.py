import hashlib
import json
from pathlib import Path


work = Path(__file__).resolve().parents[1]
root = work.parents[1]
candidate = work / 'reports/native-repair-ranged-use-candidate.json'
receipt = json.loads(candidate.read_text(encoding='utf-8'))
tag = Path(receipt['attempt']).name.removeprefix('native-repair-')
binding = work / ('reports/native-repair-ranged-use-candidate-' + tag + '.json')
assert not binding.exists()
binding.write_bytes(candidate.read_bytes())
preparation = work / 'reports/native-trident-structure-diagnostic-preparation-9d1607e8.json'
frozen = json.loads(preparation.read_text(encoding='utf-8'))
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
for path in [old_source, old_launcher]:
    assert hashlib.sha256(path.read_bytes()).hexdigest() == frozen['inputHashes'][str(path)]
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
source = old_source.read_text(encoding='utf-8').replace('for (int sample : new int[]{4})', 'for (int sample : new int[]{0, 1, 2, 3, 5, 4})')
anchor = '                row.put("genuineNonNullLeaseAndGenerationBeforeAttack", true);'
assert source.count(anchor) == 1
source = source.replace(anchor, anchor + '''
                row.put("nativeFoundationApplies", net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(actual));
                row.put("nativeFoundationBlocksUse", net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.blocksUse(actual));
                row.put("nativeFoundationStatus", String.valueOf(net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.status(actual)));
                row.put("oldSwordRequiredMissing", net.farmingtales.ftboss.item.SwordCavityBuild.missingRequired(actual));
                row.put("lifecycleBrokenBeforeUse", net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(actual));
                if (net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(actual))
                    throw new AssertionError("Actual paid native foundation was falsely broken for " + id);
                if (sample != 4) continue;
                CompoundTag missingImage = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                missingImage.getCompound("components").getCompound("minecraft:custom_data").getCompound("FTBossFourWeaponFoundation").put("Cells", new net.minecraft.nbt.ListTag());
                var missingWeapon = net.minecraft.world.item.ItemStack.parseOptional(server.registryAccess(), missingImage);
                boolean missingDenied = !missingWeapon.isEmpty() && net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.blocksUse(missingWeapon)
                    && net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(missingWeapon);
                row.put("missingNativeCellsDenied", missingDenied);
                CompoundTag invalidImage = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                invalidImage.getCompound("components").getCompound("minecraft:custom_data").getCompound("FTBossFourWeaponFoundation").putInt("Schema", 0);
                var invalidWeapon = net.minecraft.world.item.ItemStack.parseOptional(server.registryAccess(), invalidImage);
                boolean invalidDenied = !invalidWeapon.isEmpty() && net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.blocksUse(invalidWeapon)
                    && net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(invalidWeapon);
                row.put("malformedNativeFoundationDenied", invalidDenied);
                row.put("sameGenuineAuthorityAfterGateControls", leaseMethod.invoke(null, actual) == actualLease
                    && generationMethod.invoke(null, actual) == generation && !Boolean.TRUE.equals(read(generation, "retired")));
                if (!missingDenied || !invalidDenied || !Boolean.TRUE.equals(row.get("sameGenuineAuthorityAfterGateControls")))
                    throw new AssertionError("Native foundation gate or negative control changed authority");
''')
source = source.replace('report.put("readOnlyDiagnosticNotPositiveAcceptance", true);', 'report.put("foundationGateScopeOnly", true);')
new_source = work / ('tests/trident-foundation-gate-' + tag + '/PositiveWeapon436.java')
new_launcher = work / ('tools/run_native_trident_foundation_gate_' + tag + '.py')
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8')
launcher = launcher.replace('native-repair-ranged-use-candidate-9d1607e8.json', binding.name)
launcher = launcher.replace('7a0661df7ceb74920c4ccf4db3425fb7e444e1e75eddfb6e047376b908fb5e97', hashlib.sha256(binding.read_bytes()).hexdigest())
launcher = launcher.replace('trident-structure-diagnostic-9d1607e8', 'trident-foundation-gate-' + tag)
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
output = work / ('reports/native-trident-foundation-gate-preparation-' + tag + '.json')
assert not output.exists()
paths = [Path(__file__), preparation, helper, binding, old_source, old_launcher, new_source, new_launcher]
output.write_text(json.dumps({'candidate': str(binding), 'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'authorityFieldsWritten': False, 'fullLifecycleAccepted': False, 'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'launcher': str(new_launcher), 'candidateTag': tag}))
