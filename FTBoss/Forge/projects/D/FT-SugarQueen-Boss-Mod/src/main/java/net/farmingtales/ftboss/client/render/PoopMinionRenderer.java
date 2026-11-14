package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.model.PoopMinionModel;
import net.farmingtales.ftboss.entity.PoopMinionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PoopMinionRenderer extends GeoEntityRenderer<PoopMinionEntity> {
    public PoopMinionRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PoopMinionModel());
        this.shadowRadius = 0.4f;
    }
}
