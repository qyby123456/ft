package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.network.AppleBlastNetwork.Burst;
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

/** 苹果自爆光效：2026-10-12，金白细光环与局部闪光；无烟幕、无碎屑、无全屏闪白。 */
@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AppleBlastLightVisual {
    private static final int GOLD = 0xFFD16A;
    private static final int WHITE = 0xFFF3D3;
    private static final RenderType TYPE = RenderType.lightning();
    private static final List<Flash> ACTIVE = new ArrayList<>();
    private static ClientLevel world;

    private AppleBlastLightVisual() {}

    public static void accept(Burst burst) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !burst.valid() || !level.dimension().location().equals(burst.dimension())) return;
        if (world != level) {
            ACTIVE.clear();
            world = level;
        }
        long now = level.getGameTime();
        ACTIVE.removeIf(flash -> flash.born == now && flash.center.distanceToSqr(burst.x(), burst.y(), burst.z()) < 0.01);
        if (ACTIVE.size() >= AppleBlastLightRules.MAX_ACTIVE) ACTIVE.remove(0);
        ACTIVE.add(new Flash(new Vec3(burst.x(), burst.y(), burst.z()), burst.radius(), now));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || world != level) {
            ACTIVE.clear();
            world = level;
            return;
        }
        long now = level.getGameTime();
        ACTIVE.removeIf(flash -> now < flash.born || now - flash.born >= AppleBlastLightRules.LIFE_TICKS);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == world) {
            ACTIVE.clear();
            world = null;
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        ACTIVE.clear();
        world = null;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()
                || minecraft.level == null || minecraft.level != world) return;
        Vec3 camera = event.getCamera().getPosition();
        Vec3 right = new Vec3(event.getCamera().getLeftVector());
        Vec3 up = new Vec3(event.getCamera().getUpVector());
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertices = buffers.getBuffer(TYPE);
        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            for (Flash flash : ACTIVE) {
                if (camera.distanceToSqr(flash.center) > AppleBlastLightRules.VIEW_DISTANCE * AppleBlastLightRules.VIEW_DISTANCE) continue;
                float age = minecraft.level.getGameTime() - flash.born + event.getPartialTick();
                float fade = AppleBlastLightRules.fade(age);
                if (fade <= 0) continue;
                double radius = AppleBlastLightRules.ringRadius(flash.radius, age);
                Vec3 center = flash.center.add(0, -0.55, 0);
                ring(pose, vertices, center, radius, 0.10, GOLD, fade * 0.12F);
                ring(pose, vertices, center.add(0, 0.004, 0), radius, 0.022, WHITE, fade * 0.65F);
                if (age < AppleBlastLightRules.CORE_TICKS) {
                    float coreFade = (1 - age / AppleBlastLightRules.CORE_TICKS) * 0.28F;
                    double size = 0.55 + 0.15 * age;
                    quad(pose, vertices, flash.center.add(right.scale(-size)), flash.center.add(up.scale(size)),
                            flash.center.add(right.scale(size)), flash.center.add(up.scale(-size)), WHITE, coreFade);
                }
            }
        } finally {
            pose.popPose();
            buffers.endBatch(TYPE);
        }
    }

    private static void ring(PoseStack pose, VertexConsumer vertices, Vec3 center,
            double radius, double width, int color, float alpha) {
        for (int segment = 0; segment < AppleBlastLightRules.SEGMENTS; segment++) {
            double angle = segment * Math.PI * 2 / AppleBlastLightRules.SEGMENTS;
            double next = (segment + 1) * Math.PI * 2 / AppleBlastLightRules.SEGMENTS;
            Vec3 direction = new Vec3(Math.cos(angle), 0, Math.sin(angle));
            Vec3 nextDirection = new Vec3(Math.cos(next), 0, Math.sin(next));
            quad(pose, vertices, center.add(direction.scale(radius + width)), center.add(nextDirection.scale(radius + width)),
                    center.add(nextDirection.scale(radius - width)), center.add(direction.scale(radius - width)), color, alpha);
        }
    }

    private static void quad(PoseStack pose, VertexConsumer vertices, Vec3 first, Vec3 second,
            Vec3 third, Vec3 fourth, int color, float alpha) {
        vertex(pose, vertices, first, color, alpha);
        vertex(pose, vertices, second, color, alpha);
        vertex(pose, vertices, third, color, alpha);
        vertex(pose, vertices, fourth, color, alpha);
    }

    private static void vertex(PoseStack pose, VertexConsumer vertices, Vec3 point, int color, float alpha) {
        vertices.vertex(pose.last().pose(), (float) point.x, (float) point.y, (float) point.z)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255,
                        Math.max(0, Math.min(255, (int) (alpha * 255)))).endVertex();
    }

    private record Flash(Vec3 center, double radius, long born) {}
}
