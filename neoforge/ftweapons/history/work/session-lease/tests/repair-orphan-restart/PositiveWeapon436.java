package ftboss.storage308;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
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
        ServerPlayer restored = null;
        try {
            var server = event.getServer();
            UUID owner = new UUID(334, 1);
            GameProfile profile = new GameProfile(owner, "ft317_0");
            restored = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            if (storage.load(restored).isEmpty()) throw new AssertionError("Copied original player save failed to load on actual new server boot");
            ItemStack actual = restored.getInventory().getItem(0);
            Class<?> storeType = Class.forName("net.farmingtales.ftboss.item.OwnerLineageRevocationStore");
            var openStore = storeType.getDeclaredMethod("open", net.minecraft.server.MinecraftServer.class);
            openStore.setAccessible(true);
            Object store = openStore.invoke(null, server);
            UUID journalOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-journal/v1/" + owner).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            var readDocument = storeType.getDeclaredMethod("readRepairDocument", UUID.class, boolean.class);
            readDocument.setAccessible(true);
            CompoundTag document = (CompoundTag)readDocument.invoke(store, journalOwner, false);
            CompoundTag tasks = document.getCompound("Tasks");
            if (tasks.getAllKeys().size() != 1) throw new AssertionError("Actual restarted signed task missing");
            CompoundTag task = tasks.getCompound(tasks.getAllKeys().iterator().next());
            if (!task.getString("Status").equals("LEASE_ISSUED") || task.getLong("Sequence") != 5L)
                throw new AssertionError("Expected genuine terminal claim journal not recovered");
            CompoundTag image = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
            if (task.getCompound("Expected").equals(image)) throw new AssertionError("Orphan fixture unexpectedly contains persisted claimed weapon");
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var lease = guard.getDeclaredMethod("lease", Object.class); lease.setAccessible(true);
            var generation = guard.getDeclaredMethod("generation", Object.class); generation.setAccessible(true);
            if (lease.invoke(null, actual) != null || generation.invoke(null, actual) != null)
                throw new AssertionError("Restarted orphan journal manufactured a repair lease or generation");
            var boundary = new net.farmingtales.ftboss.transaction.ExistingTransactionBoundary(java.util.Set.of(), new net.farmingtales.ftboss.transaction.FoundationApprovedRules());
            var statuePos = new net.minecraft.core.BlockPos(1, -59, 0);
            restored.moveTo(0.0, -59.0, 0.0, 0.0F, 0.0F);
            var binding = new net.farmingtales.ftboss.transaction.FourWeaponTransactions.Binding(owner,
                net.farmingtales.ftboss.item.FoodWeaponLifecycle.weaponId(actual),
                net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.registryId(actual), 0, 0,
                restored.level().dimension().location().toString(), statuePos.asLong(), boundary.statueIdentity(restored, statuePos));
            Class<?> runtime = Class.forName("net.farmingtales.ftboss.transaction.FourWeaponTransactionRuntime");
            Field authorityField = runtime.getDeclaredField("authority"); authorityField.setAccessible(true);
            var authority = (net.farmingtales.ftboss.transaction.FourWeaponTransactions)authorityField.get(null);
            ((List<ServerPlayer>)read(server.getPlayerList(), "players")).add(restored);
            ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).put(owner, restored);
            var opened = authority.open(owner, new net.farmingtales.ftboss.transaction.FourWeaponTransactions.Open(UUID.randomUUID(), binding));
            if (opened.snapshot() != null && opened.snapshot().allowed().contains(net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation.CLAIM_REPAIR))
                throw new AssertionError("Restarted orphan made CLAIM available again");
            if (!document.equals(readDocument.invoke(store, journalOwner, false)) || lease.invoke(null, actual) != null)
                throw new AssertionError("Orphan inspection changed signed terminal or reissued lease");
            report.put("failures", 0);
            report.put("actualNewServerBoot", true);
            report.put("signedJournalStatus", task.getString("Status"));
            report.put("signedJournalSequence", task.getLong("Sequence"));
            report.put("stalePersistedWeaponDiffersFromClaim", true);
            report.put("orphanIssuedJournalDidNotReissueAuthority", true);
            report.put("claimNotAvailableFromOrphan", true);
            report.put("openReplyCode", opened.code().name());
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        } finally {
            if (restored != null) {
                try {
                    ((List<ServerPlayer>)read(event.getServer().getPlayerList(), "players")).remove(restored);
                    ((Map<UUID, ServerPlayer>)read(event.getServer().getPlayerList(), "playersByUUID")).remove(restored.getUUID(), restored);
                } catch (Exception failure) { report.put("failures", 1); report.put("cleanupFailure", failure.toString()); }
            }
        }
        report.put("applicationAuthorityWrittenByReflection", false);
        report.put("authenticatedMultiplayerAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
