package net.farmingtales.ftboss.boss.ai;

import java.util.HashMap;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class DandanHitTeleport {
    public static final double RADIUS = 3.0;
    private static final Map<ServerPlayer, Pending> PENDING = new HashMap<>();

    private record Pending(ServerLevel level, Vec3 origin, float health) {}

    private DandanHitTeleport() {}

    private static boolean isDandan(Entity source) {
        for (int depth = 0; source != null && depth < 8; depth++) {
            if (source instanceof Player) return false;
            if (source instanceof EasterBossEntity boss) {
                return boss.getVariant() == EasterBossEntity.DANDAN_VARIANT && !boss.isMini();
            }
            Entity owner = ManorFightMembers.owner(source);
            if (owner == source) return false;
            source = owner;
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !BossTargets.valid(player)
                || !Float.isFinite(event.getAmount()) || event.getAmount() <= 0) return;
        Entity source = event.getSource().getEntity();
        if (source == null) source = event.getSource().getDirectEntity();
        if (!isDandan(source)) return;
        PENDING.putIfAbsent(player, new Pending(player.serverLevel(), player.position(), player.getHealth()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) return;
        var hits = new HashMap<>(PENDING);
        PENDING.clear();
        for (var entry : hits.entrySet()) {
            ServerPlayer player = entry.getKey();
            Pending pending = entry.getValue();
            if (!BossTargets.valid(player) || player.serverLevel() != pending.level()
                    || player.getHealth() >= pending.health()
                    || player.position().distanceToSqr(pending.origin()) > RADIUS * RADIUS) continue;
            teleport(player);
        }
    }

    public static boolean teleport(ServerPlayer player) {
        if (!BossTargets.valid(player)) return false;
        ServerLevel level = player.serverLevel();
        Vec3 destination = ManorSafeTeleport.nearby(level, player, RADIUS);
        if (destination == null) return false;
        Vec3 origin = player.position();
        ManorSafeTeleport.move(level, player, destination);
        level.sendParticles(ParticleTypes.PORTAL, origin.x, origin.y + 0.8, origin.z, 16, 0.3, 0.5, 0.3, 0.1);
        level.sendParticles(ParticleTypes.PORTAL, destination.x, destination.y + 0.8, destination.z, 16, 0.3, 0.5, 0.3, 0.1);
        level.playSound(null, destination.x, destination.y, destination.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.7F, 1.2F);
        return true;
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING.clear();
    }
}
