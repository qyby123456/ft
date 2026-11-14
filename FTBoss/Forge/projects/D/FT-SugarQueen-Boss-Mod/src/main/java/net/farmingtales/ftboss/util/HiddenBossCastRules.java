package net.farmingtales.ftboss.util;

public final class HiddenBossCastRules {
    public static final int DURATION_PERCENT = 35;
    public static final int MIN_WINDUP_TICKS = 3;
    public static final float POSE_BLEND_TICKS = 2.0F;

    private HiddenBossCastRules() {}

    public static int quicker(int originalTicks) {
        if (originalTicks <= 0) return 0;
        int scaled = (int) (((long) originalTicks * DURATION_PERCENT + 99) / 100);
        return Math.min(originalTicks, Math.max(MIN_WINDUP_TICKS, scaled));
    }
}
