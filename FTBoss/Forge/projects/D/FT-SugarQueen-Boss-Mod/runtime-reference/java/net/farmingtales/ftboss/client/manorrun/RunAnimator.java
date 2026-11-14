/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.world.entity.HumanoidArm
 */
package net.farmingtales.ftboss.client.manorrun;

import java.util.WeakHashMap;
import net.farmingtales.ftboss.client.manorrun.RunGait;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.qizairun.QizaiRunClient;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;

public final class RunAnimator {
    private static final WeakHashMap<EasterBossEntity, Frame> FRAMES = new WeakHashMap();

    private RunAnimator() {
    }

    private static ModelPart[] parts(EasterBossModel easterBossModel) {
        return new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
    }

    public static void prepare(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity2, float f, float f2, float f3) {
        if (QizaiRunClient.owns(easterBossEntity2)) {
            return;
        }
        if (EasterBossEntity.isManorVariant(easterBossEntity2.getVariant())) {
            return;
        }
        Frame frame = FRAMES.computeIfAbsent(easterBossEntity2, easterBossEntity -> new Frame());
        frame.captured = false;
        if (easterBossEntity2.getVariant() < 0 || easterBossEntity2.getVariant() >= RunGait.count() || !easterBossEntity2.m_6084_() || easterBossEntity2.isMini() || easterBossEntity2.isGiant()) {
            frame.clock.update(f3, 0.0, false, 0.0, easterBossEntity2.getVariant());
            return;
        }
        double d = easterBossEntity2.m_20185_() - easterBossEntity2.f_19854_;
        double d2 = easterBossEntity2.m_20189_() - easterBossEntity2.f_19856_;
        double d3 = Math.hypot(d, d2);
        boolean bl = easterBossEntity2.m_20096_() && !easterBossEntity2.m_20069_() && !easterBossEntity2.m_20159_() && !easterBossEntity2.m_21255_() && !easterBossEntity2.m_21525_();
        frame.clock.update(f3, d3, bl, (double)f * 0.6662, easterBossEntity2.getVariant());
        if (frame.clock.blend <= 0.001 || !Float.isFinite(f) || !Float.isFinite(f2)) {
            return;
        }
        double d4 = Math.toRadians(easterBossEntity2.f_20883_);
        if (d3 > 0.001) {
            frame.forward = (d2 * Math.cos(d4) - d * Math.sin(d4)) / d3;
            frame.sideways = (d * Math.cos(d4) + d2 * Math.sin(d4)) / d3;
        }
        if (!Double.isFinite(frame.forward) || !Double.isFinite(frame.sideways)) {
            return;
        }
        ModelPart[] modelPartArray = RunAnimator.parts(easterBossModel);
        for (int i = 0; i < modelPartArray.length; ++i) {
            ModelPart modelPart = modelPartArray[i];
            int n = i * 6;
            frame.base[n] = modelPart.f_104200_;
            frame.base[n + 1] = modelPart.f_104201_;
            frame.base[n + 2] = modelPart.f_104202_;
            frame.base[n + 3] = modelPart.f_104203_;
            frame.base[n + 4] = modelPart.f_104204_;
            frame.base[n + 5] = modelPart.f_104205_;
        }
        frame.age = f3;
        frame.captured = true;
    }

    private static double allowance(ModelPart modelPart, double[] dArray, int n) {
        double d = Math.max(Math.abs((double)modelPart.f_104203_ - dArray[n + 3]), Math.max(Math.abs((double)modelPart.f_104204_ - dArray[n + 4]), Math.abs((double)modelPart.f_104205_ - dArray[n + 5])));
        double d2 = Math.max(Math.abs((double)modelPart.f_104200_ - dArray[n]), Math.max(Math.abs((double)modelPart.f_104201_ - dArray[n + 1]), Math.abs((double)modelPart.f_104202_ - dArray[n + 2])));
        return RunGait.channelAllowance(d, d2);
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f) {
        if (QizaiRunClient.owns(easterBossEntity)) {
            return;
        }
        if (EasterBossEntity.isManorVariant(easterBossEntity.getVariant())) {
            return;
        }
        Frame frame = FRAMES.get(easterBossEntity);
        if (frame == null || !frame.captured || frame.age != f) {
            return;
        }
        frame.captured = false;
        if (!easterBossEntity.m_6084_() || frame.clock.blend <= 0.001) {
            return;
        }
        boolean bl = easterBossEntity.m_5737_() == HumanoidArm.LEFT;
        boolean bl2 = !easterBossEntity.m_21205_().m_41619_();
        RunGait.sample(easterBossEntity.getVariant(), frame.clock.phase, frame.clock.speed, bl, bl2, frame.pose);
        RunGait.orient(frame.pose, frame.forward, frame.sideways);
        ModelPart[] modelPartArray = RunAnimator.parts(easterBossModel);
        ModelPart[] modelPartArray2 = new ModelPart[]{easterBossModel.f_102809_, easterBossModel.f_103378_, easterBossModel.f_103375_, easterBossModel.f_103374_, easterBossModel.f_103377_, easterBossModel.f_103376_};
        double d = Math.max(0.0f, Math.min(1.0f, easterBossEntity.getCombatAnimationWeight(Math.max(0.0f, Math.min(1.0f, f - (float)easterBossEntity.f_19797_)))));
        double d2 = (1.0 - RunGait.smooth(d / 0.25)) * RunAnimator.allowance(modelPartArray[1], frame.base, 6);
        if (easterBossEntity.isCastingLike()) {
            d2 = 0.0;
        }
        for (int i = 0; i < modelPartArray.length; ++i) {
            ModelPart modelPart = modelPartArray[i];
            int n = i * 6;
            double d3 = RunAnimator.allowance(modelPart, frame.base, n);
            double d4 = frame.clock.blend * (i < 4 ? d2 * RunGait.channelAllowance(0.0, Math.max(Math.abs((double)modelPart.f_104201_ - frame.base[n + 1]), Math.abs((double)modelPart.f_104202_ - frame.base[n + 2]))) : d3);
            double d5 = frame.clock.blend * (i < 4 ? d2 * d3 : d3);
            if (i > 1 && i < 4) {
                boolean bl3;
                boolean bl4 = i == (bl ? 3 : 2);
                boolean bl5 = bl3 = easterBossEntity.getVariant() == 2 || easterBossEntity.getVariant() == 6 || easterBossEntity.getVariant() == 11;
                if (easterBossEntity.isCastingLike() || d > 0.04 || bl4 && bl2 && bl3 || !bl4 && !easterBossEntity.m_21206_().m_41619_()) {
                    d5 = 0.0;
                }
            }
            if (d4 <= 0.001 && d5 <= 0.001) continue;
            float f2 = modelPart.f_104200_;
            float f3 = modelPart.f_104201_;
            float f4 = modelPart.f_104202_;
            float f5 = modelPart.f_104203_;
            float f6 = modelPart.f_104204_;
            float f7 = modelPart.f_104205_;
            modelPart.f_104200_ += (float)(frame.pose[n] * d4);
            modelPart.f_104201_ += (float)(frame.pose[n + 1] * d4);
            modelPart.f_104202_ += (float)(frame.pose[n + 2] * d4);
            modelPart.f_104203_ = modelPart.f_104203_ + (float)((frame.pose[n + 3] - (i == 0 ? 0.0 : frame.base[n + 3])) * d5);
            modelPart.f_104204_ = modelPart.f_104204_ + (float)((frame.pose[n + 4] - (i == 0 ? 0.0 : frame.base[n + 4])) * d5);
            modelPart.f_104205_ = modelPart.f_104205_ + (float)((frame.pose[n + 5] - (i == 0 ? 0.0 : frame.base[n + 5])) * d5);
            if (i >= 4 && d5 > 0.001) {
                modelPart.f_104201_ = Math.min(modelPart.f_104201_, (float)(24.0 - RunGait.sole(modelPart.f_104203_, modelPart.f_104204_, modelPart.f_104205_)));
            }
            modelPartArray2[i].f_104200_ += modelPart.f_104200_ - f2;
            modelPartArray2[i].f_104201_ += modelPart.f_104201_ - f3;
            modelPartArray2[i].f_104202_ += modelPart.f_104202_ - f4;
            modelPartArray2[i].f_104203_ += modelPart.f_104203_ - f5;
            modelPartArray2[i].f_104204_ += modelPart.f_104204_ - f6;
            modelPartArray2[i].f_104205_ += modelPart.f_104205_ - f7;
        }
    }

    private static final class Frame {
        final RunGait.Clock clock = new RunGait.Clock();
        final double[] base = new double[36];
        final double[] pose = new double[36];
        float age;
        double forward;
        double sideways;
        boolean captured;

        private Frame() {
        }
    }
}

