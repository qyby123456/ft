/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.SlimeModel
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.MobRenderer
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.client.renderer.entity.layers.SlimeOuterLayer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.entity.CompostSlimeEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CompostSlimeRenderer
extends MobRenderer<CompostSlimeEntity, SlimeModel<CompostSlimeEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/entity/compost_slime.png");

    public CompostSlimeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, (EntityModel)new SlimeModel(ctx.m_174023_(ModelLayers.f_171241_)), 0.25f);
        this.m_115326_((RenderLayer)new SlimeOuterLayer((RenderLayerParent)this, ctx.m_174027_()));
    }

    public ResourceLocation getTextureLocation(CompostSlimeEntity entity) {
        return TEXTURE;
    }

    protected void scale(CompostSlimeEntity entity, PoseStack poseStack, float partialTicks) {
        float size = entity.m_33632_();
        float squish = Mth.m_14179_((float)partialTicks, (float)entity.f_33585_, (float)entity.f_33584_) / (size * 0.5f + 1.0f);
        float f = 1.0f / (squish + 1.0f);
        poseStack.m_85841_(0.999f, 0.999f, 0.999f);
        poseStack.m_252880_(0.0f, 0.001f, 0.0f);
        poseStack.m_85841_(f * size, 1.0f / f * size, f * size);
    }
}

