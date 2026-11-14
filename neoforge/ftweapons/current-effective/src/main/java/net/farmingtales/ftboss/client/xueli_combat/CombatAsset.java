package net.farmingtales.ftboss.client.xueli_combat;

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

public final class CombatAsset {
    public static final Set<String> IDS = Set.of(
        "maid_guard",
        "guard_thread",
        "guard_break",
        "guard_hit",
        "pan_charge",
        "slam_charge",
        "pan_sweep",
        "roll_wake",
        "crush_lock",
        "slam_impact",
        "clone_fuse",
        "clone_burst",
        "stun_haze"
    );

    private CombatAsset() {
    }

    public static boolean centerRay(String var0) {
        if (var0.equals("lance:1") || var0.equals("afterimage:1")) {
            return true;
        } else if (!var0.startsWith("trail:")) {
            return false;
        } else {
            int var1 = Integer.parseInt(var0.substring(6));
            return var1 >= 7 && var1 < 14;
        }
    }

    private static double[] vector(JsonArray var0, int var1) {
        if (var0.size() != var1) {
            throw new IllegalArgumentException("Invalid vector length");
        } else {
            double[] var2 = new double[var1];

            for (int var3 = 0; var3 < var1; var3++) {
                var2[var3] = var0.get(var3).getAsDouble();
                if (!Double.isFinite(var2[var3]) || Math.abs(var2[var3]) > 32.0) {
                    throw new IllegalArgumentException("Unsafe coordinate");
                }
            }

            return var2;
        }
    }

    public static Map<String, CombatAsset.Effect> load() {
        Map<String, CombatAsset.Effect> var0 = new LinkedHashMap<>();

        for (String var2 : IDS) {
            try (InputStream var3 = CombatAsset.class.getResourceAsStream("/assets/ftboss/xueli_combat/" + var2 + ".json")) {
                if (var3 == null) {
                    throw new IllegalStateException("Missing fresh asset: " + var2);
                }

                JsonObject var4 = JsonParser.parseReader(new InputStreamReader(var3, StandardCharsets.UTF_8)).getAsJsonObject();
                List<CombatAsset.Bone> var5 = new ArrayList<>();

                for (JsonElement var7 : var4.getAsJsonArray("bones")) {
                    JsonObject var8 = var7.getAsJsonObject();
                    var5.add(new CombatAsset.Bone(var8.get("name").getAsString(), var8.get("motion").getAsString(), vector(var8.getAsJsonArray("pivot"), 3)));
                }

                List<CombatAsset.Face> var19 = new ArrayList<>();

                for (JsonElement var22 : var4.getAsJsonArray("faces")) {
                    JsonObject var9 = var22.getAsJsonObject();
                    int var10 = var9.getAsJsonArray("points").size();
                    int var11 = var9.get("bone").getAsInt();
                    if ((var10 == 3 || var10 == 4) && var11 >= 0 && var11 < var5.size()) {
                        double[][] var12 = new double[var10][];
                        double[][] var13 = new double[var10][];

                        for (int var14 = 0; var14 < var10; var14++) {
                            var12[var14] = vector(var9.getAsJsonArray("points").get(var14).getAsJsonArray(), 3);
                            var13[var14] = vector(var9.getAsJsonArray("uv").get(var14).getAsJsonArray(), 2);
                            if (var13[var14][0] < 0.0 || var13[var14][0] > 1.0 || var13[var14][1] < 0.0 || var13[var14][1] > 1.0) {
                                throw new IllegalArgumentException("Out-of-atlas coordinates");
                            }
                        }

                        double var26 = var9.get("alpha").getAsDouble();
                        if (!(var26 <= 0.0) && !(var26 > 1.0)) {
                            var19.add(new CombatAsset.Face(var12, var13, var26, var11, var9.get("light").getAsBoolean()));
                            continue;
                        }

                        throw new IllegalArgumentException("Invalid material opacity");
                    }

                    throw new IllegalArgumentException("Invalid fresh rig topology");
                }

                JsonObject var21 = var4.getAsJsonObject("layout");
                String var23 = var21.get("anchor").getAsString();
                if (!Set.of("GROUND", "SELF", "FORWARD").contains(var23) || var19.size() > 400 || var5.size() > 64) {
                    throw new IllegalArgumentException("Unbounded effect");
                }

                double var24 = var4.get("scale").getAsDouble();
                double var25 = var21.has("innerRadius") ? var21.get("innerRadius").getAsDouble() : 1.0;
                if (!Double.isFinite(var24) || var24 <= 0.0 || !Double.isFinite(var25) || var25 <= 0.0) {
                    throw new IllegalArgumentException("Invalid placement scale");
                }

                var0.put(
                    var2, new CombatAsset.Effect(var2, List.copyOf(var5), List.copyOf(var19), var23, var24, vector(var4.getAsJsonArray("offset"), 3), var25)
                );
            } catch (Exception var18) {
                throw new IllegalStateException("Cannot load new arcane effect " + var2, var18);
            }
        }

        return Map.copyOf(var0);
    }

    public static record Bone(String name, String track, double[] pivot) {
    }

    public static record Effect(
        String id, List<CombatAsset.Bone> bones, List<CombatAsset.Face> faces, String anchor, double scale, double[] offset, double innerRadius
    ) {
        public double radiusScale(double var1) {
            return this.anchor.equals("SELF") ? Math.max(1.0, (var1 / Math.sqrt(2.0) + 0.33) / (this.innerRadius * this.scale)) : 1.0;
        }
    }

    public static record Face(double[][] positions, double[][] uv, double alpha, int bone, boolean light) {
    }
}
