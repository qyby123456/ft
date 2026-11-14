package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.minecraft.resources.ResourceLocation;

public class TideFishBossModel extends PathGeoModel<TideFishBossEntity> {
    private static final ResourceLocation SECOND_PHASE = new ResourceLocation("ftboss", "textures/entity/tide_fish_boss_p2.png");

    public TideFishBossModel() {
        super("geo/tide_fish_boss.geo.json", "textures/entity/tide_fish_boss.png", "animations/tide_fish_boss.animation.json");
    }

    @Override
    public ResourceLocation getTextureResource(TideFishBossEntity entity) {
        return entity.getPhase() == 2 ? SECOND_PHASE : super.getTextureResource(entity);
    }
}
