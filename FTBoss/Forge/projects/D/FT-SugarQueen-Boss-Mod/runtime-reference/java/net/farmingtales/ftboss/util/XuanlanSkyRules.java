/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class XuanlanSkyRules {
    public static final int MAX_USES = 2;
    public static final int STRIKES = 60;
    public static final int ASCEND_TICKS = 20;
    public static final int WARNING_TICKS = 10;
    public static final int STRIKE_PERIOD_TICKS = 26;
    public static final int STRIKES_PER_PERIOD = 3;
    public static final int DESCEND_TICKS = 20;
    public static final int RECOVERY_TICKS = 200;
    public static final double HEIGHT = 24.0;
    public static final double RADIUS = 3.0;
    public static final double RANGE = 64.0;
    public static final float DAMAGE = 230.0f;

    private XuanlanSkyRules() {
    }

    public static int warningTick(int strikeIndex) {
        return strikeIndex * 26 / 3;
    }

    public static int attackTicks() {
        return XuanlanSkyRules.warningTick(59) + 10 + 1;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static boolean ready(int uses, float health, float maximum) {
        if (uses < 0) return false;
        if (uses >= 2) return false;
        if (!Float.isFinite(health)) return false;
        if (!Float.isFinite(maximum)) return false;
        if (!(health > 0.0f)) return false;
        if (!(maximum > 0.0f)) return false;
        float f = uses == 0 ? 0.7f : 0.35f;
        if (!(health <= maximum * f)) return false;
        return true;
    }

    public static double rise(double progress) {
        double bounded = Math.max(0.0, Math.min(1.0, progress));
        return bounded * bounded * (3.0 - 2.0 * bounded);
    }
}

