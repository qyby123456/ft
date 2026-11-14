package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.network.ZicaiVoidNetwork.Burst;
import net.farmingtales.ftboss.network.ZicaiVoidNetwork.Mark;
import net.farmingtales.ftboss.util.ZicaiVoidRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ftboss", value = Dist.CLIENT)
public final class ZicaiVoidVisual {
    private static final List<Burst> ACTIVE = new ArrayList<>();
    private static final RenderType TYPE = RenderType.lightning();
    private static final int GOLD = 0xF4C45F;
    private static final int WHITE = 0xFFF4D9;

    private ZicaiVoidVisual() {}

    public static void accept(Burst burst) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !minecraft.level.dimension().location().equals(burst.dimension())) return;
        ACTIVE.removeIf(previous -> previous.owner() == burst.owner() && previous.fountain() == burst.fountain());
        if (ACTIVE.size() >= ZicaiVoidRules.MAX_FIELDS) ACTIVE.remove(0);
        ACTIVE.add(burst);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var level = Minecraft.getInstance().level;
        if (level == null) { ACTIVE.clear(); return; }
        ACTIVE.removeIf(burst -> !level.dimension().location().equals(burst.dimension())
                || !(level.getEntity(burst.owner()) instanceof LivingEntity owner) || !owner.isAlive()
                || level.getGameTime() - burst.born() > burst.marks().stream().mapToInt(Mark::strike).max().orElse(0)
                        + ZicaiVoidRules.TAIL_TICKS);
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { ACTIVE.clear(); }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { if (event.getLevel().isClientSide()) ACTIVE.clear(); }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || minecraft.level == null || ACTIVE.isEmpty()) return;
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertices = buffers.getBuffer(TYPE);
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        try {
            for (Burst burst : ACTIVE) {
                double age = minecraft.level.getGameTime() - burst.born() + event.getPartialTick();
                for (Mark mark : burst.marks()) {
                    if (age < mark.warning() || age > mark.strike() + ZicaiVoidRules.TAIL_TICKS) continue;
                    Vec3 center = mark.position();
                    if (center.distanceToSqr(camera) > ZicaiVoidRules.VIEW_RANGE * ZicaiVoidRules.VIEW_RANGE) continue;
                    double radius = ZicaiVoidRules.radius(burst.fountain())
                            * net.farmingtales.ftboss.skill.SpellVisuals.playerRadiusScale(minecraft.level.getEntity(burst.owner()));
                    if (age < mark.strike()) {
                        double progress = (age - mark.warning()) / (mark.strike() - mark.warning());
                        ring(pose, vertices, center, radius, 0.045, GOLD, 0.5F);
                        ring(pose, vertices, center.add(0, 0.01, 0), radius * (1 - progress * 0.7), 0.025, WHITE, 0.8F);
                    } else {
                        double elapsed = age - mark.strike();
                        float fade = (float) Math.max(0, 1 - elapsed / ZicaiVoidRules.TAIL_TICKS);
                        double height = ZicaiVoidRules.height(burst.fountain()) * Math.min(1, (elapsed + 1) / 3);
                        ring(pose, vertices, center, radius * (1 + elapsed * 0.025), 0.06, WHITE, fade * 0.7F);
                        pillar(pose, vertices, center, radius * 0.65, height, GOLD, fade * 0.5F);
                        pillar(pose, vertices, center, radius * 0.11, height * 1.04, WHITE, fade * 0.9F);
                        if (burst.fountain()) ring(pose, vertices, center.add(0, height * 0.6, 0),
                                radius * 0.75, 0.035, WHITE, fade * 0.55F);
                    }
                }
            }
        } finally {
            pose.popPose();
            buffers.endBatch(TYPE);
        }
    }

    private static void ring(PoseStack pose, VertexConsumer vertices, Vec3 center, double radius, double width, int color, float alpha) {
        for (int segment = 0; segment < 24; segment++) {
            double angle = segment * Math.PI * 2 / 24;
            double next = (segment + 1) * Math.PI * 2 / 24;
            Vec3 direction = new Vec3(Math.cos(angle), 0, Math.sin(angle));
            Vec3 nextDirection = new Vec3(Math.cos(next), 0, Math.sin(next));
            quad(pose, vertices, center.add(direction.scale(radius + width)), center.add(nextDirection.scale(radius + width)),
                    center.add(nextDirection.scale(radius - width)), center.add(direction.scale(radius - width)), color, alpha);
        }
    }

    private static void pillar(PoseStack pose, VertexConsumer vertices, Vec3 center, double width, double height, int color, float alpha) {
        for (int side = 0; side < 3; side++) {
            double angle = side * Math.PI / 3;
            Vec3 edge = new Vec3(Math.cos(angle) * width, 0, Math.sin(angle) * width);
            quad(pose, vertices, center.subtract(edge), center.add(edge),
                    center.add(edge.scale(0.25)).add(0, height, 0), center.subtract(edge.scale(0.25)).add(0, height, 0), color, alpha);
        }
    }

    private static void quad(PoseStack pose, VertexConsumer vertices, Vec3 first, Vec3 second,
            Vec3 third, Vec3 fourth, int color, float alpha) {
        vertex(pose, vertices, first, color, alpha);
        vertex(pose, vertices, second, color, alpha);
        vertex(pose, vertices, third, color, alpha);
        vertex(pose, vertices, fourth, color, alpha);
    }

    private static void vertex(PoseStack pose, VertexConsumer vertices, Vec3 point, int color, float alpha) {
        vertices.vertex(pose.last().pose(), (float) point.x, (float) point.y, (float) point.z)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, (int) (Math.max(0, Math.min(1, alpha)) * 255)).endVertex();
    }
}
