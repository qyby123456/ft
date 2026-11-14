/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoEntityRenderer
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.model.GingerbreadMinionGeoModel;
import net.farmingtales.ftboss.entity.GingerbreadMinionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GingerbreadMinionRenderer
extends GeoEntityRenderer<GingerbreadMinionEntity> {
    public GingerbreadMinionRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, (GeoModel)new GingerbreadMinionGeoModel());
        this.f_114477_ = 0.28f;
    }
}

