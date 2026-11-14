package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.entity.RatBossEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 鼠群渲染: GeckoLib(大老鼠/小老鼠 geo + 共用手动动画) */
public class RatGeoRenderer extends GeoEntityRenderer<RatBossEntity> {

    public RatGeoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new net.farmingtales.ftboss.client.model.RatGeoModel());
        this.shadowRadius = 0.4F;
    }
}
