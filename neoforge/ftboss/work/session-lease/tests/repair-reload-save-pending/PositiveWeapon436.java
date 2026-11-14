package ftboss.storage308;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
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
    public PositiveWeapon436() {
        NeoForge.EVENT_BUS.addListener(PositiveWeapon436::started);
    }

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
            ServerPlayer restored = new ServerPlayer(server, server.overworld(), new GameProfile(owner, "ft317_0"), ClientInformation.createDefault());
            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            if (storage.load(restored).isEmpty()) throw new AssertionError("Actual original reload did not run");
            var actual = restored.getInventory().getItem(0);
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var lease = guard.getDeclaredMethod("lease", Object.class); lease.setAccessible(true);
            var generation = guard.getDeclaredMethod("generation", Object.class); generation.setAccessible(true);
            Object genuine = lease.invoke(null, actual);
            if (genuine == null || generation.invoke(null, actual) == null) throw new AssertionError("Genuine reload control missing");
            Object store = read(genuine, "store");
            Class<?> storeType = store.getClass();
            var readDocument = storeType.getDeclaredMethod("readRepairDocument", UUID.class, boolean.class); readDocument.setAccessible(true);
            UUID documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-continuation/v1/" + owner).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            storage.save(restored);
            Path playerFile = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(owner + ".dat");
            byte[] savedBytes = Files.readAllBytes(playerFile);
            CompoundTag saved = NbtIo.readCompressed(playerFile, NbtAccounter.unlimitedHeap());
            CompoundTag ticket = saved.getList("Inventory", 10).getCompound(0).getCompound("FTBossRepairContinuation");
            CompoundTag document = (CompoundTag)readDocument.invoke(store, documentOwner, false);
            CompoundTag record = document.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString());
            CompoundTag image = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
            if (!document.getString("Status").equals("COMMITTED_ORIGINAL_SAVE") || !record.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")
                || !ticket.equals(record.getCompound("Ticket")) || !image.equals(record.getCompound("Expected"))
                || ticket.getLong("LeaseGeneration") != ((Number)read(genuine, "generation")).longValue())
                throw new AssertionError("Original successor save not exactly committed before revocation");
            Object originalGeneration = generation.invoke(null, actual);
            try (java.nio.channels.FileChannel held = java.nio.channels.FileChannel.open(playerFile,
                java.nio.file.StandardOpenOption.READ, com.sun.nio.file.ExtendedOpenOption.NOSHARE_DELETE)) {
                storage.save(restored);
            }
            CompoundTag pending = (CompoundTag)readDocument.invoke(store, documentOwner, false);
            CompoundTag pendingRecord = pending.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString());
            if (!pending.getString("Status").equals("PENDING_ORIGINAL_SAVE")
                || !pendingRecord.getString("Status").equals("PENDING_ORIGINAL_SAVE")
                || pending.getLong("Sequence") != document.getLong("Sequence") + 1L
                || pendingRecord.getCompound("Ticket").getUUID("Save").equals(ticket.getUUID("Save")))
                throw new AssertionError("Real original replacement failure did not leave a new PENDING-only continuation");
            var locallyLive = storeType.getDeclaredMethod("locallyLive", genuine.getClass()); locallyLive.setAccessible(true);
            Object input = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
            if (!Boolean.TRUE.equals(locallyLive.invoke(null, genuine)) || input == null || Boolean.TRUE.equals(read(input, "retired"))
                || lease.invoke(null, actual) != genuine || generation.invoke(null, actual) != originalGeneration)
                throw new AssertionError("Real original save failure changed existing live physical authority");
            if (!Arrays.equals(savedBytes, Files.readAllBytes(playerFile))
                || !image.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual)))
                throw new AssertionError("Failed replacement changed original saved file or actual whole item");
            report.put("failures", 0);
            report.put("actualOriginalReloadNonNullLease", true);
            report.put("newOriginalSaveCommittedBeforeRevocation", true);
            report.put("realWindowsDeleteShareFailureLeavesPendingWithoutRecoverySave", true);
            report.put("originalPlayerFileAndActualLeaseGenerationWholeItemUnchanged", true);
            report.put("pendingSaveSequence", pending.getLong("Sequence"));
            report.put("committedSavedGeneration", ticket.getLong("LeaseGeneration"));
            report.put("checkpointImage", image.toString());
            report.put("checkpointTask", Files.readString(Path.of("claimed-repair-seed-task.txt")));
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        }
        report.put("authorityWrittenByReflection", false); report.put("authenticatedMultiplayerAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
