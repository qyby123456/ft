package net.farmingtales.ftboss.block;

import net.farmingtales.ftboss.menu.BossPreviewMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

/** 一格高投影水晶；2026-10-12：右键只读图鉴，不开始 Boss 战。 */
public final class BlankStatueBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(Block.box(1, 0, 1, 15, 6.5, 15), Block.box(4, 6.5, 4, 12, 16, 12));

    public BlankStatueBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
                               BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((containerId, inventory, owner) -> {
                BossPreviewMenu menu = new BossPreviewMenu(containerId, inventory, pos);
                menu.insertFromHand(hand);
                return menu;
            }, Component.translatable("block.ftboss.statue_blank")), pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) level.addParticle(ParticleTypes.END_ROD,
                pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5, pos.getY() + 0.85,
                pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5, 0.0, 0.01, 0.0);
    }
}
