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
package net.farmingtales.ftboss.dandanrun.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.dandaniron.IronPacket;
import net.farmingtales.ftboss.dandanrun.RunCycle;
import net.farmingtales.ftboss.dandanrun.RunLease;
import net.farmingtales.ftboss.dandanrun.RunState;
import net.farmingtales.ftboss.entity.EasterBossEntity;
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
public final class RunActor {
    private static final Map<UUID, Entry> STATES = new LinkedHashMap<UUID, Entry>();
    private static ClientLevel world;

    private RunActor() {
    }

    private static boolean eligible(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 1 && EasterBossEntity.isManorVariant(1) && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && easterBossEntity.m_6084_() && !easterBossEntity.isManorIntroHidden();
    }

    private static Entry entry(EasterBossEntity easterBossEntity) {
        Entry entry = STATES.get(easterBossEntity.m_20148_());
        if (entry != null && entry.ownerId != easterBossEntity.m_19879_()) {
            STATES.remove(easterBossEntity.m_20148_());
        }
        if (STATES.size() >= 64 && !STATES.containsKey(easterBossEntity.m_20148_())) {
            STATES.remove(STATES.keySet().iterator().next());
        }
        return STATES.computeIfAbsent(easterBossEntity.m_20148_(), uUID -> new Entry(easterBossEntity.m_19879_(), world.m_46467_()));
    }

    private static boolean level() {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel != world) {
            STATES.clear();
            world = clientLevel;
        }
        return clientLevel != null;
    }

    public static void notice(IronPacket ironPacket) {
        EasterBossEntity easterBossEntity;
        if (!(RunActor.level() && ironPacket.valid() && ironPacket.visual().dimension().equals((Object)world.m_46472_().m_135782_()))) {
            return;
        }
        Entity entity = world.m_6815_(ironPacket.visual().ownerId());
        if (!(entity instanceof EasterBossEntity && RunActor.eligible(easterBossEntity = (EasterBossEntity)entity) && easterBossEntity.m_20148_().equals(ironPacket.visual().owner()))) {
            return;
        }
        Entry entry = RunActor.entry(easterBossEntity);
        if (!entry.lease.receive(ironPacket.visual().phase().name(), ironPacket.cast(), ironPacket.event(), ironPacket.start(), ironPacket.ticks(), ironPacket.cancelled(), world.m_46467_())) {
            return;
        }
        entry.run.reset();
        entry.touched = world.m_46467_();
    }

    private static boolean allowed(EasterBossEntity easterBossEntity, Entry entry, long l) {
        if (!RunActor.eligible(easterBossEntity) || !easterBossEntity.m_20096_() || easterBossEntity.m_20069_() || easterBossEntity.m_20072_() || easterBossEntity.m_6144_() || easterBossEntity.m_20159_() || easterBossEntity.m_21255_()) {
            return false;
        }
        if (easterBossEntity.isCastingLike() || easterBossEntity.getActionTicks() > 0 || (double)easterBossEntity.getCombatAnimationWeight(0.0f) > 0.02 || entry.lease.busy(l)) {
            return false;
        }
        EasterBossEntity easterBossEntity2 = easterBossEntity;
        CompoundTag compoundTag = easterBossEntity2.dandanMotionData();
        return !RunState.activeTag(l, compoundTag.m_128454_("dandan_motion_start"), compoundTag.m_128451_("dandan_motion_ticks"), compoundTag.m_128451_("dandan_motion_schema"));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END || !RunActor.level()) {
            return;
        }
        long l = world.m_46467_();
        STATES.entrySet().removeIf(entry -> {
            EasterBossEntity easterBossEntity;
            Entity entity = world.m_6815_(((Entry)entry.getValue()).ownerId);
            return !(entity instanceof EasterBossEntity) || !RunActor.eligible(easterBossEntity = (EasterBossEntity)entity) || !easterBossEntity.m_20148_().equals(entry.getKey()) || l - ((Entry)entry.getValue()).touched > 40L;
        });
        for (Entry entry2 : STATES.values()) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)world.m_6815_(entry2.ownerId);
            entry2.run.update(l, easterBossEntity.m_20185_(), easterBossEntity.m_20189_(), RunActor.allowed(easterBossEntity, entry2, l));
        }
    }

    public static void animate(PlayerModel<?> playerModel, EasterBossEntity easterBossEntity, float f) {
        double d;
        double d2;
        long l;
        if (!RunActor.level() || !RunActor.eligible(easterBossEntity) || easterBossEntity.m_9236_() != world) {
            return;
        }
        Entry entry = RunActor.entry(easterBossEntity);
        entry.touched = l = world.m_46467_();
        if (!RunActor.allowed(easterBossEntity, entry, l)) {
            entry.run.reset();
            return;
        }
        if (entry.run.tick == Long.MIN_VALUE) {
            entry.run.update(l, easterBossEntity.m_20185_(), easterBossEntity.m_20189_(), true);
        }
        if ((d2 = entry.run.weightAt(d = (double)Math.max(0.0f, Math.min(1.0f, f - (float)easterBossEntity.f_19797_)))) < 0.001) {
            return;
        }
        double[][] dArray = RunCycle.sample(entry.run.phaseAt(d), Math.max(0.0, Math.min(1.0, (entry.run.speed - 0.18) / 0.22)));
        ModelPart[] modelPartArray = new ModelPart[]{playerModel.f_102808_, playerModel.f_102810_, playerModel.f_102811_, playerModel.f_102812_, playerModel.f_102813_, playerModel.f_102814_};
        float f2 = playerModel.f_102808_.f_104203_;
        float f3 = playerModel.f_102808_.f_104204_;
        for (int i = 0; i < 6; ++i) {
            ModelPart modelPart = modelPartArray[i];
            modelPart.f_104200_ = (float)((double)modelPart.f_104200_ * (1.0 - d2) + dArray[i][0] * d2);
            modelPart.f_104201_ = (float)((double)modelPart.f_104201_ * (1.0 - d2) + dArray[i][1] * d2);
            modelPart.f_104202_ = (float)((double)modelPart.f_104202_ * (1.0 - d2) + dArray[i][2] * d2);
            modelPart.f_104203_ = (float)((double)modelPart.f_104203_ * (1.0 - d2) + Math.toRadians(dArray[i][3]) * d2);
            modelPart.f_104204_ = (float)((double)modelPart.f_104204_ * (1.0 - d2) + Math.toRadians(dArray[i][4]) * d2);
            modelPart.f_104205_ = (float)((double)modelPart.f_104205_ * (1.0 - d2) + Math.toRadians(dArray[i][5]) * d2);
            if (i < 4) continue;
            modelPart.f_104201_ = Math.min(modelPart.f_104201_, (float)RunCycle.footPivot(Math.toDegrees(modelPart.f_104203_)));
        }
        playerModel.f_102808_.f_104203_ = (float)((double)playerModel.f_102808_.f_104203_ + (double)f2 * d2);
        playerModel.f_102808_.f_104204_ = (float)((double)playerModel.f_102808_.f_104204_ + (double)f3 * d2);
        playerModel.f_102809_.m_104315_(playerModel.f_102808_);
        playerModel.f_103378_.m_104315_(playerModel.f_102810_);
        playerModel.f_103375_.m_104315_(playerModel.f_102811_);
        playerModel.f_103374_.m_104315_(playerModel.f_102812_);
        playerModel.f_103376_.m_104315_(playerModel.f_102813_);
        playerModel.f_103377_.m_104315_(playerModel.f_102814_);
    }

    private static final class Entry {
        private final RunState run = new RunState();
        private final RunLease lease = new RunLease();
        private final int ownerId;
        private long touched;

        private Entry(int n, long l) {
            this.ownerId = n;
            this.touched = l;
        }
    }
}

