/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.nbt.CompoundTag
 */
package net.farmingtales.ftboss.dandanmotion.client;

import net.farmingtales.ftboss.dandanmotion.MotionAccess;
import net.farmingtales.ftboss.dandanmotion.MotionCurves;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;

public final class MotionActor {
    private MotionActor() {
    }

    public static void animate(PlayerModel<?> playerModel, EasterBossEntity easterBossEntity, float f) {
        double d;
        double d2;
        if (easterBossEntity.getVariant() != 1 || easterBossEntity.isMini() || !easterBossEntity.m_6084_() || !(easterBossEntity instanceof MotionAccess)) {
            return;
        }
        MotionAccess motionAccess = easterBossEntity;
        float f2 = (float)MotionCurves.clamp(f - (float)easterBossEntity.f_19797_);
        CompoundTag compoundTag = motionAccess.dandanMotionData();
        String string = compoundTag.m_128461_("dandan_motion_clip");
        int n = compoundTag.m_128451_("dandan_motion_ticks");
        double d3 = (float)(easterBossEntity.m_9236_().m_46467_() - compoundTag.m_128454_("dandan_motion_start")) + f2;
        if (compoundTag.m_128451_("dandan_motion_schema") == 1 && MotionCurves.has(string) && n > 0 && n <= 200 && d3 >= 0.0 && d3 < (double)n) {
            d2 = d3 / (double)n;
            d = 1.0;
            if (compoundTag.m_128471_("dandan_motion_reverse")) {
                d2 = 1.0 - d2;
            }
        } else {
            if (easterBossEntity.isCastingLike()) {
                return;
            }
            d = easterBossEntity.getCombatAnimationWeight(f2);
            if (d <= 0.0) {
                return;
            }
            if (compoundTag.m_128451_("dandan_motion_schema") == 1 && MotionCurves.has(string) && d3 >= (double)n) {
                d2 = compoundTag.m_128471_("dandan_motion_reverse") ? 0.0 : 1.0;
            } else {
                return;
            }
        }
        double[][] dArray = MotionCurves.resolve(MotionCurves.sample(string, d2));
        ModelPart[] modelPartArray = new ModelPart[]{playerModel.f_102808_, playerModel.f_102810_, playerModel.f_102811_, playerModel.f_102812_};
        for (int i = 0; i < modelPartArray.length; ++i) {
            ModelPart modelPart = modelPartArray[i];
            modelPart.f_104200_ = (float)((double)modelPart.f_104200_ * (1.0 - d) + dArray[i][0] * d);
            modelPart.f_104201_ = (float)((double)modelPart.f_104201_ * (1.0 - d) + dArray[i][1] * d);
            modelPart.f_104202_ = (float)((double)modelPart.f_104202_ * (1.0 - d) + dArray[i][2] * d);
            modelPart.f_104203_ = (float)((double)modelPart.f_104203_ * (1.0 - d) + Math.toRadians(dArray[i][3]) * d);
            modelPart.f_104204_ = (float)((double)modelPart.f_104204_ * (1.0 - d) + Math.toRadians(dArray[i][4]) * d);
            modelPart.f_104205_ = (float)((double)modelPart.f_104205_ * (1.0 - d) + Math.toRadians(dArray[i][5]) * d);
        }
        playerModel.f_102809_.m_104315_(playerModel.f_102808_);
        playerModel.f_103378_.m_104315_(playerModel.f_102810_);
        playerModel.f_103374_.m_104315_(playerModel.f_102812_);
        playerModel.f_103375_.m_104315_(playerModel.f_102811_);
    }
}

