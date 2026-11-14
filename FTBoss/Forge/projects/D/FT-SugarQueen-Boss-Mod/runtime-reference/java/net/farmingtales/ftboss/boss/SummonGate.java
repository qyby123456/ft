/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class SummonGate {
    public static final boolean OVERWORLD_ONLY = true;

    private SummonGate() {
    }

    public static boolean isAllowedDim(Level level) {
        return level != null && Level.f_46428_.equals((Object)level.m_46472_());
    }

    public static String dimName(Level level) {
        if (level == null) {
            return "?";
        }
        ResourceKey k = level.m_46472_();
        if (Level.f_46428_.equals((Object)k)) {
            return "\u4e3b\u4e16\u754c";
        }
        if (Level.f_46429_.equals((Object)k)) {
            return "\u4e0b\u754c";
        }
        if (Level.f_46430_.equals((Object)k)) {
            return "\u672b\u5730";
        }
        return k.m_135782_().toString();
    }

    public static boolean allow(Level level, Player player, String bossName) {
        if (SummonGate.isAllowedDim(level)) {
            return true;
        }
        String dim = SummonGate.dimName(level);
        if (player != null) {
            player.m_5661_((Component)Component.m_237110_((String)"msg.ftboss.overworld_only", (Object[])new Object[]{bossName, dim}), true);
        }
        FTBossMod.LOGGER.info("[FTBoss][\u53ec\u5524] \u963b\u6b62\u5728\u300c{}\u300d\u53ec\u5524 {} \u2014\u2014 \u5168\u90e8 Boss \u53ea\u80fd\u5728\u4e3b\u4e16\u754c\u53ec\u5524", (Object)dim, (Object)bossName);
        return false;
    }
}

