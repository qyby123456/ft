/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.common.util.FakePlayer
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 */
package net.farmingtales.ftboss.boss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.ManorTransitionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ManorTransition {
    private static final Map<UUID, Show> ACTIVE = new HashMap<UUID, Show>();

    private ManorTransition() {
    }

    public static void prepare(ServerLevel level, UUID fight, Vec3 center, int variant, int remaining) {
        Show previous = ACTIVE.get(fight);
        if (previous != null && level.m_46467_() - previous.started < 60L) {
            return;
        }
        int age = Math.max(0, Math.min(59, 60 - remaining));
        ACTIVE.put(fight, new Show(level.m_46472_().m_135782_(), center, variant, level.m_46467_() - (long)age));
        ManorTransition.send(level, ManorTransition.snapshot(level, fight));
    }

    public static void arrive(ServerLevel level, UUID fight, Vec3 center, int variant) {
        ACTIVE.put(fight, new Show(level.m_46472_().m_135782_(), center, variant, level.m_46467_() - 60L));
        ManorTransition.send(level, ManorTransition.snapshot(level, fight));
    }

    public static ManorTransitionPacket snapshot(ServerLevel level, UUID fight) {
        Show show = ACTIVE.get(fight);
        if (show == null || !show.dimension.equals((Object)level.m_46472_().m_135782_())) {
            return null;
        }
        long age = level.m_46467_() - show.started;
        if (age < 0L || age >= 90L) {
            return null;
        }
        return new ManorTransitionPacket(fight, show.dimension, show.center, show.variant, (int)age, true);
    }

    public static void cancel(ServerLevel level, UUID fight) {
        Show show = ACTIVE.remove(fight);
        if (show != null) {
            ManorTransition.send(level, new ManorTransitionPacket(fight, show.dimension, show.center, show.variant, 0, false));
        }
    }

    private static void send(ServerLevel level, ManorTransitionPacket packet) {
        if (packet == null || !packet.valid()) {
            return;
        }
        for (ServerPlayer player : level.m_6907_()) {
            if (player instanceof FakePlayer || player.f_8906_ == null || player.f_8906_.f_9742_.channel() == null || packet.active() && player.m_20238_(packet.center()) > 16384.0) continue;
            FTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)packet);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) {
            return;
        }
        for (ServerLevel level : event.getServer().m_129785_()) {
            for (UUID fight : List.copyOf(ACTIVE.keySet())) {
                Show show = ACTIVE.get(fight);
                if (show == null || !show.dimension.equals((Object)level.m_46472_().m_135782_())) continue;
                ManorTransitionPacket packet = ManorTransition.snapshot(level, fight);
                if (packet == null || !ManorFight.hasActive(fight)) {
                    ManorTransition.cancel(level, fight);
                    continue;
                }
                if (level.m_46467_() % 20L != 0L) continue;
                ManorTransition.send(level, packet);
            }
        }
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) {
        ACTIVE.clear();
    }

    private record Show(ResourceLocation dimension, Vec3 center, int variant, long started) {
    }
}

