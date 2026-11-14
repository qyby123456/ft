package net.farmingtales.ftboss.client.qizaiaction;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class QizaiActionRig {
    private final Map<String, QizaiActionRig.Clip> clips;

    private QizaiActionRig(Map<String, QizaiActionRig.Clip> var1) {
        this.clips = Map.copyOf(var1);
    }

    public static QizaiActionRig read(Reader var0) {
        JsonObject var1 = JsonParser.parseReader(var0).getAsJsonObject();
        if (var1.get("format").getAsInt() == 1 && var1.getAsJsonArray("channels").size() == 14) {
            LinkedHashMap var2 = new LinkedHashMap();

            for (Entry var4 : var1.getAsJsonObject("profiles").entrySet()) {
                JsonObject var5 = ((JsonElement)var4.getValue()).getAsJsonObject();
                double var6 = var5.get("length").getAsDouble();
                if (Double.isFinite(var6) && !(var6 <= 0.0) && !(var6 > 20.0)) {
                    ArrayList var8 = new ArrayList();
                    double var9 = -1.0;

                    for (JsonElement var12 : var5.getAsJsonArray("keys")) {
                        JsonArray var13 = var12.getAsJsonArray();
                        if (var13.size() != 15) {
                            throw new IllegalArgumentException("Invalid key width");
                        }

                        double var14 = var13.get(0).getAsDouble();
                        if (!Double.isFinite(var14) || var14 < 0.0 || var14 > 1.0 || var14 <= var9) {
                            throw new IllegalArgumentException("Invalid key ordering");
                        }

                        double[] var16 = new double[14];

                        for (int var17 = 0; var17 < 14; var17++) {
                            var16[var17] = var13.get(var17 + 1).getAsDouble();
                            if (!Double.isFinite(var16[var17]) || Math.abs(var16[var17]) > 180.0) {
                                throw new IllegalArgumentException("Invalid action value");
                            }
                        }

                        var8.add(new QizaiActionRig.Key(var14, var16));
                        var9 = var14;
                    }

                    if (var8.size() >= 2
                        && var8.size() <= 64
                        && ((QizaiActionRig.Key)var8.get(0)).time() == 0.0
                        && ((QizaiActionRig.Key)var8.get(var8.size() - 1)).time() == 1.0) {
                        var2.put((String)var4.getKey(), new QizaiActionRig.Clip(var6, List.copyOf(var8)));
                        continue;
                    }

                    throw new IllegalArgumentException("Incomplete action keys");
                }

                throw new IllegalArgumentException("Invalid clip duration");
            }

            for (String var19 : List.of(
                "ready",
                "volley_charge",
                "volley_hold",
                "volley_release",
                "nova_charge",
                "nova_release",
                "fall_charge",
                "fall_hold",
                "fall_release",
                "prayer_charge",
                "prayer_hold",
                "prayer_release",
                "step_release",
                "cross_charge",
                "cross_hold",
                "cross_release",
                "refrain_charge",
                "refrain_hold",
                "refrain_release",
                "sweep_charge",
                "sweep_hold",
                "sweep_release",
                "prism_charge",
                "prism_hold",
                "prism_release",
                "barrage_charge",
                "barrage_hold",
                "barrage_release"
            )) {
                if (!var2.containsKey(var19)) {
                    throw new IllegalArgumentException("Missing caster clip: " + var19);
                }
            }

            return new QizaiActionRig(var2);
        } else {
            throw new IllegalArgumentException("Unsupported action data");
        }
    }

    private static double slope(List<QizaiActionRig.Key> var0, int var1, int var2) {
        if (var1 != 0 && var1 != var0.size() - 1) {
            QizaiActionRig.Key var3 = (QizaiActionRig.Key)var0.get(var1 - 1);
            QizaiActionRig.Key var4 = (QizaiActionRig.Key)var0.get(var1);
            QizaiActionRig.Key var5 = (QizaiActionRig.Key)var0.get(var1 + 1);
            double var6 = (var4.values()[var2] - var3.values()[var2]) / (var4.time() - var3.time());
            double var8 = (var5.values()[var2] - var4.values()[var2]) / (var5.time() - var4.time());
            if (var6 * var8 <= 0.0) {
                return 0.0;
            } else {
                double var10 = var4.time() - var3.time();
                double var12 = var5.time() - var4.time();
                double var14 = 2.0 * var12 + var10;
                double var16 = var12 + 2.0 * var10;
                return (var14 + var16) / (var14 / var6 + var16 / var8);
            }
        } else {
            return 0.0;
        }
    }

    public double[] channels(String var1, double var2) {
        QizaiActionRig.Clip var4 = this.clips.get(var1);
        if (var4 != null && Double.isFinite(var2)) {
            List var5 = var4.keys();
            double var6 = Math.max(0.0, Math.min(1.0, var2));
            if (var6 == 0.0) {
                return (double[])((QizaiActionRig.Key)var5.get(0)).values().clone();
            } else if (var6 == 1.0) {
                return (double[])((QizaiActionRig.Key)var5.get(var5.size() - 1)).values().clone();
            } else {
                int var8 = 1;

                while (((QizaiActionRig.Key)var5.get(var8)).time() < var6) {
                    var8++;
                }

                QizaiActionRig.Key var9 = (QizaiActionRig.Key)var5.get(var8 - 1);
                QizaiActionRig.Key var10 = (QizaiActionRig.Key)var5.get(var8);
                double var11 = var10.time() - var9.time();
                double var13 = (var6 - var9.time()) / var11;
                double var15 = var13 * var13;
                double var17 = var15 * var13;
                double[] var19 = new double[14];

                for (int var20 = 0; var20 < 14; var20++) {
                    var19[var20] = (2.0 * var17 - 3.0 * var15 + 1.0) * var9.values()[var20]
                        + (var17 - 2.0 * var15 + var13) * var11 * slope(var5, var8 - 1, var20)
                        + (-2.0 * var17 + 3.0 * var15) * var10.values()[var20]
                        + (var17 - var15) * var11 * slope(var5, var8, var20);
                }

                return var19;
            }
        } else {
            throw new IllegalArgumentException("Invalid action sample");
        }
    }

    public QizaiActionRig.Pose sample(String var1, double var2, boolean var4, double var5, double var7) {
        return pose(this.channels(var1, var2), var4, var5, var7);
    }

    public static double[] rotate(double[] var0, double[] var1) {
        double var2 = var0[0];
        double var4 = var0[1];
        double var6 = var0[2];
        double var8 = var1[0];
        double var10 = var1[1];
        double var12 = var1[2];
        double var14 = var4 * Math.cos(var8) - var6 * Math.sin(var8);
        var6 = var4 * Math.sin(var8) + var6 * Math.cos(var8);
        double var19 = var2 * Math.cos(var10) + var6 * Math.sin(var10);
        var6 = -var2 * Math.sin(var10) + var6 * Math.cos(var10);
        return new double[]{var19 * Math.cos(var12) - var14 * Math.sin(var12), var19 * Math.sin(var12) + var14 * Math.cos(var12), var6};
    }

    private static double[] angles(double[] var0, int var1) {
        return new double[]{Math.toRadians(var0[var1]), Math.toRadians(var0[var1 + 1]), Math.toRadians(var0[var1 + 2])};
    }

    private static double[] point(double[] var0, double[] var1, double[] var2) {
        double[] var3 = rotate(var0, var2);

        for (int var4 = 0; var4 < 3; var4++) {
            var3[var4] += var1[var4];
        }

        return var3;
    }

    private static double[] compose(double[] var0, double[] var1) {
        double[][] var2 = new double[][]{
            rotate(rotate(new double[]{1.0, 0.0, 0.0}, var1), var0),
            rotate(rotate(new double[]{0.0, 1.0, 0.0}, var1), var0),
            rotate(rotate(new double[]{0.0, 0.0, 1.0}, var1), var0)
        };
        return new double[]{Math.atan2(var2[1][2], var2[2][2]), Math.asin(Math.max(-1.0, Math.min(1.0, -var2[0][2]))), Math.atan2(var2[0][1], var2[0][0])};
    }

    public static QizaiActionRig.Pose pose(double[] var0, boolean var1, double var2, double var4) {
        double[] var6 = angles(var0, 0);
        double[] var7 = angles(var0, 3);
        double[] var8 = angles(var0, 6);
        double var9 = var0[9];
        if (var1) {
            var6[1] *= -1.0;
            var6[2] *= -1.0;
            var7[1] *= -1.0;
            var7[2] *= -1.0;
            var8[1] *= -1.0;
            var8[2] *= -1.0;
            var9 *= -1.0;
        }

        double[][] var11 = new double[6][3];
        double[][] var12 = new double[6][3];
        double[] var13 = new double[]{-var0[10] / 12.0 + var6[2] * 0.2, -var0[10] / 12.0 - var6[2] * 0.2};

        for (int var14 = 0; var14 < 2; var14++) {
            double var15 = var13[var14];
            var11[var14 + 4] = new double[]{var14 == 0 ? -1.9 : 1.9, 24.0 - 12.0 * Math.cos(var15) - 2.0 * Math.abs(Math.sin(var15)), -12.0 * Math.sin(var15)};
            var12[var14 + 4] = new double[]{var15, 0.0, 0.0};
        }

        double[] var18 = new double[]{var9, (var11[4][1] + var11[5][1]) / 2.0, var0[10]};
        double[] var19 = rotate(new double[]{0.0, 12.0, 0.0}, var6);
        double[] var16 = new double[3];

        for (int var17 = 0; var17 < 3; var17++) {
            var16[var17] = var18[var17] - var19[var17];
        }

        var11[0] = (double[])var16.clone();
        var11[1] = (double[])var16.clone();
        var12[1] = var6;
        var12[0] = new double[]{
            Math.max(-0.65, Math.min(0.45, var4 + Math.toRadians(var0[11]))),
            Math.max(-1.35, Math.min(1.35, var2 + Math.toRadians(var0[12]) * (double)(var1 ? -1 : 1))),
            Math.toRadians(var0[13]) * (double)(var1 ? -1 : 1)
        };
        var11[2] = point(new double[]{-5.0, 2.5, 0.0}, var16, var6);
        var11[3] = point(new double[]{5.0, 2.5, 0.0}, var16, var6);
        var12[2] = compose(var6, var1 ? var8 : var7);
        var12[3] = compose(var6, var1 ? var7 : var8);
        return new QizaiActionRig.Pose(var11, var12);
    }

    public static record Clip(double length, List<QizaiActionRig.Key> keys) {
    }

    public static record Key(double time, double[] values) {
    }

    public static record Pose(double[][] position, double[][] rotation) {
    }
}
