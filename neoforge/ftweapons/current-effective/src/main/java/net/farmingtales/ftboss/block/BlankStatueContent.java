package net.farmingtales.ftboss.block;

import net.farmingtales.ftboss.item.FTCreativeTabs;
import net.farmingtales.ftboss.menu.BossPreviewMenu;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.MOD
)
public final class BlankStatueContent {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("ftboss", "statue_blank");
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, "ftboss");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, "ftboss");
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, "ftboss");
    public static final DeferredHolder<Block, BlankStatueBlock> BLOCK = BLOCKS.register(
        "statue_blank", () -> new BlankStatueBlock(Properties.ofFullCopy(Blocks.STONE).strength(2.0F, 6.0F).lightLevel(var0 -> 10).noOcclusion())
    );
    public static final DeferredHolder<Item, BlockItem> ITEM = ITEMS.register(
        "statue_blank", () -> new BlockItem((Block)BLOCK.get(), new net.minecraft.world.item.Item.Properties())
    );
    public static final DeferredHolder<MenuType<?>, MenuType<BossPreviewMenu>> MENU = MENUS.register("statue_blank", () -> IMenuTypeExtension.create(BossPreviewMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<SwordCavityMenu>> SWORD_CAVITY_MENU = MENUS.register(
        "sword_cavity", () -> IMenuTypeExtension.create(SwordCavityMenu::new)
    );

    private BlankStatueContent() {
    }

    public static void register(IEventBus var0) {
        BLOCKS.register(var0);
        ITEMS.register(var0);
        MENUS.register(var0);
    }

    @SubscribeEvent
    public static void creativeTab(BuildCreativeModeTabContentsEvent var0) {
        if (var0.getTabKey().equals(FTCreativeTabs.MAIN.getKey())) {
            var0.accept((ItemLike)ITEM.get());
        }
    }
}
