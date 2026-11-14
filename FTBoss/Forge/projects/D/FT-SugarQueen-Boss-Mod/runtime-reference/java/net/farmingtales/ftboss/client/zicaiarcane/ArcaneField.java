/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.client.zicaiarcane;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.farmingtales.ftboss.client.zicaiarcane.ArcaneRig;

public final class ArcaneField {
    public static final Set<String> IDS = Set.of("luminous_rebuke", "halo_sanctuary", "seraph_descent");
    public static final Map<String, Model> MODELS = new LinkedHashMap<String, Model>();
    public static final Reference REFERENCE;

    private static double[] vector(JsonArray jsonArray, int n) {
        if (jsonArray.size() != n) {
            throw new IllegalStateException("Bad field vector");
        }
        double[] dArray = new double[n];
        for (int i = 0; i < n; ++i) {
            dArray[i] = jsonArray.get(i).getAsDouble();
            if (Double.isFinite(dArray[i]) && !(Math.abs(dArray[i]) > 32.0)) continue;
            throw new IllegalStateException("Unsafe field coordinate");
        }
        return dArray;
    }

    private static double[][] matrix(JsonArray jsonArray, int n) {
        double[][] dArrayArray = new double[jsonArray.size()][];
        for (int i = 0; i < dArrayArray.length; ++i) {
            dArrayArray[i] = ArcaneField.vector(jsonArray.get(i).getAsJsonArray(), n);
        }
        return dArrayArray;
    }

    public static int triangleBudget(int n) {
        return n == 0 ? 6000 : (n == 1 ? 11000 : 16000);
    }

    public static int visibleFields(int n) {
        return n == 0 ? 1 : (n == 1 ? 2 : 4);
    }

    private ArcaneField() {
    }

    static {
        try (InputStream inputStream = ArcaneField.class.getResourceAsStream("/assets/ftboss/zicai_arcane/fields.json");){
            if (inputStream == null) {
                throw new IllegalStateException("Missing new Zicai fields");
            }
            JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!jsonObject.get("schema").getAsString().equals("zicai-arcane-zero-v1")) {
                throw new IllegalStateException("Unknown field schema");
            }
            JsonObject jsonObject2 = jsonObject.getAsJsonObject("reference");
            REFERENCE = new Reference(ArcaneField.matrix(jsonObject2.getAsJsonArray("breath"), 2), jsonObject2.get("breathLength").getAsDouble(), ArcaneField.matrix(jsonObject2.getAsJsonArray("descent"), 2));
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("models")) {
                Object object;
                Object object2;
                Object object32;
                JsonObject jsonObject3 = jsonElement.getAsJsonObject();
                String string = jsonObject3.get("id").getAsString();
                if (!IDS.contains(string) || MODELS.containsKey(string)) {
                    throw new IllegalStateException("Unexpected field " + string);
                }
                String string2 = jsonObject3.get("anchor").getAsString();
                if (!Set.of("CASTER", "FIELD", "DESCENT").contains(string2)) {
                    throw new IllegalStateException("Unknown anchor");
                }
                ArrayList<Bone> arrayList = new ArrayList<Bone>();
                LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<String, Integer>();
                for (Object object32 : jsonObject3.getAsJsonArray("bones")) {
                    object2 = object32.getAsJsonObject();
                    object = object2.get("name").getAsString();
                    if (linkedHashMap.put((String)object, arrayList.size()) != null) {
                        throw new IllegalStateException("Duplicate rig bone");
                    }
                    String string3 = object2.get("behavior").getAsString();
                    ArcaneRig.frame(string3, 0.0, false);
                    arrayList.add(new Bone((String)object, string3, ArcaneField.vector(object2.getAsJsonArray("pivot"), 3)));
                }
                ArrayList arrayList2 = new ArrayList();
                object32 = new ArrayList();
                object2 = new LinkedHashMap();
                object = new ArrayList();
                for (JsonElement jsonElement2 : jsonObject3.getAsJsonArray("triangles")) {
                    JsonObject jsonObject4 = jsonElement2.getAsJsonObject();
                    Integer n2 = (Integer)linkedHashMap.get(jsonObject4.get("bone").getAsString());
                    if (n2 == null) {
                        throw new IllegalStateException("Orphan field triangle");
                    }
                    double[][] dArray = ArcaneField.matrix(jsonObject4.getAsJsonArray("points"), 3);
                    double[][] dArray2 = ArcaneField.matrix(jsonObject4.getAsJsonArray("uv"), 2);
                    if (dArray.length != 3 || dArray2.length != 3) {
                        throw new IllegalStateException("Non-triangle field face");
                    }
                    int[] nArray = new int[3];
                    for (int i = 0; i < 3; ++i) {
                        String string4 = n2 + ":" + Arrays.toString(dArray[i]);
                        Integer n3 = (Integer)object2.get(string4);
                        if (n3 == null) {
                            n3 = arrayList2.size();
                            object2.put(string4, n3);
                            arrayList2.add(dArray[i]);
                            object32.add(n2);
                        }
                        nArray[i] = n3;
                        for (double d : dArray2[i]) {
                            if (!(d < 0.0) && !(d > 1.0)) continue;
                            throw new IllegalStateException("UV outside new atlas");
                        }
                    }
                    double d = jsonObject4.get("alpha").getAsDouble();
                    if (!Double.isFinite(d) || d < 0.0 || d > 1.0) {
                        throw new IllegalStateException("Invalid field opacity");
                    }
                    object.add(new Triangle(nArray, dArray2, d, jsonObject4.get("detail").getAsBoolean()));
                }
                if (object.isEmpty() || object.size() > 6000 || arrayList.size() > 32) {
                    throw new IllegalStateException("Field budget exceeded");
                }
                boolean bl = jsonObject3.has("visualStyle") && jsonObject3.get("visualStyle").getAsString().equals("soft-light");
                MODELS.put(string, new Model(string, string2, List.copyOf(arrayList), (double[][])arrayList2.toArray(n -> new double[n][]), object32.stream().mapToInt(Integer::intValue).toArray(), List.copyOf(object), bl));
            }
            if (!MODELS.keySet().equals(IDS)) {
                throw new IllegalStateException("Incomplete arcane rebuild");
            }
        }
        catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    public record Reference(double[][] breath, double breathLength, double[][] descent) {
    }

    public record Bone(String name, String behavior, double[] pivot) {
    }

    public record Triangle(int[] indices, double[][] uv, double alpha, boolean detail) {
    }

    public record Model(String id, String anchor, List<Bone> bones, double[][] vertices, int[] vertexBones, List<Triangle> triangles, boolean softLight) {
    }
}

