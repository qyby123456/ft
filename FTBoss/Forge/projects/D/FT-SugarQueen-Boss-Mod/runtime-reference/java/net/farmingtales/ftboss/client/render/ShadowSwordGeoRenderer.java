/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoItemRenderer
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.render.SwordAxisDebugLayer;
import net.farmingtales.ftboss.item.weapons.ShadowSwordItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ShadowSwordGeoRenderer
extends GeoItemRenderer<ShadowSwordItem> {
    public ShadowSwordGeoRenderer() {
        super((GeoModel)new Model());
        FTBossMod.LOGGER.info("[FTBoss][\u6697\u5f71\u5251] \u8c03\u7528\u94fe\u2461 \u6784\u9020 ShadowSwordGeoRenderer\uff08\u6a21\u578b=ftboss:geo/shadow_sword.geo.json\uff09");
        this.addRenderLayer(SwordAxisDebugLayer.of(this));
    }

    public ResourceLocation getTextureLocation(ShadowSwordItem animatable) {
        ItemStack stack = this.getCurrentItemStack();
        if (stack != null && stack.m_41782_() && stack.m_41783_().m_128471_("ftboss_ready")) {
            return Model.READY;
        }
        return Model.NORMAL;
    }

    public static class Model
    extends GeoModel<ShadowSwordItem> {
        private static final ResourceLocation NORMAL = new ResourceLocation("ftboss", "textures/item/shadow_sword.png");
        private static final ResourceLocation READY = new ResourceLocation("ftboss", "textures/item/shadow_sword_ready.png");

        public ResourceLocation getModelResource(ShadowSwordItem animatable) {
            return new ResourceLocation("ftboss", "geo/shadow_sword.geo.json");
        }

        public ResourceLocation getAnimationResource(ShadowSwordItem animatable) {
            return new ResourceLocation("ftboss", "animations/shadow_sword.animation.json");
        }

        public ResourceLocation getTextureResource(ShadowSwordItem animatable) {
            return NORMAL;
        }
    }
}

