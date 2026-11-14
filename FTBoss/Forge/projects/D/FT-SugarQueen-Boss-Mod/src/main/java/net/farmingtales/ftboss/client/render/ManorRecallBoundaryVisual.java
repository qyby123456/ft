package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.ManorRecallBoundaryPacket;
import net.farmingtales.ftboss.util.ManorRecallBoundaryGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

/** A moving, depth-tested red/gold fence at the exact Manor recall threshold. */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT)
public final class ManorRecallBoundaryVisual {
    private record Boundary(ManorRecallBoundaryPacket packet, long refreshed) {}
    private static final Map<UUID, Boundary> ACTIVE = new LinkedHashMap<>();
    private static final DustParticleOptions RED_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.12F, 0.28F), 0.85F);
    private static final DustParticleOptions GOLD_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.72F, 0.24F), 0.72F);
    private static final int TIMEOUT_TICKS = 45;

    private ManorRecallBoundaryVisual() {}

    public static void receive(ManorRecallBoundaryPacket packet) {
        if (!packet.valid()) return;
        if (!packet.active()) { ACTIVE.remove(packet.fightId()); return; }
        var level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().location().equals(packet.dimension())) return;
        if (!ACTIVE.containsKey(packet.fightId()) && ACTIVE.size() >= 4) ACTIVE.remove(ACTIVE.keySet().iterator().next());
        ACTIVE.put(packet.fightId(), new Boundary(packet, level.getGameTime()));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) { ACTIVE.clear(); return; }
        long now = client.level.getGameTime();
        ACTIVE.values().removeIf(boundary -> !boundary.packet.dimension().equals(client.level.dimension().location())
                || now < boundary.refreshed || now - boundary.refreshed > TIMEOUT_TICKS);
        if (client.isPaused() || now % 4 != 0) return;
        ParticleStatus status = client.options.particles().get();
        int particles = status == ParticleStatus.MINIMAL ? 2 : status == ParticleStatus.DECREASED ? 4 : 6;
        for (Boundary boundary : ACTIVE.values()) {
            Entity boss = boss(client, boundary.packet);
            if (boss == null) continue;
            Vec3 center = boss.position();
            Vec3 toward = client.player.position().subtract(center).multiply(1, 0, 1);
            double base = toward.lengthSqr() < 1.0E-6 ? now * 0.08 : Math.atan2(toward.z, toward.x);
            for (int index = 0; index < particles; index++) {
                double offset = (index - (particles - 1) * 0.5) * 0.105;
                double angle = base + offset + Math.sin(now * 0.07 + index) * 0.018;
                double radius = boundary.packet.radius();
                double height = center.y + 0.16 + (index % 3) * 0.34;
                client.level.addParticle(index % 3 == 0 ? GOLD_DUST : RED_DUST,
                        center.x + Math.cos(angle) * radius, height, center.z + Math.sin(angle) * radius,
                        0, 0.008 + (index % 2) * 0.006, 0);
            }
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || client.level == null
                || client.player == null || ACTIVE.isEmpty()) return;
        Vec3 camera = event.getCamera().getPosition();
        var buffers = client.renderBuffers().bufferSource();
        VertexConsumer vertices = null;
        float partial = client.isPaused() ? 0 : event.getPartialTick();
        boolean reduced = client.options.particles().get() != ParticleStatus.ALL;
        for (Boundary boundary : ACTIVE.values()) {
            Entity boss = boss(client, boundary.packet);
            if (boss == null) continue;
            Vec3 center = boss.getPosition(partial);
            double radius = boundary.packet.radius();
            AABB bounds = new AABB(center.x - radius - 1, center.y - 1, center.z - radius - 1,
                    center.x + radius + 1, center.y + 3, center.z + radius + 1);
            if (camera.distanceToSqr(center) > 112 * 112
                    || event.getFrustum() != null && !event.getFrustum().isVisible(bounds)) continue;
            float urgency = Math.max(boundary.packet.disengageWarning(),
                    ManorRecallBoundaryGeometry.urgency(client.player.distanceTo(boss), radius));
            if (vertices == null) vertices = buffers.getBuffer(ManorRecallBoundaryRenderType.TYPE);
            VertexConsumer batch = vertices;
            var poses = event.getPoseStack();
            poses.pushPose();
            try {
                poses.translate(center.x - camera.x, center.y - camera.y, center.z - camera.z);
                var matrix = poses.last().pose();
                double age = client.level.getGameTime() + partial;
                ManorRecallBoundaryGeometry.draw(age, radius, reduced, urgency,
                        (first, second, third, fourth, color, alpha) -> {
                            vertex(batch, matrix, first, color, alpha);
                            vertex(batch, matrix, second, color, alpha);
                            vertex(batch, matrix, third, color, alpha);
                            vertex(batch, matrix, fourth, color, alpha);
                        });
            } finally { poses.popPose(); }
        }
        if (vertices != null) buffers.endBatch(ManorRecallBoundaryRenderType.TYPE);
    }

    private static Entity boss(Minecraft client, ManorRecallBoundaryPacket packet) {
        if (client.level == null || !packet.active()) return null;
        Entity entity = client.level.getEntity(packet.bossEntityId());
        return entity != null && entity.isAlive() && packet.bossId().equals(entity.getUUID()) ? entity : null;
    }

    private static void vertex(VertexConsumer vertices, org.joml.Matrix4f matrix,
                               ManorRecallBoundaryGeometry.Point point, int color, float alpha) {
        vertices.vertex(matrix, (float) point.x(), (float) point.y(), (float) point.z())
                .color(color >> 16 & 255, color >> 8 & 255, color & 255,
                        Math.max(0, Math.min(255, Math.round(alpha * 255)))).endVertex();
    }

    @SubscribeEvent public static void unload(LevelEvent.Unload event) { if (event.getLevel().isClientSide()) ACTIVE.clear(); }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { ACTIVE.clear(); }
}
