package net.farmingtales.ftboss.util;

/**
 * 祁崽 第八招「巨型铁魔法 · 天陨星裁」的全部数值 —— 2026-09-20 v189 按用户要求新增。
 *
 * <p>用户原话：「给祁崽做一个巨型铁魔法法术以及特效：飞上天空进入无法选中模式，
 * 蓄力5秒后，对半径48格的玩家造成500点魔法伤害」。
 *
 * <p>口径确认（开工前问过用户）：
 * <ul>
 *   <li><b>无法选中</b> = 蓄力期间**完全无法选中也无法伤害**（无敌 + 不被索敌 + 清掉现有目标），
 *       落地结算后立刻恢复；</li>
 *   <li><b>500 点</b> = **固定值**，不随普通/终结难度、也不吃 Boss 的攻击加成浮动
 *       （与「超能光束」那种"数字已含全局修正"的写法一致）。</li>
 * </ul>
 *
 * <p>数值集中在这里，是为了让"演出时序"与"平衡数值"分开：
 * {@code QizaiSkyNova} 只管状态机，改手感只动本文件。
 */
public final class QizaiSkyNovaRules {

    /** 升空高度（格）。够高才有"飞上天空"的观感，也让地面的近战够不着。 */
    public static final double RISE_HEIGHT = 24.0;
    /** 升空过程占多少 tick（这一段是"起飞"，还没开始蓄力读条）。 */
    public static final int RISE_TICKS = 20;
    /**
     * 蓄力读条 —— ★ 用户明确要求 **5 秒**（普通难度）。
     *
     * <p><b>2026-09-21 改口径</b>：原先这里**刻意**不走 {@code acceleratedTicks}（为了保住"5 秒"这句承诺）。
     * 用户随后要求「终结难度下祁崽法术施法速度翻倍，cd减半」并选择"补齐例外"，于是本招的升空与蓄力
     * **也过难度**：普通难度仍是 5 秒，终结难度（{@code ManorDifficulty.IMPOSSIBLE}）**2.5 秒**。
     * 生效点在 {@code QizaiSkyNova.begin}（{@code acceleratedTicks} 的结果存进 {@code riseTicks/chargeTicks}，
     * 状态机、cast 动作与 cue 都读那两个字段，客户端进度才对得上）。
     */
    public static final int CHARGE_TICKS = 5 * 20;
    /** 落地/收招的余韵 tick（此时已经结算完伤害，只是等演出播完再恢复 AI）。 */
    public static final int RECOVERY_TICKS = 20;
    /** 从升空到完全恢复的总时长（给调用方做冷却参考） */
    public static final int TOTAL_TICKS = RISE_TICKS + CHARGE_TICKS + RECOVERY_TICKS
            + (QizaiRules.RELEASES - 1) * QizaiRules.REPEAT_INTERVAL;

    /** 结算半径（格）—— ★ 用户要求 48。 */
    public static final double RADIUS = 48.0;
    /** 固定魔法伤害 —— ★ 用户要求 500，且不参与任何倍率。 */
    public static final float DAMAGE = 1000.0F;

    /** 垂直判定容差（格）：48 格是"水平半径"，垂直给足，免得站在坑里/塔上被漏掉。 */
    public static final double VERTICAL_TOLERANCE = 32.0;

    /** 预警圈随时间收缩：内圈从 RADIUS 收到这个下限（视觉上"越缩越紧"）。 */
    public static final double WARNING_INNER_MIN = 8.0;
    /** 冲击视觉的半径（比判定半径略小，看着更像"砸下来"而不是"平铺"）。 */
    public static final double IMPACT_VISUAL_RADIUS = 30.0;

    /** 出招循环里的位置 —— 放在 PATTERN 末尾，作为"打久了才会见到的大招"。 */
    public static final int SEQUENCE_POSITION = 99;

    private QizaiSkyNovaRules() {}
}
