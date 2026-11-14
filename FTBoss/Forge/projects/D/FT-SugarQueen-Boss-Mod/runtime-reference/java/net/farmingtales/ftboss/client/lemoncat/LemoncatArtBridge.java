/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.client.lemoncat;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.client.lemoncat.LemoncatArtRenderer;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.client.render.SpellVisualRenderer;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class LemoncatArtBridge {
    private static final List<Visual> ACTIVE = new ArrayList<Visual>();
    private static ClientLevel world;

    private LemoncatArtBridge() {
    }

    private static ClientLevel update() {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel != world) {
            ACTIVE.clear();
            world = clientLevel;
        }
        if (world != null) {
            long l = world.m_46467_();
            ACTIVE.removeIf(visual -> !visual.packet.dimension().equals((Object)world.m_46472_().m_135782_()) || l < visual.refreshed || l - visual.refreshed >= (long)visual.packet.spell().lifetime(visual.packet.phase()));
        }
        return world;
    }

    public static void accept(SpellVisualPacket spellVisualPacket) {
        if (spellVisualPacket == null || !spellVisualPacket.valid()) {
            return;
        }
        if (!LemoncatArtRenderer.handles(spellVisualPacket.spell().id)) {
            SpellVisualRenderer.accept(spellVisualPacket);
            return;
        }
        ClientLevel clientLevel = LemoncatArtBridge.update();
        if (clientLevel == null || !spellVisualPacket.dimension().equals((Object)clientLevel.m_46472_().m_135782_())) {
            return;
        }
        long l = clientLevel.m_46467_();
        if (spellVisualPacket.phase() != SpellVisualRules.Phase.CHARGE) {
            ACTIVE.removeIf(visual -> visual.packet.owner().equals(spellVisualPacket.owner()) && visual.packet.spell() == spellVisualPacket.spell() && visual.packet.phase() == SpellVisualRules.Phase.CHARGE);
        }
        for (Visual visual2 : ACTIVE) {
            if (!visual2.packet.owner().equals(spellVisualPacket.owner()) || visual2.packet.spell() != spellVisualPacket.spell() || visual2.packet.phase() != spellVisualPacket.phase() || !(visual2.packet.start().m_82557_(spellVisualPacket.start()) < 0.16)) continue;
            visual2.packet = spellVisualPacket;
            visual2.refreshed = l;
            return;
        }
        if (ACTIVE.size() >= 96) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new Visual(spellVisualPacket, l));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase == TickEvent.Phase.END) {
            LemoncatArtBridge.update();
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        ClientLevel clientLevel = LemoncatArtBridge.update();
        if (clientLevel != null && !ACTIVE.isEmpty()) {
            LemoncatArtRenderer.renderIndependent(ACTIVE, renderLevelStageEvent, clientLevel);
        }
    }

    private static final class Visual
    implements OptimizedSpellVisual {
        private SpellVisualPacket packet;
        private final long born;
        private long refreshed;

        private Visual(SpellVisualPacket spellVisualPacket, long l) {
            this.packet = spellVisualPacket;
            this.born = l;
            this.refreshed = l;
        }

        @Override
        public SpellVisualPacket packet() {
            return this.packet;
        }

        @Override
        public long born() {
            return this.born;
        }

        @Override
        public long refreshed() {
            return this.refreshed;
        }

        @Override
        public boolean nativeLight() {
            return false;
        }
    }
}

