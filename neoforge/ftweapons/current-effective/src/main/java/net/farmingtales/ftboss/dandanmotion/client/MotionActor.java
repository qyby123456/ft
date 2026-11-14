package net.farmingtales.ftboss.dandanmotion.client;

import net.farmingtales.ftboss.dandanmotion.MotionAccess;
import net.farmingtales.ftboss.dandanmotion.MotionCurves;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;

public final class MotionActor {
    private MotionActor() {
    }

    public static void animate(PlayerModel<?> var0, EasterBossEntity var1, float var2) {
        if (var1.getVariant() == 1 && !var1.isMini() && var1.isAlive() && (Object)var1 instanceof MotionAccess var3) {
            float var4 = (float)MotionCurves.clamp((double)(var2 - (float)var1.tickCount));
            CompoundTag var5 = var3.dandanMotionData();
            String var6 = var5.getString("dandan_motion_clip");
            int var7 = var5.getInt("dandan_motion_ticks");
            double var8 = (double)((float)(var1.level().getGameTime() - var5.getLong("dandan_motion_start")) + var4);
            double var10;
            double var12;
            if (var5.getInt("dandan_motion_schema") == 1 && MotionCurves.has(var6) && var7 > 0 && var7 <= 200 && var8 >= 0.0 && var8 < (double)var7) {
                var10 = var8 / (double)var7;
                var12 = 1.0;
                if (var5.getBoolean("dandan_motion_reverse")) {
                    var10 = 1.0 - var10;
                }
            } else {
                if (var1.isCastingLike()) {
                    return;
                }

                var12 = (double)var1.getCombatAnimationWeight(var4);
                if (var12 <= 0.0) {
                    return;
                }

                if (var5.getInt("dandan_motion_schema") != 1 || !MotionCurves.has(var6) || !(var8 >= (double)var7)) {
                    return;
                }

                var10 = var5.getBoolean("dandan_motion_reverse") ? 0.0 : 1.0;
            }

            double[][] var14 = MotionCurves.resolve(MotionCurves.sample(var6, var10));
            ModelPart[] var15 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm};

            for (int var16 = 0; var16 < var15.length; var16++) {
                ModelPart var17 = var15[var16];
                var17.x = (float)((double)var17.x * (1.0 - var12) + var14[var16][0] * var12);
                var17.y = (float)((double)var17.y * (1.0 - var12) + var14[var16][1] * var12);
                var17.z = (float)((double)var17.z * (1.0 - var12) + var14[var16][2] * var12);
                var17.xRot = (float)((double)var17.xRot * (1.0 - var12) + Math.toRadians(var14[var16][3]) * var12);
                var17.yRot = (float)((double)var17.yRot * (1.0 - var12) + Math.toRadians(var14[var16][4]) * var12);
                var17.zRot = (float)((double)var17.zRot * (1.0 - var12) + Math.toRadians(var14[var16][5]) * var12);
            }

            var0.hat.copyFrom(var0.head);
            var0.jacket.copyFrom(var0.body);
            var0.leftSleeve.copyFrom(var0.leftArm);
            var0.rightSleeve.copyFrom(var0.rightArm);
        }
    }
}
