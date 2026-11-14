package ftboss.storage308;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
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
        try {
            var server = event.getServer();
            UUID owner = new UUID(334, 1);
            Path playerFile = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(owner + ".dat");
            CompoundTag saved = NbtIo.readCompressed(playerFile, NbtAccounter.unlimitedHeap());
            CompoundTag ticket = saved.getList("Inventory", 10).getCompound(0).getCompound("FTBossRepairContinuation");
            Class<?> storeType = Class.forName("net.farmingtales.ftboss.item.OwnerLineageRevocationStore");
            var open = storeType.getDeclaredMethod("open", net.minecraft.server.MinecraftServer.class); open.setAccessible(true);
            Object store = open.invoke(null, server);
            var readDocument = storeType.getDeclaredMethod("readRepairDocument", UUID.class, boolean.class); readDocument.setAccessible(true);
            UUID documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-continuation/v1/" + owner).getBytes(StandardCharsets.UTF_8));
            CompoundTag before = (CompoundTag)readDocument.invoke(store, documentOwner, false);
            CompoundTag record = before.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString());
            if (!before.getString("Status").equals("COMMITTED_ORIGINAL_SAVE") || !record.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")
                || !ticket.equals(record.getCompound("Ticket")) || ticket.getLong("LeaseGeneration") != 1)
                throw new AssertionError("New boot lacks actual post-recovery committed generation-one save");
            var recordPath = storeType.getDeclaredMethod("recordPath", UUID.class, UUID.class); recordPath.setAccessible(true);
            Path lineagePath = (Path)recordPath.invoke(store, owner, ticket.getUUID("Weapon"));
            var readRecord = storeType.getDeclaredMethod("read", Path.class, UUID.class, UUID.class); readRecord.setAccessible(true);
            Object predecessor = readRecord.invoke(store, lineagePath, owner, ticket.getUUID("Weapon"));
            if (!Boolean.TRUE.equals(read(predecessor, "active")) || !ticket.getUUID("LeaseEpoch").equals(read(predecessor, "incarnation"))
                || ticket.getLong("LeaseGeneration") != ((Number)read(predecessor, "generation")).longValue())
                throw new AssertionError("Actual recovery predecessor is inactive or mismatched");
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var lease = guard.getDeclaredMethod("lease", Object.class); lease.setAccessible(true);
            var generation = guard.getDeclaredMethod("generation", Object.class); generation.setAccessible(true);
            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            ServerPlayer restored = new ServerPlayer(server, server.overworld(), new GameProfile(owner, "ft317_0"), ClientInformation.createDefault());
            if (storage.load(restored).isEmpty()) throw new AssertionError("Actual new-process original recovery load did not run");
            var actual = restored.getInventory().getItem(0);
            Object actualLease = lease.invoke(null, actual);
            Object actualGeneration = generation.invoke(null, actual);
            CompoundTag image = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
            if (actualLease == null || actualGeneration == null || !image.equals(record.getCompound("Expected")))
                throw new AssertionError("Post-recovery normal restart lacks genuine authority or whole-item match");
            Object successor = readRecord.invoke(store, lineagePath, owner, ticket.getUUID("Weapon"));
            if (!Boolean.TRUE.equals(read(successor, "active")) || !ticket.getUUID("LeaseEpoch").equals(read(successor, "incarnation"))
                || ((Number)read(successor, "generation")).longValue() != 2L) throw new AssertionError("Recovery restart did not advance same epoch generation one to two");
            CompoundTag consumed = (CompoundTag)readDocument.invoke(store, documentOwner, false);
            if (!consumed.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString()).getString("Status").equals("CONSUMED_ORIGINAL_LOAD"))
                throw new AssertionError("New-process recovery load did not consume continuation");
            UUID identity = image.getCompound("components").getCompound("minecraft:custom_data").getCompound("FTBossWeaponLifecycle").getCompound("NativeWear").getUUID("Identity");
            CompoundTag wear = NbtIo.readCompressed(server.getWorldPath(LevelResource.ROOT).resolve("data/ftboss_native_weapon_wear.dat"), NbtAccounter.unlimitedHeap())
                .getCompound("data").getCompound("Weapons").getCompound(identity.toString());
            if (wear.getInt("Damage") != actual.getDamageValue() || wear.getInt("Maximum") != actual.getMaxDamage())
                throw new AssertionError("Post-recovery original whole item and wear are incoherent");
            byte[] lineageBytes = Files.readAllBytes(lineagePath);
            ServerPlayer replay = new ServerPlayer(server, server.overworld(), new GameProfile(owner, "ft317_0"), ClientInformation.createDefault());
            if (storage.load(replay).isEmpty()) throw new AssertionError("Original consumed-file replay did not run");
            if (lease.invoke(null, replay.getInventory().getItem(0)) != null || generation.invoke(null, replay.getInventory().getItem(0)) != null
                || lease.invoke(null, actual) != actualLease || generation.invoke(null, actual) != actualGeneration
                || !Arrays.equals(lineageBytes, Files.readAllBytes(lineagePath)) || !consumed.equals(readDocument.invoke(store, documentOwner, false)))
                throw new AssertionError("Recovery normal-restart replay reissued or altered genuine successor");
            storage.save(restored);
            CompoundTag committed = (CompoundTag)readDocument.invoke(store, documentOwner, false);
            if (!committed.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")
                || lease.invoke(null, actual) != actualLease || generation.invoke(null, actual) != actualGeneration)
                throw new AssertionError("Recovery normal-restart original save did not commit without replacing physical authority");
            report.put("failures", 0); report.put("actualNewServerProcessFromNormallySavedCorruptRecoveryWorld", true);
            report.put("originalLoadHasGenuineNonNullLeaseAndGeneration", true); report.put("sameEpochGenerationOneToTwo", true);
            report.put("originalWholeItemAndWearCoherent", true); report.put("consumedReplayNoReissueAndFirstSuccessorUnchanged", true);
            report.put("actualOriginalPostRestartSaveCommitted", true);
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        }
        report.put("authorityWrittenByReflection", false); report.put("authorityFilesAlteredByThisRuntimeObserver", false);
        report.put("authenticatedMultiplayerAccepted", false); report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
