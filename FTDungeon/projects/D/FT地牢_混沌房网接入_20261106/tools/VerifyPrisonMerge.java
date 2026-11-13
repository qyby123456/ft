package net.farmingtales.ftdungeonmobs;

import java.util.HashSet;
import net.farmingtales.ftdungeonprops.FTDungeonProps;
import net.farmingtales.ftdungeonprops.PrisonCatalog;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "ftdungeonmobs", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class VerifyPrisonMerge {
    private VerifyPrisonMerge() {}

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        CreativeModeTab tab = FTDungeonMobs.TAB.get();
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(FeatureFlags.DEFAULT_FLAGS, true, RegistryAccess.EMPTY));
        var displayed = new HashSet<>(tab.getDisplayItems().stream().map(stack -> stack.getItem()).toList());
        require(PrisonCatalog.ALL.size() == 24, "24 catalog entries");
        require(FTDungeonProps.ITEM_MAP.size() == 24, "24 registered items");
        for (var spec : PrisonCatalog.ALL) {
            var item = FTDungeonProps.ITEM_MAP.get(spec.id()).get();
            var key = ResourceLocation.fromNamespaceAndPath("ftdungeonprops", spec.id());
            require(ForgeRegistries.ITEMS.getValue(key) == item, "Stable item ID: " + key);
            require(ForgeRegistries.BLOCKS.getValue(key) == FTDungeonProps.BLOCK_MAP.get(spec.id()).get(), "Stable block ID: " + key);
            require(displayed.contains(item), "Present in original FT dungeon tab: " + key);
            require(tab.getDisplayItems().stream().filter(stack -> stack.getItem() == item).count() == 1, "No duplicate tab item: " + key);
        }
        require(!BuiltInRegistries.CREATIVE_MODE_TAB.containsKey(ResourceLocation.fromNamespaceAndPath("ftdungeonprops", "prison_decor")), "Standalone tab removed");
        System.out.println("PRISON_MERGE_VERIFIED: 24 stable block/item IDs, all 24 present exactly once in FT dungeon tab, no standalone tab.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("PRISON_MERGE_FAILURE: " + message);
        }
    }
}
