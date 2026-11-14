/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneRig;

public final class ArcaneTrail {
    public static final double LIFETIME = 8.0;
    public static final double MAX_LENGTH = 3.4;
    public static final double MIN_SPACING = 0.1;
    private final ArrayList<Mark> marks = new ArrayList();
    private Mark head;

    public void offer(double d, double[] dArray) {
        if (!Double.isFinite(d) || dArray.length != 3) {
            throw new IllegalArgumentException("Invalid trail time/point");
        }
        for (double d2 : dArray) {
            if (Double.isFinite(d2)) continue;
            throw new IllegalArgumentException("Invalid trail position");
        }
        Mark mark = new Mark(d, (double[])dArray.clone());
        if (this.head != null && (d < this.head.time() || ArcaneTrail.distance(this.head.point(), dArray) > 4.0)) {
            this.marks.clear();
        }
        if (this.marks.isEmpty() || ArcaneTrail.distance(this.marks.get(this.marks.size() - 1).point(), dArray) >= 0.1) {
            this.marks.add(mark);
        }
        this.head = mark;
        while (this.marks.size() > 48) {
            this.marks.remove(0);
        }
        this.prune(d);
    }

    public void prune(double d) {
        this.marks.removeIf(mark -> d - mark.time() > 8.0);
    }

    public int size() {
        return this.marks.size();
    }

    public boolean expired(double d) {
        return this.head == null || d - this.head.time() >= 8.0;
    }

    public List<Strip> strips(double d, boolean bl) {
        if (this.expired(d)) {
            return List.of();
        }
        this.prune(d);
        ArrayList<Mark> arrayList = new ArrayList<Mark>(this.marks);
        if (arrayList.isEmpty()) {
            return List.of();
        }
        if (ArcaneTrail.distance(((Mark)arrayList.get(arrayList.size() - 1)).point(), this.head.point()) > 0.002) {
            arrayList.add(this.head);
        }
        ArrayList<Strip> arrayList2 = new ArrayList<Strip>();
        double d2 = 0.0;
        for (int i = arrayList.size() - 1; i > 0; --i) {
            double d3;
            Mark mark = (Mark)arrayList.get(i);
            Mark mark2 = (Mark)arrayList.get(i - 1);
            double d4 = ArcaneTrail.distance(mark.point(), mark2.point());
            if (d4 < 0.008) continue;
            double d5 = Math.min(d4, 3.4 - d2);
            if (d5 < 0.008) break;
            double[] dArray = ArcaneTrail.lerp(mark.point(), mark2.point(), d5 / d4);
            double d6 = Math.max(0.0, Math.min(1.0, 1.0 - (d - mark.time()) / 8.0));
            double d7 = d6 * d6 * (d3 = Math.max(0.0, 1.0 - d2 / 3.4));
            if (d7 > 0.005) {
                ArcaneTrail.segment(arrayList2, mark.point(), dArray, 0.009 * (0.2 + 0.8 * d3), 0.92 * d7, 0, 0.003);
                if (bl) {
                    ArcaneTrail.segment(arrayList2, mark.point(), dArray, 0.03 * (0.2 + 0.8 * d3), 0.28 * d7, 1, 0.001);
                }
            }
            if ((d2 += d5) >= 3.392) break;
        }
        return List.copyOf(arrayList2);
    }

    private static void segment(List<Strip> list, double[] dArray, double[] dArray2, double d, double d2, int n, double d3) {
        double[] dArray3;
        double d4 = ArcaneTrail.distance(dArray, dArray2);
        if (d4 < d3 * 2.0 + 0.001) {
            return;
        }
        double[] dArray4 = ArcaneTrail.lerp(dArray2, dArray, d3 / d4);
        double[] dArray5 = ArcaneTrail.lerp(dArray, dArray2, d3 / d4);
        double[] dArray6 = ArcaneTrail.unit(ArcaneRig.difference(dArray5, dArray4));
        if (Math.abs(dArray6[1]) > 0.85) {
            double[] dArray7 = new double[3];
            dArray7[0] = 0.0;
            dArray7[1] = 0.0;
            dArray3 = dArray7;
            dArray7[2] = 1.0;
        } else {
            double[] dArray8 = new double[3];
            dArray8[0] = 0.0;
            dArray8[1] = 1.0;
            dArray3 = dArray8;
            dArray8[2] = 0.0;
        }
        double[] dArray9 = ArcaneTrail.unit(ArcaneRig.cross(dArray6, dArray3));
        double[] dArray10 = ArcaneRig.cross(dArray6, dArray9);
        double[][] dArray11 = new double[8][3];
        int[][] nArrayArray = new int[][]{{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 4; ++j) {
                for (int k = 0; k < 3; ++k) {
                    dArray11[i * 4 + j][k] = (i == 0 ? dArray4 : dArray5)[k] + d * (dArray9[k] * (double)nArrayArray[j][0] + dArray10[k] * (double)nArrayArray[j][1]);
                }
            }
        }
        for (int[] nArray : new int[][]{{0, 3, 2, 1}, {4, 5, 6, 7}, {0, 1, 5, 4}, {1, 2, 6, 5}, {2, 3, 7, 6}, {3, 0, 4, 7}}) {
            double[][] dArray12 = new double[4][3];
            for (int i = 0; i < 4; ++i) {
                dArray12[i] = (double[])dArray11[nArray[i]].clone();
            }
            double[] dArray13 = ArcaneTrail.unit(ArcaneRig.cross(ArcaneRig.difference(dArray12[1], dArray12[0]), ArcaneRig.difference(dArray12[2], dArray12[0])));
            list.add(new Strip(dArray12, dArray13, d2, n));
        }
    }

    private static double[] unit(double[] dArray) {
        double d = Math.sqrt(ArcaneRig.dot(dArray, dArray));
        if (d < 1.0E-12) {
            throw new IllegalArgumentException("Zero trail segment");
        }
        int n = 0;
        while (n < 3) {
            int n2 = n++;
            dArray[n2] = dArray[n2] / d;
        }
        return dArray;
    }

    private static double distance(double[] dArray, double[] dArray2) {
        double[] dArray3 = ArcaneRig.difference(dArray, dArray2);
        return Math.sqrt(ArcaneRig.dot(dArray3, dArray3));
    }

    private static double[] lerp(double[] dArray, double[] dArray2, double d) {
        return new double[]{dArray[0] + (dArray2[0] - dArray[0]) * d, dArray[1] + (dArray2[1] - dArray[1]) * d, dArray[2] + (dArray2[2] - dArray[2]) * d};
    }

    public record Mark(double time, double[] point) {
    }

    public record Strip(double[][] points, double[] normal, double opacity, int tile) {
    }
}

