package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.weapons.MaterialStaffItem;
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
public final class MaterialStaffItems {
    public static final List<String> IDS = List.of("wooden_staff", "stone_staff", "iron_staff",
            "golden_staff", "diamond_staff", "netherite_staff");
    private static final List<Tiers> TIERS = List.of(Tiers.WOOD, Tiers.STONE, Tiers.IRON, Tiers.GOLD, Tiers.DIAMOND, Tiers.NETHERITE);

    private MaterialStaffItems() {}

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            for (int index = 0; index < IDS.size(); index++) {
                Item.Properties properties = new Item.Properties();
                if (TIERS.get(index) == Tiers.NETHERITE) properties.fireResistant();
                helper.register(new ResourceLocation(FTBossMod.MOD_ID, IDS.get(index)),
                        new MaterialStaffItem(TIERS.get(index), properties));
            }
        });
    }

    @SubscribeEvent
    public static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != FTCreativeTabs.MAIN.get() && !event.getTabKey().equals(CreativeModeTabs.COMBAT)) return;
        for (String id : IDS) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(FTBossMod.MOD_ID, id));
            if (item != null) event.accept(item);
        }
    }
}
