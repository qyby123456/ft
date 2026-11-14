package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.OrbType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import java.util.EnumMap;
import java.util.Map;

/** 故障机器人：尖塔风格重制模型、分球种核心贴图与独立动作。 */
public class DefectCoreGeoModel extends GeoModel<DefectCoreEntity> {

    private static final ResourceLocation GEO = new ResourceLocation(FTBossMod.MOD_ID, "geo/defect_spire.geo.json");
    private static final ResourceLocation ANIM = new ResourceLocation(FTBossMod.MOD_ID, "animations/defect_spire.animation.json");
    private static final Map<OrbType, ResourceLocation> TEXTURES = new EnumMap<>(OrbType.class);

    static {
        for (OrbType type : OrbType.values()) {
            TEXTURES.put(type, new ResourceLocation(FTBossMod.MOD_ID, "textures/entity/defect_spire_" + type.id + ".png"));
        }
    }

    @Override
    public ResourceLocation getModelResource(DefectCoreEntity entity) {
        return GEO;
    }

    @Override
    public ResourceLocation getTextureResource(DefectCoreEntity entity) {
        return TEXTURES.get(entity.coreTint());
    }

    @Override
    public ResourceLocation getAnimationResource(DefectCoreEntity entity) {
        return ANIM;
    }
}
