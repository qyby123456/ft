/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.maque;

import net.farmingtales.ftboss.client.maque.MaqueArcaneAssets;

public final class MaqueArcanePose {
    public static final double LIFT_TICKS = 160.0;
    public static final double FLIGHT_TICKS = 12.0;

    private MaqueArcanePose() {
    }

    public static double hinge(int n, double d) {
        return Math.toRadians((n < 0 ? 0.9 : -1.05) * Math.cos(d * Math.PI * 2.0 / 160.0));
    }

    public static double liftHeight(double d) {
        return 0.065 * Math.sin(d * Math.PI * 2.0 / 160.0);
    }

    public static int visualLifetime(String string, String string2, int n) {
        if (string.equals("wingstep") && !string2.equals("CHARGE")) {
            return Math.max(n, 180);
        }
        if (string.equals("feather_fan") && !string2.equals("CHARGE")) {
            return Math.max(n, 18);
        }
        return n;
    }

    public static Flight flight(MaqueArcaneAssets.Bone bone, double d, double[] dArray, boolean bl) {
        double[] dArray2;
        double[] dArray3;
        double d2 = Math.max(0.0, Math.min(1.0, d / 12.0));
        double d3 = 0.82 * d2 + 0.18 * d2 * d2;
        int n = bone.role().lane();
        double[] dArray4 = new double[]{bone.pivot()[0], bone.pivot()[1] + 0.09, 1.08};
        if (bl) {
            dArray3 = dArray;
        } else {
            double[] dArray5 = new double[3];
            dArray5[0] = dArray4[0] + (double)n * 0.65;
            dArray5[1] = dArray4[1] + (double)n * 0.075;
            dArray3 = dArray5;
            dArray5[2] = dArray[2];
        }
        double[] dArray6 = dArray3;
        double d4 = Math.max(1.0, dArray6[2] - dArray4[2]);
        double[] dArray7 = new double[]{dArray4[0] + (double)n * 0.42, dArray4[1] + 0.08, dArray4[2] + d4 * 0.34};
        double[] dArray8 = new double[]{dArray6[0] + (double)n * 0.15, dArray6[1] + 0.06, dArray6[2] - d4 * 0.28};
        double d5 = 1.0 - d3;
        double[] dArray9 = new double[3];
        double[] dArray10 = new double[3];
        for (int i = 0; i < 3; ++i) {
            dArray9[i] = d5 * d5 * d5 * dArray4[i] + 3.0 * d5 * d5 * d3 * dArray7[i] + 3.0 * d5 * d3 * d3 * dArray8[i] + d3 * d3 * d3 * dArray6[i];
            dArray10[i] = 3.0 * d5 * d5 * (dArray7[i] - dArray4[i]) + 6.0 * d5 * d3 * (dArray8[i] - dArray7[i]) + 3.0 * d3 * d3 * (dArray6[i] - dArray8[i]);
        }
        double d6 = Math.sqrt(MaqueArcanePose.dot(dArray10, dArray10));
        int n2 = 0;
        while (n2 < 3) {
            int n3 = n2++;
            dArray10[n3] = dArray10[n3] / Math.max(d6, 1.0E-9);
        }
        double d7 = Math.hypot(dArray10[0], dArray10[2]);
        if (d7 > 1.0E-8) {
            double[] dArray11 = new double[3];
            dArray11[0] = dArray10[2] / d7;
            dArray11[1] = 0.0;
            dArray2 = dArray11;
            dArray11[2] = -dArray10[0] / d7;
        } else {
            double[] dArray12 = new double[3];
            dArray12[0] = 1.0;
            dArray12[1] = 0.0;
            dArray2 = dArray12;
            dArray12[2] = 0.0;
        }
        double[] dArray13 = dArray2;
        return new Flight(dArray9, dArray10, dArray13, MaqueArcanePose.cross(dArray10, dArray13), dArray4);
    }

    public static void transform(MaqueArcaneAssets.Bone bone, double d, double[] dArray, double[] dArray2) {
        Flight flight = bone.role().kind().equals("scatter") ? MaqueArcanePose.flight(bone, d, new double[]{1.15, 1.22, 6.8}, true) : null;
        MaqueArcanePose.transform(bone, d, dArray, flight, dArray2);
    }

    public static void transform(MaqueArcaneAssets.Bone bone, double d, double[] dArray, Flight flight, double[] dArray2) {
        System.arraycopy(dArray, 0, dArray2, 0, 3);
        MaqueArcaneAssets.Role role = bone.role();
        if (role.kind().equals("scatter")) {
            double d2 = dArray[0] - flight.origin[0];
            double d3 = dArray[1] - flight.origin[1];
            double d4 = dArray[2] - flight.origin[2];
            for (int i = 0; i < 3; ++i) {
                dArray2[i] = flight.center[i] + d2 * flight.right[i] + d3 * flight.up[i] + d4 * flight.forward[i];
            }
        } else if (role.kind().equals("lift")) {
            double d5 = MaqueArcanePose.hinge(role.side(), d);
            double d6 = dArray[0] - bone.pivot()[0];
            double d7 = dArray[2] - bone.pivot()[2];
            dArray2[0] = bone.pivot()[0] + d6 * Math.cos(d5) + d7 * Math.sin(d5);
            dArray2[2] = bone.pivot()[2] - d6 * Math.sin(d5) + d7 * Math.cos(d5);
            dArray2[1] = dArray2[1] + MaqueArcanePose.liftHeight(d);
        } else if (role.kind().equals("shelter")) {
            double d8 = 1.0 + 0.018 * Math.sin(d * Math.PI / 15.0);
            dArray2[0] = bone.pivot()[0] + (dArray[0] - bone.pivot()[0]) * d8;
            dArray2[1] = bone.pivot()[1] + (dArray[1] - bone.pivot()[1]) * d8;
        } else if (role.kind().equals("haven")) {
            dArray2[1] = dArray2[1] + 0.023 * Math.sin(d * Math.PI / 15.0);
        }
    }

    public static double smooth(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double envelope(String string, double d, double d2) {
        double d3 = MaqueArcanePose.smooth(d / (double)(string.equals("scatter") ? 2 : 5));
        double d4 = MaqueArcanePose.smooth((d2 - d) / (double)(string.equals("scatter") ? 5 : 9));
        double d5 = string.equals("haven") ? 0.88 + 0.12 * Math.sin(d * Math.PI / 15.0) : 1.0;
        return d3 * d4 * d5;
    }

    public static double bodyGap(double[][] dArray, double[] dArray2) {
        double d = 0.0;
        for (int i = 0; i < 3; ++i) {
            double d2 = Double.POSITIVE_INFINITY;
            double d3 = Double.NEGATIVE_INFINITY;
            for (double[] dArray3 : dArray) {
                d2 = Math.min(d2, dArray3[i]);
                d3 = Math.max(d3, dArray3[i]);
            }
            d = Math.max(d, Math.max(dArray2[i] - d3, d2 - dArray2[i + 3]));
        }
        return MaqueArcanePose.smooth(d / 0.14);
    }

    public static double sightFade(double[] dArray, double[] dArray2, double[] dArray3, boolean bl) {
        double d = dArray[0] - dArray2[0];
        double d2 = dArray[1] - dArray2[1];
        double d3 = dArray[2] - dArray2[2];
        double d4 = Math.sqrt(d * d + d2 * d2 + d3 * d3);
        double d5 = MaqueArcanePose.smooth((d4 - 0.38) / 0.55);
        if (!bl || d4 < 1.0E-8) {
            return d5;
        }
        double d6 = (d * dArray3[0] + d2 * dArray3[1] + d3 * dArray3[2]) / d4;
        double d7 = Math.acos(Math.max(-1.0, Math.min(1.0, d6)));
        return d5 * MaqueArcanePose.smooth((d7 - 0.18) / 0.2);
    }

    public static boolean rayTouches(double[][] dArray, double[] dArray2, double[] dArray3) {
        return MaqueArcanePose.triangle(dArray[0], dArray[1], dArray[2], dArray2, dArray3) || MaqueArcanePose.triangle(dArray[0], dArray[2], dArray[3], dArray2, dArray3);
    }

    private static boolean triangle(double[] dArray, double[] dArray2, double[] dArray3, double[] dArray4, double[] dArray5) {
        double[] dArray6;
        double[] dArray7;
        double[] dArray8 = MaqueArcanePose.subtract(dArray2, dArray);
        double d = MaqueArcanePose.dot(dArray8, dArray7 = MaqueArcanePose.cross(dArray5, dArray6 = MaqueArcanePose.subtract(dArray3, dArray)));
        if (Math.abs(d) < 1.0E-10) {
            return false;
        }
        double d2 = 1.0 / d;
        double[] dArray9 = MaqueArcanePose.subtract(dArray4, dArray);
        double d3 = MaqueArcanePose.dot(dArray9, dArray7) * d2;
        if (d3 < 0.0 || d3 > 1.0) {
            return false;
        }
        double[] dArray10 = MaqueArcanePose.cross(dArray9, dArray8);
        double d4 = MaqueArcanePose.dot(dArray5, dArray10) * d2;
        return d4 >= 0.0 && d4 + d3 <= 1.0 && MaqueArcanePose.dot(dArray6, dArray10) * d2 > 0.0;
    }

    private static double[] subtract(double[] dArray, double[] dArray2) {
        return new double[]{dArray[0] - dArray2[0], dArray[1] - dArray2[1], dArray[2] - dArray2[2]};
    }

    private static double dot(double[] dArray, double[] dArray2) {
        return dArray[0] * dArray2[0] + dArray[1] * dArray2[1] + dArray[2] * dArray2[2];
    }

    private static double[] cross(double[] dArray, double[] dArray2) {
        return new double[]{dArray[1] * dArray2[2] - dArray[2] * dArray2[1], dArray[2] * dArray2[0] - dArray[0] * dArray2[2], dArray[0] * dArray2[1] - dArray[1] * dArray2[0]};
    }

    public record Flight(double[] center, double[] forward, double[] right, double[] up, double[] origin) {
    }
}

