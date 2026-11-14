/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 */
package net.farmingtales.ftboss.client.xuanlanarcane;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ArcaneForms {
    private static final Set<String> IDS = Set.of("orbital_gears", "pressure_lance", "magnetic_storm");
    private static final Map<String, Spell> LOADED = ArcaneForms.read();

    private ArcaneForms() {
    }

    public static boolean belongs(String string) {
        return string != null && IDS.contains(string);
    }

    public static Spell get(String string) {
        return LOADED.get(string);
    }

    public static Map<String, Spell> all() {
        return LOADED;
    }

    private static Map<String, Spell> read() {
        Map map;
        block21: {
            InputStream inputStream = ArcaneForms.class.getResourceAsStream("/assets/ftboss/xuanlan_arcane/forms.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Arcane forms missing");
                }
                Document document = (Document)new Gson().fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), Document.class);
                if (!"ftboss.xuanlan.arcane/1".equals(document.schema()) || document.atlas() != 256) {
                    throw new IllegalStateException("Wrong arcane schema");
                }
                LinkedHashMap<String, Spell> linkedHashMap = new LinkedHashMap<String, Spell>();
                for (Spell spell : document.spells()) {
                    if (!ArcaneForms.belongs(spell.id()) || linkedHashMap.put(spell.id(), spell) != null) {
                        throw new IllegalStateException("Wrong spell set");
                    }
                    for (Joint joint : spell.joints()) {
                        ArcaneForms.vector(joint.pivot(), 3, 8.0f);
                        ArcaneForms.vector(joint.axis(), 3, 1.0f);
                        double d = 0.0;
                        for (float f : joint.axis()) {
                            d += (double)(f * f);
                        }
                        if (!(Math.abs(d - 1.0) > 1.0E-4) && joint.slot() >= 0 && joint.slot() <= 3) continue;
                        throw new IllegalStateException("Invalid new rig");
                    }
                    for (Layer layer : spell.layers()) {
                        if (!Set.of("solid", "glow", "beam", "boundary").contains(layer.role()) || layer.tile() < 0 || layer.tile() > 6 || layer.joint() < -1 || layer.joint() >= spell.joints().size() || !Float.isFinite(layer.alpha()) || layer.alpha() <= 0.0f || layer.alpha() > 1.0f) {
                            throw new IllegalStateException("Invalid arcane layer");
                        }
                        for (Facet facet : layer.facets()) {
                            if (facet.position().length != 4 || facet.uv().length != 4) {
                                throw new IllegalStateException("Invalid quad");
                            }
                            for (float[] fArray : facet.position()) {
                                ArcaneForms.vector(fArray, 3, 8.0f);
                            }
                            for (float[] fArray : facet.uv()) {
                                ArcaneForms.vector(fArray, 2, 1.0f);
                                if (!(fArray[0] < 0.0f) && !(fArray[1] < 0.0f)) continue;
                                throw new IllegalStateException("Invalid UV");
                            }
                        }
                    }
                }
                if (!linkedHashMap.keySet().equals(IDS)) {
                    throw new IllegalStateException("Incomplete arcane library");
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
                    throw new IllegalStateException("Cannot read new Xuanlan arcane assets", exception);
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
            throw new IllegalStateException("Unbounded vector");
        }
    }

    public record Spell(String id, String title, String meaning, List<Joint> joints, List<Layer> layers) {
    }

    private record Document(String schema, int atlas, List<Spell> spells) {
    }

    public record Joint(String name, float[] pivot, float[] axis, String rhythm, int slot) {
    }

    public record Layer(String name, String role, int tile, int joint, boolean detail, float alpha, List<Facet> facets) {
    }

    public record Facet(float[][] position, float[][] uv) {
    }
}

