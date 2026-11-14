package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class QizaiStaffRenderer extends GeoItemRenderer<QizaiPrayerStaffItem> {
    public QizaiStaffRenderer() { super(new Model()); addRenderLayer(new AutoGlowingGeoLayer<>(this)); }

    public static final class Model extends GeoModel<QizaiPrayerStaffItem> {
        private static final ResourceLocation GEOMETRY = new ResourceLocation("ftboss", "geo/qizai_prayer_staff.geo.json");
        private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/item/qizai_prayer_staff.png");
        private static final ResourceLocation ANIMATION = new ResourceLocation("ftboss", "animations/qizai_prayer_staff.animation.json");
        @Override public ResourceLocation getModelResource(QizaiPrayerStaffItem item) { return GEOMETRY; }
        @Override public ResourceLocation getTextureResource(QizaiPrayerStaffItem item) { return TEXTURE; }
        @Override public ResourceLocation getAnimationResource(QizaiPrayerStaffItem item) { return ANIMATION; }
    }
}
