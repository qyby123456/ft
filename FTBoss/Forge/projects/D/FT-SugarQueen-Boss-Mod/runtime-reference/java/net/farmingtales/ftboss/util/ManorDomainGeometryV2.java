/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  org.slf4j.LoggerFactory
 */
package net.farmingtales.ftboss.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.util.ManorCeremonyModel;
import net.farmingtales.ftboss.util.ManorDomainExpansion;
import org.slf4j.LoggerFactory;

public final class ManorDomainGeometryV2 {
    private static final Map<Integer, Model> MODELS = ManorDomainGeometryV2.load();

    private ManorDomainGeometryV2() {
    }

    public static Model model(int n) {
        return MODELS.get(n);
    }

    private static Map<Integer, Model> load() {
        Map<Integer, Model> map;
        block9: {
            InputStream inputStream = ManorDomainGeometryV2.class.getResourceAsStream("/assets/ftboss/manor/domain_geometry_v2.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing manor domain geometry v2");
                }
                map = ManorDomainGeometryV2.read(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                if (inputStream == null) break block9;
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
                    LoggerFactory.getLogger(ManorDomainGeometryV2.class).error("Could not load manor domain geometry v2", (Throwable)exception);
                    return Map.of();
                }
            }
            inputStream.close();
        }
        return map;
    }

    public static Map<Integer, Model> read(Reader reader) {
        Gson gson = new Gson();
        JsonObject jsonObject = (JsonObject)gson.fromJson(reader, JsonObject.class);
        if (jsonObject == null || jsonObject.size() != 11) {
            throw new IllegalArgumentException("Expected eleven manor domains");
        }
        HashMap<Integer, Model> hashMap = new HashMap<Integer, Model>();
        for (Map.Entry entry : jsonObject.entrySet()) {
            Model model = (Model)gson.fromJson((JsonElement)entry.getValue(), Model.class);
            if (model.variant < 0 || model.variant > 11 || model.variant == 9 || hashMap.containsKey(model.variant) || model.groups == null || model.groups.size() > 16) {
                throw new IllegalArgumentException("Invalid domain model " + (String)entry.getKey());
            }
            if (model.name == null || model.name.isBlank() || model.groups.isEmpty() || model.glow < 0 || model.glow > 0xFFFFFF || model.accent < 0 || model.accent > 0xFFFFFF) {
                throw new IllegalArgumentException("Invalid domain identity");
            }
            int n = 0;
            for (Group group : model.groups.values()) {
                if (group == null || group.vertices == null || group.colors == null || group.vertices.length != group.colors.length * 3 || group.colors.length % 4 != 0 || !Double.isFinite(group.yaw)) {
                    throw new IllegalArgumentException("Invalid domain mesh " + (String)entry.getKey());
                }
                if (!"solid".equals(group.layer) && !"glow".equals(group.layer)) {
                    throw new IllegalArgumentException("Invalid domain layer");
                }
                if (!("sigil".equals(group.phase) || "foundation".equals(group.phase) || "boundary".equals(group.phase) || "monuments".equals(group.phase))) {
                    throw new IllegalArgumentException("Invalid domain phase");
                }
                for (int i = 0; i < group.vertices.length; i += 3) {
                    double d = group.vertices[i];
                    double d2 = group.vertices[i + 1];
                    double d3 = group.vertices[i + 2];
                    if (Double.isFinite(d) && Double.isFinite(d2) && Double.isFinite(d3) && !(Math.hypot(d, d3) > 9.1) && !(d2 < 0.0) && !(d2 > 1.0)) continue;
                    throw new IllegalArgumentException("Out of bounds domain mesh");
                }
                n += group.colors.length;
                for (int n2 : group.colors) {
                    if (n2 >= 0 && n2 <= 0xFFFFFF) continue;
                    throw new IllegalArgumentException("Invalid domain vertex color");
                }
            }
            if (n > 24000) {
                throw new IllegalArgumentException("Domain mesh exceeds budget");
            }
            hashMap.put(model.variant, model);
        }
        if (hashMap.size() != 11) {
            throw new IllegalArgumentException("Expected eleven manor domains");
        }
        return Map.copyOf(hashMap);
    }

    public static void emit(Model model, String string, double d, boolean bl, float f, ManorCeremonyModel.Sink sink) {
        if (model == null || !Double.isFinite(d) || !Float.isFinite(f) || d < 0.0 || f <= 0.001f) {
            return;
        }
        f = Math.min(1.0f, f);
        for (Group group : model.groups.values()) {
            double d2;
            if (!string.equals(group.layer) || bl && group.detail || (d2 = ManorDomainExpansion.scale(group.phase, d)) <= 0.001) continue;
            double d3 = "monuments".equals(group.phase) ? 1.0 : d2;
            double d4 = "monuments".equals(group.phase) ? d2 : 1.0;
            double d5 = group.yaw * Math.max(0.0, d - 64.0);
            double d6 = Math.cos(d5);
            double d7 = Math.sin(d5);
            float f2 = f * (float)d2 * ("glow".equals(string) ? 0.68f : 1.0f);
            for (int i = 0; i < group.colors.length; ++i) {
                double d8 = group.vertices[i * 3] * d3;
                double d9 = group.vertices[i * 3 + 1] * d4;
                double d10 = group.vertices[i * 3 + 2] * d3;
                sink.vertex(d8 * d6 + d10 * d7, d9, d10 * d6 - d8 * d7, group.colors[i], f2);
            }
        }
    }

    public static final class Model {
        public int variant;
        public String name;
        public int glow;
        public int accent;
        public LinkedHashMap<String, Group> groups;
    }

    public static final class Group {
        public String layer;
        public String phase;
        public double yaw;
        public boolean detail;
        public double[] vertices;
        public int[] colors;
    }
}

