/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.nbt.CompoundTag
 */
package net.farmingtales.ftboss.client.manorrun;

import java.util.WeakHashMap;
import net.farmingtales.ftboss.client.manorrun.RunCycle;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.qizairun.QizaiRunClient;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.XuanlanMoonWeaponItem;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;

public final class ManorRunAnimation {
    private static final WeakHashMap<EasterBossEntity, RunCycle.State> STATES = new WeakHashMap();

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f) {
        if (QizaiRunClient.owns(easterBossEntity)) {
            return;
        }
        if (!EasterBossEntity.isManorVariant(easterBossEntity.getVariant())) {
            return;
        }
        float f2 = (float)RunCycle.clamp(f - (float)easterBossEntity.f_19797_, 0.0, 1.0);
        RunCycle.State state = ManorRunAnimation.prepare(easterBossEntity, f);
        if (state == null) {
            return;
        }
        boolean bl = ManorRunAnimation.blocked(easterBossEntity, f2);
        if (bl) {
            return;
        }
        double d = state.previousWeight + (state.weight - state.previousWeight) * (double)f2;
        if (d < 1.0E-4 || easterBossEntity.getVariant() == 3) {
            return;
        }
        double d2 = state.previousPhase + (state.phase - state.previousPhase) * (double)f2;
        int n = easterBossEntity.getVariant() == 6 && XuanlanMoonWeaponItem.isWhite(easterBossEntity.m_21205_()) ? 12 : easterBossEntity.getVariant();
        RunCycle.Pose pose = RunCycle.pose(n, state.speed, d2);
        double d3 = pose.lean() * state.travelSign;
        float f3 = (float)(d3 * d);
        easterBossModel.f_102810_.f_104203_ += f3;
        easterBossModel.f_102811_.f_104201_ = easterBossModel.f_102812_.f_104201_ = 2.0f * (float)Math.cos(f3);
        easterBossModel.f_102811_.f_104202_ = easterBossModel.f_102812_.f_104202_ = 2.0f * (float)Math.sin(f3);
        easterBossModel.f_102813_.f_104201_ = easterBossModel.f_102814_.f_104201_ = 12.0f * (float)Math.cos(f3);
        easterBossModel.f_102813_.f_104202_ = easterBossModel.f_102814_.f_104202_ = 12.0f * (float)Math.sin(f3);
        ManorRunAnimation.rotate(easterBossModel.f_102813_, pose.leg() + d3, 0.0, 0.0, d);
        ManorRunAnimation.rotate(easterBossModel.f_102814_, -pose.leg() + d3, 0.0, 0.0, d);
        ManorRunAnimation.rotate(easterBossModel.f_102811_, pose.rightArm() + d3, 0.0, pose.roll(), d);
        ManorRunAnimation.rotate(easterBossModel.f_102812_, pose.leftArm() + d3, 0.0, -pose.roll(), d);
        float f4 = easterBossEntity.getVariant() == 11 ? 0.25f : 0.0f;
        float f5 = Math.max(0.0f, Math.max(ManorRunAnimation.footY(easterBossModel.f_102813_, f4), ManorRunAnimation.footY(easterBossModel.f_102814_, f4)) - 24.0f);
        easterBossModel.f_102808_.f_104201_ -= f5;
        easterBossModel.f_102810_.f_104201_ -= f5;
        easterBossModel.f_102811_.f_104201_ -= f5;
        easterBossModel.f_102812_.f_104201_ -= f5;
        easterBossModel.f_102813_.f_104201_ -= f5;
        easterBossModel.f_102814_.f_104201_ -= f5;
        easterBossModel.f_102809_.m_104315_(easterBossModel.f_102808_);
        easterBossModel.f_103374_.m_104315_(easterBossModel.f_102812_);
        easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
        easterBossModel.f_103376_.m_104315_(easterBossModel.f_102814_);
        easterBossModel.f_103377_.m_104315_(easterBossModel.f_102813_);
        easterBossModel.f_103378_.m_104315_(easterBossModel.f_102810_);
    }

    static RunCycle.State current(EasterBossEntity easterBossEntity) {
        return STATES.get(easterBossEntity);
    }

    static RunCycle.State prepare(EasterBossEntity easterBossEntity2, float f) {
        if (!easterBossEntity2.m_6084_() || easterBossEntity2.m_213877_()) {
            STATES.remove(easterBossEntity2);
            return null;
        }
        float f2 = (float)RunCycle.clamp(f - (float)easterBossEntity2.f_19797_, 0.0, 1.0);
        RunCycle.State state = STATES.computeIfAbsent(easterBossEntity2, easterBossEntity -> new RunCycle.State());
        double d = easterBossEntity2.m_20185_() - state.lastX;
        double d2 = easterBossEntity2.m_20189_() - state.lastZ;
        double d3 = Math.toRadians(easterBossEntity2.f_20883_);
        double d4 = -d * Math.sin(d3) + d2 * Math.cos(d3);
        state.update(easterBossEntity2.f_19797_, easterBossEntity2.m_20185_(), easterBossEntity2.m_20189_(), ManorRunAnimation.blocked(easterBossEntity2, f2), d4);
        return state;
    }

    static boolean blocked(EasterBossEntity easterBossEntity, float f) {
        CompoundTag compoundTag = easterBossEntity.getSkillCue();
        double d = (float)(easterBossEntity.m_9236_().m_46467_() - compoundTag.m_128454_("start")) + f;
        boolean bl = compoundTag.m_128441_("start") && compoundTag.m_128451_("duration") > 0 && d >= 0.0 && d < (double)compoundTag.m_128451_("duration");
        return !easterBossEntity.m_6084_() || easterBossEntity.m_213877_() || !easterBossEntity.m_20096_() || easterBossEntity.m_20069_() || easterBossEntity.m_20159_() || easterBossEntity.m_21525_() || easterBossEntity.m_5803_() || easterBossEntity.f_20916_ > 0 || easterBossEntity.isCastingLike() || easterBossEntity.isPerformingSkill() || easterBossEntity.getCombatAnimationWeight(f) > 0.001f || easterBossEntity.isDoraReloading() || easterBossEntity.getDoraSpearTicks() > 0 || easterBossEntity.isManorIntroHidden() || easterBossEntity.isXuanlanSkyHidden() || bl;
    }

    private static float footY(ModelPart modelPart, float f) {
        double d = Math.sin(modelPart.f_104203_);
        double d2 = Math.cos(modelPart.f_104203_);
        double d3 = Math.sin(modelPart.f_104204_);
        double d4 = Math.cos(modelPart.f_104204_);
        double d5 = Math.sin(modelPart.f_104205_);
        double d6 = Math.cos(modelPart.f_104205_);
        double d7 = d5 * d4;
        double d8 = d5 * d3 * d + d6 * d2;
        double d9 = d5 * d3 * d2 - d6 * d;
        return (float)((double)modelPart.f_104201_ + 6.0 * d8 + (double)(6.0f + f) * Math.abs(d8) + (double)(2.0f + f) * (Math.abs(d7) + Math.abs(d9)));
    }

    private static void rotate(ModelPart modelPart, double d, double d2, double d3, double d4) {
        modelPart.f_104203_ += (float)((d - (double)modelPart.f_104203_) * d4);
        modelPart.f_104204_ += (float)((d2 - (double)modelPart.f_104204_) * d4);
        modelPart.f_104205_ += (float)((d3 - (double)modelPart.f_104205_) * d4);
    }

    private ManorRunAnimation() {
    }
}

