package net.farmingtales.ftboss.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public final class GongdiSpellModel {
    private static final Map<String, GongdiSpellModel.Model> MODELS = load();

    private GongdiSpellModel() {
    }

    public static GongdiSpellModel.Model find(String var0) {
        return MODELS.get(var0);
    }

    public static Map<String, GongdiSpellModel.Model> all() {
        return MODELS;
    }

    private static double[] numbers(JsonArray var0) {
        double[] var1 = new double[var0.size()];

        for (int var2 = 0; var2 < var1.length; var2++) {
            var1[var2] = var0.get(var2).getAsDouble();
            if (!Double.isFinite(var1[var2]) || Math.abs(var1[var2]) > 4096.0) {
                throw new IllegalStateException("Nonfinite Gongdi data");
            }
        }

        return var1;
    }

    private static double[][] matrix(JsonArray var0) {
        double[][] var1 = new double[var0.size()][];

        for (int var2 = 0; var2 < var1.length; var2++) {
            var1[var2] = numbers(var0.get(var2).getAsJsonArray());
        }

        return var1;
    }

    private static Map<String, GongdiSpellModel.Model> load() {
        try {
            Map var26;
            try (InputStream var0 = GongdiSpellModel.class.getResourceAsStream("/assets/ftboss/fx/gongdi_spells.json")) {
                if (var0 == null) {
                    throw new IllegalStateException("Gongdi model resource missing");
                }

                JsonObject var1 = JsonParser.parseReader(new InputStreamReader(var0, StandardCharsets.UTF_8)).getAsJsonObject();
                LinkedHashMap var2 = new LinkedHashMap();

                for (JsonElement var4 : var1.getAsJsonArray("models")) {
                    JsonObject var5 = var4.getAsJsonObject();
                    JsonArray var6 = var5.getAsJsonArray("parts");
                    GongdiSpellModel.Part[] var7 = new GongdiSpellModel.Part[var6.size()];

                    for (int var8 = 0; var8 < var7.length; var8++) {
                        JsonObject var9 = var6.get(var8).getAsJsonObject();
                        JsonArray var10 = var9.getAsJsonArray("faces");
                        GongdiSpellModel.Face[] var11 = new GongdiSpellModel.Face[var10.size()];

                        for (int var12 = 0; var12 < var11.length; var12++) {
                            JsonObject var13 = var10.get(var12).getAsJsonObject();
                            var11[var12] = new GongdiSpellModel.Face(
                                matrix(var13.getAsJsonArray("points")),
                                matrix(var13.getAsJsonArray("uv")),
                                var13.get("material").getAsString(),
                                var13.get("alpha").getAsDouble(),
                                var13.get("tint").getAsInt()
                            );
                            if (var11[var12].points().length != 4 || var11[var12].uv().length != 4) {
                                throw new IllegalStateException("Gongdi quad required");
                            }

                            for (double[] var17 : var11[var12].uv()) {
                                for (double var21 : var17) {
                                    if (var21 < 0.0 || var21 > 1.0) {
                                        throw new IllegalStateException("Gongdi UV outside texture");
                                    }
                                }
                            }
                        }

                        LinkedHashMap var28 = new LinkedHashMap();

                        for (Entry var30 : var9.getAsJsonObject("motion").entrySet()) {
                            JsonArray var31 = ((JsonElement)var30.getValue()).getAsJsonArray();
                            GongdiSpellModel.Key[] var32 = new GongdiSpellModel.Key[var31.size()];

                            for (int var33 = 0; var33 < var32.length; var33++) {
                                JsonObject var34 = var31.get(var33).getAsJsonObject();
                                var32[var33] = new GongdiSpellModel.Key(
                                    var34.get("time").getAsDouble(),
                                    numbers(var34.getAsJsonArray("position")),
                                    numbers(var34.getAsJsonArray("rotation")),
                                    numbers(var34.getAsJsonArray("scale")),
                                    var34.get("alpha").getAsDouble(),
                                    var34.get("easing").getAsString()
                                );
                            }

                            var28.put((String)var30.getKey(), var32);
                        }

                        var7[var8] = new GongdiSpellModel.Part(
                            var9.get("name").getAsString(),
                            numbers(var9.getAsJsonArray("pivot")),
                            var11,
                            Map.copyOf(var28),
                            var9.has("sideSign") ? var9.get("sideSign").getAsInt() : 0,
                            var9.has("telegraph") && var9.get("telegraph").getAsBoolean()
                        );
                    }

                    String var27 = var5.get("id").getAsString();
                    var2.put(var27, new GongdiSpellModel.Model(var27, var5.get("anchor").getAsString(), var7, var5.get("span").getAsDouble()));
                }

                if (!var2.keySet().equals(Set.of("rending_cleave", "crimson_oath", "blood_verdict"))) {
                    throw new IllegalStateException("Gongdi-only scope mismatch");
                }

                var26 = Map.copyOf(var2);
            }

            return var26;
        } catch (Exception var25) {
            throw new IllegalStateException("Cannot load independent Gongdi models", var25);
        }
    }

    public static GongdiSpellModel.State sample(GongdiSpellModel.Part var0, String var1, double var2) {
        GongdiSpellModel.Key[] var4 = var0.motion().getOrDefault(var1, var0.motion().get("RELEASE"));
        GongdiSpellModel.Key var5 = var4[0];
        GongdiSpellModel.Key var6 = var4[var4.length - 1];

        for (int var7 = 1; var7 < var4.length; var7++) {
            if (var2 <= var4[var7].time()) {
                var5 = var4[var7 - 1];
                var6 = var4[var7];
                break;
            }
        }

        double var13 = Math.max(0.0, Math.min(1.0, (var2 - var5.time()) / Math.max(1.0E-5, var6.time() - var5.time())));
        String var9 = var6.easing();

        var13 = switch (var9) {
            case "easeInOutSine" -> (1.0 - Math.cos(Math.PI * var13)) / 2.0;
            case "easeOutQuad" -> 1.0 - (1.0 - var13) * (1.0 - var13);
            case "easeInQuad" -> var13 * var13;
            default -> var13;
        };
        double[] var15 = new double[3];
        double[] var16 = new double[3];
        double[] var11 = new double[3];

        for (int var12 = 0; var12 < 3; var12++) {
            var15[var12] = var5.position()[var12] + (var6.position()[var12] - var5.position()[var12]) * var13;
            var16[var12] = var5.rotation()[var12] + (var6.rotation()[var12] - var5.rotation()[var12]) * var13;
            var11[var12] = var5.scale()[var12] + (var6.scale()[var12] - var5.scale()[var12]) * var13;
        }

        return new GongdiSpellModel.State(var15, var16, var11, var5.alpha() + (var6.alpha() - var5.alpha()) * var13);
    }

    public static Quaterniond rotation(GongdiSpellModel.State var0) {
        return new Quaterniond()
            .rotationZ(Math.toRadians(var0.rotation()[2]))
            .rotateY(Math.toRadians(var0.rotation()[1]))
            .rotateX(Math.toRadians(var0.rotation()[0]));
    }

    public static double[] pose(double[] var0, GongdiSpellModel.Part var1, GongdiSpellModel.State var2) {
        return pose(var0, var1, var2, rotation(var2));
    }

    public static double[] pose(double[] var0, GongdiSpellModel.Part var1, GongdiSpellModel.State var2, Quaterniond var3) {
        Vector3d var4 = new Vector3d(
            (var0[0] - var1.pivot()[0]) * var2.scale()[0], (var0[1] - var1.pivot()[1]) * var2.scale()[1], (var0[2] - var1.pivot()[2]) * var2.scale()[2]
        );
        var3.transform(var4);
        return new double[]{
            var4.x + var1.pivot()[0] + var2.position()[0], var4.y + var1.pivot()[1] + var2.position()[1], var4.z + var1.pivot()[2] + var2.position()[2]
        };
    }

    public static record Face(double[][] points, double[][] uv, String material, double alpha, int tint) {
    }

    public static record Key(double time, double[] position, double[] rotation, double[] scale, double alpha, String easing) {
    }

    public static record Model(String id, String anchor, GongdiSpellModel.Part[] parts, double span) {
    }

    public static record Part(
        String name, double[] pivot, GongdiSpellModel.Face[] faces, Map<String, GongdiSpellModel.Key[]> motion, int sideSign, boolean telegraph
    ) {
    }

    public static record State(double[] position, double[] rotation, double[] scale, double alpha) {
    }
}
