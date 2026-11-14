/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.event.BuildCreativeModeTabContentsEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.ForgeRegistries$Keys
 *  net.minecraftforge.registries.RegisterEvent
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.item.FTCreativeTabs;
import net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class MaterialCrossbows {
    public static final List<String> IDS = List.of("wooden_crossbow", "stone_crossbow", "iron_crossbow", "golden_crossbow", "diamond_crossbow", "netherite_crossbow");
    private static final List<Tiers> TIERS = List.of(Tiers.WOOD, Tiers.STONE, Tiers.IRON, Tiers.GOLD, Tiers.DIAMOND, Tiers.NETHERITE);

    private MaterialCrossbows() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent registerEvent) {
        registerEvent.register(ForgeRegistries.Keys.ITEMS, registerHelper -> {
            for (int i = 0; i < IDS.size(); ++i) {
                Item.Properties properties = new Item.Properties();
                if (TIERS.get(i) == Tiers.NETHERITE) {
                    properties.m_41486_();
                }
                registerHelper.register(new ResourceLocation("ftboss", IDS.get(i)), (Object)new MaterialCrossbowItem((Tier)TIERS.get(i), properties));
            }
        });
    }

    @SubscribeEvent
    public static void creativeTab(BuildCreativeModeTabContentsEvent buildCreativeModeTabContentsEvent) {
        if (buildCreativeModeTabContentsEvent.getTab() != FTCreativeTabs.MAIN.get()) {
            return;
        }
        for (String string : IDS) {
            Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation("ftboss", string));
            if (item == null) continue;
            buildCreativeModeTabContentsEvent.m_246326_((ItemLike)item);
        }
    }
}

