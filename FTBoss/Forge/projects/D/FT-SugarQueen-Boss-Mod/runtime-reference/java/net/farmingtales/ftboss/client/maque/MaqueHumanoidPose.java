/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.client.maque;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MaqueHumanoidPose {
    public static final String[] PARTS = new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    private static final Map<String, Clip> CLIPS = MaqueHumanoidPose.load();

    private MaqueHumanoidPose() {
    }

    private static Map<String, Clip> load() {
        Map map;
        InputStream inputStream = MaqueHumanoidPose.class.getResourceAsStream("/assets/ftboss/maque_arcane/humanoid_profile.json");
        if (inputStream == null) {
            throw new IllegalStateException("Missing Maque humanoid profile");
        }
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        try {
            JsonObject jsonObject = JsonParser.parseReader((Reader)inputStreamReader).getAsJsonObject();
            if (jsonObject.get("schema").getAsInt() != 1) {
                throw new IllegalStateException("Unsupported Maque motion schema");
            }
            LinkedHashMap<JsonObject, Clip> linkedHashMap = new LinkedHashMap<JsonObject, Clip>();
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("clips")) {
                int n;
                Object object;
                int n2;
                JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                JsonArray jsonArray = jsonObject2.getAsJsonArray("keys");
                double[] dArray = new double[jsonArray.size()];
                double[][][] dArray2 = new double[jsonArray.size()][8][3];
                for (n2 = 0; n2 < jsonArray.size(); ++n2) {
                    object = jsonArray.get(n2).getAsJsonObject();
                    dArray[n2] = object.get("time").getAsDouble();
                    if (!Double.isFinite(dArray[n2]) || n2 > 0 && dArray[n2] <= dArray[n2 - 1]) {
                        throw new IllegalStateException("Invalid Maque key time");
                    }
                    for (int i = 0; i < 7; ++i) {
                        for (n = 0; n < 3; ++n) {
                            double d;
                            block22: {
                                block21: {
                                    d = object.getAsJsonArray(i < 6 ? PARTS[i] : "shift").get(n).getAsDouble();
                                    if (!Double.isFinite(d)) break block21;
                                    double d2 = Math.abs(d);
                                    int n3 = i < 6 ? 140 : 4;
                                    if (!(d2 > (double)n3)) break block22;
                                }
                                throw new IllegalStateException("Unsafe Maque pose channel");
                            }
                            dArray2[n2][i][n] = d;
                        }
                    }
                    dArray2[n2][7][0] = object.get("lift").getAsDouble();
                    if (Double.isFinite(dArray2[n2][7][0]) && !(dArray2[n2][7][0] < 0.0) && !(dArray2[n2][7][0] > 4.0)) continue;
                    throw new IllegalStateException("Unsafe visual lift");
                }
                n2 = jsonObject2.get("ticks").getAsInt();
                object = jsonObject2.get("id").getAsString();
                double[][][] dArray3 = new double[jsonArray.size()][8][3];
                for (n = 1; n < jsonArray.size() - 1; ++n) {
                    for (int i = 0; i < 8; ++i) {
                        for (int j = 0; j < 3; ++j) {
                            double d = (dArray2[n][i][j] - dArray2[n - 1][i][j]) / (dArray[n] - dArray[n - 1]);
                            double d3 = (dArray2[n + 1][i][j] - dArray2[n][i][j]) / (dArray[n + 1] - dArray[n]);
                            dArray3[n][i][j] = d * d3 <= 0.0 ? 0.0 : 2.0 * d * d3 / (d + d3);
                        }
                    }
                }
                if (n2 >= 8 && n2 <= 200 && dArray[0] == 0.0 && dArray[dArray.length - 1] == 1.0 && linkedHashMap.put((JsonObject)object, new Clip((String)object, n2, dArray, dArray2, dArray3)) == null) continue;
                throw new IllegalStateException("Invalid Maque clip");
            }
            if (linkedHashMap.size() != 7) {
                throw new IllegalStateException("Incomplete Maque motion profile");
            }
            map = Map.copyOf(linkedHashMap);
        }
        catch (Throwable throwable) {
            try {
                try {
                    inputStreamReader.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Exception exception) {
                throw new IllegalStateException("Cannot load Maque humanoid motions", exception);
            }
        }
        inputStreamReader.close();
        return map;
    }

    public static Clip clip(String string) {
        return CLIPS.get(string);
    }

    public static double clamp(double d, double d2, double d3) {
        return Math.max(d2, Math.min(d3, d));
    }

    public static double smooth(double d) {
        double d2 = MaqueHumanoidPose.clamp(d, 0.0, 1.0);
        return d2 * d2 * d2 * (10.0 + d2 * (-15.0 + 6.0 * d2));
    }

    private static double sample(Clip clip, double d, int n, int n2) {
        int n3;
        double d2 = MaqueHumanoidPose.clamp(d, 0.0, 1.0);
        for (n3 = 0; n3 < clip.times.length - 2 && d2 > clip.times[n3 + 1]; ++n3) {
        }
        double d3 = clip.times[n3 + 1] - clip.times[n3];
        double d4 = MaqueHumanoidPose.clamp((d2 - clip.times[n3]) / d3, 0.0, 1.0);
        double d5 = d4 * d4;
        double d6 = d5 * d4;
        return (2.0 * d6 - 3.0 * d5 + 1.0) * clip.values[n3][n][n2] + (d6 - 2.0 * d5 + d4) * d3 * clip.slopes[n3][n][n2] + (-2.0 * d6 + 3.0 * d5) * clip.values[n3 + 1][n][n2] + (d6 - d5) * d3 * clip.slopes[n3 + 1][n][n2];
    }

    public static double[] rotation(double[] dArray) {
        double d = Math.cos(dArray[0]);
        double d2 = Math.sin(dArray[0]);
        double d3 = Math.cos(dArray[1]);
        double d4 = Math.sin(dArray[1]);
        double d5 = Math.cos(dArray[2]);
        double d6 = Math.sin(dArray[2]);
        return new double[]{d5 * d3, d5 * d4 * d2 - d6 * d, d5 * d4 * d + d6 * d2, d6 * d3, d6 * d4 * d2 + d5 * d, d6 * d4 * d - d5 * d2, -d4, d3 * d2, d3 * d};
    }

    public static double[] transform(double[] dArray, double[] dArray2) {
        double[] dArray3 = new double[3];
        for (int i = 0; i < 3; ++i) {
            dArray3[i] = dArray[i * 3] * dArray2[0] + dArray[i * 3 + 1] * dArray2[1] + dArray[i * 3 + 2] * dArray2[2];
        }
        return dArray3;
    }

    private static double[] multiply(double[] dArray, double[] dArray2) {
        double[] dArray3 = new double[9];
        for (int i = 0; i < 9; ++i) {
            int n = i / 3;
            int n2 = i % 3;
            dArray3[i] = dArray[n * 3] * dArray2[n2] + dArray[n * 3 + 1] * dArray2[n2 + 3] + dArray[n * 3 + 2] * dArray2[n2 + 6];
        }
        return dArray3;
    }

    private static double[] euler(double[] dArray) {
        return new double[]{Math.atan2(dArray[7], dArray[8]), Math.asin(MaqueHumanoidPose.clamp(-dArray[6], -1.0, 1.0)), Math.atan2(dArray[3], dArray[0])};
    }

    private static double[] part(double[] dArray, double[] dArray2) {
        return new double[]{dArray[0], dArray[1], dArray[2], dArray2[0], dArray2[1], dArray2[2]};
    }

    public static double[][] evaluate(String string, double d, boolean bl) {
        double[] dArray;
        double[] dArray2;
        int n;
        Clip clip = MaqueHumanoidPose.clip(string);
        if (clip == null || !Double.isFinite(d)) {
            throw new IllegalArgumentException("Invalid Maque motion request");
        }
        double[][] dArray3 = new double[6][3];
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 3; ++j) {
                dArray3[i][j] = Math.toRadians(MaqueHumanoidPose.sample(clip, d, i, j));
            }
        }
        double d2 = MaqueHumanoidPose.sample(clip, d, 7, 0);
        double[] dArray4 = new double[]{MaqueHumanoidPose.sample(clip, d, 6, 0), MaqueHumanoidPose.sample(clip, d, 6, 1) - d2, MaqueHumanoidPose.sample(clip, d, 6, 2)};
        if (bl) {
            for (int n2 : new int[]{2, 4}) {
                double[] dArray5 = dArray3[n2];
                dArray3[n2] = dArray3[n2 + 1];
                dArray3[n2 + 1] = dArray5;
            }
            double[][] dArray6 = dArray3;
            int n3 = dArray6.length;
            for (n = 0; n < n3; ++n) {
                double[] dArray7 = dArray6[n];
                dArray7[1] = dArray7[1] * -1.0;
                dArray7[2] = dArray7[2] * -1.0;
            }
            dArray4[0] = dArray4[0] * -1.0;
        }
        double[] dArray8 = MaqueHumanoidPose.rotation(dArray3[1]);
        double[] dArray9 = MaqueHumanoidPose.transform(dArray8, new double[]{0.0, -12.0, 0.0});
        for (n = 0; n < 3; ++n) {
            int n4 = n;
            dArray9[n4] = dArray9[n4] + (dArray4[n] + (double)(n == 1 ? 12 : 0));
        }
        double[][] dArrayArray = new double[6][];
        dArrayArray[1] = MaqueHumanoidPose.part(dArray9, dArray3[1]);
        for (int n5 : new int[]{0, 2, 3}) {
            double[] dArray10;
            if (n5 == 0) {
                dArray10 = new double[3];
            } else {
                double[] dArray11 = new double[3];
                dArray11[0] = n5 == 2 ? -5.0 : 5.0;
                dArray11[1] = 2.5;
                dArray10 = dArray11;
                dArray11[2] = 0.0;
            }
            dArray2 = dArray10;
            dArray = MaqueHumanoidPose.transform(dArray8, dArray2);
            for (int d7 = 0; d7 < 3; ++d7) {
                int n6 = d7;
                dArray[n6] = dArray[n6] + dArray9[d7];
            }
            dArrayArray[n5] = MaqueHumanoidPose.part(dArray, MaqueHumanoidPose.euler(MaqueHumanoidPose.multiply(dArray8, MaqueHumanoidPose.rotation(dArray3[n5]))));
        }
        for (int n5 : new int[]{4, 5}) {
            dArray2 = MaqueHumanoidPose.rotation(dArray3[n5]);
            dArray = MaqueHumanoidPose.transform(dArray2, new double[]{0.0, 12.0, 0.0});
            double d3 = Double.NEGATIVE_INFINITY;
            for (double d4 : new double[]{-2.0, 2.0}) {
                for (double d5 : new double[]{-2.0, 2.0}) {
                    d3 = Math.max(d3, MaqueHumanoidPose.transform(dArray2, new double[]{d4, 12.0, d5})[1]);
                }
            }
            dArrayArray[n5] = MaqueHumanoidPose.part(new double[]{(n5 == 4 ? -1.9 : 1.9) - dArray[0], 24.0 - d3 - d2, -dArray[2]}, dArray3[n5]);
        }
        return dArrayArray;
    }

    public record Clip(String id, int ticks, double[] times, double[][][] values, double[][][] slopes) {
    }
}

