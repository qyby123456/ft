/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.animation.qingyicast;

public final class QingyiCastTime {
    private static final int[] RELEASE = new int[]{18, 22, 24, 12, 14, 14, 20, 12};

    private QingyiCastTime() {
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, Double.isFinite(d) ? d : 0.0));
    }

    private static double smooth(double d) {
        double d2 = QingyiCastTime.clamp(d);
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static Sample sample(int n, double d, int n2, int n3, double d2, boolean bl, double d3, double d4) {
        double d5;
        if (!(n >= 0 && n < RELEASE.length && Double.isFinite(d) && Double.isFinite(d2) && Double.isFinite(d3) && Double.isFinite(d4) && n2 >= 1 && n2 <= 200 && !(d < 0.0))) {
            return new Sample(0.0, 0.0);
        }
        if (n3 == 0) {
            return new Sample(0.42 * QingyiCastTime.clamp(d / (double)n2), QingyiCastTime.smooth(d / (double)Math.min(3, n2)));
        }
        if (n3 == 2) {
            return new Sample(QingyiCastTime.clamp(d3), (1.0 - QingyiCastTime.smooth(d / 6.0)) * QingyiCastTime.smooth(d2 / (double)Math.min(3, n2)));
        }
        if (n3 != 1) {
            return new Sample(0.0, 0.0);
        }
        if (n == 7 && !bl) {
            return new Sample(0.6 + Math.sin((d + d2) * 0.18) * 0.025, QingyiCastTime.smooth((d + d2) / 4.0));
        }
        if (n == 7) {
            return new Sample(d3 + (1.0 - d3) * QingyiCastTime.smooth((d - d4) / (double)RELEASE[n]), 1.0 - QingyiCastTime.smooth((d - d4 - (double)(RELEASE[n] - 6)) / 6.0));
        }
        double d6 = RELEASE[n] - 6;
        double d7 = d < 3.0 ? d3 + (0.55 - d3) * QingyiCastTime.smooth(d / 3.0) : (d5 = d < 5.0 ? 0.55 + 0.09 * QingyiCastTime.smooth((d - 3.0) / 2.0) : 0.64 + 0.16 * QingyiCastTime.smooth((d - 5.0) / (d6 - 5.0)));
        if (bl && d > d6) {
            d5 = 0.8 + 0.2 * QingyiCastTime.smooth((d - d6) / 6.0);
        }
        return new Sample(QingyiCastTime.clamp(d5), bl ? 1.0 - QingyiCastTime.smooth((d - d6) / 6.0) : 1.0);
    }

    public record Sample(double progress, double weight) {
    }
}

