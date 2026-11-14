import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = root / 'work/session-lease'
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
preparation = work / 'reports/native-trident-foundation-gate-preparation-04a9311d.json'
prepared = json.loads(preparation.read_text(encoding='utf-8'))
for name, expected in prepared['inputHashes'].items():
    assert hashlib.sha256(Path(name).read_bytes()).hexdigest() == expected, name
old_source = Path(prepared['newObserver'])
old_launcher = Path(prepared['newLauncher'])
source = old_source.read_text(encoding='utf-8')
anchor = '                    for (int ticks = 0; ticks < 12 && !arrow.isRemoved() && target.getHealth() == healthBefore; ticks++) arrow.tick();'
assert source.count(anchor) == 1
observation = """                    try {
                        var thrown = (net.minecraft.world.entity.projectile.ThrownTrident)arrow;
                        var copied = net.farmingtales.ftboss.item.FoodWeaponTridentCarrierReader.readOriginalItem(thrown);
                        var copiedImage = net.farmingtales.ftboss.item.NativeFoundationItemData.image(copied);
                        var paid = ((net.farmingtales.ftboss.item.NativePaidWeaponCarrier)arrow).ftboss$originalOwnerPaidWeapon();
                        Object lineage = ((net.farmingtales.ftboss.item.OwnerTridentCarrierAccess)arrow).ftboss$ownerTridentLineage();
                        flight.put("sourceKind", image.getString("SourceKind"));
                        flight.put("family", image.getString("Family"));
                        flight.put("copiedImageEqualsSignedWeapon", copiedImage.equals(image.getCompound("Weapon")));
                        flight.put("signedWeaponDamage", image.getCompound("Weapon").getCompound("components").getInt("minecraft:damage"));
                        flight.put("copiedWeaponDamage", copied.getDamageValue());
                        flight.put("copiedHasLease", leaseMethod.invoke(null, copied) != null);
                        flight.put("lineagePresent", lineage != null);
                        if (lineage != null) {
                            flight.put("lineageObservationRejected", read(lineage, "observationRejected"));
                            Object failure = read(lineage, "observationFailure");
                            flight.put("lineageObservationFailure", failure == null ? null : failure.toString());
                            flight.put("lineageActualCopySame", read(lineage, "copied") == copied);
                            flight.put("lineageReceiptSame", paid != null && read(lineage, "origin") == read(paid, "receipt"));
                        }
                        if (paid != null) {
                            Object receipt = read(paid, "receipt");
                            flight.put("paidMaterialPresent", read(paid, "material") != null);
                            flight.put("paidExpectationClosed", read(read(paid, "expectation"), "closed"));
                            flight.put("paidReceiptMaterialRejected", read(receipt, "materialRejected"));
                            flight.put("paidOperationInvalid", read(read(receipt, "operation"), "invalid"));
                        }
                        Class<?> ledger = Class.forName("net.farmingtales.ftboss.item.FoodWeaponSourceLedger");
                        var authorized = ledger.getDeclaredMethod("damageAuthorized", CompoundTag.class); authorized.setAccessible(true);
                        flight.put("signedDamageAuthorized", authorized.invoke(null, image));
                        var rawMatches = ledger.getDeclaredMethod("tridentRawMatchesExpected", net.minecraft.world.item.ItemStack.class, CompoundTag.class); rawMatches.setAccessible(true);
                        flight.put("copiedRawMatchesSignedWeapon", rawMatches.invoke(null, copied, image.getCompound("Weapon")));
                        for (String methodName : List.of("boundProfile", "materialFingerprint")) {
                            var method = ledger.getDeclaredMethod(methodName, net.minecraft.world.item.ItemStack.class); method.setAccessible(true);
                            flight.put("actual_" + methodName, method.invoke(null, copied));
                        }
                        var socket = ledger.getDeclaredMethod("socketFingerprint", net.minecraft.world.item.ItemStack.class, String.class); socket.setAccessible(true);
                        flight.put("actual_socketFingerprint", socket.invoke(null, copied, "MELEE"));
                        for (String key : List.of("BoundProfile", "RepairProfile", "SocketFingerprint", "MaterialFingerprint")) flight.put("signed_" + key, image.getString(key));
                        var diagnosticSource = player.damageSources().trident(arrow, player);
                        flight.put("originalTypedSourceIsProjectile", diagnosticSource.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE));
                        flight.put("resolvedBeforeOriginalImpactPresent", !net.farmingtales.ftboss.item.FoodWeaponSourceLedger.resolveReadOnly(diagnosticSource, player).isEmpty());
                    } catch (Throwable diagnosticFailure) {
                        flight.put("impactDiagnosticFailure", diagnosticFailure.toString());
                        if (diagnosticFailure instanceof java.lang.reflect.InvocationTargetException wrapped)
                            flight.put("impactDiagnosticCause", wrapped.getCause().toString());
                    }
"""
source = source.replace(anchor, observation + anchor)
new_source = work / 'tests/trident-impact-diagnostic-04a9311d/PositiveWeapon436.java'
new_launcher = work / 'tools/run_native_trident_impact_diagnostic_04a9311d.py'
code = old_launcher.read_text(encoding='utf-8')
code = code.replace('tests/trident-foundation-gate-04a9311d/PositiveWeapon436.java', 'tests/trident-impact-diagnostic-04a9311d/PositiveWeapon436.java')
code = code.replace('reports/native-repair-runtime-trident-foundation-gate-04a9311d.json', 'reports/native-repair-runtime-trident-impact-diagnostic-04a9311d.json')
anchor = "save(attempt / 'inputs-before308.json', inputs)"
before, found, after = code.rpartition(anchor); assert found
code = before + 'inputs[str(WORK / "tools/prepare_native_trident_impact_diagnostic_04a9311d.py")] = digest(WORK / "tools/prepare_native_trident_impact_diagnostic_04a9311d.py")\n' + found + after
compile(code, str(new_launcher), 'exec')
namespace['add'](new_source.relative_to(root).as_posix(), source)
namespace['add'](new_launcher.relative_to(root).as_posix(), code)
paths = [Path(__file__), helper, preparation, old_source, old_launcher, new_source, new_launcher]
output = {'candidate': prepared['candidate'], 'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'authorityFieldsWritten': False,
          'diagnosticOnly': True, 'fullLifecycleAccepted': False, 'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}}
namespace['add']('work/session-lease/reports/native-trident-impact-diagnostic-preparation-04a9311d.json', json.dumps(output, indent=2))
print('Read-only copied carrier and impact resolver diagnostic prepared')
