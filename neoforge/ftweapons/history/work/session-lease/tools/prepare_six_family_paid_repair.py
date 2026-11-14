import hashlib
import json
import subprocess
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
original = work / 'tests/timer-aba/PositiveWeapon436.java'
text = original.read_text(encoding='utf-8-sig')
start = text.index('    private static void runNativeRepair(')
end = text.index('    private static Object verifySixWeaponScope436()', start)
helper = text[start:end]
assert 'action = 0; action < 5' in helper
assert 'Native claim did not publish genuine lease/generation' in helper
target = work / 'tests/six-family-paid-repair/PositiveWeapon436.java'
assert not target.exists()
body = r'''package ftboss.storage308;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("ftboss_storage308")
public final class PositiveWeapon436 {
    public PositiveWeapon436() { NeoForge.EVENT_BUS.addListener(PositiveWeapon436::started); }

    private static Object read(Object instance, String name) throws Exception {
        for (Class<?> type = instance.getClass(); type != null; type = type.getSuperclass()) {
            try { Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(instance); }
            catch (NoSuchFieldException missing) { }
        }
        throw new NoSuchFieldException(name);
    }

    private static Object readStatic(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(null);
    }

    private static void started(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        List<Map<String, Object>> cases = new ArrayList<>();
        List<ServerPlayer> scratches = new ArrayList<>();
        report.put("cases", cases);
        try {
            var server = event.getServer();
            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var leaseMethod = guard.getDeclaredMethod("lease", Object.class); leaseMethod.setAccessible(true);
            var generationMethod = guard.getDeclaredMethod("generation", Object.class); generationMethod.setAccessible(true);
            Class<?> storeType = Class.forName("net.farmingtales.ftboss.item.OwnerLineageRevocationStore");
            var open = storeType.getDeclaredMethod("open", net.minecraft.server.MinecraftServer.class); open.setAccessible(true);
            Object store = open.invoke(null, server);
            var readDocument = storeType.getDeclaredMethod("readRepairDocument", UUID.class, boolean.class); readDocument.setAccessible(true);
            var recordPath = storeType.getDeclaredMethod("recordPath", UUID.class, UUID.class); recordPath.setAccessible(true);
            var readRecord = storeType.getDeclaredMethod("read", Path.class, UUID.class, UUID.class); readRecord.setAccessible(true);
            List<String> ids = List.of("minecraft:iron_sword", "minecraft:bow", "minecraft:crossbow", "minecraft:iron_axe", "minecraft:trident", "ftboss:frost_queen_staff");
            for (int sample = 0; sample < ids.size(); sample++) {
                String id = ids.get(sample);
                UUID owner = new UUID(620, sample + 1);
                GameProfile profile = new GameProfile(owner, "ft_six_" + sample);
                ServerPlayer original = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
                scratches.add(original);
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(net.minecraft.resources.ResourceLocation.parse(id)).orElseThrow();
                ItemStack weapon = new ItemStack(item);
                Map<String, Object> row = new LinkedHashMap<>(); row.put("id", id); row.put("owner", owner.toString()); row.put("itemClass", item.getClass().getName()); cases.add(row);
                CompoundTag data = new CompoundTag();
                CompoundTag evolution = new CompoundTag();
                evolution.putString("Profile", "butcher"); evolution.putString("EvolutionSkill", "blood_feast");
                evolution.putInt("Level", 1); evolution.putInt("Experience", 0); evolution.putInt("ProgressionVersion", 2);
                data.put("FTBossFoodEvolution", evolution);
                weapon.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
                if (!net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.initializeForServerEdit(weapon))
                    throw new AssertionError("Original native foundation initialization failed for " + id);
                original.getInventory().setItem(0, weapon);
                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).add(original);
                net.farmingtales.ftboss.item.NativeFoodWeaponWear.manage(original, weapon);
                net.farmingtales.ftboss.item.FoodWeaponLifecycle.ensureTracked(weapon);
                weapon.setDamageValue(10);
                storage.save(original);
                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(original);
                ServerPlayer repairedPlayer = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
                scratches.add(repairedPlayer);
                if (storage.load(repairedPlayer).isEmpty()) throw new AssertionError("Original pre-repair player load did not run for " + id);
                ItemStack actual = repairedPlayer.getInventory().getItem(0);
                if (!actual.is(item) || !net.farmingtales.ftboss.item.NativeFoodWeaponWear.eligible(actual) || actual.getDamageValue() != 10)
                    throw new AssertionError("Original native wear fixture is not eligible/coherent for " + id);
                runNativeRepair(event, repairedPlayer, actual, row);
                Object predecessorLease = leaseMethod.invoke(null, actual);
                Object predecessorGeneration = generationMethod.invoke(null, actual);
                if (predecessorLease == null || predecessorGeneration == null) throw new AssertionError("Actual paid CLAIM did not publish physical authority for " + id);
                CompoundTag expected = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                storage.save(repairedPlayer);
                UUID documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-continuation/v1/" + owner).getBytes(StandardCharsets.UTF_8));
                CompoundTag committed = (CompoundTag)readDocument.invoke(store, documentOwner, false);
                if (!committed.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")) throw new AssertionError("Actual paid save did not commit for " + id);
                ServerPlayer successorPlayer = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
                scratches.add(successorPlayer);
                if (storage.load(successorPlayer).isEmpty()) throw new AssertionError("Actual original paid reload did not run for " + id);
                ItemStack successor = successorPlayer.getInventory().getItem(0);
                Object successorLease = leaseMethod.invoke(null, successor);
                Object successorGeneration = generationMethod.invoke(null, successor);
                if (successorLease == null || successorGeneration == null || successorLease == predecessorLease || successorGeneration == predecessorGeneration
                    || !expected.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(successor)) || !Boolean.TRUE.equals(read(predecessorGeneration, "retired")))
                    throw new AssertionError("Actual same-process paid reload failed to advance/revoke predecessor for " + id);
                UUID weaponId = net.farmingtales.ftboss.item.FoodWeaponLifecycle.weaponId(successor);
                Path lineagePath = (Path)recordPath.invoke(store, owner, weaponId);
                Object lineage = readRecord.invoke(store, lineagePath, owner, weaponId);
                if (!Boolean.TRUE.equals(read(lineage, "active")) || ((Number)read(lineage, "generation")).longValue() != 1L)
                    throw new AssertionError("Original lineage did not reach generation one for " + id);
                byte[] lineageBytes = Files.readAllBytes(lineagePath);
                CompoundTag consumed = (CompoundTag)readDocument.invoke(store, documentOwner, false);
                ServerPlayer replay = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
                scratches.add(replay);
                if (storage.load(replay).isEmpty()) throw new AssertionError("Original consumed replay did not run for " + id);
                if (leaseMethod.invoke(null, replay.getInventory().getItem(0)) != null || generationMethod.invoke(null, replay.getInventory().getItem(0)) != null
                    || leaseMethod.invoke(null, successor) != successorLease || generationMethod.invoke(null, successor) != successorGeneration
                    || !Arrays.equals(lineageBytes, Files.readAllBytes(lineagePath)) || !consumed.equals(readDocument.invoke(store, documentOwner, false)))
                    throw new AssertionError("Consumed save replay reissued or mutated genuine successor for " + id);
                storage.save(successorPlayer);
                CompoundTag saved = NbtIo.readCompressed(server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(owner + ".dat"), NbtAccounter.unlimitedHeap());
                CompoundTag ticket = saved.getList("Inventory", 10).getCompound(0).getCompound("FTBossRepairContinuation");
                CompoundTag finalDocument = (CompoundTag)readDocument.invoke(store, documentOwner, false);
                if (ticket.getLong("LeaseGeneration") != 1L || !finalDocument.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")
                    || !ticket.equals(finalDocument.getCompound("Continuations").getCompound(weaponId.toString()).getCompound("Ticket")))
                    throw new AssertionError("Original successor save lacks exact committed generation-one ticket for " + id);
                row.put("actualPaidClaimAndThreeSupplyConsumptions", true); row.put("actualOriginalSaveCommitted", true);
                row.put("actualOriginalLoadRestoredNonNullGenuineSuccessor", true); row.put("sameEpochGenerationZeroToOne", true);
                row.put("oldPhysicalGenerationRetired", true); row.put("consumedReplayDeniedAndFirstSuccessorUnchanged", true);
                row.put("actualSuccessorOriginalSaveCommitted", true); row.put("successorWholeItemImage", expected.toString());
                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(repairedPlayer);
                ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).remove(owner, repairedPlayer);
            }
            report.put("failures", 0); report.put("sixNativeDamageableFamilyRepresentativesPaidRepairAndSameProcessReload", true);
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        } finally {
            try {
                var players = (List<ServerPlayer>)read(event.getServer().getPlayerList(), "players");
                var byId = (Map<UUID, ServerPlayer>)read(event.getServer().getPlayerList(), "playersByUUID");
                for (ServerPlayer scratch : scratches) { players.remove(scratch); byId.remove(scratch.getUUID(), scratch); }
                report.put("scratchPlayersRemoved", scratches.stream().noneMatch(players::contains));
            } catch (Exception failure) { report.put("failures", 1); report.put("cleanupFailure", failure.toString()); }
        }
        report.put("authorityWrittenByReflection", false); report.put("authenticatedMultiplayerAccepted", false);
        report.put("fullSixFamilyRuntimeMatrixAccepted", false); report.put("fullSignedCombatSessionRunByThisFixture", false);
        report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }

'''
body += helper + '}\n'
patcher = 'C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe'
completed = []
remaining = body.splitlines()
while remaining:
    chunk = []
    size = 0
    while remaining and (not chunk or size + len(remaining[0]) < 10000):
        line = remaining.pop(0)
        chunk.append(line)
        size += len(line) + 3
    if completed:
        patch = '*** Begin Patch\n*** Update File: ' + target.relative_to(root).as_posix() + '\n@@\n'
        patch += ''.join(' ' + line + '\n' for line in completed[-10:])
        patch += ''.join('+' + line + '\n' for line in chunk) + '*** End of File\n*** End Patch\n'
    else:
        patch = '*** Begin Patch\n*** Add File: ' + target.relative_to(root).as_posix() + '\n' + ''.join('+' + line + '\n' for line in chunk) + '*** End Patch\n'
    subprocess.run([patcher, '--codex-run-as-apply-patch', patch], cwd=root, check=True)
    completed.extend(chunk)
receipt = work / 'reports/six-family-paid-repair-helper-origin.json'
assert not receipt.exists()
receipt.write_text(json.dumps({'originalAcceptedFixture': str(original), 'originalSha256': hashlib.sha256(original.read_bytes()).hexdigest(),
    'helperSha256': hashlib.sha256(helper.encode('utf-8')).hexdigest(), 'helperCopiedVerbatim': True,
    'newFixture': str(target), 'newFixtureSha256': hashlib.sha256(target.read_bytes()).hexdigest()}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
