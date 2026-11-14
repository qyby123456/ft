package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.entity.TeaMinionEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 茶芽小妖渲染: GeckoLib 山茶花芽模型 */
public class TeaMinionRenderer extends GeoEntityRenderer<TeaMinionEntity> {

    public TeaMinionRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new net.farmingtales.ftboss.client.model.TeaMinionGeoModel());
        this.shadowRadius = 0.3F;
    }
}
