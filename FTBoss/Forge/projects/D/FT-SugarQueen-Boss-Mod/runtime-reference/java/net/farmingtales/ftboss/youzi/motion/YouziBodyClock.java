/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.youzi.motion;

public final class YouziBodyClock {
    public static Sample sample(String string, double d, double d2, double d3, double d4, double d5) {
        double d6;
        if (!Double.isFinite(d) || !Double.isFinite(d2) || !Double.isFinite(d3) || !Double.isFinite(d4) || !Double.isFinite(d5) || d3 <= 0.0 || d2 < 0.0 || "CHARGE".equals(string) && d2 == 0.0 || d4 <= 0.0 || d4 >= 1.0 || !"CHARGE".equals(string) && !"RELEASE".equals(string) && !"SUMMON".equals(string)) {
            return new Sample(0.0, 0.0, true);
        }
        if (string.equals("CHARGE")) {
            double d7 = Math.max(0.0, Math.min(1.0, d / Math.max(1.0, d2)));
            return new Sample(d7 * d4, YouziBodyClock.smooth(Math.max(0.0, d) / 2.0), d5 > 8.0);
        }
        double d8 = d6 = string.equals("SUMMON") ? d3 * 20.0 : d3 * (1.0 - d4) * 20.0;
        if (!Double.isFinite(d6) || d6 <= 0.0) {
            return new Sample(0.0, 0.0, true);
        }
        double d9 = Math.max(0.0, Math.min(1.0, d / Math.max(1.0, d6)));
        double d10 = string.equals("SUMMON") ? d9 : d4 + (1.0 - d4) * d9;
        return new Sample(d10, YouziBodyClock.smooth((d6 - d) / Math.min(5.0, d6 * 0.3)), d >= d6 || d < -2.0);
    }

    private static double smooth(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public record Sample(double phase, double weight, boolean expired) {
    }
}

