package net.farmingtales.ftboss.client.zicaicast;

import net.farmingtales.ftboss.animation.zicaicast.ZicaiCastAccess;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ZicaiCastAnimation {
    private ZicaiCastAnimation() {
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
        if (var1.getVariant() == 8 && var1.isAlive() && var1 instanceof ZicaiCastAccess var7) {
            ItemStack var8 = var1.getMainHandItem();
            if (!var8.isEmpty()) {
                ResourceLocation var9 = BuiltInRegistries.ITEM.getKey(var8.getItem());
                if (var9 == null || !"ftboss:puffer_bat".equals(var9.toString())) {
                    return;
                }
            }

            CompoundTag var30 = var7.zicaiCastState();
            if (!var30.isEmpty()) {
                int var10 = var30.getInt("track");
                int var11 = var30.getInt("duration");
                int var12 = var30.getInt("stage");
                long var13 = var30.getLong("start");
                long var15 = var30.getLong("stop");
                long var17 = var1.level().getGameTime();
                double var19 = (double)Math.max(0.0F, Math.min(1.0F, var4 - (float)var1.tickCount));
                if (var12 == 0 || var15 >= var13) {
                    long var21 = var17 - (long)Math.round(var1.getCombatAnimationAge(0.0F));
                    if (!(var1.getCombatAnimationWeight((float)var19) > 0.0F) || var21 <= var13 || var1.isCastingLike()) {
                        ZicaiCastTiming.Sample var23 = ZicaiCastTiming.sample(
                            var10, (double)(var12 == 0 ? var17 - var13 : var17 - var15) + var19, var11, var12, (double)(var15 - var13)
                        );
                        if (!(var23.weight() <= 0.0)) {
                            ZicaiCastPose.Pose var24 = ZicaiCastPose.sample(
                                var10,
                                var23.progress(),
                                1.0,
                                var1.getMainArm() == HumanoidArm.LEFT,
                                (double)var2,
                                (double)var3,
                                (double)var5 * Math.PI / 180.0,
                                (double)var6 * Math.PI / 180.0
                            );
                            ModelPart[] var25 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};
                            ZicaiCastPose.Pose var26 = new ZicaiCastPose.Pose();

                            for (int var27 = 0; var27 < var25.length; var27++) {
                                ModelPart var28 = var25[var27];
                                var26.position[var27][0] = (double)var28.x;
                                var26.position[var27][1] = (double)var28.y;
                                var26.position[var27][2] = (double)var28.z;
                                var26.rotation[var27][0] = (double)var28.xRot;
                                var26.rotation[var27][1] = (double)var28.yRot;
                                var26.rotation[var27][2] = (double)var28.zRot;
                            }

                            ZicaiCastPose.Pose var31 = ZicaiCastPose.blend(var24, var26, var23.weight(), (double)var3);

                            for (int var32 = 0; var32 < var25.length; var32++) {
                                ModelPart var29 = var25[var32];
                                var29.x = (float)var31.position[var32][0];
                                var29.y = (float)var31.position[var32][1];
                                var29.z = (float)var31.position[var32][2];
                                var29.xRot = (float)var31.rotation[var32][0];
                                var29.yRot = (float)var31.rotation[var32][1];
                                var29.zRot = (float)var31.rotation[var32][2];
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
    }
}
