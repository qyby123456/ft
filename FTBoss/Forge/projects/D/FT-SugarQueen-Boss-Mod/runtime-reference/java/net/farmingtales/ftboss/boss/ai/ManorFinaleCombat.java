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
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.boss.ai.ManorCombatContinuity;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.boss.ai.ManorMultiplayerBalance;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.ManorAssaultVisuals;
import net.farmingtales.ftboss.util.ManorFinaleRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ManorFinaleCombat {
    private static final String SHOT_DUE = "ftboss_manor_shot_due";
    private static final String SHOT_NEXT = "ftboss_manor_shot_next";
    private static final String SHOT_TARGET = "ftboss_manor_shot_target";
    private static final String SHOT_X = "ftboss_manor_shot_x";
    private static final String SHOT_Y = "ftboss_manor_shot_y";
    private static final String SHOT_Z = "ftboss_manor_shot_z";
    private static final String SHOT_INDEX = "ftboss_manor_finale_shot_index";

    private ManorFinaleCombat() {
    }

    public static boolean manages(LivingEntity livingEntity) {
        EasterBossEntity easterBossEntity;
        return livingEntity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)livingEntity).isImpossibleChallenge() && !easterBossEntity.isGiant() && ManorMultiplayerBalance.isManagedManorBoss(livingEntity);
    }

    public static void tick(EasterBossEntity easterBossEntity) {
        Level level;
        if (!ManorFinaleCombat.manages((LivingEntity)easterBossEntity) || !((level = easterBossEntity.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        level = ManorFight.anchorOf(ManorFightMembers.fightId((Entity)easterBossEntity));
        if (level == null) {
            return;
        }
        List<ServerPlayer> list = serverLevel.m_6907_().stream().filter(serverPlayer -> ManorFinaleCombat.validTarget((LivingEntity)easterBossEntity, (LivingEntity)serverPlayer)).toList();
        ManorFinaleCombat.updateAggro(easterBossEntity, list);
        ManorFinaleCombat.tickAssault(serverLevel, easterBossEntity, list, (Vec3)level);
    }

    public static boolean keepCommittedTarget(Mob mob) {
        EasterBossEntity easterBossEntity;
        return ManorFinaleCombat.manages((LivingEntity)mob) && mob instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)mob).isPerformingSkill() && ManorFinaleCombat.validTarget((LivingEntity)easterBossEntity, easterBossEntity.m_5448_());
    }

    private static boolean combatReady(EasterBossEntity easterBossEntity) {
        return ManorFinaleCombat.manages((LivingEntity)easterBossEntity) && easterBossEntity.m_6084_() && !easterBossEntity.m_21224_() && !easterBossEntity.m_21525_() && !easterBossEntity.isGiant() && !easterBossEntity.m_20147_() && !HiddenBossIntro.isActing(easterBossEntity.m_20148_());
    }

    public static boolean validTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (!ManorFinaleCombat.manages(livingEntity) || !ManorCombatContinuity.present((Entity)livingEntity, livingEntity2)) {
            return false;
        }
        Vec3 vec3 = ManorFight.anchorOf(ManorFightMembers.fightId((Entity)livingEntity));
        return vec3 != null && ManorFinaleCombat.insideArena(livingEntity2.m_20182_(), vec3);
    }

    private static boolean insideArena(Vec3 vec3, Vec3 vec32) {
        return ManorFinaleRules.insideArena(vec3.f_82479_ - vec32.f_82479_, vec3.f_82480_ - vec32.f_82480_, vec3.f_82481_ - vec32.f_82481_);
    }

    public static void updateAggro(EasterBossEntity easterBossEntity, List<ServerPlayer> list) {
        if (!ManorFinaleCombat.combatReady(easterBossEntity)) {
            return;
        }
        LivingEntity livingEntity = easterBossEntity.m_5448_();
        if (easterBossEntity.isPerformingSkill() && ManorFinaleCombat.validTarget((LivingEntity)easterBossEntity, livingEntity)) {
            return;
        }
        LivingEntity livingEntity2 = BossAggro.highest((LivingEntity)easterBossEntity);
        if (!ManorFinaleCombat.validTarget((LivingEntity)easterBossEntity, livingEntity2)) {
            Object object = livingEntity2 = ManorFinaleCombat.validTarget((LivingEntity)easterBossEntity, livingEntity) ? livingEntity : null;
        }
        if (livingEntity2 == null && ManorFinaleCombat.validTarget((LivingEntity)easterBossEntity, easterBossEntity.m_21188_())) {
            livingEntity2 = easterBossEntity.m_21188_();
        }
        if (livingEntity2 == null) {
            livingEntity2 = list.stream().filter(serverPlayer -> ManorFinaleCombat.validTarget((LivingEntity)easterBossEntity, (LivingEntity)serverPlayer)).min(Comparator.comparingInt(serverPlayer -> easterBossEntity.m_142582_((Entity)serverPlayer) ? 0 : 1).thenComparingDouble(serverPlayer -> easterBossEntity.m_20280_((Entity)serverPlayer)).thenComparing(serverPlayer -> serverPlayer.m_20148_().toString())).orElse(null);
        }
        if (livingEntity2 != livingEntity) {
            easterBossEntity.m_6710_(livingEntity2);
            easterBossEntity.m_21573_().m_26573_();
        }
    }

    public static void huntTarget(EasterBossEntity easterBossEntity, ServerLevel serverLevel) {
        LivingEntity livingEntity = easterBossEntity.m_5448_();
        if (!ManorFinaleCombat.combatReady(easterBossEntity) || !ManorFinaleCombat.validTarget((LivingEntity)easterBossEntity, livingEntity)) {
            return;
        }
        easterBossEntity.m_21563_().m_24960_((Entity)livingEntity, 30.0f, 30.0f);
        if (easterBossEntity.m_20280_((Entity)livingEntity) > 400.0 && serverLevel.m_46467_() % 10L == 0L) {
            easterBossEntity.m_21573_().m_5624_((Entity)livingEntity, 1.6);
        }
    }

    public static void tickAssault(ServerLevel serverLevel, EasterBossEntity easterBossEntity, List<ServerPlayer> list, Vec3 vec3) {
        List<LivingEntity> list2;
        CompoundTag compoundTag = easterBossEntity.getPersistentData();
        compoundTag.m_128473_("ftboss_manor_edge_target");
        compoundTag.m_128473_("ftboss_manor_edge_since");
        if (!ManorFinaleCombat.combatReady(easterBossEntity)) {
            ManorFinaleCombat.clearDue(compoundTag);
            compoundTag.m_128473_(SHOT_NEXT);
            return;
        }
        long l = serverLevel.m_46467_();
        if (compoundTag.m_128441_(SHOT_DUE)) {
            list2 = compoundTag.m_128403_(SHOT_TARGET) ? ManorLivingTargets.resolve((Level)serverLevel, compoundTag.m_128342_(SHOT_TARGET)) : null;
            Vec3 vec32 = ManorFinaleCombat.storedField(compoundTag);
            if (!ManorFinaleCombat.validTarget((LivingEntity)easterBossEntity, list2) || !ManorFinaleCombat.insideArena(vec32, vec3)) {
                ManorFinaleCombat.clearDue(compoundTag);
            } else if (l >= compoundTag.m_128454_(SHOT_DUE)) {
                ManorFinaleCombat.fireAssault(serverLevel, easterBossEntity, list, list2, vec32);
                ManorFinaleCombat.clearDue(compoundTag);
            } else if (compoundTag.m_128454_(SHOT_DUE) - l == 10L) {
                ManorFinaleCombat.warningRing(serverLevel, vec32);
            }
        }
        if ((list2 = ManorFinaleCombat.candidates(easterBossEntity, list)).isEmpty()) {
            ManorFinaleCombat.clearDue(compoundTag);
            compoundTag.m_128473_(SHOT_NEXT);
            return;
        }
        int n = easterBossEntity.m_20148_().hashCode();
        if (!compoundTag.m_128441_(SHOT_NEXT)) {
            compoundTag.m_128356_(SHOT_NEXT, l + 30L + (long)Math.floorMod(n, 16));
        }
        if (compoundTag.m_128441_(SHOT_DUE) || l < compoundTag.m_128454_(SHOT_NEXT)) {
            return;
        }
        LivingEntity livingEntity = easterBossEntity.m_5448_();
        List<LivingEntity> list3 = list2.stream().filter(livingEntity2 -> livingEntity2 != livingEntity).toList();
        List<LivingEntity> list4 = list3.isEmpty() ? list2 : list3;
        long l2 = compoundTag.m_128454_(SHOT_INDEX);
        LivingEntity livingEntity3 = list4.get(ManorFinaleRules.rotationStart(n, l2, list4.size()));
        Vec3 vec33 = ManorFinaleCombat.fieldAt(serverLevel, easterBossEntity, livingEntity3.m_20182_(), vec3);
        compoundTag.m_128362_(SHOT_TARGET, livingEntity3.m_20148_());
        compoundTag.m_128347_(SHOT_X, vec33.f_82479_);
        compoundTag.m_128347_(SHOT_Y, vec33.f_82480_);
        compoundTag.m_128347_(SHOT_Z, vec33.f_82481_);
        compoundTag.m_128356_(SHOT_DUE, l + 20L);
        compoundTag.m_128356_(SHOT_NEXT, l + (long)ManorFinaleRules.cooldown(n));
        compoundTag.m_128356_(SHOT_INDEX, l2 + 1L & Long.MAX_VALUE);
        ManorAssaultVisuals.show(serverLevel, easterBossEntity, vec33, false);
        ManorFinaleCombat.warningRing(serverLevel, vec33);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123759_, vec33.f_82479_, vec33.f_82480_ + 0.12, vec33.f_82481_, 18, 1.0, 0.03, 1.0, 0.01);
        serverLevel.m_6263_(null, vec33.f_82479_, vec33.f_82480_, vec33.f_82481_, SoundEvents.f_144243_, SoundSource.HOSTILE, 0.8f, 0.75f);
        FTBossMod.LOGGER.info("[FTBoss][ManorAssault] charge boss={} variant={} target={} mode=finale windup={} cooldown={}", new Object[]{easterBossEntity.m_7755_().getString(), easterBossEntity.getVariant(), livingEntity3.m_7755_().getString(), 20, ManorFinaleRules.cooldown(n)});
    }

    private static List<LivingEntity> candidates(EasterBossEntity easterBossEntity, List<ServerPlayer> list) {
        ArrayList<LivingEntity> arrayList = new ArrayList<LivingEntity>();
        for (ServerPlayer serverPlayer : list) {
            ManorFinaleCombat.addCandidate(arrayList, easterBossEntity, (LivingEntity)serverPlayer);
        }
        ManorFinaleCombat.addCandidate(arrayList, easterBossEntity, easterBossEntity.m_5448_());
        ManorFinaleCombat.addCandidate(arrayList, easterBossEntity, easterBossEntity.m_21188_());
        ManorFinaleCombat.addCandidate(arrayList, easterBossEntity, BossAggro.highest((LivingEntity)easterBossEntity));
        arrayList.sort(Comparator.comparing(livingEntity -> livingEntity.m_20148_().toString()));
        return arrayList;
    }

    private static void addCandidate(List<LivingEntity> list, EasterBossEntity easterBossEntity, LivingEntity livingEntity) {
        if (ManorFinaleCombat.validTarget((LivingEntity)easterBossEntity, livingEntity) && !list.contains(livingEntity)) {
            list.add(livingEntity);
        }
    }

    private static Vec3 fieldAt(ServerLevel serverLevel, EasterBossEntity easterBossEntity, Vec3 vec3, Vec3 vec32) {
        BlockHitResult blockHitResult = serverLevel.m_45547_(new ClipContext(vec3.m_82520_(0.0, 0.05, 0.0), vec3.m_82520_(0.0, -4.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)easterBossEntity));
        if (blockHitResult.m_6662_() != HitResult.Type.MISS) {
            Vec3 vec33 = blockHitResult.m_82450_();
            if (vec3.f_82480_ - vec33.f_82480_ <= 3.0 && ManorFinaleCombat.insideArena(vec33, vec32)) {
                return vec33;
            }
        }
        return vec3;
    }

    private static Vec3 storedField(CompoundTag compoundTag) {
        return new Vec3(compoundTag.m_128459_(SHOT_X), compoundTag.m_128459_(SHOT_Y), compoundTag.m_128459_(SHOT_Z));
    }

    private static void warningRing(ServerLevel serverLevel, Vec3 vec3) {
        for (int i = 0; i < 20; ++i) {
            double d = (double)i * Math.PI * 2.0 / 20.0;
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, vec3.f_82479_ + Math.cos(d) * 3.0, vec3.f_82480_ + 0.12, vec3.f_82481_ + Math.sin(d) * 3.0, 1, 0.0, 0.02, 0.0, 0.0);
        }
    }

    private static void clearDue(CompoundTag compoundTag) {
        compoundTag.m_128473_(SHOT_DUE);
        compoundTag.m_128473_(SHOT_TARGET);
        compoundTag.m_128473_(SHOT_X);
        compoundTag.m_128473_(SHOT_Y);
        compoundTag.m_128473_(SHOT_Z);
    }

    private static void fireAssault(ServerLevel serverLevel, EasterBossEntity easterBossEntity, List<ServerPlayer> list, LivingEntity livingEntity, Vec3 vec3) {
        ManorAssaultVisuals.show(serverLevel, easterBossEntity, vec3, true);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123759_, vec3.f_82479_, vec3.f_82480_ + 0.25, vec3.f_82481_, 18, 1.2, 0.12, 1.2, 0.04);
        serverLevel.m_6263_(null, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, SoundEvents.f_11705_, SoundSource.HOSTILE, 0.9f, 0.85f);
        List<LivingEntity> list2 = ManorFinaleCombat.candidates(easterBossEntity, list);
        ManorFinaleCombat.addCandidate(list2, easterBossEntity, livingEntity);
        int n = 0;
        for (LivingEntity livingEntity2 : list2) {
            AABB aABB = livingEntity2.m_20191_();
            if (!ManorFinaleRules.intersectsField(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, aABB.f_82288_, aABB.f_82289_, aABB.f_82290_, aABB.f_82291_, aABB.f_82292_, aABB.f_82293_)) continue;
            Vec3 vec32 = livingEntity2.m_20182_().m_82520_(0.0, (double)livingEntity2.m_20206_() * 0.5, 0.0);
            BlockHitResult blockHitResult = serverLevel.m_45547_(new ClipContext(vec3.m_82520_(0.0, 0.15, 0.0), vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)easterBossEntity));
            if (blockHitResult.m_6662_() != HitResult.Type.MISS || !livingEntity2.m_6469_(easterBossEntity.m_269291_().m_269104_((Entity)easterBossEntity, (Entity)easterBossEntity), 5.0f)) continue;
            ++n;
        }
        FTBossMod.LOGGER.info("[FTBoss][ManorAssault] release boss={} variant={} hits={} mode=finale center=({},{},{})", new Object[]{easterBossEntity.m_7755_().getString(), easterBossEntity.getVariant(), n, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_});
    }
}

