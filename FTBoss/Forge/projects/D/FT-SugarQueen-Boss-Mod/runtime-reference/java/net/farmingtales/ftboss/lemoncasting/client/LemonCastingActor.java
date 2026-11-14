/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.world.entity.HumanoidArm
 */
package net.farmingtales.ftboss.lemoncasting.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.lemoncasting.CastingClock;
import net.farmingtales.ftboss.lemoncasting.CastingPacket;
import net.farmingtales.ftboss.lemoncasting.client.CastingPose;
import net.farmingtales.ftboss.lemoncasting.client.LemonCastingCurves;
import net.farmingtales.ftboss.lemonmotion.LemonMotionAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.HumanoidArm;

public final class LemonCastingActor {
    private static final Map<UUID, State> ACTIVE = new HashMap<UUID, State>();
    private static final ThreadLocal<Base> BASE = new ThreadLocal();
    private static Object world;
    private static long cleanedTick;
    private static final double RAD = Math.PI / 180;

    private LemonCastingActor() {
    }

    private static ModelPart[] parts(PlayerModel<?> playerModel) {
        return new ModelPart[]{playerModel.f_102808_, playerModel.f_102810_, playerModel.f_102811_, playerModel.f_102812_, playerModel.f_102813_, playerModel.f_102814_};
    }

    private static void resetWorld() {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel != world) {
            ACTIVE.clear();
            BASE.remove();
            world = clientLevel;
            cleanedTick = Long.MIN_VALUE;
        }
        if (clientLevel != null && cleanedTick != clientLevel.m_46467_()) {
            cleanedTick = clientLevel.m_46467_();
            ACTIVE.entrySet().removeIf(entry -> CastingClock.expiredHistory(((State)entry.getValue()).cue(), cleanedTick));
        }
    }

    public static void receive(CastingPacket castingPacket) {
        LemonCastingActor.resetWorld();
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !castingPacket.valid() || !castingPacket.dimension().equals((Object)clientLevel.m_46472_().m_135782_()) || CastingClock.stale(castingPacket.cue(), clientLevel.m_46467_())) {
            return;
        }
        State state = ACTIVE.get(castingPacket.owner());
        if (!CastingClock.supersedes(state == null ? null : state.cue(), castingPacket.cue())) {
            return;
        }
        if (ACTIVE.size() >= 128 && !ACTIVE.containsKey(castingPacket.owner())) {
            return;
        }
        ACTIVE.put(castingPacket.owner(), new State(castingPacket.cue()));
    }

    public static void capture(PlayerModel<?> playerModel, EasterBossEntity easterBossEntity) {
        BASE.remove();
        if (easterBossEntity.getVariant() != 4 || easterBossEntity.isMini()) {
            return;
        }
        LemonCastingActor.resetWorld();
        if (!ACTIVE.containsKey(easterBossEntity.m_20148_())) {
            return;
        }
        float[][] fArrayArray = new float[6][];
        ModelPart[] modelPartArray = LemonCastingActor.parts(playerModel);
        for (int i = 0; i < 6; ++i) {
            ModelPart modelPart = modelPartArray[i];
            fArrayArray[i] = new float[]{modelPart.f_104200_, modelPart.f_104201_, modelPart.f_104202_, modelPart.f_104203_, modelPart.f_104204_, modelPart.f_104205_};
        }
        BASE.set(new Base(playerModel, easterBossEntity, fArrayArray));
    }

    private static boolean ritualPriority(EasterBossEntity easterBossEntity) {
        if (!easterBossEntity.isCastingLike() || !(easterBossEntity instanceof LemonMotionAccess)) {
            return false;
        }
        LemonMotionAccess lemonMotionAccess = easterBossEntity;
        String string = lemonMotionAccess.lemonMotionData().m_128461_("lemon_motion_v1");
        return "absorb".equals(string) || "summon".equals(string);
    }

    public static void animate(PlayerModel<?> playerModel, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5, ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3) {
        float[] fArray;
        ModelPart modelPart4;
        double d;
        Base base = BASE.get();
        BASE.remove();
        LemonCastingActor.resetWorld();
        if (base == null || base.model() != playerModel || base.owner() != easterBossEntity || !easterBossEntity.m_6084_() || LemonCastingActor.ritualPriority(easterBossEntity)) {
            return;
        }
        State state = ACTIVE.get(easterBossEntity.m_20148_());
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (state == null || easterBossEntity.m_9236_() != clientLevel) {
            return;
        }
        CastingClock.Cue cue = state.cue();
        double d2 = CastingClock.age(cue, d = (double)clientLevel.m_46467_() + LemonCastingCurves.clamp(f3 - (float)easterBossEntity.f_19797_, 0.0, 1.0));
        if (d2 < 0.0 || CastingClock.stale(cue, d)) {
            return;
        }
        int n = easterBossEntity.m_5737_() == HumanoidArm.RIGHT ? 1 : -1;
        int n2 = cue.phase() == 0 ? cue.duration() : CastingClock.recovery(cue.duration());
        CastingPose.Frame frame = CastingPose.evaluate(cue, d, n, f, f2, f4, f5);
        double[][] dArray = frame.delta();
        double d3 = frame.influence();
        ModelPart[] modelPartArray = LemonCastingActor.parts(playerModel);
        for (int n3 : new int[]{0, 1, 2, 3}) {
            modelPart4 = modelPartArray[n3];
            fArray = base.poses()[n3];
            modelPart4.f_104200_ = (float)((double)fArray[0] * (1.0 - d3) + LemonCastingCurves.PIVOTS[n3][0] * d3 + dArray[n3][0]);
            modelPart4.f_104201_ = (float)((double)fArray[1] * (1.0 - d3) + LemonCastingCurves.PIVOTS[n3][1] * d3 + dArray[n3][1]);
            modelPart4.f_104202_ = (float)((double)fArray[2] * (1.0 - d3) + LemonCastingCurves.PIVOTS[n3][2] * d3 + dArray[n3][2]);
            modelPart4.f_104203_ = (float)((double)fArray[3] * (1.0 - d3) + dArray[n3][3] * (Math.PI / 180));
            modelPart4.f_104204_ = (float)((double)fArray[4] * (1.0 - d3) + dArray[n3][4] * (Math.PI / 180));
            modelPart4.f_104205_ = (float)((double)fArray[5] * (1.0 - d3) + dArray[n3][5] * (Math.PI / 180));
        }
        for (int n3 : new int[]{4, 5}) {
            modelPart4 = modelPartArray[n3];
            fArray = base.poses()[n3];
            modelPart4.f_104200_ = fArray[0];
            modelPart4.f_104201_ = fArray[1];
            modelPart4.f_104202_ = fArray[2];
            modelPart4.f_104203_ = fArray[3];
            modelPart4.f_104204_ = fArray[4];
            modelPart4.f_104205_ = fArray[5];
        }
        modelPart.m_233569_();
        modelPart2.m_233569_();
        modelPart3.m_233569_();
        double d4 = Math.sin(Math.PI * LemonCastingCurves.clamp(d2 / (double)n2, 0.0, 1.0)) * d3;
        modelPart.f_104205_ += (float)(d4 * 0.022 * (double)n);
        modelPart2.f_104203_ += (float)(d4 * 0.08);
        modelPart3.f_104203_ += (float)(d4 * 0.028);
        playerModel.f_102809_.m_104315_(playerModel.f_102808_);
        playerModel.f_103378_.m_104315_(playerModel.f_102810_);
        playerModel.f_103374_.m_104315_(playerModel.f_102812_);
        playerModel.f_103375_.m_104315_(playerModel.f_102811_);
        playerModel.f_103376_.m_104315_(playerModel.f_102814_);
        playerModel.f_103377_.m_104315_(playerModel.f_102813_);
    }

    static {
        cleanedTick = Long.MIN_VALUE;
    }

    private record State(CastingClock.Cue cue) {
    }

    private record Base(PlayerModel<?> model, EasterBossEntity owner, float[][] poses) {
    }
}

