package net.farmingtales.ftboss.client.qingyi_basic;

import java.lang.reflect.Method;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.qingyi_basic.BasicAccess;
import net.farmingtales.ftboss.qingyi_basic.BasicMotion;
import net.farmingtales.ftboss.qingyi_basic.BasicServer;
import net.farmingtales.ftboss.qingyi_basic.BasicMotion.State;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.HumanoidArm;

public final class BasicPose {
    private static final float GUARD_PITCH = (float)Math.toRadians(-6.0);
    private static final float GUARD_ROLL = (float)Math.toRadians(12.0);
    private static final Method CAST_LAYER = castLayer();

    private BasicPose() {
    }

    private static Method castLayer() {
        try {
            return Class.forName("net.farmingtales.ftboss.client.qingyicast.QingyiCastAnimation", false, BasicPose.class.getClassLoader())
                .getMethod("layerWeight", EasterBossEntity.class, float.class);
        } catch (ReflectiveOperationException var1) {
            return null;
        }
    }

    private static double castWeight(EasterBossEntity var0, float var1) {
        if (CAST_LAYER == null) {
            return 0.0;
        } else {
            try {
                return ((Number)CAST_LAYER.invoke(null, var0, var1)).doubleValue();
            } catch (ReflectiveOperationException var3) {
                throw new IllegalStateException("Qingyi cast layer query failed", var3);
            }
        }
    }

    public static double tailWeight(double var0) {
        return !(var0 < 0.0) && !(var0 >= 10.0) ? 1.0 - BasicMotion.smooth((var0 - 8.0) / 2.0) : 0.0;
    }

    public static double recoveryAllowance(double var0) {
        return Double.isFinite(var0) && !(var0 < 0.0) && !(var0 >= 10.0)
            ? Math.min(1.0, 1.0 - BasicMotion.smooth((var0 - 0.5) / 1.5) + BasicMotion.smooth((var0 - 8.0) / 2.0))
            : 1.0;
    }

    public static double weaponTail(EasterBossEntity var0, double var1) {
        if (!BasicServer.supported(var0)) {
            return 0.0;
        } else {
            CompoundTag var3 = ((BasicAccess)var0).ftboss$getQingyiBasic();
            return var3.contains("tailStart")
                    && var3.getString("tailWeapon").equals(BasicServer.weapon(var0))
                    && var3.getBoolean("tailLeft") == (var0.getMainArm() == HumanoidArm.LEFT)
                ? tailWeight((double)(var0.level().getGameTime() - var3.getLong("tailStart")) + var1)
                : 0.0;
        }
    }

    public static boolean weaponOccupied(EasterBossEntity var0) {
        if (!BasicServer.supported(var0)) {
            return false;
        } else if (weaponTail(var0, 0.0) > 0.0) {
            return true;
        } else {
            CompoundTag var1 = ((BasicAccess)var0).ftboss$getQingyiBasic();
            double var2 = (double)(var0.level().getGameTime() - var1.getLong("start"));
            return var1.getBoolean("active")
                && BasicMotion.clips().containsKey(var1.getString("clip"))
                && var1.getString("weapon").equals(BasicServer.weapon(var0))
                && var1.getBoolean("left") == (var0.getMainArm() == HumanoidArm.LEFT)
                && BasicMotion.weight(var1.getBoolean("preparation"), var2, var1.getInt("duration")) > 0.0;
        }
    }

    public static float filterGeneric(float var0, EasterBossEntity var1) {
        return var0 != 0.0F && weaponOccupied(var1) ? 0.0F : var0;
    }

    public static void guard(EasterBossModel var0, boolean var1) {
        ModelPart var2 = var1 ? var0.leftArm : var0.rightArm;
        var2.xRot = Math.min(var2.xRot, GUARD_PITCH);
        var2.zRot = var1 ? Math.min(var2.zRot, -GUARD_ROLL) : Math.max(var2.zRot, GUARD_ROLL);
    }

    public static void applyTailGuard(EasterBossModel var0, boolean var1, double var2) {
        if (!(var2 <= 0.0)) {
            blend(var1 ? var0.leftArm : var0.rightArm, new double[]{-6.0, 0.0, var1 ? -12.0 : 12.0}, var2, false);
            (var1 ? var0.leftSleeve : var0.rightSleeve).copyFrom(var1 ? var0.leftArm : var0.rightArm);
        }
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3) {
        if (!armMeleeAnimationDisabled()) {
            if (BasicServer.supported(var1)) {
                double var4 = (double)Math.max(0.0F, Math.min(1.0F, var2 - (float)var1.tickCount));
                double var6 = castWeight(var1, (float)var4);
                if (var6 <= 0.0 && (var1.isCastingLike() || var1.isPerformingSkill())) {
                    var6 = 1.0;
                }

                boolean var8 = var1.getMainArm() == HumanoidArm.LEFT;
                double var9 = weaponTail(var1, var4);
                if (var6 <= 0.0) {
                    guard(var0, var8);
                } else {
                    applyTailGuard(var0, var8, var9);
                }

                CompoundTag var11 = ((BasicAccess)var1).ftboss$getQingyiBasic();
                if (var11.getBoolean("active")) {
                    String var12 = var11.getString("clip");
                    boolean var13 = var11.getBoolean("left");
                    if (BasicMotion.clips().containsKey(var12) && var11.getString("weapon").equals(BasicServer.weapon(var1)) && var13 == var8) {
                        double var14 = (double)(var1.level().getGameTime() - var11.getLong("start")) + var4;
                        boolean var16 = var11.getBoolean("preparation");
                        double var17 = BasicMotion.weight(var16, var14, var11.getInt("duration"));
                        double var19 = var9 > 0.0 ? recoveryAllowance((double)(var1.level().getGameTime() - var11.getLong("tailStart")) + var4) : 1.0;
                        if (!(var17 <= 0.0)) {
                            double[][] var21 = BasicMotion.sampleAngles(var12, BasicMotion.phase(var16, var14, var11.getInt("duration")), var13);
                            if (var6 > 0.0) {
                                applyJointAngles(var0, var21, var17, var13, (double)Math.abs(var3) > 0.02, var6, 1.0 - var9 * (1.0 - var19));
                            } else {
                                applyAngles(var0, var21, var17, var13, (double)Math.abs(var3) > 0.02, false);
                                applyTailGuard(var0, var13, var9 * (1.0 - var19));
                            }
                        }
                    }
                }
            }
        }
    }

    public static double outsideYaw(double var0) {
        return var0 <= -20.0 ? -12.0 : (var0 >= -4.0 ? var0 : -12.0 + (var0 + 20.0) * (var0 + 20.0) / 32.0);
    }

    public static void applyJointAngles(EasterBossModel var0, double[][] var1, double var2, boolean var4, boolean var5, double var6) {
        applyJointAngles(var0, var1, var2, var4, var5, var6, 1.0);
    }

    public static void applyJointAngles(EasterBossModel var0, double[][] var1, double var2, boolean var4, boolean var5, double var6, double var8) {
        if (!(var2 <= 0.0)) {
            double var10 = Math.max(0.0, Math.min(1.0, var6));
            double var12 = 1.0 - var10;
            double var14 = var4 ? -1.0 : 1.0;
            int var16 = var4 ? 3 : 2;
            ModelPart[] var17 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};
            int var18 = var5 ? 4 : 6;
            double[][] var19 = null;
            if (var12 > 0.0) {
                double[][] var20 = new double[6][3];

                for (int var21 = 0; var21 < var18; var21++) {
                    var20[var21] = new double[]{
                        Math.toDegrees((double)var17[var21].xRot), Math.toDegrees((double)var17[var21].yRot), Math.toDegrees((double)var17[var21].zRot)
                    };
                }

                var19 = BasicMotion.positions(var20, !var5);
            }

            for (int var24 = 0; var24 < var18; var24++) {
                double[] var26 = var1[var24];
                if (var24 == var16) {
                    var26 = (double[])var26.clone();
                    var26[1] += (var14 * outsideYaw(var14 * var26[1]) - var26[1]) * var10;
                    blend(var17[var24], var26, var2 * var8, false);
                } else if (var12 > 0.0) {
                    blend(var17[var24], var26, var2 * var12, var24 == 0);
                }
            }

            if (var12 > 0.0) {
                double[][] var25 = new double[6][3];

                for (int var28 = 0; var28 < var18; var28++) {
                    var25[var28] = new double[]{
                        Math.toDegrees((double)var17[var28].xRot), Math.toDegrees((double)var17[var28].yRot), Math.toDegrees((double)var17[var28].zRot)
                    };
                }

                double[][] var29 = BasicMotion.positions(var25, !var5);

                for (int var22 = 0; var22 < var18; var22++) {
                    ModelPart var23 = var17[var22];
                    var23.x = var23.x + (float)((var29[var22][0] - var19[var22][0]) * var12 + (var19[var22][0] - (double)var23.x) * var12 * var2);
                    var23.y = var23.y + (float)((var29[var22][1] - var19[var22][1]) * var12 + (var19[var22][1] - (double)var23.y) * var12 * var2);
                    var23.z = var23.z + (float)((var29[var22][2] - var19[var22][2]) * var12 + (var19[var22][2] - (double)var23.z) * var12 * var2);
                    if (var22 >= 4) {
                        var23.y = Math.min(var23.y, (float)var29[var22][1]);
                    }
                }
            }

            var0.hat.copyFrom(var0.head);
            var0.jacket.copyFrom(var0.body);
            var0.rightSleeve.copyFrom(var0.rightArm);
            var0.leftSleeve.copyFrom(var0.leftArm);
            var0.rightPants.copyFrom(var0.rightLeg);
            var0.leftPants.copyFrom(var0.leftLeg);
        }
    }

    public static void applyLayer(EasterBossModel var0, State var1, double var2, boolean var4, boolean var5, boolean var6) {
        applyAngles(var0, var1.angles(), var2, var4, var5, var6);
    }

    private static void blend(ModelPart var0, double[] var1, double var2, boolean var4) {
        if (var4) {
            var0.xRot = var0.xRot + (float)(Math.toRadians(var1[0]) * var2);
            var0.yRot = var0.yRot + (float)(Math.toRadians(var1[1]) * var2);
        } else {
            var0.xRot = var0.xRot + (float)((Math.toRadians(var1[0]) - (double)var0.xRot) * var2);
            var0.yRot = var0.yRot + (float)((Math.toRadians(var1[1]) - (double)var0.yRot) * var2);
        }

        var0.zRot = var0.zRot + (float)((Math.toRadians(var1[2]) - (double)var0.zRot) * var2);
    }

    public static void applyAngles(EasterBossModel var0, double[][] var1, double var2, boolean var4, boolean var5, boolean var6) {
        if (!(var2 <= 0.0)) {
            if (var6) {
                ModelPart var12 = var4 ? var0.leftArm : var0.rightArm;
                blend(var12, var1[var4 ? 3 : 2], var2, false);
                (var4 ? var0.leftSleeve : var0.rightSleeve).copyFrom(var12);
            } else {
                ModelPart[] var7 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};
                int var8 = var5 ? 4 : 6;

                for (int var9 = 0; var9 < var8; var9++) {
                    blend(var7[var9], var1[var9], var2, var9 == 0);
                }

                double[][] var13 = new double[6][3];

                for (int var10 = 0; var10 < var8; var10++) {
                    var13[var10] = new double[]{
                        Math.toDegrees((double)var7[var10].xRot), Math.toDegrees((double)var7[var10].yRot), Math.toDegrees((double)var7[var10].zRot)
                    };
                }

                double[][] var14 = BasicMotion.positions(var13, !var5);

                for (int var11 = 0; var11 < var8; var11++) {
                    var7[var11].x = (float)var14[var11][0];
                    var7[var11].y = (float)var14[var11][1];
                    var7[var11].z = (float)var14[var11][2];
                }

                var0.hat.copyFrom(var0.head);
                var0.jacket.copyFrom(var0.body);
                var0.rightSleeve.copyFrom(var0.rightArm);
                var0.leftSleeve.copyFrom(var0.leftArm);
                var0.rightPants.copyFrom(var0.rightLeg);
                var0.leftPants.copyFrom(var0.leftLeg);
            }
        }
    }

    public static final boolean armMeleeAnimationDisabled() {
        return true;
    }
}
