/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.SimpleMenuProvider
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.minecraftforge.network.NetworkHooks
 */
package net.farmingtales.ftboss.block;

import net.farmingtales.ftboss.menu.BossPreviewMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

public final class BlankStatueBlock
extends Block {
    private static final VoxelShape SHAPE = Shapes.m_83110_((VoxelShape)Block.m_49796_((double)1.0, (double)0.0, (double)1.0, (double)15.0, (double)6.5, (double)15.0), (VoxelShape)Block.m_49796_((double)4.0, (double)6.5, (double)4.0, (double)12.0, (double)16.0, (double)12.0));

    public BlankStatueBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public VoxelShape m_5940_(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    public InteractionResult m_6227_(BlockState blockState, Level level, BlockPos blockPos, Player player2, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (player2 instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player2;
            NetworkHooks.openScreen((ServerPlayer)serverPlayer, (MenuProvider)new SimpleMenuProvider((n, inventory, player) -> {
                BossPreviewMenu bossPreviewMenu = new BossPreviewMenu(n, inventory, blockPos);
                bossPreviewMenu.insertFromHand(interactionHand);
                return bossPreviewMenu;
            }, (Component)Component.m_237115_((String)"block.ftboss.statue_blank")), (BlockPos)blockPos);
        }
        return InteractionResult.m_19078_((boolean)level.f_46443_);
    }

    public void m_214162_(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (randomSource.m_188503_(4) == 0) {
            level.m_7106_((ParticleOptions)ParticleTypes.f_123810_, (double)blockPos.m_123341_() + 0.5 + (randomSource.m_188500_() - 0.5) * 0.5, (double)blockPos.m_123342_() + 0.85, (double)blockPos.m_123343_() + 0.5 + (randomSource.m_188500_() - 0.5) * 0.5, 0.0, 0.01, 0.0);
        }
    }
}

