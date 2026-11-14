/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.dandanmotion;

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

public final class MotionCurves {
    public static final double[][] PIVOTS = new double[][]{{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {-5.0, 2.5, 0.0}, {5.0, 2.5, 0.0}, {-1.9, 12.0, 0.0}, {1.9, 12.0, 0.0}};
    private static final Map<String, Clip> CLIPS = MotionCurves.load();

    private MotionCurves() {
    }

    private static Map<String, Clip> load() {
        LinkedHashMap<String, Clip> linkedHashMap = new LinkedHashMap<String, Clip>();
        try (InputStream inputStream = MotionCurves.class.getResourceAsStream("/assets/ftboss/dandan_motion/motion.json");){
            if (inputStream == null) {
                throw new IllegalStateException("Missing Dandan character curves");
            }
            JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
            if (jsonObject.get("schema").getAsInt() != 1) {
                throw new IllegalStateException("Unsupported Dandan motion schema");
            }
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("clips")) {
                JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                JsonArray jsonArray = jsonObject2.getAsJsonArray("keys");
                Key[] keyArray = new Key[jsonArray.size()];
                for (int i = 0; i < keyArray.length; ++i) {
                    JsonArray jsonArray2 = jsonArray.get(i).getAsJsonArray();
                    double[][] dArray = new double[6][6];
                    for (int j = 0; j < 6; ++j) {
                        for (int k = 0; k < 6; ++k) {
                            dArray[j][k] = jsonArray2.get(1).getAsJsonArray().get(j).getAsJsonArray().get(k).getAsDouble();
                        }
                    }
                    keyArray[i] = new Key(jsonArray2.get(0).getAsDouble(), dArray);
                }
                String string = jsonObject2.get("id").getAsString();
                linkedHashMap.put(string, new Clip(string, keyArray));
            }
        }
        catch (Exception exception) {
            throw new IllegalStateException("Cannot read Dandan motion curves", exception);
        }
        return Map.copyOf(linkedHashMap);
    }

    public static boolean has(String string) {
        return CLIPS.containsKey(string);
    }

    public static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    public static double smooth(double d) {
        double d2 = MotionCurves.clamp(d);
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double[][] sample(String string, double d) {
        Clip clip = CLIPS.get(string);
        if (clip == null) {
            throw new IllegalArgumentException("Unknown Dandan motion: " + string);
        }
        double d2 = MotionCurves.clamp(d);
        Key key = clip.keys()[0];
        Key key2 = clip.keys()[clip.keys().length - 1];
        for (int i = 1; i < clip.keys().length; ++i) {
            if (!(d2 <= clip.keys()[i].time())) continue;
            key = clip.keys()[i - 1];
            key2 = clip.keys()[i];
            break;
        }
        double d3 = MotionCurves.smooth((d2 - key.time()) / (key2.time() - key.time()));
        double[][] dArray = new double[6][6];
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 6; ++j) {
                dArray[i][j] = key.pose()[i][j] * (1.0 - d3) + key2.pose()[i][j] * d3;
            }
        }
        return dArray;
    }

    public static double[] rotate(double[] dArray, double[] dArray2) {
        double d = Math.toRadians(dArray2[0]);
        double d2 = Math.toRadians(dArray2[1]);
        double d3 = Math.toRadians(dArray2[2]);
        double d4 = dArray[1] * Math.cos(d) - dArray[2] * Math.sin(d);
        double d5 = dArray[1] * Math.sin(d) + dArray[2] * Math.cos(d);
        double d6 = dArray[0] * Math.cos(d2) + d5 * Math.sin(d2);
        double d7 = -dArray[0] * Math.sin(d2) + d5 * Math.cos(d2);
        return new double[]{d6 * Math.cos(d3) - d4 * Math.sin(d3), d6 * Math.sin(d3) + d4 * Math.cos(d3), d7};
    }

    public static double[][] resolve(double[][] dArray) {
        double[][] dArray2 = new double[6][6];
        double[] dArray3 = MotionCurves.rotate(new double[]{0.0, 12.0, 0.0}, new double[]{dArray[1][3], dArray[1][4], dArray[1][5]});
        double[] dArray4 = new double[]{dArray[1][0] - dArray3[0], dArray[1][1] + 12.0 - dArray3[1], dArray[1][2] - dArray3[2]};
        for (int i = 0; i < 6; ++i) {
            boolean bl = i == 2 || i == 3;
            double[] dArray5 = bl ? MotionCurves.rotate(PIVOTS[i], new double[]{dArray[1][3], dArray[1][4], dArray[1][5]}) : PIVOTS[i];
            for (int j = 0; j < 3; ++j) {
                dArray2[i][j] = dArray5[j] + (i == 1 ? dArray4[j] : dArray[i][j] + (i < 4 ? dArray4[j] : 0.0));
                dArray2[i][j + 3] = dArray[i][j + 3] + (bl ? dArray[1][j + 3] : 0.0);
            }
        }
        return dArray2;
    }

    public record Key(double time, double[][] pose) {
    }

    public record Clip(String id, Key[] keys) {
    }
}

