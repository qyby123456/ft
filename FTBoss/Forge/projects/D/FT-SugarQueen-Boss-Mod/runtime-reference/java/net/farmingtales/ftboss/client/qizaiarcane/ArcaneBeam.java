/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneRig;

public final class ArcaneBeam {
    private static final Map<Key, ArcaneRig.Effect> cache = new LinkedHashMap<Key, ArcaneRig.Effect>();

    private ArcaneBeam() {
    }

    public static ArcaneRig.Effect mesh(ArcaneRig.Effect effect, double d) {
        if (!Double.isFinite(d) || d < 0.04) {
            throw new IllegalArgumentException("Invalid ray length");
        }
        int n = (int)Math.floor(Math.min(64.0, d) * 512.0);
        Key key = new Key(effect.id(), effect.texture(), n);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        double d2 = (double)n / 512.0;
        double d3 = effect.id().equals("star_cross") ? 0.014 : 0.018;
        int n2 = Math.max(1, (int)Math.ceil(d2 / 1.5));
        ArrayList<ArcaneRig.Bone> arrayList = new ArrayList<ArcaneRig.Bone>();
        ArcaneRig.Motion motion = new ArcaneRig.Motion(List.of(), List.of(), List.of(), List.of(new ArcaneRig.Moment(0.0, new double[]{1.0})));
        int[][] nArrayArray = new int[][]{{0, 3, 2, 1}, {4, 5, 6, 7}, {0, 1, 5, 4}, {1, 2, 6, 5}, {2, 3, 7, 6}, {3, 0, 4, 7}};
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < n2; ++j) {
                double d4 = d2 * (double)j / (double)n2 + (j == 0 ? 0.0 : 0.006);
                double d5 = d2 * (double)(j + 1) / (double)n2 - (j == n2 - 1 ? 0.0 : 0.006);
                if (i == 1) {
                    d4 += 0.004;
                    d5 -= 0.004;
                }
                double d6 = d3 * (i == 0 ? 1.0 : 2.5);
                double[][] dArrayArray = new double[][]{{-d6, -d6, d4}, {d6, -d6, d4}, {d6, d6, d4}, {-d6, d6, d4}, {-d6, -d6, d5}, {d6, -d6, d5}, {d6, d6, d5}, {-d6, d6, d5}};
                ArrayList<ArcaneRig.Quad> arrayList2 = new ArrayList<ArcaneRig.Quad>();
                for (int[] nArray : nArrayArray) {
                    double[][] dArray = new double[4][3];
                    for (int k = 0; k < 4; ++k) {
                        dArray[k] = (double[])dArrayArray[nArray[k]].clone();
                    }
                    double[] dArray2 = ArcaneRig.cross(ArcaneRig.difference(dArray[1], dArray[0]), ArcaneRig.difference(dArray[2], dArray[0]));
                    double d7 = Math.sqrt(ArcaneRig.dot(dArray2, dArray2));
                    int n3 = 0;
                    while (n3 < 3) {
                        int n4 = n3++;
                        dArray2[n4] = dArray2[n4] / d7;
                    }
                    double d8 = i == 0 ? 0.0 : 0.25;
                    arrayList2.add(new ArcaneRig.Quad(dArray, new double[][]{{0.04 + d8, 0.2}, {0.2 + d8, 0.2}, {0.2 + d8, 0.04}, {0.04 + d8, 0.04}}, dArray2));
                }
                arrayList.add(new ArcaneRig.Bone("collision_bounded_ray_" + i + "_" + j, new double[3], false, false, true, i == 0 ? 0.9 : 0.24, Map.of("idle", motion), List.copyOf(arrayList2)));
            }
        }
        ArcaneRig.Effect effect2 = new ArcaneRig.Effect(effect.id(), effect.texture(), 0, 0.0, List.copyOf(arrayList));
        if (cache.size() == 48) {
            cache.remove(cache.keySet().iterator().next());
        }
        cache.put(key, effect2);
        return effect2;
    }

    private record Key(String id, String texture, int span) {
    }
}

