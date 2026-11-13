package net.farmingtales.ftdungeonloot;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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

@Mod(FTDungeonLoot.ID)
public final class FTDungeonLoot {
    public static final String ID = "ftdungeonloot";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    public static final RegistryObject<Block> STAND = BLOCKS.register("loot_stand", () -> new LootStandBlock(
            BlockBehaviour.Properties.of().strength(3.5F).sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.BLOCK)));
    public static final RegistryObject<Item> STAND_ITEM = ITEMS.register("loot_stand", () -> new BlockItem(STAND.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<LootStandEntity>> STAND_ENTITY = ENTITIES.register("loot_stand", () ->
            BlockEntityType.Builder.of(LootStandEntity::new, STAND.get()).build(null));
    private static final RegistryObject<CreativeModeTab> TAB = TABS.register("loot", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ftdungeonloot")).icon(() -> STAND_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> output.accept(STAND_ITEM.get())).build());

    public FTDungeonLoot() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        TABS.register(modBus);
        MinecraftForge.EVENT_BUS.addListener(FTDungeonLoot::reload);
        MinecraftForge.EVENT_BUS.addListener(LootCommands::register);
        MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST, DungeonChests::interact);
        MinecraftForge.EVENT_BUS.addListener(FTDungeonLoot::tick);
        MinecraftForge.EVENT_BUS.addListener(FTDungeonLoot::logout);
        MinecraftForge.EVENT_BUS.addListener(FTDungeonLoot::stop);
    }

    private static void reload(AddReloadListenerEvent event) { event.addListener(LootProfiles.INSTANCE); }
    private static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.getServer().getTickCount() % 20 == 0) {
            LootTrades.expire(event.getServer().overworld().getGameTime());
        }
    }
    private static void logout(PlayerEvent.PlayerLoggedOutEvent event) { LootTrades.cancel(event.getEntity().getUUID()); }
    private static void stop(ServerStoppingEvent event) { LootTrades.clear(); }
}
