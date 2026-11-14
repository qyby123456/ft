/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.zicaicast;

public final class ZicaiCastTiming {
    private static final int[] RELEASE = new int[]{16, 22, 20};

    private ZicaiCastTiming() {
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, Double.isFinite(d) ? d : 0.0));
    }

    private static double ease(double d) {
        double d2 = ZicaiCastTiming.clamp(d);
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static Sample sample(int n, double d, int n2, int n3, double d2) {
        if (n < 0 || n >= 3 || !Double.isFinite(d) || !Double.isFinite(d2) || n2 < 1 || n2 > 200 || d < 0.0) {
            return new Sample(0.0, 0.0);
        }
        if (n3 == 0) {
            return new Sample(0.42 * ZicaiCastTiming.clamp(d / (double)n2), ZicaiCastTiming.ease(d / (double)Math.min(4, n2)) * (1.0 - ZicaiCastTiming.ease((d - (double)n2 - 20.0) / 6.0)));
        }
        if (n3 == 2) {
            return new Sample(0.42 * ZicaiCastTiming.clamp(d2 / (double)n2), (1.0 - ZicaiCastTiming.ease(d / 6.0)) * ZicaiCastTiming.ease(d2 / (double)Math.min(4, n2)));
        }
        if (n3 != 1) {
            return new Sample(0.0, 0.0);
        }
        double d3 = 0.42 * ZicaiCastTiming.clamp(d2 / (double)n2);
        double d4 = ZicaiCastTiming.clamp(d / (double)RELEASE[n]);
        return new Sample(d3 + (1.0 - d3) * d4, 1.0 - ZicaiCastTiming.ease((d - (double)(RELEASE[n] - 6)) / 6.0));
    }

    public record Sample(double progress, double weight) {
    }
}

