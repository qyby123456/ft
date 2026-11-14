package net.farmingtales.ftboss.client.zicaiarcane;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ArcaneField {
    public static final Set<String> IDS = Set.of("luminous_rebuke", "halo_sanctuary", "seraph_descent");
    public static final Map<String, ArcaneField.Model> MODELS = new LinkedHashMap<>();
    public static final ArcaneField.Reference REFERENCE;

    private static double[] vector(JsonArray var0, int var1) {
        if (var0.size() != var1) {
            throw new IllegalStateException("Bad field vector");
        } else {
            double[] var2 = new double[var1];

            for (int var3 = 0; var3 < var1; var3++) {
                var2[var3] = var0.get(var3).getAsDouble();
                if (!Double.isFinite(var2[var3]) || Math.abs(var2[var3]) > 32.0) {
                    throw new IllegalStateException("Unsafe field coordinate");
                }
            }

            return var2;
        }
    }

    private static double[][] matrix(JsonArray var0, int var1) {
        double[][] var2 = new double[var0.size()][];

        for (int var3 = 0; var3 < var2.length; var3++) {
            var2[var3] = vector(var0.get(var3).getAsJsonArray(), var1);
        }

        return var2;
    }

    public static int triangleBudget(int var0) {
        return var0 == 0 ? 6000 : (var0 == 1 ? 11000 : 16000);
    }

    public static int visibleFields(int var0) {
        return var0 == 0 ? 1 : (var0 == 1 ? 2 : 4);
    }

    private ArcaneField() {
    }

    static {
        try {
            try (InputStream var0 = ArcaneField.class.getResourceAsStream("/assets/ftboss/zicai_arcane/fields.json")) {
                if (var0 == null) {
                    throw new IllegalStateException("Missing new Zicai fields");
                }

                JsonObject var1 = JsonParser.parseReader(new InputStreamReader(var0, StandardCharsets.UTF_8)).getAsJsonObject();
                if (!var1.get("schema").getAsString().equals("zicai-arcane-zero-v1")) {
                    throw new IllegalStateException("Unknown field schema");
                }

                JsonObject var2 = var1.getAsJsonObject("reference");
                REFERENCE = new ArcaneField.Reference(
                    matrix(var2.getAsJsonArray("breath"), 2), var2.get("breathLength").getAsDouble(), matrix(var2.getAsJsonArray("descent"), 2)
                );

                for (JsonElement var4 : var1.getAsJsonArray("models")) {
                    JsonObject var5 = var4.getAsJsonObject();
                    String var6 = var5.get("id").getAsString();
                    if (!IDS.contains(var6) || MODELS.containsKey(var6)) {
                        throw new IllegalStateException("Unexpected field " + var6);
                    }

                    String var7 = var5.get("anchor").getAsString();
                    if (!Set.of("CASTER", "FIELD", "DESCENT").contains(var7)) {
                        throw new IllegalStateException("Unknown anchor");
                    }

                    ArrayList var8 = new ArrayList();
                    LinkedHashMap var9 = new LinkedHashMap();

                    for (JsonElement var11 : var5.getAsJsonArray("bones")) {
                        JsonObject var12 = var11.getAsJsonObject();
                        String var13 = var12.get("name").getAsString();
                        if (var9.put(var13, var8.size()) != null) {
                            throw new IllegalStateException("Duplicate rig bone");
                        }

                        String var14 = var12.get("behavior").getAsString();
                        ArcaneRig.frame(var14, 0.0, false);
                        var8.add(new ArcaneField.Bone(var13, var14, vector(var12.getAsJsonArray("pivot"), 3)));
                    }

                    ArrayList<double[]> var32 = new ArrayList<>();
                    ArrayList<Integer> var33 = new ArrayList<>();
                    LinkedHashMap var34 = new LinkedHashMap();
                    ArrayList var35 = new ArrayList();

                    for (JsonElement var15 : var5.getAsJsonArray("triangles")) {
                        JsonObject var16 = var15.getAsJsonObject();
                        Integer var17 = (Integer)var9.get(var16.get("bone").getAsString());
                        if (var17 == null) {
                            throw new IllegalStateException("Orphan field triangle");
                        }

                        double[][] var18 = matrix(var16.getAsJsonArray("points"), 3);
                        double[][] var19 = matrix(var16.getAsJsonArray("uv"), 2);
                        if (var18.length == 3 && var19.length == 3) {
                            int[] var20 = new int[3];

                            for (int var21 = 0; var21 < 3; var21++) {
                                String var22 = var17 + ":" + Arrays.toString(var18[var21]);
                                Integer var23 = (Integer)var34.get(var22);
                                if (var23 == null) {
                                    var23 = var32.size();
                                    var34.put(var22, var23);
                                    var32.add(var18[var21]);
                                    var33.add(var17);
                                }

                                var20[var21] = var23;

                                for (double var27 : var19[var21]) {
                                    if (var27 < 0.0 || var27 > 1.0) {
                                        throw new IllegalStateException("UV outside new atlas");
                                    }
                                }
                            }

                            double var38 = var16.get("alpha").getAsDouble();
                            if (Double.isFinite(var38) && !(var38 < 0.0) && !(var38 > 1.0)) {
                                var35.add(new ArcaneField.Triangle(var20, var19, var38, var16.get("detail").getAsBoolean()));
                                continue;
                            }

                            throw new IllegalStateException("Invalid field opacity");
                        }

                        throw new IllegalStateException("Non-triangle field face");
                    }

                    if (var35.isEmpty() || var35.size() > 6000 || var8.size() > 32) {
                        throw new IllegalStateException("Field budget exceeded");
                    }

                    boolean var37 = var5.has("visualStyle") && var5.get("visualStyle").getAsString().equals("soft-light");
                    MODELS.put(
                        var6,
                        new ArcaneField.Model(
                            var6,
                            var7,
                            List.copyOf(var8),
                            var32.toArray(double[][]::new),
                            var33.stream().mapToInt(Integer::intValue).toArray(),
                            List.copyOf(var35),
                            var37
                        )
                    );
                }

                if (!MODELS.keySet().equals(IDS)) {
                    throw new IllegalStateException("Incomplete arcane rebuild");
                }
            }
        } catch (Exception var31) {
            throw new ExceptionInInitializerError(var31);
        }
    }

    public static record Bone(String name, String behavior, double[] pivot) {
    }

    public static record Model(
        String id, String anchor, List<ArcaneField.Bone> bones, double[][] vertices, int[] vertexBones, List<ArcaneField.Triangle> triangles, boolean softLight
    ) {
    }

    public static record Reference(double[][] breath, double breathLength, double[][] descent) {
    }

    public static record Triangle(int[] indices, double[][] uv, double alpha, boolean detail) {
    }
}
