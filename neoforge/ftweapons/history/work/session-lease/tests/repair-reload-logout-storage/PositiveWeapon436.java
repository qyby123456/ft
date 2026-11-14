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
            GameProfile profile = new GameProfile(owner, "ft317_0");
            ServerPlayer first = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            if (storage.load(first).isEmpty()) throw new AssertionError("Initial actual original reload missing");
            var oldWeapon = first.getInventory().getItem(0);
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var lease = guard.getDeclaredMethod("lease", Object.class); lease.setAccessible(true);
            var generation = guard.getDeclaredMethod("generation", Object.class); generation.setAccessible(true);
            Object predecessor = lease.invoke(null, oldWeapon);
            Object predecessorGeneration = generation.invoke(null, oldWeapon);
            if (predecessor == null || predecessorGeneration == null) throw new AssertionError("Initial genuine reload authority missing");
            CompoundTag expected = net.farmingtales.ftboss.item.NativeFoundationItemData.image(oldWeapon);
            net.minecraft.network.Connection localConnection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
            io.netty.channel.embedded.EmbeddedChannel localChannel = new io.netty.channel.embedded.EmbeddedChannel(localConnection);
            first.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(server,
                localConnection, first,
                net.minecraft.server.network.CommonListenerCookie.createInitial(profile, false));
            ((List<ServerPlayer>)read(server.getPlayerList(), "players")).add(first);
            ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).put(owner, first);
            registered = first;
            server.getPlayerList().remove(first);
            registered = null;
            if (server.getPlayerList().getPlayer(owner) != null || !first.isRemoved()) throw new AssertionError("Original player-list logout did not remove original player");
            Path playerFile = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(owner + ".dat");
            CompoundTag saved = NbtIo.readCompressed(playerFile, NbtAccounter.unlimitedHeap());
            CompoundTag ticket = saved.getList("Inventory", 10).getCompound(0).getCompound("FTBossRepairContinuation");
            if (ticket.getLong("LeaseGeneration") != ((Number)read(predecessor, "generation")).longValue())
                throw new AssertionError("Actual logout save did not persist current successor ticket");
            ServerPlayer rejoined = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
            if (storage.load(rejoined).isEmpty()) throw new AssertionError("Actual same-process login load missing");
            var actual = rejoined.getInventory().getItem(0);
            Object successor = lease.invoke(null, actual);
            if (successor == null || generation.invoke(null, actual) == null || successor == predecessor
                || !expected.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                || !read(successor, "incarnation").equals(read(predecessor, "incarnation"))
                || ((Number)read(successor, "generation")).longValue() != ((Number)read(predecessor, "generation")).longValue() + 1L)
                throw new AssertionError("Actual logout/login lost exact item or genuine successor lineage");
            Class<?> storeType = successor.getClass().getEnclosingClass();
            var locallyLive = storeType.getDeclaredMethod("locallyLive", successor.getClass()); locallyLive.setAccessible(true);
            Object oldInput = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)oldWeapon).ftboss$ownerNativeWeaponInput();
            if (Boolean.TRUE.equals(locallyLive.invoke(null, predecessor)) || !Boolean.TRUE.equals(locallyLive.invoke(null, successor))
                || (oldInput != null && !Boolean.TRUE.equals(read(oldInput, "retired"))))
                throw new AssertionError("Same-process rejoin retained old physical repair authority");
            report.put("failures", 0);
            report.put("originalPlayerListLogoutRan", true);
            report.put("originalLogoutSaveThenSameProcessOriginalLoadRestoredGenuineSuccessor", true);
            report.put("sameEpochAndWholeItemPreserved", true);
            report.put("oldPhysicalLeaseAndInputRetiredAfterSuccessor", true);
            report.put("originalLogoutSavedGeneration", ticket.getLong("LeaseGeneration"));
            report.put("sameProcessSuccessorGeneration", read(successor, "generation"));
            report.put("worldJoinAndNegotiatedClientLoginNotExercised", true);
            localChannel.finishAndReleaseAll();
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
        report.put("scratchPlayerNotAuthenticated", true); report.put("authorityWrittenByReflection", false);
        report.put("authenticatedMultiplayerAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
