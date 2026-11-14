/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.HumanoidArm
 */
package net.farmingtales.ftboss.client.xueli_cast_actions;

import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.xueli_cast_actions.CastMotion;
import net.farmingtales.ftboss.xueli_cast_actions.CastServer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.HumanoidArm;

public final class CastPose {
    private CastPose() {
    }

    private static double age(EasterBossEntity easterBossEntity, CompoundTag compoundTag, float f) {
        return (float)(easterBossEntity.m_9236_().m_46467_() - compoundTag.m_128454_("start")) + f;
    }

    private static boolean active(EasterBossEntity easterBossEntity, CompoundTag compoundTag, float f) {
        return !compoundTag.m_128456_() && CastServer.validCue(compoundTag.m_128461_("id"), compoundTag.m_128461_("stage"), compoundTag.m_128451_("duration")) && CastMotion.clips().containsKey(compoundTag.m_128461_("id")) && compoundTag.m_128461_("weapon").equals(CastServer.weapon(easterBossEntity)) && compoundTag.m_128471_("left") == (easterBossEntity.m_5737_() == HumanoidArm.LEFT) && CastPose.age(easterBossEntity, compoundTag, f) >= 0.0 && CastPose.age(easterBossEntity, compoundTag, f) < (double)compoundTag.m_128451_("duration");
    }

    private static CompoundTag cue(EasterBossEntity easterBossEntity, float f) {
        CompoundTag compoundTag = easterBossEntity.ftboss$getXueliCastActions();
        CompoundTag compoundTag2 = compoundTag.m_128469_("native");
        if (CastPose.active(easterBossEntity, compoundTag2, f)) {
            return compoundTag2;
        }
        CompoundTag compoundTag3 = compoundTag.m_128469_("iron");
        if (!CastPose.active(easterBossEntity, compoundTag3, f) || easterBossEntity.isPerformingSkill() || !easterBossEntity.isCastingLike() && easterBossEntity.getCombatAnimationWeight(f) > 0.0f) {
            return new CompoundTag();
        }
        return compoundTag3;
    }

    public static float filterGeneric(float f, EasterBossEntity easterBossEntity) {
        return CastServer.supported(easterBossEntity) && !CastPose.cue(easterBossEntity, 0.0f).m_128456_() ? 0.0f : f;
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2) {
        if (!CastServer.supported(easterBossEntity)) {
            return;
        }
        float f3 = Math.max(0.0f, Math.min(1.0f, f - (float)easterBossEntity.f_19797_));
        CompoundTag compoundTag = CastPose.cue(easterBossEntity, f3);
        if (compoundTag.m_128456_()) {
            return;
        }
        String string = compoundTag.m_128461_("stage");
        double d = CastPose.age(easterBossEntity, compoundTag, f3);
        double d2 = CastMotion.weight(string, d, compoundTag.m_128451_("duration"));
        double d3 = CastMotion.phase(string, d, compoundTag.m_128451_("duration"));
        boolean bl = compoundTag.m_128471_("left");
        CastMotion.State state = CastMotion.combined(compoundTag.m_128461_("id"), compoundTag.m_128461_("secondary"), d3, bl);
        if (compoundTag.m_128461_("id").equals("slam_cast") && string.equals("charge")) {
            CastPose.tuckSlam(easterBossModel);
        }
        CastPose.pose(easterBossModel, state, d2, (double)Math.abs(f2) > 0.02);
    }

    public static void tuckSlam(EasterBossModel easterBossModel) {
        easterBossModel.f_102808_.f_104203_ = (float)Math.max(-0.7853981633974483, Math.min(0.7853981633974483, (double)easterBossModel.f_102808_.f_104203_));
        easterBossModel.f_102808_.f_104204_ = (float)Math.max(-Math.toRadians(25.0), Math.min(Math.toRadians(25.0), (double)easterBossModel.f_102808_.f_104204_));
    }

    public static void pose(EasterBossModel easterBossModel, CastMotion.State state, double d, boolean bl) {
        ModelPart[] modelPartArray = new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
        double[][] dArray = new double[6][3];
        for (int i = 0; i < 6; ++i) {
            ModelPart modelPart = modelPartArray[i];
            if (i < 4 || !bl) {
                if (i == 0) {
                    modelPart.f_104203_ += (float)(Math.toRadians(state.angles()[i][0]) * d);
                    modelPart.f_104204_ += (float)(Math.toRadians(state.angles()[i][1]) * d);
                } else {
                    modelPart.f_104203_ += (float)((Math.toRadians(state.angles()[i][0]) - (double)modelPart.f_104203_) * d);
                    modelPart.f_104204_ += (float)((Math.toRadians(state.angles()[i][1]) - (double)modelPart.f_104204_) * d);
                }
                modelPart.f_104205_ += (float)((Math.toRadians(state.angles()[i][2]) - (double)modelPart.f_104205_) * d);
            }
            dArray[i] = new double[]{Math.toDegrees(modelPart.f_104203_), Math.toDegrees(modelPart.f_104204_), Math.toDegrees(modelPart.f_104205_)};
        }
        double[][] dArray2 = CastMotion.positions(dArray);
        for (int i = 0; i < (bl ? 4 : 6); ++i) {
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
}

