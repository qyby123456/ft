package net.farmingtales.ftboss.util;

public final class ManorIntroTimeline {
    public static final int DURATION_SECONDS = 46;
    public static final int DURATION_TICKS = DURATION_SECONDS * 20;
    public static final long DURATION_MILLIS = DURATION_SECONDS * 1000L;
    public static final long SEAL_MILLIS = 8_000L;
    public static final long ORBIT_FULL_MILLIS = 12_000L;
    public static final long ORBIT_MILLIS = 24_000L;
    public static final long ASCENT_MILLIS = 36_000L;
    public static final long RELEASE_MILLIS = 43_000L;
    public static final long FADE_IN_MILLIS = 2_000L;
    public static final long FADE_OUT_MILLIS = 1_000L;
    public static final long RUNE_STEP_MILLIS = 500L;
    public static final long RUNE_FADE_MILLIS = 3_000L;
    public static final long DIALOGUE_END_MILLIS = 40_000L;
    public static final long SYNC_MILLIS = 2_000L;

    private ManorIntroTimeline() {}

    public static long dialogueMillis(int index, int count) {
        return count <= 1 ? 0L : Math.max(0, index) * DIALOGUE_END_MILLIS / (count - 1);
    }

    public static float progress(long elapsedMillis) {
        return Math.max(0.0F, Math.min(1.0F, elapsedMillis / (float) DURATION_MILLIS));
    }

    public static float ramp(double elapsedMillis, long fromMillis, long toMillis) {
        float progress = (float) Math.max(0.0, Math.min(1.0,
                (elapsedMillis - fromMillis) / (toMillis - fromMillis)));
        return progress * progress * (3.0F - 2.0F * progress);
    }
}
