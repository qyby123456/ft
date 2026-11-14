/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.boss.ManorFight;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class ManorFlyBan {
    private static final Map<ServerLevel, Window> WINDOWS = new ConcurrentHashMap<ServerLevel, Window>();
    private static final Map<UUID, Integer> NOTIFY = new ConcurrentHashMap<UUID, Integer>();

    private ManorFlyBan() {
    }

    public static void trigger(ServerLevel serverLevel, double d, double d2, double d3) {
    }

    public static boolean active(ServerPlayer serverPlayer) {
        if (serverPlayer != null && serverPlayer.m_6084_() && !serverPlayer.m_7500_() && !serverPlayer.m_5833_()) {
            return ManorFight.isFlightBanned((Player)serverPlayer);
        }
        return false;
    }

    public static boolean stripFlight(ServerPlayer player) {
        boolean flying = false;
        if (player.m_150110_().f_35935_) {
            player.m_150110_().f_35935_ = false;
            player.m_6885_();
            flying = true;
        }
        if (player.m_21255_()) {
            player.m_36321_();
            flying = true;
        }
        return flying;
    }

    public static void notifyPlayer(ServerPlayer player) {
        int now = player.f_19797_;
        Integer next = NOTIFY.get(player.m_20148_());
        if (next != null && now < next) {
            return;
        }
        NOTIFY.put(player.m_20148_(), now + 60);
        player.m_5661_((Component)Component.m_237113_((String)"\u00a7c[\u751c\u82f9\u679c\u5e84\u56ed] \u00a77\u672c\u573a\u6218\u6597\u5168\u7a0b\u7981\u98de \u2014\u2014 \u8bf7\u5728\u5730\u9762\u63a5\u6218"), true);
    }

    public static void clear(ServerLevel level) {
        if (level == null) {
            WINDOWS.clear();
            NOTIFY.clear();
        } else {
            WINDOWS.remove(level);
        }
    }

    public static boolean anyActive(ServerLevel serverLevel) {
        if (serverLevel != null) {
            return ManorFight.hasActive(serverLevel);
        }
        return false;
    }

    public static boolean active(Player player) {
        ServerPlayer sp;
        return player instanceof ServerPlayer && ManorFlyBan.active(sp = (ServerPlayer)player);
    }

    private record Window(long until, double x, double y, double z) {
    }
}

