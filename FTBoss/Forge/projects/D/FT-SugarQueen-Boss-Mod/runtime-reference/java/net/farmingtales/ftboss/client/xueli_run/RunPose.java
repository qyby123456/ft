/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.HumanoidArm
 */
package net.farmingtales.ftboss.client.xueli_run;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.xueli_run.RunMath;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.xueli_cast_actions.CastServer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.HumanoidArm;

public final class RunPose {
    private static final Map<EasterBossEntity, Blend> BLENDS = new WeakHashMap<EasterBossEntity, Blend>();

    private RunPose() {
    }

    private static boolean active(EasterBossEntity easterBossEntity, CompoundTag compoundTag, double d) {
        double d2 = (double)(easterBossEntity.m_9236_().m_46467_() - compoundTag.m_128454_("start")) + d;
        return !compoundTag.m_128456_() && d2 >= 0.0 && d2 < (double)compoundTag.m_128451_("duration");
    }

    private static boolean action(EasterBossEntity easterBossEntity, double d) {
        if (easterBossEntity.isCastingLike() || easterBossEntity.isPerformingSkill() || (double)easterBossEntity.getCombatAnimationWeight((float)d) > 0.001) {
            return true;
        }
        if (RunPose.active(easterBossEntity, easterBossEntity.ftboss$getXueliBasic(), d)) {
            return true;
        }
        CompoundTag compoundTag = easterBossEntity.ftboss$getXueliCastActions();
        if (RunPose.active(easterBossEntity, compoundTag.m_128469_("native"), d) || RunPose.active(easterBossEntity, compoundTag.m_128469_("iron"), d)) {
            return true;
        }
        return RunPose.active(easterBossEntity, easterBossEntity.getSkillCue(), d);
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2, float f3) {
        if (easterBossEntity == null || easterBossEntity.getVariant() != 10) {
            return;
        }
        double d = Math.max(0.0f, Math.min(1.0f, f3 - (float)easterBossEntity.f_19797_));
        double d2 = easterBossEntity.m_20185_() - easterBossEntity.f_19790_;
        double d3 = easterBossEntity.m_20189_() - easterBossEntity.f_19792_;
        double d4 = Math.hypot(d2, d3);
        double d5 = Math.toRadians(easterBossEntity.m_146908_());
        double d6 = -d2 * Math.sin(d5) + d3 * Math.cos(d5);
        if (!(RunMath.eligible(easterBossEntity.getVariant(), easterBossEntity.m_6084_(), easterBossEntity.m_21525_(), easterBossEntity.m_20096_(), easterBossEntity.m_20069_(), easterBossEntity.m_20159_(), RunPose.action(easterBossEntity, d), d4 > 0.02 && d6 < -0.2 * d4, d4, f2) && Float.isFinite(f3) && Float.isFinite(f))) {
            BLENDS.remove(easterBossEntity);
            return;
        }
        String string = CastServer.weapon(easterBossEntity);
        String string2 = string.equals("pin") || string.equals("pan") ? string : "empty";
        double d7 = RunMath.activation(d4);
        Blend blend = BLENDS.get(easterBossEntity);
        double d8 = f3;
        if (blend != null && (d8 < blend.time() || d8 - blend.time() > 4.0)) {
            blend = null;
        }
        double d9 = blend == null ? 0.0 : d8 - blend.time();
        double d10 = RunMath.smooth(blend == null ? 0.0 : blend.weight(), d7, d9);
        BLENDS.put(easterBossEntity, new Blend(d8, d10));
        if (d10 < 0.001) {
            return;
        }
        double d11 = (double)f * RunMath.PHASE_FACTOR;
        RunPose.pose(easterBossModel, RunMath.sample(string2, d11, easterBossEntity.m_5737_() == HumanoidArm.LEFT, d7), d11, d10);
    }

    public static void pose(EasterBossModel easterBossModel, RunMath.State state, double d, double d2) {
        if (d2 <= 0.0) {
            return;
        }
        ModelPart[] modelPartArray = new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
        double[][] dArray = new double[6][3];
        for (int i = 0; i < 6; ++i) {
            ModelPart modelPart = modelPartArray[i];
            if (i == 0) {
                modelPart.f_104203_ += (float)Math.toRadians(state.angles()[i][0] * d2);
                modelPart.f_104205_ += (float)Math.toRadians(state.angles()[i][2] * d2);
            } else {
                modelPart.f_104203_ += (float)((Math.toRadians(state.angles()[i][0]) - (double)modelPart.f_104203_) * d2);
                modelPart.f_104204_ += (float)((Math.toRadians(state.angles()[i][1]) - (double)modelPart.f_104204_) * d2);
                modelPart.f_104205_ += (float)((Math.toRadians(state.angles()[i][2]) - (double)modelPart.f_104205_) * d2);
            }
            dArray[i] = new double[]{Math.toDegrees(modelPart.f_104203_), Math.toDegrees(modelPart.f_104204_), Math.toDegrees(modelPart.f_104205_)};
        }
        double[][] dArray2 = RunMath.positions(dArray, d, d2);
        for (int i = 0; i < 6; ++i) {
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

    private record Blend(double time, double weight) {
    }
}

