/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoEntityRenderer
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.model.FlyGeoModel;
import net.farmingtales.ftboss.entity.FlyMinionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FlyRenderer
extends GeoEntityRenderer<FlyMinionEntity> {
    public FlyRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, (GeoModel)new FlyGeoModel());
        this.f_114477_ = 0.2f;
    }
}

