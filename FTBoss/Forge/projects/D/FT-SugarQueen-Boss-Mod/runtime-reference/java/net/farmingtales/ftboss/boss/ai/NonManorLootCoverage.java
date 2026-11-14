/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ManorVictory;
import net.farmingtales.ftboss.boss.ai.BossMercy;
import net.farmingtales.ftboss.boss.ai.FoodRewardFallback;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class NonManorLootCoverage {
    private NonManorLootCoverage() {
    }

    public static boolean eligible(LivingEntity livingEntity) {
        SugarQueenEntity sugarQueenEntity;
        if (livingEntity == null || livingEntity instanceof EasterBossEntity || LemonSummonRewards.suppressed((Entity)livingEntity)) {
            return false;
        }
        if (livingEntity instanceof SugarQueenEntity && (sugarQueenEntity = (SugarQueenEntity)livingEntity).isCloneQueen()) {
            return false;
        }
        return BossMercy.typeOf((Entity)livingEntity) != null || livingEntity instanceof AppleBossEntity || livingEntity instanceof CompostBossEntity || livingEntity instanceof BengerEntity;
    }

    public static LivingEntity creditedPlayer(LivingEntity livingEntity) {
        LivingEntity livingEntity2 = livingEntity.m_21232_();
        if (livingEntity2 instanceof ServerPlayer || !NonManorLootCoverage.eligible(livingEntity)) {
            return livingEntity2;
        }
        return FoodRewardFallback.resolve(null, livingEntity);
    }

    public static BlockPos position(ServerLevel serverLevel, BlockPos blockPos, ServerPlayer serverPlayer) {
        BlockPos blockPos2 = ManorVictory.findSite(serverLevel, blockPos);
        if (blockPos2 != null || serverPlayer == null || serverPlayer.m_9236_() != serverLevel) {
            return blockPos2;
        }
        return ManorVictory.findSite(serverLevel, serverPlayer.m_20183_());
    }
}

