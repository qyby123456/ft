/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.boss.ai.SparrowDisruption;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.network.ZhuniBlackwallNetwork;
import net.farmingtales.ftboss.turret.ZhuniEntity;
import net.farmingtales.ftboss.util.ZhuniBlackwallRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ZhuniBlackwallDisruption {
    private static final String NEXT_PULSE = "ftboss:zhuni_blackwall_next";

    private ZhuniBlackwallDisruption() {
    }

    public static void tick(ZhuniEntity zhuniEntity, EasterBossEntity easterBossEntity, ServerLevel serverLevel) {
        long l = serverLevel.m_46467_();
        CompoundTag compoundTag = zhuniEntity.getPersistentData();
        if (!compoundTag.m_128441_(NEXT_PULSE) || compoundTag.m_128454_(NEXT_PULSE) - l > 200L) {
            compoundTag.m_128356_(NEXT_PULSE, l + 200L);
            return;
        }
        if (!ZhuniBlackwallRules.due(l, compoundTag.m_128454_(NEXT_PULSE))) {
            return;
        }
        compoundTag.m_128356_(NEXT_PULSE, l + 200L);
        for (ServerPlayer serverPlayer : ManorBalanceHooks.players(easterBossEntity, serverLevel)) {
            ZhuniBlackwallDisruption.disrupt(zhuniEntity, serverPlayer);
        }
    }

    private static void disrupt(ZhuniEntity zhuniEntity, ServerPlayer serverPlayer) {
        LinkedHashSet<Item> linkedHashSet = new LinkedHashSet<Item>();
        Object object = serverPlayer.m_150109_().f_35974_.iterator();
        while (object.hasNext()) {
            ItemStack itemStack = (ItemStack)object.next();
            if (itemStack.m_41619_()) continue;
            linkedHashSet.add(itemStack.m_41720_());
        }
        if (!serverPlayer.m_21206_().m_41619_()) {
            linkedHashSet.add(serverPlayer.m_21206_().m_41720_());
        }
        if ((object = ZhuniBlackwallRules.choose(new ArrayList(linkedHashSet), arg_0 -> ((RandomSource)zhuniEntity.m_217043_()).m_188503_(arg_0))).isEmpty()) {
            return;
        }
        SparrowDisruption.silenceItems(serverPlayer, (List<Item>)object, 100);
        ZhuniBlackwallNetwork.show(serverPlayer, zhuniEntity.m_217043_().m_188502_(), object.size());
        serverPlayer.m_5661_((Component)Component.m_237113_((String)("\u00a74\u7af9\u9e1f \u00b7 \u9ed1\u5899\u5165\u4fb5 \u00a77\u968f\u673a\u6c89\u9ed8 \u00a7c" + object.size() + "\u00a77 \u79cd\u7269\u54c1\uff0c\u6301\u7eed \u00a7c5\u00a77 \u79d2")), true);
    }
}

