/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockBehaviour
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraftforge.common.extensions.IForgeMenuType
 *  net.minecraftforge.event.BuildCreativeModeTabContentsEvent
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package net.farmingtales.ftboss.block;

import net.farmingtales.ftboss.block.BlankStatueBlock;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class BlankStatueContent {
    public static final ResourceLocation ID = new ResourceLocation("ftboss", "statue_blank");
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.BLOCKS, (String)"ftboss");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.ITEMS, (String)"ftboss");
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.MENU_TYPES, (String)"ftboss");
    public static final RegistryObject<BlankStatueBlock> BLOCK = BLOCKS.register("statue_blank", () -> new BlankStatueBlock(BlockBehaviour.Properties.m_60926_((BlockBehaviour)Blocks.f_50069_).m_60913_(2.0f, 6.0f).m_60953_(blockState -> 10).m_60955_()));
    public static final RegistryObject<BlockItem> ITEM = ITEMS.register("statue_blank", () -> new BlockItem((Block)BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<MenuType<BossPreviewMenu>> MENU = MENUS.register("statue_blank", () -> IForgeMenuType.create(BossPreviewMenu::new));
    public static final RegistryObject<MenuType<SwordCavityMenu>> SWORD_CAVITY_MENU = MENUS.register("sword_cavity", () -> IForgeMenuType.create(SwordCavityMenu::new));

    private BlankStatueContent() {
    }

    public static void register(IEventBus iEventBus) {
        BLOCKS.register(iEventBus);
        ITEMS.register(iEventBus);
        MENUS.register(iEventBus);
    }

    @SubscribeEvent
    public static void creativeTab(BuildCreativeModeTabContentsEvent buildCreativeModeTabContentsEvent) {
        if (buildCreativeModeTabContentsEvent.getTabKey().equals((Object)FTCreativeTabs.MAIN.getKey())) {
            buildCreativeModeTabContentsEvent.m_246326_((ItemLike)ITEM.get());
        }
    }
}

