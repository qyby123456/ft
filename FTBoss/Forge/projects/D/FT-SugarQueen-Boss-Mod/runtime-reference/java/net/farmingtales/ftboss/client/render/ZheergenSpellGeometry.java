/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ZheergenSpellGeometry {
    public static final Set<String> IDS = Set.of("shadow_brand", "void_echo", "nightfall_blades");
    private static final Map<String, Model> MODELS = ZheergenSpellGeometry.load();

    private ZheergenSpellGeometry() {
    }

    public static Map<String, Model> all() {
        return MODELS;
    }

    public static Model find(String string) {
        return MODELS.get(string);
    }

    private static double[] vector(JsonArray jsonArray, int n, double d) {
        if (jsonArray == null || jsonArray.size() != n) {
            throw new IllegalArgumentException("Invalid independent model vector");
        }
        double[] dArray = new double[n];
        for (int i = 0; i < n; ++i) {
            dArray[i] = jsonArray.get(i).getAsDouble();
            if (Double.isFinite(dArray[i]) && !(Math.abs(dArray[i]) > d)) continue;
            throw new IllegalArgumentException("Invalid independent model coordinate");
        }
        return dArray;
    }

    private static Map<String, Model> load() {
        Map map;
        block20: {
            InputStream inputStream = ZheergenSpellGeometry.class.getResourceAsStream("/assets/ftboss/fx/zheergen_models.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing independently built Zheergen models");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                if (jsonObject.get("schema").getAsInt() != 1) {
                    throw new IllegalArgumentException("Unsupported independent model schema");
                }
                LinkedHashMap<String, Model> linkedHashMap = new LinkedHashMap<String, Model>();
                for (Map.Entry entry : jsonObject.getAsJsonObject("models").entrySet()) {
                    Object object;
                    if (!IDS.contains(entry.getKey())) {
                        throw new IllegalArgumentException("Unexpected model outside Zheergen scope");
                    }
                    JsonObject jsonObject2 = ((JsonElement)entry.getValue()).getAsJsonObject();
                    ArrayList<Face> arrayList = new ArrayList<Face>();
                    int n = 0;
                    for (JsonElement jsonElement : jsonObject2.getAsJsonArray("faces")) {
                        int n2;
                        JsonObject jsonObject3 = jsonElement.getAsJsonObject();
                        JsonArray jsonArray = jsonObject3.getAsJsonArray("points");
                        JsonArray jsonArray2 = jsonObject3.getAsJsonArray("uv");
                        if (jsonArray.size() != 4 || jsonArray2.size() != 4) {
                            throw new IllegalArgumentException("Invalid independent model topology");
                        }
                        double[][] dArrayArray = new double[4][];
                        object = new double[4][];
                        for (n2 = 0; n2 < 4; ++n2) {
                            dArrayArray[n2] = ZheergenSpellGeometry.vector(jsonArray.get(n2).getAsJsonArray(), 3, 16.0);
                            object[n2] = ZheergenSpellGeometry.vector(jsonArray2.get(n2).getAsJsonArray(), 2, 1.0);
                            for (double d : object[n2]) {
                                if (!(d < 0.0)) continue;
                                throw new IllegalArgumentException("Negative independent UV");
                            }
                        }
                        n2 = jsonObject3.get("color").getAsInt();
                        int n3 = jsonObject3.get("bone").getAsInt();
                        float f = jsonObject3.get("opacity").getAsFloat();
                        if (n2 < 0 || n2 > 0xFFFFFF || !Float.isFinite(f) || f <= 0.0f || f > 1.0f || n3 != 0 && (n3 < 20 || n3 > 44)) {
                            throw new IllegalArgumentException("Invalid independent surface data");
                        }
                        int n4 = jsonObject3.get("group").getAsString().equals("detail") ? 1 : 0;
                        if (!n4) {
                            ++n;
                        }
                        arrayList.add(new Face(dArrayArray, (double[][])object, n2, f, n3, ZheergenSpellGeometry.vector(jsonObject3.getAsJsonArray("pivot"), 3, 16.0), n4 != 0));
                    }
                    double d = jsonObject2.get("span").getAsDouble();
                    double d2 = jsonObject2.get("height").getAsDouble();
                    double d3 = jsonObject2.get("scale").getAsDouble();
                    if (!Double.isFinite(d) || d < 2.0 || d > 7.0 || !Double.isFinite(d2) || d2 < 0.0 || d2 > 4.0 || !Double.isFinite(d3) || d3 <= 0.0 || d3 > 4.0 || arrayList.size() > 4000 || n == 0) {
                        throw new IllegalArgumentException("Invalid independent model budget");
                    }
                    object = jsonObject2.getAsJsonObject("animatedBounds");
                    linkedHashMap.put((String)entry.getKey(), new Model((String)entry.getKey(), d, d2, d3, ZheergenSpellGeometry.vector(object.getAsJsonArray("minimum"), 3, 16.0), ZheergenSpellGeometry.vector(object.getAsJsonArray("maximum"), 3, 16.0), List.copyOf(arrayList), n));
                }
                if (!linkedHashMap.keySet().equals(IDS)) {
                    throw new IllegalArgumentException("Incomplete independent Zheergen models");
                }
                map = Collections.unmodifiableMap(linkedHashMap);
                if (inputStream == null) break block20;
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
                    throw new IllegalStateException("Cannot load independent Zheergen geometry", exception);
                }
            }
            inputStream.close();
        }
        return map;
    }

    public record Model(String id, double span, double height, double scale, double[] minimum, double[] maximum, List<Face> faces, int bodyFaces) {
    }

    public record Face(double[][] points, double[][] uv, int color, float opacity, int bone, double[] pivot, boolean detail) {
    }
}

