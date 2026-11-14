/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
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

public final class ManorStopTokenItem
extends Item {
    public ManorStopTokenItem() {
        super(new Item.Properties().m_41487_(1).m_41497_(Rarity.EPIC).m_41486_());
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (!player.m_7500_() || !player.m_6144_()) {
            if (!level.f_46443_) {
                player.m_5661_((Component)Component.m_237115_((String)(player.m_7500_() ? "message.ftboss.manor_stop_token.sneak" : "message.ftboss.manor_stop_token.creative_only")), true);
            }
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            boolean stopped = ManorFight.stopForCreative(serverPlayer);
            player.m_5661_((Component)Component.m_237115_((String)(stopped ? "message.ftboss.manor_stop_token.stopped" : "message.ftboss.manor_stop_token.none")), true);
            if (!stopped) {
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            player.m_36335_().m_41524_((Item)this, 10);
        }
        return InteractionResultHolder.m_19092_((Object)stack, (boolean)level.f_46443_);
    }

    public InteractionResult m_6225_(UseOnContext context) {
        Player player = context.m_43723_();
        return player == null ? InteractionResult.PASS : this.m_7203_(context.m_43725_(), player, context.m_43724_()).m_19089_();
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add((Component)Component.m_237115_((String)"tooltip.ftboss.manor_stop_token.creative_only").m_130940_(ChatFormatting.LIGHT_PURPLE));
        lines.add((Component)Component.m_237115_((String)"tooltip.ftboss.manor_stop_token.use").m_130940_(ChatFormatting.YELLOW));
        lines.add((Component)Component.m_237115_((String)"tooltip.ftboss.manor_stop_token.result").m_130940_(ChatFormatting.GRAY));
    }
}

