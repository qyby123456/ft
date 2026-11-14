/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.Entity
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.dandanironmotion.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.dandaniron.IronPacket;
import net.farmingtales.ftboss.dandanironmotion.IronActorRules;
import net.farmingtales.ftboss.dandanironmotion.IronPoses;
import net.farmingtales.ftboss.dandanmotion.MotionCurves;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class IronActor {
    private static final Map<UUID, Active> ACTIVE = new LinkedHashMap<UUID, Active>();
    private static ClientLevel world;

    private IronActor() {
    }

    public static void receive(IronPacket ironPacket) {
        EasterBossEntity easterBossEntity;
        Entity entity;
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !ironPacket.valid() || !ironPacket.visual().dimension().equals((Object)clientLevel.m_46472_().m_135782_())) {
            return;
        }
        if (clientLevel != world) {
            ACTIVE.clear();
            world = clientLevel;
        }
        if (!((entity = clientLevel.m_6815_(ironPacket.visual().ownerId())) instanceof EasterBossEntity && IronActor.eligible(easterBossEntity = (EasterBossEntity)entity) && easterBossEntity.m_20148_().equals(ironPacket.visual().owner()))) {
            return;
        }
        Active active = ACTIVE.get(ironPacket.visual().owner());
        if (active != null && active.packet().cast() > ironPacket.cast()) {
            return;
        }
        if (ironPacket.cancelled()) {
            if (active != null && !active.recovering() && active.packet().cast() == ironPacket.cast() && active.packet().visual().phase() == SpellVisualRules.Phase.CHARGE) {
                double d = MotionCurves.clamp((double)(clientLevel.m_46467_() - active.packet().start()) / (double)active.packet().ticks());
                ACTIVE.put(ironPacket.visual().owner(), new Active(active.packet(), true, clientLevel.m_46467_(), d));
            }
            return;
        }
        if (IronActorRules.autonomous(ironPacket.visual().phase().name())) {
            return;
        }
        if (active != null && IronActorRules.stale(active.packet().visual().phase().name(), active.packet().cast(), active.packet().event(), ironPacket.visual().phase().name(), ironPacket.cast(), ironPacket.event())) {
            return;
        }
        if (ACTIVE.size() >= 64 && !ACTIVE.containsKey(ironPacket.visual().owner())) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }
        ACTIVE.put(ironPacket.visual().owner(), new Active(ironPacket, false, 0L, 0.0));
    }

    private static boolean eligible(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 1 && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && easterBossEntity.m_6084_();
    }

    private static boolean expired(Active active, long l) {
        EasterBossEntity easterBossEntity;
        Entity entity = world.m_6815_(active.packet().visual().ownerId());
        if (!(entity instanceof EasterBossEntity && IronActor.eligible(easterBossEntity = (EasterBossEntity)entity) && easterBossEntity.m_20148_().equals(active.packet().visual().owner()))) {
            return true;
        }
        return IronActorRules.expires(active.recovering(), l, active.recoveryStart(), active.packet().visual().phase() == SpellVisualRules.Phase.CHARGE, active.packet().start(), active.packet().ticks(), active.packet().event());
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || clientLevel != world) {
            ACTIVE.clear();
            world = clientLevel;
            return;
        }
        ACTIVE.values().removeIf(active -> IronActor.expired(active, clientLevel.m_46467_()));
    }

    public static void animate(PlayerModel<?> playerModel, EasterBossEntity easterBossEntity, float f) {
        if (!IronActor.eligible(easterBossEntity) || world == null || easterBossEntity.m_9236_() != world) {
            return;
        }
        Active active = ACTIVE.get(easterBossEntity.m_20148_());
        if (active == null) {
            return;
        }
        if (IronActor.expired(active, world.m_46467_())) {
            ACTIVE.remove(easterBossEntity.m_20148_());
            return;
        }
        float f2 = (float)MotionCurves.clamp(f - (float)easterBossEntity.f_19797_);
        double d = (float)world.m_46467_() + f2;
        EasterBossEntity easterBossEntity2 = easterBossEntity;
        CompoundTag compoundTag = easterBossEntity2.dandanMotionData();
        if (IronActorRules.nativeHasPriority(compoundTag.m_128454_("dandan_motion_start"), compoundTag.m_128451_("dandan_motion_ticks"), active.packet().event(), d)) {
            return;
        }
        if ((active.recovering() || active.packet().visual().phase() == SpellVisualRules.Phase.RELEASE) && easterBossEntity.isCastingLike()) {
            return;
        }
        String string = active.packet().visual().spell().id + (active.packet().visual().phase() == SpellVisualRules.Phase.CHARGE ? "_charge" : "_release");
        double d2 = MotionCurves.clamp((d - (double)active.packet().start()) / (double)active.packet().ticks());
        double[][] dArray = active.recovering() ? IronPoses.recover(string, active.recoveryPose(), (d - (double)active.recoveryStart()) / 7.0) : IronPoses.sample(string, d2);
        double[][] dArray2 = MotionCurves.resolve(dArray);
        double d3 = active.recovering() ? 1.0 : (active.packet().visual().phase() == SpellVisualRules.Phase.CHARGE ? 1.0 : MotionCurves.clamp(((double)(active.packet().start() + (long)active.packet().ticks()) - d) / 3.0));
        ModelPart[] modelPartArray = new ModelPart[]{playerModel.f_102808_, playerModel.f_102810_, playerModel.f_102811_, playerModel.f_102812_};
        for (int i = 0; i < modelPartArray.length; ++i) {
            ModelPart modelPart = modelPartArray[i];
            modelPart.f_104200_ = (float)((double)modelPart.f_104200_ * (1.0 - d3) + dArray2[i][0] * d3);
            modelPart.f_104201_ = (float)((double)modelPart.f_104201_ * (1.0 - d3) + dArray2[i][1] * d3);
            modelPart.f_104202_ = (float)((double)modelPart.f_104202_ * (1.0 - d3) + dArray2[i][2] * d3);
            modelPart.f_104203_ = (float)((double)modelPart.f_104203_ * (1.0 - d3) + Math.toRadians(dArray2[i][3]) * d3);
            modelPart.f_104204_ = (float)((double)modelPart.f_104204_ * (1.0 - d3) + Math.toRadians(dArray2[i][4]) * d3);
            modelPart.f_104205_ = (float)((double)modelPart.f_104205_ * (1.0 - d3) + Math.toRadians(dArray2[i][5]) * d3);
        }
        playerModel.f_102809_.m_104315_(playerModel.f_102808_);
        playerModel.f_103378_.m_104315_(playerModel.f_102810_);
        playerModel.f_103374_.m_104315_(playerModel.f_102812_);
        playerModel.f_103375_.m_104315_(playerModel.f_102811_);
    }

    private record Active(IronPacket packet, boolean recovering, long recoveryStart, double recoveryPose) {
    }
}

