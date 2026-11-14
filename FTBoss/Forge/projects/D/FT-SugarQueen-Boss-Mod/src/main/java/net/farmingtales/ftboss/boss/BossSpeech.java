package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 【分行定时播报】(2026-09-12 按用户要求): 隐藏 Boss 登场时那种"一句一句念出来"的台词。
 *
 * <p>用法(隐藏 Boss 登场处调用一次即可):
 * <pre>
 *   BossSpeech.speak(level, x, y, z, 64.0, 45,
 *       "§6哦哦，此乃吾辈之戟也。",
 *       "§6则汝当是勇士也？", ...);
 * </pre>
 * 每句间隔 {@code intervalTicks}(45 tick ≈ 2.25 秒), 播给半径内的玩家。
 * 台词队列由 {@link #Ticker} 在服务端每 tick 推进 —— 即使召唤者(Boss)已经退场也照常播完。
 */
public class BossSpeech {

    private static final List<Line> PENDING = new CopyOnWriteArrayList<>();

    private static final class Line {
        final ResourceKey<Level> dim;
        final double x, y, z, radius;
        final long due;
        final String text;

        Line(ResourceKey<Level> dim, double x, double y, double z, double radius, long due, String text) {
            this.dim = dim;
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
            this.due = due;
            this.text = text;
        }
    }

    /** 排入一段台词(每句间隔 intervalTicks), 到点自动播给 radius 格内的玩家 */
    public static void speak(Level level, double x, double y, double z,
                             double radius, int intervalTicks, String... lines) {
        if (level == null || level.isClientSide) return;
        long now = level.getGameTime();
        int n = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i] == null || lines[i].isEmpty()) continue;
            PENDING.add(new Line(level.dimension(), x, y, z, radius, now + (long) i * intervalTicks, lines[i]));
            n++;
        }
        FTBossMod.LOGGER.info("[FTBoss] 台词排队: {} 句, 间隔 {} tick", n, intervalTicks);
    }

    /** 立即清空待播台词(换阶段/重开时可用) */
    public static void clear() {
        PENDING.clear();
    }

    /** 服务端每 tick 推进台词队列 */
    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
    public static final class Ticker {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent e) {
            if (e.phase != net.minecraftforge.event.TickEvent.Phase.END || PENDING.isEmpty()) return;
            var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;
            /* 2026-09-12 修崩溃: CopyOnWriteArrayList 的迭代器**不支持 remove()**
               (之前用 it.remove() -> UnsupportedOperationException, 台词一到点就崩服务器 tick 循环)。
               改成先收集、再 removeAll。 */
            java.util.List<Line> done = null;
            for (Line l : PENDING) {
                var sl = server.getLevel(l.dim);
                if (sl == null) {
                    if (done == null) done = new java.util.ArrayList<>();
                    done.add(l);
                    continue;
                }
                if (sl.getGameTime() < l.due) continue;
                for (ServerPlayer p : sl.players()) {
                    if (p.distanceToSqr(l.x, l.y, l.z) <= l.radius * l.radius) {
                        p.sendSystemMessage(Component.literal(l.text));
                    }
                }
                if (done == null) done = new java.util.ArrayList<>();
                done.add(l);
            }
            if (done != null && !done.isEmpty()) PENDING.removeAll(done);
        }
    }
}
