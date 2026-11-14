package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.model.FlyGeoModel;
import net.farmingtales.ftboss.entity.FlyMinionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 苍蝇小怪渲染器(2026-09-12) */
public class FlyRenderer extends GeoEntityRenderer<FlyMinionEntity> {
    public FlyRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new FlyGeoModel());
        this.shadowRadius = 0.2f;
    }
}
