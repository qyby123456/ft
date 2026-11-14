/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.boss.ai.ManorFinaleCombat;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.boss.ai.ManorMultiplayerBalance;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.qingyi_basic.BasicServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class ManorCombatContinuity {
    private ManorCombatContinuity() {
    }

    public static boolean present(Entity entity, LivingEntity livingEntity) {
        ServerLevel serverLevel;
        Level level;
        return ManorLivingTargets.valid(entity, livingEntity) && (!((level = entity.m_9236_()) instanceof ServerLevel) || (serverLevel = (ServerLevel)level).m_8791_(livingEntity.m_20148_()) == livingEntity);
    }

    private static boolean combatBoss(EasterBossEntity easterBossEntity) {
        return easterBossEntity != null && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && EasterBossEntity.isManorVariant(easterBossEntity.getVariant());
    }

    private static boolean target(EasterBossEntity easterBossEntity, LivingEntity livingEntity) {
        return ManorCombatContinuity.present((Entity)easterBossEntity, livingEntity) && (ManorMultiplayerBalance.isManagedManorBoss((LivingEntity)easterBossEntity) ? ManorMultiplayerBalance.validManorTarget((LivingEntity)easterBossEntity, livingEntity) : easterBossEntity.m_20280_((Entity)livingEntity) <= 4096.0);
    }

    public static boolean canTeleport(EasterBossEntity easterBossEntity, LivingEntity livingEntity) {
        ServerPlayer serverPlayer;
        block6: {
            block5: {
                if (!(livingEntity instanceof Player)) {
                    return ManorLivingTargets.valid((Entity)easterBossEntity, livingEntity);
                }
                if (!(livingEntity instanceof ServerPlayer)) break block5;
                serverPlayer = (ServerPlayer)livingEntity;
                if (ManorCombatContinuity.present((Entity)easterBossEntity, livingEntity)) break block6;
            }
            return false;
        }
        if (serverPlayer.m_20148_().equals(easterBossEntity.getManorSummonerId())) {
            return true;
        }
        return ManorCombatContinuity.combatBoss(easterBossEntity) && ManorCombatContinuity.target(easterBossEntity, livingEntity) && (livingEntity == easterBossEntity.m_5448_() || livingEntity == BossAggro.highest((LivingEntity)easterBossEntity) || ManorMultiplayerBalance.validManorTarget((LivingEntity)easterBossEntity, livingEntity));
    }

    public static boolean canAttack(EasterBossEntity easterBossEntity, Entity entity) {
        LivingEntity livingEntity;
        return entity instanceof LivingEntity && ManorCombatContinuity.present((Entity)easterBossEntity, livingEntity = (LivingEntity)entity);
    }

    private static LivingEntity choose(EasterBossEntity easterBossEntity, List<ServerPlayer> list) {
        LivingEntity livingEntity = BossAggro.highest((LivingEntity)easterBossEntity);
        if (ManorCombatContinuity.target(easterBossEntity, livingEntity)) {
            return livingEntity;
        }
        LivingEntity livingEntity2 = easterBossEntity.m_5448_();
        if (ManorCombatContinuity.target(easterBossEntity, livingEntity2)) {
            return livingEntity2;
        }
        return list.stream().filter(serverPlayer -> ManorCombatContinuity.target(easterBossEntity, (LivingEntity)serverPlayer)).min(Comparator.comparingInt(serverPlayer -> easterBossEntity.m_142582_((Entity)serverPlayer) ? 0 : 1).thenComparingDouble(serverPlayer -> easterBossEntity.m_20280_((Entity)serverPlayer)).thenComparing(serverPlayer -> serverPlayer.m_20148_().toString())).orElse(null);
    }

    private static void change(EasterBossEntity easterBossEntity, LivingEntity livingEntity) {
        if (livingEntity == easterBossEntity.m_5448_()) {
            return;
        }
        easterBossEntity.m_6710_(livingEntity);
        easterBossEntity.m_21573_().m_26573_();
        BasicServer.cancel(easterBossEntity);
        net.farmingtales.ftboss.xueli_basic.BasicServer.cancel(easterBossEntity, true);
    }

    public static void repair(EasterBossEntity easterBossEntity) {
        ServerLevel serverLevel;
        block3: {
            block2: {
                Level level;
                if (!ManorCombatContinuity.combatBoss(easterBossEntity) || !((level = easterBossEntity.m_9236_()) instanceof ServerLevel)) break block2;
                serverLevel = (ServerLevel)level;
                if (easterBossEntity.m_6084_() && !easterBossEntity.m_21224_() && !easterBossEntity.m_21525_() && !easterBossEntity.m_20147_() && !easterBossEntity.isManorIntroHidden() && !HiddenBossIntro.isActing(easterBossEntity.m_20148_()) && !ManorFinaleCombat.manages((LivingEntity)easterBossEntity)) break block3;
            }
            return;
        }
        ManorCombatContinuity.change(easterBossEntity, ManorCombatContinuity.choose(easterBossEntity, serverLevel.m_6907_()));
    }

    public static void update(EasterBossEntity easterBossEntity, List<ServerPlayer> list) {
        if (ManorFinaleCombat.manages((LivingEntity)easterBossEntity)) {
            ManorFinaleCombat.updateAggro(easterBossEntity, list);
            return;
        }
        if (easterBossEntity.m_21525_()) {
            return;
        }
        ManorCombatContinuity.change(easterBossEntity, ManorCombatContinuity.choose(easterBossEntity, list));
    }

    public static ServerPlayer fightPlayer(EasterBossEntity easterBossEntity) {
        ServerPlayer serverPlayer2;
        ServerPlayer serverPlayer3;
        Level level = easterBossEntity.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        level = easterBossEntity.m_5448_();
        if (level instanceof ServerPlayer && ManorCombatContinuity.target(easterBossEntity, (LivingEntity)(serverPlayer3 = (ServerPlayer)level))) {
            return serverPlayer3;
        }
        serverPlayer3 = BossAggro.highest((LivingEntity)easterBossEntity);
        if (serverPlayer3 instanceof ServerPlayer && ManorCombatContinuity.target(easterBossEntity, (LivingEntity)(serverPlayer2 = serverPlayer3))) {
            return serverPlayer2;
        }
        return serverLevel.m_6907_().stream().filter(serverPlayer -> ManorCombatContinuity.target(easterBossEntity, (LivingEntity)serverPlayer)).min(Comparator.comparingDouble(serverPlayer -> easterBossEntity.m_20280_((Entity)serverPlayer)).thenComparing(serverPlayer -> serverPlayer.m_20148_().toString())).orElse(null);
    }
}

