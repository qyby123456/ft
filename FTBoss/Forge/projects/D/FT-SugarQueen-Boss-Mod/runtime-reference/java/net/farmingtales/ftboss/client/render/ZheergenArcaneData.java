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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ZheergenArcaneData {
    public static final Set<String> SPELLS = Set.of("shadow_brand", "void_echo", "nightfall_blades");
    private static final Map<String, Shape> SHAPES = ZheergenArcaneData.read();

    private ZheergenArcaneData() {
    }

    public static Shape shape(String string) {
        return SHAPES.get(string);
    }

    private static double scalar(JsonObject jsonObject, String string, double d, double d2) {
        double d3 = jsonObject.get(string).getAsDouble();
        if (!Double.isFinite(d3) || d3 < d || d3 > d2) {
            throw new IllegalArgumentException("Invalid arcane field: " + string);
        }
        return d3;
    }

    private static double[] vector(JsonArray jsonArray, int n, double d, double d2) {
        if (jsonArray.size() != n) {
            throw new IllegalArgumentException("Invalid arcane vector size");
        }
        double[] dArray = new double[n];
        for (int i = 0; i < n; ++i) {
            dArray[i] = jsonArray.get(i).getAsDouble();
            if (Double.isFinite(dArray[i]) && !(dArray[i] < d) && !(dArray[i] > d2)) continue;
            throw new IllegalArgumentException("Invalid arcane coordinate");
        }
        return dArray;
    }

    private static Map<String, Shape> read() {
        Map map;
        block20: {
            InputStream inputStream = ZheergenArcaneData.class.getResourceAsStream("/assets/ftboss/fx/zheergen_arcane_models.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing arcane rebuild payload");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                if (jsonObject.get("schema").getAsInt() != 2) {
                    throw new IllegalArgumentException("Arcane schema mismatch");
                }
                LinkedHashMap<String, Shape> linkedHashMap = new LinkedHashMap<String, Shape>();
                for (Map.Entry entry : jsonObject.getAsJsonObject("models").entrySet()) {
                    Object object;
                    Object object22;
                    if (!SPELLS.contains(entry.getKey())) {
                        throw new IllegalArgumentException("Unexpected spell in arcane payload");
                    }
                    JsonObject jsonObject2 = ((JsonElement)entry.getValue()).getAsJsonObject();
                    ArrayList<Surface> arrayList = new ArrayList<Surface>();
                    int n = 0;
                    for (Object object22 : jsonObject2.getAsJsonArray("faces")) {
                        double d;
                        int n2;
                        object = object22.getAsJsonObject();
                        double[][] dArrayArray = new double[4][];
                        double[][] dArrayArray2 = new double[4][];
                        if (object.getAsJsonArray("points").size() != 4 || object.getAsJsonArray("uv").size() != 4) {
                            throw new IllegalArgumentException("Arcane face must be a quad");
                        }
                        for (n2 = 0; n2 < 4; ++n2) {
                            dArrayArray[n2] = ZheergenArcaneData.vector(object.getAsJsonArray("points").get(n2).getAsJsonArray(), 3, -16.0, 16.0);
                            dArrayArray2[n2] = ZheergenArcaneData.vector(object.getAsJsonArray("uv").get(n2).getAsJsonArray(), 2, 0.0, 1.0);
                        }
                        n2 = (int)ZheergenArcaneData.scalar(object, "color", 0.0, 1.6777215E7);
                        int n3 = (int)ZheergenArcaneData.scalar(object, "bone", 20.0, 44.0);
                        float f = (float)ZheergenArcaneData.scalar(object, "opacity", 0.001, 1.0);
                        boolean bl = object.get("group").getAsString().equals("detail");
                        if (!bl) {
                            ++n;
                        }
                        double d2 = d = object.has("alphaLag") ? ZheergenArcaneData.scalar(object, "alphaLag", 0.0, 6.0) : 0.0;
                        if (d > 0.0 && (Math.abs(d - 1.8) > 1.0E-8 || n3 < 33 || n3 > 37)) {
                            throw new IllegalArgumentException("Unsupported delayed material channel");
                        }
                        arrayList.add(new Surface(dArrayArray, dArrayArray2, n2, f, n3, ZheergenArcaneData.vector(object.getAsJsonArray("pivot"), 3, -16.0, 16.0), bl, d));
                    }
                    if (arrayList.isEmpty() || arrayList.size() > 4000 || n == 0) {
                        throw new IllegalArgumentException("Invalid arcane surface budget");
                    }
                    Iterator iterator = jsonObject2.getAsJsonObject("animatedBounds");
                    object22 = ZheergenArcaneData.vector(iterator.getAsJsonArray("minimum"), 3, -16.0, 16.0);
                    object = ZheergenArcaneData.vector(iterator.getAsJsonArray("maximum"), 3, -16.0, 16.0);
                    for (int i = 0; i < 3; ++i) {
                        if (!(object22[i] >= object[i])) continue;
                        throw new IllegalArgumentException("Invalid arcane hull");
                    }
                    linkedHashMap.put((String)entry.getKey(), new Shape(ZheergenArcaneData.scalar(jsonObject2, "span", 2.0, 24.0), ZheergenArcaneData.scalar(jsonObject2, "height", 0.0, 4.0), ZheergenArcaneData.scalar(jsonObject2, "scale", 0.01, 4.0), (double[])object22, (double[])object, List.copyOf(arrayList), n));
                }
                if (!linkedHashMap.keySet().equals(SPELLS)) {
                    throw new IllegalArgumentException("Incomplete arcane rebuild");
                }
                map = Map.copyOf(linkedHashMap);
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
                    throw new IllegalStateException("Cannot load independently rebuilt arcane assets", exception);
                }
            }
            inputStream.close();
        }
        return map;
    }

    public record Shape(double size, double height, double scale, double[] lower, double[] upper, List<Surface> surfaces, int essential) {
    }

    public record Surface(double[][] positions, double[][] uv, int tint, float opacity, int channel, double[] pivot, boolean detail, double alphaLag) {
    }
}

