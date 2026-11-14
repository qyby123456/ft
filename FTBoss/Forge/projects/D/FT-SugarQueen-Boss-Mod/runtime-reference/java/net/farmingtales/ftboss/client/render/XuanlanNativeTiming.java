/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

public final class XuanlanNativeTiming {
    private static final double[] SWING_TIMES = new double[]{0.0, 0.75, 1.375, 1.9583};
    private static final double[] SWING_PITCH = new double[]{0.0, -115.9351, 134.06, 0.0};
    private static final double[] SHARD_TICKS = new double[]{28.0, 14.4, 22.4, 19.2};

    private XuanlanNativeTiming() {
    }

    public static double secondarySwing(double d) {
        double d2 = Math.max(0.0, d % 39.166) / 20.0;
        for (int i = 1; i < SWING_TIMES.length; ++i) {
            if (!(d2 <= SWING_TIMES[i])) continue;
            double d3 = (d2 - SWING_TIMES[i - 1]) / (SWING_TIMES[i] - SWING_TIMES[i - 1]);
            d3 = d3 * d3 * (3.0 - 2.0 * d3);
            return Math.toRadians(SWING_PITCH[i - 1] + (SWING_PITCH[i] - SWING_PITCH[i - 1]) * d3) * 0.025;
        }
        return 0.0;
    }

    public static double shardScale(double[] dArray, double d) {
        int n = (dArray[1] > 1.2 ? 0 : 2) + (dArray[0] > 0.0 ? 1 : 0);
        double d2 = Math.max(0.0, Math.min(1.0, d / SHARD_TICKS[n]));
        return 0.86 + 0.14 * d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double rayRecoil(double d) {
        double d2 = Math.max(0.0, d) / 20.0;
        if (d2 >= 0.4) {
            return 0.0;
        }
        double d3 = d2 <= 0.125 ? d2 / 0.125 : (0.4 - d2) / 0.275;
        return -0.13 * Math.max(0.0, d3);
    }
}

