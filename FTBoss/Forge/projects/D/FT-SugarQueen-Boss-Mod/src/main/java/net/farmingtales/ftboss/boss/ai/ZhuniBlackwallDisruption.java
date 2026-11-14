package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.network.ZhuniBlackwallNetwork;
import net.farmingtales.ftboss.turret.ZhuniEntity;
import net.farmingtales.ftboss.util.ZhuniBlackwallRules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ZhuniBlackwallDisruption {
    private static final String NEXT_PULSE = "ftboss:zhuni_blackwall_next";
    private ZhuniBlackwallDisruption() {}

    public static void tick(ZhuniEntity bird, EasterBossEntity boss, ServerLevel level) {
        long now = level.getGameTime();
        var data = bird.getPersistentData();
        if (!data.contains(NEXT_PULSE) || data.getLong(NEXT_PULSE) - now > ZhuniBlackwallRules.INTERVAL_TICKS) {
            data.putLong(NEXT_PULSE, now + ZhuniBlackwallRules.INTERVAL_TICKS);
            return;
        }
        if (!ZhuniBlackwallRules.due(now, data.getLong(NEXT_PULSE))) return;
        data.putLong(NEXT_PULSE, now + ZhuniBlackwallRules.INTERVAL_TICKS);
        for (ServerPlayer player : ManorBalanceHooks.players(boss, level)) disrupt(bird, player);
    }

    private static void disrupt(ZhuniEntity bird, ServerPlayer player) {
        var items = new LinkedHashSet<Item>();
        for (ItemStack stack : player.getInventory().items) if (!stack.isEmpty()) items.add(stack.getItem());
        if (!player.getOffhandItem().isEmpty()) items.add(player.getOffhandItem().getItem());
        var selected = ZhuniBlackwallRules.choose(new ArrayList<>(items), bird.getRandom()::nextInt);
        if (selected.isEmpty()) return;
        SparrowDisruption.silenceItems(player, selected, ZhuniBlackwallRules.DURATION_TICKS);
        ZhuniBlackwallNetwork.show(player, bird.getRandom().nextInt(), selected.size());
        player.displayClientMessage(Component.literal("§4竹鸟 · 黑墙入侵 §7随机沉默 §c" + selected.size() + "§7 种物品，持续 §c5§7 秒"), true);
    }
}
