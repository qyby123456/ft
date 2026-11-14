package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.SparrowHitEvasionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 麻雀受击战术；2026-10-15：确认命中后短暂优先避开玩家、靠近同场主Boss，保留原治疗与终结走位。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class SparrowHitEvasion {
    private static final Map<EasterBossEntity, State> STATES = new WeakHashMap<>();
    private static final double[] TURNS = {0.0D, 0.55D, -0.55D, 1.1D, -1.1D, 1.7D, -1.7D};

    private static final class State {
        UUID attacker;
        ResourceLocation dimension;
        long last;
        long until;
        long nextPath;
        long controlled = Long.MIN_VALUE;
        Path path;
    }

    private SparrowHitEvasion() {}

    private static boolean eligible(EasterBossEntity sparrow) {
        return sparrow.level() instanceof ServerLevel && sparrow.getVariant() == EasterBossEntity.SPARROW_VARIANT
                && !sparrow.isMini() && !sparrow.isGiant() && sparrow.isAlive() && !sparrow.isRemoved()
                && !sparrow.isNoAi() && !sparrow.isInvulnerable() && !sparrow.isInvisible()
                && !sparrow.isManorIntroHidden() && !sparrow.isXuanlanSkyHidden()
                && !HiddenBossIntro.isActing(sparrow.getUUID());
    }

    private static boolean validPlayer(ServerPlayer player, EasterBossEntity sparrow) {
        return player.isAlive() && !player.isRemoved() && !player.isSpectator() && !player.isCreative()
                && player.level() == sparrow.level();
    }

    private static ServerPlayer playerOwner(Entity entity, EasterBossEntity sparrow) {
        var visited = new HashSet<UUID>();
        for (int depth = 0; entity != null && depth < 8 && visited.add(entity.getUUID()); depth++) {
            if (entity instanceof ServerPlayer player) return validPlayer(player, sparrow) ? player : null;
            Entity owner = ManorFightMembers.owner(entity);
            if (owner == entity) break;
            entity = owner;
        }
        return null;
    }

    public static void confirmedHit(EasterBossEntity sparrow, DamageSource source) {
        if (!eligible(sparrow) || source == null || source.is(DamageTypes.GENERIC_KILL)
                || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        ServerPlayer attacker = playerOwner(source.getEntity(), sparrow);
        if (attacker == null) attacker = playerOwner(source.getDirectEntity(), sparrow);
        if (attacker == null || sparrow.distanceToSqr(attacker) > SparrowHitEvasionRules.ATTACKER_RANGE * SparrowHitEvasionRules.ATTACKER_RANGE) return;
        long now = sparrow.level().getGameTime();
        State state = STATES.computeIfAbsent(sparrow, ignored -> new State());
        state.attacker = attacker.getUUID();
        state.dimension = sparrow.level().dimension().location();
        state.last = now;
        state.until = now + SparrowHitEvasionRules.REACTION_TICKS;
        state.nextPath = 0;
    }

    private static boolean validAlly(EasterBossEntity sparrow, EasterBossEntity ally) {
        return ally != null && ally != sparrow && ally.level() == sparrow.level() && ally.isAlive() && !ally.isRemoved()
                && !ally.isMini() && ally.getVariant() != EasterBossEntity.SPARROW_VARIANT && !ally.isInvisible()
                && !ally.isManorIntroHidden() && !ally.isXuanlanSkyHidden() && !HiddenBossIntro.isActing(ally.getUUID())
                && Objects.equals(ManorFight.fightId(sparrow), ManorFight.fightId(ally))
                && Objects.equals(sparrow.getManorSummonerId(), ally.getManorSummonerId())
                && sparrow.distanceToSqr(ally) <= SparrowHitEvasionRules.ALLY_RANGE * SparrowHitEvasionRules.ALLY_RANGE;
    }

    private static EasterBossEntity ally(ServerLevel level, EasterBossEntity sparrow, EasterBossEntity original) {
        UUID fight = ManorFight.fightId(sparrow);
        UUID current = fight == null ? null : ManorFight.currentBoss(fight);
        if (current != null && level.getEntity(current) instanceof EasterBossEntity boss && validAlly(sparrow, boss)) return boss;
        if (validAlly(sparrow, original)) return original;
        return level.getEntitiesOfClass(EasterBossEntity.class, sparrow.getBoundingBox().inflate(SparrowHitEvasionRules.ALLY_RANGE),
                candidate -> validAlly(sparrow, candidate)).stream().min(Comparator.comparingDouble(sparrow::distanceToSqr)).orElse(null);
    }

    private static double horizontal(Vec3 first, Vec3 second) {
        return Math.hypot(first.x - second.x, first.z - second.z);
    }

    private static double nearest(Vec3 point, List<ServerPlayer> players) {
        double distance = Double.MAX_VALUE;
        for (ServerPlayer player : players) distance = Math.min(distance, horizontal(point, player.position()));
        return distance;
    }

    private static Vec3 rotated(Vec3 direction, double turn) {
        return new Vec3(direction.x * Math.cos(turn) - direction.z * Math.sin(turn), 0,
                direction.x * Math.sin(turn) + direction.z * Math.cos(turn));
    }

    private static boolean safe(Path path, ServerLevel level, EasterBossEntity sparrow, List<ServerPlayer> players, double nearest) {
        if (path == null || !path.canReach() || path.getNodeCount() < 2) return false;
        for (int index = 0; index < path.getNodeCount(); index++) {
            var node = path.getNode(index);
            BlockPathTypes type = node.type;
            if (type != BlockPathTypes.WALKABLE && type != BlockPathTypes.OPEN
                    && type != BlockPathTypes.DOOR_OPEN && type != BlockPathTypes.WALKABLE_DOOR) return false;
            if (!level.getWorldBorder().isWithinBounds(new BlockPos(node.x, node.y, node.z))
                    || nearest(new Vec3(node.x + 0.5D, node.y, node.z + 0.5D), players) < Math.max(0.0D, Math.min(nearest, 4.0D) - 0.35D)) return false;
        }
        var end = path.getEndNode();
        if (end == null || end.type == BlockPathTypes.OPEN) return false;
        Vec3 point = new Vec3(end.x + 0.5D, end.y, end.z + 0.5D);
        return level.noCollision(sparrow, sparrow.getBoundingBox().move(point.subtract(sparrow.position())))
                && level.getFluidState(BlockPos.containing(point)).isEmpty();
    }

    public static EasterBossEntity prepare(EasterBossEntity original, EasterBossEntity sparrow) {
        State state = STATES.get(sparrow);
        if (state == null) return original;
        long now = sparrow.level().getGameTime();
        if (!eligible(sparrow) || !Objects.equals(state.dimension, sparrow.level().dimension().location())
                || !SparrowHitEvasionRules.active(now, state.last, state.until)) {
            clear(sparrow);
            return original;
        }
        state.last = now;
        state.controlled = Long.MIN_VALUE;
        if (sparrow.isPerformingSkill() || sparrow.isCastingLike() && !sparrow.castsWithoutStopping()) {
            stopOwnedPath(sparrow, state);
            return original;
        }
        ServerLevel level = (ServerLevel) sparrow.level();
        if (!(level.getEntity(state.attacker) instanceof ServerPlayer attacker) || !validPlayer(attacker, sparrow)
                || sparrow.distanceToSqr(attacker) > SparrowHitEvasionRules.ATTACKER_RANGE * SparrowHitEvasionRules.ATTACKER_RANGE) {
            clear(sparrow);
            return original;
        }
        List<ServerPlayer> players = new ArrayList<>();
        players.add(attacker);
        for (ServerPlayer player : level.players()) {
            if (player != attacker && validPlayer(player, sparrow) && player.distanceToSqr(sparrow) <= 20.0D * 20.0D) players.add(player);
        }
        EasterBossEntity boss = ally(level, sparrow, original);
        double currentNearest = nearest(sparrow.position(), players);
        double currentAlly = boss == null ? 0 : horizontal(sparrow.position(), boss.position());
        if (SparrowHitEvasionRules.settled(currentNearest, boss != null, currentAlly)) {
            clear(sparrow);
            return original;
        }
        if (now < state.nextPath) {
            if (state.path != null && sparrow.getNavigation().getPath() == state.path && !sparrow.getNavigation().isDone()) state.controlled = now;
            return original;
        }
        state.nextPath = now + SparrowHitEvasionRules.REPATH_TICKS;
        Vec3 away = sparrow.position().subtract(attacker.position()).multiply(1, 0, 1);
        if (away.lengthSqr() < 1.0E-6D) away = new Vec3(1, 0, 0);
        away = away.normalize();
        List<Vec3> points = new ArrayList<>();
        for (double turn : TURNS) points.add(sparrow.position().add(rotated(away, turn).scale(6.0D)));
        if (boss != null) {
            Vec3 toward = boss.position().subtract(sparrow.position()).multiply(1, 0, 1);
            if (toward.lengthSqr() > 1.0E-6D) points.add(sparrow.position().add(toward.normalize().scale(Math.min(7.0D, Math.max(1.0D, currentAlly - 4.0D)))));
            Vec3 cover = boss.position().subtract(attacker.position()).multiply(1, 0, 1).normalize();
            for (double turn : new double[]{0, 0.7D, -0.7D}) points.add(boss.position().add(rotated(cover, turn).scale(4.0D)));
        }
        Path best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double threatDistance = horizontal(sparrow.position(), attacker.position());
        for (Vec3 point : points) {
            if (horizontal(point, sparrow.position()) > 12.0D) continue;
            Path path = sparrow.getNavigation().createPath(BlockPos.containing(point.x, sparrow.getY(), point.z), 0);
            if (!safe(path, level, sparrow, players, currentNearest)) continue;
            var end = path.getEndNode();
            Vec3 endpoint = new Vec3(end.x + 0.5D, end.y, end.z + 0.5D);
            double score = SparrowHitEvasionRules.score(boss != null, threatDistance, currentNearest, currentAlly,
                    horizontal(endpoint, attacker.position()), nearest(endpoint, players),
                    boss == null ? 0 : horizontal(endpoint, boss.position()), horizontal(endpoint, sparrow.position()));
            if (score > bestScore) {
                best = path;
                bestScore = score;
            }
        }
        if (best != null && sparrow.getNavigation().moveTo(best, SparrowHitEvasionRules.SPEED)) {
            state.path = sparrow.getNavigation().getPath();
            state.controlled = now;
        } else stopOwnedPath(sparrow, state);
        return original;
    }

    public static boolean evadeSupport(ManorKiting original, EasterBossEntity sparrow) {
        State state = STATES.get(sparrow);
        return state != null && state.controlled == sparrow.level().getGameTime() || original.evadeSupport(sparrow);
    }

    private static void stopOwnedPath(EasterBossEntity sparrow, State state) {
        if (state.path != null && sparrow.getNavigation().getPath() == state.path) sparrow.getNavigation().stop();
        state.path = null;
    }

    private static void clear(EasterBossEntity sparrow) {
        State state = STATES.remove(sparrow);
        if (state != null) stopOwnedPath(sparrow, state);
    }

    @SubscribeEvent
    public static void leave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof EasterBossEntity sparrow && !sparrow.level().isClientSide) clear(sparrow);
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) { STATES.clear(); }
}
