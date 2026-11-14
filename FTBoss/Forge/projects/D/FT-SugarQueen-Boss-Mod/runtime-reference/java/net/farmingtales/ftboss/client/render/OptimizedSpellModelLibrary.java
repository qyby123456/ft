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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class OptimizedSpellModelLibrary {
    private static final Map<String, Model> MODELS = OptimizedSpellModelLibrary.load();

    private OptimizedSpellModelLibrary() {
    }

    public static Model find(String string) {
        return MODELS.get(string);
    }

    public static Map<String, Model> all() {
        return MODELS;
    }

    private static Group group(JsonObject jsonObject) {
        int n;
        JsonArray jsonArray = jsonObject.getAsJsonArray("vertices");
        JsonArray jsonArray2 = jsonObject.getAsJsonArray("colors");
        double[] dArray = new double[jsonArray.size()];
        int[] nArray = new int[jsonArray2.size()];
        float[] fArray = new float[nArray.length / 4];
        int[] nArray2 = new int[nArray.length / 4];
        int[] nArray3 = new int[nArray.length / 4];
        double[] dArray2 = new double[nArray3.length * 3];
        double[] dArray3 = new double[nArray3.length * 8];
        for (n = 0; n < dArray.length; ++n) {
            dArray[n] = jsonArray.get(n).getAsDouble();
            if (Double.isFinite(dArray[n]) && !(Math.abs(dArray[n]) > 32.0)) continue;
            throw new IllegalStateException("Invalid spell vertex");
        }
        for (n = 0; n < nArray.length; ++n) {
            nArray[n] = jsonArray2.get(n).getAsInt();
        }
        for (n = 0; n < fArray.length; ++n) {
            int n2;
            for (n2 = 0; n2 < 4; ++n2) {
                for (int i = 0; i < 2; ++i) {
                    int n3 = n * 8 + n2 * 2 + i;
                    double d = jsonObject.has("uvs") ? jsonObject.getAsJsonArray("uvs").get(n3).getAsDouble() : (i == 0 ? (double)(n2 >= 2 ? 1 : 0) : (dArray3[n3] = (double)(n2 == 0 || n2 == 3 ? 1 : 0)));
                    if (Double.isFinite(dArray3[n3]) && !(dArray3[n3] < 0.0) && !(dArray3[n3] > 1.0)) continue;
                    throw new IllegalStateException("Invalid continuous UV");
                }
            }
            fArray[n] = jsonObject.getAsJsonArray("alphas").get(n).getAsFloat();
            nArray2[n] = jsonObject.getAsJsonArray("materials").get(n).getAsInt();
            if (jsonObject.has("bones")) {
                nArray3[n] = jsonObject.getAsJsonArray("bones").get(n).getAsInt();
                if (nArray3[n] != 0 && nArray3[n] != 1 && nArray3[n] != 2 && nArray3[n] != 3 && nArray3[n] != 4 && nArray3[n] != 5 && nArray3[n] != 7 && nArray3[n] != 8 && nArray3[n] != 9) {
                    throw new IllegalStateException("Invalid animated bone");
                }
                for (n2 = 0; n2 < 3; ++n2) {
                    dArray2[n * 3 + n2] = jsonObject.getAsJsonArray("pivots").get(n * 3 + n2).getAsDouble();
                    if (Double.isFinite(dArray2[n * 3 + n2]) && !(Math.abs(dArray2[n * 3 + n2]) > 32.0)) continue;
                    throw new IllegalStateException("Invalid animated pivot");
                }
            }
            if (Float.isFinite(fArray[n]) && !(fArray[n] <= 0.0f) && !(fArray[n] > 1.0f) && nArray2[n] >= 0 && nArray2[n] < 4096) continue;
            throw new IllegalStateException("Invalid detailed material");
        }
        if (dArray.length != nArray.length * 3 || nArray.length % 4 != 0) {
            throw new IllegalStateException("Invalid spell topology");
        }
        if (jsonObject.has("uvs") && jsonObject.getAsJsonArray("uvs").size() != dArray3.length) {
            throw new IllegalStateException("Invalid UV topology");
        }
        return new Group(dArray, nArray, fArray, nArray2, nArray3, dArray2, dArray3);
    }

    private static Map<String, Model> load() {
        Map map;
        block18: {
            InputStream inputStream = OptimizedSpellModelLibrary.class.getResourceAsStream("/assets/ftboss/fx/optimized_spell_models.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing optimized spell models");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                LinkedHashMap<String, Model> linkedHashMap = new LinkedHashMap<String, Model>();
                for (Map.Entry entry : jsonObject.entrySet()) {
                    JsonObject jsonObject2 = ((JsonElement)entry.getValue()).getAsJsonObject().getAsJsonObject("groups");
                    Group group = OptimizedSpellModelLibrary.group(jsonObject2.getAsJsonObject("body"));
                    Group group2 = OptimizedSpellModelLibrary.group(jsonObject2.getAsJsonObject("detail"));
                    double[] dArray = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
                    double[] dArray2 = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
                    for (Group group3 : new Group[]{group, group2}) {
                        for (int i = 0; i < group3.vertices().length; ++i) {
                            int n = i % 3;
                            dArray[n] = Math.min(dArray[n], group3.vertices()[i]);
                            dArray2[n] = Math.max(dArray2[n], group3.vertices()[i]);
                        }
                    }
                    double d = Math.max(Math.max(dArray2[0] - dArray[0], dArray2[1] - dArray[1]), dArray2[2] - dArray[2]);
                    JsonObject jsonObject3 = ((JsonElement)entry.getValue()).getAsJsonObject().getAsJsonObject("scene");
                    Scene record2 = new Scene(jsonObject3.get("anchor").getAsString(), jsonObject3.get("span").getAsDouble(), jsonObject3.get("height").getAsDouble(), jsonObject3.get("cue").getAsString(), jsonObject3.get("effect").getAsString(), d);
                    if (!Set.of("FRONT", "SIDE", "BACK", "ABOVE", "TARGET", "AROUND").contains(record2.anchor()) || !Double.isFinite(record2.span()) || record2.span() < 2.8 || record2.span() > 8.0 || !Double.isFinite(record2.height()) || record2.height() < 1.0 || record2.height() > 6.0 || d <= 0.0) {
                        throw new IllegalStateException("Invalid spell spatial profile");
                    }
                    for (Group group4 : new Group[]{group, group2}) {
                        for (int i = 0; i < group4.faces(); ++i) {
                            if (group4.bones()[i] == 0) continue;
                            for (int j = 0; j < 4; ++j) {
                                double d2;
                                int n;
                                int n2;
                                if (group4.bones()[i] >= 8) {
                                    n2 = group4.bones()[i] == 8 ? 2 : 1;
                                    n = i * 12 + j * 3;
                                    d2 = n2 == 2 ? 0.65 : 0.45;
                                    dArray[n2] = Math.min(dArray[n2], group4.vertices()[n + n2] - d2);
                                    dArray2[n2] = Math.max(dArray2[n2], group4.vertices()[n + n2] + d2);
                                    continue;
                                }
                                n2 = i * 12 + j * 3;
                                n = group4.bones()[i] <= 2 ? 2 : 1;
                                d2 = Math.hypot(group4.vertices()[n2] - group4.pivots()[i * 3], group4.vertices()[n2 + n] - group4.pivots()[i * 3 + n]);
                                for (int n3 : new int[]{0, n}) {
                                    dArray[n3] = Math.min(dArray[n3], group4.pivots()[i * 3 + n3] - d2);
                                    dArray2[n3] = Math.max(dArray2[n3], group4.pivots()[i * 3 + n3] + d2);
                                }
                            }
                        }
                    }
                    linkedHashMap.put((String)entry.getKey(), new Model(group, group2, OptimizedSpellModelLibrary.group(jsonObject2.getAsJsonObject("beam_body")), OptimizedSpellModelLibrary.group(jsonObject2.getAsJsonObject("beam_detail")), dArray, dArray2, record2));
                }
                map = Collections.unmodifiableMap(linkedHashMap);
                if (inputStream == null) break block18;
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
                    throw new IllegalStateException("Cannot load optimized spell models", exception);
                }
            }
            inputStream.close();
        }
        return map;
    }

    public record Model(Group body, Group detail, Group beamBody, Group beamDetail, double[] minimum, double[] maximum, Scene scene) {
    }

    public record Group(double[] vertices, int[] colors, float[] alphas, int[] materials, int[] bones, double[] pivots, double[] uvs) {
        public int faces() {
            return this.colors.length / 4;
        }
    }

    public record Scene(String anchor, double span, double height, String cue, String effect, double nativeSpan) {
    }
}

