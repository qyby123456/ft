package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.entity.GingerbreadMinionEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** 小姜饼人渲染: GeckoLib 模型 */
public class GingerbreadMinionRenderer extends GeoEntityRenderer<GingerbreadMinionEntity> {

    public GingerbreadMinionRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new net.farmingtales.ftboss.client.model.GingerbreadMinionGeoModel());
        this.shadowRadius = 0.28F;
    }
}
