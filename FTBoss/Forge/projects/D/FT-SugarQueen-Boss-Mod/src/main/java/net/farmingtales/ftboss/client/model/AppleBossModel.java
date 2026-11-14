package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** 苹果爵士模型(2026-09-12): 二阶段(虫蛀)换一张腐烂贴图, 几何/动画共用。 */
public class AppleBossModel extends GeoModel<AppleBossEntity> {

    private static final ResourceLocation GEO = new ResourceLocation(FTBossMod.MOD_ID, "geo/apple_boss.geo.json");
    private static final ResourceLocation ANIM = new ResourceLocation(FTBossMod.MOD_ID, "animations/apple_boss.animation.json");
    private static final ResourceLocation TEX_1 = new ResourceLocation(FTBossMod.MOD_ID, "textures/entity/apple_boss.png");
    private static final ResourceLocation TEX_2 = new ResourceLocation(FTBossMod.MOD_ID, "textures/entity/apple_boss_p2.png");

    @Override
    public ResourceLocation getModelResource(AppleBossEntity e) { return GEO; }

    @Override
    public ResourceLocation getTextureResource(AppleBossEntity e) {
        return e.getPhase() >= 2 ? TEX_2 : TEX_1;
    }

    @Override
    public ResourceLocation getAnimationResource(AppleBossEntity e) { return ANIM; }
}
