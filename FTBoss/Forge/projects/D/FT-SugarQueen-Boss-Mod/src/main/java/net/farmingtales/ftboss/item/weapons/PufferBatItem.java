package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.util.PufferBatRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 【河豚棒】—— 彩蛋 Boss「紫菜」的掉落武器（2026-09-12 入库，2026-09-18 补技能）。
 *
 * <p>定位：庄园武器里唯一带"铺区域"能力的近战棒。普攻保持原来的钻石级手感
 * （{@code Tiers.DIAMOND}, +3 伤害修正），右键放出<b>胀气毒雾</b>：
 * 在瞄准落点留下一团持续 5 秒的毒云；潜行右键发动近身毒刺膨爆，击退敌人并对中毒目标增伤。
 *
 * <p>技能数值全部从 {@link PufferBatRules} 读，伤害再经
 * {@code ManorWeaponRules.skillDamage} 走庄园武器的「技能 ×1.5」口径。
 */
public final class PufferBatItem extends SwordItem {
    public PufferBatItem() {
        super(net.minecraft.world.item.Tiers.DIAMOND, 3, -2.0F,
                new Item.Properties().rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.isAlive() || player.isSpectator()) {
            return InteractionResultHolder.pass(stack);
        }
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        boolean shockwave = player.isShiftKeyDown();
        boolean cast = shockwave
                ? PufferBatCombat.shockwave(serverPlayer, stack, hand)
                : PufferBatCombat.cast(serverPlayer, stack, hand);
        if (!cast) {
            serverPlayer.displayClientMessage(Component.translatable("message.ftboss.puffer_bat.blocked")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }
        player.getCooldowns().addCooldown(this, shockwave
                ? PufferBatRules.SHOCKWAVE_COOLDOWN_TICKS : PufferBatRules.COOLDOWN_TICKS);
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.ftboss.puffer_bat.origin").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.ftboss.puffer_bat.skill",
                (int) PufferBatRules.CLOUD_RADIUS,
                PufferBatRules.CLOUD_TICKS / 20,
                (int) PufferBatRules.tickDamage(),
                (int) PufferBatRules.CAST_RANGE,
                PufferBatRules.COOLDOWN_TICKS / 20).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.ftboss.puffer_bat.debuff",
                PufferBatRules.POISON_TICKS / 20, PufferBatRules.SLOW_TICKS / 20).withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("tooltip.ftboss.puffer_bat.shockwave",
                (int) PufferBatRules.SHOCKWAVE_RADIUS,
                (int) PufferBatRules.shockwaveDamage(false),
                PufferBatRules.SHOCKWAVE_POISONED_MULTIPLIER,
                PufferBatRules.SHOCKWAVE_COOLDOWN_TICKS / 20,
                PufferBatRules.SHOCKWAVE_DURABILITY_COST)
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.ftboss.puffer_bat.rules",
                PufferBatRules.DURABILITY_COST, PufferBatRules.MAX_ACTIVE_CLOUDS).withStyle(ChatFormatting.GRAY));
    }
}
