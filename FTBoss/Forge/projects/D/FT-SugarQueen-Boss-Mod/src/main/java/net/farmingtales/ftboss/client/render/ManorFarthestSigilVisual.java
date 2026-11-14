package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ai.ManorFarthestSigilRules;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.network.ManorFarthestSigilNetwork.Burst;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/** 多人法阵的客户端限量生命周期与单批次绘制，退出世界时清空。 */
@Mod.EventBusSubscriber(modid = "ftboss", value = Dist.CLIENT)
public final class ManorFarthestSigilVisual {
    private static final Map<UUID, Burst> ACTIVE = new HashMap<>();
    private static ClientLevel world;

    private ManorFarthestSigilVisual() {}

    public static void accept(Burst burst) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !burst.valid() || !level.dimension().location().equals(burst.dimension())) return;
        if (level != world) { ACTIVE.clear(); world = level; }
        if (burst.phase() == 2) { ACTIVE.remove(burst.owner()); return; }
        if (ACTIVE.size() < 16 || ACTIVE.containsKey(burst.owner())) ACTIVE.put(burst.owner(), burst);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level != world) { ACTIVE.clear(); world = level; return; }
        ACTIVE.values().removeIf(burst -> {
            long age = level.getGameTime() - burst.born();
            Entity owner = level.getEntity(burst.ownerId());
            return age < 0 || age >= (burst.phase() == 1 ? ManorFarthestSigilRules.RELEASE_TICKS : ManorFarthestSigilRules.WINDUP_TICKS + 4)
                    || owner != null && (!owner.getUUID().equals(burst.owner()) || !owner.isAlive());
        });
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { ACTIVE.clear(); world = null; }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == world) { ACTIVE.clear(); world = null; }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        boolean bloom = SpellBloomRenderer.begin(event, !ACTIVE.isEmpty());
        try {
            renderWithoutBloom(event);
        } finally {
            SpellBloomRenderer.end(bloom);
        }
    }

    private static void renderWithoutBloom(RenderLevelStageEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || client.level == null || ACTIVE.isEmpty()) return;
        var camera = event.getCamera().getPosition();
        var poses = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = client.renderBuffers().bufferSource();
        VertexConsumer batch = buffers.getBuffer(ManorCeremonyRenderType.TYPE);
        try {
            for (Burst burst : ACTIVE.values()) {
                var center = burst.center();
                if (center.distanceToSqr(camera) > ManorFarthestSigilRules.VIEW_RANGE * ManorFarthestSigilRules.VIEW_RANGE
                        || event.getFrustum() != null && !event.getFrustum().isVisible(new AABB(
                                center.add(-8.2, 0, -8.2), center.add(8.2, 21, 8.2)))) continue;
                double age = client.level.getGameTime() - burst.born() + (client.isPaused() ? 0 : event.getPartialTick());
                poses.pushPose();
                try {
                    poses.translate(center.x - camera.x, center.y - camera.y + 0.04, center.z - camera.z);
                    Matrix4f matrix = poses.last().pose();
                    double cosine = Math.cos(burst.yaw());
                    double sine = Math.sin(burst.yaw());
                    ManorFarthestSigilMesh.draw(burst.style(), burst.phase() == 1, age, (positionX, positionY, positionZ, color, alpha) ->
                            batch.vertex(matrix, (float) (positionX * cosine - positionZ * sine), (float) positionY,
                                    (float) (positionX * sine + positionZ * cosine))
                                    .color((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, alpha).endVertex());
                } finally {
                    poses.popPose();
                }
            }
        } finally {
            buffers.endBatch(ManorCeremonyRenderType.TYPE);
        }
    }
}
