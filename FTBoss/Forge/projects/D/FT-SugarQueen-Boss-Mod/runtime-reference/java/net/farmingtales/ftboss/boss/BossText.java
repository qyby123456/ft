/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 */
package net.farmingtales.ftboss.boss;

import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class BossText {
    public static final boolean QUIET = true;

    private BossText() {
    }

    public static void essential(Player player, String text) {
        if (player == null || text == null) {
            return;
        }
        player.m_5661_((Component)Component.m_237113_((String)text), true);
    }

    public static void flavor(String tag, String text) {
        if (text == null) {
            return;
        }
        FTBossMod.LOGGER.info("[FTBoss][\u6587\u672c\u00b7\u5df2\u9759\u97f3][{}] {}", (Object)tag, (Object)text);
    }

    public static void flavorWithHint(Player player, String tag, String longText, String shortHint) {
        BossText.flavor(tag, longText);
        if (shortHint != null) {
            BossText.essential(player, shortHint);
        }
    }

    public static void essentialNearby(List<ServerPlayer> players, String text) {
        if (players == null || text == null) {
            return;
        }
        MutableComponent c = Component.m_237113_((String)text);
        for (ServerPlayer p : players) {
            if (p == null) continue;
            p.m_5661_((Component)c, true);
        }
    }
}

