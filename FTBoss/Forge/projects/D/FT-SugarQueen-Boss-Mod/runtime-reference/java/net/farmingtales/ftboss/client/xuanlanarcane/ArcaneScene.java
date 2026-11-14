/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 */
package net.farmingtales.ftboss.client.xuanlanarcane;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.xuanlanarcane.ArcaneForms;
import net.farmingtales.ftboss.client.xuanlanarcane.ArcaneInk;
import net.farmingtales.ftboss.client.xuanlanarcane.ArcaneRhythm;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class ArcaneScene {
    private static final Map<Identity, Moment> MOMENTS = new LinkedHashMap<Identity, Moment>();
    private static final ResourceLocation ATLAS = new ResourceLocation("ftboss", "xuanlan_arcane/luminous-script.png");
    private static final RenderType FORM = RenderType.m_110473_((ResourceLocation)ATLAS);
    private static final RenderType RADIANCE = RenderType.m_234338_((ResourceLocation)ATLAS);
    private static ClientLevel activeWorld;

    private ArcaneScene() {
    }

    private static HitResult clip(ClientLevel clientLevel, Moment moment, Vec3 vec3, Vec3 vec32, Entity entity) {
        long l = clientLevel.m_46467_();
        if (moment.clipTick != l || moment.clipStart == null || moment.clipStart.m_82557_(vec3) > 1.0E-4 || moment.clipEnd.m_82557_(vec32) > 1.0E-4) {
            moment.clipResult = clientLevel.m_45547_(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
            moment.clipTick = l;
            moment.clipStart = vec3;
            moment.clipEnd = vec32;
        }
        return moment.clipResult;
    }

    public static boolean accept(SpellVisualPacket spellVisualPacket) {
        if (spellVisualPacket == null || spellVisualPacket.spell() == null || !ArcaneForms.belongs(spellVisualPacket.spell().id)) {
            return false;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null || !spellVisualPacket.valid() || !spellVisualPacket.dimension().equals((Object)clientLevel.m_46472_().m_135782_())) {
            return true;
        }
        if (clientLevel != activeWorld) {
            MOMENTS.clear();
            activeWorld = clientLevel;
        }
        Identity identity = new Identity(spellVisualPacket.owner(), spellVisualPacket.spell().id, spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM);
        long l = clientLevel.m_46467_();
        Moment moment = MOMENTS.get(identity);
        if (moment != null && moment.packet.phase() == spellVisualPacket.phase() && moment.packet.start().m_82557_(spellVisualPacket.start()) < 0.64) {
            moment.packet = spellVisualPacket;
            moment.refreshed = l;
        } else {
            if (MOMENTS.size() == 48 && !MOMENTS.containsKey(identity)) {
                MOMENTS.remove(MOMENTS.keySet().iterator().next());
            }
            MOMENTS.put(identity, new Moment(spellVisualPacket, l));
        }
        return true;
    }

    @SubscribeEvent
    public static void advance(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel != activeWorld || clientLevel == null) {
            MOMENTS.clear();
            activeWorld = clientLevel;
            return;
        }
        long l = clientLevel.m_46467_();
        MOMENTS.values().removeIf(moment -> l < moment.refreshed || l - moment.refreshed > (long)moment.packet.spell().lifetime(moment.packet.phase()));
    }

    @SubscribeEvent
    public static void exit(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        MOMENTS.clear();
        activeWorld = null;
    }

    @SubscribeEvent
    public static void leave(LevelEvent.Unload unload) {
        if (unload.getLevel() == activeWorld) {
            MOMENTS.clear();
            activeWorld = null;
        }
    }

    @SubscribeEvent
    public static void paint(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || MOMENTS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null || clientLevel != activeWorld) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        Entity entity = renderLevelStageEvent.getCamera().m_90592_();
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        double d = minecraft.m_91104_() ? 0.0 : (double)renderLevelStageEvent.getPartialTick();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n = particleStatus == ParticleStatus.MINIMAL ? 4000 : (particleStatus == ParticleStatus.DECREASED ? 8000 : 14000);
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        ArcaneInk.Brush brush = new ArcaneInk.Brush();
        ArrayList<Moment> arrayList = new ArrayList<Moment>(MOMENTS.values());
        arrayList.sort(Comparator.comparingDouble(moment -> moment.packet.start().m_82557_(vec3)));
        int n2 = 0;
        for (Moment moment2 : arrayList) {
            Quaternionf quaternionf;
            double d2;
            SpellVisualPacket spellVisualPacket = moment2.packet;
            Entity entity2 = clientLevel.m_6815_(spellVisualPacket.ownerId());
            if (entity2 != null && (!entity2.m_20148_().equals(spellVisualPacket.owner()) || !entity2.m_6084_())) continue;
            ArcaneForms.Spell spell = ArcaneForms.get(spellVisualPacket.spell().id);
            boolean bl2 = spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM;
            boolean bl3 = bl && spellVisualPacket.spell().attached && entity != null && entity.m_20148_().equals(spellVisualPacket.owner());
            double d3 = (double)clientLevel.m_46467_() + d - (double)moment2.begin;
            double d4 = (double)spellVisualPacket.spell().lifetime(spellVisualPacket.phase()) - ((double)clientLevel.m_46467_() + d - (double)moment2.refreshed);
            float f = (float)Math.max(0.0, Math.min(1.0, d4 / 7.0));
            if (f <= 0.0f) continue;
            Vec3 vec32 = spellVisualPacket.end().m_82546_(spellVisualPacket.start());
            if (vec32.m_82556_() < 1.0E-8) {
                vec32 = entity2 == null ? new Vec3(0.0, 0.0, 1.0) : entity2.m_20252_((float)d);
            }
            vec32 = vec32.m_82541_();
            double d5 = entity2 == null ? 1.0 : SpellVisuals.radiusScale(entity2, spell.id());
            double d6 = entity2 == null ? 0.6 : (double)entity2.m_20205_();
            double d7 = d2 = ArcaneRhythm.size(spell.id(), d5, d6);
            Vec3 vec33 = spellVisualPacket.start();
            if (bl2) {
                Vec3 vec34 = ArcaneScene.clip(clientLevel, moment2, vec33, spellVisualPacket.end(), entity2).m_82450_();
                Vec3 vec35 = vec34.m_82546_(vec33);
                d7 = vec35.m_82553_();
                if (d7 < 0.02 || d7 > 128.0) continue;
                vec32 = vec35.m_82541_();
                d2 = 1.0;
                quaternionf = ArcaneRhythm.orientPositiveZ(new Quaternionf(), 0.0f, 0.0f, 1.0f, (float)vec32.f_82479_, (float)vec32.f_82480_, (float)vec32.f_82481_);
            } else if (spell.id().equals("pressure_lance")) {
                double d8;
                double d9;
                Vec3 vec36 = entity2 == null ? vec33.m_82520_(0.0, 1.6, 0.0) : entity2.m_146892_();
                HitResult hitResult = ArcaneScene.clip(clientLevel, moment2, vec36, (vec33 = vec36.m_82549_(vec32.m_82490_((d9 = ArcaneRhythm.muzzle(spell, d2, d6, entity2 == null ? 1.8 : (double)entity2.m_20206_(), entity2 == null ? 1.6 : vec36.f_82480_ - entity2.m_20186_(), vec32.f_82479_, vec32.f_82480_, vec32.f_82481_)) + (d8 = spellVisualPacket.phase() == SpellVisualRules.Phase.RELEASE ? ArcaneRhythm.recoil(d3) : 0.0)))).m_82549_(vec32.m_82490_(d2)), entity2);
                if (hitResult.m_6662_() != HitResult.Type.MISS) continue;
                quaternionf = ArcaneRhythm.orientPositiveZ(new Quaternionf(), 0.0f, 0.0f, 1.0f, (float)vec32.f_82479_, (float)vec32.f_82480_, (float)vec32.f_82481_);
            } else if (spell.id().equals("orbital_gears")) {
                if (entity2 != null) {
                    vec33 = entity2.m_20318_((float)d);
                }
                quaternionf = new Quaternionf().rotationY((float)(Math.atan2(vec32.f_82479_, vec32.f_82481_) + d3 * 0.021));
            } else {
                HitResult hitResult = ArcaneScene.clip(clientLevel, moment2, vec33.m_82520_(0.0, 1.0, 0.0), vec33.m_82520_(0.0, -3.0, 0.0), entity2);
                if (hitResult.m_6662_() == HitResult.Type.BLOCK) {
                    vec33 = hitResult.m_82450_().m_82520_(0.0, 0.03, 0.0);
                }
                quaternionf = new Quaternionf().rotationY((float)(d3 * 0.009));
            }
            double d10 = Math.max(0.5, Math.min(12.0, spellVisualPacket.spell().radius * d5));
            double d11 = spell.id().equals("magnetic_storm") ? Math.max(4.0, d10) : 4.0;
            AABB aABB = bl2 ? new AABB(vec33, vec33.m_82549_(vec32.m_82490_(d7))).m_82400_(0.25) : new AABB(vec33.m_82520_(-d11, -2.0, -d11), vec33.m_82520_(d11, 5.0, d11));
            if (aABB.m_272282_(vec3) > 6400.0 || !renderLevelStageEvent.getFrustum().m_113029_(aABB)) continue;
            int n3 = LevelRenderer.m_109541_((BlockAndTintGetter)clientLevel, (BlockPos)BlockPos.m_274446_((Position)vec33));
            boolean bl4 = particleStatus != ParticleStatus.MINIMAL && vec33.m_82557_(vec3) < 900.0;
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 2; ++j) {
                    for (ArcaneForms.Layer layer : spell.layers()) {
                        int n4;
                        int n5 = layer.role().equals("boundary") ? 0 : (n4 = layer.role().equals("solid") ? 1 : 2);
                        if (layer.role().equals("beam") != bl2 || n4 != i) continue;
                        if (layer.detail() != (j == 1) || layer.detail() && !bl4 || bl3 && !bl2 && layer.role().equals("solid") || !ArcaneRhythm.wholeLayer(layer, n - n2)) continue;
                        float f2 = f * layer.alpha() * ArcaneRhythm.pulse(layer, d3) * ArcaneRhythm.viewWeight(layer.role(), bl3, bl2);
                        double d12 = layer.role().equals("boundary") ? d10 : d2;
                        Matrix4f matrix4f = ArcaneRhythm.relative(spell, layer, d3, vec33.f_82479_, vec33.f_82480_, vec33.f_82481_, vec3, quaternionf, d12, bl2 ? d7 : d12);
                        VertexConsumer vertexConsumer = bufferSource.m_6299_(i == 1 ? FORM : RADIANCE);
                        for (ArcaneForms.Facet facet : layer.facets()) {
                            n2 += ArcaneInk.quad(facet, layer, matrix4f, vertexConsumer, renderLevelStageEvent.getPoseStack(), Vec3.f_82478_, !bl2 && bl, f2, i == 1 ? n3 : 0xF000F0, brush);
                        }
                    }
                }
            }
        }
        bufferSource.m_109912_(FORM);
        bufferSource.m_109912_(RADIANCE);
    }

    private static final class Moment {
        private SpellVisualPacket packet;
        private final long begin;
        private long refreshed;
        private long clipTick = Long.MIN_VALUE;
        private Vec3 clipStart;
        private Vec3 clipEnd;
        private HitResult clipResult;

        private Moment(SpellVisualPacket spellVisualPacket, long l) {
            this.packet = spellVisualPacket;
            this.begin = l;
            this.refreshed = l;
        }
    }

    private record Identity(UUID owner, String spell, boolean beam) {
    }
}

