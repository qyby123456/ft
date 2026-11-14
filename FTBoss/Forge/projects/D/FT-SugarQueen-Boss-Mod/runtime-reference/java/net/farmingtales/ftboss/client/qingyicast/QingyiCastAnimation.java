/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.HumanoidArm
 */
package net.farmingtales.ftboss.client.qingyicast;

import java.lang.reflect.Method;
import net.farmingtales.ftboss.animation.qingyicast.QingyiCastAccess;
import net.farmingtales.ftboss.animation.qingyicast.QingyiCastTime;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.qingyicast.QingyiCastPose;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.HumanoidArm;

public final class QingyiCastAnimation {
    private static final Method BASIC_HAND = QingyiCastAnimation.basicHand();

    private QingyiCastAnimation() {
    }

    private static Method basicHand() {
        try {
            return Class.forName("net.farmingtales.ftboss.client.qingyi_basic.BasicPose", false, QingyiCastAnimation.class.getClassLoader()).getMethod("weaponOccupied", EasterBossEntity.class);
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            return null;
        }
    }

    private static boolean occupied(EasterBossEntity easterBossEntity) {
        if (BASIC_HAND == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(BASIC_HAND.invoke(null, easterBossEntity));
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            return false;
        }
    }

    private static QingyiCastTime.Sample time(CompoundTag compoundTag, long l, double d) {
        int n = compoundTag.m_128451_("stage");
        long l2 = compoundTag.m_128454_("start");
        long l3 = compoundTag.m_128454_("stop");
        if (compoundTag.m_128456_() || n == 0 && l - l2 > (long)(compoundTag.m_128451_("duration") + 24) || n != 0 && l3 < l2 || l - l2 > 240L) {
            return new QingyiCastTime.Sample(0.0, 0.0);
        }
        return QingyiCastTime.sample(compoundTag.m_128451_("track"), (double)(l - (n == 0 ? l2 : l3)) + d, compoundTag.m_128451_("duration"), n, l3 - l2, compoundTag.m_128471_("final"), compoundTag.m_128459_("origin"), compoundTag.m_128454_("finalAt") - l3);
    }

    public static Frame layer(CompoundTag compoundTag, long l, double d) {
        CompoundTag compoundTag2 = compoundTag.m_128469_("native");
        QingyiCastTime.Sample sample = QingyiCastAnimation.time(compoundTag2, l, d);
        if (sample.weight() <= 0.0) {
            compoundTag2 = compoundTag.m_128469_("iron");
            sample = QingyiCastAnimation.time(compoundTag2, l, d);
        }
        return sample.weight() > 0.0 ? new Frame(compoundTag2, sample) : null;
    }

    private static Frame selected(EasterBossEntity easterBossEntity, float f) {
        if (easterBossEntity.getVariant() != 5 || !easterBossEntity.m_6084_() || easterBossEntity.isMini() || !(easterBossEntity instanceof QingyiCastAccess)) {
            return null;
        }
        QingyiCastAccess qingyiCastAccess = easterBossEntity;
        long l = easterBossEntity.m_9236_().m_46467_();
        Frame frame = QingyiCastAnimation.layer(qingyiCastAccess.qingyiCastState(), l, f);
        if (frame == null) {
            return null;
        }
        CompoundTag compoundTag = frame.cue();
        long l2 = l - (long)Math.round(easterBossEntity.getCombatAnimationAge(0.0f));
        long l3 = Math.max(Math.max(compoundTag.m_128454_("start"), compoundTag.m_128454_("stop")), compoundTag.m_128454_("finalAt"));
        return !QingyiCastAnimation.occupied(easterBossEntity) && easterBossEntity.getCombatAnimationWeight(f) > 0.0f && l2 > l3 && !easterBossEntity.isCastingLike() ? null : frame;
    }

    public static boolean layerActive(EasterBossEntity easterBossEntity, float f) {
        return QingyiCastAnimation.selected(easterBossEntity, f) != null;
    }

    public static double layerWeight(EasterBossEntity easterBossEntity, float f) {
        Frame frame = QingyiCastAnimation.selected(easterBossEntity, f);
        return frame == null ? 0.0 : frame.timing().weight();
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5) {
        double d = Math.max(0.0f, Math.min(1.0f, f3 - (float)easterBossEntity.f_19797_));
        Frame frame = QingyiCastAnimation.selected(easterBossEntity, (float)d);
        if (frame == null) {
            return;
        }
        CompoundTag compoundTag = frame.cue();
        QingyiCastTime.Sample sample = frame.timing();
        QingyiCastPose.Pose pose = QingyiCastPose.sample(compoundTag.m_128451_("track"), sample.progress(), easterBossEntity.m_5737_() == HumanoidArm.LEFT, f2, f, (double)f4 * Math.PI / 180.0, (double)f5 * Math.PI / 180.0);
        QingyiCastAnimation.applyPose(easterBossModel, pose, sample.weight());
    }

    public static void applyPose(EasterBossModel easterBossModel, QingyiCastPose.Pose pose, double d) {
        ModelPart modelPart;
        int n;
        ModelPart[] modelPartArray = new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
        float f = (float)Math.max(0.0, Math.min(1.0, d));
        if (f <= 0.0f) {
            return;
        }
        for (n = 0; n < modelPartArray.length; ++n) {
            modelPart = modelPartArray[n];
            modelPart.f_104200_ = (float)((double)modelPart.f_104200_ + (pose.position[n][0] - (double)modelPart.f_104200_) * (double)f);
            modelPart.f_104201_ = (float)((double)modelPart.f_104201_ + (pose.position[n][1] - (double)modelPart.f_104201_) * (double)f);
            modelPart.f_104202_ = (float)((double)modelPart.f_104202_ + (pose.position[n][2] - (double)modelPart.f_104202_) * (double)f);
            modelPart.f_104203_ = (float)((double)modelPart.f_104203_ + (pose.rotation[n][0] - (double)modelPart.f_104203_) * (double)f);
            modelPart.f_104204_ = (float)((double)modelPart.f_104204_ + (pose.rotation[n][1] - (double)modelPart.f_104204_) * (double)f);
            modelPart.f_104205_ = (float)((double)modelPart.f_104205_ + (pose.rotation[n][2] - (double)modelPart.f_104205_) * (double)f);
        }
        for (n = 4; n < 6; ++n) {
            modelPart = modelPartArray[n];
            double d2 = Math.sin(modelPart.f_104203_);
            double d3 = Math.cos(modelPart.f_104203_);
            double d4 = Math.sin(modelPart.f_104204_);
            double d5 = Math.cos(modelPart.f_104204_);
            double d6 = Math.sin(modelPart.f_104205_);
            double d7 = Math.cos(modelPart.f_104205_);
            double d8 = d6 * d5;
            double d9 = d6 * d4 * d2 + d7 * d3;
            double d10 = d6 * d4 * d3 - d7 * d2;
            double d11 = 12.0 * Math.max(0.0, d9) + 2.0 * Math.abs(d8) + 2.0 * Math.abs(d10);
            modelPart.f_104201_ = Math.min(modelPart.f_104201_, (float)(24.0 - d11));
        }
        easterBossModel.f_102809_.m_104315_(easterBossModel.f_102808_);
        easterBossModel.f_103378_.m_104315_(easterBossModel.f_102810_);
        easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
        easterBossModel.f_103374_.m_104315_(easterBossModel.f_102812_);
        easterBossModel.f_103377_.m_104315_(easterBossModel.f_102813_);
        easterBossModel.f_103376_.m_104315_(easterBossModel.f_102814_);
    }

    public record Frame(CompoundTag cue, QingyiCastTime.Sample timing) {
    }
}

