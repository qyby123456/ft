/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.entity.HumanoidArm
 */
package net.farmingtales.ftboss.manorrun;

import java.util.WeakHashMap;
import net.farmingtales.ftboss.body.BodyMotion;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.qizairun.QizaiRunClient;
import net.farmingtales.ftboss.dandanmotion.MotionAccess;
import net.farmingtales.ftboss.doramotion.DoraMotionAccess;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.manorrun.RunMotion;
import net.farmingtales.ftboss.manorrun.RunState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.HumanoidArm;

public final class RunAnimator {
    private static final WeakHashMap<EasterBossEntity, RunState> STATES = new WeakHashMap();
    private static final ThreadLocal<Layer> LAYER = new ThreadLocal();

    private RunAnimator() {
    }

    public static boolean owns(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 7;
    }

    public static void generic(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f) {
        if (QizaiRunClient.owns(easterBossEntity)) {
            return;
        }
        if (!RunAnimator.owns(easterBossEntity)) {
            net.farmingtales.ftboss.client.manorrun.RunAnimator.apply(easterBossModel, easterBossEntity, f);
        }
    }

    public static void begin(EasterBossEntity easterBossEntity) {
        if (RunAnimator.owns(easterBossEntity)) {
            LAYER.set(new Layer(easterBossEntity));
        } else {
            LAYER.remove();
        }
    }

    public static void reserve(double d, EasterBossEntity easterBossEntity) {
        Layer layer = LAYER.get();
        if (layer != null && layer.owner == easterBossEntity && Double.isFinite(d) && d > 0.001) {
            layer.reserved = true;
        }
    }

    public static void reserveBody(BodyMotion.Pose pose, EasterBossEntity easterBossEntity) {
        RunAnimator.reserve(pose.activity(), easterBossEntity);
    }

    public static boolean timed(long l, int n, double d) {
        return n > 0 && n <= 2400 && d >= (double)l && d < (double)(l + (long)n + 6L);
    }

    private static boolean cueBusy(CompoundTag compoundTag, double d, int n) {
        if (n > 3 || compoundTag.m_128456_()) {
            return false;
        }
        if (compoundTag.m_128441_("start") && RunAnimator.timed(compoundTag.m_128454_("start"), compoundTag.m_128451_("duration"), d)) {
            return true;
        }
        if (compoundTag.m_128441_("dandan_motion_start") && RunAnimator.timed(compoundTag.m_128454_("dandan_motion_start"), compoundTag.m_128451_("dandan_motion_ticks"), d)) {
            return true;
        }
        if (compoundTag.m_128441_("gunTick") && RunAnimator.timed(compoundTag.m_128454_("gunTick"), 9, d) || compoundTag.m_128441_("spearTick") && RunAnimator.timed(compoundTag.m_128454_("spearTick"), 12, d)) {
            return true;
        }
        for (String string : compoundTag.m_128431_()) {
            CompoundTag compoundTag2;
            Tag tag = compoundTag.m_128423_(string);
            if (!(tag instanceof CompoundTag) || !RunAnimator.cueBusy(compoundTag2 = (CompoundTag)tag, d, n + 1)) continue;
            return true;
        }
        return false;
    }

    public static boolean busy(EasterBossEntity easterBossEntity, float f) {
        Object object;
        double d = (float)easterBossEntity.m_9236_().m_46467_() + f;
        if (easterBossEntity.isCastingLike() || (double)easterBossEntity.getCombatAnimationWeight(f) > 0.001 || easterBossEntity.isDoraReloading() || easterBossEntity.isXuanlanSkyCasting() || RunAnimator.cueBusy(easterBossEntity.getSkillCue(), d, 0)) {
            return true;
        }
        EasterBossEntity easterBossEntity2 = easterBossEntity;
        if (easterBossEntity2 instanceof MotionAccess && RunAnimator.cueBusy((object = (MotionAccess)easterBossEntity2).dandanMotionData(), d, 0)) {
            return true;
        }
        easterBossEntity2 = easterBossEntity;
        return easterBossEntity2 instanceof DoraMotionAccess && RunAnimator.cueBusy((object = (DoraMotionAccess)easterBossEntity2).doraMotionState(), d, 0);
    }

    public static void finish(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity2, float f, float f2, float f3, float f4, float f5) {
        if (QizaiRunClient.owns(easterBossEntity2)) {
            return;
        }
        Layer layer = LAYER.get();
        LAYER.remove();
        if (!RunAnimator.owns(easterBossEntity2) || easterBossEntity2.isMini() || easterBossEntity2.isGiant() || !easterBossEntity2.m_6084_() || easterBossEntity2.isManorIntroHidden() || easterBossEntity2.m_20159_() || easterBossEntity2.m_5803_()) {
            STATES.remove(easterBossEntity2);
            return;
        }
        float f6 = (float)RunMotion.clamp(f3 - (float)easterBossEntity2.f_19797_, 0.0, 1.0);
        boolean bl = layer != null && layer.owner == easterBossEntity2 && layer.reserved || easterBossEntity2.f_20916_ > 0 || RunAnimator.busy(easterBossEntity2, f6);
        boolean bl2 = easterBossEntity2.m_20096_() && !easterBossEntity2.m_20072_() && !easterBossEntity2.m_20077_();
        RunState runState = STATES.computeIfAbsent(easterBossEntity2, easterBossEntity -> new RunState());
        RunState.Frame frame = runState.advance(easterBossEntity2.getVariant(), easterBossEntity2.f_19797_, (float)easterBossEntity2.f_19797_ + f6, easterBossEntity2.m_20185_(), easterBossEntity2.m_20189_(), bl2, bl);
        if (frame.blend() < 1.0E-4 || bl) {
            return;
        }
        RunMotion.Pose pose = RunMotion.sample(easterBossEntity2.getVariant(), frame.phase(), frame.speed(), easterBossEntity2.m_5737_() == HumanoidArm.LEFT, f4, f5);
        ModelPart[] modelPartArray = new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
        for (int i = 0; i < 6; ++i) {
            ModelPart modelPart = modelPartArray[i];
            double d = frame.blend();
            double[] dArray = pose.position()[i];
            double[] dArray2 = pose.rotation()[i];
            modelPart.f_104200_ += (float)((dArray[0] - (double)modelPart.f_104200_) * d);
            modelPart.f_104201_ += (float)((dArray[1] - (double)modelPart.f_104201_) * d);
            modelPart.f_104202_ += (float)((dArray[2] - (double)modelPart.f_104202_) * d);
            modelPart.f_104203_ += (float)((dArray2[0] - (double)modelPart.f_104203_) * d);
            modelPart.f_104204_ += (float)((dArray2[1] - (double)modelPart.f_104204_) * d);
            modelPart.f_104205_ += (float)((dArray2[2] - (double)modelPart.f_104205_) * d);
        }
        easterBossModel.f_102809_.m_104315_(easterBossModel.f_102808_);
        easterBossModel.f_103378_.m_104315_(easterBossModel.f_102810_);
        easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
        easterBossModel.f_103374_.m_104315_(easterBossModel.f_102812_);
        easterBossModel.f_103377_.m_104315_(easterBossModel.f_102813_);
        easterBossModel.f_103376_.m_104315_(easterBossModel.f_102814_);
    }

    private static final class Layer {
        private final EasterBossEntity owner;
        private boolean reserved;

        private Layer(EasterBossEntity easterBossEntity) {
            this.owner = easterBossEntity;
        }
    }
}

