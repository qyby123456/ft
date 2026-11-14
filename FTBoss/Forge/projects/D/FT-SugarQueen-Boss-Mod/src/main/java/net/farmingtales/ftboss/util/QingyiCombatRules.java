package net.farmingtales.ftboss.util;

/**
 * 青衣（庄园第 5 位）的战斗规则常量。
 *
 * <p>2026-09-21 按用户要求「青衣的伤害减半，三叉戟召唤速度减半」：
 * <ul>
 *   <li>{@link #OUTGOING_DAMAGE_MULT} = 0.25 —— 在原有 0.5 倍率基础上再次减半。
 *       挂在 {@code boss/ai/QingyiDamageHalved} 的 {@code LivingHurtEvent} 上，**不是**逐个去改技能常量：
 *       她的出伤路径有六七条（原版近战 / 庄园三招 / 铁魔法闪电 / 激流冲刺 / 投戟两招 / 三叉戟落雷 /
 *       三叉戟本体命中补伤 = 32 点魔法），其中近战与"原版三叉戟本体伤害"**根本不走**
 *       {@code manorSkillDamage}，逐个改必漏，而且以后再添一招又会漏。</li>
 *   <li>{@link #FALLING_TRIDENT_INTERVAL} 20 → 40 —— 坠戟召唤速度减半（每秒一把 → 每两秒一把）。
 *       注意这条**连带**把「每召唤一把 +1 层」的成长速度也减半了（层数是坠戟召唤时加的），
 *       也就是她滚雪球的速度同样慢一半，这是用户确认过的口径。</li>
 * </ul>
 */
public final class QingyiCombatRules {
    public static final int MELEE_INTERVAL = 10;
    public static final double MELEE_REACH_SQUARED = 3.5 * 3.5;
    public static final double CHASE_SPEED = 1.5;
    /** ★ 2026-09-21 用户要求「三叉戟召唤速度减半」：20 → 40 tick（坠戟召唤间隔，越大约慢）。 */
    public static final int FALLING_TRIDENT_INTERVAL = 40;
    public static final double FALLING_TRIDENT_RANGE = 32.0;
    public static final int MAX_TRIDENTS = 48;
    public static final int TRIDENT_LIFETIME = 600;
    public static final double TRIDENT_OWNER_RANGE = 64.0;

    /**
     * ★ 2026-09-21 用户要求「青衣的伤害减半」：她对玩家造成的**全部**伤害的统一倍率
     * （近战 + 庄园三招 + 铁魔法闪电 + 激流冲刺 + 投戟 + 三叉戟落雷/本体补伤）。
     * 生效点在 {@code boss/ai/QingyiDamageHalved#onHurt}（{@code LivingHurtEvent}）。
     */
    public static final double OUTGOING_DAMAGE_MULT = 0.25;
    public static final int TRIDENTS_PER_HEAL = 5;
    public static final float TRIDENT_HEAL_AMOUNT = 10.0F;

    private QingyiCombatRules() {}
}
