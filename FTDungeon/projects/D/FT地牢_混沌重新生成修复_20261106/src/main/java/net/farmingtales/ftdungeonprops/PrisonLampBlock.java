package net.farmingtales.ftdungeonprops;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class PrisonLampBlock extends PrisonDecorBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public PrisonLampBlock(BlockBehaviour.Properties properties, PrisonCatalog.Spec spec) {
        super(properties, spec);
        registerDefaultState(defaultBlockState().setValue(LIT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos position, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!player.mayBuild() || !level.mayInteract(player, position)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            BlockState next = state.cycle(LIT);
            level.setBlock(position, next, Block.UPDATE_ALL);
            level.playSound(null, position, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS,
                    0.35F, next.getValue(LIT) ? 0.65F : 0.5F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
