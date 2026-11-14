package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 炫彩答辩超人模型: **二阶段起自动换用彩色答辩头版本(_p2)**(2026-09-12)。
 * GeckoLib 会在资源路径变化时自动重载模型, 无需额外实体或动画。
 */
public class CompostGeoModel extends GeoModel<CompostBossEntity> {

    private static final ResourceLocation GEO = new ResourceLocation(FTBossMod.MOD_ID, "geo/compost_superman.geo.json");
    private static final ResourceLocation ANIM = new ResourceLocation(FTBossMod.MOD_ID, "animations/compost_superman.animation.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(FTBossMod.MOD_ID, "textures/entity/compost_superman.png");
    private static final ResourceLocation PHASE_TWO_TEXTURE = new ResourceLocation(FTBossMod.MOD_ID, "textures/entity/compost_superman_p2.png");

    @Override
    public ResourceLocation getModelResource(CompostBossEntity e) {
        return GEO;
    }

    @Override
    public ResourceLocation getTextureResource(CompostBossEntity e) {
        return e.getPhase() >= 2 ? PHASE_TWO_TEXTURE : TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CompostBossEntity e) {
        return ANIM;
    }
}
