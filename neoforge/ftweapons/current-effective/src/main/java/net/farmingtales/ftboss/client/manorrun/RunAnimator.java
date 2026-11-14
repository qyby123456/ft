package net.farmingtales.ftboss.client.manorrun;

import java.util.WeakHashMap;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.qizairun.QizaiRunClient;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;

public final class RunAnimator {
    private static final WeakHashMap<EasterBossEntity, RunAnimator.Frame> FRAMES = new WeakHashMap<>();

    private RunAnimator() {
    }

    private static ModelPart[] parts(EasterBossModel var0) {
        return new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};
    }

    public static void prepare(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4) {
        if (!QizaiRunClient.owns(var1)) {
            if (!EasterBossEntity.isManorVariant(var1.getVariant())) {
                RunAnimator.Frame var5 = FRAMES.computeIfAbsent(var1, var0x -> new RunAnimator.Frame());
                var5.captured = false;
                if (var1.getVariant() >= 0 && var1.getVariant() < RunGait.count() && var1.isAlive() && !var1.isMini() && !var1.isGiant()) {
                    double var6 = var1.getX() - var1.xo;
                    double var8 = var1.getZ() - var1.zo;
                    double var10 = Math.hypot(var6, var8);
                    boolean var12 = var1.onGround() && !var1.isInWater() && !var1.isPassenger() && !var1.isFallFlying() && !var1.isNoAi();
                    var5.clock.update((double)var4, var10, var12, (double)var2 * 0.6662, var1.getVariant());
                    if (!(var5.clock.blend <= 0.001) && Float.isFinite(var2) && Float.isFinite(var3)) {
                        double var13 = Math.toRadians((double)var1.yBodyRot);
                        if (var10 > 0.001) {
                            var5.forward = (var8 * Math.cos(var13) - var6 * Math.sin(var13)) / var10;
                            var5.sideways = (var6 * Math.cos(var13) + var8 * Math.sin(var13)) / var10;
                        }

                        if (Double.isFinite(var5.forward) && Double.isFinite(var5.sideways)) {
                            ModelPart[] var15 = parts(var0);

                            for (int var16 = 0; var16 < var15.length; var16++) {
                                ModelPart var17 = var15[var16];
                                int var18 = var16 * 6;
                                var5.base[var18] = (double)var17.x;
                                var5.base[var18 + 1] = (double)var17.y;
                                var5.base[var18 + 2] = (double)var17.z;
                                var5.base[var18 + 3] = (double)var17.xRot;
                                var5.base[var18 + 4] = (double)var17.yRot;
                                var5.base[var18 + 5] = (double)var17.zRot;
                            }

                            var5.age = var4;
                            var5.captured = true;
                        }
                    }
                } else {
                    var5.clock.update((double)var4, 0.0, false, 0.0, var1.getVariant());
                }
            }
        }
    }

    private static double allowance(ModelPart var0, double[] var1, int var2) {
        double var3 = Math.max(
            Math.abs((double)var0.xRot - var1[var2 + 3]), Math.max(Math.abs((double)var0.yRot - var1[var2 + 4]), Math.abs((double)var0.zRot - var1[var2 + 5]))
        );
        double var5 = Math.max(
            Math.abs((double)var0.x - var1[var2]), Math.max(Math.abs((double)var0.y - var1[var2 + 1]), Math.abs((double)var0.z - var1[var2 + 2]))
        );
        return RunGait.channelAllowance(var3, var5);
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2) {
        if (!QizaiRunClient.owns(var1)) {
            if (!EasterBossEntity.isManorVariant(var1.getVariant())) {
                RunAnimator.Frame var3 = FRAMES.get(var1);
                if (var3 != null && var3.captured && var3.age == var2) {
                    var3.captured = false;
                    if (var1.isAlive() && !(var3.clock.blend <= 0.001)) {
                        boolean var4 = var1.getMainArm() == HumanoidArm.LEFT;
                        boolean var5 = !var1.getMainHandItem().isEmpty();
                        RunGait.sample(var1.getVariant(), var3.clock.phase, var3.clock.speed, var4, var5, var3.pose);
                        RunGait.orient(var3.pose, var3.forward, var3.sideways);
                        ModelPart[] var6 = parts(var0);
                        ModelPart[] var7 = new ModelPart[]{var0.hat, var0.jacket, var0.rightSleeve, var0.leftSleeve, var0.rightPants, var0.leftPants};
                        double var8 = (double)Math.max(
                            0.0F, Math.min(1.0F, var1.getCombatAnimationWeight(Math.max(0.0F, Math.min(1.0F, var2 - (float)var1.tickCount))))
                        );
                        double var10 = (1.0 - RunGait.smooth(var8 / 0.25)) * allowance(var6[1], var3.base, 6);
                        if (var1.isCastingLike()) {
                            var10 = 0.0;
                        }

                        for (int var12 = 0; var12 < var6.length; var12++) {
                            ModelPart var13 = var6[var12];
                            int var14 = var12 * 6;
                            double var15 = allowance(var13, var3.base, var14);
                            double var17 = var3.clock.blend
                                * (
                                    var12 < 4
                                        ? var10
                                            * RunGait.channelAllowance(
                                                0.0,
                                                Math.max(Math.abs((double)var13.y - var3.base[var14 + 1]), Math.abs((double)var13.z - var3.base[var14 + 2]))
                                            )
                                        : var15
                                );
                            double var19 = var3.clock.blend * (var12 < 4 ? var10 * var15 : var15);
                            if (var12 > 1 && var12 < 4) {
                                boolean var21 = var12 == (var4 ? 3 : 2);
                                boolean var22 = var1.getVariant() == 2 || var1.getVariant() == 6 || var1.getVariant() == 11;
                                if (var1.isCastingLike() || var8 > 0.04 || var21 && var5 && var22 || !var21 && !var1.getOffhandItem().isEmpty()) {
                                    var19 = 0.0;
                                }
                            }

                            if (!(var17 <= 0.001) || !(var19 <= 0.001)) {
                                float var27 = var13.x;
                                float var28 = var13.y;
                                float var23 = var13.z;
                                float var24 = var13.xRot;
                                float var25 = var13.yRot;
                                float var26 = var13.zRot;
                                var13.x = var13.x + (float)(var3.pose[var14] * var17);
                                var13.y = var13.y + (float)(var3.pose[var14 + 1] * var17);
                                var13.z = var13.z + (float)(var3.pose[var14 + 2] * var17);
                                var13.xRot = var13.xRot + (float)((var3.pose[var14 + 3] - (var12 == 0 ? 0.0 : var3.base[var14 + 3])) * var19);
                                var13.yRot = var13.yRot + (float)((var3.pose[var14 + 4] - (var12 == 0 ? 0.0 : var3.base[var14 + 4])) * var19);
                                var13.zRot = var13.zRot + (float)((var3.pose[var14 + 5] - (var12 == 0 ? 0.0 : var3.base[var14 + 5])) * var19);
                                if (var12 >= 4 && var19 > 0.001) {
                                    var13.y = Math.min(var13.y, (float)(24.0 - RunGait.sole((double)var13.xRot, (double)var13.yRot, (double)var13.zRot)));
                                }

                                var7[var12].x = var7[var12].x + (var13.x - var27);
                                var7[var12].y = var7[var12].y + (var13.y - var28);
                                var7[var12].z = var7[var12].z + (var13.z - var23);
                                var7[var12].xRot = var7[var12].xRot + (var13.xRot - var24);
                                var7[var12].yRot = var7[var12].yRot + (var13.yRot - var25);
                                var7[var12].zRot = var7[var12].zRot + (var13.zRot - var26);
                            }
                        }
                    }
                }
            }
        }
    }

    private static final class Frame {
        final RunGait.Clock clock = new RunGait.Clock();
        final double[] base = new double[36];
        final double[] pose = new double[36];
        float age;
        double forward;
        double sideways;
        boolean captured;
    }
}
