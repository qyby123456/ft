package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.LemonCatFxPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LemonCatSkillVisual {
    private static final int LIFE = 22;
    private static final int MAX_ACTIVE = 48;
    private static final float[] GOLD = {1.0F, 0.72F, 0.15F};
    private static final float[] MINT = {0.35F, 1.0F, 0.68F};
    private static final List<Flash> ACTIVE = new ArrayList<>();
    private static ClientLevel activeLevel;

    private record Flash(LemonCatFxPacket message, long born) {}

    private static final class GlowType {
        private static final RenderType VALUE = RenderType.eyes(new ResourceLocation(FTBossMod.MOD_ID, "textures/fx/lemoncat_white.png"));
    }

    public static void receive(LemonCatFxPacket message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !message.dimension().equals(level.dimension().location()) || message.kind() < 0 || message.kind() > 3) return;
        Vec3 position = message.position();
        if (!Double.isFinite(position.x) || !Double.isFinite(position.y) || !Double.isFinite(position.z) || !Float.isFinite(message.yaw())) return;
        if (activeLevel != level) {
            ACTIVE.clear();
            activeLevel = level;
        }
        if (ACTIVE.size() >= MAX_ACTIVE) ACTIVE.remove(0);
        ACTIVE.add(new Flash(message, level.getGameTime()));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level != activeLevel) {
            ACTIVE.clear();
            activeLevel = level;
            return;
        }
        ACTIVE.removeIf(flash -> level.getGameTime() - flash.born >= LIFE);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            ACTIVE.clear();
            activeLevel = null;
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level != activeLevel) return;
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        var buffers = minecraft.renderBuffers().bufferSource();
        RenderType glow = GlowType.VALUE;
        VertexConsumer vertices = buffers.getBuffer(glow);
        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            for (Flash flash : ACTIVE) {
                Vec3 center = flash.message.position();
                if (camera.distanceToSqr(center) > 64.0D * 64.0D) continue;
                float age = minecraft.level.getGameTime() - flash.born + event.getPartialTick();
                float progress = Mth.clamp(age / LIFE, 0.0F, 1.0F);
                float alpha = (1.0F - progress) * 0.85F;
                boolean heal = flash.message.kind() % 2 == 1;
                boolean paired = flash.message.kind() >= 2;
                pose.pushPose();
                try {
                    pose.translate(center.x, center.y + 0.12D, center.z);
                    float heading = (float) Math.toRadians(flash.message.yaw());
                    if (heal) {
                        ring(vertices, pose.last(), 0, 0, 0, 0.6F + progress * 1.35F, 0.10F, 0, Mth.TWO_PI, MINT, alpha);
                        if (paired) ring(vertices, pose.last(), 0, 0.10F, 0, 0.85F + progress * 1.4F, 0.07F, 0, Mth.TWO_PI, GOLD, alpha);
                        ring(vertices, pose.last(), 0, 0.01F, 0.05F, 0.28F, 0.28F, 0, Mth.TWO_PI, MINT, alpha * 0.5F);
                        for (int toe = 0; toe < 4; toe++) {
                            float angle = -1.0F + toe * 0.67F;
                            ring(vertices, pose.last(), Mth.sin(angle) * 0.46F, 0.01F, -Mth.cos(angle) * 0.46F,
                                    0.105F, 0.105F, 0, Mth.TWO_PI, GOLD, alpha * 0.65F);
                        }
                    } else {
                        float radius = (paired ? 5.5F : 4.5F) * Math.min(1.0F, 0.15F + progress * 1.8F);
                        ring(vertices, pose.last(), 0, 0.55F + progress * 0.55F, 0, radius, 0.16F,
                                heading - 0.96F, heading + 0.96F, GOLD, alpha);
                        if (paired) ring(vertices, pose.last(), 0, 0.40F + progress * 0.4F, 0, radius * 0.88F, 0.09F,
                                heading - 0.96F, heading + 0.96F, MINT, alpha);
                    }
                } finally { pose.popPose(); }
            }
        } finally {
            pose.popPose();
            buffers.endBatch(glow);
        }
    }

    private static void ring(VertexConsumer vertices, PoseStack.Pose pose, float centerX, float height, float centerZ,
                             float radius, float width, float from, float to, float[] color, float alpha) {
        int segments = 40;
        for (int segment = 0; segment < segments; segment++) {
            float first = Mth.lerp(segment / (float) segments, from, to);
            float second = Mth.lerp((segment + 1.0F) / segments, from, to);
            vertex(vertices, pose, centerX - Mth.sin(first) * radius, height, centerZ + Mth.cos(first) * radius, color, alpha);
            vertex(vertices, pose, centerX - Mth.sin(first) * (radius - width), height, centerZ + Mth.cos(first) * (radius - width), color, alpha);
            vertex(vertices, pose, centerX - Mth.sin(second) * (radius - width), height, centerZ + Mth.cos(second) * (radius - width), color, alpha);
            vertex(vertices, pose, centerX - Mth.sin(second) * radius, height, centerZ + Mth.cos(second) * radius, color, alpha);
        }
    }

    private static void vertex(VertexConsumer vertices, PoseStack.Pose pose, float horizontal, float height, float depth,
                               float[] color, float alpha) {
        vertices.vertex(pose.pose(), horizontal, height, depth).color(color[0] * alpha, color[1] * alpha, color[2] * alpha, alpha)
                .uv(0.5F, 0.5F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.normal(), 0, 1, 0).endVertex();
    }
}
