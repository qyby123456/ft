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
            Path playerFile = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(owner + ".dat");
            CompoundTag saved = NbtIo.readCompressed(playerFile, NbtAccounter.unlimitedHeap());
            CompoundTag ticket = saved.getList("Inventory", 10).getCompound(0).getCompound("FTBossRepairContinuation");
            Class<?> storeType = Class.forName("net.farmingtales.ftboss.item.OwnerLineageRevocationStore");
            var open = storeType.getDeclaredMethod("open", net.minecraft.server.MinecraftServer.class); open.setAccessible(true);
            Object store = open.invoke(null, server);
            var readDocument = storeType.getDeclaredMethod("readRepairDocument", UUID.class, boolean.class); readDocument.setAccessible(true);
            UUID documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-continuation/v1/" + owner).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            CompoundTag before = (CompoundTag)readDocument.invoke(store, documentOwner, false);
            CompoundTag originalRecord = before.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString());
            if (!before.getString("Status").equals("COMMITTED_ORIGINAL_SAVE") || !originalRecord.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")
                || !ticket.equals(originalRecord.getCompound("Ticket"))) throw new AssertionError("Inactive fixture lacks an otherwise exact unused committed ticket");
            var recordPath = storeType.getDeclaredMethod("recordPath", UUID.class, UUID.class); recordPath.setAccessible(true);
            Path lineagePath = (Path)recordPath.invoke(store, owner, ticket.getUUID("Weapon"));
            var readRecord = storeType.getDeclaredMethod("read", Path.class, UUID.class, UUID.class); readRecord.setAccessible(true);
            Object lineage = readRecord.invoke(store, lineagePath, owner, ticket.getUUID("Weapon"));
            if (!Boolean.FALSE.equals(read(lineage, "active"))) throw new AssertionError("Original revoked lineage unexpectedly active");
            byte[] lineageBytes = Files.readAllBytes(lineagePath);
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var lease = guard.getDeclaredMethod("lease", Object.class); lease.setAccessible(true);
            var generation = guard.getDeclaredMethod("generation", Object.class); generation.setAccessible(true);
            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            CompoundTag reserved = null;
            for (int attempt = 0; attempt < 2; attempt++) {
                ServerPlayer rejected = new ServerPlayer(server, server.overworld(), new GameProfile(owner, "ft317_0"), ClientInformation.createDefault());
                if (storage.load(rejected).isEmpty()) throw new AssertionError("Original inactive load did not run");
                var actual = rejected.getInventory().getItem(0);
                CompoundTag image = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                if (lease.invoke(null, actual) != null || generation.invoke(null, actual) != null
                    || !image.equals(originalRecord.getCompound("Expected"))) throw new AssertionError("Inactive lineage revived or whole item changed");
                UUID identity = image.getCompound("components").getCompound("minecraft:custom_data").getCompound("FTBossWeaponLifecycle").getCompound("NativeWear").getUUID("Identity");
                CompoundTag wear = NbtIo.readCompressed(server.getWorldPath(LevelResource.ROOT).resolve("data/ftboss_native_weapon_wear.dat"), NbtAccounter.unlimitedHeap())
                    .getCompound("data").getCompound("Weapons").getCompound(identity.toString());
                if (wear.getInt("Damage") != actual.getDamageValue() || wear.getInt("Maximum") != actual.getMaxDamage())
                    throw new AssertionError("Inactive fixture has incoherent original wear data");
                CompoundTag after = (CompoundTag)readDocument.invoke(store, documentOwner, false);
                CompoundTag record = after.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString());
                if (!record.getString("Status").equals("RESERVED_ORIGINAL_LOAD_NO_REISSUE") || !ticket.equals(record.getCompound("Ticket"))
                    || !originalRecord.getCompound("Expected").equals(record.getCompound("Expected")) || !Arrays.equals(lineageBytes, Files.readAllBytes(lineagePath)))
                    throw new AssertionError("Inactive rejection reopened ticket or mutated lineage");
                if (reserved != null && !reserved.equals(after)) throw new AssertionError("Reserved continuation was consumed again");
                reserved = after;
            }
            report.put("failures", 0);
            report.put("actualNewServerBoot", true);
            report.put("originalUnusedCommittedTicketAndWholeItemCoherent", true);
            report.put("originalLineageInactiveBeforeLoad", true);
            report.put("inactiveLineageAndSecondReplayGrantedNoAuthority", true);
            report.put("continuationReservedNoReissue", true);
            report.put("originalInactiveLineageBytesUnchanged", true);
            report.put("originalTicketGeneration", ticket.getLong("LeaseGeneration"));
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        }
        report.put("authorityWrittenByReflection", false); report.put("authenticatedMultiplayerAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
