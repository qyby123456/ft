/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.world.entity.Entity
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoEntityRenderer
 */
package net.farmingtales.ftboss.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PathGeoRenderer<T extends Entity>
extends GeoEntityRenderer<T> {
    public PathGeoRenderer(EntityRendererProvider.Context ctx, GeoModel<T> model, float shadowRadius) {
        super(ctx, model);
        this.f_114477_ = shadowRadius;
    }
}

