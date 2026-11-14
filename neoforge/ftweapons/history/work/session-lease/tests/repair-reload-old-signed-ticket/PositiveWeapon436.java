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
            byte[] formerlySignedBytes = Files.readAllBytes(playerFile);
            CompoundTag formerlySignedTicket = NbtIo.readCompressed(playerFile, NbtAccounter.unlimitedHeap())
                .getList("Inventory", 10).getCompound(0).getCompound("FTBossRepairContinuation");
            CompoundTag formerlySignedDocument = (CompoundTag)readDocument.invoke(store, documentOwner, false);
            CompoundTag formerlySignedRecord = formerlySignedDocument.getCompound("Continuations").getCompound(formerlySignedTicket.getUUID("Weapon").toString());
            if (!formerlySignedDocument.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")
                || !formerlySignedRecord.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")
                || !formerlySignedTicket.equals(formerlySignedRecord.getCompound("Ticket")))
                throw new AssertionError("Former save was never an actual authenticated committed continuation");
            storage.save(restored);
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
            if (formerlySignedTicket.getUUID("Save").equals(ticket.getUUID("Save"))) throw new AssertionError("Two real saves did not rotate the original ticket");
            try {
                Files.write(playerFile, formerlySignedBytes);
                ServerPlayer stale = new ServerPlayer(server, server.overworld(), new GameProfile(owner, "ft317_0"), ClientInformation.createDefault());
                if (storage.load(stale).isEmpty()) throw new AssertionError("Original stale signed load did not run");
                var staleWeapon = stale.getInventory().getItem(0);
                if (lease.invoke(null, staleWeapon) != null || generation.invoke(null, staleWeapon) != null
                    || !image.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(staleWeapon))
                    || !document.equals(readDocument.invoke(store, documentOwner, false)) || lease.invoke(null, actual) != genuine)
                    throw new AssertionError("Formerly signed save reissued authority or consumed the latest committed ticket");
            } finally {
                Files.write(playerFile, savedBytes);
            }
            Class<?> curioItem = Class.forName("top.theillusivec4.curios.api.type.capability.ICurioItem");
            Object changedProvider = java.lang.reflect.Proxy.newProxyInstance(curioItem.getClassLoader(), new Class<?>[]{curioItem},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                    if (method.getName().equals("equals")) return proxy == arguments[0];
                    if (method.getName().equals("toString")) return "Saved reload provider revocation input";
                    if (method.getReturnType() == boolean.class) return false;
                    return null;
                });
            Class<?> hooks = Class.forName("top.theillusivec4.curios.mixin.CuriosImplMixinHooks");
            var registryField = hooks.getDeclaredField("REGISTRY"); registryField.setAccessible(true);
            var registry = (Map<Object, Object>)registryField.get(null);
            if (registry.containsKey(actual.getItem())) throw new AssertionError("Original provider registry not inert");
            registry.put(actual.getItem(), changedProvider);
            registry.remove(actual.getItem(), changedProvider);
            var locallyLive = storeType.getDeclaredMethod("locallyLive", genuine.getClass()); locallyLive.setAccessible(true);
            Object input = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
            if (Boolean.TRUE.equals(locallyLive.invoke(null, genuine)) || (input != null && !Boolean.TRUE.equals(read(input, "retired"))))
                throw new AssertionError("Actual provider mutation failed to retire restored authority");
            if (!document.equals(readDocument.invoke(store, documentOwner, false)) || !Arrays.equals(savedBytes, Files.readAllBytes(playerFile))
                || !image.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual)))
                throw new AssertionError("Revocation changed committed ticket or saved whole item");
            report.put("failures", 0);
            report.put("actualOriginalReloadNonNullLease", true);
            report.put("newOriginalSaveCommittedBeforeRevocation", true);
            report.put("rawProviderAbaRetiredActualReloadedLeaseAndInput", true);
            report.put("validCommittedContinuationAndWholeItemPreservedAfterRevocation", true);
            report.put("committedSavedGeneration", ticket.getLong("LeaseGeneration"));
            report.put("twoRealOriginalSavesRotatedSignedTicket", true);
            report.put("formerlyValidSignedSaveDeniedWithoutConsumingLatestContinuation", true);
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
