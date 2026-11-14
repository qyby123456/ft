/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.client.qizairun;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.qizairun.QizaiRunClock;
import net.farmingtales.ftboss.client.qizairun.QizaiRunPose;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class QizaiRunClient {
    private static final Map<UUID, Frame> actors = new LinkedHashMap<UUID, Frame>();
    private static ClientLevel scene;

    private QizaiRunClient() {
    }

    public static boolean owns(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 11 && !easterBossEntity.isMini() && !easterBossEntity.isGiant();
    }

    private static ModelPart[] parts(EasterBossModel easterBossModel) {
        return new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
    }

    private static void read(ModelPart[] modelPartArray, double[] dArray) {
        for (int i = 0; i < 6; ++i) {
            ModelPart modelPart = modelPartArray[i];
            int n = i * 6;
            dArray[n] = modelPart.f_104200_;
            dArray[n + 1] = modelPart.f_104201_;
            dArray[n + 2] = modelPart.f_104202_;
            dArray[n + 3] = modelPart.f_104203_;
            dArray[n + 4] = modelPart.f_104204_;
            dArray[n + 5] = modelPart.f_104205_;
        }
    }

    public static void prepare(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5) {
        if (!QizaiRunClient.owns(easterBossEntity)) {
            return;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || easterBossEntity.m_9236_() != clientLevel || !easterBossEntity.m_6084_() || easterBossEntity.m_213877_()) {
            actors.remove(easterBossEntity.m_20148_());
            return;
        }
        if (scene != clientLevel) {
            actors.clear();
            scene = clientLevel;
        }
        float f6 = Float.isFinite(f3) ? (float)QizaiRunPose.clamp(f3 - (float)easterBossEntity.f_19797_, 0.0, 1.0) : 0.0f;
        double d = (float)clientLevel.m_46467_() + f6;
        actors.values().removeIf(frame -> d - frame.lastSeen > 60.0 || d < frame.lastSeen);
        if (!actors.containsKey(easterBossEntity.m_20148_()) && actors.size() >= 96) {
            actors.remove(actors.keySet().iterator().next());
        }
        Frame frame2 = actors.computeIfAbsent(easterBossEntity.m_20148_(), uUID -> new Frame());
        frame2.elapsed = QizaiRunPose.clamp(d - frame2.lastSeen, 0.0, 6.0);
        frame2.lastSeen = d;
        frame2.partial = f6;
        frame2.captured = false;
        boolean bl = easterBossEntity.m_20096_() && !easterBossEntity.m_20069_() && !easterBossEntity.m_20159_() && !easterBossEntity.m_21255_() && !easterBossEntity.m_21525_() && easterBossEntity.m_20089_() == Pose.STANDING;
        Vec3 vec3 = easterBossEntity.m_20318_(f6);
        frame2.state = frame2.clock.update(d, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, bl && Float.isFinite(f3) && Float.isFinite(f) && Float.isFinite(f2), easterBossEntity.f_20883_);
        if (frame2.state.blend() <= 0.001) {
            return;
        }
        QizaiRunClient.read(QizaiRunClient.parts(easterBossModel), frame2.before);
        frame2.captured = true;
    }

    public static void finish(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity) {
        int n;
        Frame frame = actors.get(easterBossEntity.m_20148_());
        if (frame == null || !frame.captured || !QizaiRunClient.owns(easterBossEntity)) {
            return;
        }
        frame.captured = false;
        if (!easterBossEntity.m_6084_() || easterBossEntity.m_213877_() || easterBossEntity.m_9236_() != scene) {
            return;
        }
        ModelPart[] modelPartArray = QizaiRunClient.parts(easterBossModel);
        QizaiRunClient.read(modelPartArray, frame.pose);
        boolean bl = easterBossEntity.isCastingLike() || easterBossEntity.getCombatAnimationWeight(frame.partial) > 0.02f || easterBossEntity.getActionTicks() > 0;
        for (n = 0; n < 24; ++n) {
            if (!(Math.abs(frame.pose[n] - frame.before[n]) > 0.025)) continue;
            bl = true;
        }
        frame.upperWeight = bl ? 0.0 : 1.0 - (1.0 - frame.upperWeight) * Math.exp(-frame.elapsed / 3.0);
        QizaiRunPose.apply(frame.pose, frame.state.phase(), frame.state.speed(), frame.state.blend(), easterBossEntity.m_5737_() == HumanoidArm.LEFT, !easterBossEntity.m_21205_().m_41619_(), !easterBossEntity.m_21206_().m_41619_(), frame.upperWeight, frame.state.forward(), frame.state.side());
        for (n = 0; n < 6; ++n) {
            ModelPart modelPart = modelPartArray[n];
            int n2 = n * 6;
            modelPart.f_104200_ = (float)frame.pose[n2];
            modelPart.f_104201_ = (float)frame.pose[n2 + 1];
            modelPart.f_104202_ = (float)frame.pose[n2 + 2];
            modelPart.f_104203_ = (float)frame.pose[n2 + 3];
            modelPart.f_104204_ = (float)frame.pose[n2 + 4];
            modelPart.f_104205_ = (float)frame.pose[n2 + 5];
        }
        easterBossModel.f_102809_.m_104315_(easterBossModel.f_102808_);
        easterBossModel.f_103378_.m_104315_(easterBossModel.f_102810_);
        easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
        easterBossModel.f_103374_.m_104315_(easterBossModel.f_102812_);
        easterBossModel.f_103377_.m_104315_(easterBossModel.f_102813_);
        easterBossModel.f_103376_.m_104315_(easterBossModel.f_102814_);
    }

    @SubscribeEvent
    public static void unloaded(LevelEvent.Unload unload) {
        if (unload.getLevel() == scene) {
            actors.clear();
            scene = null;
        }
    }

    private static final class Frame {
        final QizaiRunClock clock = new QizaiRunClock();
        final double[] before = new double[36];
        final double[] pose = new double[36];
        QizaiRunClock.State state;
        double lastSeen;
        double elapsed;
        double upperWeight;
        float partial;
        boolean captured;

        private Frame() {
        }
    }
}

