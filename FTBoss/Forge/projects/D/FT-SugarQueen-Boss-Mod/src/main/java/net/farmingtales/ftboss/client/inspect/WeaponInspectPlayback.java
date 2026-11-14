package net.farmingtales.ftboss.client.inspect;

/** 视检时钟；2026-09-17，仅客户端 tick 推进，重复渲染不推进，提前收回耗时 6 tick。 */
public final class WeaponInspectPlayback {
    public static final int TICKS_PER_SECOND = 20;
    public static final int RETURN_TICKS = 6;
    private WeaponInspectMotion.Clip clip;
    private WeaponInspectMotion.Pose returnPose;
    private int ticks;
    private int previousTicks;

    public void start(WeaponInspectMotion.Clip selected) {
        clear();
        clip = selected;
    }

    public boolean active() {
        return clip != null;
    }

    public boolean returning() {
        return returnPose != null;
    }

    public void tick() {
        if (!active()) return;
        previousTicks = ticks++;
        double durationTicks = returning() ? RETURN_TICKS : clip.duration() * TICKS_PER_SECOND;
        if (previousTicks >= durationTicks) clear();
    }

    public WeaponInspectMotion.Pose sample(double partialTick) {
        if (!active()) return WeaponInspectMotion.REST;
        double time = previousTicks + (ticks - previousTicks) * Math.max(0, Math.min(1, partialTick));
        return returning() ? returnPose.blend(WeaponInspectMotion.REST, time / RETURN_TICKS)
                : clip.sample(time / TICKS_PER_SECOND);
    }

    public void finish(double partialTick) {
        if (!active() || returning()) return;
        returnPose = sample(partialTick);
        previousTicks = ticks = 0;
    }

    public void clear() {
        clip = null;
        returnPose = null;
        previousTicks = ticks = 0;
    }
}
