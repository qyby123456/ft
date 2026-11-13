package net.farmingtales.ftdungeonmobs;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ArenaTorchBlock extends BaseEntityBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final IntegerProperty TIER = BloodthornBrazierBlock.TIER;
    private static final ChatFormatting[] FORMATS = {ChatFormatting.GRAY, ChatFormatting.AQUA, ChatFormatting.GREEN, ChatFormatting.GOLD,
            ChatFormatting.LIGHT_PURPLE, ChatFormatting.RED};
    private static final VoxelShape LOWER_SHAPE = Block.box(0, 0, 0, 16, 10, 16);

    public ArenaTorchBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(TIER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, TIER);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new ArenaTorchBlockEntity(position, state) : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return null;
    }

    @Override
    public Item asItem() {
        return FTDungeonMobs.BLOODTHORN_BRAZIER_ITEM.get();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos position) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(position.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
        return level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
            BlockPos position, BlockPos neighborPosition) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y
                && ((half == DoubleBlockHalf.LOWER && direction == Direction.UP)
                || (half == DoubleBlockHalf.UPPER && direction == Direction.DOWN))) {
            if (!neighbor.is(this) || neighbor.getValue(HALF) == half) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canSurvive(level, position)) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockState result = super.updateShape(state, direction, neighbor, level, position, neighborPosition);
        if (result.is(this) && half == DoubleBlockHalf.UPPER && direction == Direction.DOWN && neighbor.is(this)) {
            return result.setValue(TIER, neighbor.getValue(TIER));
        }
        return result;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPE : Shapes.empty();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos position, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator() || !level.mayInteract(player, position)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            BlockPos source = state.getValue(HALF) == DoubleBlockHalf.LOWER ? position : position.below();
            if (player instanceof ServerPlayer serverPlayer && DungeonGateTravel.brazierLocked(serverPlayer.serverLevel(), source)) {
                DungeonGateInteraction.showLocked(serverPlayer);
                return InteractionResult.CONSUME;
            }
            int tier = (state.getValue(TIER) + 1) % 6;
            int changed = ArenaDifficultyApi.setTier((ServerLevel) level, position, tier);
            MutableComponent message = tier == 0 ? Component.translatable("message.ftdungeonmobs.bloodthorn_unlit", changed)
                    : Component.translatable("message.ftdungeonmobs.bloodthorn_tier", tier,
                    Component.translatable("tier.ftdungeonmobs." + tier),
                    changed);
            player.displayClientMessage(message.withStyle(FORMATS[tier]), true);
            level.playSound(null, position, tier == 0 ? SoundEvents.FIRE_EXTINGUISH : SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS,
                    0.8F, 0.7F + tier * 0.15F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos position, BlockState previous, boolean moving) {
        super.onPlace(state, level, position, previous, moving);
        if (level instanceof ServerLevel serverLevel && !previous.is(this)) {
            BlockPos base = state.getValue(HALF) == DoubleBlockHalf.LOWER ? position : position.below();
            BlockState lower = level.getBlockState(base);
            BlockState upper = level.getBlockState(base.above());
            if (lower.is(this) && upper.is(this) && lower.getValue(HALF) == DoubleBlockHalf.LOWER
                    && upper.getValue(HALF) == DoubleBlockHalf.UPPER) {
                ArenaDifficultyApi.register(serverLevel, base, lower.getValue(TIER));
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos position, BlockState replacement, boolean moving) {
        super.onRemove(state, level, position, replacement, moving);
        if (level instanceof ServerLevel serverLevel && state.getValue(HALF) == DoubleBlockHalf.LOWER
                && !replacement.is(this)) {
            ArenaDifficultyApi.unregister(serverLevel, position);
        }
    }
}
