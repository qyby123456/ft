/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.Container
 *  net.minecraft.world.Containers
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.BaseEntityBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.DirectionProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package net.farmingtales.ftboss.block;

import net.farmingtales.ftboss.block.ManorLootChestBlockEntity;
import net.farmingtales.ftboss.boss.ai.FoodBossInventoryRewards;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ManorLootChestBlock
extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.f_61374_;
    private static final VoxelShape SHAPE = Block.m_49796_((double)1.0, (double)0.0, (double)1.0, (double)15.0, (double)14.0, (double)15.0);

    public ManorLootChestBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.m_49959_((BlockState)((BlockState)this.f_49792_.m_61090_()).m_61124_((Property)FACING, (Comparable)Direction.NORTH));
    }

    protected void m_7926_(StateDefinition.Builder<Block, BlockState> builder) {
        builder.m_61104_(new Property[]{FACING});
    }

    public BlockState m_5573_(BlockPlaceContext context) {
        return (BlockState)this.m_49966_().m_61124_((Property)FACING, (Comparable)context.m_8125_().m_122424_());
    }

    public RenderShape m_7514_(BlockState state) {
        return RenderShape.MODEL;
    }

    public VoxelShape m_5940_(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) {
        return SHAPE;
    }

    public BlockEntity m_142194_(BlockPos position, BlockState state) {
        return new ManorLootChestBlockEntity(position, state);
    }

    public InteractionResult m_6227_(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity;
        if (!level.f_46443_ && (blockEntity = level.m_7702_(position)) instanceof ManorLootChestBlockEntity) {
            ManorLootChestBlockEntity chest = (ManorLootChestBlockEntity)blockEntity;
            FoodBossInventoryRewards.claimChest(chest, player);
            player.m_5893_((MenuProvider)chest);
        }
        return InteractionResult.m_19078_((boolean)level.f_46443_);
    }

    public void m_6810_(BlockState state, Level level, BlockPos position, BlockState replacement, boolean moving) {
        if (!state.m_60713_(replacement.m_60734_())) {
            BlockEntity blockEntity;
            if (!level.f_46443_ && (blockEntity = level.m_7702_(position)) instanceof ManorLootChestBlockEntity) {
                ManorLootChestBlockEntity chest = (ManorLootChestBlockEntity)blockEntity;
                Containers.m_19002_((Level)level, (BlockPos)position, (Container)chest);
                level.m_46717_(position, (Block)this);
            }
            super.m_6810_(state, level, position, replacement, moving);
        }
    }

    public boolean m_7278_(BlockState state) {
        return true;
    }

    public int m_6782_(BlockState state, Level level, BlockPos position) {
        return AbstractContainerMenu.m_38918_((BlockEntity)level.m_7702_(position));
    }
}

