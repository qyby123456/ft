package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.model.ChargedOrbGeoModel;
import net.farmingtales.ftboss.entity.ChargedOrbEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** 五种独立充能球轮廓与对应的自发光层。 */
public class ChargedOrbRenderer extends GeoEntityRenderer<ChargedOrbEntity> {

    public ChargedOrbRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ChargedOrbGeoModel());
        this.shadowRadius = 0.22f;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
