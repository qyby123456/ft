/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.nbt.CompoundTag
 */
package net.farmingtales.ftboss.client.xueli_basic;

import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.xueli_basic.BasicAccess;
import net.farmingtales.ftboss.xueli_basic.BasicMath;
import net.farmingtales.ftboss.xueli_basic.BasicServer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;

public final class BasicPose {
    private BasicPose() {
    }

    private static CompoundTag cue(EasterBossEntity easterBossEntity) {
        CompoundTag compoundTag;
        if (easterBossEntity instanceof BasicAccess) {
            BasicAccess basicAccess = easterBossEntity;
            compoundTag = basicAccess.ftboss$getXueliBasic();
        } else {
            compoundTag = new CompoundTag();
        }
        return compoundTag;
    }

    public static float filterBlend(float f, EasterBossEntity easterBossEntity) {
        CompoundTag compoundTag = BasicPose.cue(easterBossEntity);
        return BasicServer.supported(easterBossEntity) && !compoundTag.m_128456_() && compoundTag.m_128461_("weapon").equals(BasicServer.weapon(easterBossEntity)) ? 0.0f : f;
    }

    private static float mix(float f, double d, double d2) {
        return (float)((double)f + (d - (double)f) * d2);
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2) {
        if (BasicPose.armMeleeAnimationDisabled()) {
            return;
        }
        if (!BasicServer.supported(easterBossEntity)) {
            return;
        }
        CompoundTag compoundTag = BasicPose.cue(easterBossEntity);
        String string = compoundTag.m_128461_("attack");
        if (!BasicMath.attacks().containsKey(string) || !compoundTag.m_128461_("weapon").equals(BasicServer.weapon(easterBossEntity))) {
            return;
        }
        double d = Math.max(0.0f, Math.min(1.0f, f - (float)easterBossEntity.f_19797_));
        double d2 = (double)(easterBossEntity.m_9236_().m_46467_() - compoundTag.m_128454_("start")) + d;
        int n = compoundTag.m_128451_("duration");
        boolean bl = compoundTag.m_128461_("phase").equals("wind");
        double d3 = BasicMath.envelope(bl, d2, n);
        if (d3 <= 0.0) {
            return;
        }
        BasicMath.Pose pose = BasicMath.sample(string, BasicMath.timing(bl, d2, n), compoundTag.m_128471_("left"));
        ModelPart[] modelPartArray = new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
        double[][] dArray = new double[6][3];
        boolean bl2 = Math.abs(f2) > 0.02f;
        for (int i = 0; i < 6; ++i) {
            ModelPart modelPart = modelPartArray[i];
            if (i < 4 || !bl2) {
                if (i == 0) {
                    modelPart.f_104203_ += (float)(Math.toRadians(pose.angles()[i][0]) * d3);
                    modelPart.f_104204_ += (float)(Math.toRadians(pose.angles()[i][1]) * d3);
                } else {
                    modelPart.f_104203_ = BasicPose.mix(modelPart.f_104203_, Math.toRadians(pose.angles()[i][0]), d3);
                    modelPart.f_104204_ = BasicPose.mix(modelPart.f_104204_, Math.toRadians(pose.angles()[i][1]), d3);
                }
                modelPart.f_104205_ = BasicPose.mix(modelPart.f_104205_, Math.toRadians(pose.angles()[i][2]), d3);
            }
            dArray[i] = new double[]{Math.toDegrees(modelPart.f_104203_), Math.toDegrees(modelPart.f_104204_), Math.toDegrees(modelPart.f_104205_)};
        }
        double[][] dArray2 = BasicMath.stance(dArray);
        for (int i = 0; i < (bl2 ? 4 : 6); ++i) {
            modelPartArray[i].f_104200_ = (float)dArray2[i][0];
            modelPartArray[i].f_104201_ = (float)dArray2[i][1];
            modelPartArray[i].f_104202_ = (float)dArray2[i][2];
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

