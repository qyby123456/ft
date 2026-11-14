/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.CreativeModeTabs
 *  net.minecraft.world.item.Item
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

import net.farmingtales.ftboss.item.FTCreativeTabs;
import net.farmingtales.ftboss.item.weapons.MaterialBowItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class MaterialBowItems {
    private MaterialBowItems() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent registerEvent) {
        registerEvent.register(ForgeRegistries.Keys.ITEMS, registerHelper -> {
            for (Kind kind : Kind.values()) {
                registerHelper.register(kind.id(), (Object)new MaterialBowItem(kind));
            }
        });
    }

    public static MaterialBowItem get(Kind kind) {
        Item item = (Item)ForgeRegistries.ITEMS.getValue(kind.id());
        if (!(item instanceof MaterialBowItem)) {
            throw new IllegalStateException("Missing material bow: " + kind.id());
        }
        MaterialBowItem materialBowItem = (MaterialBowItem)item;
        return materialBowItem;
    }

    @SubscribeEvent
    public static void creativeContents(BuildCreativeModeTabContentsEvent buildCreativeModeTabContentsEvent) {
        if (buildCreativeModeTabContentsEvent.getTab() != FTCreativeTabs.MAIN.get() && !buildCreativeModeTabContentsEvent.getTabKey().equals((Object)CreativeModeTabs.f_256797_)) {
            return;
        }
        for (Kind kind : Kind.values()) {
            buildCreativeModeTabContentsEvent.m_246326_((ItemLike)MaterialBowItems.get(kind));
        }
    }

    public static enum Kind {
        WOOD("wooden_bow", Tiers.WOOD),
        STONE("stone_bow", Tiers.STONE),
        IRON("iron_bow", Tiers.IRON),
        GOLD("golden_bow", Tiers.GOLD),
        DIAMOND("diamond_bow", Tiers.DIAMOND),
        NETHERITE("netherite_bow", Tiers.NETHERITE);

        public final String itemId;
        public final Tiers tier;

        private Kind(String string2, Tiers tiers) {
            this.itemId = string2;
            this.tier = tiers;
        }

        public ResourceLocation id() {
            return ResourceLocation.fromNamespaceAndPath((String)"ftboss", (String)this.itemId);
        }
    }
}

