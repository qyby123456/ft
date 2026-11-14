/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoEntityRenderer
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.model.TeaMinionGeoModel;
import net.farmingtales.ftboss.entity.TeaMinionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TeaMinionRenderer
extends GeoEntityRenderer<TeaMinionEntity> {
    public TeaMinionRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, (GeoModel)new TeaMinionGeoModel());
        this.f_114477_ = 0.3f;
    }
}

