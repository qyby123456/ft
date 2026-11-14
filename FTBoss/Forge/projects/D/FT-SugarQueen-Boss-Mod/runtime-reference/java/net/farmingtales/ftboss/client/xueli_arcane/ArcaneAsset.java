/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.client.xueli_arcane;

import com.google.gson.JsonArray;
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
import java.util.Set;

public final class ArcaneAsset {
    public static final Set<String> IDS = Set.of("frost_needles", "whiteout_edict", "sugar_hail", "glacial_prism", "royal_shatter", "winter_clock");

    private ArcaneAsset() {
    }

    public static boolean centerRay(String string) {
        if (string.equals("lance:1") || string.equals("afterimage:1")) {
            return true;
        }
        if (!string.startsWith("trail:")) {
            return false;
        }
        int n = Integer.parseInt(string.substring(6));
        return n >= 7 && n < 14;
    }

    private static double[] vector(JsonArray jsonArray, int n) {
        if (jsonArray.size() != n) {
            throw new IllegalArgumentException("Invalid vector length");
        }
        double[] dArray = new double[n];
        for (int i = 0; i < n; ++i) {
            dArray[i] = jsonArray.get(i).getAsDouble();
            if (Double.isFinite(dArray[i]) && !(Math.abs(dArray[i]) > 32.0)) continue;
            throw new IllegalArgumentException("Unsafe coordinate");
        }
        return dArray;
    }

    public static Map<String, Effect> load() {
        LinkedHashMap<String, Effect> linkedHashMap = new LinkedHashMap<String, Effect>();
        for (String string : IDS) {
            try {
                InputStream inputStream = ArcaneAsset.class.getResourceAsStream("/assets/ftboss/xueli_arcane/" + string + ".json");
                try {
                    double d;
                    Object object3;
                    Object object22;
                    if (inputStream == null) {
                        throw new IllegalStateException("Missing fresh asset: " + string);
                    }
                    JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                    ArrayList<Bone> arrayList = new ArrayList<Bone>();
                    for (Object object22 : jsonObject.getAsJsonArray("bones")) {
                        object3 = object22.getAsJsonObject();
                        arrayList.add(new Bone(object3.get("name").getAsString(), object3.get("motion").getAsString(), ArcaneAsset.vector(object3.getAsJsonArray("pivot"), 3)));
                    }
                    ArrayList arrayList2 = new ArrayList();
                    for (Object object3 : jsonObject.getAsJsonArray("faces")) {
                        JsonObject jsonObject2 = object3.getAsJsonObject();
                        int n = jsonObject2.getAsJsonArray("points").size();
                        int n2 = jsonObject2.get("bone").getAsInt();
                        if (n != 3 && n != 4 || n2 < 0 || n2 >= arrayList.size()) {
                            throw new IllegalArgumentException("Invalid fresh rig topology");
                        }
                        double[][] dArrayArray = new double[n][];
                        double[][] dArrayArray2 = new double[n][];
                        for (int i = 0; i < n; ++i) {
                            dArrayArray[i] = ArcaneAsset.vector(jsonObject2.getAsJsonArray("points").get(i).getAsJsonArray(), 3);
                            dArrayArray2[i] = ArcaneAsset.vector(jsonObject2.getAsJsonArray("uv").get(i).getAsJsonArray(), 2);
                            if (!(dArrayArray2[i][0] < 0.0 || dArrayArray2[i][0] > 1.0 || dArrayArray2[i][1] < 0.0) && !(dArrayArray2[i][1] > 1.0)) continue;
                            throw new IllegalArgumentException("Out-of-atlas coordinates");
                        }
                        double d2 = jsonObject2.get("alpha").getAsDouble();
                        if (d2 <= 0.0 || d2 > 1.0) {
                            throw new IllegalArgumentException("Invalid material opacity");
                        }
                        arrayList2.add(new Face(dArrayArray, dArrayArray2, d2, n2, jsonObject2.get("light").getAsBoolean()));
                    }
                    object22 = jsonObject.getAsJsonObject("layout");
                    object3 = object22.get("anchor").getAsString();
                    if (!Set.of("GROUND", "SELF", "FORWARD").contains(object3) || arrayList2.size() > 400 || arrayList.size() > 64) {
                        throw new IllegalArgumentException("Unbounded effect");
                    }
                    double d3 = jsonObject.get("scale").getAsDouble();
                    double d4 = d = object22.has("innerRadius") ? object22.get("innerRadius").getAsDouble() : 1.0;
                    if (!Double.isFinite(d3) || d3 <= 0.0 || !Double.isFinite(d) || d <= 0.0) {
                        throw new IllegalArgumentException("Invalid placement scale");
                    }
                    linkedHashMap.put(string, new Effect(string, List.copyOf(arrayList), List.copyOf(arrayList2), (String)object3, d3, ArcaneAsset.vector(jsonObject.getAsJsonArray("offset"), 3), d));
                }
                finally {
                    if (inputStream == null) continue;
                    inputStream.close();
                }
            }
            catch (Exception exception) {
                throw new IllegalStateException("Cannot load new arcane effect " + string, exception);
            }
        }
        return Map.copyOf(linkedHashMap);
    }

    public record Bone(String name, String track, double[] pivot) {
    }

    public record Face(double[][] positions, double[][] uv, double alpha, int bone, boolean light) {
    }

    public record Effect(String id, List<Bone> bones, List<Face> faces, String anchor, double scale, double[] offset, double innerRadius) {
        public double radiusScale(double d) {
            return this.anchor.equals("SELF") ? Math.max(1.0, (d / Math.sqrt(2.0) + 0.33) / (this.innerRadius * this.scale)) : 1.0;
        }
    }
}

