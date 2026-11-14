package net.farmingtales.ftboss.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 通用 Geo 渲染器(2026-09-12): 传入 model + 影子半径即可, 省掉每个召唤物一个 Renderer 类。 */
public class PathGeoRenderer<T extends Entity & GeoAnimatable> extends GeoEntityRenderer<T> {

    public PathGeoRenderer(EntityRendererProvider.Context ctx, GeoModel<T> model, float shadowRadius) {
        super(ctx, model);
        this.shadowRadius = shadowRadius;
    }
}
