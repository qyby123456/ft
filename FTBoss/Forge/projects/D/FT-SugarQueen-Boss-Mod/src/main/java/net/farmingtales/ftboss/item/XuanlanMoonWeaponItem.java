package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.skill.XuanlanBladeBurst;
import net.farmingtales.ftboss.skill.XuanlanCombat;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.farmingtales.ftboss.util.XuanlanRules;
import net.farmingtales.ftboss.util.XuanlanMotionRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public final class XuanlanMoonWeaponItem extends Item {
    public XuanlanMoonWeaponItem() {
        super(new Item.Properties().durability(768).rarity(Rarity.EPIC));
    }

    public static boolean isWhite(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean("XuanlanWhite");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                boolean white = !isWhite(stack);
                stack.getOrCreateTag().putBoolean("XuanlanWhite", white);
                player.getCooldowns().addCooldown(this, 4);
                player.displayClientMessage(Component.translatable(white ? "message.ftboss.xuanlan.white" : "message.ftboss.xuanlan.green"), true);
                level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, white ? 1.5F : 0.8F);
                XuanlanFxPacket.send(serverPlayer.serverLevel(), XuanlanFxPacket.SWITCH, player.getId(), player.getEyePosition(), player.position());
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (!isWhite(stack)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (!XuanlanCombat.canLaunchBlade(serverPlayer) || XuanlanBladeBurst.isFiring(serverPlayer)) {
                player.displayClientMessage(Component.translatable("message.ftboss.xuanlan.returning"), true);
                return InteractionResultHolder.fail(stack);
            }
            if (XuanlanBladeBurst.start(serverPlayer, hand, stack)) {
                stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(hand));
                player.getCooldowns().addCooldown(this, XuanlanRules.BLADE_COOLDOWN_TICKS);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTicks) {
        int chargeTicks = getUseDuration(stack) - remainingTicks;
        if (!(entity instanceof ServerPlayer player) || isWhite(stack)
                || chargeTicks < XuanlanRules.MIN_CHARGE_TICKS || player.getCooldowns().isOnCooldown(this)) return;
        InteractionHand hand = player.getUsedItemHand();
        XuanlanCombat.fire(player, chargeTicks);
        XuanlanFxPacket.send(player.serverLevel(), hand == InteractionHand.MAIN_HAND
                ? XuanlanMotionRules.SHOT_MAIN : XuanlanMotionRules.SHOT_OFF,
                player.getId(), player.getEyePosition(), player.position());
        stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(hand));
        player.getCooldowns().addCooldown(this, 8);
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS, 0.65F, 1.6F);
    }

    @Override
    public int getUseDuration(ItemStack stack) { return 72000; }

    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
            private net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new net.farmingtales.ftboss.client.render.XuanlanWeaponRenderer();
                return renderer;
            }
        });
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) { return ingredient.is(Items.AMETHYST_SHARD); }

    @Override
    public int getEnchantmentValue() { return 12; }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.MENDING;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return EnchantmentHelper.getEnchantments(book).keySet().stream()
                .allMatch(enchantment -> canApplyAtEnchantingTable(stack, enchantment));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(isWhite(stack) ? "message.ftboss.xuanlan.white" : "message.ftboss.xuanlan.green").withStyle(ChatFormatting.AQUA));
        for (String key : List.of("switch", "green", "white", "combo", "repair")) {
            tooltip.add(Component.translatable("tooltip.ftboss.xuanlan." + key).withStyle(ChatFormatting.GRAY));
        }
    }
}
