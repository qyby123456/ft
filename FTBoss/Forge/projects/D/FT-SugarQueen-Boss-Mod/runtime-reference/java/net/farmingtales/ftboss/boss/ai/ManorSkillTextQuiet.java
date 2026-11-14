/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ManorFight;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class ManorSkillTextQuiet {
    private ManorSkillTextQuiet() {
    }

    public static void deliver(Player player, Component component, boolean bl) {
        if (ManorSkillTextQuiet.suppressed(player)) {
            return;
        }
        player.m_5661_(component, bl);
    }

    public static boolean suppressed(Player player) {
        if (player == null) {
            return false;
        }
        Level level = player.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        return ManorFight.hasActive(serverLevel);
    }
}

