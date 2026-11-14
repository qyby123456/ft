/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ModelEvent$RegisterAdditional
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.render.DandanScytheGlow;
import net.farmingtales.ftboss.util.DandanScytheMotionRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
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

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class DandanScytheRenderer
extends BlockEntityWithoutLevelRenderer {
    private static final ThreadLocal<Prepared> PREPARED = new ThreadLocal();
    private static final Map<String, DandanScytheMotionRules.Joint> JOINTS = new LinkedHashMap<String, DandanScytheMotionRules.Joint>();
    private static final int IVORY = 16773849;
    private static final int VIOLET = 12297215;
    private static final double[][] EDGE = new double[][]{{9.5, 23.9}, {4.5, 25.1}, {0.5, 24.4}, {-3.5, 22.6}, {-6.5, 20.1}, {-8.5, 17.4}, {-10.75, 13.9}, {-11.8, 12.4}};
    private final ModelPart bone = new ModelPart(List.of(), Map.of());

    public DandanScytheRenderer() {
        super(Minecraft.m_91087_().m_167982_(), Minecraft.m_91087_().m_167973_());
    }

    public static void prepare(ItemStack stack, DandanScytheMotionRules.Pose motion) {
        PREPARED.set(new Prepared(stack, motion));
    }

    public static void clearPrepared() {
        PREPARED.remove();
    }

    private static ResourceLocation model(String name) {
        return new ResourceLocation("ftboss", "item/dandan_rig/" + name);
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        JOINTS.keySet().forEach(name -> event.register(DandanScytheRenderer.model(name)));
    }

    public void m_108829_(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Prepared prepared = PREPARED.get();
        DandanScytheRenderer.clearPrepared();
        boolean held = context.m_269069_() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        DandanScytheMotionRules.Pose motion = held && prepared != null && prepared.stack == stack ? prepared.motion : DandanScytheMotionRules.sample(0.0f, 0.0f, -1.0f, false, 0.0f);
        Minecraft client = Minecraft.m_91087_();
        VertexConsumer solid = ItemRenderer.m_115222_((MultiBufferSource)buffers, (RenderType)RenderType.m_110458_((ResourceLocation)TextureAtlas.f_118259_), (boolean)true, (boolean)stack.m_41790_());
        for (DandanScytheMotionRules.Joint joint : JOINTS.values()) {
            pose.m_85836_();
            this.apply(joint.name(), motion, pose);
            client.m_91291_().m_115189_(client.m_91304_().getModel(DandanScytheRenderer.model(joint.name())), stack, joint.name().endsWith("core") ? 0xF000F0 : light, overlay, pose, solid);
            pose.m_85849_();
        }
        if (!held) {
            return;
        }
        VertexConsumer glow = buffers.m_6299_(DandanScytheGlow.TYPE);
        this.drawCores(pose, glow, motion);
        this.drawEdge(pose, glow, motion);
        this.drawCharge(pose, glow, motion);
    }

    private void apply(String name, DandanScytheMotionRules.Pose motion, PoseStack pose) {
        DandanScytheMotionRules.Joint joint = JOINTS.get(name);
        if (!joint.parent().isEmpty()) {
            this.apply(joint.parent(), motion, pose);
        }
        DandanScytheMotionRules.Bone values = DandanScytheMotionRules.bone(name, motion);
        this.bone.m_104227_(joint.pivotX() + values.moveX(), joint.pivotY() + values.moveY(), joint.pivotZ() + values.moveZ());
        this.bone.f_104203_ = 0.0f;
        this.bone.f_104204_ = 0.0f;
        this.bone.f_104205_ = (float)Math.toRadians(values.rotateZ());
        this.bone.f_233553_ = values.scale();
        this.bone.f_233554_ = values.scale();
        this.bone.f_233555_ = values.scale();
        this.bone.m_104299_(pose);
        pose.m_252880_(-joint.pivotX() / 16.0f, -joint.pivotY() / 16.0f, -joint.pivotZ() / 16.0f);
    }

    private Vec3 edgePoint(double[] point, DandanScytheMotionRules.Pose motion, float lag) {
        PoseStack local = new PoseStack();
        local.m_85837_(0.5, 0.25, 0.5);
        local.m_252781_(Axis.f_252403_.m_252977_(lag));
        local.m_85837_(-0.5, -0.25, -0.5);
        this.apply(point[0] < -7.0 ? "blade_tip" : (point[0] < 0.0 ? "blade_mid" : "blade_base"), motion, local);
        Vector3f transformed = local.m_85850_().m_252922_().transformPosition((float)point[0] / 16.0f, (float)point[1] / 16.0f, 0.5f, new Vector3f());
        return new Vec3((double)transformed.x, (double)transformed.y, (double)transformed.z);
    }

    private void drawEdge(PoseStack pose, VertexConsumer vertices, DandanScytheMotionRules.Pose motion) {
        float strength = motion.trail();
        for (int layer = 4; layer >= 0; --layer) {
            float alpha;
            float start = (float)layer * (3.0f + 9.0f * strength);
            float end = start + (layer == 0 ? 0.7f : 2.0f + 5.0f * strength);
            float f = alpha = layer == 0 ? 0.18f + motion.glow() * 0.48f : strength * 0.24f / (float)layer;
            if (alpha <= 0.001f) continue;
            for (int segment = 0; segment < EDGE.length - 1; ++segment) {
                DandanScytheRenderer.quad(pose, vertices, this.edgePoint(EDGE[segment], motion, start), this.edgePoint(EDGE[segment + 1], motion, start), this.edgePoint(EDGE[segment + 1], motion, end), this.edgePoint(EDGE[segment], motion, end), layer % 2 == 0 ? 16773849 : 12297215, alpha);
            }
        }
    }

    private void drawCores(PoseStack pose, VertexConsumer vertices, DandanScytheMotionRules.Pose motion) {
        for (String name : List.of("front_core", "back_core")) {
            DandanScytheMotionRules.Joint joint = JOINTS.get(name);
            pose.m_85836_();
            this.apply(name, motion, pose);
            Vec3 center = new Vec3((double)(joint.pivotX() / 16.0f), (double)(joint.pivotY() / 16.0f), (double)(joint.pivotZ() / 16.0f));
            double side = name.equals("front_core") ? -1.0 : 1.0;
            center = center.m_82520_(0.0, 0.0, side * 0.035);
            double pulse = 0.96 + 0.04 * Math.sin((double)motion.clock() * 0.25);
            double radius = (0.072 + (double)motion.open() * 0.09) * pulse;
            DandanScytheRenderer.ring(pose, vertices, center, radius, 0.01, motion.blink() ? 12297215 : 16773849, motion.glow(), (double)motion.clock() * 0.08 * side, true);
            DandanScytheRenderer.ring(pose, vertices, center, radius * 1.55, 0.006, 12297215, motion.open() * 0.55f, (double)(-motion.clock()) * 0.05 * side, true);
            float flare = motion.recoil();
            DandanScytheRenderer.ring(pose, vertices, center, 0.2 + (double)flare * 0.25, 0.012 * (1.0 - (double)flare * 0.7), 16773849, flare * 0.55f, (double)motion.clock() * 0.12, false);
            pose.m_85849_();
        }
    }

    private void drawCharge(PoseStack pose, VertexConsumer vertices, DandanScytheMotionRules.Pose motion) {
        if (motion.charge() <= 0.0f) {
            return;
        }
        pose.m_85836_();
        this.apply("handle", motion, pose);
        for (int mote = 0; mote < 8; ++mote) {
            double travel = ((double)motion.clock() * 0.075 + (double)mote / 8.0) % 1.0;
            double angle = travel * Math.PI * 4.0 + (double)mote * 1.2;
            double radius = 0.2 * (1.0 - travel) + 0.045;
            Vec3 center = new Vec3(0.5 + Math.cos(angle) * radius, 0.35 + travel * 1.24, 0.5 + Math.sin(angle) * radius);
            float alpha = motion.charge() * (float)Math.sin(Math.PI * travel);
            double size = 0.008 + 0.014 * (double)motion.charge();
            DandanScytheRenderer.quad(pose, vertices, center.m_82520_(-size, 0.0, 0.0), center.m_82520_(0.0, size * 2.0, 0.0), center.m_82520_(size, 0.0, 0.0), center.m_82520_(0.0, -size * 2.0, 0.0), mote % 2 == 0 ? 16773849 : 12297215, alpha);
        }
        pose.m_85849_();
    }

    private static void ring(PoseStack pose, VertexConsumer vertices, Vec3 center, double radius, double width, int color, float alpha, double rotation, boolean broken) {
        if (alpha <= 0.001f) {
            return;
        }
        for (int segment = 0; segment < 40; ++segment) {
            if (broken && segment % 10 >= 7) continue;
            double first = rotation + (double)segment * Math.PI / 20.0;
            double second = rotation + (double)(segment + 1) * Math.PI / 20.0;
            Vec3 direction = new Vec3(Math.cos(first), Math.sin(first), 0.0);
            Vec3 next = new Vec3(Math.cos(second), Math.sin(second), 0.0);
            DandanScytheRenderer.quad(pose, vertices, center.m_82549_(direction.m_82490_(radius + width)), center.m_82549_(next.m_82490_(radius + width)), center.m_82549_(next.m_82490_(radius - width)), center.m_82549_(direction.m_82490_(radius - width)), color, alpha);
        }
    }

    private static void quad(PoseStack pose, VertexConsumer vertices, Vec3 first, Vec3 second, Vec3 third, Vec3 fourth, int color, float alpha) {
        DandanScytheRenderer.vertex(pose, vertices, first, color, alpha);
        DandanScytheRenderer.vertex(pose, vertices, second, color, alpha);
        DandanScytheRenderer.vertex(pose, vertices, third, color, alpha);
        DandanScytheRenderer.vertex(pose, vertices, fourth, color, alpha);
    }

    private static void vertex(PoseStack pose, VertexConsumer vertices, Vec3 point, int color, float alpha) {
        vertices.m_252986_(pose.m_85850_().m_252922_(), (float)point.f_82479_, (float)point.f_82480_, (float)point.f_82481_).m_6122_(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, Math.round(DandanScytheMotionRules.bounded(alpha) * 255.0f)).m_5752_();
    }

    static {
        DandanScytheMotionRules.JOINTS.forEach(joint -> JOINTS.put(joint.name(), (DandanScytheMotionRules.Joint)joint));
    }

    private record Prepared(ItemStack stack, DandanScytheMotionRules.Pose motion) {
    }
}

