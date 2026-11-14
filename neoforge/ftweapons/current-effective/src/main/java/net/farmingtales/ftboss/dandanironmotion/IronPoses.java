package net.farmingtales.ftboss.dandanironmotion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.dandanmotion.MotionCurves;

public final class IronPoses {
    private static final Map<String, IronPoses.Key[]> CLIPS = load();

    private IronPoses() {
    }

    private static Map<String, IronPoses.Key[]> load() {
        LinkedHashMap var0 = new LinkedHashMap();

        try (InputStream var1 = IronPoses.class.getResourceAsStream("/assets/ftboss/dandan_iron_motion/motion.json")) {
            if (var1 == null) {
                throw new IllegalStateException("Missing final Iron-specific character curves");
            }

            JsonObject var2 = JsonParser.parseReader(new InputStreamReader(var1, StandardCharsets.UTF_8)).getAsJsonObject();
            if (var2.get("schema").getAsInt() != 1) {
                throw new IllegalStateException("Unsupported Iron pose schema");
            }

            for (JsonElement var4 : var2.getAsJsonArray("clips")) {
                JsonObject var5 = var4.getAsJsonObject();
                JsonArray var6 = var5.getAsJsonArray("keys");
                IronPoses.Key[] var7 = new IronPoses.Key[var6.size()];

                for (int var8 = 0; var8 < var7.length; var8++) {
                    JsonArray var9 = var6.get(var8).getAsJsonArray();
                    double[][] var10 = new double[6][6];

                    for (int var11 = 0; var11 < 6; var11++) {
                        for (int var12 = 0; var12 < 6; var12++) {
                            var10[var11][var12] = var9.get(1).getAsJsonArray().get(var11).getAsJsonArray().get(var12).getAsDouble();
                            if (!Double.isFinite(var10[var11][var12])) {
                                throw new IllegalStateException("Nonfinite character pose");
                            }
                        }
                    }

                    var7[var8] = new IronPoses.Key(var9.get(0).getAsDouble(), var10);
                    if (var8 > 0 && var7[var8].time() <= var7[var8 - 1].time()) {
                        throw new IllegalStateException("Unordered character keys");
                    }
                }

                var0.put(var5.get("id").getAsString(), var7);
            }
        } catch (Exception var15) {
            throw new IllegalStateException("Cannot load Iron character curves", var15);
        }

        if (var0.size() != 6) {
            throw new IllegalStateException("Six final Iron character clips required");
        } else {
            return var0;
        }
    }

    public static double[][] sample(String var0, double var1) {
        IronPoses.Key[] var3 = CLIPS.get(var0);
        if (var3 == null) {
            throw new IllegalArgumentException("Unknown Iron pose: " + var0);
        } else {
            var1 = MotionCurves.clamp(var1);
            IronPoses.Key var4 = var3[0];
            IronPoses.Key var5 = var3[var3.length - 1];

            for (int var6 = 1; var6 < var3.length; var6++) {
                if (var1 <= var3[var6].time()) {
                    var4 = var3[var6 - 1];
                    var5 = var3[var6];
                    break;
                }
            }

            double var12 = MotionCurves.clamp((var1 - var4.time()) / (var5.time() - var4.time()));
            var12 = var12 * var12 * (3.0 - 2.0 * var12);
            double[][] var8 = new double[6][6];

            for (int var9 = 0; var9 < 6; var9++) {
                for (int var10 = 0; var10 < 6; var10++) {
                    var8[var9][var10] = var4.pose()[var9][var10] * (1.0 - var12) + var5.pose()[var9][var10] * var12;
                }
            }

            return var8;
        }
    }

    public static double[][] recover(String var0, double var1, double var3) {
        double[][] var5 = sample(var0, var1);
        double[][] var6 = sample("candle_reap_charge", 0.0);
        var3 = MotionCurves.clamp(var3);
        var3 = var3 * var3 * (3.0 - 2.0 * var3);

        for (int var7 = 0; var7 < 6; var7++) {
            for (int var8 = 0; var8 < 6; var8++) {
                var5[var7][var8] = var5[var7][var8] * (1.0 - var3) + var6[var7][var8] * var3;
            }
        }

        return var5;
    }

    private static record Key(double time, double[][] pose) {
    }
}
