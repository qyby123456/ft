import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
tag = '2e5c6034'
candidate = work / ('reports/native-repair-frost-ingress-candidate-' + tag + '.json')
assert hashlib.sha256(candidate.read_bytes()).hexdigest() == '4b8d5adca7c6e8531088fa9d9e71f431c5c2e340bf8798ddceab04f0302ff891'
frozen = json.loads((work / ('reports/native-frost-paid-signed-combat-focused-evidence-' + tag + '.json')).read_text(encoding='utf-8'))
old_source = work / ('tests/paid-frost-ingress-' + tag + '/PositiveWeapon436.java')
old_launcher = work / ('tools/run_native_paid_frost_ingress_' + tag + '.py')
for path in [old_source, old_launcher]:
    assert hashlib.sha256(path.read_bytes()).hexdigest() == frozen['inputHashes'][str(path)]
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
source = old_source.read_text(encoding='utf-8')
source = source.replace('for (int sample : new int[]{5})', 'for (int sample : new int[]{2})')
source = source.replace('                var target = new net.minecraft.world.entity.monster.Zombie(server.overworld());', '                player.getInventory().setItem(1, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW, 16));\n                var target = new net.minecraft.world.entity.monster.Zombie(server.overworld());')
start = source.index('                var useResult = actual.use(')
finish = source.index('                List<Map<String, Object>> publications', start)
source = source[:start] + '''                var useResult = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                row.put("actualOriginalUseResult", useResult.getResult().toString());
                row.put("actualUseStarted", player.isUsingItem());
                row.put("generationRetiredAfterUse", read(generation, "retired"));
                if (!useResult.getResult().consumesAction() || !player.isUsingItem() || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Original paid Crossbow ItemStack.use did not start with live authority");
                int ammoBefore = player.getInventory().getItem(1).getCount();
                actual.releaseUsing(server.overworld(), player, actual.getUseDuration(player) - 40);
                var charged = actual.get(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES);
                row.put("ammoBefore", ammoBefore); row.put("ammoAfterCharge", player.getInventory().getItem(1).getCount());
                row.put("chargedProjectilesPresent", charged != null && !charged.isEmpty());
                row.put("generationRetiredAfterCharge", read(generation, "retired"));
                if (charged == null || charged.isEmpty() || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Original paid Crossbow charge lost projectiles or genuine authority");
                player.stopUsingItem();
                var fireResult = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                row.put("actualOriginalFireUseResult", fireResult.getResult().toString());
                row.put("generationRetiredAfterFire", read(generation, "retired"));
                List<Map<String, Object>> flights = new ArrayList<>(); row.put("actualProjectileFlights", flights);
                List<net.minecraft.world.entity.projectile.AbstractArrow> arrows = new ArrayList<>();
                for (var entity : server.overworld().getAllEntities()) {
                    if (entity instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow && arrow.getOwner() == player) arrows.add(arrow);
                }
                for (var arrow : arrows) {
                    Map<String, Object> flight = new LinkedHashMap<>(); flights.add(flight);
                    CompoundTag image = arrow.getPersistentData().getCompound("FTBossWeaponMaterials");
                    flight.put("snapshotPresent", !image.isEmpty());
                    flight.put("snapshotValid", net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(image, player));
                    flight.put("repairEpochPresent", image.hasUUID("OwnerRepairAuthorityEpoch"));
                    flight.put("signaturePresent", image.getString("ServerSignature").length() == 64);
                    flight.put("purpose", image.getString("DamagePurpose"));
                    flight.put("nativeEntry", image.getString("NativeEntry"));
                    flight.put("paidCarrierPresent", arrow instanceof net.farmingtales.ftboss.item.NativePaidWeaponCarrier carrier && carrier.ftboss$originalOwnerPaidWeapon() != null);
                    for (int ticks = 0; ticks < 12 && !arrow.isRemoved() && target.getHealth() == healthBefore; ticks++) arrow.tick();
                    flight.put("targetHealthAfterFlight", target.getHealth());
                    arrow.discard();
                }
                row.put("originalCrossbowChargeAndFireReturned", true);
                row.put("healthBefore", healthBefore); row.put("healthAfter", target.getHealth());
                row.put("wearBefore", wearBefore); row.put("wearAfter", actual.getDamageValue());
                row.put("damageEvents", new ArrayList<>(damageEvents));
''' + source[finish:]
start = source.index('                row.put("capturedNativeFrostPurpose"')
finish = source.index('                target.discard();', start)
source = source[:start] + '''                if (flights.size() != 1 || flights.stream().anyMatch(value -> !Boolean.TRUE.equals(value.get("snapshotValid"))
                    || !Boolean.TRUE.equals(value.get("repairEpochPresent")) || !Boolean.TRUE.equals(value.get("signaturePresent"))
                    || !Boolean.TRUE.equals(value.get("paidCarrierPresent")) || !"CROSSBOW".equals(value.get("nativeEntry"))))
                    throw new AssertionError("Actual original Crossbow did not publish one genuinely paid signed native carrier");
''' + source[finish:]
source = source.replace('Actual paid original Frost ray', 'Actual paid original Crossbow arrow').replace('Actual original Frost ray', 'Actual original Crossbow arrow')
source = source.replace('Actual post-Frost original save', 'Actual post-Crossbow original save')
start = source.index('        report.put("allSixFamilySignedCombatAccepted"')
finish = source.index('        report.put("fullLifecycleAccepted"', start)
source = source[:start] + '''        report.put("allSixFamilySignedCombatAccepted", false);
        report.put("crossbowEntry", "Actual ItemStack.use -> original Crossbow charge/releaseUsing -> actual charged ItemStack.use -> native arrow.tick impact");
''' + source[finish:]
new_source = work / ('tests/paid-crossbow-combat-' + tag + '/PositiveWeapon436.java')
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8')
launcher = launcher.replace('tests/paid-frost-ingress-', 'tests/paid-crossbow-combat-')
launcher = launcher.replace('reports/native-repair-runtime-paid-frost-ingress-', 'reports/native-repair-runtime-paid-crossbow-combat-')
new_launcher = work / ('tools/run_native_paid_crossbow_combat_' + tag + '.py')
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
output = work / ('reports/native-crossbow-combat-preparation-' + tag + '.json')
assert not output.exists()
output.write_text(json.dumps({'sameFrozenFrostCandidate': str(candidate), 'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'actualGenuinePaidSeedSample': 2, 'privateAuthorityWritten': False, 'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in [Path(__file__), candidate, old_source, old_launcher, new_source, new_launcher]}}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'crossbowObserverPrepared': True, 'productUnchanged': True}))
