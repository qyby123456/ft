package cn.farmingtales.qysevent;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(QysEventMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(QysEventMod.MOD_ID);

    public static final DeferredBlock<WeatherForecasterBlock> WEATHER_FORECASTER = BLOCKS.register("weather_forecaster", () ->
            new WeatherForecasterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
                    .strength(3.5F, 6.0F).sound(SoundType.COPPER).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> WEATHER_FORECASTER_ITEM = ITEMS.register("weather_forecaster", () ->
            new BlockItem(WEATHER_FORECASTER.get(), new Item.Properties()));

    public static final DeferredBlock<OracleBlock> ORACLE = BLOCKS.register("oracle", () ->
            new OracleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
                    .strength(2.0F, 6.0F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> ORACLE_ITEM = ITEMS.register("oracle", () ->
            new BlockItem(ORACLE.get(), new Item.Properties()));

    public static final DeferredBlock<WishingWellBlock> WISHING_WELL = BLOCKS.register("wishing_well", () ->
            new WishingWellBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .strength(2.5F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredItem<WishingWellItem> WISHING_WELL_ITEM = ITEMS.register("wishing_well", () ->
            new WishingWellItem(WISHING_WELL.get(), new Item.Properties()));


    private ModBlocks() {}
}

