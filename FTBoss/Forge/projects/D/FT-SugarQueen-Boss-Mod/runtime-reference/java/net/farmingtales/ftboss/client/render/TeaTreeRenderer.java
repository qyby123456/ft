/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.MobRenderer
 *  net.minecraft.resources.ResourceLocation
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.client.model.SayaPlayerModel;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TeaTreeRenderer
extends MobRenderer<TeaTreeBossEntity, SayaPlayerModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/entity/tea_alex.png");

    public TeaTreeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, (EntityModel)new SayaPlayerModel(ctx.m_174023_(ModelLayers.f_171166_)), 0.5f);
    }

    public ResourceLocation getTextureLocation(TeaTreeBossEntity entity) {
        return TEXTURE;
    }

    protected void scale(TeaTreeBossEntity entity, PoseStack pose, float partialTick) {
        if (entity.isTeaMinion()) {
            pose.m_85841_(0.55f, 0.55f, 0.55f);
        } else {
            pose.m_85841_(1.3f, 1.3f, 1.3f);
        }
    }
}

