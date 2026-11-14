package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ZheergenAfterimageHistory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = "ftboss", value = Dist.CLIENT)
public final class ZheergenAfterimageVisual {
    private static final RenderType TYPE = RenderType.entityTranslucent(
            new ResourceLocation("ftboss", "textures/fx/zheergen_afterimage.png"));
    private static final RenderType DANDAN_TYPE = RenderType.entityTranslucent(
            new ResourceLocation("ftboss", "textures/fx/dandan_afterimage.png"));
    private static final Map<Integer, Trail> TRAILS = new LinkedHashMap<>();
    private static final int MAX_TRAILS = 24;
    private static final double VIEW_RANGE_SQUARED = 64 * 64;
    private static ClientLevel world;
    private static Frame frame;

    private ZheergenAfterimageVisual() {}

    private static boolean visible(EasterBossEntity boss) {
        return (boss.getVariant() == EasterBossEntity.ZHEERGEN_VARIANT || boss.getVariant() == EasterBossEntity.DANDAN_VARIANT)
                && boss.isAlive() && !boss.isRemoved()
                && !boss.isMini() && !boss.isGiant() && !boss.isInvisible() && !boss.isManorIntroHidden();
    }

    private static void useLevel(ClientLevel level) {
        if (level == world) return;
        TRAILS.clear();
        frame = null;
        world = level;
    }

    @SubscribeEvent
    public static void prepare(RenderLivingEvent.Pre<?, ?> event) {
        frame = null;
        if (!(event.getEntity() instanceof EasterBossEntity boss) || !visible(boss)) return;
        Minecraft client = Minecraft.getInstance();
        useLevel(client.level);
        if (client.level == null || client.isPaused() || client.getCameraEntity() == null
                || boss.distanceToSqr(client.getCameraEntity()) > VIEW_RANGE_SQUARED) return;
        Trail trail = TRAILS.get(boss.getId());
        if (trail == null || trail.boss != boss) {
            if (TRAILS.size() >= MAX_TRAILS) return;
            trail = new Trail(boss, new ZheergenAfterimageHistory<>());
            TRAILS.put(boss.getId(), trail);
        }
        boolean moving = boss.position().distanceToSqr(new Vec3(boss.xo, boss.yo, boss.zo)) > 0.0009;
        if (!trail.history.ready(boss.tickCount, moving || boss.swinging || boss.isCastingLike())) return;
        frame = new Frame(boss, new Matrix4f(event.getPoseStack().last().pose()).invert(), boss.getPosition(event.getPartialTick()));
    }

    public static void capture(EasterBossEntity boss, EasterBossModel model, PoseStack pose) {
        Frame current = frame;
        if (current == null || current.boss != boss) return;
        frame = null;
        Trail trail = TRAILS.get(boss.getId());
        if (trail == null || trail.boss != boss) return;
        Matrix4f transform = new Matrix4f(current.inverseBase).mul(pose.last().pose());
        ZheergenAfterimageMesh mesh = new ZheergenAfterimageMesh(transform);
        model.renderToBuffer(new PoseStack(), mesh, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        float[] vertices = mesh.snapshot();
        if (vertices.length == 0) return;
        Vec3 movement = boss.position().subtract(boss.xo, boss.yo, boss.zo).multiply(1, 0, 1);
        Vec3 backward = movement.lengthSqr() > 0.0009 ? movement.normalize().scale(-1)
                : boss.getLookAngle().multiply(-1, 0, -1).normalize();
        trail.history.add(boss.tickCount, current.center.x, current.center.y, current.center.z, backward.x, backward.z, vertices);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        useLevel(client.level);
        if (world == null || client.isPaused()) return;
        TRAILS.values().removeIf(trail -> !visible(trail.boss) || world.getEntity(trail.boss.getId()) != trail.boss
                || client.getCameraEntity() == null || trail.boss.distanceToSqr(client.getCameraEntity()) > VIEW_RANGE_SQUARED);
        TRAILS.values().forEach(trail -> trail.history.expire(trail.boss.tickCount));
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft client = Minecraft.getInstance();
        useLevel(client.level);
        if (world == null || TRAILS.isEmpty()) return;
        var buffers = client.renderBuffers().bufferSource();
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        try {
            for (Trail trail : TRAILS.values()) {
                if (!visible(trail.boss)) continue;
                RenderType type = trail.boss.getVariant() == EasterBossEntity.DANDAN_VARIANT ? DANDAN_TYPE : TYPE;
                VertexConsumer vertices = buffers.getBuffer(type);
                for (var sample : trail.history.samples()) {
                    double age = trail.boss.tickCount - sample.tick() + event.getPartialTick();
                    float alpha = ZheergenAfterimageHistory.alpha(age);
                    if (alpha <= 0) continue;
                    double drift = age * ZheergenAfterimageHistory.DRIFT_PER_TICK;
                    pose.pushPose();
                    pose.translate(sample.positionX() + sample.backwardX() * drift, sample.positionY(),
                            sample.positionZ() + sample.backwardZ() * drift);
                    draw(pose, vertices, sample.payload(), alpha);
                    pose.popPose();
                }
            }
        } finally {
            pose.popPose();
            buffers.endBatch(TYPE);
            buffers.endBatch(DANDAN_TYPE);
        }
    }

    private static void draw(PoseStack pose, VertexConsumer vertices, float[] data, float alpha) {
        for (int index = 0; index < data.length; index += ZheergenAfterimageMesh.STRIDE) {
            vertices.vertex(pose.last().pose(), data[index], data[index + 1], data[index + 2])
                    .color(0.68F, 0.38F, 1F, alpha).uv(data[index + 3], data[index + 4])
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                    .normal(pose.last().normal(), data[index + 5], data[index + 6], data[index + 7]).endVertex();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { useLevel(null); }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { if (event.getLevel() == world) useLevel(null); }

    private record Frame(EasterBossEntity boss, Matrix4f inverseBase, Vec3 center) {}
    private record Trail(EasterBossEntity boss, ZheergenAfterimageHistory<float[]> history) {}
}
