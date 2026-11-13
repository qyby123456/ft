package net.farmingtales.teamlag;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(LagQuery.ID)
public final class LagQuery {
    public static final String ID = "ftteamlag";
    public static final Logger LOGGER = LogUtils.getLogger();
    static final ForgeConfigSpec SPEC;
    static final ForgeConfigSpec.IntValue WINDOW;
    static final ForgeConfigSpec.BooleanValue ONLINE_ONLY;
    static {
        var builder = new ForgeConfigSpec.Builder();
        WINDOW = builder.comment("Read-only timing window in server ticks").defineInRange("measurementWindowTicks", 1200, 100, 24000);
        ONLINE_ONLY = builder.comment("Divide by online members; zero online is reported as N/A").define("perCapitaOnlineOnly", true);
        SPEC = builder.build();
    }
    private static final class Counter { long nanos; }
    private static final Map<ChunkKey, Counter> CURRENT = new HashMap<>();
    private static final Set<Class<?>> WARNED = new HashSet<>();
    private static Map<ChunkKey, Double> previous = Map.of();
    private static Map<ChunkKey, Double> observed = Map.of();
    private static int previousTicks;
    private static int observedTicks;
    private static int ticks;
    private static long observedAt;
    private static MinecraftServer server;
    record Measurement(double micros, int sampled, int ticks, String source) {}

    public LagQuery() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "ftteamlag.toml");
        MinecraftForge.EVENT_BUS.addListener(this::started);
        MinecraftForge.EVENT_BUS.addListener(this::stopping);
        MinecraftForge.EVENT_BUS.addListener(this::tick);
        MinecraftForge.EVENT_BUS.addListener(QueryCommands::register);
    }
    private void started(ServerStartedEvent event) {
        clear(); server = event.getServer();
        TimingHooks.check();
        LOGGER.info("[团队卡顿查询] 只读模式：不限制区块、不清理动物、不修改经济，不读取旧限制存档");
    }
    private void stopping(ServerStoppingEvent event) { if (server == event.getServer()) { server = null; clear(); } }
    private static void clear() {
        CURRENT.clear(); previous = Map.of(); observed = Map.of(); WARNED.clear();
        ticks = 0; previousTicks = 0; observedTicks = 0; observedAt = 0;
        MachineTiming.clear(); RefinedStorageTiming.clear(); ObservableReports.clear();
    }
    private void tick(TickEvent.ServerTickEvent event) {
        if (server == null || !server.isSameThread()) return;
        if (event.phase == TickEvent.Phase.START) { MachineTiming.clear(); ticks++; return; }
        if (ticks < WINDOW.get()) return;
        previous = localSamples(); previousTicks = ticks;
        CURRENT.clear(); ticks = 0;
    }
    public static MinecraftServer server() { return server; }
    public static void record(ServerLevel level, BlockPos pos, long nanos) {
        if (level.getServer() != server || !server.isSameThread() || nanos < 0) return;
        Counter counter = CURRENT.computeIfAbsent(ChunkKey.at(level, pos), ignored -> new Counter());
        counter.nanos = counter.nanos > Long.MAX_VALUE - nanos ? Long.MAX_VALUE : counter.nanos + nanos;
    }
    static double measuredMicros(ChunkKey key) { Counter counter = CURRENT.get(key); return counter == null ? 0 : counter.nanos / 1000.0; }
    private static Map<ChunkKey, Double> localSamples() {
        Map<ChunkKey, Double> samples = new HashMap<>();
        if (ticks > 0) CURRENT.forEach((key, counter) -> samples.put(key, counter.nanos / 1000.0 / ticks));
        return samples;
    }
    static Measurement measurement(Collection<ChunkKey> chunks) {
        Map<ChunkKey, Double> samples = localSamples();
        boolean completed = chunks.stream().noneMatch(samples::containsKey);
        if (completed) samples = new HashMap<>(previous);
        boolean local = chunks.stream().anyMatch(samples::containsKey);
        boolean fresh = observedAt > 0 && System.currentTimeMillis() - observedAt < WINDOW.get() * 100L;
        if (fresh) for (var sample : observed.entrySet()) samples.merge(sample.getKey(), sample.getValue(), Math::max);
        double total = 0; int count = 0;
        for (ChunkKey key : chunks) {
            Double micros = samples.get(key);
            if (micros == null || !Double.isFinite(micros) || micros < 0) continue;
            total = Math.min(Double.MAX_VALUE, total + micros); count++;
        }
        return new Measurement(total, count, local ? (completed ? previousTicks : ticks) : observedTicks,
                local ? (completed ? "上一完整窗口" : "当前窗口（尚未结束）") + (fresh ? " + OBS（同区块取较大值）" : "") : "仅 OBS 报告");
    }
    static void acceptObserved(Map<ChunkKey, Double> samples, int reportTicks) {
        observed = Map.copyOf(samples); observedTicks = reportTicks; observedAt = System.currentTimeMillis();
    }
    static int observedChunks() { return observed.size(); }
    static String status() {
        return "只读团队卡顿查询；当前窗口 " + ticks + "/" + WINDOW.get() + " tick，实测 " + CURRENT.size()
                + " 个区块；OBS " + observed.size() + " 个区块/" + observedTicks + " tick；无冻结、动物清理或经济功能";
    }
    public static void warnOnce(Class<?> type, String message, Throwable error) {
        if (WARNED.add(type)) LOGGER.warn("[团队卡顿查询] " + message, error);
    }
}
