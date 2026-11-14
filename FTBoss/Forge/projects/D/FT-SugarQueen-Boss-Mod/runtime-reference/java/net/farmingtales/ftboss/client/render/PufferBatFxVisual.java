/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.world.phys.AABB
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
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayDeque;
import net.farmingtales.ftboss.skill.WeaponFxPacket;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class PufferBatFxVisual {
    private static final int MAX_ACTIVE = 64;
    private static final double VIEW_DISTANCE_SQUARED = 5184.0;
    private static final int TOXIN = 8767818;
    private static final int ACID = 14286706;
    private static final int GOLD = 16045146;
    private static final ArrayDeque<Effect> ACTIVE = new ArrayDeque();
    private static ClientLevel activeLevel;

    private PufferBatFxVisual() {
    }

    public static void receive(WeaponFxPacket packet) {
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (!(level != null && packet.valid() && packet.kind().pufferBat() && level.m_46472_().m_135782_().equals((Object)packet.dimension()))) {
            return;
        }
        if (activeLevel != level) {
            PufferBatFxVisual.clear();
            activeLevel = level;
        }
        if (packet.kind() == WeaponFxRules.Kind.PUFFER_CLOUD) {
            ACTIVE.removeIf(effect -> effect.packet.kind() == WeaponFxRules.Kind.PUFFER_CLOUD && effect.packet.start().m_82557_(packet.start()) < 0.04);
        }
        while (ACTIVE.size() >= 64) {
            ACTIVE.removeFirst();
        }
        ACTIVE.addLast(new Effect(packet, level.m_46467_()));
    }

    private static void clear() {
        ACTIVE.clear();
        activeLevel = null;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null || minecraft.f_91073_ != activeLevel) {
            PufferBatFxVisual.clear();
            return;
        }
        if (!minecraft.m_91104_()) {
            long now = minecraft.f_91073_.m_46467_();
            ACTIVE.removeIf(effect -> now < effect.started || now - effect.started >= (long)effect.packet.kind().ticks);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == activeLevel) {
            PufferBatFxVisual.clear();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        PufferBatFxVisual.clear();
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
        if (minecraft.f_91073_ == null || minecraft.f_91073_ != activeLevel) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        VertexConsumer vertices = buffers.m_6299_(PufferRenderType.TYPE);
        pose.m_85836_();
        try {
            pose.m_85837_(-camera.f_82479_, -camera.f_82480_, -camera.f_82481_);
            Matrix4f matrix = pose.m_85850_().m_252922_();
            double worldTime = (double)minecraft.f_91073_.m_46467_() + (minecraft.m_91104_() ? 0.0 : (double)event.getPartialTick());
            for (Effect effect : ACTIVE) {
                WeaponFxPacket packet = effect.packet;
                AABB bounds = new AABB(packet.start(), packet.end()).m_82400_((double)packet.radius() + 1.0);
                if (camera.m_82557_(bounds.m_82399_()) > 5184.0 || event.getFrustum() != null && !event.getFrustum().m_113029_(bounds)) continue;
                double age = worldTime - (double)effect.started;
                float progress = PufferBatFxVisual.clamp((float)(age / (double)packet.kind().ticks));
                switch (packet.kind()) {
                    case PUFFER_CLOUD: {
                        PufferBatFxVisual.cloud(vertices, matrix, packet.start(), packet.radius(), progress, worldTime);
                        break;
                    }
                    case PUFFER_BURST: {
                        PufferBatFxVisual.burst(vertices, matrix, packet.start(), packet.radius(), progress, worldTime);
                        break;
                    }
                    case PUFFER_SHOCKWAVE: {
                        PufferBatFxVisual.shockwave(vertices, matrix, packet.start(), packet.radius(), progress, worldTime);
                        break;
                    }
                }
            }
        }
        finally {
            buffers.m_109912_(PufferRenderType.TYPE);
            pose.m_85849_();
        }
    }

    private static void cloud(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius, float progress, double time) {
        float fade = progress < 0.78f ? 1.0f : 1.0f - PufferBatFxVisual.smooth((progress - 0.78f) / 0.22f);
        double breath = 1.0 + Math.sin(time * 0.2) * 0.025;
        Vec3 horizontal = new Vec3(1.0, 0.0, 0.0);
        Vec3 depth = new Vec3(0.0, 0.0, 1.0);
        PufferBatFxVisual.ring(vertices, matrix, center.m_82520_(0.0, 0.055, 0.0), horizontal, depth, radius * breath, 0.12, 8767818, 0.58f * fade, 48, time * 0.025);
        PufferBatFxVisual.ring(vertices, matrix, center.m_82520_(0.0, 0.065, 0.0), horizontal, depth, radius * 0.82, 0.055, 14286706, 0.34f * fade, 40, -time * 0.045);
        PufferBatFxVisual.ring(vertices, matrix, center.m_82520_(0.0, 0.08, 0.0), horizontal, depth, radius * (0.32 + 0.08 * Math.sin(time * 0.13)), 0.08, 16045146, 0.24f * fade, 28, time * 0.065);
        for (int layer = 0; layer < 3; ++layer) {
            double height = 0.28 + (double)layer * 0.34 + Math.sin(time * 0.15 + (double)layer) * 0.045;
            double layerRadius = radius * (0.72 - (double)layer * 0.14);
            PufferBatFxVisual.ring(vertices, matrix, center.m_82520_(0.0, height, 0.0), horizontal, depth, layerRadius, 0.07, layer == 1 ? 14286706 : 8767818, (0.18f - (float)layer * 0.035f) * fade, 32, time * (layer % 2 == 0 ? 0.04 : -0.05) + (double)layer);
        }
        PufferBatFxVisual.crown(vertices, matrix, center.m_82520_(0.0, 0.07, 0.0), radius * 0.93, 0.3 + Math.sin(time * 0.22) * 0.05, 8767818, 0.38f * fade, 16, time * 0.03);
    }

    private static void burst(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius, float progress, double time) {
        float fade = 1.0f - PufferBatFxVisual.smooth(progress);
        double expansion = radius * (0.12 + 0.88 * (double)PufferBatFxVisual.smooth(progress));
        PufferBatFxVisual.ring(vertices, matrix, center.m_82520_(0.0, 0.07, 0.0), new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), expansion, Math.max(0.06, radius * 0.055), 14286706, 0.9f * fade, 40, time * 0.08);
        double dome = Math.min(expansion, radius * 0.75);
        PufferBatFxVisual.ring(vertices, matrix, center.m_82520_(0.0, 0.15, 0.0), new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 1.0, 0.0), dome, Math.max(0.035, radius * 0.025), 8767818, 0.42f * fade, 28, time * 0.06);
        PufferBatFxVisual.ring(vertices, matrix, center.m_82520_(0.0, 0.15, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(0.0, 1.0, 0.0), dome, Math.max(0.035, radius * 0.025), 16045146, 0.3f * fade, 28, -time * 0.05);
        PufferBatFxVisual.crown(vertices, matrix, center.m_82520_(0.0, 0.05, 0.0), expansion * 0.92, Math.max(0.25, radius * 0.18), 14286706, 0.75f * fade, 16, time * 0.05);
    }

    private static void shockwave(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius, float progress, double time) {
        float fade = 1.0f - PufferBatFxVisual.smooth(progress);
        double eased = PufferBatFxVisual.smooth(progress);
        double outer = radius * (0.1 + 0.9 * eased);
        double inner = Math.max(0.05, outer - 0.22 - 0.12 * (1.0 - (double)progress));
        Vec3 horizontal = new Vec3(1.0, 0.0, 0.0);
        Vec3 depth = new Vec3(0.0, 0.0, 1.0);
        PufferBatFxVisual.ring(vertices, matrix, center.m_82520_(0.0, 0.08, 0.0), horizontal, depth, outer, outer - inner, 14286706, 0.88f * fade, 48, time * 0.04);
        PufferBatFxVisual.ring(vertices, matrix, center.m_82520_(0.0, 0.11, 0.0), horizontal, depth, outer * 0.78, 0.07, 16045146, 0.78f * fade, 40, -time * 0.07);
        PufferBatFxVisual.wall(vertices, matrix, center, outer, 0.18 + 0.85 * (1.0 - (double)progress), 8767818, 0.32f * fade, 32, time * 0.035);
        PufferBatFxVisual.crown(vertices, matrix, center.m_82520_(0.0, 0.07, 0.0), outer * 0.9, 0.55 + 0.45 * (1.0 - (double)progress), 16045146, 0.84f * fade, 20, time * 0.06);
    }

    private static void crown(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius, double height, int color, float alpha, int spikes, double spin) {
        for (int index = 0; index < spikes; ++index) {
            double angle = spin + (double)index * Math.PI * 2.0 / (double)spikes;
            Vec3 outward = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            Vec3 tangent = new Vec3(-outward.f_82481_, 0.0, outward.f_82479_);
            Vec3 base = center.m_82549_(outward.m_82490_(radius));
            Vec3 tip = center.m_82549_(outward.m_82490_(radius + height * 0.28)).m_82520_(0.0, height, 0.0);
            double width = Math.max(0.025, height * 0.11);
            PufferBatFxVisual.quad(vertices, matrix, base.m_82546_(tangent.m_82490_(width)), base.m_82549_(tangent.m_82490_(width)), tip.m_82549_(tangent.m_82490_(width * 0.12)), tip.m_82546_(tangent.m_82490_(width * 0.12)), color, alpha);
        }
    }

    private static void wall(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius, double height, int color, float alpha, int segments, double spin) {
        for (int index = 0; index < segments; ++index) {
            double first = spin + (double)index * Math.PI * 2.0 / (double)segments;
            double second = spin + ((double)index + 1.0) * Math.PI * 2.0 / (double)segments;
            Vec3 lowFirst = center.m_82520_(Math.cos(first) * radius, 0.06, Math.sin(first) * radius);
            Vec3 lowSecond = center.m_82520_(Math.cos(second) * radius, 0.06, Math.sin(second) * radius);
            PufferBatFxVisual.quad(vertices, matrix, lowFirst, lowSecond, lowSecond.m_82520_(0.0, height, 0.0), lowFirst.m_82520_(0.0, height, 0.0), color, alpha);
        }
    }

    private static void ring(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Vec3 horizontal, Vec3 vertical, double radius, double width, int color, float alpha, int segments, double spin) {
        double inner = Math.max(0.0, radius - width);
        for (int index = 0; index < segments; ++index) {
            double first = spin + (double)index * Math.PI * 2.0 / (double)segments;
            double second = spin + ((double)index + 1.0) * Math.PI * 2.0 / (double)segments;
            Vec3 firstAxis = horizontal.m_82490_(Math.cos(first)).m_82549_(vertical.m_82490_(Math.sin(first)));
            Vec3 secondAxis = horizontal.m_82490_(Math.cos(second)).m_82549_(vertical.m_82490_(Math.sin(second)));
            PufferBatFxVisual.quad(vertices, matrix, center.m_82549_(firstAxis.m_82490_(inner)), center.m_82549_(firstAxis.m_82490_(radius)), center.m_82549_(secondAxis.m_82490_(radius)), center.m_82549_(secondAxis.m_82490_(inner)), color, alpha);
        }
    }

    private static void quad(VertexConsumer vertices, Matrix4f matrix, Vec3 first, Vec3 second, Vec3 third, Vec3 fourth, int color, float alpha) {
        PufferBatFxVisual.vertex(vertices, matrix, first, color, alpha);
        PufferBatFxVisual.vertex(vertices, matrix, second, color, alpha);
        PufferBatFxVisual.vertex(vertices, matrix, third, color, alpha);
        PufferBatFxVisual.vertex(vertices, matrix, fourth, color, alpha);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, Vec3 point, int color, float alpha) {
        vertices.m_252986_(matrix, (float)point.f_82479_, (float)point.f_82480_, (float)point.f_82481_).m_6122_(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, Math.max(0, Math.min(255, (int)(alpha * 255.0f)))).m_5752_();
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static float smooth(float value) {
        float bounded = PufferBatFxVisual.clamp(value);
        return bounded * bounded * (3.0f - 2.0f * bounded);
    }

    private record Effect(WeaponFxPacket packet, long started) {
    }

    private static final class PufferRenderType
    extends RenderType {
        private static final RenderType TYPE = PufferRenderType.m_173215_((String)"ftboss_puffer_bat_mesh", (VertexFormat)DefaultVertexFormat.f_85815_, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)8192, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.m_110628_().m_173292_(f_173104_).m_110685_(f_110139_).m_110663_(f_110113_).m_110687_(f_110115_).m_110661_(f_110110_).m_110691_(false));

        private PufferRenderType() {
            super("ftboss_puffer_bat_mesh", DefaultVertexFormat.f_85815_, VertexFormat.Mode.QUADS, 8192, false, true, () -> {}, () -> {});
        }
    }
}

