/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.RatBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RatGeoModel
extends GeoModel<RatBossEntity> {
    private static final ResourceLocation SMALL_GEO = new ResourceLocation("ftboss", "geo/small_rat.geo.json");
    private static final ResourceLocation BIG_GEO = new ResourceLocation("ftboss", "geo/big_rat.geo.json");
    private static final ResourceLocation SMALL_TEXTURE = new ResourceLocation("ftboss", "textures/entity/small_rat.png");
    private static final ResourceLocation BIG_TEXTURE = new ResourceLocation("ftboss", "textures/entity/big_rat.png");
    private static final ResourceLocation ANIM = new ResourceLocation("ftboss", "animations/rat.animation.json");

    public ResourceLocation getModelResource(RatBossEntity entity) {
        return entity.isMinion() ? SMALL_GEO : BIG_GEO;
    }

    public ResourceLocation getTextureResource(RatBossEntity entity) {
        return entity.isMinion() ? SMALL_TEXTURE : BIG_TEXTURE;
    }

    public ResourceLocation getAnimationResource(RatBossEntity entity) {
        return ANIM;
    }
}

