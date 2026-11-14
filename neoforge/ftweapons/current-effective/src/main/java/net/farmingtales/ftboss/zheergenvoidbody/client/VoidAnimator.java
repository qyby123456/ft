package net.farmingtales.ftboss.zheergenvoidbody.client;

import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.manorrun.RunAnimator;
import net.farmingtales.ftboss.zheergennightfall.NightfallAccess;
import net.farmingtales.ftboss.zheergenvoidbody.VoidPhase;
import net.farmingtales.ftboss.zheergenvoidbody.VoidSignals;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public final class VoidAnimator {
    private VoidAnimator() {
    }

    public static boolean accepts(int var0, boolean var1, boolean var2, boolean var3, String var4, boolean var5) {
        return var0 == 7 && !var1 && !var2 && var3 && "ftboss:shadow_sword".equals(var4) && var5;
    }

    private static float partialTicks(float var0, int var1) {
        return Math.max(0.0F, Math.min(1.0F, var0 - (float)var1));
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
        if (var1.getVariant() == 7 && !var1.isMini() && !var1.isGiant() && var1.isAlive() && var1 instanceof NightfallAccess var7) {
            ItemStack var8 = var1.getMainHandItem();
            if (!var8.isEmpty()) {
                ResourceLocation var9 = BuiltInRegistries.ITEM.getKey(var8.getItem());
                if (var9 != null && "ftboss:shadow_sword".equals(var9.toString())) {
                    CompoundTag var10 = var7.ftboss$nightfallRead();
                    if (!VoidSignals.higherActive(var10, var1.level().getGameTime(), (double)partialTicks(var4, var1.tickCount))
                        && var10.contains("zheergenVoidBody", 10)) {
                        CompoundTag var11 = var10.getCompound("zheergenVoidBody");
                        if ("void_echo".equals(var11.getString("id")) && var11.getInt("schema") == 1) {
                            float var12 = Math.max(0.0F, Math.min(1.0F, var4 - (float)var1.tickCount));
                            double var13 = (double)(var1.level().getGameTime() - var11.getLong("start")) + (double)var12;
                            VoidPhase var15 = VoidPhase.sample(var11.getString("phase"), var13, var11.getInt("duration"), var11.getDouble("frozen"));
                            if (var15.active()) {
                                boolean var10000 = var15.track().equals("release");
                                double var10001 = var15.progress();
                                double var10002 = var15.weight();
                                RunAnimator.reserve(var10002, var1);
                                VoidPose var16 = VoidPose.sample(
                                    var10000,
                                    var10001,
                                    var10002,
                                    var1.getMainArm() == HumanoidArm.LEFT,
                                    (double)var2,
                                    (double)var3,
                                    Math.toRadians((double)var5),
                                    Math.toRadians((double)var6)
                                );
                                ModelPart[] var17 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};

                                for (int var18 = 0; var18 < var17.length; var18++) {
                                    ModelPart var19 = var17[var18];
                                    var19.x = (float)var16.position[var18][0];
                                    var19.y = (float)var16.position[var18][1];
                                    var19.z = (float)var16.position[var18][2];
                                    var19.xRot = (float)var16.rotation[var18][0];
                                    var19.yRot = (float)var16.rotation[var18][1];
                                    var19.zRot = (float)var16.rotation[var18][2];
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
