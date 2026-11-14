package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.skill.YouziStatueSkills;
import net.farmingtales.ftboss.util.YouziStatueRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public final class YouziStatueItem extends SwordItem {
    public YouziStatueItem() {
        super(Tiers.IRON, 3, -2.2F, new Properties().durability(512).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isAlive() || player.isSpectator() || player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (player instanceof ServerPlayer serverPlayer && !YouziStatueSkills.begin(serverPlayer, hand, player.isShiftKeyDown())) {
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.ftboss.statue_youzi.origin").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.ftboss.statue_youzi.echo", YouziStatueRules.ECHO_COUNT,
                (int) YouziStatueRules.echoDamage(), (int) YouziStatueRules.ECHO_RANGE,
                YouziStatueRules.ECHO_COOLDOWN / 20).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.ftboss.statue_youzi.ward", (int) (YouziStatueRules.WARD_REDUCTION * 100),
                YouziStatueRules.WARD_DURATION / 20, YouziStatueRules.WARD_COOLDOWN / 20).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.ftboss.statue_youzi.limit").withStyle(ChatFormatting.GRAY));
    }
}
