package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.weapons.MaterialBowItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MaterialBowItems {
    public enum Kind {
        WOOD("wooden_bow", Tiers.WOOD),
        STONE("stone_bow", Tiers.STONE),
        IRON("iron_bow", Tiers.IRON),
        GOLD("golden_bow", Tiers.GOLD),
        DIAMOND("diamond_bow", Tiers.DIAMOND),
        NETHERITE("netherite_bow", Tiers.NETHERITE);

        public final String itemId;
        public final Tiers tier;

        Kind(String itemId, Tiers tier) {
            this.itemId = itemId;
            this.tier = tier;
        }

        public ResourceLocation id() {
            return ResourceLocation.fromNamespaceAndPath(FTBossMod.MOD_ID, itemId);
        }
    }

    private MaterialBowItems() {}

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            for (Kind kind : Kind.values()) helper.register(kind.id(), new MaterialBowItem(kind));
        });
    }

    public static MaterialBowItem get(Kind kind) {
        Item item = ForgeRegistries.ITEMS.getValue(kind.id());
        if (!(item instanceof MaterialBowItem bow)) throw new IllegalStateException("Missing material bow: " + kind.id());
        return bow;
    }

    @SubscribeEvent
    public static void creativeContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != FTCreativeTabs.MAIN.get() && !event.getTabKey().equals(CreativeModeTabs.COMBAT)) return;
        for (Kind kind : Kind.values()) event.accept(get(kind));
    }
}
