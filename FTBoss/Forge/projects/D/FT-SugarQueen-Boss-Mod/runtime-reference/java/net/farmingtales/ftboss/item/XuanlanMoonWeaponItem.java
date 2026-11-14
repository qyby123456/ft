/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.UseAnim
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import java.util.function.Consumer;
import net.farmingtales.ftboss.client.render.XuanlanWeaponRenderer;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.PlayerMoonChakramEntity;
import net.farmingtales.ftboss.skill.XuanlanCombat;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.farmingtales.ftboss.util.XuanlanMotionRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class XuanlanMoonWeaponItem
extends Item {
    public XuanlanMoonWeaponItem() {
        super(new Item.Properties().m_41503_(768).m_41497_(Rarity.EPIC));
    }

    public static boolean isWhite(ItemStack stack) {
        return stack.m_41783_() != null && stack.m_41783_().m_128471_("XuanlanWhite");
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (player.m_36335_().m_41519_((Item)this)) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        if (player.m_6144_()) {
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                boolean white = !XuanlanMoonWeaponItem.isWhite(stack);
                stack.m_41784_().m_128379_("XuanlanWhite", white);
                player.m_36335_().m_41524_((Item)this, 4);
                player.m_5661_((Component)Component.m_237115_((String)(white ? "message.ftboss.xuanlan.white" : "message.ftboss.xuanlan.green")), true);
                level.m_5594_(null, player.m_20183_(), SoundEvents.f_144243_, SoundSource.PLAYERS, 0.7f, white ? 1.5f : 0.8f);
                XuanlanFxPacket.send(serverPlayer.m_284548_(), 4, player.m_19879_(), player.m_146892_(), player.m_20182_());
            }
            return InteractionResultHolder.m_19092_((Object)stack, (boolean)level.f_46443_);
        }
        if (!XuanlanMoonWeaponItem.isWhite(stack)) {
            player.m_6672_(hand);
            return InteractionResultHolder.m_19096_((Object)stack);
        }
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            if (!XuanlanCombat.canLaunchBlade(serverPlayer)) {
                player.m_5661_((Component)Component.m_237115_((String)"message.ftboss.xuanlan.returning"), true);
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            PlayerMoonChakramEntity blade = new PlayerMoonChakramEntity((EntityType<? extends PlayerMoonChakramEntity>)((EntityType)FTEntities.PLAYER_MOON_CHAKRAM.get()), level);
            blade.m_5602_((Entity)player);
            blade.setLaunchHand(hand);
            blade.m_146884_(player.m_146892_().m_82520_(0.0, -0.15, 0.0));
            blade.m_20256_(player.m_20154_().m_82490_(1.25));
            if (level.m_7967_((Entity)blade)) {
                int slot = XuanlanCombat.trackBlade(serverPlayer, blade);
                if (slot < 0) {
                    blade.m_146870_();
                    return InteractionResultHolder.m_19100_((Object)stack);
                }
                XuanlanFxPacket.send(serverPlayer.m_284548_(), XuanlanMotionRules.throwKind(slot, hand == InteractionHand.OFF_HAND), player.m_19879_(), player.m_146892_(), blade.m_20182_());
                stack.m_41622_(1, (LivingEntity)player, owner -> owner.m_21190_(hand));
                player.m_36335_().m_41524_((Item)this, 8);
                level.m_5594_(null, player.m_20183_(), SoundEvents.f_12520_, SoundSource.PLAYERS, 0.8f, 1.25f);
            }
        }
        return InteractionResultHolder.m_19092_((Object)stack, (boolean)level.f_46443_);
    }

    public void m_5551_(ItemStack stack, Level level, LivingEntity entity, int remainingTicks) {
        ServerPlayer player;
        int chargeTicks;
        block3: {
            block2: {
                chargeTicks = this.m_8105_(stack) - remainingTicks;
                if (!(entity instanceof ServerPlayer)) break block2;
                player = (ServerPlayer)entity;
                if (!XuanlanMoonWeaponItem.isWhite(stack) && chargeTicks >= 4 && !player.m_36335_().m_41519_((Item)this)) break block3;
            }
            return;
        }
        InteractionHand hand = player.m_7655_();
        XuanlanCombat.fire(player, chargeTicks);
        XuanlanFxPacket.send(player.m_284548_(), hand == InteractionHand.MAIN_HAND ? 6 : 7, player.m_19879_(), player.m_146892_(), player.m_20182_());
        stack.m_41622_(1, (LivingEntity)player, owner -> owner.m_21190_(hand));
        player.m_36335_().m_41524_((Item)this, 8);
        level.m_5594_(null, player.m_20183_(), SoundEvents.f_12517_, SoundSource.PLAYERS, 0.65f, 1.6f);
    }

    public int m_8105_(ItemStack stack) {
        return 72000;
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private BlockEntityWithoutLevelRenderer renderer;

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new XuanlanWeaponRenderer();
                }
                return this.renderer;
            }
        });
    }

    public UseAnim m_6164_(ItemStack stack) {
        return UseAnim.BOW;
    }

    public boolean m_6832_(ItemStack stack, ItemStack ingredient) {
        return ingredient.m_150930_(Items.f_151049_);
    }

    public int m_6473_() {
        return 12;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.f_44986_ || enchantment == Enchantments.f_44962_;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return EnchantmentHelper.m_44831_((ItemStack)book).keySet().stream().allMatch(enchantment -> this.canApplyAtEnchantingTable(stack, (Enchantment)enchantment));
    }

    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add((Component)Component.m_237115_((String)(XuanlanMoonWeaponItem.isWhite(stack) ? "message.ftboss.xuanlan.white" : "message.ftboss.xuanlan.green")).m_130940_(ChatFormatting.AQUA));
        for (String key : List.of("switch", "green", "white", "combo", "repair")) {
            tooltip.add((Component)Component.m_237115_((String)("tooltip.ftboss.xuanlan." + key)).m_130940_(ChatFormatting.GRAY));
        }
    }
}

