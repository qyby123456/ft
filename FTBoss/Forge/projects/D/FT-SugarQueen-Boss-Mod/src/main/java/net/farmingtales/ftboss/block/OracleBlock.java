package net.farmingtales.ftboss.block;

import java.util.List;
import net.farmingtales.ftboss.worldevent.DailyWorldEventManager;
import net.farmingtales.ftboss.worldevent.DailyWorldEventRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Emerald-powered ten-day candidate-event forecast. The hidden 10% rolls are never disclosed. */
public final class OracleBlock extends HorizontalDirectionalBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public OracleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack payment = player.getItemInHand(hand);
        if (!payment.is(Items.EMERALD)) {
            if (hand == InteractionHand.MAIN_HAND && player.getOffhandItem().is(Items.EMERALD)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.translatable("message.ftboss.oracle.needs_emerald")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.CONSUME;
        }
        if (!player.getAbilities().instabuild) payment.shrink(1);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D,
                36, 0.65D, 0.55D, 0.65D, 0.08D);
        serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 1.15D, pos.getZ() + 0.5D,
                8, 0.22D, 0.4D, 0.22D, 0.02D);
        serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 0.85F);
        DailyWorldEventManager.showForecast(serverPlayer, DailyWorldEventRules.FORECAST_DAYS);
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ftboss.oracle").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ftboss.oracle.cost").withStyle(ChatFormatting.GREEN));
    }
}