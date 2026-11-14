/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class FoodRewardFallback {
    public static final double NEAREST_RADIUS = 16.0;
    private static final double NEAREST_RADIUS_SQR = 256.0;

    private FoodRewardFallback() {
    }

    public static ServerPlayer resolve(ServerPlayer serverPlayer, LivingEntity livingEntity) {
        if (serverPlayer != null) {
            return serverPlayer;
        }
        if (livingEntity == null) {
            return null;
        }
        LivingEntity livingEntity2 = livingEntity.m_21188_();
        if (livingEntity2 instanceof ServerPlayer) {
            ServerPlayer serverPlayer2 = (ServerPlayer)livingEntity2;
            return serverPlayer2;
        }
        Level level = livingEntity.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        livingEntity2 = (ServerLevel)level;
        List list = livingEntity2.m_6907_();
        ServerPlayer serverPlayer3 = null;
        double d = 256.0;
        for (ServerPlayer serverPlayer4 : list) {
            double d2;
            if (serverPlayer4 == null || serverPlayer4.m_5833_() || !((d2 = serverPlayer4.m_20280_((Entity)livingEntity)) <= d)) continue;
            d = d2;
            serverPlayer3 = serverPlayer4;
        }
        return serverPlayer3;
    }
}

