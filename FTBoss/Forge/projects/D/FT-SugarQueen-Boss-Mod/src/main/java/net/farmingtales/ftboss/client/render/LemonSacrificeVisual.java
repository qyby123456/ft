package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.LemonSacrificePacket;
import net.farmingtales.ftboss.util.LemonSacrificeGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT)
public final class LemonSacrificeVisual {
    private static final int MAX_ACTIVE = 8;
    private record Show(LemonSacrificePacket packet, long started) {}
    private static final Map<UUID, Show> ACTIVE = new LinkedHashMap<>();
    private static ClientLevel world;

    private LemonSacrificeVisual() {}

    public static void receive(LemonSacrificePacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (!packet.valid() || level == null || !level.dimension().location().equals(packet.dimension())) return;
        long now = level.getGameTime();
        if (packet.started() > now + 20 || now - packet.started() >= LemonSacrificeGeometry.DURATION) return;
        if (world != level) { ACTIVE.clear(); world = level; }
        Show previous = ACTIVE.get(packet.owner());
        if (previous != null && previous.packet.started() >= packet.started()) return;
        if (previous == null && ACTIVE.size() >= MAX_ACTIVE) ACTIVE.remove(ACTIVE.keySet().iterator().next());
        ACTIVE.put(packet.owner(), new Show(packet, Math.min(now, packet.started())));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || world != level) { ACTIVE.clear(); world = level; return; }
        ACTIVE.values().removeIf(show -> !LemonSacrificeGeometry.alive(level.getGameTime() - show.started));
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { if (event.getLevel() == world) { ACTIVE.clear(); world = null; } }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { ACTIVE.clear(); world = null; }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level != world) return;
        Vec3 camera = event.getCamera().getPosition();
        boolean reduced = minecraft.options.particles().get() != ParticleStatus.ALL;
        var visible = ACTIVE.values().stream().filter(show -> camera.distanceToSqr(show.packet.center()) <= 96 * 96)
                .filter(show -> event.getFrustum() == null || event.getFrustum().isVisible(new AABB(show.packet.center(), show.packet.center().add(0, 10, 0)).inflate(26)))
                .sorted(Comparator.comparingDouble(show -> camera.distanceToSqr(show.packet.center())))
                .limit(reduced ? 2 : 4).toList();
        if (visible.isEmpty()) return;
        var buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertices = buffers.getBuffer(ManorIntroRenderType.TYPE);
        PoseStack poses = event.getPoseStack();
        try {
            for (Show show : visible) {
                LemonSacrificePacket packet = show.packet;
                double age = minecraft.level.getGameTime() - show.started + (minecraft.isPaused() ? 0 : event.getPartialTick());
                poses.pushPose();
                try {
                    poses.translate(packet.center().x - camera.x, packet.center().y - camera.y, packet.center().z - camera.z);
                    PoseStack.Pose matrix = poses.last();
                    LemonSacrificeGeometry.draw(age, reduced || camera.distanceToSqr(packet.center()) > 48 * 48, packet.souls(),
                            (horizontal, height, depth, color, alpha) -> vertices.vertex(matrix.pose(), (float) horizontal, (float) height, (float) depth)
                                    .color((color >> 16 & 255) / 255F * alpha, (color >> 8 & 255) / 255F * alpha, (color & 255) / 255F * alpha, 1)
                                    .uv(0.5F, 0.5F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                                    .normal(matrix.normal(), 0, 1, 0).endVertex());
                } finally { poses.popPose(); }
            }
        } finally { buffers.endBatch(ManorIntroRenderType.TYPE); }
    }
}
