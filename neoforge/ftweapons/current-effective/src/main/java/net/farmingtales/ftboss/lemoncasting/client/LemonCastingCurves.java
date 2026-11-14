package net.farmingtales.ftboss.lemoncasting.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class LemonCastingCurves {
    private static final Map<String, LemonCastingCurves.Clip> CLIPS = load();
    private static final double RAD = Math.PI / 180.0;
    public static final double[][] PIVOTS = new double[][]{
        {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {-5.0, 2.5, 0.0}, {5.0, 2.5, 0.0}, {-1.9, 12.0, 0.0}, {1.9, 12.0, 0.0}
    };

    private LemonCastingCurves() {
    }

    private static Map<String, LemonCastingCurves.Clip> load() {
        HashMap var0 = new HashMap();

        try {
            Map var15;
            try (InputStream var1 = LemonCastingCurves.class.getResourceAsStream("/assets/ftboss/lemon_casting/choreography.json")) {
                if (var1 == null) {
                    throw new IllegalStateException("Missing Lemoncat choreography");
                }

                JsonObject var2 = JsonParser.parseReader(new InputStreamReader(var1, StandardCharsets.UTF_8)).getAsJsonObject();

                for (JsonElement var4 : var2.getAsJsonArray("clips")) {
                    JsonObject var5 = var4.getAsJsonObject();
                    JsonArray var6 = var5.getAsJsonArray("keys");
                    double[][] var7 = new double[var6.size()][13];

                    for (int var8 = 0; var8 < var6.size(); var8++) {
                        JsonArray var9 = var6.get(var8).getAsJsonArray();
                        var7[var8][0] = var9.get(0).getAsDouble();

                        for (int var10 = 1; var10 <= 4; var10++) {
                            for (int var11 = 0; var11 < 3; var11++) {
                                var7[var8][1 + (var10 - 1) * 3 + var11] = var9.get(var10).getAsJsonArray().get(var11).getAsDouble();
                            }
                        }
                    }

                    var0.put(var5.get("id").getAsString(), new LemonCastingCurves.Clip(var5.get("id").getAsString(), var7));
                }

                var15 = Map.copyOf(var0);
            }

            return var15;
        } catch (IOException var14) {
            throw new IllegalStateException(var14);
        }
    }

    public static LemonCastingCurves.Clip clip(String var0) {
        return CLIPS.get(var0);
    }

    public static double clamp(double var0, double var2, double var4) {
        return Math.max(var2, Math.min(var4, var0));
    }

    public static double[] sample(LemonCastingCurves.Clip var0, double var1) {
        double[][] var3 = var0.keys();
        var1 = clamp(var1, 0.0, 1.0);
        int var4 = 1;

        while (var4 < var3.length - 1 && var3[var4][0] < var1) {
            var4++;
        }

        double[] var5 = var3[var4 - 1];
        double[] var6 = var3[var4];
        double var7 = clamp((var1 - var5[0]) / (var6[0] - var5[0]), 0.0, 1.0);
        double var9 = var7 * var7 * (3.0 - 2.0 * var7);
        double[] var11 = new double[12];

        for (int var12 = 0; var12 < 12; var12++) {
            var11[var12] = var5[var12 + 1] + (var6[var12 + 1] - var5[var12 + 1]) * var9;
        }

        return var11;
    }

    public static double[] rotate(double[] var0, double[] var1) {
        double var2 = var1[0] * (Math.PI / 180.0);
        double var4 = var1[1] * (Math.PI / 180.0);
        double var6 = var1[2] * (Math.PI / 180.0);
        double var8 = var0[0];
        double var10 = var0[1] * Math.cos(var2) - var0[2] * Math.sin(var2);
        double var12 = var0[1] * Math.sin(var2) + var0[2] * Math.cos(var2);
        double var14 = var8 * Math.cos(var4) + var12 * Math.sin(var4);
        var12 = -var8 * Math.sin(var4) + var12 * Math.cos(var4);
        return new double[]{var14 * Math.cos(var6) - var10 * Math.sin(var6), var14 * Math.sin(var6) + var10 * Math.cos(var6), var12};
    }

    public static double[] compose(double[] var0, double[] var1) {
        double[][] var2 = new double[3][];

        for (int var3 = 0; var3 < 3; var3++) {
            double[] var4 = new double[3];
            var4[var3] = 1.0;
            var2[var3] = rotate(rotate(var4, var1), var0);
        }

        return new double[]{
            Math.atan2(var2[1][2], var2[2][2]) / (Math.PI / 180.0),
            Math.asin(clamp(-var2[0][2], -1.0, 1.0)) / (Math.PI / 180.0),
            Math.atan2(var2[0][1], var2[0][0]) / (Math.PI / 180.0)
        };
    }

    public static double weight(double var0, double var2, String var4) {
        boolean var5 = var4.endsWith("_charge");
        if (!(var2 <= 0.0) && !(var0 < 0.0) && (var5 || !(var0 >= var2))) {
            double var6 = clamp((var5 ? var0 : var2 - var0) / Math.min(2.0, var2 * 0.18), 0.0, 1.0);
            return var6 * var6 * (3.0 - 2.0 * var6);
        } else {
            return 0.0;
        }
    }

    public static double[][] pose(String var0, double var1, double var3, int var5, double var6, double var8, double var10, double var12) {
        double[][] var14 = new double[6][6];

        for (int var15 = 0; var15 < 6; var15++) {
            System.arraycopy(PIVOTS[var15], 0, var14[var15], 0, 3);
        }

        LemonCastingCurves.Clip var32 = clip(var0);
        if (var32 == null) {
            return var14;
        } else {
            double var16 = clamp(var1 / var3, 0.0, 1.0);
            double var18 = weight(var1, var3, var0);
            double[] var20 = sample(var32, var16);
            double[] var21 = sample(var32, Math.max(0.0, var16 - 0.035));
            double[] var22 = new double[]{var20[3], var20[4] * (double)var5, var20[5] * (double)var5};
            double[] var23 = new double[]{var20[0] * (double)var5, var20[1], var20[2]};
            var14[0][3] = var12;
            var14[0][4] = var10;

            for (int var27 : new int[]{2, 3, 4, 5}) {
                var14[var27][3] = Math.cos(var6 * 0.6662 + (var27 != 2 && var27 != 5 ? 0.0 : Math.PI)) * var8 * (var27 >= 4 ? 1.4 : 1.0) / (Math.PI / 180.0);
            }

            for (int var33 = 0; var33 < 3; var33++) {
                var14[1][var33] = var23[var33] * var18;
                var14[1][var33 + 3] = var22[var33] * var18;
            }

            for (int var37 : new int[]{0, 2, 3}) {
                double[] var28;
                if (var37 == 0) {
                    var28 = new double[]{var12 - var22[0] * 0.5, var10 - var21[4] * (double)var5 * 0.55, -var22[2] * 0.5};
                } else {
                    int var29 = var37 == (var5 == 1 ? 2 : 3) ? 6 : 9;
                    var28 = new double[]{var20[var29], var20[var29 + 1] * (double)var5, var20[var29 + 2] * (double)var5};
                }

                double[] var38 = compose(var22, var28);
                double[] var30 = rotate(PIVOTS[var37], var22);

                for (int var31 = 0; var31 < 3; var31++) {
                    var14[var37][var31] = PIVOTS[var37][var31] + (var30[var31] + var23[var31] - PIVOTS[var37][var31]) * var18;
                    var14[var37][var31 + 3] = var14[var37][var31 + 3] + (var38[var31] - var14[var37][var31 + 3]) * var18;
                }
            }

            return var14;
        }
    }

    public static record Clip(String id, double[][] keys) {
    }
}
