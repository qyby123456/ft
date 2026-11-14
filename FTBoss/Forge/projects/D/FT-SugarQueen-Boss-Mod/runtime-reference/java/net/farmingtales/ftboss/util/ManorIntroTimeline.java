/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class ManorIntroTimeline {
    public static final int DURATION_SECONDS = 46;
    public static final int DURATION_TICKS = 920;
    public static final long DURATION_MILLIS = 46000L;
    public static final long SEAL_MILLIS = 8000L;
    public static final long ORBIT_FULL_MILLIS = 12000L;
    public static final long ORBIT_MILLIS = 24000L;
    public static final long ASCENT_MILLIS = 36000L;
    public static final long RELEASE_MILLIS = 43000L;
    public static final long FADE_IN_MILLIS = 2000L;
    public static final long FADE_OUT_MILLIS = 1000L;
    public static final long RUNE_STEP_MILLIS = 500L;
    public static final long RUNE_FADE_MILLIS = 3000L;
    public static final long DIALOGUE_END_MILLIS = 40000L;
    public static final long SYNC_MILLIS = 2000L;

    private ManorIntroTimeline() {
    }

    public static long dialogueMillis(int index, int count) {
        return count <= 1 ? 0L : (long)Math.max(0, index) * 40000L / (long)(count - 1);
    }

    public static float progress(long elapsedMillis) {
        return Math.max(0.0f, Math.min(1.0f, (float)elapsedMillis / 46000.0f));
    }

    public static float ramp(double elapsedMillis, long fromMillis, long toMillis) {
        float progress = (float)Math.max(0.0, Math.min(1.0, (elapsedMillis - (double)fromMillis) / (double)(toMillis - fromMillis)));
        return progress * progress * (3.0f - 2.0f * progress);
    }
}

