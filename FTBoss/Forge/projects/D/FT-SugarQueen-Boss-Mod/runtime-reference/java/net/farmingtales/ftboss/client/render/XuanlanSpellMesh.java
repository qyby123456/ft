/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 */
package net.farmingtales.ftboss.client.render;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class XuanlanSpellMesh {
    private static final Map<String, Model> MODELS = XuanlanSpellMesh.load();

    private XuanlanSpellMesh() {
    }

    public static Model find(String string) {
        return MODELS.get(string);
    }

    public static Map<String, Model> all() {
        return MODELS;
    }

    private static Map<String, Model> load() {
        Map map;
        block23: {
            InputStream inputStream = XuanlanSpellMesh.class.getResourceAsStream("/assets/ftboss/xuanlan/models.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing independent Xuanlan models");
                }
                Asset asset = (Asset)new Gson().fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), Asset.class);
                if (asset.atlasSize() != 256) {
                    throw new IllegalStateException("Invalid Xuanlan atlas size");
                }
                LinkedHashMap<String, Model> linkedHashMap = new LinkedHashMap<String, Model>();
                for (Model model : asset.models()) {
                    if (linkedHashMap.put(model.id(), model) != null) {
                        throw new IllegalStateException("Duplicate Xuanlan model");
                    }
                    for (Face face : model.faces()) {
                        if (face.points().length != 4 || face.uv().length != 4 || face.material() < 0 || face.material() >= 256 || !Float.isFinite(face.opacity()) || face.opacity() <= 0.0f || face.opacity() > 1.0f || face.bone() != 0 && face.bone() != 3 && face.bone() != 7) {
                            throw new IllegalStateException("Invalid Xuanlan face");
                        }
                        for (double[] dArray : face.points()) {
                            if (dArray.length != 3) {
                                throw new IllegalStateException("Invalid Xuanlan vertex");
                            }
                            for (double d : dArray) {
                                if (Double.isFinite(d) && !(Math.abs(d) > 8.0)) continue;
                                throw new IllegalStateException("Unbounded Xuanlan vertex");
                            }
                        }
                        for (double[] dArray : face.uv()) {
                            if (dArray.length != 2) {
                                throw new IllegalStateException("Invalid Xuanlan UV");
                            }
                            for (double d : dArray) {
                                if (Double.isFinite(d) && !(d < 0.0) && !(d > 1.0)) continue;
                                throw new IllegalStateException("Invalid Xuanlan UV coordinate");
                            }
                        }
                        if (face.bone() == 0) continue;
                        if (face.pivot() == null || face.pivot().length != 3) {
                            throw new IllegalStateException("Missing Xuanlan pivot");
                        }
                        for (double d : face.pivot()) {
                            if (Double.isFinite(d) && !(Math.abs(d) > 8.0)) continue;
                            throw new IllegalStateException("Invalid Xuanlan pivot");
                        }
                    }
                }
                if (!linkedHashMap.keySet().equals(Set.of("orbital_gears", "pressure_lance", "magnetic_storm"))) {
                    throw new IllegalStateException("Wrong Xuanlan spell set");
                }
                map = Map.copyOf(linkedHashMap);
                if (inputStream == null) break block23;
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
                    throw new IllegalStateException("Cannot load independent Xuanlan assets", exception);
                }
            }
            inputStream.close();
        }
        return map;
    }

    public record Model(String id, String title, List<Face> faces) {
    }

    private record Asset(int atlasSize, List<Model> models) {
    }

    public record Face(double[][] points, double[][] uv, String kind, String group, String part, int material, float opacity, int bone, double[] pivot) {
    }
}

