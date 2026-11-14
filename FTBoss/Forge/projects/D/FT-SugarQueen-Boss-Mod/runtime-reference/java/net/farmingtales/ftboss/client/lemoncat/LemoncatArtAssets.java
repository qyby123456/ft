/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.client.lemoncat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LemoncatArtAssets {
    private static final Map<String, Sculpture> SCULPTURES = LemoncatArtAssets.read();

    private LemoncatArtAssets() {
    }

    public static Map<String, Sculpture> all() {
        return SCULPTURES;
    }

    public static Sculpture find(String string) {
        return SCULPTURES.get(string);
    }

    private static double[] vector(JsonArray jsonArray, int n) {
        if (jsonArray.size() != n) {
            throw new IllegalStateException("Invalid Lemoncat vector");
        }
        double[] dArray = new double[n];
        for (int i = 0; i < n; ++i) {
            dArray[i] = jsonArray.get(i).getAsDouble();
            if (Double.isFinite(dArray[i]) && !(Math.abs(dArray[i]) > 32.0)) continue;
            throw new IllegalStateException("Invalid Lemoncat coordinate");
        }
        return dArray;
    }

    private static double[][] matrix(JsonArray jsonArray, int n, int n2) {
        if (jsonArray.size() != n) {
            throw new IllegalStateException("Invalid Lemoncat surface");
        }
        double[][] dArrayArray = new double[n][];
        for (int i = 0; i < n; ++i) {
            dArrayArray[i] = LemoncatArtAssets.vector(jsonArray.get(i).getAsJsonArray(), n2);
        }
        return dArrayArray;
    }

    private static Map<String, Sculpture> read() {
        Map map;
        block19: {
            InputStream inputStream = LemoncatArtAssets.class.getResourceAsStream("/assets/ftboss/lemoncat_v3/sculptures.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing Lemoncat V3 sculptures");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                if (jsonObject.get("version").getAsInt() != 3) {
                    throw new IllegalStateException("Wrong Lemoncat asset revision");
                }
                LinkedHashMap<String, Sculpture> linkedHashMap = new LinkedHashMap<String, Sculpture>();
                for (JsonElement jsonElement : jsonObject.getAsJsonArray("models")) {
                    Object object;
                    JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                    String string = jsonObject2.get("id").getAsString();
                    if (!List.of("searing_pan", "hearth_feast", "citrus_nova").contains(string) || linkedHashMap.containsKey(string)) {
                        throw new IllegalStateException("Invalid Lemoncat model ID");
                    }
                    ArrayList<Surface> arrayList = new ArrayList<Surface>();
                    for (JsonElement jsonElement2 : jsonObject2.getAsJsonArray("faces")) {
                        double[][] dArray;
                        object = jsonElement2.getAsJsonObject();
                        int n = object.get("bone").getAsInt();
                        float f = object.get("opacity").getAsFloat();
                        if (!((n == 0 || n >= 201 && n <= 207 || n >= 211 && n <= 219 || n >= 231 && n <= 242) && f > 0.0f && f <= 1.0f)) {
                            throw new IllegalStateException("Invalid Lemoncat bone or opacity");
                        }
                        double[][] dArray2 = dArray = LemoncatArtAssets.matrix(object.getAsJsonArray("uv"), 4, 2);
                        int n2 = dArray2.length;
                        for (int i = 0; i < n2; ++i) {
                            double[] dArray3;
                            for (double d : dArray3 = dArray2[i]) {
                                if (!(d < 0.0) && !(d > 1.0)) continue;
                                throw new IllegalStateException("Invalid Lemoncat UV");
                            }
                        }
                        arrayList.add(new Surface(LemoncatArtAssets.matrix(object.getAsJsonArray("points"), 4, 3), dArray, n, LemoncatArtAssets.vector(object.getAsJsonArray("pivot"), 3), object.get("material").getAsString(), f));
                    }
                    double d = jsonObject2.get("scale").getAsDouble();
                    if (!(d > 0.0) || !(d < 4.0) || arrayList.isEmpty() || arrayList.size() > 2048) {
                        throw new IllegalStateException("Invalid Lemoncat scale or budget");
                    }
                    object = jsonObject2.get("anchor").getAsString();
                    if (!List.of("FRONT", "TARGET").contains(object)) {
                        throw new IllegalStateException("Invalid Lemoncat anchor");
                    }
                    linkedHashMap.put(string, new Sculpture(string, (String)object, d, jsonObject2.get("height").getAsDouble(), jsonObject2.get("ceiling").getAsDouble(), jsonObject2.get("front").getAsDouble(), jsonObject2.get("lateral").getAsDouble(), jsonObject2.get("life").getAsInt(), LemoncatArtAssets.vector(jsonObject2.getAsJsonArray("minimum"), 3), LemoncatArtAssets.vector(jsonObject2.getAsJsonArray("maximum"), 3), LemoncatArtAssets.vector(jsonObject2.getAsJsonArray("envelopeMin"), 3), LemoncatArtAssets.vector(jsonObject2.getAsJsonArray("envelopeMax"), 3), List.copyOf(arrayList)));
                }
                if (linkedHashMap.size() != 3) {
                    throw new IllegalStateException("Incomplete Lemoncat sculpture set");
                }
                map = Map.copyOf(linkedHashMap);
                if (inputStream == null) break block19;
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
                    throw new IllegalStateException("Cannot read independently authored Lemoncat V3 assets", exception);
                }
            }
            inputStream.close();
        }
        return map;
    }

    public record Sculpture(String id, String anchor, double scale, double height, double ceiling, double front, double lateral, int life, double[] minimum, double[] maximum, double[] envelopeMin, double[] envelopeMax, List<Surface> faces) {
    }

    public record Surface(double[][] points, double[][] uv, int bone, double[] pivot, String material, float opacity) {
    }
}

