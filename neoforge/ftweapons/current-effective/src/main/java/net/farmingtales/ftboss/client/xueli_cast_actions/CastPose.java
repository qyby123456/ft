package net.farmingtales.ftboss.client.xueli_cast_actions;

import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.xueli_cast_actions.CastMotion;
import net.farmingtales.ftboss.xueli_cast_actions.CastServer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.farmingtales.ftboss.xueli_basic.BasicAccess;
import net.farmingtales.ftboss.xueli_cast_actions.CastAccess;
import net.minecraft.world.entity.HumanoidArm;

public final class CastPose {
    private CastPose() {
    }

    private static double age(EasterBossEntity var0, CompoundTag var1, float var2) {
        return (double)((float)(var0.level().getGameTime() - var1.getLong("start")) + var2);
    }

    private static boolean active(EasterBossEntity var0, CompoundTag var1, float var2) {
        return !var1.isEmpty()
            && CastServer.validCue(var1.getString("id"), var1.getString("stage"), var1.getInt("duration"))
            && CastMotion.clips().containsKey(var1.getString("id"))
            && var1.getString("weapon").equals(CastServer.weapon(var0))
            && var1.getBoolean("left") == (var0.getMainArm() == HumanoidArm.LEFT)
            && age(var0, var1, var2) >= 0.0
            && age(var0, var1, var2) < (double)var1.getInt("duration");
    }

    private static CompoundTag cue(EasterBossEntity var0, float var1) {
        CompoundTag var2 = ((CastAccess)(Object)var0).ftboss$getXueliCastActions();
        CompoundTag var3 = var2.getCompound("native");
        if (active(var0, var3, var1)) {
            return var3;
        } else {
            CompoundTag var4 = var2.getCompound("iron");
            return active(var0, var4, var1) && !var0.isPerformingSkill() && (var0.isCastingLike() || !(var0.getCombatAnimationWeight(var1) > 0.0F))
                ? var4
                : new CompoundTag();
        }
    }

    public static float filterGeneric(float var0, EasterBossEntity var1) {
        return CastServer.supported(var1) && !cue(var1, 0.0F).isEmpty() ? 0.0F : var0;
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3) {
        if (CastServer.supported(var1)) {
            float var4 = Math.max(0.0F, Math.min(1.0F, var2 - (float)var1.tickCount));
            CompoundTag var5 = cue(var1, var4);
            if (!var5.isEmpty()) {
                String var6 = var5.getString("stage");
                double var7 = age(var1, var5, var4);
                double var9 = CastMotion.weight(var6, var7, var5.getInt("duration"));
                double var11 = CastMotion.phase(var6, var7, var5.getInt("duration"));
                boolean var13 = var5.getBoolean("left");
                CastMotion.State var14 = CastMotion.combined(var5.getString("id"), var5.getString("secondary"), var11, var13);
                if (var5.getString("id").equals("slam_cast") && var6.equals("charge")) {
                    tuckSlam(var0);
                }

                pose(var0, var14, var9, (double)Math.abs(var3) > 0.02);
            }
        }
    }

    public static void tuckSlam(EasterBossModel var0) {
        var0.head.xRot = (float)Math.max(-Math.PI / 4, Math.min(Math.PI / 4, (double)var0.head.xRot));
        var0.head.yRot = (float)Math.max(-Math.toRadians(25.0), Math.min(Math.toRadians(25.0), (double)var0.head.yRot));
    }

    public static void pose(EasterBossModel var0, CastMotion.State var1, double var2, boolean var4) {
        ModelPart[] var5 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};
        double[][] var6 = new double[6][3];

        for (int var7 = 0; var7 < 6; var7++) {
            ModelPart var8 = var5[var7];
            if (var7 < 4 || !var4) {
                if (var7 == 0) {
                    var8.xRot = var8.xRot + (float)(Math.toRadians(var1.angles()[var7][0]) * var2);
                    var8.yRot = var8.yRot + (float)(Math.toRadians(var1.angles()[var7][1]) * var2);
                } else {
                    var8.xRot = var8.xRot + (float)((Math.toRadians(var1.angles()[var7][0]) - (double)var8.xRot) * var2);
                    var8.yRot = var8.yRot + (float)((Math.toRadians(var1.angles()[var7][1]) - (double)var8.yRot) * var2);
                }

                var8.zRot = var8.zRot + (float)((Math.toRadians(var1.angles()[var7][2]) - (double)var8.zRot) * var2);
            }

            var6[var7] = new double[]{Math.toDegrees((double)var8.xRot), Math.toDegrees((double)var8.yRot), Math.toDegrees((double)var8.zRot)};
        }

        double[][] var9 = CastMotion.positions(var6);

        for (int var10 = 0; var10 < (var4 ? 4 : 6); var10++) {
            var5[var10].x = (float)var9[var10][0];
            var5[var10].y = (float)var9[var10][1];
            var5[var10].z = (float)var9[var10][2];
        }

        var0.hat.copyFrom(var0.head);
        var0.jacket.copyFrom(var0.body);
        var0.rightSleeve.copyFrom(var0.rightArm);
        var0.leftSleeve.copyFrom(var0.leftArm);
        var0.rightPants.copyFrom(var0.rightLeg);
        var0.leftPants.copyFrom(var0.leftLeg);
    }
}
