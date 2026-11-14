package net.farmingtales.ftboss.client.xueli_run;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.xueli_cast_actions.CastServer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.farmingtales.ftboss.xueli_basic.BasicAccess;
import net.farmingtales.ftboss.xueli_cast_actions.CastAccess;
import net.minecraft.world.entity.HumanoidArm;

public final class RunPose {
    private static final Map<EasterBossEntity, RunPose.Blend> BLENDS = new WeakHashMap<>();

    private RunPose() {
    }

    private static boolean active(EasterBossEntity var0, CompoundTag var1, double var2) {
        double var4 = (double)(var0.level().getGameTime() - var1.getLong("start")) + var2;
        return !var1.isEmpty() && var4 >= 0.0 && var4 < (double)var1.getInt("duration");
    }

    private static boolean action(EasterBossEntity var0, double var1) {
        if (var0.isCastingLike() || var0.isPerformingSkill() || (double)var0.getCombatAnimationWeight((float)var1) > 0.001) {
            return true;
        } else if (active(var0, ((BasicAccess)(Object)var0).ftboss$getXueliBasic(), var1)) {
            return true;
        } else {
            CompoundTag var3 = ((CastAccess)(Object)var0).ftboss$getXueliCastActions();
            return !active(var0, var3.getCompound("native"), var1) && !active(var0, var3.getCompound("iron"), var1)
                ? active(var0, var0.getSkillCue(), var1)
                : true;
        }
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4) {
        if (var1 != null && var1.getVariant() == 10) {
            double var5 = (double)Math.max(0.0F, Math.min(1.0F, var4 - (float)var1.tickCount));
            double var7 = var1.getX() - var1.xOld;
            double var9 = var1.getZ() - var1.zOld;
            double var11 = Math.hypot(var7, var9);
            double var13 = Math.toRadians((double)var1.getYRot());
            double var15 = -var7 * Math.sin(var13) + var9 * Math.cos(var13);
            if (RunMath.eligible(
                    var1.getVariant(),
                    var1.isAlive(),
                    var1.isNoAi(),
                    var1.onGround(),
                    var1.isInWater(),
                    var1.isPassenger(),
                    action(var1, var5),
                    var11 > 0.02 && var15 < -0.2 * var11,
                    var11,
                    var3
                )
                && Float.isFinite(var4)
                && Float.isFinite(var2)) {
                String var17 = CastServer.weapon(var1);
                String var18 = !var17.equals("pin") && !var17.equals("pan") ? "empty" : var17;
                double var19 = RunMath.activation(var11);
                RunPose.Blend var21 = BLENDS.get(var1);
                double var22 = (double)var4;
                if (var21 != null && (var22 < var21.time() || var22 - var21.time() > 4.0)) {
                    var21 = null;
                }

                double var24 = var21 == null ? 0.0 : var22 - var21.time();
                double var26 = RunMath.smooth(var21 == null ? 0.0 : var21.weight(), var19, var24);
                BLENDS.put(var1, new RunPose.Blend(var22, var26));
                if (!(var26 < 0.001)) {
                    double var28 = (double)var2 * RunMath.PHASE_FACTOR;
                    pose(var0, RunMath.sample(var18, var28, var1.getMainArm() == HumanoidArm.LEFT, var19), var28, var26);
                }
            } else {
                BLENDS.remove(var1);
            }
        }
    }

    public static void pose(EasterBossModel var0, RunMath.State var1, double var2, double var4) {
        if (!(var4 <= 0.0)) {
            ModelPart[] var6 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};
            double[][] var7 = new double[6][3];

            for (int var8 = 0; var8 < 6; var8++) {
                ModelPart var9 = var6[var8];
                if (var8 == 0) {
                    var9.xRot = var9.xRot + (float)Math.toRadians(var1.angles()[var8][0] * var4);
                    var9.zRot = var9.zRot + (float)Math.toRadians(var1.angles()[var8][2] * var4);
                } else {
                    var9.xRot = var9.xRot + (float)((Math.toRadians(var1.angles()[var8][0]) - (double)var9.xRot) * var4);
                    var9.yRot = var9.yRot + (float)((Math.toRadians(var1.angles()[var8][1]) - (double)var9.yRot) * var4);
                    var9.zRot = var9.zRot + (float)((Math.toRadians(var1.angles()[var8][2]) - (double)var9.zRot) * var4);
                }

                var7[var8] = new double[]{Math.toDegrees((double)var9.xRot), Math.toDegrees((double)var9.yRot), Math.toDegrees((double)var9.zRot)};
            }

            double[][] var10 = RunMath.positions(var7, var2, var4);

            for (int var11 = 0; var11 < 6; var11++) {
                var6[var11].x = (float)var10[var11][0];
                var6[var11].y = (float)var10[var11][1];
                var6[var11].z = (float)var10[var11][2];
            }

            var0.hat.copyFrom(var0.head);
            var0.jacket.copyFrom(var0.body);
            var0.rightSleeve.copyFrom(var0.rightArm);
            var0.leftSleeve.copyFrom(var0.leftArm);
            var0.rightPants.copyFrom(var0.rightLeg);
            var0.leftPants.copyFrom(var0.leftLeg);
        }
    }

    private static record Blend(double time, double weight) {
    }
}
