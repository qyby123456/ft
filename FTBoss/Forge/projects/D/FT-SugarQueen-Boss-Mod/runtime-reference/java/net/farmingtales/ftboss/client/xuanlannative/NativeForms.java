/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 */
package net.farmingtales.ftboss.client.xuanlannative;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NativeForms {
    private static final Map<String, Model> MODELS = NativeForms.load();

    private NativeForms() {
    }

    public static Model get(String string) {
        return MODELS.get(string);
    }

    public static Map<String, Model> all() {
        return MODELS;
    }

    private static Map<String, Model> load() {
        Map map;
        block21: {
            InputStream inputStream = NativeForms.class.getResourceAsStream("/assets/ftboss/xuanlan_native/forms.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Native Xuanlan forms missing");
                }
                Document document = (Document)new Gson().fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), Document.class);
                if (!document.schema().equals("ftboss.xuanlan.native/1") || document.atlas() != 256) {
                    throw new IllegalStateException("Wrong native schema");
                }
                LinkedHashMap<String, Model> linkedHashMap = new LinkedHashMap<String, Model>();
                for (Model model : document.spells()) {
                    if (linkedHashMap.put(model.id(), model) != null || model.life() < 1 || model.life() > 120) {
                        throw new IllegalStateException("Invalid model identity");
                    }
                    for (Joint joint : model.joints()) {
                        NativeForms.vector(joint.pivot(), 3, 32.0f);
                        NativeForms.vector(joint.axis(), 3, 1.0f);
                        double d = 0.0;
                        for (float f : joint.axis()) {
                            d += (double)(f * f);
                        }
                        if (!(Math.abs(d - 1.0) > 1.0E-5) && joint.slot() >= 0 && joint.slot() <= 7) continue;
                        throw new IllegalStateException("Invalid native rig");
                    }
                    for (Layer layer : model.layers()) {
                        if (!Set.of("solid", "glow", "beam", "boundary").contains(layer.role()) || layer.tile() < 0 || layer.tile() > 7 || layer.joint() < -1 || layer.joint() >= model.joints().size() || layer.facets().isEmpty() || !Float.isFinite(layer.alpha()) || layer.alpha() <= 0.0f || layer.alpha() > 1.0f) {
                            throw new IllegalStateException("Invalid native layer");
                        }
                        for (Face face : layer.facets()) {
                            if (face.position().length != 4 || face.uv().length != 4) {
                                throw new IllegalStateException("Invalid native face");
                            }
                            for (float[] fArray : face.position()) {
                                NativeForms.vector(fArray, 3, 32.0f);
                            }
                            for (float[] fArray : face.uv()) {
                                NativeForms.vector(fArray, 2, 1.0f);
                                if (!(fArray[0] < 0.0f) && !(fArray[1] < 0.0f)) continue;
                                throw new IllegalStateException("Negative UV");
                            }
                        }
                    }
                }
                if (linkedHashMap.size() != 14) {
                    throw new IllegalStateException("Incomplete native library");
                }
                map = Map.copyOf(linkedHashMap);
                if (inputStream == null) break block21;
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
                    throw new IllegalStateException("Cannot read native Xuanlan assets", exception);
                }
            }
            inputStream.close();
        }
        return map;
    }

    private static void vector(float[] fArray, int n, float f) {
        if (fArray == null || fArray.length != n) {
            throw new IllegalStateException("Invalid vector");
        }
        for (float f2 : fArray) {
            if (Float.isFinite(f2) && !(Math.abs(f2) > f)) continue;
            throw new IllegalStateException("Nonfinite or unbounded vector");
        }
    }

    public record Model(String id, String title, String meaning, String space, int life, List<Joint> joints, List<Layer> layers) {
    }

    private record Document(String schema, int atlas, List<Model> spells) {
    }

    public record Joint(String name, float[] pivot, float[] axis, String rhythm, int slot) {
    }

    public record Layer(String name, String role, int tile, int joint, int detail, float alpha, List<Face> facets) {
    }

    public record Face(float[][] position, float[][] uv) {
    }
}

