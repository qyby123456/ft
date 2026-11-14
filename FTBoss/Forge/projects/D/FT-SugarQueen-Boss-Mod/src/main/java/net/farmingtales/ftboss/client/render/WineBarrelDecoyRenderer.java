package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.model.WineBarrelDecoyModel;
import net.farmingtales.ftboss.entity.WineBarrelDecoyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 酒桶王分身渲染器(2026-09-12) */
public class WineBarrelDecoyRenderer extends GeoEntityRenderer<WineBarrelDecoyEntity> {
    public WineBarrelDecoyRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new WineBarrelDecoyModel());
        this.shadowRadius = 1.2f;
    }
}
