import hashlib
import json
import re
import sys
from pathlib import Path


work = Path(__file__).resolve().parents[1]
root = work.parents[1]
tag = sys.argv[1]
assert re.fullmatch('[0-9a-f]{8}', tag)
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


preparation = work / ('reports/native-ranged-use-preparation-' + tag + '.json')
original = json.loads(preparation.read_text(encoding='utf-8'))
old_source = Path(original['newObserver'])
old_launcher = Path(original['newLauncher'])
for path in [old_source, old_launcher]:
    assert digest(path) == original['inputHashes'][str(path)]
source = old_source.read_text(encoding='utf-8')
anchor = '                player.stopUsingItem();'
assert source.count(anchor) == 1
source = source.replace(anchor, '''                Object inputBeforeForeign = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                CompoundTag imageBeforeForeign = (CompoundTag)actual.save(player.registryAccess());
                net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeRangedOriginalUseEntered(actual, server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority.nativeCrossbowComponentReturned(actual, net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES,
                    net.minecraft.world.item.component.ChargedProjectiles.EMPTY, charged);
                net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeRangedOriginalUseReturned(actual);
                boolean foreignCallbacksUnchanged = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput() == inputBeforeForeign
                    && imageBeforeForeign.equals(actual.save(player.registryAccess())) && !Boolean.TRUE.equals(read(generation, "retired"));
                row.put("outsideOriginalCallbacksPreservedCurrentInputImageAndAuthority", foreignCallbacksUnchanged);
                if (!foreignCallbacksUnchanged) throw new AssertionError("Foreign callback modified original charge authority");
''' + anchor)
old_fire = '                var fireResult = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);'
assert source.count(old_fire) == 1
source = source.replace(old_fire, '                var fireResult = net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeUse(actual.getItem(), actual, server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);')
start = source.index('                List<Map<String, Object>> publications = new ArrayList<>();')
finish = source.index('                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(player);', start)
source = source[:start] + '''                row.put("foreignNativeUseOriginalReturnedOnce", fireResult.getResult().consumesAction() && arrows.size() == 1 && actual.getDamageValue() == wearBefore + 1);
                row.put("foreignNativeUseAuthorityRetired", read(generation, "retired"));
                row.put("foreignNativeUseNoSignedCarrier", flights.size() == 1 && flights.stream().noneMatch(value -> Boolean.TRUE.equals(value.get("snapshotPresent"))
                    || Boolean.TRUE.equals(value.get("snapshotValid")) || Boolean.TRUE.equals(value.get("paidCarrierPresent"))));
                boolean scopesClosed = true;
                for (String field : List.of("NATIVE_OPERATION", "PRIVATE_RANGED_USE", "PRIVATE_NATIVE_HURT", "PRIVATE_INGRESS"))
                    scopesClosed &= ((ThreadLocal<?>)readStatic(net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class, field)).get() == null;
                for (String field : List.of("NATIVE_REPAIR_OPERATION", "NATIVE_WEAR"))
                    scopesClosed &= ((ThreadLocal<?>)readStatic(Class.forName("net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority"), field)).get() == null;
                row.put("privateScopesClosedWithoutWrites", scopesClosed);
                if (!Boolean.TRUE.equals(row.get("foreignNativeUseOriginalReturnedOnce")) || !Boolean.TRUE.equals(read(generation, "retired"))
                    || !Boolean.TRUE.equals(row.get("foreignNativeUseNoSignedCarrier")) || !scopesClosed || !actions.isEmpty()
                    || damageEvents.isEmpty() || damageEvents.stream().anyMatch(value -> Boolean.TRUE.equals(value.get("resolvedSnapshotValid"))))
                    throw new AssertionError("Foreign nativeUse minted signed paid combat authority or did not preserve original operation");
''' + source[finish:]
source = source.replace('report.put("crossbowEntry", "Actual ItemStack.use -> original Crossbow charge/releaseUsing -> actual charged ItemStack.use -> native arrow.tick impact");', 'report.put("crossbowEntry", "Actual original paid charge, then outside callback no-ops and foreign public nativeUse delegation with original arrow.tick but no signed paid authority");')
new_source = work / ('tests/crossbow-foreign-control-' + tag + '/PositiveWeapon436.java')
new_launcher = work / ('tools/run_native_crossbow_foreign_control_' + tag + '.py')
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8')
launcher = launcher.replace('tests/paid-crossbow-combat-' + tag, 'tests/crossbow-foreign-control-' + tag)
launcher = launcher.replace('reports/native-repair-runtime-paid-crossbow-combat-' + tag, 'reports/native-repair-runtime-crossbow-foreign-control-' + tag)
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
output = work / ('reports/native-crossbow-foreign-control-preparation-' + tag + '.json')
assert not output.exists()
output.write_text(json.dumps({'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'authorityFieldsWritten': False, 'inputHashes': {str(path): digest(path) for path in [Path(__file__), helper, preparation, old_source, old_launcher, new_source, new_launcher]}}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'tag': tag, 'launcher': str(new_launcher)}))
