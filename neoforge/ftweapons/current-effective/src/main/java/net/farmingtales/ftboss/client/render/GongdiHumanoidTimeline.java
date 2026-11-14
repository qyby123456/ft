package net.farmingtales.ftboss.client.render;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public final class GongdiHumanoidTimeline {
    private GongdiHumanoidTimeline() {
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, var0));
    }

    public static GongdiBasicAttackSampler.Frame blend(GongdiBasicAttackSampler.Frame var0, GongdiBasicAttackSampler.Frame var1, double var2) {
        var2 = clamp(var2);
        LinkedHashMap var4 = new LinkedHashMap();

        for (Entry var6 : var0.bones().entrySet()) {
            GongdiBasicAttackSampler.Bone var7 = (GongdiBasicAttackSampler.Bone)var6.getValue();
            GongdiBasicAttackSampler.Bone var8 = var1.bones().get(var6.getKey());
            double[] var9 = new double[3];

            for (int var10 = 0; var10 < 3; var10++) {
                var9[var10] = var7.position()[var10] + (var8.position()[var10] - var7.position()[var10]) * var2;
            }

            Quaterniond var12 = new Quaterniond(var7.quaternion()[0], var7.quaternion()[1], var7.quaternion()[2], var7.quaternion()[3])
                .slerp(new Quaterniond(var8.quaternion()[0], var8.quaternion()[1], var8.quaternion()[2], var8.quaternion()[3]), var2);
            var4.put((String)var6.getKey(), new GongdiBasicAttackSampler.Bone(var9, new double[]{var12.x, var12.y, var12.z, var12.w}));
        }

        return constrain(new GongdiBasicAttackSampler.Frame(var1.time(), Map.copyOf(var4)));
    }

    private static Vector3d position(GongdiBasicAttackSampler.Frame var0, String var1) {
        return new Vector3d(var0.bones().get(var1).position());
    }

    private static Quaterniond rotation(GongdiBasicAttackSampler.Bone var0) {
        double[] var1 = var0.quaternion();
        return new Quaterniond(var1[0], var1[1], var1[2], var1[3]);
    }

    private static GongdiBasicAttackSampler.Bone bone(Vector3d var0, Quaterniond var1) {
        return new GongdiBasicAttackSampler.Bone(new double[]{var0.x, var0.y, var0.z}, new double[]{var1.x, var1.y, var1.z, var1.w});
    }

    private static Vector3d joint(Vector3d var0, Vector3d var1, double var2, double var4, Vector3d var6) {
        Vector3d var7 = new Vector3d(var1).sub(var0);
        double var8 = var7.length();
        if (!(var8 <= 1.0E-5) && !(var8 > var2 + var4 + 1.0E-5)) {
            var7.div(var8);
            Vector3d var10 = new Vector3d(var6).sub(new Vector3d(var7).mul(var6.dot(var7))).normalize();
            double var11 = (var2 * var2 - var4 * var4 + var8 * var8) / (2.0 * var8);
            double var13 = Math.sqrt(Math.max(0.0, var2 * var2 - var11 * var11));
            return new Vector3d(var0).add(new Vector3d(var7).mul(var11)).add(var10.mul(var13));
        } else {
            throw new IllegalStateException("Blended limb target outside reach");
        }
    }

    private static Quaterniond limb(Vector3d var0, Vector3d var1) {
        Vector3d var2 = new Vector3d(var1).sub(var0).normalize();
        return new Quaterniond().rotationTo(0.0, -1.0, 0.0, var2.x, var2.y, var2.z);
    }

    public static GongdiBasicAttackSampler.Frame constrain(GongdiBasicAttackSampler.Frame var0) {
        LinkedHashMap var1 = new LinkedHashMap<>(var0.bones());
        GongdiBasicAttackSampler.Bone var2 = var0.bones().get("weapon");
        Quaterniond var3 = rotation(var2);
        Vector3d var4 = position(var0, "weapon");
        Vector3d var5 = position(var0, "right_upper_arm");
        Vector3d var6 = new Vector3d(var4).sub(var5);
        if (var6.length() > 0.53) {
            var4.set(var5).add(var6.normalize(0.53));
        }

        var1.put("weapon", bone(var4, var3));
        Quaterniond var7 = rotation(var0.bones().get("torso"));
        Vector3d var8 = var7.transform(new Vector3d(0.0, -1.0, 0.0));

        for (String var10 : List.of("right", "left")) {
            boolean var11 = var10.equals("right");
            Vector3d var12 = position(var0, var10 + "_upper_arm");
            Vector3d var13 = var11 ? new Vector3d(var4) : position(var0, "left_wrist");
            Vector3d var14 = new Vector3d(var13).sub(var12);
            if (var14.length() > 0.53) {
                var13.set(var12).add(var14.normalize(0.53));
            }

            Vector3d var15 = joint(var12, var13, 0.3125, 0.21875, new Vector3d(var11 ? -0.3 : 0.3, -0.2, -0.25));
            Vector3d var16 = new Vector3d(var15).sub(var12).normalize();
            Vector3d var17 = new Vector3d(var13).sub(var15).normalize();
            Quaterniond var18 = new Quaterniond().rotationTo(var8, var16).mul(var7);
            Quaterniond var19 = new Quaterniond().rotationTo(var16, var17).mul(var18);
            var1.put(var10 + "_upper_arm", bone(var12, var18));
            var1.put(var10 + "_forearm", bone(var15, var19));
            var1.put(var10 + "_wrist", bone(var13, var11 ? var3 : rotation(var0.bones().get("left_wrist"))));
            Vector3d var20 = position(var0, var10 + "_thigh");
            Vector3d var21 = position(var0, var10 + "_ankle");
            Vector3d var22 = joint(var20, var21, 0.375, 0.25, new Vector3d(var11 ? -0.15 : 0.15, 0.0, 1.0));
            var1.put(var10 + "_thigh", bone(var20, limb(var20, var22)));
            var1.put(var10 + "_shin", bone(var22, limb(var22, var21)));
        }

        return GongdiNaturalArmPose.apply(new GongdiBasicAttackSampler.Frame(var0.time(), Map.copyOf(var1)));
    }

    public static double ordinaryProgress(double var0) {
        return clamp(var0 <= 0.45 ? var0 : 0.45 + (var0 - 0.45) * 1.5);
    }

    public static GongdiBasicAttackSampler.Frame select(GongdiHumanoidTimeline.State var0, GongdiHumanoidTimeline.Cue var1, float var2) {
        double var3 = (double)var1.limbSwing() * 0.6662 / (Math.PI * 2) % 1.0;
        if (var3 < 0.0) {
            var3++;
        }

        GongdiBasicAttackSampler.Frame var6;
        GongdiBasicAttackSampler.Frame var7;
        boolean var10000;
        label104: {
            GongdiBasicAttackSampler.Frame var5 = GongdiBasicAttackSampler.sample("held_idle", (double)var2 / 60.0 % 1.0);
            var6 = GongdiBasicAttackSampler.sample("weighted_walk", var3);
            var7 = blend(var5, var6, clamp((double)(var1.movement() * 2.0F)));
            if (!var1.casting()) {
                var1.actionWeight();
                if (0.0F > 0.001F) {
                    var10000 = true;
                    break label104;
                }
            }

            var10000 = false;
        }

        boolean var8 = var10000;
        long var9 = var1.actionStart();
        if (var8 && (!var0.previousActionActive || var9 != var0.previousCueStart)) {
            var0.attackSequence++;
        }

        var0.previousActionActive = var8;
        var0.previousCueStart = var9;
        GongdiBasicAttackSampler.Frame var11 = var7;
        if (var1.casting()) {
            String var13 = var1.skill();

            String var12 = switch (var13) {
                case "decimate", "rending_cleave" -> "heavy_forehand";
                case "guillotine", "blood_verdict" -> "downward_chop";
                case "crippling_strike" -> "basic_chop";
                case "apprehend" -> "apprehend_pull";
                default -> "crimson_guard";
            };
            double var21 = var1.released() ? 0.45 + clamp(var1.progress()) * 0.55 : clamp(var1.progress()) * 0.3;
            var11 = GongdiBasicAttackSampler.sample(var12, var21);
        } else if (var8) {
            String var17 = "basic_chop";
            var11 = GongdiBasicAttackSampler.sample(var17, ordinaryProgress(var1.progress()));
            if (var1.movement() > 0.02F) {
                LinkedHashMap var22 = new LinkedHashMap<>(var11.bones());

                for (String var15 : var6.bones().keySet()) {
                    if (var15.contains("thigh") || var15.contains("shin") || var15.contains("ankle")) {
                        var22.put(var15, var6.bones().get(var15));
                    }
                }

                var11 = new GongdiBasicAttackSampler.Frame(var11.time(), Map.copyOf(var22));
            }
        }

        if (var1.hurt() && !var0.previousHurt) {
            var0.interrupted = var0.last == null ? var11 : var0.last;
            var0.hurtStart = var2;
        }

        var0.previousHurt = var1.hurt();
        if (var0.interrupted != null) {
            double var18 = clamp((double)(var2 - var0.hurtStart) / 7.0);
            var18 = var18 * var18 * (3.0 - 2.0 * var18);
            var11 = blend(var0.interrupted, var11, var18);
            if (var18 >= 1.0) {
                var0.interrupted = null;
            }
        }

        var11 = constrain(var11);
        var0.last = var11;
        LinkedHashMap var20 = new LinkedHashMap<>(var11.bones());
        GongdiBasicAttackSampler.Bone var23 = var11.bones().get("head");
        Quaterniond var25 = rotation(var23)
            .rotateY(Math.toRadians((double)(-Math.max(-50.0F, Math.min(50.0F, var1.headYaw())))))
            .rotateX(Math.toRadians((double)Math.max(-30.0F, Math.min(30.0F, var1.headPitch()))));
        var20.put("head", new GongdiBasicAttackSampler.Bone(var23.position(), new double[]{var25.x, var25.y, var25.z, var25.w}));
        return new GongdiBasicAttackSampler.Frame(var11.time(), Map.copyOf(var20));
    }

    public static record Cue(
        boolean casting,
        boolean released,
        String skill,
        double progress,
        float actionAge,
        float actionWeight,
        boolean hurt,
        float movement,
        float limbSwing,
        float headYaw,
        float headPitch,
        long actionStart
    ) {
    }

    public static final class State {
        private long previousCueStart = Long.MIN_VALUE;
        private boolean previousActionActive;
        private boolean previousHurt;
        private int attackSequence;
        private float hurtStart;
        private GongdiBasicAttackSampler.Frame last;
        private GongdiBasicAttackSampler.Frame interrupted;

        public int attackSequence() {
            return this.attackSequence;
        }
    }
}
