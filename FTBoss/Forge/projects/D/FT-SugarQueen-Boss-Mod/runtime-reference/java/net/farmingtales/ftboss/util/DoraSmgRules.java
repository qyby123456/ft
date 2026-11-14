/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class DoraSmgRules {
    public static final int MAGAZINE_SIZE = 30;
    public static final int SHOT_INTERVAL = 2;
    public static final int RELOAD_TICKS = 36;
    public static final double RANGE = 28.0;
    public static final double EFFECTIVE_RANGE = 14.0;
    public static final float DAMAGE = 5.0f;
    public static final float DAMAGE_MULTIPLIER = 4.0f;

    private DoraSmgRules() {
    }

    public static boolean shouldDrop(int variant, boolean mini, boolean giant, boolean noAi, boolean finale, boolean playerKill) {
        return variant == 2 && !mini && !giant && !noAi && !finale && playerKill;
    }

    public static int rounds(int stored) {
        return Math.max(0, Math.min(30, stored));
    }

    public static int reloadTicks(int stored) {
        return Math.max(0, Math.min(36, stored));
    }

    public static float damage(double distance) {
        if (!Double.isFinite(distance) || distance < 0.0 || distance > 28.0) {
            return 0.0f * 2.0f;
        }
        double falloff = Math.max(0.0, (distance - 14.0) / 14.0);
        return (float)(10.0 * (1.0 - 0.5 * falloff)) * 2.0f;
    }
}

