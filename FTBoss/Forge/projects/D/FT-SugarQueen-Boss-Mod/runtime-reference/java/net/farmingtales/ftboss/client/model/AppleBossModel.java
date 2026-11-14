/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AppleBossModel
extends GeoModel<AppleBossEntity> {
    private static final ResourceLocation GEO = new ResourceLocation("ftboss", "geo/apple_boss.geo.json");
    private static final ResourceLocation ANIM = new ResourceLocation("ftboss", "animations/apple_boss.animation.json");
    private static final ResourceLocation TEX_1 = new ResourceLocation("ftboss", "textures/entity/apple_boss.png");
    private static final ResourceLocation TEX_2 = new ResourceLocation("ftboss", "textures/entity/apple_boss_p2.png");

    public ResourceLocation getModelResource(AppleBossEntity e) {
        return GEO;
    }

    public ResourceLocation getTextureResource(AppleBossEntity e) {
        return e.getPhase() >= 2 ? TEX_2 : TEX_1;
    }

    public ResourceLocation getAnimationResource(AppleBossEntity e) {
        return ANIM;
    }
}

