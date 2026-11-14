package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.item.weapons.QingyiConfluenceTridentItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class QingyiConfluenceTridentRenderer extends GeoItemRenderer<QingyiConfluenceTridentItem> {
    public QingyiConfluenceTridentRenderer() {
        super(new Model());
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    public static final class Model extends GeoModel<QingyiConfluenceTridentItem> {
        private static final ResourceLocation GEOMETRY = new ResourceLocation("ftboss", "geo/qingyi_confluence_trident.geo.json");
        private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/item/qingyi_confluence_trident.png");
        private static final ResourceLocation ANIMATION = new ResourceLocation("ftboss", "animations/qingyi_confluence_trident.animation.json");

        @Override
        public ResourceLocation getModelResource(QingyiConfluenceTridentItem item) {
            return GEOMETRY;
        }

        @Override
        public ResourceLocation getTextureResource(QingyiConfluenceTridentItem item) {
            return TEXTURE;
        }

        @Override
        public ResourceLocation getAnimationResource(QingyiConfluenceTridentItem item) {
            return ANIMATION;
        }
    }
}
