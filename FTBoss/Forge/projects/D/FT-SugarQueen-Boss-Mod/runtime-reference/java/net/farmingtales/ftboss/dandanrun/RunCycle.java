/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.dandanrun;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.farmingtales.ftboss.dandanmotion.MotionCurves;

public final class RunCycle {
    private static final double[][][] KEYS = RunCycle.load();

    private RunCycle() {
    }

    private static double[][][] load() {
        double[][][] dArray;
        block17: {
            InputStream inputStream = RunCycle.class.getResourceAsStream("/assets/ftboss/dandan_run/motion.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing final Dandan run curves");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                if (jsonObject.get("schema").getAsInt() != 1) {
                    throw new IllegalStateException("Unsupported run schema");
                }
                JsonArray jsonArray = jsonObject.getAsJsonArray("keys");
                if (jsonArray.size() != 17) {
                    throw new IllegalStateException("Run loop requires 17 ordered knots");
                }
                double[][][] dArray2 = new double[16][6][6];
                for (int i = 0; i < 17; ++i) {
                    JsonArray jsonArray2 = jsonArray.get(i).getAsJsonArray();
                    if (Math.abs(jsonArray2.get(0).getAsDouble() - (double)i / 16.0) > 1.0E-9) {
                        throw new IllegalStateException("Unordered run keys");
                    }
                    for (int j = 0; j < 6; ++j) {
                        for (int k = 0; k < 6; ++k) {
                            double d = jsonArray2.get(1).getAsJsonArray().get(j).getAsJsonArray().get(k).getAsDouble();
                            if (!Double.isFinite(d)) {
                                throw new IllegalStateException("Nonfinite run curves");
                            }
                            if (i == 16) {
                                if (!(Math.abs(dArray2[0][j][k] - d) > 1.0E-9)) continue;
                                throw new IllegalStateException("Run loop seam");
                            }
                            dArray2[i][j][k] = d;
                        }
                    }
                }
                dArray = dArray2;
                if (inputStream == null) break block17;
            }
            catch (Throwable throwable) {
                try {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception exception) {
                    throw new IllegalStateException("Cannot load Dandan run animation", exception);
                }
            }
            inputStream.close();
        }
        return dArray;
    }

    public static double[][] sample(double d, double d2) {
        int n;
        double d3 = (d - Math.floor(d)) * 16.0;
        int n2 = (int)Math.floor(d3);
        double d4 = d3 - (double)n2;
        double[][] dArray = new double[6][6];
        for (int i = 0; i < 6; ++i) {
            for (n = 0; n < 6; ++n) {
                double d5 = KEYS[Math.floorMod(n2 - 1, 16)][i][n];
                double d6 = KEYS[n2 % 16][i][n];
                double d7 = KEYS[(n2 + 1) % 16][i][n];
                double d8 = KEYS[(n2 + 2) % 16][i][n];
                double d9 = 0.5 * (2.0 * d6 + (-d5 + d7) * d4 + (2.0 * d5 - 5.0 * d6 + 4.0 * d7 - d8) * d4 * d4 + (-d5 + 3.0 * d6 - 3.0 * d7 + d8) * d4 * d4 * d4);
                dArray[i][n] = n >= 3 ? d9 * (0.78 + 0.22 * MotionCurves.clamp(d2)) : d9;
            }
        }
        double[][] dArray2 = MotionCurves.resolve(dArray);
        for (n = 4; n < 6; ++n) {
            dArray2[n][1] = RunCycle.footPivot(dArray2[n][3]) - Math.max(0.0, dArray[1][1] - dArray[n][1]);
        }
        return dArray2;
    }

    public static double footPivot(double d) {
        double d2 = Math.toRadians(d);
        return 24.0 - 12.0 * Math.cos(d2) - 2.0 * Math.abs(Math.sin(d2));
    }
}

