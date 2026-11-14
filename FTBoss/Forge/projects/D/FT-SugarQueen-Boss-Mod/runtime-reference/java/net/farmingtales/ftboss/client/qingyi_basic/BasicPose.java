/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.HumanoidArm
 */
package net.farmingtales.ftboss.client.qingyi_basic;

import java.lang.reflect.Method;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.qingyi_basic.BasicAccess;
import net.farmingtales.ftboss.qingyi_basic.BasicMotion;
import net.farmingtales.ftboss.qingyi_basic.BasicServer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.HumanoidArm;

public final class BasicPose {
    private static final float GUARD_PITCH = (float)Math.toRadians(-6.0);
    private static final float GUARD_ROLL = (float)Math.toRadians(12.0);
    private static final Method CAST_LAYER = BasicPose.castLayer();

    private BasicPose() {
    }

    private static Method castLayer() {
        try {
            return Class.forName("net.farmingtales.ftboss.client.qingyicast.QingyiCastAnimation", false, BasicPose.class.getClassLoader()).getMethod("layerWeight", EasterBossEntity.class, Float.TYPE);
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            return null;
        }
    }

    private static double castWeight(EasterBossEntity easterBossEntity, float f) {
        if (CAST_LAYER == null) {
            return 0.0;
        }
        try {
            return ((Number)CAST_LAYER.invoke(null, easterBossEntity, Float.valueOf(f))).doubleValue();
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new IllegalStateException("Qingyi cast layer query failed", reflectiveOperationException);
        }
    }

    public static double tailWeight(double d) {
        return d < 0.0 || d >= 10.0 ? 0.0 : 1.0 - BasicMotion.smooth((d - 8.0) / 2.0);
    }

    public static double recoveryAllowance(double d) {
        return !Double.isFinite(d) || d < 0.0 || d >= 10.0 ? 1.0 : Math.min(1.0, 1.0 - BasicMotion.smooth((d - 0.5) / 1.5) + BasicMotion.smooth((d - 8.0) / 2.0));
    }

    public static double weaponTail(EasterBossEntity easterBossEntity, double d) {
        if (!BasicServer.supported(easterBossEntity)) {
            return 0.0;
        }
        CompoundTag compoundTag = ((BasicAccess)easterBossEntity).ftboss$getQingyiBasic();
        return compoundTag.m_128441_("tailStart") && compoundTag.m_128461_("tailWeapon").equals(BasicServer.weapon(easterBossEntity)) && compoundTag.m_128471_("tailLeft") == (easterBossEntity.m_5737_() == HumanoidArm.LEFT) ? BasicPose.tailWeight((double)(easterBossEntity.m_9236_().m_46467_() - compoundTag.m_128454_("tailStart")) + d) : 0.0;
    }

    public static boolean weaponOccupied(EasterBossEntity easterBossEntity) {
        if (!BasicServer.supported(easterBossEntity)) {
            return false;
        }
        if (BasicPose.weaponTail(easterBossEntity, 0.0) > 0.0) {
            return true;
        }
        CompoundTag compoundTag = ((BasicAccess)easterBossEntity).ftboss$getQingyiBasic();
        double d = easterBossEntity.m_9236_().m_46467_() - compoundTag.m_128454_("start");
        return compoundTag.m_128471_("active") && BasicMotion.clips().containsKey(compoundTag.m_128461_("clip")) && compoundTag.m_128461_("weapon").equals(BasicServer.weapon(easterBossEntity)) && compoundTag.m_128471_("left") == (easterBossEntity.m_5737_() == HumanoidArm.LEFT) && BasicMotion.weight(compoundTag.m_128471_("preparation"), d, compoundTag.m_128451_("duration")) > 0.0;
    }

    public static float filterGeneric(float f, EasterBossEntity easterBossEntity) {
        return f != 0.0f && BasicPose.weaponOccupied(easterBossEntity) ? 0.0f : f;
    }

    public static void guard(EasterBossModel easterBossModel, boolean bl) {
        ModelPart modelPart = bl ? easterBossModel.f_102812_ : easterBossModel.f_102811_;
        modelPart.f_104203_ = Math.min(modelPart.f_104203_, GUARD_PITCH);
        modelPart.f_104205_ = bl ? Math.min(modelPart.f_104205_, -GUARD_ROLL) : Math.max(modelPart.f_104205_, GUARD_ROLL);
    }

    public static void applyTailGuard(EasterBossModel easterBossModel, boolean bl, double d) {
        if (d <= 0.0) {
            return;
        }
        BasicPose.blend(bl ? easterBossModel.f_102812_ : easterBossModel.f_102811_, new double[]{-6.0, 0.0, bl ? -12.0 : 12.0}, d, false);
        (bl ? easterBossModel.f_103374_ : easterBossModel.f_103375_).m_104315_(bl ? easterBossModel.f_102812_ : easterBossModel.f_102811_);
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2) {
        double d;
        if (BasicPose.armMeleeAnimationDisabled()) {
            return;
        }
        if (!BasicServer.supported(easterBossEntity)) {
            return;
        }
        double d2 = Math.max(0.0f, Math.min(1.0f, f - (float)easterBossEntity.f_19797_));
        double d3 = BasicPose.castWeight(easterBossEntity, (float)d2);
        if (d3 <= 0.0 && (easterBossEntity.isCastingLike() || easterBossEntity.isPerformingSkill())) {
            d3 = 1.0;
        }
        boolean bl = easterBossEntity.m_5737_() == HumanoidArm.LEFT;
        double d4 = BasicPose.weaponTail(easterBossEntity, d2);
        if (d3 <= 0.0) {
            BasicPose.guard(easterBossModel, bl);
        } else {
            BasicPose.applyTailGuard(easterBossModel, bl, d4);
        }
        CompoundTag compoundTag = ((BasicAccess)easterBossEntity).ftboss$getQingyiBasic();
        if (!compoundTag.m_128471_("active")) {
            return;
        }
        String string = compoundTag.m_128461_("clip");
        boolean bl2 = compoundTag.m_128471_("left");
        if (!BasicMotion.clips().containsKey(string) || !compoundTag.m_128461_("weapon").equals(BasicServer.weapon(easterBossEntity)) || bl2 != bl) {
            return;
        }
        double d5 = (double)(easterBossEntity.m_9236_().m_46467_() - compoundTag.m_128454_("start")) + d2;
        boolean bl3 = compoundTag.m_128471_("preparation");
        double d6 = BasicMotion.weight(bl3, d5, compoundTag.m_128451_("duration"));
        double d7 = d = d4 > 0.0 ? BasicPose.recoveryAllowance((double)(easterBossEntity.m_9236_().m_46467_() - compoundTag.m_128454_("tailStart")) + d2) : 1.0;
        if (d6 <= 0.0) {
            return;
        }
        double[][] dArray = BasicMotion.sampleAngles(string, BasicMotion.phase(bl3, d5, compoundTag.m_128451_("duration")), bl2);
        if (d3 > 0.0) {
            BasicPose.applyJointAngles(easterBossModel, dArray, d6, bl2, (double)Math.abs(f2) > 0.02, d3, 1.0 - d4 * (1.0 - d));
        } else {
            BasicPose.applyAngles(easterBossModel, dArray, d6, bl2, (double)Math.abs(f2) > 0.02, false);
            BasicPose.applyTailGuard(easterBossModel, bl2, d4 * (1.0 - d));
        }
    }

    public static double outsideYaw(double d) {
        return d <= -20.0 ? -12.0 : (d >= -4.0 ? d : -12.0 + (d + 20.0) * (d + 20.0) / 32.0);
    }

    public static void applyJointAngles(EasterBossModel easterBossModel, double[][] dArray, double d, boolean bl, boolean bl2, double d2) {
        BasicPose.applyJointAngles(easterBossModel, dArray, d, bl, bl2, d2, 1.0);
    }

    public static void applyJointAngles(EasterBossModel easterBossModel, double[][] dArray, double d, boolean bl, boolean bl2, double d2, double d3) {
        int n;
        if (d <= 0.0) {
            return;
        }
        double d4 = Math.max(0.0, Math.min(1.0, d2));
        double d5 = 1.0 - d4;
        double d6 = bl ? -1.0 : 1.0;
        int n2 = bl ? 3 : 2;
        ModelPart[] modelPartArray = new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
        int n3 = bl2 ? 4 : 6;
        double[][] dArray2 = null;
        if (d5 > 0.0) {
            double[][] dArray3 = new double[6][3];
            for (n = 0; n < n3; ++n) {
                dArray3[n] = new double[]{Math.toDegrees(modelPartArray[n].f_104203_), Math.toDegrees(modelPartArray[n].f_104204_), Math.toDegrees(modelPartArray[n].f_104205_)};
            }
            dArray2 = BasicMotion.positions(dArray3, !bl2);
        }
        for (int i = 0; i < n3; ++i) {
            double[] dArray4 = dArray[i];
            if (i == n2) {
                dArray4 = (double[])dArray4.clone();
                dArray4[1] = dArray4[1] + (d6 * BasicPose.outsideYaw(d6 * dArray4[1]) - dArray4[1]) * d4;
                BasicPose.blend(modelPartArray[i], dArray4, d * d3, false);
                continue;
            }
            if (!(d5 > 0.0)) continue;
            BasicPose.blend(modelPartArray[i], dArray4, d * d5, i == 0);
        }
        if (d5 > 0.0) {
            double[][] dArray5 = new double[6][3];
            for (n = 0; n < n3; ++n) {
                dArray5[n] = new double[]{Math.toDegrees(modelPartArray[n].f_104203_), Math.toDegrees(modelPartArray[n].f_104204_), Math.toDegrees(modelPartArray[n].f_104205_)};
            }
            double[][] dArray6 = BasicMotion.positions(dArray5, !bl2);
            for (int i = 0; i < n3; ++i) {
                ModelPart modelPart = modelPartArray[i];
                modelPart.f_104200_ += (float)((dArray6[i][0] - dArray2[i][0]) * d5 + (dArray2[i][0] - (double)modelPart.f_104200_) * d5 * d);
                modelPart.f_104201_ += (float)((dArray6[i][1] - dArray2[i][1]) * d5 + (dArray2[i][1] - (double)modelPart.f_104201_) * d5 * d);
                modelPart.f_104202_ += (float)((dArray6[i][2] - dArray2[i][2]) * d5 + (dArray2[i][2] - (double)modelPart.f_104202_) * d5 * d);
                if (i < 4) continue;
                modelPart.f_104201_ = Math.min(modelPart.f_104201_, (float)dArray6[i][1]);
            }
        }
        easterBossModel.f_102809_.m_104315_(easterBossModel.f_102808_);
        easterBossModel.f_103378_.m_104315_(easterBossModel.f_102810_);
        easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
        easterBossModel.f_103374_.m_104315_(easterBossModel.f_102812_);
        easterBossModel.f_103377_.m_104315_(easterBossModel.f_102813_);
        easterBossModel.f_103376_.m_104315_(easterBossModel.f_102814_);
    }

    public static void applyLayer(EasterBossModel easterBossModel, BasicMotion.State state, double d, boolean bl, boolean bl2, boolean bl3) {
        BasicPose.applyAngles(easterBossModel, state.angles(), d, bl, bl2, bl3);
    }

    private static void blend(ModelPart modelPart, double[] dArray, double d, boolean bl) {
        if (bl) {
            modelPart.f_104203_ += (float)(Math.toRadians(dArray[0]) * d);
            modelPart.f_104204_ += (float)(Math.toRadians(dArray[1]) * d);
        } else {
            modelPart.f_104203_ += (float)((Math.toRadians(dArray[0]) - (double)modelPart.f_104203_) * d);
            modelPart.f_104204_ += (float)((Math.toRadians(dArray[1]) - (double)modelPart.f_104204_) * d);
        }
        modelPart.f_104205_ += (float)((Math.toRadians(dArray[2]) - (double)modelPart.f_104205_) * d);
    }

    public static void applyAngles(EasterBossModel easterBossModel, double[][] dArray, double d, boolean bl, boolean bl2, boolean bl3) {
        if (d <= 0.0) {
            return;
        }
        if (bl3) {
            ModelPart modelPart = bl ? easterBossModel.f_102812_ : easterBossModel.f_102811_;
            BasicPose.blend(modelPart, dArray[bl ? 3 : 2], d, false);
            (bl ? easterBossModel.f_103374_ : easterBossModel.f_103375_).m_104315_(modelPart);
            return;
        }
        ModelPart[] modelPartArray = new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
        int n = bl2 ? 4 : 6;
        for (int i = 0; i < n; ++i) {
            BasicPose.blend(modelPartArray[i], dArray[i], d, i == 0);
        }
        double[][] dArray2 = new double[6][3];
        for (int i = 0; i < n; ++i) {
            dArray2[i] = new double[]{Math.toDegrees(modelPartArray[i].f_104203_), Math.toDegrees(modelPartArray[i].f_104204_), Math.toDegrees(modelPartArray[i].f_104205_)};
        }
        double[][] dArray3 = BasicMotion.positions(dArray2, !bl2);
        for (int i = 0; i < n; ++i) {
            modelPartArray[i].f_104200_ = (float)dArray3[i][0];
            modelPartArray[i].f_104201_ = (float)dArray3[i][1];
            modelPartArray[i].f_104202_ = (float)dArray3[i][2];
        }
        easterBossModel.f_102809_.m_104315_(easterBossModel.f_102808_);
        easterBossModel.f_103378_.m_104315_(easterBossModel.f_102810_);
        easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
        easterBossModel.f_103374_.m_104315_(easterBossModel.f_102812_);
        easterBossModel.f_103377_.m_104315_(easterBossModel.f_102813_);
        easterBossModel.f_103376_.m_104315_(easterBossModel.f_102814_);
    }

    public static final boolean armMeleeAnimationDisabled() {
        return true;
    }
}

