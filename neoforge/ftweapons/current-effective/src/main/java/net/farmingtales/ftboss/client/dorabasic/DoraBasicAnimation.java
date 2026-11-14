package net.farmingtales.ftboss.client.dorabasic;

import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.core.registries.BuiltInRegistries;

public final class DoraBasicAnimation {
    private DoraBasicAnimation() {
    }

    public static boolean accepts(int var0, boolean var1, boolean var2, boolean var3, boolean var4, boolean var5) {
        return var0 == 2 && var1 && !var2 && !var3 && !var4 && !var5;
    }

    public static boolean accepts(int var0, boolean var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6, boolean var7) {
        return accepts(var0, var1, var2, var3, var4, var5) && !var6 && !var7;
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
        if (!armMeleeAnimationDisabled()) {
            if (var1.getVariant() == 2
                && var1.isAlive()
                && !var1.isMini()
                && !var1.isCastingLike()
                && !var1.isDoraReloading()
                && !var1.isNoAi()
                && !var1.isPassenger()) {
                CompoundTag var7 = var1.getSkillCue();
                if (!var7.getBoolean("doraSniper") && var7.contains("doraBasic")) {
                    ResourceLocation var8 = BuiltInRegistries.ITEM.getKey(var1.getMainHandItem().getItem());
                    ResourceLocation var9 = BuiltInRegistries.ITEM.getKey(var1.getOffhandItem().getItem());
                    boolean var10 = var8 != null && "ftboss:dora_nova_launcher".equals(var8.toString());
                    boolean var11 = var9 != null && "ftboss:dora_lance".equals(var9.toString());
                    if (var10 || var11) {
                        CompoundTag var12 = var7.getCompound("doraBasic");
                        double var13 = (double)Math.max(0.0F, Math.min(1.0F, var4 - (float)var1.tickCount));
                        long var15 = var1.level().getGameTime();
                        double var17 = var12.contains("gunTick") ? (double)(var15 - var12.getLong("gunTick")) + var13 : -1.0;
                        double var19 = var12.contains("spearTick") ? (double)(var15 - var12.getLong("spearTick")) + var13 : -1.0;
                        double var21 = var10 ? DoraBasicPose.weight(true, var17) : 0.0;
                        double var23 = var11 ? DoraBasicPose.weight(false, var19) : 0.0;
                        if (var21 != 0.0 || var23 != 0.0) {
                            boolean var25 = var1.getMainArm() == HumanoidArm.LEFT;
                            int var26 = Math.floorMod(var12.getInt("gunSequence") - 1, 3);
                            int var27 = 3 + Math.floorMod(var12.getInt("spearSequence") - 1, 3);
                            ModelPart[] var28 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm};
                            double[][] var29 = new double[6][3];
                            int var30 = var25 ? 3 : 2;
                            int var31 = var25 ? 2 : 3;

                            for (int var32 = 0; var32 < 4; var32++) {
                                for (int var33 = 0; var33 < 3; var33++) {
                                    ModelPart var34 = var28[var32];
                                    var29[var32][var33] = Math.toDegrees(
                                        var33 == 0 ? (double)var34.xRot : (var33 == 1 ? (double)var34.yRot : (double)var34.zRot)
                                    );
                                }
                            }

                            DoraBasicPose.Pose var37 = DoraBasicPose.overlay(
                                var29, var26, var27, var10 ? var17 : -1.0, var11 ? var19 : -1.0, var25, (double)var5, (double)var6
                            );
                            double[][] var38 = var37.angles();
                            double[][] var39 = var37.positions();

                            for (int var35 = 0; var35 < var28.length; var35++) {
                                if ((var35 != var30 || var10) && (var35 != var31 || var11)) {
                                    ModelPart var36 = var28[var35];
                                    var36.x = (float)var39[var35][0];
                                    var36.y = (float)var39[var35][1];
                                    var36.z = (float)var39[var35][2];
                                    var36.xRot = (float)Math.toRadians(var38[var35][0]);
                                    var36.yRot = (float)Math.toRadians(var38[var35][1]);
                                    var36.zRot = (float)Math.toRadians(var38[var35][2]);
                                }
                            }

                            var0.hat.copyFrom(var0.head);
                            var0.jacket.copyFrom(var0.body);
                            var0.rightSleeve.copyFrom(var0.rightArm);
                            var0.leftSleeve.copyFrom(var0.leftArm);
                        }
                    }
                }
            }
        }
    }

    public static final boolean armMeleeAnimationDisabled() {
        return true;
    }
}
