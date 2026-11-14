package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.ManorAssaultPacket;
import net.farmingtales.ftboss.util.ManorAssaultGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.GAME,
    value = {Dist.CLIENT}
)
public final class ManorAssaultVisual {
    private static final Map<UUID, ManorAssaultVisual.Show> ACTIVE = new HashMap<>();
    private static ClientLevel world;

    private ManorAssaultVisual() {
    }

    public static void accept(ManorAssaultPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.dimension().location().equals(packet.dimension()) && ManorAssaultGeometry.supports(packet.variant())) {
            if (world != level) {
                ACTIVE.clear();
                world = level;
            }

            if (ACTIVE.size() < 16 || ACTIVE.containsKey(packet.owner())) {
                ACTIVE.put(packet.owner(), new ManorAssaultVisual.Show(packet, level.getGameTime()));
            }
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null && level == world) {
                ACTIVE.values().removeIf(show -> {
                    long age = level.getGameTime() - show.started;
                    return age < 0L || age > (show.packet.release() ? 14L : 32L);
                });
            } else {
                clear();
            }
        }
    }

    @SubscribeEvent
    public static void unload(Unload event) {
        if (event.getLevel() == world) {
            clear();
        }
    }

    @SubscribeEvent
    public static void logout(LoggingOut event) {
        clear();
    }

    private static void clear() {
        ACTIVE.clear();
        world = null;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent var0) {
        boolean var1 = SpellBloomRenderer.begin(var0, ACTIVE.isEmpty() ^ true);

        try {
            ftboss$renderWithoutSoftBloom(var0);
        } finally {
            SpellBloomRenderer.end(var1);
        }
    }

    private static void ftboss$renderWithoutSoftBloom(RenderLevelStageEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (event.getStage() == Stage.AFTER_PARTICLES && client.level != null && !ACTIVE.isEmpty()) {
            Vec3 camera = event.getCamera().getPosition();
            BufferSource buffers = client.renderBuffers().bufferSource();
            VertexConsumer batch = buffers.getBuffer(ManorCeremonyRenderType.TYPE);

            for (ManorAssaultVisual.Show show : ACTIVE.values()) {
                Vec3 center = show.packet.center();
                if (!(camera.distanceToSqr(center) > 9216.0)
                    && (event.getFrustum() == null || event.getFrustum().isVisible(new AABB(center.add(-3.4, 0.0, -3.4), center.add(3.4, 2.2, 3.4))))
                    && client.level.getEntity(show.packet.ownerId()) instanceof EasterBossEntity boss
                    && boss.getUUID().equals(show.packet.owner())
                    && boss.isAlive()
                    && boss.getVariant() == show.packet.variant()
                    && !boss.isManorIntroHidden()) {
                    double now = (double)client.level.getGameTime() + (client.isPaused() ? 0.0 : (double)event.getPartialTick().getGameTimeDeltaPartialTick(false));
                    double age = now - (double)show.started;
                    float alpha = show.packet.release() ? 0.98F : 0.78F;
                    boolean reduced = camera.distanceToSqr(center) > 2304.0 || client.options.particles().get() != ParticleStatus.ALL;
                    PoseStack poses = event.getPoseStack();
                    poses.pushPose();

                    try {
                        poses.translate(center.x - camera.x, center.y - camera.y + 0.025, center.z - camera.z);
                        Matrix4f matrix = poses.last().pose();
                        ManorAssaultGeometry.draw(
                            show.packet.variant(),
                            age,
                            show.packet.release(),
                            reduced,
                            alpha,
                            (x, y, z, color, opacity) -> batch.addVertex(matrix, (float)x, (float)y, (float)z)
                                    .setColor((float)(color >> 16 & 0xFF) / 255.0F, (float)(color >> 8 & 0xFF) / 255.0F, (float)(color & 0xFF) / 255.0F, opacity)
                        );
                    } finally {
                        poses.popPose();
                    }
                }
            }

            buffers.endBatch(ManorCeremonyRenderType.TYPE);
        }
    }

    private static record Show(ManorAssaultPacket packet, long started) {
    }
}
