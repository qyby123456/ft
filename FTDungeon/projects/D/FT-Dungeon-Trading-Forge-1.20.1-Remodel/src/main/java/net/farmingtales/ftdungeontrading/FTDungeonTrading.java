package net.farmingtales.ftdungeontrading;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(FTDungeonTrading.ID)
public final class FTDungeonTrading {
    public static final String ID = "ftdungeontrading";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    public static final RegistryObject<Block> TRADING_TABLE = BLOCKS.register("trading_table", () -> new TradeTableBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(3.0F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.BLOCK)));
    public static final RegistryObject<Item> TABLE_ITEM = ITEMS.register("trading_table", () -> new TradeTableBlockItem(TRADING_TABLE.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<TradeTableBlockEntity>> TABLE_ENTITY = BLOCK_ENTITIES.register("trading_table", () ->
            BlockEntityType.Builder.of(TradeTableBlockEntity::new, TRADING_TABLE.get()).build(null));
    private static final RegistryObject<CreativeModeTab> TRADING_TAB = TABS.register("trading", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ftdungeontrading"))
            .icon(() -> TABLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> output.accept(TABLE_ITEM.get())).build());

    public FTDungeonTrading() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        TABS.register(modBus);
        MinecraftForge.EVENT_BUS.addListener(FTDungeonTrading::reload);
        MinecraftForge.EVENT_BUS.addListener(TradeCommands::register);
        MinecraftForge.EVENT_BUS.addListener(FTDungeonTrading::tick);
        MinecraftForge.EVENT_BUS.addListener(FTDungeonTrading::logout);
        MinecraftForge.EVENT_BUS.addListener(FTDungeonTrading::stop);
    }

    private static void reload(AddReloadListenerEvent event) { event.addListener(TradeOfferRepository.INSTANCE); }

    private static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.getServer().getTickCount() % 20 == 0) {
            TradeService.expire(event.getServer().overworld().getGameTime());
        }
    }

    private static void logout(PlayerEvent.PlayerLoggedOutEvent event) { TradeService.cancel(event.getEntity().getUUID()); }
    private static void stop(ServerStoppingEvent event) { TradeService.clear(); }
}
