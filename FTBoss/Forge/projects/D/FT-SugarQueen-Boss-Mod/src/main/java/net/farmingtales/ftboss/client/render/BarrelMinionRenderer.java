package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.model.BarrelMinionGeoModel;
import net.farmingtales.ftboss.entity.SmallBarrelEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 小酒桶渲染器(2026-09-11 新增) */
public class BarrelMinionRenderer extends GeoEntityRenderer<SmallBarrelEntity> {

    public BarrelMinionRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new BarrelMinionGeoModel());
        this.shadowRadius = 0.35f;
    }
}
