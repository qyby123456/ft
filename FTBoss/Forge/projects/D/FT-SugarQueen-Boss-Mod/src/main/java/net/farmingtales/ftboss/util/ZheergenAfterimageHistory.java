package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.List;

public final class ZheergenAfterimageHistory<Payload> {
    public static final int INTERVAL_TICKS = 2;
    public static final int LIFETIME_TICKS = 10;
    public static final int MAX_IMAGES = 6;
    public static final double DRIFT_PER_TICK = 0.045;
    private final List<Sample<Payload>> samples = new ArrayList<>();
    private long lastTick = Long.MIN_VALUE;

    public boolean ready(long tick, boolean active) {
        expire(tick);
        return active && (lastTick == Long.MIN_VALUE || tick - lastTick >= INTERVAL_TICKS);
    }

    public void add(long tick, double positionX, double positionY, double positionZ, double backwardX, double backwardZ, Payload payload) {
        if (!Double.isFinite(positionX) || !Double.isFinite(positionY) || !Double.isFinite(positionZ) || !Double.isFinite(backwardX)
                || !Double.isFinite(backwardZ) || payload == null || !ready(tick, true)) return;
        samples.add(new Sample<>(tick, positionX, positionY, positionZ, backwardX, backwardZ, payload));
        lastTick = tick;
        if (samples.size() > MAX_IMAGES) samples.remove(0);
    }

    public void expire(long tick) {
        if (lastTick != Long.MIN_VALUE && tick < lastTick) clear();
        samples.removeIf(sample -> tick - sample.tick >= LIFETIME_TICKS);
    }

    public List<Sample<Payload>> samples() { return List.copyOf(samples); }

    public void clear() { samples.clear(); lastTick = Long.MIN_VALUE; }

    public static float alpha(double age) {
        if (!Double.isFinite(age) || age < 1 || age >= LIFETIME_TICKS) return 0;
        double remaining = 1 - age / LIFETIME_TICKS;
        return (float) (0.38 * remaining * remaining);
    }

    public record Sample<Payload>(long tick, double positionX, double positionY, double positionZ,
            double backwardX, double backwardZ, Payload payload) {}
}
