package net.farmingtales.ftboss.client.manorrun;

import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.client.render.GongdiBasicAttackSampler;
import net.farmingtales.ftboss.client.render.GongdiHumanoidTimeline;
import net.farmingtales.ftboss.client.render.GongdiNaturalArmPose;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import org.joml.Quaterniond;

public final class GongdiRunFrame {
    public static GongdiBasicAttackSampler.Frame apply(GongdiBasicAttackSampler.Frame var0, EasterBossEntity var1, float var2) {
        float var3 = (float)RunCycle.clamp((double)(var2 - (float)var1.tickCount), 0.0, 1.0);
        RunCycle.State var4 = ManorRunAnimation.prepare(var1, var2);
        if (var4 != null && !ManorRunAnimation.blocked(var1, var3)) {
            double var5 = var4.previousWeight + (var4.weight - var4.previousWeight) * (double)var3;
            double var7 = var4.previousPhase + (var4.phase - var4.previousPhase) * (double)var3;
            if (var5 < 1.0E-4) {
                GongdiBasicAttackSampler.Frame var10 = var0;
                if (var1.getVariant() == 3) {
                    var10 = GongdiNaturalArmPose.apply(var0);
                }

                return var10;
            } else {
                GongdiBasicAttackSampler.Frame var9 = transform(var0, var4.speed, var7, var5);
                if (var1.getVariant() == 3) {
                    var9 = GongdiNaturalArmPose.apply(var9);
                }

                return var9;
            }
        } else {
            GongdiBasicAttackSampler.Frame var10000 = var0;
            if (var1.getVariant() == 3) {
                var10000 = GongdiNaturalArmPose.apply(var0);
            }

            return var10000;
        }
    }

    public static GongdiBasicAttackSampler.Frame locomotionBase(GongdiBasicAttackSampler.Frame var0, double var1, double var3) {
        double var5 = var3 / (Math.PI * 2);
        var5 -= Math.floor(var5);
        GongdiBasicAttackSampler.Frame var7 = GongdiBasicAttackSampler.sample("weighted_walk", var5);
        double var8 = RunCycle.clamp(var1 / 0.145, 0.0, 1.0);
        GongdiBasicAttackSampler.Frame var10 = GongdiHumanoidTimeline.blend(GongdiRunFrame.Library.IDLE, var7, var8);
        LinkedHashMap var11 = new LinkedHashMap<>(var10.bones());
        GongdiBasicAttackSampler.Bone var12 = (GongdiBasicAttackSampler.Bone)var11.get("head");
        var11.put("head", new GongdiBasicAttackSampler.Bone(var12.position(), var0.bones().get("head").quaternion()));
        GongdiBasicAttackSampler.Bone var13 = new GongdiBasicAttackSampler.Bone(
            new double[]{-0.35, 0.73, 0.17}, ((GongdiBasicAttackSampler.Bone)var11.get("weapon")).quaternion()
        );
        var11.put("weapon", var13);
        var11.put("right_wrist", var13);
        var11.put(
            "left_wrist",
            new GongdiBasicAttackSampler.Bone(
                new double[]{0.34, 0.71, 0.03 - 0.12 * var8 * Math.sin(var3 - 0.2)}, ((GongdiBasicAttackSampler.Bone)var11.get("left_wrist")).quaternion()
            )
        );
        return GongdiHumanoidTimeline.constrain(new GongdiBasicAttackSampler.Frame(var10.time(), Map.copyOf(var11)));
    }

    public static GongdiBasicAttackSampler.Frame transform(GongdiBasicAttackSampler.Frame var0, double var1, double var3, double var5) {
        GongdiBasicAttackSampler.Frame var7 = GongdiRunFrame.Library.IDLE;
        LinkedHashMap var8 = new LinkedHashMap<>(var7.bones());
        var8.put("head", new GongdiBasicAttackSampler.Bone(var7.bones().get("head").position(), var0.bones().get("head").quaternion()));
        double var9 = RunCycle.clamp((var1 - 0.145) / 0.33499999999999996, 0.0, 1.0);
        double var11 = Math.sin(var3 - 0.2);
        double[] var13 = var7.bones().get("weapon").quaternion();
        Quaterniond var14 = new Quaterniond(var13[0], var13[1], var13[2], var13[3]).rotateX(0.025 * var11).normalize();
        double[] var15 = new double[]{var14.x, var14.y, var14.z, var14.w};
        GongdiBasicAttackSampler.Bone var16 = new GongdiBasicAttackSampler.Bone(new double[]{-0.35, 0.73 + 0.012 * var11, 0.17}, var15);
        var8.put("weapon", var16);
        var8.put("right_wrist", var16);
        var8.put("left_wrist", new GongdiBasicAttackSampler.Bone(new double[]{0.34, 0.71, 0.03 - 0.12 * var11}, var7.bones().get("left_wrist").quaternion()));

        for (String var20 : new String[]{"right", "left"}) {
            double var21 = Math.sin(var3 + (var20.equals("right") ? 0.0 : Math.PI));
            double var23 = Math.max(0.0, var21) * (0.11 + 0.04 * var9);
            double var25 = var21 * (0.12 + 0.035 * var9);
            var8.put(
                var20 + "_ankle",
                new GongdiBasicAttackSampler.Bone(new double[]{var20.equals("right") ? -0.145 : 0.145, 0.125 + var23, var25}, new double[]{0.0, 0.0, 0.0, 1.0})
            );
        }

        GongdiBasicAttackSampler.Frame var27 = GongdiHumanoidTimeline.constrain(new GongdiBasicAttackSampler.Frame(var0.time(), Map.copyOf(var8)));
        return GongdiHumanoidTimeline.blend(var0, var27, var5);
    }

    private GongdiRunFrame() {
    }

    private static final class Library {
        private static final GongdiBasicAttackSampler.Frame IDLE = GongdiBasicAttackSampler.sample("held_idle", 0.0);
    }
}
