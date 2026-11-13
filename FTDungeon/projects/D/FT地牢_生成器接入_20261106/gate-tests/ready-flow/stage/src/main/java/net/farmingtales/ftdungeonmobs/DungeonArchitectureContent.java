package net.farmingtales.ftdungeonmobs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class DungeonArchitectureContent {
    public record Entry(DungeonArchitectureCatalog.Model model, RegistryObject<Block> block, RegistryObject<Item> item) {}

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FTDungeonMobs.ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FTDungeonMobs.ID);
    public static final List<Entry> ENTRIES = createEntries();

    private DungeonArchitectureContent() {}

    private static List<Entry> createEntries() {
        List<Entry> entries = new ArrayList<>();
        Map<String, RegistryObject<Block>> blocks = new LinkedHashMap<>();
        for (DungeonArchitectureCatalog.Model model : DungeonArchitectureCatalog.MODELS) {
            RegistryObject<Block> block = BLOCKS.register(model.id(), () -> switch (model.nativeParent()) {
                case "stairs" -> new StairBlock(() -> blocks.get("dungeon_ashlar").get().defaultBlockState(), properties(model));
                case "slab" -> new SlabBlock(properties(model));
                default -> new DungeonArchitectureBlock(properties(model), model.boxes());
            });
            blocks.put(model.id(), block);
            RegistryObject<Item> item = ITEMS.register(model.id(), () -> new BlockItem(block.get(), new Item.Properties()));
            entries.add(new Entry(model, block, item));
        }
        return List.copyOf(entries);
    }

    private static BlockBehaviour.Properties properties(DungeonArchitectureCatalog.Model model) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().strength(3.5F, 9.0F)
                .sound(SoundType.DEEPSLATE_BRICKS).lightLevel(state -> model.light());
        return model.full() ? properties : properties.noOcclusion();
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
