package net.farmingtales.ftboss.xueli_basic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BasicMath {
    public static final String[] NAMES = new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    public static final double[][] BASES = new double[][]{
        {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {-5.0, 2.5, 0.0}, {5.0, 2.5, 0.0}, {-1.9, 12.0, 0.0}, {1.9, 12.0, 0.0}
    };
    private static final Map<String, BasicMath.Attack> ATTACKS = load();

    private BasicMath() {
    }

    public static Map<String, BasicMath.Attack> attacks() {
        return ATTACKS;
    }

    private static Map<String, BasicMath.Attack> load() {
        try {
            Map var20;
            try (InputStream var0 = BasicMath.class.getResourceAsStream("/assets/ftboss/xueli_basic/attacks.json")) {
                if (var0 == null) {
                    throw new IllegalStateException("Missing weapon-specific basic motions");
                }

                JsonObject var1 = JsonParser.parseReader(new InputStreamReader(var0, StandardCharsets.UTF_8)).getAsJsonObject();
                LinkedHashMap var2 = new LinkedHashMap();

                for (JsonElement var4 : var1.getAsJsonArray("attacks")) {
                    JsonObject var5 = var4.getAsJsonObject();
                    ArrayList var6 = new ArrayList();
                    double var7 = -1.0;

                    for (JsonElement var10 : var5.getAsJsonArray("keys")) {
                        JsonObject var11 = var10.getAsJsonObject();
                        double var12 = var11.get("phase").getAsDouble();
                        if (!Double.isFinite(var12) || var12 < 0.0 || var12 > 1.0 || var12 <= var7) {
                            throw new IllegalArgumentException("Invalid motion key order");
                        }

                        var7 = var12;
                        double[][] var14 = new double[6][3];

                        for (int var15 = 0; var15 < 6; var15++) {
                            for (int var16 = 0; var16 < 3; var16++) {
                                var14[var15][var16] = var11.getAsJsonObject("angles").getAsJsonArray(NAMES[var15]).get(var16).getAsDouble();
                                if (!Double.isFinite(var14[var15][var16]) || Math.abs(var14[var15][var16]) > 165.0) {
                                    throw new IllegalArgumentException("Invalid joint rotation");
                                }
                            }
                        }

                        var6.add(new BasicMath.Key(var12, var14));
                    }

                    String var21 = var5.get("id").getAsString();
                    var2.put(var21, new BasicMath.Attack(var21, var5.get("weapon").getAsString(), var5.get("seconds").getAsDouble(), List.copyOf(var6)));
                }

                if (var2.size() != 6) {
                    throw new IllegalArgumentException("Exactly six basic clips required");
                }

                var20 = Map.copyOf(var2);
            }

            return var20;
        } catch (Exception var19) {
            throw new IllegalStateException("Cannot load Xueli basic motion", var19);
        }
    }

    public static int wind(int var0) {
        return Math.min(5, Math.max(1, var0 / 3));
    }

    public static int recovery(int var0) {
        return Math.max(1, Math.min(12, var0 - wind(var0)));
    }

    public static double smooth(double var0) {
        double var2 = Math.max(0.0, Math.min(1.0, var0));
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static double timing(boolean var0, double var1, int var3) {
        return var0
            ? Math.min(0.4, Math.max(0.0, var1) / (double)Math.max(1, var3) * 0.4)
            : Math.min(1.0, 0.4 + Math.max(0.0, var1) / (double)Math.max(1, var3) * 0.6);
    }

    public static double envelope(boolean var0, double var1, int var3) {
        if (!(var1 < 0.0) && !(var1 >= (double)var3)) {
            return var0 ? smooth(var1 / (double)Math.min(2, var3)) : 1.0 - smooth((var1 - (double)var3 * 0.65) / ((double)var3 * 0.35));
        } else {
            return 0.0;
        }
    }

    public static double[] rotate(double[] var0, double[] var1) {
        double var2 = var0[0];
        double var4 = var0[1];
        double var6 = var0[2];
        double[] var8 = new double[]{Math.toRadians(var1[0]), Math.toRadians(var1[1]), Math.toRadians(var1[2])};
        double var9 = var4 * Math.cos(var8[0]) - var6 * Math.sin(var8[0]);
        var6 = var4 * Math.sin(var8[0]) + var6 * Math.cos(var8[0]);
        double var16 = var2 * Math.cos(var8[1]) + var6 * Math.sin(var8[1]);
        var6 = -var2 * Math.sin(var8[1]) + var6 * Math.cos(var8[1]);
        double var17 = var16 * Math.cos(var8[2]) - var9 * Math.sin(var8[2]);
        var4 = var16 * Math.sin(var8[2]) + var9 * Math.cos(var8[2]);
        return new double[]{var17, var4, var6};
    }

    public static BasicMath.Pose sample(String var0, double var1, boolean var3) {
        BasicMath.Attack var4 = ATTACKS.get(var0);
        if (var4 == null) {
            throw new IllegalArgumentException("Unknown basic attack");
        } else {
            double var5 = Math.max(0.0, Math.min(1.0, var1));
            int var7 = 0;

            while (var7 < var4.keys().size() - 1 && var4.keys().get(var7).phase() < var5) {
                var7++;
            }

            BasicMath.Key var8 = var4.keys().get(Math.max(0, var7 - 1));
            BasicMath.Key var9 = var4.keys().get(var7);
            double var10 = smooth(var8.phase() == var9.phase() ? 0.0 : (var5 - var8.phase()) / (var9.phase() - var8.phase()));
            double[][] var12 = new double[6][3];

            for (int var13 = 0; var13 < 6; var13++) {
                int var14 = var3 && var13 >= 2 ? (var13 % 2 == 0 ? var13 + 1 : var13 - 1) : var13;

                for (int var15 = 0; var15 < 3; var15++) {
                    var12[var13][var15] = var8.angles()[var14][var15] + (var9.angles()[var14][var15] - var8.angles()[var14][var15]) * var10;
                    if (var3 && var15 != 0) {
                        var12[var13][var15] = var12[var13][var15] * -1.0;
                    }
                }
            }

            return new BasicMath.Pose(var12, stance(var12));
        }
    }

    public static double[][] stance(double[][] var0) {
        double[][] var1 = new double[6][3];
        double[] var2 = rotate(new double[]{0.0, 12.0, 0.0}, var0[1]);
        var1[1] = new double[]{-var2[0], 12.0 - var2[1], -var2[2]};
        var1[0] = (double[])var1[1].clone();

        for (int var3 = 2; var3 < 4; var3++) {
            var1[var3] = rotate(BASES[var3], var0[1]);

            for (int var4 = 0; var4 < 3; var4++) {
                var1[var3][var4] = var1[var3][var4] + var1[1][var4];
            }
        }

        for (int var21 = 4; var21 < 6; var21++) {
            double var22 = Double.NEGATIVE_INFINITY;

            for (double var9 : new double[]{-2.0, 2.0}) {
                for (double var14 : new double[]{0.0, 12.0}) {
                    for (double var19 : new double[]{-2.0, 2.0}) {
                        var22 = Math.max(var22, rotate(new double[]{var9, var14, var19}, var0[var21])[1]);
                    }
                }
            }

            double[] var23 = rotate(new double[]{0.0, 12.0, 0.0}, var0[var21]);
            var1[var21] = new double[]{BASES[var21][0] - var23[0], 24.0 - var22, BASES[var21][2] - var23[2]};
        }

        return var1;
    }

    public static record Attack(String id, String weapon, double seconds, List<BasicMath.Key> keys) {
    }

    public static record Key(double phase, double[][] angles) {
    }

    public static record Pose(double[][] angles, double[][] positions) {
    }
}
