package net.farmingtales.ftdungeonmobs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.item.BlockItem;

@Mod(FTDungeonMobs.ID)
public final class FTDungeonMobs {
    public static final String ID = "ftdungeonmobs";
    public static final String MODEL_PROVIDER = "一颗大豆，b站uid355533780";
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, ID);

    public static final RegistryObject<EntityType<DungeonSkeletonEntity>> WARRIOR = registerSkeleton(SkeletonVariant.WARRIOR);
    public static final RegistryObject<EntityType<DungeonSkeletonEntity>> ARCHER = registerSkeleton(SkeletonVariant.ARCHER);
    public static final RegistryObject<EntityType<DungeonSkeletonEntity>> MAGE = registerSkeleton(SkeletonVariant.MAGE);
    public static final RegistryObject<EntityType<DungeonSkeletonEntity>> KING = registerSkeleton(SkeletonVariant.KING);

    public static final RegistryObject<Item> WARRIOR_EGG = registerEgg(WARRIOR, 0xDDD6C2, 0x727787);
    public static final RegistryObject<Item> ARCHER_EGG = registerEgg(ARCHER, 0xDDD6C2, 0x74634D);
    public static final RegistryObject<Item> MAGE_EGG = registerEgg(MAGE, 0xDDD6C2, 0x7241A6);
    public static final RegistryObject<Item> KING_EGG = registerEgg(KING, 0xDDD6C2, 0xD7AC3B);
    public static final RegistryObject<DungeonSpawnPointBlock> DUNGEON_SPAWN_POINT = BLOCKS.register("dungeon_spawn_point",
            () -> new DungeonSpawnPointBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F)
                    .sound(SoundType.STONE).noCollission().noOcclusion().noLootTable()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)
                    .lightLevel(state -> state.getValue(DungeonSpawnPointBlock.SPENT) ? 0 : 3)));
    public static final RegistryObject<ArenaTorchBlock> ARENA_TORCH = BLOCKS.register("arena_torch", () -> new ArenaTorchBlock(
            BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL).noOcclusion()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                    .lightLevel(state -> state.getValue(ArenaTorchBlock.HALF) == DoubleBlockHalf.LOWER
                            && state.getValue(ArenaTorchBlock.TIER) > 0 ? 14 : 0)));
    public static final RegistryObject<Item> ARENA_TORCH_ITEM = ITEMS.register("arena_torch", FTDungeonMobs::legacyArenaItem);
    public static final RegistryObject<BloodthornBrazierBlock> BLOODTHORN_BRAZIER = BLOCKS.register("bloodthorn_brazier", () -> new BloodthornBrazierBlock(
            BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL).noOcclusion()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                    .lightLevel(state -> state.getValue(BloodthornBrazierBlock.TIER) > 0 ? 14 : 0)));
    public static final RegistryObject<Item> BLOODTHORN_BRAZIER_ITEM = ITEMS.register("bloodthorn_brazier",
            () -> new BlockItem(BLOODTHORN_BRAZIER.get(), new Item.Properties()));
    public static final RegistryObject<DungeonGateBlock> DUNGEON_ENTRY_GATE = registerGate("dungeon_entry_gate", false);
    public static final RegistryObject<DungeonGateBlock> DUNGEON_RETURN_GATE = registerGate("dungeon_return_gate", true);
    public static final RegistryObject<DungeonGateBlock> DUNGEON_BOSS_GATE = BLOCKS.register("dungeon_boss_gate",
            () -> new DungeonGateBlock(BlockBehaviour.Properties.of().strength(4.0F, 12.0F).sound(SoundType.METAL)
                    .noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)
                    .lightLevel(state -> 7), false, true));
    public static final RegistryObject<Item> DUNGEON_BOSS_GATE_ITEM = ITEMS.register("dungeon_boss_gate",
            () -> new BlockItem(DUNGEON_BOSS_GATE.get(), new Item.Properties()));
    public static final RegistryObject<Item> DUNGEON_ENTRY_GATE_ITEM = ITEMS.register("dungeon_entry_gate",
            () -> new BlockItem(DUNGEON_ENTRY_GATE.get(), new Item.Properties()));
    public static final RegistryObject<Item> DUNGEON_RETURN_GATE_ITEM = ITEMS.register("dungeon_return_gate",
            () -> new BlockItem(DUNGEON_RETURN_GATE.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<ArenaTorchBlockEntity>> ARENA_TORCH_ENTITY = BLOCK_ENTITY_TYPES.register(
            "arena_torch", () -> BlockEntityType.Builder.of(ArenaTorchBlockEntity::new, ARENA_TORCH.get()).build(null));
    public static final RegistryObject<BlockEntityType<DungeonGateBlockEntity>> DUNGEON_GATE_ENTITY = BLOCK_ENTITY_TYPES.register(
            "dungeon_gate", () -> BlockEntityType.Builder.of(DungeonGateBlockEntity::new,
                    DUNGEON_ENTRY_GATE.get(), DUNGEON_RETURN_GATE.get(), DUNGEON_BOSS_GATE.get()).build(null));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("dungeon_mobs", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ftdungeonmobs"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> KING_EGG.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(WARRIOR_EGG.get());
                output.accept(ARCHER_EGG.get());
                output.accept(MAGE_EGG.get());
                output.accept(KING_EGG.get());
                output.accept(BLOODTHORN_BRAZIER_ITEM.get());
                output.accept(DUNGEON_ENTRY_GATE_ITEM.get());
                output.accept(DUNGEON_RETURN_GATE_ITEM.get());
                output.accept(DUNGEON_BOSS_GATE_ITEM.get());
                BloodPoolContent.BUILDING_ITEMS.forEach(item -> output.accept(item.get()));
                output.accept(BloodPoolContent.BLOOD_BUCKET.get());
                DungeonArchitectureContent.ENTRIES.forEach(entry -> output.accept(entry.item().get()));
            }).build());

    public FTDungeonMobs() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        BloodPoolContent.register(modBus);
        DungeonArchitectureContent.register(modBus);
        ENTITIES.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        TABS.register(modBus);
        modBus.addListener(this::createAttributes);
        modBus.addListener(DungeonDifficultyConfig::onConfig);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DungeonDifficultyConfig.SPEC, DungeonDifficultyConfig.FILE_NAME);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DungeonSpawnConfig.SPEC, DungeonSpawnConfig.FILE_NAME);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(DungeonDifficultyApi::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(DungeonGateTravel::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(DungeonSpawnPoints::onLevelTick);
        MinecraftForge.EVENT_BUS.addListener(DungeonSpawnPoints::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(DungeonGateTravel::onPlayerClone);
        MinecraftForge.EVENT_BUS.addListener(DungeonGateTravel::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(DungeonGateTravel::onPlayerRespawn);
        MinecraftForge.EVENT_BUS.addListener(DungeonGateTravel::onPlayerDimension);
        MinecraftForge.EVENT_BUS.addListener(DungeonGateTravel::onBreak);
        MinecraftForge.EVENT_BUS.addListener(DungeonGateTravel::onPlace);
        MinecraftForge.EVENT_BUS.addListener(DungeonGateTravel::onExplosion);
        MinecraftForge.EVENT_BUS.addListener(DungeonGateTravel::onPiston);
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(ID, path);
    }

    private static Item legacyArenaItem() {
        return new BlockItem(BLOODTHORN_BRAZIER.get(), new Item.Properties());
    }

    private static RegistryObject<DungeonGateBlock> registerGate(String name, boolean returning) {
        return BLOCKS.register(name, () -> new DungeonGateBlock(BlockBehaviour.Properties.of()
                .strength(4.0F, 12.0F).sound(SoundType.METAL).noOcclusion()
                .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK).lightLevel(state -> 7), returning));
    }

    private static RegistryObject<EntityType<DungeonSkeletonEntity>> registerSkeleton(SkeletonVariant variant) {
        return ENTITIES.register(variant.id(), () -> EntityType.Builder
                .<DungeonSkeletonEntity>of((entityType, level) -> new DungeonSkeletonEntity(entityType, level, variant), MobCategory.MONSTER)
                .sized(variant.width(), variant.height())
                .clientTrackingRange(10)
                .build(ID + ":" + variant.id()));
    }

    private static RegistryObject<Item> registerEgg(RegistryObject<EntityType<DungeonSkeletonEntity>> entity, int primary, int secondary) {
        return ITEMS.register(entity.getId().getPath() + "_spawn_egg", () -> new ForgeSpawnEggItem(entity, primary, secondary, new Item.Properties()));
    }

    private void createAttributes(EntityAttributeCreationEvent event) {
        event.put(WARRIOR.get(), DungeonSkeletonEntity.createAttributes(SkeletonVariant.WARRIOR).build());
        event.put(ARCHER.get(), DungeonSkeletonEntity.createAttributes(SkeletonVariant.ARCHER).build());
        event.put(MAGE.get(), DungeonSkeletonEntity.createAttributes(SkeletonVariant.MAGE).build());
        event.put(KING.get(), DungeonSkeletonEntity.createAttributes(SkeletonVariant.KING).build());
    }

    private void registerCommands(RegisterCommandsEvent event) {
        DungeonCommands.register(event.getDispatcher());
        DungeonBossGateCommands.register(event.getDispatcher());
        BloodPoolCommands.register(event.getDispatcher());
    }
}
