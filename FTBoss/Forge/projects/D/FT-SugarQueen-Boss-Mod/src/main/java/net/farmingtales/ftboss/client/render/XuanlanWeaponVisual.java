package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.farmingtales.ftboss.util.XuanlanRules;
import net.farmingtales.ftboss.util.XuanlanMotionRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT)
public final class XuanlanWeaponVisual {
    private record Effect(XuanlanFxPacket packet, long born, int life) {}
    private static final List<Effect> EFFECTS = new ArrayList<>();
    private static final int GREEN = 0x67FFCC;
    private static final int WHITE = 0xDBEEFF;

    private XuanlanWeaponVisual() {}

    public static void receive(XuanlanFxPacket packet) {
        var level = Minecraft.getInstance().level;
        if (XuanlanMotionRules.action(packet.kind())) {
            XuanlanWeaponMotion.receive(packet);
            return;
        }
        if (level == null || !level.dimension().location().equals(packet.dimension())
                || packet.kind() < XuanlanFxPacket.BEAM
                || packet.kind() > XuanlanFxPacket.HIT && !XuanlanSkyVisual.handles(packet.kind())) return;
        if (packet.kind() == XuanlanFxPacket.SKY_CANCEL) {
            EFFECTS.removeIf(effect -> effect.packet.entityId() == packet.entityId()
                    && XuanlanSkyVisual.handles(effect.packet.kind()));
            return;
        }
        if (packet.kind() == XuanlanFxPacket.MARK || packet.kind() == XuanlanFxPacket.BURST) {
            EFFECTS.removeIf(effect -> effect.packet.kind() == XuanlanFxPacket.MARK
                    && effect.packet.entityId() == packet.entityId());
        }
        if (EFFECTS.size() >= 128) EFFECTS.remove(0);
        int life = XuanlanSkyVisual.handles(packet.kind()) ? XuanlanSkyVisual.lifetime(packet.kind())
                : packet.kind() == XuanlanFxPacket.MARK ? XuanlanRules.MARK_TICKS
                : packet.kind() <= XuanlanFxPacket.EMPOWERED_BEAM ? 8 : 14;
        EFFECTS.add(new Effect(packet, level.getGameTime(), life));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var level = Minecraft.getInstance().level;
        XuanlanWeaponMotion.tick(level);
        if (level == null) EFFECTS.clear();
        else EFFECTS.removeIf(effect -> !level.dimension().location().equals(effect.packet.dimension())
                || level.getGameTime() - effect.born >= effect.life || level.getGameTime() < effect.born);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            EFFECTS.clear();
            if (event.getLevel() == Minecraft.getInstance().level) XuanlanWeaponMotion.useLevel(null);
            XuanlanWeaponRenderer.clearPrepared();
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || minecraft.level == null) return;
        Vec3 camera = event.getCamera().getPosition();
        Vec3 right = new Vec3(event.getCamera().getLeftVector());
        Vec3 up = new Vec3(event.getCamera().getUpVector());
        float partial = event.getPartialTick();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertices = buffers.getBuffer(RenderType.lightning());
        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            for (Effect effect : EFFECTS) {
                XuanlanFxPacket packet = effect.packet;
                float age = minecraft.level.getGameTime() - effect.born + partial;
                float fade = Math.max(0, 1 - age / effect.life);
                if (XuanlanSkyVisual.handles(packet.kind())) {
                    XuanlanSkyVisual.render(pose, vertices, packet, camera, age, effect.life);
                } else if (packet.kind() <= XuanlanFxPacket.EMPOWERED_BEAM) {
                    boolean empowered = packet.kind() == XuanlanFxPacket.EMPOWERED_BEAM;
                    line(pose, vertices, packet.from(), packet.to(), camera, empowered ? 0.11 : 0.065, GREEN, fade * 0.35F);
                    line(pose, vertices, packet.from(), packet.to(), camera, 0.018, WHITE, fade);
                    ring(pose, vertices, packet.to(), right, up, 0.12 + age * 0.025, 0.025, GREEN, fade, age * 0.12, false);
                } else if (packet.kind() == XuanlanFxPacket.MARK) {
                    Entity target = minecraft.level.getEntity(packet.entityId());
                    if (target == null || !target.isAlive()) continue;
                    Vec3 center = target.getPosition(partial).add(0, target.getBbHeight() + 0.3, 0);
                    float opacity = Math.min(1.0F, (effect.life - age) / 12.0F) * 0.8F;
                    ring(pose, vertices, center, right, up, 0.25, 0.024, GREEN, opacity, age * 0.035, true);
                    line(pose, vertices, center.add(up.scale(-0.09)), center.add(up.scale(0.09)), camera, 0.018, WHITE, opacity);
                } else {
                    boolean burst = packet.kind() == XuanlanFxPacket.BURST;
                    double radius = (burst ? 0.2 : 0.12) + age * (burst ? 0.095 : 0.035);
                    ring(pose, vertices, packet.from(), right, up, radius, 0.035 * fade, burst ? GREEN : WHITE, fade, age * 0.16, true);
                    if (burst) ring(pose, vertices, packet.from(), right, up, radius * 0.72, 0.025, WHITE, fade, -age * 0.12, true);
                }
            }
        } finally {
            pose.popPose();
            buffers.endBatch(RenderType.lightning());
        }
    }

    public static void ring(PoseStack pose, VertexConsumer vertices, Vec3 center, Vec3 right, Vec3 up,
                            double radius, double thickness, int color, float alpha, double rotation, boolean broken) {
        for (int segment = 0; segment < 48; segment++) {
            if (broken && segment % 24 >= 18) continue;
            double angle = rotation + segment * Math.PI / 24;
            double next = rotation + (segment + 1) * Math.PI / 24;
            Vec3 direction = right.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
            Vec3 nextDirection = right.scale(Math.cos(next)).add(up.scale(Math.sin(next)));
            quad(pose, vertices, center.add(direction.scale(radius + thickness)), center.add(nextDirection.scale(radius + thickness)),
                    center.add(nextDirection.scale(Math.max(0, radius - thickness))), center.add(direction.scale(Math.max(0, radius - thickness))), color, alpha);
        }
    }

    public static void line(PoseStack pose, VertexConsumer vertices, Vec3 from, Vec3 to, Vec3 camera,
                             double width, int color, float alpha) {
        Vec3 side = to.subtract(from).cross(camera.subtract(from)).normalize();
        if (side.lengthSqr() < 0.01) side = new Vec3(1, 0, 0);
        side = side.scale(width);
        quad(pose, vertices, from.add(side), to.add(side), to.subtract(side), from.subtract(side), color, alpha);
    }

    private static void quad(PoseStack pose, VertexConsumer vertices, Vec3 first, Vec3 second, Vec3 third, Vec3 fourth,
                             int color, float alpha) {
        vertex(pose, vertices, first, color, alpha);
        vertex(pose, vertices, second, color, alpha);
        vertex(pose, vertices, third, color, alpha);
        vertex(pose, vertices, fourth, color, alpha);
    }

    private static void vertex(PoseStack pose, VertexConsumer vertices, Vec3 position, int color, float alpha) {
        vertices.vertex(pose.last().pose(), (float) position.x, (float) position.y, (float) position.z)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, Math.max(0, Math.min(255, (int) (alpha * 255)))).endVertex();
    }
}
