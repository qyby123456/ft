package net.farmingtales.ftboss.client.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public final class GongdiNaturalArmPose {
    private static Vector3d position(GongdiBasicAttackSampler.Bone var0) {
        return new Vector3d(var0.position());
    }

    private static Quaterniond rotation(GongdiBasicAttackSampler.Bone var0) {
        return new Quaterniond(var0.quaternion()[0], var0.quaternion()[1], var0.quaternion()[2], var0.quaternion()[3]).normalize();
    }

    private static GongdiBasicAttackSampler.Bone bone(Vector3d var0, Quaterniond var1) {
        var1.normalize();
        return new GongdiBasicAttackSampler.Bone(new double[]{var0.x, var0.y, var0.z}, new double[]{var1.x, var1.y, var1.z, var1.w});
    }

    private static Vector3d elbow(Vector3d var0, Vector3d var1, Quaterniond var2, double var3, double var5, boolean var7) {
        Quaterniond var8 = new Quaterniond(var2).invert();
        Vector3d var9 = new Vector3d(var1).sub(var0).rotate(var8);
        double var10 = var9.length();
        if (!(var10 <= Math.abs(var3 - var5) + 1.0E-7) && !(var10 > var3 + var5 + 1.0E-7)) {
            Vector3d var12 = new Vector3d(var9).div(var10);
            double var13 = (var3 * var3 - var5 * var5 + var10 * var10) / (2.0 * var10);
            double var15 = Math.sqrt(Math.max(0.0, var3 * var3 - var13 * var13));
            Vector3d var17 = new Vector3d(var12).mul(var13);
            Vector3d var18 = new Vector3d(1.0, 0.0, 0.0).sub(new Vector3d(var12).mul(var12.x));
            double var19 = var18.length();
            if (!(var19 < 1.0E-7) && !(var15 < 1.0E-7)) {
                var18.div(var19);
                Vector3d var21 = new Vector3d(var12).cross(var18).normalize();
                double var22 = var7 ? -0.015 : 0.008;
                double var24 = Math.max(-1.0, Math.min(1.0, (var22 - var17.x) / (var15 * var19)));
                double var26 = Math.sqrt(Math.max(0.0, 1.0 - var24 * var24));
                Vector3d var28 = new Vector3d(var17).add(new Vector3d(var18).mul(var15 * var24));
                Vector3d var29 = new Vector3d(var28).add(new Vector3d(var21).mul(var15 * var26));
                Vector3d var30 = new Vector3d(var28).sub(new Vector3d(var21).mul(var15 * var26));
                Vector3d var31 = var7 ? (var29.y <= var30.y ? var29 : var30) : (var29.z <= var30.z ? var29 : var30);
                return var31.rotate(var2).add(var0);
            } else {
                return var17.rotate(var2).add(var0);
            }
        } else {
            throw new IllegalStateException("Natural arm target outside authored reach");
        }
    }

    public static GongdiBasicAttackSampler.Frame rebuild(GongdiBasicAttackSampler.Frame var0, double var1, double var3) {
        LinkedHashMap var5 = new LinkedHashMap<>(var0.bones());
        Quaterniond var6 = rotation((GongdiBasicAttackSampler.Bone)var5.get("torso"));
        Vector3d var7 = new Vector3d(0.0, -1.0, 0.0).rotate(var6);

        for (String var11 : new String[]{"right", "left"}) {
            boolean var12 = var11.equals("right");
            Vector3d var13 = position((GongdiBasicAttackSampler.Bone)var5.get(var11 + "_upper_arm"));
            Vector3d var14 = position((GongdiBasicAttackSampler.Bone)var5.get(var12 ? "weapon" : "left_wrist"));
            Vector3d var15 = elbow(var13, var14, var6, var1, var3, var12);
            Vector3d var16 = new Vector3d(var15).sub(var13).normalize();
            Vector3d var17 = new Vector3d(var14).sub(var15).normalize();
            Quaterniond var18 = new Quaterniond().rotationTo(var7, var16).mul(var6).normalize();
            Quaterniond var19 = new Quaterniond().rotationTo(var16, var17).mul(var18).normalize();
            var5.put(var11 + "_upper_arm", bone(var13, var18));
            var5.put(var11 + "_forearm", bone(var15, var19));
            var5.put(var11 + "_wrist", bone(var14, var19));
        }

        return new GongdiBasicAttackSampler.Frame(var0.time(), Map.copyOf(var5));
    }

    public static GongdiBasicAttackSampler.Frame apply(GongdiBasicAttackSampler.Frame var0) {
        return rebuild(var0, GongdiNaturalArmPose.Lengths.UPPER, GongdiNaturalArmPose.Lengths.LOWER);
    }

    private GongdiNaturalArmPose() {
    }

    private static final class Lengths {
        static final double UPPER;
        static final double LOWER;

        static {
            try {
                try (InputStream var0 = GongdiNaturalArmPose.class.getResourceAsStream("/assets/ftboss/fx/gongdi_basic_attacks.json")) {
                    if (var0 == null) {
                        throw new IllegalStateException("Missing Gongdi limb rest geometry");
                    }

                    JsonObject var1 = JsonParser.parseReader(new InputStreamReader(var0, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("rest");
                    UPPER = var1.getAsJsonArray("right_upper_arm").get(1).getAsDouble() - var1.getAsJsonArray("right_forearm").get(1).getAsDouble();
                    LOWER = var1.getAsJsonArray("right_forearm").get(1).getAsDouble() - var1.getAsJsonArray("right_wrist").get(1).getAsDouble();
                    if (Math.abs(UPPER - 0.3125) > 1.0E-8 || LOWER < 0.2 || LOWER > 0.32) {
                        throw new IllegalStateException("Unknown Gongdi limb dimensions");
                    }
                }
            } catch (Exception var5) {
                throw new IllegalStateException("Cannot resolve Gongdi limb rest geometry", var5);
            }
        }
    }
}
