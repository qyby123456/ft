package net.farmingtales.ftboss.client.maque;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MaqueHumanoidPose {
    public static final String[] PARTS = new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    private static final Map<String, MaqueHumanoidPose.Clip> CLIPS = load();

    private MaqueHumanoidPose() {
    }

    private static Map<String, MaqueHumanoidPose.Clip> load() {
        InputStream var0 = MaqueHumanoidPose.class.getResourceAsStream("/assets/ftboss/maque_arcane/humanoid_profile.json");
        if (var0 == null) {
            throw new IllegalStateException("Missing Maque humanoid profile");
        } else {
            try {
                Map var23;
                try (InputStreamReader var1 = new InputStreamReader(var0, StandardCharsets.UTF_8)) {
                    JsonObject var2 = JsonParser.parseReader(var1).getAsJsonObject();
                    if (var2.get("schema").getAsInt() != 1) {
                        throw new IllegalStateException("Unsupported Maque motion schema");
                    }

                    LinkedHashMap var3 = new LinkedHashMap();

                    for (JsonElement var5 : var2.getAsJsonArray("clips")) {
                        JsonObject var6 = var5.getAsJsonObject();
                        JsonArray var7 = var6.getAsJsonArray("keys");
                        double[] var8 = new double[var7.size()];
                        double[][][] var9 = new double[var7.size()][8][3];
                        int var10 = 0;

                        while (var10 < var7.size()) {
                            JsonObject var11 = var7.get(var10).getAsJsonObject();
                            var8[var10] = var11.get("time").getAsDouble();
                            if (Double.isFinite(var8[var10]) && (var10 <= 0 || !(var8[var10] <= var8[var10 - 1]))) {
                                for (int var12 = 0; var12 < 7; var12++) {
                                    for (int var13 = 0; var13 < 3; var13++) {
                                        double var14 = var11.getAsJsonArray(var12 < 6 ? PARTS[var12] : "shift").get(var13).getAsDouble();
                                        if (!Double.isFinite(var14) || Math.abs(var14) > (double)(var12 < 6 ? 140 : 4)) {
                                            throw new IllegalStateException("Unsafe Maque pose channel");
                                        }

                                        var9[var10][var12][var13] = var14;
                                    }
                                }

                                var9[var10][7][0] = var11.get("lift").getAsDouble();
                                if (Double.isFinite(var9[var10][7][0]) && !(var9[var10][7][0] < 0.0) && !(var9[var10][7][0] > 4.0)) {
                                    var10++;
                                    continue;
                                }

                                throw new IllegalStateException("Unsafe visual lift");
                            }

                            throw new IllegalStateException("Invalid Maque key time");
                        }

                        var10 = var6.get("ticks").getAsInt();
                        String var25 = var6.get("id").getAsString();
                        double[][][] var26 = new double[var7.size()][8][3];

                        for (int var27 = 1; var27 < var7.size() - 1; var27++) {
                            for (int var28 = 0; var28 < 8; var28++) {
                                for (int var15 = 0; var15 < 3; var15++) {
                                    double var16 = (var9[var27][var28][var15] - var9[var27 - 1][var28][var15]) / (var8[var27] - var8[var27 - 1]);
                                    double var18 = (var9[var27 + 1][var28][var15] - var9[var27][var28][var15]) / (var8[var27 + 1] - var8[var27]);
                                    var26[var27][var28][var15] = var16 * var18 <= 0.0 ? 0.0 : 2.0 * var16 * var18 / (var16 + var18);
                                }
                            }
                        }

                        if (var10 < 8
                            || var10 > 200
                            || var8[0] != 0.0
                            || var8[var8.length - 1] != 1.0
                            || var3.put(var25, new MaqueHumanoidPose.Clip(var25, var10, var8, var9, var26)) != null) {
                            throw new IllegalStateException("Invalid Maque clip");
                        }
                    }

                    if (var3.size() != 7) {
                        throw new IllegalStateException("Incomplete Maque motion profile");
                    }

                    var23 = Map.copyOf(var3);
                }

                return var23;
            } catch (Exception var22) {
                throw new IllegalStateException("Cannot load Maque humanoid motions", var22);
            }
        }
    }

    public static MaqueHumanoidPose.Clip clip(String var0) {
        return CLIPS.get(var0);
    }

    public static double clamp(double var0, double var2, double var4) {
        return Math.max(var2, Math.min(var4, var0));
    }

    public static double smooth(double var0) {
        double var2 = clamp(var0, 0.0, 1.0);
        return var2 * var2 * var2 * (10.0 + var2 * (-15.0 + 6.0 * var2));
    }

    private static double sample(MaqueHumanoidPose.Clip var0, double var1, int var3, int var4) {
        double var5 = clamp(var1, 0.0, 1.0);
        int var7 = 0;

        while (var7 < var0.times.length - 2 && var5 > var0.times[var7 + 1]) {
            var7++;
        }

        double var8 = var0.times[var7 + 1] - var0.times[var7];
        double var10 = clamp((var5 - var0.times[var7]) / var8, 0.0, 1.0);
        double var12 = var10 * var10;
        double var14 = var12 * var10;
        return (2.0 * var14 - 3.0 * var12 + 1.0) * var0.values[var7][var3][var4]
            + (var14 - 2.0 * var12 + var10) * var8 * var0.slopes[var7][var3][var4]
            + (-2.0 * var14 + 3.0 * var12) * var0.values[var7 + 1][var3][var4]
            + (var14 - var12) * var8 * var0.slopes[var7 + 1][var3][var4];
    }

    public static double[] rotation(double[] var0) {
        double var1 = Math.cos(var0[0]);
        double var3 = Math.sin(var0[0]);
        double var5 = Math.cos(var0[1]);
        double var7 = Math.sin(var0[1]);
        double var9 = Math.cos(var0[2]);
        double var11 = Math.sin(var0[2]);
        return new double[]{
            var9 * var5,
            var9 * var7 * var3 - var11 * var1,
            var9 * var7 * var1 + var11 * var3,
            var11 * var5,
            var11 * var7 * var3 + var9 * var1,
            var11 * var7 * var1 - var9 * var3,
            -var7,
            var5 * var3,
            var5 * var1
        };
    }

    public static double[] transform(double[] var0, double[] var1) {
        double[] var2 = new double[3];

        for (int var3 = 0; var3 < 3; var3++) {
            var2[var3] = var0[var3 * 3] * var1[0] + var0[var3 * 3 + 1] * var1[1] + var0[var3 * 3 + 2] * var1[2];
        }

        return var2;
    }

    private static double[] multiply(double[] var0, double[] var1) {
        double[] var2 = new double[9];

        for (int var3 = 0; var3 < 9; var3++) {
            int var4 = var3 / 3;
            int var5 = var3 % 3;
            var2[var3] = var0[var4 * 3] * var1[var5] + var0[var4 * 3 + 1] * var1[var5 + 3] + var0[var4 * 3 + 2] * var1[var5 + 6];
        }

        return var2;
    }

    private static double[] euler(double[] var0) {
        return new double[]{Math.atan2(var0[7], var0[8]), Math.asin(clamp(-var0[6], -1.0, 1.0)), Math.atan2(var0[3], var0[0])};
    }

    private static double[] part(double[] var0, double[] var1) {
        return new double[]{var0[0], var0[1], var0[2], var1[0], var1[1], var1[2]};
    }

    public static double[][] evaluate(String var0, double var1, boolean var3) {
        MaqueHumanoidPose.Clip var4 = clip(var0);
        if (var4 != null && Double.isFinite(var1)) {
            double[][] var5 = new double[6][3];

            for (int var6 = 0; var6 < 6; var6++) {
                for (int var7 = 0; var7 < 3; var7++) {
                    var5[var6][var7] = Math.toRadians(sample(var4, var1, var6, var7));
                }
            }

            double var30 = sample(var4, var1, 7, 0);
            double[] var8 = new double[]{sample(var4, var1, 6, 0), sample(var4, var1, 6, 1) - var30, sample(var4, var1, 6, 2)};
            if (var3) {
                for (int var12 : new int[]{2, 4}) {
                    double[] var13 = var5[var12];
                    var5[var12] = var5[var12 + 1];
                    var5[var12 + 1] = var13;
                }

                for (double[] var38 : var5) {
                    var38[1] *= -1.0;
                    var38[2] *= -1.0;
                }

                var8[0] *= -1.0;
            }

            double[] var32 = rotation(var5[1]);
            double[] var34 = transform(var32, new double[]{0.0, -12.0, 0.0});

            for (int var36 = 0; var36 < 3; var36++) {
                var34[var36] += var8[var36] + (double)(var36 == 1 ? 12 : 0);
            }

            double[][] var37 = new double[6][];
            var37[1] = part(var34, var5[1]);

            for (int var15 : new int[]{0, 2, 3}) {
                double[] var16 = var15 == 0 ? new double[3] : new double[]{var15 == 2 ? -5.0 : 5.0, 2.5, 0.0};
                double[] var17 = transform(var32, var16);

                for (int var18 = 0; var18 < 3; var18++) {
                    var17[var18] += var34[var18];
                }

                var37[var15] = part(var17, euler(multiply(var32, rotation(var5[var15]))));
            }

            for (int var44 : new int[]{4, 5}) {
                double[] var45 = rotation(var5[var44]);
                double[] var46 = transform(var45, new double[]{0.0, 12.0, 0.0});
                double var47 = Double.NEGATIVE_INFINITY;

                for (double var23 : new double[]{-2.0, 2.0}) {
                    for (double var28 : new double[]{-2.0, 2.0}) {
                        var47 = Math.max(var47, transform(var45, new double[]{var23, 12.0, var28})[1]);
                    }
                }

                var37[var44] = part(new double[]{(var44 == 4 ? -1.9 : 1.9) - var46[0], 24.0 - var47 - var30, -var46[2]}, var5[var44]);
            }

            return var37;
        } else {
            throw new IllegalArgumentException("Invalid Maque motion request");
        }
    }

    public static record Clip(String id, int ticks, double[] times, double[][][] values, double[][][] slopes) {
    }
}
