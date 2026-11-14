/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
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
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.network.AppleBlastNetwork;
import net.farmingtales.ftboss.util.AppleBlastLightRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class AppleBlastLightVisual {
    private static final int GOLD = 16765290;
    private static final int WHITE = 0xFFF3D3;
    private static final RenderType TYPE = RenderType.m_110502_();
    private static final List<Flash> ACTIVE = new ArrayList<Flash>();
    private static ClientLevel world;

    private AppleBlastLightVisual() {
    }

    public static void accept(AppleBlastNetwork.Burst burst) {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !burst.valid() || !clientLevel.m_46472_().m_135782_().equals((Object)burst.dimension())) {
            return;
        }
        if (world != clientLevel) {
            ACTIVE.clear();
            world = clientLevel;
        }
        long l = clientLevel.m_46467_();
        ACTIVE.removeIf(flash -> flash.born == l && flash.center.m_82531_(burst.x(), burst.y(), burst.z()) < 0.01);
        if (ACTIVE.size() >= 24) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new Flash(new Vec3(burst.x(), burst.y(), burst.z()), burst.radius(), l));
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
        ACTIVE.removeIf(flash -> l < flash.born || l - flash.born >= 10L);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty() || minecraft.f_91073_ == null || minecraft.f_91073_ != world) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        Vec3 vec32 = new Vec3(renderLevelStageEvent.getCamera().m_252775_());
        Vec3 vec33 = new Vec3(renderLevelStageEvent.getCamera().m_253028_());
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        VertexConsumer vertexConsumer = bufferSource.m_6299_(TYPE);
        poseStack.m_85836_();
        try {
            poseStack.m_85837_(-vec3.f_82479_, -vec3.f_82480_, -vec3.f_82481_);
            for (Flash flash : ACTIVE) {
                float f;
                float f2;
                if (vec3.m_82557_(flash.center) > 4096.0 || (f2 = AppleBlastLightRules.fade(f = (float)(minecraft.f_91073_.m_46467_() - flash.born) + renderLevelStageEvent.getPartialTick())) <= 0.0f) continue;
                double d = AppleBlastLightRules.ringRadius(flash.radius, f);
                Vec3 vec34 = flash.center.m_82520_(0.0, -0.55, 0.0);
                AppleBlastLightVisual.ring(poseStack, vertexConsumer, vec34, d, 0.1, 16765290, f2 * 0.12f);
                AppleBlastLightVisual.ring(poseStack, vertexConsumer, vec34.m_82520_(0.0, 0.004, 0.0), d, 0.022, 0xFFF3D3, f2 * 0.65f);
                if (!(f < 2.0f)) continue;
                float f3 = (1.0f - f / 2.0f) * 0.28f;
                double d2 = 0.55 + 0.15 * (double)f;
                AppleBlastLightVisual.quad(poseStack, vertexConsumer, flash.center.m_82549_(vec32.m_82490_(-d2)), flash.center.m_82549_(vec33.m_82490_(d2)), flash.center.m_82549_(vec32.m_82490_(d2)), flash.center.m_82549_(vec33.m_82490_(-d2)), 0xFFF3D3, f3);
            }
        }
        finally {
            poseStack.m_85849_();
            bufferSource.m_109912_(TYPE);
        }
    }

    private static void ring(PoseStack poseStack, VertexConsumer vertexConsumer, Vec3 vec3, double d, double d2, int n, float f) {
        for (int i = 0; i < 32; ++i) {
            double d3 = (double)i * Math.PI * 2.0 / 32.0;
            double d4 = (double)(i + 1) * Math.PI * 2.0 / 32.0;
            Vec3 vec32 = new Vec3(Math.cos(d3), 0.0, Math.sin(d3));
            Vec3 vec33 = new Vec3(Math.cos(d4), 0.0, Math.sin(d4));
            AppleBlastLightVisual.quad(poseStack, vertexConsumer, vec3.m_82549_(vec32.m_82490_(d + d2)), vec3.m_82549_(vec33.m_82490_(d + d2)), vec3.m_82549_(vec33.m_82490_(d - d2)), vec3.m_82549_(vec32.m_82490_(d - d2)), n, f);
        }
    }

    private static void quad(PoseStack poseStack, VertexConsumer vertexConsumer, Vec3 vec3, Vec3 vec32, Vec3 vec33, Vec3 vec34, int n, float f) {
        AppleBlastLightVisual.vertex(poseStack, vertexConsumer, vec3, n, f);
        AppleBlastLightVisual.vertex(poseStack, vertexConsumer, vec32, n, f);
        AppleBlastLightVisual.vertex(poseStack, vertexConsumer, vec33, n, f);
        AppleBlastLightVisual.vertex(poseStack, vertexConsumer, vec34, n, f);
    }

    private static void vertex(PoseStack poseStack, VertexConsumer vertexConsumer, Vec3 vec3, int n, float f) {
        vertexConsumer.m_252986_(poseStack.m_85850_().m_252922_(), (float)vec3.f_82479_, (float)vec3.f_82480_, (float)vec3.f_82481_).m_6122_(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, Math.max(0, Math.min(255, (int)(f * 255.0f)))).m_5752_();
    }

    private record Flash(Vec3 center, double radius, long born) {
    }
}

