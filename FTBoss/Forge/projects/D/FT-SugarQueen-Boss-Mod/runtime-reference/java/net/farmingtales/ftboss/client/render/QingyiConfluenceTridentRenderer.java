/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoItemRenderer
 *  software.bernie.geckolib.renderer.GeoRenderer
 *  software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.item.weapons.QingyiConfluenceTridentItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class QingyiConfluenceTridentRenderer
extends GeoItemRenderer<QingyiConfluenceTridentItem> {
    public QingyiConfluenceTridentRenderer() {
        super((GeoModel)new Model());
        this.addRenderLayer((GeoRenderLayer)new AutoGlowingGeoLayer((GeoRenderer)this));
    }

    public static final class Model
    extends GeoModel<QingyiConfluenceTridentItem> {
        private static final ResourceLocation GEOMETRY = new ResourceLocation("ftboss", "geo/qingyi_confluence_trident.geo.json");
        private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/item/qingyi_confluence_trident.png");
        private static final ResourceLocation ANIMATION = new ResourceLocation("ftboss", "animations/qingyi_confluence_trident.animation.json");

        public ResourceLocation getModelResource(QingyiConfluenceTridentItem item) {
            return GEOMETRY;
        }

        public ResourceLocation getTextureResource(QingyiConfluenceTridentItem item) {
            return TEXTURE;
        }

        public ResourceLocation getAnimationResource(QingyiConfluenceTridentItem item) {
            return ANIMATION;
        }
    }
}

