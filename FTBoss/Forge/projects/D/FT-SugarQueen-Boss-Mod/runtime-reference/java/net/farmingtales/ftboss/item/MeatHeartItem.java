/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.boss.MeatBoss;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class MeatHeartItem
extends Item {
    public MeatHeartItem() {
        super(new Item.Properties().m_41487_(16).m_41497_(Rarity.RARE));
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        if (!level.f_46443_) {
            ItemStack stack = player.m_21120_(hand);
            if (MeatBoss.tryStart((ServerPlayer)player)) {
                stack.m_41774_(1);
                player.m_9236_().m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11957_, SoundSource.HOSTILE, 1.0f, 0.8f);
            }
        }
        return InteractionResultHolder.m_19092_((Object)player.m_21120_(hand), (boolean)level.f_46443_);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add((Component)Component.m_237113_((String)"\u00a77\u8089\u7c7bBoss\u53ec\u5524\u7269\uff1a\u00a7f\u53f3\u952e\u6d88\u80171\u4e2a\u53ec\u5524 \u5c60\u592b"));
        tip.add((Component)Component.m_237113_((String)"\u00a78\u5408\u6210\uff1a8\u79cd\u6700\u9ad8\u4ef7\u8089\u6599\u7406\u73af\u7ed5 1\u4e2a\u91d1\u5e01\u9505"));
    }
}

