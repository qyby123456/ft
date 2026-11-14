/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemDisplayContext
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.entity.XueliKnifeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;

public final class XueliKnifeRenderer
extends EntityRenderer<XueliKnifeEntity> {
    private final ItemRenderer itemRenderer;

    public XueliKnifeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.m_174025_();
    }

    public void render(XueliKnifeEntity knife, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.m_85836_();
        poseStack.m_252781_(this.f_114476_.m_253208_());
        poseStack.m_252781_(Axis.f_252436_.m_252977_(180.0f));
        this.itemRenderer.m_269128_(knife.m_7846_(), ItemDisplayContext.GROUND, 0xF000F0, OverlayTexture.f_118083_, poseStack, buffers, knife.m_9236_(), knife.m_19879_());
        poseStack.m_85849_();
        super.m_7392_((Entity)knife, yaw, partialTick, poseStack, buffers, packedLight);
    }

    public ResourceLocation getTextureLocation(XueliKnifeEntity knife) {
        return TextureAtlas.f_118259_;
    }
}

