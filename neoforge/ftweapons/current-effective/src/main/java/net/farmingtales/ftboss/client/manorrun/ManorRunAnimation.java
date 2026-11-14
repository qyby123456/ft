package net.farmingtales.ftboss.client.manorrun;

import java.util.WeakHashMap;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.qizairun.QizaiRunClient;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.XuanlanMoonWeaponItem;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;

public final class ManorRunAnimation {
    private static final WeakHashMap<EasterBossEntity, RunCycle.State> STATES = new WeakHashMap<>();

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2) {
        if (!QizaiRunClient.owns(var1)) {
            if (EasterBossEntity.isManorVariant(var1.getVariant())) {
                float var3 = (float)RunCycle.clamp((double)(var2 - (float)var1.tickCount), 0.0, 1.0);
                RunCycle.State var4 = prepare(var1, var2);
                if (var4 != null) {
                    boolean var5 = blocked(var1, var3);
                    if (!var5) {
                        double var6 = var4.previousWeight + (var4.weight - var4.previousWeight) * (double)var3;
                        if (!(var6 < 1.0E-4) && var1.getVariant() != 3) {
                            double var8 = var4.previousPhase + (var4.phase - var4.previousPhase) * (double)var3;
                            int var10 = var1.getVariant() == 6 && XuanlanMoonWeaponItem.isWhite(var1.getMainHandItem()) ? 12 : var1.getVariant();
                            RunCycle.Pose var11 = RunCycle.pose(var10, var4.speed, var8);
                            double var12 = var11.lean() * var4.travelSign;
                            float var14 = (float)(var12 * var6);
                            var0.body.xRot += var14;
                            var0.rightArm.y = var0.leftArm.y = 2.0F * (float)Math.cos((double)var14);
                            var0.rightArm.z = var0.leftArm.z = 2.0F * (float)Math.sin((double)var14);
                            var0.rightLeg.y = var0.leftLeg.y = 12.0F * (float)Math.cos((double)var14);
                            var0.rightLeg.z = var0.leftLeg.z = 12.0F * (float)Math.sin((double)var14);
                            rotate(var0.rightLeg, var11.leg() + var12, 0.0, 0.0, var6);
                            rotate(var0.leftLeg, -var11.leg() + var12, 0.0, 0.0, var6);
                            rotate(var0.rightArm, var11.rightArm() + var12, 0.0, var11.roll(), var6);
                            rotate(var0.leftArm, var11.leftArm() + var12, 0.0, -var11.roll(), var6);
                            float var15 = var1.getVariant() == 11 ? 0.25F : 0.0F;
                            float var16 = Math.max(0.0F, Math.max(footY(var0.rightLeg, var15), footY(var0.leftLeg, var15)) - 24.0F);
                            var0.head.y -= var16;
                            var0.body.y -= var16;
                            var0.rightArm.y -= var16;
                            var0.leftArm.y -= var16;
                            var0.rightLeg.y -= var16;
                            var0.leftLeg.y -= var16;
                            var0.hat.copyFrom(var0.head);
                            var0.leftSleeve.copyFrom(var0.leftArm);
                            var0.rightSleeve.copyFrom(var0.rightArm);
                            var0.leftPants.copyFrom(var0.leftLeg);
                            var0.rightPants.copyFrom(var0.rightLeg);
                            var0.jacket.copyFrom(var0.body);
                        }
                    }
                }
            }
        }
    }

    static RunCycle.State current(EasterBossEntity var0) {
        return STATES.get(var0);
    }

    static RunCycle.State prepare(EasterBossEntity var0, float var1) {
        if (var0.isAlive() && !var0.isRemoved()) {
            float var2 = (float)RunCycle.clamp((double)(var1 - (float)var0.tickCount), 0.0, 1.0);
            RunCycle.State var3 = STATES.computeIfAbsent(var0, var0x -> new RunCycle.State());
            double var4 = var0.getX() - var3.lastX;
            double var6 = var0.getZ() - var3.lastZ;
            double var8 = Math.toRadians((double)var0.yBodyRot);
            double var10 = -var4 * Math.sin(var8) + var6 * Math.cos(var8);
            var3.update(var0.tickCount, var0.getX(), var0.getZ(), blocked(var0, var2), var10);
            return var3;
        } else {
            STATES.remove(var0);
            return null;
        }
    }

    static boolean blocked(EasterBossEntity var0, float var1) {
        CompoundTag var2 = var0.getSkillCue();
        double var3 = (double)((float)(var0.level().getGameTime() - var2.getLong("start")) + var1);
        boolean var5 = var2.contains("start") && var2.getInt("duration") > 0 && var3 >= 0.0 && var3 < (double)var2.getInt("duration");
        return !var0.isAlive()
            || var0.isRemoved()
            || !var0.onGround()
            || var0.isInWater()
            || var0.isPassenger()
            || var0.isNoAi()
            || var0.isSleeping()
            || var0.hurtTime > 0
            || var0.isCastingLike()
            || var0.isPerformingSkill()
            || var0.getCombatAnimationWeight(var1) > 0.001F
            || var0.isDoraReloading()
            || var0.getDoraSpearTicks() > 0
            || var0.isManorIntroHidden()
            || var0.isXuanlanSkyHidden()
            || var5;
    }

    private static float footY(ModelPart var0, float var1) {
        double var2 = Math.sin((double)var0.xRot);
        double var4 = Math.cos((double)var0.xRot);
        double var6 = Math.sin((double)var0.yRot);
        double var8 = Math.cos((double)var0.yRot);
        double var10 = Math.sin((double)var0.zRot);
        double var12 = Math.cos((double)var0.zRot);
        double var14 = var10 * var8;
        double var16 = var10 * var6 * var2 + var12 * var4;
        double var18 = var10 * var6 * var4 - var12 * var2;
        return (float)((double)var0.y + 6.0 * var16 + (double)(6.0F + var1) * Math.abs(var16) + (double)(2.0F + var1) * (Math.abs(var14) + Math.abs(var18)));
    }

    private static void rotate(ModelPart var0, double var1, double var3, double var5, double var7) {
        var0.xRot = var0.xRot + (float)((var1 - (double)var0.xRot) * var7);
        var0.yRot = var0.yRot + (float)((var3 - (double)var0.yRot) * var7);
        var0.zRot = var0.zRot + (float)((var5 - (double)var0.zRot) * var7);
    }

    private ManorRunAnimation() {
    }
}
