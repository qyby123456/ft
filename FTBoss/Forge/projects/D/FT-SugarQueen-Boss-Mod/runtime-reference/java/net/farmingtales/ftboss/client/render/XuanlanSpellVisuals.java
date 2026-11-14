/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
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
 *  net.minecraft.world.phys.BlockHitResult
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
 *  org.joml.Quaternionf
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.client.render.XuanlanFacetPainter;
import net.farmingtales.ftboss.client.render.XuanlanSpellMesh;
import net.farmingtales.ftboss.client.render.XuanlanSpellMotion;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class XuanlanSpellVisuals {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "xuanlan/materials.png");
    private static final RenderType METAL = RenderType.m_110473_((ResourceLocation)TEXTURE);
    private static final RenderType LIGHT = RenderType.m_234338_((ResourceLocation)TEXTURE);
    private static final Map<String, Cast> ACTIVE = new LinkedHashMap<String, Cast>();
    private static ClientLevel world;

    private XuanlanSpellVisuals() {
    }

    public static boolean accept(SpellVisualPacket spellVisualPacket) {
        if (spellVisualPacket == null || spellVisualPacket.spell() == null || !XuanlanSpellMotion.supports(spellVisualPacket.spell().id)) {
            return false;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !spellVisualPacket.valid() || !clientLevel.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension())) {
            return true;
        }
        if (world != clientLevel) {
            ACTIVE.clear();
            world = clientLevel;
        }
        boolean bl = spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM;
        String string = String.valueOf(spellVisualPacket.owner()) + "/" + spellVisualPacket.spell().id + (bl ? "/beam" : "/model");
        long l = clientLevel.m_46467_();
        Cast cast = ACTIVE.get(string);
        if (cast != null && cast.packet.phase() == spellVisualPacket.phase() && cast.packet.start().m_82557_(spellVisualPacket.start()) < 1.0) {
            cast.packet = spellVisualPacket;
            cast.seen = l;
        } else {
            if (ACTIVE.size() >= 64 && !ACTIVE.containsKey(string)) {
                ACTIVE.remove(ACTIVE.keySet().iterator().next());
            }
            ACTIVE.put(string, new Cast(spellVisualPacket, l));
        }
        return true;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || world != clientLevel) {
            ACTIVE.clear();
            world = clientLevel;
            return;
        }
        long l = clientLevel.m_46467_();
        ACTIVE.values().removeIf(cast -> l < cast.seen || l - cast.seen > (long)cast.packet.spell().lifetime(cast.packet.phase()));
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == world) {
            ACTIVE.clear();
            world = null;
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        ACTIVE.clear();
        world = null;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        boolean bl = SpellBloomRenderer.begin(renderLevelStageEvent, ACTIVE.isEmpty() ^ true);
        try {
            XuanlanSpellVisuals.ftboss$renderWithoutSoftBloom(renderLevelStageEvent);
            return;
        }
        finally {
            SpellBloomRenderer.end(bl);
        }
    }

    private static void drawWarning(VertexConsumer vertexConsumer, PoseStack poseStack, Vec3 vec3, double d, double d2, float f, Vec3 vec32, boolean bl, Entity entity, XuanlanFacetPainter.Brush brush) {
        if (!Double.isFinite(d)) {
            return;
        }
        d = Math.max(0.5, Math.min(12.0, d));
        for (int i = 0; i < 12; ++i) {
            double d3 = (double)i * Math.PI / 6.0;
            double d4 = Math.cos(d3);
            double d5 = Math.sin(d3);
            double d6 = 0.027;
            double d7 = i % 3 == 0 ? 0.28 : 0.13;
            double[][] dArray = new double[4][3];
            for (int j = 0; j < 4; ++j) {
                double d8 = d + (j < 2 ? -d7 : d7);
                double d9 = j == 0 || j == 3 ? -d6 : d6;
                dArray[j] = new double[]{d4 * d8 - d5 * d9, 0.055, d5 * d8 + d4 * d9};
            }
            XuanlanSpellMesh.Face face = new XuanlanSpellMesh.Face(dArray, new double[][]{{0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}, {0.0, 0.0}}, "light", "cue", "flux_boundary", 7, 1.0f, 0, null);
            XuanlanFacetPainter.draw(vertexConsumer, poseStack, face, vec3, new Quaternionf(), 1.0, 1.0, d2, f * (float)(0.72 + 0.2 * Math.sin(d2 * 0.32)), vec32, bl, entity, false, 0xF000F0, brush);
        }
    }

    private static void ftboss$renderWithoutSoftBloom(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null || clientLevel != world) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        Entity entity = renderLevelStageEvent.getCamera().m_90592_();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n = particleStatus == ParticleStatus.MINIMAL ? 6000 : (particleStatus == ParticleStatus.DECREASED ? 12000 : 20000);
        boolean bl2 = particleStatus != ParticleStatus.MINIMAL;
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        double d = minecraft.m_91104_() ? 0.0 : (double)renderLevelStageEvent.getPartialTick();
        ArrayList<Cast> arrayList = new ArrayList<Cast>(ACTIVE.values());
        arrayList.sort(Comparator.comparingDouble(cast -> cast.packet.start().m_82557_(vec3)));
        int n2 = 0;
        XuanlanFacetPainter.Brush brush = new XuanlanFacetPainter.Brush();
        for (Cast cast2 : arrayList) {
            BlockHitResult blockHitResult;
            Quaternionf quaternionf;
            Vec3 vec32;
            XuanlanSpellMesh.Model model;
            boolean bl3;
            SpellVisualPacket spellVisualPacket = cast2.packet;
            Entity entity2 = clientLevel.m_6815_(spellVisualPacket.ownerId());
            if (entity2 != null && (!entity2.m_20148_().equals(spellVisualPacket.owner()) || !entity2.m_6084_())) continue;
            boolean bl4 = bl3 = spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM;
            if (bl && entity != null && entity.m_20148_().equals(spellVisualPacket.owner()) && spellVisualPacket.spell().attached && !bl3 || (model = XuanlanSpellMesh.find(spellVisualPacket.spell().id)) == null) continue;
            double d2 = (double)clientLevel.m_46467_() + d - (double)cast2.born;
            double d3 = (double)clientLevel.m_46467_() + d - (double)cast2.seen;
            double d4 = spellVisualPacket.spell().lifetime(spellVisualPacket.phase());
            float f = (float)Math.min(1.0, Math.max(0.0, (d4 - d3) / Math.min(8.0, d4)));
            if (f <= 0.0f) continue;
            Vec3 vec33 = spellVisualPacket.end().m_82546_(spellVisualPacket.start());
            if (vec33.m_82556_() < 1.0E-6) {
                vec33 = entity2 != null ? entity2.m_20252_((float)d) : new Vec3(0.0, 0.0, 1.0);
            }
            vec33 = vec33.m_82541_();
            double d5 = entity2 != null ? SpellVisuals.radiusScale(entity2, spellVisualPacket.spell().id) : 1.0;
            double d6 = XuanlanSpellMotion.scale(spellVisualPacket.spell().id, d5, d2);
            if (spellVisualPacket.spell().id.equals("orbital_gears") && entity2 != null) {
                d6 = XuanlanSpellMotion.orbitalScale(d6, entity2.m_20205_());
            }
            Vec3 vec34 = spellVisualPacket.start();
            double d7 = d6;
            if (bl3) {
                vec32 = clientLevel.m_45547_(new ClipContext(spellVisualPacket.start(), spellVisualPacket.end(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity2)).m_82450_();
                Vec3 vec35 = vec32.m_82546_(spellVisualPacket.start());
                d7 = vec35.m_82553_();
                if (d7 < 0.02 || d7 > 128.0) continue;
                vec33 = vec35.m_82541_();
                d6 = 1.0;
                quaternionf = new Quaternionf().rotationTo(0.0f, 0.0f, 1.0f, (float)vec33.f_82479_, (float)vec33.f_82480_, (float)vec33.f_82481_);
            } else if (spellVisualPacket.spell().id.equals("pressure_lance")) {
                vec32 = entity2 != null ? entity2.m_146892_() : vec34.m_82520_(0.0, 1.4, 0.0);
                double d8 = XuanlanSpellMotion.forwardOffset(model, d6, entity2 != null ? (double)entity2.m_20205_() : 0.6, entity2 != null ? (double)entity2.m_20206_() : 1.8, entity2 != null ? vec32.f_82480_ - entity2.m_20186_() : 1.6, vec33.f_82479_, vec33.f_82480_, vec33.f_82481_);
                vec34 = vec32.m_82549_(vec33.m_82490_(d8 + (spellVisualPacket.phase() == SpellVisualRules.Phase.RELEASE ? XuanlanSpellMotion.recoil(d2) : 0.0))).m_82520_(0.0, -0.22, 0.0);
                quaternionf = new Quaternionf().rotationTo(0.0f, 0.0f, 1.0f, (float)vec33.f_82479_, (float)vec33.f_82480_, (float)vec33.f_82481_);
                blockHitResult = clientLevel.m_45547_(new ClipContext(vec32, vec34.m_82549_(vec33.m_82490_(1.03 * d6)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity2));
                if (blockHitResult.m_6662_() != HitResult.Type.MISS) {
                    continue;
                }
            } else if (spellVisualPacket.spell().id.equals("orbital_gears")) {
                vec34 = entity2 != null ? entity2.m_20318_((float)d) : vec34;
                vec34 = vec34.m_82520_(0.0, 0.28 + 0.035 * Math.sin(d2 * 0.11), 0.0);
                quaternionf = new Quaternionf().rotationY((float)(Math.atan2(vec33.f_82479_, vec33.f_82481_) + d2 * 0.024));
            } else {
                vec32 = clientLevel.m_45547_(new ClipContext(vec34.m_82520_(0.0, 0.2, 0.0), vec34.m_82520_(0.0, -2.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity2));
                vec34 = new Vec3(vec34.f_82479_, Math.max(vec34.f_82480_, vec32.m_82450_().f_82480_) + 0.15, vec34.f_82481_);
                quaternionf = new Quaternionf().rotationY((float)Math.atan2(vec33.f_82479_, vec33.f_82481_));
            }
            if ((vec32 = bl3 ? new AABB(vec34, vec34.m_82549_(vec33.m_82490_(d7))).m_82400_(1.0) : new AABB(vec34.m_82520_(-5.0, -1.0, -5.0), vec34.m_82520_(5.0, 6.0, 5.0))).m_272282_(vec3) > 6400.0 || !renderLevelStageEvent.getFrustum().m_113029_((AABB)vec32)) continue;
            int n3 = LevelRenderer.m_109541_((BlockAndTintGetter)clientLevel, (BlockPos)BlockPos.m_274446_((Position)vec34));
            if (!bl3 && spellVisualPacket.spell().id.equals("magnetic_storm")) {
                XuanlanSpellVisuals.drawWarning(bufferSource.m_6299_(LIGHT), renderLevelStageEvent.getPoseStack(), vec34, spellVisualPacket.spell().radius * d5, d2, f, vec3, bl, entity, brush);
            }
            block1: for (int i = 0; i < 2; ++i) {
                blockHitResult = bufferSource.m_6299_(i == 0 ? METAL : LIGHT);
                for (XuanlanSpellMesh.Face face : model.faces()) {
                    if (face.group().startsWith("beam") != bl3 || face.kind().equals("light") != (i == 1) || (!bl2 || vec34.m_82557_(vec3) > 900.0) && face.group().endsWith("detail")) continue;
                    if (n2 >= (i == 0 ? n - 700 : n)) continue block1;
                    n2 += XuanlanFacetPainter.draw((VertexConsumer)blockHitResult, renderLevelStageEvent.getPoseStack(), face, vec34, quaternionf, d6, d7, d2, f * face.opacity(), vec3, bl, entity, bl3, i == 1 ? 0xF000F0 : n3, brush);
                }
            }
        }
        bufferSource.m_109912_(METAL);
        bufferSource.m_109912_(LIGHT);
    }

    private static final class Cast {
        private SpellVisualPacket packet;
        private final long born;
        private long seen;

        private Cast(SpellVisualPacket spellVisualPacket, long l) {
            this.packet = spellVisualPacket;
            this.born = l;
            this.seen = l;
        }
    }
}

