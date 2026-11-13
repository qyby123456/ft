package net.farmingtales.teamlag;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public final class ObservableReports {
    private record EntryAccess(Method type, Method position, Method rate, Method ticks) {}
    private static final ClassValue<EntryAccess> ENTRIES = new ClassValue<>() {
        @Override protected EntryAccess computeValue(Class<?> type) {
            try {
                Method entityType;
                try { entityType = type.getMethod("getType"); } catch (NoSuchMethodException ignored) { entityType = null; }
                return new EntryAccess(entityType, type.getMethod("getPosition"), type.getMethod("getRate"), type.getMethod("getTicks"));
            } catch (ReflectiveOperationException error) { throw new IllegalStateException(error); }
        }
    };
    private static final AtomicBoolean BUSY = new AtomicBoolean();
    private static volatile int applied;
    private static volatile String note = "尚未收到报告（可选）";
    private static volatile long lastTrigger;
    private static Object lastReport;
    private ObservableReports() {}
    public static synchronized void accept(Object report) {
        var server = LagQuery.server();
        if (server == null || report == null || report == lastReport || !BUSY.compareAndSet(false, true)) return;
        lastReport = report;
        CompletableFuture.runAsync(() -> {
            try {
                int ticks = ((Number) report.getClass().getMethod("getTicks").invoke(report)).intValue();
                if (ticks < 100) { note = "报告少于 100 tick，未采用"; return; }
                Map<ChunkKey, Double> costs = new HashMap<>();
                int entries = 0;
                for (String group : new String[] {"getBlocks", "getEntities"}) {
                    Map<?, ?> dimensions = (Map<?, ?>) report.getClass().getMethod(group).invoke(report);
                    for (var dimension : dimensions.entrySet()) {
                        for (Object entry : (Iterable<?>) dimension.getValue()) {
                            if (++entries > 500000) throw new IllegalArgumentException("报告超过 500000 条，已跳过");
                            var access = ENTRIES.get(entry.getClass());
                            if (group.equals("getEntities") && (access.type() == null || "minecraft:player".equals(String.valueOf(access.type().invoke(entry))))) continue;
                            var pos = (BlockPos) access.position().invoke(entry);
                            double nanos = ((Number) access.rate().invoke(entry)).doubleValue();
                            int calls = ((Number) access.ticks().invoke(entry)).intValue();
                            double micros = nanos * calls / ticks / 1000.0;
                            if (calls < 0 || !Double.isFinite(micros) || micros < 0) continue;
                            costs.merge(new ChunkKey(String.valueOf(dimension.getKey()), pos.getX() >> 4, pos.getZ() >> 4), micros,
                                    (left, right) -> Math.min(Double.MAX_VALUE, left + right));
                        }
                    }
                }
                server.execute(() -> {
                    if (LagQuery.server() != server) return;
                    LagQuery.acceptObserved(costs, ticks); applied++;
                    note = "已收到 " + applied + " 份报告，最近 " + ticks + " tick / " + costs.size() + " 区块（玩家已排除）";
                    LagQuery.LOGGER.info("[团队卡顿查询] OBS {}", note);
                });
            } catch (ReflectiveOperationException | RuntimeException error) {
                note = "报告解析失败，保留本地实测";
                LagQuery.warnOnce(ObservableReports.class, note, error);
            } finally { BUSY.set(false); }
        });
    }
    public static String status() { return "Observable：" + note; }
    static int appliedCount() { return applied; }
    static String trigger(ServerPlayer player, int seconds) {
        if (System.currentTimeMillis() - lastTrigger < 30000) return "请等待上次采样完成（触发间隔至少 30 秒）";
        try {
            Class<?> observable = Class.forName("observable.Observable");
            Object profiler = observable.getMethod("getPROFILER").invoke(observable.getField("INSTANCE").get(null));
            profiler.getClass().getMethod("runWithDuration", ServerPlayer.class, int.class, boolean.class).invoke(profiler, player, seconds, true);
            lastTrigger = System.currentTimeMillis();
            return "已触发 OBS " + seconds + " 秒真实采样，报告生成后自动更新团队查询；不会更改限制或收入";
        } catch (ReflectiveOperationException | LinkageError | RuntimeException error) { return "OBS 未安装或接口不兼容：" + error.getClass().getSimpleName(); }
    }
    static synchronized void clear() { lastReport = null; lastTrigger = 0; applied = 0; note = "尚未收到报告（可选）"; }
}
