package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.item.weapons.BlackCleaverItem;
import net.farmingtales.ftboss.item.weapons.CleaverSkillController;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Map;

/** 黑切厚刃血怒渲染；2026-09-16 绝对姿态替代旧角度叠加，四层发光与技能共用采样时间，绘制后还原共享模型。 */
public class BlackCleaverGeoRenderer extends GeoItemRenderer<BlackCleaverItem> {
    private static final float THIRD_PERSON_GRIP_Y = 7.5f;
    private CleaverMotionData.Frame currentFrame;

    public BlackCleaverGeoRenderer() {
        super(new Model());
        addRenderLayer(new BloodGlowLayer(this, "core", "black_cleaver_core_emissive.png"));
        addRenderLayer(new BloodGlowLayer(this, "rune", "black_cleaver_rune_emissive.png"));
        addRenderLayer(new BloodGlowLayer(this, "edge", "black_cleaver_edge_emissive.png"));
        addRenderLayer(new BloodGlowLayer(this, "seam", "black_cleaver_blood_seam_emissive.png"));
    }

    private boolean isHeldContext() {
        ItemDisplayContext context = renderPerspective;
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    @Override
    public void preRender(PoseStack poseStack, BlackCleaverItem item, BakedGeoModel model, MultiBufferSource buffers,
                          VertexConsumer buffer, boolean isReRender, float partialTick, int light, int overlay,
                          float red, float green, float blue, float alpha) {
        if (!isReRender) currentFrame = null;
        super.preRender(poseStack, item, model, buffers, buffer, isReRender, partialTick, light, overlay, red, green, blue, alpha);
        if (!isReRender) poseStack.translate(0, -0.01, 0);
    }

    private CleaverMotionData.Frame frame(float partialTick) {
        if (currentFrame != null) return currentFrame;
        CleaverMotionData library = CleaverBloodVisualState.data();
        if (library == null) return currentFrame = new CleaverMotionData.Frame(Map.of(), Map.of());
        ItemStack stack = getCurrentItemStack();
        if (stack == null || stack.isEmpty() || !isHeldContext()) return currentFrame = library.sample("idle", 0);
        CleaverBloodVisualState.observe(stack);
        long key = CleaverBloodVisualState.keyOf(stack);
        AnimationController<?> controller = getAnimatable().getAnimatableInstanceCache().getManagerForId(key).getAnimationControllers().get(CleaverSkillController.NAME);
        CleaverSkillController skill = controller instanceof CleaverSkillController candidate ? candidate : null;
        String action = skill == null ? null : skill.clipName();
        currentFrame = CleaverBloodVisualState.frameOf(key, partialTick, action != null);
        if (action != null) currentFrame = library.skill(currentFrame, action, skill.seconds(), CleaverBloodVisualState.amountOf(key, partialTick));
        return currentFrame;
    }

    @Override
    public void renderRecursively(PoseStack poseStack, BlackCleaverItem item, GeoBone bone, RenderType type,
                                  MultiBufferSource buffers, VertexConsumer buffer, boolean isReRender, float partialTick,
                                  int light, int overlay, float red, float green, float blue, float alpha) {
        if (!CleaverMotionData.BONES.contains(bone.getName())) {
            super.renderRecursively(poseStack, item, bone, type, buffers, buffer, isReRender, partialTick, light, overlay, red, green, blue, alpha);
            return;
        }
        CleaverMotionData.Pose pose = frame(partialTick).bone(bone.getName());
        float rotationX = bone.getRotX();
        float rotationY = bone.getRotY();
        float rotationZ = bone.getRotZ();
        float scaleX = bone.getScaleX();
        float scaleY = bone.getScaleY();
        float scaleZ = bone.getScaleZ();
        float pivotY = bone.getPivotY();
        bone.setRotX((float) -Math.toRadians(pose.rotation().axisX()));
        bone.setRotY((float) -Math.toRadians(pose.rotation().axisY()));
        bone.setRotZ((float) Math.toRadians(pose.rotation().axisZ()));
        bone.setScaleX((float) pose.scale().axisX());
        bone.setScaleY((float) pose.scale().axisY());
        bone.setScaleZ((float) pose.scale().axisZ());
        if (bone.getName().equals("weapon") && (renderPerspective == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || renderPerspective == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {
            bone.setPivotY(THIRD_PERSON_GRIP_Y);
        }
        try {
            super.renderRecursively(poseStack, item, bone, type, buffers, buffer, isReRender, partialTick, light, overlay, red, green, blue, alpha);
        } finally {
            bone.setPivotY(pivotY);
            bone.setRotX(rotationX);
            bone.setRotY(rotationY);
            bone.setRotZ(rotationZ);
            bone.setScaleX(scaleX);
            bone.setScaleY(scaleY);
            bone.setScaleZ(scaleZ);
        }
    }

    public static class Model extends GeoModel<BlackCleaverItem> {
        @Override
        public ResourceLocation getModelResource(BlackCleaverItem item) {
            return new ResourceLocation("ftboss", "geo/black_cleaver.geo.json");
        }

        @Override
        public ResourceLocation getAnimationResource(BlackCleaverItem item) {
            return new ResourceLocation("ftboss", "animations/black_cleaver.animation.json");
        }

        @Override
        public ResourceLocation getTextureResource(BlackCleaverItem item) {
            return new ResourceLocation("ftboss", "textures/item/black_cleaver_geo.png");
        }
    }

    private static final class BloodGlowLayer extends GeoRenderLayer<BlackCleaverItem> {
        private final BlackCleaverGeoRenderer owner;
        private final String channel;
        private final ResourceLocation texture;

        BloodGlowLayer(BlackCleaverGeoRenderer owner, String channel, String filename) {
            super(owner);
            this.owner = owner;
            this.channel = channel;
            texture = new ResourceLocation("ftboss", "textures/item/" + filename);
        }

        @Override
        public void render(PoseStack poseStack, BlackCleaverItem item, BakedGeoModel model, RenderType type,
                           MultiBufferSource buffers, VertexConsumer buffer, float partialTick, int light, int overlay) {
            float strength = (float) Math.max(0, Math.min(1, owner.frame(partialTick).effect(channel)));
            if (strength <= 0.002f) return;
            RenderType glow = RenderType.eyes(texture);
            owner.reRender(model, poseStack, buffers, item, glow, buffers.getBuffer(glow), partialTick,
                    LightTexture.FULL_BRIGHT, overlay, strength, strength, strength, 1);
        }
    }
}
