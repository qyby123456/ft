package ftboss.storage308;

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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
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

    private static void started(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        List<Map<String, Object>> cases = new ArrayList<>(); report.put("cases", cases);
        try {
            var server = event.getServer();
            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            Class<?> storeType = Class.forName("net.farmingtales.ftboss.item.OwnerLineageRevocationStore");
            var open = storeType.getDeclaredMethod("open", net.minecraft.server.MinecraftServer.class); open.setAccessible(true);
            Object store = open.invoke(null, server);
            var readDocument = storeType.getDeclaredMethod("readRepairDocument", UUID.class, boolean.class); readDocument.setAccessible(true);
            var recordPath = storeType.getDeclaredMethod("recordPath", UUID.class, UUID.class); recordPath.setAccessible(true);
            var readRecord = storeType.getDeclaredMethod("read", Path.class, UUID.class, UUID.class); readRecord.setAccessible(true);
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var lease = guard.getDeclaredMethod("lease", Object.class); lease.setAccessible(true);
            var generation = guard.getDeclaredMethod("generation", Object.class); generation.setAccessible(true);
            List<String> ids = List.of("minecraft:iron_sword", "minecraft:bow", "minecraft:crossbow", "minecraft:iron_axe", "minecraft:trident", "ftboss:frost_queen_staff");
            for (int sample : new int[]{5}) {
                String id = ids.get(sample);
                UUID owner = new UUID(620, sample + 1);
                Path playerFile = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(owner + ".dat");
                CompoundTag saved = NbtIo.readCompressed(playerFile, NbtAccounter.unlimitedHeap());
                CompoundTag ticket = saved.getList("Inventory", 10).getCompound(0).getCompound("FTBossRepairContinuation");
                UUID documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-continuation/v1/" + owner).getBytes(StandardCharsets.UTF_8));
                CompoundTag before = (CompoundTag)readDocument.invoke(store, documentOwner, false);
                CompoundTag record = before.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString());
                if (!before.getString("Status").equals("COMMITTED_ORIGINAL_SAVE") || !record.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")
                    || !ticket.equals(record.getCompound("Ticket")) || ticket.getLong("LeaseGeneration") != 3)
                    throw new AssertionError("New boot lacks actual six-family committed generation-three post-combat save for " + id);
                Path lineagePath = (Path)recordPath.invoke(store, owner, ticket.getUUID("Weapon"));
                Object predecessor = readRecord.invoke(store, lineagePath, owner, ticket.getUUID("Weapon"));
                if (!Boolean.TRUE.equals(read(predecessor, "active")) || !ticket.getUUID("LeaseEpoch").equals(read(predecessor, "incarnation"))
                    || ((Number)read(predecessor, "generation")).longValue() != 3L) throw new AssertionError("Original predecessor mismatch for " + id);
                GameProfile profile = new GameProfile(owner, "ft_six_" + sample);
                ServerPlayer restored = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
                if (storage.load(restored).isEmpty()) throw new AssertionError("Actual new-process original load did not run for " + id);
                var actual = restored.getInventory().getItem(0);
                Object actualLease = lease.invoke(null, actual);
                Object actualGeneration = generation.invoke(null, actual);
                CompoundTag image = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                if (actualLease == null || actualGeneration == null || !image.equals(record.getCompound("Expected"))
                    || !id.equals(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(actual.getItem()).toString()))
                    throw new AssertionError("Actual new-process original load lacks genuine successor or exact whole item for " + id);
                if (actual.getDamageValue() != (4)) throw new AssertionError("Original post-combat wear was not restored exactly for " + id);
                Object successor = readRecord.invoke(store, lineagePath, owner, ticket.getUUID("Weapon"));
                if (!Boolean.TRUE.equals(read(successor, "active")) || !ticket.getUUID("LeaseEpoch").equals(read(successor, "incarnation"))
                    || ((Number)read(successor, "generation")).longValue() != 4L) throw new AssertionError("New process did not advance same epoch three to four for " + id);
                UUID identity = image.getCompound("components").getCompound("minecraft:custom_data").getCompound("FTBossWeaponLifecycle").getCompound("NativeWear").getUUID("Identity");
                CompoundTag wear = NbtIo.readCompressed(server.getWorldPath(LevelResource.ROOT).resolve("data/ftboss_native_weapon_wear.dat"), NbtAccounter.unlimitedHeap())
                    .getCompound("data").getCompound("Weapons").getCompound(identity.toString());
                if (wear.getInt("Damage") != actual.getDamageValue() || wear.getInt("Maximum") != actual.getMaxDamage())
                    throw new AssertionError("Original whole item and wear mismatch after new-process load for " + id);
                CompoundTag consumed = (CompoundTag)readDocument.invoke(store, documentOwner, false);
                if (!consumed.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString()).getString("Status").equals("CONSUMED_ORIGINAL_LOAD"))
                    throw new AssertionError("Actual new-process original load did not consume exact ticket for " + id);
                byte[] lineageBytes = Files.readAllBytes(lineagePath);
                ServerPlayer replay = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
                if (storage.load(replay).isEmpty()) throw new AssertionError("Original consumed replay did not run for " + id);
                if (lease.invoke(null, replay.getInventory().getItem(0)) != null || generation.invoke(null, replay.getInventory().getItem(0)) != null
                    || lease.invoke(null, actual) != actualLease || generation.invoke(null, actual) != actualGeneration
                    || !Arrays.equals(lineageBytes, Files.readAllBytes(lineagePath)) || !consumed.equals(readDocument.invoke(store, documentOwner, false)))
                    throw new AssertionError("New-process consumed replay reissued or changed live successor for " + id);
                storage.save(restored);
                CompoundTag finalDocument = (CompoundTag)readDocument.invoke(store, documentOwner, false);
                CompoundTag savedAgain = NbtIo.readCompressed(playerFile, NbtAccounter.unlimitedHeap());
                CompoundTag newTicket = savedAgain.getList("Inventory", 10).getCompound(0).getCompound("FTBossRepairContinuation");
                if (!finalDocument.getString("Status").equals("COMMITTED_ORIGINAL_SAVE") || newTicket.getLong("LeaseGeneration") != 4L
                    || !newTicket.equals(finalDocument.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString()).getCompound("Ticket")))
                    throw new AssertionError("Actual new-process original successor save did not commit for " + id);
                cases.add(Map.of("id", id, "originalWholeItemAndWearCoherent", true, "genuineNonNullLeaseAndGeneration", true,
                    "sameEpochGenerationThreeToFour", true, "consumedReplayDeniedWithoutChangingFirstSuccessor", true,
                    "actualOriginalGenerationFourSaveCommitted", true));
            }
            report.put("failures", 0); report.put("paidFrostNovaPostCombatContinuationAcrossNewProcess", true);
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        }
        report.put("authorityWrittenByReflection", false); report.put("authenticatedMultiplayerAccepted", false);
        report.put("fullSixFamilyRuntimeMatrixAccepted", false); report.put("fullSignedCombatSessionRunByThisFixture", false);
        report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
