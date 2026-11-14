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
import net.farmingtales.ftboss.boss.SugarQueenBoss;
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

public class SugarQueenCrystalItem
extends Item {
    public SugarQueenCrystalItem() {
        super(new Item.Properties().m_41487_(16).m_41497_(Rarity.RARE));
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        if (!level.f_46443_) {
            ItemStack stack = player.m_21120_(hand);
            if (SugarQueenBoss.tryStart((ServerPlayer)player)) {
                stack.m_41774_(1);
                player.m_9236_().m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11868_, SoundSource.HOSTILE, 1.0f, 1.2f);
            } else {
                player.m_5661_((Component)Component.m_237113_((String)"\u00a7c\u7cd6\u971c\u5973\u7687\u5df2\u5728\u6218\u6597\u4e2d\uff0c\u6216\u6b64\u5904\u65e0\u6cd5\u53ec\u5524"), true);
            }
        }
        return InteractionResultHolder.m_19092_((Object)player.m_21120_(hand), (boolean)level.f_46443_);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add((Component)Component.m_237113_((String)"\u00a77\u751c\u70b9\u7cfbBoss\u53ec\u5524\u7269\uff1a\u00a7f\u53f3\u952e\u6d88\u80171\u4e2a\u53ec\u5524 \u59dc\u997c\u56fd\u738b"));
        tip.add((Component)Component.m_237113_((String)"\u00a78\u5408\u6210\uff1a8\u79cd\u6700\u9ad8\u4ef7\u751c\u70b9\u73af\u7ed5 1\u4e2a\u91d1\u5e01\u9505"));
    }
}

