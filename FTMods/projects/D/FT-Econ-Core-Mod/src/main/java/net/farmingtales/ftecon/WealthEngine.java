package net.farmingtales.ftecon;

import net.minecraft.server.level.ServerPlayer;

/**
 * 定价引擎：与旧 KubeJS 算法一致，参数来自 WealthConfig（config/ftecon-config.json）。
 * multiplier = 1 + (factor - 1) × tierRatio；成交单价 = max(1, round(基础价 × multiplier)) 由调用方处理。
 * 说明：货币类条目的排除判断在调用侧(KubeJS脚本)完成，引擎不做字符串比对。
 */
public final class WealthEngine {

    private WealthEngine() {}

    /** 累计收入 -> 档位比例 0~1 */
    public static double tierRatio(double totalIncome) {
        if (!(totalIncome > 0)) return 0;
        double[] min = WealthConfig.tierMin();
        double[] ratio = WealthConfig.tierRatio();
        double r = 0;
        for (int i = 0; i < min.length; i++) {
            if (totalIncome >= min[i]) r = ratio[i];
            else break;
        }
        return r;
    }

    /** 复杂度分档 factor */
    public static double factorFor(double basePrice) {
        if (!(basePrice > 0)) return 1;
        double[] max = WealthConfig.bandMax();
        double[] factor = WealthConfig.bandFactor();
        for (int i = 0; i < max.length; i++) {
            if (basePrice <= max[i]) return factor[i];
        }
        return 1;
    }

    /** 满进度时最高/最低 factor（用于提示文案） */
    public static double maxBandFactor() {
        double[] f = WealthConfig.bandFactor();
        double m = 1;
        for (double v : f) {
            if (v > m) m = v;
        }
        return m;
    }

    /**
     * 倍率。开关关闭 / 收入<1万 / 基础价非法 => 1。
     * 低级货(降价档 factor<1)在达到各高收入档后，按 lowGoodsExtra 逐档再打折，
     * 且不低于 lowGoodsFloor（默认：100万×0.95 / 1000万×0.90 / 1亿×0.80）。
     */
    public static double multiplierFor(ServerPlayer player, double basePrice) {
        if (player == null) return 1;
        if (!WealthSwitch.isEnabled()) return 1; // FTB 任务：财富价格变动已关闭
        if (!(basePrice > 0)) return 1;
        double totalIncome = WealthData.totalIncomeOf(player);
        /* ===== v150 按用户要求：档位取「累积收入」与「SDM 金币余额」的**较高者** =====
           以前只看出货箱累积收入；服务器上如果累积收入没记上（或玩家就是靠 SDM 金币富有），
           倍率会一直是 1。现在两者取 max 作为判断依据。 */
        double balance = FTEconMod.sdmBalanceOf(player);
        double basis = Math.max(totalIncome, balance);
        double ratio = tierRatio(basis);
        if (totalIncome <= 0 && balance > 0) {
            FTEconMod.LOGGER.debug("[FTEcon] income=0, fallback to SDM balance {} for {}", (long) balance, player.getStringUUID());
        }
        if (!(ratio > 0)) return 1; // 累计收入<1万，保持基础价
        double factor = factorFor(basePrice);
        double m = 1 + (factor - 1) * ratio;
        if (factor < 1) {
            double[] extras = WealthConfig.lowExtra();
            int idx = ratioIndex(ratio);
            if (idx >= 0 && idx < extras.length && extras[idx] < 1.0) {
                m *= extras[idx];
                double floor = WealthConfig.lowFloor();
                if (m < floor) m = floor;
            }
        }
        if (m < 0.01) m = 0.01;
        return m;
    }

    private static int ratioIndex(double ratio) {
        double[] rs = WealthConfig.tierRatio();
        for (int i = 0; i < rs.length; i++) {
            if (Math.abs(rs[i] - ratio) < 1e-9) return i;
        }
        return -1;
    }

    /** 便捷：成交单价 = max(1, round(base × mult)) */
    public static long finalUnit(double basePrice, double multiplier) {
        return Math.max(1, Math.round(basePrice * multiplier));
    }
}
