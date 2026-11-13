package cn.farmingtales.qysevent;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, QysEventMod.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, QysEventMod.MOD_ID);

    public static final RegistryObject<Block> WEATHER_FORECASTER = BLOCKS.register("weather_forecaster", () ->
            new WeatherForecasterBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK)
                    .strength(3.5F, 6.0F).sound(SoundType.COPPER).requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> WEATHER_FORECASTER_ITEM = ITEMS.register("weather_forecaster", () ->
            new BlockItem(WEATHER_FORECASTER.get(), new Item.Properties()));

    public static final RegistryObject<Block> ORACLE = BLOCKS.register("oracle", () ->
            new OracleBlock(BlockBehaviour.Properties.copy(Blocks.AMETHYST_BLOCK)
                    .strength(2.0F, 6.0F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> ORACLE_ITEM = ITEMS.register("oracle", () ->
            new BlockItem(ORACLE.get(), new Item.Properties()));

    public static final RegistryObject<Block> WISHING_WELL = BLOCKS.register("wishing_well", () ->
            new WishingWellBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(2.5F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> WISHING_WELL_ITEM = ITEMS.register("wishing_well", () ->
            new WishingWellItem(WISHING_WELL.get(), new Item.Properties()));

    private ModBlocks() {}
}
