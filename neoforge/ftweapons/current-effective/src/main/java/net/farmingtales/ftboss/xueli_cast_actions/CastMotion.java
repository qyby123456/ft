package net.farmingtales.ftboss.xueli_cast_actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CastMotion {
    public static final String[] NAMES = new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    public static final double[][] BASE = new double[][]{
        {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {-5.0, 2.5, 0.0}, {5.0, 2.5, 0.0}, {-1.9, 12.0, 0.0}, {1.9, 12.0, 0.0}
    };
    private static final Map<String, CastMotion.Clip> CLIPS = load();

    private CastMotion() {
    }

    private static Map<String, CastMotion.Clip> load() {
        try {
            Map var20;
            try (InputStream var0 = CastMotion.class.getResourceAsStream("/assets/ftboss/xueli_cast_actions/motions.json")) {
                if (var0 == null) {
                    throw new IllegalStateException("Missing Xueli cast motions");
                }

                JsonObject var1 = JsonParser.parseReader(new InputStreamReader(var0, StandardCharsets.UTF_8)).getAsJsonObject();
                LinkedHashMap var2 = new LinkedHashMap();

                for (JsonElement var4 : var1.getAsJsonArray("clips")) {
                    JsonObject var5 = var4.getAsJsonObject();
                    ArrayList var6 = new ArrayList();
                    double var7 = -1.0;

                    for (JsonElement var10 : var5.getAsJsonArray("keys")) {
                        JsonObject var11 = var10.getAsJsonObject();
                        double var12 = var11.get("phase").getAsDouble();
                        if (!Double.isFinite(var12) || var12 < 0.0 || var12 > 1.0 || var12 <= var7) {
                            throw new IllegalArgumentException("Invalid Xueli motion phases");
                        }

                        var7 = var12;
                        double[][] var14 = new double[6][3];

                        for (int var15 = 0; var15 < 6; var15++) {
                            for (int var16 = 0; var16 < 3; var16++) {
                                var14[var15][var16] = var11.getAsJsonArray("angles").get(var15).getAsJsonArray().get(var16).getAsDouble();
                                if (!Double.isFinite(var14[var15][var16]) || Math.abs(var14[var15][var16]) > 165.0) {
                                    throw new IllegalArgumentException("Unsafe Xueli rotation");
                                }
                            }
                        }

                        var6.add(new CastMotion.Key(var12, var14));
                    }

                    String var21 = var5.get("id").getAsString();
                    if (((CastMotion.Key)var6.get(0)).phase() != 0.0
                        || ((CastMotion.Key)var6.get(var6.size() - 1)).phase() != 1.0
                        || var2.put(var21, new CastMotion.Clip(var21, List.copyOf(var6))) != null) {
                        throw new IllegalArgumentException("Bad cast clip");
                    }
                }

                if (var2.size() != 13) {
                    throw new IllegalArgumentException("Expected thirteen Xueli cast clips");
                }

                var20 = Collections.unmodifiableMap(var2);
            }

            return var20;
        } catch (Exception var19) {
            throw new IllegalStateException("Xueli cast asset load failed", var19);
        }
    }

    public static Map<String, CastMotion.Clip> clips() {
        return CLIPS;
    }

    public static double smooth(double var0) {
        double var2 = Math.max(0.0, Math.min(1.0, var0));
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static double phase(String var0, double var1, int var3) {
        double var4 = Math.max(0.0, Math.min(1.0, var1 / (double)Math.max(1, var3)));
        return var0.equals("charge") ? 0.46 * var4 : (var0.equals("hold") ? 0.46 : 0.46 + 0.54 * var4);
    }

    public static double weight(String var0, double var1, int var3) {
        if (Double.isFinite(var1) && !(var1 < 0.0) && !(var1 >= (double)var3) && var3 >= 1 && var3 <= 240) {
            double var4 = !var0.equals("charge") && !var0.equals("hold")
                ? (var0.equals("instant") ? smooth(var1 / (double)Math.min(1, var3)) : 1.0)
                : smooth(var1 / (double)Math.min(2, var3));
            return var0.equals("charge") ? var4 : var4 * smooth(((double)var3 - var1) / Math.min(4.0, (double)var3 * 0.5));
        } else {
            return 0.0;
        }
    }

    public static CastMotion.State combined(String var0, String var1, double var2, boolean var4) {
        double[][] var5 = sampleAngles(var0, var2, var4);
        if (!var0.equals(var1) && CLIPS.containsKey(var1)) {
            int var6 = var4 ? 2 : 3;
            var5[var6] = sampleAngles(var1, var2, var4)[var6];
        }

        return new CastMotion.State(var5, positions(var5));
    }

    public static CastMotion.State sample(String var0, double var1, boolean var3) {
        double[][] var4 = sampleAngles(var0, var1, var3);
        return new CastMotion.State(var4, positions(var4));
    }

    private static double[][] sampleAngles(String var0, double var1, boolean var3) {
        CastMotion.Clip var4 = CLIPS.get(var0);
        if (var4 != null && Double.isFinite(var1)) {
            double var5 = Math.max(0.0, Math.min(1.0, var1));
            CastMotion.Key var7 = var4.keys().get(0);
            CastMotion.Key var8 = var7;

            for (CastMotion.Key var10 : var4.keys()) {
                var8 = var10;
                if (var10.phase() >= var5) {
                    break;
                }

                var7 = var10;
            }

            double var16 = var8.phase() > var7.phase() ? smooth((var5 - var7.phase()) / (var8.phase() - var7.phase())) : 0.0;
            double[][] var11 = new double[6][3];

            for (int var12 = 0; var12 < 6; var12++) {
                for (int var13 = 0; var13 < 3; var13++) {
                    var11[var12][var13] = var7.angles()[var12][var13] + (var8.angles()[var12][var13] - var7.angles()[var12][var13]) * var16;
                }
            }

            if (var3) {
                double[][] var17 = new double[6][3];
                int[] var18 = new int[]{0, 1, 3, 2, 5, 4};

                for (int var14 = 0; var14 < 6; var14++) {
                    for (int var15 = 0; var15 < 3; var15++) {
                        var17[var14][var15] = var11[var18[var14]][var15] * (double)(var15 == 0 ? 1 : -1);
                    }
                }

                var11 = var17;
            }

            return var11;
        } else {
            throw new IllegalArgumentException("Invalid Xueli cast sample");
        }
    }

    public static double[] rotate(double[] var0, double[] var1) {
        double var2 = var0[0];
        double var4 = var0[1];
        double var6 = var0[2];
        double var8 = Math.toRadians(var1[0]);
        double var10 = Math.toRadians(var1[1]);
        double var12 = Math.toRadians(var1[2]);
        double var14 = var4 * Math.cos(var8) - var6 * Math.sin(var8);
        double var16 = var4 * Math.sin(var8) + var6 * Math.cos(var8);
        double var18 = var2 * Math.cos(var10) + var16 * Math.sin(var10);
        var16 = -var2 * Math.sin(var10) + var16 * Math.cos(var10);
        double var23 = var18 * Math.cos(var12) - var14 * Math.sin(var12);
        var14 = var18 * Math.sin(var12) + var14 * Math.cos(var12);
        return new double[]{var23, var14, var16};
    }

    public static double[][] positions(double[][] var0) {
        double[][] var1 = new double[6][3];
        double[] var2 = rotate(new double[]{0.0, -12.0, 0.0}, var0[1]);
        var1[1] = new double[]{var2[0], 12.0 + var2[1], var2[2]};

        for (int var6 : new int[]{0, 2, 3}) {
            double[] var7 = rotate(new double[]{BASE[var6][0], BASE[var6][1] - 12.0, BASE[var6][2]}, var0[1]);
            var1[var6] = new double[]{var7[0], 12.0 + var7[1], var7[2]};
        }

        for (int var16 = 4; var16 < 6; var16++) {
            double var17 = -Double.MAX_VALUE;

            for (double var9 : new double[]{-2.0, 2.0}) {
                for (double var14 : new double[]{-2.0, 2.0}) {
                    var17 = Math.max(var17, rotate(new double[]{var9, 12.0, var14}, var0[var16])[1]);
                }
            }

            double[] var19 = rotate(new double[]{0.0, 12.0, 0.0}, var0[var16]);
            var1[var16] = new double[]{BASE[var16][0] - var19[0], 24.0 - var17, -var19[2]};
        }

        return var1;
    }

    public static record Clip(String id, List<CastMotion.Key> keys) {
    }

    public static record Key(double phase, double[][] angles) {
    }

    public static record State(double[][] angles, double[][] positions) {
    }
}
