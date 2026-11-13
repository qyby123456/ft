package net.farmingtales.ftdungeonmobs;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BloodPoolContent {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, FTDungeonMobs.ID);
    public static final DeferredRegister<net.minecraft.world.level.material.Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, FTDungeonMobs.ID);
    public static final RegistryObject<FluidType> BLOOD_TYPE = FLUID_TYPES.register("sacrificial_blood", () -> new FluidType(
            FluidType.Properties.create().density(1400).viscosity(1800).temperature(300).motionScale(0.007)
                    .canSwim(true).canDrown(true).canHydrate(false).canExtinguish(false).canConvertToSource(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return FTDungeonMobs.resource("block/sacrificial_blood_still");
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return FTDungeonMobs.resource("block/sacrificial_blood_flow");
                }
            });
        }
    });
    public static final RegistryObject<FlowingFluid> BLOOD = FLUIDS.register("sacrificial_blood", () -> new ForgeFlowingFluid.Source(fluidProperties()));
    public static final RegistryObject<FlowingFluid> FLOWING_BLOOD = FLUIDS.register("flowing_sacrificial_blood", () -> new ForgeFlowingFluid.Flowing(fluidProperties()));
    public static final RegistryObject<LiquidBlock> BLOOD_BLOCK = FTDungeonMobs.BLOCKS.register("sacrificial_blood", () -> new LiquidBlock(
            BLOOD, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable().pushReaction(PushReaction.DESTROY)));
    public static final RegistryObject<Item> BLOOD_BUCKET = FTDungeonMobs.ITEMS.register("sacrificial_blood_bucket", () -> new BucketItem(
            BLOOD, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final RegistryObject<Block> BRICKS = FTDungeonMobs.BLOCKS.register("bloodstone_bricks", () -> new Block(stone()));
    public static final RegistryObject<Block> RUNES = FTDungeonMobs.BLOCKS.register("blood_rune_block", () -> new BloodPoolDecorBlock(stone(), BloodPoolDecorBlock.Kind.RUNE));
    public static final RegistryObject<Block> RIM = FTDungeonMobs.BLOCKS.register("blood_pool_rim", () -> new BloodPoolDecorBlock(stone().noOcclusion(), BloodPoolDecorBlock.Kind.RIM));
    public static final RegistryObject<Block> CORNER = FTDungeonMobs.BLOCKS.register("blood_pool_corner", () -> new BloodPoolDecorBlock(stone().noOcclusion(), BloodPoolDecorBlock.Kind.CORNER));
    public static final RegistryObject<Block> SKULL = FTDungeonMobs.BLOCKS.register("blood_skull_relief", () -> new BloodPoolDecorBlock(stone().noOcclusion(), BloodPoolDecorBlock.Kind.SKULL));
    public static final RegistryObject<Block> SHRINE = FTDungeonMobs.BLOCKS.register("blood_skull_shrine", () -> new BloodPoolDecorBlock(stone().noOcclusion(), BloodPoolDecorBlock.Kind.SHRINE));
    public static final RegistryObject<StairBlock> STAIRS = FTDungeonMobs.BLOCKS.register("bloodstone_stairs", () -> new StairBlock(() -> BRICKS.get().defaultBlockState(), stone()));
    public static final RegistryObject<SlabBlock> SLAB = FTDungeonMobs.BLOCKS.register("bloodstone_slab", () -> new SlabBlock(stone()));
    public static final RegistryObject<BloodOfferingAltarBlock> ALTAR = FTDungeonMobs.BLOCKS.register("blood_offering_altar", () -> new BloodOfferingAltarBlock(stone().noOcclusion()
            .pushReaction(PushReaction.BLOCK).lightLevel(state -> state.getValue(BloodOfferingAltarBlock.PHASE) == 2 ? 12
                    : state.getValue(BloodOfferingAltarBlock.PHASE) == 1 ? 5 : 0)));
    public static final List<RegistryObject<? extends Block>> BUILDING_BLOCKS = List.of(BRICKS, RUNES, RIM, CORNER, SKULL, SHRINE, STAIRS, SLAB, ALTAR);
    public static final List<RegistryObject<Item>> BUILDING_ITEMS = BUILDING_BLOCKS.stream().map(block -> FTDungeonMobs.ITEMS.<Item>register(
            block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()))).toList();
    public static final RegistryObject<BlockEntityType<BloodOfferingAltarEntity>> ALTAR_ENTITY = FTDungeonMobs.BLOCK_ENTITY_TYPES.register(
            "blood_offering_altar", () -> BlockEntityType.Builder.of(BloodOfferingAltarEntity::new, ALTAR.get()).build(null));

    private BloodPoolContent() {}

    public static void register(IEventBus bus) {
        FLUID_TYPES.register(bus);
        FLUIDS.register(bus);
    }

    private static BlockBehaviour.Properties stone() {
        return BlockBehaviour.Properties.of().strength(3.5F, 9.0F).sound(SoundType.DEEPSLATE_BRICKS)
                .lightLevel(state -> state.hasProperty(BloodPoolDecorBlock.LIT) && state.getValue(BloodPoolDecorBlock.LIT) ? 7 : 0);
    }

    private static ForgeFlowingFluid.Properties fluidProperties() {
        return new ForgeFlowingFluid.Properties(BLOOD_TYPE, BLOOD, FLOWING_BLOOD)
                .bucket(BLOOD_BUCKET).block(BLOOD_BLOCK).tickRate(10).slopeFindDistance(3).levelDecreasePerBlock(2).explosionResistance(100);
    }
}
