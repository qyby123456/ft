package net.farmingtales.ftecon;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.lang.reflect.Method;

@Mod(FTEconMod.MOD_ID)
public class FTEconMod {
    public static final String MOD_ID = "ftecon";
    public static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("removal")
    public FTEconMod() {
        MinecraftForge.EVENT_BUS.register(this);
        var modBus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        WealthSwitch.load();
        WealthConfig.load(net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get());
        LOGGER.info("[FT经济] qy's progressive taxation (ftecon) 加载完成，开关=" + (WealthSwitch.isEnabled() ? "开启" : "关闭")
                + "，高档factor=" + WealthEngine.maxBandFactor());
    }

    // ---------- 命令：/ftprice、/ft wealth(OP2)、/ftecon test|wealth ----------
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();

        // /ftprice
        d.register(Commands.literal("ftprice").executes(ctx -> {
            if (ctx.getSource().getEntity() instanceof ServerPlayer sp) {
                sendFtprice(sp);
            } else {
                LOGGER.info("[FT经济] /ftprice 仅玩家可用");
            }
            return 1;
        }));

        // /ft wealth true|false|status （FTB 可选任务调用，需 OP2）
        d.register(Commands.literal("ft")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("wealth")
                        .then(Commands.literal("true").executes(ctx -> {
                            FteconApi.setWealthEnabled(true);
                            refreshShopFor(ctx);
                            reply(ctx, legacy("§6[FT经济] §a财富价格变动已开启（高档最多×" + factorText() + "，低复杂度降价）"));
                            return 1;
                        }))
                        .then(Commands.literal("false").executes(ctx -> {
                            FteconApi.setWealthEnabled(false);
                            refreshShopFor(ctx);
                            reply(ctx, legacy("§6[FT经济] §7财富价格变动已关闭（一律按价格表基础价结算）"));
                            return 1;
                        }))
                        .then(Commands.literal("status").executes(ctx -> {
                            reply(ctx, legacy("§6[FT经济] 财富价格变动当前: " + (FteconApi.wealthEnabled() ? "§a开启中" : "§c已关闭")));
                            return 1;
                        }))));
    }

    /* ===== v145 服务器可用性改造 =====
       1) 默认生效：启动时明确加载开关并打日志（配置文件缺失 = 默认开启）；
       2) 允许服务器单独使用 / 不需要双端都装：本模组全是服务端逻辑，
          对 SDM 商店的调用改成"装了才调、调错也不崩"——以前服务器没装 SDM 时
          会在 onServerStarted 直接 NoClassDefFoundError 崩服。 */
    @SubscribeEvent
    public void onServerStarted(net.minecraftforge.event.server.ServerStartedEvent event) {
        net.farmingtales.ftecon.WealthSwitch.load();
        LOGGER.info("[FT经济] 经济系统: {} (config/farmingtales_wealth_switch.json; 文件不存在=默认开启)",
                net.farmingtales.ftecon.WealthSwitch.isEnabled() ? "已开启" : "已关闭");
        if (!sdmShopAvailable()) {
            LOGGER.info("[FT经济] 未检测到 SDM 商店 -> 跳过商店价格刷新（经济引擎本身照常工作）");
            return;
        }
        try {
            net.farmingtales.ftecon.sdm.SdmShopPricer.serverReady(event.getServer());
        } catch (Throwable t) {
            LOGGER.warn("[FT经济] 商店价格初始化失败（已忽略，不影响经济引擎）: {}", t.toString());
        }
    }

    /** SDM 商店在不在（服务器可以不装：缺了只跳过价格刷新，绝不崩） */
    public static boolean sdmShopAvailable() {
        try {
            var ml = net.minecraftforge.fml.ModList.get();
            return ml.isLoaded("sdmshoprework") || ml.isLoaded("sdmshop") || ml.isLoaded("sdm_shop");
        } catch (Throwable t) {
            return false;
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!sdmShopAvailable()) return;
        try {
            net.farmingtales.ftecon.sdm.SdmShopPricer.refresh(sp);
        } catch (Throwable t) {
            LOGGER.warn("[FT经济] 登录刷新商店价格失败（已忽略）: {}", t.toString());
        }
    }

    /* ===== v150: SDM 金币余额（读不到就是 0）===== */
    public static double sdmBalanceOf(net.minecraft.world.entity.player.Player p) {
        try {
            Object bal = readSdmBalance(p);
            return (bal instanceof Number n) ? n.doubleValue() : 0.0;
        } catch (Throwable t) {
            return 0.0;
        }
    }

    /** 价格判断依据 = max(累积收入, SDM 金币余额) */
    public static double wealthBasisOf(net.minecraft.server.level.ServerPlayer p) {
        double income = net.farmingtales.ftecon.WealthData.totalIncomeOf(p);
        double balance = sdmBalanceOf(p);
        return Math.max(income, balance);
    }

    private static void refreshShopFor(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer p = ctx.getSource().getPlayer();
            if (p != null && sdmShopAvailable()) net.farmingtales.ftecon.sdm.SdmShopPricer.refresh(p);
        } catch (Throwable ignored) {}
    }

    /** 满进度高档倍率文案（取配置中最高 factor） */
    private static String factorText() {
        double m = WealthEngine.maxBandFactor();
        return m == Math.floor(m) ? String.valueOf((long) m) : String.valueOf(m);
    }

    /** /ftprice：与脚本版同口径（余额读取失败不影响） */
    private static void sendFtprice(ServerPlayer p) {
        double total = FteconApi.totalIncomeOf(p);
        double ratio = FteconApi.tierRatio(total);
        Object bal = readSdmBalance(p);
        p.sendSystemMessage(legacy("§6[FT经济] 状态诊断(核心:mod ftecon)"));
        p.sendSystemMessage(legacy("§7SDMShop绑定: §f可用"));
        p.sendSystemMessage(legacy("§7当前SDM余额: §f" + (bal == null ? "读取失败" : String.valueOf(Math.floor(((Number) bal).doubleValue())))));
        p.sendSystemMessage(legacy("§7出货箱累计总收入: §f" + (long) total));
        p.sendSystemMessage(legacy("§7梯度比例 tierRatio: §f" + ratio));
        p.sendSystemMessage(legacy("§7FTB开关任务: §f" + (FteconApi.wealthEnabled() ? "§a开启中" : "§c已关闭(财富价格变动)→基础价")));
        int[] samples = { 10, 30, 60, 80, 100, 120, 300 };
        for (int s : samples) {
            double m = FteconApi.multiplierFor(p, s);
            p.sendSystemMessage(legacy("§7基础价 " + s + " → 倍率 " + String.format("%.3f", m)
                    + " → 成交价 " + WealthEngine.finalUnit(s, m)));
        }
        p.sendSystemMessage(legacy("§7提示: 累计收入<1万时倍率恒为1(设计如此)；出货箱收入随每日入账自动累计"));
    }

    private static Object readSdmBalance(Player p) {
        try {
            Class<?> c = Class.forName("net.sixik.sdmshoprework.SDMShopR");
            Method m = null;
            try {
                m = c.getMethod("getMoney", Player.class);
            } catch (NoSuchMethodException e) {
                for (Method cand : c.getMethods()) {
                    if (cand.getName().equals("getMoney") && cand.getParameterCount() == 1
                            && cand.getParameterTypes()[0].isAssignableFrom(p.getClass())) {
                        m = cand;
                        break;
                    }
                }
            }
            if (m != null) {
                m.setAccessible(true);
                return m.invoke(null, p);
            }
        } catch (Throwable t) {
            // 读取余额失败不影响定价
        }
        return null;
    }

    private static void reply(CommandContext<CommandSourceStack> ctx, Component msg) {
        try {
            ctx.getSource().sendSuccess(() -> msg, false);
        } catch (RuntimeException e) {
            try {
                ServerPlayer p = ctx.getSource().getPlayer();
                if (p != null) p.sendSystemMessage(msg);
            } catch (RuntimeException e2) { }
        }
    }

    /** 解析 § 传统格式代码（§6/§a/§7/§c…），便于与脚本聊天文案保持一致 */
    private static Component legacy(String text) {
        MutableComponent out = Component.literal("");
        StringBuilder sb = new StringBuilder();
        ChatFormatting fmt = null;
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\u00a7' && i + 1 < text.length()) {
                appendSeg(out, sb, fmt);
                fmt = ChatFormatting.getByCode(text.charAt(i + 1));
                i += 2;
            } else {
                sb.append(c);
                i++;
            }
        }
        appendSeg(out, sb, fmt);
        return out;
    }

    private static void appendSeg(MutableComponent out, StringBuilder sb, ChatFormatting fmt) {
        if (sb.length() == 0) return;
        String s = sb.toString();
        sb.setLength(0);
        if (fmt == null) {
            out.append(Component.literal(s));
        } else {
            out.append(Component.literal(s).withStyle(fmt));
        }
    }
}
