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
import java.util.Map.Entry;
import net.farmingtales.ftboss.util.EasterCombatMath;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public final class GongdiBasicAttackSampler {
    private GongdiBasicAttackSampler() {
    }

    private static double[] numbers(JsonArray var0, int var1) {
        if (var0.size() != var1) {
            throw new IllegalArgumentException("Invalid bone channel width");
        } else {
            double[] var2 = new double[var1];

            for (int var3 = 0; var3 < var1; var3++) {
                var2[var3] = var0.get(var3).getAsDouble();
                if (!Double.isFinite(var2[var3])) {
                    throw new IllegalArgumentException("Nonfinite rig coordinate");
                }
            }

            return var2;
        }
    }

    private static Map<String, GongdiBasicAttackSampler.Frame[]> load() {
        try {
            Map var21;
            try (InputStream var0 = GongdiBasicAttackSampler.class.getResourceAsStream("/assets/ftboss/fx/gongdi_basic_attacks.json")) {
                if (var0 == null) {
                    throw new IllegalStateException("Gongdi basic rig resource missing");
                }

                JsonObject var1 = JsonParser.parseReader(new InputStreamReader(var0, StandardCharsets.UTF_8)).getAsJsonObject();
                LinkedHashMap var2 = new LinkedHashMap();

                for (JsonElement var4 : var1.getAsJsonArray("clips")) {
                    JsonObject var5 = var4.getAsJsonObject();
                    JsonArray var6 = var5.getAsJsonArray("frames");
                    GongdiBasicAttackSampler.Frame[] var7 = new GongdiBasicAttackSampler.Frame[var6.size()];
                    double var8 = -1.0;

                    for (int var10 = 0; var10 < var7.length; var10++) {
                        JsonObject var11 = var6.get(var10).getAsJsonObject();
                        double var12 = var11.get("time").getAsDouble();
                        if (var12 <= var8 || var12 < 0.0 || var12 > 1.0) {
                            throw new IllegalArgumentException("Invalid rig timeline");
                        }

                        var8 = var12;
                        LinkedHashMap var14 = new LinkedHashMap();

                        for (Entry var16 : var11.getAsJsonObject("bones").entrySet()) {
                            JsonObject var17 = ((JsonElement)var16.getValue()).getAsJsonObject();
                            var14.put(
                                (String)var16.getKey(),
                                new GongdiBasicAttackSampler.Bone(numbers(var17.getAsJsonArray("position"), 3), numbers(var17.getAsJsonArray("quaternion"), 4))
                            );
                        }

                        var7[var10] = new GongdiBasicAttackSampler.Frame(var12, Map.copyOf(var14));
                    }

                    var2.put(var5.get("id").getAsString(), var7);
                }

                var21 = Map.copyOf(var2);
            }

            return var21;
        } catch (Exception var20) {
            throw new IllegalStateException("Cannot load Gongdi basic attack prototype", var20);
        }
    }

    public static GongdiBasicAttackSampler.Frame sample(String var0, double var1) {
        GongdiBasicAttackSampler.Frame[] var3 = GongdiBasicAttackSampler.Library.CLIPS
            .getOrDefault(var0, GongdiBasicAttackSampler.Library.CLIPS.get("held_idle"));
        if (!Double.isFinite(var1)) {
            var1 = 0.0;
        }

        var1 = Math.max(0.0, Math.min(1.0, var1));
        int var4 = 1;

        while (var4 < var3.length - 1 && var1 > var3[var4].time()) {
            var4++;
        }

        GongdiBasicAttackSampler.Frame var5 = var3[var4 - 1];
        GongdiBasicAttackSampler.Frame var6 = var3[var4];
        double var7 = (var1 - var5.time()) / Math.max(1.0E-6, var6.time() - var5.time());
        LinkedHashMap var9 = new LinkedHashMap();

        for (Entry var11 : var5.bones().entrySet()) {
            GongdiBasicAttackSampler.Bone var12 = (GongdiBasicAttackSampler.Bone)var11.getValue();
            GongdiBasicAttackSampler.Bone var13 = var6.bones().get(var11.getKey());
            double[] var14 = new double[3];

            for (int var15 = 0; var15 < 3; var15++) {
                var14[var15] = var12.position()[var15] + (var13.position()[var15] - var12.position()[var15]) * var7;
            }

            Quaterniond var17 = new Quaterniond(var12.quaternion()[0], var12.quaternion()[1], var12.quaternion()[2], var12.quaternion()[3])
                .slerp(new Quaterniond(var13.quaternion()[0], var13.quaternion()[1], var13.quaternion()[2], var13.quaternion()[3]), var7);
            var9.put((String)var11.getKey(), new GongdiBasicAttackSampler.Bone(var14, new double[]{var17.x, var17.y, var17.z, var17.w}));
        }

        return new GongdiBasicAttackSampler.Frame(var1, Map.copyOf(var9));
    }

    public static GongdiBasicAttackSampler.Frame fromExistingImpactCue(String var0, float var1, int var2, boolean var3) {
        return sample(var0, (double)EasterCombatMath.progress(var1, var2, var3));
    }

    public static double[] transform(double[] var0, GongdiBasicAttackSampler.Bone var1) {
        Vector3d var2 = new Vector3d(var0[0], var0[1], var0[2]);
        new Quaterniond(var1.quaternion()[0], var1.quaternion()[1], var1.quaternion()[2], var1.quaternion()[3]).transform(var2);
        return new double[]{var2.x + var1.position()[0], var2.y + var1.position()[1], var2.z + var1.position()[2]};
    }

    public static record Bone(double[] position, double[] quaternion) {
    }

    public static record Frame(double time, Map<String, GongdiBasicAttackSampler.Bone> bones) {
    }

    private static final class Library {
        private static final Map<String, GongdiBasicAttackSampler.Frame[]> CLIPS = GongdiBasicAttackSampler.load();
    }
}
