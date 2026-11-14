/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package net.farmingtales.ftboss.client.model;

import java.util.EnumMap;
import java.util.Map;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.OrbType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DefectCoreGeoModel
extends GeoModel<DefectCoreEntity> {
    private static final ResourceLocation GEO = new ResourceLocation("ftboss", "geo/defect_spire.geo.json");
    private static final ResourceLocation ANIM = new ResourceLocation("ftboss", "animations/defect_spire.animation.json");
    private static final Map<OrbType, ResourceLocation> TEXTURES = new EnumMap<OrbType, ResourceLocation>(OrbType.class);

    public ResourceLocation getModelResource(DefectCoreEntity entity) {
        return GEO;
    }

    public ResourceLocation getTextureResource(DefectCoreEntity entity) {
        return TEXTURES.get((Object)entity.coreTint());
    }

    public ResourceLocation getAnimationResource(DefectCoreEntity entity) {
        return ANIM;
    }

    static {
        for (OrbType type : OrbType.values()) {
            TEXTURES.put(type, new ResourceLocation("ftboss", "textures/entity/defect_spire_" + type.id + ".png"));
        }
    }
}

