import hashlib
import json
import subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = Path(__file__).resolve().parents[1]
patcher = 'C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe'

def add(relative, text):
    assert not (root / relative).exists(), relative
    lines = text.splitlines()
    previous = []
    while lines:
        chunk = []
        length = 0
        while lines and length + len(lines[0]) < 6000:
            line = lines.pop(0)
            chunk.append(line)
            length += len(line) + 1
        assert chunk
        if previous:
            body = '*** Update File: ' + relative + '\n@@\n' + ''.join(' ' + line + '\n' for line in previous[-4:])
            suffix = '*** End of File\n'
        else:
            body = '*** Add File: ' + relative + '\n'
            suffix = ''
        patch = '*** Begin Patch\n' + body + ''.join('+' + line + '\n' for line in chunk) + suffix + '*** End Patch\n'
        result = subprocess.run([patcher, '--codex-run-as-apply-patch', patch], cwd=root, capture_output=True)
        assert result.returncode == 0, result.stderr.decode('utf-8', errors='replace')
        previous.extend(chunk)

candidate = work / 'reports/native-repair-melee-ingress-candidate.json'
receipt = json.loads(candidate.read_text(encoding='utf-8'))
tag = Path(receipt['attempt']).name.removeprefix('native-repair-')
frozen = work / ('reports/native-repair-melee-ingress-candidate-' + tag + '.json')
assert not frozen.exists()
frozen.write_bytes(candidate.read_bytes())
source = (work / 'tests/paid-melee-noop/PositiveWeapon436.java').read_text(encoding='utf-8')
source = source.replace('    private static ServerStartedEvent pending;', '    private static ServerStartedEvent pending;\n    private static net.minecraft.world.damagesource.DamageSource capturedSource;\n    private static CompoundTag capturedSigned;')
source = source.replace('                row.put("resolvedSnapshotPresent", !signed.isEmpty());', '                capturedSource = event.getSource(); capturedSigned = signed.copy();\n                row.put("resolvedSnapshotPresent", !signed.isEmpty());')
anchor = '                actions.clear(); damageEvents.clear();'
replacement = '''                int[] attempts = new int[]{0};
                boolean foreignResult = net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeMeleeActualHurt(player, target,
                    player.damageSources().playerAttack(player), 1.0F, arguments -> {
                        attempts[0]++;
                        try {
                            if (((ThreadLocal<?>)readStatic(net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class, "PRIVATE_NATIVE_HURT")).get() != null
                                || ((ThreadLocal<?>)readStatic(net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class, "PRIVATE_INGRESS")).get() != null)
                                throw new AssertionError("Foreign direct callback manufactured native melee authority");
                        } catch (Exception failure) { throw new IllegalStateException(failure); }
                        return false;
                    });
                if (foreignResult || attempts[0] != 1 || !beforeTracking.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    || leaseMethod.invoke(null, actual) != actualLease || generationMethod.invoke(null, actual) != generation
                    || Boolean.TRUE.equals(read(generation, "retired"))) throw new AssertionError("Foreign direct native melee call changed original outcome or authority");
                row.put("foreignDirectEntryDelegatesExactlyOnceWithoutAuthorityOrDataMutation", true);
                RuntimeException originalFailure = new RuntimeException("original foreign-call identity sentinel");
                try {
                    net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeMeleeActualHurt(player, target,
                        player.damageSources().playerAttack(player), 1.0F, arguments -> { throw originalFailure; });
                    throw new AssertionError("Original callback exception was swallowed");
                } catch (RuntimeException returned) {
                    if (returned != originalFailure) throw new AssertionError("Original callback exception identity changed", returned);
                }
                row.put("foreignDirectEntryPreservesOriginalCallbackExceptionIdentity", true);
                capturedSource = null; capturedSigned = null;
                actions.clear(); damageEvents.clear();'''
assert source.count(anchor) == 1
source = source.replace(anchor, replacement)
anchor = '                row.put("generationRetiredAfterAttack", read(generation, "retired"));'
source = source.replace(anchor, anchor + '''
                row.put("capturedNativeMeleePurpose", capturedSigned == null ? "" : capturedSigned.getString("DamagePurpose"));
                boolean sourceDeniedAfterReturn = capturedSource != null && capturedSigned != null
                    && net.farmingtales.ftboss.item.FoodWeaponSourceLedger.resolveReadOnly(capturedSource, player).isEmpty()
                    && !net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(capturedSigned, player);
                row.put("sameOriginalSourceAndSignedSnapshotDeniedOutsideActualHurtScope", sourceDeniedAfterReturn);
                if (!sourceDeniedAfterReturn) throw new AssertionError("Original melee callback returned with reusable actual-hurt authority");''')
source = source.replace('                row.put("actualOriginalPostCombatSaveCalled", true);', '''                row.put("actualOriginalPostCombatSaveCalled", true);
                Class<?> storeType = Class.forName("net.farmingtales.ftboss.item.OwnerLineageRevocationStore");
                var readDocument = storeType.getDeclaredMethod("readRepairDocument", UUID.class, boolean.class); readDocument.setAccessible(true);
                Object store = read(actualLease, "store");
                UUID documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-continuation/v1/" + owner).getBytes(StandardCharsets.UTF_8));
                CompoundTag committed = (CompoundTag)readDocument.invoke(store, documentOwner, false);
                Path playerFile = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_DATA_DIR).resolve(owner + ".dat");
                CompoundTag saved = net.minecraft.nbt.NbtIo.readCompressed(playerFile, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                CompoundTag ticket = saved.getList("Inventory", 10).getCompound(0).getCompound("FTBossRepairContinuation");
                CompoundTag committedRecord = committed.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString());
                if (!committed.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")
                    || !committedRecord.getString("Status").equals("COMMITTED_ORIGINAL_SAVE") || ticket.getLong("LeaseGeneration") != 3L
                    || !ticket.getUUID("Save").equals(saved.getUUID("FTBossRepairSave")))
                    throw new AssertionError("Actual post-melee original save did not commit the genuine generation-three continuation");
                row.put("actualOriginalPostCombatSaveAuthenticatedCommitted", true);''')
add('work/session-lease/tests/paid-melee-ingress-' + tag + '/PositiveWeapon436.java', source)
launcher = (work / 'tools/run_native_paid_melee_noop.py').read_text(encoding='utf-8')
launcher = launcher.replace('native-repair-melee-noop-candidate-fd5476e2.json', frozen.name)
launcher = launcher.replace('6efebd38bade0bab0f22c70824e36588d342ad58eb69d2992881198451ae24ac', hashlib.sha256(frozen.read_bytes()).hexdigest())
launcher = launcher.replace('tests/paid-melee-noop/PositiveWeapon436.java', 'tests/paid-melee-ingress-' + tag + '/PositiveWeapon436.java')
launcher = launcher.replace('native-repair-runtime-paid-melee-noop.json', 'native-repair-runtime-paid-melee-ingress-' + tag + '.json')
launcher = launcher.replace("            raise RuntimeError('Focused observer deadline reached; inspect live process, do not restart automatically')",
    "            print({'observationDeadline': True, 'pid': process.pid, 'sameProcessStillObserved': True}, flush=True)\n            deadline = time.monotonic() + 1800")
add('work/session-lease/tools/run_native_paid_melee_ingress_' + tag + '.py', launcher)
print(json.dumps({'candidate': str(frozen), 'sha256': hashlib.sha256(frozen.read_bytes()).hexdigest(), 'tag': tag}))
