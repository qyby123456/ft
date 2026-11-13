package cn.farmingtales.qysevent;

import cn.farmingtales.qysevent.worldevent.DailyWorldEventCommand;
import cn.farmingtales.qysevent.worldevent.DailyWorldEventHooks;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(QysEventMod.MOD_ID)
public final class QysEventMod {
    public static final String MOD_ID = "qys_event";
    public static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<WeatherCropLootModifier>> CROP_YIELD_MODIFIER =
            LOOT_MODIFIERS.register("crop_yield", () -> WeatherCropLootModifier.CODEC);

    public QysEventMod(IEventBus modBus, ModContainer container) {
        LOOT_MODIFIERS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlocks.ITEMS.register(modBus);
        modBus.addListener(this::addCreativeTabItems);
        modBus.addListener(WeatherNetwork::register);
        container.registerConfig(ModConfig.Type.COMMON, WeatherConfig.SPEC, "qys_event-common.toml");
        NeoForge.EVENT_BUS.register(WeatherManager.class);
        NeoForge.EVENT_BUS.register(WeatherCommands.class);
        NeoForge.EVENT_BUS.register(DailyWorldEventHooks.class);
        NeoForge.EVENT_BUS.register(DailyWorldEventCommand.class);
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS || event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ModBlocks.WEATHER_FORECASTER_ITEM);
            event.accept(ModBlocks.ORACLE_ITEM);
            event.accept(ModBlocks.WISHING_WELL_ITEM);
        }
    }
}


