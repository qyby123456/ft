package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.ManorIntroPacket;
import net.farmingtales.ftboss.util.ManorIntroTimeline;
import net.farmingtales.ftboss.util.PresentationClock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ManorIntroVisual {
    private static final RenderType TYPE = ManorRitualRenderType.TYPE;
    private static final int MAX_ACTIVE = 4;
    private static final double VIEW_DISTANCE_SQUARED = 128.0 * 128.0;
    private static final Map<UUID, Show> ACTIVE = new LinkedHashMap<>();

    private static final class Show {
        final ResourceLocation dimension;
        final long startMillis;
        Vec3 position;
        AABB bounds;
        final ManorRitualTerrain terrain = new ManorRitualTerrain();

        Show(ManorIntroPacket packet) {
            dimension = packet.dimension();
            startMillis = PresentationClock.millis() - packet.elapsedMillis();
            move(packet.position());
        }

        void move(Vec3 nextPosition) {
            position = nextPosition;
            bounds = new AABB(position.x - 10, position.y - 2, position.z - 10,
                    position.x + 10, position.y + 12, position.z + 10);
        }
    }

    private ManorIntroVisual() {}

    public static void receive(ManorIntroPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!packet.active()) {
            ACTIVE.remove(packet.id());
            return;
        }
        if (minecraft.level == null || !minecraft.level.dimension().location().equals(packet.dimension())
                || packet.elapsedMillis() < 0 || packet.elapsedMillis() >= ManorIntroTimeline.DURATION_MILLIS) return;
        Show show = ACTIVE.get(packet.id());
        if (show == null) {
            if (ACTIVE.size() >= MAX_ACTIVE) ACTIVE.remove(ACTIVE.keySet().iterator().next());
            ACTIVE.put(packet.id(), new Show(packet));
            FTBossMod.LOGGER.info("[FTBoss][ManorIntro] client mesh start id={} elapsed={}ms", packet.id(), packet.elapsedMillis());
        } else {
            show.move(packet.position());
        }
    }

    private static void clear() {
        ACTIVE.clear();
        PresentationClock.setPaused(false);
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            PresentationClock.setPaused(Minecraft.getInstance().isPaused());
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            clear();
            return;
        }
        long nowMillis = PresentationClock.millis();
        ACTIVE.values().removeIf(show -> !minecraft.level.dimension().location().equals(show.dimension)
                || nowMillis - show.startMillis >= ManorIntroTimeline.DURATION_MILLIS);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) clear();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) { clear(); }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        boolean rendered = false;
        VertexConsumer vertices = null;
        long nowMillis = PresentationClock.millis();
        for (Show show : ACTIVE.values()) {
            if (!minecraft.level.dimension().location().equals(show.dimension)
                    || camera.distanceToSqr(show.position) > VIEW_DISTANCE_SQUARED
                    || (event.getFrustum() != null && !event.getFrustum().isVisible(show.bounds))) continue;
            long elapsedMillis = nowMillis - show.startMillis;
            if (elapsedMillis < 0 || elapsedMillis >= ManorIntroTimeline.DURATION_MILLIS) continue;
            boolean reduced = minecraft.options.particles().get() == ParticleStatus.MINIMAL
                    || camera.distanceToSqr(show.position) > 48.0 * 48.0;
            pose.pushPose();
            try {
                pose.translate(show.position.x - camera.x, show.position.y - camera.y, show.position.z - camera.z);
                PoseStack.Pose matrix = pose.last();
                if (vertices == null) vertices = buffers.getBuffer(TYPE);
                VertexConsumer batch = vertices;
                show.terrain.begin(minecraft.level, show.position, nowMillis, (posX, posY, posZ, color, alpha) ->
                        batch.vertex(matrix.pose(), (float) posX, (float) posY, (float) posZ)
                                .color((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F,
                                        (color & 255) / 255.0F, alpha).endVertex());
                ManorIntroMesh.emit(elapsedMillis, reduced, show.terrain::vertex);
                rendered = true;
            } finally {
                pose.popPose();
            }
        }
        if (rendered) buffers.endBatch(TYPE);
    }
}
