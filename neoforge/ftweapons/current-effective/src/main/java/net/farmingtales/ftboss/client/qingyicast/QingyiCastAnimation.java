package net.farmingtales.ftboss.client.qingyicast;

import java.lang.reflect.Method;
import net.farmingtales.ftboss.animation.qingyicast.QingyiCastAccess;
import net.farmingtales.ftboss.animation.qingyicast.QingyiCastTime;
import net.farmingtales.ftboss.animation.qingyicast.QingyiCastTime.Sample;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.HumanoidArm;

public final class QingyiCastAnimation {
    private static final Method BASIC_HAND = basicHand();

    private QingyiCastAnimation() {
    }

    private static Method basicHand() {
        try {
            return Class.forName("net.farmingtales.ftboss.client.qingyi_basic.BasicPose", false, QingyiCastAnimation.class.getClassLoader())
                .getMethod("weaponOccupied", EasterBossEntity.class);
        } catch (ReflectiveOperationException var1) {
            return null;
        }
    }

    private static boolean occupied(EasterBossEntity var0) {
        if (BASIC_HAND == null) {
            return false;
        } else {
            try {
                return Boolean.TRUE.equals(BASIC_HAND.invoke(null, var0));
            } catch (ReflectiveOperationException var2) {
                return false;
            }
        }
    }

    private static Sample time(CompoundTag var0, long var1, double var3) {
        int var5 = var0.getInt("stage");
        long var6 = var0.getLong("start");
        long var8 = var0.getLong("stop");
        return !var0.isEmpty() && (var5 != 0 || var1 - var6 <= (long)(var0.getInt("duration") + 24)) && (var5 == 0 || var8 >= var6) && var1 - var6 <= 240L
            ? QingyiCastTime.sample(
                var0.getInt("track"),
                (double)(var1 - (var5 == 0 ? var6 : var8)) + var3,
                var0.getInt("duration"),
                var5,
                (double)(var8 - var6),
                var0.getBoolean("final"),
                var0.getDouble("origin"),
                (double)(var0.getLong("finalAt") - var8)
            )
            : new Sample(0.0, 0.0);
    }

    public static QingyiCastAnimation.Frame layer(CompoundTag var0, long var1, double var3) {
        CompoundTag var5 = var0.getCompound("native");
        Sample var6 = time(var5, var1, var3);
        if (var6.weight() <= 0.0) {
            var5 = var0.getCompound("iron");
            var6 = time(var5, var1, var3);
        }

        return var6.weight() > 0.0 ? new QingyiCastAnimation.Frame(var5, var6) : null;
    }

    private static QingyiCastAnimation.Frame selected(EasterBossEntity var0, float var1) {
        if (var0.getVariant() == 5 && var0.isAlive() && !var0.isMini() && var0 instanceof QingyiCastAccess var2) {
            long var3 = var0.level().getGameTime();
            QingyiCastAnimation.Frame var5 = layer(var2.qingyiCastState(), var3, (double)var1);
            if (var5 == null) {
                return null;
            } else {
                CompoundTag var6 = var5.cue();
                long var7 = var3 - (long)Math.round(var0.getCombatAnimationAge(0.0F));
                long var9 = Math.max(Math.max(var6.getLong("start"), var6.getLong("stop")), var6.getLong("finalAt"));
                return !occupied(var0) && var0.getCombatAnimationWeight(var1) > 0.0F && var7 > var9 && !var0.isCastingLike() ? null : var5;
            }
        } else {
            return null;
        }
    }

    public static boolean layerActive(EasterBossEntity var0, float var1) {
        return selected(var0, var1) != null;
    }

    public static double layerWeight(EasterBossEntity var0, float var1) {
        QingyiCastAnimation.Frame var2 = selected(var0, var1);
        return var2 == null ? 0.0 : var2.timing().weight();
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
        double var7 = (double)Math.max(0.0F, Math.min(1.0F, var4 - (float)var1.tickCount));
        QingyiCastAnimation.Frame var9 = selected(var1, (float)var7);
        if (var9 != null) {
            CompoundTag var10 = var9.cue();
            Sample var11 = var9.timing();
            QingyiCastPose.Pose var12 = QingyiCastPose.sample(
                var10.getInt("track"),
                var11.progress(),
                var1.getMainArm() == HumanoidArm.LEFT,
                (double)var3,
                (double)var2,
                (double)var5 * Math.PI / 180.0,
                (double)var6 * Math.PI / 180.0
            );
            applyPose(var0, var12, var11.weight());
        }
    }

    public static void applyPose(EasterBossModel var0, QingyiCastPose.Pose var1, double var2) {
        ModelPart[] var4 = new ModelPart[]{var0.head, var0.body, var0.rightArm, var0.leftArm, var0.rightLeg, var0.leftLeg};
        float var5 = (float)Math.max(0.0, Math.min(1.0, var2));
        if (!(var5 <= 0.0F)) {
            for (int var6 = 0; var6 < var4.length; var6++) {
                ModelPart var7 = var4[var6];
                var7.x = (float)((double)var7.x + (var1.position[var6][0] - (double)var7.x) * (double)var5);
                var7.y = (float)((double)var7.y + (var1.position[var6][1] - (double)var7.y) * (double)var5);
                var7.z = (float)((double)var7.z + (var1.position[var6][2] - (double)var7.z) * (double)var5);
                var7.xRot = (float)((double)var7.xRot + (var1.rotation[var6][0] - (double)var7.xRot) * (double)var5);
                var7.yRot = (float)((double)var7.yRot + (var1.rotation[var6][1] - (double)var7.yRot) * (double)var5);
                var7.zRot = (float)((double)var7.zRot + (var1.rotation[var6][2] - (double)var7.zRot) * (double)var5);
            }

            for (int var28 = 4; var28 < 6; var28++) {
                ModelPart var29 = var4[var28];
                double var8 = Math.sin((double)var29.xRot);
                double var10 = Math.cos((double)var29.xRot);
                double var12 = Math.sin((double)var29.yRot);
                double var14 = Math.cos((double)var29.yRot);
                double var16 = Math.sin((double)var29.zRot);
                double var18 = Math.cos((double)var29.zRot);
                double var20 = var16 * var14;
                double var22 = var16 * var12 * var8 + var18 * var10;
                double var24 = var16 * var12 * var10 - var18 * var8;
                double var26 = 12.0 * Math.max(0.0, var22) + 2.0 * Math.abs(var20) + 2.0 * Math.abs(var24);
                var29.y = Math.min(var29.y, (float)(24.0 - var26));
            }

            var0.hat.copyFrom(var0.head);
            var0.jacket.copyFrom(var0.body);
            var0.rightSleeve.copyFrom(var0.rightArm);
            var0.leftSleeve.copyFrom(var0.leftArm);
            var0.rightPants.copyFrom(var0.rightLeg);
            var0.leftPants.copyFrom(var0.leftLeg);
        }
    }

    public static record Frame(CompoundTag cue, Sample timing) {
    }
}
