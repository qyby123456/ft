package net.farmingtales.ftboss.client.maque;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.maque.support.MaqueSupportPacket;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.Level;

public final class MaqueHumanoidAnimations {
    private static final Map<UUID, MaqueHumanoidAnimations.Motion> ACTIVE = new LinkedHashMap<>();
    private static final Map<UUID, MaqueHumanoidAnimations.Transition> TRANSITIONS = new LinkedHashMap<>();
    private static Level world;

    private MaqueHumanoidAnimations() {
    }

    private static void bind(Level var0) {
        if (world != var0) {
            ACTIVE.clear();
            TRANSITIONS.clear();
            world = var0;
        }
    }

    public static void clear() {
        bind(null);
    }

    private static void add(Level var0, UUID var1, String var2, int var3, boolean var4) {
        bind(var0);
        long var5 = var0.getGameTime();
        ACTIVE.values().removeIf(var2x -> var5 < var2x.born || var5 - var2x.born > (long)(MaqueHumanoidPose.clip(var2x.clip).ticks() + 40));
        MaqueHumanoidAnimations.Motion var7 = ACTIVE.get(var1);
        boolean var8 = var7 != null
            && MaqueHumanoidTiming.merge(var7.clip.equals(var2), var7.charge, var4, var5, var7.born, var7.updated, MaqueHumanoidPose.clip(var2).ticks());
        if (var7 == null || var7.priority <= var3 || var5 - var7.born >= (long)MaqueHumanoidPose.clip(var7.clip).ticks()) {
            double var9 = var8 ? var7.releaseStart : (!var2.equals("scatter") && (var7 == null || !var7.clip.equals(var2) || !var7.charge || var4) ? 0.0 : 0.4);
            ACTIVE.put(var1, new MaqueHumanoidAnimations.Motion(var2, var8 ? var7.born : var5, var5, var3, var4, var9));
            if (ACTIVE.size() > 64) {
                ACTIVE.remove(ACTIVE.keySet().iterator().next());
            }
        }
    }

    public static void support(Level var0, MaqueSupportPacket var1) {
        int var2 = var1.kind();
        if (var2 == 0) {
            add(var0, var1.source(), "arrival", 10, false);
        } else if (var2 == 6) {
            add(var0, var1.source(), "intercept", 35, false);
        } else if (var2 >= 2 && var2 <= 5) {
            add(var0, var1.source(), "support", 20, false);
        }
    }

    public static void spell(Level var0, SpellVisualPacket var1) {
        String var2 = var1.spell().id;
        String var3 = var2.equals("feather_fan") ? "scatter" : (var2.equals("wingstep") ? "hover" : (var2.equals("feather_refuge") ? "refuge" : null));
        if (var3 != null) {
            add(var0, var1.owner(), var3, 40, var1.phase().name().equals("CHARGE"));
        }
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
        if (var1.getVariant() == 9 && var1.isAlive() && !var1.isPassenger()) {
            Level var7 = var1.level();
            bind(var7);
            double var8 = MaqueHumanoidPose.clamp((double)(var4 - (float)var1.tickCount), 0.0, 1.0);
            MaqueHumanoidAnimations.Motion var10 = ACTIVE.get(var1.getUUID());
            String var11 = null;
            String var12 = "idle";
            double var13 = 0.0;
            double var15 = 0.0;
            double var17 = (double)var7.getGameTime() + var8;
            if (var10 != null) {
                double var19 = var17 - (double)var10.born;
                int var21 = MaqueHumanoidPose.clip(var10.clip).ticks();
                if (var19 >= 0.0 && var19 < (double)var21) {
                    var11 = var10.clip;
                    var12 = var11 + ":" + var10.born + ":" + var10.charge;
                    MaqueHumanoidTiming.Sample var22 = MaqueHumanoidTiming.sample(var11, var10.charge, var19, var21, var10.releaseStart);
                    var13 = var22.progress();
                    var15 = var22.weight();
                } else {
                    ACTIVE.remove(var1.getUUID());
                }
            }

            double var33 = (double)var1.getCombatAnimationWeight((float)var8);
            if (!var1.isCastingLike() && var1.getActionTicks() > 0 && var33 > 0.001) {
                var11 = "impact";
                var12 = "impact:" + (long)(var17 - (double)var1.getCombatAnimationAge((float)var8));
                var13 = (double)var1.getActionProgress((float)var8);
                var15 = var33;
            } else if (var11 == null && var1.isCastingLike() && var33 > 0.001) {
                var11 = "support";
                var12 = "cast:" + (long)(var17 - (double)var1.getCombatAnimationAge((float)var8));
                var13 = (double)var1.getActionProgress((float)var8);
                var15 = var33;
            }

            if (var11 == null) {
                var11 = "support";
                var13 = 1.0;
                var15 = 0.7;
            }

            double[][] var34 = MaqueHumanoidPose.evaluate(var11, var13, var1.getMainArm() == HumanoidArm.LEFT);
            ModelPart[] var35 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};

            for (int var23 = 0; var23 < 4; var23++) {
                var35[var23].resetPose();
            }

            var35[0].xRot = (float)Math.toRadians((double)var6);
            var35[0].yRot = (float)Math.toRadians((double)var5);
            var35[2].xRot = -0.17F + (float)Math.cos((double)var2 * 0.6662 + Math.PI) * var3;
            var35[3].xRot = -0.12F + (float)Math.cos((double)var2 * 0.6662) * var3;
            var35[2].zRot = 0.1F;
            var35[3].zRot = -0.09F;
            MaqueHumanoidAnimations.Transition var36 = TRANSITIONS.computeIfAbsent(var1.getUUID(), var0x -> new MaqueHumanoidAnimations.Transition());
            if (var17 < var36.lastRendered || var17 - var36.lastRendered > 10.0) {
                var36.last = null;
                var36.start = null;
            }

            if (!var12.equals(var36.signature)) {
                var36.signature = var12;
                var36.switched = var17;
                var36.start = var36.last;
            }

            double var24 = var36.start == null ? 1.0 : MaqueHumanoidTiming.transition(var17 - var36.switched);
            double var26 = visualLift(var34[4]) * var15;
            double[][] var28 = new double[6][6];

            for (int var29 = 0; var29 < var35.length; var29++) {
                ModelPart var30 = var35[var29];
                if (var29 >= 4 && (var3 > 0.035F || !var1.onGround()) && !var11.equals("hover")) {
                    var30.y -= (float)var26;
                    if (var24 < 1.0) {
                        var30.y = (float)(var36.start[var29][1] + ((double)var30.y - var36.start[var29][1]) * var24);
                    }

                    var28[var29] = snapshot(var30);
                } else {
                    double[] var31 = var34[var29];
                    if (var29 == 0) {
                        var31[3] += Math.toRadians((double)var6) * 0.55;
                        var31[4] += Math.toRadians((double)var5) * 0.55;
                    }

                    var30.x = var30.x + (float)((var31[0] - (double)var30.x) * var15);
                    var30.y = var30.y + (float)((var31[1] - (double)var30.y) * var15);
                    var30.z = var30.z + (float)((var31[2] - (double)var30.z) * var15);
                    var30.xRot = var30.xRot + (float)((var31[3] - (double)var30.xRot) * var15);
                    var30.yRot = var30.yRot + (float)((var31[4] - (double)var30.yRot) * var15);
                    var30.zRot = var30.zRot + (float)((var31[5] - (double)var30.zRot) * var15);
                    if (var24 < 1.0) {
                        double[] var32 = var36.start[var29];
                        var30.x = (float)(var32[0] + ((double)var30.x - var32[0]) * var24);
                        var30.y = (float)(var32[1] + ((double)var30.y - var32[1]) * var24);
                        var30.z = (float)(var32[2] + ((double)var30.z - var32[2]) * var24);
                        var30.xRot = (float)(var32[3] + ((double)var30.xRot - var32[3]) * var24);
                        var30.yRot = (float)(var32[4] + ((double)var30.yRot - var32[4]) * var24);
                        var30.zRot = (float)(var32[5] + ((double)var30.zRot - var32[5]) * var24);
                    }

                    if (var29 >= 4) {
                        anchorLeg(var30, var29 == 4 ? -1.9 : 1.9);
                    }

                    var28[var29] = snapshot(var30);
                }
            }

            var36.last = var28;
            var36.lastRendered = var17;
            if (TRANSITIONS.size() > 64) {
                TRANSITIONS.remove(TRANSITIONS.keySet().iterator().next());
            }

            var0.hat.copyFrom(var0.head);
            var0.jacket.copyFrom(var0.body);
            var0.rightSleeve.copyFrom(var0.rightArm);
            var0.leftSleeve.copyFrom(var0.leftArm);
            var0.rightPants.copyFrom(var0.rightLeg);
            var0.leftPants.copyFrom(var0.leftLeg);
        }
    }

    private static double[] snapshot(ModelPart var0) {
        return new double[]{(double)var0.x, (double)var0.y, (double)var0.z, (double)var0.xRot, (double)var0.yRot, (double)var0.zRot};
    }

    private static double visualLift(double[] var0) {
        double[] var1 = MaqueHumanoidPose.rotation(new double[]{var0[3], var0[4], var0[5]});
        double var2 = Double.NEGATIVE_INFINITY;

        for (double var7 : new double[]{-2.0, 2.0}) {
            for (double var12 : new double[]{-2.0, 2.0}) {
                var2 = Math.max(var2, MaqueHumanoidPose.transform(var1, new double[]{var7, 12.0, var12})[1]);
            }
        }

        return Math.max(0.0, 24.0 - var0[1] - var2);
    }

    private static void anchorLeg(ModelPart var0, double var1) {
        double[] var3 = MaqueHumanoidPose.rotation(new double[]{(double)var0.xRot, (double)var0.yRot, (double)var0.zRot});
        double[] var4 = MaqueHumanoidPose.transform(var3, new double[]{0.0, 12.0, 0.0});
        double var5 = Double.NEGATIVE_INFINITY;

        for (double var10 : new double[]{-2.0, 2.0}) {
            for (double var15 : new double[]{-2.0, 2.0}) {
                var5 = Math.max(var5, MaqueHumanoidPose.transform(var3, new double[]{var10, 12.0, var15})[1]);
            }
        }

        var0.x = (float)(var1 - var4[0]);
        var0.z = (float)(-var4[2]);
        var0.y = (float)Math.min((double)var0.y, 24.0 - var5);
    }

    private static record Motion(String clip, long born, long updated, int priority, boolean charge, double releaseStart) {
    }

    private static final class Transition {
        String signature;
        double switched;
        double[][] start;
        double[][] last;
        double lastRendered;
    }
}
