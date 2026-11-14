package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * 【Boss 文本输出口径】—— 2026-09-20 v182 按用户要求
 * 「优化 boss 文本，非必要减少文字输出」（口径：**激进** —— 只留必需机制提示；**优先删行动栏**）。
 *
 * <p>在此之前，战斗中的提示是"想到就发"，结果两类噪音很大：
 * <ul>
 *   <li><b>重复刷屏</b>：同一件事每命中/每结算一次就发一条；</li>
 *   <li><b>战报式播报</b>：把玩家刚刚亲眼看到的动作再用文字复述一遍
 *       （"XX 结束""XX 已启动""XX 进入阶段"）。</li>
 * </ul>
 *
 * <p>现在收敛成**两级**，让"要不要给玩家看"变成一个显式决定，而不是各处随手写：
 * <ul>
 *   <li>{@link #essential} —— <b>必需机制提示</b>：不发玩家就会挨打/卡住的信息。
 *       典型：技能预警与倒计时、必须立刻反应的机制（进红圈/离开射线/禁疗/盾还活着）、
 *       以及"为什么没生效"（召唤失败、此处放不下）。这类**只进行动栏**（{@code true}）。</li>
 *   <li>{@link #flavor} —— <b>非必要</b>：战报、复述、纯氛围、可按 F3 看到的数值。
 *       这类**不给玩家**，只写服务器日志 —— 排查时不丢信息，玩家侧彻底安静。</li>
 * </ul>
 *
 * <p>★ 为什么"非必要"是进日志而不是直接删：这些行在排查"机制到底有没有触发"时很有用
 * （本项目多次靠日志定位问题）。所以策略是**降级而不是删除**。
 *
 * <p>想整体恢复旧行为：把 {@link #QUIET} 设成 false。
 */
public final class BossText {

    /**
     * 总开关。true = 非必要文本只进日志(当前口径)；
     * false = 恢复"什么都往屏幕上发"的旧行为。
     */
    public static final boolean QUIET = true;

    private BossText() {}

    /**
     * 必需机制提示 —— 发给玩家（行动栏）。
     *
     * <p>判断标准只有一条：**不发这条，玩家会不会因此挨打或卡住？**
     * 会 → 用这个；不会 → 用 {@link #flavor}。
     *
     * <p>参数用 {@link Player} 而不是 {@code ServerPlayer} —— 调用点常常只有
     * {@code Player} 的静态类型（例如遍历 {@code level.players()}），
     * 这里不需要服务端独占能力，收窄类型只会逼调用方到处强转。
     */
    public static void essential(Player player, String text) {
        if (player == null || text == null) return;
        player.displayClientMessage(Component.literal(text), true);
    }

    /**
     * 非必要文本 —— 只写日志，不给玩家看（{@link #QUIET} 为 true 时的行为）。
     *
     * @param tag  来源标记（如 "米"、"Manor"），写进日志便于检索
     * @param text 原文
     */
    public static void flavor(String tag, String text) {
        if (text == null) return;
        FTBossMod.LOGGER.info("[FTBoss][文本·已静音][{}] {}", tag, text);
    }

    /**
     * 非必要文本，但**在行动栏留一句短提示**。
     *
     * <p>用于"虽然不重要、但完全没反馈会让人困惑"的那一类
     * （例如抽掉了一个增益）。玩家侧只看到一句短的，长文案进日志。
     */
    public static void flavorWithHint(Player player, String tag, String longText, String shortHint) {
        flavor(tag, longText);
        if (shortHint != null) essential(player, shortHint);
    }

    /** 给"附近所有玩家"发必需提示（替代原来那种同时刷行动栏+聊天栏的写法）。 */
    public static void essentialNearby(java.util.List<ServerPlayer> players, String text) {
        if (players == null || text == null) return;
        Component c = Component.literal(text);
        for (ServerPlayer p : players) {
            if (p == null) continue;
            p.displayClientMessage(c, true);
        }
    }
}
