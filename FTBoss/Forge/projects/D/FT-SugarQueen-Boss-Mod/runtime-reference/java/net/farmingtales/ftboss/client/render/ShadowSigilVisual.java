/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class ShadowSigilVisual {
    private static final ResourceLocation SIGIL = new ResourceLocation("ftboss", "textures/fx/shadow_sigil.png");
    private static final ResourceLocation PILLAR = new ResourceLocation("ftboss", "textures/fx/shadow_pillar.png");
    private static final int LIFE = 32;
    private static final int FADE_IN = 5;
    private static final int FADE_OUT = 10;
    private static final float SIGIL_SIZE = 2.8f;
    private static final double SIGIL_RADIUS = (double)1.4f;
    private static final float SPIN_DEG = 100.0f;
    private static final float PILLAR_SIZE = 1.05f;
    private static final float PILLAR_HEIGHT = 2.8f;
    private static final int PILLAR_SLICES = 4;
    private static final double LIFT = 0.03;
    private static final int DEDUPE_TICKS = 6;
    private static final List<Sigil> ACTIVE = new ArrayList<Sigil>();
    private static final int MAX_ACTIVE = 24;
    private static final boolean DEBUG = Boolean.getBoolean("ftboss.debugSigil");

    private ShadowSigilVisual() {
    }

    public static void spawn(double x, double y, double z, boolean pillar) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        long now = level.m_46467_();
        for (Sigil s : ACTIVE) {
            if (now - s.spawnTick > 6L) continue;
            double dx = s.x - x;
            double dz = s.z - z;
            double dy = s.y - y;
            if (!(dx * dx + dz * dz < 1.9599999332427984) || !(Math.abs(dy) < 1.0)) continue;
            return;
        }
        if (ACTIVE.size() >= 24) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new Sigil(x, y, z, pillar, now));
        if (DEBUG) {
            FTBossMod.LOGGER.info("[FTBoss][\u9547\u5370] \u767b\u8bb0: ({}, {}, {}) \u80fd\u91cf\u67f1={} \u6d3b\u52a8\u6570={}", new Object[]{String.format("%.2f", x), String.format("%.2f", y), String.format("%.2f", z), pillar, ACTIVE.size()});
        }
    }

    private static void tick() {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            ACTIVE.clear();
            return;
        }
        long now = level.m_46467_();
        Iterator<Sigil> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Sigil s = it.next();
            if (now - s.spawnTick < 32L) continue;
            it.remove();
            if (!DEBUG) continue;
            FTBossMod.LOGGER.info("[FTBoss][\u9547\u5370] \u8fc7\u671f\u79fb\u9664\uff0c\u5269\u4f59 {}", (Object)ACTIVE.size());
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            ShadowSigilVisual.tick();
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload e) {
        ACTIVE.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        Vec3 camPos = e.getCamera().m_90583_();
        float partialTick = e.getPartialTick();
        PoseStack pose = e.getPoseStack();
        pose.m_85836_();
        try {
            pose.m_85837_(-camPos.f_82479_, -camPos.f_82480_, -camPos.f_82481_);
            MultiBufferSource.BufferSource buffers = Minecraft.m_91087_().m_91269_().m_110104_();
            VertexConsumer sigilBuf = null;
            VertexConsumer pillarBuf = null;
            for (Sigil s : ACTIVE) {
                float age = (float)(level.m_46467_() - s.spawnTick) + partialTick;
                float alpha = ShadowSigilVisual.alphaAt(age);
                if (alpha <= 0.01f) continue;
                AABB box = new AABB(s.x - (double)1.4f, s.y - 0.2, s.z - (double)1.4f, s.x + (double)1.4f, s.y + (s.pillar ? (double)2.8f : 0.4), s.z + (double)1.4f);
                if (e.getFrustum() != null && !e.getFrustum().m_113029_(box)) continue;
                if (sigilBuf == null) {
                    sigilBuf = buffers.m_6299_(RenderType.m_110488_((ResourceLocation)SIGIL));
                }
                if (s.pillar && pillarBuf == null) {
                    pillarBuf = buffers.m_6299_(RenderType.m_110488_((ResourceLocation)PILLAR));
                }
                float spin = 100.0f * (age / 32.0f);
                pose.m_85836_();
                try {
                    pose.m_85837_(s.x, s.y + 0.03, s.z);
                    ShadowSigilVisual.renderSigil(pose, sigilBuf, spin, alpha);
                    if (!s.pillar) continue;
                    ShadowSigilVisual.renderPillar(pose, pillarBuf, spin, alpha * 0.85f, age);
                }
                finally {
                    pose.m_85849_();
                }
            }
            if (pillarBuf != null) {
                buffers.m_109912_(RenderType.m_110488_((ResourceLocation)PILLAR));
            }
            if (sigilBuf != null) {
                buffers.m_109912_(RenderType.m_110488_((ResourceLocation)SIGIL));
            }
        }
        finally {
            pose.m_85849_();
        }
    }

    private static float alphaAt(float age) {
        if (age < 0.0f) {
            return 0.0f;
        }
        if (age < 5.0f) {
            return age / 5.0f;
        }
        float tail = 22.0f;
        if (age <= tail) {
            return 1.0f;
        }
        float t = (age - tail) / 10.0f;
        return t >= 1.0f ? 0.0f : 1.0f - t;
    }

    private static void renderSigil(PoseStack pose, VertexConsumer buf, float spinDeg, float alpha) {
        pose.m_85836_();
        ShadowSigilVisual.rotY(pose, spinDeg);
        PoseStack.Pose poseState = pose.m_85850_();
        float h = 1.4f;
        ShadowSigilVisual.vert(buf, poseState, -h, 0.0f, -h, 0.0f, 0.0f, alpha, 0.0f, 1.0f, 0.0f);
        ShadowSigilVisual.vert(buf, poseState, -h, 0.0f, h, 0.0f, 1.0f, alpha, 0.0f, 1.0f, 0.0f);
        ShadowSigilVisual.vert(buf, poseState, h, 0.0f, h, 1.0f, 1.0f, alpha, 0.0f, 1.0f, 0.0f);
        ShadowSigilVisual.vert(buf, poseState, h, 0.0f, -h, 1.0f, 0.0f, alpha, 0.0f, 1.0f, 0.0f);
        pose.m_85849_();
    }

    private static void renderPillar(PoseStack pose, VertexConsumer buf, float spinDeg, float alpha, float age) {
        pose.m_85836_();
        ShadowSigilVisual.rotY(pose, -spinDeg * 0.6f);
        float hw = 0.525f;
        float breathe = 1.0f + 0.06f * (float)Math.sin((double)age * 0.35);
        float top = 2.8f * breathe;
        for (int i = 0; i < 4; ++i) {
            pose.m_85836_();
            ShadowSigilVisual.rotY(pose, 180.0f * (float)i / 4.0f);
            PoseStack.Pose poseState = pose.m_85850_();
            ShadowSigilVisual.vert(buf, poseState, -hw, 0.0f, 0.0f, 0.0f, 1.0f, alpha, 0.0f, 0.0f, 1.0f);
            ShadowSigilVisual.vert(buf, poseState, hw, 0.0f, 0.0f, 1.0f, 1.0f, alpha, 0.0f, 0.0f, 1.0f);
            ShadowSigilVisual.vert(buf, poseState, hw, top, 0.0f, 1.0f, 0.0f, alpha, 0.0f, 0.0f, 1.0f);
            ShadowSigilVisual.vert(buf, poseState, -hw, top, 0.0f, 0.0f, 0.0f, alpha, 0.0f, 0.0f, 1.0f);
            pose.m_85849_();
        }
        pose.m_85849_();
    }

    private static void vert(VertexConsumer vertices, PoseStack.Pose pose, float posX, float posY, float posZ, float texU, float texV, float alpha, float normalX, float normalY, float normalZ) {
        vertices.m_252986_(pose.m_252922_(), posX, posY, posZ).m_85950_(1.0f, 1.0f, 1.0f, alpha).m_7421_(texU, texV).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(pose.m_252943_(), normalX, normalY, normalZ).m_5752_();
    }

    private static void rotY(PoseStack pose, float deg) {
        pose.m_252781_(Axis.f_252436_.m_252977_(deg));
    }

    private static final class Sigil {
        final double x;
        final double y;
        final double z;
        final boolean pillar;
        final long spawnTick;

        Sigil(double x, double y, double z, boolean pillar, long spawnTick) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.pillar = pillar;
            this.spawnTick = spawnTick;
        }
    }
}

