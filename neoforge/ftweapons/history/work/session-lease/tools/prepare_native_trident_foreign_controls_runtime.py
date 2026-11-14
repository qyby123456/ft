import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = root / 'work/session-lease'
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
current = json.loads((work / 'reports/native-repair-ranged-use-candidate.json').read_text(encoding='utf-8'))
tag = Path(current['attempt']).name.removeprefix('native-repair-')
preparation = work / ('reports/native-trident-foundation-gate-preparation-' + tag + '.json')
qualified = work / ('reports/native-trident-strict-qualified-preparation-' + tag + '.json')
if qualified.exists():
    preparation = qualified
prepared = json.loads(preparation.read_text(encoding='utf-8'))
for name, expected in prepared['inputHashes'].items():
    assert hashlib.sha256(Path(name).read_bytes()).hexdigest() == expected, name
positive = work / ('reports/native-repair-runtime-trident-foundation-gate-' + tag + '.json')
result = json.loads(positive.read_text(encoding='utf-8'))
assert result['exitCode'] == 0 and result['probe']['failures'] == 0
terminal = Path(result['attempt']) / 'terminal308.json'
closed = json.loads(terminal.read_text(encoding='utf-8'))
assert closed['stopSent'] and closed['completeSave']
old_source = Path(prepared['newObserver'])
old_launcher = Path(prepared['newLauncher'])
original = old_source.read_text(encoding='utf-8')
reports = []
for mode in ['external-remove', 'foreign-release']:
    source = original
    start = source.index('                CompoundTag beforeEarlyRelease =')
    end = source.index('                row.put("actualOriginalReleaseReturned", true);', start)
    controls = """                CompoundTag beforeForeign = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                Object beforeForeignInput = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                Class<?> semantic = Class.forName("net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority");
                var mutation = semantic.getDeclaredMethod("nativeOperationMutation", Object.class, int.class); mutation.setAccessible(true);
                if (Boolean.TRUE.equals(mutation.invoke(null, actual, 21))) throw new AssertionError("Foreign mutation callback granted authority");
                var factoryReady = semantic.getDeclaredMethod("nativeTridentFactoryReady", net.minecraft.world.item.ItemStack.class, Object.class, Object.class); factoryReady.setAccessible(true);
                if (Boolean.TRUE.equals(factoryReady.invoke(null, actual, actualLease, generation))) throw new AssertionError("Foreign factory terminal granted authority");
                Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
                var refresh = guard.getDeclaredMethod("refreshNativeOperation", net.minecraft.world.item.ItemStack.class, actualLease.getClass(), Object.class); refresh.setAccessible(true);
                if (Boolean.TRUE.equals(refresh.invoke(null, actual, actualLease, generation))) throw new AssertionError("Foreign refresh granted authority");
                RuntimeException sentinel = new RuntimeException("original-trident-factory-sentinel");
                int[] originalCalls = new int[]{0};
                boolean originalExceptionPreserved = false;
                try {
                    net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeTridentFactory(server.overworld(), player, actual, arguments -> {
                        originalCalls[0]++;
                        throw sentinel;
                    });
                } catch (RuntimeException originalFailure) {
                    originalExceptionPreserved = originalFailure == sentinel;
                }
                boolean helpersUnchanged = beforeForeign.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    && beforeForeignInput == ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput()
                    && leaseMethod.invoke(null, actual) == actualLease && generationMethod.invoke(null, actual) == generation
                    && !Boolean.TRUE.equals(read(generation, "retired"));
                row.put("foreignCallbacksPreserveExactInputImageAndAuthority", helpersUnchanged);
                row.put("foreignOriginalFactoryExceptionIdentityPreserved", originalExceptionPreserved);
                row.put("foreignOriginalFactoryCallCount", originalCalls[0]);
                if (!helpersUnchanged || !originalExceptionPreserved || originalCalls[0] != 1)
                    throw new AssertionError("Foreign callbacks modified authority or changed original exception identity");
"""
    if mode == 'external-remove':
        controls += """                actual.remove(net.minecraft.core.component.DataComponents.INTANGIBLE_PROJECTILE);
                row.put("externalAbsentComponentRemovalImageUnchanged", beforeForeign.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual)));
                row.put("externalAbsentComponentRemovalRetiresAuthority", read(generation, "retired"));
                if (!Boolean.TRUE.equals(row.get("externalAbsentComponentRemovalImageUnchanged")) || !Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Outside absent-component removal was incorrectly admitted as a native factory operation");
                actual.releaseUsing(server.overworld(), player, actual.getUseDuration(player) - 20);
"""
    else:
        controls += """                net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeRelease(actual.getItem(), actual, server.overworld(), player, actual.getUseDuration(player) - 20);
"""
    source = source[:start] + controls + source[end:]
    strict_start = '                if (publications.isEmpty() || publications.stream().anyMatch(value ->'
    strict_end = '                    throw new AssertionError("Original signed Session is not completely covered and live");'
    if strict_start in source:
        start = source.index(strict_start)
        end = source.index(strict_end, start) + len(strict_end)
        source = source[:start] + source[end:]
    start = source.index('                if (flights.size() != 1 || flights.stream().anyMatch(')
    end = source.index('                row.put("originalThrowPickupAndPersistenceAccepted", false);', start)
    rejection = """                boolean unsignedCarrier = flights.size() == 1 && flights.stream().noneMatch(value -> Boolean.TRUE.equals(value.get("snapshotPresent"))
                    || Boolean.TRUE.equals(value.get("snapshotValid")) || Boolean.TRUE.equals(value.get("signaturePresent")) || Boolean.TRUE.equals(value.get("paidCarrierPresent")));
                boolean scopesClosed = ((ThreadLocal<?>)readStatic(semantic, "NATIVE_REPAIR_OPERATION")).get() == null
                    && ((ThreadLocal<?>)readStatic(semantic, "NATIVE_WEAR")).get() == null
                    && ((ThreadLocal<?>)readStatic(Class.forName("net.farmingtales.ftboss.item.FoodWeaponSourceLedger"), "NATIVE_OPERATION")).get() == null
                    && Integer.valueOf(0).equals(((ThreadLocal<?>)readStatic(Class.forName("net.farmingtales.ftboss.item.FoodWeaponSourceLedger"), "NATIVE_DEPTH")).get());
                row.put("foreignNativeEntryOriginalThrowReturnedOnce", arrows.size() == 1 && actual.getDamageValue() == wearBefore + 1 && target.getHealth() < healthBefore);
                row.put("foreignNativeEntryNoSignedCarrier", unsignedCarrier);
                row.put("foreignNativeEntryAuthorityRetired", read(generation, "retired"));
                row.put("foreignNativeEntryScopesClosed", scopesClosed);
                if (!Boolean.TRUE.equals(row.get("foreignNativeEntryOriginalThrowReturnedOnce")) || !unsignedCarrier || !scopesClosed
                    || !Boolean.TRUE.equals(read(generation, "retired")) || !publications.isEmpty() || !actions.isEmpty()
                    || damageEvents.stream().anyMatch(value -> Boolean.TRUE.equals(value.get("resolvedSnapshotValid")) || Boolean.TRUE.equals(value.get("serverSignaturePresent"))))
                    throw new AssertionError("Foreign Trident entry minted paid signed authority or changed the original operation");
"""
    source = source[:start] + rejection + source[end:]
    source = source.replace('Actual paid ItemStack.use -> original Trident releaseUsing/constructor/add/remove inventory -> actual ThrownTrident.tick impact; pickup and persistence still unaccepted', 'Genuine paid original use, rejected outside helpers and original factory exception identity, then ' + mode + ' original throw and real impact without signed authority')
    new_source = work / ('tests/trident-' + mode + '-' + tag + '/PositiveWeapon436.java')
    new_launcher = work / ('tools/run_native_trident_' + mode.replace('-', '_') + '_' + tag + '.py')
    code = old_launcher.read_text(encoding='utf-8')
    source_path = old_source.relative_to(work).as_posix()
    assert code.count(source_path) == 1
    code = code.replace(source_path, new_source.relative_to(work).as_posix())
    code = code.replace('reports/native-repair-runtime-trident-foundation-gate-' + tag + '.json', 'reports/native-repair-runtime-trident-' + mode + '-' + tag + '.json')
    anchor = "save(attempt / 'inputs-before308.json', inputs)"
    before, found, after = code.rpartition(anchor); assert found
    code = before + 'inputs[str(WORK / "tools/prepare_native_trident_foreign_controls_runtime.py")] = digest(WORK / "tools/prepare_native_trident_foreign_controls_runtime.py")\n' + found + after
    compile(code, str(new_launcher), 'exec')
    namespace['add'](new_source.relative_to(root).as_posix(), source)
    namespace['add'](new_launcher.relative_to(root).as_posix(), code)
    reports.append({'mode': mode, 'observer': str(new_source), 'launcher': str(new_launcher)})
paths = [Path(__file__), helper, preparation, positive, terminal, old_source, old_launcher]
for item in reports:
    paths.extend([Path(item['observer']), Path(item['launcher'])])
output = {'candidate': prepared['candidate'], 'controls': reports, 'authorityFieldsWritten': False, 'fullLifecycleAccepted': False,
          'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}}
namespace['add']('work/session-lease/reports/native-trident-foreign-controls-preparation-' + tag + '.json', json.dumps(output, indent=2))
print(json.dumps({'candidateTag': tag, 'controlsPrepared': len(reports)}))
