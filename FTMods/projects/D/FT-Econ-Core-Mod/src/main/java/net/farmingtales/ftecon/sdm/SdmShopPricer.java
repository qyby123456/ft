package net.farmingtales.ftecon.sdm;

import net.farmingtales.ftecon.FTEconMod;
import net.farmingtales.ftecon.FteconApi;
import net.farmingtales.ftecon.WealthSwitch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.sixik.sdmshoprework.api.shop.AbstractShopEntry;
import net.sixik.sdmshoprework.api.shop.AbstractShopEntryType;
import net.sixik.sdmshoprework.api.shop.AbstractShopTab;
import net.sixik.sdmshoprework.common.shop.MultiShop;
import net.sixik.sdmshoprework.common.shop.ShopBase;
import net.sixik.sdmshoprework.common.shop.type.ShopItemEntryType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * SDM 商店动态价（内存层，mixin 不依赖）。
 * 触发点：玩家进服 / 出货结算入账 / /ft wealth 开关切换。
 * 规则与脚本一致：只涨价不降价、单条目 ≤ ×1.5；金币袋/箱/锅(kubejs:coin/money、
 * farmingtales:gold_*) 与地契(house_*) 永久排除；非物品类条目不动。
 * 只改内存 entryPrice，不写 snbt —— 服务器重启后商店自动还原为配置文件里的基础价。
 */
public final class SdmShopPricer {
    public static final double SDM_MAX_RAISE = 1.5;

    private static final Map<UUID, Long> BASE = new HashMap<>(); // 会话内基础价(首次见到时=配置原价)
    private static final String HOUSE_PREFIX = "farmingtales:house_";
    private static final Set<String> EXACT_EXCLUDE = new HashSet<>(Arrays.asList(
            "kubejs:coin", "kubejs:money",
            "farmingtales:gold_bundle", "farmingtales:gold_chest", "farmingtales:gold_bucket"));
    private static MinecraftServer lastServer = null;

    private SdmShopPricer() {}

    /** 进服/入账/开关后刷新(按开关状态决定改价还是还原) */
    public static void refresh(ServerPlayer player) {
        try {
            if (player == null) return;
            lastServer = player.getServer();
            applyOrRestore(player);
        } catch (Throwable t) {
            FTEconMod.LOGGER.warn("[FT经济][SDM] 商店调价应用失败: {}", t.toString());
        }
    }

    public static void serverReady(MinecraftServer server) {
        lastServer = server;
    }

    private static List<ShopBase> collectShops() {
        List<ShopBase> shops = new ArrayList<>();
        try {
            if (ShopBase.SERVER != null) shops.add(ShopBase.SERVER);
        } catch (Throwable ignored) {}
        try {
            if (MultiShop.SHOP_MAP != null) shops.addAll(new ArrayList<>(MultiShop.SHOP_MAP.values()));
        } catch (Throwable ignored) {}
        return shops;
    }

    private static void applyOrRestore(ServerPlayer player) {
        boolean enabled = WealthSwitch.isEnabled();
        List<ShopBase> shops = collectShops();
        int changed = 0;
        for (ShopBase shop : shops) {
            if (shop == null) continue;
            List<? extends AbstractShopTab> tabs = shop.getShopTabs();
            if (tabs == null) continue;
            for (AbstractShopTab tab : tabs) {
                if (tab == null) continue;
                List<? extends AbstractShopEntry> entries;
                try {
                    entries = tab.getTabEntry();
                } catch (Throwable t) {
                    continue;
                }
                if (entries == null) continue;
                for (AbstractShopEntry e : entries) {
                    if (e == null) continue;
                    UUID uid = e.entryUUID;
                    if (uid == null) continue;
                    if (!isPricable(e)) continue;
                    long cur = e.entryPrice;
                    if (cur <= 0) continue;
                    BASE.putIfAbsent(uid, cur);
                    long base = BASE.get(uid);
                    long target = base;
                    if (enabled) {
                        double m = FteconApi.multiplierFor(player, base);
                        double used = (m > 1) ? Math.min(m, SDM_MAX_RAISE) : 1.0;
                        target = Math.max(base, Math.round(base * used));
                    }
                    if (target != cur) {
                        try {
                            e.setEntryPrice(target, e.entryCount);
                            changed++;
                        } catch (Throwable t) { }
                    }
                }
            }
        }
        if (changed > 0) {
            FTEconMod.LOGGER.info("[FT经济][SDM] 商店条目已刷新 {} 项 ({})", changed, enabled ? "开启→动态价" : "关闭→还原基础价");
            syncAll(shops);
        }
    }

    /** 判断是否参与动态价：必须是普通物品条目，且不在排除清单 */
    private static boolean isPricable(AbstractShopEntry e) {
        try {
            AbstractShopEntryType t = e.getEntryType();
            if (!(t instanceof ShopItemEntryType itemType)) return false;
            ItemStack st = itemType.itemStack;
            if (st == null || st.isEmpty()) return false;
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(st.getItem());
            if (key == null) return false;
            String id = key.toString();
            if (EXACT_EXCLUDE.contains(id)) return false;
            return !id.startsWith(HOUSE_PREFIX);
        } catch (Throwable t) {
            return false;
        }
    }

    private static void syncAll(List<ShopBase> shops) {
        if (lastServer == null) return;
        for (ShopBase shop : shops) {
            if (shop == null) continue;
            try {
                shop.syncShop(lastServer);
            } catch (Throwable ignored) {}
        }
    }
}
