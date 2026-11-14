/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  software.bernie.geckolib.model.GeoModel
 */
package net.farmingtales.ftboss.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.model.GeoModel;

public class PathGeoModel<T extends Entity>
extends GeoModel<T> {
    private final ResourceLocation geo;
    private final ResourceLocation texture;
    private final ResourceLocation anim;

    public PathGeoModel(String geoPath, String texPath, String animPath) {
        this.geo = new ResourceLocation("ftboss", geoPath);
        this.texture = new ResourceLocation("ftboss", texPath);
        this.anim = new ResourceLocation("ftboss", animPath);
    }

    public ResourceLocation getModelResource(T entity) {
        return this.geo;
    }

    public ResourceLocation getTextureResource(T entity) {
        return this.texture;
    }

    public ResourceLocation getAnimationResource(T entity) {
        return this.anim;
    }
}

