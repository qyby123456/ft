package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.DandanScytheMotionRules;
import net.farmingtales.ftboss.util.DandanScytheMotionRules.Joint;
import net.farmingtales.ftboss.util.DandanScytheMotionRules.Pose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DandanScytheRenderer extends BlockEntityWithoutLevelRenderer {
    private record Prepared(ItemStack stack, Pose motion) {}
    private static final ThreadLocal<Prepared> PREPARED = new ThreadLocal<>();
    private static final Map<String, Joint> JOINTS = new LinkedHashMap<>();
    private static final int IVORY = 0xFFF2D9;
    private static final int VIOLET = 0xBBA3FF;
    private static final double[][] EDGE = {{9.5, 23.9}, {4.5, 25.1}, {0.5, 24.4}, {-3.5, 22.6},
            {-6.5, 20.1}, {-8.5, 17.4}, {-10.75, 13.9}, {-11.8, 12.4}};
    private final ModelPart bone = new ModelPart(List.of(), Map.of());

    static {
        DandanScytheMotionRules.JOINTS.forEach(joint -> JOINTS.put(joint.name(), joint));
    }

    public DandanScytheRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static void prepare(ItemStack stack, Pose motion) { PREPARED.set(new Prepared(stack, motion)); }
    public static void clearPrepared() { PREPARED.remove(); }

    private static ResourceLocation model(String name) {
        return new ResourceLocation(FTBossMod.MOD_ID, "item/dandan_rig/" + name);
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        JOINTS.keySet().forEach(name -> event.register(model(name)));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
                             MultiBufferSource buffers, int light, int overlay) {
        Prepared prepared = PREPARED.get();
        clearPrepared();
        boolean held = context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        Pose motion = held && prepared != null && prepared.stack == stack ? prepared.motion
                : DandanScytheMotionRules.sample(0, 0, -1, false, 0);
        var client = Minecraft.getInstance();
        VertexConsumer solid = ItemRenderer.getFoilBufferDirect(buffers,
                RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS), true, stack.hasFoil());
        for (Joint joint : JOINTS.values()) {
            pose.pushPose();
            apply(joint.name(), motion, pose);
            client.getItemRenderer().renderModelLists(client.getModelManager().getModel(model(joint.name())), stack,
                    joint.name().endsWith("core") ? LightTexture.FULL_BRIGHT : light, overlay, pose, solid);
            pose.popPose();
        }
        if (!held) return;
        VertexConsumer glow = buffers.getBuffer(DandanScytheGlow.TYPE);
        drawCores(pose, glow, motion);
        drawEdge(pose, glow, motion);
        drawCharge(pose, glow, motion);
    }

    private void apply(String name, Pose motion, PoseStack pose) {
        Joint joint = JOINTS.get(name);
        if (!joint.parent().isEmpty()) apply(joint.parent(), motion, pose);
        var values = DandanScytheMotionRules.bone(name, motion);
        bone.setPos(joint.pivotX() + values.moveX(), joint.pivotY() + values.moveY(), joint.pivotZ() + values.moveZ());
        bone.xRot = 0;
        bone.yRot = 0;
        bone.zRot = (float) Math.toRadians(values.rotateZ());
        bone.xScale = values.scale();
        bone.yScale = values.scale();
        bone.zScale = values.scale();
        bone.translateAndRotate(pose);
        pose.translate(-joint.pivotX() / 16, -joint.pivotY() / 16, -joint.pivotZ() / 16);
    }

    private Vec3 edgePoint(double[] point, Pose motion, float lag) {
        PoseStack local = new PoseStack();
        local.translate(0.5, 0.25, 0.5);
        local.mulPose(Axis.ZP.rotationDegrees(lag));
        local.translate(-0.5, -0.25, -0.5);
        apply(point[0] < -7 ? "blade_tip" : point[0] < 0 ? "blade_mid" : "blade_base", motion, local);
        Vector3f transformed = local.last().pose().transformPosition((float) point[0] / 16, (float) point[1] / 16, 0.5F, new Vector3f());
        return new Vec3(transformed.x, transformed.y, transformed.z);
    }

    private void drawEdge(PoseStack pose, VertexConsumer vertices, Pose motion) {
        float strength = motion.trail();
        for (int layer = DandanScytheMotionRules.TRAIL_LAYERS; layer >= 0; layer--) {
            float start = layer * (3 + 9 * strength);
            float end = start + (layer == 0 ? 0.7F : 2 + 5 * strength);
            float alpha = layer == 0 ? 0.18F + motion.glow() * 0.48F : strength * 0.24F / layer;
            if (alpha <= 0.001F) continue;
            for (int segment = 0; segment < EDGE.length - 1; segment++) {
                quad(pose, vertices, edgePoint(EDGE[segment], motion, start), edgePoint(EDGE[segment + 1], motion, start),
                        edgePoint(EDGE[segment + 1], motion, end), edgePoint(EDGE[segment], motion, end),
                        layer % 2 == 0 ? IVORY : VIOLET, alpha);
            }
        }
    }

    private void drawCores(PoseStack pose, VertexConsumer vertices, Pose motion) {
        for (String name : List.of("front_core", "back_core")) {
            Joint joint = JOINTS.get(name);
            pose.pushPose();
            apply(name, motion, pose);
            Vec3 center = new Vec3(joint.pivotX() / 16, joint.pivotY() / 16, joint.pivotZ() / 16);
            double side = name.equals("front_core") ? -1 : 1;
            center = center.add(0, 0, side * 0.035);
            double pulse = 0.96 + 0.04 * Math.sin(motion.clock() * 0.25);
            double radius = (0.072 + motion.open() * 0.09) * pulse;
            ring(pose, vertices, center, radius, 0.01, motion.blink() ? VIOLET : IVORY,
                    motion.glow(), motion.clock() * 0.08 * side, true);
            ring(pose, vertices, center, radius * 1.55, 0.006, VIOLET,
                    motion.open() * 0.55F, -motion.clock() * 0.05 * side, true);
            float flare = motion.recoil();
            ring(pose, vertices, center, 0.2 + flare * 0.25, 0.012 * (1 - flare * 0.7), IVORY,
                    flare * 0.55F, motion.clock() * 0.12, false);
            pose.popPose();
        }
    }

    private void drawCharge(PoseStack pose, VertexConsumer vertices, Pose motion) {
        if (motion.charge() <= 0) return;
        pose.pushPose();
        apply("handle", motion, pose);
        for (int mote = 0; mote < 8; mote++) {
            double travel = (motion.clock() * 0.075 + mote / 8.0) % 1;
            double angle = travel * Math.PI * 4 + mote * 1.2;
            double radius = 0.2 * (1 - travel) + 0.045;
            Vec3 center = new Vec3(0.5 + Math.cos(angle) * radius, 0.35 + travel * 1.24,
                    0.5 + Math.sin(angle) * radius);
            float alpha = motion.charge() * (float) Math.sin(Math.PI * travel);
            double size = 0.008 + 0.014 * motion.charge();
            quad(pose, vertices, center.add(-size, 0, 0), center.add(0, size * 2, 0),
                    center.add(size, 0, 0), center.add(0, -size * 2, 0), mote % 2 == 0 ? IVORY : VIOLET, alpha);
        }
        pose.popPose();
    }

    private static void ring(PoseStack pose, VertexConsumer vertices, Vec3 center, double radius, double width,
                             int color, float alpha, double rotation, boolean broken) {
        if (alpha <= 0.001F) return;
        for (int segment = 0; segment < 40; segment++) {
            if (broken && segment % 10 >= 7) continue;
            double first = rotation + segment * Math.PI / 20;
            double second = rotation + (segment + 1) * Math.PI / 20;
            Vec3 direction = new Vec3(Math.cos(first), Math.sin(first), 0);
            Vec3 next = new Vec3(Math.cos(second), Math.sin(second), 0);
            quad(pose, vertices, center.add(direction.scale(radius + width)), center.add(next.scale(radius + width)),
                    center.add(next.scale(radius - width)), center.add(direction.scale(radius - width)), color, alpha);
        }
    }

    private static void quad(PoseStack pose, VertexConsumer vertices, Vec3 first, Vec3 second, Vec3 third, Vec3 fourth,
                             int color, float alpha) {
        vertex(pose, vertices, first, color, alpha);
        vertex(pose, vertices, second, color, alpha);
        vertex(pose, vertices, third, color, alpha);
        vertex(pose, vertices, fourth, color, alpha);
    }

    private static void vertex(PoseStack pose, VertexConsumer vertices, Vec3 point, int color, float alpha) {
        vertices.vertex(pose.last().pose(), (float) point.x, (float) point.y, (float) point.z)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255,
                        Math.round(DandanScytheMotionRules.bounded(alpha) * 255)).endVertex();
    }
}
