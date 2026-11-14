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

import net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class QizaiStaffRenderer
extends GeoItemRenderer<QizaiPrayerStaffItem> {
    public QizaiStaffRenderer() {
        super((GeoModel)new Model());
        this.addRenderLayer((GeoRenderLayer)new AutoGlowingGeoLayer((GeoRenderer)this));
    }

    public static final class Model
    extends GeoModel<QizaiPrayerStaffItem> {
        private static final ResourceLocation GEOMETRY = new ResourceLocation("ftboss", "geo/qizai_prayer_staff.geo.json");
        private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/item/qizai_prayer_staff.png");
        private static final ResourceLocation ANIMATION = new ResourceLocation("ftboss", "animations/qizai_prayer_staff.animation.json");

        public ResourceLocation getModelResource(QizaiPrayerStaffItem item) {
            return GEOMETRY;
        }

        public ResourceLocation getTextureResource(QizaiPrayerStaffItem item) {
            return TEXTURE;
        }

        public ResourceLocation getAnimationResource(QizaiPrayerStaffItem item) {
            return ANIMATION;
        }
    }
}

