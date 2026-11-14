package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.model.MaggotGeoModel;
import net.farmingtales.ftboss.entity.MaggotMinionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 蛆小怪渲染器(2026-09-11) */
public class MaggotRenderer extends GeoEntityRenderer<MaggotMinionEntity> {
    public MaggotRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new MaggotGeoModel());
        this.shadowRadius = 0.35f;
    }
}
