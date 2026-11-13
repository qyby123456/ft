package net.farmingtales.teamlag;

import java.util.ArrayDeque;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

public final class RefinedStorageTiming {
    private static boolean observableAvailable = ModList.get().isLoaded("observable");
    private static long updates;
    private static long observableUpdates;
    private static final ArrayDeque<Sample> FREE_SAMPLES = new ArrayDeque<>();

    public static final class Sample {
        private ServerLevel level;
        private BlockPos pos;
        private boolean nativeTiming;
        private Object observation;
        private long started;

        private Sample() {}
    }

    private RefinedStorageTiming() {}

    public static Sample begin(Level level, BlockPos pos, Class<?> source) {
        if (!(level instanceof ServerLevel serverLevel) || !serverLevel.getServer().isSameThread()) return null;
        boolean nativeTiming = true;
        Object observation = null;
        if (observableAvailable) {
            try {
                observation = ObservableBlockTiming.begin(serverLevel, pos, source);
            } catch (RuntimeException | LinkageError exception) {
                observableAvailable = false;
                LagQuery.warnOnce(RefinedStorageTiming.class, "RS 的 Observable 计时接口不兼容，保留本地实测", new IllegalStateException(exception));
            }
        }
        if (!nativeTiming && observation == null) return null;
        Sample sample = FREE_SAMPLES.pollFirst();
        if (sample == null) sample = new Sample();
        sample.level = serverLevel;
        sample.pos = pos;
        sample.nativeTiming = nativeTiming;
        sample.observation = observation;
        sample.started = System.nanoTime();
        return sample;
    }

    public static void end(Sample sample) {
        if (sample == null) return;
        long elapsed = Math.max(0, System.nanoTime() - sample.started);
        try {
            updates++;
            if (sample.nativeTiming) {
                try {
                    LagQuery.record(sample.level, sample.pos, elapsed);
                } catch (RuntimeException exception) {
                    LagQuery.warnOnce(RefinedStorageTiming.class, "RS 本地耗时记录失败，未修改其更新行为", exception);
                }
            }
            if (sample.observation != null) {
                try {
                    ObservableBlockTiming.end(sample.observation, elapsed);
                    observableUpdates++;
                } catch (RuntimeException | LinkageError exception) {
                    observableAvailable = false;
                    LagQuery.warnOnce(RefinedStorageTiming.class, "RS 的 Observable 计时收尾失败，保留本地实测", new IllegalStateException(exception));
                }
            }
        } finally {
            sample.level = null;
            sample.pos = null;
            sample.observation = null;
            FREE_SAMPLES.addFirst(sample);
        }
    }

    public static String status() {
        if (!ModList.get().isLoaded("refinedstorage")) return "RS 计时：未安装 Refined Storage（可选）";
        return "RS 计时：控制器与节点按各自所在区块累计；已实测 " + updates + " 次更新，写入 OBS "
                + observableUpdates + " 次；OBS 接口=" + (observableAvailable ? "可用（仅采样时写入）" : "不可用/未安装")
                + "；只读计时，不修改存储库存";
    }

    static void clear() {
        updates = 0;
        observableUpdates = 0;
        FREE_SAMPLES.clear();
    }
}
