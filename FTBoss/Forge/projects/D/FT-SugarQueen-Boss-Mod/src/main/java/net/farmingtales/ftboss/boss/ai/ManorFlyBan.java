package net.farmingtales.ftboss.boss.ai;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.util.ManorChaseRules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;


public final class ManorFlyBan {
    private static final Map<ServerLevel, Window> WINDOWS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> NOTIFY = new ConcurrentHashMap<>();

    private record Window(long until, double x, double y, double z) {}

    private ManorFlyBan() {}

    public static void trigger(ServerLevel level, double x, double y, double z) {
    }

    public static boolean active(ServerPlayer player) {
        return player != null && player.isAlive() && !player.isCreative() && !player.isSpectator()
                && net.farmingtales.ftboss.boss.ManorFight.isFlightBanned(player);
    }

    /** 掐断飞行；返回 true 表示这一次**真的**打断了一个正在飞的玩家。 */
    public static boolean stripFlight(ServerPlayer player) {
        boolean flying = false;
        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            flying = true;
        }
        if (player.isFallFlying()) {
            player.stopFallFlying();
            flying = true;
        }
        return flying;
    }

    /** 给被禁飞的玩家一条提示(每 {@link ManorChaseRules#FLY_BAN_NOTIFY_TICKS} tick 最多一条)。 */
    public static void notifyPlayer(ServerPlayer player) {
        int now = player.tickCount;
        Integer next = NOTIFY.get(player.getUUID());
        if (next != null && now < next) return;
        NOTIFY.put(player.getUUID(), now + ManorChaseRules.FLY_BAN_NOTIFY_TICKS);
        player.displayClientMessage(Component.literal(
                "§c[甜苹果庄园] §7本场战斗全程禁飞 —— 请在地面接战"), true);
    }

    /** 战斗结束 / 换维度 / 服务器停机时清理。 */
    public static void clear(ServerLevel level) {
        if (level == null) {
            WINDOWS.clear();
            NOTIFY.clear();
        } else {
            WINDOWS.remove(level);
        }
    }

    public static boolean anyActive(ServerLevel level) {
        return level != null && net.farmingtales.ftboss.boss.ManorFight.hasActive(level);
    }

    /** 兼容旧写法的别名（Player 侧判定）。 */
    public static boolean active(Player player) {
        return player instanceof ServerPlayer sp && active(sp);
    }
}
