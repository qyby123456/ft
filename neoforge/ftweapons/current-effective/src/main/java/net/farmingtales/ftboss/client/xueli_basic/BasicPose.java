package net.farmingtales.ftboss.client.xueli_basic;

import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.xueli_basic.BasicAccess;
import net.farmingtales.ftboss.xueli_basic.BasicMath;
import net.farmingtales.ftboss.xueli_basic.BasicServer;
import net.farmingtales.ftboss.xueli_basic.BasicMath.Pose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;

public final class BasicPose {
    private BasicPose() {
    }

    private static CompoundTag cue(EasterBossEntity var0) {
        return var0 instanceof BasicAccess var1 ? var1.ftboss$getXueliBasic() : new CompoundTag();
    }

    public static float filterBlend(float var0, EasterBossEntity var1) {
        CompoundTag var2 = cue(var1);
        return BasicServer.supported(var1) && !var2.isEmpty() && var2.getString("weapon").equals(BasicServer.weapon(var1)) ? 0.0F : var0;
    }

    private static float mix(float var0, double var1, double var3) {
        return (float)((double)var0 + (var1 - (double)var0) * var3);
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3) {
        if (!armMeleeAnimationDisabled()) {
            if (BasicServer.supported(var1)) {
                CompoundTag var4 = cue(var1);
                String var5 = var4.getString("attack");
                if (BasicMath.attacks().containsKey(var5) && var4.getString("weapon").equals(BasicServer.weapon(var1))) {
                    double var6 = (double)Math.max(0.0F, Math.min(1.0F, var2 - (float)var1.tickCount));
                    double var8 = (double)(var1.level().getGameTime() - var4.getLong("start")) + var6;
                    int var10 = var4.getInt("duration");
                    boolean var11 = var4.getString("phase").equals("wind");
                    double var12 = BasicMath.envelope(var11, var8, var10);
                    if (!(var12 <= 0.0)) {
                        Pose var14 = BasicMath.sample(var5, BasicMath.timing(var11, var8, var10), var4.getBoolean("left"));
                        ModelPart[] var15 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};
                        double[][] var16 = new double[6][3];
                        boolean var17 = Math.abs(var3) > 0.02F;

                        for (int var18 = 0; var18 < 6; var18++) {
                            ModelPart var19 = var15[var18];
                            if (var18 < 4 || !var17) {
                                if (var18 == 0) {
                                    var19.xRot = var19.xRot + (float)(Math.toRadians(var14.angles()[var18][0]) * var12);
                                    var19.yRot = var19.yRot + (float)(Math.toRadians(var14.angles()[var18][1]) * var12);
                                } else {
                                    var19.xRot = mix(var19.xRot, Math.toRadians(var14.angles()[var18][0]), var12);
                                    var19.yRot = mix(var19.yRot, Math.toRadians(var14.angles()[var18][1]), var12);
                                }

                                var19.zRot = mix(var19.zRot, Math.toRadians(var14.angles()[var18][2]), var12);
                            }

                            var16[var18] = new double[]{
                                Math.toDegrees((double)var19.xRot), Math.toDegrees((double)var19.yRot), Math.toDegrees((double)var19.zRot)
                            };
                        }

                        double[][] var20 = BasicMath.stance(var16);

                        for (int var21 = 0; var21 < (var17 ? 4 : 6); var21++) {
                            var15[var21].x = (float)var20[var21][0];
                            var15[var21].y = (float)var20[var21][1];
                            var15[var21].z = (float)var20[var21][2];
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
        }
    }

    public static final boolean armMeleeAnimationDisabled() {
        return true;
    }
}
