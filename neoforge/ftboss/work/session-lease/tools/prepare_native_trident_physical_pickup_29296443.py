import hashlib
import json
from pathlib import Path
work = Path(__file__).resolve().parents[1]
root = work.parents[1]
parent = work / 'reports/native-trident-foundation-gate-preparation-29296443.json'
evidence = work / 'reports/native-trident-paid-signed-combat-focused-evidence-29296443.json'
accepted = json.loads(evidence.read_text(encoding='utf-8'))
assert accepted['failures'] == 0 and accepted['fullSignedSessionAccepted']
frozen = json.loads(parent.read_text(encoding='utf-8'))
for receipt in [accepted, frozen]:
    for name, wanted in receipt['inputHashes'].items():
        assert hashlib.sha256(Path(name).read_bytes()).hexdigest() == wanted, name
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
new_source = work / 'tests/trident-physical-pickup-29296443/PositiveWeapon436.java'
new_launcher = work / 'tools/run_native_trident_physical_pickup_29296443.py'
preparation = work / 'reports/native-trident-physical-pickup-preparation-29296443.json'
source = old_source.read_text(encoding='utf-8')
anchor = '                row.put("originalThrowPickupAndPersistenceAccepted", false);'
assert source.count(anchor) == 1
source = source.replace(anchor, '                var physical = (net.minecraft.world.entity.projectile.ThrownTrident)arrows.get(0);\n                CompoundTag originalBeforePickup = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);\n                Map<String, Object> pickupRow = new LinkedHashMap<>(); row.put("actualPhysicalPickup", pickupRow);\n                net.minecraft.world.item.ItemStack picked = net.minecraft.world.item.ItemStack.EMPTY;\n                int physicalTicks = 0;\n                for (; physicalTicks < 160 && !physical.isRemoved(); physicalTicks++) {\n                    physical.tick();\n                    player.moveTo(physical.getX(), physical.getY(), physical.getZ(), 0.0F, 0.0F);\n                    physical.playerTouch(player);\n                }\n                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {\n                    var candidate = player.getInventory().getItem(slot);\n                    if (!candidate.isEmpty() && candidate.getItem() == actual.getItem()) {\n                        if (!picked.isEmpty()) throw new AssertionError("Original pickup duplicated the thrown weapon");\n                        picked = candidate;\n                    }\n                }\n                pickupRow.put("originalPlayerTouchPhysicsTicks", physicalTicks);\n                pickupRow.put("actualProjectileRemovedByPickup", physical.isRemoved());\n                pickupRow.put("pickedPresent", !picked.isEmpty());\n                pickupRow.put("pickedCount", picked.getCount());\n                pickupRow.put("pickedWear", picked.getDamageValue());\n                pickupRow.put("sameOriginalObject", picked == actual);\n                pickupRow.put("sameOriginalWholeImage", originalBeforePickup.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(picked)));\n                pickupRow.put("pickedNonNullLease", leaseMethod.invoke(null, picked) != null);\n                pickupRow.put("pickedNonNullGeneration", generationMethod.invoke(null, picked) != null);\n                pickupRow.put("sameGenuineLease", leaseMethod.invoke(null, picked) == actualLease);\n                pickupRow.put("sameGenuineGeneration", generationMethod.invoke(null, picked) == generation);\n                pickupRow.put("originalGenerationRetired", read(generation, "retired"));\n                if (!physical.isRemoved() || picked.isEmpty() || picked.getCount() != 1 || picked.getDamageValue() != wearBefore + 1)\n                    throw new AssertionError("Original physical Trident pickup did not return exactly one worn weapon");\n                if (picked != actual || !Boolean.TRUE.equals(pickupRow.get("sameOriginalWholeImage"))\n                    || !Boolean.TRUE.equals(pickupRow.get("sameGenuineLease")) || !Boolean.TRUE.equals(pickupRow.get("sameGenuineGeneration"))\n                    || Boolean.TRUE.equals(read(generation, "retired")))\n                    throw new AssertionError("Original physical Trident pickup lost genuine paid original custody or semantic state");\n                storage.save(player);\n                pickupRow.put("originalPostPickupSaveReturned", true);\n' + anchor)
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8')
launcher = launcher.replace(old_source.relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
launcher = launcher.replace('reports/native-repair-runtime-trident-foundation-gate-29296443.json', 'reports/native-repair-runtime-trident-physical-pickup-29296443.json')
anchor = "save(attempt / 'inputs-before308.json', inputs)"
head, tail = launcher.rsplit(anchor, 1)
launcher = head + "inputs[str(WORK / 'reports/native-trident-physical-pickup-preparation-29296443.json')] = digest(WORK / 'reports/native-trident-physical-pickup-preparation-29296443.json')\n" + anchor + tail
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent, evidence, helper, old_source, old_launcher, new_source, new_launcher]
with preparation.open('x', encoding='utf-8') as stream:
    json.dump({'candidate': frozen['candidate'], 'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'authorityFieldsWritten': False, 'physicalEntry': 'Original ThrownTrident.tick and public playerTouch without private-field writes', 'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}}, stream, ensure_ascii=False, indent=2)
print(json.dumps({'prepared': preparation.name}))
