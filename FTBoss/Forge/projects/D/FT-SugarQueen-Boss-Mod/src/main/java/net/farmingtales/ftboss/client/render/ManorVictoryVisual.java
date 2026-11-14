package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.ManorVictoryPacket;
import net.farmingtales.ftboss.util.ManorVictoryGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
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
public final class ManorVictoryVisual {
    private record Show(ManorVictoryPacket packet, long started) {}
    private static final Map<UUID, Show> ACTIVE = new LinkedHashMap<>();

    private ManorVictoryVisual() {}

    public static void receive(ManorVictoryPacket packet) {
        var level = Minecraft.getInstance().level;
        if (!packet.valid() || level == null || !level.dimension().location().equals(packet.dimension())) return;
        if (!ACTIVE.containsKey(packet.id()) && ACTIVE.size() >= 4) ACTIVE.remove(ACTIVE.keySet().iterator().next());
        ACTIVE.putIfAbsent(packet.id(), new Show(packet, level.getGameTime() - packet.age()));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var level = Minecraft.getInstance().level;
        if (level == null) { ACTIVE.clear(); return; }
        ACTIVE.values().removeIf(show -> !level.dimension().location().equals(show.packet.dimension())
                || level.getGameTime() < show.started || level.getGameTime() - show.started >= ManorVictoryGeometry.DURATION);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { if (event.getLevel().isClientSide()) ACTIVE.clear(); }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { ACTIVE.clear(); }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        Vec3 camera = event.getCamera().getPosition();
        var buffers = minecraft.renderBuffers().bufferSource();
        boolean rendered = false;
        VertexConsumer vertices = null;
        for (Show show : ACTIVE.values()) {
            if (!minecraft.level.dimension().location().equals(show.packet.dimension())) continue;
            Vec3 center = show.packet.center();
            if (camera.distanceToSqr(center) > 128 * 128 || event.getFrustum() != null
                    && !event.getFrustum().isVisible(new AABB(center, center.add(0, 5, 0)).inflate(7))) continue;
            double age = minecraft.level.getGameTime() - show.started + (minecraft.isPaused() ? 0 : event.getPartialTick());
            PoseStack poses = event.getPoseStack();
            poses.pushPose();
            try {
                poses.translate(center.x - camera.x, center.y - camera.y, center.z - camera.z);
                PoseStack.Pose matrix = poses.last();
                if (vertices == null) vertices = buffers.getBuffer(ManorCeremonyRenderType.TYPE);
                VertexConsumer batch = vertices;
                boolean reduced = minecraft.options.particles().get() != ParticleStatus.ALL || camera.distanceToSqr(center) > 48 * 48;
                ManorVictoryGeometry.draw(age, reduced, (horizontal, height, depth, color, alpha) ->
                        batch.vertex(matrix.pose(), (float) horizontal, (float) height, (float) depth)
                                .color((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, alpha).endVertex());
                rendered = true;
            } finally { poses.popPose(); }
        }
        if (rendered) buffers.endBatch(ManorCeremonyRenderType.TYPE);
    }
}
