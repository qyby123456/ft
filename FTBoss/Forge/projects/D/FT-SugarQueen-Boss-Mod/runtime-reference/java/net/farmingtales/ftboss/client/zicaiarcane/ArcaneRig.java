/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.zicaiarcane;

import java.util.List;
import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;
import net.farmingtales.ftboss.client.zicaiarcane.ArcaneField;

public final class ArcaneRig {
    public static double ease(double d) {
        return 0.5 - 0.5 * Math.cos(Math.PI * Math.max(0.0, Math.min(1.0, d)));
    }

    public static double spatialScale(double d) {
        return Double.isFinite(d) && d > 0.0 ? Math.max(0.5, Math.min(4.0, d)) : 1.0;
    }

    public static double animationAge(String string, boolean bl, double d, int n) {
        if (bl || string.equals("halo_sanctuary")) {
            return Math.max(0.0, d);
        }
        return Math.max(0.0, d) * Math.max(1.0, (string.equals("seraph_descent") ? 44.0 : 36.0) / (double)Math.max(1, n));
    }

    public static double[] floorAnchor(String string, String string2, double[] dArray, double[] dArray2, double[] dArray3) {
        if (string.equals("CASTER") && dArray3 != null) {
            return (double[])dArray3.clone();
        }
        if (string.equals("DESCENT") && string2.equals("BEAM")) {
            return new double[]{dArray2[0], dArray[1] - 3.0, dArray2[2]};
        }
        return (double[])dArray.clone();
    }

    public static double persistence(double d, int n) {
        double d2 = Math.min(6.0, Math.max(1.0, (double)n / 3.0));
        return 1.0 - ArcaneRig.ease((d - (double)n + d2) / d2);
    }

    public static double sample(double[][] dArray, double d) {
        if (d <= dArray[0][0]) {
            return dArray[0][1];
        }
        for (int i = 1; i < dArray.length; ++i) {
            if (!(d <= dArray[i][0])) continue;
            double[] dArray2 = dArray[i - 1];
            double[] dArray3 = dArray[i];
            return dArray2[1] + (dArray3[1] - dArray2[1]) * ArcaneRig.ease((d - dArray2[0]) / (dArray3[0] - dArray2[0]));
        }
        return dArray[dArray.length - 1][1];
    }

    public static Frame frame(String string, double d, boolean bl) {
        double d2 = d / 20.0;
        double d3 = ArcaneRig.ease((d - 2.0) / 19.0);
        double d4 = 1.0;
        double d5 = 1.0;
        double d6 = 0.0;
        double d7 = 0.0;
        double d8 = 1.0;
        if (string.equals("compression")) {
            d4 = bl ? 0.88 + 0.08 * Math.sin(d2 * 5.0) : 1.0 - 0.25 * ArcaneRig.ease(d / 8.0);
            d8 = bl ? 0.85 : 1.0 - 0.98 * ArcaneRig.ease((d - 4.0) / 10.0);
        } else if (string.equals("blast")) {
            d4 = bl ? 0.23 + 0.018 * Math.sin(d2 * 6.0) : 0.2 + 0.8 * (1.0 - Math.pow(1.0 - Math.max(0.0, Math.min(1.0, d / 12.0)), 3.0));
            d5 = bl ? 0.74 : 1.0 - 0.22 * ArcaneRig.ease(d / 18.0);
            d8 = bl ? 0.25 : (0.28 + 0.72 * ArcaneRig.ease(d / 3.0)) * (1.0 - 0.96 * ArcaneRig.ease((d - 12.0) / 22.0));
        } else if (string.equals("wake")) {
            d4 = bl ? 0.25 : 0.18 + 0.82 * (1.0 - Math.pow(1.0 - Math.max(0.0, Math.min(1.0, (d - 4.0) / 18.0)), 3.0));
            d5 = 0.88;
            d8 = bl ? 0.06 : ArcaneRig.ease((d - 3.0) / 5.0) * (1.0 - 0.98 * ArcaneRig.ease((d - 19.0) / 23.0));
        } else if (string.equals("boundary")) {
            d8 = bl ? 0.16 : (0.3 + 0.7 * ArcaneRig.ease((d - 9.0) / 7.0)) * (1.0 - 0.97 * ArcaneRig.ease((d - 25.0) / 22.0));
        } else if (string.equals("cloud")) {
            d4 = bl ? 0.88 + 0.08 * ArcaneRig.ease(d / 24.0) : 1.0 + 0.025 * Math.sin(d2 * 1.9);
            d5 = 1.0 + 0.035 * Math.sin(d2 * 2.1);
            d8 = bl ? 0.5 + 0.35 * ArcaneRig.ease(d / 8.0) : 1.0 - 0.94 * ArcaneRig.ease((d - 33.0) / 25.0);
        } else if (string.equals("judgment")) {
            d5 = bl ? 0.68 : 0.88 + 0.12 * ArcaneRig.ease(d / 10.0);
            d7 = bl ? 0.36 : 0.15 * (1.0 - ArcaneRig.ease(d / 12.0));
            d8 = bl ? 0.06 : (0.14 + 0.86 * ArcaneRig.ease((d - 5.0) / 5.0)) * (1.0 - 0.96 * ArcaneRig.ease((d - 16.0) / 15.0));
        } else if (string.equals("pressure")) {
            d4 = bl ? 0.96 + 0.035 * Math.sin(d2 * 4.0) : 1.0 + 0.33 * d3;
            d5 = bl ? 1.0 : 1.0 - 0.18 * d3;
            d8 = bl ? 0.74 : 1.0 - 0.78 * ArcaneRig.ease((d - 9.0) / 19.0);
        } else if (string.equals("recoil") || string.equals("aftershock")) {
            boolean bl2 = string.equals("aftershock");
            double d9 = bl ? 0.74 : (d4 = 0.82 + (bl2 ? 0.54 : 0.7) * ArcaneRig.ease((d - (double)(bl2 ? 6 : 1)) / 23.0));
            d8 = bl ? 0.2 : (0.24 + 0.76 * ArcaneRig.ease(d / 6.0)) * (1.0 - 0.95 * ArcaneRig.ease((d - (double)(bl2 ? 16 : 13)) / 30.0));
        } else if (string.startsWith("concord")) {
            int n = Integer.parseInt(string.substring(string.length() - 1));
            d4 = 0.97 + 0.17 * (ArcaneRig.sample(ArcaneField.REFERENCE.breath(), (d2 + (double)n * 0.23) % ArcaneField.REFERENCE.breathLength()) - 1.0);
            d6 = Math.sin(d2 * 0.56 + (double)n * 0.8) * 3.5;
            d8 = 0.72 + 0.24 * ArcaneRig.ease((Math.sin(d2 * 2.1 - (double)n) + 1.0) / 2.0);
        } else if (string.equals("weave")) {
            d6 = d2 * 4.6;
            d7 = 0.035 * Math.sin(d2 * 1.9);
            d8 = 0.78 + 0.2 * Math.pow(Math.sin(d2 * 1.6), 2.0);
        } else if (string.equals("motes")) {
            d7 = 0.24 * Math.sin(d2 * 1.6);
            d8 = 0.4 + 0.5 * Math.pow(Math.sin(d2 * 1.9), 2.0);
        } else if (string.startsWith("aperture")) {
            int n = Integer.parseInt(string.substring(string.length() - 1));
            d6 = (double)(n % 2 == 1 ? -1 : 1) * d2 * (double)(9 + n * 3);
            d4 = bl ? 0.87 + 0.07 * Math.sin(d2 * 2.0) : 0.89 + 0.19 * d3;
            d8 = bl ? 0.83 : 1.0 - 0.7 * ArcaneRig.ease((d - 17.0) / 31.0);
        } else if (string.equals("fall")) {
            double d10 = ArcaneRig.sample(ArcaneField.REFERENCE.descent(), Math.min(0.5833, d2 * 0.58));
            d7 = bl ? 0.58 : 0.74 * (1.0 - d10 / 5.0);
            d5 = bl ? 0.63 : 0.65 + 0.35 * ArcaneRig.ease(d / 17.0);
            d8 = bl ? 0.28 : (0.3 + 0.7 * ArcaneRig.ease(d / 7.0)) * (1.0 - 0.88 * ArcaneRig.ease((d - 22.0) / 24.0));
        } else if (string.equals("impact")) {
            d4 = bl ? 0.71 : 0.75 + 0.65 * ArcaneRig.ease((d - 10.0) / 21.0);
            d8 = bl ? 0.1 : ArcaneRig.ease((d - 8.0) / 9.0) * (1.0 - 0.93 * ArcaneRig.ease((d - 24.0) / 23.0));
        } else {
            throw new IllegalStateException("Unknown new bone behavior " + string);
        }
        double d11 = Math.toRadians(d6);
        return new Frame(d4, d5, d6, d7, d8, Math.cos(d11), Math.sin(d11));
    }

    public static void position(double[] dArray, ArcaneField.Bone bone, Frame frame, double[] dArray2) {
        double d = (dArray[0] - bone.pivot()[0]) * frame.radius();
        double d2 = (dArray[2] - bone.pivot()[2]) * frame.radius();
        dArray2[0] = bone.pivot()[0] + d * frame.cosine() + d2 * frame.sine();
        dArray2[1] = bone.pivot()[1] + (dArray[1] - bone.pivot()[1]) * frame.height() + frame.altitude();
        dArray2[2] = bone.pivot()[2] - d * frame.sine() + d2 * frame.cosine();
    }

    public static double visibility(double[][] dArray, int[] nArray, double[] dArray2, double[] dArray3, List<Box> list, boolean bl) {
        double d;
        double d2;
        double d3;
        double d4;
        double d5;
        double d6 = 0.0;
        double d7 = 0.0;
        double d8 = 0.0;
        for (int n : nArray) {
            d6 += dArray[n][0] / 3.0;
            d7 += dArray[n][1] / 3.0;
            d8 += dArray[n][2] / 3.0;
        }
        double d9 = 0.0;
        for (int n : nArray) {
            d5 = dArray[n][0] - d6;
            d4 = dArray[n][1] - d7;
            d3 = dArray[n][2] - d8;
            d9 = Math.max(d9, Math.sqrt(d5 * d5 + d4 * d4 + d3 * d3));
        }
        double d10 = 1.0;
        for (Box box : list) {
            d5 = Math.max(Math.max(box.minX() - d6, 0.0), d6 - box.maxX());
            d2 = d5 * d5 + (d4 = Math.max(Math.max(box.minY() - d7, 0.0), d7 - box.maxY())) * d4 + (d3 = Math.max(Math.max(box.minZ() - d8, 0.0), d8 - box.maxZ())) * d3;
            if (d2 >= (d = d9 + 0.22) * d || (d10 = Math.min(d10, ArcaneRig.ease((Math.sqrt(d2) - d9) / 0.22))) != 0.0) continue;
            return 0.0;
        }
        double d11 = d6 - dArray2[0];
        d5 = d7 - dArray2[1];
        d4 = d8 - dArray2[2];
        d3 = Math.sqrt(d11 * d11 + d5 * d5 + d4 * d4);
        d10 *= FirstPersonSpellVisibility.cameraFactor(ArcaneRig.ease((d3 - d9 - 0.22) / 0.7), bl);
        if (false && (d2 = d11 * dArray3[0] + d5 * dArray3[1] + d4 * dArray3[2]) + d9 > 0.0) {
            d = Math.sqrt(Math.max(0.0, d3 * d3 - d2 * d2)) - d9;
            d10 *= ArcaneRig.ease((d - Math.max(0.0, d2) * 0.13) / Math.max(0.12, Math.max(0.0, d2) * 0.12));
        }
        return d10;
    }

    private ArcaneRig() {
    }

    public static double vertexVisibility(double[] dArray, double[] dArray2, double[] dArray3, List<Box> list, boolean bl) {
        return ArcaneRig.visibilityAt(dArray[0], dArray[1], dArray[2], 0.0, dArray2, dArray3, list, bl);
    }

    private static double visibilityAt(double d, double d2, double d3, double d4, double[] dArray, double[] dArray2, List<Box> list, boolean bl) {
        double d5;
        double d6;
        double d7;
        double d8 = 1.0;
        for (Box box : list) {
            double d9;
            d7 = Math.max(Math.max(box.minX() - d, 0.0), d - box.maxX());
            double d10 = d7 * d7 + (d6 = Math.max(Math.max(box.minY() - d2, 0.0), d2 - box.maxY())) * d6 + (d5 = Math.max(Math.max(box.minZ() - d3, 0.0), d3 - box.maxZ())) * d5;
            if (d10 >= (d9 = d4 + 0.22) * d9 || (d8 = Math.min(d8, ArcaneRig.ease((Math.sqrt(d10) - d4) / 0.22))) != 0.0) continue;
            return 0.0;
        }
        double d11 = d - dArray[0];
        d7 = d2 - dArray[1];
        d6 = d3 - dArray[2];
        d5 = Math.sqrt(d11 * d11 + d7 * d7 + d6 * d6);
        return d8 *= FirstPersonSpellVisibility.cameraFactor(ArcaneRig.ease((d5 - d4 - 0.22) / 0.7), bl);
    }

    public record Frame(double radius, double height, double yaw, double altitude, double opacity, double cosine, double sine) {
    }

    public record Box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    }
}

