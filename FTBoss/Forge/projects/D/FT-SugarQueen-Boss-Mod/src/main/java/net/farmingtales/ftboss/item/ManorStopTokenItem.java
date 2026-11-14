package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.boss.ManorFight;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ManorStopTokenItem extends Item {
    public ManorStopTokenItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isCreative() || !player.isShiftKeyDown()) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable(player.isCreative()
                    ? "message.ftboss.manor_stop_token.sneak" : "message.ftboss.manor_stop_token.creative_only"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            boolean stopped = ManorFight.stopForCreative(serverPlayer);
            player.displayClientMessage(Component.translatable(stopped
                    ? "message.ftboss.manor_stop_token.stopped" : "message.ftboss.manor_stop_token.none"), true);
            if (!stopped) return InteractionResultHolder.fail(stack);
            player.getCooldowns().addCooldown(this, 10);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        return player == null ? InteractionResult.PASS : use(context.getLevel(), player, context.getHand()).getResult();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.ftboss.manor_stop_token.creative_only").withStyle(ChatFormatting.LIGHT_PURPLE));
        lines.add(Component.translatable("tooltip.ftboss.manor_stop_token.use").withStyle(ChatFormatting.YELLOW));
        lines.add(Component.translatable("tooltip.ftboss.manor_stop_token.result").withStyle(ChatFormatting.GRAY));
    }
}
