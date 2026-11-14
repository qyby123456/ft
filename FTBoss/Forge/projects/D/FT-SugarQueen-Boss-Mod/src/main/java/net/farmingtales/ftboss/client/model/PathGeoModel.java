package net.farmingtales.ftboss.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

/**
 * 通用 GeoModel(2026-09-12): 三个苹果召唤物只是「换贴图 + 换实体类」,
 * 几何/动画直接复用现有资产, 所以不必各写一个 Model 类 —— 路径从构造参数传进来即可。
 */
public class PathGeoModel<T extends Entity & GeoAnimatable> extends GeoModel<T> {

    private final ResourceLocation geo;
    private final ResourceLocation texture;
    private final ResourceLocation anim;

    public PathGeoModel(String geoPath, String texPath, String animPath) {
        this.geo = new ResourceLocation(net.farmingtales.ftboss.FTBossMod.MOD_ID, geoPath);
        this.texture = new ResourceLocation(net.farmingtales.ftboss.FTBossMod.MOD_ID, texPath);
        this.anim = new ResourceLocation(net.farmingtales.ftboss.FTBossMod.MOD_ID, animPath);
    }

    @Override
    public ResourceLocation getModelResource(T entity) { return this.geo; }

    @Override
    public ResourceLocation getTextureResource(T entity) { return this.texture; }

    @Override
    public ResourceLocation getAnimationResource(T entity) { return this.anim; }
}
