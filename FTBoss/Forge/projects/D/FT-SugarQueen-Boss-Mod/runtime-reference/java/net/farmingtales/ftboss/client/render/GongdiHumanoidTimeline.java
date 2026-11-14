/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaterniond
 *  org.joml.Quaterniondc
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package net.farmingtales.ftboss.client.render;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.render.GongdiBasicAttackSampler;
import net.farmingtales.ftboss.client.render.GongdiNaturalArmPose;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class GongdiHumanoidTimeline {
    private GongdiHumanoidTimeline() {
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    public static GongdiBasicAttackSampler.Frame blend(GongdiBasicAttackSampler.Frame frame, GongdiBasicAttackSampler.Frame frame2, double d) {
        d = GongdiHumanoidTimeline.clamp(d);
        LinkedHashMap<String, GongdiBasicAttackSampler.Bone> linkedHashMap = new LinkedHashMap<String, GongdiBasicAttackSampler.Bone>();
        for (Map.Entry<String, GongdiBasicAttackSampler.Bone> entry : frame.bones().entrySet()) {
            GongdiBasicAttackSampler.Bone bone = entry.getValue();
            GongdiBasicAttackSampler.Bone bone2 = frame2.bones().get(entry.getKey());
            double[] dArray = new double[3];
            for (int i = 0; i < 3; ++i) {
                dArray[i] = bone.position()[i] + (bone2.position()[i] - bone.position()[i]) * d;
            }
            Quaterniond quaterniond = new Quaterniond(bone.quaternion()[0], bone.quaternion()[1], bone.quaternion()[2], bone.quaternion()[3]).slerp((Quaterniondc)new Quaterniond(bone2.quaternion()[0], bone2.quaternion()[1], bone2.quaternion()[2], bone2.quaternion()[3]), d);
            linkedHashMap.put(entry.getKey(), new GongdiBasicAttackSampler.Bone(dArray, new double[]{quaterniond.x, quaterniond.y, quaterniond.z, quaterniond.w}));
        }
        return GongdiHumanoidTimeline.constrain(new GongdiBasicAttackSampler.Frame(frame2.time(), Map.copyOf(linkedHashMap)));
    }

    private static Vector3d position(GongdiBasicAttackSampler.Frame frame, String string) {
        return new Vector3d(frame.bones().get(string).position());
    }

    private static Quaterniond rotation(GongdiBasicAttackSampler.Bone bone) {
        double[] dArray = bone.quaternion();
        return new Quaterniond(dArray[0], dArray[1], dArray[2], dArray[3]);
    }

    private static GongdiBasicAttackSampler.Bone bone(Vector3d vector3d, Quaterniond quaterniond) {
        return new GongdiBasicAttackSampler.Bone(new double[]{vector3d.x, vector3d.y, vector3d.z}, new double[]{quaterniond.x, quaterniond.y, quaterniond.z, quaterniond.w});
    }

    private static Vector3d joint(Vector3d vector3d, Vector3d vector3d2, double d, double d2, Vector3d vector3d3) {
        Vector3d vector3d4 = new Vector3d((Vector3dc)vector3d2).sub((Vector3dc)vector3d);
        double d3 = vector3d4.length();
        if (d3 <= 1.0E-5 || d3 > d + d2 + 1.0E-5) {
            throw new IllegalStateException("Blended limb target outside reach");
        }
        vector3d4.div(d3);
        Vector3d vector3d5 = new Vector3d((Vector3dc)vector3d3).sub((Vector3dc)new Vector3d((Vector3dc)vector3d4).mul(vector3d3.dot((Vector3dc)vector3d4))).normalize();
        double d4 = (d * d - d2 * d2 + d3 * d3) / (2.0 * d3);
        double d5 = Math.sqrt(Math.max(0.0, d * d - d4 * d4));
        return new Vector3d((Vector3dc)vector3d).add((Vector3dc)new Vector3d((Vector3dc)vector3d4).mul(d4)).add((Vector3dc)vector3d5.mul(d5));
    }

    private static Quaterniond limb(Vector3d vector3d, Vector3d vector3d2) {
        Vector3d vector3d3 = new Vector3d((Vector3dc)vector3d2).sub((Vector3dc)vector3d).normalize();
        return new Quaterniond().rotationTo(0.0, -1.0, 0.0, vector3d3.x, vector3d3.y, vector3d3.z);
    }

    public static GongdiBasicAttackSampler.Frame constrain(GongdiBasicAttackSampler.Frame frame) {
        Vector3d vector3d;
        LinkedHashMap<String, GongdiBasicAttackSampler.Bone> linkedHashMap = new LinkedHashMap<String, GongdiBasicAttackSampler.Bone>(frame.bones());
        GongdiBasicAttackSampler.Bone bone = frame.bones().get("weapon");
        Quaterniond quaterniond = GongdiHumanoidTimeline.rotation(bone);
        Vector3d vector3d2 = GongdiHumanoidTimeline.position(frame, "weapon");
        Vector3d vector3d3 = new Vector3d((Vector3dc)vector3d2).sub((Vector3dc)(vector3d = GongdiHumanoidTimeline.position(frame, "right_upper_arm")));
        if (vector3d3.length() > 0.53) {
            vector3d2.set((Vector3dc)vector3d).add((Vector3dc)vector3d3.normalize(0.53));
        }
        linkedHashMap.put("weapon", GongdiHumanoidTimeline.bone(vector3d2, quaterniond));
        Quaterniond quaterniond2 = GongdiHumanoidTimeline.rotation(frame.bones().get("torso"));
        Vector3d vector3d4 = quaterniond2.transform(new Vector3d(0.0, -1.0, 0.0));
        for (String string : List.of("right", "left")) {
            Vector3d vector3d5;
            boolean bl = string.equals("right");
            Vector3d vector3d6 = bl ? new Vector3d((Vector3dc)vector3d2) : GongdiHumanoidTimeline.position(frame, "left_wrist");
            Vector3d vector3d7 = new Vector3d((Vector3dc)vector3d6).sub((Vector3dc)(vector3d5 = GongdiHumanoidTimeline.position(frame, string + "_upper_arm")));
            if (vector3d7.length() > 0.53) {
                vector3d6.set((Vector3dc)vector3d5).add((Vector3dc)vector3d7.normalize(0.53));
            }
            Vector3d vector3d8 = GongdiHumanoidTimeline.joint(vector3d5, vector3d6, 0.3125, 0.21875, new Vector3d(bl ? -0.3 : 0.3, -0.2, -0.25));
            Vector3d vector3d9 = new Vector3d((Vector3dc)vector3d8).sub((Vector3dc)vector3d5).normalize();
            Vector3d vector3d10 = new Vector3d((Vector3dc)vector3d6).sub((Vector3dc)vector3d8).normalize();
            Quaterniond quaterniond3 = new Quaterniond().rotationTo((Vector3dc)vector3d4, (Vector3dc)vector3d9).mul((Quaterniondc)quaterniond2);
            Quaterniond quaterniond4 = new Quaterniond().rotationTo((Vector3dc)vector3d9, (Vector3dc)vector3d10).mul((Quaterniondc)quaterniond3);
            linkedHashMap.put(string + "_upper_arm", GongdiHumanoidTimeline.bone(vector3d5, quaterniond3));
            linkedHashMap.put(string + "_forearm", GongdiHumanoidTimeline.bone(vector3d8, quaterniond4));
            linkedHashMap.put(string + "_wrist", GongdiHumanoidTimeline.bone(vector3d6, bl ? quaterniond : GongdiHumanoidTimeline.rotation(frame.bones().get("left_wrist"))));
            Vector3d vector3d11 = GongdiHumanoidTimeline.position(frame, string + "_thigh");
            Vector3d vector3d12 = GongdiHumanoidTimeline.position(frame, string + "_ankle");
            Vector3d vector3d13 = GongdiHumanoidTimeline.joint(vector3d11, vector3d12, 0.375, 0.25, new Vector3d(bl ? -0.15 : 0.15, 0.0, 1.0));
            linkedHashMap.put(string + "_thigh", GongdiHumanoidTimeline.bone(vector3d11, GongdiHumanoidTimeline.limb(vector3d11, vector3d13)));
            linkedHashMap.put(string + "_shin", GongdiHumanoidTimeline.bone(vector3d13, GongdiHumanoidTimeline.limb(vector3d13, vector3d12)));
        }
        return GongdiNaturalArmPose.apply(new GongdiBasicAttackSampler.Frame(frame.time(), Map.copyOf(linkedHashMap)));
    }

    public static double ordinaryProgress(double d) {
        return GongdiHumanoidTimeline.clamp(d <= 0.45 ? d : 0.45 + (d - 0.45) * 1.5);
    }

    /*
     * Unable to fully structure code
     */
    public static GongdiBasicAttackSampler.Frame select(State var0, Cue var1_1, float var2_2) {
        var3_3 = (double)var1_1.limbSwing() * 0.6662 / 6.283185307179586 % 1.0;
        if (var3_3 < 0.0) {
            var3_3 += 1.0;
        }
        var5_4 = GongdiBasicAttackSampler.sample("held_idle", (double)var2_2 / 60.0 % 1.0);
        var6_5 = GongdiBasicAttackSampler.sample("weighted_walk", var3_3);
        var7_6 = GongdiHumanoidTimeline.blend(var5_4, var6_5, GongdiHumanoidTimeline.clamp(var1_1.movement() * 2.0f));
        if (var1_1.casting()) ** GOTO lbl-1000
        var1_1.actionWeight();
        if (0.0f > 0.001f) {
            v0 = true;
        } else lbl-1000:
        // 2 sources

        {
            v0 = false;
        }
        var8_7 = v0;
        var9_8 = var1_1.actionStart();
        if (var8_7 && (!var0.previousActionActive || var9_8 != var0.previousCueStart)) {
            ++var0.attackSequence;
        }
        var0.previousActionActive = var8_7;
        var0.previousCueStart = var9_8;
        var11_9 = var7_6;
        if (var1_1.casting()) {
            var13_10 = var1_1.skill();
            var14_14 = -1;
            switch (var13_10.hashCode()) {
                case 565532716: {
                    if (!var13_10.equals("decimate")) break;
                    var14_14 = 0;
                    break;
                }
                case 700543770: {
                    if (!var13_10.equals("rending_cleave")) break;
                    var14_14 = 1;
                    break;
                }
                case -1209168704: {
                    if (!var13_10.equals("guillotine")) break;
                    var14_14 = 2;
                    break;
                }
                case -1806361452: {
                    if (!var13_10.equals("blood_verdict")) break;
                    var14_14 = 3;
                    break;
                }
                case -1979584895: {
                    if (!var13_10.equals("crippling_strike")) break;
                    var14_14 = 4;
                    break;
                }
                case -1921767033: {
                    if (!var13_10.equals("apprehend")) break;
                    var14_14 = 5;
                }
            }
            switch (var14_14) {
                case 0: 
                case 1: {
                    v1 = "heavy_forehand";
                    break;
                }
                case 2: 
                case 3: {
                    v1 = "downward_chop";
                    break;
                }
                case 4: {
                    v1 = "basic_chop";
                    break;
                }
                case 5: {
                    v1 = "apprehend_pull";
                    break;
                }
                default: {
                    v1 = "crimson_guard";
                }
            }
            var12_17 = v1;
            var13_11 = var1_1.released() != false ? 0.45 + GongdiHumanoidTimeline.clamp(var1_1.progress()) * 0.55 : GongdiHumanoidTimeline.clamp(var1_1.progress()) * 0.3;
            var11_9 = GongdiBasicAttackSampler.sample(var12_17, var13_11);
        } else if (var8_7) {
            var12_17 = "basic_chop";
            var11_9 = GongdiBasicAttackSampler.sample(var12_17, GongdiHumanoidTimeline.ordinaryProgress(var1_1.progress()));
            if (var1_1.movement() > 0.02f) {
                var13_12 = new LinkedHashMap<String, GongdiBasicAttackSampler.Bone>(var11_9.bones());
                for (String var15_20 : var6_5.bones().keySet()) {
                    if (!var15_20.contains("thigh") && !var15_20.contains("shin") && !var15_20.contains("ankle")) continue;
                    var13_12.put(var15_20, var6_5.bones().get(var15_20));
                }
                var11_9 = new GongdiBasicAttackSampler.Frame(var11_9.time(), Map.copyOf(var13_12));
            }
        }
        if (var1_1.hurt() && !var0.previousHurt) {
            var0.interrupted = var0.last == null ? var11_9 : var0.last;
            var0.hurtStart = var2_2;
        }
        var0.previousHurt = var1_1.hurt();
        if (var0.interrupted != null) {
            var12_18 = GongdiHumanoidTimeline.clamp((double)(var2_2 - var0.hurtStart) / 7.0);
            var12_18 = var12_18 * var12_18 * (3.0 - 2.0 * var12_18);
            var11_9 = GongdiHumanoidTimeline.blend(var0.interrupted, var11_9, var12_18);
            if (var12_18 >= 1.0) {
                var0.interrupted = null;
            }
        }
        var0.last = var11_9 = GongdiHumanoidTimeline.constrain(var11_9);
        var12_19 = new LinkedHashMap<String, GongdiBasicAttackSampler.Bone>(var11_9.bones());
        var13_13 = var11_9.bones().get("head");
        var14_16 = GongdiHumanoidTimeline.rotation(var13_13).rotateY(Math.toRadians(-Math.max(-50.0f, Math.min(50.0f, var1_1.headYaw())))).rotateX(Math.toRadians(Math.max(-30.0f, Math.min(30.0f, var1_1.headPitch()))));
        var12_19.put("head", new GongdiBasicAttackSampler.Bone(var13_13.position(), new double[]{var14_16.x, var14_16.y, var14_16.z, var14_16.w}));
        return new GongdiBasicAttackSampler.Frame(var11_9.time(), Map.copyOf(var12_19));
    }

    public record Cue(boolean casting, boolean released, String skill, double progress, float actionAge, float actionWeight, boolean hurt, float movement, float limbSwing, float headYaw, float headPitch, long actionStart) {
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

