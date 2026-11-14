package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.CombatVisualEntities;
import net.farmingtales.ftboss.entity.ChargedOrbEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.OrbType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 充能球与实际手部、胸口骨骼的输能连线；2026-09-17，使用同步技能阶段与有界渲染预算。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class DefectOrbVisual {
    private record Anchor(Vec3 position, long tick) {}
    private static final Map<UUID, Map<String, Anchor>> ANCHORS = new HashMap<>();
    private static final String[] HANDS = {"hand_l", "hand_r"};
    private static Level currentLevel;

    private DefectOrbVisual() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        useLevel(client.level);
        if (client.level == null || client.isPaused()) return;
        long tick = client.level.getGameTime();
        if (tick % 20 == 0) ANCHORS.values().removeIf(points -> points.values().stream().allMatch(point -> tick - point.tick > 40));
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == currentLevel) useLevel(null);
    }

    private static void useLevel(Level level) {
        if (currentLevel == level) return;
        ANCHORS.clear();
        currentLevel = level;
    }

    public static void track(DefectCoreEntity boss, String bone, Vector3d position) {
        useLevel(boss.level());
        ANCHORS.computeIfAbsent(boss.getUUID(), ignored -> new HashMap<>()).put(bone,
                new Anchor(new Vec3(position.x, position.y, position.z), boss.level().getGameTime()));
    }

    private static Vec3 anchor(DefectCoreEntity boss, String bone, float partialTick) {
        var tracked = ANCHORS.get(boss.getUUID());
        Anchor point = tracked == null ? null : tracked.get(bone);
        if (point != null && boss.level().getGameTime() - point.tick <= 1) return point.position;
        return boss.orbAnchor(!bone.equals("core")).add(boss.getPosition(partialTick).subtract(boss.position()));
    }

    private static int color(OrbType type) {
        return switch (type) {
            case GLASS -> 0x72EEED;
            case LIGHTNING -> 0xFFE374;
            case FROST -> 0xB6E8FF;
            case ION -> 0xEE91EE;
            case DARK -> 0xAB75F4;
        };
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft client = Minecraft.getInstance();
        useLevel(client.level);
        if (client.level == null) return;
        var candidates = CombatVisualEntities.get(client.level).orbLinks();
        if (candidates.isEmpty()) return;
        long tick = client.level.getGameTime();
        Vec3 camera = event.getCamera().getPosition();
        float partialTick = event.getPartialTick();
        double clock = tick + partialTick;
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = client.renderBuffers().bufferSource();
        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            Matrix4f matrix = pose.last().pose();
            VertexConsumer vertices = buffers.getBuffer(LinkRenderType.TYPE);
            int linkBudget = 48;
            int coreBudget = 12;
            for (Entity candidate : candidates) {
                if (linkBudget <= 0 && coreBudget <= 0) break;
                if (candidate.isRemoved() || candidate.distanceToSqr(camera) > 64 * 64) continue;
                if (candidate instanceof ChargedOrbEntity orb && linkBudget > 0) {
                    DefectCoreEntity boss = orb.ownerBoss();
                    if (boss == null || !boss.isAlive()) continue;
                    String stage = boss.orbLinkStage();
                    String visual = orb.visualState();
                    boolean focus = stage.equals("focus") && boss.orbCue().getInt("orb") == orb.getId();
                    boolean forming = visual.equals("summon");
                    boolean flowing = stage.startsWith("beam") || (stage.equals("passive") && orb.getOrbType() != OrbType.DARK) || visual.equals("drain")
                            || visual.equals("return") || stage.equals("phase") || stage.equals("echo");
                    if (!focus && !forming && !flowing) continue;
                    int tint = color(orb.getOrbType());
                    Vec3 orbCenter = orb.getPosition(partialTick).add(0, 0.25, 0);
                    Vec3 source = anchor(boss, focus || forming ? "hand_r" : "core", partialTick);
                    Vec3 start = forming ? source : orbCenter;
                    Vec3 end = forming ? orbCenter : source;
                    double thickness = stage.startsWith("beam") ? 0.028 : 0.014;
                    strand(vertices, matrix, start, end, camera, thickness, tint, 0.35f, clock, orb.getId());
                    for (int bead = 0; bead < 3; bead++) {
                        double progress = (clock * 0.055 + bead / 3.0 + orb.getId() * 0.17) % 1;
                        Vec3 point = start.lerp(end, progress);
                        segment(vertices, matrix, point, start.lerp(end, Math.min(1, progress + 0.065)),
                                camera, thickness * 2.6, 0xF6FFFF, 0.9f);
                    }
                    if (focus) ring(vertices, matrix, orbCenter, camera, 0.46, tint, 0.65f, clock * 0.04, false);
                    linkBudget--;
                } else if (candidate instanceof DefectCoreEntity boss && coreBudget > 0 && boss.isAlive()) {
                    var cue = boss.orbCue();
                    String stage = boss.orbLinkStage();
                    if (stage.isEmpty()) continue;
                    double age = clock - cue.getLong("start");
                    double progress = Math.min(1, Math.max(0, age / Math.max(1, cue.getInt("duration"))));
                    OrbType type = OrbType.byId(cue.getString("type"));
                    int tint = color(type);
                    Vec3 core = anchor(boss, "core", partialTick);
                    if (stage.startsWith("beam")) {
                        ring(vertices, matrix, core, camera, 0.38 + 0.22 * progress, tint, 0.8f, clock * 0.07, false);
                        for (String hand : HANDS)
                            strand(vertices, matrix, anchor(boss, hand, partialTick), core, camera, 0.035, tint, 0.7f, clock, 0);
                    } else if (stage.equals("release") || stage.equals("interrupt")) {
                        Vec3 center = type == OrbType.FROST || type == OrbType.ION ? core : anchor(boss, "hand_r", partialTick);
                        double radius = stage.equals("interrupt") ? 0.55 * (1 - progress) : 0.25 + progress * 1.25;
                        ring(vertices, matrix, center, camera, radius, tint, (float) (0.8 * (1 - progress)), clock * 0.08, type == OrbType.FROST);
                        if (type == OrbType.ION || type == OrbType.DARK)
                            ring(vertices, matrix, center, camera, radius * 0.72, tint, (float) (0.7 * (1 - progress)), -clock * 0.08, true);
                    } else {
                        ring(vertices, matrix, core, camera, 0.32 + Math.sin(clock * 0.25) * 0.035, tint, 0.5f, clock * 0.03, false);
                    }
                    coreBudget--;
                }
            }
            buffers.endBatch(LinkRenderType.TYPE);
        } finally {
            pose.popPose();
        }
    }

    private static void strand(VertexConsumer vertices, Matrix4f matrix, Vec3 start, Vec3 end, Vec3 camera,
                               double width, int color, float opacity, double clock, int seed) {
        Vec3 previous = start;
        for (int step = 1; step <= 10; step++) {
            double progress = step / 10.0;
            double wave = Math.sin(Math.PI * progress) * 0.055;
            Vec3 point = start.lerp(end, progress).add(Math.sin(clock * 0.16 + seed + step) * wave,
                    Math.cos(clock * 0.13 + seed + step) * wave, 0);
            segment(vertices, matrix, previous, point, camera, width, color, opacity);
            previous = point;
        }
    }

    private static void ring(VertexConsumer vertices, Matrix4f matrix, Vec3 center, Vec3 camera,
                             double radius, int color, float opacity, double angle, boolean horizontal) {
        Vec3 normal = camera.subtract(center).normalize();
        Vec3 across = horizontal ? new Vec3(1, 0, 0) : normal.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 up = horizontal ? new Vec3(0, 0, 1) : across.cross(normal).normalize();
        for (int step = 0; step < 32; step++) {
            if (step % 8 == 7) continue;
            double first = angle + step * Math.PI / 16;
            double second = angle + (step + 1) * Math.PI / 16;
            Vec3 start = center.add(across.scale(Math.cos(first) * radius)).add(up.scale(Math.sin(first) * radius));
            Vec3 end = center.add(across.scale(Math.cos(second) * radius)).add(up.scale(Math.sin(second) * radius));
            segment(vertices, matrix, start, end, camera, 0.02, color, opacity);
        }
    }

    private static void segment(VertexConsumer vertices, Matrix4f matrix, Vec3 start, Vec3 end, Vec3 camera,
                                double width, int color, float opacity) {
        Vec3 side = end.subtract(start).cross(camera.subtract(start)).normalize().scale(width);
        vertex(vertices, matrix, start.add(side), color, opacity);
        vertex(vertices, matrix, end.add(side), color, opacity);
        vertex(vertices, matrix, end.subtract(side), color, opacity);
        vertex(vertices, matrix, start.subtract(side), color, opacity);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, Vec3 point, int color, float opacity) {
        vertices.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(color >> 16 & 255, color >> 8 & 255, color & 255, (int) (255 * Math.max(0, Math.min(1, opacity)))).endVertex();
    }

    private static final class LinkRenderType extends RenderType {
        private static final RenderType TYPE = create("ftboss_defect_link", DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS, 8192, false, true, CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL)
                        .createCompositeState(false));

        private LinkRenderType() {
            super("ftboss_defect_link", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                    8192, false, true, () -> {}, () -> {});
        }
    }
}
