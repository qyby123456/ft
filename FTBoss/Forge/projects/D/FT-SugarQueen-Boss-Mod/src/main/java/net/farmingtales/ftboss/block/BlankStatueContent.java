package net.farmingtales.ftboss.block;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FTCreativeTabs;
import net.farmingtales.ftboss.menu.BossPreviewMenu;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** 石像菜单延迟注册；2026-10-13 增加剑体容器，构造期只创建供应器，注册事件内才实例化内容。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class BlankStatueContent {
    public static final ResourceLocation ID = new ResourceLocation(FTBossMod.MOD_ID, "statue_blank");
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FTBossMod.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FTBossMod.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, FTBossMod.MOD_ID);
    public static final RegistryObject<BlankStatueBlock> BLOCK = BLOCKS.register("statue_blank", () ->
            new BlankStatueBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(2.0F, 6.0F).lightLevel(state -> 10).noOcclusion()));
    public static final RegistryObject<BlockItem> ITEM = ITEMS.register("statue_blank", () -> new BlockItem(BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<MenuType<BossPreviewMenu>> MENU = MENUS.register("statue_blank", () -> IForgeMenuType.create(BossPreviewMenu::new));
    public static final RegistryObject<MenuType<SwordCavityMenu>> SWORD_CAVITY_MENU = MENUS.register("sword_cavity", () -> IForgeMenuType.create(SwordCavityMenu::new));

    private BlankStatueContent() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        MENUS.register(bus);
    }

    @SubscribeEvent
    public static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(FTCreativeTabs.MAIN.getKey())) event.accept(ITEM.get());
    }
}
