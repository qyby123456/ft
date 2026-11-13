package cn.farmingtales.qysevent;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

/** Wishing-well item with no construction tutorial; only failed placement is reported. */
public final class WishingWellItem extends BlockItem {
    public WishingWellItem(Block block, Item.Properties properties) { super(block, properties); }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);
        Player player = context.getPlayer();
        if (!result.consumesAction() && !context.getLevel().isClientSide && player != null) {
            player.displayClientMessage(Component.translatable("message.qys_event.well.placement_error"), true);
        }
        return result;
    }
}
