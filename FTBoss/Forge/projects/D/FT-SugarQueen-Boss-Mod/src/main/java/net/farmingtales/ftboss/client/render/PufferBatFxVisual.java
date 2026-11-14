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

/** 河豚棒专属世界网格：毒云、鼓胀爆发与近身毒刺震波，不创建粒子。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PufferBatFxVisual {
    private static final int MAX_ACTIVE = 64;
    private static final double VIEW_DISTANCE_SQUARED = 72.0 * 72.0;
    private static final int TOXIN = 0x85C94A;
    private static final int ACID = 0xD9FF72;
    private static final int GOLD = 0xF4D45A;
    private static final ArrayDeque<Effect> ACTIVE = new ArrayDeque<>();
    private static ClientLevel activeLevel;

    private record Effect(WeaponFxPacket packet, long started) {}

    private PufferBatFxVisual() {}

    public static void receive(WeaponFxPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !packet.valid() || !packet.kind().pufferBat()
                || !level.dimension().location().equals(packet.dimension())) return;
        if (activeLevel != level) {
            clear();
            activeLevel = level;
        }
        if (packet.kind() == Kind.PUFFER_CLOUD) {
            ACTIVE.removeIf(effect -> effect.packet.kind() == Kind.PUFFER_CLOUD
                    && effect.packet.start().distanceToSqr(packet.start()) < 0.04);
        }
        while (ACTIVE.size() >= MAX_ACTIVE) ACTIVE.removeFirst();
        ACTIVE.addLast(new Effect(packet, level.getGameTime()));
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
        VertexConsumer vertices = buffers.getBuffer(PufferRenderType.TYPE);
        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            Matrix4f matrix = pose.last().pose();
            double worldTime = minecraft.level.getGameTime()
                    + (minecraft.isPaused() ? 0.0 : event.getPartialTick());
            for (Effect effect : ACTIVE) {
                WeaponFxPacket packet = effect.packet;
                AABB bounds = new AABB(packet.start(), packet.end()).inflate(packet.radius() + 1.0);
                if (camera.distanceToSqr(bounds.getCenter()) > VIEW_DISTANCE_SQUARED
                        || event.getFrustum() != null && !event.getFrustum().isVisible(bounds)) continue;
                double age = worldTime - effect.started;
                float progress = clamp((float) (age / packet.kind().ticks));
                switch (packet.kind()) {
                    case PUFFER_CLOUD -> cloud(vertices, matrix, packet.start(), packet.radius(), progress, worldTime);
                    case PUFFER_BURST -> burst(vertices, matrix, packet.start(), packet.radius(), progress, worldTime);
                    case PUFFER_SHOCKWAVE -> shockwave(vertices, matrix, packet.start(), packet.radius(), progress, worldTime);
                    default -> { }
                }
            }
        } finally {
            buffers.endBatch(PufferRenderType.TYPE);
            pose.popPose();
        }
    }

    private static void cloud(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius,
                              float progress, double time) {
        float fade = progress < 0.78F ? 1.0F : 1.0F - smooth((progress - 0.78F) / 0.22F);
        double breath = 1.0 + Math.sin(time * 0.20) * 0.025;
        Vec3 horizontal = new Vec3(1, 0, 0);
        Vec3 depth = new Vec3(0, 0, 1);
        ring(vertices, matrix, center.add(0, 0.055, 0), horizontal, depth, radius * breath,
                0.12, TOXIN, 0.58F * fade, 48, time * 0.025);
        ring(vertices, matrix, center.add(0, 0.065, 0), horizontal, depth, radius * 0.82,
                0.055, ACID, 0.34F * fade, 40, -time * 0.045);
        ring(vertices, matrix, center.add(0, 0.08, 0), horizontal, depth,
                radius * (0.32 + 0.08 * Math.sin(time * 0.13)), 0.08,
                GOLD, 0.24F * fade, 28, time * 0.065);

        for (int layer = 0; layer < 3; layer++) {
            double height = 0.28 + layer * 0.34 + Math.sin(time * 0.15 + layer) * 0.045;
            double layerRadius = radius * (0.72 - layer * 0.14);
            ring(vertices, matrix, center.add(0, height, 0), horizontal, depth, layerRadius,
                    0.07, layer == 1 ? ACID : TOXIN, (0.18F - layer * 0.035F) * fade,
                    32, time * (layer % 2 == 0 ? 0.04 : -0.05) + layer);
        }
        crown(vertices, matrix, center.add(0, 0.07, 0), radius * 0.93,
                0.30 + Math.sin(time * 0.22) * 0.05, TOXIN, 0.38F * fade, 16, time * 0.03);
    }

    private static void burst(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius,
                              float progress, double time) {
        float fade = 1.0F - smooth(progress);
        double expansion = radius * (0.12 + 0.88 * smooth(progress));
        ring(vertices, matrix, center.add(0, 0.07, 0), new Vec3(1, 0, 0), new Vec3(0, 0, 1),
                expansion, Math.max(0.06, radius * 0.055), ACID, 0.9F * fade, 40, time * 0.08);
        double dome = Math.min(expansion, radius * 0.75);
        ring(vertices, matrix, center.add(0, 0.15, 0), new Vec3(1, 0, 0), new Vec3(0, 1, 0),
                dome, Math.max(0.035, radius * 0.025), TOXIN, 0.42F * fade, 28, time * 0.06);
        ring(vertices, matrix, center.add(0, 0.15, 0), new Vec3(0, 0, 1), new Vec3(0, 1, 0),
                dome, Math.max(0.035, radius * 0.025), GOLD, 0.30F * fade, 28, -time * 0.05);
        crown(vertices, matrix, center.add(0, 0.05, 0), expansion * 0.92,
                Math.max(0.25, radius * 0.18), ACID, 0.75F * fade, 16, time * 0.05);
    }

    private static void shockwave(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius,
                                  float progress, double time) {
        float fade = 1.0F - smooth(progress);
        double eased = smooth(progress);
        double outer = radius * (0.10 + 0.90 * eased);
        double inner = Math.max(0.05, outer - 0.22 - 0.12 * (1.0 - progress));
        Vec3 horizontal = new Vec3(1, 0, 0);
        Vec3 depth = new Vec3(0, 0, 1);
        ring(vertices, matrix, center.add(0, 0.08, 0), horizontal, depth, outer,
                outer - inner, ACID, 0.88F * fade, 48, time * 0.04);
        ring(vertices, matrix, center.add(0, 0.11, 0), horizontal, depth, outer * 0.78,
                0.07, GOLD, 0.78F * fade, 40, -time * 0.07);
        wall(vertices, matrix, center, outer, 0.18 + 0.85 * (1.0 - progress),
                TOXIN, 0.32F * fade, 32, time * 0.035);
        crown(vertices, matrix, center.add(0, 0.07, 0), outer * 0.90,
                0.55 + 0.45 * (1.0 - progress), GOLD, 0.84F * fade, 20, time * 0.06);
    }

    private static void crown(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius,
                              double height, int color, float alpha, int spikes, double spin) {
        for (int index = 0; index < spikes; index++) {
            double angle = spin + index * Math.PI * 2.0 / spikes;
            Vec3 outward = new Vec3(Math.cos(angle), 0, Math.sin(angle));
            Vec3 tangent = new Vec3(-outward.z, 0, outward.x);
            Vec3 base = center.add(outward.scale(radius));
            Vec3 tip = center.add(outward.scale(radius + height * 0.28)).add(0, height, 0);
            double width = Math.max(0.025, height * 0.11);
            quad(vertices, matrix, base.subtract(tangent.scale(width)), base.add(tangent.scale(width)),
                    tip.add(tangent.scale(width * 0.12)), tip.subtract(tangent.scale(width * 0.12)), color, alpha);
        }
    }

    private static void wall(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius, double height,
                             int color, float alpha, int segments, double spin) {
        for (int index = 0; index < segments; index++) {
            double first = spin + index * Math.PI * 2.0 / segments;
            double second = spin + (index + 1.0) * Math.PI * 2.0 / segments;
            Vec3 lowFirst = center.add(Math.cos(first) * radius, 0.06, Math.sin(first) * radius);
            Vec3 lowSecond = center.add(Math.cos(second) * radius, 0.06, Math.sin(second) * radius);
            quad(vertices, matrix, lowFirst, lowSecond, lowSecond.add(0, height, 0), lowFirst.add(0, height, 0),
                    color, alpha);
        }
    }

    private static void ring(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Vec3 horizontal, Vec3 vertical,
                             double radius, double width, int color, float alpha, int segments, double spin) {
        double inner = Math.max(0.0, radius - width);
        for (int index = 0; index < segments; index++) {
            double first = spin + index * Math.PI * 2.0 / segments;
            double second = spin + (index + 1.0) * Math.PI * 2.0 / segments;
            Vec3 firstAxis = horizontal.scale(Math.cos(first)).add(vertical.scale(Math.sin(first)));
            Vec3 secondAxis = horizontal.scale(Math.cos(second)).add(vertical.scale(Math.sin(second)));
            quad(vertices, matrix, center.add(firstAxis.scale(inner)), center.add(firstAxis.scale(radius)),
                    center.add(secondAxis.scale(radius)), center.add(secondAxis.scale(inner)), color, alpha);
        }
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

    private static final class PufferRenderType extends RenderType {
        private static final RenderType TYPE = create("ftboss_puffer_bat_mesh", DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS, 8192, false, true, CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false));

        private PufferRenderType() {
            super("ftboss_puffer_bat_mesh", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                    8192, false, true, () -> {}, () -> {});
        }
    }
}
