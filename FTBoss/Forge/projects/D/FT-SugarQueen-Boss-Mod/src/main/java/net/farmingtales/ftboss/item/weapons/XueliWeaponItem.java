package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.item.FTWeaponTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

public final class XueliWeaponItem extends SwordItem {
    private final boolean skillet;

    public XueliWeaponItem(boolean skillet) {
        super(FTWeaponTier.INSTANCE, skillet ? 8 : 10, skillet ? -2.5F : -2.9F,
                new Item.Properties().rarity(Rarity.EPIC).fireResistant());
        this.skillet = skillet;
    }

    public boolean isSkillet() {
        return this.skillet;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(stack);
        if (!player.isAlive() || player.isSpectator() || player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (this.skillet && !player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) XueliWeaponCombat.sweep(serverPlayer, stack);
        } else {
            player.startUsingItem(hand);
            if (this.skillet && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.getCooldowns().addCooldown(this, XueliWeaponRules.GUARD_COOLDOWN);
                XueliWeaponCombat.guardEffect(serverPlayer);
            }
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return this.skillet ? XueliWeaponRules.GUARD_DURATION : 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return this.skillet ? UseAnim.SPEAR : UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingTicks) {
        if (!this.skillet && user instanceof ServerPlayer player) {
            int elapsed = getUseDuration(stack) - remainingTicks;
            if (elapsed == XueliWeaponRules.MIN_CHARGE || elapsed == XueliWeaponRules.FULL_CHARGE) {
                XueliWeaponCombat.chargeEffect(player, elapsed == XueliWeaponRules.FULL_CHARGE);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingTicks) {
        if (this.skillet || !(user instanceof ServerPlayer player)) return;
        int charge = getUseDuration(stack) - remainingTicks;
        if (charge < XueliWeaponRules.MIN_CHARGE || player.getMainHandItem() != stack
                || !player.isAlive() || player.isSpectator() || player.getCooldowns().isOnCooldown(this)) return;
        XueliWeaponCombat.roll(player, stack, Math.min(charge, XueliWeaponRules.FULL_CHARGE));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (result && this.skillet && attacker instanceof ServerPlayer player && target.isAlive()
                && XueliWeaponCombat.canTarget(player, target)) XueliWeaponCombat.mark(player, target);
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String prefix = this.skillet ? "tooltip.ftboss.xueli_skillet." : "tooltip.ftboss.xueli_rolling_pin.";
        tooltip.add(Component.translatable("tooltip.ftboss.xueli.origin").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable(prefix + "skill").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable(prefix + "extra").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.ftboss.xueli.rules").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) this.renderer = new net.farmingtales.ftboss.client.render.XueliWeaponRenderer();
                return this.renderer;
            }
        });
    }
}
