package net.farmingtales.ftboss.dandanmotion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MotionCurves {
    public static final double[][] PIVOTS = new double[][]{
        {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {-5.0, 2.5, 0.0}, {5.0, 2.5, 0.0}, {-1.9, 12.0, 0.0}, {1.9, 12.0, 0.0}
    };
    private static final Map<String, MotionCurves.Clip> CLIPS = load();

    private MotionCurves() {
    }

    private static Map<String, MotionCurves.Clip> load() {
        LinkedHashMap var0 = new LinkedHashMap();

        try (InputStream var1 = MotionCurves.class.getResourceAsStream("/assets/ftboss/dandan_motion/motion.json")) {
            if (var1 == null) {
                throw new IllegalStateException("Missing Dandan character curves");
            }

            JsonObject var2 = JsonParser.parseReader(new InputStreamReader(var1, StandardCharsets.UTF_8)).getAsJsonObject();
            if (var2.get("schema").getAsInt() != 1) {
                throw new IllegalStateException("Unsupported Dandan motion schema");
            }

            for (JsonElement var4 : var2.getAsJsonArray("clips")) {
                JsonObject var5 = var4.getAsJsonObject();
                JsonArray var6 = var5.getAsJsonArray("keys");
                MotionCurves.Key[] var7 = new MotionCurves.Key[var6.size()];

                for (int var8 = 0; var8 < var7.length; var8++) {
                    JsonArray var9 = var6.get(var8).getAsJsonArray();
                    double[][] var10 = new double[6][6];

                    for (int var11 = 0; var11 < 6; var11++) {
                        for (int var12 = 0; var12 < 6; var12++) {
                            var10[var11][var12] = var9.get(1).getAsJsonArray().get(var11).getAsJsonArray().get(var12).getAsDouble();
                        }
                    }

                    var7[var8] = new MotionCurves.Key(var9.get(0).getAsDouble(), var10);
                }

                String var16 = var5.get("id").getAsString();
                var0.put(var16, new MotionCurves.Clip(var16, var7));
            }
        } catch (Exception var15) {
            throw new IllegalStateException("Cannot read Dandan motion curves", var15);
        }

        return Map.copyOf(var0);
    }

    public static boolean has(String var0) {
        return CLIPS.containsKey(var0);
    }

    public static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, var0));
    }

    public static double smooth(double var0) {
        double var2 = clamp(var0);
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static double[][] sample(String var0, double var1) {
        MotionCurves.Clip var3 = CLIPS.get(var0);
        if (var3 == null) {
            throw new IllegalArgumentException("Unknown Dandan motion: " + var0);
        } else {
            double var4 = clamp(var1);
            MotionCurves.Key var6 = var3.keys()[0];
            MotionCurves.Key var7 = var3.keys()[var3.keys().length - 1];

            for (int var8 = 1; var8 < var3.keys().length; var8++) {
                if (var4 <= var3.keys()[var8].time()) {
                    var6 = var3.keys()[var8 - 1];
                    var7 = var3.keys()[var8];
                    break;
                }
            }

            double var13 = smooth((var4 - var6.time()) / (var7.time() - var6.time()));
            double[][] var10 = new double[6][6];

            for (int var11 = 0; var11 < 6; var11++) {
                for (int var12 = 0; var12 < 6; var12++) {
                    var10[var11][var12] = var6.pose()[var11][var12] * (1.0 - var13) + var7.pose()[var11][var12] * var13;
                }
            }

            return var10;
        }
    }

    public static double[] rotate(double[] var0, double[] var1) {
        double var2 = Math.toRadians(var1[0]);
        double var4 = Math.toRadians(var1[1]);
        double var6 = Math.toRadians(var1[2]);
        double var8 = var0[1] * Math.cos(var2) - var0[2] * Math.sin(var2);
        double var10 = var0[1] * Math.sin(var2) + var0[2] * Math.cos(var2);
        double var12 = var0[0] * Math.cos(var4) + var10 * Math.sin(var4);
        double var14 = -var0[0] * Math.sin(var4) + var10 * Math.cos(var4);
        return new double[]{var12 * Math.cos(var6) - var8 * Math.sin(var6), var12 * Math.sin(var6) + var8 * Math.cos(var6), var14};
    }

    public static double[][] resolve(double[][] var0) {
        double[][] var1 = new double[6][6];
        double[] var2 = rotate(new double[]{0.0, 12.0, 0.0}, new double[]{var0[1][3], var0[1][4], var0[1][5]});
        double[] var3 = new double[]{var0[1][0] - var2[0], var0[1][1] + 12.0 - var2[1], var0[1][2] - var2[2]};

        for (int var4 = 0; var4 < 6; var4++) {
            boolean var5 = var4 == 2 || var4 == 3;
            double[] var6 = var5 ? rotate(PIVOTS[var4], new double[]{var0[1][3], var0[1][4], var0[1][5]}) : PIVOTS[var4];

            for (int var7 = 0; var7 < 3; var7++) {
                var1[var4][var7] = var6[var7] + (var4 == 1 ? var3[var7] : var0[var4][var7] + (var4 < 4 ? var3[var7] : 0.0));
                var1[var4][var7 + 3] = var0[var4][var7 + 3] + (var5 ? var0[1][var7 + 3] : 0.0);
            }
        }

        return var1;
    }

    public static record Clip(String id, MotionCurves.Key[] keys) {
    }

    public static record Key(double time, double[][] pose) {
    }
}
