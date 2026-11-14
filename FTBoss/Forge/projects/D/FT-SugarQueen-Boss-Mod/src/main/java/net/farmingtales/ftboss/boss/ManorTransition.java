package net.farmingtales.ftboss.boss;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.ManorTransitionPacket;
import net.farmingtales.ftboss.util.ManorCeremonyGeometry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ManorTransition {
    private record Show(ResourceLocation dimension, Vec3 center, int variant, long started) {}
    private static final Map<UUID, Show> ACTIVE = new HashMap<>();
    private ManorTransition() {}

    public static void prepare(ServerLevel level, UUID fight, Vec3 center, int variant, int remaining) {
        Show previous = ACTIVE.get(fight);
        if (previous != null && level.getGameTime() - previous.started < ManorCeremonyGeometry.SWITCH_TICKS) return;
        int age = Math.max(0, Math.min(ManorCeremonyGeometry.SWITCH_TICKS - 1, ManorCeremonyGeometry.SWITCH_TICKS - remaining));
        ACTIVE.put(fight, new Show(level.dimension().location(), center, variant, level.getGameTime() - age));
        send(level, snapshot(level, fight));
    }

    public static void arrive(ServerLevel level, UUID fight, Vec3 center, int variant) {
        ACTIVE.put(fight, new Show(level.dimension().location(), center, variant, level.getGameTime() - ManorCeremonyGeometry.SWITCH_TICKS));
        send(level, snapshot(level, fight));
    }

    public static ManorTransitionPacket snapshot(ServerLevel level, UUID fight) {
        Show show = ACTIVE.get(fight);
        if (show == null || !show.dimension.equals(level.dimension().location())) return null;
        long age = level.getGameTime() - show.started;
        if (age < 0 || age >= ManorCeremonyGeometry.TRANSITION_DURATION) return null;
        return new ManorTransitionPacket(fight, show.dimension, show.center, show.variant, (int) age, true);
    }

    public static void cancel(ServerLevel level, UUID fight) {
        Show show = ACTIVE.remove(fight);
        if (show != null) send(level, new ManorTransitionPacket(fight, show.dimension, show.center, show.variant, 0, false));
    }

    private static void send(ServerLevel level, ManorTransitionPacket packet) {
        if (packet == null || !packet.valid()) return;
        for (var player : level.players()) {
            if (player instanceof FakePlayer || player.connection == null || player.connection.connection.channel() == null) continue;
            if (packet.active() && player.distanceToSqr(packet.center()) > 128 * 128) continue;
            FTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) return;
        for (var level : event.getServer().getAllLevels()) for (UUID fight : java.util.List.copyOf(ACTIVE.keySet())) {
            Show show = ACTIVE.get(fight);
            if (show == null || !show.dimension.equals(level.dimension().location())) continue;
            var packet = snapshot(level, fight);
            if (packet == null || !ManorFight.hasActive(fight)) cancel(level, fight);
            else if (level.getGameTime() % 20 == 0) send(level, packet);
        }
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) { ACTIVE.clear(); }
}
