package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.ManorTransitionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 庄园切换客户端入口：复用现有切换包，独立材质与网格；2026-10-25重做并隔离开场和结束。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT)
public final class ManorTransitionVisual {
    private static final ManorTransitionState STATE = new ManorTransitionState();
    private static final Map<UUID, ManorTransitionSurface> SURFACES = new HashMap<>();
    private static Object worldIdentity;

    private ManorTransitionVisual() {}

    public static void receive(ManorTransitionPacket packet) {
        if (!packet.valid()) return;
        var level = Minecraft.getInstance().level;
        checkWorld(level);
        if (packet.active()) SURFACES.remove(packet.id());
        STATE.receive(packet.id(), packet.dimension().toString(), packet.center().x, packet.center().y, packet.center().z,
                packet.variant(), packet.age(), packet.active(), level == null ? null : level.dimension().location().toString());
        pruneSurfaces();
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var client = Minecraft.getInstance();
        checkWorld(client.level);
        STATE.tick(client.level == null ? null : client.level.dimension().location().toString(), client.isPaused());
        pruneSurfaces();
    }

    private static void checkWorld(Object world) {
        if (worldIdentity != world) {
            clear();
            worldIdentity = world;
        }
    }

    private static void pruneSurfaces() {
        var live = STATE.shows().stream().map(show -> show.id).toList();
        SURFACES.keySet().retainAll(live);
    }

    private static void clear() {
        STATE.clear();
        SURFACES.clear();
        worldIdentity = null;
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == worldIdentity) clear();
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { clear(); }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        var client = Minecraft.getInstance();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || client.level == null) return;
        checkWorld(client.level);
        var shows = STATE.shows();
        if (shows.isEmpty()) return;
        var camera = event.getCamera().getPosition();
        shows.sort(Comparator.comparingDouble(show -> camera.distanceToSqr(show.centerX, show.centerY, show.centerZ)));
        var visible = shows.stream().filter(show -> camera.distanceToSqr(show.centerX, show.centerY, show.centerZ)
                <= ManorTransitionRules.CULL_DISTANCE * ManorTransitionRules.CULL_DISTANCE
                && (event.getFrustum() == null || event.getFrustum().isVisible(new AABB(
                        show.centerX - ManorTransitionRules.RADIUS, show.centerY - 1.4, show.centerZ - ManorTransitionRules.RADIUS,
                        show.centerX + ManorTransitionRules.RADIUS, show.centerY + ManorTransitionRules.HEIGHT,
                        show.centerZ + ManorTransitionRules.RADIUS)))).toList();
        if (visible.isEmpty()) return;
        ParticleStatus particles = client.options.particles().get();
        int particleSetting = particles == ParticleStatus.ALL ? 0 : particles == ParticleStatus.DECREASED ? 1 : 2;
        var buffers = client.renderBuffers().bufferSource();
        VertexConsumer vertices = buffers.getBuffer(ManorTransitionRenderType.TYPE);
        int remaining = ManorTransitionRules.FRAME_VERTEX_BUDGET;
        var pose = event.getPoseStack();
        try {
            for (var show : visible) {
                if (remaining < 12) break;
                double age = show.age(event.getPartialTick(), client.isPaused());
                if (!ManorTransitionRules.valid(show.variant(), age)) continue;
                int detail = ManorTransitionRules.detail(camera.distanceToSqr(show.centerX, show.centerY, show.centerZ), particleSetting);
                if (visible.size() > 2) detail = Math.max(1, detail);
                ManorTransitionSurface surface = SURFACES.computeIfAbsent(show.id,
                        ignored -> new ManorTransitionSurface(client.level, show.centerX, show.centerY, show.centerZ));
                pose.pushPose();
                try {
                    pose.translate(show.centerX - camera.x, show.centerY - camera.y, show.centerZ - camera.z);
                    var matrix = pose.last().pose();
                    remaining -= ManorTransitionMesh.draw(age, show.variant(), detail, remaining, surface,
                            (posX, posY, posZ, color, alpha) -> vertices.vertex(matrix, (float) posX, (float) posY, (float) posZ)
                                    .color((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, alpha).endVertex());
                } finally {
                    pose.popPose();
                }
            }
        } finally {
            buffers.endBatch(ManorTransitionRenderType.TYPE);
        }
    }
}
