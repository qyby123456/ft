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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MaqueModels {
    private static final Map<String, Model> MODELS = MaqueModels.load();

    private MaqueModels() {
    }

    public static Model get(String string) {
        return MODELS.get(string);
    }

    public static Map<String, Model> all() {
        return MODELS;
    }

    private static double[] numbers(JsonArray jsonArray, int n) {
        if (jsonArray.size() != n) {
            throw new IllegalStateException("Invalid maque coordinate count");
        }
        double[] dArray = new double[n];
        for (int i = 0; i < n; ++i) {
            dArray[i] = jsonArray.get(i).getAsDouble();
            if (Double.isFinite(dArray[i]) && !(Math.abs(dArray[i]) > 16.0)) continue;
            throw new IllegalStateException("Invalid maque coordinate");
        }
        return dArray;
    }

    private static Map<String, Model> load() {
        Map map;
        block17: {
            InputStream inputStream = MaqueModels.class.getResourceAsStream("/assets/ftboss/maque/models.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing independently authored maque models");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                LinkedHashMap<String, Model> linkedHashMap = new LinkedHashMap<String, Model>();
                for (Map.Entry entry : jsonObject.entrySet()) {
                    JsonObject jsonObject2 = ((JsonElement)entry.getValue()).getAsJsonObject();
                    ArrayList<Face> arrayList = new ArrayList<Face>();
                    ArrayList arrayList2 = new ArrayList();
                    for (String string : List.of("faces", "liteFaces")) {
                        for (JsonElement jsonElement : jsonObject2.getAsJsonArray(string)) {
                            int n;
                            JsonObject jsonObject3 = jsonElement.getAsJsonObject();
                            double[][] dArrayArray = new double[4][];
                            double[][] dArrayArray2 = new double[4][];
                            for (n = 0; n < 4; ++n) {
                                dArrayArray[n] = MaqueModels.numbers(jsonObject3.getAsJsonArray("points").get(n).getAsJsonArray(), 3);
                                dArrayArray2[n] = MaqueModels.numbers(jsonObject3.getAsJsonArray("uv").get(n).getAsJsonArray(), 2);
                                if (!(dArrayArray2[n][0] < 0.0 || dArrayArray2[n][0] > 1.0 || dArrayArray2[n][1] < 0.0) && !(dArrayArray2[n][1] > 1.0)) continue;
                                throw new IllegalStateException("Invalid maque UV");
                            }
                            n = jsonObject3.get("material").getAsInt();
                            int n2 = jsonObject3.get("bone").getAsInt();
                            float f = jsonObject3.get("opacity").getAsFloat();
                            if (n < 0 || n >= 17 || n2 < 0 || n2 > 7 || !Float.isFinite(f) || f <= 0.0f || f > 1.0f) {
                                throw new IllegalStateException("Invalid maque face");
                            }
                            (string.equals("faces") ? arrayList : arrayList2).add(new Face(dArrayArray, dArrayArray2, n, f, n2, MaqueModels.numbers(jsonObject3.getAsJsonArray("pivot"), 3), jsonObject3.get("group").getAsString().equals("detail")));
                        }
                    }
                    double d = jsonObject2.get("scale").getAsDouble();
                    if (!Double.isFinite(d) || d < 0.5 || d > 2.0 || arrayList.size() > 6000) {
                        throw new IllegalStateException("Invalid maque model");
                    }
                    if (arrayList2.isEmpty() || arrayList2.size() >= arrayList.size()) {
                        throw new IllegalStateException("Invalid independently generated LOD");
                    }
                    linkedHashMap.put((String)entry.getKey(), new Model((String)entry.getKey(), d, MaqueModels.numbers(jsonObject2.getAsJsonArray("offset"), 3), List.copyOf(arrayList), List.copyOf(arrayList2)));
                }
                if (linkedHashMap.size() != 3) {
                    throw new IllegalStateException("Expected three maque spells");
                }
                map = Map.copyOf(linkedHashMap);
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
                    throw new IllegalStateException("Cannot load maque models", exception);
                }
            }
            inputStream.close();
        }
        return map;
    }

    public record Model(String id, double scale, double[] offset, List<Face> faces, List<Face> liteFaces) {
    }

    public record Face(double[][] points, double[][] uv, int material, float opacity, int bone, double[] pivot, boolean detail) {
    }
}

