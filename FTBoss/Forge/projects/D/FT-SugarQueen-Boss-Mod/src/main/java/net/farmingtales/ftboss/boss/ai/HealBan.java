package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 【禁疗】(2026-09-13 v96 用户要求): 麻雀每次"获取玩家增益"时有 10% 概率让那名玩家
 * **30 秒内无法回复血量**。
 *
 * <p>实现分两层:
 * <ol>
 *   <li><b>可见标记</b>: 优先挂现成的 {@code relics:anti_heal}(反治疗, Relics 模组)。
 *       没装那个模组就只用下面的计时表(玩家只会收到一条提示, 没有图标)。</li>
 *   <li><b>真正生效</b>: 监听 {@link LivingHealEvent} —— 原版**所有**回血都走
 *       {@code LivingEntity.heal()} (自然回血 / 生命恢复效果 / 金苹果 / 瞬间治疗药水),
 *       所以这里把量改成 0 就一条都回不上。{@code setAmount(0)} 比 cancel 安全
 *       (这个事件在部分版本不支持 cancel, 直接调会抛异常)。</li>
 * </ol>
 *
 * <p>判定与标记**同源**: 挂了 anti_heal 效果时就以"身上有没有这个效果"为准 ——
 * 这样"麻雀的禁疗"和任何别的途径加的 anti_heal 表现一致, 而且被 Forge 的祝福/牛奶清掉后
 * 立即解除(不会有"图标没了但还在禁疗"的鬼影)。
 *
 * <p>★★★ 2026-09-14 v106 按用户反馈「雪黎的禁疗未生效」★★★
 * 这个类以前挂的是 {@code @Mod.EventBusSubscriber} 自动注册, 和项目里其它监听器(显式 register)
 * 不一致, 排查时说不清到底有没有生效。现在**去掉注解**, 改成在 {@code FTBossMod} 里和其它
 * 监听器一样显式 {@code MinecraftForge.EVENT_BUS.register(HealBan.class)} —— 只注册一次,
 * 一眼能查到。同时加了"拦下回血"的日志(每名玩家每 2 秒最多一条), 以后能直接从日志确认禁疗有没有生效。
 */
public final class HealBan {

    /** 禁疗时长(30 秒) */
    public static final int BAN_TICKS = 30 * 20;

    /** 玩家 UUID -> 解禁时刻(游戏刻)。**仅在 anti_heal 不可用时**作为判据 */
    private static final Map<UUID, Long> UNTIL = new ConcurrentHashMap<>();

    private HealBan() {}

    /** ★ 2026-09-14 v119 按用户要求「禁疗做成一个 buff」:
        以前借 {@code relics:anti_heal} 当图标(没装那个 mod 就只剩一张计时表);
        现在挂的是**本模组自己的状态效果** {@code ftboss:heal_ban}, 有图标 + 倒计时。 */
    private static MobEffect healBanEffect() {
        try {
            return net.farmingtales.ftboss.effect.FTEffects.HEAL_BAN.get();
        } catch (Throwable t) {
            return null;
        }
    }

    /** 禁止这名玩家回复血量(30 秒) —— 默认署名"麻雀" */
    public static void ban(ServerPlayer p) { ban(p, "麻雀"); }

    /**
     * 禁止这名玩家回复血量(30 秒)。
     *
     * @param byWhom 提示里"谁封的"(麻雀 / 雪黎的飞刀 …) —— v103 加, 以前写死"麻雀"
     */
    public static void ban(ServerPlayer p, String byWhom) {
        if (p == null) return;
        MobEffect eff = healBanEffect();
        if (eff != null) {
            p.addEffect(new MobEffectInstance(eff, BAN_TICKS, 0, false, true, true));
        } else {
            UNTIL.put(p.getUUID(), p.level().getGameTime() + BAN_TICKS);
        }
        p.displayClientMessage(Component.literal(
                "§4§l" + byWhom + "封住了你的恢复力！§c30 秒内无法回复血量"), true);
        FTBossMod.LOGGER.info("[FTBoss][禁疗] {} 被「{}」禁疗 {} 秒(标记效果={})",
                p.getName().getString(), byWhom, BAN_TICKS / 20,
                eff != null ? "ftboss:heal_ban" : "仅计时表");
    }

    /** 这名实体现在是否被禁疗 */
    public static boolean isBanned(LivingEntity le) {
        if (CombatStatusHooks.grievous(le)) return true;
        if (le == null) return false;
        MobEffect eff = healBanEffect();
        if (eff != null && le.hasEffect(eff)) return true;
        Long t = UNTIL.get(le.getUUID());
        if (t == null) return false;
        if (le.level().getGameTime() > t) {
            UNTIL.remove(le.getUUID());
            return false;
        }
        return true;
    }

    /** v106: 拦住回血时的日志节流(玩家 UUID -> 上次打日志的游戏刻) */
    private static final Map<UUID, Long> LAST_LOG = new ConcurrentHashMap<>();
    /** v112: 节流表最多留这么多条(超出就顺手清掉"2 秒内不会再打日志"的旧项), 免得只增不减 */
    private static final int LOG_CAP = 256;

    /** 原版所有回血都过这里: 禁疗期间把回血量改成 0 */
    @SubscribeEvent
    public static void onHeal(LivingHealEvent e) {
        try {
            if (e.getAmount() > 0.0f && isBanned(e.getEntity())) {
                LivingEntity le = e.getEntity();
                long now = le.level().getGameTime();
                /* v112 修复: **先把真实回血量记下来再清零** —— 以前先 setAmount(0) 再打印,
                   日志里的 amount 永远是 0.00, v106 想留的排查证据等于没有。 */
                float blocked = e.getAmount();
                e.setAmount(0.0f);
                /* v106: 留一条证据 —— 用户报「禁疗未生效」时, 有/没有这条日志能一眼分清是
                   "没拦住"还是"根本没触发禁疗"。每名玩家每 2 秒最多一条。 */
                Long last = LAST_LOG.get(le.getUUID());
                if (last == null || now - last >= 40L) {
                    if (LAST_LOG.size() > LOG_CAP) {
                        LAST_LOG.entrySet().removeIf(en -> now - en.getValue() >= 40L);
                    }
                    LAST_LOG.put(le.getUUID(), now);
                    FTBossMod.LOGGER.info("[FTBoss][禁疗] {} 的回血被拦下({} 点)",
                            le.getName().getString(), String.format("%.2f", blocked));
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /* ==================================================================================
     * 2026-09-18 按用户要求「优化禁疗效果，禁疗范围要包括不死图腾和血王子焰形剑的回复，
     * 和任意回复效果」。
     *
     * ★ 实测出来的两条**绕过 heal() 的回复路**（现有的 onHeal 对它们完全无效）：
     *
     *   ① **不死图腾**：原版 {@code checkTotemDeathProtection()} 直接 {@code setHealth(1.0F)}
     *      并挂生命恢复 II + 伤害吸收 II —— 方法体里**没有任何 LivingHealEvent 触发点**；
     *      而 Forge 的 {@code LivingUseTotemEvent} 只有三个 getter、**没有 setCanceled**，
     *      拦不住。
     *
     *   ② **血王子焰形剑**（armamentarium）：反汇编 {@code SyphonFlambergeProcedure} 看到的是
     *      {@code setHealth(getHealth() + 吸血量 / 10.0)} —— 也是直接改血量，不过 heal()。
     *      更麻烦的是它**在同一次命中流程内**改受害者血量，事件层没有任何切入点。
     *
     * ★ 结论：**事件层拦不全，必须在血量层兜底** —— 见 {@link #onPlayerTickEnd}：
     *   每 tick 记一次快照，tick 末如果发现血量**比快照高**就压回去。
     *   这一条同时覆盖 heal() / setHealth() / 图腾 / 任何模组的回复，
     *   而且**天然只压"涨"的方向** —— 掉血不受影响（快照取的是实时值，掉血后绝不回涨）。
     * ================================================================================== */

    /** 禁疗期间"本 tick 血量上限"：UUID -> tick 开始时的血量 */
    private static final Map<UUID, Float> SNAPSHOT = new ConcurrentHashMap<>();

    /**
     * 禁疗期间的**血量回滚兜底**。
     *
     * <p>每个 tick 末执行一次：拿 tick 开始时的血量当上限，发现涨了就压回去。
     * 因为快照每 tick 刷新、且取的是**实时血量**，所以：
     * <ul>
     *   <li>受伤：血量下降 → 快照下一 tick 跟着降 → **永远压不到"掉血"**；</li>
     *   <li>回复：无论走 heal() 还是 setHealth()，涨上去的部分在 tick 末被抹掉；</li>
     *   <li>图腾：致死判定把它设成 1 点，但快照是 0（致死前那一刻）→ 压回 0 → **图腾的复活被抹掉**，
     *       玩家照常死亡。这正是"禁疗期间图腾不救你"该有的表现。</li>
     * </ul>
     *
     * <p>用 {@link TickEvent.Phase#END} 是为了让整个 tick 的所有结算（含第三方模组的
     * {@code setHealth}）都跑完再压。
     */
    @SubscribeEvent
    public static void onPlayerTickEnd(net.minecraftforge.event.TickEvent.PlayerTickEvent e) {
        try {
            if (e == null || e.player == null) return;
            if (e.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
            LivingEntity le = e.player;
            UUID id = le.getUUID();
            if (!isBanned(le)) {
                SNAPSHOT.remove(id);
                return;
            }
            float now = le.getHealth();
            Float cap = SNAPSHOT.get(id);
            if (cap == null) {
                SNAPSHOT.put(id, now);
                return;
            }
            /* 只在"涨了"且"上限是个正数"时才压 —— 被禁疗的人不该靠回复把血拉回来。
               如果 cap <= 0 说明快照那一刻已经该死了，不干预（交给正常的死亡流程）。 */
            if (now > cap && cap > 0.0f) {
                le.setHealth(cap);
                logBlocked(le, "回复", cap);
            }
            SNAPSHOT.put(id, le.getHealth());
        } catch (Throwable ignored) {
        }
    }

    /**
     * 禁疗期间**拒绝"回复类"状态效果**。
     *
     * <p>这一条属于"锦上添花"：即便不拦，{@link #onHeal} 与上面的血量回滚也会把回血抹掉；
     * 拦住它只是让玩家**看得到**原因（图标不出现），而不是白挂一个没用的 buff。
     *
     * <p>清单一律按**注册名**取，不用映射字段（本工程踩过"字段名随映射变"的坑）。
     * 只对这四条生效，绝不误伤别的效果；"生命提升"不在此列（那是改最大生命，不是回复）。
     */
    private static final java.util.Set<String> RECOVERY_EFFECTS = java.util.Set.of(
            "minecraft:regeneration",       // 生命恢复
            "minecraft:absorption",         // 伤害吸收
            "minecraft:saturation",         // 饱和
            "minecraft:instant_health"      // 瞬间治疗（本身走 heal()，这里双保险）
    );

    /** 某条效果是不是"回复类"（按注册名判，取不到名字就返回 false，绝不误伤） */
    private static boolean isRecoveryEffect(MobEffect effect) {
        if (effect == null) return false;
        try {
            var key = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getKey(effect);
            return key != null && RECOVERY_EFFECTS.contains(key.toString());
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 禁疗期间不给挂"回复类"状态效果。
     *
     * <p>用 {@link EventPriority#LOWEST} 是为了**让其它模组/本模组先表态** ——
     * 有别人已经 DENY 掉的就不必我们再动。1.20.1 上这个事件**不能 setCanceled**，
     * 只能 {@code setResult(Result.DENY)}。
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEffectApplicable(MobEffectEvent.Applicable e) {
        try {
            if (e == null || e.getEntity() == null) return;
            if (!isRecoveryEffect(e.getEffectInstance() == null ? null : e.getEffectInstance().getEffect())) return;
            if (!isBanned(e.getEntity())) return;
            e.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        } catch (Throwable ignored) {
        }
    }

    /** 统一的"拦下了什么"日志 + 动作栏提示（沿用 v106/v112 的节流表） */
    private static void logBlocked(LivingEntity le, String what, float kept) {
        long now = le.level().getGameTime();
        Long last = LAST_LOG.get(le.getUUID());
        if (last != null && now - last < 40L) return;
        if (LAST_LOG.size() > LOG_CAP) {
            LAST_LOG.entrySet().removeIf(en -> now - en.getValue() >= 40L);
        }
        LAST_LOG.put(le.getUUID(), now);
        FTBossMod.LOGGER.info("[FTBoss][禁疗] {} 的{}被拦下(血量保持在 {})",
                le.getName().getString(), what, String.format("%.1f", kept));
        if (le instanceof ServerPlayer sp) {
            sp.displayClientMessage(Component.literal("§4禁疗中：恢复效果无效"), true);
        }
    }
}
