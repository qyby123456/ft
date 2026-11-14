/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CompostGeoModel
extends GeoModel<CompostBossEntity> {
    private static final ResourceLocation GEO = new ResourceLocation("ftboss", "geo/compost_superman.geo.json");
    private static final ResourceLocation ANIM = new ResourceLocation("ftboss", "animations/compost_superman.animation.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/entity/compost_superman.png");
    private static final ResourceLocation PHASE_TWO_TEXTURE = new ResourceLocation("ftboss", "textures/entity/compost_superman_p2.png");

    public ResourceLocation getModelResource(CompostBossEntity e) {
        return GEO;
    }

    public ResourceLocation getTextureResource(CompostBossEntity e) {
        return e.getPhase() >= 2 ? PHASE_TWO_TEXTURE : TEXTURE;
    }

    public ResourceLocation getAnimationResource(CompostBossEntity e) {
        return ANIM;
    }
}

