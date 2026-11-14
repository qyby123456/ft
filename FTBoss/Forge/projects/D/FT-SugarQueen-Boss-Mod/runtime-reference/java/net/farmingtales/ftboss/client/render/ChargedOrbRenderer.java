/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoEntityRenderer
 *  software.bernie.geckolib.renderer.GeoRenderer
 *  software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.model.ChargedOrbGeoModel;
import net.farmingtales.ftboss.entity.ChargedOrbEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ChargedOrbRenderer
extends GeoEntityRenderer<ChargedOrbEntity> {
    public ChargedOrbRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, (GeoModel)new ChargedOrbGeoModel());
        this.f_114477_ = 0.22f;
        this.addRenderLayer((GeoRenderLayer)new AutoGlowingGeoLayer((GeoRenderer)this));
    }
}

