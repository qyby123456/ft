/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.LavaSlimeModel
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.MobRenderer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.entity.SheepDungEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SheepDungRenderer
extends MobRenderer<SheepDungEntity, LavaSlimeModel<SheepDungEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/entity/sheep_dung.png");

    public SheepDungRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, (EntityModel)new LavaSlimeModel(ctx.m_174023_(ModelLayers.f_171197_)), 0.25f);
    }

    public ResourceLocation getTextureLocation(SheepDungEntity entity) {
        return TEXTURE;
    }

    protected void scale(SheepDungEntity entity, PoseStack poseStack, float partialTicks) {
        float size = entity.m_33632_();
        float squish = Mth.m_14179_((float)partialTicks, (float)entity.f_33585_, (float)entity.f_33584_) / (size * 0.5f + 1.0f);
        float f = 1.0f / (squish + 1.0f);
        poseStack.m_85841_(0.999f, 0.999f, 0.999f);
        poseStack.m_252880_(0.0f, 0.001f, 0.0f);
        poseStack.m_85841_(f * size, 1.0f / f * size, f * size);
    }
}

