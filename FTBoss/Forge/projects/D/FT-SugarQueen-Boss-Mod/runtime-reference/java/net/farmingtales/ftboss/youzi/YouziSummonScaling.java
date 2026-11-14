/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 */
package net.farmingtales.ftboss.youzi;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.YouziSummonRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class YouziSummonScaling {
    private YouziSummonScaling() {
    }

    public static int playerCount(ServerLevel serverLevel, EasterBossEntity easterBossEntity) {
        int n = 0;
        for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
            if (!YouziSummonRules.countsPlayer(serverPlayer.m_6084_(), serverPlayer.m_7500_(), serverPlayer.m_5833_(), serverPlayer.m_213877_(), easterBossEntity.m_20280_((Entity)serverPlayer))) continue;
            ++n;
        }
        return Math.max(1, n);
    }
}

