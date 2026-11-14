/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.HashMap;
import java.util.Map;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.dandannativelights.LightHooks;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class DandanHitTeleport {
    public static final double RADIUS = 3.0;
    private static final Map<ServerPlayer, Pending> PENDING = new HashMap<ServerPlayer, Pending>();

    private DandanHitTeleport() {
    }

    private static boolean isDandan(Entity source) {
        for (int depth = 0; source != null && depth < 8; ++depth) {
            if (source instanceof Player) {
                return false;
            }
            if (source instanceof EasterBossEntity) {
                EasterBossEntity boss = (EasterBossEntity)source;
                return boss.getVariant() == 1 && !boss.isMini();
            }
            Entity owner = ManorFightMembers.owner(source);
            if (owner == source) {
                return false;
            }
            source = owner;
        }
        return false;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent event) {
        ServerPlayer player;
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer) || !BossTargets.valid((Player)(player = (ServerPlayer)livingEntity)) || !Float.isFinite(event.getAmount()) || event.getAmount() <= 0.0f) {
            return;
        }
        Entity source = event.getSource().m_7639_();
        if (source == null) {
            source = event.getSource().m_7640_();
        }
        if (!DandanHitTeleport.isDandan(source)) {
            return;
        }
        PENDING.putIfAbsent(player, new Pending(player.m_284548_(), player.m_20182_(), player.m_21223_()));
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) {
            return;
        }
        HashMap<ServerPlayer, Pending> hits = new HashMap<ServerPlayer, Pending>(PENDING);
        PENDING.clear();
        for (Map.Entry<ServerPlayer, Pending> entry : hits.entrySet()) {
            ServerPlayer player = entry.getKey();
            Pending pending = entry.getValue();
            if (!BossTargets.valid((Player)player) || player.m_284548_() != pending.level() || player.m_21223_() >= pending.health() || player.m_20182_().m_82557_(pending.origin()) > 9.0) continue;
            DandanHitTeleport.teleport(player);
        }
    }

    public static boolean teleport(ServerPlayer player) {
        if (!BossTargets.valid((Player)player)) {
            return false;
        }
        ServerLevel level = player.m_284548_();
        Vec3 destination = ManorSafeTeleport.nearby(level, player, 3.0);
        if (destination == null) {
            return false;
        }
        Vec3 origin = player.m_20182_();
        LightHooks.move(level, (Entity)player, destination);
        level.m_8767_((ParticleOptions)ParticleTypes.f_123760_, origin.f_82479_, origin.f_82480_ + 0.8, origin.f_82481_, 16, 0.3, 0.5, 0.3, 0.1);
        level.m_8767_((ParticleOptions)ParticleTypes.f_123760_, destination.f_82479_, destination.f_82480_ + 0.8, destination.f_82481_, 16, 0.3, 0.5, 0.3, 0.1);
        level.m_6263_(null, destination.f_82479_, destination.f_82480_, destination.f_82481_, SoundEvents.f_11852_, SoundSource.HOSTILE, 0.7f, 1.2f);
        return true;
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING.clear();
    }

    private record Pending(ServerLevel level, Vec3 origin, float health) {
    }
}

