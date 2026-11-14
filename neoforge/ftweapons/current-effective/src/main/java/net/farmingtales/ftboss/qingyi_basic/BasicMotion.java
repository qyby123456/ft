package net.farmingtales.ftboss.qingyi_basic;

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

public final class BasicMotion {
    public static final String[] NAMES = new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    public static final double[][] PIVOTS = new double[][]{
        {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {-5.0, 2.5, 0.0}, {5.0, 2.5, 0.0}, {-1.9, 12.0, 0.0}, {1.9, 12.0, 0.0}
    };
    private static final Map<String, BasicMotion.Clip> CLIPS = load();
    private static final int[] MIRROR = new int[]{0, 1, 3, 2, 5, 4};

    private BasicMotion() {
    }

    private static Map<String, BasicMotion.Clip> load() {
        try {
            Map var20;
            try (InputStream var0 = BasicMotion.class.getResourceAsStream("/assets/ftboss/qingyi_basic_actions/motions.json")) {
                if (var0 == null) {
                    throw new IllegalStateException("Missing Qingyi melee motions");
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
                            throw new IllegalArgumentException("Unordered melee keys");
                        }

                        var7 = var12;
                        double[][] var14 = new double[6][3];

                        for (int var15 = 0; var15 < 6; var15++) {
                            for (int var16 = 0; var16 < 3; var16++) {
                                var14[var15][var16] = var11.getAsJsonArray("angles").get(var15).getAsJsonArray().get(var16).getAsDouble();
                                if (!Double.isFinite(var14[var15][var16]) || Math.abs(var14[var15][var16]) > 165.0) {
                                    throw new IllegalArgumentException("Unsafe melee key");
                                }
                            }
                        }

                        var6.add(new BasicMotion.Key(var12, var14));
                    }

                    if (((BasicMotion.Key)var6.get(0)).phase() != 0.0 || ((BasicMotion.Key)var6.get(var6.size() - 1)).phase() != 1.0) {
                        throw new IllegalArgumentException("Incomplete melee clip");
                    }

                    String var21 = var5.get("id").getAsString();
                    if (var2.put(var21, new BasicMotion.Clip(var21, List.copyOf(var6))) != null) {
                        throw new IllegalArgumentException("Duplicate melee id");
                    }
                }

                var20 = Collections.unmodifiableMap(var2);
            }

            return var20;
        } catch (Exception var19) {
            throw new IllegalStateException("Cannot load Qingyi melee motions", var19);
        }
    }

    public static Map<String, BasicMotion.Clip> clips() {
        return CLIPS;
    }

    public static double smooth(double var0) {
        double var2 = Math.max(0.0, Math.min(1.0, var0));
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static int preparation(int var0) {
        return Math.min(4, Math.max(1, var0 / 3));
    }

    public static int recovery(int var0) {
        return Math.min(10, Math.max(1, var0 - preparation(var0)));
    }

    public static double phase(boolean var0, double var1, int var3) {
        double var4 = Math.max(0.0, Math.min(1.0, var1 / (double)Math.max(1, var3)));
        return var0 ? 0.33 * var4 : 0.42 + 0.58 * var4;
    }

    public static double weight(boolean var0, double var1, int var3) {
        if (Double.isFinite(var1) && !(var1 < 0.0) && !(var1 >= (double)var3) && var3 >= 1 && var3 <= 40) {
            return var0 ? smooth((var1 + 0.1) / (double)Math.min(2, var3)) : smooth(((double)var3 - var1) / Math.min(3.0, (double)var3 * 0.4));
        } else {
            return 0.0;
        }
    }

    public static BasicMotion.State sample(String var0, double var1, boolean var3) {
        double[][] var4 = sampleAngles(var0, var1, var3);
        return new BasicMotion.State(var4, positions(var4));
    }

    public static double[][] sampleAngles(String var0, double var1, boolean var3) {
        BasicMotion.Clip var4 = CLIPS.get(var0);
        if (var4 != null && Double.isFinite(var1)) {
            double var5 = Math.max(0.0, Math.min(1.0, var1));
            BasicMotion.Key var7 = var4.keys().get(0);
            BasicMotion.Key var8 = var7;

            for (BasicMotion.Key var10 : var4.keys()) {
                var8 = var10;
                if (var10.phase() >= var5) {
                    break;
                }

                var7 = var10;
            }

            double var15 = var8.phase() > var7.phase() ? smooth((var5 - var7.phase()) / (var8.phase() - var7.phase())) : 0.0;
            double[][] var11 = new double[6][3];

            for (int var12 = 0; var12 < 6; var12++) {
                for (int var13 = 0; var13 < 3; var13++) {
                    int var14 = var3 ? MIRROR[var12] : var12;
                    var11[var12][var13] = (var7.angles()[var14][var13] + (var8.angles()[var14][var13] - var7.angles()[var14][var13]) * var15)
                        * (double)(var3 && var13 != 0 ? -1 : 1);
                }
            }

            return var11;
        } else {
            throw new IllegalArgumentException("Invalid melee sample");
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

    private static double[] matrix(double[] var0) {
        double var1 = Math.toRadians(var0[0]);
        double var3 = Math.toRadians(var0[1]);
        double var5 = Math.toRadians(var0[2]);
        double var7 = Math.cos(var1);
        double var9 = Math.sin(var1);
        double var11 = Math.cos(var3);
        double var13 = Math.sin(var3);
        double var15 = Math.cos(var5);
        double var17 = Math.sin(var5);
        return new double[]{
            var15 * var11,
            var15 * var13 * var9 - var17 * var7,
            var15 * var13 * var7 + var17 * var9,
            var17 * var11,
            var17 * var13 * var9 + var15 * var7,
            var17 * var13 * var7 - var15 * var9,
            -var13,
            var11 * var9,
            var11 * var7
        };
    }

    public static double[][] positions(double[][] var0) {
        return positions(var0, true);
    }

    public static double[][] positions(double[][] var0, boolean var1) {
        double[][] var2 = new double[6][3];
        double[] var3 = matrix(var0[1]);

        for (int var4 = 0; var4 < 4; var4++) {
            double var5 = PIVOTS[var4][0];
            double var7 = PIVOTS[var4][1] - 12.0;
            var2[var4][0] = var3[0] * var5 + var3[1] * var7;
            var2[var4][1] = 12.0 + var3[3] * var5 + var3[4] * var7;
            var2[var4][2] = var3[6] * var5 + var3[7] * var7;
        }

        if (var1) {
            for (int var9 = 4; var9 < 6; var9++) {
                double[] var10 = matrix(var0[var9]);
                double var6 = 12.0 * var10[4] + 2.0 * Math.abs(var10[3]) + 2.0 * Math.abs(var10[5]);
                var2[var9][0] = PIVOTS[var9][0] - 12.0 * var10[1];
                var2[var9][1] = 24.0 - var6;
                var2[var9][2] = -12.0 * var10[7];
            }
        }

        return var2;
    }

    public static record Clip(String id, List<BasicMotion.Key> keys) {
    }

    public static record Key(double phase, double[][] angles) {
    }

    public static record State(double[][] angles, double[][] positions) {
    }
}
