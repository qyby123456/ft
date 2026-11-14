/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class HiddenBossCastRules {
    public static final int DURATION_PERCENT = 35;
    public static final int MIN_WINDUP_TICKS = 3;
    public static final float POSE_BLEND_TICKS = 2.0f;

    private HiddenBossCastRules() {
    }

    public static int quicker(int originalTicks) {
        if (originalTicks <= 0) {
            return 0;
        }
        int scaled = (int)(((long)originalTicks * 35L + 99L) / 100L);
        return Math.min(originalTicks, Math.max(3, scaled));
    }
}

