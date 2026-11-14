package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.entity.SugarQueenEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 糖霜女王渲染: GeckoLib 姜饼人国王模型(几何已按 1.3 倍烘焙) */
public class SugarQueenGeoRenderer extends GeoEntityRenderer<SugarQueenEntity> {

    public SugarQueenGeoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new net.farmingtales.ftboss.client.model.GingerbreadKingModel());
        this.shadowRadius = 1.0F;
    }
}
