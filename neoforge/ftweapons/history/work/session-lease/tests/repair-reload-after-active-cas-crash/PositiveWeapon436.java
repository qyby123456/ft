package ftboss.storage308;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
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
        ServerPlayer registered = null;
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
            if (!before.getString("Status").equals("COMMITTED_ORIGINAL_SAVE") || !originalRecord.getString("Status").equals("RESERVED_ORIGINAL_LOAD_NO_REISSUE")
                || !ticket.equals(originalRecord.getCompound("Ticket")) || !originalRecord.hasUUID("Load"))
                throw new AssertionError("Crash restart lacks its exact authenticated reserved continuation");
            var recordPath = storeType.getDeclaredMethod("recordPath", UUID.class, UUID.class); recordPath.setAccessible(true);
            Path lineagePath = (Path)recordPath.invoke(store, owner, ticket.getUUID("Weapon"));
            var readRecord = storeType.getDeclaredMethod("read", Path.class, UUID.class, UUID.class); readRecord.setAccessible(true);
            Object lineage = readRecord.invoke(store, lineagePath, owner, ticket.getUUID("Weapon"));
            if (!Boolean.TRUE.equals(read(lineage, "active")) || !ticket.getUUID("LeaseEpoch").equals(read(lineage, "incarnation"))
                || ((Number)read(lineage, "generation")).longValue() != ticket.getLong("LeaseGeneration") + 1L
                || !originalRecord.getUUID("Load").equals(read(lineage, "transaction")))
                throw new AssertionError("Crash restart did not preserve the genuinely committed active CAS successor");
            byte[] lineageBytes = Files.readAllBytes(lineagePath);
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var lease = guard.getDeclaredMethod("lease", Object.class); lease.setAccessible(true);
            var generation = guard.getDeclaredMethod("generation", Object.class); generation.setAccessible(true);
            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            CompoundTag reserved = null;
            ServerPlayer lastLoaded = null;
            for (int attempt = 0; attempt < 2; attempt++) {
                ServerPlayer rejected = new ServerPlayer(server, server.overworld(), new GameProfile(owner, "ft317_0"), ClientInformation.createDefault());
                lastLoaded = rejected;
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
                if (!record.getString("Status").equals("RESERVED_ORIGINAL_LOAD_NO_REISSUE") || !before.equals(after) || !ticket.equals(record.getCompound("Ticket"))
                    || !originalRecord.getCompound("Expected").equals(record.getCompound("Expected")) || !Arrays.equals(lineageBytes, Files.readAllBytes(lineagePath)))
                    throw new AssertionError("Inactive rejection reopened ticket or mutated lineage");
                if (reserved != null && !reserved.equals(after)) throw new AssertionError("Reserved continuation was consumed again");
                reserved = after;
            }
            var boundary = new net.farmingtales.ftboss.transaction.ExistingTransactionBoundary(java.util.Set.of(), new net.farmingtales.ftboss.transaction.FoundationApprovedRules());
            var statuePos = new net.minecraft.core.BlockPos(1, -59, 0);
            lastLoaded.moveTo(0.0, -59.0, 0.0, 0.0F, 0.0F);
            var actualWeapon = lastLoaded.getInventory().getItem(0);
            var binding = new net.farmingtales.ftboss.transaction.FourWeaponTransactions.Binding(owner,
                net.farmingtales.ftboss.item.FoodWeaponLifecycle.weaponId(actualWeapon),
                net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.registryId(actualWeapon), 0, 0,
                lastLoaded.level().dimension().location().toString(), statuePos.asLong(), boundary.statueIdentity(lastLoaded, statuePos));
            Class<?> runtime = Class.forName("net.farmingtales.ftboss.transaction.FourWeaponTransactionRuntime");
            Field authorityField = runtime.getDeclaredField("authority"); authorityField.setAccessible(true);
            var authority = (net.farmingtales.ftboss.transaction.FourWeaponTransactions)authorityField.get(null);
            ((List<ServerPlayer>)read(server.getPlayerList(), "players")).add(lastLoaded);
            ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).put(owner, lastLoaded);
            registered = lastLoaded;
            var opened = authority.open(owner, new net.farmingtales.ftboss.transaction.FourWeaponTransactions.Open(UUID.randomUUID(), binding));
            if (opened.snapshot() != null && opened.snapshot().allowed().contains(net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation.CLAIM_REPAIR))
                throw new AssertionError("Actual crash continuation reopened original CLAIM");
            if (!before.equals(readDocument.invoke(store, documentOwner, false)) || !Arrays.equals(lineageBytes, Files.readAllBytes(lineagePath))
                || lease.invoke(null, actualWeapon) != null || generation.invoke(null, actualWeapon) != null)
                throw new AssertionError("Original post-crash request admitted authority or changed reservation/lineage");
            report.put("originalPostCrashOpenDidNotReopenClaimOrAdmitAuthority", true);
            report.put("originalPostCrashOpenReplyCode", opened.code().name());
            report.put("failures", 0);
            report.put("actualNewServerBoot", true);
            report.put("originalCrashSavedTicketWholeItemAndWearCoherent", true);
            report.put("originalActiveCasSuccessorAlreadyPersistedBeforeLoad", true);
            report.put("reservedCrashContinuationAndSecondReplayGrantedNoAuthority", true);
            report.put("continuationReservedNoReissue", true);
            report.put("originalAdvancedActiveLineageBytesUnchanged", true);
            report.put("originalReservedDocumentUnchanged", true);
            report.put("activeCasSuccessorGeneration", read(lineage, "generation"));
            report.put("originalTicketGeneration", ticket.getLong("LeaseGeneration"));
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        } finally {
            if (registered != null) {
                try {
                    ((List<ServerPlayer>)read(event.getServer().getPlayerList(), "players")).remove(registered);
                    ((Map<UUID, ServerPlayer>)read(event.getServer().getPlayerList(), "playersByUUID")).remove(registered.getUUID(), registered);
                } catch (Exception failure) { report.put("failures", 1); report.put("cleanupFailure", failure.toString()); }
            }
        }
        report.put("authorityWrittenByReflection", false); report.put("authenticatedMultiplayerAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
