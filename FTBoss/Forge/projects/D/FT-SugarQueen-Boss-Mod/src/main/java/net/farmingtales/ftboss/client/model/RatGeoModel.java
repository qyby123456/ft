package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** 鼠群 GeckoLib 模型提供器: 大老鼠/小老鼠共用骨骼动画, 各用各贴图 */
public class RatGeoModel extends GeoModel<RatBossEntity> {

    private static final ResourceLocation SMALL_GEO = new ResourceLocation(FTBossMod.MOD_ID, "geo/small_rat.geo.json");
    private static final ResourceLocation BIG_GEO = new ResourceLocation(FTBossMod.MOD_ID, "geo/big_rat.geo.json");
    private static final ResourceLocation SMALL_TEXTURE = new ResourceLocation(FTBossMod.MOD_ID, "textures/entity/small_rat.png");
    private static final ResourceLocation BIG_TEXTURE = new ResourceLocation(FTBossMod.MOD_ID, "textures/entity/big_rat.png");
    private static final ResourceLocation ANIM = new ResourceLocation(FTBossMod.MOD_ID, "animations/rat.animation.json");

    @Override
    public ResourceLocation getModelResource(RatBossEntity entity) {
        return entity.isMinion() ? SMALL_GEO : BIG_GEO;
    }

    @Override
    public ResourceLocation getTextureResource(RatBossEntity entity) {
        return entity.isMinion() ? SMALL_TEXTURE : BIG_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(RatBossEntity entity) {
        return ANIM;
    }
}
