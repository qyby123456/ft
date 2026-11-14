/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animation.AnimationState
 *  software.bernie.geckolib.model.GeoModel
 */
package net.farmingtales.ftboss.client.model;

import java.util.EnumMap;
import java.util.Map;
import net.farmingtales.ftboss.client.model.PathGeoModel;
import net.farmingtales.ftboss.entity.ChargedOrbEntity;
import net.farmingtales.ftboss.entity.OrbType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class ChargedOrbGeoModel
extends GeoModel<ChargedOrbEntity> {
    private static final Map<OrbType, PathGeoModel<ChargedOrbEntity>> MODELS = new EnumMap<OrbType, PathGeoModel<ChargedOrbEntity>>(OrbType.class);

    public void setCustomAnimations(ChargedOrbEntity entity, long instanceId, AnimationState<ChargedOrbEntity> state) {
        super.setCustomAnimations((GeoAnimatable)entity, instanceId, state);
        if (entity.getOrbType() != OrbType.DARK) {
            return;
        }
        this.getBone("core").ifPresent(core -> {
            float growth = 1.0f + entity.chargeFraction() * 0.22f;
            core.setScaleX(core.getScaleX() * growth);
            core.setScaleY(core.getScaleY() * growth);
            core.setScaleZ(core.getScaleZ() * growth);
        });
    }

    public ResourceLocation getModelResource(ChargedOrbEntity entity) {
        return MODELS.get((Object)entity.getOrbType()).getModelResource(entity);
    }

    public ResourceLocation getTextureResource(ChargedOrbEntity entity) {
        return MODELS.get((Object)entity.getOrbType()).getTextureResource(entity);
    }

    public ResourceLocation getAnimationResource(ChargedOrbEntity entity) {
        return MODELS.get((Object)entity.getOrbType()).getAnimationResource(entity);
    }

    static {
        for (OrbType type : OrbType.values()) {
            MODELS.put(type, new PathGeoModel("geo/orb_link_" + type.id + ".geo.json", "textures/entity/orb_link_" + type.id + ".png", "animations/orb_link_" + type.id + ".animation.json"));
        }
    }
}

