package net.farmingtales.ftboss.transaction;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** 交易生命周期方法模板；2026-10-20 tick 仅失效轻量非法状态，完整源与版本校验保留在每次提交入口。 */
public final class TransactionLifecycle {
    private final FourWeaponTransactions transactions = null;

    @SubscribeEvent
    public void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.isRemoved() || !player.isAlive() || player.isSpectator() || player.isSleeping() || player.isUsingItem()) {
            transactions.invalidate(player.getUUID());
        }
    }
}
