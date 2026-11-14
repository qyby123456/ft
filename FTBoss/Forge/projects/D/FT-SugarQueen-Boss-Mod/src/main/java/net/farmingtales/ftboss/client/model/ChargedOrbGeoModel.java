package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.ChargedOrbEntity;
import net.farmingtales.ftboss.entity.OrbType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.core.animation.AnimationState;

import java.util.EnumMap;
import java.util.Map;

/** 五种独立轮廓、八段动作的充能球模型。 */
public class ChargedOrbGeoModel extends GeoModel<ChargedOrbEntity> {

    private static final Map<OrbType, PathGeoModel<ChargedOrbEntity>> MODELS = new EnumMap<>(OrbType.class);

    static {
        for (OrbType type : OrbType.values()) {
            MODELS.put(type, new PathGeoModel<>("geo/orb_link_" + type.id + ".geo.json",
                    "textures/entity/orb_link_" + type.id + ".png", "animations/orb_link_" + type.id + ".animation.json"));
        }
    }

    @Override
    public void setCustomAnimations(ChargedOrbEntity entity, long instanceId, AnimationState<ChargedOrbEntity> state) {
        super.setCustomAnimations(entity, instanceId, state);
        if (entity.getOrbType() != OrbType.DARK) return;
        getBone("core").ifPresent(core -> {
            float growth = 1.0f + entity.chargeFraction() * 0.22f;
            core.setScaleX(core.getScaleX() * growth);
            core.setScaleY(core.getScaleY() * growth);
            core.setScaleZ(core.getScaleZ() * growth);
        });
    }

    @Override
    public ResourceLocation getModelResource(ChargedOrbEntity entity) {
        return MODELS.get(entity.getOrbType()).getModelResource(entity);
    }

    @Override
    public ResourceLocation getTextureResource(ChargedOrbEntity entity) {
        return MODELS.get(entity.getOrbType()).getTextureResource(entity);
    }

    @Override
    public ResourceLocation getAnimationResource(ChargedOrbEntity entity) {
        return MODELS.get(entity.getOrbType()).getAnimationResource(entity);
    }
}
