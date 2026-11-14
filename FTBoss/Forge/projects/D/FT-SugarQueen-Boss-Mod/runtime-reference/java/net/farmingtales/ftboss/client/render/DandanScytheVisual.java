/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
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
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.client.render.DandanScytheMotion;
import net.farmingtales.ftboss.client.render.DandanScytheRenderer;
import net.farmingtales.ftboss.skill.DandanScytheFxPacket;
import net.farmingtales.ftboss.util.DandanScytheMotionRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class DandanScytheVisual {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/fx/dandan_candle_light.png");
    private static final RenderType LIGHT = RenderType.m_110488_((ResourceLocation)TEXTURE);
    private static final int MAX_ACTIVE = 96;
    private static final List<Flash> ACTIVE = new ArrayList<Flash>();
    private static ClientLevel activeLevel;

    private DandanScytheVisual() {
    }

    public static void receive(DandanScytheFxPacket message) {
        if (DandanScytheMotionRules.action(message.kind())) {
            DandanScytheMotion.receive(message);
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null || !level.m_46472_().m_135782_().equals((Object)message.dimension())) {
            return;
        }
        if (activeLevel != level) {
            ACTIVE.clear();
            activeLevel = level;
        }
        if (message.kind() == 2 || message.kind() == 3) {
            ACTIVE.removeIf(flash -> flash.message.kind() == 2 && flash.message.targetId() == message.targetId());
        }
        while (ACTIVE.size() >= 96) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new Flash(message, level.m_46467_(), message.kind() == 2 ? 100 : 14));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        DandanScytheMotion.tick((Level)level);
        if (level == null || level != activeLevel) {
            ACTIVE.clear();
            activeLevel = level;
            return;
        }
        ACTIVE.removeIf(flash -> level.m_46467_() - flash.startTick >= (long)flash.lifetime || flash.message.kind() == 2 && (level.m_6815_(flash.message.targetId()) == null || !level.m_6815_(flash.message.targetId()).m_6084_()));
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == Minecraft.m_91087_().f_91073_) {
            DandanScytheMotion.reset(null);
            DandanScytheRenderer.clearPrepared();
        }
        if (event.getLevel() == activeLevel) {
            ACTIVE.clear();
            activeLevel = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || level != activeLevel) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        pose.m_85836_();
        try {
            pose.m_85837_(-camera.f_82479_, -camera.f_82480_, -camera.f_82481_);
            VertexConsumer vertices = buffers.m_6299_(LIGHT);
            for (Flash flash : ACTIVE) {
                DandanScytheFxPacket message = flash.message;
                float age = (float)(level.m_46467_() - flash.startTick) + event.getPartialTick();
                float progress = age / (float)flash.lifetime;
                if (progress < 0.0f || progress >= 1.0f) continue;
                Vec3 position = message.position();
                if (message.kind() == 2) {
                    Entity target = level.m_6815_(message.targetId());
                    if (target == null) continue;
                    position = target.m_20318_(event.getPartialTick()).m_82520_(0.0, (double)target.m_20206_() + 0.28, 0.0);
                }
                double radius = message.radius();
                AABB bounds = new AABB(position.f_82479_ - radius, position.f_82480_ - 0.2, position.f_82481_ - radius, position.f_82479_ + radius, position.f_82480_ + 2.0, position.f_82481_ + radius);
                if (event.getFrustum() != null && !event.getFrustum().m_113029_(bounds)) continue;
                pose.m_85836_();
                try {
                    pose.m_85837_(position.f_82479_, position.f_82480_, position.f_82481_);
                    float alpha = (1.0f - progress) * 0.88f;
                    if (message.kind() == 0) {
                        DandanScytheVisual.arc(vertices, pose.m_85850_(), radius * (0.82 + (double)progress * 0.18), 0.14, message.yaw() - 70.0f, message.yaw() + 70.0f, 0.0, alpha, false);
                        DandanScytheVisual.arc(vertices, pose.m_85850_(), radius * (0.82 + (double)progress * 0.18) - 0.22, 0.06, message.yaw() - 70.0f, message.yaw() + 70.0f, 0.05, alpha * 0.5f, true);
                        continue;
                    }
                    if (message.kind() == 2) {
                        float pulse = 0.4f + 0.15f * (float)Math.sin((double)age * 0.18);
                        DandanScytheVisual.arc(vertices, pose.m_85850_(), 0.3, 0.065, age * 2.0f, age * 2.0f + 300.0f, 0.0, pulse * Math.min(1.0f, (1.0f - progress) * 8.0f), true);
                        continue;
                    }
                    double growth = message.kind() == 1 ? 0.55 + (double)progress * 0.45 : 0.2 + (double)progress * 0.8;
                    DandanScytheVisual.arc(vertices, pose.m_85850_(), radius * growth, 0.1, 0.0, 360.0, 0.0, alpha, false);
                    DandanScytheVisual.arc(vertices, pose.m_85850_(), radius * growth * 0.78, 0.055, 0.0, 360.0, 0.09, alpha * 0.65f, true);
                    for (int ray = 0; ray < 6; ++ray) {
                        double radians = (double)ray * Math.PI / 3.0;
                        double horizontal = Math.cos(radians) * radius * growth * 0.5;
                        double depth = Math.sin(radians) * radius * growth * 0.5;
                        DandanScytheVisual.vertical(vertices, pose.m_85850_(), horizontal, depth, 0.45 * (double)(1.0f - progress), alpha * 0.5f);
                    }
                }
                finally {
                    pose.m_85849_();
                }
            }
            buffers.m_109912_(LIGHT);
        }
        finally {
            pose.m_85849_();
        }
    }

    private static void arc(VertexConsumer vertices, PoseStack.Pose pose, double radius, double width, double start, double end, double height, float alpha, boolean violet) {
        int segments = (int)Math.ceil((end - start) / 5.0);
        for (int segment = 0; segment < segments; ++segment) {
            double first = Math.toRadians(start + (end - start) * (double)segment / (double)segments);
            double second = Math.toRadians(start + (end - start) * (double)(segment + 1) / (double)segments);
            DandanScytheVisual.point(vertices, pose, -Math.sin(first) * radius, height, Math.cos(first) * radius, 0.0f, 0.0f, alpha, violet);
            DandanScytheVisual.point(vertices, pose, -Math.sin(first) * (radius - width), height, Math.cos(first) * (radius - width), 0.0f, 1.0f, alpha, violet);
            DandanScytheVisual.point(vertices, pose, -Math.sin(second) * (radius - width), height, Math.cos(second) * (radius - width), 1.0f, 1.0f, alpha, violet);
            DandanScytheVisual.point(vertices, pose, -Math.sin(second) * radius, height, Math.cos(second) * radius, 1.0f, 0.0f, alpha, violet);
        }
    }

    private static void vertical(VertexConsumer vertices, PoseStack.Pose pose, double horizontal, double depth, double height, float alpha) {
        DandanScytheVisual.point(vertices, pose, horizontal - 0.025, 0.0, depth, 0.0f, 0.0f, alpha, false);
        DandanScytheVisual.point(vertices, pose, horizontal + 0.025, 0.0, depth, 1.0f, 0.0f, alpha, false);
        DandanScytheVisual.point(vertices, pose, horizontal + 0.025, height, depth, 1.0f, 1.0f, alpha, false);
        DandanScytheVisual.point(vertices, pose, horizontal - 0.025, height, depth, 0.0f, 1.0f, alpha, false);
    }

    private static void point(VertexConsumer vertices, PoseStack.Pose pose, double horizontal, double vertical, double depth, float texU, float texV, float alpha, boolean violet) {
        vertices.m_252986_(pose.m_252922_(), (float)horizontal, (float)vertical, (float)depth).m_85950_((violet ? 0.68f : 1.0f) * alpha, (violet ? 0.5f : 0.9f) * alpha, (violet ? 1.0f : 0.72f) * alpha, alpha).m_7421_(texU, texV).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(pose.m_252943_(), 0.0f, 1.0f, 0.0f).m_5752_();
    }

    private record Flash(DandanScytheFxPacket message, long startTick, int lifetime) {
    }
}

