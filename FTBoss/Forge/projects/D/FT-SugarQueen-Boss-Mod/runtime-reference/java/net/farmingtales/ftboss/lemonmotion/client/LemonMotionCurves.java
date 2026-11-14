/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.lemonmotion.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class LemonMotionCurves {
    private static final Map<String, Clip> CLIPS = LemonMotionCurves.load();
    private static final double RAD = Math.PI / 180;
    public static final double[][] PIVOTS = new double[][]{{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {-5.0, 2.5, 0.0}, {5.0, 2.5, 0.0}, {-1.9, 12.0, 0.0}, {1.9, 12.0, 0.0}};

    private LemonMotionCurves() {
    }

    private static Map<String, Clip> load() {
        Map map;
        block13: {
            HashMap<String, Clip> hashMap = new HashMap<String, Clip>();
            InputStream inputStream = LemonMotionCurves.class.getResourceAsStream("/assets/ftboss/lemon_motion/choreography.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing Lemoncat choreography");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                for (JsonElement jsonElement : jsonObject.getAsJsonArray("clips")) {
                    JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                    JsonArray jsonArray = jsonObject2.getAsJsonArray("keys");
                    double[][] dArray = new double[jsonArray.size()][13];
                    for (int i = 0; i < jsonArray.size(); ++i) {
                        JsonArray jsonArray2 = jsonArray.get(i).getAsJsonArray();
                        dArray[i][0] = jsonArray2.get(0).getAsDouble();
                        for (int j = 1; j <= 4; ++j) {
                            for (int k = 0; k < 3; ++k) {
                                dArray[i][1 + (j - 1) * 3 + k] = jsonArray2.get(j).getAsJsonArray().get(k).getAsDouble();
                            }
                        }
                    }
                    hashMap.put(jsonObject2.get("id").getAsString(), new Clip(jsonObject2.get("id").getAsString(), dArray));
                }
                map = Map.copyOf(hashMap);
                if (inputStream == null) break block13;
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
                catch (IOException iOException) {
                    throw new IllegalStateException(iOException);
                }
            }
            inputStream.close();
        }
        return map;
    }

    public static Clip clip(String string) {
        return CLIPS.get(string);
    }

    public static double clamp(double d, double d2, double d3) {
        return Math.max(d2, Math.min(d3, d));
    }

    public static double[] sample(Clip clip, double d) {
        int n;
        double[][] dArray = clip.keys();
        d = LemonMotionCurves.clamp(d, 0.0, 1.0);
        for (n = 1; n < dArray.length - 1 && dArray[n][0] < d; ++n) {
        }
        double[] dArray2 = dArray[n - 1];
        double[] dArray3 = dArray[n];
        double d2 = LemonMotionCurves.clamp((d - dArray2[0]) / (dArray3[0] - dArray2[0]), 0.0, 1.0);
        double d3 = d2 * d2 * (3.0 - 2.0 * d2);
        double[] dArray4 = new double[12];
        for (int i = 0; i < 12; ++i) {
            dArray4[i] = dArray2[i + 1] + (dArray3[i + 1] - dArray2[i + 1]) * d3;
        }
        return dArray4;
    }

    public static double[] rotate(double[] dArray, double[] dArray2) {
        double d = dArray2[0] * (Math.PI / 180);
        double d2 = dArray2[1] * (Math.PI / 180);
        double d3 = dArray2[2] * (Math.PI / 180);
        double d4 = dArray[0];
        double d5 = dArray[1] * Math.cos(d) - dArray[2] * Math.sin(d);
        double d6 = dArray[1] * Math.sin(d) + dArray[2] * Math.cos(d);
        double d7 = d4 * Math.cos(d2) + d6 * Math.sin(d2);
        d6 = -d4 * Math.sin(d2) + d6 * Math.cos(d2);
        return new double[]{d7 * Math.cos(d3) - d5 * Math.sin(d3), d7 * Math.sin(d3) + d5 * Math.cos(d3), d6};
    }

    public static double[] compose(double[] dArray, double[] dArray2) {
        double[][] dArrayArray = new double[3][];
        for (int i = 0; i < 3; ++i) {
            double[] dArray3 = new double[3];
            dArray3[i] = 1.0;
            dArrayArray[i] = LemonMotionCurves.rotate(LemonMotionCurves.rotate(dArray3, dArray2), dArray);
        }
        return new double[]{Math.atan2(dArrayArray[1][2], dArrayArray[2][2]) / (Math.PI / 180), Math.asin(LemonMotionCurves.clamp(-dArrayArray[0][2], -1.0, 1.0)) / (Math.PI / 180), Math.atan2(dArrayArray[0][1], dArrayArray[0][0]) / (Math.PI / 180)};
    }

    public static double weight(double d, double d2) {
        if (d2 <= 0.0 || d < 0.0 || d >= d2) {
            return 0.0;
        }
        double d3 = LemonMotionCurves.clamp(Math.min(d, d2 - d) / Math.min(2.0, d2 * 0.18), 0.0, 1.0);
        return d3 * d3 * (3.0 - 2.0 * d3);
    }

    public static double[][] pose(String string, double d, double d2, int n, double d3, double d4, double d5, double d6) {
        double[][] dArray = new double[6][6];
        for (int i = 0; i < 6; ++i) {
            System.arraycopy(PIVOTS[i], 0, dArray[i], 0, 3);
        }
        Clip clip = LemonMotionCurves.clip(string);
        if (clip == null) {
            return dArray;
        }
        double d7 = LemonMotionCurves.clamp(d / d2, 0.0, 1.0);
        double d8 = LemonMotionCurves.weight(d, d2);
        double[] dArray2 = LemonMotionCurves.sample(clip, d7);
        double[] dArray3 = LemonMotionCurves.sample(clip, Math.max(0.0, d7 - 0.035));
        double[] dArray4 = new double[]{dArray2[3], dArray2[4] * (double)n, dArray2[5] * (double)n};
        double[] dArray5 = new double[]{dArray2[0] * (double)n, dArray2[1], dArray2[2]};
        dArray[0][3] = d6;
        dArray[0][4] = d5;
        for (int n2 : new int[]{2, 3, 4, 5}) {
            dArray[n2][3] = Math.cos(d3 * 0.6662 + (n2 == 2 || n2 == 5 ? Math.PI : 0.0)) * d4 * (n2 >= 4 ? 1.4 : 1.0) / (Math.PI / 180);
        }
        for (int i = 0; i < 3; ++i) {
            dArray[1][i] = dArray5[i] * d8;
            dArray[1][i + 3] = dArray4[i] * d8;
        }
        for (int n2 : new int[]{0, 2, 3}) {
            double[] dArray6;
            if (n2 == 0) {
                dArray6 = new double[]{d6 - dArray4[0] * 0.5, d5 - dArray3[4] * (double)n * 0.55, -dArray4[2] * 0.5};
            } else {
                int n3 = n2 == (n == 1 ? 2 : 3) ? 6 : 9;
                dArray6 = new double[]{dArray2[n3], dArray2[n3 + 1] * (double)n, dArray2[n3 + 2] * (double)n};
            }
            double[] dArray7 = LemonMotionCurves.compose(dArray4, dArray6);
            double[] dArray8 = LemonMotionCurves.rotate(PIVOTS[n2], dArray4);
            for (int i = 0; i < 3; ++i) {
                dArray[n2][i] = PIVOTS[n2][i] + (dArray8[i] + dArray5[i] - PIVOTS[n2][i]) * d8;
                double[] dArray9 = dArray[n2];
                int n4 = i + 3;
                dArray9[n4] = dArray9[n4] + (dArray7[i] - dArray[n2][i + 3]) * d8;
            }
        }
        return dArray;
    }

    public record Clip(String id, double[][] keys) {
    }
}

