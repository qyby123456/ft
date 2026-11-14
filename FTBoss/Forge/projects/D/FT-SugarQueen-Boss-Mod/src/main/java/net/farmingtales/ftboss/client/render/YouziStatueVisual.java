package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.skill.YouziStatuePacket;
import net.farmingtales.ftboss.util.YouziStatueRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class YouziStatueVisual {
    private static final int PINK = 0xFFAFD7;
    private static final int WHITE = 0xFFF4FF;
    private static final Map<UUID, Aura> AURAS = new HashMap<>();
    private static final List<Trail> TRAILS = new ArrayList<>();
    private static final MultiBufferSource.BufferSource BUFFERS = MultiBufferSource.immediate(new BufferBuilder(8192));
    private static ItemStack miniature = ItemStack.EMPTY;

    private record Aura(int entityId, int kind, long start, int duration) {}
    private record Trail(int kind, Vec3 start, Vec3 end, long born, int duration) {}

    private YouziStatueVisual() {}

    public static void receive(YouziStatuePacket packet) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || packet.kind() < YouziStatuePacket.ECHO || packet.kind() > YouziStatuePacket.BLOCK
                || packet.duration() < 0 || packet.duration() > YouziStatueRules.WARD_DURATION
                || !finite(packet.origin()) || !finite(packet.target())) return;
        long now = client.level.getGameTime();
        if (packet.kind() == YouziStatuePacket.BEAM || packet.kind() == YouziStatuePacket.BLOCK) {
            if (packet.origin().distanceToSqr(packet.target()) > 20.0 * 20.0 || packet.duration() == 0) return;
            if (TRAILS.size() >= 64) TRAILS.remove(0);
            TRAILS.add(new Trail(packet.kind(), packet.origin(), packet.target(), now, packet.duration()));
            return;
        }
        if (!(client.level.getEntity(packet.actorId()) instanceof Player player)) return;
        if (packet.kind() == YouziStatuePacket.STOP) {
            AURAS.remove(player.getUUID());
        } else if (AURAS.size() < 32 || AURAS.containsKey(player.getUUID())) {
            AURAS.put(player.getUUID(), new Aura(player.getId(), packet.kind(), now, packet.duration()));
        }
    }

    private static boolean finite(Vec3 vector) {
        return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
    }

    private static boolean holding(Player player) {
        return player.getMainHandItem().is(FTBossItems.STATUE_YOUZI.get())
                || player.getOffhandItem().is(FTBossItems.STATUE_YOUZI.get());
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || client.isPaused()) return;
        if (client.level == null) {
            AURAS.clear();
            TRAILS.clear();
            return;
        }
        long now = client.level.getGameTime();
        AURAS.entrySet().removeIf(entry -> {
            Aura aura = entry.getValue();
            return now < aura.start || now - aura.start >= aura.duration
                    || !(client.level.getEntity(aura.entityId) instanceof Player player)
                    || !player.getUUID().equals(entry.getKey()) || !player.isAlive() || !holding(player);
        });
        TRAILS.removeIf(trail -> now < trail.born || now - trail.born >= trail.duration);
        if (client.getCameraEntity() == null || now % 5 != 0) return;
        int budget = 16;
        for (Player player : client.level.players()) {
            if (!player.isAlive() || player.isSpectator() || !holding(player)
                    || player.distanceToSqr(client.getCameraEntity()) > 32.0 * 32.0) continue;
            boolean main = player.getMainHandItem().is(FTBossItems.STATUE_YOUZI.get());
            double side = (player.getMainArm() == HumanoidArm.RIGHT) == main ? 0.3 : -0.3;
            double yaw = Math.toRadians(player.yBodyRot);
            Vec3 wrist = player.position().add(-Math.cos(yaw) * side - Math.sin(yaw) * 0.2,
                    player.isCrouching() ? 0.75 : 1.0, -Math.sin(yaw) * side + Math.cos(yaw) * 0.2);
            client.level.addParticle(ParticleTypes.END_ROD, wrist.x, wrist.y, wrist.z, 0.0, 0.012, 0.0);
            if (--budget <= 0) break;
        }
    }

    @SubscribeEvent
    public static void onUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide()) return;
        AURAS.clear();
        TRAILS.clear();
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || (AURAS.isEmpty() && TRAILS.isEmpty())) return;
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        Vec3 camera = event.getCamera().getPosition();
        float partial = event.getPartialTick();
        double now = client.level.getGameTime() + partial;
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        try {
            for (Aura aura : AURAS.values()) {
                if (!(client.level.getEntity(aura.entityId) instanceof Player player)) continue;
                Vec3 origin = player.getPosition(partial);
                if (origin.distanceToSqr(camera) > 48.0 * 48.0 || !event.getFrustum().isVisible(player.getBoundingBox().inflate(1.5))) continue;
                float age = (float) (now - aura.start);
                float fade = Mth.clamp(age / 5.0F, 0.0F, 1.0F) * Mth.clamp((aura.duration - age) / 10.0F, 0.0F, 1.0F);
                if (fade <= 0.0F) continue;
                boolean ward = aura.kind == YouziStatuePacket.WARD;
                VertexConsumer vertices = BUFFERS.getBuffer(GlowRenderType.TYPE);
                Matrix4f matrix = pose.last().pose();
                ring(vertices, matrix, origin.add(0, 0.07, 0), ward ? 1.1 : 0.75, 0.035, PINK, fade * 0.65F);
                if (ward) ring(vertices, matrix, origin.add(0, 0.85 + Math.sin(age * 0.12) * 0.08, 0), 0.92, 0.025, WHITE, fade * 0.7F);
                int remaining = ward ? 3 : Math.max(0, 3 - YouziStatueRules.dueEchoes((long) age));
                for (int index = 0; index < remaining; index++) {
                    double angle = age * 0.055 + index * Math.PI * 2.0 / 3.0;
                    Vec3 echo = origin.add(Math.cos(angle) * 0.82, 1.05 + Math.sin(angle * 2.0) * 0.1, Math.sin(angle) * 0.82);
                    ring(vertices, matrix, echo.add(0, -0.19, 0), 0.23, 0.045, PINK, fade);
                    star(vertices, matrix, echo.add(0, 0.27, 0), camera, 0.12, WHITE, fade);
                }
                BUFFERS.endBatch(GlowRenderType.TYPE);
                for (int index = 0; index < remaining; index++) {
                    double angle = age * 0.055 + index * Math.PI * 2.0 / 3.0;
                    Vec3 echo = origin.add(Math.cos(angle) * 0.82, 1.05 + Math.sin(angle * 2.0) * 0.1, Math.sin(angle) * 0.82);
                    renderMiniature(client, pose, echo, age, index, fade);
                }
            }
            VertexConsumer vertices = BUFFERS.getBuffer(GlowRenderType.TYPE);
            Matrix4f matrix = pose.last().pose();
            for (Trail trail : TRAILS) {
                float progress = Mth.clamp((float) (now - trail.born) / trail.duration, 0.0F, 1.0F);
                float fade = 1.0F - progress;
                if (fade <= 0.0F || !event.getFrustum().isVisible(new AABB(trail.start, trail.end).inflate(2))) continue;
                if (trail.kind == YouziStatuePacket.BLOCK) {
                    ring(vertices, matrix, trail.start.add(0, 0.9, 0), 0.8 + progress * 0.65, 0.08 * fade, WHITE, fade);
                } else {
                    beam(vertices, matrix, trail.start, trail.end, camera, 0.12 * fade, PINK, fade * 0.5F);
                    beam(vertices, matrix, trail.start, trail.end, camera, 0.027 * fade, WHITE, fade);
                    star(vertices, matrix, trail.end, camera, 0.18 + progress * 0.3, PINK, fade);
                    star(vertices, matrix, trail.end, camera, 0.08 + progress * 0.12, WHITE, fade);
                }
            }
        } finally {
            BUFFERS.endBatch();
            pose.popPose();
        }
    }

    private static void renderMiniature(Minecraft client, PoseStack pose, Vec3 position, float age, int index, float fade) {
        if (miniature.isEmpty()) miniature = new ItemStack(FTBossItems.STATUE_YOUZI.get());
        pose.pushPose();
        pose.translate(position.x, position.y, position.z);
        pose.mulPose(Axis.YP.rotationDegrees(age * 2.0F + index * 120.0F));
        float scale = 0.36F * fade;
        pose.scale(scale, scale, scale);
        client.getItemRenderer().renderStatic(miniature, ItemDisplayContext.NONE, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, pose, BUFFERS, client.level, index);
        pose.popPose();
    }

    private static void beam(VertexConsumer vertices, Matrix4f matrix, Vec3 start, Vec3 end, Vec3 camera,
                             double width, int color, float opacity) {
        Vec3 direction = end.subtract(start).normalize();
        Vec3 side = direction.cross(camera.subtract(start)).normalize();
        if (side.lengthSqr() < 0.01) side = direction.cross(new Vec3(0, 1, 0)).normalize();
        if (side.lengthSqr() < 0.01) side = new Vec3(1, 0, 0);
        side = side.scale(width);
        quad(vertices, matrix, start.subtract(side), end.subtract(side), end.add(side), start.add(side), color, opacity);
    }

    private static void star(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Vec3 camera, double size, int color, float opacity) {
        Vec3 forward = camera.subtract(center).normalize();
        Vec3 right = forward.cross(new Vec3(0, 1, 0)).normalize();
        if (right.lengthSqr() < 0.01) right = new Vec3(1, 0, 0);
        Vec3 upward = right.cross(forward).normalize();
        quad(vertices, matrix, center.add(upward.scale(size)), center.add(right.scale(size * 0.28)),
                center.subtract(upward.scale(size)), center.subtract(right.scale(size * 0.28)), color, opacity);
        quad(vertices, matrix, center.add(right.scale(size)), center.add(upward.scale(size * 0.28)),
                center.subtract(right.scale(size)), center.subtract(upward.scale(size * 0.28)), color, opacity);
    }

    private static void ring(VertexConsumer vertices, Matrix4f matrix, Vec3 center, double radius, double width, int color, float opacity) {
        for (int index = 0; index < 48; index++) {
            double start = index * Math.PI / 24.0;
            double end = (index + 1) * Math.PI / 24.0;
            quad(vertices, matrix, center.add(Math.cos(start) * radius, 0, Math.sin(start) * radius),
                    center.add(Math.cos(end) * radius, 0, Math.sin(end) * radius),
                    center.add(Math.cos(end) * (radius - width), 0, Math.sin(end) * (radius - width)),
                    center.add(Math.cos(start) * (radius - width), 0, Math.sin(start) * (radius - width)), color, opacity);
        }
    }

    private static void quad(VertexConsumer vertices, Matrix4f matrix, Vec3 first, Vec3 second, Vec3 third, Vec3 fourth, int color, float opacity) {
        for (Vec3 point : new Vec3[]{first, second, third, fourth}) {
            vertices.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                    .color(color >> 16 & 255, color >> 8 & 255, color & 255, (int) (Mth.clamp(opacity, 0.0F, 1.0F) * 255)).endVertex();
        }
    }

    private static final class GlowRenderType extends RenderType {
        private static final RenderType TYPE = create("ftboss_youzi_glow", DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS, 4096, false, true, CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER).setTransparencyState(LIGHTNING_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL).createCompositeState(false));

        private GlowRenderType() {
            super("ftboss_youzi_glow", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                    4096, false, true, () -> {}, () -> {});
        }
    }
}
