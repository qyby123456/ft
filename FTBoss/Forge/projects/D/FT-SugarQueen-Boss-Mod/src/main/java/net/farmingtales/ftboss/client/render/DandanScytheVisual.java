package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.DandanScytheFxPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class DandanScytheVisual {
    private static final ResourceLocation TEXTURE = new ResourceLocation(FTBossMod.MOD_ID, "textures/fx/dandan_candle_light.png");
    private static final RenderType LIGHT = RenderType.eyes(TEXTURE);
    private static final int MAX_ACTIVE = 96;
    private static final List<Flash> ACTIVE = new ArrayList<>();
    private static ClientLevel activeLevel;

    private record Flash(DandanScytheFxPacket message, long startTick, int lifetime) {}

    private DandanScytheVisual() {}

    public static void receive(DandanScytheFxPacket message) {
        if (net.farmingtales.ftboss.util.DandanScytheMotionRules.action(message.kind())) {
            DandanScytheMotion.receive(message);
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().location().equals(message.dimension())) return;
        if (activeLevel != level) {
            ACTIVE.clear();
            activeLevel = level;
        }
        if (message.kind() == DandanScytheFxPacket.MARK || message.kind() == DandanScytheFxPacket.BURST) {
            ACTIVE.removeIf(flash -> flash.message.kind() == DandanScytheFxPacket.MARK && flash.message.targetId() == message.targetId());
        }
        while (ACTIVE.size() >= MAX_ACTIVE) ACTIVE.remove(0);
        ACTIVE.add(new Flash(message, level.getGameTime(), message.kind() == DandanScytheFxPacket.MARK ? 100 : 14));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ClientLevel level = Minecraft.getInstance().level;
        DandanScytheMotion.tick(level);
        if (level == null || level != activeLevel) {
            ACTIVE.clear();
            activeLevel = level;
            return;
        }
        ACTIVE.removeIf(flash -> level.getGameTime() - flash.startTick >= flash.lifetime
                || flash.message.kind() == DandanScytheFxPacket.MARK
                && (level.getEntity(flash.message.targetId()) == null || !level.getEntity(flash.message.targetId()).isAlive()));
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == Minecraft.getInstance().level) {
            DandanScytheMotion.reset(null);
            DandanScytheRenderer.clearPrepared();
        }
        if (event.getLevel() == activeLevel) {
            ACTIVE.clear();
            activeLevel = null;
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || level != activeLevel) return;
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            VertexConsumer vertices = buffers.getBuffer(LIGHT);
            for (Flash flash : ACTIVE) {
                DandanScytheFxPacket message = flash.message;
                float age = level.getGameTime() - flash.startTick + event.getPartialTick();
                float progress = age / flash.lifetime;
                if (progress < 0 || progress >= 1) continue;
                Vec3 position = message.position();
                if (message.kind() == DandanScytheFxPacket.MARK) {
                    Entity target = level.getEntity(message.targetId());
                    if (target == null) continue;
                    position = target.getPosition(event.getPartialTick()).add(0, target.getBbHeight() + 0.28, 0);
                }
                double radius = message.radius();
                AABB bounds = new AABB(position.x - radius, position.y - 0.2, position.z - radius,
                        position.x + radius, position.y + 2.0, position.z + radius);
                if (event.getFrustum() != null && !event.getFrustum().isVisible(bounds)) continue;
                pose.pushPose();
                try {
                    pose.translate(position.x, position.y, position.z);
                    float alpha = (1.0F - progress) * 0.88F;
                    if (message.kind() == DandanScytheFxPacket.SWEEP) {
                        arc(vertices, pose.last(), radius * (0.82 + progress * 0.18), 0.14, message.yaw() - 70,
                                message.yaw() + 70, 0, alpha, false);
                        arc(vertices, pose.last(), radius * (0.82 + progress * 0.18) - 0.22, 0.06,
                                message.yaw() - 70, message.yaw() + 70, 0.05, alpha * 0.5F, true);
                    } else if (message.kind() == DandanScytheFxPacket.MARK) {
                        float pulse = 0.4F + 0.15F * (float) Math.sin(age * 0.18);
                        arc(vertices, pose.last(), 0.3, 0.065, age * 2, age * 2 + 300, 0, pulse * Math.min(1, (1 - progress) * 8), true);
                    } else {
                        double growth = message.kind() == DandanScytheFxPacket.BLINK ? 0.55 + progress * 0.45 : 0.2 + progress * 0.8;
                        arc(vertices, pose.last(), radius * growth, 0.1, 0, 360, 0, alpha, false);
                        arc(vertices, pose.last(), radius * growth * 0.78, 0.055, 0, 360, 0.09, alpha * 0.65F, true);
                        for (int ray = 0; ray < 6; ray++) {
                            double radians = ray * Math.PI / 3;
                            double horizontal = Math.cos(radians) * radius * growth * 0.5;
                            double depth = Math.sin(radians) * radius * growth * 0.5;
                            vertical(vertices, pose.last(), horizontal, depth, 0.45 * (1 - progress), alpha * 0.5F);
                        }
                    }
                } finally {
                    pose.popPose();
                }
            }
            buffers.endBatch(LIGHT);
        } finally {
            pose.popPose();
        }
    }

    private static void arc(VertexConsumer vertices, PoseStack.Pose pose, double radius, double width,
                            double start, double end, double height, float alpha, boolean violet) {
        int segments = (int) Math.ceil((end - start) / 5);
        for (int segment = 0; segment < segments; segment++) {
            double first = Math.toRadians(start + (end - start) * segment / segments);
            double second = Math.toRadians(start + (end - start) * (segment + 1) / segments);
            point(vertices, pose, -Math.sin(first) * radius, height, Math.cos(first) * radius, 0, 0, alpha, violet);
            point(vertices, pose, -Math.sin(first) * (radius - width), height, Math.cos(first) * (radius - width), 0, 1, alpha, violet);
            point(vertices, pose, -Math.sin(second) * (radius - width), height, Math.cos(second) * (radius - width), 1, 1, alpha, violet);
            point(vertices, pose, -Math.sin(second) * radius, height, Math.cos(second) * radius, 1, 0, alpha, violet);
        }
    }

    private static void vertical(VertexConsumer vertices, PoseStack.Pose pose, double horizontal, double depth, double height, float alpha) {
        point(vertices, pose, horizontal - 0.025, 0, depth, 0, 0, alpha, false);
        point(vertices, pose, horizontal + 0.025, 0, depth, 1, 0, alpha, false);
        point(vertices, pose, horizontal + 0.025, height, depth, 1, 1, alpha, false);
        point(vertices, pose, horizontal - 0.025, height, depth, 0, 1, alpha, false);
    }

    private static void point(VertexConsumer vertices, PoseStack.Pose pose, double horizontal, double vertical, double depth,
                              float texU, float texV, float alpha, boolean violet) {
        vertices.vertex(pose.pose(), (float) horizontal, (float) vertical, (float) depth)
                .color((violet ? 0.68F : 1.0F) * alpha, (violet ? 0.5F : 0.9F) * alpha, (violet ? 1.0F : 0.72F) * alpha, alpha)
                .uv(texU, texV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.normal(), 0, 1, 0).endVertex();
    }
}
