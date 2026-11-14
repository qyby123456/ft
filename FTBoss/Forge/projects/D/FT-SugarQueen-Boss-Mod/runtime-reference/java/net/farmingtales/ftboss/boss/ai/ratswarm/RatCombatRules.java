/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.boss.ai.ratswarm;

public final class RatCombatRules {
    public static final int SPELL_COOLDOWN = 90;
    public static final int MELEE_COOLDOWN = 32;
    public static final int BOLT_LIFETIME = 42;
    public static final int FOG_LIFETIME = 50;
    public static final double FOG_RADIUS = 2.5;
    public static final double WAVE_RADIUS = 5.5;
    public static final int FANG_WINDUP = 10;
    public static final int FANG_IMPACT = 16;

    public static double lungeSpeed(double d) {
        return Double.isFinite(d) ? Math.max(0.0, Math.min(0.52, d - 0.8)) : 0.0;
    }

    private RatCombatRules() {
    }

    public static int spell(int n, double d) {
        int n2 = Math.floorMod(n, 3);
        return n2 == 2 && d > 5.9 ? 0 : n2;
    }

    public static double waveRadius(int n) {
        return Math.max(0.0, Math.min(5.5, (double)(n - 14) * 0.4));
    }

    public static boolean inCone(double d, double d2, double d3, double d4, double d5) {
        double d6 = d * d + d2 * d2;
        if (d6 > d5 * d5) {
            return false;
        }
        if (d6 < 1.0E-4) {
            return true;
        }
        double d7 = Math.hypot(d3, d4);
        if (d7 < 1.0E-4) {
            return false;
        }
        return (d * d3 + d2 * d4) / (Math.sqrt(d6) * d7) >= 0.45;
    }

    public static boolean inWave(double d, double d2) {
        return d2 > 0.0 && d <= d2 + 0.45 && d >= Math.max(0.0, d2 - 0.65);
    }
}

