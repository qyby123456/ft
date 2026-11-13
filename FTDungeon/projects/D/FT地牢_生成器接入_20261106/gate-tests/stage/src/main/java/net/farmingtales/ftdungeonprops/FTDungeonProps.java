package net.farmingtales.ftdungeonprops;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(FTDungeonProps.MOD_ID)
public final class FTDungeonProps {
    public static final String MOD_ID = "ftdungeonprops";
    public static final String AUTHOR = "一颗大豆 / B站 UID355533780";
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final Map<String, RegistryObject<Block>> BLOCK_MAP = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> ITEM_MAP = new LinkedHashMap<>();

    static {
        for (PrisonCatalog.Spec spec : PrisonCatalog.ALL) {
            RegistryObject<Block> block = BLOCKS.register(spec.id(), () -> createBlock(spec));
            BLOCK_MAP.put(spec.id(), block);
            ITEM_MAP.put(spec.id(), ITEMS.register(spec.id(), () -> new BlockItem(block.get(), new Item.Properties())));
        }
    }

    public FTDungeonProps() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        bus.addListener(FTDungeonProps::addVanillaTabItems);
    }

    private static Block createBlock(PrisonCatalog.Spec spec) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE).strength(2.5F, 6.0F)
                .requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE);
        return switch (spec.kind()) {
            case "solid" -> new Block(properties);
            case "stairs" -> new StairBlock(() -> BLOCK_MAP.get(spec.base()).get().defaultBlockState(), properties);
            case "slab" -> new SlabBlock(properties);
            default -> {
                properties.noOcclusion();
                if (spec.light() > 0) {
                    properties.lightLevel(state -> state.getValue(PrisonLampBlock.LIT) ? spec.light() : 0);
                    yield new PrisonLampBlock(properties, spec);
                }
                yield new PrisonDecorBlock(properties, spec);
            }
        };
    }

    private static void addVanillaTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            PrisonCatalog.ALL.stream().filter(spec -> spec.id().equals("prison_blue_bricks")
                    || spec.id().equals("prison_damp_bricks") || spec.id().equals("prison_fractured_bricks")
                    || spec.id().equals("prison_inlaid_tiles") || spec.kind().equals("stairs") || spec.kind().equals("slab"))
                    .forEach(spec -> event.accept(ITEM_MAP.get(spec.id())));
        }
    }
}
