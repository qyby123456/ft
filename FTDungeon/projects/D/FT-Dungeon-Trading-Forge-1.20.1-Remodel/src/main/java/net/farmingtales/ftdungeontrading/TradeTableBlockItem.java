package net.farmingtales.ftdungeontrading;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class TradeTableBlockItem extends BlockItem {
    public TradeTableBlockItem(Block block, Item.Properties properties) { super(block, properties); }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        TradeTableBlock block = (TradeTableBlock) getBlock();
        if (!block.canPlacePartner(context, state)) return false;
        var level = context.getLevel();
        BlockPos primary = context.getClickedPos();
        BlockPos partner = TradeTableBlock.partnerPosition(state, primary);
        BlockState previous = level.getBlockState(primary);
        if (!super.placeBlock(context, state)) return false;
        if (level.setBlock(partner, state.setValue(TradeTableBlock.PART, TradeTableBlock.TablePart.RIGHT), 3)) return true;
        level.setBlock(primary, previous, 35);
        return false;
    }
}
