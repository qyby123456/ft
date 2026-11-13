package net.farmingtales.ftecon;

import net.minecraft.server.level.ServerPlayer;

/**
 * 对外开放 API（v1.5 由 KubeJS 脚本 Java.loadClass 桥接调用；
 * 将来出货箱整体迁入 mod 后也走这里）。
 */
public final class FteconApi {
    private FteconApi() {}

    /** 倍率：等价于旧 global.ftEconMultiplier(player, basePrice) */
    public static double multiplierFor(ServerPlayer player, double basePrice) {
        return WealthEngine.multiplierFor(player, basePrice);
    }

    /** 记录累计收入：等价于旧 global.ftEconRecordIncome */
    public static double recordIncome(ServerPlayer player, double amount) {
        return WealthData.recordIncome(player, amount);
    }

    /** 读取累计收入：等价于旧 global.ftEconTotalIncomeOf */
    public static double totalIncomeOf(ServerPlayer player) {
        return WealthData.totalIncomeOf(player);
    }

    /** 档位比例：等价于旧 global.ftEconTierRatio */
    public static double tierRatio(double totalIncome) {
        return WealthEngine.tierRatio(totalIncome);
    }

    /** 开关是否开启 */
    public static boolean wealthEnabled() {
        return WealthSwitch.isEnabled();
    }

    /** 设置开关并落盘（等价于 /ft wealth true|false） */
    public static boolean setWealthEnabled(boolean enabled) {
        return WealthSwitch.setEnabled(enabled);
    }
}
