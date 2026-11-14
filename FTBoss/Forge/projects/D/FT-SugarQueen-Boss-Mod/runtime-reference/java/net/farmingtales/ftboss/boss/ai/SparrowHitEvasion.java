/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.pathfinder.BlockPathTypes
 *  net.minecraft.world.level.pathfinder.Node
 *  net.minecraft.world.level.pathfinder.Path
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.EntityLeaveLevelEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.ManorKiting;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.SparrowHitEvasionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class SparrowHitEvasion {
    private static final Map<EasterBossEntity, State> STATES = new WeakHashMap<EasterBossEntity, State>();
    private static final double[] TURNS = new double[]{0.0, 0.55, -0.55, 1.1, -1.1, 1.7, -1.7};

    private SparrowHitEvasion() {
    }

    private static boolean eligible(EasterBossEntity sparrow) {
        return sparrow.m_9236_() instanceof ServerLevel && sparrow.getVariant() == 9 && !sparrow.isMini() && !sparrow.isGiant() && sparrow.m_6084_() && !sparrow.m_213877_() && !sparrow.m_21525_() && !sparrow.m_20147_() && !sparrow.m_20145_() && !sparrow.isManorIntroHidden() && !sparrow.isXuanlanSkyHidden() && !HiddenBossIntro.isActing(sparrow.m_20148_());
    }

    private static boolean validPlayer(ServerPlayer player, EasterBossEntity sparrow) {
        return player.m_6084_() && !player.m_213877_() && !player.m_5833_() && !player.m_7500_() && player.m_9236_() == sparrow.m_9236_();
    }

    private static ServerPlayer playerOwner(Entity entity, EasterBossEntity sparrow) {
        HashSet<UUID> visited = new HashSet<UUID>();
        for (int depth = 0; entity != null && depth < 8 && visited.add(entity.m_20148_()); ++depth) {
            if (entity instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer)entity;
                return SparrowHitEvasion.validPlayer(player, sparrow) ? player : null;
            }
            Entity owner = ManorFightMembers.owner(entity);
            if (owner == entity) break;
            entity = owner;
        }
        return null;
    }

    public static void confirmedHit(EasterBossEntity sparrow, DamageSource source) {
        if (!SparrowHitEvasion.eligible(sparrow) || source == null || source.m_276093_(DamageTypes.f_286979_) || source.m_269533_(DamageTypeTags.f_268738_)) {
            return;
        }
        ServerPlayer attacker = SparrowHitEvasion.playerOwner(source.m_7639_(), sparrow);
        if (attacker == null) {
            attacker = SparrowHitEvasion.playerOwner(source.m_7640_(), sparrow);
        }
        if (attacker == null || sparrow.m_20280_((Entity)attacker) > 4096.0) {
            return;
        }
        long now = sparrow.m_9236_().m_46467_();
        State state = STATES.computeIfAbsent(sparrow, ignored -> new State());
        state.attacker = attacker.m_20148_();
        state.dimension = sparrow.m_9236_().m_46472_().m_135782_();
        state.last = now;
        state.until = now + 60L;
        state.nextPath = 0L;
    }

    private static boolean validAlly(EasterBossEntity sparrow, EasterBossEntity ally) {
        return ally != null && ally != sparrow && ally.m_9236_() == sparrow.m_9236_() && ally.m_6084_() && !ally.m_213877_() && !ally.isMini() && ally.getVariant() != 9 && !ally.m_20145_() && !ally.isManorIntroHidden() && !ally.isXuanlanSkyHidden() && !HiddenBossIntro.isActing(ally.m_20148_()) && Objects.equals(ManorFight.fightId((Entity)sparrow), ManorFight.fightId((Entity)ally)) && Objects.equals(sparrow.getManorSummonerId(), ally.getManorSummonerId()) && sparrow.m_20280_((Entity)ally) <= 2304.0;
    }

    private static EasterBossEntity ally(ServerLevel level, EasterBossEntity sparrow, EasterBossEntity original) {
        EasterBossEntity boss;
        Entity entity;
        UUID current;
        UUID fight = ManorFight.fightId((Entity)sparrow);
        UUID uUID = current = fight == null ? null : ManorFight.currentBoss(fight);
        if (current != null && (entity = level.m_8791_(current)) instanceof EasterBossEntity && SparrowHitEvasion.validAlly(sparrow, boss = (EasterBossEntity)entity)) {
            return boss;
        }
        if (SparrowHitEvasion.validAlly(sparrow, original)) {
            return original;
        }
        return level.m_6443_(EasterBossEntity.class, sparrow.m_20191_().m_82400_(48.0), candidate -> SparrowHitEvasion.validAlly(sparrow, candidate)).stream().min(Comparator.comparingDouble(arg_0 -> ((EasterBossEntity)sparrow).m_20280_(arg_0))).orElse(null);
    }

    private static double horizontal(Vec3 first, Vec3 second) {
        return Math.hypot(first.f_82479_ - second.f_82479_, first.f_82481_ - second.f_82481_);
    }

    private static double nearest(Vec3 point, List<ServerPlayer> players) {
        double distance = Double.MAX_VALUE;
        for (ServerPlayer player : players) {
            distance = Math.min(distance, SparrowHitEvasion.horizontal(point, player.m_20182_()));
        }
        return distance;
    }

    private static Vec3 rotated(Vec3 direction, double turn) {
        return new Vec3(direction.f_82479_ * Math.cos(turn) - direction.f_82481_ * Math.sin(turn), 0.0, direction.f_82479_ * Math.sin(turn) + direction.f_82481_ * Math.cos(turn));
    }

    private static boolean safe(Path path, ServerLevel level, EasterBossEntity sparrow, List<ServerPlayer> players, double nearest) {
        if (path == null || !path.m_77403_() || path.m_77398_() < 2) {
            return false;
        }
        for (int index = 0; index < path.m_77398_(); ++index) {
            Node node = path.m_77375_(index);
            BlockPathTypes type = node.f_77282_;
            if (type != BlockPathTypes.WALKABLE && type != BlockPathTypes.OPEN && type != BlockPathTypes.DOOR_OPEN && type != BlockPathTypes.WALKABLE_DOOR) {
                return false;
            }
            if (level.m_6857_().m_61937_(new BlockPos(node.f_77271_, node.f_77272_, node.f_77273_))) {
                Vec3 vec3 = new Vec3((double)node.f_77271_ + 0.5, (double)node.f_77272_, (double)node.f_77273_ + 0.5);
                if (!(SparrowHitEvasion.nearest(vec3, players) < Math.max(0.0, Math.min(nearest, 4.0) - 0.35))) continue;
            }
            return false;
        }
        Node end = path.m_77395_();
        if (end == null || end.f_77282_ == BlockPathTypes.OPEN) {
            return false;
        }
        Vec3 point = new Vec3((double)end.f_77271_ + 0.5, (double)end.f_77272_, (double)end.f_77273_ + 0.5);
        return level.m_45756_((Entity)sparrow, sparrow.m_20191_().m_82383_(point.m_82546_(sparrow.m_20182_()))) && level.m_6425_(BlockPos.m_274446_((Position)point)).m_76178_();
    }

    public static EasterBossEntity prepare(EasterBossEntity original, EasterBossEntity sparrow) {
        double currentAlly;
        ServerPlayer attacker;
        State state = STATES.get(sparrow);
        if (state == null) {
            return original;
        }
        long now = sparrow.m_9236_().m_46467_();
        if (!(SparrowHitEvasion.eligible(sparrow) && Objects.equals(state.dimension, sparrow.m_9236_().m_46472_().m_135782_()) && SparrowHitEvasionRules.active(now, state.last, state.until))) {
            SparrowHitEvasion.clear(sparrow);
            return original;
        }
        state.last = now;
        state.controlled = Long.MIN_VALUE;
        if (sparrow.isPerformingSkill() || sparrow.isCastingLike() && !sparrow.castsWithoutStopping()) {
            SparrowHitEvasion.stopOwnedPath(sparrow, state);
            return original;
        }
        ServerLevel level = (ServerLevel)sparrow.m_9236_();
        Entity entity = level.m_8791_(state.attacker);
        if (!(entity instanceof ServerPlayer) || !SparrowHitEvasion.validPlayer(attacker = (ServerPlayer)entity, sparrow) || sparrow.m_20280_((Entity)attacker) > 4096.0) {
            SparrowHitEvasion.clear(sparrow);
            return original;
        }
        ArrayList<ServerPlayer> players = new ArrayList<ServerPlayer>();
        players.add(attacker);
        for (ServerPlayer player : level.m_6907_()) {
            if (player == attacker || !SparrowHitEvasion.validPlayer(player, sparrow) || !(player.m_20280_((Entity)sparrow) <= 400.0)) continue;
            players.add(player);
        }
        EasterBossEntity boss = SparrowHitEvasion.ally(level, sparrow, original);
        double currentNearest = SparrowHitEvasion.nearest(sparrow.m_20182_(), players);
        if (SparrowHitEvasionRules.settled(currentNearest, boss != null, currentAlly = boss == null ? 0.0 : SparrowHitEvasion.horizontal(sparrow.m_20182_(), boss.m_20182_()))) {
            SparrowHitEvasion.clear(sparrow);
            return original;
        }
        if (now < state.nextPath) {
            if (state.path != null && sparrow.m_21573_().m_26570_() == state.path && !sparrow.m_21573_().m_26571_()) {
                state.controlled = now;
            }
            return original;
        }
        state.nextPath = now + 8L;
        Vec3 away = sparrow.m_20182_().m_82546_(attacker.m_20182_()).m_82542_(1.0, 0.0, 1.0);
        if (away.m_82556_() < 1.0E-6) {
            away = new Vec3(1.0, 0.0, 0.0);
        }
        away = away.m_82541_();
        ArrayList<Vec3> points = new ArrayList<Vec3>();
        for (double turn : TURNS) {
            points.add(sparrow.m_20182_().m_82549_(SparrowHitEvasion.rotated(away, turn).m_82490_(6.0)));
        }
        if (boss != null) {
            Vec3 toward = boss.m_20182_().m_82546_(sparrow.m_20182_()).m_82542_(1.0, 0.0, 1.0);
            if (toward.m_82556_() > 1.0E-6) {
                points.add(sparrow.m_20182_().m_82549_(toward.m_82541_().m_82490_(Math.min(7.0, Math.max(1.0, currentAlly - 4.0)))));
            }
            Vec3 cover = boss.m_20182_().m_82546_(attacker.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
            for (double turn : new double[]{0.0, 0.7, -0.7}) {
                points.add(boss.m_20182_().m_82549_(SparrowHitEvasion.rotated(cover, turn).m_82490_(4.0)));
            }
        }
        Path best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double threatDistance = SparrowHitEvasion.horizontal(sparrow.m_20182_(), attacker.m_20182_());
        for (Vec3 point : points) {
            Path path;
            if (SparrowHitEvasion.horizontal(point, sparrow.m_20182_()) > 12.0 || !SparrowHitEvasion.safe(path = sparrow.m_21573_().m_7864_(BlockPos.m_274561_((double)point.f_82479_, (double)sparrow.m_20186_(), (double)point.f_82481_), 0), level, sparrow, players, currentNearest)) continue;
            Node end = path.m_77395_();
            Vec3 endpoint = new Vec3((double)end.f_77271_ + 0.5, (double)end.f_77272_, (double)end.f_77273_ + 0.5);
            boolean bl = boss != null;
            double d = SparrowHitEvasion.horizontal(endpoint, attacker.m_20182_());
            double d2 = SparrowHitEvasion.nearest(endpoint, players);
            double d3 = boss == null ? 0.0 : SparrowHitEvasion.horizontal(endpoint, boss.m_20182_());
            double score = SparrowHitEvasionRules.score(bl, threatDistance, currentNearest, currentAlly, d, d2, d3, SparrowHitEvasion.horizontal(endpoint, sparrow.m_20182_()));
            if (!(score > bestScore)) continue;
            best = path;
            bestScore = score;
        }
        if (best != null && sparrow.m_21573_().m_26536_(best, 1.25)) {
            state.path = sparrow.m_21573_().m_26570_();
            state.controlled = now;
        } else {
            SparrowHitEvasion.stopOwnedPath(sparrow, state);
        }
        return original;
    }

    public static boolean evadeSupport(ManorKiting original, EasterBossEntity sparrow) {
        State state = STATES.get(sparrow);
        return state != null && state.controlled == sparrow.m_9236_().m_46467_() || original.evadeSupport(sparrow);
    }

    private static void stopOwnedPath(EasterBossEntity sparrow, State state) {
        if (state.path != null && sparrow.m_21573_().m_26570_() == state.path) {
            sparrow.m_21573_().m_26573_();
        }
        state.path = null;
    }

    private static void clear(EasterBossEntity sparrow) {
        State state = STATES.remove(sparrow);
        if (state != null) {
            SparrowHitEvasion.stopOwnedPath(sparrow, state);
        }
    }

    @SubscribeEvent
    public static void leave(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EasterBossEntity) {
            EasterBossEntity sparrow = (EasterBossEntity)entity;
            if (!sparrow.m_9236_().f_46443_) {
                SparrowHitEvasion.clear(sparrow);
            }
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        STATES.clear();
    }

    private static final class State {
        UUID attacker;
        ResourceLocation dimension;
        long last;
        long until;
        long nextPath;
        long controlled = Long.MIN_VALUE;
        Path path;

        private State() {
        }
    }
}

