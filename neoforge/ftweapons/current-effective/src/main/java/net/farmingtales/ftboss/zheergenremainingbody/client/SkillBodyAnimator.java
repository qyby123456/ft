package net.farmingtales.ftboss.zheergenremainingbody.client;

import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.zheergennightfall.NightfallAccess;
import net.farmingtales.ftboss.zheergenremainingbody.SkillBodySignals;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public final class SkillBodyAnimator {
    private SkillBodyAnimator() {
    }

    private static double ease(double var0) {
        double var2 = Math.max(0.0, Math.min(1.0, var0));
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static double[] clock(String var0, double var1, int var3, double var4) {
        if (!(var1 < 0.0) && var3 >= 1 && var3 <= 200 && Double.isFinite(var1)) {
            double var6 = Math.max(0.0, Math.min(1.0, var1 / (double)var3));
            if ("charge".equals(var0)) {
                return new double[]{var6, var1 <= (double)(var3 + 1) ? 1.0 : 0.0, 0.0};
            } else if ("release".equals(var0)) {
                return new double[]{var6, var1 < (double)var3 ? 1.0 - ease((var6 - 0.72) / 0.28) : 0.0, 1.0};
            } else if ("cancel".equals(var0) && Double.isFinite(var4)) {
                double var8 = 1.0 - var6 * var6 * (3.0 - 2.0 * var6);
                return new double[]{Math.max(0.0, Math.min(1.0, var4)), var1 < (double)var3 ? var8 : 0.0, 0.0};
            } else {
                return new double[]{0.0, 0.0, 0.0};
            }
        } else {
            return new double[]{0.0, 0.0, 0.0};
        }
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
        if (var1.getVariant() == 7 && !var1.isMini() && !var1.isGiant() && var1.isAlive() && (Object)var1 instanceof NightfallAccess var7) {
            ItemStack var8 = var1.getMainHandItem();
            if (!var8.isEmpty()) {
                ResourceLocation var9 = BuiltInRegistries.ITEM.getKey(var8.getItem());
                if (var9 != null && "ftboss:shadow_sword".equals(var9.toString())) {
                    CompoundTag var10 = var7.ftboss$nightfallRead();
                    if (var10.contains("ftboss_zheergen_remaining_body", 10)) {
                        CompoundTag var11 = var10.getCompound("ftboss_zheergen_remaining_body");
                        String var12 = var11.getString("id");
                        if (var11.getInt("schema") == 1 && SkillBodySignals.idValid(var12)) {
                            double var13 = (double)(var1.level().getGameTime() - var11.getLong("start"))
                                + (double)Math.max(0.0F, Math.min(1.0F, var4 - (float)var1.tickCount));
                            String var15 = var11.getString("phase");
                            double[] var16 = clock(var15, var13, var11.getInt("duration"), var11.getDouble("frozen"));
                            if (!(var16[1] <= 0.0)) {
                                boolean var17 = var16[2] == 1.0 || "cancel".equals(var15) && "release".equals(var11.getString("track"));
                                boolean var18 = var1.getMainArm() == HumanoidArm.LEFT;
                                if (var17 && "cancel".equals(var15)) {
                                    var16[1] *= 1.0 - ease((var16[0] - 0.72) / 0.28);
                                }

                                double[][] var19;
                                double[][] var20;
                                if ("shadow_cross".equals(var12)) {
                                    CrossPose var21 = CrossPose.sample(
                                        var17,
                                        var16[0],
                                        var16[1],
                                        var18,
                                        (double)var2,
                                        (double)var3,
                                        Math.toRadians((double)var5),
                                        Math.toRadians((double)var6)
                                    );
                                    var19 = var21.position;
                                    var20 = var21.rotation;
                                } else if ("shadow_cage".equals(var12)) {
                                    CagePose var24 = CagePose.sample(
                                        var17,
                                        var16[0],
                                        var16[1],
                                        var18,
                                        (double)var2,
                                        (double)var3,
                                        Math.toRadians((double)var5),
                                        Math.toRadians((double)var6)
                                    );
                                    var19 = var24.position;
                                    var20 = var24.rotation;
                                } else {
                                    StrikePose var25 = StrikePose.sample(
                                        var17,
                                        var16[0],
                                        var16[1],
                                        var18,
                                        (double)var2,
                                        (double)var3,
                                        Math.toRadians((double)var5),
                                        Math.toRadians((double)var6)
                                    );
                                    var19 = var25.position;
                                    var20 = var25.rotation;
                                }

                                ModelPart[] var26 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};

                                for (int var22 = 0; var22 < 6; var22++) {
                                    ModelPart var23 = var26[var22];
                                    var23.x = (float)var19[var22][0];
                                    var23.y = (float)var19[var22][1];
                                    var23.z = (float)var19[var22][2];
                                    var23.xRot = (float)var20[var22][0];
                                    var23.yRot = (float)var20[var22][1];
                                    var23.zRot = (float)var20[var22][2];
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
}
