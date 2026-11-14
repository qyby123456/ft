/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.joml.Quaternionf
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneAssets;
import net.farmingtales.ftboss.client.qizaiarcane.ArcanePainter;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneRig;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class ArcanePacketVisuals {
    private static final ArrayList<Pulse> pulses = new ArrayList();
    private static ClientLevel scene;

    private ArcanePacketVisuals() {
    }

    public static boolean receive(SpellVisualPacket spellVisualPacket) {
        if (spellVisualPacket == null || spellVisualPacket.spell() == null) {
            return false;
        }
        String string = spellVisualPacket.spell().id;
        if (!string.equals("star_cross") && !string.equals("rose_starfall")) {
            return false;
        }
        if (ArcaneAssets.get(string) == null) {
            return false;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (!spellVisualPacket.valid() || clientLevel == null || !spellVisualPacket.dimension().equals((Object)clientLevel.m_46472_().m_135782_())) {
            return true;
        }
        if (scene != clientLevel) {
            pulses.clear();
            scene = clientLevel;
        }
        for (Entity entity : clientLevel.m_104735_()) {
            EasterBossEntity easterBossEntity;
            if (!(entity instanceof EasterBossEntity) || !(easterBossEntity = (EasterBossEntity)entity).m_20148_().equals(spellVisualPacket.owner()) || easterBossEntity.getVariant() != 11) continue;
            int n = easterBossEntity.getSkillCue().m_128451_("qizai");
            if (spellVisualPacket.phase() != SpellVisualRules.Phase.BEAM || n < 7 || n > 10 || ArcaneAssets.kind(n) == null) break;
            return true;
        }
        pulses.removeIf(pulse -> pulse.packet().owner().equals(spellVisualPacket.owner()) && pulse.packet().spell() == spellVisualPacket.spell() && pulse.packet().start().m_82557_(spellVisualPacket.start()) < 1.0E-4 && (pulse.packet().phase() != SpellVisualRules.Phase.BEAM && spellVisualPacket.phase() != SpellVisualRules.Phase.BEAM || pulse.packet().phase() == spellVisualPacket.phase() && pulse.packet().end().m_82557_(spellVisualPacket.end()) < 1.0E-4));
        if (pulses.size() == 64) {
            pulses.remove(0);
        }
        pulses.add(new Pulse(spellVisualPacket, clientLevel.m_46467_()));
        return true;
    }

    @SubscribeEvent
    public static void unloaded(LevelEvent.Unload unload) {
        if (unload.getLevel() == scene) {
            pulses.clear();
            scene = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void draw(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel != scene || clientLevel == null) {
            pulses.clear();
            scene = clientLevel;
            return;
        }
        long l = clientLevel.m_46467_();
        pulses.removeIf(pulse -> l < pulse.start() || l - pulse.start() > (long)pulse.packet().spell().lifetime(pulse.packet().phase()));
        if (pulses.isEmpty()) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n = particleStatus == ParticleStatus.MINIMAL ? 3000 : (particleStatus == ParticleStatus.DECREASED ? 6500 : 12000);
        List<Pulse> list = pulses.stream().filter(pulse -> pulse.packet().start().m_82557_(vec3) < 4096.0 && renderLevelStageEvent.getFrustum().m_113029_(new AABB(pulse.packet().start(), pulse.packet().end()).m_82400_(6.5))).sorted(Comparator.comparingDouble(pulse -> pulse.packet().start().m_82557_(vec3))).toList();
        for (Pulse object : list) {
            double d;
            double d2;
            float f;
            SpellVisualPacket spellVisualPacket = object.packet();
            ArcaneRig.Effect effect = ArcaneAssets.get(spellVisualPacket.spell().id);
            if (effect == null || (f = (float)Math.max(0.0, Math.min(1.0, ((d2 = (double)Math.max(1, spellVisualPacket.spell().lifetime(spellVisualPacket.phase()))) - (d = (double)((float)(l - object.start()) + renderLevelStageEvent.getPartialTick()))) / 3.0))) <= 0.0f) continue;
            boolean bl = spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM;
            boolean bl2 = spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE;
            String string = bl2 ? "charge" : "release";
            double d3 = Math.min(bl2 ? 1.4 : 1.1, d / d2 * (bl2 ? 1.4 : 1.1));
            poseStack.m_85836_();
            try {
                poseStack.m_85837_(spellVisualPacket.start().f_82479_ - vec3.f_82479_, spellVisualPacket.start().f_82480_ - vec3.f_82480_, spellVisualPacket.start().f_82481_ - vec3.f_82481_);
                if (bl) {
                    if (n <= 0) continue;
                    n -= ArcanePainter.beam(effect, spellVisualPacket.start(), spellVisualPacket.end(), poseStack, (MultiBufferSource)bufferSource, n, f * (float)Math.min(1.0, 0.15 + d / 1.7));
                    continue;
                }
                poseStack.m_85841_(2.0f, 1.0f, 2.0f);
                Vec3 vec32 = new Vec3(2.0, 1.0, 2.0);
                ArcanePainter.scaled(effect, string, d3, poseStack, (MultiBufferSource)bufferSource, spellVisualPacket.start(), new Quaternionf(), false, true, false, 240, 0xF000F0, f, vec32);
                if (n <= 0) continue;
                n -= ArcanePainter.scaled(effect, string, d3, poseStack, (MultiBufferSource)bufferSource, spellVisualPacket.start(), new Quaternionf(), spellVisualPacket.start().m_82557_(vec3) < 144.0, false, true, n, 0xF000F0, f, vec32);
            }
            finally {
                poseStack.m_85849_();
            }
        }
        for (String string : List.of("star_cross", "rose_starfall")) {
            bufferSource.m_109912_(RenderType.m_110470_((ResourceLocation)new ResourceLocation("ftboss", "qizai_arcane/" + string + ".png")));
        }
    }

    private record Pulse(SpellVisualPacket packet, long start) {
    }
}

