/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import javax.annotation.Nullable;
import net.farmingtales.ftboss.item.weapons.PufferBatCombat;
import net.farmingtales.ftboss.util.PufferBatRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class PufferBatItem
extends SwordItem {
    public PufferBatItem() {
        super((Tier)Tiers.DIAMOND, 3, -2.0f, new Item.Properties().m_41497_(Rarity.EPIC));
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        boolean cast;
        ItemStack stack = player.m_21120_(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.m_6084_() || player.m_5833_()) {
            return InteractionResultHolder.m_19098_((Object)stack);
        }
        if (player.m_36335_().m_41519_((Item)this)) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.m_19092_((Object)stack, (boolean)true);
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        boolean shockwave = player.m_6144_();
        boolean bl = cast = shockwave ? PufferBatCombat.shockwave(serverPlayer, stack, hand) : PufferBatCombat.cast(serverPlayer, stack, hand);
        if (!cast) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.puffer_bat.blocked").m_130940_(ChatFormatting.GRAY), true);
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        player.m_36335_().m_41524_((Item)this, shockwave ? 240 : 180);
        player.m_21011_(hand, true);
        return InteractionResultHolder.m_19092_((Object)stack, (boolean)level.f_46443_);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.m_7373_(stack, level, tooltip, flag);
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.puffer_bat.origin").m_130940_(ChatFormatting.DARK_PURPLE));
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.puffer_bat.skill", (Object[])new Object[]{4, 5, (int)PufferBatRules.tickDamage(), 6, 9}).m_130940_(ChatFormatting.GOLD));
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.puffer_bat.debuff", (Object[])new Object[]{10, 6}).m_130940_(ChatFormatting.GREEN));
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.puffer_bat.shockwave", (Object[])new Object[]{5, (int)PufferBatRules.shockwaveDamage(false), Float.valueOf(1.5f), 12, 4}).m_130940_(ChatFormatting.LIGHT_PURPLE));
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.puffer_bat.rules", (Object[])new Object[]{3, 4}).m_130940_(ChatFormatting.GRAY));
    }
}

