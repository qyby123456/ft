/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.RenderLevelStageEvent
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
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayDeque;
import net.farmingtales.ftboss.client.dora_native.DoraNativeVisual;
import net.farmingtales.ftboss.skill.WeaponFxPacket;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
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
public final class DoraGunFxVisual {
    private static final int MAX_ACTIVE = 96;
    private static final double VIEW_DISTANCE_SQUARED = 5184.0;
    private static final ArrayDeque<Shot> ACTIVE = new ArrayDeque();
    private static ClientLevel activeLevel;

    private DoraGunFxVisual() {
    }

    public static void receive(WeaponFxPacket weaponFxPacket) {
        DoraNativeVisual.receive(weaponFxPacket);
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
            DoraGunFxVisual.clear();
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
            DoraGunFxVisual.clear();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        DoraGunFxVisual.clear();
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
    }

    private static void renderCharge(VertexConsumer vertices, Matrix4f matrix, WeaponFxPacket packet, float progress, float age) {
        Basis basis = DoraGunFxVisual.basis(packet.end().m_82546_(packet.start()));
        Vec3 center = packet.start().m_165921_(packet.end(), 0.45);
        float fade = 1.0f - progress;
        double spin = (double)age * 0.48;
        int segments = 16;
        DoraGunFxVisual.ring(vertices, matrix, center, basis.side, basis.up, 0.08 + 0.18 * (double)fade, 0.022, packet.theme().color, 0.58f * fade, segments, spin);
        DoraGunFxVisual.ring(vertices, matrix, center.m_82549_(basis.forward.m_82490_(0.035)), basis.side, basis.up, 0.055 + 0.11 * (double)fade, 0.014, packet.theme().highlight, 0.9f * fade, segments, -spin * 1.35);
        DoraGunFxVisual.cross(vertices, matrix, center, basis.side, basis.up, 0.055 + 0.025 * (double)fade, 0.012, packet.theme().highlight, 0.95f * fade, spin);
    }

    private static void renderPulse(VertexConsumer vertices, Matrix4f matrix, WeaponFxPacket packet, float progress, float age, Vec3 camera) {
        Vec3 delta = packet.end().m_82546_(packet.start());
        double length = delta.m_82553_();
        if (length < 0.05) {
            return;
        }
        Basis basis = DoraGunFxVisual.basis(delta);
        Vec3 start = packet.start().m_82549_(basis.forward.m_82490_(Math.min(0.62, length * 0.28)));
        Vec3 end = packet.end();
        boolean nova = packet.kind() == WeaponFxRules.Kind.DORA_NOVA;
        float fade = (float)Math.pow(1.0 - (double)progress, 1.35);
        double breathe = 1.0 + Math.sin((double)age * 1.45) * 0.12;
        double width = (nova ? 0.16 : 0.095) * breathe;
        Vec3 facing = DoraGunFxVisual.billboardSide(start, end, camera, basis.side);
        Vec3 facingUp = basis.forward.m_82537_(facing).m_82541_();
        DoraGunFxVisual.ribbon(vertices, matrix, start, end, facing.m_82490_(width), packet.theme().color, 0.25f * fade);
        DoraGunFxVisual.ribbon(vertices, matrix, start, end, facingUp.m_82490_(width), packet.theme().color, 0.25f * fade);
        DoraGunFxVisual.ribbon(vertices, matrix, start, end, facing.m_82490_(width * 0.38), packet.theme().highlight, 0.92f * fade);
        DoraGunFxVisual.ribbon(vertices, matrix, start, end, facingUp.m_82490_(width * 0.38), packet.theme().highlight, 0.92f * fade);
        DoraGunFxVisual.ribbon(vertices, matrix, start, end, facing.m_82490_(width * 0.12), 0xFFFFFF, fade);
        int gates = nova ? 7 : 4;
        for (int index = 0; index < gates; ++index) {
            double along = ((double)index + 0.38 + (double)age * (nova ? 0.16 : 0.11)) / (double)gates;
            along -= Math.floor(along);
            Vec3 center = start.m_165921_(end, along);
            double radius = width * (nova ? 1.75 : 1.35);
            DoraGunFxVisual.ring(vertices, matrix, center, basis.side, basis.up, radius, width * 0.13, index % 2 == 0 ? packet.theme().highlight : packet.theme().color, 0.52f * fade, nova ? 12 : 10, (double)age * 0.22 + (double)index * 0.7);
        }
        DoraGunFxVisual.ring(vertices, matrix, start, basis.side, basis.up, width * 2.15, width * 0.22, packet.theme().highlight, 0.85f * fade, 16, (double)(-age) * 0.35);
        DoraGunFxVisual.cross(vertices, matrix, end.m_82546_(basis.forward.m_82490_(0.018)), basis.side, basis.up, width * (nova ? 2.6 : 2.0), width * 0.22, packet.theme().highlight, 0.8f * fade, (double)age * 0.4);
    }

    private static void renderTracer(VertexConsumer vertices, Matrix4f matrix, WeaponFxPacket packet, float progress, float age, Vec3 camera) {
        Vec3 delta = packet.end().m_82546_(packet.start());
        double length = delta.m_82553_();
        if (length < 0.03) {
            return;
        }
        Basis basis = DoraGunFxVisual.basis(delta);
        Vec3 start = packet.start().m_82549_(basis.forward.m_82490_(Math.min(0.54, length * 0.32)));
        Vec3 end = packet.end();
        Vec3 facing = DoraGunFxVisual.billboardSide(start, end, camera, basis.side);
        Vec3 facingUp = basis.forward.m_82537_(facing).m_82541_();
        float fade = 1.0f - DoraGunFxVisual.smooth(progress);
        double width = 0.035 + 0.014 * (1.0 - (double)progress);
        DoraGunFxVisual.taperedRibbon(vertices, matrix, start, end, facing, width * 1.8, width * 0.35, packet.theme().color, 0.4f * fade);
        DoraGunFxVisual.taperedRibbon(vertices, matrix, start, end, facingUp, width * 1.4, width * 0.22, packet.theme().highlight, 0.9f * fade);
        DoraGunFxVisual.ribbon(vertices, matrix, start, end, facing.m_82490_(width * 0.2), 0xFFFFFF, fade);
        DoraGunFxVisual.ring(vertices, matrix, start, basis.side, basis.up, 0.09 + width, 0.018, packet.theme().highlight, 0.8f * fade, 10, (double)age * 0.7);
        DoraGunFxVisual.cross(vertices, matrix, end, basis.side, basis.up, 0.1 + width * 1.8, 0.014, packet.theme().highlight, 0.9f * fade, (double)age * 0.55);
    }

    private static void renderImpact(VertexConsumer vertices, Matrix4f matrix, WeaponFxPacket packet, float progress, float age, Vec3 camera) {
        Vec3 center = packet.start();
        Vec3 view = camera.m_82546_(center);
        Basis viewBasis = DoraGunFxVisual.basis(view.m_82556_() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : view);
        float fade = 1.0f - DoraGunFxVisual.smooth(progress);
        double expansion = 0.15 + 0.85 * (double)DoraGunFxVisual.smooth(progress);
        double radius = Math.max(0.15, (double)packet.radius() * expansion);
        int segments = radius > 2.0 ? 32 : 20;
        DoraGunFxVisual.ring(vertices, matrix, center, new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), radius, Math.max(0.025, radius * 0.055), packet.theme().color, 0.72f * fade, segments, (double)age * 0.11);
        double halo = Math.min(radius, 1.35);
        DoraGunFxVisual.ring(vertices, matrix, center, viewBasis.side, viewBasis.up, halo, Math.max(0.025, halo * 0.065), packet.theme().highlight, 0.9f * fade, segments, (double)(-age) * 0.22);
        DoraGunFxVisual.cross(vertices, matrix, center, viewBasis.side, viewBasis.up, Math.min(0.9, 0.22 + (double)packet.radius() * 0.18), 0.028, 0xFFFFFF, fade, (double)age * 0.35);
        int rays = 8;
        for (int index = 0; index < rays; ++index) {
            double angle = (double)index * Math.PI * 2.0 / (double)rays + (double)age * 0.08;
            Vec3 direction = viewBasis.side.m_82490_(Math.cos(angle)).m_82549_(viewBasis.up.m_82490_(Math.sin(angle)));
            Vec3 tangent = viewBasis.forward.m_82537_(direction).m_82541_();
            double inner = halo * 0.35;
            double outer = halo * (0.72 + (double)(index & 1) * 0.22);
            DoraGunFxVisual.taperedRibbon(vertices, matrix, center.m_82549_(direction.m_82490_(inner)), center.m_82549_(direction.m_82490_(outer)), tangent, 0.035, 0.006, packet.theme().highlight, 0.62f * fade);
        }
    }

    private static Basis basis(Vec3 direction) {
        Vec3 forward = direction.m_82556_() < 1.0E-8 ? new Vec3(0.0, 0.0, 1.0) : direction.m_82541_();
        Vec3 reference = Math.abs(forward.f_82480_) > 0.92 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 side = forward.m_82537_(reference).m_82541_();
        return new Basis(forward, side, side.m_82537_(forward).m_82541_());
    }

    private static Vec3 billboardSide(Vec3 start, Vec3 end, Vec3 camera, Vec3 fallback) {
        Vec3 direction = end.m_82546_(start).m_82541_();
        Vec3 side = direction.m_82537_(camera.m_82546_(start.m_82549_(end).m_82490_(0.5)));
        return side.m_82556_() < 1.0E-8 ? fallback : side.m_82541_();
    }

    private static void ring(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Vec3 horizontal, Vec3 vertical, double radius, double width, int color, float alpha, int segments, double spin) {
        double inner = Math.max(0.0, radius - width);
        for (int index = 0; index < segments; ++index) {
            double first = spin + (double)index * Math.PI * 2.0 / (double)segments;
            double second = spin + ((double)index + 1.0) * Math.PI * 2.0 / (double)segments;
            Vec3 firstDirection = horizontal.m_82490_(Math.cos(first)).m_82549_(vertical.m_82490_(Math.sin(first)));
            Vec3 secondDirection = horizontal.m_82490_(Math.cos(second)).m_82549_(vertical.m_82490_(Math.sin(second)));
            DoraGunFxVisual.quad(vertices, matrix, center.m_82549_(firstDirection.m_82490_(inner)), center.m_82549_(firstDirection.m_82490_(radius)), center.m_82549_(secondDirection.m_82490_(radius)), center.m_82549_(secondDirection.m_82490_(inner)), color, alpha);
        }
    }

    private static void cross(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Vec3 horizontal, Vec3 vertical, double radius, double halfWidth, int color, float alpha, double spin) {
        Vec3 first = horizontal.m_82490_(Math.cos(spin)).m_82549_(vertical.m_82490_(Math.sin(spin))).m_82541_();
        Vec3 second = horizontal.m_82490_(-Math.sin(spin)).m_82549_(vertical.m_82490_(Math.cos(spin))).m_82541_();
        DoraGunFxVisual.taperedRibbon(vertices, matrix, center.m_82546_(first.m_82490_(radius)), center.m_82549_(first.m_82490_(radius)), second, halfWidth, halfWidth * 0.35, color, alpha);
        DoraGunFxVisual.taperedRibbon(vertices, matrix, center.m_82546_(second.m_82490_(radius)), center.m_82549_(second.m_82490_(radius)), first, halfWidth, halfWidth * 0.35, color, alpha);
    }

    private static void ribbon(VertexConsumer vertices, Matrix4f matrix, Vec3 start, Vec3 end, Vec3 halfWidth, int color, float alpha) {
        DoraGunFxVisual.quad(vertices, matrix, start.m_82546_(halfWidth), end.m_82546_(halfWidth), end.m_82549_(halfWidth), start.m_82549_(halfWidth), color, alpha);
    }

    private static void taperedRibbon(VertexConsumer vertices, Matrix4f matrix, Vec3 start, Vec3 end, Vec3 axis, double startWidth, double endWidth, int color, float alpha) {
        Vec3 first = axis.m_82490_(startWidth);
        Vec3 second = axis.m_82490_(endWidth);
        DoraGunFxVisual.quad(vertices, matrix, start.m_82546_(first), end.m_82546_(second), end.m_82549_(second), start.m_82549_(first), color, alpha);
    }

    private static void quad(VertexConsumer vertices, Matrix4f matrix, Vec3 first, Vec3 second, Vec3 third, Vec3 fourth, int color, float alpha) {
        DoraGunFxVisual.vertex(vertices, matrix, first, color, alpha);
        DoraGunFxVisual.vertex(vertices, matrix, second, color, alpha);
        DoraGunFxVisual.vertex(vertices, matrix, third, color, alpha);
        DoraGunFxVisual.vertex(vertices, matrix, fourth, color, alpha);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, Vec3 point, int color, float alpha) {
        vertices.m_252986_(matrix, (float)point.f_82479_, (float)point.f_82480_, (float)point.f_82481_).m_6122_(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, Math.max(0, Math.min(255, (int)(alpha * 255.0f)))).m_5752_();
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static float smooth(float value) {
        float bounded = DoraGunFxVisual.clamp(value);
        return bounded * bounded * (3.0f - 2.0f * bounded);
    }

    private static /* synthetic */ boolean lambda$receive$0(WeaponFxPacket packet, Shot effect) {
        return effect.packet.kind() == WeaponFxRules.Kind.DORA_CHARGE && effect.packet.start().m_82557_(packet.start()) < 1.0;
    }

    private record Shot(WeaponFxPacket packet, long started) {
    }

    private static final class DoraGunRenderType
    extends RenderType {
        private static final RenderType TYPE = DoraGunRenderType.m_173215_((String)"ftboss_dora_gun_mesh", (VertexFormat)DefaultVertexFormat.f_85815_, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)8192, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.m_110628_().m_173292_(f_173104_).m_110685_(f_110139_).m_110663_(f_110113_).m_110687_(f_110115_).m_110661_(f_110110_).m_110691_(false));

        private DoraGunRenderType() {
            super("ftboss_dora_gun_mesh", DefaultVertexFormat.f_85815_, VertexFormat.Mode.QUADS, 8192, false, true, () -> {}, () -> {});
        }
    }

    private record Basis(Vec3 forward, Vec3 side, Vec3 up) {
    }
}

