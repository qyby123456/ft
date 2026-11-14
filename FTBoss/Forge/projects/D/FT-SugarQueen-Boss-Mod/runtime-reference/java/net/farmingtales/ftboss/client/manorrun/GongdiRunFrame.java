/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaterniond
 */
package net.farmingtales.ftboss.client.manorrun;

import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.client.manorrun.ManorRunAnimation;
import net.farmingtales.ftboss.client.manorrun.RunCycle;
import net.farmingtales.ftboss.client.render.GongdiBasicAttackSampler;
import net.farmingtales.ftboss.client.render.GongdiHumanoidTimeline;
import net.farmingtales.ftboss.client.render.GongdiNaturalArmPose;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import org.joml.Quaterniond;

public final class GongdiRunFrame {
    public static GongdiBasicAttackSampler.Frame apply(GongdiBasicAttackSampler.Frame frame, EasterBossEntity easterBossEntity, float f) {
        float f2 = (float)RunCycle.clamp(f - (float)easterBossEntity.f_19797_, 0.0, 1.0);
        RunCycle.State state = ManorRunAnimation.prepare(easterBossEntity, f);
        if (state == null || ManorRunAnimation.blocked(easterBossEntity, f2)) {
            GongdiBasicAttackSampler.Frame frame2 = frame;
            if (easterBossEntity.getVariant() == 3) {
                frame2 = GongdiNaturalArmPose.apply(frame2);
            }
            return frame2;
        }
        double d = state.previousWeight + (state.weight - state.previousWeight) * (double)f2;
        double d2 = state.previousPhase + (state.phase - state.previousPhase) * (double)f2;
        if (d < 1.0E-4) {
            GongdiBasicAttackSampler.Frame frame3 = frame;
            if (easterBossEntity.getVariant() == 3) {
                frame3 = GongdiNaturalArmPose.apply(frame3);
            }
            return frame3;
        }
        GongdiBasicAttackSampler.Frame frame4 = GongdiRunFrame.transform(frame, state.speed, d2, d);
        if (easterBossEntity.getVariant() == 3) {
            frame4 = GongdiNaturalArmPose.apply(frame4);
        }
        return frame4;
    }

    public static GongdiBasicAttackSampler.Frame locomotionBase(GongdiBasicAttackSampler.Frame frame, double d, double d2) {
        double d3 = d2 / (Math.PI * 2);
        d3 -= Math.floor(d3);
        GongdiBasicAttackSampler.Frame frame2 = GongdiBasicAttackSampler.sample("weighted_walk", d3);
        double d4 = RunCycle.clamp(d / 0.145, 0.0, 1.0);
        GongdiBasicAttackSampler.Frame frame3 = GongdiHumanoidTimeline.blend(Library.IDLE, frame2, d4);
        LinkedHashMap<String, GongdiBasicAttackSampler.Bone> linkedHashMap = new LinkedHashMap<String, GongdiBasicAttackSampler.Bone>(frame3.bones());
        GongdiBasicAttackSampler.Bone bone = linkedHashMap.get("head");
        linkedHashMap.put("head", new GongdiBasicAttackSampler.Bone(bone.position(), frame.bones().get("head").quaternion()));
        GongdiBasicAttackSampler.Bone bone2 = new GongdiBasicAttackSampler.Bone(new double[]{-0.35, 0.73, 0.17}, linkedHashMap.get("weapon").quaternion());
        linkedHashMap.put("weapon", bone2);
        linkedHashMap.put("right_wrist", bone2);
        linkedHashMap.put("left_wrist", new GongdiBasicAttackSampler.Bone(new double[]{0.34, 0.71, 0.03 - 0.12 * d4 * Math.sin(d2 - 0.2)}, linkedHashMap.get("left_wrist").quaternion()));
        return GongdiHumanoidTimeline.constrain(new GongdiBasicAttackSampler.Frame(frame3.time(), Map.copyOf(linkedHashMap)));
    }

    public static GongdiBasicAttackSampler.Frame transform(GongdiBasicAttackSampler.Frame frame, double d, double d2, double d3) {
        GongdiBasicAttackSampler.Frame frame2 = Library.IDLE;
        LinkedHashMap<String, GongdiBasicAttackSampler.Bone> linkedHashMap = new LinkedHashMap<String, GongdiBasicAttackSampler.Bone>(frame2.bones());
        linkedHashMap.put("head", new GongdiBasicAttackSampler.Bone(frame2.bones().get("head").position(), frame.bones().get("head").quaternion()));
        double d4 = RunCycle.clamp((d - 0.145) / 0.33499999999999996, 0.0, 1.0);
        double d5 = Math.sin(d2 - 0.2);
        double[] dArray = frame2.bones().get("weapon").quaternion();
        Quaterniond quaterniond = new Quaterniond(dArray[0], dArray[1], dArray[2], dArray[3]).rotateX(0.025 * d5).normalize();
        double[] dArray2 = new double[]{quaterniond.x, quaterniond.y, quaterniond.z, quaterniond.w};
        GongdiBasicAttackSampler.Bone bone = new GongdiBasicAttackSampler.Bone(new double[]{-0.35, 0.73 + 0.012 * d5, 0.17}, dArray2);
        linkedHashMap.put("weapon", bone);
        linkedHashMap.put("right_wrist", bone);
        linkedHashMap.put("left_wrist", new GongdiBasicAttackSampler.Bone(new double[]{0.34, 0.71, 0.03 - 0.12 * d5}, frame2.bones().get("left_wrist").quaternion()));
        for (String string : new String[]{"right", "left"}) {
            double d6 = Math.sin(d2 + (string.equals("right") ? 0.0 : Math.PI));
            double d7 = Math.max(0.0, d6) * (0.11 + 0.04 * d4);
            double d8 = d6 * (0.12 + 0.035 * d4);
            linkedHashMap.put(string + "_ankle", new GongdiBasicAttackSampler.Bone(new double[]{string.equals("right") ? -0.145 : 0.145, 0.125 + d7, d8}, new double[]{0.0, 0.0, 0.0, 1.0}));
        }
        GongdiBasicAttackSampler.Frame frame3 = GongdiHumanoidTimeline.constrain(new GongdiBasicAttackSampler.Frame(frame.time(), Map.copyOf(linkedHashMap)));
        return GongdiHumanoidTimeline.blend(frame, frame3, d3);
    }

    private GongdiRunFrame() {
    }

    private static final class Library {
        private static final GongdiBasicAttackSampler.Frame IDLE = GongdiBasicAttackSampler.sample("held_idle", 0.0);

        private Library() {
        }
    }
}

