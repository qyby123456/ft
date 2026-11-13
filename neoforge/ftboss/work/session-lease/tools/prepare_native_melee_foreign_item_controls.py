import hashlib
import json
import subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = Path(__file__).resolve().parents[1]
tag = 'eef72352'
seed = work / ('reports/native-repair-runtime-melee-post-combat-restart-' + tag + '.json')
assert json.loads(seed.read_text(encoding='utf-8'))['probe']['failures'] == 0
source = '''package ftboss.storage308;

import com.google.gson.GsonBuilder;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("ftboss_storage308")
public final class PositiveWeapon436 {
    public PositiveWeapon436() { NeoForge.EVENT_BUS.addListener(PositiveWeapon436::started); }

    private static Object read(Object instance, String name) throws Exception {
        for (Class<?> type = instance instanceof Class<?> supplied ? supplied : instance.getClass(); type != null; type = type.getSuperclass()) {
            try { Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(instance instanceof Class<?> ? null : instance); }
            catch (NoSuchFieldException missing) { }
        }
        throw new NoSuchFieldException(name);
    }

    private static void started(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        List<Map<String, Object>> cases = new ArrayList<>(); report.put("cases", cases);
        try {
            var server = event.getServer();
            var storage = (net.minecraft.world.level.storage.PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var lease = guard.getDeclaredMethod("lease", Object.class); lease.setAccessible(true);
            var generation = guard.getDeclaredMethod("generation", Object.class); generation.setAccessible(true);
            Class<?> semantic = Class.forName("net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority");
            for (int sample : new int[]{0, 3}) {
                ServerPlayer player = new ServerPlayer(server, server.overworld(), new com.mojang.authlib.GameProfile(new UUID(620, sample + 1), "ft_six_" + sample), net.minecraft.server.level.ClientInformation.createDefault());
                if (storage.load(player).isEmpty()) throw new AssertionError("Original genuine post-combat load did not run");
                ItemStack actual = player.getInventory().getItem(0);
                String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(actual.getItem()).toString();
                Object actualLease = lease.invoke(null, actual);
                Object actualGeneration = generation.invoke(null, actual);
                if (actualLease == null || actualGeneration == null || ((Number)read(actualLease, "generation")).longValue() != 5L || Boolean.TRUE.equals(read(actualGeneration, "retired"))) throw new AssertionError("Control lacks genuine live generation five");
                CompoundTag before = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                var target = new net.minecraft.world.entity.monster.Zombie(server.overworld());
                RuntimeException originalFailure = new RuntimeException("foreign-original-item-callback");
                int[] calls = new int[]{0};
                try {
                    if (sample == 0) net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeMeleeOriginalCopy(player, actual, arguments -> { calls[0]++; throw originalFailure; });
                    else net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeMeleeOriginalPost(player, actual, target, player, arguments -> { calls[0]++; throw originalFailure; });
                    throw new AssertionError("Foreign callback exception swallowed");
                } catch (RuntimeException expected) {
                    if (expected != originalFailure || calls[0] != 1 || Boolean.TRUE.equals(read(actualGeneration, "retired")) || !before.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))) throw new AssertionError("Foreign callback exception identity or unchanged authority differs");
                }
                int wearBefore = actual.getDamageValue();
                calls[0] = 0;
                if (sample == 0) {
                    ItemStack copied = net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeMeleeOriginalCopy(player, actual, arguments -> { calls[0]++; return actual.copy(); });
                    if (copied == actual || lease.invoke(null, copied) != null || generation.invoke(null, copied) != null || !before.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(copied)) || !before.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))) throw new AssertionError("Foreign copy acquired authority or changed the item image");
                } else {
                    net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeMeleeOriginalPost(player, actual, target, player, arguments -> { calls[0]++; actual.postHurtEnemy(target, player); return null; });
                    if (actual.getDamageValue() != wearBefore + 2) throw new AssertionError("Original foreign axe post callback was not preserved");
                }
                if (calls[0] != 1 || !Boolean.TRUE.equals(read(actualGeneration, "retired")) || lease.invoke(null, actual) != actualLease || generation.invoke(null, actual) != actualGeneration) throw new AssertionError("Foreign original item callback retained or reissued live authority");
                for (String name : List.of("NATIVE_REPAIR_OPERATION", "NATIVE_WEAR")) if (((ThreadLocal<?>)read(semantic, name)).get() != null) throw new AssertionError("Foreign callback created semantic permission");
                for (String name : List.of("PRIVATE_NATIVE_HURT", "PRIVATE_INGRESS")) if (((ThreadLocal<?>)read(net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class, name)).get() != null) throw new AssertionError("Foreign callback created hurt permission");
                cases.add(Map.of("id", id, "genuineGenerationFiveBeforeControl", true, "originalForeignCallbackExactlyOnce", true, "originalCallbackExceptionIdentityPreservedWithLiveUnchangedBeforeMutation", true, "foreignOriginalMutationRetiresGenuineAuthority", true, "noPrivatePermissionOrAuthorityReissued", true, "copyControl", sample == 0));
            }
            report.put("failures", 0);
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        }
        report.put("authorityWrittenByReflection", false); report.put("authenticatedMultiplayerAccepted", false); report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
'''


def add(path, text):
    assert not path.exists(), path
    pending = text.splitlines()
    previous = []
    while pending:
        chunk = []
        length = 0
        while pending and length + len(pending[0]) < 6000:
            line = pending.pop(0)
            chunk.append(line)
            length += len(line) + 1
        assert chunk
        body = ('*** Update File: ' + path.as_posix() + '\n@@\n' + ''.join(' ' + line + '\n' for line in previous[-4:])) if previous else '*** Add File: ' + path.as_posix() + '\n'
        patch = '*** Begin Patch\n' + body + ''.join('+' + line + '\n' for line in chunk) + ('*** End of File\n' if previous else '') + '*** End Patch\n'
        result = subprocess.run(['C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe', '--codex-run-as-apply-patch', patch], cwd=root, capture_output=True)
        assert result.returncode == 0, result.stderr.decode('utf-8', errors='replace')
        previous.extend(chunk)


test = work / 'tests/melee-foreign-item-eef72352/PositiveWeapon436.java'
add(test, source)
old_launcher = work / 'tools/run_native_melee_post_combat_restart_eef72352.py'
launcher = old_launcher.read_text(encoding='utf-8')
launcher = launcher.replace("seed_receipt = WORK / 'reports/native-repair-runtime-paid-melee-ingress-eef72352.json'", "seed_receipt = WORK / 'reports/native-repair-runtime-melee-post-combat-restart-eef72352.json'")
launcher = launcher.replace("assert all(case['actualOriginalPostCombatSaveAuthenticatedCommitted'] for case in seed_result['probe']['cases'])", "assert all(case['actualOriginalGenerationFourSaveCommitted'] for case in seed_result['probe']['cases'])")
launcher = launcher.replace('tests/melee-post-combat-restart-eef72352/PositiveWeapon436.java', 'tests/melee-foreign-item-eef72352/PositiveWeapon436.java')
launcher = launcher.replace("output = WORK / 'reports/native-repair-runtime-melee-post-combat-restart-eef72352.json'", "output = WORK / 'reports/native-repair-runtime-melee-foreign-item-eef72352.json'")
new_launcher = work / 'tools/run_native_melee_foreign_item_eef72352.py'
add(new_launcher, launcher)
manifest = work / 'reports/native-melee-foreign-item-preparation-eef72352.json'
assert not manifest.exists()
manifest.write_text(json.dumps({'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in [Path(__file__), seed, old_launcher, test, new_launcher]}, 'installable': False}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'launcher': str(new_launcher), 'productCandidateUnchanged': True}))
