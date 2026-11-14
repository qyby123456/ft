package net.farmingtales.ftboss.client.xueli_run;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RunMath {
    public static final String[] NAMES = new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    private static final Map<String, RunMath.Clip> CLIPS = new LinkedHashMap<>();
    public static final double START;
    public static final double FULL;
    public static final double MAX_STEP;
    public static final double BLEND_TICKS;
    public static final double PHASE_FACTOR;

    private RunMath() {
    }

    public static Map<String, RunMath.Clip> clips() {
        return Collections.unmodifiableMap(CLIPS);
    }

    public static double activation(double var0) {
        if (Double.isFinite(var0) && !(var0 < 0.0) && !(var0 > MAX_STEP)) {
            double var2 = Math.max(0.0, Math.min(1.0, (var0 - START) / (FULL - START)));
            return var2 * var2 * (3.0 - 2.0 * var2);
        } else {
            return 0.0;
        }
    }

    public static double smooth(double var0, double var2, double var4) {
        return var0 + (var2 - var0) * (1.0 - Math.exp(-Math.max(0.0, Math.min(4.0, var4)) / BLEND_TICKS));
    }

    public static boolean eligible(
        int var0, boolean var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6, boolean var7, double var8, float var10
    ) {
        return var0 == 10
            && var1
            && !var2
            && var3
            && !var4
            && !var5
            && !var6
            && !var7
            && Double.isFinite(var8)
            && var8 <= MAX_STEP
            && Float.isFinite(var10)
            && (double)Math.abs(var10) > 0.02;
    }

    public static double[] rotate(double[] var0, double[] var1) {
        double var2 = Math.toRadians(var1[0]);
        double var4 = Math.toRadians(var1[1]);
        double var6 = Math.toRadians(var1[2]);
        double var8 = var0[1] * Math.cos(var2) - var0[2] * Math.sin(var2);
        double var10 = var0[1] * Math.sin(var2) + var0[2] * Math.cos(var2);
        double var12 = var0[0] * Math.cos(var4) + var10 * Math.sin(var4);
        var10 = -var0[0] * Math.sin(var4) + var10 * Math.cos(var4);
        return new double[]{var12 * Math.cos(var6) - var8 * Math.sin(var6), var12 * Math.sin(var6) + var8 * Math.cos(var6), var10};
    }

    public static double[][] positions(double[][] var0, double var1, double var3) {
        double var5 = Math.sin(var1 * Math.PI * 2.0);
        double var7 = -100.0;

        for (int var12 : new int[]{4, 5}) {
            for (double var16 : new double[]{-2.0, 2.0}) {
                for (double var21 : new double[]{0.0, 12.0}) {
                    for (double var26 : new double[]{-2.0, 2.0}) {
                        var7 = Math.max(var7, rotate(new double[]{var16, var21, var26}, var0[var12])[1]);
                    }
                }
            }
        }

        double var28 = 24.0 - var7;
        double[][] var29 = new double[6][3];
        var29[4] = new double[]{-1.9, var28 - Math.max(0.0, var5) * 0.85 * var3, 0.0};
        var29[5] = new double[]{1.9, var28 - Math.max(0.0, -var5) * 0.85 * var3, 0.0};
        double[] var30 = rotate(new double[]{0.0, -12.0, 0.0}, var0[1]);
        var29[1] = new double[]{var30[0], var28 + var30[1], var30[2]};
        var29[0] = (double[])var29[1].clone();

        for (int var34 : new int[]{2, 3}) {
            double[] var17 = rotate(new double[]{var34 == 2 ? -5.0 : 5.0, -9.5, 0.0}, var0[1]);
            var29[var34] = new double[]{var17[0], var28 + var17[1], var17[2]};
        }

        return var29;
    }

    public static RunMath.State sample(String var0, double var1, boolean var3, double var4) {
        RunMath.Clip var6 = CLIPS.get(var0);
        if (var6 == null) {
            throw new IllegalArgumentException("Unknown running clip: " + var0);
        } else {
            double var7 = Math.sin(var1 * Math.PI * 2.0);
            double var9 = 9.0 + 3.0 * var4 + 1.4 * Math.cos(var1 * Math.PI * 4.0);
            double var11 = (30.0 + 7.0 * var4) * var7;
            int var13 = var3 ? 3 : 2;
            int var14 = var3 ? 2 : 3;
            int var15 = var3 ? -1 : 1;
            double[][] var16 = new double[][]{
                {-var9 * 0.3, 0.0, -var7},
                {var9, 2.6 * var7, 1.6 * var7},
                {-12.0 + 25.0 * var7, 0.0, 9.0},
                {-12.0 - 25.0 * var7, 0.0, -9.0},
                {-var11, 0.0, 0.0},
                {var11, 0.0, 0.0}
            };
            var16[var13] = new double[]{var6.pitch() + (double)var15 * var6.swing() * var7, (double)(-var15 * 7), (double)var15 * var6.roll()};
            if (var0.equals("empty")) {
                var16[var14] = new double[]{var6.pitch() - (double)var15 * var6.swing() * var7, (double)(var15 * 7), (double)(-var15) * var6.roll()};
            }

            return new RunMath.State(var16, positions(var16, var1, 1.0));
        }
    }

    static {
        try {
            try (InputStream var0 = RunMath.class.getResourceAsStream("/assets/ftboss/xueli_run/gait.json")) {
                if (var0 == null) {
                    throw new IllegalStateException("Missing authored Xueli running resource");
                }

                JsonObject var1 = JsonParser.parseReader(new InputStreamReader(var0, StandardCharsets.UTF_8)).getAsJsonObject();
                START = var1.get("startSpeed").getAsDouble();
                FULL = var1.get("fullSpeed").getAsDouble();
                MAX_STEP = var1.get("maxTrustedStep").getAsDouble();
                BLEND_TICKS = var1.get("blendTicks").getAsDouble();
                PHASE_FACTOR = var1.get("phaseFactor").getAsDouble();

                for (JsonElement var3 : var1.getAsJsonArray("clips")) {
                    JsonObject var4 = var3.getAsJsonObject();
                    String var5 = var4.get("id").getAsString();
                    CLIPS.put(
                        var5,
                        new RunMath.Clip(var5, var4.get("carryPitch").getAsDouble(), var4.get("carrySwing").getAsDouble(), var4.get("carryRoll").getAsDouble())
                    );
                }

                if (!(START > 0.0) || !(FULL > START) || !(MAX_STEP > FULL) || !(BLEND_TICKS > 0.0) || CLIPS.size() != 3) {
                    throw new IllegalStateException("Invalid authored running configuration");
                }
            }
        } catch (Exception var8) {
            throw new IllegalStateException("Cannot load Xueli running motion", var8);
        }
    }

    public static record Clip(String id, double pitch, double swing, double roll) {
    }

    public static record State(double[][] angles, double[][] positions) {
    }
}
