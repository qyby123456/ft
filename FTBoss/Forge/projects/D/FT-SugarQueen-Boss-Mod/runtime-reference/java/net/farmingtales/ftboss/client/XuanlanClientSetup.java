/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.item.ItemProperties
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.item.Item
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.EntityRenderersEvent$RegisterRenderers
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
 */
package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.client.render.PlayerMoonChakramRenderer;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.XuanlanMoonWeaponItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class XuanlanClientSetup {
    private XuanlanClientSetup() {
    }

    @SubscribeEvent
    public static void models(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register((Item)((Item)FTBossItems.WENXUANLAN_FLYWHEEL.get()), (ResourceLocation)new ResourceLocation("ftboss", "xuanlan_white"), (stack, level, entity, seed) -> XuanlanMoonWeaponItem.isWhite(stack) ? 1.0f : 0.0f));
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer((EntityType)FTEntities.PLAYER_MOON_CHAKRAM.get(), PlayerMoonChakramRenderer::new);
    }
}

