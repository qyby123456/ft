/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoItemRenderer
 *  software.bernie.geckolib.renderer.GeoRenderer
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.farmingtales.ftboss.client.render.CleaverBloodVisualState;
import net.farmingtales.ftboss.client.render.CleaverMotionData;
import net.farmingtales.ftboss.item.weapons.BlackCleaverItem;
import net.farmingtales.ftboss.item.weapons.CleaverSkillController;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class BlackCleaverGeoRenderer
extends GeoItemRenderer<BlackCleaverItem> {
    private static final float THIRD_PERSON_GRIP_Y = 7.5f;
    private CleaverMotionData.Frame currentFrame;

    public BlackCleaverGeoRenderer() {
        super((GeoModel)new Model());
        this.addRenderLayer(new BloodGlowLayer(this, "core", "black_cleaver_core_emissive.png"));
        this.addRenderLayer(new BloodGlowLayer(this, "rune", "black_cleaver_rune_emissive.png"));
        this.addRenderLayer(new BloodGlowLayer(this, "edge", "black_cleaver_edge_emissive.png"));
        this.addRenderLayer(new BloodGlowLayer(this, "seam", "black_cleaver_blood_seam_emissive.png"));
    }

    private boolean isHeldContext() {
        ItemDisplayContext context = this.renderPerspective;
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    public void preRender(PoseStack poseStack, BlackCleaverItem item, BakedGeoModel model, MultiBufferSource buffers, VertexConsumer buffer, boolean isReRender, float partialTick, int light, int overlay, float red, float green, float blue, float alpha) {
        if (!isReRender) {
            this.currentFrame = null;
        }
        super.preRender(poseStack, (Item)item, model, buffers, buffer, isReRender, partialTick, light, overlay, red, green, blue, alpha);
        if (!isReRender) {
            poseStack.m_85837_(0.0, -0.01, 0.0);
        }
    }

    private CleaverMotionData.Frame frame(float partialTick) {
        CleaverSkillController candidate;
        if (this.currentFrame != null) {
            return this.currentFrame;
        }
        CleaverMotionData library = CleaverBloodVisualState.data();
        if (library == null) {
            this.currentFrame = new CleaverMotionData.Frame(Map.of(), Map.of());
            return this.currentFrame;
        }
        ItemStack stack = this.getCurrentItemStack();
        if (stack == null || stack.m_41619_() || !this.isHeldContext()) {
            this.currentFrame = library.sample("idle", 0.0);
            return this.currentFrame;
        }
        CleaverBloodVisualState.observe(stack);
        long key = CleaverBloodVisualState.keyOf(stack);
        AnimationController controller = (AnimationController)((BlackCleaverItem)this.getAnimatable()).getAnimatableInstanceCache().getManagerForId(key).getAnimationControllers().get("cleaver_skill");
        CleaverSkillController skill = controller instanceof CleaverSkillController ? (candidate = (CleaverSkillController)controller) : null;
        String action = skill == null ? null : skill.clipName();
        this.currentFrame = CleaverBloodVisualState.frameOf(key, partialTick, action != null);
        if (action != null) {
            this.currentFrame = library.skill(this.currentFrame, action, skill.seconds(), CleaverBloodVisualState.amountOf(key, partialTick));
        }
        return this.currentFrame;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderRecursively(PoseStack poseStack, BlackCleaverItem item, GeoBone bone, RenderType type, MultiBufferSource buffers, VertexConsumer buffer, boolean isReRender, float partialTick, int light, int overlay, float red, float green, float blue, float alpha) {
        if (!CleaverMotionData.BONES.contains(bone.getName())) {
            super.renderRecursively(poseStack, (Item)item, bone, type, buffers, buffer, isReRender, partialTick, light, overlay, red, green, blue, alpha);
            return;
        }
        CleaverMotionData.Pose pose = this.frame(partialTick).bone(bone.getName());
        float rotationX = bone.getRotX();
        float rotationY = bone.getRotY();
        float rotationZ = bone.getRotZ();
        float scaleX = bone.getScaleX();
        float scaleY = bone.getScaleY();
        float scaleZ = bone.getScaleZ();
        float pivotY = bone.getPivotY();
        bone.setRotX((float)(-Math.toRadians(pose.rotation().axisX())));
        bone.setRotY((float)(-Math.toRadians(pose.rotation().axisY())));
        bone.setRotZ((float)Math.toRadians(pose.rotation().axisZ()));
        bone.setScaleX((float)pose.scale().axisX());
        bone.setScaleY((float)pose.scale().axisY());
        bone.setScaleZ((float)pose.scale().axisZ());
        if (bone.getName().equals("weapon") && (this.renderPerspective == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || this.renderPerspective == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {
            bone.setPivotY(0.0f);
        }
        try {
            super.renderRecursively(poseStack, (Item)item, bone, type, buffers, buffer, isReRender, partialTick, light, overlay, red, green, blue, alpha);
        }
        finally {
            bone.setPivotY(pivotY);
            bone.setRotX(rotationX);
            bone.setRotY(rotationY);
            bone.setRotZ(rotationZ);
            bone.setScaleX(scaleX);
            bone.setScaleY(scaleY);
            bone.setScaleZ(scaleZ);
        }
    }

    public static class Model
    extends GeoModel<BlackCleaverItem> {
        public ResourceLocation getModelResource(BlackCleaverItem item) {
            return new ResourceLocation("ftboss", "geo/black_cleaver.geo.json");
        }

        public ResourceLocation getAnimationResource(BlackCleaverItem item) {
            return new ResourceLocation("ftboss", "animations/black_cleaver.animation.json");
        }

        public ResourceLocation getTextureResource(BlackCleaverItem item) {
            return new ResourceLocation("ftboss", "textures/item/black_cleaver_geo.png");
        }
    }

    private static final class BloodGlowLayer
    extends GeoRenderLayer<BlackCleaverItem> {
        private final BlackCleaverGeoRenderer owner;
        private final String channel;
        private final ResourceLocation texture;

        BloodGlowLayer(BlackCleaverGeoRenderer owner, String channel, String filename) {
            super((GeoRenderer)owner);
            this.owner = owner;
            this.channel = channel;
            this.texture = new ResourceLocation("ftboss", "textures/item/" + filename);
        }

        public void render(PoseStack poseStack, BlackCleaverItem item, BakedGeoModel model, RenderType type, MultiBufferSource buffers, VertexConsumer buffer, float partialTick, int light, int overlay) {
            float strength = (float)Math.max(0.0, Math.min(1.0, this.owner.frame(partialTick).effect(this.channel)));
            if (strength <= 0.002f) {
                return;
            }
            RenderType glow = RenderType.m_110488_((ResourceLocation)this.texture);
            this.owner.reRender(model, poseStack, buffers, (GeoAnimatable)item, glow, buffers.m_6299_(glow), partialTick, 0xF000F0, overlay, strength, strength, strength, 1.0f);
        }
    }
}

