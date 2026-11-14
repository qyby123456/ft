/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.qizairun;

public final class QizaiRunPose {
    private QizaiRunPose() {
    }

    public static double clamp(double d, double d2, double d3) {
        return Math.max(d2, Math.min(d3, d));
    }

    public static double smooth(double d) {
        double d2 = QizaiRunPose.clamp(d, 0.0, 1.0);
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double[] neutral() {
        return new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -5.0, 2.5, 0.0, -0.18, 0.0, 0.08, 5.0, 2.5, 0.0, 0.0, 0.0, -0.08, -1.9, 12.0, 0.0, 0.0, 0.0, 0.0, 1.9, 12.0, 0.0, 0.0, 0.0, 0.0};
    }

    private static double[] rotate(double d, double d2, double d3, double d4, double d5, double d6) {
        double d7 = d2 * Math.cos(d4) - d3 * Math.sin(d4);
        double d8 = d2 * Math.sin(d4) + d3 * Math.cos(d4);
        double d9 = d * Math.cos(d5) + d8 * Math.sin(d5);
        double d10 = -d * Math.sin(d5) + d8 * Math.cos(d5);
        return new double[]{d9 * Math.cos(d6) - d7 * Math.sin(d6), d9 * Math.sin(d6) + d7 * Math.cos(d6), d10};
    }

    public static double footBottom(double[] dArray, int n) {
        int n2 = n * 6;
        double d = Double.NEGATIVE_INFINITY;
        for (double d2 : new double[]{-2.0, 2.0}) {
            for (double d3 : new double[]{-2.0, 2.0}) {
                d = Math.max(d, dArray[n2 + 1] + QizaiRunPose.rotate(d2, 12.0, d3, dArray[n2 + 3], dArray[n2 + 4], dArray[n2 + 5])[1]);
            }
        }
        return d;
    }

    public static void apply(double[] dArray, double d, double d2, double d3, boolean bl, boolean bl2, boolean bl3, double d4, double d5, double d6) {
        int n;
        double d7;
        int n2;
        if (dArray.length != 36) {
            throw new IllegalArgumentException("Expected six model parts");
        }
        if (!(Double.isFinite(d) && Double.isFinite(d2) && Double.isFinite(d3) && Double.isFinite(d4) && Double.isFinite(d5) && Double.isFinite(d6) && !(d3 <= 0.001))) {
            return;
        }
        for (double d8 : dArray) {
            if (Double.isFinite(d8)) continue;
            return;
        }
        d3 = QizaiRunPose.clamp(d3, 0.0, 1.0);
        boolean bl4 = (d4 = QizaiRunPose.clamp(d4, 0.0, 1.0)) <= 0.001;
        double[] dArray2 = (double[])dArray.clone();
        double d9 = QizaiRunPose.smooth((d2 - 0.24) / 0.4);
        double d10 = d + (bl ? Math.PI : 0.0);
        double d11 = bl ? -1.0 : 1.0;
        double d12 = 1.0 - 2.0 * QizaiRunPose.smooth(-d5);
        double d13 = QizaiRunPose.clamp(Math.atan2(d6, Math.abs(d5)) * 0.42, -0.52, 0.52);
        double d14 = (0.11 + 0.07 * d9) * d12;
        double d15 = 0.035 * Math.sin(d10) * d11;
        double d16 = 0.018 * Math.sin(d10) * d11 - 0.045 * d6;
        if (!bl4) {
            dArray2[9] = d14;
            dArray2[10] = d15;
            dArray2[11] = d16;
            dArray2[3] = dArray[3] - 0.32 * d14;
            dArray2[4] = dArray[4];
            dArray2[5] = -0.35 * d16;
            for (n2 = 2; n2 < 4; ++n2) {
                int n3 = n2 * 6;
                boolean bl5 = n2 == (bl ? 3 : 2);
                boolean bl6 = bl5 ? bl2 : bl3;
                d7 = n2 == 2 ? -1.0 : 1.0;
                double d17 = d10 + (n2 == 2 ? Math.PI : 0.0);
                dArray2[n3 + 3] = d14 + (bl6 ? -0.78 : -0.18) + (bl6 ? 0.055 : 0.42) * Math.sin(d17) * d12;
                dArray2[n3 + 4] = d15 + d7 * (bl6 ? 0.1 : 0.05);
                dArray2[n3 + 5] = d16 - d7 * (bl6 ? 0.16 : 0.12);
            }
        }
        for (n2 = 0; n2 < 24; ++n2) {
            dArray2[n2] = dArray[n2] + (dArray2[n2] - dArray[n2]) * d4;
        }
        double d18 = dArray2[9];
        double d19 = dArray2[10];
        d7 = dArray2[11];
        for (int i = 4; i < 6; ++i) {
            double d20;
            n = i * 6;
            double d21 = (d10 + (i == 4 ? 0.0 : Math.PI)) / (Math.PI * 2) % 1.0;
            if (d21 < 0.0) {
                d21 += 1.0;
            }
            double d22 = d21 < (d20 = 0.54) ? d21 / d20 : (d21 - d20) / (1.0 - d20);
            double d23 = (d21 < d20 ? 0.68 - 1.44 * QizaiRunPose.smooth(d22) : -0.76 + 1.44 * QizaiRunPose.smooth(d22)) * (0.93 + 0.07 * d9) * d12;
            double d24 = d21 < d20 ? 0.0 : (1.15 + 0.4 * d9) * Math.pow(Math.sin(d22 * Math.PI), 2.0);
            double[] dArray3 = QizaiRunPose.rotate(i == 4 ? -1.9 : 1.9, 12.0, 0.0, d18, d19, d7);
            dArray2[n] = dArray2[6] + dArray3[0];
            dArray2[n + 1] = dArray2[7] + dArray3[1] - d24;
            dArray2[n + 2] = dArray2[8] + dArray3[2];
            dArray2[n + 3] = d23;
            dArray2[n + 4] = d13;
            dArray2[n + 5] = (double)(i == 4 ? 1 : -1) * 0.025;
        }
        if (!bl4) {
            double[] dArray4 = QizaiRunPose.rotate(0.0, 12.0, 0.0, d18, d19, d7);
            dArray2[6] = -dArray4[0];
            dArray2[7] = 12.0 - dArray4[1];
            dArray2[8] = -dArray4[2];
            for (n = 4; n < 6; ++n) {
                int n4 = n * 6;
                dArray2[n4] = dArray2[n4] - dArray4[0];
                int n5 = n * 6 + 1;
                dArray2[n5] = dArray2[n5] + (12.0 - dArray4[1]);
                int n6 = n * 6 + 2;
                dArray2[n6] = dArray2[n6] - dArray4[2];
            }
            dArray2[0] = dArray2[6];
            dArray2[1] = dArray2[7];
            dArray2[2] = dArray2[8];
            for (n = 2; n < 4; ++n) {
                double[] dArray5 = QizaiRunPose.rotate(n == 2 ? -5.0 : 5.0, 2.5, 0.0, d18, d19, d7);
                for (int i = 0; i < 3; ++i) {
                    dArray2[n * 6 + i] = dArray2[6 + i] + dArray5[i];
                }
            }
        }
        for (int i = 0; i < 36; ++i) {
            if (bl4 && i < 24) continue;
            int n7 = i;
            dArray[n7] = dArray[n7] + (dArray2[i] - dArray[i]) * d3;
        }
        double d25 = Math.max(QizaiRunPose.footBottom(dArray, 4), QizaiRunPose.footBottom(dArray, 5));
        double d26 = (24.0 - d25) * QizaiRunPose.smooth(d3);
        for (int i = 0; i < 6; ++i) {
            int n8 = i * 6 + 1;
            dArray[n8] = dArray[n8] + d26;
        }
    }
}

