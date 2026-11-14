/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
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
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.render.XuanlanGlow;
import net.farmingtales.ftboss.client.render.XuanlanWeaponVisual;
import net.farmingtales.ftboss.item.XuanlanMoonWeaponItem;
import net.farmingtales.ftboss.util.XuanlanMotionRules;
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

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class XuanlanWeaponRenderer
extends BlockEntityWithoutLevelRenderer {
    private static final ThreadLocal<Prepared> PREPARED = new ThreadLocal();
    private static final List<Joint> GUN = List.of(new Joint("gun_body", 1.0f, 6.0f, 8.0f), new Joint("gun_stock", 1.0f, 5.0f, 8.0f), new Joint("gun_upper", 10.75f, 8.1f, 8.0f), new Joint("gun_lower", 10.75f, 8.1f, 8.0f), new Joint("gun_core", 10.75f, 8.1f, 8.0f));
    private static final List<Joint> WHEEL = List.of(new Joint("wheel_hub", 8.0f, 8.0f, 8.0f), new Joint("wheel_upper", 8.0f, 8.0f, 8.0f), new Joint("wheel_lower", 8.0f, 8.0f, 8.0f), new Joint("wheel_locks", 8.0f, 8.0f, 8.0f));
    private static final Vec3 CENTER = new Vec3(0.5, 0.5, 0.5);
    private static final Vec3 MUZZLE = new Vec3(1.78125, 0.50625, 0.5);
    private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);
    private static final Vec3 RIGHT = new Vec3(1.0, 0.0, 0.0);
    private static final Vec3 DEPTH = new Vec3(0.0, 0.0, 1.0);

    public XuanlanWeaponRenderer() {
        super(Minecraft.m_91087_().m_167982_(), Minecraft.m_91087_().m_167973_());
    }

    public static void prepare(ItemStack stack, XuanlanMotionRules.Pose motion) {
        PREPARED.set(new Prepared(stack, motion));
    }

    public static void clearPrepared() {
        PREPARED.remove();
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        for (Joint joint : GUN) {
            event.register(joint.model);
        }
        for (Joint joint : WHEEL) {
            event.register(joint.model);
        }
    }

    public void m_108829_(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Prepared prepared = PREPARED.get();
        XuanlanWeaponRenderer.clearPrepared();
        XuanlanMotionRules.Pose motion = prepared != null && prepared.stack == stack ? prepared.motion : XuanlanMotionRules.sample(0.0f, -1.0f, -1.0f, -1.0f, 0.0f);
        XuanlanWeaponRenderer.renderRig(stack, XuanlanMoonWeaponItem.isWhite(stack), motion, pose, buffers, light, overlay);
    }

    public static void renderRig(ItemStack stack, boolean white, XuanlanMotionRules.Pose motion, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Minecraft client = Minecraft.m_91087_();
        VertexConsumer solid = ItemRenderer.m_115222_((MultiBufferSource)buffers, (RenderType)RenderType.m_110458_((ResourceLocation)TextureAtlas.f_118259_), (boolean)true, (boolean)stack.m_41790_());
        for (Joint joint : white ? WHEEL : GUN) {
            pose.m_85836_();
            XuanlanWeaponRenderer.applyJoint(joint, motion, pose);
            boolean luminous = joint.name.equals("gun_core") || joint.name.equals("wheel_locks");
            client.m_91291_().m_115189_(client.m_91304_().getModel(joint.model), stack, luminous ? 0xF000F0 : light, overlay, pose, solid);
            pose.m_85849_();
        }
        VertexConsumer glow = buffers.m_6299_(XuanlanGlow.TYPE);
        if (white) {
            float pulse = motion.catchPulse();
            XuanlanWeaponVisual.ring(pose, glow, CENTER, RIGHT, UP, 0.17 + 0.025 * (double)pulse, 0.007 + 0.006 * (double)pulse, 6815692, 0.25f + pulse * 0.6f, (double)motion.clock() * 0.05, true);
            if (pulse > 0.0f) {
                XuanlanWeaponVisual.ring(pose, glow, CENTER, RIGHT, UP, 0.18 + (double)(1.0f - pulse) * 0.35, 0.01, 14413567, pulse * 0.55f, (double)(-motion.clock()) * 0.12, true);
            }
        } else {
            pose.m_85836_();
            XuanlanWeaponRenderer.applyJoint(GUN.get(4), motion, pose);
            float charge = motion.charge();
            float flash = motion.recoil();
            float strength = 0.15f + 0.65f * charge + 0.2f * flash;
            XuanlanWeaponVisual.ring(pose, glow, MUZZLE, DEPTH, UP, 0.045 + 0.065 * (double)charge + 0.12 * (double)flash, 0.008 + 0.018 * (double)flash, 6815692, strength, (double)motion.clock() * 0.16, true);
            if (charge > 0.0f || flash > 0.0f) {
                XuanlanWeaponVisual.ring(pose, glow, MUZZLE.m_82520_(0.018, 0.0, 0.0), DEPTH, UP, 0.025 + 0.018 * (double)charge, 0.006, 0xEFFFFF, strength, (double)(-motion.clock()) * 0.2, false);
                for (int index = 0; index < 3; ++index) {
                    double travel = (((double)motion.clock() * 0.08 + (double)index / 3.0) % 1.0 + 1.0) % 1.0;
                    Vec3 center = new Vec3((12.0 + travel * 15.0) / 16.0, 0.50625, 0.5);
                    XuanlanWeaponVisual.ring(pose, glow, center, DEPTH, UP, 0.045 * (1.0 - travel) + 0.025, 0.007, 6815692, charge * (float)Math.sin(travel * Math.PI) * 0.7f, (double)motion.clock() * 0.08, true);
                }
            }
            pose.m_85849_();
        }
    }

    private static void applyJoint(Joint joint, XuanlanMotionRules.Pose motion, PoseStack pose) {
        XuanlanMotionRules.Bone values = XuanlanMotionRules.bone(joint.name, motion);
        ModelPart bone = joint.bone;
        bone.m_104227_(joint.pivotX + values.moveX(), joint.pivotY + values.moveY(), joint.pivotZ);
        bone.f_104203_ = 0.0f;
        bone.f_104204_ = 0.0f;
        bone.f_104205_ = (float)Math.toRadians(values.rotateZ());
        bone.f_233553_ = values.scale();
        bone.f_233554_ = values.scale();
        bone.f_233555_ = values.scale();
        bone.m_104299_(pose);
        pose.m_252880_(-joint.pivotX / 16.0f, -joint.pivotY / 16.0f, -joint.pivotZ / 16.0f);
    }

    private record Prepared(ItemStack stack, XuanlanMotionRules.Pose motion) {
    }

    private record Joint(String name, ResourceLocation model, float pivotX, float pivotY, float pivotZ, ModelPart bone) {
        Joint(String name, float x, float y, float z) {
            this(name, new ResourceLocation("ftboss", "item/xuanlan_rig/" + name), x, y, z, new ModelPart(List.of(), Map.of()));
        }
    }
}

