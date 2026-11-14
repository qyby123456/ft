package net.farmingtales.ftboss.dandanrun;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.farmingtales.ftboss.dandanmotion.MotionCurves;

public final class RunCycle {
    private static final double[][][] KEYS = load();

    private RunCycle() {
    }

    private static double[][][] load() {
        try {
            double[][][] var13;
            try (InputStream var0 = RunCycle.class.getResourceAsStream("/assets/ftboss/dandan_run/motion.json")) {
                if (var0 == null) {
                    throw new IllegalStateException("Missing final Dandan run curves");
                }

                JsonObject var1 = JsonParser.parseReader(new InputStreamReader(var0, StandardCharsets.UTF_8)).getAsJsonObject();
                if (var1.get("schema").getAsInt() != 1) {
                    throw new IllegalStateException("Unsupported run schema");
                }

                JsonArray var2 = var1.getAsJsonArray("keys");
                if (var2.size() != 17) {
                    throw new IllegalStateException("Run loop requires 17 ordered knots");
                }

                double[][][] var3 = new double[16][6][6];

                for (int var4 = 0; var4 < 17; var4++) {
                    JsonArray var5 = var2.get(var4).getAsJsonArray();
                    if (Math.abs(var5.get(0).getAsDouble() - (double)var4 / 16.0) > 1.0E-9) {
                        throw new IllegalStateException("Unordered run keys");
                    }

                    for (int var6 = 0; var6 < 6; var6++) {
                        for (int var7 = 0; var7 < 6; var7++) {
                            double var8 = var5.get(1).getAsJsonArray().get(var6).getAsJsonArray().get(var7).getAsDouble();
                            if (!Double.isFinite(var8)) {
                                throw new IllegalStateException("Nonfinite run curves");
                            }

                            if (var4 == 16) {
                                if (Math.abs(var3[0][var6][var7] - var8) > 1.0E-9) {
                                    throw new IllegalStateException("Run loop seam");
                                }
                            } else {
                                var3[var4][var6][var7] = var8;
                            }
                        }
                    }
                }

                var13 = var3;
            }

            return var13;
        } catch (Exception var12) {
            throw new IllegalStateException("Cannot load Dandan run animation", var12);
        }
    }

    public static double[][] sample(double var0, double var2) {
        double var4 = (var0 - Math.floor(var0)) * 16.0;
        int var6 = (int)Math.floor(var4);
        double var7 = var4 - (double)var6;
        double[][] var9 = new double[6][6];

        for (int var10 = 0; var10 < 6; var10++) {
            for (int var11 = 0; var11 < 6; var11++) {
                double var12 = KEYS[Math.floorMod(var6 - 1, 16)][var10][var11];
                double var14 = KEYS[var6 % 16][var10][var11];
                double var16 = KEYS[(var6 + 1) % 16][var10][var11];
                double var18 = KEYS[(var6 + 2) % 16][var10][var11];
                double var20 = 0.5
                    * (
                        2.0 * var14
                            + (-var12 + var16) * var7
                            + (2.0 * var12 - 5.0 * var14 + 4.0 * var16 - var18) * var7 * var7
                            + (-var12 + 3.0 * var14 - 3.0 * var16 + var18) * var7 * var7 * var7
                    );
                var9[var10][var11] = var11 >= 3 ? var20 * (0.78 + 0.22 * MotionCurves.clamp(var2)) : var20;
            }
        }

        double[][] var22 = MotionCurves.resolve(var9);

        for (int var23 = 4; var23 < 6; var23++) {
            var22[var23][1] = footPivot(var22[var23][3]) - Math.max(0.0, var9[1][1] - var9[var23][1]);
        }

        return var22;
    }

    public static double footPivot(double var0) {
        double var2 = Math.toRadians(var0);
        return 24.0 - 12.0 * Math.cos(var2) - 2.0 * Math.abs(Math.sin(var2));
    }
}
