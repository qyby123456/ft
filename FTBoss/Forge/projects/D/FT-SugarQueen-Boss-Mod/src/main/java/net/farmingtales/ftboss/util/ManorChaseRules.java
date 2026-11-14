package net.farmingtales.ftboss.util;

/**
 * 庄园「追不上玩家」这条线的规则常量（2026-09-24 按用户要求重做）。
 *
 * <p>用户原话：「boss 的珍珠追击没触发（减少触发，除非长时间未攻击到玩家 15s，
 * 超过 120s 则禁飞场地 60s，结束后允许飞行并重新倒计时禁飞）」、「蛋蛋会更多使用珍珠」。
 *
 * <p>改前的口径（v26 起）是「离目标超过 {@code PEARL_NEAR}(4 格) 累计 20 tick 就投珍珠」——
 * 实战里几乎永远不触发：Boss 一直在贴身近战，4 格这个门槛只在玩家跑开时才短暂成立，
 * 而计数器一进 4 格就被清零（{@code meleeLockTicks}）。现在改成**按"打没打到人"计时**：
 *
 * <ol>
 *   <li>珍珠追击只在「连续 {@link #PEARL_IDLE_TICKS} tick(15 秒) 没打到任何玩家」之后才允许；</li>
 *   <li>每次追击之后还有 {@link #PEARL_COOLDOWN_TICKS}(10 秒) 冷却，避免一开闸就刷屏；</li>
 *   <li>蛋蛋(变体 1)的门槛与冷却都更短（{@link #DANDAN_PEARL_IDLE_TICKS} /
 *       {@link #DANDAN_PEARL_COOLDOWN_TICKS}）—— 用户要的「蛋蛋会更多使用珍珠」；</li>
 *   <li>连续 {@link #FLY_BAN_TRIGGER_TICKS} tick(120 秒) 都没打到玩家 → 场地禁飞
 *       {@link #FLY_BAN_DURATION_TICKS} tick(60 秒)，窗口结束恢复飞行并**重新开始** 120 秒倒计时。</li>
 * </ol>
 *
 * <p>「打到玩家」的判据是**真的造成了伤害**（{@code LivingHurtEvent} 里来源能回溯到本 Boss），
 * 不是"挥了刀"——所以玩家只要一直被摸到，就不会触发珍珠和禁飞。
 */
public final class ManorChaseRules {
    /** 珍珠追击的闲置门槛：连续这么久(15 秒)没打到玩家才允许追。 */
    public static final int PEARL_IDLE_TICKS = 15 * 20;

    /** 蛋蛋专用（更短）：更快进入"追不上"状态，也就是更常用珍珠。 */
    public static final int DANDAN_PEARL_IDLE_TICKS = 10 * 20;

    /** 每次珍珠追击之后的冷却（减少触发频率）。 */
    public static final int PEARL_COOLDOWN_TICKS = 10 * 20;

    /** 蛋蛋专用冷却（更短）。 */
    public static final int DANDAN_PEARL_COOLDOWN_TICKS = 6 * 20;

    /** 连续这么久(120 秒)没打到玩家 → 开一次场地禁飞。 */
    public static final int FLY_BAN_TRIGGER_TICKS = 120 * 20;

    /** 场地禁飞的窗口时长(60 秒)；窗口结束恢复飞行并重新倒计时。 */
    public static final int FLY_BAN_DURATION_TICKS = 60 * 20;

    /** 禁飞只作用于战场附近这么多格内的玩家。 */
    public static final double FLY_BAN_RADIUS = 96.0;

    /** 禁飞期间对玩家的提示节流(每 3 秒最多一条)。 */
    public static final int FLY_BAN_NOTIFY_TICKS = 3 * 20;

    private ManorChaseRules() {}
}
