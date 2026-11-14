/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

public final class ZheergenSpellMotion {
    private static final double[] SWING_TIMES = new double[]{0.0, 0.75, 1.375, 1.9583};
    private static final double[] SWING_VALUES = new double[]{0.0, -115.9351, 134.06, 0.0};
    private static final double[] ROOT_TIMES = new double[]{0.0, 0.68};
    private static final double[] ROOT_VALUES = new double[]{-22.0, 0.0};
    private static final double[] RIFT_TIMES = new double[]{0.0, 0.72, 1.56};
    private static final double[] RIFT_VALUES = new double[]{12.5, -6.58344, 0.0};

    private ZheergenSpellMotion() {
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    private static double pulse(double d) {
        return Math.sin(ZheergenSpellMotion.clamp(d / 12.0) * Math.PI) * Math.exp(-d / 28.0);
    }

    private static double sample(double[] dArray, double[] dArray2, double d) {
        for (int i = 1; i < dArray.length; ++i) {
            if (!(d <= dArray[i])) continue;
            double d2 = ZheergenSpellMotion.clamp((d - dArray[i - 1]) / (dArray[i] - dArray[i - 1]));
            d2 = (1.0 - Math.cos(d2 * Math.PI)) * 0.5;
            return dArray2[i - 1] + (dArray2[i] - dArray2[i - 1]) * d2;
        }
        return dArray2[dArray2.length - 1];
    }

    private static double nail(double d, boolean bl) {
        return ZheergenSpellMotion.sample(ROOT_TIMES, ROOT_VALUES, Math.min(0.68, Math.max(0.0, d) / (double)(bl ? 10 : 14) * 0.68)) / 22.0 * (bl ? -0.11 : 0.16);
    }

    public static State state(int n, double d, boolean bl) {
        double d2;
        double d3 = bl ? 0.15 : ZheergenSpellMotion.pulse(Math.max(0.0, d));
        double d4 = 0.0;
        double d5 = 0.0;
        double d6 = 0.0;
        double d7 = 1.0;
        double d8 = 1.0;
        double d9 = 0.0;
        if (n >= 20 && n <= 22) {
            d5 = ZheergenSpellMotion.nail(d, bl);
            d9 = (n == 20 ? -1.0 : (n == 21 ? 1.0 : 0.5)) * 0.06 * d3;
        }
        if (n == 23) {
            d7 = 1.0 - 0.065 * d3;
            d8 = 1.0 - 0.035 * d3;
        }
        if (n == 24) {
            d5 = 0.065 * d3;
        }
        if (n >= 25 && n <= 28) {
            d2 = n % 2 == 1 ? -1.0 : 1.0;
            boolean bl2 = n >= 27;
            double d10 = bl2 ? ZheergenSpellMotion.pulse(Math.max(0.0, d - 3.0)) * 0.8 : d3;
            d4 = d2 * 0.14 * d10;
            d6 = bl2 ? 0.1 * d10 : 0.0;
            d9 = d2 * ZheergenSpellMotion.sample(RIFT_TIMES, RIFT_VALUES, ZheergenSpellMotion.clamp(Math.max(0.0, d - (double)(bl2 ? 3 : 0)) / 24.0) * 1.56) * 0.004 * (bl2 ? 0.8 : 1.0);
        }
        if (n == 29) {
            d5 = 0.11 * d3;
            d6 = 0.07 * d3;
        }
        if (n >= 30 && n <= 35) {
            d2 = n % 2 == 0 ? 1.0 : -1.0;
            double d11 = bl ? ZheergenSpellMotion.clamp(d / 10.0) * 0.75 : 1.375 + ZheergenSpellMotion.clamp(d / 24.0) * 0.5832999999999999;
            d9 = d2 * (ZheergenSpellMotion.sample(SWING_TIMES, SWING_VALUES, d11) - (bl ? 0.0 : 134.06)) * 0.004;
            if (n >= 34) {
                d9 *= 0.66;
            }
        }
        if (n >= 36 && n <= 44) {
            d5 = ZheergenSpellMotion.nail(d, bl) * (double)((n - 36) % 3 + 1) / 3.0;
        }
        return new State(d4, d5, d6, d7, d8, d9);
    }

    public static double[] point(double[] dArray, int n, double[] dArray2, double d, boolean bl) {
        State state = ZheergenSpellMotion.state(n, d, bl);
        double[] dArray3 = new double[3];
        ZheergenSpellMotion.apply(dArray, dArray2, state, dArray3);
        return dArray3;
    }

    public static void apply(double[] dArray, double[] dArray2, State state, double[] dArray3) {
        double d = (dArray[0] - dArray2[0]) * state.scaleX();
        double d2 = (dArray[1] - dArray2[1]) * state.scaleY();
        double d3 = Math.cos(state.roll());
        double d4 = Math.sin(state.roll());
        dArray3[0] = dArray2[0] + d * d3 - d2 * d4 + state.horizontal();
        dArray3[1] = dArray2[1] + d * d4 + d2 * d3 + state.vertical();
        dArray3[2] = dArray[2] + state.depth();
    }

    public record State(double horizontal, double vertical, double depth, double scaleX, double scaleY, double roll) {
    }
}

