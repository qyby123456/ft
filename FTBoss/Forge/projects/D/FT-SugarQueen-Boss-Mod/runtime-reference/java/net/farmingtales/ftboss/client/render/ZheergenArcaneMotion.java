/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

public final class ZheergenArcaneMotion {
    private ZheergenArcaneMotion() {
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    private static double ease(double d) {
        return (1.0 - Math.cos(ZheergenArcaneMotion.clamp(d) * Math.PI)) * 0.5;
    }

    public static Frame frame(int n, double d, boolean bl) {
        double d2 = 0.0;
        double d3 = 0.0;
        double d4 = 0.0;
        double d5 = 1.0;
        double d6 = 1.0;
        double d7 = 1.0;
        double d8 = 0.0;
        double d9 = 1.0;
        if (n >= 23 && n <= 25) {
            int n2 = n - 23;
            double d10 = ZheergenArcaneMotion.ease((d - (double)n2 * 0.55) / (double)(bl ? 10 : 5));
            d7 = bl ? 0.9 + 0.1 * d10 : 1.0 - 0.025 * Math.sin(d * 0.25 + (double)n2);
            d5 = d7;
            d6 = bl ? 0.12 + 0.45 * d10 : 0.78 + 0.22 * ZheergenArcaneMotion.ease(d / 5.0);
            d9 = bl ? 0.18 + 0.32 * d10 : (0.84 + 0.1 * Math.sin(d * 0.21 + (double)n2)) * (1.0 - ZheergenArcaneMotion.ease((d - 48.0) / 12.0));
        } else if (n >= 33 && n <= 37) {
            int n3 = n - 33;
            double d11 = Math.max(0.0, d - (double)n3 * 0.66);
            double d12 = ZheergenArcaneMotion.ease(d11 / 1.65);
            double d13 = ZheergenArcaneMotion.ease((d11 - 4.0) / 8.0);
            double d14 = n3 % 2 == 0 ? 1.0 : -1.0;
            d5 = bl ? 0.72 + 0.14 * ZheergenArcaneMotion.ease(d / 10.0) : 0.26 + 0.74 * d12;
            d6 = bl ? 0.78 + 0.09 * ZheergenArcaneMotion.ease(d / 10.0) : 0.68 + 0.32 * d12;
            d2 = bl ? 0.0 : d14 * 0.44 * (1.0 - d12) + 0.08 * (double)(n3 - 2) * d13;
            d3 = bl ? 0.0 : (double)(2 - n3) * 0.12 * (1.0 - d12);
            d4 = bl ? 0.0 : d14 * 0.1 * Math.sin(d13 * Math.PI);
            d8 = bl ? (double)(n3 - 2) * 0.012 * ZheergenArcaneMotion.ease(d / 10.0) : d14 * (0.07 * (1.0 - d12) - 0.025 * d13);
            d9 = bl ? 0.1 + 0.22 * ZheergenArcaneMotion.ease(d / 10.0) : d12 * (1.0 - ZheergenArcaneMotion.ease((d11 - 10.0) / 10.0));
        } else if (n >= 38 && n <= 40) {
            int n4 = n - 38;
            double d15 = Math.max(0.0, d - (double)n4 * 0.65);
            double d16 = ZheergenArcaneMotion.ease(d15 / 8.0);
            double d17 = ZheergenArcaneMotion.ease((d15 - 14.0) / 10.0);
            d5 = bl ? 0.56 + 0.12 * ZheergenArcaneMotion.ease(d / 10.0) : 0.82 + 0.23 * Math.sin(d16 * Math.PI) - 0.1 * d17;
            d6 = bl ? 0.4 + 0.25 * ZheergenArcaneMotion.ease(d / 10.0) : 1.32 - 0.64 * d16 - 0.15 * d17;
            d7 = bl ? 0.47 + 0.16 * ZheergenArcaneMotion.ease(d / 10.0) : 0.64 + 0.63 * d16 + 0.11 * d17;
            d2 = bl ? 0.0 : (double)(n4 - 1) * 0.13 * Math.sin(d16 * Math.PI);
            d3 = bl ? -0.32 * (1.0 - ZheergenArcaneMotion.ease(d / 10.0)) : 0.09 * Math.sin(d16 * Math.PI) - 0.24 * d17;
            d4 = bl ? -2.2 : -2.6 + 4.9 * ZheergenArcaneMotion.ease(d15 / 19.0);
            d8 = bl ? 0.0 : (double)(n4 - 1) * 0.07 * Math.sin(d15 * 0.31);
            d9 = bl ? 0.35 + 0.35 * ZheergenArcaneMotion.ease(d / 10.0) : ZheergenArcaneMotion.ease((d15 + 0.8) / 2.0) * (1.0 - ZheergenArcaneMotion.ease((d15 - 20.0) / 6.0));
        } else if (n >= 41 && n <= 43) {
            int n5 = n - 41;
            double d18 = Math.max(0.0, d - 3.8 - (double)n5 * 0.65);
            double d19 = ZheergenArcaneMotion.ease(d18 / 16.0);
            d5 = bl ? 0.65 : 1.0 + 0.16 * d19;
            d6 = bl ? 0.42 : 0.52 - 0.27 * d19;
            d7 = bl ? 0.45 : 0.62 + 0.52 * d19;
            d3 = -0.06;
            d4 = bl ? -2.2 : -1.3 + 1.9 * d19;
            d9 = bl ? 0.0 : ZheergenArcaneMotion.ease(d18 / 3.0) * (1.0 - ZheergenArcaneMotion.ease((d - 19.0) / 7.0));
        } else if (n == 44) {
            double d20 = ZheergenArcaneMotion.ease((d - 7.0) / 9.0);
            double d21 = ZheergenArcaneMotion.ease((d - 17.0) / 9.0);
            d7 = bl ? 0.5 : 0.55 + 0.75 * d20;
            d5 = d7;
            d6 = bl ? 0.5 : 0.85 + 0.45 * Math.sin(d20 * Math.PI) - 0.45 * d21;
            d3 = bl ? -0.5 : 0.52 * Math.sin(d20 * Math.PI) - 0.4 * d21;
            d4 = bl ? -1.5 : -0.6 + 2.6 * d20;
            d9 = bl ? 0.0 : ZheergenArcaneMotion.ease((d - 6.0) / 4.0) * (1.0 - ZheergenArcaneMotion.ease((d - 19.0) / 7.0));
        }
        return new Frame(d2, d3, d4, d5, d6, d7, d8, ZheergenArcaneMotion.clamp(d9));
    }

    public static void position(double[] dArray, double[] dArray2, Frame frame, double[] dArray3) {
        double d = (dArray[0] - dArray2[0]) * frame.scale();
        double d2 = (dArray[1] - dArray2[1]) * frame.verticalScale();
        double d3 = Math.cos(frame.angle());
        double d4 = Math.sin(frame.angle());
        dArray3[0] = dArray2[0] + d * d3 - d2 * d4 + frame.horizontal();
        dArray3[1] = dArray2[1] + d * d4 + d2 * d3 + frame.vertical();
        dArray3[2] = dArray2[2] + (dArray[2] - dArray2[2]) * frame.depthScale() + frame.depth();
    }

    public record Frame(double horizontal, double vertical, double depth, double scale, double verticalScale, double depthScale, double angle, double alpha) {
    }
}

