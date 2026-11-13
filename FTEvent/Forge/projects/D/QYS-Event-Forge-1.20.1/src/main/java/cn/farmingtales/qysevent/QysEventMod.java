package cn.farmingtales.qysevent;

import com.mojang.serialization.Codec;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import cn.farmingtales.qysevent.worldevent.DailyWorldEventCommand;
import cn.farmingtales.qysevent.worldevent.DailyWorldEventHooks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(QysEventMod.MOD_ID)
public final class QysEventMod {
    public static final String MOD_ID = "qys_event";
    public static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);

    public static final RegistryObject<Codec<WeatherCropLootModifier>> CROP_YIELD_MODIFIER =
            LOOT_MODIFIERS.register("crop_yield", () -> WeatherCropLootModifier.CODEC);

    public QysEventMod() {
        WeatherNetwork.register();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        LOOT_MODIFIERS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlocks.ITEMS.register(modBus);
        modBus.addListener(this::addCreativeTabItems);
        MinecraftForge.EVENT_BUS.addListener(this::remapLegacyContent);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WeatherConfig.SPEC, "qys_event-common.toml");
        MinecraftForge.EVENT_BUS.register(WeatherManager.class);
        MinecraftForge.EVENT_BUS.register(WeatherCommands.class);
        MinecraftForge.EVENT_BUS.register(DailyWorldEventHooks.class);
        MinecraftForge.EVENT_BUS.register(DailyWorldEventCommand.class);
    }


    private void remapLegacyContent(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<net.minecraft.world.level.block.Block> mapping
                : event.getMappings(Registries.BLOCK, "solarweather")) {
            if (mapping.getKey().getPath().equals("weather_forecaster")) mapping.remap(ModBlocks.WEATHER_FORECASTER.get());
        }
        for (MissingMappingsEvent.Mapping<net.minecraft.world.item.Item> mapping
                : event.getMappings(Registries.ITEM, "solarweather")) {
            if (mapping.getKey().getPath().equals("weather_forecaster")) mapping.remap(ModBlocks.WEATHER_FORECASTER_ITEM.get());
        }
        for (MissingMappingsEvent.Mapping<net.minecraft.world.level.block.Block> mapping
                : event.getMappings(Registries.BLOCK, "ftboss")) {
            if (mapping.getKey().getPath().equals("oracle")) mapping.remap(ModBlocks.ORACLE.get());
        }
        for (MissingMappingsEvent.Mapping<net.minecraft.world.item.Item> mapping
                : event.getMappings(Registries.ITEM, "ftboss")) {
            if (mapping.getKey().getPath().equals("oracle")) mapping.remap(ModBlocks.ORACLE_ITEM.get());
        }
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS || event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ModBlocks.WEATHER_FORECASTER_ITEM);
            event.accept(ModBlocks.ORACLE_ITEM);
            event.accept(ModBlocks.WISHING_WELL_ITEM);
        }
    }
}

