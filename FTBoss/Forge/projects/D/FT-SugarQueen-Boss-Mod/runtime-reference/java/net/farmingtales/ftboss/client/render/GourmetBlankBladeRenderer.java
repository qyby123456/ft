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
 *  net.minecraft.world.item.ItemStack
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoItemRenderer
 *  software.bernie.geckolib.renderer.GeoRenderer
 *  software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.weapons.GourmetBlankBladeItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class GourmetBlankBladeRenderer
extends GeoItemRenderer<GourmetBlankBladeItem> {
    private static final float SOCKET_COLOR_BLEND = 0.15f;
    private int renderedSockets;

    public GourmetBlankBladeRenderer() {
        super((GeoModel)new Model());
        this.addRenderLayer((GeoRenderLayer)new AutoGlowingGeoLayer((GeoRenderer)this));
    }

    public RenderType getRenderType(GourmetBlankBladeItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.m_110458_((ResourceLocation)texture);
    }

    public void preRender(PoseStack poseStack, GourmetBlankBladeItem item, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (!isReRender) {
            ItemStack stack = this.getCurrentItemStack();
            this.renderedSockets = stack == null || stack.m_41619_() ? 0 : FoodWeaponEvolution.socketCount(stack);
        }
        super.preRender(poseStack, (Item)item, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void renderRecursively(PoseStack poseStack, GourmetBlankBladeItem animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        String name = bone.getName();
        int requiredSockets = GourmetBlankBladeRenderer.socketStageRequirement(name);
        if (requiredSockets > this.renderedSockets) {
            return;
        }
        if (name.startsWith("sigil_")) {
            try {
                int required = Integer.parseInt(name.substring("sigil_".length()));
                if (FoodWeaponEvolution.evolutionLevel(this.getCurrentItemStack()) < required) {
                    return;
                }
            }
            catch (NumberFormatException required) {
                // empty catch block
            }
        }
        if (name.startsWith("socket_fill_")) {
            int color;
            try {
                int slot = Integer.parseInt(name.substring("socket_fill_".length()));
                color = FoodWeaponEvolution.socketVisualColor(this.getCurrentItemStack(), slot);
            }
            catch (NumberFormatException ignored) {
                return;
            }
            if (color < 0) {
                return;
            }
            red *= GourmetBlankBladeRenderer.subduedSocketChannel(color >> 16 & 0xFF, 0.78f);
            green *= GourmetBlankBladeRenderer.subduedSocketChannel(color >> 8 & 0xFF, 0.69f);
            blue *= GourmetBlankBladeRenderer.subduedSocketChannel(color & 0xFF, 0.48f);
        }
        super.renderRecursively(poseStack, (Item)animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private static int socketStageRequirement(String boneName) {
        if (boneName.startsWith("socket_stage_30")) {
            return 30;
        }
        if (boneName.startsWith("socket_stage_15")) {
            return 15;
        }
        if (boneName.startsWith("socket_stage_01")) {
            return 1;
        }
        return 0;
    }

    private static float subduedSocketChannel(int channel, float agedBrass) {
        return agedBrass * 0.85f + (float)channel / 255.0f * 0.15f;
    }

    private static final class Model
    extends GeoModel<GourmetBlankBladeItem> {
        private static final ResourceLocation GEOMETRY = new ResourceLocation("ftboss", "geo/gourmet_blank_blade.geo.json");
        private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/item/gourmet_blank_blade.png");
        private static final ResourceLocation ANIMATION = new ResourceLocation("ftboss", "animations/gourmet_blank_blade.animation.json");

        private Model() {
        }

        public ResourceLocation getModelResource(GourmetBlankBladeItem item) {
            return GEOMETRY;
        }

        public ResourceLocation getTextureResource(GourmetBlankBladeItem item) {
            return TEXTURE;
        }

        public ResourceLocation getAnimationResource(GourmetBlankBladeItem item) {
            return ANIMATION;
        }
    }
}

