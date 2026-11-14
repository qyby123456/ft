package net.farmingtales.ftboss.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public final class ZheergenArcaneData {
    public static final Set<String> SPELLS = Set.of("shadow_brand", "void_echo", "nightfall_blades");
    private static final Map<String, ZheergenArcaneData.Shape> SHAPES = read();

    private ZheergenArcaneData() {
    }

    public static ZheergenArcaneData.Shape shape(String var0) {
        return SHAPES.get(var0);
    }

    private static double scalar(JsonObject var0, String var1, double var2, double var4) {
        double var6 = var0.get(var1).getAsDouble();
        if (Double.isFinite(var6) && !(var6 < var2) && !(var6 > var4)) {
            return var6;
        } else {
            throw new IllegalArgumentException("Invalid arcane field: " + var1);
        }
    }

    private static double[] vector(JsonArray var0, int var1, double var2, double var4) {
        if (var0.size() != var1) {
            throw new IllegalArgumentException("Invalid arcane vector size");
        } else {
            double[] var6 = new double[var1];

            for (int var7 = 0; var7 < var1; var7++) {
                var6[var7] = var0.get(var7).getAsDouble();
                if (!Double.isFinite(var6[var7]) || var6[var7] < var2 || var6[var7] > var4) {
                    throw new IllegalArgumentException("Invalid arcane coordinate");
                }
            }

            return var6;
        }
    }

    private static Map<String, ZheergenArcaneData.Shape> read() {
        try {
            Map var22;
            try (InputStream var0 = ZheergenArcaneData.class.getResourceAsStream("/assets/ftboss/fx/zheergen_arcane_models.json")) {
                if (var0 == null) {
                    throw new IllegalStateException("Missing arcane rebuild payload");
                }

                JsonObject var1 = JsonParser.parseReader(new InputStreamReader(var0, StandardCharsets.UTF_8)).getAsJsonObject();
                if (var1.get("schema").getAsInt() != 2) {
                    throw new IllegalArgumentException("Arcane schema mismatch");
                }

                LinkedHashMap var2 = new LinkedHashMap();

                for (Entry var4 : var1.getAsJsonObject("models").entrySet()) {
                    if (!SPELLS.contains(var4.getKey())) {
                        throw new IllegalArgumentException("Unexpected spell in arcane payload");
                    }

                    JsonObject var5 = ((JsonElement)var4.getValue()).getAsJsonObject();
                    ArrayList var6 = new ArrayList();
                    int var7 = 0;

                    for (JsonElement var9 : var5.getAsJsonArray("faces")) {
                        JsonObject var10 = var9.getAsJsonObject();
                        double[][] var11 = new double[4][];
                        double[][] var12 = new double[4][];
                        if (var10.getAsJsonArray("points").size() == 4 && var10.getAsJsonArray("uv").size() == 4) {
                            for (int var13 = 0; var13 < 4; var13++) {
                                var11[var13] = vector(var10.getAsJsonArray("points").get(var13).getAsJsonArray(), 3, -16.0, 16.0);
                                var12[var13] = vector(var10.getAsJsonArray("uv").get(var13).getAsJsonArray(), 2, 0.0, 1.0);
                            }

                            int var27 = (int)scalar(var10, "color", 0.0, 1.6777215E7);
                            int var14 = (int)scalar(var10, "bone", 20.0, 44.0);
                            float var15 = (float)scalar(var10, "opacity", 0.001, 1.0);
                            boolean var16 = var10.get("group").getAsString().equals("detail");
                            if (!var16) {
                                var7++;
                            }

                            double var17 = var10.has("alphaLag") ? scalar(var10, "alphaLag", 0.0, 6.0) : 0.0;
                            if (!(var17 > 0.0) || !(Math.abs(var17 - 1.8) > 1.0E-8) && var14 >= 33 && var14 <= 37) {
                                var6.add(
                                    new ZheergenArcaneData.Surface(
                                        var11, var12, var27, var15, var14, vector(var10.getAsJsonArray("pivot"), 3, -16.0, 16.0), var16, var17
                                    )
                                );
                                continue;
                            }

                            throw new IllegalArgumentException("Unsupported delayed material channel");
                        }

                        throw new IllegalArgumentException("Arcane face must be a quad");
                    }

                    if (var6.isEmpty() || var6.size() > 4000 || var7 == 0) {
                        throw new IllegalArgumentException("Invalid arcane surface budget");
                    }

                    JsonObject var23 = var5.getAsJsonObject("animatedBounds");
                    double[] var24 = vector(var23.getAsJsonArray("minimum"), 3, -16.0, 16.0);
                    double[] var25 = vector(var23.getAsJsonArray("maximum"), 3, -16.0, 16.0);

                    for (int var26 = 0; var26 < 3; var26++) {
                        if (var24[var26] >= var25[var26]) {
                            throw new IllegalArgumentException("Invalid arcane hull");
                        }
                    }

                    var2.put(
                        (String)var4.getKey(),
                        new ZheergenArcaneData.Shape(
                            scalar(var5, "span", 2.0, 24.0),
                            scalar(var5, "height", 0.0, 4.0),
                            scalar(var5, "scale", 0.01, 4.0),
                            var24,
                            var25,
                            List.copyOf(var6),
                            var7
                        )
                    );
                }

                if (!var2.keySet().equals(SPELLS)) {
                    throw new IllegalArgumentException("Incomplete arcane rebuild");
                }

                var22 = Map.copyOf(var2);
            }

            return var22;
        } catch (Exception var21) {
            throw new IllegalStateException("Cannot load independently rebuilt arcane assets", var21);
        }
    }

    public static record Shape(
        double size, double height, double scale, double[] lower, double[] upper, List<ZheergenArcaneData.Surface> surfaces, int essential
    ) {
    }

    public static record Surface(double[][] positions, double[][] uv, int tint, float opacity, int channel, double[] pivot, boolean detail, double alphaLag) {
    }
}
