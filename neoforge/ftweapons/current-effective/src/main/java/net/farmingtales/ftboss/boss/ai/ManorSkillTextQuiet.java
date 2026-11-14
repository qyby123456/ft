package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ManorFight;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public final class ManorSkillTextQuiet {
    private ManorSkillTextQuiet() {
    }

    public static void deliver(Player var0, Component var1, boolean var2) {
        if (!suppressed(var0)) {
            var0.displayClientMessage(var1, var2);
        }
    }

    public static boolean suppressed(Player var0) {
        if (var0 == null) {
            return false;
        } else {
            return var0.level() instanceof ServerLevel var2 ? ManorFight.hasActive(var2) : false;
        }
    }
}
