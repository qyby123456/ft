package net.farmingtales.ftdungeonmobs;

import java.util.List;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BloodthornBrazierBlock extends Block {
    public static final IntegerProperty TIER = IntegerProperty.create("tier", 0, 5);
    private static final ChatFormatting[] FORMATS = {ChatFormatting.GRAY, ChatFormatting.AQUA, ChatFormatting.GREEN, ChatFormatting.GOLD,
            ChatFormatting.LIGHT_PURPLE, ChatFormatting.RED};
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 10, 16);

    public BloodthornBrazierBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TIER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos position = context.getClickedPos();
        Level level = context.getLevel();
        if (!level.getFluidState(position).isEmpty() || !defaultBlockState().canSurvive(level, position)) {
            return null;
        }
        return defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos position) {
        BlockPos support = position.below();
        return level.getBlockState(support).isFaceSturdy(level, support, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
            LevelAccessor level, BlockPos position, BlockPos neighborPosition) {
        if (direction == Direction.DOWN && !state.canSurvive(level, position)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbor, level, position, neighborPosition);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos position, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator() || !level.mayInteract(player, position)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            if (player instanceof ServerPlayer serverPlayer && DungeonGateTravel.brazierLocked(serverPlayer.serverLevel(), position)) {
                DungeonGateInteraction.showLocked(serverPlayer);
                return InteractionResult.CONSUME;
            }
            int tier = (state.getValue(TIER) + 1) % 6;
            int changed = ArenaDifficultyApi.setTier((ServerLevel) level, position, tier);
            MutableComponent message = tier == 0 ? Component.translatable("message.ftdungeonmobs.bloodthorn_unlit", changed)
                    : Component.translatable("message.ftdungeonmobs.bloodthorn_tier", tier,
                    Component.translatable("tier.ftdungeonmobs." + tier),
                    DungeonDifficultyConfig.settings().arenaRadius(), changed);
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
            ArenaDifficultyApi.register(serverLevel, position, state.getValue(TIER));
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos position, BlockState replacement, boolean moving) {
        super.onRemove(state, level, position, replacement, moving);
        if (level instanceof ServerLevel serverLevel && !replacement.is(this)) {
            ArenaDifficultyApi.unregister(serverLevel, position);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ftdungeonmobs.arena_torch.cycle").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.ftdungeonmobs.bloodthorn_brazier.radius").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ftdungeonmobs.bloodthorn_brazier.flame").withStyle(ChatFormatting.DARK_RED));
    }
}
