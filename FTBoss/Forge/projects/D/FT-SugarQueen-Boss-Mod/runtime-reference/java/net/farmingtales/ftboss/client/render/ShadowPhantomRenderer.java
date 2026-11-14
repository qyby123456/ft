/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.MobRenderer
 *  net.minecraft.resources.ResourceLocation
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.model.ShadowPhantomModel;
import net.farmingtales.ftboss.entity.ShadowPhantomEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ShadowPhantomRenderer
extends MobRenderer<ShadowPhantomEntity, ShadowPhantomModel> {
    public static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/entity/easter/phantom_zheergen.png");

    public ShadowPhantomRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, (EntityModel)new ShadowPhantomModel(ctx.m_174023_(ModelLayers.f_171162_)), 0.0f);
    }

    public ResourceLocation getTextureLocation(ShadowPhantomEntity entity) {
        return TEXTURE;
    }

    protected RenderType getRenderType(ShadowPhantomEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.m_110473_((ResourceLocation)TEXTURE);
    }
}

