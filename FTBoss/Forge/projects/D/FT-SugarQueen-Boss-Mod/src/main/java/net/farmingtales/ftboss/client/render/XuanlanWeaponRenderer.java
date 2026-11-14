package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.XuanlanMoonWeaponItem;
import net.farmingtales.ftboss.util.XuanlanMotionRules;
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

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class XuanlanWeaponRenderer extends BlockEntityWithoutLevelRenderer {
    private record Prepared(ItemStack stack, XuanlanMotionRules.Pose motion) {}
    private record Joint(String name, ResourceLocation model, float pivotX, float pivotY, float pivotZ, ModelPart bone) {
        Joint(String name, float x, float y, float z) {
            this(name, new ResourceLocation(FTBossMod.MOD_ID, "item/xuanlan_rig/" + name), x, y, z, new ModelPart(List.of(), Map.of()));
        }
    }
    private static final ThreadLocal<Prepared> PREPARED = new ThreadLocal<>();
    private static final List<Joint> GUN = List.of(new Joint("gun_body", 1, 6, 8), new Joint("gun_stock", 1, 5, 8),
            new Joint("gun_upper", 10.75F, 8.1F, 8), new Joint("gun_lower", 10.75F, 8.1F, 8), new Joint("gun_core", 10.75F, 8.1F, 8));
    private static final List<Joint> WHEEL = List.of(new Joint("wheel_hub", 8, 8, 8), new Joint("wheel_upper", 8, 8, 8),
            new Joint("wheel_lower", 8, 8, 8), new Joint("wheel_locks", 8, 8, 8));
    private static final Vec3 CENTER = new Vec3(0.5, 0.5, 0.5);
    private static final Vec3 MUZZLE = new Vec3(28.5 / 16, 8.1 / 16, 0.5);
    private static final Vec3 UP = new Vec3(0, 1, 0);
    private static final Vec3 RIGHT = new Vec3(1, 0, 0);
    private static final Vec3 DEPTH = new Vec3(0, 0, 1);

    public XuanlanWeaponRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static void prepare(ItemStack stack, XuanlanMotionRules.Pose motion) { PREPARED.set(new Prepared(stack, motion)); }
    public static void clearPrepared() { PREPARED.remove(); }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        for (Joint joint : GUN) event.register(joint.model);
        for (Joint joint : WHEEL) event.register(joint.model);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Prepared prepared = PREPARED.get();
        clearPrepared();
        XuanlanMotionRules.Pose motion = prepared != null && prepared.stack == stack ? prepared.motion
                : XuanlanMotionRules.sample(0, -1, -1, -1, 0);
        renderRig(stack, XuanlanMoonWeaponItem.isWhite(stack), motion, pose, buffers, light, overlay);
    }

    public static void renderRig(ItemStack stack, boolean white, XuanlanMotionRules.Pose motion,
                                 PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var client = Minecraft.getInstance();
        var solid = ItemRenderer.getFoilBufferDirect(buffers, RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS), true, stack.hasFoil());
        for (Joint joint : white ? WHEEL : GUN) {
            pose.pushPose();
            applyJoint(joint, motion, pose);
            boolean luminous = joint.name.equals("gun_core") || joint.name.equals("wheel_locks");
            client.getItemRenderer().renderModelLists(client.getModelManager().getModel(joint.model), stack,
                    luminous ? LightTexture.FULL_BRIGHT : light, overlay, pose, solid);
            pose.popPose();
        }
        VertexConsumer glow = buffers.getBuffer(XuanlanGlow.TYPE);
        if (white) {
            float pulse = motion.catchPulse();
            XuanlanWeaponVisual.ring(pose, glow, CENTER, RIGHT, UP, 0.17 + 0.025 * pulse, 0.007 + 0.006 * pulse,
                    0x67FFCC, 0.25F + pulse * 0.6F, motion.clock() * 0.05, true);
            if (pulse > 0) XuanlanWeaponVisual.ring(pose, glow, CENTER, RIGHT, UP, 0.18 + (1 - pulse) * 0.35,
                    0.01, 0xDBEEFF, pulse * 0.55F, -motion.clock() * 0.12, true);
        } else {
            pose.pushPose();
            applyJoint(GUN.get(4), motion, pose);
            float charge = motion.charge(), flash = motion.recoil();
            float strength = 0.15F + 0.65F * charge + 0.2F * flash;
            XuanlanWeaponVisual.ring(pose, glow, MUZZLE, DEPTH, UP, 0.045 + 0.065 * charge + 0.12 * flash,
                    0.008 + 0.018 * flash, 0x67FFCC, strength, motion.clock() * 0.16, true);
            if (charge > 0 || flash > 0) {
                XuanlanWeaponVisual.ring(pose, glow, MUZZLE.add(0.018, 0, 0), DEPTH, UP, 0.025 + 0.018 * charge,
                        0.006, 0xEFFFFF, strength, -motion.clock() * 0.2, false);
                for (int index = 0; index < 3; index++) {
                    double travel = ((motion.clock() * 0.08 + index / 3.0) % 1 + 1) % 1;
                    Vec3 center = new Vec3((12 + travel * 15) / 16, 8.1 / 16, 0.5);
                    XuanlanWeaponVisual.ring(pose, glow, center, DEPTH, UP, 0.045 * (1 - travel) + 0.025,
                            0.007, 0x67FFCC, charge * (float) Math.sin(travel * Math.PI) * 0.7F, motion.clock() * 0.08, true);
                }
            }
            pose.popPose();
        }
    }

    private static void applyJoint(Joint joint, XuanlanMotionRules.Pose motion, PoseStack pose) {
        XuanlanMotionRules.Bone values = XuanlanMotionRules.bone(joint.name, motion);
        ModelPart bone = joint.bone;
        bone.setPos(joint.pivotX + values.moveX(), joint.pivotY + values.moveY(), joint.pivotZ);
        bone.xRot = 0;
        bone.yRot = 0;
        bone.zRot = (float) Math.toRadians(values.rotateZ());
        bone.xScale = values.scale();
        bone.yScale = values.scale();
        bone.zScale = values.scale();
        bone.translateAndRotate(pose);
        pose.translate(-joint.pivotX / 16, -joint.pivotY / 16, -joint.pivotZ / 16);
    }
}
