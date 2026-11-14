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
package net.farmingtales.ftboss.item;

import java.util.List;
import javax.annotation.Nullable;
import net.farmingtales.ftboss.skill.YouziStatueSkills;
import net.farmingtales.ftboss.util.YouziStatueRules;
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

public final class YouziStatueItem
extends SwordItem {
    public YouziStatueItem() {
        super((Tier)Tiers.IRON, 3, -2.2f, new Item.Properties().m_41503_(512).m_41497_(Rarity.EPIC).m_41486_());
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ServerPlayer serverPlayer;
        ItemStack stack = player.m_21120_(hand);
        if (!player.m_6084_() || player.m_5833_() || player.m_36335_().m_41519_((Item)this)) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        if (player instanceof ServerPlayer && !YouziStatueSkills.begin(serverPlayer = (ServerPlayer)player, hand, player.m_6144_())) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        return InteractionResultHolder.m_19092_((Object)stack, (boolean)level.f_46443_);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.m_7373_(stack, level, tooltip, flag);
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.statue_youzi.origin").m_130940_(ChatFormatting.LIGHT_PURPLE));
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.statue_youzi.echo", (Object[])new Object[]{3, (int)YouziStatueRules.echoDamage(), 18, 8}).m_130940_(ChatFormatting.AQUA));
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.statue_youzi.ward", (Object[])new Object[]{50, 6, 16}).m_130940_(ChatFormatting.LIGHT_PURPLE));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.statue_youzi.limit").m_130940_(ChatFormatting.GRAY));
    }
}

