/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.dandanironmotion;

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
import net.farmingtales.ftboss.dandanmotion.MotionCurves;

public final class IronPoses {
    private static final Map<String, Key[]> CLIPS = IronPoses.load();

    private IronPoses() {
    }

    private static Map<String, Key[]> load() {
        LinkedHashMap<String, Key[]> linkedHashMap = new LinkedHashMap<String, Key[]>();
        try (InputStream inputStream = IronPoses.class.getResourceAsStream("/assets/ftboss/dandan_iron_motion/motion.json");){
            if (inputStream == null) {
                throw new IllegalStateException("Missing final Iron-specific character curves");
            }
            JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
            if (jsonObject.get("schema").getAsInt() != 1) {
                throw new IllegalStateException("Unsupported Iron pose schema");
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
                            if (Double.isFinite(dArray[j][k])) continue;
                            throw new IllegalStateException("Nonfinite character pose");
                        }
                    }
                    keyArray[i] = new Key(jsonArray2.get(0).getAsDouble(), dArray);
                    if (i <= 0 || !(keyArray[i].time() <= keyArray[i - 1].time())) continue;
                    throw new IllegalStateException("Unordered character keys");
                }
                linkedHashMap.put(jsonObject2.get("id").getAsString(), keyArray);
            }
        }
        catch (Exception exception) {
            throw new IllegalStateException("Cannot load Iron character curves", exception);
        }
        if (linkedHashMap.size() != 6) {
            throw new IllegalStateException("Six final Iron character clips required");
        }
        return linkedHashMap;
    }

    public static double[][] sample(String string, double d) {
        Key[] keyArray = CLIPS.get(string);
        if (keyArray == null) {
            throw new IllegalArgumentException("Unknown Iron pose: " + string);
        }
        d = MotionCurves.clamp(d);
        Key key = keyArray[0];
        Key key2 = keyArray[keyArray.length - 1];
        for (int i = 1; i < keyArray.length; ++i) {
            if (!(d <= keyArray[i].time())) continue;
            key = keyArray[i - 1];
            key2 = keyArray[i];
            break;
        }
        double d2 = MotionCurves.clamp((d - key.time()) / (key2.time() - key.time()));
        d2 = d2 * d2 * (3.0 - 2.0 * d2);
        double[][] dArray = new double[6][6];
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 6; ++j) {
                dArray[i][j] = key.pose()[i][j] * (1.0 - d2) + key2.pose()[i][j] * d2;
            }
        }
        return dArray;
    }

    public static double[][] recover(String string, double d, double d2) {
        double[][] dArray = IronPoses.sample(string, d);
        double[][] dArray2 = IronPoses.sample("candle_reap_charge", 0.0);
        d2 = MotionCurves.clamp(d2);
        d2 = d2 * d2 * (3.0 - 2.0 * d2);
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 6; ++j) {
                dArray[i][j] = dArray[i][j] * (1.0 - d2) + dArray2[i][j] * d2;
            }
        }
        return dArray;
    }

    private record Key(double time, double[][] pose) {
    }
}

