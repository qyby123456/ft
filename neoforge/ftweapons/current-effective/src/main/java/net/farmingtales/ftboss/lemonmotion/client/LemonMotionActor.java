package net.farmingtales.ftboss.lemonmotion.client;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.lemonmotion.LemonMotionAccess;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.HumanoidArm;

public final class LemonMotionActor {
    private static final double RAD = Math.PI / 180.0;
    private static final ThreadLocal<LemonMotionActor.Base> BASE = new ThreadLocal<>();

    private LemonMotionActor() {
    }

    private static ModelPart[] parts(PlayerModel<?> var0) {
        return new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};
    }

    public static void capture(PlayerModel<?> var0, EasterBossEntity var1) {
        BASE.remove();
        if (var1.getVariant() == 4 && !var1.isMini()) {
            ModelPart[] var2 = parts(var0);
            float[][] var3 = new float[6][];

            for (int var4 = 0; var4 < 6; var4++) {
                ModelPart var5 = var2[var4];
                var3[var4] = new float[]{var5.x, var5.y, var5.z, var5.xRot, var5.yRot, var5.zRot};
            }

            BASE.set(new LemonMotionActor.Base(var0, var1, var3));
        }
    }

    public static boolean animate(
        PlayerModel<?> var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6, ModelPart var7, ModelPart var8, ModelPart var9
    ) {
        LemonMotionActor.Base var10 = BASE.get();
        BASE.remove();
        if (var10 == null || var10.model() != var0 || var10.owner() != var1) {
            return false;
        } else if (var1.getVariant() == 4 && !var1.isMini()) {
            if (!var1.isCastingLike()
                || var1 instanceof LemonMotionAccess var11
                    && LemonMotionCurves.clip(ftboss$basicBodyClip(var11.lemonMotionData().getString("lemon_motion_v1"))) != null) {
                ModelPart[] var32 = parts(var0);

                for (int var12 = 0; var12 < 6; var12++) {
                    ModelPart var13 = var32[var12];
                    float[] var14 = var10.poses()[var12];
                    var13.x = var14[0];
                    var13.y = var14[1];
                    var13.z = var14[2];
                    var13.xRot = var14[3];
                    var13.yRot = var14[4];
                    var13.zRot = var14[5];
                }

                var7.resetPose();
                var8.resetPose();
                var9.resetPose();
                if (var1.isAlive() && var1 instanceof LemonMotionAccess var33) {
                    CompoundTag var34 = var33.lemonMotionData();
                    String var35 = ftboss$basicBodyClip(var34.getString("lemon_motion_v1"));
                    int var15 = var34.getInt("duration");
                    double var16 = LemonMotionCurves.clamp((double)(var4 - (float)var1.tickCount), 0.0, 1.0);
                    double var18 = (double)var1.getCombatAnimationAge((float)var16);
                    if (var34.getInt("lemon_motion_schema") == 1
                        && LemonMotionCurves.clip(var35) != null
                        && var15 > 0
                        && var15 <= 2400
                        && var18 >= 0.0
                        && var18 < (double)var15) {
                        int var20 = var1.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
                        double var21 = LemonMotionCurves.weight(var18, (double)var15);
                        double[][] var23 = LemonMotionCurves.pose(var35, var18, (double)var15, var20, (double)var2, (double)var3, (double)var5, (double)var6);
                        double[][] var24 = LemonMotionCurves.pose(var35, 0.0, (double)var15, var20, (double)var2, (double)var3, (double)var5, (double)var6);
                        ModelPart[] var25 = var32;

                        for (int var29 : new int[]{0, 1, 2, 3}) {
                            ModelPart var30 = var25[var29];
                            var30.x = var30.x + (float)(var23[var29][0] - LemonMotionCurves.PIVOTS[var29][0]);
                            var30.y = var30.y + (float)(var23[var29][1] - LemonMotionCurves.PIVOTS[var29][1]);
                            var30.z = var30.z + (float)(var23[var29][2] - LemonMotionCurves.PIVOTS[var29][2]);
                            double[] var31 = new double[]{
                                (double)var30.xRot / (Math.PI / 180.0), (double)var30.yRot / (Math.PI / 180.0), (double)var30.zRot / (Math.PI / 180.0)
                            };
                            var30.xRot = (float)((var31[0] * (1.0 - var21) + var23[var29][3] - var24[var29][3] * (1.0 - var21)) * (Math.PI / 180.0));
                            var30.yRot = (float)((var31[1] * (1.0 - var21) + var23[var29][4] - var24[var29][4] * (1.0 - var21)) * (Math.PI / 180.0));
                            var30.zRot = (float)((var31[2] * (1.0 - var21) + var23[var29][5] - var24[var29][5] * (1.0 - var21)) * (Math.PI / 180.0));
                        }

                        double var36 = Math.sin(Math.PI * LemonMotionCurves.clamp(var18 / (double)var15 - 0.055, 0.0, 1.0)) * var21;
                        var8.xRot += (float)(var36 * 0.07);
                        var7.zRot += (float)(var36 * 0.018 * (double)var20);
                        var9.xRot += (float)(var36 * 0.03);
                    }
                }

                var0.hat.copyFrom(var0.head);
                var0.jacket.copyFrom(var0.body);
                var0.leftSleeve.copyFrom(var0.leftArm);
                var0.rightSleeve.copyFrom(var0.rightArm);
                var0.leftPants.copyFrom(var0.leftLeg);
                var0.rightPants.copyFrom(var0.rightLeg);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static String ftboss$basicBodyClip(String var0) {
        return "strike".equals(var0) ? "" : var0;
    }

    private static record Base(PlayerModel<?> model, EasterBossEntity owner, float[][] poses) {
    }
}
