package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayDeque;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.WeaponFxPacket;
import net.farmingtales.ftboss.util.WeaponFxRules.Kind;
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

/**
 * 朵拉枪械的纯网格特效。所有枪口、弹道、冲击波都直接写世界顶点；这里绝不创建粒子。
 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class DoraGunFxVisual {
    private static final int MAX_ACTIVE = 96;
    private static final double VIEW_DISTANCE_SQUARED = 72.0 * 72.0;
    private static final ArrayDeque<Shot> ACTIVE = new ArrayDeque<>();
    private static ClientLevel activeLevel;

    private record Shot(WeaponFxPacket packet, long started, long chargeStarted) {}

    private record Basis(Vec3 forward, Vec3 side, Vec3 up) {}

    private DoraGunFxVisual() {}

    public static void receive(WeaponFxPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !packet.valid() || !packet.kind().doraGun()
                || !level.dimension().location().equals(packet.dimension())) return;
        if (activeLevel != level) {
            clear();
            activeLevel = level;
        }
        long now = level.getGameTime();
        long chargeStarted = now;
        if (packet.kind() == Kind.DORA_CHARGE) {
            for (Shot effect : ACTIVE) {
                if (effect.packet.kind() == Kind.DORA_CHARGE
                        && effect.packet.start().distanceToSqr(packet.start()) < 1.0
                        && now >= effect.started && now - effect.started < Kind.DORA_CHARGE.ticks) {
                    chargeStarted = effect.chargeStarted;
                    break;
                }
            }
            ACTIVE.removeIf(effect -> effect.packet.kind() == Kind.DORA_CHARGE
                    && effect.packet.start().distanceToSqr(packet.start()) < 1.0);
        }
        while (ACTIVE.size() >= MAX_ACTIVE) ACTIVE.removeFirst();
        ACTIVE.addLast(new Shot(packet, now, chargeStarted));
    }

    private static void clear() {
        ACTIVE.clear();
        activeLevel = null;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level != activeLevel) {
            clear();
            return;
        }
        if (!minecraft.isPaused()) {
            long now = minecraft.level.getGameTime();
            ACTIVE.removeIf(effect -> now < effect.started
                    || now - effect.started >= effect.packet.kind().ticks);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == activeLevel) clear();
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level != activeLevel) return;
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertices = buffers.getBuffer(DoraGunRenderType.TYPE);
        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            Matrix4f matrix = pose.last().pose();
            long now = minecraft.level.getGameTime();
            for (Shot effect : ACTIVE) {
                WeaponFxPacket packet = effect.packet;
                AABB bounds = new AABB(packet.start(), packet.end()).inflate(packet.radius() + 0.75);
                if (camera.distanceToSqr(bounds.getCenter()) > VIEW_DISTANCE_SQUARED
                        || event.getFrustum() != null && !event.getFrustum().isVisible(bounds)) continue;
                float age = now - effect.started + (minecraft.isPaused() ? 0.0F : event.getPartialTick());
                float progress = clamp(age / packet.kind().ticks);
                switch (packet.kind()) {
                    case DORA_CHARGE -> renderCharge(vertices, matrix, packet, progress,
                            now - effect.chargeStarted + (minecraft.isPaused() ? 0.0F : event.getPartialTick()));
                    case DORA_PULSE, DORA_NOVA -> renderPulse(vertices, matrix, packet, progress, age, camera);
                    case DORA_TRACER -> renderTracer(vertices, matrix, packet, progress, age, camera);
                    case DORA_IMPACT -> renderImpact(vertices, matrix, packet, progress, age, camera);
                    default -> { continue; }
                }
            }
        } finally {
            buffers.endBatch(DoraGunRenderType.TYPE);
            pose.popPose();
        }
    }

    private static void renderCharge(VertexConsumer vertices, Matrix4f matrix, WeaponFxPacket packet,
                                     float progress, float age) {
        Basis basis = basis(packet.end().subtract(packet.start()));
        Vec3 center = packet.start().lerp(packet.end(), 0.55);
        float fade = 1.0F - smooth(progress);
        float power = smooth(age / 24.0F);
        double spin = age * 0.18;
        double radius = 0.26 + power * 0.13;
        for (int index = 0; index < 3; index++) {
            double angle = spin + index * Math.PI * 2.0 / 3.0;
            arc(vertices, matrix, center, basis.side, basis.up, radius, 0.035,
                    packet.theme().color, 0.72F * fade, 12, angle, Math.PI * 0.46);
            arc(vertices, matrix, center.add(basis.forward.scale(0.06)), basis.side, basis.up,
                    radius * 0.66, 0.018, packet.theme().highlight, fade, 10, -angle, Math.PI * 0.36);
            Vec3 outside = center.add(basis.side.scale(Math.cos(angle) * radius))
                    .add(basis.up.scale(Math.sin(angle) * radius)).add(basis.forward.scale(0.34));
            taperedRibbon(vertices, matrix, outside, center, basis.up, 0.012, 0.003,
                    packet.theme().highlight, 0.8F * fade);
        }
        ring(vertices, matrix, center, basis.side, basis.up, 0.06 + power * 0.025, 0.025,
                0xFFFFFF, fade, 16, spin);
        cross(vertices, matrix, center, basis.side, basis.up, 0.10 + power * 0.08,
                0.008 + power * 0.006, packet.theme().highlight, fade, spin * 0.5);
    }

    private static void renderPulse(VertexConsumer vertices, Matrix4f matrix, WeaponFxPacket packet,
                                    float progress, float age, Vec3 camera) {
        Vec3 delta = packet.end().subtract(packet.start());
        double length = delta.length();
        if (length < 0.05) return;
        Basis basis = basis(delta);
        Vec3 start = packet.start().add(basis.forward.scale(Math.min(0.62, length * 0.28)));
        Vec3 end = packet.end();
        boolean nova = packet.kind() == Kind.DORA_NOVA;
        float fade = (float) Math.pow(1.0 - progress, 1.6);
        double width = (nova ? 0.19 : 0.11) * (1.0 - 0.65 * smooth(progress));
        Vec3 facing = billboardSide(start, end, camera, basis.side);
        Vec3 facingUp = basis.forward.cross(facing).normalize();

        taperedRibbon(vertices, matrix, start, end, facing, width * 2.2, width * 0.5,
                packet.theme().color, 0.18F * fade);
        taperedRibbon(vertices, matrix, start, end, facingUp, width * 1.4, width * 0.28,
                packet.theme().color, 0.26F * fade);
        taperedRibbon(vertices, matrix, start, end, facing, width * 0.6, width * 0.12,
                packet.theme().highlight, 0.85F * fade);
        taperedRibbon(vertices, matrix, start, end, facingUp, width * 0.25, width * 0.08,
                0xFFFFFF, fade);
        helix(vertices, matrix, start, end, basis, width * (nova ? 1.9 : 1.3), width * 0.12,
                age * -0.5, nova ? 4.0 : 2.0, packet.theme().color, 0.8F * fade);
        if (nova) helix(vertices, matrix, start, end, basis, width * 1.9, width * 0.09,
                age * -0.5 + Math.PI, 4.0, packet.theme().highlight, 0.7F * fade);
        muzzle(vertices, matrix, start, basis, nova ? 0.48 : 0.32, progress,
                packet.theme().color, packet.theme().highlight);
        if (!nova) cross(vertices, matrix, end.subtract(basis.forward.scale(0.02)), facing, facingUp,
                0.22 + progress * 0.26, 0.018 * fade, packet.theme().highlight, fade, 0.2);
    }

    private static void renderTracer(VertexConsumer vertices, Matrix4f matrix, WeaponFxPacket packet,
                                     float progress, float age, Vec3 camera) {
        Vec3 delta = packet.end().subtract(packet.start());
        double length = delta.length();
        if (length < 0.03) return;
        Basis basis = basis(delta);
        Vec3 start = packet.start().add(basis.forward.scale(Math.min(0.54, length * 0.32)));
        Vec3 end = packet.end();
        double travel = Math.min(1.0, progress / 0.32);
        double headDistance = start.distanceTo(end) * travel;
        Vec3 head = start.add(basis.forward.scale(headDistance));
        Vec3 tail = start.add(basis.forward.scale(Math.max(0, headDistance - 1.45)));
        Vec3 facing = billboardSide(start, end, camera, basis.side);
        Vec3 facingUp = basis.forward.cross(facing).normalize();
        float fade = 1.0F - smooth(progress);
        taperedRibbon(vertices, matrix, tail, head, facing, 0.012, 0.075,
                packet.theme().color, 0.65F * fade);
        taperedRibbon(vertices, matrix, tail, head, facingUp, 0.004, 0.026,
                packet.theme().highlight, fade);
        ribbon(vertices, matrix, tail, head, facing.scale(0.012), 0xFFFFFF, fade);
        cross(vertices, matrix, head, basis.side, basis.up, 0.055, 0.022,
                packet.theme().highlight, fade, 0.0);
        muzzle(vertices, matrix, start, basis, 0.18, progress * 0.6F,
                packet.theme().color, packet.theme().highlight);
        if (travel >= 1.0) {
            cross(vertices, matrix, end.subtract(basis.forward.scale(0.02)), facing, facingUp,
                    0.13 + progress * 0.18, 0.012, packet.theme().highlight, fade, 0.3);
            ring(vertices, matrix, end.subtract(basis.forward.scale(0.025)), facing, facingUp,
                    0.08 + progress * 0.20, 0.015, packet.theme().color, 0.65F * fade, 12, 0);
        }
    }

    private static void renderImpact(VertexConsumer vertices, Matrix4f matrix, WeaponFxPacket packet,
                                     float progress, float age, Vec3 camera) {
        Vec3 center = packet.start();
        Vec3 view = camera.subtract(center);
        Basis viewBasis = basis(view.lengthSqr() < 1.0E-6 ? new Vec3(0, 0, 1) : view);
        float fade = 1.0F - smooth(progress);
        double expansion = Math.sqrt(progress);
        double radius = Math.max(0.15, packet.radius() * expansion);
        int segments = radius > 2.0 ? 32 : 20;
        ring(vertices, matrix, center, new Vec3(1, 0, 0), new Vec3(0, 0, 1), radius,
                Math.max(0.025, radius * 0.055), packet.theme().color, 0.72F * fade,
                segments, age * 0.11);
        double halo = radius * 0.72;
        ring(vertices, matrix, center, viewBasis.side, viewBasis.up, halo,
                Math.max(0.025, halo * 0.065), packet.theme().highlight, 0.9F * fade,
                segments, -age * 0.22);
        ring(vertices, matrix, center.add(0, 0.015, 0), new Vec3(1, 0, 0), new Vec3(0, 0, 1),
                radius * 0.60, Math.max(0.015, radius * 0.025), packet.theme().highlight,
                fade, segments, -age * 0.08);
        for (int index = 0; index < 4; index++) {
            arc(vertices, matrix, center.add(0, 0.025, 0), new Vec3(1, 0, 0), new Vec3(0, 0, 1),
                    radius * 0.85, Math.max(0.02, radius * 0.09), packet.theme().color, fade,
                    8, index * Math.PI * 0.5 + age * 0.06, Math.PI * 0.28);
        }
        cross(vertices, matrix, center, viewBasis.side, viewBasis.up,
                Math.min(1.1, packet.radius() * 0.55) * (1.0 - progress), 0.04 * fade,
                0xFFFFFF, fade, 0.25);
        int rays = 12;
        for (int index = 0; index < rays; index++) {
            double angle = index * Math.PI * 2.0 / rays;
            Vec3 direction = viewBasis.side.scale(Math.cos(angle)).add(viewBasis.up.scale(Math.sin(angle)));
            Vec3 tangent = viewBasis.forward.cross(direction).normalize();
            double inner = halo * 0.5;
            double outer = radius * (0.75 + (index & 1) * 0.25);
            taperedRibbon(vertices, matrix, center.add(direction.scale(inner)), center.add(direction.scale(outer)),
                    tangent, Math.max(0.012, radius * 0.035), 0.002, packet.theme().highlight, 0.8F * fade);
        }
    }

    private static void muzzle(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Basis basis,
                               double radius, float progress, int color, int highlight) {
        float flash = 1.0F - smooth(Math.min(1.0F, progress * 3.0F));
        ring(vertices, matrix, center, basis.side, basis.up, radius * (0.55 + progress),
                radius * 0.12, color, flash, 20, 0);
        cross(vertices, matrix, center, basis.side, basis.up, radius * (1.0 + progress),
                radius * 0.065, highlight, flash, Math.PI * 0.25);
        taperedRibbon(vertices, matrix, center, center.add(basis.forward.scale(radius * 2.5)), basis.side,
                radius * 0.24, 0.002, 0xFFFFFF, flash);
    }

    private static void helix(VertexConsumer vertices, Matrix4f matrix, Vec3 start, Vec3 end, Basis basis,
                              double radius, double width, double spin, double turns, int color, float alpha) {
        int segments = 48;
        Vec3 previous = start.add(basis.side.scale(Math.cos(spin) * radius)).add(basis.up.scale(Math.sin(spin) * radius));
        for (int index = 1; index <= segments; index++) {
            double along = index / (double) segments;
            double angle = spin + along * Math.PI * 2.0 * turns;
            double taper = 1.0 - along * 0.75;
            Vec3 radial = basis.side.scale(Math.cos(angle)).add(basis.up.scale(Math.sin(angle)));
            Vec3 current = start.lerp(end, along).add(radial.scale(radius * taper));
            ribbon(vertices, matrix, previous, current, radial.scale(width), color, alpha);
            previous = current;
        }
    }

    private static void arc(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Vec3 horizontal, Vec3 vertical,
                            double radius, double width, int color, float alpha, int segments, double spin, double sweep) {
        double inner = Math.max(0.0, radius - width);
        for (int index = 0; index < segments; index++) {
            double first = spin + index * sweep / segments;
            double second = spin + (index + 1.0) * sweep / segments;
            Vec3 firstDirection = horizontal.scale(Math.cos(first)).add(vertical.scale(Math.sin(first)));
            Vec3 secondDirection = horizontal.scale(Math.cos(second)).add(vertical.scale(Math.sin(second)));
            quad(vertices, matrix, center.add(firstDirection.scale(inner)), center.add(firstDirection.scale(radius)),
                    center.add(secondDirection.scale(radius)), center.add(secondDirection.scale(inner)), color, alpha);
        }
    }

    private static Basis basis(Vec3 direction) {
        Vec3 forward = direction.lengthSqr() < 1.0E-8 ? new Vec3(0, 0, 1) : direction.normalize();
        Vec3 reference = Math.abs(forward.y) > 0.92 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 side = forward.cross(reference).normalize();
        return new Basis(forward, side, side.cross(forward).normalize());
    }

    private static Vec3 billboardSide(Vec3 start, Vec3 end, Vec3 camera, Vec3 fallback) {
        Vec3 direction = end.subtract(start).normalize();
        Vec3 side = direction.cross(camera.subtract(start.add(end).scale(0.5)));
        return side.lengthSqr() < 1.0E-8 ? fallback : side.normalize();
    }

    private static void ring(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Vec3 horizontal, Vec3 vertical,
                             double radius, double width, int color, float alpha, int segments, double spin) {
        double inner = Math.max(0.0, radius - width);
        for (int index = 0; index < segments; index++) {
            double first = spin + index * Math.PI * 2.0 / segments;
            double second = spin + (index + 1.0) * Math.PI * 2.0 / segments;
            Vec3 firstDirection = horizontal.scale(Math.cos(first)).add(vertical.scale(Math.sin(first)));
            Vec3 secondDirection = horizontal.scale(Math.cos(second)).add(vertical.scale(Math.sin(second)));
            quad(vertices, matrix, center.add(firstDirection.scale(inner)), center.add(firstDirection.scale(radius)),
                    center.add(secondDirection.scale(radius)), center.add(secondDirection.scale(inner)), color, alpha);
        }
    }

    private static void cross(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Vec3 horizontal, Vec3 vertical,
                              double radius, double halfWidth, int color, float alpha, double spin) {
        Vec3 first = horizontal.scale(Math.cos(spin)).add(vertical.scale(Math.sin(spin))).normalize();
        Vec3 second = horizontal.scale(-Math.sin(spin)).add(vertical.scale(Math.cos(spin))).normalize();
        taperedRibbon(vertices, matrix, center.subtract(first.scale(radius)), center.add(first.scale(radius)),
                second, halfWidth, halfWidth * 0.35, color, alpha);
        taperedRibbon(vertices, matrix, center.subtract(second.scale(radius)), center.add(second.scale(radius)),
                first, halfWidth, halfWidth * 0.35, color, alpha);
    }

    private static void ribbon(VertexConsumer vertices, Matrix4f matrix, Vec3 start, Vec3 end, Vec3 halfWidth,
                               int color, float alpha) {
        quad(vertices, matrix, start.subtract(halfWidth), end.subtract(halfWidth),
                end.add(halfWidth), start.add(halfWidth), color, alpha);
    }

    private static void taperedRibbon(VertexConsumer vertices, Matrix4f matrix, Vec3 start, Vec3 end, Vec3 axis,
                                      double startWidth, double endWidth, int color, float alpha) {
        Vec3 first = axis.scale(startWidth);
        Vec3 second = axis.scale(endWidth);
        quad(vertices, matrix, start.subtract(first), end.subtract(second), end.add(second), start.add(first), color, alpha);
    }

    private static void quad(VertexConsumer vertices, Matrix4f matrix, Vec3 first, Vec3 second, Vec3 third, Vec3 fourth,
                             int color, float alpha) {
        vertex(vertices, matrix, first, color, alpha);
        vertex(vertices, matrix, second, color, alpha);
        vertex(vertices, matrix, third, color, alpha);
        vertex(vertices, matrix, fourth, color, alpha);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, Vec3 point, int color, float alpha) {
        vertices.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(color >> 16 & 255, color >> 8 & 255, color & 255,
                        Math.max(0, Math.min(255, (int) (alpha * 255.0F)))).endVertex();
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static float smooth(float value) {
        float bounded = clamp(value);
        return bounded * bounded * (3.0F - 2.0F * bounded);
    }

    private static final class DoraGunRenderType extends RenderType {
        private static final RenderType TYPE = create("ftboss_dora_gun_mesh", DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS, 8192, false, true, CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER)
                        .setTransparencyState(LIGHTNING_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false));

        private DoraGunRenderType() {
            super("ftboss_dora_gun_mesh", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                    8192, false, true, () -> {}, () -> {});
        }
    }
}
