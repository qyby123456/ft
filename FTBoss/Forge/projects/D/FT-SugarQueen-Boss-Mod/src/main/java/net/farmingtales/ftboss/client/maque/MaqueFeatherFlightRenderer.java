package net.farmingtales.ftboss.client.maque;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = "ftboss", value = Dist.CLIENT)
public final class MaqueFeatherFlightRenderer {
    private static final Map<UUID, Flight> ACTIVE = new LinkedHashMap<>();
    private static ClientLevel identity;
    private static MaqueArcaneAssets.Surface[] surfaces;
    private static double[] origin;

    private MaqueFeatherFlightRenderer() {}

    private static void bind(ClientLevel level) { if (identity != level) { ACTIVE.clear(); identity = level; } }

    public static boolean accept(SpellVisualPacket packet, Map<String, ?> previousEffects) {
        if (!packet.spell().id.equals("feather_fan")) return false;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !packet.valid() || !level.dimension().location().equals(packet.dimension())) return true;
        bind(level);
        if (packet.ownerId() != Integer.MAX_VALUE) {
            if (packet.phase() == Phase.CHARGE) return false;
            previousEffects.remove(packet.owner() + ":" + packet.spell().id);
            MaqueHumanoidAnimations.spell(level, packet);
            return true;
        }
        if (packet.phase() == Phase.PULSE) { ACTIVE.remove(packet.owner()); return true; }
        if (packet.phase() != Phase.BEAM) return true;
        long now = level.getGameTime();
        Flight flight = ACTIVE.computeIfAbsent(packet.owner(), unused -> new Flight());
        flight.position = packet.start();
        flight.velocity = packet.end().subtract(packet.start());
        flight.updated = now;
        flight.history.addFirst(packet.start());
        while (flight.history.size() > 8) flight.history.removeLast();
        if (ACTIVE.size() > 320) ACTIVE.remove(ACTIVE.keySet().iterator().next());
        return true;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ClientLevel level = Minecraft.getInstance().level;
        bind(level);
        if (level != null) ACTIVE.values().removeIf(flight -> level.getGameTime() - flight.updated > 8);
    }

    @SubscribeEvent
    public static void disconnect(ClientPlayerNetworkEvent.LoggingOut event) { bind(null); }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { if (event.getLevel() == identity) bind(null); }

    private static void assets() {
        if (surfaces != null) return;
        MaqueArcaneAssets.Art art = MaqueArcaneAssets.get("feather_fan");
        surfaces = java.util.Arrays.stream(art.lite()).filter(surface -> surface.bone() == 2).toArray(MaqueArcaneAssets.Surface[]::new);
        double[] pivot = art.bones()[2].pivot();
        origin = new double[]{pivot[0], pivot[1] + 0.09, 1.08};
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty() || !MaqueArcaneLayers.ready()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != identity) return;
        assets();
        boolean bloom = SpellBloomRenderer.begin(event, true);
        try {
            Vec3 camera = event.getCamera().getPosition();
            double[] eye = {camera.x, camera.y, camera.z};
            Vector3f facing = new Vector3f(0, 0, -1).rotate(event.getCamera().rotation());
            double[] direction = {facing.x, facing.y, facing.z};
            boolean firstPerson = !event.getCamera().isDetached();
            Matrix4f position = event.getPoseStack().last().pose();
            Matrix3f normal = event.getPoseStack().last().normal();
            var buffers = minecraft.renderBuffers().bufferSource();
            List<Flight> flights = ACTIVE.values().stream().filter(flight -> flight.position.distanceToSqr(camera) <= 96 * 96)
                    .sorted(Comparator.comparingDouble(flight -> flight.position.distanceToSqr(camera))).limit(32).toList();
            for (boolean refractive : new boolean[]{true, false}) {
                VertexConsumer consumer = buffers.getBuffer(refractive ? MaqueArcaneLayers.REFRACTION : MaqueArcaneLayers.RADIANCE);
                for (Flight flight : flights) {
                    Vec3 point = flight.position.add(flight.velocity.scale(Math.max(0, Math.min(2,
                            identity.getGameTime() - flight.updated + event.getPartialTick()))));
                    double[] center = {point.x, point.y, point.z};
                    double[] velocity = {flight.velocity.x, flight.velocity.y, flight.velocity.z};
                    for (MaqueArcaneAssets.Surface surface : surfaces) {
                        if ((surface.material() >= 4) != refractive) continue;
                        double[][] quad = new double[4][];
                        for (int corner = 0; corner < 4; corner++) quad[corner] = MaqueRicochetPose.transform(surface.points()[corner], origin, center, velocity);
                        draw(consumer, position, normal, quad, surface.material(), surface.opacity(), eye, direction, firstPerson);
                    }
                    if (!refractive) {
                        List<Vec3> history = new ArrayList<>(flight.history);
                        Vec3 head = point;
                        for (int segment = 0; segment < history.size(); segment++) {
                            Vec3 tail = history.get(segment);
                            if (head.distanceToSqr(tail) > 1e-6) {
                                for (int plane = 0; plane < 2; plane++) {
                                    double[][] quad = MaqueRicochetPose.trailQuad(new double[]{head.x, head.y, head.z},
                                            new double[]{tail.x, tail.y, tail.z}, segment, history.size(), plane);
                                    draw(consumer, position, normal, quad, plane, MaqueRicochetPose.trailAlpha(segment, history.size()),
                                            MaqueRicochetPose.trailAlpha(segment + 1, history.size()), eye, direction, firstPerson);
                                }
                            }
                            head = tail;
                        }
                    }
                }
                buffers.endBatch(refractive ? MaqueArcaneLayers.REFRACTION : MaqueArcaneLayers.RADIANCE);
            }
        } finally { SpellBloomRenderer.end(bloom); }
    }

    private static void draw(VertexConsumer consumer, Matrix4f position, Matrix3f normalMatrix, double[][] quad,
            int material, double opacity, double[] eye, double[] direction, boolean firstPerson) {
        draw(consumer, position, normalMatrix, quad, material, opacity, opacity, eye, direction, firstPerson);
    }

    private static void draw(VertexConsumer consumer, Matrix4f position, Matrix3f normalMatrix, double[][] quad,
            int material, double headOpacity, double tailOpacity, double[] eye, double[] direction, boolean firstPerson) {
        double[] middle = new double[3];
        for (double[] point : quad) for (int axis = 0; axis < 3; axis++) middle[axis] += point[axis] * 0.25;
        double visibility = firstPerson ? FirstPersonSpellVisibility.cameraFactor(
                Math.sqrt(Math.pow(middle[0] - eye[0], 2) + Math.pow(middle[1] - eye[1], 2) + Math.pow(middle[2] - eye[2], 2)), true) : 1;
        if (firstPerson) visibility *= MaqueArcanePose.sightFade(middle, eye, direction, true);
        if (Math.max(headOpacity, tailOpacity) * visibility < 0.01) return;
        double normalHorizontal = (quad[1][1] - quad[0][1]) * (quad[2][2] - quad[0][2]) - (quad[1][2] - quad[0][2]) * (quad[2][1] - quad[0][1]);
        double normalVertical = (quad[1][2] - quad[0][2]) * (quad[2][0] - quad[0][0]) - (quad[1][0] - quad[0][0]) * (quad[2][2] - quad[0][2]);
        double normalDepth = (quad[1][0] - quad[0][0]) * (quad[2][1] - quad[0][1]) - (quad[1][1] - quad[0][1]) * (quad[2][0] - quad[0][0]);
        double length = Math.sqrt(normalHorizontal * normalHorizontal + normalVertical * normalVertical + normalDepth * normalDepth);
        if (length < 1e-10) return;
        for (int corner = 0; corner < 4; corner++) {
            double alpha = (corner < 2 ? headOpacity : tailOpacity) * visibility;
            float textureHorizontal = (float) ((material * 64 + (corner == 1 || corner == 2 ? 59 : 4)) / 384.0);
            float textureVertical = (float) ((corner >= 2 ? 251 : 4) / 256.0);
            consumer.vertex(position, (float) (quad[corner][0] - eye[0]), (float) (quad[corner][1] - eye[1]), (float) (quad[corner][2] - eye[2]))
                    .color(255, 255, 255, (int) Math.round(Math.min(1, alpha) * 255)).uv(textureHorizontal, textureVertical)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0)
                    .normal(normalMatrix, (float) (normalHorizontal / length), (float) (normalVertical / length), (float) (normalDepth / length)).endVertex();
        }
    }

    private static final class Flight {
        final ArrayDeque<Vec3> history = new ArrayDeque<>();
        Vec3 position;
        Vec3 velocity;
        long updated;
    }
}
