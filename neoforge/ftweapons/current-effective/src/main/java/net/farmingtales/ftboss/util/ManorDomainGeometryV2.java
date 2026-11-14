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
import java.util.Map.Entry;
import net.farmingtales.ftboss.util.ManorCeremonyModel.Sink;
import org.slf4j.LoggerFactory;

public final class ManorDomainGeometryV2 {
    private static final Map<Integer, ManorDomainGeometryV2.Model> MODELS = load();

    private ManorDomainGeometryV2() {
    }

    public static ManorDomainGeometryV2.Model model(int var0) {
        return MODELS.get(var0);
    }

    private static Map<Integer, ManorDomainGeometryV2.Model> load() {
        try {
            Map var1;
            try (InputStream var0 = ManorDomainGeometryV2.class.getResourceAsStream("/assets/ftboss/manor/domain_geometry_v2.json")) {
                if (var0 == null) {
                    throw new IllegalStateException("Missing manor domain geometry v2");
                }

                var1 = read(new InputStreamReader(var0, StandardCharsets.UTF_8));
            }

            return var1;
        } catch (Exception var5) {
            LoggerFactory.getLogger(ManorDomainGeometryV2.class).error("Could not load manor domain geometry v2", (Throwable)var5);
            return Map.of();
        }
    }

    public static Map<Integer, ManorDomainGeometryV2.Model> read(Reader var0) {
        Gson var1 = new Gson();
        JsonObject var2 = var1.fromJson(var0, JsonObject.class);
        if (var2 != null && var2.size() == 11) {
            Map<Integer, ManorDomainGeometryV2.Model> var3 = new HashMap<>();

            for (Entry<String, JsonElement> var5 : var2.entrySet()) {
                ManorDomainGeometryV2.Model var6 = var1.fromJson((JsonElement)var5.getValue(), ManorDomainGeometryV2.Model.class);
                if (var6.variant >= 0
                    && var6.variant <= 11
                    && var6.variant != 9
                    && !var3.containsKey(var6.variant)
                    && var6.groups != null
                    && var6.groups.size() <= 16) {
                    if (var6.name != null
                        && !var6.name.isBlank()
                        && !var6.groups.isEmpty()
                        && var6.glow >= 0
                        && var6.glow <= 16777215
                        && var6.accent >= 0
                        && var6.accent <= 16777215) {
                        int var7 = 0;

                        for (ManorDomainGeometryV2.Group var9 : var6.groups.values()) {
                            if (var9 == null
                                || var9.vertices == null
                                || var9.colors == null
                                || var9.vertices.length != var9.colors.length * 3
                                || var9.colors.length % 4 != 0
                                || !Double.isFinite(var9.yaw)) {
                                throw new IllegalArgumentException("Invalid domain mesh " + (String)var5.getKey());
                            }

                            if (!"solid".equals(var9.layer) && !"glow".equals(var9.layer)) {
                                throw new IllegalArgumentException("Invalid domain layer");
                            }

                            if (!"sigil".equals(var9.phase)
                                && !"foundation".equals(var9.phase)
                                && !"boundary".equals(var9.phase)
                                && !"monuments".equals(var9.phase)) {
                                throw new IllegalArgumentException("Invalid domain phase");
                            }

                            for (int var10 = 0; var10 < var9.vertices.length; var10 += 3) {
                                double var11 = var9.vertices[var10];
                                double var13 = var9.vertices[var10 + 1];
                                double var15 = var9.vertices[var10 + 2];
                                if (!Double.isFinite(var11)
                                    || !Double.isFinite(var13)
                                    || !Double.isFinite(var15)
                                    || Math.hypot(var11, var15) > 9.1
                                    || var13 < 0.0
                                    || var13 > 1.0) {
                                    throw new IllegalArgumentException("Out of bounds domain mesh");
                                }
                            }

                            var7 += var9.colors.length;

                            for (int var19 : var9.colors) {
                                if (var19 < 0 || var19 > 16777215) {
                                    throw new IllegalArgumentException("Invalid domain vertex color");
                                }
                            }
                        }

                        if (var7 > 24000) {
                            throw new IllegalArgumentException("Domain mesh exceeds budget");
                        }

                        var3.put(var6.variant, var6);
                        continue;
                    }

                    throw new IllegalArgumentException("Invalid domain identity");
                }

                throw new IllegalArgumentException("Invalid domain model " + (String)var5.getKey());
            }

            if (var3.size() != 11) {
                throw new IllegalArgumentException("Expected eleven manor domains");
            } else {
                return Map.copyOf(var3);
            }
        } else {
            throw new IllegalArgumentException("Expected eleven manor domains");
        }
    }

    public static void emit(ManorDomainGeometryV2.Model var0, String var1, double var2, boolean var4, float var5, Sink var6) {
        if (var0 != null && Double.isFinite(var2) && Float.isFinite(var5) && !(var2 < 0.0) && !(var5 <= 0.001F)) {
            var5 = Math.min(1.0F, var5);

            for (ManorDomainGeometryV2.Group var8 : var0.groups.values()) {
                if (var1.equals(var8.layer) && (!var4 || !var8.detail)) {
                    double var9 = ManorDomainExpansion.scale(var8.phase, var2);
                    if (!(var9 <= 0.001)) {
                        double var11 = "monuments".equals(var8.phase) ? 1.0 : var9;
                        double var13 = "monuments".equals(var8.phase) ? var9 : 1.0;
                        double var15 = var8.yaw * Math.max(0.0, var2 - 64.0);
                        double var17 = Math.cos(var15);
                        double var19 = Math.sin(var15);
                        float var21 = var5 * (float)var9 * ("glow".equals(var1) ? 0.68F : 1.0F);

                        for (int var22 = 0; var22 < var8.colors.length; var22++) {
                            double var23 = var8.vertices[var22 * 3] * var11;
                            double var25 = var8.vertices[var22 * 3 + 1] * var13;
                            double var27 = var8.vertices[var22 * 3 + 2] * var11;
                            var6.vertex(var23 * var17 + var27 * var19, var25, var27 * var17 - var23 * var19, var8.colors[var22], var21);
                        }
                    }
                }
            }
        }
    }

    public static final class Group {
        public String layer;
        public String phase;
        public double yaw;
        public boolean detail;
        public double[] vertices;
        public int[] colors;
    }

    public static final class Model {
        public int variant;
        public String name;
        public int glow;
        public int accent;
        public LinkedHashMap<String, ManorDomainGeometryV2.Group> groups;
    }
}
