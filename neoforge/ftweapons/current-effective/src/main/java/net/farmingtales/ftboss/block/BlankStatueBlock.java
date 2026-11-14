package net.farmingtales.ftboss.block;

import com.mojang.serialization.MapCodec;
import net.farmingtales.ftboss.menu.BossPreviewMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlankStatueBlock extends Block {
    public static final MapCodec<BlankStatueBlock> CODEC = simpleCodec(BlankStatueBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(Block.box(1.0, 0.0, 1.0, 15.0, 6.5, 15.0), Block.box(4.0, 6.5, 4.0, 12.0, 16.0, 12.0));

    public BlankStatueBlock(Properties var1) {
        super(var1);
    }

    @Override
    protected MapCodec<BlankStatueBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, BlockHitResult hit) {
        openPreview(player, position, hand);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos position, Player player, BlockHitResult hit) {
        openPreview(player, position, InteractionHand.MAIN_HAND);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void openPreview(Player player, BlockPos position, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider((containerId, inventory, owner) -> {
                BossPreviewMenu menu = new BossPreviewMenu(containerId, inventory, position);
                menu.insertFromHand(hand);
                return menu;
            }, Component.translatable("block.ftboss.statue_blank")), position);
        }
    }

    @Override
    public void animateTick(BlockState var1, Level var2, BlockPos var3, RandomSource var4) {
        if (var4.nextInt(4) == 0) {
            var2.addParticle(
                ParticleTypes.END_ROD,
                (double)var3.getX() + 0.5 + (var4.nextDouble() - 0.5) * 0.5,
                (double)var3.getY() + 0.85,
                (double)var3.getZ() + 0.5 + (var4.nextDouble() - 0.5) * 0.5,
                0.0,
                0.01,
                0.0
            );
        }
    }
}
