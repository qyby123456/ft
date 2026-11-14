/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.item.ItemProperties
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
 */
package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.item.MaterialBowItems;
import net.farmingtales.ftboss.item.weapons.MaterialBowItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class MaterialBowClient {
    private MaterialBowClient() {
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent fMLClientSetupEvent) {
        fMLClientSetupEvent.enqueueWork(() -> {
            for (MaterialBowItems.Kind kind : MaterialBowItems.Kind.values()) {
                MaterialBowItem materialBowItem = MaterialBowItems.get(kind);
                ItemProperties.register((Item)materialBowItem, (ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"minecraft", (String)"pull"), (itemStack, clientLevel, livingEntity, n) -> livingEntity != null && livingEntity.m_21211_() == itemStack ? (float)(itemStack.m_41779_() - livingEntity.m_21212_()) / 20.0f : 0.0f);
                ItemProperties.register((Item)materialBowItem, (ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"minecraft", (String)"pulling"), (itemStack, clientLevel, livingEntity, n) -> livingEntity != null && livingEntity.m_6117_() && livingEntity.m_21211_() == itemStack ? 1.0f : 0.0f);
            }
        });
    }
}

