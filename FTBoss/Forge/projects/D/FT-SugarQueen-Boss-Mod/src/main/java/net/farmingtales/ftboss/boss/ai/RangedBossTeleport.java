package net.farmingtales.ftboss.boss.ai;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class RangedBossTeleport {
    private static final int HIT_COOLDOWN_TICKS = 20;
    private static final int MOVE_COOLDOWN_TICKS = 40;
    private static final int RETRY_TICKS = 10;
    private static final int REACTION_TICKS = 60;
    private static final double NEAR_DISTANCE = 8.0;
    private static final double FAR_DISTANCE = 16.0;
    private static final double MAX_STEP = 8.0;
    private static final double[] TURNS = {0.0, 0.45, -0.45, 0.9, -0.9, 1.35, -1.35};
    private static final Map<EasterBossEntity, State> STATES = new WeakHashMap<>();

    private static final class State {
        UUID attacker;
        long reactionUntil;
        long nextHit;
        long nextMove;
        boolean fallback;
    }

    private RangedBossTeleport() {}

    private static boolean eligible(EasterBossEntity boss) {
        return (boss.getVariant() == 6 || boss.getVariant() == EasterBossEntity.QIZAI_VARIANT)
                && boss.level() instanceof ServerLevel && boss.isAlive() && !boss.isRemoved()
                && !boss.isMini() && !boss.isGiant() && !boss.isNoAi() && !boss.isInvulnerable()
                && !boss.isInvisible() && !boss.isManorIntroHidden() && !boss.isXuanlanSkyHidden()
                && !HiddenBossIntro.isActing(boss.getUUID());
    }

    private static boolean valid(ServerPlayer player, EasterBossEntity boss) {
        return BossTargets.valid(player) && player.level() == boss.level() && boss.distanceToSqr(player) <= 64.0 * 64.0;
    }

    private static ServerPlayer owner(Entity entity, EasterBossEntity boss) {
        var visited = new HashSet<UUID>();
        for (int depth = 0; entity != null && depth < 8 && visited.add(entity.getUUID()); depth++) {
            if (entity instanceof ServerPlayer player) return valid(player, boss) ? player : null;
            entity = ManorFightMembers.owner(entity);
        }
        return null;
    }

    public static void confirmedHit(EasterBossEntity boss, DamageSource source) {
        if (!eligible(boss) || source == null || source.is(DamageTypes.GENERIC_KILL)
                || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        ServerPlayer attacker = owner(source.getEntity(), boss);
        if (attacker == null) attacker = owner(source.getDirectEntity(), boss);
        if (attacker == null) return;
        State state = STATES.computeIfAbsent(boss, ignored -> new State());
        state.attacker = attacker.getUUID();
        state.reactionUntil = boss.level().getGameTime() + REACTION_TICKS;
    }

    public static boolean tick(EasterBossEntity boss, LivingEntity target) {
        if (!eligible(boss)) {
            STATES.remove(boss);
            return false;
        }
        if (!boss.castsWithoutStopping() || !(target instanceof ServerPlayer player) || !valid(player, boss)) return false;
        ServerLevel level = (ServerLevel) boss.level();
        long now = level.getGameTime();
        State state = STATES.computeIfAbsent(boss, ignored -> new State());
        Entity resolved = state.attacker == null ? null : level.getPlayerByUUID(state.attacker);
        ServerPlayer attacker = resolved instanceof ServerPlayer found ? found : null;
        if (attacker == null || !valid(attacker, boss) || now >= state.reactionUntil) state.attacker = null;
        if (state.attacker != null && now >= state.nextHit) {
            state.nextHit = now + RETRY_TICKS;
            if (blink(level, boss, attacker, true)) {
                state.attacker = null;
                state.nextHit = now + HIT_COOLDOWN_TICKS;
                state.nextMove = now + MOVE_COOLDOWN_TICKS;
                state.fallback = false;
                return true;
            }
        }
        if (now < state.nextMove) {
            if (!state.fallback) boss.getNavigation().stop();
            return !state.fallback;
        }
        boolean retreat = horizontalDistance(boss.position(), player.position()) < NEAR_DISTANCE * NEAR_DISTANCE;
        boolean moved = blink(level, boss, player, retreat);
        state.fallback = !moved;
        state.nextMove = now + (moved ? MOVE_COOLDOWN_TICKS : RETRY_TICKS);
        return moved;
    }

    private static double horizontalDistance(Vec3 first, Vec3 second) {
        return first.subtract(second).horizontalDistanceSqr();
    }

    private static boolean blink(ServerLevel level, EasterBossEntity boss, ServerPlayer player, boolean retreat) {
        Vec3 origin = boss.position();
        Vec3 target = player.position();
        double distance = Math.sqrt(horizontalDistance(origin, target));
        Vec3 away = origin.subtract(target).multiply(1, 0, 1);
        if (away.lengthSqr() < 1.0E-6) away = new Vec3(1, 0, 0);
        away = away.normalize();
        double radius = retreat ? Math.max(10.0, distance + 4.0) : 12.0;
        double direction = (boss.getUUID().getLeastSignificantBits() & 1L) == 0 ? 1 : -1;
        double turn = retreat || distance > FAR_DISTANCE ? 0.0 : 0.5 * direction;
        for (double offset : TURNS) {
            double angle = turn + offset * direction;
            Vec3 radial = new Vec3(away.x * Math.cos(angle) - away.z * Math.sin(angle), 0,
                    away.x * Math.sin(angle) + away.z * Math.cos(angle));
            Vec3 preferred = target.add(radial.scale(radius));
            Vec3 step = preferred.subtract(origin).multiply(1, 0, 1);
            if (step.lengthSqr() > MAX_STEP * MAX_STEP) preferred = origin.add(step.normalize().scale(MAX_STEP));
            Vec3 spot = ManorSafeTeleport.find(level, boss, new Vec3(preferred.x, origin.y, preferred.z), 1);
            if (spot == null || !acceptable(level, boss, player, origin, spot, retreat)) continue;
            ManorSafeTeleport.move(level, boss, spot);
            level.sendParticles(ParticleTypes.PORTAL, origin.x, origin.y + 1, origin.z, 16, 0.3, 0.6, 0.3, 0.05);
            level.sendParticles(ParticleTypes.PORTAL, spot.x, spot.y + 1, spot.z, 16, 0.3, 0.6, 0.3, 0.05);
            level.playSound(null, boss.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.7F, 1.3F);
            return true;
        }
        return false;
    }

    private static boolean acceptable(ServerLevel level, EasterBossEntity boss, ServerPlayer player,
            Vec3 origin, Vec3 spot, boolean retreat) {
        double step = horizontalDistance(origin, spot);
        double before = horizontalDistance(origin, player.position());
        double after = horizontalDistance(spot, player.position());
        if (step < 4.0 || step > MAX_STEP * MAX_STEP || Math.abs(spot.y - origin.y) > 3.0
                || after < NEAR_DISTANCE * NEAR_DISTANCE || retreat && after <= before + 4.0
                || !retreat && before > FAR_DISTANCE * FAR_DISTANCE && after >= before) return false;
        for (ServerPlayer other : level.players()) {
            if (other != player && valid(other, boss) && horizontalDistance(spot, other.position()) < NEAR_DISTANCE * NEAR_DISTANCE
                    && horizontalDistance(spot, other.position()) < horizontalDistance(origin, other.position())) return false;
        }
        return true;
    }
}
