package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.network.ZhuniBlackwallNetwork;
import net.farmingtales.ftboss.turret.ZhuniEntity;
import net.farmingtales.ftboss.util.ZhuniBlackwallRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class ZhuniBlackwallDisruption {
    private static final String NEXT_PULSE = "ftboss:zhuni_blackwall_next";

    private ZhuniBlackwallDisruption() {
    }

    public static void tick(ZhuniEntity var0, EasterBossEntity var1, ServerLevel var2) {
        long var3 = var2.getGameTime();
        CompoundTag var5 = var0.getPersistentData();
        if (!var5.contains("ftboss:zhuni_blackwall_next") || var5.getLong("ftboss:zhuni_blackwall_next") - var3 > 200L) {
            var5.putLong("ftboss:zhuni_blackwall_next", var3 + 200L);
        } else if (ZhuniBlackwallRules.due(var3, var5.getLong("ftboss:zhuni_blackwall_next"))) {
            var5.putLong("ftboss:zhuni_blackwall_next", var3 + 200L);

            for (ServerPlayer var7 : ManorBalanceHooks.players(var1, var2)) {
                disrupt(var0, var7);
            }
        }
    }

    private static void disrupt(ZhuniEntity var0, ServerPlayer var1) {
        LinkedHashSet var2 = new LinkedHashSet();

        for (ItemStack var4 : var1.getInventory().items) {
            if (!var4.isEmpty()) {
                var2.add(var4.getItem());
            }
        }

        if (!var1.getOffhandItem().isEmpty()) {
            var2.add(var1.getOffhandItem().getItem());
        }

        List var5 = ZhuniBlackwallRules.choose(new ArrayList(var2), var0.getRandom()::nextInt);
        if (!var5.isEmpty()) {
            SparrowDisruption.silenceItems(var1, var5, 100);
            ZhuniBlackwallNetwork.show(var1, var0.getRandom().nextInt(), var5.size());
            var1.displayClientMessage(Component.literal("§4竹鸟 · 黑墙入侵 §7随机沉默 §c" + var5.size() + "§7 种物品，持续 §c5§7 秒"), true);
        }
    }
}
