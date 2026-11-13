package net.farmingtales.ftecon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * 玩家累计总收入（出货箱每日入账后累加，只增不减）。
 * 与旧 KubeJS 兼容：persistentData 键名 ftEconTotal。
 * Forge 服务端 persistentData 必然可用，无需会话内存兜底（省去一张 Map）。
 */
public final class WealthData {
    private static final String NBT_KEY = "ftEconTotal";

    private WealthData() {}

    private static final String[] FALLBACK_KEYS = { "ftEconTotal", "ft_econ_total", "ftEconIncome", "fteconTotal" };

    /** 累积收入（出货箱写入, 只增不减）；v150 增加多键兜底, 服务器上旧存档/旧脚本写的键名也能认。 */
    public static double totalIncomeOf(ServerPlayer player) {
        if (player == null) return 0;
        CompoundTag tag = player.getPersistentData();
        if (tag == null) return 0;
        for (String k : FALLBACK_KEYS) {
            if (!tag.contains(k)) continue;
            double v = tag.getDouble(k);
            if (v > 0) return v;
        }
        return 0;
    }

    /** 调试用：把玩家身上所有可能相关的键打出来（服务器排查"检测不到累积收入"时用） */
    public static String debugKeys(ServerPlayer player) {
        if (player == null) return "player=null";
        CompoundTag tag = player.getPersistentData();
        StringBuilder sb = new StringBuilder();
        sb.append("keys=").append(tag == null ? "null" : tag.getAllKeys().toString());
        for (String k : FALLBACK_KEYS) sb.append(" ").append(k).append('=').append(tag == null ? 0 : tag.getDouble(k));
        return sb.toString();
    }

    public static double recordIncome(ServerPlayer player, double income) {
        if (player == null) return 0;
        double add = income > 0 ? income : 0;
        double oldTotal = totalIncomeOf(player);
        double newTotal = oldTotal + add;
        player.getPersistentData().putDouble(NBT_KEY, newTotal);
        if (add >= 10000) {
            FTEconMod.LOGGER.info("[FT经济] 累计收入更新: {}... {} +{} -> {}",
                    uuid8(player), (long) oldTotal, (long) add, (long) newTotal);
        }
        try {
            net.farmingtales.ftecon.sdm.SdmShopPricer.refresh(player); // 收入变化→同步刷新商店动态价
        } catch (Throwable ignored) {}
        return newTotal;
    }

    private static String uuid8(ServerPlayer player) {
        String s = player.getStringUUID();
        return s.length() > 8 ? s.substring(0, 8) : s;
    }
}
