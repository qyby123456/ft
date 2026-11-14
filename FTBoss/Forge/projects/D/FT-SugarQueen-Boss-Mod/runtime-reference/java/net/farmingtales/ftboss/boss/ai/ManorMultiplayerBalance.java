/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.ManorCombatContinuity;
import net.farmingtales.ftboss.boss.ai.ManorFinaleCombat;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.ManorAssaultVisuals;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ManorMultiplayerBalance {
    public static final double FIGHT_RADIUS = 96.0;
    public static final int MAX_SCALING_PLAYERS = 6;
    public static final double BOSS_HEALTH_PER_EXTRA_PLAYER = 0.1;
    public static final double SPARROW_HEALTH_PER_EXTRA_PLAYER = 0.06;
    public static final int RANGED_WINDUP_TICKS = 20;
    public static final int RANGED_COOLDOWN_TICKS = 140;
    public static final double RANGED_MAX_DISTANCE = 72.0;
    public static final double RANGED_AREA_RADIUS = 3.0;
    public static final double EDGE_PRESSURE_RADIUS = 72.0;
    public static final int EDGE_UNTARGETED_TICKS = 120;
    private static final String BASE_HP = "ftboss_base_hp";
    private static final String BASE_ATTACK = "ftboss_base_atk";
    private static final String DAMAGE_MULTIPLIER = "ftboss_dmg_mul";
    private static final String PLAYER_COUNT = "ftboss_manor_player_count";
    private static final String SHOT_DUE = "ftboss_manor_shot_due";
    private static final String SHOT_NEXT = "ftboss_manor_shot_next";
    private static final String SHOT_TARGET = "ftboss_manor_shot_target";
    private static final String SHOT_X = "ftboss_manor_shot_x";
    private static final String SHOT_Y = "ftboss_manor_shot_y";
    private static final String SHOT_Z = "ftboss_manor_shot_z";
    private static final String EDGE_TARGET = "ftboss_manor_edge_target";
    private static final String EDGE_SINCE = "ftboss_manor_edge_since";

    private ManorMultiplayerBalance() {
    }

    public static boolean tick(LivingEntity entity) {
        if (!(entity instanceof EasterBossEntity)) {
            return false;
        }
        EasterBossEntity boss = (EasterBossEntity)entity;
        UUID fightId = ManorMultiplayerBalance.activeFightId(boss);
        if (fightId == null) {
            return false;
        }
        if (boss.m_9236_().f_46443_ || boss.m_21224_()) {
            return true;
        }
        Vec3 anchor = ManorFight.anchorOf(fightId);
        if (anchor == null || !(boss.m_9236_() instanceof ServerLevel)) {
            return true;
        }
        ServerLevel level = (ServerLevel)boss.m_9236_();
        List<ServerPlayer> players = ManorMultiplayerBalance.playersInArena(level, anchor);
        int playerCount = Math.max(1, players.size());
        int scalingPlayers = Math.min(6, playerCount);
        boolean sparrow = boss.getVariant() == 9;
        boolean combatBoss = ManorMultiplayerBalance.isCombatBoss(boss);
        double healthMultiplier = sparrow ? 1.0 + 0.06 * (double)(scalingPlayers - 1) : (combatBoss ? 1.0 + 0.1 * (double)(scalingPlayers - 1) : 1.0);
        ManorMultiplayerBalance.applyHealthAndRestoreDamage(boss, healthMultiplier, playerCount);
        if (combatBoss) {
            ManorMultiplayerBalance.updateIndependentAggro(boss, players);
            ManorMultiplayerBalance.tickIndependentRangedAttack(level, boss, players, anchor);
        } else {
            ManorMultiplayerBalance.clearShot(boss.getPersistentData());
        }
        return true;
    }

    public static boolean isManagedManorBoss(LivingEntity entity) {
        if (!(entity instanceof EasterBossEntity)) {
            return false;
        }
        EasterBossEntity boss = (EasterBossEntity)entity;
        UUID fightId = ManorMultiplayerBalance.activeFightId(boss);
        return fightId != null && ManorMultiplayerBalance.isCombatBoss(boss);
    }

    /*
     * WARNING - void declaration
     */
    public static boolean validManorTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
        void target;
        LivingEntity entity;
        if (!ManorCombatContinuity.present((Entity)livingEntity, livingEntity2)) {
            return false;
        }
        if (ManorFinaleCombat.manages(livingEntity)) {
            return ManorFinaleCombat.validTarget(livingEntity, livingEntity2);
        }
        if (!ManorMultiplayerBalance.isManagedManorBoss(entity) || !(target instanceof ServerPlayer)) {
            return false;
        }
        ServerPlayer player = (ServerPlayer)target;
        if (!player.m_6084_() || player.m_5833_() || player.m_7500_() || player.m_9236_() != entity.m_9236_()) {
            return false;
        }
        UUID fightId = ManorFightMembers.fightId((Entity)entity);
        Vec3 anchor = ManorFight.anchorOf(fightId);
        return anchor != null && player.m_20275_(anchor.f_82479_, anchor.f_82480_, anchor.f_82481_) <= 9216.0;
    }

    private static UUID activeFightId(EasterBossEntity boss) {
        int variant = boss.getVariant();
        if (variant != 9 && !EasterBossEntity.isManorVariant(variant)) {
            return null;
        }
        UUID fightId = ManorFightMembers.fightId((Entity)boss);
        return fightId != null && ManorFight.hasActive(fightId) ? fightId : null;
    }

    private static boolean isCombatBoss(EasterBossEntity boss) {
        return !boss.isMini() && boss.getVariant() != 9 && EasterBossEntity.isManorVariant(boss.getVariant()) && !boss.isManorIntroHidden();
    }

    private static List<ServerPlayer> playersInArena(ServerLevel level, Vec3 anchor) {
        ArrayList<ServerPlayer> result = new ArrayList<ServerPlayer>();
        double radiusSqr = 9216.0;
        for (ServerPlayer player2 : level.m_6907_()) {
            if (!player2.m_6084_() || player2.m_5833_() || player2.m_7500_() || !(player2.m_20275_(anchor.f_82479_, anchor.f_82480_, anchor.f_82481_) <= radiusSqr)) continue;
            result.add(player2);
        }
        result.sort(Comparator.comparing(player -> player.m_20148_().toString()));
        return result;
    }

    private static void applyHealthAndRestoreDamage(EasterBossEntity boss, double multiplier, int playerCount) {
        double baseHealth;
        CompoundTag tag = boss.getPersistentData();
        AttributeInstance health = boss.m_21051_(Attributes.f_22276_);
        if (!tag.m_128441_(BASE_HP)) {
            tag.m_128347_(BASE_HP, health != null ? health.m_22115_() : (double)boss.m_21233_());
        }
        if (!tag.m_128441_(BASE_ATTACK)) {
            AttributeInstance attack = boss.m_21051_(Attributes.f_22281_);
            tag.m_128347_(BASE_ATTACK, attack != null ? attack.m_22115_() : 0.0);
        }
        if (!Double.isFinite(baseHealth = tag.m_128459_(BASE_HP)) || baseHealth <= 0.0) {
            baseHealth = health != null ? health.m_22115_() : (double)boss.m_21233_();
            tag.m_128347_(BASE_HP, baseHealth);
        }
        double targetHealth = baseHealth * multiplier;
        if (baseHealth <= 1024.0) {
            targetHealth = Math.min(1024.0, targetHealth);
        }
        if (health != null && Math.abs(health.m_22115_() - targetHealth) > 0.01) {
            float ratio = boss.m_21233_() > 0.0f ? boss.m_21223_() / boss.m_21233_() : 1.0f;
            health.m_22100_(targetHealth);
            float newMax = boss.m_21233_();
            boss.m_21153_(Math.max(1.0f, Math.min(newMax, ratio * newMax)));
        }
        tag.m_128347_(DAMAGE_MULTIPLIER, 1.0);
        tag.m_128405_(PLAYER_COUNT, playerCount);
        AttributeInstance attack = boss.m_21051_(Attributes.f_22281_);
        double baseAttack = tag.m_128459_(BASE_ATTACK);
        if (attack != null && Double.isFinite(baseAttack) && baseAttack >= 0.0 && Math.abs(attack.m_22115_() - baseAttack) > 0.001) {
            attack.m_22100_(baseAttack);
        }
    }

    private static void updateIndependentAggro(EasterBossEntity easterBossEntity, List<ServerPlayer> list) {
        ManorCombatContinuity.update(easterBossEntity, list);
    }

    /*
     * WARNING - void declaration
     */
    private static void tickIndependentRangedAttack(ServerLevel serverLevel, EasterBossEntity easterBossEntity, List<ServerPlayer> list, Vec3 vec3) {
        ServerPlayer target;
        boolean edgePressureReady;
        void anchor;
        void players;
        ServerLevel level;
        void boss;
        if (ManorFinaleCombat.manages((LivingEntity)easterBossEntity)) {
            ManorFinaleCombat.tickAssault(serverLevel, easterBossEntity, list, vec3);
            return;
        }
        CompoundTag tag = boss.getPersistentData();
        long now = level.m_46467_();
        if (boss.m_21525_()) {
            ManorMultiplayerBalance.clearShot(tag);
            ManorMultiplayerBalance.clearEdgePressure(tag);
            return;
        }
        ServerPlayer edgeTarget = ManorMultiplayerBalance.trackUntargetedEdgePlayer((EasterBossEntity)boss, (List<ServerPlayer>)players, (Vec3)anchor, tag, now);
        boolean bl = edgePressureReady = edgeTarget != null && tag.m_128441_(EDGE_SINCE) && now - tag.m_128454_(EDGE_SINCE) >= 120L;
        if (tag.m_128441_(SHOT_DUE) && now >= tag.m_128454_(SHOT_DUE)) {
            ManorMultiplayerBalance.fireStoredShot(level, (EasterBossEntity)boss, tag);
        }
        if (players.size() < 2 && !edgePressureReady) {
            ManorMultiplayerBalance.clearDueShot(tag);
            tag.m_128473_(SHOT_NEXT);
            return;
        }
        if (!tag.m_128441_(SHOT_NEXT)) {
            tag.m_128356_(SHOT_NEXT, edgePressureReady ? now : now + 60L + (long)Math.floorMod(boss.m_20148_().hashCode(), 31));
        }
        if (tag.m_128441_(SHOT_DUE) || now < tag.m_128454_(SHOT_NEXT)) {
            return;
        }
        ServerPlayer serverPlayer = target = edgePressureReady ? edgeTarget : ManorMultiplayerBalance.chooseRangedTarget((EasterBossEntity)boss, (List<ServerPlayer>)players, now);
        if (target == null) {
            tag.m_128356_(SHOT_NEXT, now + 40L);
            return;
        }
        Vec3 field = ManorMultiplayerBalance.groundAt(level, (EasterBossEntity)boss, target.m_20182_());
        tag.m_128362_(SHOT_TARGET, target.m_20148_());
        tag.m_128347_(SHOT_X, field.f_82479_);
        tag.m_128347_(SHOT_Y, field.f_82480_);
        tag.m_128347_(SHOT_Z, field.f_82481_);
        tag.m_128356_(SHOT_DUE, now + 20L);
        tag.m_128356_(SHOT_NEXT, now + 140L + (long)Math.floorMod(boss.m_20148_().hashCode(), 21));
        if (edgePressureReady) {
            ManorMultiplayerBalance.clearEdgePressure(tag);
        }
        FTBossMod.LOGGER.info("[FTBoss][ManorAssault] charge boss={} variant={} target={} mode={} center=({},{},{})", new Object[]{boss.m_7755_().getString(), boss.getVariant(), target.m_6302_(), edgePressureReady ? "edge" : "multiplayer", String.format("%.1f", field.f_82479_), String.format("%.1f", field.f_82480_), String.format("%.1f", field.f_82481_)});
        ManorAssaultVisuals.show(level, (EasterBossEntity)boss, field, false);
        ParticleOptions particle = ManorMultiplayerBalance.particleFor(boss.getVariant());
        level.m_8767_(particle, target.m_20185_(), target.m_20186_() + 0.12, target.m_20189_(), 18, 1.0, 0.03, 1.0, 0.01);
        level.m_5594_(null, target.m_20183_(), SoundEvents.f_144243_, SoundSource.HOSTILE, 0.8f, 0.75f);
    }

    private static ServerPlayer trackUntargetedEdgePlayer(EasterBossEntity boss, List<ServerPlayer> players, Vec3 anchor, CompoundTag tag, long now) {
        double edgeRadiusSqr = 5184.0;
        ServerPlayer tracked = null;
        if (tag.m_128403_(EDGE_TARGET)) {
            UUID trackedId = tag.m_128342_(EDGE_TARGET);
            for (ServerPlayer player : players) {
                if (!player.m_20148_().equals(trackedId) || !(player.m_20275_(anchor.f_82479_, anchor.f_82480_, anchor.f_82481_) >= edgeRadiusSqr)) continue;
                tracked = player;
                break;
            }
        }
        if (tracked == null) {
            double farthest = edgeRadiusSqr;
            for (ServerPlayer player : players) {
                double distance = player.m_20275_(anchor.f_82479_, anchor.f_82480_, anchor.f_82481_);
                if (!(distance >= farthest)) continue;
                farthest = distance;
                tracked = player;
            }
            ManorMultiplayerBalance.clearEdgePressure(tag);
            if (tracked != null) {
                tag.m_128362_(EDGE_TARGET, tracked.m_20148_());
                tag.m_128356_(EDGE_SINCE, now);
                FTBossMod.LOGGER.info("[FTBoss][ManorAssault] edge-watch boss={} target={} anchorDistance={}", new Object[]{boss.m_7755_().getString(), tracked.m_6302_(), String.format("%.1f", Math.sqrt(tracked.m_20275_(anchor.f_82479_, anchor.f_82480_, anchor.f_82481_)))});
            }
        }
        if (tracked == null) {
            ManorMultiplayerBalance.clearEdgePressure(tag);
            return null;
        }
        if (!tag.m_128441_(EDGE_SINCE)) {
            tag.m_128356_(EDGE_SINCE, now);
        }
        return tracked;
    }

    private static void clearEdgePressure(CompoundTag tag) {
        tag.m_128473_(EDGE_TARGET);
        tag.m_128473_(EDGE_SINCE);
    }

    private static Vec3 groundAt(ServerLevel level, EasterBossEntity boss, Vec3 point) {
        BlockHitResult hit = level.m_45547_(new ClipContext(point.m_82520_(0.0, 3.0, 0.0), point.m_82520_(0.0, -10.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)boss));
        return hit.m_6662_() == HitResult.Type.MISS ? point : hit.m_82450_();
    }

    private static ServerPlayer chooseRangedTarget(EasterBossEntity boss, List<ServerPlayer> players, long now) {
        LivingEntity mainTarget = boss.m_5448_();
        int start = Math.floorMod(boss.m_20148_().hashCode() + (int)(now / 140L), players.size());
        double maxDistanceSqr = 5184.0;
        for (int pass = 0; pass < 2; ++pass) {
            for (int offset = 0; offset < players.size(); ++offset) {
                ServerPlayer candidate = players.get((start + offset) % players.size());
                if (pass == 0 && candidate == mainTarget || !(boss.m_20280_((Entity)candidate) <= maxDistanceSqr) || !boss.m_142582_((Entity)candidate)) continue;
                return candidate;
            }
        }
        return null;
    }

    private static void fireStoredShot(ServerLevel level, EasterBossEntity boss, CompoundTag tag) {
        double x = tag.m_128459_(SHOT_X);
        double y = tag.m_128459_(SHOT_Y);
        double z = tag.m_128459_(SHOT_Z);
        ManorMultiplayerBalance.clearDueShot(tag);
        Vec3 field = new Vec3(x, y, z);
        ManorAssaultVisuals.show(level, boss, field, true);
        ParticleOptions particle = ManorMultiplayerBalance.particleFor(boss.getVariant());
        level.m_8767_(particle, x, y + 0.2, z, 28, 1.4, 0.08, 1.4, 0.04);
        level.m_8767_((ParticleOptions)ParticleTypes.f_123759_, x, y + 0.25, z, 18, 1.2, 0.12, 1.2, 0.04);
        level.m_6263_(null, x, y, z, SoundEvents.f_11705_, SoundSource.HOSTILE, 0.9f, 0.85f);
        double radiusSqr = 9.0;
        float damage = boss.isImpossibleChallenge() ? 5.0f : 4.0f;
        int hits = 0;
        for (ServerPlayer player : level.m_6907_()) {
            double dz;
            double dy;
            double dx;
            if (!ManorMultiplayerBalance.validManorTarget((LivingEntity)boss, (LivingEntity)player) || !((dx = player.m_20185_() - x) * dx + (dy = player.m_20186_() - y) * dy + (dz = player.m_20189_() - z) * dz <= radiusSqr) || !player.m_6469_(boss.m_269291_().m_269104_((Entity)boss, (Entity)boss), damage)) continue;
            ++hits;
        }
        FTBossMod.LOGGER.info("[FTBoss][ManorAssault] release boss={} variant={} hits={} center=({},{},{})", new Object[]{boss.m_7755_().getString(), boss.getVariant(), hits, String.format("%.1f", x), String.format("%.1f", y), String.format("%.1f", z)});
    }

    private static ParticleOptions particleFor(int variant) {
        switch (variant) {
            case 0: {
                return ParticleTypes.f_123744_;
            }
            case 1: {
                return ParticleTypes.f_123771_;
            }
            case 2: {
                return ParticleTypes.f_123760_;
            }
            case 3: {
                return ParticleTypes.f_123745_;
            }
            case 4: {
                return ParticleTypes.f_123749_;
            }
            case 5: {
                return ParticleTypes.f_175830_;
            }
            case 6: {
                return ParticleTypes.f_123797_;
            }
            case 7: {
                return ParticleTypes.f_123762_;
            }
            case 8: {
                return ParticleTypes.f_123810_;
            }
            case 10: {
                return ParticleTypes.f_175821_;
            }
            case 11: {
                return ParticleTypes.f_123779_;
            }
        }
        return ParticleTypes.f_123810_;
    }

    private static void clearShot(CompoundTag tag) {
        ManorMultiplayerBalance.clearDueShot(tag);
        tag.m_128473_(SHOT_NEXT);
    }

    private static void clearDueShot(CompoundTag tag) {
        tag.m_128473_(SHOT_DUE);
        tag.m_128473_(SHOT_TARGET);
        tag.m_128473_(SHOT_X);
        tag.m_128473_(SHOT_Y);
        tag.m_128473_(SHOT_Z);
    }

    private static /* synthetic */ double lambda$updateIndependentAggro$1(EasterBossEntity boss, ServerPlayer player) {
        return player.m_20280_((Entity)boss);
    }
}

