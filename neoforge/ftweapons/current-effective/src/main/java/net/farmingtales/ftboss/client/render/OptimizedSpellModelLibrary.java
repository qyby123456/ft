package net.farmingtales.ftboss.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public final class OptimizedSpellModelLibrary {
    private static final Map<String, OptimizedSpellModelLibrary.Model> MODELS = load();

    private OptimizedSpellModelLibrary() {
    }

    public static OptimizedSpellModelLibrary.Model find(String var0) {
        return MODELS.get(var0);
    }

    public static Map<String, OptimizedSpellModelLibrary.Model> all() {
        return MODELS;
    }

    private static OptimizedSpellModelLibrary.Group group(JsonObject var0) {
        JsonArray var1 = var0.getAsJsonArray("vertices");
        JsonArray var2 = var0.getAsJsonArray("colors");
        double[] var3 = new double[var1.size()];
        int[] var4 = new int[var2.size()];
        float[] var5 = new float[var4.length / 4];
        int[] var6 = new int[var4.length / 4];
        int[] var7 = new int[var4.length / 4];
        double[] var8 = new double[var7.length * 3];
        double[] var9 = new double[var7.length * 8];

        for (int var10 = 0; var10 < var3.length; var10++) {
            var3[var10] = var1.get(var10).getAsDouble();
            if (!Double.isFinite(var3[var10]) || Math.abs(var3[var10]) > 32.0) {
                throw new IllegalStateException("Invalid spell vertex");
            }
        }

        for (int var14 = 0; var14 < var4.length; var14++) {
            var4[var14] = var2.get(var14).getAsInt();
        }

        for (int var15 = 0; var15 < var5.length; var15++) {
            for (int var11 = 0; var11 < 4; var11++) {
                for (int var12 = 0; var12 < 2; var12++) {
                    int var13 = var15 * 8 + var11 * 2 + var12;
                    var9[var13] = var0.has("uvs")
                        ? var0.getAsJsonArray("uvs").get(var13).getAsDouble()
                        : (var12 == 0 ? (double)(var11 < 2 ? 0 : 1) : (double)(var11 != 0 && var11 != 3 ? 0 : 1));
                    if (!Double.isFinite(var9[var13]) || var9[var13] < 0.0 || var9[var13] > 1.0) {
                        throw new IllegalStateException("Invalid continuous UV");
                    }
                }
            }

            var5[var15] = var0.getAsJsonArray("alphas").get(var15).getAsFloat();
            var6[var15] = var0.getAsJsonArray("materials").get(var15).getAsInt();
            if (var0.has("bones")) {
                var7[var15] = var0.getAsJsonArray("bones").get(var15).getAsInt();
                if (var7[var15] != 0
                    && var7[var15] != 1
                    && var7[var15] != 2
                    && var7[var15] != 3
                    && var7[var15] != 4
                    && var7[var15] != 5
                    && var7[var15] != 7
                    && var7[var15] != 8
                    && var7[var15] != 9) {
                    throw new IllegalStateException("Invalid animated bone");
                }

                for (int var16 = 0; var16 < 3; var16++) {
                    var8[var15 * 3 + var16] = var0.getAsJsonArray("pivots").get(var15 * 3 + var16).getAsDouble();
                    if (!Double.isFinite(var8[var15 * 3 + var16]) || Math.abs(var8[var15 * 3 + var16]) > 32.0) {
                        throw new IllegalStateException("Invalid animated pivot");
                    }
                }
            }

            if (!Float.isFinite(var5[var15]) || var5[var15] <= 0.0F || var5[var15] > 1.0F || var6[var15] < 0 || var6[var15] >= 4096) {
                throw new IllegalStateException("Invalid detailed material");
            }
        }

        if (var3.length != var4.length * 3 || var4.length % 4 != 0) {
            throw new IllegalStateException("Invalid spell topology");
        } else if (var0.has("uvs") && var0.getAsJsonArray("uvs").size() != var9.length) {
            throw new IllegalStateException("Invalid UV topology");
        } else {
            return new OptimizedSpellModelLibrary.Group(var3, var4, var5, var6, var7, var8, var9);
        }
    }

    private static Map<String, OptimizedSpellModelLibrary.Model> load() {
        try {
            Map var31;
            try (InputStream var0 = OptimizedSpellModelLibrary.class.getResourceAsStream("/assets/ftboss/fx/optimized_spell_models.json")) {
                if (var0 == null) {
                    throw new IllegalStateException("Missing optimized spell models");
                }

                JsonObject var1 = JsonParser.parseReader(new InputStreamReader(var0, StandardCharsets.UTF_8)).getAsJsonObject();
                LinkedHashMap var2 = new LinkedHashMap();

                for (Entry var4 : var1.entrySet()) {
                    JsonObject var5 = ((JsonElement)var4.getValue()).getAsJsonObject().getAsJsonObject("groups");
                    OptimizedSpellModelLibrary.Group var6 = group(var5.getAsJsonObject("body"));
                    OptimizedSpellModelLibrary.Group var7 = group(var5.getAsJsonObject("detail"));
                    double[] var8 = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
                    double[] var9 = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};

                    for (OptimizedSpellModelLibrary.Group var13 : new OptimizedSpellModelLibrary.Group[]{var6, var7}) {
                        for (int var14 = 0; var14 < var13.vertices().length; var14++) {
                            int var15 = var14 % 3;
                            var8[var15] = Math.min(var8[var15], var13.vertices()[var14]);
                            var9[var15] = Math.max(var9[var15], var13.vertices()[var14]);
                        }
                    }

                    double var32 = Math.max(Math.max(var9[0] - var8[0], var9[1] - var8[1]), var9[2] - var8[2]);
                    JsonObject var33 = ((JsonElement)var4.getValue()).getAsJsonObject().getAsJsonObject("scene");
                    OptimizedSpellModelLibrary.Scene var34 = new OptimizedSpellModelLibrary.Scene(
                        var33.get("anchor").getAsString(),
                        var33.get("span").getAsDouble(),
                        var33.get("height").getAsDouble(),
                        var33.get("cue").getAsString(),
                        var33.get("effect").getAsString(),
                        var32
                    );
                    if (!Set.of("FRONT", "SIDE", "BACK", "ABOVE", "TARGET", "AROUND").contains(var34.anchor())
                        || !Double.isFinite(var34.span())
                        || var34.span() < 2.8
                        || var34.span() > 8.0
                        || !Double.isFinite(var34.height())
                        || var34.height() < 1.0
                        || var34.height() > 6.0
                        || var32 <= 0.0) {
                        throw new IllegalStateException("Invalid spell spatial profile");
                    }

                    for (OptimizedSpellModelLibrary.Group var17 : new OptimizedSpellModelLibrary.Group[]{var6, var7}) {
                        for (int var18 = 0; var18 < var17.faces(); var18++) {
                            if (var17.bones()[var18] != 0) {
                                for (int var19 = 0; var19 < 4; var19++) {
                                    if (var17.bones()[var18] >= 8) {
                                        int var37 = var17.bones()[var18] == 8 ? 2 : 1;
                                        int var38 = var18 * 12 + var19 * 3;
                                        double var39 = var37 == 2 ? 0.65 : 0.45;
                                        var8[var37] = Math.min(var8[var37], var17.vertices()[var38 + var37] - var39);
                                        var9[var37] = Math.max(var9[var37], var17.vertices()[var38 + var37] + var39);
                                    } else {
                                        int var20 = var18 * 12 + var19 * 3;
                                        int var21 = var17.bones()[var18] <= 2 ? 2 : 1;
                                        double var22 = Math.hypot(
                                            var17.vertices()[var20] - var17.pivots()[var18 * 3],
                                            var17.vertices()[var20 + var21] - var17.pivots()[var18 * 3 + var21]
                                        );

                                        for (int var27 : new int[]{0, var21}) {
                                            var8[var27] = Math.min(var8[var27], var17.pivots()[var18 * 3 + var27] - var22);
                                            var9[var27] = Math.max(var9[var27], var17.pivots()[var18 * 3 + var27] + var22);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    var2.put(
                        (String)var4.getKey(),
                        new OptimizedSpellModelLibrary.Model(
                            var6, var7, group(var5.getAsJsonObject("beam_body")), group(var5.getAsJsonObject("beam_detail")), var8, var9, var34
                        )
                    );
                }

                var31 = Collections.unmodifiableMap(var2);
            }

            return var31;
        } catch (Exception var30) {
            throw new IllegalStateException("Cannot load optimized spell models", var30);
        }
    }

    public static record Group(double[] vertices, int[] colors, float[] alphas, int[] materials, int[] bones, double[] pivots, double[] uvs) {
        public int faces() {
            return this.colors.length / 4;
        }
    }

    public static record Model(
        OptimizedSpellModelLibrary.Group body,
        OptimizedSpellModelLibrary.Group detail,
        OptimizedSpellModelLibrary.Group beamBody,
        OptimizedSpellModelLibrary.Group beamDetail,
        double[] minimum,
        double[] maximum,
        OptimizedSpellModelLibrary.Scene scene
    ) {
    }

    public static record Scene(String anchor, double span, double height, String cue, String effect, double nativeSpan) {
    }
}
