package net.farmingtales.ftboss.youzi;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.YouziSummonRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class YouziSummonScaling {
    private YouziSummonScaling() {}

    public static int playerCount(ServerLevel level, EasterBossEntity boss) {
        int players = 0;
        for (ServerPlayer player : level.players()) {
            if (YouziSummonRules.countsPlayer(player.isAlive(), player.isCreative(), player.isSpectator(),
                    player.isRemoved(), boss.distanceToSqr(player))) players++;
        }
        return Math.max(1, players);
    }
}
