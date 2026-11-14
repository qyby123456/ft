/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.client.maque;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.maque.MaqueHumanoidPose;
import net.farmingtales.ftboss.client.maque.MaqueHumanoidTiming;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.maque.support.MaqueSupportPacket;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.Level;

public final class MaqueHumanoidAnimations {
    private static final Map<UUID, Motion> ACTIVE = new LinkedHashMap<UUID, Motion>();
    private static final Map<UUID, Transition> TRANSITIONS = new LinkedHashMap<UUID, Transition>();
    private static Level world;

    private MaqueHumanoidAnimations() {
    }

    private static void bind(Level level) {
        if (world != level) {
            ACTIVE.clear();
            TRANSITIONS.clear();
            world = level;
        }
    }

    public static void clear() {
        MaqueHumanoidAnimations.bind(null);
    }

    private static void add(Level level, UUID uUID, String string, int n, boolean bl) {
        boolean bl2;
        MaqueHumanoidAnimations.bind(level);
        long l = level.m_46467_();
        ACTIVE.values().removeIf(motion -> l < motion.born || l - motion.born > (long)(MaqueHumanoidPose.clip(motion.clip).ticks() + 40));
        Motion motion2 = ACTIVE.get(uUID);
        boolean bl3 = bl2 = motion2 != null && MaqueHumanoidTiming.merge(motion2.clip.equals(string), motion2.charge, bl, l, motion2.born, motion2.updated, MaqueHumanoidPose.clip(string).ticks());
        if (motion2 != null && motion2.priority > n && l - motion2.born < (long)MaqueHumanoidPose.clip(motion2.clip).ticks()) {
            return;
        }
        double d = bl2 ? motion2.releaseStart : (string.equals("scatter") || motion2 != null && motion2.clip.equals(string) && motion2.charge && !bl ? 0.4 : 0.0);
        ACTIVE.put(uUID, new Motion(string, bl2 ? motion2.born : l, l, n, bl, d));
        if (ACTIVE.size() > 64) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }
    }

    public static void support(Level level, MaqueSupportPacket maqueSupportPacket) {
        int n = maqueSupportPacket.kind();
        if (n == 0) {
            MaqueHumanoidAnimations.add(level, maqueSupportPacket.source(), "arrival", 10, false);
        } else if (n == 6) {
            MaqueHumanoidAnimations.add(level, maqueSupportPacket.source(), "intercept", 35, false);
        } else if (n >= 2 && n <= 5) {
            MaqueHumanoidAnimations.add(level, maqueSupportPacket.source(), "support", 20, false);
        }
    }

    public static void spell(Level level, SpellVisualPacket spellVisualPacket) {
        String string;
        String string2 = spellVisualPacket.spell().id;
        String string3 = string2.equals("feather_fan") ? "scatter" : (string2.equals("wingstep") ? "hover" : (string = string2.equals("feather_refuge") ? "refuge" : null));
        if (string != null) {
            MaqueHumanoidAnimations.add(level, spellVisualPacket.owner(), string, 40, spellVisualPacket.phase().name().equals("CHARGE"));
        }
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5) {
        ModelPart[] modelPartArray;
        double d;
        if (easterBossEntity.getVariant() != 9 || !easterBossEntity.m_6084_() || easterBossEntity.m_20159_()) {
            return;
        }
        Level level = easterBossEntity.m_9236_();
        MaqueHumanoidAnimations.bind(level);
        double d2 = MaqueHumanoidPose.clamp(f3 - (float)easterBossEntity.f_19797_, 0.0, 1.0);
        Motion motion = ACTIVE.get(easterBossEntity.m_20148_());
        String string = null;
        Object object = "idle";
        double d3 = 0.0;
        double d4 = 0.0;
        double d5 = (double)level.m_46467_() + d2;
        if (motion != null) {
            d = d5 - (double)motion.born;
            int n = MaqueHumanoidPose.clip(motion.clip).ticks();
            if (d >= 0.0 && d < (double)n) {
                string = motion.clip;
                object = string + ":" + motion.born + ":" + motion.charge;
                modelPartArray = MaqueHumanoidTiming.sample(string, motion.charge, d, n, motion.releaseStart);
                d3 = modelPartArray.progress();
                d4 = modelPartArray.weight();
            } else {
                ACTIVE.remove(easterBossEntity.m_20148_());
            }
        }
        d = easterBossEntity.getCombatAnimationWeight((float)d2);
        if (!easterBossEntity.isCastingLike() && easterBossEntity.getActionTicks() > 0 && d > 0.001) {
            string = "impact";
            object = "impact:" + (long)(d5 - (double)easterBossEntity.getCombatAnimationAge((float)d2));
            d3 = easterBossEntity.getActionProgress((float)d2);
            d4 = d;
        } else if (string == null && easterBossEntity.isCastingLike() && d > 0.001) {
            string = "support";
            object = "cast:" + (long)(d5 - (double)easterBossEntity.getCombatAnimationAge((float)d2));
            d3 = easterBossEntity.getActionProgress((float)d2);
            d4 = d;
        }
        if (string == null) {
            string = "support";
            d3 = 1.0;
            d4 = 0.7;
        }
        double[][] dArray = MaqueHumanoidPose.evaluate(string, d3, easterBossEntity.m_5737_() == HumanoidArm.LEFT);
        modelPartArray = new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
        for (int i = 0; i < 4; ++i) {
            modelPartArray[i].m_233569_();
        }
        modelPartArray[0].f_104203_ = (float)Math.toRadians(f5);
        modelPartArray[0].f_104204_ = (float)Math.toRadians(f4);
        modelPartArray[2].f_104203_ = -0.17f + (float)Math.cos((double)f * 0.6662 + Math.PI) * f2;
        modelPartArray[3].f_104203_ = -0.12f + (float)Math.cos((double)f * 0.6662) * f2;
        modelPartArray[2].f_104205_ = 0.1f;
        modelPartArray[3].f_104205_ = -0.09f;
        Transition transition = TRANSITIONS.computeIfAbsent(easterBossEntity.m_20148_(), uUID -> new Transition());
        if (d5 < transition.lastRendered || d5 - transition.lastRendered > 10.0) {
            transition.last = null;
            transition.start = null;
        }
        if (!((String)object).equals(transition.signature)) {
            transition.signature = object;
            transition.switched = d5;
            transition.start = transition.last;
        }
        double d6 = transition.start == null ? 1.0 : MaqueHumanoidTiming.transition(d5 - transition.switched);
        double d7 = MaqueHumanoidAnimations.visualLift(dArray[4]) * d4;
        double[][] dArray2 = new double[6][6];
        for (int i = 0; i < modelPartArray.length; ++i) {
            ModelPart modelPart = modelPartArray[i];
            if (!(i < 4 || !(f2 > 0.035f) && easterBossEntity.m_20096_() || string.equals("hover"))) {
                modelPart.f_104201_ -= (float)d7;
                if (d6 < 1.0) {
                    modelPart.f_104201_ = (float)(transition.start[i][1] + ((double)modelPart.f_104201_ - transition.start[i][1]) * d6);
                }
                dArray2[i] = MaqueHumanoidAnimations.snapshot(modelPart);
                continue;
            }
            double[] dArray3 = dArray[i];
            if (i == 0) {
                dArray3[3] = dArray3[3] + Math.toRadians(f5) * 0.55;
                dArray3[4] = dArray3[4] + Math.toRadians(f4) * 0.55;
            }
            modelPart.f_104200_ += (float)((dArray3[0] - (double)modelPart.f_104200_) * d4);
            modelPart.f_104201_ += (float)((dArray3[1] - (double)modelPart.f_104201_) * d4);
            modelPart.f_104202_ += (float)((dArray3[2] - (double)modelPart.f_104202_) * d4);
            modelPart.f_104203_ += (float)((dArray3[3] - (double)modelPart.f_104203_) * d4);
            modelPart.f_104204_ += (float)((dArray3[4] - (double)modelPart.f_104204_) * d4);
            modelPart.f_104205_ += (float)((dArray3[5] - (double)modelPart.f_104205_) * d4);
            if (d6 < 1.0) {
                double[] dArray4 = transition.start[i];
                modelPart.f_104200_ = (float)(dArray4[0] + ((double)modelPart.f_104200_ - dArray4[0]) * d6);
                modelPart.f_104201_ = (float)(dArray4[1] + ((double)modelPart.f_104201_ - dArray4[1]) * d6);
                modelPart.f_104202_ = (float)(dArray4[2] + ((double)modelPart.f_104202_ - dArray4[2]) * d6);
                modelPart.f_104203_ = (float)(dArray4[3] + ((double)modelPart.f_104203_ - dArray4[3]) * d6);
                modelPart.f_104204_ = (float)(dArray4[4] + ((double)modelPart.f_104204_ - dArray4[4]) * d6);
                modelPart.f_104205_ = (float)(dArray4[5] + ((double)modelPart.f_104205_ - dArray4[5]) * d6);
            }
            if (i >= 4) {
                MaqueHumanoidAnimations.anchorLeg(modelPart, i == 4 ? -1.9 : 1.9);
            }
            dArray2[i] = MaqueHumanoidAnimations.snapshot(modelPart);
        }
        transition.last = dArray2;
        transition.lastRendered = d5;
        if (TRANSITIONS.size() > 64) {
            TRANSITIONS.remove(TRANSITIONS.keySet().iterator().next());
        }
        easterBossModel.f_102809_.m_104315_(easterBossModel.f_102808_);
        easterBossModel.f_103378_.m_104315_(easterBossModel.f_102810_);
        easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
        easterBossModel.f_103374_.m_104315_(easterBossModel.f_102812_);
        easterBossModel.f_103377_.m_104315_(easterBossModel.f_102813_);
        easterBossModel.f_103376_.m_104315_(easterBossModel.f_102814_);
    }

    private static double[] snapshot(ModelPart modelPart) {
        return new double[]{modelPart.f_104200_, modelPart.f_104201_, modelPart.f_104202_, modelPart.f_104203_, modelPart.f_104204_, modelPart.f_104205_};
    }

    private static double visualLift(double[] dArray) {
        double[] dArray2 = MaqueHumanoidPose.rotation(new double[]{dArray[3], dArray[4], dArray[5]});
        double d = Double.NEGATIVE_INFINITY;
        for (double d2 : new double[]{-2.0, 2.0}) {
            for (double d3 : new double[]{-2.0, 2.0}) {
                d = Math.max(d, MaqueHumanoidPose.transform(dArray2, new double[]{d2, 12.0, d3})[1]);
            }
        }
        return Math.max(0.0, 24.0 - dArray[1] - d);
    }

    private static void anchorLeg(ModelPart modelPart, double d) {
        double[] dArray = MaqueHumanoidPose.rotation(new double[]{modelPart.f_104203_, modelPart.f_104204_, modelPart.f_104205_});
        double[] dArray2 = MaqueHumanoidPose.transform(dArray, new double[]{0.0, 12.0, 0.0});
        double d2 = Double.NEGATIVE_INFINITY;
        for (double d3 : new double[]{-2.0, 2.0}) {
            for (double d4 : new double[]{-2.0, 2.0}) {
                d2 = Math.max(d2, MaqueHumanoidPose.transform(dArray, new double[]{d3, 12.0, d4})[1]);
            }
        }
        modelPart.f_104200_ = (float)(d - dArray2[0]);
        modelPart.f_104202_ = (float)(-dArray2[2]);
        modelPart.f_104201_ = (float)Math.min((double)modelPart.f_104201_, 24.0 - d2);
    }

    private record Motion(String clip, long born, long updated, int priority, boolean charge, double releaseStart) {
    }

    private static final class Transition {
        String signature;
        double switched;
        double[][] start;
        double[][] last;
        double lastRendered;

        private Transition() {
        }
    }
}

