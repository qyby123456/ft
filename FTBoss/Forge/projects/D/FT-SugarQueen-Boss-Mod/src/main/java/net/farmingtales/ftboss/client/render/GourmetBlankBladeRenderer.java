package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.weapons.GourmetBlankBladeItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** Two-sided long-odachi renderer with cumulative 1/15/30-socket geometry and emissive rupture stages. */
public final class GourmetBlankBladeRenderer extends GeoItemRenderer<GourmetBlankBladeItem> {
    private static final float SOCKET_COLOR_BLEND = 0.15F;
    private int renderedSockets;

    public GourmetBlankBladeRenderer() {
        super(new Model());
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public RenderType getRenderType(GourmetBlankBladeItem animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        // The modeled fuller, golden hamon and evolution crests are deliberately readable from both faces.
        // NO_CULL also protects the shallow socket inlays from disappearing at hand-view angles.
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public void preRender(PoseStack poseStack, GourmetBlankBladeItem item, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
        if (!isReRender) {
            ItemStack stack = getCurrentItemStack();
            renderedSockets = stack == null || stack.isEmpty() ? 0 : FoodWeaponEvolution.socketCount(stack);
        }
        super.preRender(poseStack, item, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, GourmetBlankBladeItem animatable, GeoBone bone,
                                  RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                                  boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha) {
        String name = bone.getName();
        int requiredSockets = socketStageRequirement(name);
        if (requiredSockets > renderedSockets) return;
        if (name.startsWith("sigil_")) {
            try {
                int required = Integer.parseInt(name.substring("sigil_".length()));
                if (FoodWeaponEvolution.evolutionLevel(getCurrentItemStack()) < required) return;
            } catch (NumberFormatException ignored) {
                // sigil_orbit is the animated parent, not a gated crest.
            }
        }
        if (name.startsWith("socket_fill_")) {
            int color;
            try {
                int slot = Integer.parseInt(name.substring("socket_fill_".length()));
                color = FoodWeaponEvolution.socketVisualColor(getCurrentItemStack(), slot);
            } catch (NumberFormatException ignored) {
                return;
            }
            if (color < 0) return;
            // Keep the category readable without turning the royal purple-gold blade into a rainbow.
            red *= subduedSocketChannel((color >> 16) & 0xFF, 0.78F);
            green *= subduedSocketChannel((color >> 8) & 0xFF, 0.69F);
            blue *= subduedSocketChannel(color & 0xFF, 0.48F);
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer,
                isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private static int socketStageRequirement(String boneName) {
        if (boneName.startsWith("socket_stage_30")) return 30;
        if (boneName.startsWith("socket_stage_15")) return 15;
        if (boneName.startsWith("socket_stage_01")) return 1;
        return 0;
    }

    private static float subduedSocketChannel(int channel, float agedBrass) {
        return agedBrass * (1.0F - SOCKET_COLOR_BLEND) + channel / 255.0F * SOCKET_COLOR_BLEND;
    }

    private static final class Model extends GeoModel<GourmetBlankBladeItem> {
        private static final ResourceLocation GEOMETRY =
                new ResourceLocation("ftboss", "geo/gourmet_blank_blade.geo.json");
        private static final ResourceLocation TEXTURE =
                new ResourceLocation("ftboss", "textures/item/gourmet_blank_blade.png");
        private static final ResourceLocation ANIMATION =
                new ResourceLocation("ftboss", "animations/gourmet_blank_blade.animation.json");

        @Override
        public ResourceLocation getModelResource(GourmetBlankBladeItem item) { return GEOMETRY; }
        @Override
        public ResourceLocation getTextureResource(GourmetBlankBladeItem item) { return TEXTURE; }
        @Override
        public ResourceLocation getAnimationResource(GourmetBlankBladeItem item) { return ANIMATION; }
    }
}
